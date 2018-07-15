package com.juliogv14.turnosync.ui.mycalendar.changerequests;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityChangeRequestsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeRequestsActivity extends AppCompatActivity implements ChangeRequestsAdapter.ChangeRequestListener {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Binding
    private ActivityChangeRequestsBinding mViewBinding;

    //Firebase
    private FirebaseFirestore mFirebaseFirestore;
    private CollectionReference mChangeRequestsColl;
    private ListenerRegistration mChangesRequestsListener;

    //Intent data
    private UserWorkgroup mWorkgroup;
    private Map<String, ShiftType> mShiftTypesMap;
    private Map<String, UserRef> mUserRefsMap;
    private ArrayList<ChangeRequest> mChangeRequestList;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_change_requests);
        mWorkgroup = getIntent().getParcelableExtra(getString(R.string.data_int_workgroup));
        mShiftTypesMap = (HashMap<String, ShiftType>) getIntent().getSerializableExtra(getString(R.string.data_int_shiftTypes));
        List<UserRef> userRefList = getIntent().getParcelableArrayListExtra(getString(R.string.data_int_users));
        mUserRefsMap = new HashMap<>();
        for (UserRef userRef : userRefList) {
            mUserRefsMap.put(userRef.getUid(), userRef);
        }
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        attatchChangeRequestsListener();

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerChangeRequests.setLayoutManager(layoutManager);
        mViewBinding.recyclerChangeRequests.setHasFixedSize(true);
        mViewBinding.recyclerChangeRequests.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ChangeRequestsAdapter adapter = new ChangeRequestsAdapter(this, this,  mWorkgroup.getRole(), mShiftTypesMap, mUserRefsMap, mChangeRequestList);
        mViewBinding.recyclerChangeRequests.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mChangesRequestsListener != null){
            mChangesRequestsListener.remove();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attatchChangeRequestsListener (){
        mChangeRequestsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_changeRequests));
        mChangeRequestList = new ArrayList<>();
        mChangesRequestsListener = mChangeRequestsColl.orderBy(getString(R.string.data_key_timestamp), Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = docChange.getDocument();
                    if (doc.exists()) {
                        ChangeRequest changeRequest = doc.toObject(ChangeRequest.class);
                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                mChangeRequestList.add(changeRequest);
                                break;
                            case MODIFIED:
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Modified, same position
                                    mChangeRequestList.set(docChange.getOldIndex(), changeRequest);
                                } else {
                                    //Modified, differnt position
                                    mChangeRequestList.remove(docChange.getOldIndex());
                                    mChangeRequestList.add(docChange.getNewIndex(), changeRequest);
                                }
                                break;
                            case REMOVED:
                                //Removed
                                mChangeRequestList.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
                mViewBinding.recyclerChangeRequests.getAdapter().notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAcceptRequested(ChangeRequest changeRequest) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        docRef.update(getString(R.string.data_key_state), ChangeRequest.ACCEPTED);
    }

    @Override
    public void onApproveAccepted(ChangeRequest changeRequest) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        docRef.update(getString(R.string.data_key_state), ChangeRequest.APPROVED);
    }

    @Override
    public void onDenyRequested(ChangeRequest changeRequest) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        docRef.delete();
    }

    @Override
    public void onDenyAccepted(ChangeRequest changeRequest) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        docRef.delete();
    }
}
