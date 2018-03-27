package com.example.xuxinkai.playvideodemo.http;

import android.content.Context;

/**
 * Created by xuxinkai on 2018/3/10.
 */

public class AppEnableLoader extends ObjectLoader<AppEnableService> {

    private static AppEnableLoader INSTANCE;

    protected AppEnableLoader(Context context, Class service) {
        super(context, service);
    }

    public static AppEnableLoader getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppEnableLoader(context, AppEnableService.class);
        }
        return INSTANCE;
    }

    @Override
    protected String baseUrl() {
        return "http://www.boomsecret.com:9999/";
    }

    public void getAppEnable(ErrorSubscriber subscriber) {
        loaderComposeCommon(t.getAppEnable(), subscriber);
    }
}
