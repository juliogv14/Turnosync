package com.juliogv14.turnosync.ui.mycalendar.createshift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.DialogShiftChangesBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfirmChangesDialog extends DialogFragment {

    //Keys
    private static final String CHANGES_MAP = "changesMap";
    private static final String SHIFT_TYPES_MAP = "shiftTypesMap";
    private static final String USER_REF_LIST = "userRefList";

    //Binding
    private DialogShiftChangesBinding mViewBinding;

    //Parent fragment
    private Context mContext;
    private ConfirmChangesListener mListener;

    //Variables
    private HashMap<String, ArrayList<Shift>> mShiftChangesMap;
    private HashMap<String, ShiftType> mShiftTypesMap;
    private ArrayList<UserRef> mUserRefList;

    public static ConfirmChangesDialog newInstance(HashMap<String, ArrayList<Shift>> shiftChanges, HashMap<String, ShiftType> shiftTypes, ArrayList<UserRef> userRefs){
        ConfirmChangesDialog fragment = new ConfirmChangesDialog();
        Bundle args = new Bundle();
        args.putSerializable(CHANGES_MAP, shiftChanges);
        args.putSerializable(SHIFT_TYPES_MAP, shiftTypes);
        args.putParcelableArrayList(USER_REF_LIST, userRefs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getParentFragment() instanceof ConfirmChangesListener) {
            mListener = (ConfirmChangesListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ConfirmChangesListener");
        }

    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args!= null){
            mShiftChangesMap = (HashMap<String, ArrayList<Shift>>) args.getSerializable(CHANGES_MAP);
            mShiftTypesMap = (HashMap<String, ShiftType>) args.getSerializable(SHIFT_TYPES_MAP);
            mUserRefList = args.getParcelableArrayList(USER_REF_LIST);
        }

        HashMap<String, UserRef> userRefMap = new HashMap<>();
        for (UserRef userRef : mUserRefList) {
            userRefMap.put(userRef.getUid(), userRef);
        }


        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_shift_changes, null);
        mViewBinding = DialogShiftChangesBinding.bind(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_shiftChanges_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_shiftChanges_button_confirm, new DialogInterface.OnClickListener() {
                    //Add user to workgroup
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onConfirmChanges();
                    }
                })
                .setNegativeButton(R.string.dialog_shiftChanges_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerShiftChanges.setLayoutManager(layoutManager);
        mViewBinding.recyclerShiftChanges.setHasFixedSize(true);
        mViewBinding.recyclerShiftChanges.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        ShiftChangesAdapter adapter = new ShiftChangesAdapter(mContext, mShiftChangesMap, mShiftTypesMap, userRefMap);
        mViewBinding.recyclerShiftChanges.setAdapter(adapter);



        AlertDialog dialog = builder.create();
        return dialog;


    }

    public interface ConfirmChangesListener {
        void onConfirmChanges();
    }
}
