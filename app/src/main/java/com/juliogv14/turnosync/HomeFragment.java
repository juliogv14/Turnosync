package com.juliogv14.turnosync;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

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
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Workgroup wk = mWorkgroupsList.get(i);
                mListener.onWorkgroupSelected(wk);
            }
        });
        //Inicilize workgroup selected
        Log.d(TAG, "Start HomeFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWorkgroupsListener.remove();
    }

    private void testData() {


        if (mFirebaseUser != null) {

            Map<String, Object> leveldata = new HashMap<>();
            leveldata.put("level", "master");
            DocumentReference workgroupRef = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document();
            String workgroupID = workgroupRef.getId();
            Workgroup newGroup = new Workgroup(workgroupID, "trabajo", "");
            workgroupRef.set(newGroup);
            mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(workgroupID).set(leveldata);
            Log.d(TAG, "testData floating button");
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

                    for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {

                        docChanges.put(dc.getDocument().getId(), dc);
                        workGroupsRef.document(dc.getDocument().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    DocumentChange change = docChanges.get(documentSnapshot.getId());
                                    Workgroup workgroup = documentSnapshot.toObject(Workgroup.class);
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

    @Override
    public void onDialogPositiveClick(String name, String description) {
        if (mFirebaseUser != null) {

            Map<String, Object> leveldata = new HashMap<>();
            leveldata.put("level", "master");
            DocumentReference workgroupRef = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document();
            String workgroupID = workgroupRef.getId();
            Workgroup newGroup = new Workgroup(workgroupID, name, description);
            workgroupRef.set(newGroup);
            mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                    .collection(getString(R.string.data_ref_workgroups)).document(workgroupID).set(leveldata);
            Log.d(TAG, "Create workgroup dialog return");
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
                itemBinding.textViewGroupName.setText(workgroup.getDisplayname());
                itemBinding.textViewGroupId.setText(workgroup.getWorkgroupID());
            }
            return convertView;
        }
    }

    public interface OnHomeFragmentInteractionListener extends OnFragmentInteractionListener {
        void onWorkgroupSelected(Workgroup workgroup);

        void initilizeWorkgroup(Workgroup workgroup);
    }

}
