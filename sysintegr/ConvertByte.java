package com.example.sysintegr;

import java.io.ByteArrayInputStream;
//import java.awt.image.BufferedImage;
//import javax.imageio.ImageIO;

public class ConvertByte {
    public static long toLong(byte[] bd){
        return (bd[0] & 0xff) | (bd[1] << 8 & 0xff00) | (bd[2] << 16 & 0xff0000)  | (bd[3] << 24 & 0xff000000) | (bd[4] << 32 & 0xff00000000L) | (bd[5] << 40 & 0xff0000000000L) | (bd[6] << 48 & 0xff000000000000L) | (bd[7] << 56 & 0xff00000000000000L);
    }
    public static short toShort(byte[] bd){
        return (short) (bd[0] & 0xff | (bd[1] << 8));
    }
    public static int toInt(byte[] bd){
        return (bd[0] & 0xff) | (bd[1] << 8 & 0xff00) | (bd[2] << 16 & 0xff0000)  | (bd[3] << 24 & 0xff000000);
        //return (bd[0] & 0xff) | (bd[1] << 8 & 0xff00);
    }
    public static void toImg(byte[] bd){
        ByteArrayInputStream bis = new ByteArrayInputStream(bd);
        //BufferedImage bImage2 = ImageIO.read(bis);
        //ImageIcon imageIcon = new ImageIcon();
    }
}
