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
import android.util.DisplayMetrics;
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
import com.juliogv14.turnosync.data.User;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentMycalendarBinding;
import com.juliogv14.turnosync.ui.mycalendar.MonthPageFragment;
import com.juliogv14.turnosync.ui.mycalendar.ScheduleWeekPageFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarFragment.java
 */

public class MyCalendarFragment extends Fragment {



    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Constants
    private final int QUERY_MONTH_NUMBER = 12;
    FirebaseUser mFirebaseUser;

    //Listener DrawerActivity
    private OnCalendarFragmentInteractionListener mListener;

    //Binding
    private FragmentMycalendarBinding mViewBinding;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;
    private ListenerRegistration mGroupUsersListener;
    private ArrayList<Map<String,Object>> mGroupUsersUids;

    //Firebase Auth
    private FirebaseAuth mFirebaseAuth;

    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String CURRENT_ADAPTER_POSITION = "currentPosition";
    private UserWorkgroup mWorkgroup;
    private boolean mPersonalSchedule = true;
    private int mCurrentYear;
    private int mCurrentMonth;
    private int mCurrentPosition;

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
        mCurrentYear = cal.get(Calendar.YEAR);
        mCurrentMonth = cal.get(Calendar.MONTH);

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
        if(savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(CURRENT_ADAPTER_POSITION);
        } else {
            mCurrentPosition = 5;
        }

        PagerAdapter pagerAdapter;
        if(mPersonalSchedule){
            pagerAdapter = new MonthSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), mCurrentYear, mCurrentMonth);
        } else {
            //TODO schedule adapter
            pagerAdapter = new WeekSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), mCurrentYear, mCurrentMonth);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mycalendar, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if(menuItem.getItemId() == R.id.action_mycalendar_switch){
                if(mPersonalSchedule){
                    menuItem.setIcon(R.drawable.ic_schedule_black_24dp);
                } else {
                    menuItem.setIcon(R.drawable.ic_mycalendar_black_24dp);
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

                loadTestData();

                return true;
            case R.id.action_mycalendar_switch:
                mPersonalSchedule = !mPersonalSchedule;
                mCurrentPosition = mViewBinding.viewPagerMonths.getCurrentItem();
                ((AppCompatActivity) mListener).invalidateOptionsMenu();

                PagerAdapter pagerAdapter;
                if(mPersonalSchedule){
                    pagerAdapter = new MonthSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), mCurrentYear, mCurrentMonth);
                } else {
                    pagerAdapter = new WeekSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), mCurrentYear, mCurrentMonth);
                }

                mViewBinding.viewPagerMonths.setAdapter(pagerAdapter);
                mViewBinding.viewPagerMonths.setCurrentItem(mCurrentPosition);


                return true;
            case R.id.action_mycalendar_settings:

                Toast.makeText((Context) mListener, "Schedule", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }

    }

    private void loadTestData() {

        Shift shift1 = new Shift("M",mFirebaseUser.getUid(),2018,4,5, "", "");
        Shift shift2 = new Shift("M",mFirebaseUser.getUid(),2018,4,12, "", "");
        Shift shift3 = new Shift("M",mFirebaseUser.getUid(),2018,4,30, "", "");

        CollectionReference shiftsColl = mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                .collection(getString(R.string.data_ref_shifts));

        shiftsColl.document("testshift1").set(shift1);
        shiftsColl.document("testshift2").set(shift2);
        shiftsColl.document("testshift3").set(shift3);



        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                .collection(getString(R.string.data_ref_users));

        User[] testUsers = new User[3];

        testUsers[0] = new User("testuser1", "testuser1@email.com", "testuser1");
        testUsers[1] = new User("testuser2", "testuser2@email.com", "testuser2");
        testUsers[2] = new User("testuser3", "testuser3@email.com", "testuser3");

        for (int i = 0; i < testUsers.length; i++) {
            workgroupsUsersColl.document("testuser1").set(testUsers[i]);

            shiftsColl = mFirebaseFirestore
                    .collection(getString(R.string.data_ref_users)).document(testUsers[i].getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                    .collection(getString(R.string.data_ref_shifts));

            Shift[] testShifts = new Shift[3];

            testShifts[0] = new Shift("M",testUsers[i].getUid(),2018,4,i*2, "", "");
            testShifts[1] = new Shift("M",testUsers[i].getUid(),2018,4,i*3, "", "");
            testShifts[2] = new Shift("M",testUsers[i].getUid(),2018,4,i*4, "", "");

            for (int j = 0; j < testShifts.length; j++) {
                shiftsColl.document("testusershift"+j*i).set(testShifts[j]);
            }
        }








    }

    private void queryWorkgroupUsers() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                .collection(getString(R.string.data_ref_users));

        mGroupUsersListener = workgroupsUsersColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = docChange.getDocument();
                    if (doc.exists()) {
                        Map<String,Object> userData = doc.getData();

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

    private void queryShiftData(final MonthPageFragment pageFragment, final int year, final int month, final ArrayList<Shift> shiftList) {

        if (mFirebaseUser != null) {
            CollectionReference shiftsColl = mFirebaseFirestore
                    .collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
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

    private class MonthSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int year, month;

        MonthSlidePagerAdapter(FragmentManager fm, int year, int month) {
            super(fm);
            this.year = year;
            this.month = month;
        }

        @Override
        public Fragment getItem(int position) {

            int offMonth = month - (QUERY_MONTH_NUMBER / 2 - 1) + position;
            int offYear = year;
            if (offMonth < 0) {
                offYear = offYear - 1;
                offMonth = QUERY_MONTH_NUMBER - Math.abs(offMonth);
            }
            final ArrayList<Shift> shiftList = new ArrayList<>();


            MonthPageFragment pageFragment = MonthPageFragment.newInstance(mWorkgroup, offYear, offMonth, shiftList);
            queryShiftData(pageFragment, offYear, offMonth, shiftList);

            return pageFragment;
        }

        @Override
        public int getCount() {
            return QUERY_MONTH_NUMBER;
        }

    }

    private class WeekSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int year, month;

        WeekSlidePagerAdapter(FragmentManager fm, int year, int month) {
            super(fm);
            this.year = year;
            this.month = month;
        }

        @Override
        public Fragment getItem(int position) {
            int offMonth = month - (QUERY_MONTH_NUMBER / 2 - 1) + position;
            int offYear = year;
            if (offMonth < 0) {
                offYear = offYear - 1;
                offMonth = QUERY_MONTH_NUMBER - Math.abs(offMonth);
            }

            final ArrayList<Shift> shiftList = new ArrayList<>();
            
            ScheduleWeekPageFragment pageFragment = ScheduleWeekPageFragment.newInstance(mWorkgroup, mGroupUsersUids, offYear , offMonth);
            return pageFragment;
        }

        @Override
        public int getCount() {
            return QUERY_MONTH_NUMBER;
        }

    }

    public interface OnCalendarFragmentInteractionListener extends OnFragmentInteractionListener {

    }

}
