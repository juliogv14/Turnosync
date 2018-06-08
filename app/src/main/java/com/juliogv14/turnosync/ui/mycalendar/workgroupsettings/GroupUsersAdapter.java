package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.databinding.ItemUserBinding;

import java.util.ArrayList;
import java.util.List;

public class GroupUsersAdapter extends RecyclerView.Adapter<GroupUsersAdapter.UserViewHolder>{

    private Context mContext;
    private List<UserRef> mUserList;
    private UserOnClickListener mUserOnClickListener;

    public GroupUsersAdapter(Context context, ArrayList<UserRef> userList, UserOnClickListener listener) {
        this.mUserList = userList;
        this.mContext = context;
        this.mUserOnClickListener = listener;

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemBinding = ItemUserBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new UserViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(mUserList.get(position).getUid());
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public interface UserOnClickListener {
        void onClickRemoveUser(String uid);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        UserViewHolder(ItemUserBinding viewBinding) {
            super(viewBinding.getRoot());
            this.binding = viewBinding;

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    String uid = mUserList.get(pos).getUid();
                    Toast.makeText(mContext, "User uid: " + uid, Toast.LENGTH_SHORT).show();
                }
            });

            this.binding.buttonSettingsRemoveUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    String uid = mUserList.get(pos).getUid();
                    Toast.makeText(mContext, "Remove user" + uid, Toast.LENGTH_SHORT).show();
                    mUserOnClickListener.onClickRemoveUser(uid);
                }
            });
        }

        public void bind(String name){
            binding.textViewUserListName.setText(name);
            binding.executePendingBindings();
        }

    }
}
