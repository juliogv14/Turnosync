package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.databinding.ItemShifttypeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class ShiftTypesAdapter extends RecyclerView.Adapter<ShiftTypesAdapter.ShiftTypeViewHolder>{

    private Context mContext;
    private List<ShiftType> mShiftTypesList;
    private TypeOnClickListener mTypeOnClickListener;

    public ShiftTypesAdapter(Context context, ArrayList<ShiftType> shiftTypesList, TypeOnClickListener listener) {
        this.mShiftTypesList = shiftTypesList;
        this.mContext = context;
        this.mTypeOnClickListener = listener;

    }

    @NonNull
    @Override
    public ShiftTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShifttypeBinding itemBinding = ItemShifttypeBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ShiftTypeViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftTypeViewHolder holder, int position) {
        holder.bind(mShiftTypesList.get(position));
    }

    @Override
    public int getItemCount() {
        return mShiftTypesList.size();
    }

    public interface TypeOnClickListener {
        void onClickEditType(ShiftType type);
        void onClickRemoveType(ShiftType type);
    }

    class ShiftTypeViewHolder extends RecyclerView.ViewHolder {
        ItemShifttypeBinding binding;

        ShiftTypeViewHolder(ItemShifttypeBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;

            this.binding.buttonShiftTypeEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    ShiftType type = mShiftTypesList.get(pos);
                    mTypeOnClickListener.onClickEditType(type);
                }
            });

            this.binding.buttonShiftTypeRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    ShiftType type = mShiftTypesList.get(pos);
                    mTypeOnClickListener.onClickRemoveType(type);
                }
            });
        }

        public void bind(ShiftType shiftType){
            binding.textViewShiftTypeLong.setText(shiftType.getName());
            binding.textViewShiftTypeShort.setText(shiftType.getTag());
            binding.imageViewTypeColor.setBackgroundColor(shiftType.getColor());
            SimpleDateFormat formatDayHour = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startHour = formatDayHour.format(shiftType.getStartTime());
            String endHour = formatDayHour.format(shiftType.getEndTime());
            String timeInterval = startHour + " - " + endHour;
            binding.textViewShiftTypeInterval.setText(timeInterval);
            binding.executePendingBindings();
        }

    }
}