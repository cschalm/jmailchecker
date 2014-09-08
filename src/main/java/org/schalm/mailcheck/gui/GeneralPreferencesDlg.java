package org.schalm.mailcheck.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.schalm.mailcheck.ApplicationPreferences;

/**
 * Dialog for the general preferences.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: GeneralPreferencesDlg.java 157 2014-03-01 22:21:45Z cschalm $
 */
public class GeneralPreferencesDlg extends OkCancelDlg {
    private static final long serialVersionUID = 9084681521032508106L;
    private final MailCheckerFrame parent;
    private JPanel jContentPane = null;
    private JCheckBox cbHeaderPreview = null;
    private JCheckBox cbConfirmDelete = null;
    private ApplicationPreferences prefs = null;

    public GeneralPreferencesDlg(MailCheckerFrame parent, ApplicationPreferences prefs) {
        super(parent, true);
        this.prefs = prefs;
        this.parent = parent;
        setSize(250, 150);
        setTitle(messages.getString("title.generalPreferences"));
        setMainPanel(getMainPanel());
        getCbHeaderPreview().setSelected(prefs.isShowHeader());
        getCbConfirmDelete().setSelected(prefs.isConfirmDelete());
    }

    private JPanel getMainPanel() {
        if (jContentPane == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(10, 10, 0, 10);
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.gridy = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.insets = new Insets(10, 10, 0, 0);
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.ipadx = 0;
            gridBagConstraints1.ipady = 0;
            gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints1.fill = GridBagConstraints.BOTH;
            gridBagConstraints1.gridx = 0;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 1;
            gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints4.insets = new Insets(5, 10, 0, 10);
            gridBagConstraints4.fill = GridBagConstraints.NONE;
            gridBagConstraints4.gridy = 1;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.insets = new Insets(5, 10, 0, 0);
            gridBagConstraints5.gridy = 1;
            gridBagConstraints5.ipadx = 0;
            gridBagConstraints5.ipady = 0;
            gridBagConstraints5.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints5.fill = GridBagConstraints.BOTH;
            gridBagConstraints5.gridx = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.insets = new Insets(10, 10, 10, 10);
            gridBagConstraints3.gridy = 8;
            gridBagConstraints3.ipadx = 0;
            gridBagConstraints3.ipady = 0;
            gridBagConstraints3.anchor = GridBagConstraints.NORTHEAST;
            gridBagConstraints3.fill = GridBagConstraints.NONE;
            gridBagConstraints3.gridx = 1;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.insets = new Insets(10, 10, 10, 10);
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 8;
            gridBagConstraints2.ipadx = 0;
            gridBagConstraints2.ipady = 0;
            gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints2.fill = GridBagConstraints.NONE;
            gridBagConstraints2.gridwidth = 1;
            JLabel lblHeaderPreview = new JLabel();
            lblHeaderPreview.setText(messages.getString("prefs.showHeader"));
            JLabel lblConfirmDelete = new JLabel();
            lblConfirmDelete.setText(messages.getString("prefs.confirmDelete"));
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(lblHeaderPreview, gridBagConstraints1);
            jContentPane.add(getCbHeaderPreview(), gridBagConstraints);
            jContentPane.add(lblConfirmDelete, gridBagConstraints5);
            jContentPane.add(getCbConfirmDelete(), gridBagConstraints4);
            jContentPane.add(getBtnOk(), gridBagConstraints2);
            jContentPane.add(getBtnCancel(), gridBagConstraints3);
        }
        return jContentPane;
    }

    @Override
    protected void save() {
        prefs.setShowHeader(cbHeaderPreview.isSelected());
        prefs.setConfirmDelete(cbConfirmDelete.isSelected());
        parent.setPrefs(prefs);
        closeDialog();
    }

    private JCheckBox getCbHeaderPreview() {
        if (cbHeaderPreview == null) {
            cbHeaderPreview = new JCheckBox();
        }
        return cbHeaderPreview;
    }

    private JCheckBox getCbConfirmDelete() {
        if (cbConfirmDelete == null) {
            cbConfirmDelete = new JCheckBox();
        }
        return cbConfirmDelete;
    }

} // @jve:decl-index=0:visual-constraint="398,92"

