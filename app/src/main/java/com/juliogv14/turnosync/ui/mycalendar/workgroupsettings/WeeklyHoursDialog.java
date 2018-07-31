package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.databinding.DialogWeeklyHoursBinding;

/**
 * La clase WeeklyHoursDialog es responsable de pedir las horas semanales que se quieren establecer.
 * Es llamada dentro de WorkgroupSettingsFragment.
 * Extiende DialogFragment.
 *
 * @author Julio García
 * @see DialogFragment
 */
public class WeeklyHoursDialog extends DialogFragment {

    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String WEEKLY_HOURS_KEY = "weeklyHours";

    /** Referencia a la vista con databinding */
    private DialogWeeklyHoursBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private WeeklyHoursDialogListener mListener;

    /** Horas máximas semanales */
    private long mWeeklyHours;

    /** Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos.
     *
     * @param weeklyHours Horas actualmente establecidas
     * @return instancia de la clase WeeklyHoursDialog
     */
    public static WeeklyHoursDialog newInstance(long weeklyHours) {

        Bundle args = new Bundle();
        args.putLong(WEEKLY_HOURS_KEY, weeklyHours);
        WeeklyHoursDialog fragment = new WeeklyHoursDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /** {@inheritDoc} <br>
     * Al vincularse al contexto se obtienen referencias al contexto y la clase de escucha.
     * @see Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof WeeklyHoursDialogListener) {
            mListener = (WeeklyHoursDialogListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AddUserListener");
        }

    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Construccion del cuadro de dialogo.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mWeeklyHours = args.getLong(WEEKLY_HOURS_KEY);
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_weekly_hours, null);
        mViewBinding = DialogWeeklyHoursBinding.bind(view);
        mViewBinding.numberPickerHours.setMinValue(1);
        mViewBinding.numberPickerHours.setMaxValue(72);
        mViewBinding.numberPickerHours.setValue((int)mWeeklyHours);
        mViewBinding.numberPickerHours.setWrapSelectorWheel(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_weeklyHours_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_weeklyHours_button_set, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        long weeklyhours = mViewBinding.numberPickerHours.getValue();
                        mListener.onSetWeekyHours(weeklyhours);
                    }
                })
                .setNegativeButton(R.string.dialog_adduser_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        return builder.create();
    }

    /** {@inheritDoc} <br>
     * Al desvincularse de la actividad se ponen a null las referencias
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

    /** Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface WeeklyHoursDialogListener {
        void onSetWeekyHours(long hours);
    }
}
