package com.juliogv14.turnosync.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.juliogv14.turnosync.R;

/**
 * Created by Julio on 01/12/2017.
 * SettingsFragment.class
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);

        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences shrPreferences = prefScreen.getSharedPreferences();
        int prefCount = prefScreen.getPreferenceCount();

        for (int i = 0; i < prefCount; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = shrPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
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
