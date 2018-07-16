package com.juliogv14.turnosync.ui.mycalendar.changerequests;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.databinding.ItemChangeRequestBinding;
import com.juliogv14.turnosync.databinding.LayoutShiftChangeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChangeRequestsAdapter extends RecyclerView.Adapter<ChangeRequestsAdapter.ChangeRequestsViewHolder> {

    private Context mContext;
    private ChangeRequestListener mListener;
    private String mRole;
    private Map<String, ShiftType> mShiftTypesMap;
    private Map<String, UserRef> mUserRefsMap;
    private List<ChangeRequest> mChangeRequests;

    public ChangeRequestsAdapter(Context context, ChangeRequestListener listener, String role, Map<String, ShiftType> shiftTypes, Map<String, UserRef> userRefs, List<ChangeRequest> changeRequests) {
        this.mContext = context;
        this.mListener = listener;
        this.mRole = role;
        this.mShiftTypesMap = shiftTypes;
        this.mUserRefsMap = userRefs;
        this.mChangeRequests = changeRequests;
    }

    @NonNull
    @Override
    public ChangeRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChangeRequestBinding itemBinding = ItemChangeRequestBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ChangeRequestsViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChangeRequestsViewHolder holder, int position) {
        ChangeRequest changeRequest = mChangeRequests.get(position);
        holder.bind(changeRequest);
    }

    @Override
    public int getItemCount() {
        return mChangeRequests.size();
    }

    public interface ChangeRequestListener {
        void onAcceptRequested(ChangeRequest changeRequest);
        void onApproveAccepted(ChangeRequest changeRequest);
        void onDenyRequested(ChangeRequest changeRequest);
        void onDenyAccepted(ChangeRequest changeRequest);
    }

    class ChangeRequestsViewHolder extends RecyclerView.ViewHolder {

        ItemChangeRequestBinding binding;

        public ChangeRequestsViewHolder(ItemChangeRequestBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;
        }

        public void bind(final ChangeRequest changeRequest) {
            Shift ownShift = changeRequest.getOwnShift();
            Shift otherShift = changeRequest.getOtherShift();
            Date timestampDate = changeRequest.getTimestamp();
            final String state = changeRequest.getState();

            //Shift info
            displayShift(binding.ownShift, ownShift);
            displayShift(binding.otherShift, otherShift);

            //TimeStamp
            SimpleDateFormat formatTimeStamp = new SimpleDateFormat("dd MMM", Locale.getDefault());
            String timestamp = formatTimeStamp.format(timestampDate);
            binding.textViewRequestTimestamp.setText(timestamp);

            //State
            switch (state) {
                case ChangeRequest.REQUESTED:
                    binding.textViewRequestState.setText(mContext.getString(R.string.requests_change_requested));
                    break;
                case ChangeRequest.ACCEPTED:
                    binding.textViewRequestState.setText(mContext.getString(R.string.requests_change_accepted));
                    break;
                case ChangeRequest.APPROVED:
                    binding.textViewRequestState.setText(mContext.getString(R.string.requests_change_approved));
                    break;
            }
            // TODO: 14/07/2018 Consider argument
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Buttons
            if ((TextUtils.equals(state, ChangeRequest.ACCEPTED) && TextUtils.equals(mRole, UserRoles.MANAGER.toString()))
                    ||(TextUtils.equals(state, ChangeRequest.REQUESTED) && TextUtils.equals(otherShift.getUserId(), userId))) {
                //Manager or user who can accept
                binding.buttonRequestAccept.setVisibility(View.VISIBLE);
                binding.buttonRequestDeny.setVisibility(View.VISIBLE);
            } else if((TextUtils.equals(state, ChangeRequest.REQUESTED) && TextUtils.equals(mRole, UserRoles.MANAGER.toString()))
                    || (!TextUtils.equals(state, ChangeRequest.APPROVED) && (TextUtils.equals(otherShift.getUserId(), userId) || TextUtils.equals(ownShift.getUserId(), userId)))){
                binding.buttonRequestAccept.setVisibility(View.INVISIBLE);
                binding.buttonRequestDeny.setVisibility(View.VISIBLE);
            } else {
                binding.buttonRequestAccept.setVisibility(View.INVISIBLE);
                binding.buttonRequestDeny.setVisibility(View.INVISIBLE);
            }


            binding.buttonRequestAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case ChangeRequest.REQUESTED:
                            mListener.onAcceptRequested(changeRequest);
                            break;
                        case ChangeRequest.ACCEPTED:
                            mListener.onApproveAccepted(changeRequest);
                            break;
                    }
                }
            });
            binding.buttonRequestDeny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case ChangeRequest.REQUESTED:
                            mListener.onDenyRequested(changeRequest);
                            break;
                        case ChangeRequest.ACCEPTED:
                            mListener.onDenyAccepted(changeRequest);
                            break;
                    }
                }
            });
        }

        private void displayShift(LayoutShiftChangeBinding shiftItem, Shift shift) {
            //Label
            String userLabel = mContext.getString(R.string.dialog_requestChange_user) + ": " + mUserRefsMap.get(shift.getUserId()).getShortName();
            shiftItem.textViewRequestLabel.setText(userLabel);

            //Date
            SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            SimpleDateFormat formatWeekDay = new SimpleDateFormat("EEEE", Locale.getDefault());
            String date = formatDate.format(shift.getDate()) + System.getProperty("line.separator") + formatWeekDay.format(shift.getDate());
            String weekDay = formatWeekDay.format(shift.getDate());
            shiftItem.textViewRequestDate.setText(date);
            shiftItem.textViewRequestWeekDay.setText(weekDay);

            //Shift name
            ShiftType shiftType = mShiftTypesMap.get(shift.getType());
            String shiftName = shiftType.getName();
            shiftItem.textViewRequestName.setText(shiftName);
        }
    }
}
