package com.juliogv14.turnosync.ui.mycalendar.createshift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogAddUserBinding;
import com.juliogv14.turnosync.databinding.DialogCreateShiftBinding;
import com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes.CreateTypeDialog;
import com.juliogv14.turnosync.utils.FormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Julio on 26/02/2018.
 * CreateWorkgroupDialog
 */

public class CreateShiftDialog extends DialogFragment {

    //Constant
    private static final String DATE_KEY = "date";
    private static final String USER_REF_KEY = "userRef";
    private static final String SHIFT_TYPES_KEY = "shiftTypes";


    //Strings
    private DialogCreateShiftBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private CreateShiftListener mListener;

    //Variables
    private Date mDay;
    private UserRef mUserRef;
    private ArrayList<ShiftType> mShiftTypesList;

    public static CreateShiftDialog newInstance(Date date, UserRef userRef, Map<String, ShiftType> shiftTypes) {
        CreateShiftDialog fragment = new CreateShiftDialog();
        Bundle args = new Bundle();
        args.putLong(DATE_KEY, date.getTime());
        args.putParcelable(USER_REF_KEY, userRef);
        args.putParcelableArrayList(SHIFT_TYPES_KEY, new ArrayList<>(shiftTypes.values()));
        fragment.setArguments(args);
        return fragment;
    }

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mDay = new Date(args.getLong(DATE_KEY));
            mUserRef = args.getParcelable(USER_REF_KEY);
            mShiftTypesList = args.getParcelableArrayList(SHIFT_TYPES_KEY);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_shift, null);
        mViewBinding = DialogCreateShiftBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_createShift_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_createShift_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArrayList<Shift> newShifts = new ArrayList<>();
                        int spinnerPos = mViewBinding.spinnerCreateShiftType.getSelectedItemPosition();
                        Shift shift = new Shift(mUserRef.getUid(), mShiftTypesList.get(spinnerPos).getId(), mDay, "","");
                        newShifts.add(shift);
                        mListener.onDialogPositiveClick(newShifts);
                    }
                })
                .setNegativeButton(R.string.dialog_createShift_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //FormUtils.closeKeyboard(mContext, mViewBinding.editTextEmail);
                    }
                });

        final AlertDialog dialog = builder.create();

        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
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
                            mListener.onDialogPositiveClick(mEmail);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });*/
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

    /*private boolean attemptAddUser() {
        Boolean isReadyToClose = true;
        //Get strings from editText
        mViewBinding.editTextLayoutEmail.setError(null);
        mEmail = mViewBinding.editTextEmail.getText().toString();

        *//*Check for a valid email address.*//*
        if (TextUtils.isEmpty(mEmail)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.form_error_field_required));
            isReadyToClose = false;
        } else if (!FormUtils.isEmailValid(mEmail)) {
            mViewBinding.editTextLayoutEmail
                    .setError(getString(R.string.login_error_invalid_email));
            isReadyToClose = false;
        }

        //TODO: check if the user exists

        if (!isReadyToClose) {
            mViewBinding.editTextEmail.requestFocus();
        }
        return isReadyToClose;
    }*/

    public interface CreateShiftListener {
        void onDialogPositiveClick(ArrayList<Shift> newShifts);
    }
}
