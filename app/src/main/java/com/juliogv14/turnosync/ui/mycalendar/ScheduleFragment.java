package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentScheduleBinding;

/**
 * Created by Julio on 11/04/2018.
 * ScheduleFragment
 */

public class ScheduleFragment extends Fragment {

    //Binding
    private FragmentScheduleBinding mViewBinding;

    //Listener DrawerActivity
    private OnFragmentInteractionListener mListener;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;

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
        return mViewBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentCreated(R.id.nav_item_calendar);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
