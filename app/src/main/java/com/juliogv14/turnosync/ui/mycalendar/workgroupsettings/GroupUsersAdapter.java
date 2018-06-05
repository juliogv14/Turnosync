package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juliogv14.turnosync.databinding.ItemUserBinding;

import java.util.ArrayList;
import java.util.List;

public class GroupUsersAdapter extends RecyclerView.Adapter<GroupUsersAdapter.UserViewHolder>{

    private Context mContext;
    private List<String> mNameList;
    private UserOnClickHandler mUserOnClickHandler;

    public GroupUsersAdapter(Context context, ArrayList<String> nameList) {
        this.mNameList= nameList;
        this.mContext = context;
        if(context instanceof UserOnClickHandler){
            mUserOnClickHandler = (UserOnClickHandler) context;
        } else{
            throw new RuntimeException(context.toString()
                    + " must implement UserOnClickHandler");
        }

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemBinding = ItemUserBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new UserViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(mNameList.get(position));
        //holder.itemView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300));
    }

    @Override
    public int getItemCount() {
        return mNameList.size();
    }

    public interface UserOnClickHandler {
        void onClickUser(String uid);
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemUserBinding binding;

        UserViewHolder(ItemUserBinding viewBinding) {
            super(viewBinding.getRoot());
            viewBinding.getRoot().setOnClickListener(this);
            this.binding = viewBinding;
        }

        public void bind(String name){
            binding.textViewUserListName.setText(name);
            binding.executePendingBindings();
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            String uid = mNameList.get(pos);
            mUserOnClickHandler.onClickUser(uid);
        }
    }
}
