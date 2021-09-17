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
    public static long NByteFrame;public static int[] resolutionHW = new int[2];
    public static byte[] tmpBuf;
    private static int vi;
    int inPort;
    public tcpAsk(int portNo, int vdi){inPort = portNo; vi = vdi;}
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
        byte[] packLengthByte = new byte[8];byte[] frameHeightByte = new byte[2];byte [] frameWidthByte = new byte[2];
        //byte[] tmpBuf;//Mat image = cv2.imdecode(tmpBuf,1);
        Bitmap decBmp = null;
        boolean cmpltFrame = true;
        //Scanner scanner = new Scanner(System.in);
        MainActivity.video1txt.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.video1txt.setText("TCP 客戶端啓動了。");
            }
        });
        //System.out.println("[server] " + reader.readLine());
        while(true){
            String s = "1080,1920";
            writer.write(s);writer.newLine();writer.flush();
            ins.read(packLengthByte,0,8);ins.read(frameHeightByte,0,2);ins.read(frameWidthByte,0,2);
            NByteFrame = ConvertByte.toInt(packLengthByte);//ByteBuffer.wrap(packLengthByte).getLong();
            if (NByteFrame <= 0 || NByteFrame > 90000000){continue;}
            resolutionHW[0] = 1080;//ConvertByte.toInt(frameHeightByte);//ByteBuffer.wrap(frameHeightByte).getShort();
            resolutionHW[1] = 1920;//ConvertByte.toInt(frameWidthByte);//ByteBuffer.wrap(frameWidthByte).getShort();
            tmpBuf = new byte[(int) NByteFrame];
            ins.read(tmpBuf,0, (int) NByteFrame);
            /*MainActivity.video1txt.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.video1txt.setText("到此。");
                }
            });
            int nRead;
            byte[] data = new byte[(int) NByteFrame];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while ((nRead = ins.read(data,0, (int) NByteFrame)) > 0){
                buffer.write(data,0,nRead);
                final int nread = nRead;
                MainActivity.video1txt.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.video1txt.setText(String.valueOf(NByteFrame)+"在循環"+String.valueOf(nread));
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
            try {
                int imgHeight = decBmp.getHeight();
                cmpltFrame = true;
            } catch (Exception e){
                cmpltFrame = false;
            }
            MainActivity.video1txt.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.video1txt.setText(new StringBuilder().append(String.valueOf(tmpBuf.length)).append(" : ").append(String.valueOf(resolutionHW[0])).append("×").append(String.valueOf(resolutionHW[1])).append("\n")/*.append(imgMat.height()).append("×").append(imgMat.width())*/.toString());
                }
            });
            if(NByteFrame < 10000 ){continue;}
            MainActivity.imgVus[vi-1].post(new Runnable() {
                @Override
                public void run() {
                    if(NByteFrame < 150000){MainActivity.imgVus[vi-1].setImageBitmap(dispBmp);}
                }
            });
            try {
                Thread.sleep(100);
            } catch (Exception e){
                Log.e("","Error while  出錯：",e);
            }
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
