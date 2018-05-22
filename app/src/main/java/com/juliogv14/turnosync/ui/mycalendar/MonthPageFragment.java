package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.PageMonthBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Julio on 18/02/2018.
 * MonthPageFragment
 */

public class MonthPageFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    protected PageMonthBinding mViewBinding;

    //Firebase
    private OnMonthFragmentInteractionListener mListener;

    //Workgroup
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String CURRENT_MONTH_DATE_KEY = "currentMonthDate";
    private static final String MONTH_SHIFT_LIST_KEY = "shiftList";

    //Month
    private Date mMonthDate;

    //GridAdapter
    private BaseAdapter mGridAdapter;
    private ArrayList<Shift> mShiftList;


    public static MonthPageFragment newInstance(UserWorkgroup workgroup, Date monthCalendar, ArrayList<Shift> shiftArrayList) {
        MonthPageFragment f = new MonthPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        args.putLong(CURRENT_MONTH_DATE_KEY, monthCalendar.getTime());
        args.putParcelableArrayList(MONTH_SHIFT_LIST_KEY, shiftArrayList);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMonthFragmentInteractionListener) {
            mListener = (OnMonthFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMonthFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMonthDate = new Date(args.getLong(CURRENT_MONTH_DATE_KEY));
            mShiftList = args.getParcelableArrayList(MONTH_SHIFT_LIST_KEY);
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
        cal.add(Calendar.DAY_OF_MONTH,7);

        mViewBinding.textViewMonth.setText(CalendarUtils.getMonthString((Context) mListener, cal.get(Calendar.MONTH)));

        cal.setTime(mMonthDate);

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        mGridAdapter = new MonthAdapter((SupportActivity) mListener, mMonthDate, metrics, mShiftList);

        mViewBinding.gridViewCalendar.setAdapter(mGridAdapter);

    }

    public void notifyGridDataSetChanged(){
        ((SupportActivity)mListener).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGridAdapter.notifyDataSetChanged();
            }
        });
    }

    public interface OnMonthFragmentInteractionListener extends OnFragmentInteractionListener {
        void onShiftSelected(Shift shift);
    }

}
