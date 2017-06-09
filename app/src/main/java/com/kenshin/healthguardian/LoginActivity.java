package com.kenshin.healthguardian;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
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
/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    public static final int SUCCESS = 1;
    public static final int FAILED = 0;
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    //记住密码
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        //记住密码功能
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        rememberPwd = (CheckBox) findViewById(R.id.remember_pwd);
        boolean isRemember = pref.getBoolean("remember_password",false);
        if (isRemember) {
            //将账号密码设置到文本框
            String username = pref.getString("username", "");
            String pwd = pref.getString("password", "");
            mUsernameView.setText(username);
            mPasswordView.setText(pwd);
            rememberPwd.setChecked(true);
        }
        //登录
        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        //注册
        Button mRegisterButton = (Button) findViewById(R.id.register);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * 处理登录结果的handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final String username = mUsernameView.getText().toString();
            final String password = mPasswordView.getText().toString();
            switch (msg.what) {
                case SUCCESS:
                    //将账号密码放入editor，记住密码用
                    editor = pref.edit();
                    if (rememberPwd.isChecked()) {
                        editor.putBoolean("remember_password", true);
                        editor.putString("username", username);
                        editor.putString("password", password);
                    } else {
                        editor.clear();
                    }
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    Intent service = new Intent(LoginActivity.this,HeartBeat.class);
                    startService(service);
                    startActivity(intent);
                    finish();
                    break;
                case FAILED:
                    mPasswordView.setError("密码错误");
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = mUsernameView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("请输入密码");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError("请输入用户名");
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            final ProgressDialog loginingProDialog = android.app.ProgressDialog
                    .show(LoginActivity.this, "你好，" + username, "登录中..." );
            loginingProDialog.setCancelable(true);
            new Thread(){
                Message message = new Message();
                public void run(){
                    if(loginByPost(username,password)){
                        User.me = username; //身份标签，HeartBeat服务需要
                        Log.d("Login:", "我的名字是"+User.me);
                        message.what = SUCCESS;
                    }else{
                        message.what = FAILED;
                    }
                    loginingProDialog.dismiss(); //关闭提示框
                    handler.sendMessage(message);
                }
            }.start();
        }
    }
    /**
     *发送http请求验证用户名、密码
     * @return boolean
     */
    private boolean loginByPost(String username,String passwd){
        boolean signal=false;
        try{
            Log.d("loginByPost", "try to login");
            URL url = new URL(getResources().getString(R.string.localhost) + "/HealthGuardian/Validate.do");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //传递数据
            String data = "username=" + URLEncoder.encode(username,"UTF-8")
                    + "&password=" + URLEncoder.encode(passwd,"UTF-8");
            urlConnection.setRequestProperty("Connection","close");
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
                String res = new String(baos.toByteArray());
                Log.d(TAG, "loginByPost: res" + res);
                String res1 = res.substring(0,res.indexOf(":"));
                Log.d(TAG, "loginByPost: " + res1);
                if(res1.equals("true")) {
                    signal = true;
                    User.bindID = res.substring(res.indexOf(":") + 1);
                    Log.d(TAG, "loginByPost:bindID " + User.bindID);
                }
                else
                    signal = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return signal;
    }
}

