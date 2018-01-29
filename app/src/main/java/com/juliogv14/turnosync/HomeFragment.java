package com.juliogv14.turnosync;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class HomeFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    protected ContentHomeBinding mViewBinding;

    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mFirebaseUser;
    private ListenerRegistration mWorkgroupsListener;

    private GroupItemsAdapter mGridAdapter;
    private ArrayList<Workgroup> mWorkgroupsList;

    private OnHomeFragmentInteractionListener mListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        }
    }

    //Create objects obj = new obj()
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = ContentHomeBinding.inflate(inflater, container, false);
        mListener.onDrawerItemSelected(R.id.nav_item_home);
        return mViewBinding.getRoot();
    }

    //View setup, same as onCreate
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mWorkgroupsList = new ArrayList<>();
        mGridAdapter = new GroupItemsAdapter((Activity) mListener, R.layout.content_home, mWorkgroupsList);

        mViewBinding.gridViewGroupDisplay.setAdapter(mGridAdapter);

        Log.d(TAG, "Start HomeFragment");
        mViewBinding.floatingButtonNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testData();
            }
        });

        mViewBinding.gridViewGroupDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Workgroup wk = mWorkgroupsList.get(i);
                mListener.onWorkgroupSelected(wk);
                //Toast.makeText(getContext(), "WK: uid: " + wk.getWorkgroupID(), Toast.LENGTH_SHORT).show();
            }
        });
        attatchWorkgroupsListener();
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
                    final HashMap<String, DocumentChange.Type> docChanges = new HashMap<>();

                    for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {

                        docChanges.put(dc.getDocument().getId(), dc.getType());
                        workGroupsRef.document(dc.getDocument().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    DocumentChange.Type type = docChanges.get(documentSnapshot.getId());
                                    Workgroup workgroup = documentSnapshot.toObject(Workgroup.class);

                                    switch (type) {
                                        case ADDED:
                                            mWorkgroupsList.add(workgroup);
                                            break;
                                        case REMOVED:
                                            //TODO: look for workgroup reference in arraylist
                                            break;
                                        case MODIFIED:
                                            //TODO: handle modifed workgroup
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
    public void onDestroyView() {
        super.onDestroyView();
        mWorkgroupsListener.remove();
    }

    private class GroupItemsAdapter extends ArrayAdapter<Workgroup> {

        private ArrayList<Workgroup> data;
        ItemWorkgroupBinding itemBinding;


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
    }

}
