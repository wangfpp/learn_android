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
        initSurfaceView();
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
//                stopRecord();
//                if (CameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    CameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
//                } else {
//                    CameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
//                }
//                initSurfaceView();
//                startRecord();
                break;
        }
    }

    private void initSurfaceView() {
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setKeepScreenOn(true);
        callback = new SurfaceHolder.Callback() {

            //在控件创建的时候，进行相应的初始化工作
            public void surfaceCreated(SurfaceHolder holder) {
                //打开相机，同时进行各种控件的初始化mediaRecord等
                camera = Camera.open(CameraType);
                mediaRecorder = new MediaRecorder();
            }

            //当控件发生变化的时候调用，进行surfaceView和camera进行绑定，可以进行画面的显示
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                doChange(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

        };
        //为SurfaceView设置回调函数
        surfaceView.getHolder().addCallback(callback);
    }

    //当我们的程序开始运行，即使我们没有开始录制视频，我们的surFaceView中也要显示当前摄像头显示的内容
    private void doChange(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //设置surfaceView旋转的角度，系统默认的录制是横向的画面，把这句话注释掉运行你就会发现这行代码的作用
            camera.setDisplayOrientation(getDegree());
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getDegree() {
        //获取当前屏幕旋转的角度
        int rotating = this.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        //根据手机旋转的角度，来设置surfaceView的显示的角度
        switch (rotating) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    private void stopRecord() {
        //当结束录制之后，就将当前的资源都释放
        mediaRecorder.release();
        camera.release();
        mediaRecorder = null;
        //然后再重新初始化所有的必须的控件和对象
        camera = Camera.open();
        mediaRecorder = new MediaRecorder();
        doChange(surfaceView.getHolder());
    }

    private void startRecord() {
        //先释放被占用的camera，在将其设置为mediaRecorder所用的camera
        camera.unlock();
        mediaRecorder.setCamera(camera);

        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        //设置音频的来源  麦克风
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置视频的来源
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //设置视频的输出格式  3gp
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //设置视频中的声音和视频的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        //设置保存的路径
//        mediaRecorder.setOutputFile(path);
        //开始录制
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}