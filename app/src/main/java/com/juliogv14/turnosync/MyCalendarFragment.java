package com.juliogv14.turnosync;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.Workgroup;
import com.juliogv14.turnosync.databinding.ContentMycalendarBinding;
import com.juliogv14.turnosync.databinding.ItemShiftBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julio on 20/01/2018.
 * MyCalendarFragment.java
 */

public class MyCalendarFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    protected ContentMycalendarBinding mViewBinding;

    //Firebase
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mFirebaseUser;
    private ListenerRegistration mShiftsListener;

    private OnCalendarFragmentInteractionListener mListener;

    private static final String CURRENT_WORKGROUP_KEY = "currentWorkgroup";
    private Workgroup mWorkgroup;

    //GridAdapter
    private ShiftItemsAdapter mGridAdapter;
    private ArrayList<Shift> mShiftList;


    public static MyCalendarFragment newInstance(Workgroup workgroup) {
        MyCalendarFragment f = new MyCalendarFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_WORKGROUP_KEY, workgroup);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCalendarFragmentInteractionListener) {
            mListener = (OnCalendarFragmentInteractionListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mWorkgroup = args.getParcelable(CURRENT_WORKGROUP_KEY);
        }
        mShiftList = new ArrayList<>();
    }

    //Inflate view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = ContentMycalendarBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentCreated(R.id.nav_item_calendar);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mViewBinding.textViewWorkgroup.setText(mWorkgroup.getDisplayname());

        mGridAdapter = new ShiftItemsAdapter((Activity) mListener, R.layout.content_mycalendar, mShiftList);
        mViewBinding.gridViewCalendar.setAdapter(mGridAdapter);
        attatchShiftsListener();
        mViewBinding.buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testData();
            }
        });


        Log.d(TAG, "Start MyCalendarFragment");


    }

    private void attatchShiftsListener() {
        String userID = mFirebaseUser.getUid();
        CollectionReference shiftsReference = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                .collection(getString(R.string.data_ref_shifts));
        mShiftsListener = shiftsReference.whereEqualTo("userID", userID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    if (dc != null) {
                        Shift shift = dc.getDocument().toObject(Shift.class);

                        switch (dc.getType()) {
                            case ADDED:
                                mShiftList.add(shift);
                                mGridAdapter.notifyDataSetChanged();
                                break;
                            case MODIFIED:
                                //TODO shift modified
                                break;
                            case REMOVED:
                                //TODO shift removed
                                break;
                        }
                    }
                }
            }

        });

    }


    private void testData() {

        Shift shift = new Shift("M", mFirebaseUser.getUid(), 2018, 2, 8, "6:00", "11:00");

        CollectionReference shiftsReference = mFirebaseFirestore.collection(getString(R.string.data_ref_workgroups)).document(mWorkgroup.getWorkgroupID())
                .collection(getString(R.string.data_ref_shifts));

        shiftsReference.add(shift);
    }


    private class ShiftItemsAdapter extends ArrayAdapter<Shift> {

        private ArrayList<Shift> data;
        ItemShiftBinding itemBinding;


        ShiftItemsAdapter(@NonNull Context context, int resource, @NonNull List<Shift> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_shift, parent, false);
            }
            itemBinding = DataBindingUtil.bind(convertView);

            Shift shift = getItem(position);
            if (shift != null) {
                itemBinding.textViewShiftType.setText(shift.getType());
                itemBinding.textViewShiftInterval.setText(shift.getFormattedInterval());
            }
            return convertView;
        }

    }

    public interface OnCalendarFragmentInteractionListener extends OnFragmentInteractionListener {
        void onShiftSelected(Shift shift);
    }

}
