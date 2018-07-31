package com.juliogv14.turnosync.ui.mycalendar.changerequests;

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

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogRequestChangeBinding;
import com.juliogv14.turnosync.databinding.ItemChangeBinding;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * La clase ResetPasswordDialog es responsable de confirmar la solicitud de cambio de turno ChangeRequest
 * el email de recuperación de contraseña. Es llamada dentro de ScheduleWeekPageFragment
 * Extiende DialogFragment.
 *
 * @author Julio García
 * @see DialogFragment
 * @see ChangeRequest
 */
public class RequestChangeDialog extends DialogFragment {

    //{@
    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String OWN_SHIFT_KEY = "ownShift";
    private static final String OTHER_SHIFT_KEY = "otherShift";
    private static final String SHIFT_TYPES_MAP = "shiftTypesMap";
    private static final String USER_REF_LIST = "userRefList";
    //@}

    /** Referencia a la vista con databinding */
    private DialogRequestChangeBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private RequestChangeListener mListener;

    /** Turno del solicitante */
    private Shift mOwnShift;
    /** Turno a intercambiar */
    private Shift mOtherShift;
    /** Mapa con los tipos de turnos ShiftType */
    private HashMap<String, ShiftType> mShiftTypesMap;
    /** Mapa con las referencias de los usuarios UserRef */
    private HashMap<String, UserRef> mUserRefsMap;


    /** Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param ownShift Turno del solicitante
     * @param otherShift Turno a intercambiar
     * @param shiftTypes Mapa con los tipos de turnos ShiftType
     * @param userRefs Mapa con las referencias de los usuarios UserRef
     * @return instancia de la clase RequestChangeDialog
     */
    public static RequestChangeDialog newInstance(Shift ownShift, Shift otherShift, HashMap<String, ShiftType> shiftTypes, ArrayList<UserRef> userRefs){
        RequestChangeDialog fragment = new RequestChangeDialog();
        Bundle args = new Bundle();
        args.putParcelable(OWN_SHIFT_KEY, ownShift);
        args.putParcelable(OTHER_SHIFT_KEY, otherShift);
        args.putSerializable(SHIFT_TYPES_MAP, shiftTypes);
        args.putParcelableArrayList(USER_REF_LIST, userRefs);
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
        if (getParentFragment() instanceof RequestChangeListener) {
            mListener = (RequestChangeListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RequestChangeListener");
        }

    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Construccion del cuadro de dialogo.
     */
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args!= null){
            mOwnShift = args.getParcelable(OWN_SHIFT_KEY);
            mOtherShift = args.getParcelable(OTHER_SHIFT_KEY);
            mShiftTypesMap = (HashMap<String, ShiftType>) args.getSerializable(SHIFT_TYPES_MAP);
            List<UserRef> userRefList = args.getParcelableArrayList(USER_REF_LIST);
            mUserRefsMap = new HashMap<>();
            for (UserRef userRef : userRefList) {
                mUserRefsMap.put(userRef.getUid(), userRef);
            }
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_request_change, null);
        mViewBinding = DialogRequestChangeBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_requestChange_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_requestChange_button_request, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onRequestShiftChange(new ChangeRequest(mOwnShift, mOtherShift, new GregorianCalendar().getTime()));
                    }
                })
                .setNegativeButton(R.string.dialog_requestChange_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        //Display ownShift
        displayShift(mViewBinding.shiftItemOwnShift, mOwnShift);
        displayShift(mViewBinding.shiftItemOtherShift, mOtherShift);

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


    /** {@inheritDoc} <br>
     * Cuando se cierra el cuadro de dialogo se indica por la interfaz de comunicacion
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onCancelShiftChange();
        super.onDismiss(dialog);
    }

    /** Este metodo rellena la vista sobre un turno con el turno pasado por argumento
     * @param shiftItem Referencia a la vista de un turno
     * @param shift Instancia de turno
     */
    private void displayShift(ItemChangeBinding shiftItem, Shift shift){

        //Label
        String userLabel = getString(R.string.dialog_requestChange_user) + ": " + mUserRefsMap.get(shift.getUserId()).getShortName();
        shiftItem.textViewChangeLabel.setText(userLabel);

        //Date
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM", Locale.getDefault());
        SimpleDateFormat formatWeekDay = new SimpleDateFormat("EEEE", Locale.getDefault());
        String date = formatDate.format(shift.getDate()) + System.getProperty("line.separator") + formatWeekDay.format(shift.getDate());
        String weekDay = formatWeekDay.format(shift.getDate());
        shiftItem.textViewChangeDate.setText(date);
        shiftItem.textViewChangeWeekDay.setText(weekDay);

        //Shift name
        ShiftType shiftType = mShiftTypesMap.get(shift.getType());
        String shiftName = mContext.getString(R.string.dialog_shiftChanges_shift) + ": " + shiftType.getName();
        shiftItem.textViewChangeName.setText(shiftName);

        //Time interval
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        LocalTime startTime = shiftType.getJodaStartTime();
        Period period = shiftType.getJodaPeriod();
        LocalTime endTime = startTime.plus(period);
        String startHour = fmt.print(startTime);
        String endHour = fmt.print(endTime);
        String timeInterval = startHour + " - " + endHour;
        shiftItem.textViewChangeTime.setText(timeInterval);

        //Tag and color
        shiftItem.textViewChangeTag.setText(shiftType.getTag());
        GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
        background.setColor(shiftType.getColor());
        shiftItem.textViewChangeTag.setBackground(background);
    }

    /** Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface RequestChangeListener {
        void onRequestShiftChange(ChangeRequest changeRequest);
        void onCancelShiftChange();
    }
}
