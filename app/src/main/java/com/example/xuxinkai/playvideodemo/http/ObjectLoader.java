package com.example.xuxinkai.playvideodemo.http;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by jimmy on 16/12/7.
 * 父类ObjectLoader
 */
public abstract class ObjectLoader<T> {
    protected T t;
    protected Context mContext;
    //订阅者集合
    private static List<ErrorSubscriber> mList;

    protected abstract String baseUrl();

    protected ObjectLoader(Context context, Class<T> service) {
        RetrofitFactory factory = null;
        factory = new RetrofitFactory(baseUrl(), false);
        if (factory != null)
            t = factory.create(service);
        mList = new ArrayList<>();
        mContext = context;
    }
    /**
     * 设置主线程和io线程
     */
    protected <T> Observable.Transformer<T, T> observe() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 通用observable处理
     */
    protected void loaderComposeCommon(Observable observable, ErrorSubscriber subscriber) {
        //订阅操作
        observable.compose(observe()).subscribe(subscriber);
        //添加
        add(mContext, subscriber);
    }

    public void add(Context tag, ErrorSubscriber subscriber) {
        subscriber.setTag(tag);
        mList.add(subscriber);
    }

    public static void cancelAllByTag(Context tag) {
        if (mList == null || mList.isEmpty()) {
            return;
        }
        Log.d("network","tag:" + tag);
        Log.d("network","mList:" + mList.size());
        Iterator<ErrorSubscriber> it = mList.iterator();
        while (it.hasNext()) {
            ErrorSubscriber subscriber = it.next();
            Object obj = subscriber.getTag();
            Log.d("network","obj:" + obj.getClass().getSimpleName());
            if (subscriber.isUnsubscribed()) {
                Log.d("network","objA:" + obj.getClass().getSimpleName());
                it.remove();
            } else {
                if (obj.getClass().getSimpleName().equals(tag.getClass().getSimpleName())) {
                    Log.d("network","objB:" + obj.getClass().getSimpleName());
                    subscriber.unsubscribe();
                    it.remove();
                }
            }
        }
    }
}
