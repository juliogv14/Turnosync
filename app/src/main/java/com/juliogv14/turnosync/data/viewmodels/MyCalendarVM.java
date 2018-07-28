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

/**
 * La clase MyCalendarVM se usa para manejar los datos del fragmento MyCalendarFragment y actualizar
 * la vista acorde a sus cambios.
 * Extiende la clase ViewModel
 *
 * @author Julio García
 * @see ViewModel
 * @see MutableLiveData
 */
public class MyCalendarVM extends ViewModel {
    /** Modo editar para realizar cambios en el calendario */
    private MutableLiveData<Boolean> mEditMode = new MutableLiveData<>();;
    /** Horas maximas semanales cargadas de base de datos*/
    private MutableLiveData<Long> mWeeklyHours = new MutableLiveData<>();
    /** Turno propio seleccionado para solicitar un cambio */
    private MutableLiveData<Shift> mOwnShift = new MutableLiveData<>();
    /** Turno de otro usuario seleccionado para solicitar un cambio */
    private MutableLiveData<Shift> mOtherShift = new MutableLiveData<>();
    /** Lista de tipos de turnos cargado desde base de datos */
    private MutableLiveData<Map<String, ShiftType>> mShiftTypes;

    /** Setter editMode */
    public void setEditMode(boolean value){
        mEditMode.setValue(value);
    }

    /** Getter editMode live data */
    public MutableLiveData<Boolean> getEditMode() {
        return mEditMode;
    }

    /** Setter weeklyHours */
    public void setWeeklyHours(long value){
        mWeeklyHours.setValue(value);
    }

    /** Getter weeklyHours live data */
    public MutableLiveData<Long> getWeeklyHours() {
        return mWeeklyHours;
    }

    /** Loads shiftTypes map */
    public ListenerRegistration loadShiftTypes(CollectionReference dataRef){
        mShiftTypes = new MutableLiveData<>();
        return queryShiftTypes(dataRef);
    }

    /** Getter shiftTypes map */
    public MutableLiveData<Map<String, ShiftType>> getShiftTypes() {
        return mShiftTypes;
    }

    /** Obtiene el mapa de tipos de turno y escucha por cambios en base de datos para actualizarlos en tiempo real
     * @return ListenerRegistration de la query para poder desvincular la escucha
     */
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

    /** Setter ownShift */
    public void setOwnShift(Shift shift){
        mOwnShift.setValue(shift);
    }

    /** Getter ownShift live data */
    public MutableLiveData<Shift> getOwnShift() {
        return mOwnShift;
    }

    /** Setter otherShift */
    public void setOtherShift(Shift shift){
        mOtherShift.setValue(shift);
    }

    /** Getter otherShift live data */
    public MutableLiveData<Shift> getOtherShift() {
        return mOtherShift;
    }
}