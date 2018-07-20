package com.juliogv14.turnosync.ui.mycalendar.changerequests;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
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
        mChangesRequestsListener = mChangeRequestsColl.orderBy(getString(R.string.data_key_timestamp), Query.Direction.ASCENDING)
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
                                if(TextUtils.equals(changeRequest.getState(), ChangeRequest.REQUESTED)
                                        || TextUtils.equals(changeRequest.getState(), ChangeRequest.ACCEPTED)){
                                    mChangeRequestList.add(0,changeRequest);
                                }
                                break;
                            case MODIFIED:
                                boolean exist = false;
                                int pos = 0;
                                for (int i = 0; i < mChangeRequestList.size(); i++) {
                                    ChangeRequest change = mChangeRequestList.get(i);
                                    if (TextUtils.equals(change.getId(), change.getId())) {
                                        exist = true;
                                        pos = i;
                                        break;
                                    }
                                }
                                if(exist){
                                    if(TextUtils.equals(changeRequest.getState(), ChangeRequest.ACCEPTED)){
                                        mChangeRequestList.set(pos, changeRequest);
                                    } else {
                                        mChangeRequestList.remove(pos);
                                    }
                                }
                                break;
                            case REMOVED:
                                //Removed
                                for (int i = 0; i < mChangeRequestList.size(); i++) {
                                    ChangeRequest change = mChangeRequestList.get(i);
                                    if (TextUtils.equals(change.getId(), change.getId())) {
                                        mChangeRequestList.remove(i);
                                        break;
                                    }
                                }
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
    public void onAcceptAccepted(final ChangeRequest changeRequest) {
        final DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        //Proceed to make the change
        final Shift ownShift = changeRequest.getOwnShift();
        final Shift otherShift = changeRequest.getOtherShift();

        final boolean[] sameShifts = new boolean[2];
        Task<QuerySnapshot> ownShiftQuery = getShiftRef(ownShift, sameShifts, 0);
        Task<QuerySnapshot> otherShiftQuery = getShiftRef(otherShift, sameShifts, 1);

        Tasks.whenAll(ownShiftQuery, otherShiftQuery).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (sameShifts[0] && sameShifts[1]){
                        //Shifts schedule hasnt change
                        Shift newOwnShift = new Shift(ownShift.getUserId(), otherShift.getDate(), otherShift.getType());
                        Shift newOtherShift = new Shift(otherShift.getUserId(), ownShift.getDate(), ownShift.getType());
                        removeShift(ownShift);
                        writeShift(newOwnShift);
                        removeShift(otherShift);
                        writeShift(newOtherShift);
                        resolveConflicts(changeRequest);
                        docRef.update(getString(R.string.data_key_state), ChangeRequest.APPROVED);
                    }
                } else {
                    if (task.getException() != null) {
                        Log.e(TAG, task.getException().getMessage());
                    }
                }
            }
        });

    }

    private Task<QuerySnapshot> getShiftRef(Shift shift, final boolean[] sameShifts, final int pos){
        Task<QuerySnapshot> shiftQuery =  mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shifts)).whereEqualTo(getString(R.string.data_key_date), shift.getDate()).get();

        shiftQuery.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if (doc != null && doc.exists()) {
                            Shift shift = doc.toObject(Shift.class);
                            //Same shift
                            sameShifts[pos] = TextUtils.equals(shift.getType(), shift.getType());
                        }
                    }
                } else {
                    if (task.getException() != null) {
                        Log.e(TAG, task.getException().getMessage());
                    }
                }
            }
        });
        return shiftQuery;
    }

    private void writeShift(Shift shift){
        DocumentReference shiftRef =  mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shifts)).document();
        shift.setId(shiftRef.getId());
        shiftRef.set(shift);
    }

    private void removeShift(Shift shift){
        DocumentReference shiftRef =  mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shifts)).document(shift.getId());
        shiftRef.delete();
    }

    private void resolveConflicts(ChangeRequest changeRequest){
        Shift own = changeRequest.getOwnShift();
        Shift other = changeRequest.getOtherShift();

        for (ChangeRequest req : mChangeRequestList) {
            if(req != changeRequest && (TextUtils.equals(req.getState(), ChangeRequest.REQUESTED)
                    || TextUtils.equals(req.getState(), ChangeRequest.ACCEPTED))){
                Shift reqOwn = req.getOwnShift();
                Shift reqOther = req.getOtherShift();
                if(own.getDate().getTime() == reqOwn.getDate().getTime() || own.getDate().getTime() == reqOther.getDate().getTime()
                        || other.getDate().getTime() == reqOwn.getDate().getTime() || other.getDate().getTime() == reqOther.getDate().getTime()){
                    req.setState(ChangeRequest.CONFLICT);
                    mChangeRequestsColl.document(req.getId()).update(getString(R.string.data_key_state), ChangeRequest.CONFLICT);

                }
            }
        }

    }

    @Override
    public void onDenyRequested(ChangeRequest changeRequest, String uid, UserRoles role) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        if (TextUtils.equals(uid, changeRequest.getOwnShift().getUserId())){
            docRef.delete();
        } else {
            docRef.update(getString(R.string.data_key_state), ChangeRequest.DENIED_USER);
        }
    }

    @Override
    public void onDenyAccepted(ChangeRequest changeRequest, String uid, UserRoles role) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());

        if(role == UserRoles.MANAGER){
            docRef.update(getString(R.string.data_key_state), ChangeRequest.DENIED_MANAGER);
        } else {
            docRef.update(getString(R.string.data_key_state), ChangeRequest.CANCELLED);
        }
    }
}
