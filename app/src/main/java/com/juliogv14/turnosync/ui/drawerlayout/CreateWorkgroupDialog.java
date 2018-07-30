package com.juliogv14.turnosync.ui.drawerlayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.databinding.DialogCreateWorkgroupBinding;
import com.juliogv14.turnosync.utils.FormUtils;

/**
 * La clase CreateWorkgroupDialog es responsable de pedir el nombre y descripcion de un grupo de trabajo.
 * Es llamada dentro de HomeFragment.
 * Extiende DialogFragment.
 *
 * @author Julio García
 * @see DialogFragment
 */

public class CreateWorkgroupDialog extends DialogFragment {
    /**
     * Referencia a la vista con databinding */
    private DialogCreateWorkgroupBinding mViewBinding;


    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private CreateWorkgroupListener mListener;

    /** Nombre del grupo de trabajo */
    String mName;
    /** Descripción del grupo de trabajo */
    String mDescription;

    /** {@inheritDoc}
     * Al vincularse al contexto se obtienen referencias al contexto y la clase de escucha.
     * @see Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof CreateWorkgroupListener) {
            mListener = (CreateWorkgroupListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CreateTypeListener");
        }
    }

    /** {@inheritDoc}
     * Lifecycle callback.
     * Construccion del cuadro de dialogo.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_workgroup, null);
        mViewBinding = DialogCreateWorkgroupBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_workgroup_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_workgroup_button_create, new DialogInterface.OnClickListener() {
                    //Create new workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton(R.string.dialog_workgroup_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FormUtils.closeKeyboard(mContext, mViewBinding.editTextWorkgroupName);
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                FormUtils.openKeyboard(mContext, mViewBinding.editTextWorkgroupName);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isReadyToClose = attemptCreateWorkgroup();
                        if (isReadyToClose) {
                            FormUtils.closeKeyboard(mContext, mViewBinding.editTextWorkgroupName);
                            mListener.onDialogPositiveClick(mName, mDescription);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    /** {@inheritDoc}
     * Lifecycle callback.
     * Al crear la vista se centra la antención en el campo vacío.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewBinding.editTextWorkgroupName.requestFocus();
    }

    /** {@inheritDoc}
     * Al desvincularse de la actividad se ponen a null las referencias
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

    /** Se intenta enviar los datos comprobando si son cadenas validas
     * @return true si los datos son validos. False en caso contrario.
     */
    private boolean attemptCreateWorkgroup() {
        Boolean isReadyToClose = true;
        //Get strings from editText
        mName = mViewBinding.editTextWorkgroupName.getText().toString();
        mDescription = mViewBinding.editTextWorkgroupDesc.getText().toString();
        if (TextUtils.isEmpty(mName)) {
            mViewBinding.editTextLayoutWorkgroupName
                    .setError(getString(R.string.form_error_field_required));
            isReadyToClose = false;
        } else if (!FormUtils.isDisplayNameValid(mName)) {
            mViewBinding.editTextLayoutWorkgroupName
                    .setError(getString(R.string.form_error_name));
            isReadyToClose = false;
        }

        if (!isReadyToClose) {
            mViewBinding.editTextWorkgroupName.requestFocus();
        }
        return isReadyToClose;
    }

    /** Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface CreateWorkgroupListener {
        void onDialogPositiveClick(String name, String description);
    }
}
