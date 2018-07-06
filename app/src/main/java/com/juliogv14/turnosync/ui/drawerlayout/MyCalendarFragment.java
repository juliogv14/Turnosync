package com.juliogv14.turnosync.ui.drawerlayout;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentMycalendarBinding;
import com.juliogv14.turnosync.ui.mycalendar.MonthPageFragment;
import com.juliogv14.turnosync.ui.mycalendar.ScheduleWeekPageFragment;
import com.juliogv14.turnosync.ui.mycalendar.createshift.ConfirmChangesDialog;
import com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.WorkgroupSettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarFragment.java
 */

public class MyCalendarFragment extends Fragment implements ConfirmChangesDialog.ConfirmChangesListener {


    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Constants
    //TODO set number of months queried to settings
    private final int QUERY_MONTH_NUMBER = 12;
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String CURRENT_ADAPTER_POSITION = "currentPosition";
    private static final String CURRENT_PERSONAL_SCHEDULE = "currentPersonalSchedule";

    //Listener DrawerActivity
    private OnCalendarFragmentInteractionListener mListener;

    //Binding
    private FragmentMycalendarBinding mViewBinding;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;
    private ListenerRegistration mGroupUsersListener;

    //Firebase Auth
    private FirebaseUser mFirebaseUser;
    private UserWorkgroup mWorkgroup;

    //Calendar var
    private int mTotalWeeks;
    private int mCurrentPosition;
    private Calendar mInitMonth;

    //Runtime variables
    private boolean mPersonalSchedule = true;
    private AtomicBoolean mEditMode;
    private boolean mMadeChanges;
    private AtomicLong mWeeklyHours;

    //Data lists
    private ArrayList<ListenerRegistration> mUserShiftsListeners;
    private ArrayList<UserRef> mGroupUsersRef;
    private HashMap<String, ShiftType> mShiftTypes;
    private HashMap<String, ArrayList<Shift>> mShiftChanges;

    public static MyCalendarFragment newInstance(UserWorkgroup workgroup) {
        MyCalendarFragment f = new MyCalendarFragment();

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

        mGroupUsersRef = new ArrayList<>();
        Calendar cal = new GregorianCalendar();
        mTotalWeeks = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);

        //Init month date
        int mInitMonthOffset = (QUERY_MONTH_NUMBER / 2 - 1);
        mInitMonth = new GregorianCalendar();
        mInitMonth.add(Calendar.MONTH, -mInitMonthOffset);
        mInitMonth.set(Calendar.WEEK_OF_MONTH, 1);
        mInitMonth.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        mUserShiftsListeners = new ArrayList<>();
        mShiftTypes = new HashMap<>();
        mShiftChanges = new HashMap<>();
        mEditMode = new AtomicBoolean(false);
        mMadeChanges = false;
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentSwapped(R.string.fragment_mycalendar);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        queryWeeklyHours();
        queryWorkgroupUsers();
        queryShiftTypes();

        Log.d(TAG, "Start MyCalendarFragment");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PagerAdapter pagerAdapter = new MonthSlidePagerAdapter(getChildFragmentManager());
        mCurrentPosition = (QUERY_MONTH_NUMBER / 2 - 1);

        mViewBinding.viewPagerMonths.setAdapter(pagerAdapter);
        mViewBinding.viewPagerMonths.setCurrentItem(mCurrentPosition);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_ADAPTER_POSITION, mViewBinding.viewPagerMonths.getCurrentItem());
        outState.putBoolean(CURRENT_PERSONAL_SCHEDULE, mPersonalSchedule);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                if (!mWorkgroup.getRole().equals(UserRoles.MANAGER.toString()) || mPersonalSchedule) {
                    menuItem.setVisible(false);
                } else {
                    if(mEditMode.get()){
                        menuItem.setIcon(R.drawable.ic_save_black_24dp);
                    } else {
                        menuItem.setIcon(R.drawable.ic_mode_edit_black_24dp);
                    }
                }
            }
            Drawable icon = menuItem.getIcon();
            if (icon != null) {
                icon.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_mycalendar_edit:
                //loadTestData();

                if(mEditMode.get()){
                    int changesSize = mShiftChanges.get(getString(R.string.data_changes_added)).size()
                        + mShiftChanges.get(getString(R.string.data_changes_removed)).size()
                        + mShiftChanges.get(getString(R.string.data_changes_editedNew)).size()
                        + mShiftChanges.get(getString(R.string.data_changes_editedOld)).size();
                    if(changesSize > 0){
                        ConfirmChangesDialog dialog = ConfirmChangesDialog.newInstance(mShiftChanges, mShiftTypes, mGroupUsersRef);
                        dialog.show(getChildFragmentManager(),"confirmChanges");
                    } else {
                        mEditMode.set(!mEditMode.get());
                        ((AppCompatActivity) mListener).invalidateOptionsMenu();
                    }
                } else {
                    mEditMode.set(!mEditMode.get());
                    ((AppCompatActivity) mListener).invalidateOptionsMenu();
                }


                return true;
            case R.id.action_mycalendar_switch:
                mPersonalSchedule = !mPersonalSchedule;
                ((AppCompatActivity) mListener).invalidateOptionsMenu();

                mCurrentPosition = mViewBinding.viewPagerMonths.getCurrentItem();
                PagerAdapter pagerAdapter = changeAdapter();

                mViewBinding.viewPagerMonths.setAdapter(pagerAdapter);
                mViewBinding.viewPagerMonths.setCurrentItem(mCurrentPosition);


                return true;
            case R.id.action_mycalendar_settings:

                Intent workgroupSettingsIntent = new Intent((Context) mListener, WorkgroupSettingsActivity.class);
                workgroupSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                workgroupSettingsIntent.putExtra(getString(R.string.data_int_workgroup), mWorkgroup);
                workgroupSettingsIntent.putExtra(getString(R.string.data_int_hours), mWeeklyHours);
                //workgroupSettingsIntent.putExtra(getString(R.string.data_int_users), mGroupUsersRef);
                startActivity(workgroupSettingsIntent);
                return true;
            default:
                return false;
        }

    }

    private void applyChanges() {

        HashSet<String> changedUsers = new HashSet<>();

        //Added
        List<Shift> addedShifts =  mShiftChanges.get(getString(R.string.data_changes_added));
        for (Iterator<Shift> iterator = addedShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document();
            shift.setId(docRef.getId());
            docRef.set(shift);
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }

        //Removed
        List<Shift> removedShifts=  mShiftChanges.get(getString(R.string.data_changes_removed));
        for (Iterator<Shift> iterator = removedShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document(shift.getId());
            docRef.delete();
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }

        //Edited or user change
        List<Shift> editedNewShifts=  mShiftChanges.get(getString(R.string.data_changes_editedNew));
        for (Iterator<Shift> iterator = editedNewShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document();
            shift.setId(docRef.getId());
            docRef.set(shift);
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }
        List<Shift> editedOldShifts=  mShiftChanges.get(getString(R.string.data_changes_editedOld));
        for (Iterator<Shift> iterator = editedOldShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document(shift.getId());
            docRef.delete();
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }

        //Set schedule changed for users
        HashMap<String, String> notifData = new HashMap<>();
        notifData.put(getString(R.string.data_key_displayname), mWorkgroup.getDisplayName());
        for (String userId : changedUsers) {
            CollectionReference notifRef = mFirebaseFirestore.collection(getString(R.string.data_ref_messaging)).document(userId)
                    .collection(getString(R.string.data_ref_workgroups));
            notifRef.document(mWorkgroup.getWorkgroupId()).set(notifData);
        }
    }

    private void loadTestData() {

        Calendar calendar;
        calendar = new GregorianCalendar(2018, 4, 1);
        Shift shift1 = new Shift(mFirebaseUser.getUid(), "M", calendar.getTime(), "", "");
        calendar = new GregorianCalendar(2018, 4, 6);
        Shift shift2 = new Shift(mFirebaseUser.getUid(), "M", calendar.getTime(), "", "");
        calendar = new GregorianCalendar(2018, 4, 29);
        Shift shift3 = new Shift(mFirebaseUser.getUid(), "M", calendar.getTime(), "", "");

        CollectionReference shiftsColl = mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shifts));

        shiftsColl.document("testshift1").set(shift1);
        shiftsColl.document("testshift2").set(shift2);
        shiftsColl.document("testshift3").set(shift3);

        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_users));
        HashMap<String, String> userdata = new HashMap<>();
        userdata.put("uid", mFirebaseUser.getUid());
        workgroupsUsersColl.document(mFirebaseUser.getUid()).set(userdata);

        /*CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
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

        /*CollectionReference usersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users));
        Map<String,String> data = new HashMap<>();
        data.put("displayName", "test1");
        data.put("info", "");
        data.put("role", "MANAGER");
        data.put("workgroupId", "VOBMWlT3iP0t9x5fq37X");
        usersColl.document(mFirebaseUser.getUid()).collection(getString(R.string.data_ref_workgroups)).document("VOBMWlT3iP0t9x5fq37X").set(data);*/

    }

    private PagerAdapter changeAdapter() {
        PagerAdapter pagerAdapter;
        mEditMode.set(false);
        if (mPersonalSchedule) {

            Calendar calend = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
            calend.add(Calendar.WEEK_OF_YEAR, mCurrentPosition);
            calend.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            mCurrentPosition = calend.get(Calendar.MONTH);

            pagerAdapter = new MonthSlidePagerAdapter(getChildFragmentManager());
        } else {

            Calendar calinit = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
            Calendar calend = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));

            calend.add(Calendar.MONTH, mCurrentPosition);
            calend.set(Calendar.WEEK_OF_MONTH, 1);
            calend.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

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

            //Reset changes map
            mShiftChanges.put(getString(R.string.data_changes_added), new ArrayList<Shift>());
            mShiftChanges.put(getString(R.string.data_changes_removed), new ArrayList<Shift>());
            mShiftChanges.put(getString(R.string.data_changes_editedNew), new ArrayList<Shift>());
            mShiftChanges.put(getString(R.string.data_changes_editedOld), new ArrayList<Shift>());
            mMadeChanges = false;
            pagerAdapter = new WeekSlidePagerAdapter(getChildFragmentManager());
        }
        return pagerAdapter;
    }

    private void queryWorkgroupUsers() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_users));

        mGroupUsersListener = workgroupsUsersColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = docChange.getDocument();
                    if (doc.exists()) {
                        UserRef userData = doc.toObject(UserRef.class);
                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                mGroupUsersRef.add(userData);
                                break;
                            case MODIFIED:
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Modified, same position
                                    mGroupUsersRef.set(docChange.getOldIndex(), userData);
                                } else {
                                    //Modified, differnt position
                                    mGroupUsersRef.remove(docChange.getOldIndex());
                                    mGroupUsersRef.add(docChange.getNewIndex(), userData);
                                }
                                break;
                            case REMOVED:
                                //Removed
                                mGroupUsersRef.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
            }
        });

    }

    private void queryShiftTypes() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shiftTypes));

        workgroupsUsersColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if (doc != null && doc.exists()) {
                            ShiftType type = doc.toObject(ShiftType.class);
                            mShiftTypes.put(type.getId(), type);
                        }
                    }
                } else {
                    if (task.getException() != null) {
                        Log.e(TAG, task.getException().getMessage());
                    }
                }
            }
        });
    }

    private void queryWeeklyHours(){
        mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String, Object> data = task.getResult().getData();
                    mWeeklyHours = new AtomicLong((long)data.get(getString(R.string.data_key_weeklyhours)));
                }
            }
        });
    }

    @Override
    public void onConfirmChanges() {
        applyChanges();
        mMadeChanges = true;
        mEditMode.set(false);
        ((AppCompatActivity) mListener).invalidateOptionsMenu();

    }

    public interface OnCalendarFragmentInteractionListener extends OnFragmentInteractionListener {

    }

    private class MonthSlidePagerAdapter extends FragmentStatePagerAdapter {

        MonthSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Calendar cal = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
            cal.add(Calendar.MONTH, position);
            cal.set(Calendar.DAY_OF_MONTH, 1);

            final ArrayList<Shift> shiftList = new ArrayList<>();

            MonthPageFragment pageFragment = MonthPageFragment.newInstance(mWorkgroup, cal.getTime(), shiftList, mShiftTypes);
            queryShiftData(pageFragment, cal.getTime(), shiftList);

            return pageFragment;
        }

        @Override
        public int getCount() {
            return QUERY_MONTH_NUMBER;
        }

        private void queryShiftData(final MonthPageFragment pageFragment, Date date, final ArrayList<Shift> shiftList) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if (mFirebaseUser != null) {
                CollectionReference shiftsColl = mFirebaseFirestore
                        .collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                        .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                        .collection(getString(R.string.data_ref_shifts));

                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                Date firstDay = calendar.getTime();

                calendar.add(Calendar.MONTH, 1);

                calendar.set(Calendar.WEEK_OF_MONTH, calendar.getActualMaximum(Calendar.WEEK_OF_MONTH));
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                Date lastDay = calendar.getTime();
                String dateKey = getString(R.string.data_key_date);
                shiftsColl.whereGreaterThanOrEqualTo(dateKey, firstDay).whereLessThanOrEqualTo(dateKey, lastDay).orderBy(dateKey, Query.Direction.ASCENDING).get()
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

        ScheduleWeekPageFragment[] fragmentsRef = new ScheduleWeekPageFragment[mTotalWeeks];

        WeekSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Calendar cal = new GregorianCalendar(mInitMonth.get(Calendar.YEAR), mInitMonth.get(Calendar.MONTH), mInitMonth.get(Calendar.DATE));
            cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) + position);

            final LinkedHashMap<String, ArrayList<Shift>> shiftListMap = new LinkedHashMap<>();

            if(fragmentsRef[position] == null){
                fragmentsRef[position] = ScheduleWeekPageFragment.newInstance(cal.getTime(), mGroupUsersRef, shiftListMap, mShiftTypes, mShiftChanges, mEditMode, mWeeklyHours);
                queryScheduleData(fragmentsRef[position], cal.getTime(), shiftListMap);
            }

            return fragmentsRef[position];
        }

        @Override
        public int getCount() {
            return mTotalWeeks;
        }

        private void queryScheduleData(final ScheduleWeekPageFragment pageFragment, Date date, Map<String, ArrayList<Shift>> shiftListMap) {

            for (UserRef userUid : mGroupUsersRef) {
                final ArrayList<Shift> userShifts = new ArrayList<>();
                final String uid = userUid.getUid();
                shiftListMap.put(uid, userShifts);

                CollectionReference userShiftsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(userUid.getUid())
                        .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                        .collection(getString(R.string.data_ref_shifts));

                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date);

                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                Date firstDay = calendar.getTime();

                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                Date lastDay = calendar.getTime();

                String dateKey = getString(R.string.data_key_date);
                mMadeChanges = false;
                ListenerRegistration userShiftListener =
                        userShiftsColl.whereGreaterThanOrEqualTo(dateKey, firstDay).whereLessThanOrEqualTo(dateKey, lastDay)
                                .orderBy(dateKey, Query.Direction.ASCENDING)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                                        if(e != null || mMadeChanges){
                                            return;
                                        }

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

                                            }
                                        }
                                        pageFragment.notifyGridDataSetChanged();
                                    }
                                });
                mUserShiftsListeners.add(userShiftListener);
            }
        }
    }

}
