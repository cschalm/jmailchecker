package org.schalm.mailcheck.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * A splashscreen displaying information about this application.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: SplashScreen.java 157 2014-03-01 22:21:45Z cschalm $
 */
public class SplashScreen extends JWindow implements MouseListener {
    private static final long serialVersionUID = -4646132723692992112L;
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.schalm.mailcheck.gui.messages");
    private Frame owner = null;
    private JPanel contentPane = null;

    public SplashScreen(Frame owner) {
        super(owner);
        this.owner = owner;
        initialize();
    }

    public SplashScreen() {
        super();
        initialize();
    }

    private void initialize() {
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setLayout(new BorderLayout());
        Border bd1 = BorderFactory.createBevelBorder(BevelBorder.RAISED);
        Border bd2 = BorderFactory.createEtchedBorder();
        Border bd3 = BorderFactory.createCompoundBorder(bd1, bd2);
        contentPane.setBorder(bd3);
        setContentPane(contentPane);
        addMouseListener(this);
        ImageIcon icon = new ImageIcon(getClass().getResource("images/me.jpeg"));
        contentPane.add(new JLabel(messages.getString("app.name"), JLabel.CENTER), BorderLayout.NORTH);
        contentPane.add(new JLabel(icon, JLabel.CENTER), BorderLayout.CENTER);
        JLabel linkLabel = new JLabel(messages.getString("app.homepage.url"), JLabel.CENTER);
        linkLabel.setForeground(Color.BLUE);
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(messages.getString("app.homepage.url")));
                } catch (URISyntaxException | IOException ignored) {
                    // ignore
                }
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                setCursor(Cursor.getDefaultCursor());
            }

        });
        contentPane.add(linkLabel, BorderLayout.SOUTH);
        setSize(400, 200);
        setLocationRelativeTo(owner);
    }

    /**
     * Show the SplashScreen for the given number of milliseconds. 0 seconds means forever.
     *
     * @param millis
     */
    public void showFor(int millis) {
        setVisible(true);
        if (millis != 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {
                // ignore
            }
            setVisible(false);
        }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        setVisible(false);
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

}
