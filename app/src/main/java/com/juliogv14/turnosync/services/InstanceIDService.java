package com.juliogv14.turnosync.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.juliogv14.turnosync.R;

public class InstanceIDService extends FirebaseInstanceIdService {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        saveToken(token);
    }

    private void saveToken(String token){
        SharedPreferences shrPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = shrPreferences.edit();
        editor.putString(getString(R.string.data_key_token), token);
        editor.apply();
    }
}
