package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.databinding.ItemUserBinding;

import java.util.ArrayList;
import java.util.List;
/**
 * La clase ChangeRequestsAdapter es la clase encargada de proporcionar la vista en forma de lista de elementos
 * de referencias de usuario.
 * Extiende RecyclerView.Adapter.
 *
 * @author Julio García
 * @see RecyclerView.Adapter
 * @see RecyclerView.ViewHolder
 */

public class GroupUsersAdapter extends RecyclerView.Adapter<GroupUsersAdapter.UserViewHolder>{
    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private UserOnClickListener mUserOnClickListener;
    /** Rol del usuario actual */
    private String mRole;
    /** Listado de referencias de usuarios */
    private List<UserRef> mUserList;


    /**
     * Constructor del adaptador
     * @param context Contexto
     * @param listener Clase que implementa la interfaz
     * @param userList Listado de usuarios
     * @param role Rol del usuario
     */
    public GroupUsersAdapter(Context context, UserOnClickListener listener, ArrayList<UserRef> userList, String role) {
        this.mContext = context;
        this.mUserOnClickListener = listener;
        this.mUserList = userList;
        this.mRole = role;

    }

    /**
     * Infla la vista del elemento de un ViewHolder
     * @param parent Vista padre
     * @param viewType Tipo de vista
     * @return ViewHolder del elemento
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemBinding = ItemUserBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new UserViewHolder(itemBinding);
    }

    /**
     * Rellena la vista con el elemento correspondiente a partir de la posición y los datos.
     * @param holder Elemento
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(mUserList.get(position).getShortName());
    }

    /**
     * Devuelve el numero total de elementos
     * @return Tamaño total de la lista de usuarios
     */
    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    /**
     * Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface UserOnClickListener {
        void onClickRemoveUser(String uid);
        void onClickEditUser(int pos);
    }

    /**
     * Esta clase representa la vista de un elemento dentro de un recyclerview.
     * Extiende ViewHolder
     *
     * @author Julio García
     * @see RecyclerView.ViewHolder
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        UserViewHolder(ItemUserBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;

            this.binding.buttonSettingsRemoveUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    String uid = mUserList.get(pos).getUid();
                    mUserOnClickListener.onClickRemoveUser(uid);
                }
            });

            this.binding.buttonSettingsEditUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUserOnClickListener.onClickEditUser(getAdapterPosition());
                }
            });
        }

        /**
         * Se rellena la vista
         * @param name Nombre del usuario
         */
        public void bind(String name){
            if (!mRole.equals(UserRoles.MANAGER.toString())){
                binding.buttonSettingsRemoveUser.setVisibility(View.GONE);
                binding.buttonSettingsEditUser.setVisibility(View.GONE);
            }
            binding.textViewUserListName.setText(name);
            binding.executePendingBindings();
        }

    }
}
