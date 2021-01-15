package com.study.learn_android;

import androidx.appcompat.app.AppCompatActivity;

import android.net.InetAddresses;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkAcitivity extends AppCompatActivity {

    private TextView TVipInfo;
    private final String TAG = " NETWORK_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_acitivity);

        initView();
        showIP();
    }

    private void showIP() {
        String ip = getLocalIP();
        if(TVipInfo != null) {
            TVipInfo.setText(ip);
        }
        Log.d(TAG, ip);
    }

    private void initView() {
        TVipInfo = findViewById(R.id.ipinfo);
    }

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
}