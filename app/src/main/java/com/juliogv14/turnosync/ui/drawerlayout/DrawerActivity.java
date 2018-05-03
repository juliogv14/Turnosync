package com.juliogv14.turnosync.ui.drawerlayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityDrawerBinding;
import com.juliogv14.turnosync.databinding.HeaderDrawerBinding;
import com.juliogv14.turnosync.ui.account.LoginActivity;
import com.juliogv14.turnosync.ui.mycalendar.MonthPageFragment;
import com.juliogv14.turnosync.ui.mycalendar.ScheduleWeekPageFragment;
import com.juliogv14.turnosync.ui.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Map;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        HomeFragment.OnHomeFragmentInteractionListener,
        MyCalendarFragment.OnCalendarFragmentInteractionListener,
        MonthPageFragment.OnMonthFragmentInteractionListener,
        ScheduleWeekPageFragment.OnScheduleFragmentInteractionListener{

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Activity views
    protected ActivityDrawerBinding mViewBinding;

    //Firebase Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFirebaseUser;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;
    private ListenerRegistration mWorkgroupsListener;
    private ArrayList<UserWorkgroup> mWorkgroupsList;
    private HomeFragment mHomeFragment;

    //drawer
    private DrawerLayout mDrawerLayout;
    private HeaderDrawerBinding mHeaderBinding;
    private ActionBar mToolbar;

    //SavedInstanceState
    private static final String CURRENT_FRAGMENT_KEY = "currentFragment";
    private int mCurrentFragmentID;
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private UserWorkgroup mCurrentWorkgroup;

    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        if (savedInstanceState != null) {
            mCurrentFragmentID = savedInstanceState.getInt(CURRENT_FRAGMENT_KEY);
            mCurrentWorkgroup = savedInstanceState.getParcelable(CURRENT_WORKGROUP_KEY);
        } else {
            mCurrentFragmentID = R.string.fragment_home;
        }

        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_drawer);
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        /*------DRAWER-----*/
        Toolbar toolbar = mViewBinding.toolbar;
        setSupportActionBar(toolbar);
        mToolbar = getSupportActionBar();


        mDrawerLayout = (DrawerLayout) mViewBinding.getRoot();
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar,
                R.string.a11y_navigation_drawer_open,
                R.string.a11y_navigation_drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);


        mDrawerToggle.syncState();
        mViewBinding.viewNav.setNavigationItemSelectedListener(this);
        mHeaderBinding = DataBindingUtil.bind(mViewBinding.viewNav.getHeaderView(0));

        /*-------FIREBASE STATE LISTENER-------*/
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Log.d(TAG, "Authlistener: logged in user " + mFirebaseUser.getDisplayName());
                    //USER logged in
                    onSignedInInitialize(mFirebaseUser);

                } else {
                    Log.d(TAG, "Authlistener: logged out");
                    //No user logged in
                    onSignedOutCleanup();
                    Intent signInIntent = new Intent(getBaseContext(), LoginActivity.class);
                    signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signInIntent);
                    finish();
                }
            }
        };

        mWorkgroupsList = new ArrayList<>();


        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        if (mWorkgroupsListener != null) {
            mWorkgroupsListener.remove();
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT_KEY, mCurrentFragmentID);
        outState.putParcelable(CURRENT_WORKGROUP_KEY, mCurrentWorkgroup);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mCurrentFragmentID == R.string.fragment_home) {
                super.onBackPressed();
            } else {
                displaySelectedScreen(R.string.fragment_home);

            }

        }
    }

    private void onSignedInInitialize(FirebaseUser user) {
        SharedPreferences shrPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = shrPreferences.edit();
        editor.putString(getString(R.string.pref_displayname_key), user.getDisplayName());
        editor.putString(getString(R.string.pref_email_key), user.getEmail());
        editor.apply();
        mHeaderBinding.textViewDisplayName.setText(user.getDisplayName());
        mHeaderBinding.textViewEmail.setText(user.getEmail());

        attatchWorkgroupsListener();

        displaySelectedScreen(mCurrentFragmentID);

    }

    private void onSignedOutCleanup() {
        mHeaderBinding.textViewDisplayName.setText("");
    }

    private void displaySelectedScreen(int itemId) {

        mCurrentFragmentID = itemId;
        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.string.fragment_home:
                mHomeFragment = HomeFragment.newInstance(mWorkgroupsList);
                fragment = mHomeFragment;

                break;
            case R.string.fragment_mycalendar:
                fragment = MyCalendarFragment.newInstance(mCurrentWorkgroup);
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }

    private void attatchWorkgroupsListener() {

        CollectionReference userGroupsRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                .collection(getString(R.string.data_ref_workgroups));

        mWorkgroupsListener = userGroupsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for (DocumentChange docChange : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot document = docChange.getDocument();
                    //TODO Hash key to values/string.xml
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        UserWorkgroup userWorkgroup = new UserWorkgroup(data.get("workgroupID").toString(), data.get("displayname").toString(),
                                data.get("info").toString(), data.get("role").toString());

                        if (mCurrentWorkgroup == null) {
                            mCurrentWorkgroup = userWorkgroup;
                        }

                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                mWorkgroupsList.add(userWorkgroup);
                                break;
                            case MODIFIED:
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Modified, same position
                                    mWorkgroupsList.set(docChange.getOldIndex(), userWorkgroup);
                                } else {
                                    //Modified, differnt position
                                    mWorkgroupsList.remove(docChange.getOldIndex());
                                    mWorkgroupsList.add(docChange.getNewIndex(), userWorkgroup);
                                }
                                break;
                            case REMOVED:
                                //Removed
                                mWorkgroupsList.remove(docChange.getOldIndex());
                                break;
                        }
                        if(mHomeFragment != null){
                            mHomeFragment.notifyGridDataSetChanged();
                        }

                    }
                }
            }
        });
    }

    //Interfaces implementation

    //OnNavigationItemSelectedListener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_item_home:
                displaySelectedScreen(R.string.fragment_home);
                break;
            case R.id.nav_item_calendar:
                displaySelectedScreen(R.string.fragment_mycalendar);
                break;
            case R.id.nav_item_signout:
                mFirebaseAuth.signOut();
                break;
            case R.id.nav_item_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(settingsIntent);
                break;
        }


        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //OnSharedPreferenceChangeListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        /*Update Firebase with new settings*/
        if (TextUtils.equals(key, getString(R.string.pref_displayname_key))) {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            String displayName = sharedPreferences.getString(key, "");
            if (mFirebaseUser != null && !TextUtils.equals(mFirebaseUser.getEmail(), displayName)) {
                mFirebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(sharedPreferences.getString(key, "")).build())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext(), R.string.toast_profile_displayname, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else if (TextUtils.equals(key, getString(R.string.pref_email_key))) {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            String email = sharedPreferences.getString(key, "");
            if (mFirebaseUser != null && !TextUtils.equals(mFirebaseUser.getEmail(), email)) {
                mFirebaseUser.updateEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext().getApplicationContext(), R.string.toast_profile_email, Toast.LENGTH_SHORT).show();
                                } else {
                                    if (task.getException() != null) {
                                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        });
            }
        }
    }

    //OnFragmentInteractionListener
    @Override
    public void onFragmentCreated(int fragmentId) {
        switch (fragmentId) {
            case R.string.fragment_home:
                mToolbar.setTitle(R.string.fragment_home);
                mViewBinding.viewNav.setCheckedItem(R.id.nav_item_home);
                break;
            case R.string.fragment_mycalendar:
                mToolbar.setTitle(mCurrentWorkgroup.getDisplayname());
                mViewBinding.viewNav.setCheckedItem(R.id.nav_item_calendar);
                break;
        }
    }

    @Override
    public void onFragmentSwapped(int fragmentId) {
        mToolbar.setTitle(mCurrentWorkgroup.getWorkgroupID());
        displaySelectedScreen(fragmentId);
    }

    //OnHomeFragmentInteractionListener
    @Override
    public void onWorkgroupSelected(UserWorkgroup workgroup) {
        Toast.makeText(this, "WK: uid: " + workgroup.getWorkgroupID(), Toast.LENGTH_SHORT).show();
        mCurrentWorkgroup = workgroup;
        displaySelectedScreen(R.string.fragment_mycalendar);

    }

    //OnCalendarFragmentInteractionListener


    //OnMonthFragmentInteractionListener
    @Override
    public void onShiftSelected(Shift shift) {

    }
}