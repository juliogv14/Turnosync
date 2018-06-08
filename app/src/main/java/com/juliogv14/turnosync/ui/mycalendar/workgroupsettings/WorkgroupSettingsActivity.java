package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityWorkgroupSettingsBinding;

public class WorkgroupSettingsActivity extends AppCompatActivity implements WorkgroupSettingsFragment.WorkgroupSettingsListener{

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Activity views
    ActivityWorkgroupSettingsBinding mViewBinding;

    //Intent data
    private UserWorkgroup mWorkgroup;

    WorkgroupSettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workgroup_settings);
        setTitle("Workgroup settings");

        //Init
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_workgroup_settings);
        mWorkgroup = getIntent().getParcelableExtra(getString(R.string.data_int_workgroup));
        //ArrayList<UserRef> userlist = getIntent().getParcelableArrayListExtra(getString(R.string.data_int_users));
        //mSettingsFragment = WorkgroupSettingsFragment.newInstance(mWorkgroup, userlist);
        mSettingsFragment = WorkgroupSettingsFragment.newInstance(mWorkgroup);
        displaySelectedScreen(mSettingsFragment);
    }

    private void displaySelectedScreen(Fragment fragment) {

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }
}
