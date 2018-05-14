package com.juliogv14.turnosync.ui.drawerlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentMycalendarBinding;
import com.juliogv14.turnosync.ui.mycalendar.MonthPageFragment;
import com.juliogv14.turnosync.ui.mycalendar.ScheduleWeekPageFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarFragment.java
 */

public class MyCalendarFragment extends Fragment {


    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String CURRENT_ADAPTER_POSITION = "currentPosition";
    //Log TAG
    private final String TAG = this.getClass().getSimpleName();
    //Constants
    //TODO set number of months queried to settings
    private final int QUERY_MONTH_NUMBER = 12;
    FirebaseUser mFirebaseUser;
    //Listener DrawerActivity
    private OnCalendarFragmentInteractionListener mListener;
    //Binding
    private FragmentMycalendarBinding mViewBinding;
    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;
    private ListenerRegistration mGroupUsersListener;
    private ArrayList<ListenerRegistration> mUserShiftsListeners;
    private ArrayList<Map<String, Object>> mGroupUsersUids;
    //Firebase Auth
    private FirebaseAuth mFirebaseAuth;
    private UserWorkgroup mWorkgroup;
    private boolean mPersonalSchedule = true;

    //Calendar var
    private int mActualYear;
    private int mActualMonth;
    private int mActualWeek;
    private int mTotalWeeks;
    private int mCurrentPosition;
    private Calendar mInitMonth;
    private int mInitMonthOffset;


    public static MyCalendarFragment newInstance(UserWorkgroup workgroup) {
        MyCalendarFragment f = new MyCalendarFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCalendarFragmentInteractionListener) {
            mListener = (OnCalendarFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalendarFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(CURRENT_WORKGROUP_KEY);
        }
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentMycalendarBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        mGroupUsersUids = new ArrayList<>();
        Calendar cal = new GregorianCalendar();
        mActualYear = cal.get(Calendar.YEAR);
        mActualMonth = cal.get(Calendar.MONTH);
        mActualWeek = cal.get(Calendar.WEEK_OF_YEAR);
        mTotalWeeks = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);

        //Init month date
        mInitMonthOffset = (QUERY_MONTH_NUMBER / 2 - 1);
        mInitMonth = new GregorianCalendar();
        mInitMonth.add(Calendar.MONTH, -mInitMonthOffset);
        mInitMonth.set(Calendar.WEEK_OF_MONTH, 1);

        mUserShiftsListeners = new ArrayList<>();
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentCreated(R.string.fragment_mycalendar);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        queryWorkgroupUsers();

        Log.d(TAG, "Start MyCalendarFragment");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PagerAdapter pagerAdapter;
        if (mPersonalSchedule) {
            if (savedInstanceState != null) {
                mCurrentPosition = savedInstanceState.getInt(CURRENT_ADAPTER_POSITION);
            } else {
                mCurrentPosition = (QUERY_MONTH_NUMBER / 2 - 1);
            }
            pagerAdapter = new MonthSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), mActualYear, mActualMonth);
        } else {
            if (savedInstanceState != null) {
                mCurrentPosition = savedInstanceState.getInt(CURRENT_ADAPTER_POSITION);
            } else {
                mCurrentPosition = (mTotalWeeks / 2);
            }
            pagerAdapter = new WeekSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager());
        }


        mViewBinding.viewPagerMonths.setAdapter(pagerAdapter);
        mViewBinding.viewPagerMonths.setCurrentItem(mCurrentPosition);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putInt(CURRENT_ADAPTER_POSITION, mViewBinding.viewPagerMonths.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGroupUsersListener != null) {
            mGroupUsersListener.remove();
        }
        for (Iterator<ListenerRegistration> iterator = mUserShiftsListeners.iterator(); iterator.hasNext(); ) {
            ListenerRegistration userShiftListener = iterator.next();
            if (userShiftListener != null) {
                userShiftListener.remove();
            }
            iterator.remove();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mycalendar, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_mycalendar_switch) {
                if (mPersonalSchedule) {
                    menuItem.setIcon(R.drawable.ic_schedule_black_24dp);
                } else {
                    menuItem.setIcon(R.drawable.ic_mycalendar_black_24dp);
                }
            } else if (itemId == R.id.action_mycalendar_edit) {
                if(!mWorkgroup.getRole().equals(UserRoles.MANAGER.toString()) ){
                    menuItem.setVisible(false);
                }
            }
            Drawable icon = menuItem.getIcon();
            if (icon != null) {
                icon.mutate();
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_mycalendar_edit:



                return true;
            case R.id.action_mycalendar_switch:
                mPersonalSchedule = !mPersonalSchedule;
                mCurrentPosition = mViewBinding.viewPagerMonths.getCurrentItem();
                ((AppCompatActivity) mListener).invalidateOptionsMenu();

                PagerAdapter pagerAdapter;
                if (mPersonalSchedule) {

                    Calendar calend = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
                    calend.add(Calendar.WEEK_OF_YEAR, mCurrentPosition);
                    calend.get(Calendar.WEEK_OF_YEAR);
                    mCurrentPosition = calend.get(Calendar.MONTH) + 1;

                    pagerAdapter = new MonthSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), mActualYear, mActualMonth);
                } else {

                    Calendar calinit = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
                    Calendar calend = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));

                    calend.add(Calendar.MONTH, mCurrentPosition);

                    calinit.get(Calendar.WEEK_OF_YEAR);

                    int startWeek = calinit.get(Calendar.WEEK_OF_YEAR);
                    int endWeek = calend.get(Calendar.WEEK_OF_YEAR);

                    int diff = calend.get(Calendar.YEAR) - mInitMonth.get(Calendar.YEAR);

                    int deltaYears = 0;
                    for (int i = 0; i < diff; i++) {
                        deltaYears += calinit.getActualMaximum(Calendar.WEEK_OF_YEAR);
                        calinit.add(Calendar.YEAR, 1);
                    }
                    diff = (endWeek + deltaYears) - startWeek;

                    mCurrentPosition = diff;          //Position 0 + firstweek

                    pagerAdapter = new WeekSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager());
                }

                mViewBinding.viewPagerMonths.setAdapter(pagerAdapter);
                mViewBinding.viewPagerMonths.setCurrentItem(mCurrentPosition);


                return true;
            case R.id.action_mycalendar_settings:
                loadTestData();
                Toast.makeText((Context) mListener, "Test data", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }

    }

    private void loadTestData() {
/*
        Shift shift1 = new Shift("M", mFirebaseUser.getUid(), 2018, 4, 5, "", "");
        Shift shift2 = new Shift("M", mFirebaseUser.getUid(), 2018, 4, 12, "", "");
        Shift shift3 = new Shift("M", mFirebaseUser.getUid(), 2018, 4, 30, "", "");

        CollectionReference shiftsColl = mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shifts));

        shiftsColl.document("testshift1").set(shift1);
        shiftsColl.document("testshift2").set(shift2);
        shiftsColl.document("testshift3").set(shift3);


        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_users));
        CollectionReference usersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users));

        List<HashMap<String, String>> testUsers = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            User user = new User("testuser" + i, "testuser" + i + "@test.com", "testuser" + i);
            HashMap<String, String> userdata = new HashMap<>();
            userdata.put("uid", user.getUid());

            usersColl.document(user.getUid()).set(user);
            workgroupsUsersColl.document(user.getUid()).set(userdata);

            shiftsColl = mFirebaseFirestore
                    .collection(getString(R.string.data_ref_users)).document(user.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts));

            Shift[] testShifts = new Shift[3];

            testShifts[0] = new Shift("M", testUsers.get(i).get("uid"), 2018, 4, i * 2, "", "");
            testShifts[1] = new Shift("M", testUsers.get(i).get("uid"), 2018, 4, i * 3, "", "");
            testShifts[2] = new Shift("M", testUsers.get(i).get("uid"), 2018, 4, i * 4, "", "");

            for (int j = 0; j < testShifts.length; j++) {
                shiftsColl.document("testusershift" + j * i).set(testShifts[j]);
            }
        }*/

        CollectionReference usersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users));
        Map<String,String> data = new HashMap<>();
        data.put("displayName", "test1");
        data.put("info", "");
        data.put("role", "USER");
        data.put("workgroupId", "VOBMWlT3iP0t9x5fq37X");
        usersColl.document(mFirebaseUser.getUid()).collection(getString(R.string.data_ref_workgroups)).document("VOBMWlT3iP0t9x5fq37X").set(data);

    }

    private void queryWorkgroupUsers() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_users));

        mGroupUsersListener = workgroupsUsersColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = docChange.getDocument();
                    if (doc.exists()) {
                        Map<String, Object> userData = doc.getData();

                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                mGroupUsersUids.add(userData);
                                break;
                            case MODIFIED:
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Modified, same position
                                    mGroupUsersUids.set(docChange.getOldIndex(), userData);
                                } else {
                                    //Modified, differnt position
                                    mGroupUsersUids.remove(docChange.getOldIndex());
                                    mGroupUsersUids.add(docChange.getNewIndex(), userData);
                                }
                                break;
                            case REMOVED:
                                //Removed
                                mGroupUsersUids.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
            }
        });

    }

    public interface OnCalendarFragmentInteractionListener extends OnFragmentInteractionListener {

    }

    private class MonthSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int year, month;

        MonthSlidePagerAdapter(FragmentManager fm, int year, int month) {
            super(fm);
            this.year = year;
            this.month = month;
        }

        @Override
        public Fragment getItem(int position) {

            Calendar cal = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
            cal.add(Calendar.MONTH, position);
            int offMonth = cal.get(Calendar.MONTH);
            int offYear = cal.get(Calendar.YEAR);
            final ArrayList<Shift> shiftList = new ArrayList<>();

            MonthPageFragment pageFragment = MonthPageFragment.newInstance(mWorkgroup, offYear, offMonth, shiftList);
            queryShiftData(pageFragment, offYear, offMonth, shiftList);

            return pageFragment;
        }

        @Override
        public int getCount() {
            return QUERY_MONTH_NUMBER;
        }

        private void queryShiftData(final MonthPageFragment pageFragment, final int year, final int month, final ArrayList<Shift> shiftList) {

            if (mFirebaseUser != null) {
                CollectionReference shiftsColl = mFirebaseFirestore
                        .collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                        .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                        .collection(getString(R.string.data_ref_shifts));

                shiftsColl.whereEqualTo("year", year).whereEqualTo("month", month + 1).orderBy("day", Query.Direction.ASCENDING).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {

                                    for (DocumentSnapshot doc : task.getResult()) {
                                        if (doc != null && doc.exists()) {
                                            shiftList.add(doc.toObject(Shift.class));
                                        }
                                    }
                                    pageFragment.notifyGridDataSetChanged();
                                } else {
                                    if (task.getException() != null) {
                                        Log.e(TAG, task.getException().getMessage());
                                    }
                                }
                            }
                        });
            }
        }
    }

    private class WeekSlidePagerAdapter extends FragmentStatePagerAdapter {

        WeekSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Calendar cal = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
            cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) + position);
            int offYear = cal.get(Calendar.YEAR);
            int offMonth = cal.get(Calendar.MONTH);
            final Map<String, ArrayList<Shift>> shiftListMap = new HashMap<>();

            ScheduleWeekPageFragment pageFragment = ScheduleWeekPageFragment.newInstance(mWorkgroup, mGroupUsersUids, cal);
            //TODO query shifts
            //queryScheduleData(pageFragment, cal, shiftListMap);
            return pageFragment;
        }

        @Override
        public int getCount() {
            return mTotalWeeks;
        }

        private void queryScheduleData(final ScheduleWeekPageFragment pageFragment, Calendar calendar, Map<String, ArrayList<Shift>> shiftListMap) {

            for (Map<String, Object> userUid : mGroupUsersUids) {
                final ArrayList<Shift> userShifts = new ArrayList<>();
                shiftListMap.put((String) userUid.get(getString(R.string.data_key_uid)), userShifts);

                CollectionReference userShiftsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document((String) userUid.get("uid"))
                        .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                        .collection(getString(R.string.data_ref_shifts));

                calendar.set(Calendar.DATE, calendar.getFirstDayOfWeek());
                int startYear = calendar.get(Calendar.YEAR);
                int startMonth = calendar.get(Calendar.MONTH);
                int startDay = calendar.get(Calendar.DATE);

                calendar.add(Calendar.DATE, 6);
                int endYear = calendar.get(Calendar.YEAR);
                int endMonth = calendar.get(Calendar.MONTH);
                int endDay = calendar.get(Calendar.DATE);

                //TODO keys to strings
                //ListenerRegistration userShiftListener =
                userShiftsColl.whereGreaterThanOrEqualTo("year", startYear).whereLessThanOrEqualTo("year", endYear)
                        .whereGreaterThanOrEqualTo("month", startMonth + 1).whereLessThanOrEqualTo("month", endMonth + 1)
                        .whereGreaterThanOrEqualTo("day", startDay).whereLessThanOrEqualTo("day", endDay)
                        .orderBy("day", Query.Direction.ASCENDING)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Shift> list = task.getResult().toObjects(Shift.class);
                    }
                });
                        /*.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    DocumentSnapshot doc = docChange.getDocument();
                                    if (doc.exists()) {
                                        Shift shift = doc.toObject(Shift.class);

                                        switch (docChange.getType()) {
                                            case ADDED:
                                                //Added
                                                userShifts.add(shift);
                                                break;
                                            case MODIFIED:
                                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                                    //Modified, same position
                                                    userShifts.set(docChange.getOldIndex(), shift);
                                                } else {
                                                    //Modified, differnt position
                                                    userShifts.remove(docChange.getOldIndex());
                                                    userShifts.add(docChange.getNewIndex(), shift);
                                                }
                                                break;
                                            case REMOVED:
                                                //Removed
                                                userShifts.remove(docChange.getOldIndex());
                                                break;
                                        }
                                        pageFragment.notifyGridDataSetChanged();
                                    }
                                }
                            }
                        });*/
                //mUserShiftsListeners.add(userShiftListener);
            }
        }
    }

}
