package com.kenshin.healthguardian;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity implements NavigationView.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private RegisterTask mAuthTask = null;
    // UI 组件
    private EditText usernameText;
    private EditText bindIdText;
    private EditText mPasswordView;
    private EditText rePasswordText;
    private View mRegisterForm;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("注册");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(this);
        //表单项
        mRegisterForm = findViewById(R.id.register_form);
        usernameText = (EditText) findViewById(R.id.username);
        bindIdText = (EditText) findViewById(R.id.bind_id);
        mPasswordView = (EditText) findViewById(R.id.reg_pwd);
        rePasswordText = (EditText) findViewById(R.id.re_pwd);
        //注册按钮事件
        Button registerbtn = (Button) findViewById(R.id.register_button);
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
        mProgressView = findViewById(R.id.register_progress);
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        usernameText.setError(null);
        bindIdText.setError(null);
        mPasswordView.setError(null);
        rePasswordText.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameText.getText().toString();
        String bindid = bindIdText.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = rePasswordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(username)) {
            usernameText.setError("用户名不能为空");
            focusView = usernameText;
            cancel = true;
        }
        if (TextUtils.isEmpty(bindid)) {
            bindIdText.setError("绑定终端ID不能为空");
            focusView = bindIdText;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("密码不能为空");
            focusView = mPasswordView;
            cancel = true;
        }
        if (!rePassword.equals(password)) {
            rePasswordText.setError("前后密码不一致");
            focusView = rePasswordText;
            cancel = true;
        }

        // Check for a valid
        if (!isTelLenthLegal(bindid)) {
            bindIdText.setError("终端ID为3位数字");
            focusView = bindIdText;
            cancel = true;
        }
        if (!TextUtils.isEmpty(username)&&!isUsernameValid(username)) {
            usernameText.setError("用户名只能由数字字母下划线组成");
            focusView = usernameText;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password)&&!isPwdValid(password)) {
            mPasswordView.setError("密码只能由数字字母下划线组成");
            focusView = mPasswordView;
            cancel = true;
        }

        if (!isPwdLenthLegal(password)) {
            mPasswordView.setError("密码长度应为 6-16 位");
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            mAuthTask = new RegisterTask(username, bindid, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isTelLenthLegal(String bindid) {
        //TODO: Replace this with your own logic
        return bindid.length() == 3;
    }

    /**
     * 用户名只能包含数字，英文，下划线
     * @param username
     * @return
     */
    private boolean isUsernameValid(String username) {
        String reg = "\\w+([-+.]\\w+)*";
        // 创建 Pattern 对象
        Pattern p = Pattern.compile(reg);
        return p.matcher(username).matches();
    }

    //验证密码长度是否在 6 - 16位
    private boolean isPwdLenthLegal(String pwd) {
        //TODO: Replace this with your own logic
        return (pwd.length() > 5) && (pwd.length() < 17);
    }

    /**
     * 密码只能包含数字，英文，下划线
     * @param password
     * @return
     */
    private boolean isPwdValid(String password) {
        String reg = "\\w+([_]\\w+)*";
        // 创建 Pattern 对象
        Pattern p = Pattern.compile(reg);
        return p.matcher(password).matches();
    }



//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
//            mRegisterForm.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }




    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mBindid;
        private final String mPassword;
        ProgressDialog registerProDialog = new ProgressDialog(RegisterActivity.this,ProgressDialog.STYLE_SPINNER);

        RegisterTask(String username, String bindid, String password) {
            mUsername = username;
            mBindid = bindid;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            registerProDialog.setTitle("你好," + mUsername);
            registerProDialog.setMessage("注册中");
            registerProDialog.show();
            registerProDialog.setCancelable(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean registerResult = false;
            try {
                // Simulate network access.
                registerResult = RegisterByPost(mUsername, mBindid, mPassword);
                Log.d(TAG, "doInBackground:registerResult: " + registerResult);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                registerResult = false;
            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
            return registerResult;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            registerProDialog.dismiss(); //关闭提示框
            if (success) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                usernameText.setError("用户名已被占用！");
                usernameText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
//            showProgress(false);
        }
    }

    /*
    *   发送http请求验证用户名、密码
    *   @return boolean
    */
    private boolean RegisterByPost(String username, String bindid, String pwd){
        boolean signal = false;
        try{
            Log.d("RegisterByPost", "注册中...");
            Log.d(TAG, "RegisterByPost: " + username + "-" + bindid);
            URL url = new URL(getResources().getString(R.string.localhost) + "/HealthGuardian/RegisterServlet");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            //传递数据
            String data = "username=" + URLEncoder.encode(username,"UTF-8")
                    + "&password=" + URLEncoder.encode(pwd,"UTF-8")
                    + "&bindid=" + URLEncoder.encode(bindid,"UTF-8");
            Log.d(TAG, "RegisterByPost: date:" + data);
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
                if(res.equals("true")){
                    signal = true;
                }
                else {
                    signal = false;
                }
            } else {
                Log.d(TAG, "RegisterByPost: 状态码：" + urlConnection.getResponseCode());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "loginByPost: signal:" + signal);
        return signal;
    }

    @Override
    public void onClick(View v) {
        RegisterActivity.this.finish();
    }
}
