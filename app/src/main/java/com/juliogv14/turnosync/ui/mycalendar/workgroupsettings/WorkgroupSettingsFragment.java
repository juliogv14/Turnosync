package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentWorkgroupSettingsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkgroupSettingsFragment extends Fragment {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Constants
    private static final String WORKGROUP_KEY = "workgroup";
    private static final String USER_LIST = "userList";

    //Listener
    WorkgroupSettingsListener mListener;

    //Binding
    FragmentWorkgroupSettingsBinding mViewBinding;

    //Intent data
    private UserWorkgroup mWorkgroup;

    ArrayList<String> mUserList;

    public static WorkgroupSettingsFragment newInstance(UserWorkgroup workgroup, ArrayList<String> userList) {
        WorkgroupSettingsFragment f = new WorkgroupSettingsFragment();

        Bundle args = new Bundle();
        args.putParcelable(WORKGROUP_KEY, workgroup);
        args.putStringArrayList(USER_LIST, userList);
        f.setArguments(args);
        return f;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WorkgroupSettingsListener) {
            mListener = (WorkgroupSettingsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WorkgroupSettingsListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(WORKGROUP_KEY);
            mUserList = args.getStringArrayList(USER_LIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentWorkgroupSettingsBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager((Context)mListener, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerUsers.setLayoutManager(layoutManager);
        mViewBinding.recyclerUsers.setHasFixedSize(true);
        GroupUsersAdapter recyclerAdapter = new GroupUsersAdapter((Context)mListener, mUserList);
        mViewBinding.recyclerUsers.setAdapter(recyclerAdapter);

    }

    public interface WorkgroupSettingsListener {

    }
}
