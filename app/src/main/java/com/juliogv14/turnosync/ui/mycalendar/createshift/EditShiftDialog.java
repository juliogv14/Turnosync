package com.juliogv14.turnosync.ui.mycalendar.createshift;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogEditShiftBinding;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Julio on 15/06/2018.
 * EditShiftDialog
 */

public class EditShiftDialog extends DialogFragment {

    //Constant
    private static final String DATE_KEY = "date";
    private static final String USER_REF_KEY = "userRef";
    private static final String SHIFT_TYPES_KEY = "shiftTypes";
    private static final String USERS_REF_LIST = "userList";
    private static final String SELECTED_SHIFT_KEY = "selectedShift";


    //Strings
    private DialogEditShiftBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private EditShiftListener mListener;

    //Variables
    private Date mDay;
    private UserRef mUserRef;
    private ArrayList<ShiftType> mShiftTypesList;
    private Shift mSelectedShift;
    private ArrayList<UserRef> mWorkgroupUsers;

    public static EditShiftDialog newInstance(Date date, UserRef userRef, Map<String, ShiftType> shiftTypes, ArrayList<UserRef> userList, Shift shift) {
        EditShiftDialog fragment = new EditShiftDialog();
        Bundle args = new Bundle();
        args.putLong(DATE_KEY, date.getTime());
        args.putParcelable(USER_REF_KEY, userRef);
        args.putParcelableArrayList(SHIFT_TYPES_KEY, new ArrayList<>(shiftTypes.values()));
        args.putParcelableArrayList(USERS_REF_LIST, userList);
        args.putParcelable(SELECTED_SHIFT_KEY, shift);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof EditShiftListener) {
            mListener = (EditShiftListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EditShiftListener");
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
            mWorkgroupUsers = args.getParcelableArrayList(USERS_REF_LIST);
            mSelectedShift = args.getParcelable(SELECTED_SHIFT_KEY);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_shift, null);
        mViewBinding = DialogEditShiftBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_editShift_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_editShift_button_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int userPos = mViewBinding.spinnerEditShiftUser.getSelectedItemPosition();
                        int typePos = mViewBinding.spinnerEditShiftType.getSelectedItemPosition();
                        String oldUserId = mUserRef.getUid();
                        String newUserId = mWorkgroupUsers.get(userPos).getUid();

                        if(oldUserId.equals(newUserId) && !mSelectedShift.getType().equals(mShiftTypesList.get(typePos).getId())){
                            mSelectedShift.setType(mShiftTypesList.get(typePos).getId());
                            mListener.onEditShiftChange(mSelectedShift, mSelectedShift);
                        } else if (!oldUserId.equals(newUserId)) {
                            Shift newShift = new Shift(newUserId, mShiftTypesList.get(typePos).getId(), mDay, "","");
                            mListener.onEditShiftChange(mSelectedShift, newShift);
                        }
                    }
                })
                .setNeutralButton(R.string.dialog_editShift_button_remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onEditShiftRemove(mSelectedShift);
                    }
                })
                .setNegativeButton(R.string.dialog_editShift_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //FormUtils.closeKeyboard(mContext, mViewBinding.editTextEmail);
                    }
                });

        final AlertDialog dialog = builder.create();

        //Users spinner adapter
        List<String> displayUsers = new ArrayList<>();
        for (UserRef user : mWorkgroupUsers) {
            displayUsers.add(user.getUid());
        }

        ArrayAdapter<String> spinnerUsersAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, displayUsers);
        spinnerUsersAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewBinding.spinnerEditShiftUser.setAdapter(spinnerUsersAdapter);
        mViewBinding.spinnerEditShiftUser.setSelection(displayUsers.indexOf(mUserRef.getUid()));

        //Types spinner adapter
        List<String> displayTypes = new ArrayList<>();
        int typePos = 0;
        for (int i = 0; i < mShiftTypesList.size(); i++) {
            ShiftType type = mShiftTypesList.get(i);
            displayTypes.add(type.getName());
            if (TextUtils.equals(mSelectedShift.getType(), type.getId())) {
                typePos = i;
            }
        }

        ArrayAdapter<String> spinnerTypesAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, displayTypes);
        spinnerTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewBinding.spinnerEditShiftType.setAdapter(spinnerTypesAdapter);
        mViewBinding.spinnerEditShiftType.setSelection(typePos);
        mViewBinding.spinnerEditShiftType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Tag
                mViewBinding.textViewEditShiftTag.setText(mShiftTypesList.get(position).getTag());
                mViewBinding.textViewEditShiftTag.setBackgroundColor(mShiftTypesList.get(position).getColor());

                //Time interval
                SimpleDateFormat formatDayHour = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String startHour = formatDayHour.format(mShiftTypesList.get(position).getStartTime());
                String endHour = formatDayHour.format(mShiftTypesList.get(position).getEndTime());
                String timeInterval = getString(R.string.dialog_editShift_schedule) + ": " + startHour + " - " + endHour;
                mViewBinding.textViewEditShiftTime.setText(timeInterval);
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

    public interface EditShiftListener {
        void onEditShiftChange(Shift oldShift, Shift newShift);
        void onEditShiftRemove(Shift removedShifts);
    }
}
