package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
import com.juliogv14.turnosync.data.viewmodels.MyCalendarVM;
import com.juliogv14.turnosync.databinding.ItemShiftBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WeekAdapter extends BaseAdapter {
    //Arguments
    private Context mContext;
    private MyCalendarVM mCalendarVM;
    private DisplayMetrics mDisplayMetrics;
    private ArrayList<UserRef> mGroupUsers;
    private Map<String, ArrayList<Shift>> mUserShiftMap;
    private Map<String, ShiftType> mShiftsTypesMap;
    private DateTime mWeekDate;

    //Day item variables
    private List<String> mItems;
    private int mTitleHeight;
    private String[] mDays;
    private DateTime mDisplayDay;

    //Per user shifts
    private Iterator<Map.Entry<String, ArrayList<Shift>>> mShiftIterator;
    private String mCurrentUid;
    private int mCurrentShiftIndex;

    //Selected shift day
    private Shift ownShift;
    private Shift otherShift;

    WeekAdapter(Context c, MyCalendarVM calendarVM, DisplayMetrics metrics, DateTime weekDate, ArrayList<UserRef> groupUsers, Map<String, ArrayList<Shift>> userShifts, Map<String, ShiftType> shiftTypes) {
        mContext = c;
        mCalendarVM = calendarVM;
        mDisplayMetrics = metrics;
        mGroupUsers = groupUsers;
        mUserShiftMap = userShifts;
        mShiftsTypesMap = shiftTypes;

        mWeekDate = weekDate;
        mDisplayDay = new DateTime(mWeekDate);
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);

        populateMonth();
    }

    private void populateMonth() {

        /* Label items */
        mItems = new ArrayList<>();
        mItems.add(""); //First item

        for (int i = 0; i < mDays.length; i++) {
            String day = mDisplayDay.plusDays(i).getDayOfMonth() + " " + mDays[i];
            mItems.add(day);
        }

        for (UserRef userRef : mGroupUsers) {
            //User name item
            mItems.add(userRef.getShortName());

            //Week days
            for (int i = 0; i < DateTimeConstants.DAYS_PER_WEEK; i++) {
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

                if (mShiftIterator.hasNext()) {
                    Map.Entry<String, ArrayList<Shift>> entry = mShiftIterator.next();
                    names.setText(mItems.get(position));
                    mCurrentUid = entry.getKey();

                }
                if(!mShiftIterator.hasNext()){
                    mShiftIterator = mUserShiftMap.entrySet().iterator();
                }

                mDisplayDay = new DateTime(mWeekDate);
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
                    Shift shift = mUserShiftMap.get(mCurrentUid).get(mCurrentShiftIndex);
                    DateTime shiftDate = new DateTime(shift.getDate());

                    //Shfit in date
                    if (mDisplayDay.getDayOfMonth() == shiftDate.getDayOfMonth() && mDisplayDay.getDayOfMonth()== shiftDate.getDayOfMonth()) {
                        mCurrentShiftIndex++;
                        mItemShiftBinding.textViewDayMonth.setVisibility(View.GONE);
                        ShiftType type = mShiftsTypesMap.get(shift.getType());
                        mItemShiftBinding.textViewShiftType.setText(type.getTag());

                        //Check if it is selected
                        if(shift == mCalendarVM.getOwnShift().getValue()){
                            int color = ColorUtils.setAlphaComponent(type.getColor(), 128);
                            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.selected_shift);
                            background.setColor(type.getColor());
                            background.setAlpha(180);
                            int strokeWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mDisplayMetrics);
                            background.setStroke(strokeWidth, Color.BLACK);
                            convertView.setPadding(0,0,0,0);
                            convertView.setBackground(background);
                        } else if (shift == mCalendarVM.getOtherShift().getValue()){
                            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.selected_shift);
                            background.setColor(type.getColor());
                            int strokeWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mDisplayMetrics);
                            background.setStroke(strokeWidth, Color.parseColor("#faf36b"));
                            convertView.setBackground(background);
                        } else {
                            convertView.setBackgroundColor(type.getColor());
                        }
                    }
                }


                mDisplayDay = mDisplayDay.plusDays(1);
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
