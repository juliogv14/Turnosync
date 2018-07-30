package com.juliogv14.turnosync.ui.mycalendar.createshift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogCreateShiftBinding;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * La clase CreateShiftDialog es responsable de crear turnos para los usuarios a partir de una fecha
 * y un tipo de turno. Es llamada dentro del ScheduleWeekPageFragment.
 * Extiende DialogFragment.
 *
 * @author Julio García
 * @see DialogFragment
 * @see Shift
 */
public class CreateShiftDialog extends DialogFragment {

    //{@
    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String DATE_KEY = "date";
    private static final String USER_REF_KEY = "userRef";
    private static final String SHIFT_TYPES_KEY = "shiftTypes";
    private static final String CURRENT_HOURS_KEY = "currentHours";
    private static final String WEEKLY_HOURS_KEY = "weeklyHours";
    //@}

    /** Referencia a la vista con databinding */
    private DialogCreateShiftBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private CreateShiftListener mListener;

    /** Dia seleccionado */
    private DateTime mDay;
    /** Referencia del usuario */
    private UserRef mUserRef;
    /** Lista de tipos de turnos */
    private ArrayList<ShiftType> mShiftTypesList;
    /** Horas máximas semanales */
    private long mWeeklyHours;
    /** Periodo con las horas actualmente establecidas */
    private Period mSetHours;
    /** Periodo con la duración del turno */
    private Period mShiftPeriod;
    /** Numero de dias a añadir dentro de la semana */
    private int mAddDays = 1;

    /** Lista con los botones de los dias de la semana */
    private ArrayList<ToggleButton> mDayButtons;


    /** Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param date Dia seleccionado
     * @param userRef Referencia del usuario
     * @param shiftTypes Mapa con los tipos de turnos ShiftType
     * @param currentHours Horas actualmente establecidas en milisegundos
     * @param weeklyHours Horas maximas semanales en milisegundos
     * @return instancia de la clase CreateShiftDialog
     */
    public static CreateShiftDialog newInstance(DateTime date, UserRef userRef, Map<String, ShiftType> shiftTypes, long currentHours, long weeklyHours) {
        CreateShiftDialog fragment = new CreateShiftDialog();
        Bundle args = new Bundle();
        args.putLong(DATE_KEY, date.getMillis());
        args.putParcelable(USER_REF_KEY, userRef);
        args.putParcelableArrayList(SHIFT_TYPES_KEY, new ArrayList<>(shiftTypes.values()));
        args.putLong(CURRENT_HOURS_KEY, currentHours);
        args.putLong(WEEKLY_HOURS_KEY, weeklyHours);
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
        if (getParentFragment() instanceof CreateShiftListener) {
            mListener = (CreateShiftListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CreateShiftListener");
        }

    }

    /** {@inheritDoc} <br>
     * Lifecycle callback.
     * Construccion del cuadro de dialogo.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mDay = new DateTime(args.getLong(DATE_KEY));
            mUserRef = args.getParcelable(USER_REF_KEY);
            mShiftTypesList = args.getParcelableArrayList(SHIFT_TYPES_KEY);
            mSetHours = new Period(args.getLong(CURRENT_HOURS_KEY));
            mWeeklyHours = args.getLong(WEEKLY_HOURS_KEY);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_shift, null);
        mViewBinding = DialogCreateShiftBinding.bind(view);

        mDayButtons = new ArrayList<>();
        mDayButtons.add(mViewBinding.buttonCreateShift1);
        mDayButtons.add(mViewBinding.buttonCreateShift2);
        mDayButtons.add(mViewBinding.buttonCreateShift3);
        mDayButtons.add(mViewBinding.buttonCreateShift4);
        mDayButtons.add(mViewBinding.buttonCreateShift5);
        mDayButtons.add(mViewBinding.buttonCreateShift6);
        mDayButtons.add(mViewBinding.buttonCreateShift7);

        //Set current day
        int buttonPos = mDay.getDayOfWeek()-1;
        if(buttonPos == -1) buttonPos = 6;
        mDayButtons.get(buttonPos).setChecked(true);

        View.OnClickListener buttonClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton clickedButton = (ToggleButton) v;
                if(clickedButton.isChecked()){
                    mAddDays++;
                } else {
                    mAddDays--;
                }
                updateTimeCount();
            }
        };

        //Togglebutton setup
        String[] weekDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);
        for (int i = 0; i < mDayButtons.size(); i++) {
            ToggleButton button = mDayButtons.get(i);
            button.setText(weekDays[i]);
            button.setTextOn(null);
            button.setTextOff(null);
            ViewGroup.LayoutParams buttonParams = button.getLayoutParams();
            buttonParams.height = buttonParams.width;
            button.setOnClickListener(buttonClick);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_createShift_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_createShift_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArrayList<Shift> newShifts = new ArrayList<>();
                        int spinnerPos = mViewBinding.spinnerCreateShiftType.getSelectedItemPosition();

                        for (int j = 0; j < mDayButtons.size(); j++) {
                            ToggleButton toggleButton = mDayButtons.get(j);
                            if(toggleButton.isChecked()){
                                DateTime day = mDay.withDayOfWeek(j+1);
                                Shift shift = new Shift(mUserRef.getUid(), day.toDate(), mShiftTypesList.get(spinnerPos).getId());
                                newShifts.add(shift);
                            }
                        }
                        mListener.onCreateShiftCreate(newShifts);
                    }
                })
                .setNegativeButton(R.string.dialog_createShift_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //FormUtils.closeKeyboard(mContext, mViewBinding.editTextEmail);
                    }
                });

        final AlertDialog dialog = builder.create();

        //Types spinner
        List<String> displayTypes = new ArrayList<>();
        for (ShiftType type : mShiftTypesList) {
            displayTypes.add(type.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, displayTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewBinding.textViewCreateShiftTag.setText(mShiftTypesList.get(0).getTag());
        mViewBinding.spinnerCreateShiftType.setAdapter(spinnerAdapter);
        mViewBinding.spinnerCreateShiftType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewBinding.textViewCreateShiftTag.setText(mShiftTypesList.get(position).getTag());
                mViewBinding.textViewCreateShiftTag.setText(mShiftTypesList.get(position).getTag());
                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
                background.setColor(mShiftTypesList.get(position).getColor());
                mViewBinding.textViewCreateShiftTag.setBackground(background);

                //Time interval
                DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
                LocalTime startTime = mShiftTypesList.get(position).getJodaStartTime();
                mShiftPeriod = mShiftTypesList.get(position).getJodaPeriod();
                LocalTime endTime = startTime.plus(mShiftPeriod);

                String startHour = fmt.print(startTime);
                String endHour = fmt.print(endTime);
                String timeInterval = getString(R.string.dialog_createShift_schedule) + ": " + startHour + " - " + endHour;
                mViewBinding.textViewCreateShiftTime.setText(timeInterval);
                //Added hours
                updateTimeCount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return dialog;
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

    /** Actualiza el contador total de horas en la vista al incluir nuevos dias
     */
    private void updateTimeCount(){

        Period addedTime = mShiftPeriod.multipliedBy(mAddDays);

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" h")
                .appendMinutes()
                .appendSuffix(" min")
                .toFormatter();
        String hourCountDisplay = formatter.print(mSetHours) + " +" + formatter.print(addedTime);
        mViewBinding.textViewCreateShiftHours.setText(hourCountDisplay);
        if(mSetHours.plus(addedTime).getHours() > mWeeklyHours){
            Toast.makeText(mContext, "Current weekly hours are above the limit. Limit: " + mWeeklyHours, Toast.LENGTH_LONG).show();
        }
    }

    /** Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface CreateShiftListener {
        void onCreateShiftCreate(ArrayList<Shift> newShifts);
    }
}
