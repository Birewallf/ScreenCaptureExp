package bwg.server;

import bwg.netcode.ViewObject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import bwg.netcode.Client;

/**
 * Server App Controller
 * @author ssvs
 */
public class ServerStartAppController {
    private static Logger logger = Logger.getLogger(ServerStartAppController.class.getName());

    // Clients
    private static ArrayList<Client> clients = new ArrayList<>();
    private boolean TRANSLATIONFLAG = false;
    private NetListener netListener = null;
    // screen
    private static ViewObject viewObject = new ViewObject();

    public static synchronized ViewObject getViewObject() {
        return viewObject;
    }

    public static synchronized void setViewObject(BufferedImage bufImg) {
        ServerStartAppController.viewObject.setBufImg(bufImg);
    }
    public static synchronized void deleteClient(String ip){
        for (Client c : clients) {
            if (Objects.equals(c.getIpAddress().toString(), ip)){
                c.onDestroy();
                clients.remove(c);
                break;
            }
        }
    }

    @FXML
    private Label infoLabel;

    @FXML
    public void initialize() throws SocketException {
        logger.info("ClientApp init");
    }

    public void onActionTranslation() throws AWTException {
        TRANSLATIONFLAG = !TRANSLATIONFLAG;
        if (TRANSLATIONFLAG) {
            infoLabel.setText("translation on; server port: " + ServerApp.DEFAULTPORTMESSAGE);

            // listener new client
            netListener = new NetListener(ServerApp.DEFAULTPORTSERVER);
            netListener.start();
            // thread capture screen
            new Thread(new Runnable() {
                private Logger logger = Logger.getLogger(this.getClass().getName());
                private Robot robot = new Robot();
                private Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                @Override
                public void run() {
                    logger.info("Capture thread init start");
                    while (TRANSLATIONFLAG) {
                        setViewObject(robot.createScreenCapture(new Rectangle(size)));
                        if (!TRANSLATIONFLAG)
                            break;
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } else {
            for (Client c : clients){
                c.onDestroy();
            }
            clients = new ArrayList<>();
            netListener.onDestroy();
            infoLabel.setText("translation off");
        }
    }

    @Deprecated
    private InetAddress getLocalIP(){
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
            logger.info("Current IP address : " + ip.getHostAddress());
        } catch (UnknownHostException e) {
            logger.warning("getLocalIP error");
        }
        return ip;
    }

    /**
     * Listener echo request from server
     * @author ssvs
     */
    private class NetListener extends Thread implements Runnable {
        private boolean LOOP = true;
        private int DEFAULTPORT = 0;
        private DatagramSocket socket = null;

        private NetListener(int port){
            logger.info("NetListener init");
            this.DEFAULTPORT = port;
        }

        @Override
        public void run() {
            try {
                socket = new DatagramSocket(DEFAULTPORT);
            } catch (SocketException e) {
                logger.info("NetListener SocketException");
            }
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (LOOP) {
                try {
                    socket.receive(packet);

                    String text = new String( packet.getData(), "UTF-8");
                    logger.info("NetListener receive msg: " + text);
                    if (text.contains("getServer")) {
                        String out = "echoims";
                        byte[] buf = new String(out.toCharArray()).getBytes("UTF-8");
                        DatagramPacket pac = new DatagramPacket(buf, buf.length, packet.getAddress(), ServerApp.DEFAULTPORTCLIENT);
                        socket.send(pac);
                        logger.info("NetListener send echo server list");
                    } else if (text.contains("stopRTS")){
                        deleteClient(packet.getAddress().toString());
                    } else if (text.contains("getRTS")) {
                        deleteClient(packet.getAddress().toString());
                        Client client = new Client(packet.getAddress());
                        client.start();
                        clients.add(client);
                        logger.info("NetListener add new client");
                    }
                } catch (IOException e) {
                    logger.info("NetListener IOException");
                }
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
            }
            if(socket != null) socket.close();
        }

        public void onDestroy(){
            LOOP = false;
            if (socket != null) socket.close();
            logger.info("(thread)NetListener onDestroy t=" + this.getName());
        }
    }
}
