package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityWorkgroupSettingsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkgroupSettingsActivity extends AppCompatActivity implements WorkgroupSettingsFragment.WorkgroupSettingsListener, GroupUsersAdapter.UserOnClickHandler, AddUserDialog.AddUserListener {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Activity views
    ActivityWorkgroupSettingsBinding mViewBinding;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;

    //Intent data
    private UserWorkgroup mWorkgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workgroup_settings);
        setTitle("Workgroup settings");

        //Init
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_workgroup_settings);
        mWorkgroup = getIntent().getParcelableExtra(getString(R.string.data_int_workgroup));
        ArrayList<String> userlist = getIntent().getStringArrayListExtra(getString(R.string.data_int_users));

        Fragment settings = WorkgroupSettingsFragment.newInstance(mWorkgroup, userlist);
        displaySelectedScreen(settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workgroup_settings, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.mutate();
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_wkSettings_addUser) {
            AddUserDialog dialog = new AddUserDialog();
            dialog.show(this.getSupportFragmentManager(), "addUser");
            return true;
        }
        return false;
    }

    private void displaySelectedScreen(Fragment fragment) {

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }

    @Override
    public void onDialogPositiveClick(String email) {
        CollectionReference invitesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_invites));

        Map<String, String> inviteData = new HashMap<>();
        inviteData.put(getString(R.string.data_key_email), email);
        inviteData.put(getString(R.string.data_key_workgroupid), mWorkgroup.getWorkgroupId());
        invitesColl.add(inviteData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {

                        } else {
                            if (task.getException() != null) {
                                Log.e(TAG, task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    @Override
    public void onClickUser(String uid) {
        Toast.makeText(this, "User uid: " + uid, Toast.LENGTH_SHORT).show();
    }
}
