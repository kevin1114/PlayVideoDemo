package com.example.xuxinkai.playvideodemo.http;

import android.util.Log;

import rx.Subscriber;

/**
 * Created by jimmy on 16/12/8.
 * 网络加载错误的统一处理错误
 */
public abstract class ErrorSubscriber<T> extends Subscriber<T> {
    private Object mTag;

    public ErrorSubscriber setTag(Object tag) {
        mTag = tag;
        return this;
    }

    public Object getTag() {
        return mTag;
    }

    @Override
    public void onError(Throwable e) {
        Log.e("network",e.getMessage());
    }
}
