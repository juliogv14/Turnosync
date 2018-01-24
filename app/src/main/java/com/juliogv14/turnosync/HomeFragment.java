package com.juliogv14.turnosync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.databinding.ContentHomeBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julio on 26/11/2017.
 * HomeFragment.class
 */

public class HomeFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    protected ContentHomeBinding mViewBinding;

    private FirebaseFirestore mFirebaseFirestore;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    //Create objects obj = new obj()
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = ContentHomeBinding.inflate(inflater, container, false);
        mListener.onDrawerItemSelected(R.id.nav_item_home);
        return mViewBinding.getRoot();
    }

    //View setup, same as onCreate
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "Start HomeFragment");
        mViewBinding.floatingButtonNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testData();
            }
        });
    }

    private void testData() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> testData = new HashMap<>();
            String workgroupID = "groupID";
            testData.put("displayName", "trabajo");

            Map<String, Object> leveldata = new HashMap<>();
            leveldata.put("level", 0);
            mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(workgroupID).set(testData);
            mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(user.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(workgroupID).set(leveldata);
            Log.d(TAG, "testData floating button");
        }
    }

}
