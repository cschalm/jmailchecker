package org.schalm.mailcheck;

import java.io.File;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.schalm.mailcheck.gui.MailCheckerFrame;

/**
 * Main class for starting the application.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: Starter.java 147 2014-02-11 12:05:34Z cschalm $
 */
public final class Starter {
    private static final Logger log = Logger.getLogger(Starter.class);

    private Starter() {
        // utility class
    }

    public static void main(String[] args) {
        if (new File("log4j.properties").canRead()) {
            PropertyConfigurator.configure("log4j.properties");
            if (log.isDebugEnabled()) {
                log.debug("Starting...");
            }
        } else {
            BasicConfigurator.configure();
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            MailCheckerFrame.getInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            log.error(e.getMessage(), e);
        }
    }

}
