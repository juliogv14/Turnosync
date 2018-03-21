package com.juliogv14.turnosync.ui.drawerlayout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Workgroup;
import com.juliogv14.turnosync.databinding.ContentMycalendarBinding;
import com.juliogv14.turnosync.ui.mycalendar.MonthPageFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarFragment.java
 */

public class MyCalendarFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private OnFragmentInteractionListener mListener;
    private ContentMycalendarBinding mViewBinding;

    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private Workgroup mWorkgroup;

    public static MyCalendarFragment newInstance(Workgroup workgroup) {
        MyCalendarFragment f = new MyCalendarFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(CURRENT_WORKGROUP_KEY);
        }
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = ContentMycalendarBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentCreated(R.id.nav_item_calendar);
        Calendar cal = new GregorianCalendar();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        PagerAdapter mPagerAdapter = new MonthSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), year, month);
        mViewBinding.viewPagerMonths.setAdapter(mPagerAdapter);
        mViewBinding.viewPagerMonths.setCurrentItem(5);

        Log.d(TAG, "Start MyCalendarFragment");


    }

    private class MonthSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int year, month;

        MonthSlidePagerAdapter(FragmentManager fm, int year, int month) {
            super(fm);
            this.year = year;
            this.month = month;

        }

        @Override
        public Fragment getItem(int position) {
            int offMonth = month + position - 5;
            int offYear = year;
            if (offMonth < 0) {
                offYear = offYear - 1;
                offMonth = 12 - Math.abs(offMonth);
            }

            return MonthPageFragment.newInstance(mWorkgroup, offYear, offMonth);
        }

        @Override
        public int getCount() {
            return 12;
        }
    }


}
