package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.databinding.ItemShiftBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Julio on 08/02/2018.
 * MonthAdapter
 */

public class MonthAdapter extends BaseAdapter {
    private Calendar mCalendar;
    private Calendar mCalendarToday;
    private Context mContext;
    private DisplayMetrics mDisplayMetrics;
    private List<String> mItems;
    private int mMonth;
    private int mYear;
    private int mDaysShown;
    private int mDaysLastMonth;
    private int mDaysNextMonth;
    private int mTitleHeight;
    private String[] mDays;

    private ArrayList<Shift> mShiftsList;
    private HashMap<String, ShiftType> mShiftsTypesList;


    public MonthAdapter(Context c, Date monthDate, DisplayMetrics metrics, ArrayList<Shift> shifts, HashMap<String, ShiftType> types) {
        mContext = c;
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(monthDate);
        mCalendarToday = Calendar.getInstance();
        mDisplayMetrics = metrics;
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);

        mShiftsList = shifts;
        mShiftsTypesList = types;
        populateMonth();
    }

    private void populateMonth() {

        mYear = mCalendar.get(Calendar.YEAR);
        mCalendar.add(Calendar.DAY_OF_MONTH, 7);
        mMonth = mCalendar.get(Calendar.MONTH);


        mItems = new ArrayList<>();
        for (String day : mDays) {
            mItems.add(day);
            mDaysShown++;
        }
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDay = CalendarUtils.getDay(mCalendar.get(Calendar.DAY_OF_WEEK));
        int prevDay;
        if (mMonth == 0) {
            mCalendar.set(Calendar.MONTH, 11);
            prevDay = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - firstDay + 1;
        } else {
            mCalendar.set(Calendar.MONTH, mMonth - 1);
            prevDay = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - firstDay + 1;
        }
        for (int i = 0; i < firstDay; i++) {
            mItems.add(String.valueOf(prevDay + i));
            mDaysLastMonth++;
            mDaysShown++;
        }
        mCalendar.set(Calendar.MONTH, mMonth);
        int daysInMonth = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            mItems.add(String.valueOf(i));
            mDaysShown++;
        }

        mDaysNextMonth = 1;
        while (mDaysShown % 7 != 0) {
            mItems.add(String.valueOf(mDaysNextMonth));
            mDaysShown++;
            mDaysNextMonth++;
        }

        mTitleHeight = CalendarUtils.getLabelHeight(mDisplayMetrics);
    }

    private int[] getDate(int position) {
        int date[] = new int[3];
        if (position <= 6) {
            return null; // day names
        } else if (position <= mDaysLastMonth + 6) {
            // previous month
            date[0] = Integer.parseInt(mItems.get(position));
            if (mMonth == 0) {
                date[1] = 11;
                date[2] = mYear - 1;
            } else {
                date[1] = mMonth - 1;
                date[2] = mYear;
            }
        } else if (position <= mDaysShown - mDaysNextMonth) {
            // current month
            date[0] = position - (mDaysLastMonth + 6);
            date[1] = mMonth;
            date[2] = mYear;
        } else {
            // next month
            date[0] = Integer.parseInt(mItems.get(position));
            if (mMonth == 11) {
                date[1] = 0;
                date[2] = mYear + 1;
            } else {
                date[1] = mMonth + 1;
                date[2] = mYear;
            }
        }
        return date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int itemType = getItemViewType(position);

        //int dayCellHeight = (parent.getMeasuredHeight() - mTitleHeight) / 6;
        int dayCellHeight = CalendarUtils.getDayCellHeight(mDisplayMetrics);

        //If its a day type view
        int date[] = getDate(position);
        if (itemType == 0 && date != null) {
            //Needs inflation
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shift, parent, false);
            }
            ItemShiftBinding mItemShiftBinding = DataBindingUtil.bind(convertView);

            convertView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, dayCellHeight));
            String stringDayInMonth = mItems.get(position);
            mItemShiftBinding.textViewDayMonth.setText(stringDayInMonth);

            if (!mShiftsList.isEmpty()) {

                Shift shift = mShiftsList.get(0);
                Calendar calShift = new GregorianCalendar();
                calShift.setTime(shift.getDate());

                if (date[0] == calShift.get(Calendar.DAY_OF_MONTH) && date[1] == calShift.get(Calendar.MONTH)) {

                    mShiftsList.remove(shift);
                    ShiftType type = mShiftsTypesList.get(shift.getType());
                    mItemShiftBinding.textViewShiftType.setText(type.getTag());
                    convertView.setBackgroundColor(type.getColor());

                }
            }


            //Day number color
            if (date[1] != mMonth) {
                // previous or next month

                if (mItemShiftBinding.textViewShiftType.getText().equals("")) {
                    mItemShiftBinding.textViewDayMonth.setTextColor(Color.GRAY);
                } else {
                    mItemShiftBinding.textViewDayMonth.setTextColor(Color.BLACK);
                }


            } else {
                // current month
                if (CalendarUtils.isToday(mCalendarToday, date[0], date[1], date[2])) {
                    //Today
                    mItemShiftBinding.textViewDayMonth.setTextColor(Color.RED);
                } else {
                    //Other day
                    mItemShiftBinding.textViewDayMonth.setTextColor(Color.BLACK);
                }
            }

            return convertView;
        } else {
            TextView textView = new TextView(mContext);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            textView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, mTitleHeight));
            textView.setText(mItems.get(position));

            return textView;

        }

    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getDate(position) != null) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

