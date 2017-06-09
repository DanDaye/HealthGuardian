package com.kenshin.healthguardian;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kenshin on 2017/5/14.
 */

public class HealthCenterFragment extends Fragment {
    private TextView bloodPressure;
    private TextView bloodPressure_state;
    private TextView heartRate;
    private TextView heartRate_state;
    private TextView currentTimew;
    private SharedPreferences pref;

    private static final String TAG = "HealthCenterFragment";
    private static final int UPDATEVIEW = 1;
    private static final int CANCEL = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.health_center_fragment,container,false);
        //获取SharedPreferences对象
        pref = PreferenceManager.getDefaultSharedPreferences(ContextUtil.getInstance());
        Log.d("血压值" , pref.getString("bloodPressure","检测中"));
        currentTimew = (TextView) view.findViewById(R.id.update_time_text);
        bloodPressure = (TextView) view.findViewById(R.id.blood_pressure_tv);
        bloodPressure_state = (TextView) view.findViewById(R.id.blood_state_tv);
        currentTimew.setText(getCurrentTimew());
        bloodPressure.setText(pref.getString("bloodPressure","检测中"));
        String bloodPressureState = pref.getString("bloodPressure_state","");
        if(isAdded()){
            if (bloodPressureState.equals("正常")) {
                bloodPressure_state.setTextColor(getResources().getColor(R.color.themeColor));
                bloodPressure.setTextColor(getResources().getColor(R.color.themeColor));
            } else if (bloodPressureState.equals("低")) {
                bloodPressure_state.setTextColor(getResources().getColor(R.color.low));
                bloodPressure.setTextColor(getResources().getColor(R.color.low));
            } else {
                bloodPressure_state.setTextColor(getResources().getColor(R.color.danger));
                bloodPressure.setTextColor(getResources().getColor(R.color.danger));
            }
        }
        bloodPressure_state.setText("血压：" + bloodPressureState);
        //设置心率
        heartRate = (TextView) view.findViewById(R.id.heart_rate_tv);
        heartRate_state = (TextView) view.findViewById(R.id.heart_state_tv);
        heartRate.setText(pref.getString("heartRate","检测中"));
        String heartState = pref.getString("heartRate_state","");
        if(isAdded()) {
            if (heartState.equals("正常")) {
                heartRate_state.setTextColor(getResources().getColor(R.color.themeColor));
                heartRate.setTextColor(getResources().getColor(R.color.themeColor));
            } else if (heartState.equals("低")) {
                heartRate_state.setTextColor(getResources().getColor(R.color.low));
                heartRate.setTextColor(getResources().getColor(R.color.low));
            } else {
                heartRate_state.setTextColor(getResources().getColor(R.color.danger));
                heartRate.setTextColor(getResources().getColor(R.color.danger));
            }
        }
        heartRate_state.setText("心率：" + heartState );
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Message message = new Message();
                    message.what = UPDATEVIEW;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },2000,5000); //过2000毫秒后每隔5秒刷新显示
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATEVIEW:
                    showDate();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public String getCurrentTimew() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateNowStr = sdf.format(d);
        return dateNowStr;
    }


    public void showDate() {
        //血压
        currentTimew.setText(getCurrentTimew());
        bloodPressure.setText(pref.getString("bloodPressure","检测中"));
        String bloodPressureState = pref.getString("bloodPressure_state","");
        if(isAdded()) {
            if (bloodPressureState.equals("正常")) {
                bloodPressure_state.setTextColor(getResources().getColor(R.color.themeColor));
                bloodPressure.setTextColor(getResources().getColor(R.color.themeColor));
            } else if (bloodPressureState.equals("低")) {
                bloodPressure_state.setTextColor(getResources().getColor(R.color.low));
                bloodPressure.setTextColor(getResources().getColor(R.color.low));
            } else {
                bloodPressure_state.setTextColor(getResources().getColor(R.color.danger));
                bloodPressure.setTextColor(getResources().getColor(R.color.danger));
            }
        }
        bloodPressure_state.setText("血压：" + bloodPressureState);
        //心率
        heartRate.setText(pref.getString("heartRate","检测中"));
        String heartState = pref.getString("heartRate_state","");
        if(isAdded()) {
            if (heartState.equals("正常")) {
                heartRate_state.setTextColor(getResources().getColor(R.color.themeColor));
                heartRate.setTextColor(getResources().getColor(R.color.themeColor));
            } else if (heartState.equals("低")) {
                heartRate_state.setTextColor(getResources().getColor(R.color.low));
                heartRate.setTextColor(getResources().getColor(R.color.low));
            } else {
                heartRate_state.setTextColor(getResources().getColor(R.color.danger));
                heartRate.setTextColor(getResources().getColor(R.color.danger));
            }
        }
        heartRate_state.setText("心率：" + heartState);
    }

}
