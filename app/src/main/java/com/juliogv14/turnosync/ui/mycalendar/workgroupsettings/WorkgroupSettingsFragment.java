package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.data.viewmodels.UserRefsVM;
import com.juliogv14.turnosync.databinding.FragmentWorkgroupSettingsBinding;
import com.juliogv14.turnosync.ui.drawerlayout.OnFragmentInteractionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class WorkgroupSettingsFragment extends Fragment implements GroupUsersAdapter.UserOnClickListener,
        AddUserDialog.AddUserListener, EditInitialsDialog.EditInitialsListener, WeeklyHoursDialog.WeeklyHoursDialogListener {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Constants
    private static final String WORKGROUP_KEY = "workgroup";
    private static final String WEEKLY_HOURS_KEY = "weeklyHours";

    //Listener
    WorkgroupSettingsListener mListener;

    //Binding
    FragmentWorkgroupSettingsBinding mViewBinding;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;
    private ListenerRegistration mGroupUsersListener;

    //Variables
    private UserWorkgroup mWorkgroup;
    private AtomicLong mWeeklyHours;
    ArrayList<UserRef> mUserList;

    public static WorkgroupSettingsFragment newInstance(UserWorkgroup workgroup, AtomicLong weeklyHours) {
        WorkgroupSettingsFragment f = new WorkgroupSettingsFragment();

        Bundle args = new Bundle();
        args.putParcelable(WORKGROUP_KEY, workgroup);
        args.putSerializable(WEEKLY_HOURS_KEY, weeklyHours);
        f.setArguments(args);
        return f;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WorkgroupSettingsListener) {
            mListener = (WorkgroupSettingsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WorkgroupSettingsListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(WORKGROUP_KEY);
            mWeeklyHours = (AtomicLong) args.getSerializable(WEEKLY_HOURS_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentWorkgroupSettingsBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        mUserList = new ArrayList<>();
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentSwapped(R.string.fragment_workgroupSettings);

        //Init
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        attatchWorkgroupUsersListener();

        updateWeeklyHours();

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager((Context)mListener, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerUsers.setLayoutManager(layoutManager);
        mViewBinding.recyclerUsers.setHasFixedSize(true);
        GroupUsersAdapter recyclerAdapter = new GroupUsersAdapter((Context) mListener, this, mUserList, mWorkgroup.getRole());
        mViewBinding.recyclerUsers.setAdapter(recyclerAdapter);

        mViewBinding.settingsItemShiftTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.swapFragment(R.string.fragment_shiftTypes);
            }
        });

        mViewBinding.settingsItemShiftWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeeklyHoursDialog dialog = WeeklyHoursDialog.newInstance(mWeeklyHours.get());
                dialog.show(getChildFragmentManager(), "weeklyHours");
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mGroupUsersListener != null){
            mGroupUsersListener.remove();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workgroup_settings, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.mutate();
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }

            if(menu.getItem(i).getItemId() == R.id.action_wkSettings_addUser && !mWorkgroup.getRole().equals(UserRoles.MANAGER.toString())){
                menu.getItem(i).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_wkSettings_addUser) {
            AddUserDialog dialog = new AddUserDialog();
            dialog.show(getChildFragmentManager(), "addUser");
            return true;
        }
        return false;
    }

    private void attatchWorkgroupUsersListener() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_users));
        // TODO: 10/07/2018 Change to viewmodel
        /*mGroupUsersListener = workgroupsUsersColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = docChange.getDocument();
                    if (doc.exists()) {
                        UserRef userData = doc.toObject(UserRef.class);

                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                if(userData.isActive()){
                                    mUserList.add(userData);
                                }
                                break;
                            case MODIFIED:
                                //Active modified
                                if(!userData.isActive()){
                                    mUserList.remove(docChange.getOldIndex());
                                } else {
                                    UserRef userRef = mUserList.get(docChange.getNewIndex());
                                    if(TextUtils.equals(userRef.getUid(), userData.getUid())){
                                        mUserList.set(docChange.getNewIndex(), userData);
                                    } else {
                                        mUserList.add(docChange.getNewIndex(), userData);
                                    }

                                }
                                break;
                            case REMOVED:
                                //Removed
                                mUserList.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
                mViewBinding.recyclerUsers.getAdapter().notifyDataSetChanged();
            }
        });*/

        UserRefsVM userRefsVM = ViewModelProviders.of((AppCompatActivity) mListener).get(UserRefsVM.class);
        mGroupUsersListener = userRefsVM.loadUserRefs(workgroupsUsersColl, getString(R.string.data_key_shortname));
        mUserList = (ArrayList<UserRef>) userRefsVM.getUserRefs().getValue();
        userRefsVM.getUserRefs().observe(this, new Observer<List<UserRef>>() {
            @Override
            public void onChanged(@Nullable List<UserRef> userRefs) {
                mViewBinding.recyclerUsers.getAdapter().notifyDataSetChanged();
            }
        });

    }

    private void updateWeeklyHours (){
        String weeklyHours = getString(R.string.wkSettings_label_weeklyHours);
        mViewBinding.settingsItemShiftWeekly.setText(Html.fromHtml(weeklyHours + " <b>" + mWeeklyHours.get() + " h</b>" ));
    }

    //Interfaces implementation
    @Override
    public void onClickRemoveUser(final String uid) {

        new AlertDialog.Builder((Context)mListener).setTitle(getString(R.string.dialog_removeUser_title))
                .setMessage(getString(R.string.dialog_removeUser_message))
                .setPositiveButton(getString(R.string.dialog_removeUser_button_remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CollectionReference workgroupUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups))
                                .document(mWorkgroup.getWorkgroupId()).collection(getString(R.string.data_ref_users));

                        workgroupUsersColl.document(uid).update(getString(R.string.data_key_active), false);
                        mViewBinding.recyclerUsers.getAdapter().notifyDataSetChanged();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_removeUser_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    @Override
    public void onClickEditUser(int pos) {
        UserRef userRef = mUserList.get(pos);
        EditInitialsDialog dialog = EditInitialsDialog.newInstance(userRef);
        dialog.show(getChildFragmentManager(), "editInitials");
    }

    @Override
    public void onInitialsName(UserRef userRef) {
        CollectionReference workgroupUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups))
                .document(mWorkgroup.getWorkgroupId()).collection(getString(R.string.data_ref_users));
        workgroupUsersColl.document(userRef.getUid()).set(userRef);
        mViewBinding.recyclerUsers.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClickAddUser(String email) {

        CollectionReference invitesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_invites));
        Map<String, String> inviteData = new HashMap<>();
        inviteData.put(getString(R.string.data_key_email), email);
        inviteData.put(getString(R.string.data_key_workgroupid), mWorkgroup.getWorkgroupId());
        invitesColl.document(email).set(inviteData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            if (task.getException() != null) {
                                Log.e(TAG, task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    @Override
    public void onSetWeekyHours(long hours) {
        mWeeklyHours.set(hours);
        updateWeeklyHours();
        mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .update(getString(R.string.data_key_weeklyhours), mWeeklyHours.get());
    }

    public interface WorkgroupSettingsListener extends OnFragmentInteractionListener {
        void swapFragment(int fragmentId);
    }
}
