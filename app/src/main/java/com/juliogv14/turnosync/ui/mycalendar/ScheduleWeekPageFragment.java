package com.juliogv14.turnosync.ui.mycalendar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.User;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.PageMonthBinding;
import com.juliogv14.turnosync.databinding.PageWeekBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Julio on 18/02/2018.
 * MonthPageFragment
 */

public class ScheduleWeekPageFragment extends Fragment {

    //Workgroup
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String WORKGROUP_USERS_KEY = "workgroupUsers";
    /*private static final String CURRENT_YEAR_KEY = "currentYear";
    private static final String CURRENT_MONTH_KEY = "currentMonth";
    private static final String CURRENT_WEEK_KEY = "currentWeek";*/
    private static final String CURRENT_CALENDAR_KEY = "currentCalendar";
    private final String TAG = this.getClass().getSimpleName();
    protected PageWeekBinding mViewBinding;
    //Firebase
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mFirebaseUser;
    private OnScheduleFragmentInteractionListener mListener;
    private UserWorkgroup mWorkgroup;

    private ArrayList<Map<String, String>> mWorkgroupUsers;
    private ArrayList<ArrayList<Shift>> mUsersShiftList;

    //Month

    private Calendar mCalendar;
    //GridAdapter
    private BaseAdapter mGridAdapter;
    private List<Shift> mShiftList;


    public static ScheduleWeekPageFragment newInstance(UserWorkgroup workgroup, ArrayList<Map<String, Object>> workgroupUsers, Calendar calendar) {
        ScheduleWeekPageFragment f = new ScheduleWeekPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        args.putSerializable(WORKGROUP_USERS_KEY, workgroupUsers);
        args.putSerializable(CURRENT_CALENDAR_KEY,calendar);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnScheduleFragmentInteractionListener) {
            mListener = (OnScheduleFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnScheduleFragmentInteractionListener");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(CURRENT_WORKGROUP_KEY);
            mWorkgroupUsers = (ArrayList<Map<String, String>>) args.getSerializable(WORKGROUP_USERS_KEY);
            mCalendar = (Calendar) args.getSerializable(CURRENT_CALENDAR_KEY);

        }
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = PageWeekBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        //TODO get both month for first and last week
        //String week = CalendarUtils.getMonthString((Context) mListener, mMonth) + cal.getTime();
        String week = ""+mCalendar.getTime()+ "m: " +CalendarUtils.getMonthString((Context) mListener, mCalendar.get(Calendar.MONTH));
        mViewBinding.textViewWeek.setText(week);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mGridAdapter = new WeekAdapter((SupportActivity) mListener, mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.YEAR), 0, metrics, mWorkgroupUsers);

        mViewBinding.gridViewWeek.setAdapter(mGridAdapter);

        Log.d(TAG, "Start Page");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void notifyGridDataSetChanged() {
        mGridAdapter.notifyDataSetChanged();
    }

    public interface OnScheduleFragmentInteractionListener extends OnFragmentInteractionListener {
        void onShiftSelected(Shift shift);
    }

}
