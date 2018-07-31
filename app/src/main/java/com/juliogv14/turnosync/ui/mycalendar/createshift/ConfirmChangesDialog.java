package com.juliogv14.turnosync.ui.mycalendar.createshift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogShiftChangesBinding;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * La clase ConfirmChangesDialog es responsable de mostrar los cambios en el calendario hechos por
 * el manager y pedir confirmación para hacerlos efectivos. Es llamada dentro de MyCalendarFragment
 * Extiende DialogFragment.
 *
 * @author Julio García
 * @see DialogFragment
 * @see Shift
 */
public class ConfirmChangesDialog extends DialogFragment {

    //{@
    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String CHANGES_MAP = "changesMap";
    private static final String SHIFT_TYPES_MAP = "shiftTypesMap";
    private static final String USER_REF_LIST = "userRefList";
    //@}

    /** Referencia a la vista con databinding */
    private DialogShiftChangesBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private ConfirmChangesListener mListener;

    /** Mapa de la lista de turnos cambiados con el tipo de cambio como clave */
    private HashMap<String, ArrayList<Shift>> mShiftChangesMap;
    /** Mapa con los tipos de turnos ShiftType */
    private HashMap<String, ShiftType> mShiftTypesMap;
    /** Lista con las referencias de los usuarios UserRef */
    private ArrayList<UserRef> mUserRefList;

    /** Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param shiftChanges Mapa de la lista de turnos cambiados con el tipo de cambio como clave
     * @param shiftTypes Mapa con los tipos de turnos ShiftType
     * @param userRefs Lista con las referencias de los usuarios UserRef
     * @return instancia de la clase ConfirmChangesDialog
     */
    public static ConfirmChangesDialog newInstance(HashMap<String, ArrayList<Shift>> shiftChanges, HashMap<String, ShiftType> shiftTypes, ArrayList<UserRef> userRefs){
        ConfirmChangesDialog fragment = new ConfirmChangesDialog();
        Bundle args = new Bundle();
        args.putSerializable(CHANGES_MAP, shiftChanges);
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
        if (getParentFragment() instanceof ConfirmChangesListener) {
            mListener = (ConfirmChangesListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ConfirmChangesListener");
        }

    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Construccion del cuadro de dialogo. Se muestran los turnos a confirmar
     */
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args!= null){
            mShiftChangesMap = (HashMap<String, ArrayList<Shift>>) args.getSerializable(CHANGES_MAP);
            mShiftTypesMap = (HashMap<String, ShiftType>) args.getSerializable(SHIFT_TYPES_MAP);
            mUserRefList = args.getParcelableArrayList(USER_REF_LIST);
        }

        HashMap<String, UserRef> userRefMap = new HashMap<>();
        for (UserRef userRef : mUserRefList) {
            userRefMap.put(userRef.getUid(), userRef);
        }


        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_shift_changes, null);
        mViewBinding = DialogShiftChangesBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_shiftChanges_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_shiftChanges_button_confirm, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onConfirmChanges();
                    }
                })
                .setNegativeButton(R.string.dialog_shiftChanges_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerShiftChanges.setLayoutManager(layoutManager);
        mViewBinding.recyclerShiftChanges.setHasFixedSize(true);
        mViewBinding.recyclerShiftChanges.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        ShiftChangesAdapter adapter = new ShiftChangesAdapter(mContext, mShiftChangesMap, mShiftTypesMap, userRefMap);
        mViewBinding.recyclerShiftChanges.setAdapter(adapter);


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
    public interface ConfirmChangesListener {
        void onConfirmChanges();
    }
}
