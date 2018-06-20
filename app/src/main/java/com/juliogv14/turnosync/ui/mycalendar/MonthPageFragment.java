package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.PageMonthBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julio on 18/02/2018.
 * MonthPageFragment
 */

public class MonthPageFragment extends Fragment {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Constants
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String CURRENT_MONTH_DATE_KEY = "currentMonthDate";
    private static final String MONTH_SHIFT_LIST_KEY = "shiftList";
    private static final String MONTH_SHIFT_TYPES_MAP_KEY = "shiftTypesMap";

    //Binding
    protected PageMonthBinding mViewBinding;

    //Context and listener
    private Context mContext;

    //Month
    private Date mMonthDate;

    //GridAdapter
    private BaseAdapter mGridAdapter;
    private ArrayList<Shift> mShiftList;
    private Map<String, ShiftType> mShiftTypesMap;


    public static MonthPageFragment newInstance(UserWorkgroup workgroup, Date monthCalendar, ArrayList<Shift> shiftList, HashMap<String, ShiftType> shiftTypes) {
        MonthPageFragment f = new MonthPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        //TODO: workgroup needed?
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        args.putLong(CURRENT_MONTH_DATE_KEY, monthCalendar.getTime());
        args.putParcelableArrayList(MONTH_SHIFT_LIST_KEY, shiftList);
        args.putSerializable(MONTH_SHIFT_TYPES_MAP_KEY, shiftTypes);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMonthDate = new Date(args.getLong(CURRENT_MONTH_DATE_KEY));
            mShiftList = args.getParcelableArrayList(MONTH_SHIFT_LIST_KEY);
            //noinspection unchecked
            mShiftTypesMap = (HashMap<String, ShiftType>)args.getSerializable(MONTH_SHIFT_TYPES_MAP_KEY);
        }
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = PageMonthBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar cal = Calendar.getInstance();
        cal.setTime(mMonthDate);
        cal.add(Calendar.DAY_OF_MONTH, 7);

        mViewBinding.textViewMonth.setText(CalendarUtils.getMonthString(mContext, cal.get(Calendar.MONTH)));

        cal.setTime(mMonthDate);

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        mGridAdapter = new MonthAdapter(mContext, mMonthDate, metrics, mShiftList, mShiftTypesMap);

        mViewBinding.gridViewCalendar.setAdapter(mGridAdapter);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public void notifyGridDataSetChanged() {
        if (mContext != null) {
            ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGridAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
