package com.study.learn_android;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.security.Policy;
import java.util.List;

public class CaptureVideo extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback{

    private static Camera camera;
    private static int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Context mContext;
    private Button captureVideoButton;
    private Button toggleCameraButton;
    private SurfaceView surfaceView;
    private final String TAG = "CaptureVideo";
    private SurfaceHolder holder;
    private Canvas canvas;
    private Handler handler;
    private static int FACE_MSG = 101;
    private SurfaceView surfaceDrawFace;
    private SurfaceHolder drawFaceHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);
        mContext = this;
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                hideSystemUI();
            }
        });
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
        requestPermission();
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message message) {
                int what = message.what;
                if(what == FACE_MSG) {
                    Bundle bundle = message.getData();
                    String face =   bundle.getString("face");
//                  Rect rect = (Rect) face;
                    Log.d(TAG, "传递的消息"+String.valueOf(face));

                    if(canvas == null) {
                        canvas = drawFaceHolder.lockCanvas();
                    }
                    Log.d(TAG, "canvas:" + String.valueOf(drawFaceHolder));


                }
            }
        };
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
    @Override
    public void onResume() {
        super.onResume();
        hideSystemUI();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void releaseCamera() {
        if (camera!=null){
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        if (holder != null) {
            holder = null;
        }
    }
    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = camera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            Log.d(TAG, "最大支持的人脸检测:" + String.valueOf(params.getMaxNumDetectedFaces()));
            // camera supports face detection, so can start it:
            camera.startFaceDetection();
        }
    }
    private void previewCamera() {
        releaseCamera();
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
//        Camera.Parameters params = camera.getParameters();
//        List<String> focusModes = params.getSupportedFocusModes();
//        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//            // Autofocus mode is supported
//            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        }
//
//        camera.setParameters(params);
        camera = camera.open(cameraId);
        camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                if (faces.length > 0){
                    Log.d(TAG, "检测到的人脸个数:" + String.valueOf(faces.length));
                    for (int i = 0; i < faces.length; i++) {
                        Camera.Face face_info = faces[i];
//                        Log.d(TAG, "人脸信息:"  +
//                                "得分:" + String.valueOf(face_info.score) +
//                                "id:" + String.valueOf(face_info.id) +
//                                "左眼:" + String.valueOf(face_info.leftEye) +
//                                "右眼:" + String.valueOf(face_info.rightEye) +
//                                "框体:" + String.valueOf(face_info.rect) +
//                                "嘴:" + String.valueOf(face_info.mouth)
//                        );
                        new Thread(new Runnable()
                        {

                            @Override
                            public void run() {
                                Message message = new Message();
                                message.what = FACE_MSG;
                                Rect rect = face_info.rect;
                                Bundle bundle = new Bundle();
                                bundle.putString("face", String.valueOf(rect));
                                message.setData(bundle);
                                handler.sendMessage(message);
                            }
                        }).start();

                    }
                }
            }
        });
        setCameraDisplayOrientation();
        startPreviewSurface();

    }
    public Matrix prepareMatrix(Boolean isBackCamera , int displayOrientation,
                                int viewWidth, int viewHeight)
    {
        Matrix matrix = new Matrix();
        //前置摄像头处理镜像关系
        matrix.setScale(1f, isBackCamera ? -1 : 1);
        matrix.postRotate(displayOrientation);
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
        return matrix;
    }
    public void startPreviewSurface() {
        if(holder!= null && (holder.getSurface() != null)) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                startFaceDetection();
            } catch (IOException e) {
                Log.e(TAG, "相机预览异常:" + String.valueOf(e));
            }

        }
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
     * 初始化视图
     */
    private void initView() {
        captureVideoButton = (Button) findViewById(R.id.capture_video_btn);
        toggleCameraButton = (Button) findViewById(R.id.toggle_camera_btn);
        surfaceDrawFace = (SurfaceView) findViewById(R.id.draw_face);
        surfaceView = (SurfaceView) findViewById(R.id.video_frame);
        captureVideoButton.setOnClickListener(this);
        toggleCameraButton.setOnClickListener(this);
        drawFaceHolder = surfaceDrawFace.getHolder();
        drawFaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "创建");
                canvas = holder.lockCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setLinearText(true);
                canvas.drawText("啊啊啊", 100, 100, paint);
                canvas.drawRect(0 ,0, 200, 200, paint);
                if (canvas != null){
//                        drawFaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
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
                previewCamera();
                break;
        }
    }

    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        canvas.drawCircle(100, 100, 10, paint);
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        releaseCamera();

    }

//    @Override
//    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
//        Log.d(TAG, "识别人脸的监听器:" + String.valueOf(faces));
//        if (faces.length > 0){
//            Log.d("FaceDetection", "face detected: "+ faces.length +
//                    " Face 1 Location X: " + faces[0].rect.centerX() +
//                    "Y: " + faces[0].rect.centerY() );
//        }
//    }


}
