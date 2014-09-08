package org.schalm.mailcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import javax.mail.NoSuchProviderException;
import javax.xml.bind.JAXB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schalm.mailcheck.MailAccount.StateChangeType;

/**
 * Test for {@link MailAccount}.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailAccountTest.java 147 2014-02-11 12:05:34Z cschalm $
 */
public class MailAccountTest {
    private static GreenMail greenMail;
    private final String userName = "TestUser";
    private final String password = "TestUser";
    private final String mailHost = "localhost";
    private final String accountName = "TestAccount";
    private final int pollPeriode = 300;
    private final int port = 3110;
    private final boolean ssl = false;

    @BeforeClass
    public static void setUp() throws Exception {
        greenMail = new GreenMail(ServerSetupTest.ALL);
        greenMail.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    @Test
    public void testCreateAccount() throws NoSuchProviderException {
        MailAccount mailAccount = JAXB.unmarshal(this.getClass().getClassLoader().getResourceAsStream("testAccount.xml"), MailAccount.class);
        verifyAccount(mailAccount);
    }

    private void verifyAccount(MailAccount account) {
        assertEquals("Username is wrong!", userName, account.getUserName());
        assertEquals("Password is wrong!", password, account.getPassword());
        assertEquals("Accountname is wrong!", accountName, account.getAccountName());
        assertEquals("Host is wrong!", mailHost, account.getMailHost());
        assertEquals("Poll periode is wrong!", pollPeriode, account.getPollPeriode());
        assertEquals("Port is wrong!", port, account.getPort());
        assertEquals("SSL is wrong!", ssl, account.isSsl());
        assertEquals("Protocol is wrong!", MailAccount.Protocol.POP3, account.getStoreType());
    }

    @Test
    public void testCheckAccount() throws Exception {
        MailAccount mailAccount = JAXB.unmarshal(this.getClass().getClassLoader().getResourceAsStream("testAccount.xml"), MailAccount.class);
        TestObserver observer = new TestObserver();
        mailAccount.addObserver(observer);
        assertEquals("Mailbox contains already a message!", 0, mailAccount.getNoOfMails());
        assertEquals("Mailbox has wrong state!", MailAccount.MailBoxState.NOT_CHECKED, mailAccount.getState());
        GreenMailUtil.sendTextEmail(userName, "from@localhost.com", "subject", "body", ServerSetupTest.SMTP);
        mailAccount.checkAccount();
        assertEquals("Mailbox contains not expected number of messages!", 1, mailAccount.getNoOfMails());
        assertTrue("Mailbox does not contain new messages!", mailAccount.hasNewMessages());
        mailAccount.resetNewMessages();
        assertFalse("Mailbox contains still new messages!", mailAccount.hasNewMessages());
        GreenMailUtil.sendTextEmail(userName, "from@localhost.com", "subject2", "body2", ServerSetupTest.SMTP);
        mailAccount.checkAccount();
        assertEquals("Mailbox contains not expected number of messages!", 2, mailAccount.getNoOfMails());
        assertTrue("Mailbox does not contain new messages!", mailAccount.hasNewMessages());
        assertEquals("Mailbox contains not expected number of messages!", 2, mailAccount.getMessages().size());
        Collection<MailMessage> list = new ArrayList<>(mailAccount.getMessages());
        for (MailMessage message : list) {
            assertFalse("Mail is empty!", mailAccount.getContent(message, true).isEmpty());
        }
        mailAccount.deleteMailMessages(list);
        assertEquals("Mailbox contains not expected number of messages!", 0, mailAccount.getNoOfMails());
        GreenMailUtil.sendTextEmail(userName, "from@localhost.com", "subject", "body", ServerSetupTest.SMTP);
        GreenMailUtil.sendTextEmail(userName, "from@localhost.com", "subject2", "body2", ServerSetupTest.SMTP);
        mailAccount.checkAccount();
        assertEquals("Mailbox contains not expected number of messages!", 2, mailAccount.getNoOfMails());
        mailAccount.deleteMailMessages(mailAccount.getMessages());
        assertEquals("Mailbox contains not expected number of messages!", 0, mailAccount.getNoOfMails());
        assertEquals("Mailbox has wrong state!", MailAccount.MailBoxState.CHECKED, mailAccount.getState());
        assertTrue("Mailbox has no events for account!", observer.getNoOfMailAccountEvents() > 0);
        assertTrue("Mailbox has no events for mailbox!", observer.getNoOfMailBoxEvents() > 0);
    }

    /**
     * <code>Observer</code> to test notification on changes on MailAccount.
     */
    public class TestObserver implements Observer {

        private int noOfMailAccountEvents = 0;
        private int noOfMailBoxEvents = 0;

        @Override
        public void update(Observable o, Object arg) {
            if (o instanceof MailAccount) {
                if (arg != null && arg instanceof StateChangeType) {
                    StateChangeType type = (StateChangeType) arg;
                    switch (type) {
                        case MAILACCOUNT:
                            noOfMailAccountEvents++;
                            break;
                        default:
                            noOfMailBoxEvents++;
                            break;
                    }
                }
            }
        }

        public int getNoOfMailAccountEvents() {
            return noOfMailAccountEvents;
        }

        public int getNoOfMailBoxEvents() {
            return noOfMailBoxEvents;
        }

    }

}
