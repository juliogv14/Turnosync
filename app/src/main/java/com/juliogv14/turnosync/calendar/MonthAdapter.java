package com.juliogv14.turnosync.calendar;

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
import com.juliogv14.turnosync.databinding.ItemShiftBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Julio on 08/02/2018.
 * MonthAdapter
 */

public class MonthAdapter extends BaseAdapter {
    private GregorianCalendar mCalendar;
    private Calendar mCalendarToday;
    private Context mContext;
    private DisplayMetrics mDisplayMetrics;
    private List<String> mItems;
    private int mMonth;
    private int mYear;
    private int mDaysShown;
    private int mDaysLastMonth;
    private int mDaysNextMonth;
    private int mTitleHeight, mDayHeight;
    private String[] mDays;
    private final int[] mDaysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private ArrayList<Shift> mShiftsList;


    public MonthAdapter(Context c, int month, int year, DisplayMetrics metrics, ArrayList<Shift> shifts) {
        mContext = c;
        mMonth = month;
        mYear = year;
        mCalendar = new GregorianCalendar(mYear, mMonth, 1);
        mCalendarToday = Calendar.getInstance();
        mDisplayMetrics = metrics;
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);
        mShiftsList = shifts;

        populateMonth();
    }

    /**
     * @param date     - null if day title (0 - dd / 1 - mm / 2 - yy)
     * @param position - position in item list
     * @param item     - view for date
     */
    private void onDate(int[] date, int position, View item) {

    }

    ;

    private void populateMonth() {
        mItems = new ArrayList<>();
        for (String day : mDays) {
            mItems.add(day);
            mDaysShown++;
        }

        int firstDay = getDay(mCalendar.get(Calendar.DAY_OF_WEEK));
        int prevDay;
        if (mMonth == 0)
            prevDay = daysInMonth(11) - firstDay + 1;
        else
            prevDay = daysInMonth(mMonth - 1) - firstDay + 1;
        for (int i = 0; i < firstDay; i++) {
            mItems.add(String.valueOf(prevDay + i));
            mDaysLastMonth++;
            mDaysShown++;
        }

        int daysInMonth = daysInMonth(mMonth);
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


        mTitleHeight = getLabelHeight();
    }

    private int daysInMonth(int month) {
        int daysInMonth = mDaysInMonth[month];
        if (month == 1 && mCalendar.isLeapYear(mYear))
            daysInMonth++;
        return daysInMonth;
    }

    private int getLabelHeight() {
        int densityDpi = mDisplayMetrics.densityDpi;
        if (densityDpi > DisplayMetrics.DENSITY_LOW && densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 24;
        } else if (densityDpi > DisplayMetrics.DENSITY_LOW && densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 32;
        } else if (densityDpi > DisplayMetrics.DENSITY_MEDIUM && densityDpi < DisplayMetrics.DENSITY_HIGH) {
            return 48;
        } else if (densityDpi > DisplayMetrics.DENSITY_HIGH && densityDpi < DisplayMetrics.DENSITY_XHIGH) {
            return 54;
        } else if (densityDpi > DisplayMetrics.DENSITY_XHIGH && densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            return 68;
        } else if (densityDpi > DisplayMetrics.DENSITY_XXHIGH && densityDpi < DisplayMetrics.DENSITY_XXXHIGH) {
            return 80;
        } else {
            return 54;
        }
    }

    private int getDay(int day) {
        switch (day) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return 0;
        }
    }

    private boolean isToday(int day, int month, int year) {
        if (mCalendarToday.get(Calendar.MONTH) == month
                && mCalendarToday.get(Calendar.YEAR) == year
                && mCalendarToday.get(Calendar.DAY_OF_MONTH) == day) {
            return true;
        }
        return false;
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
        //Needs inflation
        if (convertView == null) {
            int dayCellHeight = (parent.getMeasuredHeight() - mTitleHeight) / 5;

            //If its a day type view
            int date[] = getDate(position);
            if (itemType == 0 && date != null) {

                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shift, parent, false);
                ItemShiftBinding mItemShiftBinding = DataBindingUtil.bind(convertView);

                convertView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, dayCellHeight));
                String stringDayInMonth = mItems.get(position);
                mItemShiftBinding.textViewDayMonth.setText(stringDayInMonth);
                int dayInMonth = Integer.parseInt(stringDayInMonth);
                if (!mShiftsList.isEmpty()) {
                    Shift shift = mShiftsList.get(0);
                    if (dayInMonth == shift.getDay() && mMonth == shift.getMonth() - 1) {
                        mShiftsList.remove(shift);
                        mItemShiftBinding.textViewShiftType.setText(shift.getType());
                        //TODO color cells from settings and type
                        convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
                    }
                }


                //Day number color
                if (date[1] != mMonth) {
                    // previous or next month
                    mItemShiftBinding.textViewDayMonth.setTextColor(Color.GRAY);

                } else {
                    // current month
                    if (isToday(date[0], date[1], date[2])) {
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
        return convertView;
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

