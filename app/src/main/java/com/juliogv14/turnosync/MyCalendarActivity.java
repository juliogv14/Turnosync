package com.juliogv14.turnosync;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarActivity
 */

public class MyCalendarActivity extends DrawerActivity {

    private FirebaseFirestore mFirebaseFirestore;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getLayoutInflater().inflate(R.layout.content_mycalendar, super.mViewBinding.contentFrame);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "Start MyCalendarActivity");

    }


}
