package org.schalm.mailcheck;

/**
 * Global preferences for the application.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: ApplicationPreferences.java 133 2014-01-21 16:29:43Z cschalm $
 */
public class ApplicationPreferences {
    private boolean showHeader = false;
    private boolean confirmDelete = false;

    public ApplicationPreferences() {
    }

    public boolean isShowHeader() {
        return this.showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

    public boolean isConfirmDelete() {
        return this.confirmDelete;
    }

    public void setConfirmDelete(boolean confirmDelete) {
        this.confirmDelete = confirmDelete;
    }

}
