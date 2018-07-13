package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.databinding.DialogCreateShiftypeBinding;
import com.juliogv14.turnosync.utils.FormUtils;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Julio on 26/02/2018.
 * CreateWorkgroupDialog
 */

public class CreateTypeDialog extends DialogFragment {

    //Constants
    private static final String TYPE_KEY = "mode";

    //Binding
    private DialogCreateShiftypeBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private CreateTypeListener mListener;

    //Edit type
    private ShiftType mShiftType;

    //Variables
    private String mName;
    private String mTag;
    private LocalTime mTimeStart;
    private LocalTime mTimeEnd;
    private Period mPeriod;

    private int mColor;
    ArrayList<ToggleButton> mColorButtons;
    ToggleButton mSelectedButton;

    public static CreateTypeDialog newInstance(ShiftType type) {
        CreateTypeDialog fragment = new CreateTypeDialog();
        Bundle args = new Bundle();
        args.putParcelable(TYPE_KEY, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof CreateTypeListener) {
            mListener = (CreateTypeListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CreateTypeListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewBinding.editTextLayoutCreateTypeName.requestFocus();

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mShiftType = args.getParcelable(TYPE_KEY);
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_shiftype, null);
        mViewBinding = DialogCreateShiftypeBinding.bind(view);

        mViewBinding.buttonCreateTypeStart.setText(getString(R.string.dialog_createType_defaultTime));
        mViewBinding.buttonCreateTypeEnd.setText(getString(R.string.dialog_createType_defaultTime));
        mTimeStart = new LocalTime(0, 0);
        mTimeEnd = new LocalTime(0, 0);
        updateTimeCount();
        View.OnClickListener timeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Button button = (Button) v;
                TimePickerDialog timePicker;
                String[] timeSet = button.getText().toString().split(":");

                timePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        button.setText(time);
                        if(button == mViewBinding.buttonCreateTypeStart){
                            mTimeStart = new LocalTime(hourOfDay, minute);
                        } else if(button == mViewBinding.buttonCreateTypeEnd){
                            mTimeEnd = new LocalTime(hourOfDay, minute);
                        }

                        updateTimeCount();
                    }
                }, Integer.parseInt(timeSet[0]), Integer.parseInt(timeSet[1]), false);
                timePicker.setTitle(getString(R.string.dialog_timePicker_title));
                timePicker.show();
            }
        };
        mViewBinding.buttonCreateTypeStart.setOnClickListener(timeListener);
        mViewBinding.buttonCreateTypeEnd.setOnClickListener(timeListener);

        mColorButtons = new ArrayList<>();
        mColorButtons.add(mViewBinding.buttonCreateTypeColor1);
        mColorButtons.add(mViewBinding.buttonCreateTypeColor2);
        mColorButtons.add(mViewBinding.buttonCreateTypeColor3);
        mColorButtons.add(mViewBinding.buttonCreateTypeColor4);
        mColorButtons.add(mViewBinding.buttonCreateTypeColor5);
        mColorButtons.add(mViewBinding.buttonCreateTypeColor6);

        int positiveButton;
        //Create or edit mode
        if(mShiftType != null){
            fillDialog();
            //Set positive button
            positiveButton = R.string.dialog_createType_button_edit;
        } else {
            positiveButton = R.string.dialog_createType_button_create;
            mSelectedButton = mColorButtons.get(0);
            mColor = getResources().getColor(R.color.customToggle1);
        }

        View.OnClickListener colorButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton clickedButton = (ToggleButton) v;
                mSelectedButton.setChecked(false);
                mSelectedButton = clickedButton;
                int pos = mColorButtons.indexOf(mSelectedButton);
                //Get color
                TypedArray ta = mContext.getResources().obtainTypedArray(R.array.shiftType_colors);
                int[] colors = new int[ta.length()];
                for (int i = 0; i < ta.length(); i++) {
                    colors[i] = ta.getColor(i, 0);
                }
                ta.recycle();
                mColor = colors[pos];
            }
        };

        for (ToggleButton button : mColorButtons) {
            ViewGroup.LayoutParams buttonParams = button.getLayoutParams();
            buttonParams.height = buttonParams.width;
            button.setOnClickListener(colorButtonListener);
        }

        //Dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_createType_title)
                .setView(view)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton(R.string.dialog_createType_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FormUtils.closeKeyboard(mContext, mViewBinding.editTextLayoutCreateTypeName);
                    }
                });
        //Set positive button and return via listener
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                FormUtils.openKeyboard(mContext, mViewBinding.editTextLayoutCreateTypeName);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isReadyToClose = attemptCreateType();
                        if (isReadyToClose) {
                            FormUtils.closeKeyboard(mContext, mViewBinding.editTextLayoutCreateTypeName);
                            if(mShiftType == null){
                                mShiftType = new ShiftType();
                            }
                            mShiftType.setJodaStartTime(mTimeStart);
                            mShiftType.setJodaPeriod(mPeriod);
                            mShiftType.setActive(true);
                            mShiftType.setName(mName);
                            mShiftType.setTag(mTag);
                            mShiftType.setColor(mColor);
                            mListener.onDialogPositiveClick(mShiftType);
                            dialog.dismiss();
                        }
                    }
                });
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

    private void fillDialog(){

        mTimeStart = mShiftType.getJodaStartTime();
        mPeriod = mShiftType.getJodaPeriod();
        mTimeEnd = mShiftType.getJodaStartTime().plus(mPeriod);
        mColor = mShiftType.getColor();

        mViewBinding.editTextCreateTypeName.setText(mShiftType.getName());
        mViewBinding.editTextCreateTypeTag.setText(mShiftType.getTag());
        updateTimeCount();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        mViewBinding.buttonCreateTypeStart.setText(fmt.print(mTimeStart));
        mViewBinding.buttonCreateTypeEnd.setText(fmt.print(mTimeEnd));

        TypedArray ta = mContext.getResources().obtainTypedArray(R.array.shiftType_colors);
        int[] colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();

        for (int i = 0; i < colors.length; i++) {
            if (mColor == colors[i]) {
                mSelectedButton = mColorButtons.get(i);
                mSelectedButton.setChecked(true);
            } else {
                mColorButtons.get(i).setChecked(false);
            }
        }


    }

    private boolean attemptCreateType() {
        //Reset error messages
        mViewBinding.editTextLayoutCreateTypeName.setError(null);

        //Get strings from editText
        mName = mViewBinding.editTextCreateTypeName.getText().toString();
        mTag = mViewBinding.editTextCreateTypeTag.getText().toString();

        Boolean isReadyToClose = true;
        View focusView = null;

        /*Check for a valid name.*/
        if (TextUtils.isEmpty(mName)) {
            mViewBinding.editTextLayoutCreateTypeName
                    .setError(getString(R.string.form_error_field_required));
            focusView = mViewBinding.editTextCreateTypeName;
            isReadyToClose = false;
        } else if (!FormUtils.isDisplayNameValid(mName)) {
            mViewBinding.editTextLayoutCreateTypeName
                    .setError(getString(R.string.form_error_name));
            focusView = mViewBinding.editTextCreateTypeName;
            isReadyToClose = false;
        }

        if (TextUtils.isEmpty(mTag)) {
            mViewBinding.editTextLayoutCreateTypeTag
                    .setError(getString(R.string.form_error_field_required));
            if (focusView == null) focusView = mViewBinding.editTextCreateTypeTag;
            isReadyToClose = false;
        }

        if (!isReadyToClose) {
            focusView.requestFocus();
        }
        return isReadyToClose;
    }

    private void updateTimeCount (){
        mPeriod = new Period(mTimeStart, mTimeEnd);
        if(mTimeStart.isAfter(mTimeEnd)) mPeriod = mPeriod.plusHours(24);
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" h ")
                .appendMinutes()
                .appendSuffix(" min")
                .toFormatter();
        String hourCountDisplay = formatter.print(mPeriod);
        mViewBinding.textViewCreateTypeDuration.setText(hourCountDisplay);

    }

    public interface CreateTypeListener {
        void onDialogPositiveClick(ShiftType shiftType);
    }
}
