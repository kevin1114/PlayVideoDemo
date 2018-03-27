package com.example.xuxinkai.playvideodemo.http;

import android.util.Log;

/**
 * Created by jimmy on 16/12/8.
 */
public abstract class NormalSubscriber<T> extends ErrorSubscriber<T> {
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        Log.e("network","当前网络可能不通,请稍后重试...");
    }
}
