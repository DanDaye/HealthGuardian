package com.kenshin.healthguardian;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kenshin.healthguardian.Model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Kenshin on 2017/5/14.
 */

public class MessageFragment extends Fragment {
    private RecyclerView contactRecyclerView;
    private CardView contactItem;
    private String signal; //从线程标志位
    private List<User> userList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment,container,false);
        contactRecyclerView = (RecyclerView) view.findViewById(R.id.contact_recycler_view);
        contactItem = (CardView) view.findViewById(R.id.contact_card);

        signal = "false";
        queryUser();
        while(true){
            if(signal.equals("true")){
                break;
            }
            else if(signal.equals("error")) {
                Toast.makeText(getContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("afterQueryUser:", "结束搜索用户");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        contactRecyclerView.setLayoutManager(linearLayoutManager);
        UserAdapter adapter = new UserAdapter(userList);
        contactRecyclerView.setAdapter(adapter);
        return view;
    }


    public void queryUser(){
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.localhost) + "/HealthGuardian/GetUserList.do";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("queryUser()", "拉取用户列表失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException{
                String responseData = response.body().string();
                //String responseData = "[{\"username\":\"kenshin\",\"id\":1},{\"username\":\"Danane\",\"id\":2},{\"username\":\"test\",\"id\":3},{\"username\":\"kenshin01\",\"id\":4},{\"username\":\"xiaoming\",\"id\":5}]";
                try{
                    JSONArray jsonArray = new JSONArray(responseData);
                    userList.clear();
                    for(int i = 0;i < jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String username = jsonObject.getString("username");
                        String id = jsonObject.getString("id");
                        if(!username.equals(User.me))
                            userList.add(new User(username,id));
                    }
                    Log.d("before signal:", signal);
                    signal = "true";//从线程结束
                }catch(Exception e){
                    e.printStackTrace();
                    signal = "error";
                }
            }
        });
    }

}
