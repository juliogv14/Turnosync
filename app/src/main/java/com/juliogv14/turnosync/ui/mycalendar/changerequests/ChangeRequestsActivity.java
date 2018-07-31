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

/**
 * La clase ChangeRequestsActivity es una actividad encargada de manejar la vista con las solicitudes
 * de cambios de turnos. Muestra la lista y efectua los cambios en el estado de las solicitudes.
 * Extiende AppCompatActivity
 * Implementa la interfaz de comunicación de ChangeRequestsAdapter
 *
 * @author Julio García
 * @see AppCompatActivity
 * @see ChangeRequestsAdapter.ChangeRequestListener
 */
public class ChangeRequestsActivity extends AppCompatActivity implements ChangeRequestsAdapter.ChangeRequestListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /** Referencia a la vista con databinding */
    private ActivityChangeRequestsBinding mViewBinding;

    /** Referencia al servicio de base de datos de Firebase Cloud Firestore */
    private FirebaseFirestore mFirebaseFirestore;
    /** Referencia a la colección de las solicitudes en base de datos */
    private CollectionReference mChangeRequestsColl;
    /** Registro de escucha de la petición de solicitudes de cambios */
    private ListenerRegistration mChangesRequestsListener;

    /** Referencia al grupo */
    private UserWorkgroup mWorkgroup;
    /** Mapa con los tipos de turnos */
    private Map<String, ShiftType> mShiftTypesMap;
    /** Mapa con los usuarios del grupo */
    private Map<String, UserRef> mUserRefsMap;
    /** Listado de las solicitudes de cambio */
    private ArrayList<ChangeRequest> mChangeRequestList;

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Al crearse se inicializa la vista. Se crea el adaptador para el recycler view que lista las solicitudes.
     */
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

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al destruirse la actividad.
     * Se desvincula la escucha de solicitudes de cambios
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mChangesRequestsListener != null){
            mChangesRequestsListener.remove();
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Responde cuando se selecciona un elemento del menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Vincula la escucha de la petición de usuarios dentro del grupo, los cambios se obtienen en tiempo real.
     * Llamado dentro de {@link #onCreate}
     */
    // TODO: 31/07/2018 Si un cambio no esta resuelto y la fecha es antigua se deben cancelar
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

    /**
     * Implementacion de la interfaz de escucha de ChangeRequestsAdapter
     * El usuario acepta desde el estado de solicitado
     * Se cambia el estado de solicitado a aceptado
     * @param changeRequest Solicitud de cambio
     */
    @Override
    public void onAcceptRequested(ChangeRequest changeRequest) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        docRef.update(getString(R.string.data_key_state), ChangeRequest.ACCEPTED);
    }

    /**
     * Implementacion de la interfaz de escucha de ChangeRequestsAdapter
     * El usuario acepta desde el estado de aceptado
     * Se cambia el estado de aceptado a aprovado y se efectuan los cambios comprobando que no haya conflicto
     * por modificaciones del calendaro posteriores.
     *
     * @param changeRequest Solicitud de cambio
     */
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



    /**
     *  Comprobación de si alguno de los dos turnos implicados han dejado de existir
     *
     * @param shift Turno a comprobar
     * @param sameShifts Vector de booleanos para indicar el resultado
     * @param pos Posicion dentro del vector
     * @return Tarea de la petición
     */
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

    /**
     * Escribe un turno en base de datos
     * @param shift Turno a escribir
     */
    private void writeShift(Shift shift){
        DocumentReference shiftRef =  mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shifts)).document();
        shift.setId(shiftRef.getId());
        shiftRef.set(shift);
    }

    /**
     * Borra un turno en base de datos
     * @param shift Turno a borrar
     */
    private void removeShift(Shift shift){
        DocumentReference shiftRef =  mFirebaseFirestore
                .collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shifts)).document(shift.getId());
        shiftRef.delete();
    }

    /**
     * Resuelve los conflictos de una solicitud de cambio aprobada cancelando las demas que tuvieran alguno
     * de los dos turnos implicados
     * @param changeRequest Solicitud de cambio de turno
     */
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

    /**
     * Implementacion de la interfaz de escucha de ChangeRequestsAdapter
     * El usuario rechaza desde el estado de solicitado
     * Dependiendo del usario se cambia el estado a rechazado o cancelado
     * @param changeRequest Solicitud de cambio
     * @param uid Indentificador del usuario que lleva a cabo la accion
     * @param role Rol del usuario
     */
    @Override
    public void onDenyRequested(ChangeRequest changeRequest, String uid, UserRoles role) {
        DocumentReference docRef = mChangeRequestsColl.document(changeRequest.getId());
        if (TextUtils.equals(uid, changeRequest.getOwnShift().getUserId())){
            docRef.update(getString(R.string.data_key_state), ChangeRequest.CANCELLED);
        } else {
            docRef.update(getString(R.string.data_key_state), ChangeRequest.DENIED_USER);
        }
    }

    /**
     * Implementacion de la interfaz de escucha de ChangeRequestsAdapter
     * El usuario rechaza desde el estado de aceptado
     * Dependiendo del usario se cambia el estado a rechazado o cancelado
     * @param changeRequest Solicitud de cambio
     * @param uid Indentificador del usuario que lleva a cabo la accion
     * @param role Rol del usuario
     */
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
