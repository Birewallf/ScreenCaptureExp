package bwg.client;

import bwg.netcode.ReceiveThread;
import bwg.netcode.Sender;
import bwg.netcode.UDPSand;
import bwg.netcode.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

/**
 * Client App Controller
 * @author ssvs
 */
public class ClientStartAppController {
    private static Logger logger = Logger.getLogger(ClientStartAppController.class.getName());
    private static InetAddress serverIP = null;
    private static Server server = null;
    private NetListener netListener = null;
    private ReceiveThread receiveThread = null;
    private boolean connectFlag = false;

    @FXML
    public ImageView imageView;
    @FXML
    public Button connectButton;
    @FXML
    public TextField ipServerAddress;
    @FXML
    public void initialize(){
        logger.info("ClientApp init");
        server = new Server(null);

        // listener echo from servers
        if (netListener != null)
            netListener.onDestroy();
        netListener = new ClientStartAppController.NetListener(ClientApp.DEFAULTPORTCLIENT, ipServerAddress);
        netListener.start();
    }
    public void onActionConnect() {
        if (connectFlag) {
            connectButton.setText("Connect");
            ipServerAddress.setEditable(true);

            sendManagementData("stopRTS", server.getIpAddress());
            if (receiveThread != null) receiveThread.onDestroy();
        }
        else {
            if (ipServerAddress.getText().split("\\.").length != 4) {
                ipServerAddress.setText("UnknownHost");
                return;
            }

            connectButton.setText("Disconnect");
            ipServerAddress.setEditable(false);

            receiveThread = new ReceiveThread(imageView);
            receiveThread.start();

            try {
                server.setIpAddress(InetAddress.getByName(ipServerAddress.getText()));
                sendManagementData("getRTS", server.getIpAddress());
            } catch (UnknownHostException e) {
                ipServerAddress.setText("UnknownHost");
            }
        }

        connectFlag = !connectFlag;
    }
    /**
     * search servers
     */
    public void onActionGetServers(){
        // send echo request
        try {
            sendManagementData("getServer", InetAddress.getByName("10.8.5.255"));
        } catch (UnknownHostException e) {
            logger.warning("ClientStartAppController: onActionGetServers UnknownHostException");
        }
    }

    /**
     * Init Sender
     * @param msg Message
     * @param ip ip address
     */
    public void sendManagementData(String msg, InetAddress ip) {
        new Thread(() -> {
            try {
                Sender sender = new UDPSand(ClientApp.DEFAULT_MANAGEMENT_PORT, ip);
                sender.send(new String(msg.toCharArray()).getBytes("UTF-8"));
                logger.info("ClientStartAppController sendManagementData " + msg);
            } catch (SocketException e) {
                logger.warning("ClientStartAppController: sendManagementData SocketException");
            } catch (UnsupportedEncodingException e) {
                logger.warning("ClientStartAppController: sendManagementData UnsupportedEncodingException");
            }

        }).start();
    }

    /**
     * listener echo request
     */
    private class NetListener extends Thread implements Runnable {
        private boolean LOOP = true;
        private int DEFAULTPORT = 0;
        private DatagramSocket socket = null;
        private TextField ipServerAddress;

        private NetListener(int port, TextField ipServerAddress){
            logger.info("NetListener init");
            this.DEFAULTPORT = port;
            this.ipServerAddress = ipServerAddress;
        }

        @Override
        public void run() {
            try {
                socket = new DatagramSocket(DEFAULTPORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (LOOP) {
                try {
                    socket.receive(packet);
                    String text = new String( packet.getData(), "UTF-8");
                    logger.info("NetListener: receive msg: " + text);
                    if (text.contains("echoims")) {
                        ClientStartAppController.serverIP = packet.getAddress();
                        ClientStartAppController.server.setIpAddress(packet.getAddress());
                        String ip = packet.getAddress().toString();
                        this.ipServerAddress.setText(ip.substring(1, ip.length()));
                        onDestroy();
                    }
                } catch (IOException e) {
                    logger.info("NetListener: IOException");
                }
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
            }
            if(socket != null) socket.close();
        }

        private void onDestroy(){
            LOOP = false;
            if (socket != null) socket.close();
            logger.info("(thread)NetListener destroy t=" + this.getName());
        }
    }
}
