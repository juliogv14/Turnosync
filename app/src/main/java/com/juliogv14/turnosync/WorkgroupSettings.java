package com.juliogv14.turnosync;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.data.User;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityWorkgroupSettingsBinding;
import com.juliogv14.turnosync.utils.FormUtils;

import java.util.HashMap;
import java.util.Map;

public class WorkgroupSettings extends AppCompatActivity {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Activity views
    ActivityWorkgroupSettingsBinding mViewBinding;

    //Firebase Auth
    private FirebaseAuth mFirebaseAuth;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;

    //Workgroup
    private UserWorkgroup mWorkgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workgroup_settings);
        setTitle("Workgroup settings");

        //Init
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_workgroup_settings);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mWorkgroup = getIntent().getParcelableExtra(getString(R.string.data_int_workgroup));


        mViewBinding.adduser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAddUser();
            }
        });

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

    private void attemptAddUser() {
        mViewBinding.editTextLayoutEmail.setError(null);
        String email = mViewBinding.editTextEmail.getText().toString();

        Boolean cancel = false;

        /*Check for a valid email address.*/
        if (TextUtils.isEmpty(email)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.form_error_field_required));
            cancel = true;
        } else if (!FormUtils.isEmailValid(email)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            cancel = true;
        }

        if (cancel) {
            mViewBinding.editTextLayoutEmail.requestFocus();
        } else {
            CollectionReference invitesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_invites));

            Map<String, String> inviteData = new HashMap<>();
            inviteData.put(getString(R.string.data_key_email), email);
            inviteData.put(getString(R.string.data_key_workgroupid), mWorkgroup.getWorkgroupId());
            invitesColl.add(inviteData)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){

                            } else {
                                if (task.getException() != null) {
                                    Log.e(TAG, task.getException().getMessage());
                                }
                            }
                        }
                    });


        }
    }
}
