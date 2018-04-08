package com.juliogv14.turnosync.ui.drawerlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentMycalendarBinding;
import com.juliogv14.turnosync.ui.mycalendar.MonthPageFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarFragment.java
 */

public class MyCalendarFragment extends Fragment {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Constants
    private final int QUERY_MONTH_NUMBER = 12;

    //Listener DrawerActivity
    private OnFragmentInteractionListener mListener;
    //Binding
    private FragmentMycalendarBinding mViewBinding;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;

    //Firebase Auth
    private FirebaseAuth mFirebaseAuth;


    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private UserWorkgroup mWorkgroup;

    public static MyCalendarFragment newInstance(UserWorkgroup workgroup) {
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
        mViewBinding = FragmentMycalendarBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentCreated(R.id.nav_item_calendar);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        Calendar cal = new GregorianCalendar();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        PagerAdapter mPagerAdapter = new MonthSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), year, month);

        mViewBinding.viewPagerMonths.setAdapter(mPagerAdapter);
        mViewBinding.viewPagerMonths.setCurrentItem(5);


        Log.d(TAG, "Start MyCalendarFragment");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mycalendar, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.mutate();
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_mycalendar_schedule) {
            Toast.makeText((Context) mListener, "Schedule", Toast.LENGTH_SHORT).show();


            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
            Shift shift = new Shift("M", currentUser.getUid(), 2018, 4, 12, "18:00", "20:00");
            mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(currentUser.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                    .collection(getString(R.string.data_ref_shifts)).add(shift);

        }
        return true;
    }

    private class MonthSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int year, month;
        private List<List<Shift>> data;

        MonthSlidePagerAdapter(FragmentManager fm, int year, int month) {
            super(fm);
            this.year = year;
            this.month = month;
            data = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {

            int offMonth = month - (QUERY_MONTH_NUMBER / 2 - 1) + position;
            int offYear = year;
            if (offMonth < 0) {
                offYear = offYear - 1;
                offMonth = QUERY_MONTH_NUMBER - Math.abs(offMonth);
            }
            /*ArrayList<Shift> shiftArrayList = new ArrayList<>();
                    shiftArrayList.addAll(data.get(position));*/
            MonthPageFragment pageFragment = MonthPageFragment.newInstance(mWorkgroup, offYear, offMonth);
            //pageFragment.notifyGridDataSetChanged();
            return pageFragment;
        }

        @Override
        public int getCount() {
            return 12;
        }

    }

    //TODO pre query shifts before displaying
    public void queryShiftData(int year, int month) {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        final List<List<Shift>> data = new ArrayList<>();
        if (user != null) {

            CollectionReference shiftsColl = mFirebaseFirestore
                    .collection(getString(R.string.data_ref_users)).document(user.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                    .collection(getString(R.string.data_ref_shifts));
            final DisplayMetrics metrics = getResources().getDisplayMetrics();

            for (int pos = 0; pos < QUERY_MONTH_NUMBER; pos++) {

                int offMonth = month - QUERY_MONTH_NUMBER / 2 - 1 + pos;
                int offYear = year;
                if (offMonth < 0) {
                    offYear = offYear - 1;
                    offMonth = QUERY_MONTH_NUMBER - Math.abs(offMonth);
                }

                shiftsColl.whereEqualTo("year", offYear).whereEqualTo("month", offMonth + 1).orderBy("day", Query.Direction.ASCENDING).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    data.add(task.getResult().toObjects(Shift.class));
                                    //When all months are queried
                                } else {
                                    if (task.getException() != null) {
                                        Log.e(TAG, task.getException().getMessage());
                                    }
                                }
                            }
                        });

            }


        }
    }


}
