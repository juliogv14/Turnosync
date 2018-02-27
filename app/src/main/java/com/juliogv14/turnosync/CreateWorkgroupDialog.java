package com.juliogv14.turnosync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.juliogv14.turnosync.databinding.DialogCreateWorkgroupBinding;
import com.juliogv14.turnosync.utils.FormUtils;

/**
 * Created by Julio on 26/02/2018.
 * CreateWorkgroupDialog
 */

public class CreateWorkgroupDialog extends DialogFragment {

    private DialogCreateWorkgroupBinding mViewBinding;
    //Parent fragment
    private CreateWorkgroupListener mListener;
    private Context mContext;

    //Strings
    String mName;
    String mDescription;


    public CreateWorkgroupDialog() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    /*@Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = DialogCreateWorkgroupBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewBinding.editTextWorkgroupName.requestFocus();
    }

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
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            mListener = (CreateWorkgroupListener) getTargetFragment();
                        } catch (ClassCastException e) {
                            throw new ClassCastException(this.getClass().getSimpleName() + "needs his interface implemented by target fragment!");
                        }
                        boolean isReadyToClose = attemptCreateWorkgroup();
                        if (isReadyToClose) {
                            FormUtils.closeKeyboard(getActivity(), mViewBinding.editTextWorkgroupName);
                            mListener.onDialogPositiveClick(mName, mDescription);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        //FormUtils.openKeyboard(mContext, mViewBinding.editTextWorkgroupName);
        return dialog;
    }

    private boolean attemptCreateWorkgroup() {
        TextInputLayout wkname = ((Activity) mContext).findViewById(R.id.editTextLayout_workgroup_name);
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

    public interface CreateWorkgroupListener {
        public void onDialogPositiveClick(String name, String description);
    }
}