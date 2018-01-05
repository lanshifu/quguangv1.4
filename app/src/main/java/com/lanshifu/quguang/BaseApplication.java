package com.lanshifu.quguang;

import android.app.Application;
import android.content.Context;

import com.lanshifu.quguang.log.LogHandler;
import com.lanshifu.quguang.utils.StorageUtil;

/**
 * Created by lanxiaobin on 2018/1/5.
 */

public class BaseApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        LogHandler logHandler = new LogHandler(this);
        logHandler.setName("LogHandler");
        StorageUtil.init(context);
    }
}
