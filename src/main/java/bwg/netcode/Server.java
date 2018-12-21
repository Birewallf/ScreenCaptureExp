package bwg.netcode;

import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Server info
 * @author ssvs
 */
public class Server {
    private Logger logger = Logger.getLogger(Server.class.getName());
    private InetAddress ipAddress;
    public Server(InetAddress ipAddress){
        logger.info("Server: init");
        this.ipAddress = ipAddress;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        logger.info("Server: set ipAddress " + ipAddress);
        this.ipAddress = ipAddress;
    }
}
