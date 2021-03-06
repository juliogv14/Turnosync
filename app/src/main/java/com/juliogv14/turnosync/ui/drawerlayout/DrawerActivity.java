package com.juliogv14.turnosync.ui.drawerlayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityDrawerBinding;
import com.juliogv14.turnosync.databinding.HeaderDrawerBinding;
import com.juliogv14.turnosync.ui.account.LoginActivity;
import com.juliogv14.turnosync.ui.settings.SettingsActivity;
import com.juliogv14.turnosync.utils.FormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * La clase DrawerActivity es la clase principal de la aplicación y que sirve de entrada.
 * Si el usuario aun no ha iniciado sesión se navega hasta LoginActivity para ello antes de interactuar
 * con la aplicación. Es la encargada de controlar el panel lateral de navegación.
 * Esta activa en la mayor parte de la aplicación y las vistas se intercambian mediante fragmentos Fragment.
 * Extiende AppCompatActivity.
 * Implementa la interfaz de escucha del fragmento HomeFragment para cambiar de vista.
 * Implementa la escucha de la vista de navegacion NavigationDrawer para cambiar de vista
 * Implementa la escucha de camibos en la configuración de la aplicación para cambiar los datos del usuario.
 *
 * @author Julio García
 * @see AppCompatActivity
 * @see Fragment
 * @see FirebaseAuth
 * @see FirebaseFirestore
 * @see NavigationView
 * @see SharedPreferences
 */
public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        HomeFragment.OnHomeFragmentInteractionListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /** Referencia a la vista con databinding */
    protected ActivityDrawerBinding mViewBinding;

    /** Referencia al servicio de autenticación de Firebase */
    private FirebaseAuth mFirebaseAuth;
    /** Escucha de cambios del estado de sesión del usuario */
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    /** Referencia al usuario conectado */
    private FirebaseUser mFirebaseUser;

    /** Referencia al servicio de base de datos de Firebase Cloud Firestore */
    private FirebaseFirestore mFirebaseFirestore;
    /** Registro de escucha en la peticion de grupos de trabajo */
    private ListenerRegistration mWorkgroupsListener;
    /** Lista de grupos de trabajo del usuario */
    private ArrayList<UserWorkgroup> mWorkgroupsList;

    /** Referencia al panel lateral */
    private DrawerLayout mDrawerLayout;
    /** Referencia a la vista del encabezado del panel lateral */
    private HeaderDrawerBinding mHeaderBinding;
    /** Referencia a la barra de acciones */
    private ActionBar mToolbar;

    //@{
    /** Claves para conservar datos al recrear la actividad */
    private static final String CURRENT_FRAGMENT_KEY = "currentFragment";
    private static final String RESTORED_FRAGMENT_KEY = "restoredFragment";
    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    //@}

    /** Identificador del fragmento actual */
    private int mCurrentFragmentID;
    /** Referencia al fragmento MyCalendarFragment */
    private MyCalendarFragment mMyCalendarFragment;
    /** Referencia al fragmento HomeFragment */
    private HomeFragment mHomeFragment;
    /** Referencia al grupo seleccionado actualmente */
    private UserWorkgroup mCurrentWorkgroup;

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Al crearse se inicializa la vista. Recupera los datos guarados en {@link #onSaveInstanceState} Se establecen las escuchas del panel lateral y de
     * cambio de configuración. Se crea la escucha de estado de sesión.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        if (savedInstanceState != null) {
            mCurrentFragmentID = savedInstanceState.getInt(CURRENT_FRAGMENT_KEY);
            mMyCalendarFragment = (MyCalendarFragment) getSupportFragmentManager().getFragment(savedInstanceState, RESTORED_FRAGMENT_KEY);
            mCurrentWorkgroup = savedInstanceState.getParcelable(CURRENT_WORKGROUP_KEY);

        } else {
            mCurrentFragmentID = R.string.fragment_home;
        }

        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_drawer);
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        /*------DRAWER-----*/
        Toolbar toolbar = mViewBinding.toolbar;
        setSupportActionBar(toolbar);
        mToolbar = getSupportActionBar();

        mDrawerLayout = (DrawerLayout) mViewBinding.getRoot();
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar,
                R.string.a11y_navigation_drawer_open,
                R.string.a11y_navigation_drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);


        mDrawerToggle.syncState();
        mViewBinding.viewNav.setNavigationItemSelectedListener(this);
        mHeaderBinding = DataBindingUtil.bind(mViewBinding.viewNav.getHeaderView(0));

        /*-------FIREBASE STATE LISTENER-------*/
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    mFirebaseUser = firebaseAuth.getCurrentUser();
                    Log.d(TAG, "Authlistener: logged in user " + mFirebaseUser.getDisplayName());
                    //USER logged in
                    onSignedInInitialize(mFirebaseUser);

                } else {
                    Log.d(TAG, "Authlistener: logged out");
                    //No user logged in
                    onSignedOutCleanup();
                    Intent signInIntent = new Intent(getBaseContext(), LoginActivity.class);
                    signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signInIntent);
                    finish();
                }
            }
        };

        //Shared preferences
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);



    }
    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al principio y al volver desde otra vista.
     * Al iniciar se vincula la escucha de cambio de sesión y se muestra el fragmento actual
     */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "AddAuthStateListener");
        //Reset title and nav coming back from settings
        onFragmentSwapped(mCurrentFragmentID);

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al principio y al volver desde otra vistad.
     * Comprobación de la versión de Google Play Services
     */
    @Override
    protected void onResume() {
        super.onResume();
        FormUtils.checkGooglePlayServices(this);
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al parar la aplicación al ocultarla.
     * Se desvinculan las escuchas del estado de sesión y de la petición de los grupos
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            Log.d(TAG, "RemoveAuthStateListener");
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        if (mWorkgroupsListener != null) {
            mWorkgroupsListener.remove();
        }
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al destruirse la actividad.
     * Se desvincula la escucha de cambios de configuracón
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama antes de destruirse la actividad
     * Se guardan los datos para poder ser restablecidos al volver a la actividad.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT_KEY, mCurrentFragmentID);
        outState.putParcelable(CURRENT_WORKGROUP_KEY, mCurrentWorkgroup);

    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida. Se llama al pulsar el boton de atras del dispositivo.
     * Se cierra el panel si está abierto, vuelve al fragmento principal HomeFragment desde MyCalendarFragment
     * o si no continua con la llamada por defecto.
     */
    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mCurrentFragmentID == R.string.fragment_home) {
                super.onBackPressed();
            } else {
                displaySelectedScreen(R.string.fragment_home);
            }
        }
    }

    /**
     * Inicializa la vista despues de que un usuario se conecta. Se vincula la escucha de la
     * petición de grupos del usuario, se muestra el fragmento principal por defecto y se registra el
     * dispositivo para enviar notificaciones.
     *
     * @param user Usuario conectado
     */
    private void onSignedInInitialize(FirebaseUser user) {
        SharedPreferences shrPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = shrPreferences.edit();
        editor.putString(getString(R.string.data_key_displayname), user.getDisplayName());
        editor.putString(getString(R.string.data_key_email), user.getEmail());
        editor.apply();
        mHeaderBinding.textViewDisplayName.setText(user.getDisplayName());
        mHeaderBinding.textViewEmail.setText(user.getEmail());

        attatchWorkgroupsListener();
        displaySelectedScreen(mCurrentFragmentID);
        registerDevice(shrPreferences);
    }

    /**
     * Limpia la vista al desconectarse un usuario.
     */
    private void onSignedOutCleanup() {
        mHeaderBinding.textViewDisplayName.setText("");
    }


    /**
     * Crea el fragmento correspondiente segun el argumento y lo carga.
     * Se llama dentro de {@link #onSignedInInitialize} ,{@link #onNavigationItemSelected} y {@link #onWorkgroupSelected(UserWorkgroup)}
     * @param itemId Identificador del fragmento a cargar.
     */
    private void displaySelectedScreen(int itemId) {

        mCurrentFragmentID = itemId;
        //creating fragment object
        Fragment fragment = null;
        String tag = "";
        //initializing the fragment object which is selected
        switch (itemId) {
            case R.string.fragment_home:
                mHomeFragment = HomeFragment.newInstance(mWorkgroupsList);
                fragment = mHomeFragment;
                tag = "home";
                break;
            case R.string.fragment_mycalendar:
                tag = "mycalendar";
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if(fragment == null){
                    if(mCurrentWorkgroup != null){
                        mMyCalendarFragment = MyCalendarFragment.newInstance(mCurrentWorkgroup);
                    } else {
                        Toast.makeText(this, R.string.toast_drawer_noWorkgroup, Toast.LENGTH_LONG).show();
                    }
                    
                }
                fragment = mMyCalendarFragment;
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment, tag);
            ft.commit();
        }
    }

    /**
     * Vincula la escucha de la petición de grupos del usuario, los cambios se obtienen en tiempo real.
     * LLamado dentro de {@link #onSignedInInitialize}
     */
    private void attatchWorkgroupsListener() {

        CollectionReference userGroupsRef = mFirebaseFirestore.collection(getString(R.string.data_ref_users)).document(mFirebaseUser.getUid())
                .collection(getString(R.string.data_ref_workgroups));
        mWorkgroupsList = new ArrayList<>();
        mWorkgroupsListener = userGroupsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }
                for (DocumentChange docChange : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot document = docChange.getDocument();
                    if (document.exists()) {
                        UserWorkgroup userWorkgroup = document.toObject(UserWorkgroup.class);

                        if (mCurrentWorkgroup == null) {
                            mCurrentWorkgroup = userWorkgroup;
                        }
                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                mWorkgroupsList.add(userWorkgroup);
                                break;
                            case MODIFIED:
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Modified, same position
                                    mWorkgroupsList.set(docChange.getOldIndex(), userWorkgroup);
                                } else {
                                    //Modified, differnt position
                                    mWorkgroupsList.remove(docChange.getOldIndex());
                                    mWorkgroupsList.add(docChange.getNewIndex(), userWorkgroup);
                                }
                                break;
                            case REMOVED:
                                //Removed
                                mWorkgroupsList.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
                if(mHomeFragment != null){
                    mHomeFragment.notifyGridDataSetChanged();
                }
            }
        });
    }

    //Interfaces implementation


    /**
     * Implementación de la escucha al seleccionar una opcion del panel de navegación.
     * Cambia la vista segun la opción seleccionada. Desconecta al usuario de la aplicación y
     * quita el registro para notificaciones.
     * @param item boton del menu
     * @return True
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_item_home:
                displaySelectedScreen(R.string.fragment_home);
                break;
            case R.id.nav_item_calendar:
                displaySelectedScreen(R.string.fragment_mycalendar);
                break;
            case R.id.nav_item_signout:
                unregisterDevice();
                mFirebaseAuth.signOut();
                break;
            case R.id.nav_item_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(settingsIntent);
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Se llama cuando un elemento de la configuración cambia. Responde a cambios del nombre
     * o email de usuario actualizandolos en base de datos y al cambiar el token de registro.
     * @param sharedPreferences configuración de la aplicación
     * @param key clave del elemento que ha cambiado
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        /*Update Firebase with new settings*/
        if (TextUtils.equals(key, getString(R.string.data_key_displayname))) {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            String displayName = sharedPreferences.getString(key, "");
            if (mFirebaseUser != null && !TextUtils.equals(mFirebaseUser.getEmail(), displayName)) {
                mFirebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(sharedPreferences.getString(key, "")).build())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(DrawerActivity.this, R.string.toast_profile_displayname, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else if (TextUtils.equals(key, getString(R.string.data_key_email))) {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            String email = sharedPreferences.getString(key, "");
            if (mFirebaseUser != null && !TextUtils.equals(mFirebaseUser.getEmail(), email)) {
                mFirebaseUser.updateEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(DrawerActivity.this, R.string.toast_profile_email, Toast.LENGTH_SHORT).show();
                                } else {
                                    if (task.getException() != null) {
                                        Toast.makeText(DrawerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        });
            }
        } else if (TextUtils.equals(key, getString(R.string.data_key_token))){
            registerDevice(sharedPreferences);

        }
    }


    /**
     * Registra la aplicación para poder recibir notificaciónes desde Firebase Cloud Messaging.
     * Esto se hace escribiendo en la base de datos que activa el registro desde la parte del servidor.
     * Se llama desde {@link #onSharedPreferenceChanged} y {@link #onSignedInInitialize}
     * @param sharedPreferences configuración de la aplicación
     */
    private void registerDevice(SharedPreferences sharedPreferences) {
        //Update app token
        String token = sharedPreferences.getString( getString(R.string.data_key_token),"");
        if (!token.isEmpty() && mFirebaseUser != null){
            DocumentReference userRef = mFirebaseFirestore.collection(getString(R.string.data_ref_messaging)).document(mFirebaseUser.getUid());

            //Set user
            Map<String, Object> userMap = new HashMap<>();
            userMap.put(getString(R.string.data_key_uid), mFirebaseUser.getUid());
            userRef.set(userMap, SetOptions.merge());

            //Set device token
            String appId = FirebaseInstanceId.getInstance().getId();
            Map<String, String> device = new HashMap<>();
            device.put(getString(R.string.data_key_deviceid), appId);
            device.put(getString(R.string.data_key_token), token);
            userRef.collection(getString(R.string.data_ref_devices)).document(appId).set(device);

        }
    }


    /**
     * Quita el registro para recibir notificaciones.
     * Se llama desde{@link #onNavigationItemSelected}
     */
    private void unregisterDevice(){
        String appId = FirebaseInstanceId.getInstance().getId();
        DocumentReference userRef = mFirebaseFirestore.collection(getString(R.string.data_ref_messaging)).document(mFirebaseUser.getUid())
        .collection(getString(R.string.data_ref_devices)).document(appId);
        userRef.delete();

    }


    /**
     * Implementación de la interfaz de comunicación para fragmentos
     * Cambia el titulo de la barra de la aplicacion y marca el elemento selecionado en el panel lateral.
     * @param fragmentId identificador de fragmento
     */
    @Override
    public void onFragmentSwapped(int fragmentId) {
        switch (fragmentId) {
            case R.string.fragment_home:
                mToolbar.setTitle(R.string.fragment_home);
                mViewBinding.viewNav.setCheckedItem(R.id.nav_item_home);
                break;
            case R.string.fragment_mycalendar:
                mToolbar.setTitle(mCurrentWorkgroup.getDisplayName());
                mViewBinding.viewNav.setCheckedItem(R.id.nav_item_calendar);
                break;
        }
    }


    /**
     * Implementación de la interfaz de comunicacion con HomeFragment
     * Cambia el fragmento actual por MyCalendarFragment con el grupo de trabajo seleccionado.
     * @see HomeFragment.OnHomeFragmentInteractionListener
     * @param workgroup grupo de trabajo seleccionado
     */
    @Override
    public void onWorkgroupSelected(UserWorkgroup workgroup) {
        mCurrentWorkgroup = workgroup;
        displaySelectedScreen(R.string.fragment_mycalendar);
    }

}