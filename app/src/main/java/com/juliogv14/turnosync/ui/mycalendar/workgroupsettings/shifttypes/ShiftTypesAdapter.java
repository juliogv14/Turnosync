package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings.shifttypes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.data.UserRoles;
import com.juliogv14.turnosync.databinding.ItemShifttypeBinding;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class ShiftTypesAdapter extends RecyclerView.Adapter<ShiftTypesAdapter.ShiftTypeViewHolder>{

    private Context mContext;
    private List<ShiftType> mShiftTypesList;
    private TypeOnClickListener mTypeOnClickListener;
    private String mRole;

    public ShiftTypesAdapter(Context context, TypeOnClickListener listener, ArrayList<ShiftType> shiftTypesList, String role) {
        this.mContext = context;
        this.mTypeOnClickListener = listener;
        this.mShiftTypesList = shiftTypesList;
        this.mRole = role;

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
            if(!mRole.equals(UserRoles.MANAGER.toString())){
                binding.buttonShiftTypeRemove.setVisibility(View.GONE);
                binding.buttonShiftTypeEdit.setVisibility(View.GONE);
            }
            binding.textViewShiftTypeLong.setText(shiftType.getName());
            binding.textViewShiftTypeShort.setText(shiftType.getTag());
            binding.imageViewTypeColor.setBackgroundColor(shiftType.getColor());

            DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
            String startHour = fmt.print(shiftType.getJodaStartTime());
            String endHour = fmt.print(shiftType.getJodaStartTime().plus(shiftType.getJodaPeriod()));
            String timeInterval = startHour + " - " + endHour;
            binding.textViewShiftTypeInterval.setText(timeInterval);
            binding.executePendingBindings();
        }

    }
}
