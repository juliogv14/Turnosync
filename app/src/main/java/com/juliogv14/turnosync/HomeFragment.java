package com.juliogv14.turnosync;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.data.Workgroup;
import com.juliogv14.turnosync.databinding.ContentHomeBinding;
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

    private final String TAG = this.getClass().getSimpleName();

    protected ContentHomeBinding mViewBinding;

    //Firebase
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mFirebaseUser;
    private ListenerRegistration mWorkgroupsListener;

    //GridAdapter
    private GroupItemsAdapter mGridAdapter;
    private ArrayList<Workgroup> mWorkgroupsList;
    private ActionMode mActionMode;

    private OnHomeFragmentInteractionListener mListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        }
    }

    //Create objects obj = new obj()
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorkgroupsList = new ArrayList<>();
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = ContentHomeBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    //View setup, same as onCreate
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mGridAdapter = new GroupItemsAdapter((Activity) mListener, R.layout.content_home, mWorkgroupsList);

        mViewBinding.gridViewGroupDisplay.setAdapter(mGridAdapter);
        mListener.onFragmentCreated(R.id.nav_item_home);
        attatchWorkgroupsListener();


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

                Workgroup wk = mWorkgroupsList.get(position);
                if (wk.isSelected()) {
                    wk.setSelected(false);
                } else {
                    mListener.onWorkgroupSelected(wk);
                }

            }
        });

        mViewBinding.gridViewGroupDisplay.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                Workgroup wk = mWorkgroupsList.get(position);
                handleSelectedWorkgroup(wk);
                return false;
            }
        });
        Log.d(TAG, "Start HomeFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWorkgroupsListener.remove();
    }

    @Override
    public void onDialogPositiveClick(String name, String description) {
        if (mFirebaseUser != null) {

            Map<String, Object> leveldata = new HashMap<>();
            leveldata.put("level", 0);
            DocumentReference workgroupRef = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document();
            String workgroupID = workgroupRef.getId();
            Workgroup newGroup = new Workgroup(workgroupID, name, description);
            workgroupRef.set(newGroup);
            mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(workgroupID).set(leveldata);
            Log.d(TAG, "Create workgroup dialog return");
        }
    }

    private void attatchWorkgroupsListener() {
        CollectionReference userGroupsRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                .collection(getString(R.string.data_ref_workgroups));


        mWorkgroupsListener = userGroupsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    CollectionReference workGroupsRef = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups));

                    final HashMap<String, DocumentChange> docChanges = new HashMap<>();
                    final HashMap<String, Integer> docChangesLevel = new HashMap<>();

                    for (DocumentChange docChange : documentSnapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = docChange.getDocument();
                        String docID = doc.getId();
                        docChanges.put(docID, docChange);
                        docChangesLevel.put(docID, Integer.parseInt(doc.get("level").toString()));

                        workGroupsRef.document(docChange.getDocument().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    DocumentChange change = docChanges.get(documentSnapshot.getId());
                                    Workgroup workgroup = documentSnapshot.toObject(Workgroup.class);
                                    workgroup.setLevel(docChangesLevel.get(documentSnapshot.getId()));
                                    mListener.initilizeWorkgroup(workgroup);

                                    switch (change.getType()) {
                                        case ADDED:
                                            //Added
                                            mWorkgroupsList.add(workgroup);
                                            break;
                                        case MODIFIED:
                                            if (change.getOldIndex() == change.getNewIndex()) {
                                                //Modified, same position
                                                mWorkgroupsList.set(change.getOldIndex(), workgroup);
                                            } else {
                                                //Modified, differnt position
                                                mWorkgroupsList.remove(change.getOldIndex());
                                                mWorkgroupsList.add(change.getNewIndex(), workgroup);
                                            }
                                            break;
                                        case REMOVED:
                                            //Removed
                                            mWorkgroupsList.remove(change.getOldIndex());

                                            break;
                                    }
                                    mGridAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void handleSelectedWorkgroup(Workgroup workgroup) {

        workgroup.setSelected(true);

        if (mActionMode == null) {
            mActionMode = ((AppCompatActivity) mListener)
                    .startSupportActionMode(new ToolbarActionModeCallback((Context) mListener, mGridAdapter, workgroup));
            mActionMode.setTitle(workgroup.getDisplayname() + " selected");
        }
    }

    private class GroupItemsAdapter extends ArrayAdapter<Workgroup> {

        private ArrayList<Workgroup> data;
        private ItemWorkgroupBinding itemBinding;


        GroupItemsAdapter(@NonNull Context context, int resource, @NonNull List<Workgroup> objects) {
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

            Workgroup workgroup = getItem(position);
            if (workgroup != null) {
                //TODO temp level display
                String display = workgroup.getDisplayname() + "-" + workgroup.getLevel();
                itemBinding.textViewGroupName.setText(display);
                itemBinding.textViewGroupId.setText(workgroup.getWorkgroupID());
            }
            return convertView;
        }
    }

    public class ToolbarActionModeCallback implements ActionMode.Callback {

        private Context mContext;
        private GroupItemsAdapter mGroupsAdapter;

        Workgroup mWorkgroup;

        ToolbarActionModeCallback(Context mContext, GroupItemsAdapter mGroupsAdapter, Workgroup workgroup) {
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
            menu.findItem(R.id.action_home_view).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_home_view:
                    Toast.makeText(mContext, "wk:" + mWorkgroup.getWorkgroupID() + ": INFO BUTTON", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mActionMode != null) {
                mActionMode = null;
            }
        }
    }

    public interface OnHomeFragmentInteractionListener extends OnFragmentInteractionListener {
        void onWorkgroupSelected(Workgroup workgroup);

        void initilizeWorkgroup(Workgroup workgroup);
    }

}
