package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.databinding.DialogWeeklyHoursBinding;

/**
 * Created by Julio on 06/07/2018.
 * WeeklyHoursDialog
 */

public class WeeklyHoursDialog extends DialogFragment {

    //Constants
    private static final String WEEKLY_HOURS_KEY = "weeklyHours";

    //Binding
    private DialogWeeklyHoursBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private WeeklyHoursDialogListener mListener;

    //Variables
    private long mWeeklyHours;

    public static WeeklyHoursDialog newInstance(long weeklyHours) {

        Bundle args = new Bundle();
        args.putLong(WEEKLY_HOURS_KEY, weeklyHours);
        WeeklyHoursDialog fragment = new WeeklyHoursDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof WeeklyHoursDialogListener) {
            mListener = (WeeklyHoursDialogListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AddUserListener");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mWeeklyHours = args.getLong(WEEKLY_HOURS_KEY);
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_weekly_hours, null);
        mViewBinding = DialogWeeklyHoursBinding.bind(view);
        mViewBinding.numberPickerHours.setMinValue(1);
        mViewBinding.numberPickerHours.setMaxValue(72);
        mViewBinding.numberPickerHours.setValue((int)mWeeklyHours);
        mViewBinding.numberPickerHours.setWrapSelectorWheel(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_weeklyHours_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_weeklyHours_button_set, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        long weeklyhours = mViewBinding.numberPickerHours.getValue();
                        mListener.onSetWeekyHours(weeklyhours);
                    }
                })
                .setNegativeButton(R.string.dialog_adduser_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mListener = null;
    }

    public interface WeeklyHoursDialogListener {
        void onSetWeekyHours(long hours);
    }
}
