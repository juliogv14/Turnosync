package com.juliogv14.turnosync.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.juliogv14.turnosync.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarUtils {

    public static final int[] mDaysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public static int getLabelHeight(DisplayMetrics displayMetrics) {
        int densityDpi = displayMetrics.densityDpi;
        if (densityDpi > DisplayMetrics.DENSITY_LOW && densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 40;
        } else if (densityDpi > DisplayMetrics.DENSITY_MEDIUM && densityDpi < DisplayMetrics.DENSITY_HIGH) {
            return 52;
        } else if (densityDpi > DisplayMetrics.DENSITY_HIGH && densityDpi < DisplayMetrics.DENSITY_XHIGH) {
            return 60;
        } else if (densityDpi > DisplayMetrics.DENSITY_XHIGH && densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            return 70;
        } else if (densityDpi > DisplayMetrics.DENSITY_XXHIGH && densityDpi < DisplayMetrics.DENSITY_XXXHIGH) {
            return 80;
        } else {
            return 54;
        }
    }

    public static int getDayCellHeight(DisplayMetrics displayMetrics) {
        int densityDpi = displayMetrics.densityDpi;
        if (densityDpi > DisplayMetrics.DENSITY_LOW && densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 70;
        } else if (densityDpi > DisplayMetrics.DENSITY_MEDIUM && densityDpi < DisplayMetrics.DENSITY_HIGH) {
            return 90;
        } else if (densityDpi > DisplayMetrics.DENSITY_HIGH && densityDpi < DisplayMetrics.DENSITY_XHIGH) {
            return 110;
        } else if (densityDpi > DisplayMetrics.DENSITY_XHIGH && densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            return 130;
        } else if (densityDpi > DisplayMetrics.DENSITY_XXHIGH && densityDpi < DisplayMetrics.DENSITY_XXXHIGH) {
            return 150;
        } else {
            return 110;
        }
    }

    public static int getDay(int day) {
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
                return -1;
        }
    }

    public static String getDayString(Context context, int day){
        String[]days = context.getResources().getStringArray(R.array.calendar_days_of_week);
        return days[day];
    }

    public static String getMonthString(Context context, int month){
        String[]months = context.getResources().getStringArray(R.array.calendar_months_of_year);
        return months[month];
    }

    public static int daysInMonth(Calendar calendar, int year, int month) {
        int daysInMonth = mDaysInMonth[month];
        GregorianCalendar gregCalendar = (GregorianCalendar) calendar;
        if (month == 1 && gregCalendar.isLeapYear(year))
            daysInMonth++;
        return daysInMonth;
    }


    public static boolean isToday(Calendar calendarToday, int day, int month, int year) {
        return (calendarToday.get(Calendar.MONTH) == month
                && calendarToday.get(Calendar.YEAR) == year
                && calendarToday.get(Calendar.DAY_OF_MONTH) == day);
    }
}
