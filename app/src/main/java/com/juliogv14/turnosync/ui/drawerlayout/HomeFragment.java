package com.juliogv14.turnosync.ui.drawerlayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.GlobalWorkgroup;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentHomeBinding;
import com.juliogv14.turnosync.databinding.ItemWorkgroupBinding;
import com.juliogv14.turnosync.utils.FormUtils;
import com.juliogv14.turnosync.utils.InterfaceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * La clase HomeFragment es el fragmento por defecto usado en DrawerActivity. Muestra el listado de grupos
 * del usuario y permite seleccionar un grupo navegando a la pantalla de calendario MyCalendarFragment del grupo.
 * Permite crear nuevos grupos de trabajo.
 * Permite seleccionar un grupo para mostrar el campo información.
 * Extiende Fragment.
 * Implementa la interfaz de escucha de CreateWorkgroupDialog.
 *
 * @author Julio García
 * @see Fragment
 * @see DrawerActivity
 * @see CreateWorkgroupDialog.CreateWorkgroupListener
 * @see GroupItemsAdapter
 * @see ActionMode
 */
public class HomeFragment extends Fragment
        implements CreateWorkgroupDialog.CreateWorkgroupListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String WORKGROUP_LIST_KEY = "workgroupList";

    /** Referencia a la vista con databinding */
    protected FragmentHomeBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private OnHomeFragmentInteractionListener mListener;

    /** Referencia al servicio de base de datos de Firebase Cloud Firestore */
    private FirebaseFirestore mFirebaseFirestore;
    /** Referencia al usuario conectado */
    private FirebaseUser mFirebaseUser;

    /** Referencia al adaptador del Gridview */
    private GroupItemsAdapter mGridAdapter;
    /** Lista de grupos del usuario*/
    private ArrayList<UserWorkgroup> mWorkgroupsList;
    /** Menu contextual que aprece al mantener pulsado un grupo */
    private ActionMode mActionMode;
    /** Grupo seleccionado con el menú contextual  */
    private UserWorkgroup mSelectedWorkgroup;


    /**
     * Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param workgroupList Lista de grupos del usuario
     * @return instancia de la clase HomeFragment
     */
    public static HomeFragment newInstance(ArrayList<UserWorkgroup> workgroupList) {
        HomeFragment f = new HomeFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(WORKGROUP_LIST_KEY, workgroupList);
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
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHomeFragmentInteractionListener");
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
            mWorkgroupsList = args.getParcelableArrayList(WORKGROUP_LIST_KEY);
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
        mViewBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se inicializa la vista y las variables. Se crea el adaptadorSe vinculan las escuchas del boton para crear grupos y
     * de pulsar sobre un grupò en el adaptador.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mGridAdapter = new GroupItemsAdapter((Activity) mListener, R.layout.fragment_home, mWorkgroupsList);

        mViewBinding.gridViewGroupDisplay.setAdapter(mGridAdapter);
        mListener.onFragmentSwapped(R.string.fragment_home);


        mViewBinding.floatingButtonNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateWorkgroupDialog dialog = new CreateWorkgroupDialog();
                dialog.show(getChildFragmentManager(), "cwk");
            }
        });

        mViewBinding.gridViewGroupDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                UserWorkgroup wk = mWorkgroupsList.get(position);
                if (wk.isSelected()) {
                    handleSelectedWorkgroup(wk);
                } else {
                    mListener.onWorkgroupSelected(wk);
                }


            }
        });

        mViewBinding.gridViewGroupDisplay.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                UserWorkgroup wk = mWorkgroupsList.get(position);
                handleSelectedWorkgroup(wk);
                return true;
            }
        });
        Log.d(TAG, "Start HomeFragment");
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se notifica al adaptador para que recargue la vista.
     */
    @Override
    public void onResume() {
        super.onResume();
        mGridAdapter.notifyDataSetChanged();
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
     * Implementación de la interfaz de escucha con el cuadro de dialogo CreateWorkgroupDialog
     * @see CreateWorkgroupDialog.CreateWorkgroupListener
     * Crea el grupo a partir de los datos proporcionados
     *
     * @param name Nombre del grupo
     * @param description Descripción del grupo
     */
    @Override
    public void onDialogPositiveClick(String name, String description) {
        if (mFirebaseUser != null) {
            //Database References
            String userUID = mFirebaseUser.getUid();
            CollectionReference globalWorkgroupsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups));
            CollectionReference userWorkgroupsColl = mFirebaseFirestore.collection(getString(R.string.data_ref_users))
                    .document(userUID).collection(getString(R.string.data_ref_workgroups));

            //Global workgroup list
            DocumentReference globalWorkgroupRef = globalWorkgroupsColl.document();
            GlobalWorkgroup workgroup = new GlobalWorkgroup(globalWorkgroupRef.getId(), name, description, (long)40, userUID);
            globalWorkgroupRef.set(workgroup);

            //Add user to workgroup
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            UserRef userData = new UserRef(mFirebaseUser.getUid());
            userData.setActive(true);
            String displayName = prefs.getString(getString(R.string.data_key_displayname), "Mng");

            String shortName = FormUtils.slugify(displayName);
            String[] split = shortName.trim().split(" ");

            if(split.length == 1){
                shortName = shortName.substring(0,3);
            } else if(split.length == 2){
                shortName = split[0].substring(0,2);
                shortName +=split[1].substring(0,1);
            } else {
                shortName = split[0].substring(0,2);
                shortName +=split[1].substring(0,1);
                shortName +=split[2].substring(0,1);
            }
            userData.setShortName(shortName);
            globalWorkgroupRef.collection(getString(R.string.data_ref_users))
                    .document(mFirebaseUser.getUid()).set(userData);

            //Personal workgroup list
            DocumentReference userWorkgroupRef = userWorkgroupsColl.document(globalWorkgroupRef.getId());
            UserWorkgroup userWorkgroup = new UserWorkgroup(globalWorkgroupRef.getId(), name, description, UserRoles.MANAGER.toString());
            userWorkgroupRef.set(userWorkgroup);

            Log.d(TAG, "Create workgroup dialog return");
        }
    }

    /**
     * Maneja los cambios en la vista cuando se mantiene seleccionado un grupo.
     * Muestra el menu contextual.
     * @see ActionMode
     *
     * @param workgroup Grupo seleccionado.
     */
    private void handleSelectedWorkgroup(UserWorkgroup workgroup) {

        if (!workgroup.isSelected()) {
            if (mSelectedWorkgroup != null) {
                mSelectedWorkgroup.setSelected(false);
            }
            mSelectedWorkgroup = workgroup;
            mSelectedWorkgroup.setSelected(true);

            ToolbarActionModeCallback tb = new ToolbarActionModeCallback(mContext, mSelectedWorkgroup);
            if (mActionMode == null) {
                mActionMode = ((AppCompatActivity) mContext)
                        .startSupportActionMode(tb);
            }
            mActionMode.setTitle(workgroup.getDisplayName() + " selected");


        } else {
            mActionMode.finish();
        }
    }

    /**
     * Permite notificar al adaptador que recargue la vista mediante la referencia al fragmento.
     */
    public void notifyGridDataSetChanged() {
        if (mGridAdapter != null) {
            mGridAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface OnHomeFragmentInteractionListener extends OnFragmentInteractionListener {
        void onWorkgroupSelected(UserWorkgroup workgroup);
    }

    /**
     * La clase GroupItemAdapter es la clase encargada de proporcionar la vista en cuadricula con los grupos
     * de usuario.
     * Extiende ArrayAdapter.
     *
     * @author Julio García
     * @see ArrayAdapter
     */
    private class GroupItemsAdapter extends ArrayAdapter<UserWorkgroup> {

        /** Referencia a la vista del elemento con databinding */
        private ItemWorkgroupBinding itemBinding;


        /**
         * Constructor por defecto pasando argumentos a la clase de la que hereda.
         */
        GroupItemsAdapter(@NonNull Context context, int resource, @NonNull List<UserWorkgroup> objects) {
            super(context, resource, objects);
        }

        /**
         * Metodo en el que se construye la vista del elemento a dibujar.
         * Se infla un elemento de la vista bajo la vista padre
         * Se crea la referencia de la vista mediante databinding.
         * Se rellenan los datos de la vista con el grupo correspondiente a la posición.
         *
         * @param position Posición del elemento
         * @param convertView Vista del elemento
         * @param parent Vista padre
         * @return Vista del elemento a dibujar
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_workgroup, parent, false);
            }
            itemBinding = DataBindingUtil.bind(convertView);
            int size = mViewBinding.gridViewGroupDisplay.getColumnWidth();
            convertView.setLayoutParams(new GridView.LayoutParams(size, size));

            UserWorkgroup workgroup = getItem(position);
            if (workgroup != null) {
                String display = workgroup.getDisplayName();
                itemBinding.textViewGroupName.setText(display);
                itemBinding.textViewGroupLevel.setText(workgroup.getRole());
                UserRoles role = UserRoles.valueOf(workgroup.getRole());

                GradientDrawable background = (GradientDrawable) convertView.getBackground();
                int strokeWidth = InterfaceUtils.dpToPx(mContext, 1.5f);
                switch (role){
                    case MANAGER:
                        background.setStroke(strokeWidth, ContextCompat.getColor(mContext, R.color.workgroup_manager));
                        background.setColor(ContextCompat.getColor(mContext, R.color.workgroup_manager_bg));
                        itemBinding.textViewGroupLevel.setTextColor(ContextCompat.getColor(mContext, R.color.workgroup_manager));
                        break;
                    case USER:
                        background.setStroke(strokeWidth, ContextCompat.getColor(mContext, R.color.workgroup_user));
                        background.setColor(ContextCompat.getColor(mContext, R.color.workgroup_user_bg));
                        itemBinding.textViewGroupLevel.setTextColor(ContextCompat.getColor(mContext, R.color.workgroup_user));
                        break;
                    case GUEST:
                        background.setStroke(strokeWidth, ContextCompat.getColor(mContext, R.color.workgroup_guest));
                        break;
                }
            }
            return convertView;
        }
    }

    /**
     * La clase ToolbarActionModeCallback es la clase encargada de manejar el menu contextual al seleccionar
     * un grupo.
     * Extiende ActionMode.Callback.
     *
     * @author Julio García
     * @see ActionMode.Callback
     */
    public class ToolbarActionModeCallback implements ActionMode.Callback {

        /** Grupo seleccionado */
        UserWorkgroup mWorkgroup;
        /** Contexto */
        private Context mContext;


        /**
         * Instancia la clase con los argumentos.
         * @param mContext Contexto
         * @param workgroup Grupo seleccionado
         */
        ToolbarActionModeCallback(Context mContext, UserWorkgroup workgroup) {
            this.mContext = mContext;
            this.mWorkgroup = workgroup;
        }

        /**
         * Callback del menu contextual.
         * Creación del menu contextual inflando la vista
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_home, menu);
            return true;
        }

        /**
         * Callback del menu contextual. Se llama cuando se invalida el menu o despues de crease por primera vez
         * Se prepara la vista cambiando el color de los iconos a blanco.
         */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            for (int i = 0; i < menu.size(); i++) {
                Drawable icon = menu.getItem(i).getIcon();
                if (icon != null) {
                    icon.mutate();
                    icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                }
            }

            return true;
        }

        /**
         * Callback del menu contextual. Se llama al seleccionar una opcion del menu
         * Muestra la informacion del grupo seleccionado.
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_home_view:
                    showInfoDialog();
                    mode.finish();
                    break;
                default:
                    return false;
            }
            return true;
        }

        /**
         * Callback del menu contextual.
         * Al destruirse el menu contextual se invalida la referencia al grupo seleccionado y
         * la referencia del menu contextual.
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mActionMode != null) {
                mActionMode = null;
                mWorkgroup.setSelected(false);
                mSelectedWorkgroup = null;
            }
        }


        /**
         * Se crea un cuadro de dialogo mostrando el campo información del grupo seleccionado
         */
        private void showInfoDialog(){
            new AlertDialog.Builder(mContext).setTitle(mSelectedWorkgroup.getDisplayName())
                    .setMessage(mSelectedWorkgroup.getInfo())
                    .setPositiveButton(getString(R.string.dialog_workgroupInfo_button_close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }

}
