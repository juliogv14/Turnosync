package com.juliogv14.turnosync.ui.drawerlayout;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.data.viewmodels.MyCalendarVM;
import com.juliogv14.turnosync.databinding.FragmentMycalendarBinding;
import com.juliogv14.turnosync.ui.mycalendar.MonthPageFragment;
import com.juliogv14.turnosync.ui.mycalendar.ScheduleWeekPageFragment;
import com.juliogv14.turnosync.ui.mycalendar.changerequests.ChangeRequestsActivity;
import com.juliogv14.turnosync.ui.mycalendar.createshift.ConfirmChangesDialog;
import com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.WorkgroupSettingsActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Months;
import org.joda.time.Weeks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * La clase MyCalendarFragment es el fragmento que contiene la vista del calendario personal del usuario
 * y del calendario grupal. Contiene un navegador de paginas para cambiar entre meses del calenario personal
 * y entre semanas del calendario grupal.
 * Se ocupa de efectuar los cambios creados desde el calendario grupal.
 * Desde el menu permite la navegación hacia el listado de cambios en proceso, cambio estre los dos
 * tipos del calendario y a la actividad de configuración del grupo.
 * Extiende Fragment.
 * Implementa la interfaz de escucha de cuadro de dialogo ConfirmChangesDialog y del fragmento ScheduleWeekPageFragment.
 *
 * @author Julio García
 * @see Fragment
 * @see ConfirmChangesDialog.ConfirmChangesListener
 * @see ScheduleWeekPageFragment.WeekPageListener
 * @see ViewModel
 * @see PagerAdapter
 */

public class MyCalendarFragment extends Fragment implements
        ConfirmChangesDialog.ConfirmChangesListener, ScheduleWeekPageFragment.WeekPageListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    //@{
    /** Claves para conservar datos al recrear la actividad */
    private static final String CURRENT_ADAPTER_POSITION = "currentPosition";
    private static final String CURRENT_PERSONAL_SCHEDULE = "currentPersonalSchedule";
    //@}
    /** Numero de meses s mostrar */
    private final int QUERY_MONTH_NUMBER = 12;
    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";

    /** Referencia a la vista con databinding */
    private FragmentMycalendarBinding mViewBinding;
    /** Referencia al view model */
    private MyCalendarVM mViewModel;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private OnFragmentInteractionListener mListener;


    /** Referencia al servicio de base de datos de Firebase Cloud Firestore */
    private FirebaseFirestore mFirebaseFirestore;
    /** Registro de escucha en la peticion de usuarios del grupo */
    private ListenerRegistration mGroupUsersListener;
    /** Registro de escucha en la peticion de tipos de turnos */
    private ListenerRegistration mShiftTypesListener;
    /** Listado de registros de escucha en las peticiones de turnos de los usuarios */
    private ArrayList<ListenerRegistration> mUserShiftsListeners;

    /** Referencia al usuario conectado */
    private FirebaseUser mFirebaseUser;
    /** Referencia al grupo de trabajo*/
    private UserWorkgroup mWorkgroup;

    /** Numero de semanas del año actual */
    private int mTotalWeeks;
    /** Posición actual del view pager */
    private int mCurrentPosition;
    /** Fecha del primer dia que se llega a mostrar en el view pager */
    private DateTime mInitMonth;

    /** Referencia al adaptador del view pager */
    PagerAdapter mPagerAdapter;

    /** Indicador para identificar el tipo de calendario actual */
    private boolean mPersonalSchedule = true;
    /** Indicador para comprobar cuando se hicieron cambios en el calendario */
    private boolean mMadeChanges;
    /** Horas maximas semanales */
    private AtomicLong mWeeklyHours;
    /** Indicador del modo edición */
    private boolean mEditMode;

    /** Listado de usuarios del grupo */
    private ArrayList<UserRef> mGroupUsersRef;
    /** Mapa con los tipos de turnos */
    private Map<String, ShiftType> mShiftTypes;
    /** Mapa con las listas segun los tipos de cambios */
    private HashMap<String, ArrayList<Shift>> mShiftChanges;


    /**
     * Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param workgroup Grupo de trabajo del calendario
     * @return instancia de la clase MyCalendarFragment
     */
    public static MyCalendarFragment newInstance(UserWorkgroup workgroup) {
        MyCalendarFragment f = new MyCalendarFragment();

        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        f.setArguments(args);
        return f;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(CURRENT_WORKGROUP_KEY);
        }

    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Infla la vista y se referencia mediante Databinding.
     * Se reserva memoria y se vinculan observadores del view model MyCalendarVM
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentMycalendarBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        //Init month date
        int mInitMonthOffset = (QUERY_MONTH_NUMBER / 2 -1);
        DateTime now = DateTime.now();
        mTotalWeeks = now.weekyear().getMaximumValue();
        mInitMonth = new DateTime().withZone(DateTimeZone.UTC).withTime(now.withTimeAtStartOfDay().toLocalTime());
        mInitMonth = mInitMonth.minusMonths(mInitMonthOffset).withDayOfMonth(1);

        //Init variables
        mUserShiftsListeners = new ArrayList<>();
        mShiftTypes = new HashMap<>();
        mShiftChanges = new HashMap<>();
        mMadeChanges = false;

        //Observers
        mViewModel = ViewModelProviders.of(this).get(MyCalendarVM.class);
        mViewModel.setEditMode(false);
        mViewModel.getEditMode().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if(aBoolean != null){
                    mEditMode = aBoolean;
                    if(mEditMode){
                        mViewBinding.floatingButtonEdit.setImageResource(R.drawable.ic_save_black_24dp);
                    } else {
                        mViewBinding.floatingButtonEdit.setImageResource(R.drawable.ic_mode_edit_black_24dp);
                    }
                }
            }
        });
        mWeeklyHours = new AtomicLong(0);
        mViewModel.setWeeklyHours(0);
        mViewModel.getWeeklyHours().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                if(aLong != null) mWeeklyHours.set(aLong);
            }
        });

        //Listeners
        if (TextUtils.equals(mWorkgroup.getRole(), UserRoles.MANAGER.toString()) && !mPersonalSchedule) {
            mViewBinding.floatingButtonEdit.setVisibility(View.VISIBLE);
        } else {
            mViewBinding.floatingButtonEdit.setVisibility(View.GONE);
        }
        mViewBinding.floatingButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditMode){
                    int changesSize = mShiftChanges.get(getString(R.string.data_changes_added)).size()
                            + mShiftChanges.get(getString(R.string.data_changes_removed)).size()
                            + mShiftChanges.get(getString(R.string.data_changes_editedNew)).size()
                            + mShiftChanges.get(getString(R.string.data_changes_editedOld)).size();
                    if(changesSize > 0){
                        ConfirmChangesDialog dialog = ConfirmChangesDialog.newInstance(mShiftChanges, (HashMap<String, ShiftType>) mShiftTypes, mGroupUsersRef);
                        dialog.show(getChildFragmentManager(),"confirmChanges");
                    } else {
                        mViewModel.setEditMode(false);
                    }
                } else {
                    mViewModel.setEditMode(true);
                    ((AppCompatActivity) mContext).invalidateOptionsMenu();
                }
            }
        });

        return mViewBinding.getRoot();
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Llamado despues de crear la vista
     * Se inicializan datos, se vincula las escucha de la peticion de usuarios del grupo y se hace una petición
     * para obtener los tipos de turnos y las horas máximas semanales
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentSwapped(R.string.fragment_mycalendar);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        queryWeeklyHours();
        attatchWorkgroupUsersListener();
        queryShiftTypes();

        Log.d(TAG, "Start MyCalendarFragment");
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Llamado despues de onViewCreated.
     * Recupera los datos guarados en {@link #onSaveInstanceState}.
     * Dependiendo de {@link #mPersonalSchedule} se crea un adaptador para meses o para semanas
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null){
            mCurrentPosition = savedInstanceState.getInt(CURRENT_ADAPTER_POSITION);
            mPersonalSchedule = savedInstanceState.getBoolean(CURRENT_PERSONAL_SCHEDULE);
        } else {
            mCurrentPosition = (QUERY_MONTH_NUMBER / 2 - 1);
            mPersonalSchedule = true;
        }
        if(mPersonalSchedule){
            mPagerAdapter = new MonthSlidePagerAdapter(getChildFragmentManager());
        } else {
            mPagerAdapter = new WeekSlidePagerAdapter(getChildFragmentManager());
        }

        mViewBinding.viewPagerMonths.setAdapter(mPagerAdapter);
        mViewBinding.viewPagerMonths.setCurrentItem(mCurrentPosition);
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama antes de destruirse la actividad
     * Se guardan los datos para poder ser restablecidos al volver a la actividad.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_ADAPTER_POSITION, mViewBinding.viewPagerMonths.getCurrentItem());
        outState.putBoolean(CURRENT_PERSONAL_SCHEDULE, mPersonalSchedule);

    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al destruirse la vista.
     * Se desvinculan todas las escuchas.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mGroupUsersListener != null) {
            mGroupUsersListener.remove();
        }
        if(mShiftTypesListener != null){
            mShiftTypesListener.remove();
        }
        for (Iterator<ListenerRegistration> iterator = mUserShiftsListeners.iterator(); iterator.hasNext(); ) {
            ListenerRegistration userShiftListener = iterator.next();
            if (userShiftListener != null) {
                userShiftListener.remove();
            }
            iterator.remove();
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
        inflater.inflate(R.menu.menu_mycalendar, menu);
    }

    /**
     * {@inheritDoc}
     * Callback del ciclo de vida
     * Se prepara la vista cambiando el color de los iconos a blanco.
     * Dependiendo del tipo de calendario actual se cambia el icono.
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_mycalendar_switch) {
                if (mPersonalSchedule) {
                    menuItem.setIcon(R.drawable.ic_schedule_black_24dp);
                } else {
                    menuItem.setIcon(R.drawable.ic_mycalendar_black_24dp);
                }
            }
            Drawable icon = menuItem.getIcon();
            if (icon != null) {
                icon.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Responde cuando se selecciona un elemento del menu.
     * Redirecciona a las pantallas de lista de solicitudes de turnos, al otro tipo de calendario y
     * a la actividad de configuración del grupo.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_mycalendar_requests:
                Intent changeRequestsIntent = new Intent(mContext, ChangeRequestsActivity.class);
                changeRequestsIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                changeRequestsIntent.putExtra(getString(R.string.data_int_workgroup), mWorkgroup);
                changeRequestsIntent.putExtra(getString(R.string.data_int_shiftTypes), (HashMap<String, ShiftType>)mShiftTypes);
                changeRequestsIntent.putExtra(getString(R.string.data_int_users), mGroupUsersRef);
                startActivity(changeRequestsIntent);

                return true;
            case R.id.action_mycalendar_switch:
                mPersonalSchedule = !mPersonalSchedule;
                ((AppCompatActivity) mContext).invalidateOptionsMenu();

                mCurrentPosition = mViewBinding.viewPagerMonths.getCurrentItem();
                mPagerAdapter = changeAdapter();

                mViewBinding.viewPagerMonths.setAdapter(mPagerAdapter);
                mViewBinding.viewPagerMonths.setCurrentItem(mCurrentPosition);


                return true;
            case R.id.action_mycalendar_settings:

                Intent workgroupSettingsIntent = new Intent(mContext, WorkgroupSettingsActivity.class);
                workgroupSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                workgroupSettingsIntent.putExtra(getString(R.string.data_int_workgroup), mWorkgroup);
                workgroupSettingsIntent.putExtra(getString(R.string.data_int_hours), mWeeklyHours);
                //workgroupSettingsIntent.putExtra(getString(R.string.data_int_users), mGroupUsersRef);
                startActivity(workgroupSettingsIntent);
                return true;
            default:
                return false;
        }

    }

    /**
     * Este metodo aplica los cambios de calendario una vez se han confirmado en el cuadro de dialogo ConfirmChangesDialog
     * Escribe en base de datos. Llamado en {@link #onConfirmChanges}
     */
    private void applyChanges() {

        HashSet<String> changedUsers = new HashSet<>();

        //Added
        List<Shift> addedShifts =  mShiftChanges.get(getString(R.string.data_changes_added));
        for (Iterator<Shift> iterator = addedShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document();
            shift.setId(docRef.getId());
            docRef.set(shift);
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }

        //Removed
        List<Shift> removedShifts=  mShiftChanges.get(getString(R.string.data_changes_removed));
        for (Iterator<Shift> iterator = removedShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document(shift.getId());
            docRef.delete();
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }

        //Edited or user change
        List<Shift> editedNewShifts=  mShiftChanges.get(getString(R.string.data_changes_editedNew));
        for (Iterator<Shift> iterator = editedNewShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document();
            shift.setId(docRef.getId());
            docRef.set(shift);
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }
        List<Shift> editedOldShifts=  mShiftChanges.get(getString(R.string.data_changes_editedOld));
        for (Iterator<Shift> iterator = editedOldShifts.iterator(); iterator.hasNext(); ) {
            Shift shift = iterator.next();
            DocumentReference docRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(shift.getUserId())
                    .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                    .collection(getString(R.string.data_ref_shifts)).document(shift.getId());
            docRef.delete();
            changedUsers.add(shift.getUserId());
            iterator.remove();
        }

        //Set schedule changed for users
        HashMap<String, String> notifData = new HashMap<>();
        notifData.put(getString(R.string.data_key_displayname), mWorkgroup.getDisplayName());
        for (String userId : changedUsers) {
            CollectionReference notifRef = mFirebaseFirestore.collection(getString(R.string.data_ref_messaging)).document(userId)
                    .collection(getString(R.string.data_ref_workgroups));
            notifRef.document(mWorkgroup.getWorkgroupId()).set(notifData);
        }
    }

    /**
     * Este metodo devuelve el adaptador apropiado y calcula la posición correcta cuando se cambia de un tipo
     * de calendario a otro. Llamado en {@link #onOptionsItemSelected}
     * @return Adaptador del view pager
     */
    private PagerAdapter changeAdapter() {
        PagerAdapter pagerAdapter;
        mViewModel.setEditMode(false);
        if (mPersonalSchedule) {
            mViewBinding.floatingButtonEdit.setVisibility(View.GONE);
            DateTime lastMonth = mInitMonth.plusWeeks(mCurrentPosition).withDayOfWeek(DateTimeConstants.SUNDAY);
            mCurrentPosition = Months.monthsBetween(mInitMonth, lastMonth).getMonths();

            pagerAdapter = new MonthSlidePagerAdapter(getChildFragmentManager());
        } else {
            if (TextUtils.equals(mWorkgroup.getRole(), UserRoles.MANAGER.toString())) {
                mViewBinding.floatingButtonEdit.setVisibility(View.VISIBLE);
            }

            DateTime start = mInitMonth.toDateTime();
            DateTime end = start.plusMonths(mCurrentPosition);
            DateTime todayd = DateTime.now();
            if(todayd.getMonthOfYear() == end.getMonthOfYear()){
                end = end.withWeekOfWeekyear(todayd.getWeekOfWeekyear());
            }
            start = start.withDayOfWeek(DateTimeConstants.MONDAY);
            end = end.withDayOfWeek(DateTimeConstants.MONDAY);

            mCurrentPosition = Weeks.weeksBetween(start,end).getWeeks();

            //Reset changes map
            mShiftChanges.put(getString(R.string.data_changes_added), new ArrayList<Shift>());
            mShiftChanges.put(getString(R.string.data_changes_removed), new ArrayList<Shift>());
            mShiftChanges.put(getString(R.string.data_changes_editedNew), new ArrayList<Shift>());
            mShiftChanges.put(getString(R.string.data_changes_editedOld), new ArrayList<Shift>());
            mMadeChanges = false;
            pagerAdapter = new WeekSlidePagerAdapter(getChildFragmentManager());
        }
        return pagerAdapter;
    }

    /**
     * Vincula la escucha de la petición de usuarios dentro del grupo, los cambios se obtienen en tiempo real.
     * Llamado dentro de {@link #onViewCreated}
     */
    private void attatchWorkgroupUsersListener() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_users));
        mGroupUsersRef = new ArrayList<>();
        mGroupUsersListener = workgroupsUsersColl.orderBy(getString(R.string.data_key_shortname))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                mGroupUsersRef.add(userData);
                                break;
                            case MODIFIED:
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Modified, same position
                                    mGroupUsersRef.set(docChange.getOldIndex(), userData);
                                } else {
                                    //Modified, differnt position
                                    mGroupUsersRef.remove(docChange.getOldIndex());
                                    mGroupUsersRef.add(docChange.getNewIndex(), userData);
                                }
                                break;
                            case REMOVED:
                                //Removed
                                mGroupUsersRef.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
            }
        });

    }

    /**
     * Hace una petición a base de datos de los tipos de turnos. Llamado dentro de {@link #onViewCreated}
     */
    private void queryShiftTypes() {
        CollectionReference workgroupsUsersColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shiftTypes));

        mShiftTypesListener = mViewModel.loadShiftTypes(workgroupsUsersColl);
        mShiftTypes = mViewModel.getShiftTypes().getValue();
    }

    /**
     * Hace una petición a base de datos de el maximo de horas semanales. Llamado dentro de {@link #onViewCreated}
     */
    private void queryWeeklyHours(){
        final String weeeklyHoursKey = getString(R.string.data_key_weeklyhours);
        mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String, Object> data = task.getResult().getData();
                    mViewModel.setWeeklyHours((long) data.get(weeeklyHoursKey));
                }
            }
        });
    }

    /**
     * Implementacion de la interfaz de comunicación de ConfirmChangesDialog.
     * Una vez se confirman se aplican en base de datos.
     */
    @Override
    public void onConfirmChanges() {
        applyChanges();
        mMadeChanges = true;
        mViewModel.setEditMode(false);
        ((AppCompatActivity) mContext).invalidateOptionsMenu();

    }

    /**
     * Implementacion de la interfaz de comunicación de WeekPageListener.
     * Una vez se confirma la solicitud se escribe en base de datos.
     * @param request Solicitud de cambio de turno
     */
    @Override
    public void onNewChangeRequest(ChangeRequest request) {
        CollectionReference changeRequestsColl = mFirebaseFirestore
                .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_changeRequests));
        DocumentReference docRef = changeRequestsColl.document();
        request.setId(docRef.getId());
        docRef.set(request);
    }

    /**
     * Implementacion de la interfaz de comunicación de WeekPageListener.
     * Se pide comprobar si hay un turno existente en la fecha proporcionada
     * @param uid Identificador del usuario
     * @param date Fecha del dia a comprobar
     */
    @Override
    public boolean hasShiftOnDate(String uid, Date date) {
        DateTime shiftWeek = new DateTime(date).withDayOfWeek(DateTimeConstants.MONDAY);
        DateTime firstWeek = mInitMonth.withDayOfWeek(DateTimeConstants.MONDAY);
        int weekPos = Weeks.weeksBetween(firstWeek, shiftWeek).getWeeks();
        LinkedHashMap<String, ArrayList<Shift>> shiftListMap = ((WeekSlidePagerAdapter)mPagerAdapter).getShiftListMapRef().get(weekPos);
        List<Shift> shiftList =  shiftListMap.get(uid);
        boolean hasShift = false;
        for (Shift sh : shiftList) {
            if(sh.getDate().getTime() == date.getTime()){
                hasShift = true;
                break;
            }
        }
        return hasShift;
    }

    /**
     * La clase MonthSlidePagerAdapter es una clase adaptador de view pager encargada de proporcionar los datos a la vista.
     * Cada pagina es un fragment que representa un mes, se hace la petición de los datos de lo que abarca cada pagina.
     * Extiende FragmentStatePagerAdapter
     *
     * @author Julio García
     * @see FragmentStatePagerAdapter
     */
    private class MonthSlidePagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Constructor por defecto para pasarle argumentos a la clase de la que hereda
         * @param fm Manager de fragmentos
         */
        MonthSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Construye el fragmento de cada pagina. Cada fragmento contiene los datos de un mes ademas
         * de los dias de principio de semana del mes anterior y de final de semana del mes siguiente.
         * @param position Posición de la pagina
         * @return Fragmento de cada pagina
         */
        @Override
        public Fragment getItem(int position) {

            DateTime month = mInitMonth.plusMonths(position).withDayOfMonth(1);
            final ArrayList<Shift> shiftList = new ArrayList<>();

            MonthPageFragment pageFragment = MonthPageFragment.newInstance(month, shiftList, (HashMap<String, ShiftType>) mShiftTypes);
            queryShiftData(pageFragment, month, shiftList);

            return pageFragment;
        }

        /**
         * Devuelve el numero de paginas
         * Se construye un total de 12 meses siendo el actual el mes central.
         * @return Numero de paginas
         */
        @Override
        public int getCount() {
            return QUERY_MONTH_NUMBER;
        }

        /**
         * Petición a base de datos de los turnso del usuario desde el lunes de la primera semana al domingo de la ultima semana.
         * @param pageFragment Referencia al fragmento
         * @param month Fecha del primer dia del mes correspondiente.
         * @param shiftList Referencia de la lista de turnos a rellenar.
         */
        private void queryShiftData(final MonthPageFragment pageFragment, DateTime month, final ArrayList<Shift> shiftList) {

            if (mFirebaseUser != null) {
                CollectionReference shiftsColl = mFirebaseFirestore
                        .collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                        .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                        .collection(getString(R.string.data_ref_shifts));

                Date firstDay = month.withDayOfWeek(DateTimeConstants.MONDAY).toDate();
                Date lastDay =  month.dayOfMonth().withMaximumValue().withDayOfWeek(DateTimeConstants.SUNDAY).toDate();
                String dateKey = getString(R.string.data_key_date);
                shiftsColl.whereGreaterThanOrEqualTo(dateKey, firstDay).whereLessThanOrEqualTo(dateKey, lastDay).orderBy(dateKey, Query.Direction.ASCENDING).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot doc : task.getResult()) {
                                        if (doc != null && doc.exists()) {
                                            shiftList.add(doc.toObject(Shift.class));
                                        }
                                    }
                                    pageFragment.notifyGridDataSetChanged();
                                } else {
                                    if (task.getException() != null) {
                                        Log.e(TAG, task.getException().getMessage());
                                    }
                                }
                            }
                        });
            }
        }
    }

    /**
     * La clase MonthSlidePagerAdapter es una clase adaptador de view pager encargada de proporcionar los datos a la vista.
     * Cada pagina es un fragment que representa una semana, se hace la petición de los datos de lo que abarca cada pagina.
     * Se obtienen los datos de todos los usuarios del grupo para la semana correspondiente..
     * Extiende FragmentStatePagerAdapter
     *
     * @author Julio García
     * @see FragmentStatePagerAdapter
     */
    private class WeekSlidePagerAdapter extends FragmentStatePagerAdapter {

        SparseArray<ScheduleWeekPageFragment> fragmentsRef = new SparseArray<>();
        SparseArray<LinkedHashMap<String, ArrayList<Shift>>> shiftListMapRef = new SparseArray<>();
        WeekSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Construye el fragmento de cada pagina. Cada fragmento contiene los datos de una semana de
         * todos los usuarios del grupo.
         * @param position Posición de la pagina
         * @return Fragmento de cada pagina
         */
        @Override
        public Fragment getItem(int position) {

            DateTime weekStart = mInitMonth.plusWeeks(position).withDayOfWeek(DateTimeConstants.MONDAY);

            final LinkedHashMap<String, ArrayList<Shift>> shiftListMap = new LinkedHashMap<>();
            shiftListMapRef.put(position, shiftListMap);
            if(fragmentsRef.get(position) == null){
                fragmentsRef.put(position, ScheduleWeekPageFragment.newInstance(position, mWorkgroup.getRole(), weekStart, mGroupUsersRef,
                        shiftListMap, mShiftChanges));
                queryScheduleData(fragmentsRef.get(position), weekStart, shiftListMap);
            }

            return fragmentsRef.get(position);
        }

        /**
         * Devuelve el numero de paginas
         * Se construye el mismo numero de paginas como de semanas hay en el año.
         * @return Numero de paginas
         */
        @Override
        public int getCount() {
            return mTotalWeeks;
        }

        /**
         * Petición a base de datos desde el lunes al domingo de la semana correspondiente
         * @param pageFragment Referencia al fragmento
         * @param date Fecha del lunes de la semana correspondiente
         * @param shiftListMap Mapa del listado de turnos con el identificador del usuario como clave a rellenar.
         */
        private void queryScheduleData(final ScheduleWeekPageFragment pageFragment, DateTime date, Map<String, ArrayList<Shift>> shiftListMap) {

            for (UserRef userUid : mGroupUsersRef) {
                final ArrayList<Shift> userShifts = new ArrayList<>();
                String uid = userUid.getUid();
                shiftListMap.put(uid, userShifts);

                CollectionReference userShiftsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(userUid.getUid())
                        .collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                        .collection(getString(R.string.data_ref_shifts));

                Date firstDay = date.withDayOfWeek(DateTimeConstants.MONDAY).toDate();
                Date lastDay = date.withDayOfWeek(DateTimeConstants.SUNDAY).toDate();

                String dateKey = getString(R.string.data_key_date);
                mMadeChanges = false;

                ListenerRegistration userShiftListener =
                        userShiftsColl.whereGreaterThanOrEqualTo(dateKey, firstDay).whereLessThanOrEqualTo(dateKey, lastDay)
                                .orderBy(dateKey, Query.Direction.ASCENDING)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                                        if(e != null || mMadeChanges){
                                            return;
                                        }
                                        for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                                            DocumentSnapshot doc = docChange.getDocument();

                                            if (doc.exists()) {
                                                Shift shift = doc.toObject(Shift.class);

                                                switch (docChange.getType()) {
                                                    case ADDED:
                                                        //Added
                                                        userShifts.add(shift);
                                                        break;
                                                    case MODIFIED:
                                                        if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                                            //Modified, same position
                                                            userShifts.set(docChange.getOldIndex(), shift);
                                                        } else {
                                                            //Modified, differnt position
                                                            userShifts.remove(docChange.getOldIndex());
                                                            userShifts.add(docChange.getNewIndex(), shift);
                                                        }
                                                        break;
                                                    case REMOVED:
                                                        //Removed
                                                        userShifts.remove(docChange.getOldIndex());
                                                        break;
                                                }
                                            }
                                        }
                                        Collections.sort(userShifts);
                                        pageFragment.notifyGridDataSetChanged();
                                    }
                                });
                mUserShiftsListeners.add(userShiftListener);
            }
        }
        SparseArray<LinkedHashMap<String, ArrayList<Shift>>> getShiftListMapRef() {
            return shiftListMapRef;
        }
    }

}
