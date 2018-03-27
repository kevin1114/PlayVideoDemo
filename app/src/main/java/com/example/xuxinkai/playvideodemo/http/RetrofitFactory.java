package com.example.xuxinkai.playvideodemo.http;

import android.os.Build;
import android.util.Log;

import com.example.xuxinkai.playvideodemo.BuildConfig;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jimmy on 17/2/8.
 */
public class RetrofitFactory {

    private Retrofit mRetrofit;
    private static final int DEFAULT_CONNECTION_TIME_OUT = 10;//连接超时
    private static final int DEFAULT_WRITE_TIME_OUT = 10;//写超时时间
    private static final int DEFAULT_READ_TIME_OUT = 10; //读超时时间

    /**
     * 实例化Retrofit
     *
     * @param baseUrl
     */
    public RetrofitFactory(String baseUrl, boolean useCertificate) {
        if (!useCertificate) {
            mRetrofit = new Retrofit.Builder()
                    .client(getUnsafeOkHttpClientBuilder().build())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .build();
        }
    }

    /**
     * 设置OkHttpClient Builder
     */
    protected OkHttpClient.Builder okhttpBuilder() {
        //设置属性
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_WRITE_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true); // 错误重连
        builder.addInterceptor(commonHeader());
        if (BuildConfig.DEBUG) {//开启日志
            builder.addInterceptor(new HttpLogInterceptor());
        }
        return builder;
    }

    /**
     * 公共请求头部
     */
    public HttpHeaderInterceptor commonHeader() {
        String str = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        String date = "m.nonobank.com/msapi/" + str.substring(0, str.length() - 3) + (Integer.parseInt(str.substring(str.length() - 2)) / 5);

        HttpHeaderInterceptor.Builder builder = new HttpHeaderInterceptor.Builder();
        builder.addHeaderParams("terminal", "13");// Android终端添加请求头参数："terminal", "13"表示该请求是从Android端发出
        builder.addHeaderParams("appName", "mxd");//应用名称
        builder.addHeaderParams("ComeFrom", "MaiZI");
        builder.addHeaderParams("osVersion", Build.VERSION.RELEASE); // 操作系统版本
        builder.addHeaderParams("osName", "android"); // 操作系统版本
        builder.addHeaderParams("phoneBrand", Build.BRAND); // 手机品牌
        builder.addHeaderParams("phoneType", Build.MODEL); // 手机类型
        return builder.build();
    }

    /**
     * 非安全连接
     */
    public OkHttpClient.Builder getUnsafeOkHttpClientBuilder() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            //
            return okhttpBuilder().sslSocketFactory(sslSocketFactory).hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 证书安全连接
     */
    public OkHttpClient.Builder getSafeOkHttpClientBuilder(InputStream certificateInputStream) {
        try {
            //CertificateFactory用来证书生成
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(certificateInputStream);

            //Create a KeyStore containing our trusted CAs
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            //Create a TrustManager that trusts the CAs in our keyStore
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            //Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return okhttpBuilder().sslSocketFactory(sslSocketFactory);
        } catch (Exception e) {
            Log.e("network",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对应Service
     *
     * @param service
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> service) {
        if (mRetrofit == null)
            throw new NullPointerException("Retrofit is null");
        return mRetrofit.create(service);
    }


}
