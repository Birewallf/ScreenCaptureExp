package bwg.netcode;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.logging.Logger;


/**
 * ViewObject
 * @author ssvs
 */
public class ViewObject implements Serializable {
    private transient Logger logger = Logger.getLogger(this.getClass().getName());
    private transient BufferedImage bufImg = null;

    public void setBufImg(BufferedImage bufImg) {
        this.bufImg = bufImg;
    }
    public BufferedImage getBufImg() {
        return bufImg;
    }
}
