package com.example.sysintegr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class tcpReceive extends Thread {
    ServerSocket serverSocket;
    Socket socket;
    BufferedReader in;
    String source_address;
    @Override
    public void run(){
        while(true){
            serverSocket = null;
            socket = null;
            in = null;
            try {
                serverSocket = new ServerSocket(7999);
                socket = serverSocket.accept();
                MainActivity.video1txt.setText("TCP receiving!");
                if (socket != null){
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    sb.append(socket.getInetAddress().getHostAddress());
                    String line = null;
                    while((line=in.readLine()) != null){
                        sb.append(line);
                    }
                    source_address = sb.toString().trim();
                    MainActivity.video1txt.setText(source_address.length());
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (socket != null)
                        socket.close();
                    if (serverSocket != null)
                        serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
/*public class tcpReceive extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}*/