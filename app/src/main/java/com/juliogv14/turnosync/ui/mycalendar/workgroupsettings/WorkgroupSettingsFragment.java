package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.data.viewmodels.UserRefsVM;
import com.juliogv14.turnosync.databinding.FragmentWorkgroupSettingsBinding;
import com.juliogv14.turnosync.ui.drawerlayout.OnFragmentInteractionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * La clase WorkgroupSettingsFragment es el fragmento por defecto usado en WorkgroupSettingsActivity.
 * Es el fragmento encargado de manejar la vista donde se muestran los usuarios del grupo siendo posible
 * añadir, editar el nombre a mostrar y eliminar usuarios.
 * Ademas se puede configurar el numero máximo de horas semanales y Contiene la navegación a ShiftTypesFragment.
 * Extiende Fragment.
 * Implementa la interfaz de escucha de GroupUsersAdapter.
 * Implementa la interfaz de escucha de AddUserDialog.
 * Implementa la interfaz de escucha de EditInitialsDialog.
 * Implementa la interfaz de escucha de WeeklyHoursDialog.
 *
 * @author Julio García
 * @see Fragment
 * @see GroupUsersAdapter.UserOnClickListener
 * @see AddUserDialog.AddUserListener
 * @see EditInitialsDialog.EditInitialsListener
 * @see WeeklyHoursDialog.WeeklyHoursDialogListener

 */
public class WorkgroupSettingsFragment extends Fragment implements GroupUsersAdapter.UserOnClickListener,
        AddUserDialog.AddUserListener, EditInitialsDialog.EditInitialsListener, WeeklyHoursDialog.WeeklyHoursDialogListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    //@{
    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String WORKGROUP_KEY = "workgroup";
    private static final String WEEKLY_HOURS_KEY = "weeklyHours";
    //@}

    /** Referencia a la vista con databinding */
    private FragmentWorkgroupSettingsBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private WorkgroupSettingsListener mListener;

    /** Referencia al servicio de base de datos de Firebase Cloud Firestore */
    private FirebaseFirestore mFirebaseFirestore;
    /** Registro de escucha de la petición de usuarios del grupo */
    private ListenerRegistration mGroupUsersListener;

    /** Referencia al grupo */
    private UserWorkgroup mWorkgroup;
    /** Horas máximas semanales */
    private AtomicLong mWeeklyHours;
    /** Lista de usuarios del grupo */
    private ArrayList<UserRef> mUserList;


    /**
     * Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param workgroup Grupo en cuestión
     * @param weeklyHours Horas máximas semanales
     * @return instancia de la clase WorkgroupSettingsFragment
     */
    public static WorkgroupSettingsFragment newInstance(UserWorkgroup workgroup, AtomicLong weeklyHours) {
        WorkgroupSettingsFragment f = new WorkgroupSettingsFragment();

        Bundle args = new Bundle();
        args.putParcelable(WORKGROUP_KEY, workgroup);
        args.putSerializable(WEEKLY_HOURS_KEY, weeklyHours);
        f.setArguments(args);
        return f;
    }

    /**
     * {@inheritDoc} <br>
     * Al vincularse al contexto se obtienen referencias al contexto y la clase de escucha.
     * @see Context
     */
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof WorkgroupSettingsListener) {
            mListener = (WorkgroupSettingsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WorkgroupSettingsListener");
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Recupera los argumentos pasados en {@link #newInstance}
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(WORKGROUP_KEY);
            mWeeklyHours = (AtomicLong) args.getSerializable(WEEKLY_HOURS_KEY);
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Infla la vista y se referencia mediante Databinding.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentWorkgroupSettingsBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        mUserList = new ArrayList<>();
        return mViewBinding.getRoot();
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se inicializa la vista y las variables. Se crea el adaptador para el recycler view que lista los usuarios
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentSwapped(R.string.fragment_workgroupSettings);

        //Init
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        attatchWorkgroupUsersListener();

        updateWeeklyHours();

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerUsers.setLayoutManager(layoutManager);
        mViewBinding.recyclerUsers.setHasFixedSize(true);
        GroupUsersAdapter recyclerAdapter = new GroupUsersAdapter(mContext, this, mUserList, mWorkgroup.getRole());
        mViewBinding.recyclerUsers.setAdapter(recyclerAdapter);

        mViewBinding.settingsItemShiftTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.swapFragment(R.string.fragment_shiftTypes);
            }
        });

        mViewBinding.settingsItemShiftWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.equals(mWorkgroup.getRole(),UserRoles.MANAGER.toString())){
                    WeeklyHoursDialog dialog = WeeklyHoursDialog.newInstance(mWeeklyHours.get());
                    dialog.show(getChildFragmentManager(), "weeklyHours");
                }

            }
        });

    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al destruirse la vista.
     * Se desvincula la escucha de tipos de turno
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mGroupUsersListener != null){
            mGroupUsersListener.remove();
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
        inflater.inflate(R.menu.menu_workgroup_settings, menu);
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

            if(menu.getItem(i).getItemId() == R.id.action_wkSettings_addUser && !mWorkgroup.getRole().equals(UserRoles.MANAGER.toString())){
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
        if (item.getItemId() == R.id.action_wkSettings_addUser) {
            AddUserDialog dialog = new AddUserDialog();
            dialog.show(getChildFragmentManager(), "addUser");
            return true;
        }
        return false;
    }

    /**
     * Vincula la escucha de la petición de usuarios del grupo, los cambios se obtienen en tiempo real.
     * Llamado dentro de {@link #onViewCreated}
     */
    private void attatchWorkgroupUsersListener() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_users));

        UserRefsVM userRefsVM = ViewModelProviders.of((AppCompatActivity) mListener).get(UserRefsVM.class);
        mGroupUsersListener = userRefsVM.loadUserRefs(workgroupsUsersColl, getString(R.string.data_key_shortname));
        mUserList = (ArrayList<UserRef>) userRefsVM.getUserRefs().getValue();
        userRefsVM.getUserRefs().observe(this, new Observer<List<UserRef>>() {
            @Override
            public void onChanged(@Nullable List<UserRef> userRefs) {
                mViewBinding.recyclerUsers.getAdapter().notifyDataSetChanged();
            }
        });

    }

    /**
     * Actualiza el numero de horas máximas semanales en la vista.
     */
    private void updateWeeklyHours (){
        String weeklyHours = getString(R.string.wkSettings_label_weeklyHours);
        mViewBinding.settingsItemShiftWeekly.setText(Html.fromHtml(weeklyHours + " <b>" + mWeeklyHours.get() + " h</b>" ));
    }

    /**
     * Implementacion de la interfaz de comunicación de GroupUsersAdapter
     * Elimina un usuario del grupo mostrando una advertencia primero
     * @param uid Identificador del usuario a quitar
     * @see GroupUsersAdapter
     */
    @Override
    public void onClickRemoveUser(final String uid) {

        new AlertDialog.Builder(mContext).setTitle(getString(R.string.dialog_removeUser_title))
                .setMessage(getString(R.string.dialog_removeUser_message))
                .setPositiveButton(getString(R.string.dialog_removeUser_button_remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CollectionReference workgroupUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups))
                                .document(mWorkgroup.getWorkgroupId()).collection(getString(R.string.data_ref_users));

                        workgroupUsersColl.document(uid).update(getString(R.string.data_key_active), false);
                        mViewBinding.recyclerUsers.getAdapter().notifyDataSetChanged();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_removeUser_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    /**
     * Implementacion de la interfaz de comunicación de GroupUsersAdapter
     * Permite editar el nombre de un usuario del grupo llamando a cuadro de dialogo EditInitialsDialog
     * @param pos Posición del usuario en la lista
     * @see GroupUsersAdapter
     * @see EditInitialsDialog
     */
    @Override
    public void onClickEditUser(int pos) {
        UserRef userRef = mUserList.get(pos);
        EditInitialsDialog dialog = EditInitialsDialog.newInstance(userRef);
        dialog.show(getChildFragmentManager(), "editInitials");
    }

    /**
     * Implementacion de la interfaz de comunicación de EditInitialsDialog
     * Efectúa el cambio de nombre dado en EditInitialsDialog
     * @param userRef Referencia del usuario
     * @see EditInitialsDialog
     */
    @Override
    public void onInitialsName(UserRef userRef) {
        CollectionReference workgroupUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups))
                .document(mWorkgroup.getWorkgroupId()).collection(getString(R.string.data_ref_users));
        workgroupUsersColl.document(userRef.getUid()).set(userRef);
        mViewBinding.recyclerUsers.getAdapter().notifyDataSetChanged();
    }

    /**
     * Implementacion de la interfaz de comunicación de AddUserDialog
     * Invita a un usuario al grupo
     * @param email Email del usuario a invitar
     * @see AddUserDialog
     */
    @Override
    public void onClickAddUser(String email) {

        CollectionReference invitesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_invites));
        Map<String, String> inviteData = new HashMap<>();
        inviteData.put(getString(R.string.data_key_email), email);
        inviteData.put(getString(R.string.data_key_workgroupid), mWorkgroup.getWorkgroupId());
        invitesColl.document(email).set(inviteData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            if (task.getException() != null) {
                                Log.e(TAG, task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    /**
     * Implementacion de la interfaz de comunicación de WeeklyHoursDialog
     * Efectúa el cambio de las horas máximas semanales recibidas de WeeklyHoursDialog
     * @param hours Nuevo valor de las horas máximas semanalaes
     * @see WeeklyHoursDialog
     */
    @Override
    public void onSetWeekyHours(long hours) {
        mWeeklyHours.set(hours);
        updateWeeklyHours();
        mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .update(getString(R.string.data_key_weeklyhours), mWeeklyHours.get());
    }

    /**
     * Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface WorkgroupSettingsListener extends OnFragmentInteractionListener {
        void swapFragment(int fragmentId);
    }
}
