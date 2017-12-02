package com.juliogv14.turnosync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.juliogv14.turnosync.settings.SettingsActivity;
import com.juliogv14.turnosync.databinding.ActivityDrawerBinding;
import com.juliogv14.turnosync.databinding.HeaderDrawerBinding;

import java.lang.reflect.Field;

public abstract class BaseDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    private static final int RC_SIGN_IN = 1;

    //Activity views
    protected ActivityDrawerBinding mViewBinding;

    //Auth
    private String mUsername;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private HeaderDrawerBinding mHeaderBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_drawer);

        /*------DRAWER-----*/
        mDrawerLayout = (DrawerLayout) mViewBinding.getRoot();
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.a11y_navigation_drawer_open,
                R.string.a11y_navigation_drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //Increase slide margin from edge
        try {
            Field mDragger = mDrawerLayout.getClass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper dragHelper = (ViewDragHelper) mDragger.get(mDrawerLayout);
            Field mEdgeSize = dragHelper.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt((dragHelper));
            mEdgeSize.setInt(dragHelper, edge * 8);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        mDrawerToggle.syncState();
        mViewBinding.viewNav.setNavigationItemSelectedListener(this);
        mHeaderBinding = DataBindingUtil.bind(mViewBinding.viewNav.getHeaderView(0));

        /*-------FIREBASE STATE LISTENER-------*/
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Authlistener: logged in user " + user.getDisplayName());
                    //User logged in
                    onSignedInInitialize(user);

                } else {
                    Log.d(TAG, "Authlistener: logged out");
                    //No user logged in
                    onSignedOutCleanup();
                    Intent signInIntent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            }
        };

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            onSignedInInitialize(currentUser);
        }
        Log.d(TAG, "AddAuthStateListener");
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            Log.d(TAG, "RemoveAuthStateListener");
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewBinding.viewNav.setCheckedItem(R.id.nav_item_main);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Signed in successfully");
                Toast.makeText(this, R.string.toast_sign_in_successfully, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Signed in canceled");
                Toast.makeText(this, R.string.toast_sign_in_canceled, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_item_main:
                break;
            case R.id.nav_item_signout:
                mFirebaseAuth.signOut();
                break;
            case R.id.nav_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }


        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, getString(R.string.pref_displayname_key))) {
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            if (user != null) {
                user.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(sharedPreferences.getString(key, null)).build())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(BaseDrawerActivity.this, R.string.toast_displayname_update, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void onSignedInInitialize(FirebaseUser user) {
        mUsername = user.getDisplayName();
        String displaytext = mUsername + ":" + user.getUid();
        SharedPreferences shrPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = shrPreferences.edit();
        editor.putString(getString(R.string.pref_displayname_key), user.getDisplayName());
        editor.putString(getString(R.string.pref_email_key), user.getEmail());
        editor.apply();
        mHeaderBinding.textViewDisplayName.setText(user.getDisplayName());
        mHeaderBinding.textViewEmail.setText(user.getEmail());

    }

    private void onSignedOutCleanup() {
        mUsername = "";
        mHeaderBinding.textViewDisplayName.setText("");
    }

}