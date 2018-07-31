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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogEditShiftBinding;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * La clase EditShiftDialog es responsable de modificar un turno ya asignado pudiendo transferirlo
 * a otra persona o cambiar el tipo de turno. Es llamada dentro del ScheduleWeekPageFragment.
 * Extiende DialogFragment.
 *
 * @author Julio García
 * @see DialogFragment
 * @see Shift
 */
public class EditShiftDialog extends DialogFragment {

    //{@
    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String DATE_KEY = "date";
    private static final String USER_REF_KEY = "userRef";
    private static final String SHIFT_TYPES_KEY = "shiftTypes";
    private static final String USERS_REF_LIST = "userList";
    private static final String SELECTED_SHIFT_KEY = "selectedShift";
    //@}

    /** Referencia a la vista con databinding */
    private DialogEditShiftBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private EditShiftListener mListener;

    /** Dia seleccionado */
    private DateTime mDay;
    /** Referencia del usuario */
    private UserRef mUserRef;
    /** Lista de tipos de turnos */
    private ArrayList<ShiftType> mShiftTypesList;
    /** Referencia del turno seleccionado a editar */
    private Shift mSelectedShift;
    /** Lista de las referencias a usuarios */
    private ArrayList<UserRef> mWorkgroupUsers;
    /** Periodo con la duración del turno */
    private Period mShiftPeriod;


    /** Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param date Dia seleccionado
     * @param userRef Referencia del usuario
     * @param shiftTypes Mapa con los tipos de turnos ShiftType
     * @param userList Lista de las referencias a usuarios
     * @param shift Referencia del turno seleccionado a editar
     * @return instancia de la clase EditShiftDialog
     */
    public static EditShiftDialog newInstance(DateTime date, UserRef userRef, Map<String, ShiftType> shiftTypes, ArrayList<UserRef> userList, Shift shift) {
        EditShiftDialog fragment = new EditShiftDialog();
        Bundle args = new Bundle();
        args.putLong(DATE_KEY, date.getMillis());
        args.putParcelable(USER_REF_KEY, userRef);
        args.putParcelableArrayList(SHIFT_TYPES_KEY, new ArrayList<>(shiftTypes.values()));
        args.putParcelableArrayList(USERS_REF_LIST, userList);
        args.putParcelable(SELECTED_SHIFT_KEY, shift);
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
        if (getParentFragment() instanceof EditShiftListener) {
            mListener = (EditShiftListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EditShiftListener");
        }

    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Construccion del cuadro de dialogo. Carga los datos del turno a editar.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mDay = new DateTime(args.getLong(DATE_KEY));
            mUserRef = args.getParcelable(USER_REF_KEY);
            mShiftTypesList = args.getParcelableArrayList(SHIFT_TYPES_KEY);
            mWorkgroupUsers = args.getParcelableArrayList(USERS_REF_LIST);
            mSelectedShift = args.getParcelable(SELECTED_SHIFT_KEY);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_shift, null);
        mViewBinding = DialogEditShiftBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_editShift_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_editShift_button_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int userPos = mViewBinding.spinnerEditShiftUser.getSelectedItemPosition();
                        int typePos = mViewBinding.spinnerEditShiftType.getSelectedItemPosition();
                        String oldUserId = mUserRef.getUid();
                        String newUserId = mWorkgroupUsers.get(userPos).getUid();

                        if(oldUserId.equals(newUserId) && !mSelectedShift.getType().equals(mShiftTypesList.get(typePos).getId())){
                            mSelectedShift.setType(mShiftTypesList.get(typePos).getId());
                            mListener.onEditShiftChange(mSelectedShift, mSelectedShift);
                        } else if (!oldUserId.equals(newUserId)) {
                            Shift newShift = new Shift(newUserId, mDay.toDate(), mShiftTypesList.get(typePos).getId());
                            mListener.onEditShiftChange(mSelectedShift, newShift);
                        }
                    }
                })
                .setNeutralButton(R.string.dialog_editShift_button_remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onEditShiftRemove(mSelectedShift);
                    }
                })
                .setNegativeButton(R.string.dialog_editShift_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //FormUtils.closeKeyboard(mContext, mViewBinding.editTextEmail);
                    }
                });

        final AlertDialog dialog = builder.create();

        //Users spinner adapter
        List<String> displayUsers = new ArrayList<>();
        for (UserRef user : mWorkgroupUsers) {
            displayUsers.add(user.getShortName());
        }

        ArrayAdapter<String> spinnerUsersAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, displayUsers);
        spinnerUsersAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewBinding.spinnerEditShiftUser.setAdapter(spinnerUsersAdapter);
        mViewBinding.spinnerEditShiftUser.setSelection(displayUsers.indexOf(mUserRef.getShortName()));

        //Types spinner adapter
        List<String> displayTypes = new ArrayList<>();
        int typePos = 0;
        for (int i = 0; i < mShiftTypesList.size(); i++) {
            ShiftType type = mShiftTypesList.get(i);
            displayTypes.add(type.getName());
            if (TextUtils.equals(mSelectedShift.getType(), type.getId())) {
                typePos = i;
            }
        }

        ArrayAdapter<String> spinnerTypesAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, displayTypes);
        spinnerTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewBinding.spinnerEditShiftType.setAdapter(spinnerTypesAdapter);
        mViewBinding.spinnerEditShiftType.setSelection(typePos);
        mViewBinding.spinnerEditShiftType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Tag
                mViewBinding.textViewEditShiftTag.setText(mShiftTypesList.get(position).getTag());
                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
                background.setColor(mShiftTypesList.get(position).getColor());
                mViewBinding.textViewEditShiftTag.setBackground(background);

                //Time interval
                DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
                LocalTime startTime = mShiftTypesList.get(position).getJodaStartTime();
                mShiftPeriod = mShiftTypesList.get(position).getJodaPeriod();
                LocalTime endTime = startTime.plus(mShiftPeriod);

                String startHour = fmt.print(startTime);
                String endHour = fmt.print(endTime);
                String timeInterval = getString(R.string.dialog_editShift_schedule) + ": " + startHour + " - " + endHour;
                mViewBinding.textViewEditShiftTime.setText(timeInterval);
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

    /** Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface EditShiftListener {
        void onEditShiftChange(Shift oldShift, Shift newShift);
        void onEditShiftRemove(Shift removedShifts);
    }
}
