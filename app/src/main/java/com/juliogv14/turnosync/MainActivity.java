package com.juliogv14.turnosync;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private static final int RC_SIGN_IN = 1;

    //Activity views
    private ActivityMainBinding mViewBinding;

    //Auth
    private String mUsername;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    HeaderDrawerBinding mHeaderBinding;
    private ActionBarDrawerToggle mDrawerToggle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        /********************Test buttons**************/
        /*mViewBinding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), LoginActivity.class));
            }
        });

        mViewBinding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
            }
        });*/
        /***************************************************/

        DrawerLayout drawerLayout = (DrawerLayout) mViewBinding.getRoot();
        mDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        mHeaderBinding = DataBindingUtil.bind(mViewBinding.navView.getHeaderView(0));

        getLayoutInflater().inflate(R.layout.content_main, mViewBinding.contentView);
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