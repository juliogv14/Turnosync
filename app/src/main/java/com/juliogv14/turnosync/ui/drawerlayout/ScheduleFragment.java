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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.User;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentScheduleBinding;
import com.juliogv14.turnosync.ui.mycalendar.ScheduleWeekPageFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Julio on 11/04/2018.
 * ScheduleFragment
 */

public class ScheduleFragment extends Fragment {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Binding
    private FragmentScheduleBinding mViewBinding;

    //Listener DrawerActivity
    private OnFragmentInteractionListener mListener;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;
    private ListenerRegistration mGroupUsersListener;
    private ArrayList<User> mGroupUsers;
    //Firebase Auth
    private FirebaseAuth mFirebaseAuth;

    //Current workgroup
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private UserWorkgroup mWorkgroup;


    public ScheduleFragment() {

    }

    public static ScheduleFragment newInstance(UserWorkgroup workgroup) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWorkgroup = getArguments().getParcelable(CURRENT_WORKGROUP_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentScheduleBinding.inflate(inflater, container, false);
        mGroupUsers = new ArrayList<>();
        setHasOptionsMenu(true);
        return mViewBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentCreated(R.string.fragment_mycalendar);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        Calendar cal = new GregorianCalendar();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        PagerAdapter mPagerAdapter = new WeekSlidePagerAdapter(((AppCompatActivity) mListener).getSupportFragmentManager(), year, month);
        mViewBinding.viewPagerWeekSchedule.setAdapter(mPagerAdapter);

        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                .collection(getString(R.string.data_ref_users));

        mGroupUsersListener = workgroupsUsersColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot doc = docChange.getDocument();
                    if(doc.exists()){
                        User user = doc.toObject(User.class);

                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                mGroupUsers.add(user);
                                break;
                            case MODIFIED:
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Modified, same position
                                    mGroupUsers.set(docChange.getOldIndex(), user);
                                } else {
                                    //Modified, differnt position
                                    mGroupUsers.remove(docChange.getOldIndex());
                                    mGroupUsers.add(docChange.getNewIndex(), user);
                                }
                                break;
                            case REMOVED:
                                //Removed
                                mGroupUsers.remove(docChange.getOldIndex());
                                break;
                        }

                    }
                }
            }
        });




    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_schedule, menu);
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
        int itemId = item.getItemId();
        if (itemId == R.id.action_schedule_switch) {

            mListener.onFragmentSwapped(R.string.fragment_mycalendar);
            /*FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
            Shift shift = new Shift("M", currentUser.getUid(), 2018, 4, 12, "18:00", "20:00");
            mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(currentUser.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                    .collection(getString(R.string.data_ref_shifts)).add(shift);*/
            return true;

        } else if (itemId == R.id.action_schedule_settings) {
            Toast.makeText((Context) mListener, "Settings", Toast.LENGTH_SHORT).show();


        }
        return true;
    }

    private class WeekSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int year, month;

        WeekSlidePagerAdapter(FragmentManager fm, int year, int month) {
            super(fm);
            this.year = year;
            this.month = month;
        }

        @Override
        public Fragment getItem(int position) {
            ScheduleWeekPageFragment pageFragment = ScheduleWeekPageFragment.newInstance(mWorkgroup, mGroupUsers, year , month);
            return pageFragment;
        }

        @Override
        public int getCount() {
            return 0;
        }

    }

}
