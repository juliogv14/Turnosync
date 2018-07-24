package com.juliogv14.turnosync.ui.mycalendar.changerequests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogRequestChangeBinding;
import com.juliogv14.turnosync.databinding.ItemChangeBinding;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RequestChangeDialog extends DialogFragment {

    //Keys
    private static final String OWN_SHIFT_KEY = "ownShift";
    private static final String OTHER_SHIFT_KEY = "otherShift";
    private static final String SHIFT_TYPES_MAP = "shiftTypesMap";
    private static final String USER_REF_LIST = "userRefList";

    //Binding
    private DialogRequestChangeBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private RequestChangeListener mListener;

    //Variables
    private Shift mOwnShift;
    private Shift mOtherShift;
    private HashMap<String, ShiftType> mShiftTypesMap;
    private HashMap<String, UserRef> mUserRefsMap;

    public static RequestChangeDialog newInstance(Shift ownShift, Shift otherShift, HashMap<String, ShiftType> shiftTypes, ArrayList<UserRef> userRefs){
        RequestChangeDialog fragment = new RequestChangeDialog();
        Bundle args = new Bundle();
        args.putParcelable(OWN_SHIFT_KEY, ownShift);
        args.putParcelable(OTHER_SHIFT_KEY, otherShift);
        args.putSerializable(SHIFT_TYPES_MAP, shiftTypes);
        args.putParcelableArrayList(USER_REF_LIST, userRefs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof RequestChangeListener) {
            mListener = (RequestChangeListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RequestChangeListener");
        }

    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args!= null){
            mOwnShift = args.getParcelable(OWN_SHIFT_KEY);
            mOtherShift = args.getParcelable(OTHER_SHIFT_KEY);
            mShiftTypesMap = (HashMap<String, ShiftType>) args.getSerializable(SHIFT_TYPES_MAP);
            List<UserRef> userRefList = args.getParcelableArrayList(USER_REF_LIST);
            mUserRefsMap = new HashMap<>();
            for (UserRef userRef : userRefList) {
                mUserRefsMap.put(userRef.getUid(), userRef);
            }
        }



        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_request_change, null);
        mViewBinding = DialogRequestChangeBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_requestChange_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_requestChange_button_request, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onRequestShiftChange(new ChangeRequest(mOwnShift, mOtherShift, new GregorianCalendar().getTime()));
                    }
                })
                .setNegativeButton(R.string.dialog_requestChange_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        //Display ownShift
        displayShift(mViewBinding.shiftItemOwnShift, mOwnShift);
        displayShift(mViewBinding.shiftItemOtherShift, mOtherShift);

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onCancelShiftChange();
        super.onDismiss(dialog);
    }

    private void displayShift(ItemChangeBinding shiftItem, Shift shift){

        //Label
        String userLabel = getString(R.string.dialog_requestChange_user) + ": " + mUserRefsMap.get(shift.getUserId()).getShortName();
        shiftItem.textViewChangeLabel.setText(userLabel);

        //Date
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM", Locale.getDefault());
        SimpleDateFormat formatWeekDay = new SimpleDateFormat("EEEE", Locale.getDefault());
        String date = formatDate.format(shift.getDate()) + System.getProperty("line.separator") + formatWeekDay.format(shift.getDate());
        String weekDay = formatWeekDay.format(shift.getDate());
        shiftItem.textViewChangeDate.setText(date);
        shiftItem.textViewChangeWeekDay.setText(weekDay);

        //Shift name
        ShiftType shiftType = mShiftTypesMap.get(shift.getType());
        String shiftName = mContext.getString(R.string.dialog_shiftChanges_shift) + ": " + shiftType.getName();
        shiftItem.textViewChangeName.setText(shiftName);

        //Time interval
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        LocalTime startTime = shiftType.getJodaStartTime();
        Period period = shiftType.getJodaPeriod();
        LocalTime endTime = startTime.plus(period);
        String startHour = fmt.print(startTime);
        String endHour = fmt.print(endTime);
        String timeInterval = startHour + " - " + endHour;
        shiftItem.textViewChangeTime.setText(timeInterval);

        //Tag and color
        shiftItem.textViewChangeTag.setText(shiftType.getTag());
        GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
        background.setColor(shiftType.getColor());
        shiftItem.textViewChangeTag.setBackground(background);
    }

    public interface RequestChangeListener {
        void onRequestShiftChange(ChangeRequest changeRequest);
        void onCancelShiftChange();
    }
}
