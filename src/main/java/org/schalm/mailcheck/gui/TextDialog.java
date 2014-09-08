package org.schalm.mailcheck.gui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * Dialog for displaying text, e.g. contents of an eMail or logfile.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: TextDialog.java 147 2014-02-11 12:05:34Z cschalm $
 */
public class TextDialog extends JDialog {
    private static final long serialVersionUID = -4377569489711332084L;
    private final JEditorPane editPane = new JEditorPane();

    public TextDialog(Frame owner) {
        super(owner, false);
        init(owner);
    }

    private void init(Frame owner) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                setVisible(false);
                dispose();
            }

        });
        setSize(owner.getSize());
        setLocationRelativeTo(owner);
        editPane.setEditable(false);
        // editPane.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        getContentPane().add(new JScrollPane(editPane));
    }

    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        editPane.setText(text);
        editPane.setCaretPosition(0);
    }

}
