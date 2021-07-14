package com.example.sysintegr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.MatOfByte;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
//import org.apache.http.conn.util.InetAddressUtils;

public class MainActivity extends AppCompatActivity {
    public static MulticastSocket ms = null;
    public static DatagramPacket dp;
    public static String udpRcvStr = "";
    public static String robotIP = "192.168.10.124";//"10.96.45.36";
    String 本機地址;
    public static boolean tcpAskConnected = false;

    Button btnForward, btnBackward, btnLeft, btnRight;
    Button showRobotIP;TextView showSelfIP;
    @SuppressLint("StaticFieldLeak")
    public static TextView platformStatus, pfRotate, pfPrism, baseSpd;
    @SuppressLint("StaticFieldLeak")
    public static TextView distFront, distBack, distLeft, distRight;
    @SuppressLint("StaticFieldLeak")
    public static TextView video1txt;
    public static VideoView videoView1;
    @SuppressLint("StaticFieldLeak")
    public static ImageView imgVu1;
    public tcpAsk tcp_ask;
    public static String 口令 = "我是主平板。";
    public static MatOfByte mat;

    public BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("OpenCV","OpenCV loaded successfully");
                    //mat = new MatOfByte(tcpAsk.tmpBuf);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new udpReceive().start();
        btnForward = (Button) findViewById(R.id.btn_front);
        btnBackward = (Button) findViewById(R.id.btn_back);
        btnLeft = (Button) findViewById(R.id.btn_left);
        btnRight = (Button) findViewById(R.id.btn_right);
        platformStatus = (TextView) findViewById(R.id.textView_sbpStatus);
        pfRotate = (TextView) findViewById(R.id.textView_rotAngle);
        pfPrism = (TextView) findViewById(R.id.textView_prism);
        baseSpd = (TextView) findViewById(R.id.textView_bsSpd);
        distFront = (TextView) findViewById(R.id.textView_distFront);
        distBack = (TextView) findViewById(R.id.textView_distBack);
        distLeft = (TextView) findViewById(R.id.textView_distLeft);
        distRight = (TextView) findViewById(R.id.textView_distRight);
        video1txt = (TextView) findViewById(R.id.textView_v1);
        videoView1 = (VideoView) findViewById(R.id.videoView1);
        imgVu1 = (ImageView) findViewById(R.id.imgVu1);
        showRobotIP = (Button) findViewById(R.id.btn_robotIP);showSelfIP = (TextView) findViewById(R.id.textView_selfIP);
        //platformStatus.setBackgroundColor(0xFFF43E06);platformStatus.setText("未發送。");
        new udpBroadCast("開始了！").start();
        //platformStatus.setBackgroundColor(0xFF11CC44);platformStatus.setText("發送了。");
        //btnForward.setOnClickListener(listener);btnBackward.setOnClickListener(listener);btnLeft.setOnClickListener(listener);btnRight.setOnClickListener(listener);
        btnForward.setOnTouchListener(touchListner);btnBackward.setOnTouchListener(touchListner);btnLeft.setOnTouchListener(touchListner);btnRight.setOnTouchListener(touchListner);
        showRobotIP.setText("Robot/"+robotIP);
        try{本機地址 = getLocalHostLANAddress().toString();showSelfIP.setText("Self"+本機地址);}catch(Exception e){e.printStackTrace();}
        tcp_ask = new tcpAsk(7890);
        tcp_ask.start();
        //new tcpReceive().start();
    }
    /*private View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (v == btnForward){
                new udpBroadCast("進").start();
            } else if (v == btnBackward) {
                new udpBroadCast("退").start();
            } else if (v == btnLeft){
                new udpBroadCast("左").start();
            } else if (v == btnRight){
                new udpBroadCast("右").start();
            }
            if (!tcpAskConnected){
                tcp_ask.start();
            }
        }
    };*/
    private View.OnTouchListener touchListner = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == btnForward){
                new udpBroadCast("進").start();
            } else if (v == btnBackward) {
                new udpBroadCast("退").start();
            } else if (v == btnLeft){
                new udpBroadCast("左").start();
            } else if (v == btnRight){
                new udpBroadCast("右").start();
            }
            if (!tcpAskConnected){
                tcp_ask.start();
            }
            return false;
        }
    };
    /*public final class unicodeBSU {
        private static final String pattern = "[0-9|a-f|A-F]{4,5}";
    }*/
    public static String 解碼(String bsu0x){
        StringBuffer retBuf = new StringBuffer();
        int maxLp = bsu0x.length();
        for(int i = 0; i < maxLp; i++){
            if(bsu0x.charAt(i) == '\\' && (i < maxLp-5) && ((bsu0x.charAt(i+1) == 'u') || (bsu0x.charAt(i+1) == 'U'))){
                    try {
                        retBuf.append((char) Integer.parseInt(bsu0x.substring(i+2, i+6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(bsu0x.charAt(i));
                    }
            } else {
                retBuf.append(bsu0x.charAt(i));
            }
        }
        return retBuf.toString();
    }
    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // 遍歷所有的網絡接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍歷IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback類型地址
                        if (inetAddr.isSiteLocalAddress()) {// 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {// site-local類型的地址未被發現，先記錄候選地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 若未發現 non-loopback地址，只能用最次選的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("JDK InetAddress.getLocalHost() 方法意外傳回空值。");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("未能得到局域网址(Failed to determine LAN address): " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }//https://www.cnblogs.com/starcrm/p/7071227.html、https://blog.csdn.net/qq_34996727/article/details/108591395
    /*public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        catch(SocketException e)
        {
            Log.e("feige", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;
    }*/
    /*@Override
    public void onResume(){
        super.onResume();
        if (!OpenCVLoader.initDebug()){
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallBack);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }*/
}
