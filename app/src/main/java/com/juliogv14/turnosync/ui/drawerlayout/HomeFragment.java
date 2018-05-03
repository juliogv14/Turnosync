package com.juliogv14.turnosync.ui.drawerlayout;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.juliogv14.turnosync.CreateWorkgroupDialog;
import com.juliogv14.turnosync.OnFragmentInteractionListener;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserLevels;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentHomeBinding;
import com.juliogv14.turnosync.databinding.ItemWorkgroupBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Julio on 26/11/2017.
 * HomeFragment.class
 */

public class HomeFragment extends Fragment
        implements CreateWorkgroupDialog.CreateWorkgroupListener {
    //Key constants
    private static final String WORKGROUP_LIST_KEY = "workgroupList";
    //Log TAG
    private final String TAG = this.getClass().getSimpleName();
    //Data binding
    protected FragmentHomeBinding mViewBinding;
    ToolbarActionModeCallback tb;
    //Firebase
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mFirebaseUser;
    private ListenerRegistration mWorkgroupsListener;
    //GridAdapter
    private GroupItemsAdapter mGridAdapter;
    private ArrayList<UserWorkgroup> mWorkgroupsList;
    private ActionMode mActionMode;
    private UserWorkgroup mSelectedWorkgroup;
    private OnHomeFragmentInteractionListener mListener;

    public static HomeFragment newInstance(ArrayList<UserWorkgroup> workgroupList) {
        HomeFragment f = new HomeFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelableArrayList(WORKGROUP_LIST_KEY, workgroupList);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHomeFragmentInteractionListener");
        }
    }

    //Create objects obj = new obj()
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroupsList = args.getParcelableArrayList(WORKGROUP_LIST_KEY);
        }

    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentHomeBinding.inflate(inflater, container, false);
        mViewBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    //View setup, same as onCreate
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mGridAdapter = new GroupItemsAdapter((Activity) mListener, R.layout.fragment_home, mWorkgroupsList);

        mViewBinding.gridViewGroupDisplay.setAdapter(mGridAdapter);
        mListener.onFragmentCreated(R.string.fragment_home);
        //attatchWorkgroupsListener();


        mViewBinding.floatingButtonNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //testData();
                CreateWorkgroupDialog dialog = new CreateWorkgroupDialog();
                dialog.setTargetFragment(HomeFragment.this, 1);
                dialog.show(((AppCompatActivity) mListener).getSupportFragmentManager(), "cwk");
            }
        });

        mViewBinding.gridViewGroupDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                UserWorkgroup wk = mWorkgroupsList.get(position);
                if (wk.isSelected()) {
                    handleSelectedWorkgroup(wk);
                } else {
                    mListener.onWorkgroupSelected(wk);
                }


            }
        });

        mViewBinding.gridViewGroupDisplay.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                UserWorkgroup wk = mWorkgroupsList.get(position);
                handleSelectedWorkgroup(wk);
                return true;
            }
        });
        Log.d(TAG, "Start HomeFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        mGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDialogPositiveClick(String name, String description) {
        if (mFirebaseUser != null) {
            //Database References
            String userUID = mFirebaseUser.getUid();
            CollectionReference globalWorkgroupsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups));
            CollectionReference userWorkgroupsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users))
                    .document(userUID).collection(getString(R.string.data_ref_workgroups));

            //Global workgroup list
            DocumentReference globalWorkgroupRef = globalWorkgroupsColl.document();
            Map<String, String> workgroupData = new HashMap<>();
            workgroupData.put("workgroupID", globalWorkgroupRef.getId());
            workgroupData.put("displayname", name);
            workgroupData.put("info", description);
            globalWorkgroupRef.set(workgroupData);

            Map<String, String> userData = new HashMap<>();
            userData.put("uid", mFirebaseUser.getUid());
            globalWorkgroupRef.collection(getString(R.string.data_ref_users))
                    .document(mFirebaseUser.getUid()).set(userData);

            //Personal workgroup list
            DocumentReference userWorkgroupRef = userWorkgroupsColl.document(globalWorkgroupRef.getId());
            UserWorkgroup userWorkgroup = new UserWorkgroup(globalWorkgroupRef.getId(), name, description, UserLevels.MANAGER.toString());
            userWorkgroupRef.set(userWorkgroup);

            Log.d(TAG, "Create workgroup dialog return");
        }
    }

    private void handleSelectedWorkgroup(UserWorkgroup workgroup) {

        if (!workgroup.isSelected()) {
            if (mSelectedWorkgroup != null) {
                mSelectedWorkgroup.setSelected(false);
            }
            mSelectedWorkgroup = workgroup;
            mSelectedWorkgroup.setSelected(true);

            tb = new ToolbarActionModeCallback((Context) mListener, mGridAdapter, mSelectedWorkgroup);
            if (mActionMode == null) {
                mActionMode = ((AppCompatActivity) mListener)
                        .startSupportActionMode(tb);
            }
            mActionMode.setTitle(workgroup.getDisplayname() + " selected");


        } else {
            mActionMode.finish();
        }
    }

    public void notifyGridDataSetChanged() {
        if (mGridAdapter != null) {
            mGridAdapter.notifyDataSetChanged();
        }
    }

    public interface OnHomeFragmentInteractionListener extends OnFragmentInteractionListener {
        void onWorkgroupSelected(UserWorkgroup workgroup);
    }

    private class GroupItemsAdapter extends ArrayAdapter<UserWorkgroup> {

        private ItemWorkgroupBinding itemBinding;


        GroupItemsAdapter(@NonNull Context context, int resource, @NonNull List<UserWorkgroup> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_workgroup, parent, false);
            }
            itemBinding = DataBindingUtil.bind(convertView);
            int size = mViewBinding.gridViewGroupDisplay.getRequestedColumnWidth();
            convertView.setLayoutParams(new GridView.LayoutParams(size, size));

            UserWorkgroup workgroup = getItem(position);
            if (workgroup != null) {
                //TODO temp level display
                String display = workgroup.getDisplayname() + "-" + workgroup.getRole();
                itemBinding.textViewGroupName.setText(display);
                itemBinding.textViewGroupId.setText(workgroup.getWorkgroupID());
            }
            return convertView;
        }
    }

    public class ToolbarActionModeCallback implements ActionMode.Callback {

        UserWorkgroup mWorkgroup;
        private Context mContext;
        private GroupItemsAdapter mGroupsAdapter;

        ToolbarActionModeCallback(Context mContext, GroupItemsAdapter mGroupsAdapter, UserWorkgroup workgroup) {
            this.mContext = mContext;
            this.mGroupsAdapter = mGroupsAdapter;
            this.mWorkgroup = workgroup;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_home, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            //TODO add more options
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
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_home_view:
                    Toast.makeText(mContext, "wk:" + mWorkgroup.getWorkgroupID() + ": INFO BUTTON", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    break;
                default:
                    return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mActionMode != null) {
                mActionMode = null;
                mWorkgroup.setSelected(false);
                mSelectedWorkgroup = null;
            }
        }
    }

}
