package com.example.sysintegr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
//import org.apache.commons.lang.StringEscapeUtils;

public class udpReceive extends Thread {
    @Override
    public void run() {
        byte[] data = new byte[1024];
        try {
            InetAddress groupAddress = InetAddress.getByName("224.0.0.1");
            MainActivity.ms = new MulticastSocket(6677);
            MainActivity.ms.joinGroup(groupAddress);
        } catch (Exception e){
            e.printStackTrace();
        }
        while (true){
            try {
                MainActivity.dp = new DatagramPacket(data, data.length);
                if (MainActivity.ms != null){
                    MainActivity.ms.receive(MainActivity.dp);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            InetAddress 對方地址 = MainActivity.dp.getAddress();
            if (對方地址 != null){
                final String questIP = 對方地址.toString();
                String hostIP = "";
                try {
                    hostIP = MainActivity.getLocalHostLANAddress().toString();
                    //System.out.println("hostIP = "+ hostIP + "　　源頭 questIP = "+ questIP);
                    if( (!hostIP.equals("")) && hostIP.equals(questIP.substring(1))){
                        continue;
                    }
                    MainActivity.udpRcvStr = new String(data, 0, MainActivity.dp.getLength());
                    MainActivity.udpRcvStr = MainActivity.解碼(MainActivity.udpRcvStr);
                    JSONObject jsonObj = JSON.parseObject(MainActivity.udpRcvStr);
                    JSONArray 自平衡臺 = jsonObj.getJSONArray("自平衡臺");
                    JSONArray 距離 = jsonObj.getJSONArray("距離");
                    JSONObject 底盤狀態 = jsonObj.getJSONObject("底盤狀態");
                    Float 車速 = 底盤狀態.getFloat("速度");
                    MainActivity.baseSpd.setText(車速.toString());
                    MainActivity.platformStatus.setText(String.valueOf(自平衡臺.get(0)));
                    MainActivity.pfRotate.setText(String.valueOf(自平衡臺.get(1))+"°");
                    MainActivity.pfPrism.setText(String.valueOf(自平衡臺.get(2)));
                    MainActivity.distFront.setText(距離.get(0).toString());
                    MainActivity.distLeft.setText(String.valueOf(距離.get(1))+"  "+String.valueOf(距離.get(2)));
                    MainActivity.distRight.setText(String.valueOf(距離.get(3))+"  "+String.valueOf(距離.get(4)));
                    MainActivity.distBack.setText(距離.get(5).toString());
                } catch (Exception e){
                    //MainActivity.video1txt.setText(MainActivity.udpRcvStr);// 加此句則出錯.
                    Log.e("","Got error 出錯：",e);
                }
            }
        }
    }
}
/*public class udpReceive extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}*/