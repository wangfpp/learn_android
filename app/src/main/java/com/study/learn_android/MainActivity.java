package com.study.learn_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class  MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private Button progressButton;
    private Button goButton;
    private long clickTime = 0;
    private Context context;
    private TextView textViewTime;
    private final String TAG = "MainActivity";
    private int timeWhat = 10001;
    private Handler handler;
    private Boolean timeing = true;
    private List<String> btnList = Arrays.asList("loading", "toCamera", "serial", "network");
    private List<Integer> idList = Arrays.asList(2000, 2001, 2002, 2003);
    private LinearLayout btn_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();
        // 定时器更新时间
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int whatMsg = msg.what;
                if (whatMsg == timeWhat) {
                    Bundle bundle = msg.getData();
                    long time = bundle.getLong("time");
                    Date date = new Date(time);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                    String curr_time = formatter.format(date);
                    textViewTime.setText(curr_time);
//                    Log.d(TAG, String.valueOf(time));
                }
                super.handleMessage(msg);
            }
        };
        setInterval();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        timeing = true;
    }

    /**
     * 初始化视图
     */
    private void initView() {
        progressBar = findViewById(R.id.progress);
        progressButton = findViewById(R.id.loding_btn);
        goButton = findViewById(R.id.search_go_btn);
        textViewTime = findViewById(R.id.curr_time);
        btn_layout = findViewById(R.id.btn_layout);
        progressButton.setOnClickListener(this);
        goButton.setOnClickListener(this);
        for (int i = 0; i < btnList.size(); i++) {
            String item = btnList.get(i);
            Log.d(TAG, String.valueOf(btnList.get(i)));
            Button btn = new Button(this);
            btn.setText(item);
            int id = idList.get(i);
            btn.setId(id);
            btn.setOnClickListener(this);
            btn.setAllCaps(false);
            btn_layout.addView(btn);
        }
    }

    /**
     * 定时器
     */
    private void setInterval() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(timeing) {
                        try {
                            long date = new Date().getTime();
                            Message timeMsg = new Message();
                            timeMsg.what = timeWhat;
                            Bundle bundle = new Bundle();
                            bundle.putLong("time", date);
                            timeMsg.setData(bundle);
                            handler.sendMessage(timeMsg);
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
    /**
     * 物理返回按键的事件处理
     */
    @Override
    public void onBackPressed() {
        long timeMillseconds = new Date().getTime();
        if (timeMillseconds - clickTime < 300 && clickTime > 0) {
            super.onBackPressed();
        } else {
            Toast toast = Toast.makeText(context, "点击两次退出APP", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            clickTime = timeMillseconds;
        }
    }

    /**
     * 给元素设置点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        int nodeId = v.getId();
        switch (nodeId) {
            case 2000:
                // 设置loading显示和隐藏
                int isVisibility = progressBar.getVisibility();
                if (isVisibility == View.GONE || isVisibility == View.INVISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                break;
            case 2001:
                timeing = false;
                Intent intent = new Intent(this, CaptureVideo.class);
                startActivity(intent);
                break;
            case 2002:
                intent = new Intent(this, SerialAcivity.class);
                startActivity(intent);
                break;
            case 2003:
                intent = new Intent(this, NetworkAcitivity.class);
                startActivity(intent);
                break;

        }
    }
}