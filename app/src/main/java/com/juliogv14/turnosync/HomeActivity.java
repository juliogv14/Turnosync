package com.juliogv14.turnosync;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.databinding.ContentHomeBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julio on 26/11/2017.
 * HomeActivity.class
 */

public class HomeActivity extends DrawerActivity {

    private final String TAG = this.getClass().getSimpleName();

    protected ContentHomeBinding mViewBinding;

    private FirebaseFirestore mFirebaseFirestore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.content_home, super.mViewBinding.contentFrame);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.content_home);
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "Start HomeActivity");
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
