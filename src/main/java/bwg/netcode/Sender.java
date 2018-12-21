package bwg.netcode;

import java.net.SocketException;

/**
 * Interface Sender
 * @author ssvs
 */
public interface Sender {
    public void send(Object buffer) throws SocketException;
}
