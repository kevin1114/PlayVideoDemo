package com.example.xuxinkai.playvideodemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.xuxinkai.playvideodemo.http.AppEnableLoader;
import com.example.xuxinkai.playvideodemo.http.BaseResponse;
import com.example.xuxinkai.playvideodemo.http.NormalSubscriber;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.vv_player)
    SelfVideoView vvPlayer;
    @BindView(R.id.iv_video_background)
    ImageView ivBackground;
    @BindView(R.id.rl_video)
    RelativeLayout rlVideo;
    @BindView(R.id.iv_video_click)
    ImageView ivVideoClick;

    private File file;
    private int currentPosition;
    private boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestNetWork();
    }

    private void requestNetWork() {
        AppEnableLoader.getInstance(this).getAppEnable(new NormalSubscriber<BaseResponse>() {
            @Override
            public void onNext(BaseResponse response) {
                if (!"1".equals(response.code)) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("提示").setMessage("APP当前不可用").setCancelable(false).create().show();
                }
            }
        });
    }

    @OnClick({R.id.btn_choosefile, R.id.iv_video_click})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_choosefile:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 100);
                break;
            case R.id.iv_video_click:
                if (file != null) {
                    if (isPlaying) {
                        //开始播放
                        startVideo();
                    } else {
                        //暂停播放
                        isPlaying = true;
                        ivVideoClick.setBackgroundResource(R.drawable.video_click_play_selector);
                        vvPlayer.pause();
                    }
                } else {
                    Toast.makeText(this, "请选择要播放的视频", Toast.LENGTH_LONG).show();
                }
                vvPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        ivBackground.setVisibility(View.VISIBLE);
                        ivVideoClick.setBackgroundResource(R.drawable.video_click_play_selector);
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            Uri uri = data.getData();
            try {
                file = getFileFromPath(this, uri);
                ivBackground.setImageBitmap(createVideoThumbnail(file, ivBackground.getWidth(), ivBackground.getHeight()));
                ivBackground.setVisibility(View.VISIBLE);
                ivVideoClick.setVisibility(View.VISIBLE);
                vvPlayer.setVideoPath(file.getAbsolutePath());
                startVideo();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 开始播放视频
     */
    private void startVideo() {
        isPlaying = false;
        ivVideoClick.setBackgroundResource(R.drawable.video_click_pause_selector);
        ivBackground.setVisibility(View.GONE);
        vvPlayer.setVisibility(View.VISIBLE);
        vvPlayer.start();
    }

    /**
     * 获取视频路径
     *
     * @param context
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    public File getFileFromPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
//                String[] projection = {"_data"};
//                try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
//                    int column_index = cursor.getColumnIndexOrThrow("_data");
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String string = cursor.getString(columnIndex);
                file = new File(string);
            }
//                } catch (Exception e) {
//                    // Eat it  Or Log it.
//                }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            file = new File(uri.getPath());
        }
        return file;
    }

    /**
     * 获取视频的缩略图
     *
     * @param file
     * @param width
     * @param height
     * @return
     */
    private Bitmap createVideoThumbnail(File file, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(file.getAbsolutePath(), new HashMap<String, String>());
            } else {
                retriever.setDataSource(file.getAbsolutePath());
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        vvPlayer.seekTo(currentPosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentPosition = vvPlayer.getCurrentPosition();
        vvPlayer.suspend();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vvPlayer.suspend();
    }
}
