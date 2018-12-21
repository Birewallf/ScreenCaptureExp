package bwg.netcode;

import bwg.server.ServerApp;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

/**
 * Client Thread
 * @author ssvs
 */
public class Client extends Thread implements Runnable {
    private Logger logger = Logger.getLogger(Client.class.getName());
    private boolean LOOP = true;
    private InetAddress ipAddress;
    private DatagramSocket socket = new DatagramSocket();

    public Client(InetAddress ipAddress) throws SocketException {
        this.ipAddress = ipAddress;
        logger.info("(thread)Client: init; ip: " +getIpAddress() + " t:" + this.getName());
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    @Override
    public void run() {
        logger.info("(thread)Client: send view " + this.getName());
        while (LOOP){
            try {
                ViewObject viewData = bwg.server.ServerStartAppController.getViewObject();
                Sender sender = new SendUDPImg(ServerApp.DEFAULTPORTMESSAGE, ipAddress);
                sender.send(viewData.getBufImg());
                //sender sleep time
                Thread.sleep(200);
            } catch (IOException | InterruptedException e) {
                //e.printStackTrace();
                logger.warning("Client: IOException");
            }
            // break loop circle
            if(!LOOP)
                break;
        }
    }

    // destroy thread
    public void onDestroy(){
        LOOP = false;
        if (socket != null) socket.close();
        logger.info("(thread)Client onDestroy c=" + getIpAddress() + " t=" + this.getName());
    }
}
