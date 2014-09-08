package org.schalm.mailcheck.gui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.schalm.util.helper.string.StringHelper;

/**
 * TableCellRenderer to display the size formatted.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailAccountSizeTableCellRenderer.java 140 2014-02-09 18:45:39Z cschalm $
 */
public class MailAccountSizeTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int rowInModel = table.convertRowIndexToModel(row);
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowInModel, column);
        if (column == 5) {
            // size-column
            ((JLabel) component).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        return component;
    }

    @Override
    protected void setValue(Object value) {
        this.setText(value == null ? "" : StringHelper.getSizeString(((Long) value).longValue()));
    }

}
