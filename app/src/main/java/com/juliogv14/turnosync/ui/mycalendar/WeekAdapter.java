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
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.ItemShiftBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WeekAdapter extends BaseAdapter {
    private GregorianCalendar mCalendar;
    private Context mContext;
    private DisplayMetrics mDisplayMetrics;
    private List<String> mItems;
    private Date mWeekDate;
    private int mTitleHeight;
    private int mNamesWidth;
    private String[] mDays;

    //Shifts variables
    private ArrayList<UserRef> mGroupUsers;
    private Map<String, ArrayList<Shift>> mUserShiftMap;
    private Map<String, ShiftType> mShiftsTypesMap;

    //Per user shifts
    private Iterator<Map.Entry<String, ArrayList<Shift>>> mShiftIterator;
    private String mCurrentUid;
    private int mCurrentShiftIndex;


    WeekAdapter(Context c, DisplayMetrics metrics, Date weekDate, ArrayList<UserRef> groupUsers, Map<String, ArrayList<Shift>> userShifts, Map<String, ShiftType> shiftTypes) {
        mContext = c;
        mDisplayMetrics = metrics;
        mGroupUsers = groupUsers;
        mUserShiftMap = userShifts;
        mShiftsTypesMap = shiftTypes;

        mCalendar = new GregorianCalendar();
        mCalendar.setTime(weekDate);
        mWeekDate = weekDate;
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);

        populateMonth();
    }

    private void populateMonth() {

        /* Label items */
        mItems = new ArrayList<>();
        mItems.add(""); //First item
        mItems.addAll(Arrays.asList(mDays));

        for (UserRef userRef : mGroupUsers) {
            //User name item
            mItems.add(userRef.getShortName());

            //Week days
            for (int i = 0; i < 7; i++) {
                mItems.add("");
            }

        }

        /*for (Map.Entry<String, ArrayList<Shift>> entry : mUserShiftMap.entrySet()) {
            //User name item
            mItems.add(entry.getKey());

            //Week days
            for (int i = 0; i < 7; i++) {
                mItems.add("");
            }
        }*/

        mShiftIterator = mUserShiftMap.entrySet().iterator();
        mTitleHeight = CalendarUtils.getLabelHeight(mDisplayMetrics);
        mNamesWidth = CalendarUtils.getDayCellHeight(mDisplayMetrics);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemType = getItemViewType(position);

        int dayCellHeight = CalendarUtils.getDayCellHeight(mDisplayMetrics);

        switch (itemType) {
            case 0:             //names and first item
                TextView names;
                if (convertView == null) {
                    names = new TextView(mContext);
                    names.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    names.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, dayCellHeight));
                    convertView = names;
                } else {
                    names = (TextView) convertView;
                }

                //TODO set name to display
                if (mShiftIterator.hasNext()) {
                    Map.Entry<String, ArrayList<Shift>> entry = mShiftIterator.next();
                    names.setText(mItems.get(position));
                    mCurrentUid = entry.getKey();

                }
                if(!mShiftIterator.hasNext()){
                    mShiftIterator = mUserShiftMap.entrySet().iterator();
                }

                mCalendar.setTime(mWeekDate);
                mCurrentShiftIndex = 0;
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
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shift, parent, false);
                }
                ItemShiftBinding mItemShiftBinding = DataBindingUtil.bind(convertView);
                mItemShiftBinding.textViewShiftType.setText("");
                convertView.setBackgroundColor(Color.WHITE);

                convertView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, dayCellHeight));

                if (mCurrentShiftIndex < mUserShiftMap.get(mCurrentUid).size() && !mShiftsTypesMap.isEmpty()) {

                    int month = mCalendar.get(Calendar.MONTH);
                    int day = mCalendar.get(Calendar.DAY_OF_MONTH);

                    Shift shift = mUserShiftMap.get(mCurrentUid).get(mCurrentShiftIndex);
                    Calendar calShift = new GregorianCalendar();
                    calShift.setTime(shift.getDate());

                    if (day == calShift.get(Calendar.DAY_OF_MONTH) && month == calShift.get(Calendar.MONTH)) {
                        mCurrentShiftIndex++;
                        //mItemShiftBinding.textViewDayMonth.setText(String.valueOf(day));
                        mItemShiftBinding.textViewDayMonth.setVisibility(View.GONE);
                        ShiftType type = mShiftsTypesMap.get(shift.getType());
                        mItemShiftBinding.textViewShiftType.setText(type.getTag());
                        convertView.setBackgroundColor(type.getColor());

                    }
                }
                mCalendar.add(Calendar.DAY_OF_MONTH, 1);

                return convertView;
            default:
                TextView error = new TextView(mContext);
                error.setBackgroundColor(Color.RED);
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
