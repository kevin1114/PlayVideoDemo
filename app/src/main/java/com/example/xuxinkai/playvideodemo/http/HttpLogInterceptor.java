package com.example.xuxinkai.playvideodemo.http;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by jimmy on 16/12/8.
 * 日志打印拦截器
 */
public final class HttpLogInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public void log(String msg) {
        Log.i("network", msg);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        String requestMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
        String tokenID = request.header("tokenID");
        String userId = request.header("userId");

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        //request body deal with
        if (hasRequestBody) {
            StringBuilder sb = new StringBuilder();
            sb.append(" (" + requestBody.contentLength() + "-byte body)");
            sb.append("\nheaders: {\"tokenID\":\"" + tokenID + "\",\"userId\":\"" + userId + "\"}");
            sb.append("\n--> Request Body: ");
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            if (isPlaintext(buffer)) {
                sb.append(URLDecoder.decode(buffer.readString(charset), "UTF-8"));
            }
            requestMessage += sb.toString();
        }
        log(requestMessage);

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        boolean hasResponseBody = responseBody != null;
        String responseMessage = "<-- " + response.code() + ' ' + response.message() + ' ' + tookMs + "ms";
        //response body deal with
        if (hasResponseBody) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(" (" + buffer.size() + "-byte body)");
            sb.append("\n<-- Response Body: ");
            if (!isPlaintext(buffer)) {
                return response;
            }
            if (responseBody.contentLength() != 0) {
                sb.append(buffer.clone().readString(charset));
            }
            responseMessage += sb.toString();
        }
        log(responseMessage);
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

}
