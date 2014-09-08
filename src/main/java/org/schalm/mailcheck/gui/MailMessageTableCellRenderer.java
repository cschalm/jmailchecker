package org.schalm.mailcheck.gui;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.schalm.mailcheck.MailMessage;

/**
 * TableCellRenderer to display unread messages in bold and to align the size-column right.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailMessageTableCellRenderer.java 140 2014-02-09 18:45:39Z cschalm $
 */
public class MailMessageTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 2981036676994111775L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int rowInModel = table.convertRowIndexToModel(row);
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowInModel, column);
        MailMessage mm = (MailMessage) table.getModel().getValueAt(rowInModel, 6);
        if (!mm.isRead()) {
            Font font = component.getFont();
            Font boldFont = new Font(font.getName(), Font.BOLD, font.getSize());
            component.setFont(boldFont);
        }
        if (column == 5) {
            // size-column
            ((JLabel) component).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        return component;
    }

}
