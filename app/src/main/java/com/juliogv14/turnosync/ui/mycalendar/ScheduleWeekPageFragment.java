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
import android.widget.BaseAdapter;

import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.ui.drawerlayout.OnFragmentInteractionListener;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.PageWeekBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julio on 18/02/2018.
 * MonthPageFragment
 */

public class ScheduleWeekPageFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    //Keys
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String WORKGROUP_USERS_KEY = "workgroupUsers";
    private static final String USERS_SHIFT_LIST_KEY = "userShiftList";
    private static final String CURRENT_WEEK_DATE_KEY = "currentCalendar";

    //Binding
    protected PageWeekBinding mViewBinding;

    //Firebase
    private OnScheduleFragmentInteractionListener mListener;
    private ArrayList<UserRef> mWorkgroupUsers;
    private Map<String, ArrayList<Shift>> mUsersShiftList;

    //Month
    private Date mWeekDate;

    //GridAdapter
    private BaseAdapter mGridAdapter;

    public static ScheduleWeekPageFragment newInstance(UserWorkgroup workgroup, Date weekDate, ArrayList<UserRef> workgroupUsers, HashMap<String, ArrayList<Shift>> userShifts) {
        ScheduleWeekPageFragment f = new ScheduleWeekPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        args.putParcelableArrayList(WORKGROUP_USERS_KEY, workgroupUsers);
        args.putSerializable(USERS_SHIFT_LIST_KEY, userShifts);
        args.putLong(CURRENT_WEEK_DATE_KEY, weekDate.getTime());
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnScheduleFragmentInteractionListener) {
            mListener = (OnScheduleFragmentInteractionListener) context;
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
        if (args != null) {
            mWorkgroupUsers = args.getParcelableArrayList(WORKGROUP_USERS_KEY);
            mUsersShiftList = (Map<String, ArrayList<Shift>>) args.getSerializable(USERS_SHIFT_LIST_KEY);
            mWeekDate = new Date(args.getLong(CURRENT_WEEK_DATE_KEY));

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

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        int firstDay = calendar.get(Calendar.DAY_OF_MONTH);
        int firstMonth = calendar.get(Calendar.MONTH);


        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int lastDay = calendar.get(Calendar.DAY_OF_MONTH);
        int lastMonth = calendar.get(Calendar.MONTH);

        String week = "" + firstDay + "/" + CalendarUtils.getMonthString((Context) mListener, firstMonth) + "-"
                + lastDay + "/" + CalendarUtils.getMonthString((Context) mListener, lastMonth);
        mViewBinding.textViewWeek.setText(week);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mGridAdapter = new WeekAdapter((SupportActivity) mListener, metrics, mWeekDate, mWorkgroupUsers, mUsersShiftList);

        mViewBinding.gridViewWeek.setAdapter(mGridAdapter);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void notifyGridDataSetChanged() {
        ((SupportActivity)mListener).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGridAdapter.notifyDataSetChanged();
            }
        });
    }

    public interface OnScheduleFragmentInteractionListener extends OnFragmentInteractionListener {
        void onShiftSelected(Shift shift);
    }

}
