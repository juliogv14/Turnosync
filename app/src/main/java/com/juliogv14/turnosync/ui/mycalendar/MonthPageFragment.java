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

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.PageMonthBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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

    //GridAdapter
    private BaseAdapter mGridAdapter;
    private ArrayList<Shift> mShiftList;
    private Map<String, ShiftType> mShiftTypesMap;

    //Variables
    private Date mMonthDate;
    private Period mMonthHours;


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
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMonthDate = new Date(args.getLong(CURRENT_MONTH_DATE_KEY));
            mShiftList = args.getParcelableArrayList(MONTH_SHIFT_LIST_KEY);
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
        ViewGroup.LayoutParams params = mViewBinding.gridViewCalendar.getLayoutParams();
        params.height = (CalendarUtils.getDayCellHeight(metrics) * (mGridAdapter.getCount()/7));
        mViewBinding.gridViewCalendar.setLayoutParams(params);
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
                    if(!mShiftTypesMap.isEmpty()){
                        calculateMonthHours();
                        PeriodFormatter formatter = new PeriodFormatterBuilder()
                                .appendHours()
                                .appendSuffix(" h")
                                .appendMinutes()
                                .appendSuffix(" min")
                                .toFormatter();
                        String totalHours = getString(R.string.calendar_total_hours) + " " +formatter.print(mMonthHours);
                        mViewBinding.textViewTotalHours.setText(totalHours);
                    }
                }
            });
        }
    }

    private void calculateMonthHours (){
        mMonthHours = new Period();
        Calendar cal = new GregorianCalendar();
        cal.setTime(mMonthDate);
        cal.set(Calendar.DAY_OF_MONTH,1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date firstDay = cal.getTime();
        cal.setTime(mMonthDate);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date lastDay = cal.getTime();
        for (Shift shift : mShiftList) {
            if(shift.getDate().after(firstDay) && shift.getDate().before(lastDay)){
                ShiftType type = mShiftTypesMap.get(shift.getType());
                mMonthHours = mMonthHours.plus(type.getJodaPeriod());
            }
        }

    }
}
