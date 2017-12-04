package com.juliogv14.turnosync.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.juliogv14.turnosync.R;

/**
 * Created by Julio on 01/12/2017.
 * SettingsFragment.class
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    FirebaseAuth mFirebaseAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);
        mFirebaseAuth = FirebaseAuth.getInstance();
        PreferenceScreen prefScreen = getPreferenceScreen();

        int prefCount = prefScreen.getPreferenceCount();
        for (int i = 0; i < prefCount; i++) {
            Preference p = prefScreen.getPreference(i);

            setPreferenceInCategory(p);
        }

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
        /*Update Firebase with new settings*/
        if (TextUtils.equals(key, getString(R.string.pref_displayname_key))) {
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            if (user != null) {
                user.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(sharedPreferences.getString(key, "")).build())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getContext(), R.string.toast_profile_displayname, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else if (TextUtils.equals(key, getString(R.string.pref_email_key))) {
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            if (user != null) {
                user.updateEmail(sharedPreferences.getString(key, ""))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), R.string.toast_profile_email, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setPreferenceInCategory(Preference p) {

        SharedPreferences shrPreferences =
                getPreferenceScreen().getSharedPreferences();

        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            int catPrefCount = cat.getPreferenceCount();
            for (int j = 0; j < catPrefCount; j++) {
                Preference pref = cat.getPreference(j);
                if (!(pref instanceof CheckBoxPreference)) {

                    String stringValue = shrPreferences.getString(pref.getKey(), "");
                    setPreferenceSummary(pref, stringValue);
                }
            }
        } else {
            if (!(p instanceof CheckBoxPreference)) {

                String stringValue = shrPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, stringValue);
            }
        }


    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            //Look for selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // Set summary from the value as a string
            preference.setSummary(stringValue);
        }

    }
}
