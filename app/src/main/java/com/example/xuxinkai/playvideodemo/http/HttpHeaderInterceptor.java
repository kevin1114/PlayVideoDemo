package com.example.xuxinkai.playvideodemo.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jimmy on 16/12/6.
 * 头部拦截器
 */
public class HttpHeaderInterceptor implements Interceptor {
    Map<String, String> mHeaderParamsMap = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request oldRequest = chain.request();
        Request.Builder requestBuilder = oldRequest.newBuilder();
        requestBuilder.method(oldRequest.method(), oldRequest.body());
        //公共参数添加到Header中
        if (mHeaderParamsMap.size() > 0) {
            for (Map.Entry<String, String> entry : mHeaderParamsMap.entrySet()) {
                if(entry.getValue() == null){
                    requestBuilder.header(entry.getKey(), "null");
                }else {
                    requestBuilder.header(entry.getKey(), entry.getValue());
                }
            }
        }
        Request request = requestBuilder.build();
        return chain.proceed(request);
    }

    public static class Builder {
        HttpHeaderInterceptor mHttpHeaderInterceptor;

        public Builder() {
            mHttpHeaderInterceptor = new HttpHeaderInterceptor();
        }

        public Builder addHeaderParams(String key, String value) {
            mHttpHeaderInterceptor.mHeaderParamsMap.put(key, value);
            return this;
        }

        public HttpHeaderInterceptor build() {
            return mHttpHeaderInterceptor;
        }
    }

}
