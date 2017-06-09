package com.kenshin.healthguardian;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kenshin.healthguardian.Model.Msg;
import com.kenshin.healthguardian.Model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HeartBeat extends Service {
    private static final String TAG = "HeartBeat";
    
    public HeartBeat() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 定时接收健康数据
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getDate();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int interval = 5000;
        long triggerAtTime = SystemClock.elapsedRealtime() + interval;
        Intent i = new Intent(this, HeartBeat.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        getDate();
    }


    public void getDate(){
        Log.d("HeartBeat:", "心跳服务开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /*OkHttpClient client = new OkHttpClient();
                    String url = getResources().getString(R.string.localhost) + "/HealthGuardian/HeartBeat.do";
                    RequestBody requestBody = new FormBody.Builder().add("username", User.me).build();
                    Request request = new Request.Builder().url(url).post(requestBody).build();
                    client.newCall(request).execute();*/
                    String localhost = getResources().getString(R.string.server);
                    int port = getResources().getInteger(R.integer.port);
                    Client client = new Client(localhost,port);
                    client.send(User.me);
                    String response;
                    //Log.d(TAG, "run: " + response);
                    while((response = client.receive())!= null && response.length()!=0){
                        Log.d("收到报文", response);
                        resolveContent(response);//解析响应报文
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).start();

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    /*OkHttpClient client = new OkHttpClient();
//                    String url = getResources().getString(R.string.localhost) + "/HealthGuardian/HeartBeat.do";
//                    RequestBody requestBody = new FormBody.Builder().add("username", User.me).build();
//                    Request request = new Request.Builder().url(url).post(requestBody).build();
//                    client.newCall(request).execute();*/
//                    String localhost = getResources().getString(R.string.server);
//                    int port = getResources().getInteger(R.integer.port);
//                    Client client = new Client(localhost,port);
//                    client.send(User.me);
//                    String response;
//                    while((response = client.receive()) != null && response.length()!=0){
//                        Log.d("收到报文", response);
//                        resolveContent(response);//解析像响应报文
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//
//                }
//            }
//        },2000,3000); //过2000毫秒后每隔3秒发送心跳

        //开启套接字监听服务器
        /*int port = getResources().getInteger(R.integer.serverPort);
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            new Thread(){
                @Override
                public void run() {
                    try {
                        while(true) {
                            Socket socket = serverSocket.accept();
                            new Thread(new ReadThread(socket)).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }catch(Exception e){
            e.printStackTrace();
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();//停止
        Log.d("HeartBeat:", "心跳服务停止");
    }
    /*class ReadThread implements Runnable{
        private BufferedReader bufferedReader;
        private Socket socket;
        public ReadThread(Socket socket) throws IOException{
            this.socket = socket;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        @Override
        public void run() {
            String content;
            try {
                while((content = bufferedReader.readLine()) != null){
                    Log.d("收到信息：", content);
                    resolveContent(content); //解析数据
                }
                bufferedReader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
    private void resolveContent(String content){
        //获取广播实例
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(HeartBeat.this);
        //确定是服务器结果反馈报文
        if(content.startsWith("PH") && content.endsWith("PT") && content.length() == 19){
            //发送广播
            Intent intent = new Intent("com.kenshin.healthguardian.HEALTHCENTER_DATA_BROADCAST");
            intent.putExtra("healthData",content);
            manager.sendBroadcast(intent);
            //Log.d("发送广播", "健康信息");
        }else if(content.startsWith("MH") && content.endsWith("MT") && content.length()!=6){
            Intent intent = new Intent("com.kenshin.healthguardian.TALK_MESSAGE");
            intent.putExtra("talkMsg",content.substring(2,content.length()-2));//去除报头和报尾
            try {
                JSONArray jsonArray = new JSONArray(content.substring(2,content.length()-2));
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sender = jsonObject.getString("sender");
                    String receiver = jsonObject.getString("receiver");
                    String message = jsonObject.getString("message");
                    Log.v("消息数据:", sender+" "+message+" "+receiver);
                    Msg c = new Msg(sender,receiver,message);
                    c.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            manager.sendBroadcast(intent);
            //Log.d("发送广播", "聊天消息");
        }else{
            Log.d("resolveContent", "报文错误 "+content);
        }
    }
}



