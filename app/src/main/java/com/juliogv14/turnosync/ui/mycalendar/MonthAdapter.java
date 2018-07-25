package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
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

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Julio on 08/02/2018.
 * MonthAdapter
 */

public class MonthAdapter extends BaseAdapter {
    private Calendar mCalendar;
    private DateTime mMonthDate;
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
    private Map<String, ShiftType> mShiftsTypesMap;
    private int mCurrentShiftIndex;


    public MonthAdapter(Context c, Date monthDate, DisplayMetrics metrics, ArrayList<Shift> shifts, Map<String, ShiftType> types) {
        mContext = c;
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(monthDate);
        mMonthDate = new DateTime(monthDate);
        mCalendarToday = Calendar.getInstance();
        mDisplayMetrics = metrics;
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);

        mShiftsList = shifts;
        mShiftsTypesMap = types;
        populateMonth();
    }

    private void populateMonth() {
        
        mYear = mCalendar.get(Calendar.YEAR);
        mCalendar.add(Calendar.DAY_OF_MONTH, 7);
        mMonth = mCalendar.get(Calendar.MONTH);

        //Header with day names
        mItems = new ArrayList<>();
        for (String day : mDays) {
            mItems.add(day);
            mDaysShown++;
        }

        //Days last month
        int firstDayWeek = mMonthDate.getDayOfWeek();
        mDaysLastMonth = firstDayWeek-1;
        mDaysShown += mDaysLastMonth;
        int firstDayDisplay = mMonthDate.minusDays(firstDayWeek-1).getDayOfMonth();
        for (int i = 0; i < mDaysLastMonth; i++) {
            mItems.add(String.valueOf(firstDayDisplay+i));
        }

        //Days current month
        int daysThisMonth = mMonthDate.dayOfMonth().getMaximumValue();
        mDaysShown += daysThisMonth;
        for (int i = 1; i <= daysThisMonth; i++) {
            mItems.add(String.valueOf(i));
        }

        //Days next month
        int lastDayWeek = mMonthDate.withDayOfMonth(mMonthDate.dayOfMonth().getMaximumValue()).getDayOfWeek();
        mDaysNextMonth = 7 - lastDayWeek;
        mDaysShown += mDaysNextMonth;

        for (int i = 1; i <= mDaysNextMonth; i++) {
            mItems.add(String.valueOf(i));
        }

         /*   mCalendar.set(Calendar.DAY_OF_MONTH, 1);
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
        */

        
        
        mTitleHeight = CalendarUtils.getLabelHeight(mDisplayMetrics);
    }

    private DateTime getDate(int position) {
        //monthDate is the first day of the month
        int header = 7;
        int monthDatePos = header + mDaysLastMonth;
        return mMonthDate.plusDays(position-monthDatePos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //init shift index
        if(position == 0) {
            mCurrentShiftIndex = 0;
        }

        //int dayCellHeight = (parent.getMeasuredHeight() - mTitleHeight) / 6;
        int dayCellHeight = CalendarUtils.getDayCellHeight(mDisplayMetrics);

        //If its a day type view
        int itemType = getItemViewType(position);
        switch (itemType){
            case 0:
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                textView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, mTitleHeight));
                textView.setText(mItems.get(position));
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                return textView;
            case 1:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shift, parent, false);
                }
                ItemShiftBinding mItemShiftBinding = DataBindingUtil.bind(convertView);
                convertView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, dayCellHeight));
                String stringDayInMonth = mItems.get(position);
                mItemShiftBinding.textViewDayMonth.setText(stringDayInMonth);

                //Background
                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
                background.setColor(Color.WHITE);
                convertView.setBackground(background);

                //Get date from position
                DateTime date = getDate(position);
                //Day number color
                if (date.getMonthOfYear() != mMonthDate.getMonthOfYear()) {
                    // previous or next month
                    if (mItemShiftBinding.textViewShiftType.getText().equals("")) {
                        mItemShiftBinding.textViewDayMonth.setTextColor(Color.GRAY);
                    } else {
                        mItemShiftBinding.textViewDayMonth.setTextColor(Color.BLACK);
                    }
                } else {
                    // current month
                    if (CalendarUtils.isToday(date)) {
                        //Today
                        mItemShiftBinding.textViewDayMonth.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    } else {
                        //Other day
                        mItemShiftBinding.textViewDayMonth.setTextColor(Color.BLACK);
                    }
                }

                //Displaying shifts
                if (!mShiftsList.isEmpty() && mCurrentShiftIndex < mShiftsList.size() && !mShiftsTypesMap.isEmpty()) {
                    Shift shift = mShiftsList.get(mCurrentShiftIndex);
                    DateTime shiftDate = new DateTime(shift.getDate());
                    if (date.getDayOfMonth() == shiftDate.getDayOfMonth() && date.getMonthOfYear() == shiftDate.getMonthOfYear()) {
                        mCurrentShiftIndex++;
                        ShiftType type = mShiftsTypesMap.get(shift.getType());
                        mItemShiftBinding.textViewShiftType.setText(type.getTag());
                        background.setColor(type.getColor());
                        convertView.setBackground(background);
                    }
                }

                return convertView;
            default:
                TextView error = new TextView(mContext);
                error.setBackgroundColor(Color.RED);
                convertView = error;
                return convertView;
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 7) {
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

