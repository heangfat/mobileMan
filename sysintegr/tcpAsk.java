package com.example.sysintegr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;

import org.bytedeco.javacpp.BytePointer;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class tcpAsk extends Thread{
    // 南洋理工大學局域网連不通!必須自組局域网! This can't connect within NTU WLAN! Must create own LAN!
    //Socket socket = null;
    /*public static void main(String[] args) throws IOException{
        Socket sock = new Socket(MainActivity.robotIP, 7888);
        try(InputStream inStream = sock.getInputStream()){
            try(OutputStream outStream = sock.getOutputStream()){
                handle(inStream,outStream);
            }
        }
        sock.close();System.out.println("斷開。");
    }*/
    public static long 幀字節數;public static short[] 分辨率 = new short[2];
    public static byte[] tmpBuf;
    int inPort;
    public tcpAsk(int portNo){inPort = portNo;}
    @Override
    public void run(){
        try {
            //MainActivity.video1txt.setText("連 TCP…");
            Socket sock = new Socket(MainActivity.robotIP, inPort);
            MainActivity.tcpAskConnected = true;
            try (InputStream inStream = sock.getInputStream()) {
                try (OutputStream outStream = sock.getOutputStream()) {
                    handle(inStream, outStream);
                }
            }
            sock.close();
            System.out.println("斷開。");
        } catch (IOException e){
            MainActivity.tcpAskConnected = false;
            Log.e("","Socket error 套接出錯：",e);
        }
    }
    private static void handle(InputStream ins, OutputStream outs) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outs, StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins, StandardCharsets.UTF_8));
        byte[] 包長字節 = new byte[8];byte[] 幀高字節 = new byte[2];byte [] 幀寬字節 = new byte[2];
        //byte[] tmpBuf;//Mat image = cv2.imdecode(tmpBuf,1);
        Bitmap decBmp = null;
        //Scanner scanner = new Scanner(System.in);
        MainActivity.video1txt.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.video1txt.setText("TCP 客戶端啓動了。");
            }
        });
        //System.out.println("[server] " + reader.readLine());
        while(true){
            String s = "480,640";
            writer.write(s);writer.newLine();writer.flush();
            ins.read(包長字節,0,8);ins.read(幀高字節,0,2);ins.read(幀寬字節,0,2);
            幀字節數 = ConvertByte.toLong(包長字節);//ByteBuffer.wrap(包長字節).getLong();
            if (幀字節數 <= 0 || 幀字節數 > 100000){continue;}
            分辨率[0] = ConvertByte.toShort(幀高字節);//ByteBuffer.wrap(幀高字節).getShort();
            分辨率[1] = ConvertByte.toShort(幀寬字節);//ByteBuffer.wrap(幀寬字節).getShort();
            tmpBuf = new byte[(int) 幀字節數];
            ins.read(tmpBuf,0, (int) 幀字節數);
            /*MainActivity.video1txt.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.video1txt.setText("到此。");
                }
            });
            int nRead;
            byte[] data = new byte[(int) 幀字節數];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while ((nRead = ins.read(data,0, (int) 幀字節數)) > 0){
                buffer.write(data,0,nRead);
                final int nread = nRead;
                MainActivity.video1txt.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.video1txt.setText(String.valueOf(幀字節數)+"在循環"+String.valueOf(nread));
                    }
                });
            }
            MainActivity.video1txt.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.video1txt.setText("循環終。");
                }
            });
            byte[] bytes = buffer.toByteArray();*/
            //Mat mat = opencv_imgcodecs.imdecode(new Mat(new BytePointer(tmpBuf),"uint8"));

            try {
                decBmp = BitmapFactory.decodeByteArray(tmpBuf,0, tmpBuf.length);
            } catch (Exception e){
                Log.e("","Error while  出錯：",e);
            }
            final Bitmap dispBmp = decBmp;
            //Mat imgMat = Imgcodecs.imdecode(mat,1);
            //Bitmap dispImg; Imgcodecs.imwrite(dispImg,imgMat);
            //org.bytedeco.opencv.opencv_core.Mat mat1 = org.bytedeco.javacpp.opencv_imgcodecs.imdecode();

            /*final byte[] imgBuf = tmpBuf;
            Frame frm = new Java2DFrameConverter().getFrame(tmpBuf);
            org.bytedeco.opencv.opencv_core.Mat jpgMat = new OpenCVFrameConverter.ToMat().convert(frm);*/
            //String resp = reader.readLine();
            MainActivity.video1txt.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.video1txt.setText(new StringBuilder().append(String.valueOf(幀字節數)).append(" : ").append(String.valueOf(分辨率[0])).append("×").append(String.valueOf(分辨率[1])).append("\n")/*.append(imgMat.height()).append("×").append(imgMat.width())*/.toString());
                }
            });
            MainActivity.imgVu1.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.imgVu1.setImageBitmap(dispBmp);
                }
            });
            /*MainActivity.videoView1.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.videoView1.setVideoURI(imgBuf);
                }
            });*/
        }
        //outs.close();
    }
    /*
    @Override
    public void run(){
        try {
            socket = new Socket(MainActivity.robotIP, 7999);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("服務器喏！".getBytes());
            InputStream inputStream = socket.getInputStream();

            MainActivity.platformStatus.setBackgroundColor(0xFF11CC44);
            MainActivity.platformStatus.setText("TCP Req sent.");
        } catch (IOException e) {
            MainActivity.platformStatus.setText("TCP Req error!");
            e.printStackTrace();
        } finally {
            MainActivity.platformStatus.setText("到此。");
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
