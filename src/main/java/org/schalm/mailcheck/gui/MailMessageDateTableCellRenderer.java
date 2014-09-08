package org.schalm.mailcheck.gui;

import java.text.SimpleDateFormat;

/**
 * TableCellRenderer to display formatted dates.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailMessageDateTableCellRenderer.java 157 2014-03-01 22:21:45Z cschalm $
 */
public class MailMessageDateTableCellRenderer extends MailMessageTableCellRenderer {
    private static final long serialVersionUID = -8074552816728124070L;
    private final SimpleDateFormat sdf;

    public MailMessageDateTableCellRenderer(SimpleDateFormat sdf) {
        super();
        this.sdf = sdf;
    }

    @Override
    protected void setValue(Object value) {
        this.setText(value == null ? "" : sdf.format(value));
    }

}
