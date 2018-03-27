package com.example.xuxinkai.playvideodemo.http;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by xuxinkai on 2018/3/10.
 */

public interface AppEnableService {

    @GET("test")
    Observable<BaseResponse> getAppEnable();
}
