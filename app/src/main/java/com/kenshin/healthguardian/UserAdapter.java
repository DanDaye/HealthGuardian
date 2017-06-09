package com.kenshin.healthguardian;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private Context mContext;

    private List<User> mUserList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView userinfo ;

        public ViewHolder(View view) {
            super(view);
            userinfo = (TextView) view.findViewById(R.id.user_info);
            cardView = (CardView) view.findViewById(R.id.contact_card);
        }
    }

    public UserAdapter(List<User> userList){
        mUserList = userList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        //item点击事件处理
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                User user = mUserList.get(position);
                Intent intent = new Intent(mContext, TalkActivity.class);
                intent.putExtra("talker", user.getUserName());
                mContext.startActivity(intent);
                Log.d("UserAdapter:", "启动TalkActivity");
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mUserList.get(position);
        holder.userinfo.setText("name:" + user.getUserName() + "\n" + "ID:" + user.getUserID());
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }
}
