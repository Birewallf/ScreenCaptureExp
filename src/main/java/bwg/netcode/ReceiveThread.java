package bwg.netcode;

import bwg.client.ClientApp;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receive Thread from Client
 * @author ssvs
 */
public class ReceiveThread extends Thread implements Runnable {
    private Logger logger = Logger.getLogger(ReceiveThread.class.getName());
    private ImageView imageView = null;
    private boolean LOOP = true;
    private DatagramSocket socket;

    public ReceiveThread(ImageView imageView){
        logger.info("ReceiveThread init");
        this.imageView = imageView;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(ClientApp.DEFAULTPORTMESSAGE);
        } catch (SocketException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        ArrayList<Byte> imgBuffer = new ArrayList<>();
        while (LOOP) {
            try {
                byte[] buffer = new byte[60008];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String text = new String( packet.getData(), "UTF-8");
                ////////////////////////////////////////////////////////////////////////////////////////////////////////
                if(text.contains("*new")){
                    imgBuffer = new ArrayList<>();
                    System.out.println("----------");
                } else if(text.contains("*end")) {
                    //System.out.println("imgBuffer.size - " + imgBuffer.size());
                    // get BufferImage
                    BufferedImage srcImage = ImageIO.read(new ByteArrayInputStream(toByteArray(imgBuffer)));
                    if (srcImage != null) {
                        // set image
                        System.out.println("imgBuffer.size() = " + imgBuffer.size());
                        Image image = SwingFXUtils.toFXImage(srcImage, null);
                        Platform.runLater(() -> {
                            imageView.setImage(image);
                            imageView.setCache(true);
                            imageView.setPreserveRatio(true);
                        });
                    }
                    imgBuffer = new ArrayList<>();
                } else {
                    // get info about data size
                    int b0 = Byte.toUnsignedInt(packet.getData()[0]);
                    int b1 = Byte.toUnsignedInt(packet.getData()[1]);
                    int b2 = Byte.toUnsignedInt(packet.getData()[2]);
                    int b3 = Byte.toUnsignedInt(packet.getData()[3]);
                    int rezSize = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
                    System.out.println("rezSize - " + rezSize);
                    // get data from packet
                    for (int n = 4; n < rezSize+4; n++) {
                        imgBuffer.add(packet.getData()[n]);
                    }
                    System.out.println("imgBuffer.size() - " + imgBuffer.size());
                }

                //logger.info("ReceiveThread get image");
            } catch (NullPointerException ignore) {
                //ignore.printStackTrace();
                logger.warning("ReceiveThread: NullPointerException");
            } catch (IOException e) {
                logger.warning("ReceiveThread: IOException");
            }
        }
    }

    /**
     * ArrayList to byte[]
     * @param bytesList
     * @return
     */
    private byte[] toByteArray(ArrayList<Byte> bytesList) {
        byte[] bytes = new byte[bytesList.size()];
        for (int i = 0; i < bytesList.size(); i++) {
            bytes[i] = bytesList.get(i);
        }

        return bytes;
    }
    public void onDestroy(){
        if(socket != null) socket.close();
        LOOP = !LOOP;
        logger.info("(thread)ReceiveThread destroy t=" + this.getName());
    }
}
