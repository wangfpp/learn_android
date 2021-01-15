package com.study.learn_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;


// https://www.jianshu.com/p/7dac7e5dffb2
public class NetworkAcitivity extends AppCompatActivity {

    private TextView TVipInfo;
    private TextView ServerRes;
    private final String TAG = " NETWORK_TAG";
    private Context mContext;
    private final int RECEIVER_DATA = 10000;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_acitivity);
        mContext = this;

        initView();
        showIP();
        getParams();
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message message) {
                int whatMsg = message.what;
                if(whatMsg == RECEIVER_DATA) {
                    Bundle bundle = message.getData();
                    String result = bundle.getString("data");
                    int size = bundle.getInt("size");
                    int mequestCode = bundle.getInt("requestCode");
                   Log.d(TAG, "result" + String.valueOf(result));
                   if (ServerRes != null) {
                       ServerRes.setText(result);
                   }
                }
            }
        };
    }

    /**
     * 把获取到的ip显示在界面上
     */
    private void showIP() {
        String ip = getLocalIP();
        if(TVipInfo != null) {
            TVipInfo.setText(ip);
        }
        Log.d(TAG, ip);
    }

    /**
     * 初始化视图
     */
    private void initView() {
        TVipInfo = findViewById(R.id.ipinfo);
        ServerRes = findViewById(R.id.server_res);
    }

    /**
     * 获取本机ip
     * @return　String 172.16.1.201
     */
    public String getLocalIP() {
        try {
            ArrayList<NetworkInterface> netList = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface info: netList) {
                ArrayList<InetAddress> idress = Collections.list(info.getInetAddresses());
                for(InetAddress adress: idress) {
                    if(!adress.isLoopbackAddress() &&  adress instanceof Inet4Address) {
                        return adress.getHostAddress();
                    }
                }
            }
        } catch ( SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
    public void send(OutputStream outStream) {
        String sendParams = "{\"ParamCmd\": \"general\", \"uuid\":\"F628840053AF54\"}";
        try {
            byte[] mbuffer = sendParams.getBytes("utf-8");
            Log.d(TAG, 11111 + String.valueOf(mbuffer));
            outStream.write(mbuffer);
            outStream.flush();
        } catch (IOException e) {
            Log.e(TAG, String.valueOf(e));
            e.printStackTrace();
        }
    }
    public void getParams() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket client = new Socket("172.16.1.121", 7800);
                   if(client != null) {
                       OutputStream outStream = client.getOutputStream(); // 发送数据
                       InputStream mInputStream =  client.getInputStream(); // 接收数据
                       byte[] buffer = new byte[1024];
                       String bufferStr = "";
                       Boolean stopSocket = false;
                       send(outStream);
//                       Looper.prepare();
//                       Toast.makeText(mContext, "连接成功", Toast.LENGTH_SHORT).show();
//                       Looper.loop();
                       int size = 0;
                       Log.d(TAG, "连接" + String.valueOf(client.isConnected()));
                       while(client.isConnected() && !stopSocket) {
                           if(mInputStream == null) return;

                           size = mInputStream.read(buffer);
                           if (size > 0) {
                               bufferStr += new String(buffer, 0, size, "utf-8");
                           }
                           Log.d(TAG, "recivie msg" + bufferStr + "size: " + String.valueOf(size));
                           if (bufferStr.endsWith("***")) {
                               Message message = new Message();
                               message.what = RECEIVER_DATA;
                               Bundle bundle = new Bundle();
                               bundle.putString("data", bufferStr);
                               bundle.putInt("size", size);
                               bundle.putInt("code", 0);
                               message.setData(bundle);
                               handler.sendMessage(message);
                           }
                           if (size < 0) {
//                               client.shutdownOutput();
//                               client.shutdownInput();
//                               mInputStream.close();
//                               outStream.close();
                               stopSocket = true;;
                           }
                       }
                   }
                } catch (IOException e) {
                    Looper.prepare();
                    Toast.makeText(mContext, String.valueOf(e), Toast.LENGTH_LONG).show();
                    Log.e(TAG, String.valueOf(e));
                    Looper.loop();
                }
            }
        }).start();
    }
}