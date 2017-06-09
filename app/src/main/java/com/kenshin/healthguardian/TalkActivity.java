package com.kenshin.healthguardian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kenshin.healthguardian.Model.Msg;
import com.kenshin.healthguardian.Model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TalkActivity extends BaseActivity implements View.OnClickListener{
    private LocalBroadcastManager manager;
    private MyReceiver receiver;
    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private String talker;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private int items;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyItemInserted(msgList.size() - 1);
            msgRecyclerView.smoothScrollToPosition(msgList.size() - 1);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.kenshin.healthguardian.TALK_MESSAGE");
        manager = LocalBroadcastManager.getInstance(this);
        receiver = new MyReceiver();
        manager.registerReceiver(receiver,intentFilter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        //返回
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TalkActivity.this.finish();
            }
        });
        TextView tooltext = (TextView) findViewById(R.id.toolbar_text);
        Intent intent = getIntent();
        talker = intent.getStringExtra("talker");
        tooltext.setText(talker);
        initMsgs();
        inputText = (EditText)findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        send.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.send:
                String content = inputText.getText().toString();
                if(!content.equals("")){
                    final Msg msg = new Msg(User.me,talker,content);
                    msg.save();
                    Log.d("TalkActivity", "before" + msgList.size());
                    msgList.add(msg);
                    //发送报文给服务器
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String localhost = getResources().getString(R.string.server);
                            int port = getResources().getInteger(R.integer.port);
                            try {
                                Client client = new Client(localhost,port);
                                String data = "MH" + "[{\"sender\":\"" + msg.getSender() + "\",\"message\":\"" + msg.getMessage() + "\",\"receiver\":\"" + msg.getReceiver() + "\"}]"
                                        + "MT";
                                Log.d("TalkActivity", "聊天->服务器");
                                client.send(data);
                                Log.d("aaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.smoothScrollToPosition(msgList.size() - 1);
                    items = msgList.size();
                    inputText.setText("");
                }
                break;
        }
    }
    private void initMsgs(){
        /*模拟初始化聊天记录开始*/
        //DataSupport.deleteAll(Msg.class);//删除数据库全部数据
        /*Msg[] msg = new Msg[5];
        for(int i=0;i<msg.length;i++){
            msg[i] = new Msg();
        }

        msg[0].setMessage("Hello guy.");
        msg[0].setSender("kenshin");
        msg[0].setReceiver(User.me);

        msg[1].setMessage("Hello, Who is that?");
        msg[1].setSender(User.me);
        msg[1].setReceiver("kenshin");

        msg[2].setMessage("This is Tom ,nice talking to you.");
        msg[2].setSender("kenshin");
        msg[2].setReceiver(User.me);

        msg[3].setMessage("hello to Danane");
        msg[3].setSender("kenshin");
        msg[3].setReceiver("Danane");

        msg[4].setMessage("hello from Danane");
        msg[4].setSender("Danane");
        msg[4].setReceiver(User.me);

        Log.d("模拟数据库插入：", "开始");
        //保证不重复插入
        for(Msg c:msg){
            List<Msg> tmp = DataSupport.where("sender = ? and message = ? and receiver = ?",
                    c.getSender(),c.getMessage(),c.getReceiver()).find(Msg.class);
            if(tmp.isEmpty())
                c.save();
            else
                Log.d("插入数据库：", "数据重复");
        }
        Log.d("模拟数据库插入：", "结束");*/
        /*模拟初始化聊天记录结束*/

        //搜索聊天记录
        try{
            msgList = DataSupport.where("(sender = ? and receiver = ?) or (sender = ? and receiver = ?)",
                    User.me,talker,talker,User.me).find(Msg.class);
            Log.d("initMsgs",msgList.size()+"" );
        }catch (Exception e){
            Log.d("initMsgs", "Buggggggggggggggggggggggggggg");
            e.printStackTrace();
        }
    }
    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseData = intent.getStringExtra("talkMsg");
            Log.d("onReceive: ", responseData);
            getMsgList(responseData);//解析JSON数据
            try{
                Message message = new Message();
                handler.sendMessage(message);
            }catch (Exception e){
                e.printStackTrace();
                Log.d("onReceive: ", "ggggggggggggggggggggg");
            }
        }
        private void getMsgList(String responseData){
            try {
                JSONArray jsonArray = new JSONArray(responseData);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sender = jsonObject.getString("sender");
                    String receiver = jsonObject.getString("receiver");
                    String message = jsonObject.getString("message");
                    Log.v("消息数据:", sender+" "+message+" "+receiver);
                    Msg c = new Msg(sender,receiver,message);
                    msgList.add(c);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}