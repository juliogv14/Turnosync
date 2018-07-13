package com.juliogv14.turnosync.data.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;

import java.util.HashMap;
import java.util.Map;

public class MyCalendarVM extends ViewModel {
    private MutableLiveData<Boolean> mEditMode;
    private MutableLiveData<Long> mWeeklyHours;
    private MutableLiveData<Map<String, ShiftType>> mShiftTypes;
    private MutableLiveData<Shift> mOwnShift;
    private MutableLiveData<Shift> mOtherShift;

    //Setter getter editMode
    public void setEditMode(boolean value){
        if (mEditMode == null){
            mEditMode = new MutableLiveData<>();
        }
        mEditMode.setValue(value);
    }
    public MutableLiveData<Boolean> getEditMode() {
        return mEditMode;
    }

    //Setter getter weeklyhours
    public void setWeeklyHours(long value){
        if (mWeeklyHours == null){
            mWeeklyHours = new MutableLiveData<>();
        }
        mWeeklyHours.setValue(value);
    }
    public MutableLiveData<Long> getWeeklyHours() {
        return mWeeklyHours;
    }

    //Setter getter shiftTypes
    public ListenerRegistration loadShiftTypes(CollectionReference dataRef){
        mShiftTypes = new MutableLiveData<>();
        return queryShiftTypes(dataRef);
    }

    public MutableLiveData<Map<String, ShiftType>> getShiftTypes() {
        return mShiftTypes;
    }

    private ListenerRegistration queryShiftTypes(CollectionReference dataRef) {
        final Map<String, ShiftType> shiftTypeMap = new HashMap<>();
        mShiftTypes.setValue(shiftTypeMap);
        return dataRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null){
                    return;
                }
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = docChange.getDocument();
                    if (doc.exists()) {
                        ShiftType shiftType = doc.toObject(ShiftType.class);

                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                if (shiftType.isActive()) {
                                    shiftTypeMap.put(shiftType.getId(), shiftType);
                                }
                                break;
                            case MODIFIED:

                                //Active modified
                                if (shiftType.isActive()) {
                                    shiftTypeMap.put(shiftType.getId(), shiftType);
                                } else {
                                    shiftTypeMap.remove(shiftType.getId());
                                }
                                break;
                            case REMOVED:
                                //Removed
                                shiftTypeMap.remove(shiftType.getId());
                                break;
                        }
                    }
                }
                mShiftTypes.setValue(shiftTypeMap);
            }
        });
    }

    //Setter and getter ownShift
    public void setOwnShift(Shift shift){
        if(mOwnShift == null){
            mOwnShift = new MutableLiveData<>();
        }
        mOwnShift.setValue(shift);
    }

    public MutableLiveData<Shift> getOwnShift() {
        if(mOwnShift == null){
            mOwnShift = new MutableLiveData<>();
        }
        return mOwnShift;
    }

    //Setter and getter otherShift
    public void setOtherShift(Shift shift){
        if(mOtherShift == null){
            mOtherShift = new MutableLiveData<>();
        }
        mOtherShift.setValue(shift);
    }

    public MutableLiveData<Shift> getOtherShift() {
        if(mOtherShift == null){
            mOtherShift = new MutableLiveData<>();
        }
        return mOtherShift;
    }
}