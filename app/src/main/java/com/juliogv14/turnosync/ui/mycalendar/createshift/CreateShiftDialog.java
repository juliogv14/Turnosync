package com.juliogv14.turnosync.ui.mycalendar.createshift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogCreateShiftBinding;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Julio on 14/06/2018.
 * CreateShiftDialog
 */

public class CreateShiftDialog extends DialogFragment {

    //Constant
    private static final String DATE_KEY = "date";
    private static final String USER_REF_KEY = "userRef";
    private static final String SHIFT_TYPES_KEY = "shiftTypes";
    private static final String CURRENT_HOURS_KEY = "currentHours";
    private static final String WEEKLY_HOURS_KEY = "weeklyHours";

    //Binding
    private DialogCreateShiftBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private CreateShiftListener mListener;

    //Variables
    private Date mDay;
    private UserRef mUserRef;
    private ArrayList<ShiftType> mShiftTypesList;

    private long mWeeklyHours;
    private Period mSetHours;
    private Period mShiftPeriod;
    private int mAddDays = 1;


    //Layout
    private ArrayList<ToggleButton> mDayButtons;

    public static CreateShiftDialog newInstance(Date date, UserRef userRef, Map<String, ShiftType> shiftTypes, long currentHours, long weeklyHours) {
        CreateShiftDialog fragment = new CreateShiftDialog();
        Bundle args = new Bundle();
        args.putLong(DATE_KEY, date.getTime());
        args.putParcelable(USER_REF_KEY, userRef);
        args.putParcelableArrayList(SHIFT_TYPES_KEY, new ArrayList<>(shiftTypes.values()));
        args.putLong(CURRENT_HOURS_KEY, currentHours);
        args.putLong(WEEKLY_HOURS_KEY, weeklyHours);
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mDay = new Date(args.getLong(DATE_KEY));
            mUserRef = args.getParcelable(USER_REF_KEY);
            mShiftTypesList = args.getParcelableArrayList(SHIFT_TYPES_KEY);
            mSetHours = new Period(args.getLong(CURRENT_HOURS_KEY));
            mWeeklyHours = args.getLong(WEEKLY_HOURS_KEY);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_shift, null);
        mViewBinding = DialogCreateShiftBinding.bind(view);

        mDayButtons = new ArrayList<>();
        mDayButtons.add(mViewBinding.buttonCreateShift1);
        mDayButtons.add(mViewBinding.buttonCreateShift2);
        mDayButtons.add(mViewBinding.buttonCreateShift3);
        mDayButtons.add(mViewBinding.buttonCreateShift4);
        mDayButtons.add(mViewBinding.buttonCreateShift5);
        mDayButtons.add(mViewBinding.buttonCreateShift6);
        mDayButtons.add(mViewBinding.buttonCreateShift7);

        //Set current day
        Calendar cal = new GregorianCalendar(Locale.FRANCE);
        cal.setTime(mDay);
        int buttonPos = cal.get(Calendar.DAY_OF_WEEK)-2;
        if(buttonPos == -1) buttonPos = 6;
        mDayButtons.get(buttonPos).setChecked(true);

        View.OnClickListener buttonClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton clickedButton = (ToggleButton) v;
                if(clickedButton.isChecked()){
                    mAddDays++;
                } else {
                    mAddDays--;
                }
                updateTimeCount();
            }
        };

        //Togglebutton setup
        String[] weekDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);
        for (int i = 0; i < mDayButtons.size(); i++) {
            ToggleButton button = mDayButtons.get(i);
            button.setText(weekDays[i]);
            button.setTextOn(null);
            button.setTextOff(null);
            ViewGroup.LayoutParams buttonParams = button.getLayoutParams();
            buttonParams.height = buttonParams.width;
            button.setOnClickListener(buttonClick);
        }



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_createShift_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_createShift_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArrayList<Shift> newShifts = new ArrayList<>();
                        int spinnerPos = mViewBinding.spinnerCreateShiftType.getSelectedItemPosition();

                        Calendar cal = new GregorianCalendar();
                        cal.setTime(mDay);
                        for (int j = 0; j < mDayButtons.size(); j++) {
                            ToggleButton toggleButton = mDayButtons.get(j);
                            if(toggleButton.isChecked()){
                                cal.set(Calendar.DAY_OF_WEEK, j+2);
                                if(j==6) cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                                Shift shift = new Shift(mUserRef.getUid(), mShiftTypesList.get(spinnerPos).getId(), cal.getTime(), "","");
                                newShifts.add(shift);
                            }
                        }
                        mListener.onCreateShiftCreate(newShifts);
                    }
                })
                .setNegativeButton(R.string.dialog_createShift_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //FormUtils.closeKeyboard(mContext, mViewBinding.editTextEmail);
                    }
                });

        final AlertDialog dialog = builder.create();

        //Types spinner
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
                mViewBinding.textViewCreateShiftTag.setText(mShiftTypesList.get(position).getTag());
                mViewBinding.textViewCreateShiftTag.setBackgroundColor(mShiftTypesList.get(position).getColor());

                //Time interval
                DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
                LocalTime startTime = mShiftTypesList.get(position).getJodaStartTime();
                mShiftPeriod = mShiftTypesList.get(position).getJodaPeriod();
                LocalTime endTime = startTime.plus(mShiftPeriod);

                String startHour = fmt.print(startTime);
                String endHour = fmt.print(endTime);
                String timeInterval = getString(R.string.dialog_createShift_schedule) + ": " + startHour + " - " + endHour;
                mViewBinding.textViewCreateShiftTime.setText(timeInterval);
                //Added hours
                updateTimeCount();
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

    private void updateTimeCount(){

        Period addedTime = mShiftPeriod.multipliedBy(mAddDays);

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" h")
                .appendMinutes()
                .appendSuffix(" min")
                .toFormatter();
        String hourCountDisplay = formatter.print(mSetHours) + " +" + formatter.print(addedTime);
        mViewBinding.textViewCreateShiftHours.setText(hourCountDisplay);
        if(mSetHours.plus(addedTime).getHours() > mWeeklyHours){
            Toast.makeText(mContext, "Current weekly hours are above the limit. Limit: " + mWeeklyHours, Toast.LENGTH_LONG).show();
        }
    }

    public interface CreateShiftListener {
        void onCreateShiftCreate(ArrayList<Shift> newShifts);
    }
}
