package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityWorkgroupSettingsBinding;

import java.util.concurrent.atomic.AtomicLong;

public class WorkgroupSettingsActivity extends AppCompatActivity implements WorkgroupSettingsFragment.WorkgroupSettingsListener, ShiftTypesFragment.OnShiftTypesListener {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Activity views
    ActivityWorkgroupSettingsBinding mViewBinding;

    //Intent data
    private UserWorkgroup mWorkgroup;

    //WorkgroupSettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workgroup_settings);

        //Init
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_workgroup_settings);
        mWorkgroup = getIntent().getParcelableExtra(getString(R.string.data_int_workgroup));
        AtomicLong weeklyHours = (AtomicLong) getIntent().getSerializableExtra(getString(R.string.data_int_hours));
        //ArrayList<UserRef> userlist = getIntent().getParcelableArrayListExtra(getString(R.string.data_int_users));
        //mSettingsFragment = WorkgroupSettingsFragment.newInstance(mWorkgroup, userlist);
        Fragment fragment = WorkgroupSettingsFragment.newInstance(mWorkgroup, weeklyHours);
        displaySelectedScreen(fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(Fragment fragment) {

        //replacing the fragment
        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            if(fragment instanceof WorkgroupSettingsFragment){
                fm.popBackStack("ROOT", 0);
                fm.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
            } else if(fragment instanceof ShiftTypesFragment){
                fm.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .addToBackStack("shifttypes")
                        .commit();
            }

        }
    }

    @Override
    public void onFragmentSwapped(int fragmentId) {
        switch (fragmentId) {
            case R.string.fragment_workgroupSettings:
                getSupportActionBar().setTitle(R.string.fragment_workgroupSettings);
                break;
            case R.string.fragment_shiftTypes:
                getSupportActionBar().setTitle(R.string.fragment_shiftTypes);
                break;
        }
    }

    @Override
    public void swapFragment(int fragmentId) {
        switch (fragmentId) {
            case R.string.fragment_shiftTypes:
                Fragment fragment = ShiftTypesFragment.newInstance(mWorkgroup);
                displaySelectedScreen(fragment);
                break;
        }
    }
}
