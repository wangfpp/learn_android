package com.study.learn_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import top.keepempty.sph.library.SerialPortHelper;
import top.keepempty.sph.library.SphCmdEntity;
import top.keepempty.sph.library.SphResultCallback;

public class SerialAcivity extends AppCompatActivity {

    private TextView textTextViewRv;
    private SerialPortHelper serialPortHelper;
    private final String TAG = "SERIAL_ACIVITY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_acivity2);

        initView();
        initSerialPort();
    }
    @Override
    public void onStop(){
        destorySerial();
        super.onStop();
    }
    @Override
    public void onDestroy() {
        destorySerial();
        super.onDestroy();

    }
    public void destorySerial() {
        textTextViewRv.setText("");
        try {
            serialPortHelper.closeDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 初始化视图
     */
    private void initView() {
        textTextViewRv = findViewById(R.id.recive_txt);
    }
    private void setText(String txt) {
        textTextViewRv.setText(txt);
    }
    /**
     * 每两个分为一组
     * @param hexString
     * @return
     */
    public List<String> parseArrayList(String hexString) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < hexString.length() / 2; i++) {
            int index = i * 2;
            String var = hexString.substring(index, index + 2);
            strings.add(var);
        }
        return strings;
    }
    public String fromCharCode(int i)
    {
        String strValue=""+(char)i;
        return strValue;
    }
    /**
     * 初始化串口
     */
    private void initSerialPort() {
        serialPortHelper = new SerialPortHelper(32);
        boolean isOpen = serialPortHelper.openDevice("/dev/ttyS4", 115200);
        if(!isOpen){
            Toast.makeText(this,"串口打开失败！",Toast.LENGTH_LONG).show();
        } else {
            serialPortHelper.addCommands("C06162C3C0");
        }
        serialPortHelper.setSphResultCallback(new SphResultCallback() {

            @Override
            public void onSendData(SphCmdEntity sendCom) {
//                Log.d(TAG, "发送命令:" + sendCom.commandsHex);
            }
    
            @Override
            public void onReceiveData(SphCmdEntity data) {
                // 先转16进制　再转字符
                Log.d(TAG, "收到命令:" + data.commandsHex);
                // 61613137322E31362E312E3132312D4636323838343030353341463534
                String recStr = "";
                List listRec = parseArrayList(data.commandsHex);
                for(int i = 0; i < listRec.size(); i++) {
                    int hex_int = Integer.parseInt((String)listRec.get(i), 16);
                    String code_ = fromCharCode(hex_int);
                    recStr += code_;
                }
                setText(recStr);
                Log.d(TAG, "接收的串口信息:" + recStr);
                // aa172.16.1.121-F628840053AF54
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "完成");
            }
        });
    }
}