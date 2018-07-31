package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.databinding.ItemShifttypeBinding;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * La clase ShiftTypesAdapter es la clase encargada de proporcionar la vista en forma de lista de elementos
 * de tipos de turnos.
 * Extiende RecyclerView.Adapter.
 *
 * @author Julio García
 * @see RecyclerView.Adapter
 * @see RecyclerView.ViewHolder
 */
public class ShiftTypesAdapter extends RecyclerView.Adapter<ShiftTypesAdapter.ShiftTypeViewHolder>{
    /**
     * Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private TypeOnClickListener mTypeOnClickListener;
    /** Rol del usuario actual */
    private String mRole;
    /** Listado de los tipos de turnos */
    private List<ShiftType> mShiftTypesList;


    /**
     * Constructor del adaptador
     * @param context Contexto
     * @param listener Clase que implementa la interfaz
     * @param shiftTypesList Listado de los tipos de turnos
     * @param role Rol del usuario
     */
    public ShiftTypesAdapter(Context context, TypeOnClickListener listener, ArrayList<ShiftType> shiftTypesList, String role) {
        this.mContext = context;
        this.mTypeOnClickListener = listener;
        this.mShiftTypesList = shiftTypesList;
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
    public ShiftTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShifttypeBinding itemBinding = ItemShifttypeBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ShiftTypeViewHolder(itemBinding);
    }

    /** Rellena la vista con el elemento correspondiente a partir de la posición y los datos.
     * @param holder Elemento
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull ShiftTypeViewHolder holder, int position) {
        holder.bind(mShiftTypesList.get(position));
    }

    /**
     * Devuelve el numero total de elementos
     * @return Tamaño total de la lista de tipos de turno
     */
    @Override
    public int getItemCount() {
        return mShiftTypesList.size();
    }

    /**
     * Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface TypeOnClickListener {
        void onClickEditType(ShiftType type);
        void onClickRemoveType(ShiftType type);
    }

    /**
     * Esta clase representa la vista de un elemento dentro de un recyclerview.
     * Extiende ViewHolder
     *
     * @author Julio García
     * @see RecyclerView.ViewHolder
     */
    class ShiftTypeViewHolder extends RecyclerView.ViewHolder {

        /** Referencia a la vista del elemento */
        ItemShifttypeBinding binding;

        ShiftTypeViewHolder(ItemShifttypeBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;

            this.binding.buttonShiftTypeEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    ShiftType type = mShiftTypesList.get(pos);
                    mTypeOnClickListener.onClickEditType(type);
                }
            });

            this.binding.buttonShiftTypeRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    ShiftType type = mShiftTypesList.get(pos);
                    mTypeOnClickListener.onClickRemoveType(type);
                }
            });
        }

        /**
         * Se rellena la vista
         * @param shiftType Tipo de turno
         */
        public void bind(ShiftType shiftType){
            if(!mRole.equals(UserRoles.MANAGER.toString())){
                binding.buttonShiftTypeRemove.setVisibility(View.GONE);
                binding.buttonShiftTypeEdit.setVisibility(View.GONE);
            }
            binding.textViewShiftTypeLong.setText(shiftType.getName());
            binding.textViewShiftTypeShort.setText(shiftType.getTag());
            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
            background.setColor(shiftType.getColor());
            binding.imageViewTypeColor.setBackground(background);

            DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
            String startHour = fmt.print(shiftType.getJodaStartTime());
            String endHour = fmt.print(shiftType.getJodaStartTime().plus(shiftType.getJodaPeriod()));
            String timeInterval = startHour + " - " + endHour;
            binding.textViewShiftTypeInterval.setText(timeInterval);
            binding.executePendingBindings();
        }

    }
}
