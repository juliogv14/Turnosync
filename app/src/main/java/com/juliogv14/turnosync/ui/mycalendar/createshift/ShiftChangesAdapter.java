package com.juliogv14.turnosync.ui.mycalendar.createshift;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.databinding.ItemChangeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ShiftChangesAdapter extends RecyclerView.Adapter<ShiftChangesAdapter.ShiftChangesViewHolder>{

    private Context mContext;
    private HashMap<String, ArrayList<Shift>> mShiftChanges;
    private HashMap<String, ShiftType> mShiftTypes;

    public ShiftChangesAdapter(Context context, HashMap<String, ArrayList<Shift>> shiftChanges, HashMap<String, ShiftType> shiftTypes) {
        this.mContext = context;
        this.mShiftChanges = shiftChanges;
        this.mShiftTypes = shiftTypes;
    }

    @NonNull
    @Override
    public ShiftChangesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChangeBinding itemBinding = ItemChangeBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ShiftChangesViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftChangesViewHolder holder, int position) {
        List<Shift> listAdded = mShiftChanges.get(mContext.getString(R.string.data_changes_added));
        List<Shift> listRemoved = mShiftChanges.get(mContext.getString(R.string.data_changes_removed));
        List<Shift> listEditedNew = mShiftChanges.get(mContext.getString(R.string.data_changes_editedNew));
        List<Shift> listEditedOld = mShiftChanges.get(mContext.getString(R.string.data_changes_editedOld));

        //Order of displaying
        if (position < listAdded.size()){
            Shift shift = listAdded.get(position);
            holder.bind(R.string.data_changes_added, shift, mShiftTypes.get(shift.getType()));
        } else if (position < listAdded.size() + listRemoved.size()){
            Shift shift = listRemoved.get(position-listAdded.size());
            holder.bind(R.string.data_changes_removed, shift, mShiftTypes.get(shift.getType()));
        } else if (position < listAdded.size() + listRemoved.size() + listEditedNew.size()){
            Shift shift = listEditedNew.get(position-listAdded.size()-listRemoved.size());
            holder.bind(R.string.data_changes_editedNew, shift, mShiftTypes.get(shift.getType()));
        } else if (position < listAdded.size() + listRemoved.size() + listEditedNew.size() + listEditedOld.size()){
            Shift shift = listEditedOld.get(position-listAdded.size()-listRemoved.size()-listEditedNew.size());
            holder.bind(R.string.data_changes_editedOld, shift, mShiftTypes.get(shift.getType()));
        }
    }

    @Override
    public int getItemCount() {
        return  mShiftChanges.get(mContext.getString(R.string.data_changes_added)).size()
                + mShiftChanges.get(mContext.getString(R.string.data_changes_removed)).size()
                + mShiftChanges.get(mContext.getString(R.string.data_changes_editedNew)).size()
                + mShiftChanges.get(mContext.getString(R.string.data_changes_editedOld)).size();
    }

    class ShiftChangesViewHolder extends RecyclerView.ViewHolder {

        ItemChangeBinding binding;

        ShiftChangesViewHolder(ItemChangeBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;
        }
        //TODO: set usernames
        public void bind (int change, Shift shift, ShiftType shiftType){
            String changeLabel = "";
            switch (change) {
                case R.string.data_changes_added:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_added);
                    break;
                case R.string.data_changes_removed:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_removed);
                    break;
                case R.string.data_changes_editedNew:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_editedNew);
                    break;
                case R.string.data_changes_editedOld:
                    changeLabel = mContext.getString(R.string.dialog_shiftChanges_editedOld);
                    break;
            }

            //Label
            changeLabel = changeLabel + " " + shift.getUserId();
            binding.textViewChangeLabel.setText(changeLabel);
            //Date
            SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            SimpleDateFormat formatWeekDay = new SimpleDateFormat("EEEE", Locale.getDefault());
            String date = formatDate.format(shift.getDate()) + System.getProperty("line.separator") + formatWeekDay.format(shift.getDate());
            String weekDay = formatWeekDay.format(shift.getDate());
            binding.textViewChangeDate.setText(date);
            binding.textViewChangeWeekDay.setText(weekDay);
            //Shift name
            String shiftName = mContext.getString(R.string.dialog_shiftChanges_shift) + ": " + shiftType.getName();
            binding.textViewChangeName.setText(shiftName);
            //Time interval
            SimpleDateFormat formatDayHour = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startHour = formatDayHour.format(shiftType.getStartTime());
            String endHour = formatDayHour.format(shiftType.getEndTime());
            String timeInterval = startHour + " - " + endHour;
            binding.textViewChangeTime.setText(timeInterval);
            //Tag and color
            binding.textViewChangeTag.setText(shiftType.getTag());
            binding.textViewChangeTag.setBackgroundColor(shiftType.getColor());
        }
    }
}
