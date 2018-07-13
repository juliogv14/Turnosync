package com.juliogv14.turnosync.data.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.data.UserRef;

import java.util.ArrayList;
import java.util.List;

public class UserRefsVM extends ViewModel {
    private MutableLiveData<List<UserRef>> userRefs;

    public ListenerRegistration loadUserRefs(CollectionReference dataRef, String orderBy) {
        userRefs = new MutableLiveData<>();
        return queryUserRefs(dataRef, orderBy);
    }

    public MutableLiveData<List<UserRef>> getUserRefs() {
        return userRefs;
    }

    private ListenerRegistration queryUserRefs (CollectionReference dataRef, String orderBy){
        final List<UserRef> users = new ArrayList<>();
        userRefs.setValue(users);
        return dataRef.orderBy(orderBy).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                    users.add(userData);
                                }
                                break;
                            case MODIFIED:
                                //Active modified
                                if(!userData.isActive()){
                                    users.remove(docChange.getOldIndex());
                                } else {
                                    UserRef userRef = users.get(docChange.getNewIndex());
                                    if(TextUtils.equals(userRef.getUid(), userData.getUid())){
                                        users.set(docChange.getNewIndex(), userData);
                                    } else {
                                        users.add(docChange.getNewIndex(), userData);
                                    }
                                }
                                break;
                            case REMOVED:
                                //Removed
                                users.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
                userRefs.setValue(users);
            }
        });
    }
}
