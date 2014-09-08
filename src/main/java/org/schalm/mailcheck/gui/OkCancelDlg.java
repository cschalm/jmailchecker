package org.schalm.mailcheck.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Dialog having Ok and Cancel buttons.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: OkCancelDlg.java 149 2014-02-12 20:52:56Z cschalm $
 */
public abstract class OkCancelDlg extends JDialog {
    protected static final ResourceBundle messages = ResourceBundle.getBundle("org.schalm.mailcheck.gui.messages");
    private final JPanel mainPane = new JPanel(new BorderLayout());
    private JButton btnOk = null;
    private JButton btnCancel = null;

    public OkCancelDlg(Frame owner, boolean modal) {
        super(owner, modal);
        init();
        this.getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }

        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void init() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                closeDialog();
            }

        });
        mainPane.add(getButtonPanel(), BorderLayout.SOUTH);
        setContentPane(mainPane);
        getRootPane().setDefaultButton(getBtnOk());
    }

    protected JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText(messages.getString("btn.ok"));
            btnOk.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    save();
                }

            });
        }
        return btnOk;
    }

    protected JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText(messages.getString("btn.cancel"));
            btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    closeDialog();
                }

            });
        }
        return btnCancel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(getBtnOk());
        buttonPanel.add(getBtnCancel());

        return buttonPanel;
    }

    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    protected abstract void save();

    protected void setMainPanel(Component mainPanel) {
        mainPane.add(mainPanel, BorderLayout.CENTER);
    }

}
