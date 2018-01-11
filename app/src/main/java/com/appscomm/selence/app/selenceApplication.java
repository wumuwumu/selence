package com.appscomm.selence.app;

import android.app.Application;

import com.necer.ncalendar.utils.AppUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.log.LoggerInterceptor;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2017/10/11.
 */

public class selenceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.init(this);
        LoggerInterceptor loggerInterceptor = new LoggerInterceptor("TAG");
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggerInterceptor)
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }
}
