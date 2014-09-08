package org.schalm.mailcheck;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for storing the application's configuration. Provides methods for loading and saving
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: Configuration.java 147 2014-02-11 12:05:34Z cschalm $
 */
@XmlRootElement(name = "JMailChecker", namespace = "http://www.schalm.org")
public class Configuration {
    private final ArrayList<MailAccount> accounts = new ArrayList<>();
    private ApplicationPreferences prefs = new ApplicationPreferences();

    public Configuration() {
        super();
    }

    public ApplicationPreferences getPreferences() {
        return prefs;
    }

    public void setPreferences(ApplicationPreferences prefs) {
        this.prefs = prefs;
    }

    @XmlElement(name = "mailAccount")
    public ArrayList<MailAccount> getAccountsList() {
        return accounts;
    }

}
