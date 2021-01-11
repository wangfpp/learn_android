package com.study.learn_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.security.Policy;

public class CaptureVideo extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, Camera.FaceDetectionListener {

    private static Camera camera;
    private static int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Context mContext;
    private Button captureVideoButton;
    private Button toggleCameraButton;
    private SurfaceView surfaceView;
    private final String TAG = "CaptureVideo";
    private SurfaceHolder holder;

    public CaptureVideo() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);
        mContext = this;
        initView();
        boolean hasCamera = exitCamera(mContext);
        if(hasCamera) {
            int cameraNum = Camera.getNumberOfCameras(); // 有几个相机
            Toast toast = Toast.makeText(mContext, "本机有:" + String.valueOf(cameraNum) + "个相机", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Toast.makeText(mContext, "无实体相机", Toast.LENGTH_SHORT).show();
        }
        initSurfaceView();
        requestPermission();
    }
    @Override
    public void onResume() {
        super.onResume();
        initSurfaceView();
    }
    private void initSurfaceView() {
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        camera = getCamera();
    }
    public boolean exitCamera(Context mContext) {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }
    private void requestPermission() {
//        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAPTURE_AUDIO_OUTPUT)) {
//
//        }
    }
    private void setCameraDisplayOrientation() {
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = ( info.orientation - degrees + 360) % 360;

            camera.setDisplayOrientation(result);
        }
    }
    /**
     * 开始预览
     */
    private void startCaptureVideo() {

        initSurfaceView();
    }
    /**
     * 获取camera实例
     */
    private static Camera getCamera() {
        try {
            camera = Camera.open(cameraId);
            return camera;
        } catch (Exception e) {
            camera = null;
            return null;
        }
    }
    private void stopCaptureVideo() {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 初始化视图
     */
    private void initView() {
        captureVideoButton = (Button) findViewById(R.id.capture_video_btn);
        toggleCameraButton = (Button) findViewById(R.id.toggle_camera_btn);
        surfaceView = (SurfaceView) findViewById(R.id.video_frame);
        captureVideoButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.capture_video_btn:
                Intent intent = new Intent(CaptureVideo.this, SerialAcivity.class);
                startActivity(intent);
                break;
            case R.id.toggle_camera_btn:
                Log.d(TAG, "切换摄像头");
                break;
        }
    }
    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = camera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            camera.startFaceDetection();
        }
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {

            camera.setPreviewDisplay(holder);
            camera.startPreview();
            startFaceDetection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if(surfaceView.getHolder() == null) {
            return;
        }
        camera.stopPreview();
        try {

            camera.setPreviewDisplay(holder);
            setCameraDisplayOrientation();
            camera.startPreview();
            startFaceDetection();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopCaptureVideo();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCaptureVideo();
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Log.d(TAG, String.valueOf(faces));
        if(faces.length > 0) {
            Log.d(TAG, "face detected: "+ faces.length +
                    " Face 1 Location X: " + faces[0].rect.centerX() +
                    "Y: " + faces[0].rect.centerY() );
        }
    }
}