package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.FragmentShiftTypesBinding;
import com.juliogv14.turnosync.ui.drawerlayout.OnFragmentInteractionListener;
import com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes.CreateTypeDialog;
import com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes.ShiftTypesAdapter;

import java.util.ArrayList;

/**
 * Created by Julio on 08/06/2018.
 * ShiftTypesFragmentFragment
 */
public class ShiftTypesFragment extends Fragment implements ShiftTypesAdapter.TypeOnClickListener, CreateTypeDialog.CreateTypeListener {

    //Log TAG
    private final String TAG = this.getClass().getSimpleName();

    //Constants
    private static final String WORKGROUP_KEY = "workgroup";

    //Binding
    private FragmentShiftTypesBinding mViewBinding;

    //Listener
    private OnShiftTypesListener mListener;

    //Firebase Firestore
    private FirebaseFirestore mFirebaseFirestore;
    private ListenerRegistration mShiftTypesListener;

    //Workgroup
    private UserWorkgroup mWorkgroup;
    private ArrayList<ShiftType> shifTypesList;

    public ShiftTypesFragment() {
    }

    public static ShiftTypesFragment newInstance(UserWorkgroup workgroup) {
        ShiftTypesFragment fragment = new ShiftTypesFragment();
        Bundle args = new Bundle();
        args.putParcelable(WORKGROUP_KEY, workgroup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShiftTypesListener) {
            mListener = (OnShiftTypesListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(WORKGROUP_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        shifTypesList = new ArrayList<>();
        mViewBinding = FragmentShiftTypesBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentSwapped(R.string.fragment_shiftTypes);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        attatchShiftTypesListener();

        //RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager((Context) mListener, LinearLayoutManager.VERTICAL, false);
        mViewBinding.recyclerShiftTypes.setLayoutManager(layoutManager);
        mViewBinding.recyclerShiftTypes.setHasFixedSize(true);
        ShiftTypesAdapter adapter = new ShiftTypesAdapter((Context) mListener, this, shifTypesList, mWorkgroup.getRole());
        mViewBinding.recyclerShiftTypes.setAdapter(adapter);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mShiftTypesListener != null) {
            mShiftTypesListener.remove();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_shifttypes, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.mutate();
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
            if(menu.getItem(i).getItemId() == R.id.action_shiftTypes_newType && !mWorkgroup.getRole().equals(UserRoles.MANAGER.toString())){
                menu.getItem(i).setVisible(false);
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_shiftTypes_newType) {
            CreateTypeDialog dialog = new CreateTypeDialog();
            dialog.show(getChildFragmentManager(), "createTypeDialog");
            return true;
        }

        return false;
    }

    private void attatchShiftTypesListener() {
        CollectionReference shiftTypesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shiftTypes));

        mShiftTypesListener = shiftTypesColl.orderBy("name").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null){
                    return;
                }
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = docChange.getDocument();
                    if (doc.exists()) {
                        ShiftType shiftType = doc.toObject(ShiftType.class);

                        switch (docChange.getType()) {
                            case ADDED:
                                //Added
                                if (shiftType.isActive()) {
                                    shifTypesList.add(docChange.getNewIndex(), shiftType);
                                }
                                break;
                            case MODIFIED:

                                //Modified, same position
                                if (docChange.getOldIndex() == docChange.getNewIndex()) {
                                    //Active modified
                                    if (shiftType.isActive()) {
                                        shifTypesList.set(docChange.getOldIndex(), shiftType);
                                    } else {
                                        shifTypesList.remove(docChange.getOldIndex());
                                    }
                                } else {
                                    //Modified, differnt position
                                    if (shiftType.isActive()) {

                                        shifTypesList.remove(docChange.getOldIndex());
                                        shifTypesList.add(docChange.getNewIndex(), shiftType);
                                    }
                                }
                                break;
                            case REMOVED:
                                //Removed
                                shifTypesList.remove(docChange.getOldIndex());
                                break;
                        }
                    }
                }
                mViewBinding.recyclerShiftTypes.getAdapter().notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onClickEditType(ShiftType type) {
        Toast.makeText((Context) mListener, "Edit type" + type.getId(), Toast.LENGTH_SHORT).show();
        CreateTypeDialog dialog = CreateTypeDialog.newInstance(type);
        dialog.show(getChildFragmentManager(), "createTypeDialog");
    }

    @Override
    public void onClickRemoveType(final ShiftType type) {

        new AlertDialog.Builder((Context)mListener).setTitle(getString(R.string.dialog_removeType_title))
                .setMessage(getString(R.string.dialog_removeType_message))
                .setPositiveButton(getString(R.string.dialog_removeType_button_remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CollectionReference shiftTypesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                                .collection(getString(R.string.data_ref_shiftTypes));

                        shiftTypesColl.document(type.getId()).update(getString(R.string.data_key_active), false);
                        mViewBinding.recyclerShiftTypes.getAdapter().notifyDataSetChanged();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_removeType_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

    }

    @Override
    public void onDialogPositiveClick(ShiftType shiftType) {
        CollectionReference shiftTypesColl = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupId())
                .collection(getString(R.string.data_ref_shiftTypes));

        DocumentReference docRef;

        if(shiftType.getId() == null){
            //New type
            docRef = shiftTypesColl.document();
            shiftType.setId(docRef.getId());
        } else {
            //Edit type
            docRef = shiftTypesColl.document(shiftType.getId());
        }
        docRef.set(shiftType);
    }

    public interface OnShiftTypesListener extends OnFragmentInteractionListener {

    }
}
