package com.kenshin.healthguardian;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by lenovo on 17/6/2.
 */

public class ContextUtil extends Application {
    private static Context context;

    public static Context getInstance() {
        return context;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
    }
}
