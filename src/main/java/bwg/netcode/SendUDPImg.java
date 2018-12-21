package bwg.netcode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendUDPImg implements Sender {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private int port = 0;
    private InetAddress ipAddress;
    private DatagramSocket socket = new DatagramSocket();

    public SendUDPImg(int port, InetAddress ipAddress) throws SocketException {
        this.port = port;
        this.ipAddress = ipAddress;
    }
    @Override
    public void send(Object obj) throws SocketException {
        BufferedImage img = (BufferedImage) obj;
        try {
            socket = new DatagramSocket();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean t = ImageIO.write(img, "JPEG", baos);
            byte[] buffer = baos.toByteArray();
            DatagramPacket packet;
            //System.out.println("-------\nbuffer.length - "+buffer.length);
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // send new packet info
            byte[] newmsg = new String("*new".toCharArray()).getBytes("UTF-8");
            packet = new DatagramPacket(newmsg, newmsg.length, ipAddress, port);
            socket.send(packet);
            Thread.sleep(5);
            // split packet
            ArrayList<Byte> buff = new ArrayList<>();
            for(int i = 0; i < buffer.length; i++){
                buff.add(buffer[i]);
                // if packet > 60K
                //if(i % 60000 == 0 && i != 0) {
                if(buff.size() % 60000 == 0) {
                    //System.out.println("buff.size() = " + buff.size());
                    // add data size info in packet
                    int length = buff.size();
                    buff.add(0, (byte) ((length >> 24)));
                    buff.add(0, (byte) ((length >> 16)));
                    buff.add(0, (byte) ((length >> 8)));
                    buff.add(0, (byte) ((length)));

                    packet = new DatagramPacket(toByteArray(buff), buff.size(), ipAddress, port);//////////
                    socket.send(packet);
                    Thread.sleep(5);
                    buff = new ArrayList<>();
                }
            }
            //System.out.println("buff.size() = " + buff.size());
            // if packet < 60K
            // add data size info in packet
            int length = buff.size();
            buff.add(0, (byte) ((length >> 24)));
            buff.add(0, (byte) ((length >> 16)));
            buff.add(0, (byte) ((length >> 8) ));
            buff.add(0, (byte) ((length)));

            packet = new DatagramPacket(toByteArray(buff), buff.size(), ipAddress, port);
            socket.send(packet);
            Thread.sleep(5);
            // send new packet info
            newmsg = new String("*end".toCharArray()).getBytes("UTF-8");
            packet = new DatagramPacket(newmsg, newmsg.length, ipAddress, port);
            socket.send(packet);

            socket.close();
            baos.flush();
        } catch (IOException | InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private byte[] toByteArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytesList.size(); i++) {
            bytes[i] = bytesList.get(i);
        }
        return bytes;
    }
}
