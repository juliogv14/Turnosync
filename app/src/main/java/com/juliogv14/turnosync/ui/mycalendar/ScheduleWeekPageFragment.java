package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.util.DisplayMetrics;
import android.util.MutableBoolean;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ItemShiftBinding;
import com.juliogv14.turnosync.databinding.PageWeekBinding;
import com.juliogv14.turnosync.ui.mycalendar.createshift.CreateShiftDialog;
import com.juliogv14.turnosync.ui.mycalendar.createshift.EditShiftDialog;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Julio on 18/02/2018.
 * MonthPageFragment
 */

public class ScheduleWeekPageFragment extends Fragment implements CreateShiftDialog.CreateShiftListener, EditShiftDialog.EditShiftListener {

    //Keys
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String CURRENT_WEEK_DATE_KEY = "currentCalendar";
    private static final String WORKGROUP_USERS_KEY = "workgroupUsers";
    private static final String USERS_SHIFT_MAP_KEY = "userShiftMap";
    private static final String SHIFT_TYPES_MAP_KEY = "shiftTypesMap";
    private static final String SHIFT_CHANGES_MAP_KEY = "shiftChangesMap";
    private static final String EDIT_MODE_KEY = "editMode";


    private final String TAG = this.getClass().getSimpleName();
    //Binding
    protected PageWeekBinding mViewBinding;

    //Context and listener
    private Context mContext;
    private OnScheduleFragmentInteractionListener mListener;

    //Variables
    private Date mWeekDate;
    private AtomicBoolean mEditMode;

    //Data lists
    private ArrayList<UserRef> mWorkgroupUsers;
    private Map<String, ArrayList<Shift>> mUsersShiftsMap;
    private Map<String, ShiftType> mShiftTypesMap;
    private Map<String, ArrayList<Shift>> mShiftChanges;

    //GridAdapter
    private BaseAdapter mGridAdapter;

    public static ScheduleWeekPageFragment newInstance(UserWorkgroup workgroup,
                                                       Date weekDate,
                                                       ArrayList<UserRef> workgroupUsers,
                                                       LinkedHashMap<String, ArrayList<Shift>> userShifts,
                                                       HashMap<String, ShiftType> shiftTypes,
                                                       HashMap<String, ArrayList<Shift>> shiftChanges,
                                                       AtomicBoolean editMode) {

        ScheduleWeekPageFragment f = new ScheduleWeekPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        //TODO: check if workgroup is needed
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        args.putLong(CURRENT_WEEK_DATE_KEY, weekDate.getTime());
        args.putParcelableArrayList(WORKGROUP_USERS_KEY, workgroupUsers);
        args.putSerializable(USERS_SHIFT_MAP_KEY, userShifts);
        args.putSerializable(SHIFT_TYPES_MAP_KEY, shiftTypes);
        args.putSerializable(SHIFT_CHANGES_MAP_KEY, shiftChanges);
        args.putSerializable(EDIT_MODE_KEY, editMode);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof OnScheduleFragmentInteractionListener) {
            mListener = (OnScheduleFragmentInteractionListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnScheduleFragmentInteractionListener");
        }
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

                    if(shiftSelected != null){
                        EditShiftDialog dialog = EditShiftDialog.newInstance(date, userRef, mShiftTypesMap, mWorkgroupUsers, shiftSelected);
                        dialog.show(getChildFragmentManager(), "esd");
                    } else {
                        CreateShiftDialog dialog = CreateShiftDialog.newInstance(date, userRef, mShiftTypesMap);
                        dialog.show(getChildFragmentManager(), "csd");
                    }
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
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

    @Override
    public void onCreateShiftCreate(ArrayList<Shift> newShifts) {
        for (Shift shift : newShifts) {
            mUsersShiftsMap.get(shift.getUserId()).add(shift);
            mShiftChanges.get(getString(R.string.data_changes_added)).add(shift );
        }
        mGridAdapter.notifyDataSetChanged();

    }

    @Override
    public void onEditShiftChange(Shift oldShift, Shift newShift) {
        if(oldShift != newShift){
            mUsersShiftsMap.get(oldShift.getUserId()).remove(oldShift);
            mUsersShiftsMap.get(newShift.getUserId()).add(newShift);

        }
        //TODO: handle user changes in firestore
        mShiftChanges.get(getString(R.string.data_changes_edited)).add(newShift);
        mGridAdapter.notifyDataSetChanged();

    }

    @Override
    public void onEditShiftRemove(Shift removedShift) {
        mUsersShiftsMap.get(removedShift.getUserId()).remove(removedShift);
        mShiftChanges.get(getString(R.string.data_changes_removed)).add(removedShift);
        mGridAdapter.notifyDataSetChanged();
    }

    public interface OnScheduleFragmentInteractionListener {
        void onWeekDaySelected(Date date, UserRef userRef);
    }

}
