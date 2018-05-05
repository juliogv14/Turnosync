package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

public class WeekAdapter extends BaseAdapter {
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
    private int mTitleHeight;
    private int mNamesWidth;
    private String[] mDays;
    private ArrayList<Map<String, String>> mGroupUsers;

    WeekAdapter(Context c, int month, int year, int firstDay, DisplayMetrics metrics, ArrayList<Map<String, String>> groupUsers) {
        mContext = c;
        mMonth = month;
        mYear = year;
        mCalendar = new GregorianCalendar(mYear, mMonth, 1);
        mCalendarToday = Calendar.getInstance();
        mDisplayMetrics = metrics;
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);
        mGroupUsers = groupUsers;

        populateMonth();
    }

    private void populateMonth() {
        /* Label items */
        mItems = new ArrayList<>();
        mItems.add(""); //First item
        for (String day : mDays) {
            mItems.add(day);
            mDaysShown++;
        }

        for (Map<String, String> userUid : mGroupUsers) {

            //User name item
            mItems.add(userUid.get("uid"));
            mDaysShown++;

            //Week days
            for (int i = 0; i < 7; i++) {
                mItems.add("");
                mDaysShown++;
            }

        }

        mTitleHeight = CalendarUtils.getLabelHeight(mDisplayMetrics);
        mNamesWidth = CalendarUtils.getDayCellHeight(mDisplayMetrics);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemType = getItemViewType(position);

        int dayCellHeight = CalendarUtils.getDayCellHeight(mDisplayMetrics);

        switch (itemType) {
            case 0:             //names and first item
                if (convertView == null) {
                    TextView names = new TextView(mContext);
                    names.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    names.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, dayCellHeight));
                    if (position == 0) {
                        names.setText("");
                    } else {
                        int nameIndex = position / 8 - 1;
                        names.setText(mGroupUsers.get(nameIndex).get("uid"));
                    }
                    convertView = names;
                }
                convertView.setBackgroundColor(Color.GRAY);
                return convertView;
            case 1:             //Header with days
                if (convertView == null) {
                    TextView days = new TextView(mContext);
                    days.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    days.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, mTitleHeight));

                    days.setText(mItems.get(position));
                    convertView = days;
                }
                convertView.setBackgroundColor(Color.LTGRAY);
                return convertView;
            case 2:             //Shifts
                if (convertView == null) {
                    TextView shifts = new TextView(mContext);
                    shifts.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    shifts.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, dayCellHeight));

                    shifts.setText(mItems.get(position));

                    convertView = shifts;
                }

                return convertView;
            default:
                TextView error = new TextView(mContext);
                error.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                error.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, mTitleHeight));
                error.setText("error");
                convertView = error;
                return convertView;
        }


    }

    @Override
    public int getItemViewType(int position) {

        if (position % 8 == 0 && position != 0) {    //names and first item
            return 0;
        } else if (position < 8) {   //Header with days
            return 1;
        } else {                    //Shifts
            return 2;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

}
