package com.juliogv14.turnosync.ui.mycalendar.changerequests;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.databinding.ItemChangeRequestBinding;
import com.juliogv14.turnosync.databinding.LayoutShiftChangeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * La clase ChangeRequestsAdapter es la clase encargada de proporcionar la vista en forma de lista de elementos
 * de solicitudes de cambio.
 * Extiende RecyclerView.Adapter.
 *
 * @author Julio García
 * @see RecyclerView.Adapter
 * @see RecyclerView.ViewHolder
 */
public class ChangeRequestsAdapter extends RecyclerView.Adapter<ChangeRequestsAdapter.ChangeRequestsViewHolder> {

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private ChangeRequestListener mListener;
    /** Rol del usuario actual */
    private UserRoles mRole;
    /** Mapa con los tipos de turnos */
    private Map<String, ShiftType> mShiftTypesMap;
    /** Mapa con los usuarios del grupo */
    private Map<String, UserRef> mUserRefsMap;
    /** Listado de las solicitudes de cambio */
    private List<ChangeRequest> mChangeRequests;


    /**
     * Constructor del adaptador
     * @param context Contexto
     * @param listener Clase que implementa la interfaz
     * @param role Rol del usuario
     * @param shiftTypes Mapa con los tipos de turnos
     * @param userRefs Mapa con los usuarios del grupo
     * @param changeRequests Listado de las solicitudes de cambio
     */
    public ChangeRequestsAdapter(Context context, ChangeRequestListener listener, String role, Map<String, ShiftType> shiftTypes, Map<String, UserRef> userRefs, List<ChangeRequest> changeRequests) {
        this.mContext = context;
        this.mListener = listener;
        this.mRole = UserRoles.valueOf(role);
        this.mShiftTypesMap = shiftTypes;
        this.mUserRefsMap = userRefs;
        this.mChangeRequests = changeRequests;
    }


    /**
     * Infla la vista del elemento de un ViewHolder
     * @param parent Vista padre
     * @param viewType Tipo de vista
     * @return ViewHolder del elemento
     */
    @NonNull
    @Override
    public ChangeRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChangeRequestBinding itemBinding = ItemChangeRequestBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ChangeRequestsViewHolder(itemBinding);
    }

    /**
     * Rellena la vista con el elemento correspondiente a partir de la posición y los datos.
     * @param holder Elemento
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull ChangeRequestsViewHolder holder, int position) {
        ChangeRequest changeRequest = mChangeRequests.get(position);
        holder.bind(changeRequest);
    }

    /**
     * Devuelve el numero total de elementos
     * @return Tamaño total de la lista de solicitudes
     */
    @Override
    public int getItemCount() {
        return mChangeRequests.size();
    }

    /**
     * Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface ChangeRequestListener {
        void onAcceptRequested(ChangeRequest changeRequest);
        void onAcceptAccepted(ChangeRequest changeRequest);
        void onDenyRequested(ChangeRequest changeRequest, String uid, UserRoles role);
        void onDenyAccepted(ChangeRequest changeRequest, String uid, UserRoles role);
    }

    /**
     * Esta clase representa la vista de un elemento dentro de un recyclerview.
     * Extiende ViewHolder
     *
     * @author Julio García
     * @see RecyclerView.ViewHolder
     */
    class ChangeRequestsViewHolder extends RecyclerView.ViewHolder {

        /** Referencia a la vista del elemento */
        ItemChangeRequestBinding binding;

        ChangeRequestsViewHolder(ItemChangeRequestBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;
        }

        /**
         * Se rellena la vista
         * @param changeRequest Solicitud de cambio
         */
        public void bind(final ChangeRequest changeRequest) {
            Shift ownShift = changeRequest.getOwnShift();
            Shift otherShift = changeRequest.getOtherShift();
            Date timestampDate = changeRequest.getTimestamp();
            final String state = changeRequest.getState();

            //Shift info
            displayShift(binding.ownShift, ownShift);
            displayShift(binding.otherShift, otherShift);

            //TimeStamp
            SimpleDateFormat formatTimeStamp = new SimpleDateFormat("dd MMM", Locale.getDefault());
            String timestamp = formatTimeStamp.format(timestampDate);
            binding.textViewRequestTimestamp.setText(timestamp);

            //State
            switch (state) {
                case ChangeRequest.REQUESTED:
                    binding.textViewRequestState.setText(mContext.getString(R.string.requests_change_requested));
                    break;
                case ChangeRequest.ACCEPTED:
                    binding.textViewRequestState.setText(mContext.getString(R.string.requests_change_accepted));
                    break;
                case ChangeRequest.APPROVED:
                    binding.textViewRequestState.setText(mContext.getString(R.string.requests_change_approved));
                    break;
            }
            
            // TODO: 14/07/2018 Consider argument
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Buttons
            if ((TextUtils.equals(state, ChangeRequest.ACCEPTED) && mRole == UserRoles.MANAGER)
                    ||(TextUtils.equals(state, ChangeRequest.REQUESTED) && TextUtils.equals(otherShift.getUserId(), userId))) {
                //Manager or user who can accept
                binding.buttonRequestAccept.setVisibility(View.VISIBLE);
                binding.buttonRequestDeny.setVisibility(View.VISIBLE);
            } else if((!TextUtils.equals(state, ChangeRequest.APPROVED) && (TextUtils.equals(otherShift.getUserId(), userId)
                    || TextUtils.equals(ownShift.getUserId(), userId)))){
                binding.buttonRequestAccept.setVisibility(View.INVISIBLE);
                binding.buttonRequestDeny.setVisibility(View.VISIBLE);
            } else {
                binding.buttonRequestAccept.setVisibility(View.INVISIBLE);
                binding.buttonRequestDeny.setVisibility(View.INVISIBLE);
            }


            binding.buttonRequestAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case ChangeRequest.REQUESTED:
                            mListener.onAcceptRequested(changeRequest);
                            break;
                        case ChangeRequest.ACCEPTED:
                            mListener.onAcceptAccepted(changeRequest);
                            break;
                    }
                }
            });
            binding.buttonRequestDeny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case ChangeRequest.REQUESTED:
                            mListener.onDenyRequested(changeRequest, userId, mRole);
                            break;
                        case ChangeRequest.ACCEPTED:
                            mListener.onDenyAccepted(changeRequest, userId, mRole);
                            break;
                    }
                }
            });
        }

        /**
         * Se rellena la vista relacionada con un turno
         * @param shiftItem Referencia a la vista del turno
         * @param shift Referencia al turno
         */
        private void displayShift(LayoutShiftChangeBinding shiftItem, Shift shift) {
            //Label
            String userLabel = mContext.getString(R.string.dialog_requestChange_user) + ": " + mUserRefsMap.get(shift.getUserId()).getShortName();
            shiftItem.textViewRequestLabel.setText(userLabel);

            //Date
            SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            SimpleDateFormat formatWeekDay = new SimpleDateFormat("EEEE", Locale.getDefault());
            String date = formatDate.format(shift.getDate()) + System.getProperty("line.separator") + formatWeekDay.format(shift.getDate());
            String weekDay = formatWeekDay.format(shift.getDate());
            shiftItem.textViewRequestDate.setText(date);
            shiftItem.textViewRequestWeekDay.setText(weekDay);

            //Shift name
            ShiftType shiftType = mShiftTypesMap.get(shift.getType());
            String shiftName = shiftType.getName();
            shiftItem.textViewRequestName.setText(shiftName);
        }
    }
}
