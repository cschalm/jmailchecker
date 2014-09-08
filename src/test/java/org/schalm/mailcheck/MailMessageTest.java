package org.schalm.mailcheck;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Test;
import org.schalm.util.helper.test.AbstractTest;

/**
 * Test for {@link MailMessage}.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailMessageTest.java 147 2014-02-11 12:05:34Z cschalm $
 */
public class MailMessageTest extends AbstractTest {

    /**
     * Test of equal MailMessages.
     */
    @Test
    public void testEqualsSuccess() {
        String from = "a@a.a";
        String to = "b@b.b";
        String subject = "subject";
        int size = 42;
        Date receivedDate = new Date();
        MailMessage one = new MailMessage(from, to, subject, receivedDate, size);
        MailMessage two = new MailMessage(from, to, subject, receivedDate, size);
        assertTrue(one.equals(two));
    }

    /**
     * Test of unequal MailMessages.
     */
    @Test
    public void testEqualsFailure() {
        String from = "a@a.a";
        String to = "b@b.b";
        String subject = "subject";
        int size = 42;
        Date receivedDate = new Date();
        MailMessage one = new MailMessage(from, to, subject, receivedDate, size);
        MailMessage two = new MailMessage(from, to, subject + " ", receivedDate, size);
        assertFalse(one.equals(two));
    }

}
