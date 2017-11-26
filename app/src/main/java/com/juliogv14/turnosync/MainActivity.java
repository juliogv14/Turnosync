package com.juliogv14.turnosync;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.juliogv14.turnosync.databinding.ActivityMainBinding;
import com.juliogv14.turnosync.databinding.HeaderDrawerBinding;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = this.getClass().getSimpleName();
    private static final int RC_SIGN_IN = 1;

    //Activity views
    private ActivityMainBinding mViewBinding;

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
        setContentView(R.layout.activity_main);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        /*------DRAWER-----*/
        mDrawerLayout = (DrawerLayout) mViewBinding.getRoot();
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
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
                    Log.d(TAG, "Authlistener: logged in user" + user.getDisplayName());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewBinding.viewNav.setCheckedItem(R.id.nav_item_main);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Signed canceled", Toast.LENGTH_SHORT).show();
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
        }


        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onSignedInInitialize(FirebaseUser user) {
        mUsername = user.getDisplayName();
        String displaytext = mUsername + ":" + user.getUid();
        mHeaderBinding.textViewDisplayName.setText(user.getDisplayName());
        mHeaderBinding.textViewEmail.setText(user.getEmail());

    }

    private void onSignedOutCleanup() {
        mUsername = "";
        mHeaderBinding.textViewDisplayName.setText("");
    }

}