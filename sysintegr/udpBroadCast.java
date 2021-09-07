package com.example.sysintegr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class udpBroadCast extends Thread {
    final String robotIP = MainActivity.robotIP;//10.25.58.72
    final int robotUDPPort = 6566;
    MulticastSocket sender = null;
    DatagramPacket dj = null;
    InetAddress group = null;
    byte[] data = new byte[1024];
    public udpBroadCast(String dataString) {
        data = (MainActivity.口令+dataString).getBytes();
    }
    @Override
    public void run(){
        try {
            sender = new MulticastSocket();
            group = InetAddress.getByName(robotIP);
            dj = new DatagramPacket(data, data.length, group, robotUDPPort);
            sender.send(dj);
        } catch(IOException e){
            e.printStackTrace();
            //sender.close();
        }
    }
}
/*public class udpBroadCast extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}*/
