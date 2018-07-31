package com.juliogv14.turnosync.ui.mycalendar.createshift;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.ItemChangeBinding;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * La clase ShiftChangesAdapter es la clase encargada de proporcionar la vista en forma de lista de elementos
 * de turnos a cambiar antes de confirmar los cambios.
 * Extiende RecyclerView.Adapter.
 *
 * @author Julio García
 * @see RecyclerView.Adapter
 * @see RecyclerView.ViewHolder
 */
public class ShiftChangesAdapter extends RecyclerView.Adapter<ShiftChangesAdapter.ShiftChangesViewHolder>{

    /** Contexto del fragmento */
    private Context mContext;
    /** Mapa de las listas de cambios segun el tipo de cambio */
    private HashMap<String, ArrayList<Shift>> mShiftChanges;
    /** Mapa con los tipos de turno */
    private HashMap<String, ShiftType> mShiftTypes;
    /** Mapa con las referencias a los usuarios */
    private HashMap<String, UserRef> mUserRefs;


    /**
     * Constructor del adaptador
     * @param context Contexto
     * @param shiftChanges Mapa de las listas de cambios segun el tipo de cambio
     * @param shiftTypes Mapa con los tipos de turno
     * @param userRefs Mapa con las referencias a los usuarios
     */
    ShiftChangesAdapter(Context context, HashMap<String, ArrayList<Shift>> shiftChanges, HashMap<String, ShiftType> shiftTypes, HashMap<String, UserRef> userRefs) {
        this.mContext = context;
        this.mShiftChanges = shiftChanges;
        this.mShiftTypes = shiftTypes;
        this.mUserRefs = userRefs;
    }

    /**
     * Infla la vista del elemento de un ViewHolder
     * @param parent Vista padre
     * @param viewType Tipo de vista
     * @return ViewHolder del elemento
     */
    @NonNull
    @Override
    public ShiftChangesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChangeBinding itemBinding = ItemChangeBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ShiftChangesViewHolder(itemBinding);
    }

    /**
     * Rellena la vista con el elemento correspondiente a partir de la posición y los datos.
     * @param holder Elemento
     * @param position Posición
     */
    @Override
    public void onBindViewHolder(@NonNull ShiftChangesViewHolder holder, int position) {
        List<Shift> listAdded = mShiftChanges.get(mContext.getString(R.string.data_changes_added));
        List<Shift> listRemoved = mShiftChanges.get(mContext.getString(R.string.data_changes_removed));
        List<Shift> listEditedNew = mShiftChanges.get(mContext.getString(R.string.data_changes_editedNew));
        List<Shift> listEditedOld = mShiftChanges.get(mContext.getString(R.string.data_changes_editedOld));

        //Order of displaying
        if (position < listAdded.size()){
            Shift shift = listAdded.get(position);
            holder.bind(R.string.data_changes_added, shift);
        } else if (position < listAdded.size() + listRemoved.size()){
            Shift shift = listRemoved.get(position-listAdded.size());
            holder.bind(R.string.data_changes_removed, shift);
        } else if (position < listAdded.size() + listRemoved.size() + listEditedNew.size()){
            Shift shift = listEditedNew.get(position-listAdded.size()-listRemoved.size());
            holder.bind(R.string.data_changes_editedNew, shift);
        } else if (position < listAdded.size() + listRemoved.size() + listEditedNew.size() + listEditedOld.size()){
            Shift shift = listEditedOld.get(position-listAdded.size()-listRemoved.size()-listEditedNew.size());
            holder.bind(R.string.data_changes_editedOld, shift);
        }
    }

    /**
     * Devuelve el numero total de elementos
     * @return Tamaño total de los cambios de turno en el calendario
     */
    @Override
    public int getItemCount() {
        return  mShiftChanges.get(mContext.getString(R.string.data_changes_added)).size()
                + mShiftChanges.get(mContext.getString(R.string.data_changes_removed)).size()
                + mShiftChanges.get(mContext.getString(R.string.data_changes_editedNew)).size()
                + mShiftChanges.get(mContext.getString(R.string.data_changes_editedOld)).size();
    }
    /**
     * Esta clase representa la vista de un elemento dentro de un recyclerview.
     * Extiende ViewHolder
     *
     * @author Julio García
     * @see RecyclerView.ViewHolder
     */
    class ShiftChangesViewHolder extends RecyclerView.ViewHolder {
        /**
         * Referencia a la vista del elemento */
        ItemChangeBinding binding;

        ShiftChangesViewHolder(ItemChangeBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;
        }


        /**
         * Se rellena la vista
         * @param change Tipo de cambio
         * @param shift Turno a mostrar
         */
        public void bind (int change, Shift shift){
            //Label
            String changeLabel = "";
            switch (change) {
                case R.string.data_changes_added:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_added);
                    break;
                case R.string.data_changes_removed:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_removed);
                    break;
                case R.string.data_changes_editedNew:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_editedNew);
                    break;
                case R.string.data_changes_editedOld:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_editedOld);
                    break;
            }
            changeLabel = changeLabel + " " + mUserRefs.get(shift.getUserId()).getShortName();
            binding.textViewChangeLabel.setText(changeLabel);

            //Date
            SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            SimpleDateFormat formatWeekDay = new SimpleDateFormat("EEEE", Locale.getDefault());
            String date = formatDate.format(shift.getDate()) + System.getProperty("line.separator") + formatWeekDay.format(shift.getDate());
            String weekDay = formatWeekDay.format(shift.getDate());
            binding.textViewChangeDate.setText(date);
            binding.textViewChangeWeekDay.setText(weekDay);

            //Shift name
            ShiftType shiftType = mShiftTypes.get(shift.getType());
            String shiftName = mContext.getString(R.string.dialog_shiftChanges_shift) + ": " + shiftType.getName();
            binding.textViewChangeName.setText(shiftName);

            //Time interval
            DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
            LocalTime startTime = shiftType.getJodaStartTime();
            Period period = shiftType.getJodaPeriod();
            LocalTime endTime = startTime.plus(period);
            String startHour = fmt.print(startTime);
            String endHour = fmt.print(endTime);
            String timeInterval = startHour + " - " + endHour;
            binding.textViewChangeTime.setText(timeInterval);

            //Tag and color
            binding.textViewChangeTag.setText(shiftType.getTag());
            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
            background.setColor(shiftType.getColor());
            binding.textViewChangeTag.setBackground(background);
        }
    }
}
