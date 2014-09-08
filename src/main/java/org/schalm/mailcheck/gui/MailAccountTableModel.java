package org.schalm.mailcheck.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.schalm.mailcheck.MailAccount;
import org.schalm.util.helper.swing.SimpleTableModel;

/**
 * TableModel for mail accounts.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailAccountTableModel.java 157 2014-03-01 22:21:45Z cschalm $
 */
public class MailAccountTableModel extends SimpleTableModel<MailAccount> {
    private static final long serialVersionUID = -2072663765837746634L;
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.schalm.mailcheck.gui.messages");

    public MailAccountTableModel() {
        super();
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Alias");
        columnNames.add("User");
        columnNames.add("Host");
        columnNames.add("Mails");
        columnNames.add("State");
        columnNames.add("Size");
        columnNames.add("Elapsed (min)");
        columnNames.add("Poll (min)");
        setColumnNames(columnNames);
    }

    public MailAccount getMailAccount(int row) {
        return getRow(row);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MailAccount mailAccount = getRow(rowIndex);
        switch (columnIndex) {
            case 0:
                return mailAccount.getAccountName();
            case 1:
                return mailAccount.getUserName();
            case 2:
                return mailAccount.getMailHost();
            case 3:
                return mailAccount.getNoOfMails();
            case 4:
                return rb.getString("MailBoxState." + mailAccount.getState().name());
            case 5:
                return Long.valueOf(mailAccount.getMailBoxSize());
            case 6:
                return mailAccount.getElapsedTime();
            case 7:
                return mailAccount.getPollPeriode();
            default:
                break;
        }
        return mailAccount.getAccountName();
    }

}
