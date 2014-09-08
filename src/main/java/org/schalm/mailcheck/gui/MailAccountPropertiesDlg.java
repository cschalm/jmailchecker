package org.schalm.mailcheck.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.mail.NoSuchProviderException;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.schalm.mailcheck.MailAccount;
import org.schalm.mailcheck.MailAccount.Protocol;
import org.schalm.util.helper.log.InMemoryLogger;

/**
 * Dialog for the properties of one mailbox.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailAccountPropertiesDlg.java 157 2014-03-01 22:21:45Z cschalm $
 */
public class MailAccountPropertiesDlg extends OkCancelDlg {
    private static final long serialVersionUID = 6611575252014736469L;
    private JPanel jContentPane = null;
    private JPanel panProtocol = null;
    private JRadioButton rbPop3 = null;
    private JRadioButton rbImap = null;
    private JSpinner spPoll = null;
    private JTextField txtAlias = null;
    private JTextField txtUser = null;
    private JTextField txtHost = null;
    private JPasswordField txtPassword = null;
    private MailAccount mailAccount = null;
    private InMemoryLogger logger;
    private JSpinner spPort = null;
    private JCheckBox ckbSsl = null;
    private PortChangeActionListener portChange = new PortChangeActionListener();
    private JEditorPane editPane = new JEditorPane();

    public MailAccountPropertiesDlg(InMemoryLogger logger, MailAccount ma) {
        super(MailCheckerFrame.getInstance(), true);
        this.logger = logger;
        setSize(400, 300);
        setTitle(messages.getString("title.mailboxProperties"));
        setMainPanel(getMainPanel());
        if (ma != null) {
            mailAccount = ma;
            getTxtAlias().setText(ma.getAccountName());
            getTxtHost().setText(ma.getMailHost());
            getTxtUser().setText(ma.getUserName());
            getTxtPassword().setText(ma.getPassword());
            getSpPoll().setValue(new Integer(ma.getPollPeriode()));
            switch (ma.getStoreType()) {
                case IMAP:
                    getRbImap().setSelected(true);
                    break;
                default:
                    getRbPop3().setSelected(true);
            }
            getSpPort().setValue(new Integer(ma.getPort()));
            getCkbSsl().setSelected(ma.isSsl());
            editPane.setText(ma.getLastConnectionLog());
            editPane.setCaretPosition(0);
        }
    }

    private JComponent getMainPanel() {
        JTabbedPane mainPanel = new JTabbedPane();
        mainPanel.addTab(messages.getString("title.props.connection"), getConnectionPanel());
        mainPanel.addTab(messages.getString("title.props.log"), getLogPanel());

        return mainPanel;
    }

    private JComponent getLogPanel() {
        editPane.setEditable(false);

        return new JScrollPane(editPane);
    }

    private JPanel getConnectionPanel() {
        if (jContentPane == null) {
            GridBagConstraints gridBagConstraintsLblProtocol = new GridBagConstraints();
            gridBagConstraintsLblProtocol.gridx = 0;
            gridBagConstraintsLblProtocol.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblProtocol.insets = new Insets(5, 10, 0, 0);
            gridBagConstraintsLblProtocol.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblProtocol.gridy = 4;
            JLabel lblProtocol = new JLabel();
            lblProtocol.setText(messages.getString("props.protocol"));
            GridBagConstraints gridBagConstraintsLblDisableInfo = new GridBagConstraints();
            gridBagConstraintsLblDisableInfo.gridx = 1;
            gridBagConstraintsLblDisableInfo.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblDisableInfo.insets = new Insets(5, 5, 0, 10);
            gridBagConstraintsLblDisableInfo.gridwidth = 1;
            gridBagConstraintsLblDisableInfo.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblDisableInfo.gridy = 8;
            JLabel lblDisableInfo = new JLabel();
            lblDisableInfo.setText(messages.getString("props.disablePolling"));
            GridBagConstraints gridBagConstraintsSpPoll = new GridBagConstraints();
            gridBagConstraintsSpPoll.insets = new Insets(5, 5, 0, 10);
            gridBagConstraintsSpPoll.gridx = 1;
            gridBagConstraintsSpPoll.gridy = 7;
            gridBagConstraintsSpPoll.ipadx = 0;
            gridBagConstraintsSpPoll.ipady = 0;
            gridBagConstraintsSpPoll.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsSpPoll.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraintsSpPoll.gridwidth = 1;
            GridBagConstraints gridBagConstraintsTxtPassword = new GridBagConstraints();
            gridBagConstraintsTxtPassword.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraintsTxtPassword.gridwidth = 1;
            gridBagConstraintsTxtPassword.gridx = 1;
            gridBagConstraintsTxtPassword.gridy = 3;
            gridBagConstraintsTxtPassword.ipadx = 0;
            gridBagConstraintsTxtPassword.ipady = 0;
            gridBagConstraintsTxtPassword.weightx = 1.0;
            gridBagConstraintsTxtPassword.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsTxtPassword.insets = new Insets(5, 5, 0, 10);
            GridBagConstraints gridBagConstraintsTxtHost = new GridBagConstraints();
            gridBagConstraintsTxtHost.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraintsTxtHost.gridwidth = 1;
            gridBagConstraintsTxtHost.gridx = 1;
            gridBagConstraintsTxtHost.gridy = 2;
            gridBagConstraintsTxtHost.ipadx = 0;
            gridBagConstraintsTxtHost.ipady = 0;
            gridBagConstraintsTxtHost.weightx = 1.0;
            gridBagConstraintsTxtHost.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsTxtHost.insets = new Insets(5, 5, 0, 10);
            GridBagConstraints gridBagConstraintsTxtUser = new GridBagConstraints();
            gridBagConstraintsTxtUser.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraintsTxtUser.gridwidth = 1;
            gridBagConstraintsTxtUser.gridx = 1;
            gridBagConstraintsTxtUser.gridy = 1;
            gridBagConstraintsTxtUser.ipadx = 0;
            gridBagConstraintsTxtUser.ipady = 0;
            gridBagConstraintsTxtUser.weightx = 1.0;
            gridBagConstraintsTxtUser.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsTxtUser.insets = new Insets(5, 5, 0, 10);
            GridBagConstraints gridBagConstraintsTxtAlias = new GridBagConstraints();
            gridBagConstraintsTxtAlias.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraintsTxtAlias.gridwidth = 1;
            gridBagConstraintsTxtAlias.gridx = 1;
            gridBagConstraintsTxtAlias.gridy = 0;
            gridBagConstraintsTxtAlias.ipadx = 0;
            gridBagConstraintsTxtAlias.ipady = 0;
            gridBagConstraintsTxtAlias.weightx = 1.0;
            gridBagConstraintsTxtAlias.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsTxtAlias.insets = new Insets(10, 5, 0, 10);
            GridBagConstraints gridBagConstraintsLblPoll = new GridBagConstraints();
            gridBagConstraintsLblPoll.insets = new Insets(5, 10, 0, 0);
            gridBagConstraintsLblPoll.gridx = 0;
            gridBagConstraintsLblPoll.gridy = 7;
            gridBagConstraintsLblPoll.ipadx = 0;
            gridBagConstraintsLblPoll.ipady = 0;
            gridBagConstraintsLblPoll.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblPoll.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblPoll.gridwidth = 1;
            GridBagConstraints gridBagConstraintsPanProtocol = new GridBagConstraints();
            gridBagConstraintsPanProtocol.insets = new Insets(5, 5, 0, 10);
            gridBagConstraintsPanProtocol.gridx = 1;
            gridBagConstraintsPanProtocol.gridy = 4;
            gridBagConstraintsPanProtocol.ipadx = 0;
            gridBagConstraintsPanProtocol.ipady = 0;
            gridBagConstraintsPanProtocol.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsPanProtocol.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraintsPanProtocol.gridheight = 1;
            gridBagConstraintsPanProtocol.gridwidth = 1;
            GridBagConstraints gridBagConstraintsLblPassword = new GridBagConstraints();
            gridBagConstraintsLblPassword.insets = new Insets(5, 10, 0, 0);
            gridBagConstraintsLblPassword.gridy = 3;
            gridBagConstraintsLblPassword.ipadx = 0;
            gridBagConstraintsLblPassword.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblPassword.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblPassword.gridx = 0;
            GridBagConstraints gridBagConstraintsLblHost = new GridBagConstraints();
            gridBagConstraintsLblHost.insets = new Insets(5, 10, 0, 0);
            gridBagConstraintsLblHost.gridy = 2;
            gridBagConstraintsLblHost.ipadx = 0;
            gridBagConstraintsLblHost.ipady = 0;
            gridBagConstraintsLblHost.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblHost.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblHost.gridx = 0;
            GridBagConstraints gridBagConstraintsLblUser = new GridBagConstraints();
            gridBagConstraintsLblUser.insets = new Insets(5, 10, 0, 0);
            gridBagConstraintsLblUser.gridy = 1;
            gridBagConstraintsLblUser.ipadx = 41;
            gridBagConstraintsLblUser.ipady = 2;
            gridBagConstraintsLblUser.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblUser.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblUser.gridx = 0;
            GridBagConstraints gridBagConstraintsLblAlias = new GridBagConstraints();
            gridBagConstraintsLblAlias.insets = new Insets(10, 10, 0, 0);
            gridBagConstraintsLblAlias.gridy = 0;
            gridBagConstraintsLblAlias.ipadx = 0;
            gridBagConstraintsLblAlias.ipady = 0;
            gridBagConstraintsLblAlias.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblAlias.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblAlias.gridx = 0;
            JLabel lblPoll = new JLabel();
            lblPoll.setText(messages.getString("props.pollPeriod"));
            JLabel lblPassword = new JLabel();
            lblPassword.setText(messages.getString("props.password"));
            JLabel lblHost = new JLabel();
            lblHost.setText(messages.getString("props.host"));
            JLabel lblUser = new JLabel();
            lblUser.setText(messages.getString("props.user"));
            JLabel lblAlias = new JLabel();
            lblAlias.setText(messages.getString("props.alias"));
            JLabel lblPort = new JLabel();
            lblPort.setText(messages.getString("props.port"));
            GridBagConstraints gridBagConstraintsLblPort = new GridBagConstraints();
            gridBagConstraintsLblPort.insets = new Insets(5, 10, 0, 0);
            gridBagConstraintsLblPort.gridx = 0;
            gridBagConstraintsLblPort.gridy = 6;
            gridBagConstraintsLblPort.ipadx = 0;
            gridBagConstraintsLblPort.ipady = 0;
            gridBagConstraintsLblPort.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblPort.fill = GridBagConstraints.BOTH;
            gridBagConstraintsLblPort.gridwidth = 1;
            GridBagConstraints gridBagConstraintsSpPort = new GridBagConstraints();
            gridBagConstraintsSpPort.insets = new Insets(5, 5, 0, 10);
            gridBagConstraintsSpPort.gridx = 1;
            gridBagConstraintsSpPort.gridy = 6;
            gridBagConstraintsSpPort.ipadx = 0;
            gridBagConstraintsSpPort.ipady = 0;
            gridBagConstraintsSpPort.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsSpPort.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraintsSpPort.gridwidth = 1;
            jContentPane = new JPanel(new GridBagLayout());
            jContentPane.add(lblAlias, gridBagConstraintsLblAlias);
            jContentPane.add(getTxtAlias(), gridBagConstraintsTxtAlias);
            jContentPane.add(lblUser, gridBagConstraintsLblUser);
            jContentPane.add(getTxtUser(), gridBagConstraintsTxtUser);
            jContentPane.add(lblHost, gridBagConstraintsLblHost);
            jContentPane.add(getTxtHost(), gridBagConstraintsTxtHost);
            jContentPane.add(lblPassword, gridBagConstraintsLblPassword);
            jContentPane.add(getTxtPassword(), gridBagConstraintsTxtPassword);
            jContentPane.add(lblProtocol, gridBagConstraintsLblProtocol);
            jContentPane.add(getPanProtocol(), gridBagConstraintsPanProtocol);
            jContentPane.add(lblPort, gridBagConstraintsLblPort);
            jContentPane.add(getSpPort(), gridBagConstraintsSpPort);
            jContentPane.add(lblPoll, gridBagConstraintsLblPoll);
            jContentPane.add(getSpPoll(), gridBagConstraintsSpPoll);
            jContentPane.add(lblDisableInfo, gridBagConstraintsLblDisableInfo);
        }
        return jContentPane;
    }

    private JPanel getPanProtocol() {
        if (panProtocol == null) {
            GridBagConstraints gridBagConstraintsRbImap = new GridBagConstraints();
            gridBagConstraintsRbImap.insets = new Insets(5, 5, 5, 0);
            gridBagConstraintsRbImap.gridy = 0;
            gridBagConstraintsRbImap.ipadx = 0;
            gridBagConstraintsRbImap.ipady = 0;
            gridBagConstraintsRbImap.fill = GridBagConstraints.VERTICAL;
            gridBagConstraintsRbImap.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsRbImap.gridx = 3;
            gridBagConstraintsRbImap.weightx = 1.0;
            GridBagConstraints gridBagConstraintsLblImap = new GridBagConstraints();
            gridBagConstraintsLblImap.insets = new Insets(5, 5, 5, 0);
            gridBagConstraintsLblImap.gridy = 0;
            gridBagConstraintsLblImap.ipadx = 0;
            gridBagConstraintsLblImap.ipady = 0;
            gridBagConstraintsLblImap.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsLblImap.fill = GridBagConstraints.VERTICAL;
            gridBagConstraintsLblImap.gridx = 2;
            gridBagConstraintsLblImap.weightx = 0.0;
            GridBagConstraints gridBagConstraintsRbPop3 = new GridBagConstraints();
            gridBagConstraintsRbPop3.insets = new Insets(5, 5, 5, 0);
            gridBagConstraintsRbPop3.gridy = 0;
            gridBagConstraintsRbPop3.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsRbPop3.fill = GridBagConstraints.VERTICAL;
            gridBagConstraintsRbPop3.gridx = 1;
            gridBagConstraintsRbPop3.weightx = 1.0;
            GridBagConstraints gridBagConstraintLblPop3 = new GridBagConstraints();
            gridBagConstraintLblPop3.insets = new Insets(5, 5, 5, 0);
            gridBagConstraintLblPop3.gridy = 0;
            gridBagConstraintLblPop3.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintLblPop3.fill = GridBagConstraints.VERTICAL;
            gridBagConstraintLblPop3.gridx = 0;
            gridBagConstraintLblPop3.weightx = 0.0;
            JLabel lblImap = new JLabel();
            lblImap.setText(messages.getString("props.protocol.imap"));
            JLabel lblPop3 = new JLabel();
            lblPop3.setText(messages.getString("props.protocol.pop3"));
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(getRbPop3());
            buttonGroup.add(getRbImap());
            GridBagConstraints gridBagConstraintsCkbSsl = new GridBagConstraints();
            gridBagConstraintsCkbSsl.insets = new Insets(5, 5, 5, 0);
            gridBagConstraintsCkbSsl.gridy = 0;
            gridBagConstraintsCkbSsl.ipadx = 0;
            gridBagConstraintsCkbSsl.ipady = 0;
            gridBagConstraintsCkbSsl.fill = GridBagConstraints.VERTICAL;
            gridBagConstraintsCkbSsl.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraintsCkbSsl.gridx = 4;
            gridBagConstraintsCkbSsl.weightx = 1.0;

            panProtocol = new JPanel(new GridBagLayout());
            panProtocol.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
            panProtocol.setPreferredSize(new Dimension(119, 20));

            panProtocol.add(lblPop3, gridBagConstraintLblPop3);
            panProtocol.add(getRbPop3(), gridBagConstraintsRbPop3);
            panProtocol.add(lblImap, gridBagConstraintsLblImap);
            panProtocol.add(getRbImap(), gridBagConstraintsRbImap);
            panProtocol.add(getCkbSsl(), gridBagConstraintsCkbSsl);
        }

        return panProtocol;
    }

    private JRadioButton getRbPop3() {
        if (rbPop3 == null) {
            rbPop3 = new JRadioButton();
            rbPop3.setSelected(true);
            rbPop3.addActionListener(portChange);
        }

        return rbPop3;
    }

    private JRadioButton getRbImap() {
        if (rbImap == null) {
            rbImap = new JRadioButton();
            rbImap.addActionListener(portChange);
        }

        return rbImap;
    }

    private JTextField getTxtAlias() {
        if (txtAlias == null) {
            txtAlias = new JTextField();
        }

        return txtAlias;
    }

    private JTextField getTxtUser() {
        if (txtUser == null) {
            txtUser = new JTextField();
        }

        return txtUser;
    }

    private JTextField getTxtHost() {
        if (txtHost == null) {
            txtHost = new JTextField();
        }

        return txtHost;
    }

    private JPasswordField getTxtPassword() {
        if (txtPassword == null) {
            txtPassword = new JPasswordField();
        }

        return txtPassword;
    }

    private JCheckBox getCkbSsl() {
        if (ckbSsl == null) {
            ckbSsl = new JCheckBox(messages.getString("props.protocol.ssl"));
            ckbSsl.addActionListener(portChange);
        }

        return ckbSsl;
    }

    private JSpinner getSpPoll() {
        if (spPoll == null) {
            spPoll = new JSpinner(new SpinnerNumberModel(15, 0, 1440, 1));
        }

        return spPoll;
    }

    private JSpinner getSpPort() {
        if (spPort == null) {
            spPort = new JSpinner(new SpinnerNumberModel(110, 0, 65535, 1));
        }

        return spPort;
    }

    @Override
    protected void save() {
        if (isSaveable()) {
            if (mailAccount == null) {
                // create new one
                mailAccount = new MailAccount();
                mailAccount.addObserver(MailCheckerFrame.getInstance());
            }
            mailAccount.setAccountName(txtAlias.getText());
            mailAccount.setMailHost(txtHost.getText());
            mailAccount.setUserName(txtUser.getText());
            mailAccount.setPassword(new String(txtPassword.getPassword()));
            mailAccount.setPollPeriode(((Integer) spPoll.getValue()).intValue());
            mailAccount.setPort(((Integer) spPort.getValue()).intValue());
            mailAccount.setSsl(ckbSsl.isSelected());
            try {
                if (rbPop3.isSelected()) {
                    mailAccount.setStoreType(Protocol.POP3);
                } else {
                    mailAccount.setStoreType(Protocol.IMAP);
                }
            } catch (NoSuchProviderException e) {
                logger.log(messages.getString("error.settingProtocol") + e.getMessage(), e);
            }
            MailCheckerFrame.getInstance().addChanged(mailAccount);
            closeDialog();
        } else {
            JOptionPane.showMessageDialog(this, messages.getString("error.fillOutAllFields"), messages.getString("error.validationError"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isSaveable() {
        boolean saveable = !txtAlias.getText().isEmpty();
        if (saveable) {
            saveable = !txtHost.getText().isEmpty();
            if (saveable) {
                saveable = !txtUser.getText().isEmpty();
                if (saveable) {
                    saveable = txtPassword.getPassword().length > 0;
                }
            }
        }

        return saveable;
    }

    /**
     * {@link ActionListener} to react on changes of protocol.
     *
     * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
     * @version $Id: MailAccountPropertiesDlg.java 157 2014-03-01 22:21:45Z cschalm $
     */
    public final class PortChangeActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getRbPop3().isSelected()) {
                if (getCkbSsl().isSelected()) {
                    spPort.setValue(new Integer(995));
                } else {
                    spPort.setValue(new Integer(110));
                }
            } else {
                // IMAP
                if (getCkbSsl().isSelected()) {
                    spPort.setValue(new Integer(993));
                } else {
                    spPort.setValue(new Integer(143));
                }
            }
        }

    }

}
