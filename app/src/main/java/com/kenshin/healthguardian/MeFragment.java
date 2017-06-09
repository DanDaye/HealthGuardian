package com.kenshin.healthguardian;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kenshin.healthguardian.Model.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kenshin on 2017/5/14.
 */

public class MeFragment extends Fragment {
    private TextView usernameText;
    private TextView bindidText;
    private static final String TAG = "MeFragment";
    private EditText editText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.me_fragment,container,false);
        //获取UI控件
        usernameText = (TextView) view.findViewById(R.id.username);
        bindidText = (TextView) view.findViewById(R.id.device);
        usernameText.setText(User.me);
        bindidText.setText(User.bindID);
        final Button bindDeviceBtn = (Button) view.findViewById(R.id.bind_device);
        Button switchAcountBtn = (Button) view.findViewById(R.id.switch_acount);
        Button exitBtn = (Button) view.findViewById(R.id.exit_app);
        //绑定ID按钮事件
        bindDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText = new EditText(getActivity());
                Log.d(TAG, "onClick: " + ContextUtil.getInstance() + getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle("请输入终端ID")
                        .setIcon(R.drawable.device)
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String bindid = editText.getText().toString();
                                if (TextUtils.isEmpty(bindid)){
                                    Toast.makeText(getActivity(), "请输入终端ID", Toast.LENGTH_SHORT).show();
                                } else if (!isID(bindid)) {
                                    Toast.makeText(getActivity(), "终端ID为3位数字", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d(TAG, "onClick: " + bindid);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final boolean res = bindDevice(bindid);
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (res) {
                                                        Toast.makeText(getActivity(), "绑定成功", Toast.LENGTH_SHORT).show();
                                                        User.bindID = bindid;
                                                        bindidText.setText(bindid);
                                                    } else {
                                                        Toast.makeText(getActivity(), "绑定失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }).start();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
            }
        });
        //切换账号按钮事件
        switchAcountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText = new EditText(getActivity());
                Log.d(TAG, "onClick: " + ContextUtil.getInstance() + getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle("是否切换账号？")
                        .setIcon(R.drawable.heart_launch)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                User.me = null;
                                User.bindID = null;
                                ActivityController.finishAll();
                                Intent intent = new Intent(getActivity(),LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
            }
        });
        //退出应用按钮事件
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText = new EditText(getActivity());
                Log.d(TAG, "onClick: " + ContextUtil.getInstance() + getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle("是否退出应用？")
                        .setIcon(R.drawable.heart_launch)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityController.finishAll();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
            }
        });
        return view;
    }

    /**
     * 匹配id是否为3位数字
     * @param id
     * @return
     */
    public boolean isID(String id) {
        String regEx = "^\\d{3}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(id);
        return matcher.matches();
    }


    /**
     *发送http更改绑定终端id
     * @return boolean
     */
    private boolean bindDevice(String bindid){
        boolean signal=false;
        try{
            Log.d("bindDevice", "try to bind");
            URL url = new URL(getResources().getString(R.string.localhost) + "/HealthGuardian/BindID.do");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //传递数据
            String data = "username=" + URLEncoder.encode(User.me,"UTF-8")
                    + "&bindid=" + URLEncoder.encode(bindid,"UTF-8");
            urlConnection.setRequestProperty("Connection","keep-alive");
            urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length",String.valueOf(data.getBytes().length));
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            //获取输出流
            OutputStream os =urlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            //接收报文
            if(urlConnection.getResponseCode()==200){
                InputStream is = urlConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len = 0;
                byte buffer[] = new byte[1024];
                while((len=is.read(buffer)) != -1){
                    baos.write(buffer,0,len);
                }
                is.close();
                baos.close();
                final String res = new String(baos.toByteArray());
                if(res.equals("true"))
                    signal = true;
                else
                    signal = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
