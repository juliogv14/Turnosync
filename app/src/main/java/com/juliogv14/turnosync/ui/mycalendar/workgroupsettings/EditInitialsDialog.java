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
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogAddUserBinding;
import com.juliogv14.turnosync.databinding.DialogEditInitialsBinding;
import com.juliogv14.turnosync.utils.FormUtils;

/**
 * Created by Julio on 26/02/2018.
 * EditInitialsDialog
 */

public class EditInitialsDialog extends DialogFragment {

    //Constants
    private static final String USER_REF_KEY = "userRef";

    //Binding
    private DialogEditInitialsBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private EditInitialsListener mListener;

    //Variables
    UserRef userRef;
    String mInitials;

    public static EditInitialsDialog newInstance(UserRef user) {
        EditInitialsDialog fragment = new EditInitialsDialog();
        Bundle args = new Bundle();
        args.putParcelable(USER_REF_KEY, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof EditInitialsListener) {
            mListener = (EditInitialsListener) getParentFragment();
        } else {
            throw new RuntimeException(getParentFragment().toString()
                    + " must implement EditInitialsListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            userRef = args.getParcelable(USER_REF_KEY);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_initials, null);
        mViewBinding = DialogEditInitialsBinding.bind(view);
        mViewBinding.editTextName.setText(userRef.getShortName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_editinitials_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_editinitials_button_edit, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton(R.string.dialog_editinitials_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FormUtils.closeKeyboard(mContext, mViewBinding.editTextName);
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                FormUtils.openKeyboard(mContext, mViewBinding.editTextName);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isReadyToClose = attemptEditInitials();
                        if (isReadyToClose) {
                            FormUtils.closeKeyboard(mContext, mViewBinding.editTextName);
                            userRef.setShortName(mInitials);
                            mListener.onInitialsName(userRef);
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
        mViewBinding.editTextName.requestFocus();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

    private boolean attemptEditInitials() {
        Boolean isReadyToClose = true;
        //Get strings from editText
        mViewBinding.editTextLayoutName.setError(null);
        mInitials = mViewBinding.editTextName.getText().toString();

        /*Check for a valid email address.*/
        if (TextUtils.isEmpty(mInitials)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.form_error_field_required));
            isReadyToClose = false;
        } else if (!FormUtils.isInitialsValid(mInitials)) {
            mViewBinding.editTextLayoutName
                    .setError(getString(R.string.dialog_editinitials_error));
            isReadyToClose = false;
        }

        if (!isReadyToClose) {
            mViewBinding.editTextName.requestFocus();
        }
        return isReadyToClose;
    }

    public interface EditInitialsListener {
        void onInitialsName(UserRef userRef);
    }
}
