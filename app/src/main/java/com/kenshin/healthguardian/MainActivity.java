package com.kenshin.healthguardian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.kenshin.healthguardian.Model.Msg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private LocalReciver localReceiver;
    private MessageReceiver messageReceiver;
    private LocalBroadcastManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.kenshin.healthguardian.HEALTHCENTER_DATA_BROADCAST");
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.kenshin.healthguardian.TALK_MESSAGE");
        localReceiver = new LocalReciver();
        messageReceiver = new MessageReceiver();
        manager = LocalBroadcastManager.getInstance(MainActivity.this);
        //注册广播接收器
        manager.registerReceiver(localReceiver,intentFilter);
        //manager.registerReceiver(messageReceiver,intentFilter2);
        /*模拟发送广播开始*/
        /*String content = "PH011000800009900PT";
        Intent intent = new Intent("com.kenshin.healthguardian.HEALTHCENTER_DATA_BROADCAST");
        intent.putExtra("healthData",content);
        manager.sendBroadcast(intent);
        Log.d("发送广播:", "发送数据");
        String content2 = "[{\"sender\":\"kenshin\",\"receiver\":\"test\",\"message\":\"hello\"},{\"sender\":\"test\",\"receiver\":\"kenshin\",\"message\":\"hello too\"},{\"sender\":\"kenshin\",\"receiver\":\"test\",\"message\":\"hello again!\"}]";
        Intent intent2 = new Intent("com.kenshin.healthguardian.TALK_MESSAGE");
        intent2.putExtra("talkMsg",content2);
        manager.sendBroadcast(intent2);
        Log.d("发送广播:", "发送消息");*/
        /*模拟发送广播结束*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        replaceFragment(new HealthCenterFragment());
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        final TextView textView = (TextView) findViewById(R.id.toolbar_text);
        textView.setText("健康中心");
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               switch (item.getItemId()) {
                   case R.id.data_center:
                       replaceFragment(new HealthCenterFragment());
                       textView.setText("健康中心");
                       break;
                   case R.id.message:
                       replaceFragment(new MessageFragment());
                       textView.setText("消息");
                       break;
                   case R.id.me:
                       replaceFragment(new MeFragment());
                       textView.setText("我");
                       break;
                   default:
                       break;
               }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterReceiver(localReceiver);//活动结束取消注册
        manager.unregisterReceiver(messageReceiver);
        Intent service = new Intent(this,HeartBeat.class);
        stopService(service);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_layout,fragment);
        transaction.commit();
    }
    class LocalReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("收到健康信息广播", "开始处理数据");
            //保存数据
            //获取SharedPreferences对象
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ContextUtil.getInstance()).edit();
            String content = intent.getStringExtra("healthData");
            StringBuffer bloodPressure = new StringBuffer(content.substring(4,7).replaceAll("^(0+)", "")); //高压
            bloodPressure.append("/"+content.substring(7,10).replaceAll("^(0+)", "")); //低压
            editor.putString("bloodPressure",bloodPressure.toString());//血压
            editor.putString("bloodPressure_state",getState(content.substring(10,12)));//血压状态
            editor.putString("heartRate",content.substring(12,15).replaceAll("^(0+)", ""));//心率
            editor.putString("heartRate_state",getState(content.substring(15,17)));//心率状态
            editor.apply(); //提交保存
        }
        private String getState(String s){
            switch (s){
                case "00":
                    return "正常";
                case "01":
                    return "高";
                case "10":
                    return "低";
            }
            return "";
        }
    }

    class MessageReceiver extends BroadcastReceiver{
        private List<Msg> msgList = new ArrayList<Msg>();
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("收到聊天消息广播", "开始解析消息");
            //DataSupport.deleteAll(Msg.class);//删除数据库全部数据
            String responseData = intent.getStringExtra("talkMsg");
            msgList = getMsgList(responseData);//解析JSON数据
            //存入数据库中
            for(Msg c:msgList) {
                List<Msg> tmp = DataSupport.where("sender = ? and message = ? and receiver = ?",
                        c.getSender(), c.getMessage(), c.getReceiver()).find(Msg.class);
                if (tmp.isEmpty())
                    c.save();
            }
        }
        private List<Msg> getMsgList(String responseData){
            List<Msg> list = new ArrayList<Msg>();
            try {
                JSONArray jsonArray = new JSONArray(responseData);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sender = jsonObject.getString("sender");
                    String receiver = jsonObject.getString("receiver");
                    String message = jsonObject.getString("message");
                    Log.d("聊天消息数据", sender+" "+message+" "+receiver);
                    list.add(new Msg(sender,receiver,message));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        }
    }
}
