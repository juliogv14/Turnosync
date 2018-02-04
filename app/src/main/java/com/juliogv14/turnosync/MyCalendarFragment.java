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

import com.google.firebase.firestore.FirebaseFirestore;
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
        mViewBinding.textViewWorkgroup.setText(mWorkgroup.getDisplayname());

        mGridAdapter = new ShiftItemsAdapter((Activity) mListener, R.layout.content_mycalendar, mShiftList);
        mViewBinding.gridViewCalendar.setAdapter(mGridAdapter);

        mViewBinding.buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testData();
            }
        });

        Log.d(TAG, "Start MyCalendarFragment");
    }

    private void testData() {

        Shift shift = new Shift("M", "6:00", "11:00");
        mShiftList.add(shift);
        mGridAdapter.notifyDataSetChanged();
    }

    public interface OnCalendarFragmentInteractionListener extends OnFragmentInteractionListener {
        void onShiftSelected(Shift shift);
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

}
