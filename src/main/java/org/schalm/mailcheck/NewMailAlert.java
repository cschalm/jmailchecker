package org.schalm.mailcheck;

import java.awt.Image;
import java.awt.TrayIcon;

/**
 * TrayIcon with two images, that can be shown alternating.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: NewMailAlert.java 147 2014-02-11 12:05:34Z cschalm $
 */
public class NewMailAlert extends Thread {
    private volatile boolean stop;
    private final float interval;
    private final Image one;
    private final Image two;
    private final TrayIcon trayIcon;

    public NewMailAlert(TrayIcon trayIcon, Image one, Image two, float interval) {
        this.trayIcon = trayIcon;
        this.one = one;
        this.two = two;
        this.interval = interval;
    }

    public void cancel() {
        this.stop = true;
    }

    @Override
    public void run() {
        stop = false;
        boolean imageOne = true;
        while (!stop) {
            if (imageOne) {
                trayIcon.setImage(two);
            } else {
                trayIcon.setImage(one);
            }
            imageOne = !imageOne;
            try {
                Thread.sleep(new Float(1000 * interval).intValue());
            } catch (InterruptedException ignored) {
                // nothing
            }
        }
    }

}
