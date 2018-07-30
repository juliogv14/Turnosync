package com.juliogv14.turnosync.ui.mycalendar;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.viewmodels.MyCalendarVM;
import com.juliogv14.turnosync.databinding.PageWeekBinding;
import com.juliogv14.turnosync.ui.mycalendar.changerequests.RequestChangeDialog;
import com.juliogv14.turnosync.ui.mycalendar.createshift.CreateShiftDialog;
import com.juliogv14.turnosync.ui.mycalendar.createshift.EditShiftDialog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Julio on 11/05/2018.
 * ScheduleWeekPageFragment
 */

public class ScheduleWeekPageFragment extends Fragment implements CreateShiftDialog.CreateShiftListener, EditShiftDialog.EditShiftListener, RequestChangeDialog.RequestChangeListener {

    //Keys
    private static final String POSITION_KEY = "position";
    private static final String ROLE_KEY = "currentCalendar";
    private static final String CURRENT_WEEK_DATE_KEY = "weekDate";
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
    private WeekPageListener mListener;

    //Parent ViewModel
    MyCalendarVM mParentViewModel;

    //Variables
    private int mPosition;
    private UserRoles mRole;
    private DateTime mWeekDate;
    private boolean mEditMode;
    private long mWeeklyHours = -1;
    private RequestChangeDialog mRequestChangeDialog;

    //Data lists
    private ArrayList<UserRef> mWorkgroupUsers;
    private Map<String, ArrayList<Shift>> mUsersShiftsMap;
    private Map<String, ShiftType> mShiftTypesMap;
    private Map<String, ArrayList<Shift>> mShiftChanges;
    private Map<String, Period> mUsersHourCount;

    //GridAdapter
    private BaseAdapter mGridAdapter;

    public static ScheduleWeekPageFragment newInstance(int position,
                                                       String role,
                                                       DateTime weekDate,
                                                       ArrayList<UserRef> workgroupUsers,
                                                       LinkedHashMap<String, ArrayList<Shift>> userShifts,
                                                       HashMap<String, ArrayList<Shift>> shiftChanges) {

        ScheduleWeekPageFragment f = new ScheduleWeekPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(POSITION_KEY, position);
        args.putString(ROLE_KEY, role);
        args.putLong(CURRENT_WEEK_DATE_KEY, weekDate.getMillis());
        args.putParcelableArrayList(WORKGROUP_USERS_KEY, workgroupUsers);
        args.putSerializable(USERS_SHIFT_MAP_KEY, userShifts);
        args.putSerializable(SHIFT_CHANGES_MAP_KEY, shiftChanges);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof WeekPageListener) {
            mListener = (WeekPageListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WeekPageListener");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setRetainInstance(true);
        if (args != null) {
            mPosition = args.getInt(POSITION_KEY);
            mRole = UserRoles.valueOf(args.getString(ROLE_KEY));
            mWorkgroupUsers = args.getParcelableArrayList(WORKGROUP_USERS_KEY);
            mUsersShiftsMap = (Map<String, ArrayList<Shift>>) args.getSerializable(USERS_SHIFT_MAP_KEY);
            mWeekDate = new DateTime(args.getLong(CURRENT_WEEK_DATE_KEY));
            mShiftChanges = (Map<String, ArrayList<Shift>>) args.getSerializable(SHIFT_CHANGES_MAP_KEY);
        }
        mParentViewModel = ViewModelProviders.of((Fragment)mListener).get(MyCalendarVM.class);
        Boolean editmode = mParentViewModel.getEditMode().getValue();
        if(editmode != null) mEditMode = editmode;
        mShiftTypesMap = mParentViewModel.getShiftTypes().getValue();

    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = PageWeekBinding.inflate(inflater, container, false);
        mParentViewModel.getEditMode().removeObservers(this);
        mParentViewModel.getEditMode().observe(this, new Observer<Boolean>() {

            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if(aBoolean != null){
                    mEditMode = aBoolean;
                    mParentViewModel.getOwnShift().postValue(null);
                    mParentViewModel.getOtherShift().postValue(null);
                }
            }
        });
        mParentViewModel.getWeeklyHours().removeObservers(this);
        mParentViewModel.getWeeklyHours().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                if(aLong != null) mWeeklyHours = aLong;
            }
        });
        mParentViewModel.getOwnShift().removeObservers(this);
        mParentViewModel.getOwnShift().observe(this, new Observer<Shift>() {
            @Override
            public void onChanged(@Nullable Shift ownShift) {
                mGridAdapter.notifyDataSetChanged();
                if (ownShift != null) {
                    Shift otherShift = mParentViewModel.getOtherShift().getValue();
                    if (otherShift != null && mRequestChangeDialog == null && !mEditMode) {
                        handleChangeRequest(ownShift, otherShift);
                    }
                }
            }
        });
        mParentViewModel.getOtherShift().removeObservers(this);
        mParentViewModel.getOtherShift().observe(this, new Observer<Shift>() {
            @Override
            public void onChanged(@Nullable Shift otherShift) {
                mGridAdapter.notifyDataSetChanged();
                if(otherShift != null){
                    Shift ownShift = mParentViewModel.getOwnShift().getValue();
                    if(ownShift != null && mRequestChangeDialog == null && !mEditMode) {
                        handleChangeRequest(ownShift, otherShift);
                    }
                }
            }
        });

        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DateTime firstDay = mWeekDate.toDateTime();
        DateTime lastDay = firstDay.withDayOfWeek(DateTimeConstants.SUNDAY);
        //Week label
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MMMM");
        String dateFrom = fmt.print(firstDay);
        String dateTo = fmt.print(lastDay);

        String week = dateFrom + " - " + dateTo;
        mViewBinding.textViewWeek.setText(week);

        //Adapter
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mGridAdapter = new WeekAdapter(mContext, mParentViewModel, metrics, mWeekDate, mWorkgroupUsers, mUsersShiftsMap, mShiftTypesMap);
        mViewBinding.gridViewWeek.setAdapter(mGridAdapter);

        mRequestChangeDialog = null;
        //createShift onclick
        mViewBinding.gridViewWeek.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int day = position % 8;
                if(position > 7 && day > 0 ){
                    DateTime date = mWeekDate.plusDays(day-1);

                    int row = position / 8;
                    UserRef userRef = mWorkgroupUsers.get(row-1);
                    if(!mEditMode || mWeeklyHours == -1 || date.isBefore(DateTime.now())){
                        return;
                    }
                    if(!mShiftTypesMap.isEmpty() && mWeeklyHours != 0 ) {
                        //Check if there is a shift there
                        Shift shiftSelected = null;
                        for (Shift shift : mUsersShiftsMap.get(userRef.getUid())) {
                            if (shift.getDate().getTime() == date.getMillis()) {
                                shiftSelected = shift;
                                break;
                            }
                        }
                        if(shiftSelected != null) {
                            EditShiftDialog dialog = EditShiftDialog.newInstance(date, userRef, mShiftTypesMap, mWorkgroupUsers, shiftSelected);
                            dialog.show(getChildFragmentManager(), "esd");
                        } else {
                            recalculateHours();
                            CreateShiftDialog dialog = CreateShiftDialog.newInstance(date, userRef, mShiftTypesMap, mUsersHourCount.get(userRef.getUid()).toStandardDuration().getMillis(), mWeeklyHours);
                            dialog.show(getChildFragmentManager(), "csd");
                        }
                    } else {
                        if(mWeeklyHours == 0){
                            Toast.makeText(mContext, R.string.toast_schedule_notLoaded, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(mContext, R.string.toast_schedule_noShiftTypes, Toast.LENGTH_LONG).show();
                        }

                    }
                }
            }
        });

        mViewBinding.gridViewWeek.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int day = position % 8;
                if(position > 7 && day > 0 ) {
                    DateTime date = mWeekDate.plusDays(day-1);

                    int row = position / 8;
                    UserRef userRef = mWorkgroupUsers.get(row - 1);

                    if(date.isBefore(DateTime.now())) return false;

                    if (!mShiftTypesMap.isEmpty()) {
                        Shift shiftSelected = null;
                        String ownUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        boolean ownShift = TextUtils.equals(userRef.getUid(), ownUid);

                        for (Shift shift : mUsersShiftsMap.get(userRef.getUid())) {
                            if (shift.getDate().getTime() == date.getMillis()) {
                                shiftSelected = shift;
                                break;
                            }
                        }
                        if(shiftSelected != null && !mEditMode){
                            if(ownShift){
                                mParentViewModel.getOwnShift().postValue(shiftSelected);
                            } else {
                                mParentViewModel.getOtherShift().postValue(shiftSelected);
                            }
                            return true;
                        } else {
                            return false;
                        }

                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void handleChangeRequest(Shift ownShift, Shift otherShift){

        //Check for
        if (ownShift.getDate().getTime() == otherShift.getDate().getTime()
                || (!mListener.hasShiftOnDate(ownShift.getUserId(), otherShift.getDate()) && !mListener.hasShiftOnDate(otherShift.getUserId(), ownShift.getDate()))){
            mRequestChangeDialog = RequestChangeDialog.newInstance(ownShift, otherShift, (HashMap<String, ShiftType>) mShiftTypesMap, mWorkgroupUsers);
            mRequestChangeDialog.show(getChildFragmentManager(), "requestChange");
        } else {
            Toast.makeText(mContext, R.string.toast_request_conflict, Toast.LENGTH_LONG).show();
        }
        mParentViewModel.setOwnShift(null);
        mParentViewModel.setOtherShift(null);

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
                Toast.makeText(mContext, R.string.toast_schedule_conflict, Toast.LENGTH_SHORT).show();
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
        //If selected for change
        if(removedShift == mParentViewModel.getOwnShift().getValue()){
            mParentViewModel.setOwnShift(null);
        } else if (removedShift == mParentViewModel.getOtherShift().getValue()) {
            mParentViewModel.setOtherShift(null);
        }
        mGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestShiftChange(ChangeRequest changeRequest) {
        mRequestChangeDialog = null;
        mListener.onNewChangeRequest(changeRequest);
    }

    @Override
    public void onCancelShiftChange() {
        mRequestChangeDialog = null;
        mParentViewModel.setOwnShift(null);
        mParentViewModel.setOtherShift(null);
    }

    public interface WeekPageListener {
        void onNewChangeRequest(ChangeRequest request);
        boolean hasShiftOnDate(String uid, Date date);
    }

}
