package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.PageWeekBinding;
import com.juliogv14.turnosync.ui.mycalendar.createshift.CreateShiftDialog;
import com.juliogv14.turnosync.ui.mycalendar.createshift.EditShiftDialog;
import com.juliogv14.turnosync.utils.CalendarUtils;

import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Julio on 11/05/2018.
 * ScheduleWeekPageFragment
 */

public class ScheduleWeekPageFragment extends Fragment implements CreateShiftDialog.CreateShiftListener, EditShiftDialog.EditShiftListener {

    //Keys
    private static final String CURRENT_WEEK_DATE_KEY = "currentCalendar";
    private static final String WORKGROUP_USERS_KEY = "workgroupUsers";
    private static final String USERS_SHIFT_MAP_KEY = "userShiftMap";
    private static final String SHIFT_TYPES_MAP_KEY = "shiftTypesMap";
    private static final String SHIFT_CHANGES_MAP_KEY = "shiftChangesMap";
    private static final String EDIT_MODE_KEY = "editMode";
    private static final String WEEKLY_HOURS_KEY = "weeklyHours";


    private final String TAG = this.getClass().getSimpleName();
    //Binding
    protected PageWeekBinding mViewBinding;

    //Context and listener
    private Context mContext;

    //Variables
    private Date mWeekDate;
    private AtomicBoolean mEditMode;
    private AtomicLong mWeeklyHours;

    //Data lists
    private ArrayList<UserRef> mWorkgroupUsers;
    private Map<String, ArrayList<Shift>> mUsersShiftsMap;
    private Map<String, ShiftType> mShiftTypesMap;
    private Map<String, ArrayList<Shift>> mShiftChanges;
    private Map<String, Period> mUsersHourCount;

    //GridAdapter
    private BaseAdapter mGridAdapter;

    public static ScheduleWeekPageFragment newInstance(Date weekDate,
                                                       ArrayList<UserRef> workgroupUsers,
                                                       LinkedHashMap<String, ArrayList<Shift>> userShifts,
                                                       HashMap<String, ShiftType> shiftTypes,
                                                       HashMap<String, ArrayList<Shift>> shiftChanges,
                                                       AtomicBoolean editMode,
                                                       AtomicLong weeklyHours) {

        ScheduleWeekPageFragment f = new ScheduleWeekPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong(CURRENT_WEEK_DATE_KEY, weekDate.getTime());
        args.putParcelableArrayList(WORKGROUP_USERS_KEY, workgroupUsers);
        args.putSerializable(USERS_SHIFT_MAP_KEY, userShifts);
        args.putSerializable(SHIFT_TYPES_MAP_KEY, shiftTypes);
        args.putSerializable(SHIFT_CHANGES_MAP_KEY, shiftChanges);
        args.putSerializable(EDIT_MODE_KEY, editMode);
        args.putSerializable(WEEKLY_HOURS_KEY, weeklyHours);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setRetainInstance(true);
        if (args != null) {
            mWorkgroupUsers = args.getParcelableArrayList(WORKGROUP_USERS_KEY);
            mUsersShiftsMap = (Map<String, ArrayList<Shift>>) args.getSerializable(USERS_SHIFT_MAP_KEY);
            mWeekDate = new Date(args.getLong(CURRENT_WEEK_DATE_KEY));
            mShiftTypesMap = (Map<String, ShiftType>) args.getSerializable(SHIFT_TYPES_MAP_KEY);
            mShiftChanges = (Map<String, ArrayList<Shift>>) args.getSerializable(SHIFT_CHANGES_MAP_KEY);
            mEditMode = (AtomicBoolean) args.getSerializable(EDIT_MODE_KEY);
            mWeeklyHours = (AtomicLong) args.getSerializable(WEEKLY_HOURS_KEY);
        }
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = PageWeekBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(mWeekDate);

        //Week label
        int firstDay = calendar.get(Calendar.DAY_OF_MONTH);
        int firstMonth = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int lastDay = calendar.get(Calendar.DAY_OF_MONTH);
        int lastMonth = calendar.get(Calendar.MONTH);

        String week = "" + firstDay + "/" + CalendarUtils.getMonthString(mContext, firstMonth) + "-"
                + lastDay + "/" + CalendarUtils.getMonthString(mContext, lastMonth);
        mViewBinding.textViewWeek.setText(week);

        //Adapter
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mGridAdapter = new WeekAdapter(mContext, metrics, mWeekDate, mWorkgroupUsers, mUsersShiftsMap, mShiftTypesMap);
        mViewBinding.gridViewWeek.setAdapter(mGridAdapter);

        //createShift onclick
        mViewBinding.gridViewWeek.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int day = position % 8;
                if(position > 7 && day > 0 && mEditMode.get()){
                    Calendar cal = new GregorianCalendar();
                    cal.setTime(mWeekDate);
                    cal.add(Calendar.DAY_OF_MONTH, + day-1);
                    Date date = cal.getTime();

                    int row = position / 8;
                    UserRef userRef = mWorkgroupUsers.get(row-1);

                    //Check if there is a shift there
                    Shift shiftSelected = null;
                    for (Shift shift : mUsersShiftsMap.get(userRef.getUid())) {
                        if (shift.getDate().getTime() == date.getTime()){
                            shiftSelected = shift;
                            break;
                        }
                    }
                    if(!mShiftTypesMap.isEmpty() && mWeeklyHours != null){
                        if(shiftSelected != null){
                            EditShiftDialog dialog = EditShiftDialog.newInstance(date, userRef, mShiftTypesMap, mWorkgroupUsers, shiftSelected);
                            dialog.show(getChildFragmentManager(), "esd");
                        } else {
                            recalculateHours();
                            CreateShiftDialog dialog = CreateShiftDialog.newInstance(date, userRef, mShiftTypesMap, mUsersHourCount.get(userRef.getUid()).toStandardDuration().getMillis(), mWeeklyHours.get());
                            dialog.show(getChildFragmentManager(), "csd");
                        }
                    } else {
                        // TODO: 04/07/2018 set to strings
                        Toast.makeText(mContext, "You need to create a shift type before setting shifts", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public void notifyGridDataSetChanged() {
        if (mContext != null) {
            ((SupportActivity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGridAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void recalculateHours(){
        mUsersHourCount = new HashMap<>();
        for (Map.Entry<String, ArrayList<Shift>> entry : mUsersShiftsMap.entrySet()) {
            Period weekHours = new Period();
            for (Shift shift : entry.getValue()) {
                ShiftType type = mShiftTypesMap.get(shift.getType());
                weekHours = weekHours.plus(type.getJodaPeriod());
            }
            mUsersHourCount.put(entry.getKey(), weekHours);

        }
    }

    @Override
    public void onCreateShiftCreate(final ArrayList<Shift> newShifts) {
        //TODO: consider other weeks
        /*Date dateFrom = newShifts.get(0).getDate();
        Date dateTo = newShifts.get(newShifts.size()-1).getDate();

        String dateKey = getString(R.string.data_key_date);
        CollectionReference shiftsColl = FirebaseFirestore.getInstance().collection(getString(R.string.data_ref_users)).document(newShifts.get(0).getUserId())
                .collection(getString(R.string.data_ref_workgroups)).document("udDHaY6rNMg0bi8koxxl").collection(getString(R.string.data_ref_shifts));
        shiftsColl.whereGreaterThanOrEqualTo(dateKey, dateFrom).whereLessThanOrEqualTo(dateKey, dateTo)
                .orderBy(dateKey, Query.Direction.ASCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    int i = 0;
                    int j = 0;
                    List<Shift> userList = task.getResult().toObjects(Shift.class);

                    while (i < newShifts.size()) {
                        Shift newShift = newShifts.get(i);
                        if (j == userList.size()) {
                            mUsersShiftsMap.get(newShift.getUserId()).add(0,newShift);
                            mShiftChanges.get(getString(R.string.data_changes_added)).add(newShift);
                            i++;
                        } else if (j < userList.size()) {
                            if (newShift.getDate().getTime() < userList.get(j).getDate().getTime()) {
                                mUsersShiftsMap.get(newShift.getUserId()).add(newShift);
                                mShiftChanges.get(getString(R.string.data_changes_added)).add(newShift);
                                i++;
                            } else if (newShift.getDate().getTime() == userList.get(j).getDate().getTime()) {
                                i++;
                                j++;
                            } else {
                                while (j+1 != userList.size() && newShift.getDate().getTime() > userList.get(j).getDate().getTime()) {
                                    j++;
                                }
                            }
                        }
                    }
                    Collections.sort(mUsersShiftsMap.get(newShifts.get(0).getUserId()));
                    notifyGridDataSetChanged();
                }
            }
        });*/

        int i = 0;
        int j = 0;
        List<Shift> userList = mUsersShiftsMap.get(newShifts.get(0).getUserId());

        while (i < newShifts.size()) {
            Shift newShift = newShifts.get(i);
            if (j == userList.size()) {
                userList.add(newShift);
                mShiftChanges.get(getString(R.string.data_changes_added)).add(newShift);
                i++;
                j++;
            } else if (j < userList.size()) {
                if (newShift.getDate().getTime() < userList.get(j).getDate().getTime()) {
                    userList.add(0,newShift);
                    mShiftChanges.get(getString(R.string.data_changes_added)).add(newShift);
                    i++;
                    j++;
                } else if (newShift.getDate().getTime() == userList.get(j).getDate().getTime()) {
                    i++;
                    j++;
                } else {
                    while (newShift.getDate().getTime() > userList.get(j).getDate().getTime()) {
                        j++;
                        if(j == userList.size()) break;
                    }
                }
            }
        }
        Collections.sort(mUsersShiftsMap.get(newShifts.get(0).getUserId()));
        notifyGridDataSetChanged();

    }

    @Override
    public void onEditShiftChange(Shift oldShift, Shift newShift) {
        if(oldShift != newShift){
            //Check if target user has already a shift that date
            List<Shift> userList = mUsersShiftsMap.get(newShift.getUserId());
            boolean exists = false;
            for (Shift shift : userList) {
                if (shift.getDate().getTime() == newShift.getDate().getTime()){
                    exists = true;
                    break;
                }
            }
            if(!exists){
                mUsersShiftsMap.get(oldShift.getUserId()).remove(oldShift);
                mUsersShiftsMap.get(newShift.getUserId()).add(newShift);
                Collections.sort(mUsersShiftsMap.get(newShift.getUserId()));
            } else {
                // TODO: 29/06/2018 notify shift exists
                Toast.makeText(mContext, "Can't assign shift to target user, date conflict", Toast.LENGTH_SHORT).show();
            }

        }

        //If undo change
        if(mShiftChanges.get(getString(R.string.data_changes_editedNew)).contains(oldShift)){
            mShiftChanges.get(getString(R.string.data_changes_editedNew)).remove(oldShift);

            List<Shift> editedOld = mShiftChanges.get(getString(R.string.data_changes_editedOld));
            for (Shift shift : editedOld) {
                    if(shift.getUserId().equals(newShift.getUserId()) && shift.getDate().getTime() == newShift.getDate().getTime()){
                        editedOld.remove(shift);
                    }
            }
        } else {
            mShiftChanges.get(getString(R.string.data_changes_editedNew)).add(newShift);
            mShiftChanges.get(getString(R.string.data_changes_editedOld)).add(oldShift);
        }
        mGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditShiftRemove(Shift removedShift) {
        mUsersShiftsMap.get(removedShift.getUserId()).remove(removedShift);

        //If undo add
        if(mShiftChanges.get(getString(R.string.data_changes_added)).contains(removedShift)){
            mShiftChanges.get(getString(R.string.data_changes_added)).remove(removedShift);
        } else {
            mShiftChanges.get(getString(R.string.data_changes_removed)).add(removedShift);
        }

        mGridAdapter.notifyDataSetChanged();
    }
}
