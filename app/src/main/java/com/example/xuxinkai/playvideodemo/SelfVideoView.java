package com.example.xuxinkai.playvideodemo;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by xuxinkai on 2018/3/9.
 */

public class SelfVideoView extends VideoView {

    //最终的视频资源宽度
    private int mVideoWidth = 480;
    //最终视频资源高度
    private int mVideoHeight = 480;
    //视频资源原始宽度
    private int videoRealW = 1;
    //视频资源原始高度
    private int videoRealH = 1;

    public SelfVideoView(Context context) {
        super(context);
    }

    public SelfVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelfVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SelfVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setVideoPath(String path) {
        super.setVideoPath(path);

        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(path);
        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
        try {
            videoRealH = Integer.parseInt(height);
            videoRealW = Integer.parseInt(width);
        } catch (NumberFormatException e) {
            Log.e("----->" + "VideoView", "setVideoPath:" + e.toString());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        if (height > width) {
            //竖屏
            if (videoRealH > videoRealW) {
                //如果视频资源是竖屏
                //占满屏幕
                mVideoHeight = height;
                mVideoWidth = width;
            } else {
                //如果视频资源是横屏
                //宽度占满，高度保存比例
                mVideoWidth = width;
                float r = videoRealH / (float) videoRealW;
                mVideoHeight = (int) (mVideoWidth * r);
            }
        } else {
            //横屏
            if (videoRealH > videoRealW) {
                //如果视频资源是竖屏
                //宽度占满，高度保存比例
                mVideoHeight = height;
                float r = videoRealW / (float) videoRealH;
                mVideoWidth = (int) (mVideoHeight * r);
            } else {
                //如果视频资源是横屏
                //占满屏幕
                mVideoHeight = height;
                mVideoWidth = width;
            }
        }
        if (videoRealH == videoRealW && videoRealH == 1) {
            //没能获取到视频真实的宽高，自适应就可以了，什么也不用做
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(mVideoWidth, mVideoHeight);
        }
    }
}
