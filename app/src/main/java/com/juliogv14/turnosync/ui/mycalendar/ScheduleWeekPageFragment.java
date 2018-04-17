package com.juliogv14.turnosync.ui.mycalendar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.User;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.PageMonthBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julio on 18/02/2018.
 * MonthPageFragment
 */

public class ScheduleWeekPageFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    protected PageMonthBinding mViewBinding;

    //Firebase
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mFirebaseUser;

    private OnScheduleFragmentInteractionListener mListener;

    //Workgroup
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private static final String CURRENT_YEAR_KEY = "currentYear";
    private static final String CURRENT_MONTH_KEY = "currentMonth";
    private static final String WORKGROUP_USERS_KEY = "workgroupUsers";
    private UserWorkgroup mWorkgroup;
    private ArrayList<User> mWorkgroupUsers;

    //Month
    private int mYear;
    private int mMonth;
    //GridAdapter
    private MonthAdapter mGridAdapter;
    private List<Shift> mShiftList;


    public static ScheduleWeekPageFragment newInstance(UserWorkgroup workgroup, ArrayList<User> workgroupUsers, int year, int month) {
        ScheduleWeekPageFragment f = new ScheduleWeekPageFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        args.putInt(CURRENT_YEAR_KEY, year);
        args.putInt(CURRENT_MONTH_KEY, month);
        args.putParcelableArrayList(WORKGROUP_USERS_KEY, workgroupUsers);
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(CURRENT_WORKGROUP_KEY);
            mYear = args.getInt(CURRENT_YEAR_KEY);
            mMonth = args.getInt(CURRENT_MONTH_KEY);
            mWorkgroupUsers = args.getParcelableArrayList(WORKGROUP_USERS_KEY);
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

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mViewBinding.textViewWorkgroup.setText("" + (mMonth + 1));

        displayMonth();
        //DisplayMetrics metrics = getResources().getDisplayMetrics();
        //mGridAdapter = new MonthAdapter((Activity) mListener, mMonth, mYear, metrics, (ArrayList<Shift>) mShiftList);
        //mViewBinding.gridViewCalendar.setAdapter(mGridAdapter);
        Log.d(TAG, "Start Page");


    }

    private void displayMonth() {
        String userID = mFirebaseUser.getUid();
        final int year = mYear;
        final int month = mMonth;

        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        CollectionReference shiftsReference = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                .collection(getString(R.string.data_ref_shifts));
        shiftsReference.whereEqualTo("userID", userID).whereEqualTo("year", year).whereEqualTo("month", month + 1).orderBy("day", Query.Direction.ASCENDING).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mShiftList = task.getResult().toObjects(Shift.class);
                            mGridAdapter = new MonthAdapter((Activity) mListener, month, year, metrics, (ArrayList<Shift>) mShiftList);
                            mViewBinding.gridViewCalendar.setAdapter(mGridAdapter);
                        } else {
                            if (task.getException() != null) {
                                Log.e(TAG, task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    public interface OnScheduleFragmentInteractionListener extends OnFragmentInteractionListener {
        void onShiftSelected(Shift shift);
    }

}
