package org.schalm.mailcheck.gui;

import java.util.ArrayList;
import java.util.List;
import org.schalm.mailcheck.MailAccount;
import org.schalm.mailcheck.MailMessage;
import org.schalm.util.helper.swing.SimpleTableModel;

/**
 * TableModel for messages (e-Mails).
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailMessageTableModel.java 157 2014-03-01 22:21:45Z cschalm $
 */
public class MailMessageTableModel extends SimpleTableModel<MailMessage> {
    private static final long serialVersionUID = 935715628167677031L;

    public MailMessageTableModel() {
        super();
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Mailbox");
        columnNames.add("From");
        columnNames.add("To");
        columnNames.add("Subject");
        columnNames.add("Date");
        columnNames.add("Size");
        setColumnNames(columnNames);
    }

    public void removeAllRows4MailAccount(MailAccount ma) {
        for (int i = 0; i < getRowCount(); i++) {
            MailMessage mailMessage = getRow(i);
            if (mailMessage.getMailAccount().equals(ma)) {
                this.removeRow(i);
            }
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MailMessage mailMessage = getRow(rowIndex);
        switch (columnIndex) {
            case 0:
                return mailMessage.getMailAccount().getAccountName();
            case 1:
                return mailMessage.getFrom();
            case 2:
                return mailMessage.getTo();
            case 3:
                return mailMessage.getSubject();
            case 4:
                return mailMessage.getDate();
            case 5:
                return Integer.valueOf(mailMessage.getSize());
            case 6:
                return mailMessage;
            case 7:
                return mailMessage.getMailAccount();
            default:
                break;
        }
        return null;
    }

    /**
     * Update model by removing no more existing mails and adding new ones for the given MailAccount.
     *
     * @param mailAccount
     */
    public void update(MailAccount mailAccount) {
        // check for mails to remove from model
        for (int i = 0; i < getRowCount(); i++) {
            MailMessage mailMessage = getRow(i);
            if (mailMessage.getMailAccount().equals(mailAccount) && !mailAccount.containsMessage(mailMessage)) {
                this.removeRow(i);
                // check same index again
                i--;
            }
        }
        for (MailMessage mailMessage : mailAccount.getMessages()) {
            if (!contains(mailMessage)) {
                this.addRow(mailMessage);
            }
        }
    }

}
