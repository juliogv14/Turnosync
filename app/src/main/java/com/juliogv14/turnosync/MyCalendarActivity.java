package com.juliogv14.turnosync;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarActivity
 */

public class MyCalendarActivity extends BaseDrawerActivity {

    private FirebaseFirestore mFirebaseFirestore;
    private CollectionReference mUsersReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.content_home, super.mViewBinding.contentView);

        mFirebaseFirestore = FirebaseFirestore.getInstance();

        testData();

    }


    private void testData() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("displayName", "trabajo");
        mFirebaseFirestore.collection("workgroups").document("groupID").set(testData);
    }
}
