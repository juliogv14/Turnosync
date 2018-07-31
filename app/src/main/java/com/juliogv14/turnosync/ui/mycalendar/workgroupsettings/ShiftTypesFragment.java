package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentShiftTypesBinding;
import com.juliogv14.turnosync.ui.drawerlayout.OnFragmentInteractionListener;
import com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes.CreateTypeDialog;
import com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes.ShiftTypesAdapter;

import java.util.ArrayList;

/**
 * La clase ShiftTypesFragment es el fragmento encargado de la gestion de los tipos de turno. Permite crearlos
 * modificarlos y eliminarlos. Utiliza un RecyclerView para mostrarlos
 * Extiende Fragment.
 * Implementa la interfaz de escucha de CreateTypeDialog.
 * * Implementa la interfaz de escucha de ShiftTypesAdapter.
 *
 * @author Julio García
 * @see Fragment
 * @see CreateTypeDialog.CreateTypeListener
 * @see ShiftTypesAdapter.TypeOnClickListener
 */
public class ShiftTypesFragment extends Fragment implements ShiftTypesAdapter.TypeOnClickListener, CreateTypeDialog.CreateTypeListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String WORKGROUP_KEY = "workgroup";

    /** Referencia a la vista con databinding */
    private FragmentShiftTypesBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private OnFragmentInteractionListener mListener;

    /** Referencia al servicio de base de datos de Firebase Cloud Firestore */
    private FirebaseFirestore mFirebaseFirestore;
    /** Registro de escucha de la petición de los tipos de turnos */
    private ListenerRegistration mShiftTypesListener;

    /** Referencia al grupo */
    private UserWorkgroup mWorkgroup;
    /** Listado de tipos de turno */
    private ArrayList<ShiftType> shifTypesList;

    public ShiftTypesFragment() {
    }

    /**
     * Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param workgroup Grupo de trabajo en cuestión
     * @return instancia de la clase ShiftTypesFragment
     */
    public static ShiftTypesFragment newInstance(UserWorkgroup workgroup) {
        ShiftTypesFragment fragment = new ShiftTypesFragment();
        Bundle args = new Bundle();
        args.putParcelable(WORKGROUP_KEY, workgroup);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc} <br>
     * Al vincularse al contexto se obtienen referencias al contexto y la clase de escucha.
     * @see Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Recupera los argumentos pasados en {@link #newInstance}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(WORKGROUP_KEY);
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Infla la vista y se referencia mediante Databinding.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        shifTypesList = new ArrayList<>();
        mViewBinding = FragmentShiftTypesBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se inicializa la vista y las variables. Se crea el adaptador para el recycler view que lista los tipos de turno
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentSwapped(R.string.fragment_shiftTypes);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        attatchShiftTypesListener();

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerShiftTypes.setLayoutManager(layoutManager);
        mViewBinding.recyclerShiftTypes.setHasFixedSize(true);
        ShiftTypesAdapter adapter = new ShiftTypesAdapter(mContext, this, shifTypesList, mWorkgroup.getRole());
        mViewBinding.recyclerShiftTypes.setAdapter(adapter);


    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al destruirse la vista.
     * Se desvincula la escucha de tipos de turno
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mShiftTypesListener != null) {
            mShiftTypesListener.remove();
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Al desvincularse de la actividad se ponen a null las referencias
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

    /**
     * {@inheritDoc}
     * Callback del ciclo de vida
     * Infla la vista del menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_shifttypes, menu);
    }

    /**
     * {@inheritDoc}
     * Callback del ciclo de vida
     * Se prepara la vista cambiando el color de los iconos a blanco.
     * Dependiendo del rol se ocultan botones
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.mutate();
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
            if(menu.getItem(i).getItemId() == R.id.action_shiftTypes_newType && !mWorkgroup.getRole().equals(UserRoles.MANAGER.toString())){
                menu.getItem(i).setVisible(false);
            }

        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Responde cuando se selecciona un elemento del menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_shiftTypes_newType) {
            CreateTypeDialog dialog = new CreateTypeDialog();
            dialog.show(getChildFragmentManager(), "createTypeDialog");
            return true;
        }

        return false;
    }

    /**
     * Vincula la escucha de la petición de tipos de turno, los cambios se obtienen en tiempo real.
     * Llamado dentro de {@link #onViewCreated}
     */
    private void attatchShiftTypesListener() {
        CollectionReference shiftTypesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shiftTypes));

        mShiftTypesListener = shiftTypesColl.orderBy("name").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                    shifTypesList.add(docChange.getNewIndex(), shiftType);
                                }
                                break;
                            case MODIFIED:

                                //Modified, same position
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Active modified
                                    if (shiftType.isActive()) {
                                        shifTypesList.set(docChange.getOldIndex(), shiftType);
                                    } else {
                                        shifTypesList.remove(docChange.getOldIndex());
                                    }
                                } else {
                                    //Modified, differnt position
                                    if (shiftType.isActive()) {

                                        shifTypesList.remove(docChange.getOldIndex());
                                        shifTypesList.add(docChange.getNewIndex(), shiftType);
                                    }
                                }
                                break;
                            case REMOVED:
                                //Removed
                                shifTypesList.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
                mViewBinding.recyclerShiftTypes.getAdapter().notifyDataSetChanged();
            }
        });

    }


    /**
     * Implementacion de la interfaz de comunicación de ShiftTypesAdapter
     * Abre el cuadro de dialogo para poder editar el turno.
     * @param type Tipo de turno seleccionado
     * @see CreateTypeDialog
     */
    @Override
    public void onClickEditType(ShiftType type) {
        CreateTypeDialog dialog = CreateTypeDialog.newInstance(type);
        dialog.show(getChildFragmentManager(), "createTypeDialog");
    }

    /**
     * Implementacion de la interfaz de comunicación de ShiftTypesAdapter
     * Abre el cuadro de dialogo para avisar de las consecuencias de borrar el tipo de turno
     * @param type Tipo de turno seleccionado
     */
    @Override
    public void onClickRemoveType(final ShiftType type) {

        new AlertDialog.Builder(mContext).setTitle(getString(R.string.dialog_removeType_title))
                .setMessage(getString(R.string.dialog_removeType_message))
                .setPositiveButton(getString(R.string.dialog_removeType_button_remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CollectionReference shiftTypesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                                .collection(getString(R.string.data_ref_shiftTypes));

                        shiftTypesColl.document(type.getId()).update(getString(R.string.data_key_active), false);
                        mViewBinding.recyclerShiftTypes.getAdapter().notifyDataSetChanged();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_removeType_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

    }

    /**
     * Implementacion de la interfaz de comunicación de CreateTypeDialog
     * Efectúa la creación o edición del tipo de turno
     * @param shiftType Tipo de turno seleccionado
     */
    @Override
    public void onDialogPositiveClick(ShiftType shiftType) {
        CollectionReference shiftTypesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shiftTypes));

        DocumentReference docRef;

        if(shiftType.getId() == null){
            //New type
            docRef = shiftTypesColl.document();
            shiftType.setId(docRef.getId());
        } else {
            //Edit type
            docRef = shiftTypesColl.document(shiftType.getId());
        }
        docRef.set(shiftType);
    }
}
