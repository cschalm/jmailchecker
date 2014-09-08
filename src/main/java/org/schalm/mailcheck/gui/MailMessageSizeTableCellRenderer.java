package org.schalm.mailcheck.gui;

import org.schalm.util.helper.string.StringHelper;

/**
 * TableCellRenderer to display the size formatted.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailMessageSizeTableCellRenderer.java 157 2014-03-01 22:21:45Z cschalm $
 */
public class MailMessageSizeTableCellRenderer extends MailMessageTableCellRenderer {
    private static final long serialVersionUID = 5891703700529660500L;

    @Override
    protected void setValue(Object value) {
        this.setText(value == null ? "" : StringHelper.getSizeString(((Integer) value).intValue()));
    }

}
