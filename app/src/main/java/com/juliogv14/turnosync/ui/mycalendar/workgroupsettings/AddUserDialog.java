package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

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
import com.juliogv14.turnosync.databinding.DialogAddUserBinding;
import com.juliogv14.turnosync.utils.FormUtils;

/**
 * La clase AddUserDialog es responsable de pedir el email del usuario a invitar al grupo
 * Es llamada dentro de WorkgroupSettingsFragment
 * Extiende DialogFragment.
 *
 * @author Julio García
 * @see DialogFragment
 */
public class AddUserDialog extends DialogFragment {

    /** Referencia a la vista con databinding */
    private DialogAddUserBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;
    /** Clase que implementa la interfaz de escucha */
    private AddUserListener mListener;

    /** Dirección de email del usuario a invitar al grupo */
    private String mEmail;

    /** {@inheritDoc} <br>
     * Al vincularse al contexto se obtienen referencias al contexto y la clase de escucha.
     * @see Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof AddUserListener) {
            mListener = (AddUserListener) getParentFragment();
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_user, null);
        mViewBinding = DialogAddUserBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_adduser_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_adduser_button_add, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton(R.string.dialog_adduser_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FormUtils.closeKeyboard(mContext, mViewBinding.editTextEmail);
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                FormUtils.openKeyboard(mContext, mViewBinding.editTextEmail);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isReadyToClose = attemptAddUser();
                        if (isReadyToClose) {
                            FormUtils.closeKeyboard(mContext, mViewBinding.editTextEmail);
                            mListener.onClickAddUser(mEmail);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Al crear la vista se centra la antención en el campo vacío.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewBinding.editTextEmail.requestFocus();
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

    /** Se intenta enviar los datos comprobando si son cadenas validas
     * @return true si los datos son validos. False en caso contrario.
     */
    private boolean attemptAddUser() {
        Boolean isReadyToClose = true;
        //Get strings from editText
        mViewBinding.editTextLayoutEmail.setError(null);
        mEmail = mViewBinding.editTextEmail.getText().toString();

        /*Check for a valid email address.*/
        if (TextUtils.isEmpty(mEmail)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.form_error_field_required));
            isReadyToClose = false;
        } else if (!FormUtils.isEmailValid(mEmail)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            isReadyToClose = false;
        }

        if (!isReadyToClose) {
            mViewBinding.editTextEmail.requestFocus();
        }
        return isReadyToClose;
    }

    /** Interfaz de escucha para comunicarse con la actividad o fragmento contenedor.
     */
    public interface AddUserListener {
        void onClickAddUser(String email);
    }
}
