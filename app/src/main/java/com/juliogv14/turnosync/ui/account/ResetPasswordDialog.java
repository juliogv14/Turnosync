package com.juliogv14.turnosync.ui.account;

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
import com.juliogv14.turnosync.databinding.DialogResetPasswordBinding;
import com.juliogv14.turnosync.utils.FormUtils;

/**
 * Created by Julio on 26/02/2018.
 * AddUserDialog
 */

public class ResetPasswordDialog extends DialogFragment {

    //Binding
    private DialogResetPasswordBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private ResetPasswordListener mListener;

    //Variables
    private String mEmail;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (mContext instanceof ResetPasswordListener) {
            mListener = (ResetPasswordListener) mContext;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AddUserListener");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_reset_password, null);
        mViewBinding = DialogResetPasswordBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_resetpassword_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_resetpassword_button_send, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton(R.string.dialog_resetpassword_button_cancel, new DialogInterface.OnClickListener() {
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
                            mListener.onResetPassword(mEmail);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewBinding.editTextEmail.requestFocus();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

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

    public interface ResetPasswordListener {
        void onResetPassword(String email);
    }
}