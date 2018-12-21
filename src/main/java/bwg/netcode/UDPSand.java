package bwg.netcode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * Send UDP datagram
 * @author ssvs
 */
public class UDPSand implements Sender {
    private static Logger logger = Logger.getLogger(UDPSand.class.getName());

    private int MESSAGEPORT = 27000;
    private InetAddress destIP = null;

    public UDPSand(int DEFAULTPORT, InetAddress destIP){
        this.MESSAGEPORT = DEFAULTPORT;
        this.destIP = destIP;
    }
    @Override
    public void send(Object obj) throws SocketException {
        byte[] buffer = (byte[]) obj;
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destIP, MESSAGEPORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            logger.warning(this.getClass().getName() +  ": IOException");
        }
        logger.info(this.getClass().getName() +  ": send");
        socket.close();
    }
}
