package com.kenshin.healthguardian;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenshin.healthguardian.Model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenshin on 2017/5/14.
 */

public class MessageFragment extends Fragment {
    private RecyclerView contactRecyclerView;
    private CardView contactItem;

    private List<User> userList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment,container,false);
        contactRecyclerView = (RecyclerView) view.findViewById(R.id.contact_recycler_view);
        contactItem = (CardView) view.findViewById(R.id.contact_card);
        queryUser();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        contactRecyclerView.setLayoutManager(linearLayoutManager);
        UserAdapter adapter = new UserAdapter(userList);
        contactRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        queryUser();
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
//        contactRecyclerView.setLayoutManager(linearLayoutManager);
//        UserAdapter adapter = new UserAdapter(userList);
//        contactRecyclerView.setAdapter(adapter);
    }

    public void queryUser(){
        for (int i = 0; i < 50; i++) {
            User user = new User("name:"+i,"ID:"+i);
            userList.add(user);
        }
    }
}
