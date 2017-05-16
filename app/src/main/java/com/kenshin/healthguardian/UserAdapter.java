package com.kenshin.healthguardian;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kenshin.healthguardian.Model.User;

import java.util.List;

/**
 * Created by Kenshin on 2017/5/16.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> mUserList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView userinfo ;

        public ViewHolder(View view) {
            super(view);
            userinfo = (TextView) view.findViewById(R.id.user_info);
        }
    }

    public UserAdapter(List<User> userList){
        mUserList = userList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mUserList.get(position);
        holder.userinfo.setText(user.getUserName() + "\n" + user.getUserID());
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }
}
