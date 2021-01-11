package com.study.learn_android;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.security.Policy;

public class CaptureVideo extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private Button captureVideoButton;
    private Button toggleCameraButton;
    private SurfaceView surfaceView;
    private final String TAG = "CaptureVideo";
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private MediaRecorder mediaRecorder;
    private SurfaceHolder.Callback callback;
    private int CameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public CaptureVideo() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);
        initView();
        requestPermission();
    }


    private void requestPermission() {
//        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAPTURE_AUDIO_OUTPUT)) {
//
//        }
    }

    /**
     * 初始化视图
     */
    private void initView() {
        captureVideoButton = (Button) findViewById(R.id.capture_video_btn);
        toggleCameraButton = (Button) findViewById(R.id.toggle_camera_btn);
        surfaceView = (SurfaceView) findViewById(R.id.video_frame);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.capture_video_btn:
                Log.d(TAG, "开始录像");
                break;
            case R.id.toggle_camera_btn:
                Log.d(TAG, "切换摄像头");
                break;
        }
    }
}