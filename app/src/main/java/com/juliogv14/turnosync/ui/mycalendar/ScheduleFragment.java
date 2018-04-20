package com.juliogv14.turnosync.ui.mycalendar;

import android.app.Activity;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.juliogv14.turnosync.data.GlobalWorkgroup;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.User;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentScheduleBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
