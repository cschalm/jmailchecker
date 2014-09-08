package org.schalm.mailcheck;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import static javax.mail.Folder.READ_ONLY;
import static javax.mail.Folder.READ_WRITE;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * A mailbox like steve.jobs@hotmail.com.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailAccount.java 168 2014-08-25 16:14:51Z cschalm $
 */
@XmlRootElement
public class MailAccount extends Observable {

    private static final Logger log = Logger.getLogger(MailAccount.class);
    private static final String CONNECTION_TIMEOUT = "30000";
    private String userName;
    // plain, decoded password
    private String plainPassword;
    // Base64-encoded password
    private String encodedPassword;
    private String accountName = null;
    private String mailHost = null;
    private Protocol protocol = Protocol.POP3;
    private int port = 110;
    private boolean ssl = false;
    private int pollPeriode = 10;
    @XmlTransient
    private int elapsedTimeSeconds = 0;
    @XmlTransient
    private MailBoxState state = MailBoxState.NOT_CHECKED;
    @XmlTransient
    private int noOfMails = 0;
    @XmlTransient
    private long mailBoxSize = 0;
    @XmlTransient
    private final HashMap<Integer, MailMessage> messages = new HashMap<>();
    @XmlTransient
    private Store store = null;
    @XmlTransient
    private boolean newMessages = false;
    @XmlTransient
    private final ByteArrayOutputStream baos;
    private PrintStream debugLogger = null;

    public MailAccount() {
        super();
        baos = new ByteArrayOutputStream();
        try {
            debugLogger = new PrintStream(baos, true, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            debugLogger = new PrintStream(baos, true);
        }
    }

    private Properties getConnectionProperties() {
        Properties properties = System.getProperties();
        properties.put("mail.pop3.connectiontimeout", CONNECTION_TIMEOUT);
        properties.put("mail.pop3.timeout", CONNECTION_TIMEOUT);
        properties.put("mail.imap.connectiontimeout", CONNECTION_TIMEOUT);
        properties.put("mail.imap.timeout", CONNECTION_TIMEOUT);
        properties.put("mail.pop3s.connectiontimeout", CONNECTION_TIMEOUT);
        properties.put("mail.pop3s.timeout", CONNECTION_TIMEOUT);
        properties.put("mail.imaps.connectiontimeout", CONNECTION_TIMEOUT);
        properties.put("mail.imaps.timeout", CONNECTION_TIMEOUT);
        properties.put("mail.host", mailHost);
        String provider;
        switch (protocol) {
            case IMAP:
                provider = "imap" + (ssl ? "s" : "");
                break;
            default:
                provider = "pop3" + (ssl ? "s" : "");
        }

        properties.put("mail.store.protocol", provider);
        properties.put("mail." + provider + ".port", Integer.toString(port));

        return properties;
    }

    /**
     * The supported protocol types for fetching mails.
     *
     * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
     * @version $Id: MailAccount.java 168 2014-08-25 16:14:51Z cschalm $
     */
    public enum Protocol {

        POP3, IMAP
    }

    /**
     * The type of state change of a mailbox.
     *
     * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
     * @version $Id: MailAccount.java 168 2014-08-25 16:14:51Z cschalm $
     */
    public enum StateChangeType {

        /**
         * the MailAccount has changed, e.g. elapsed time
         */
        MAILACCOUNT,
        /**
         * the list of mails in a mailbox has changed, e.g. new mails
         */
        MAILBOX
    }

    /**
     * The state of a mailbox.
     *
     * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
     * @version $Id: MailAccount.java 168 2014-08-25 16:14:51Z cschalm $
     */
    public enum MailBoxState {

        NOT_CHECKED, CHECKED, ERROR, FETCHINGALL, FETCHING, DELETING, CONNECTING, CONNECTED
    }

    public String getAccountName() {
        return accountName;
    }

    @XmlTransient
    public int getElapsedTime() {
        if (elapsedTimeSeconds == 0) {
            return 0;
        }
        return elapsedTimeSeconds / 60;
    }

    public String getMailHost() {
        return mailHost;
    }

    public int getNoOfMails() {
        return noOfMails;
    }

    public long getMailBoxSize() {
        return mailBoxSize;
    }

    public List<MailMessage> getMessages() {
        return new ArrayList<>(messages.values());
    }

    public int getPollPeriode() {
        return pollPeriode;
    }

    public int getPort() {
        return port;
    }

    /**
     * @param protocol Protocol to set
     * @throws NoSuchProviderException
     */
    public void setStoreType(Protocol protocol) throws NoSuchProviderException {
        this.protocol = protocol;
        Session session = Session.getInstance(getConnectionProperties());
        session.setDebug(true);
        session.setDebugOut(debugLogger);
        switch (protocol) {
            case IMAP:
                store = session.getStore("imap" + (ssl ? "s" : ""));
                break;
            default:
                store = session.getStore();
        }
    }

    public Protocol getStoreType() {
        return protocol;
    }

    public void checkAccount() throws MessagingException {
        if (log.isDebugEnabled()) {
            log.debug("Checking \"" + getAccountName() + "\"");
        }
        try {
            HashSet<Integer> mailsOnServer = new HashSet<>();
            synchronized (this) {
                Folder inbox = connect(READ_ONLY);
                setState(StateChangeType.MAILACCOUNT, MailBoxState.FETCHINGALL);
                Message[] msgs = inbox.getMessages();
                mailBoxSize = 0L;
                noOfMails = 0;
                if (msgs != null) {
                    noOfMails = msgs.length;
                    for (Message msg : msgs) {
                        MailMessage mm = convertMessage(msg);
                        mm.setMailAccount(this);
                        mm.setRead(msg.isSet(Flags.Flag.SEEN) || msg.isSet(Flags.Flag.ANSWERED));
                        mailBoxSize += msg.getSize();
                        Integer hashCode = Integer.valueOf(mm.hashCode());
                        mailsOnServer.add(hashCode);
                        if (!messages.containsKey(hashCode)) {
                            messages.put(hashCode, mm);
                            newMessages = true;
                        }
//                            if (newMessages) {
//                                if (log.isDebugEnabled()) {
//                                    log.debug(getAccountName() + " has new messages because of " + mm);
//                                }
//                            }
                    }
                    setState(StateChangeType.MAILBOX, MailBoxState.CHECKED);
                    inbox.close(false);
                }
            }
            List<Integer> deletedOnServer = new ArrayList<>();
            for (Integer hashCode : messages.keySet()) {
                if (!mailsOnServer.contains(hashCode)) {
                    deletedOnServer.add(hashCode);
                }
            }
            for (Integer hashCode : deletedOnServer) {
                messages.remove(hashCode);
            }
            elapsedTimeSeconds = 0;
            store.close();
            setState(StateChangeType.MAILACCOUNT, MailBoxState.CHECKED);
        } catch (MessagingException me) {
            setState(StateChangeType.MAILACCOUNT, MailBoxState.ERROR);
            throw me;
        } finally {
            if (store.isConnected()) {
                try {
                    store.close();
                } catch (MessagingException ignore) {
                    // ignore
                }
            }
        }
    }

    public void setElapsedTime(int milliSeconds) {
        if (milliSeconds > 0) {
            int oldTime = elapsedTimeSeconds;
            int secondsToAdd = milliSeconds / 1000;
            elapsedTimeSeconds += secondsToAdd;
//            if (log.isDebugEnabled()) {
//                log.debug(getAccountName() + ": " + elapsedTimeSeconds + " seconds elapsed time, added " + secondsToAdd
//                        + " seconds now");
//                log.debug(getAccountName() + ": poll is " + pollPeriode + ", elapsed time is " + (elapsedTimeSeconds / 60));
//            }
            if (pollPeriode > 0 && elapsedTimeSeconds / 60 >= pollPeriode) {
                try {
                    checkAccount();
                } catch (MessagingException e) {
                    log.warn("Error checking mail for " + getAccountName() + ": " + e.getMessage());
                }
            } else if (elapsedTimeSeconds != oldTime) {
                // no check necessary, but elapsed time changed -> fire event
                setState(StateChangeType.MAILACCOUNT, state);
            }
        }
    }

    public MailBoxState getState() {
        return state;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setMailHost(String mailhost) {
        this.mailHost = mailhost;
    }

    public void setPollPeriode(int pollPeriode) {
        this.pollPeriode = pollPeriode;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get the plain passwort for this MailAccount.
     *
     * @return plain password
     */
    @XmlTransient
    public String getPassword() {
        return plainPassword;
    }

    /**
     * Set the plain passwort for this MailAccount.
     *
     * @param password password to set
     */
    public void setPassword(String password) {
        plainPassword = password;
        byte[] encoded = Base64.encodeBase64(plainPassword.getBytes());
        encodedPassword = new String(encoded);
//        if (log.isDebugEnabled()) {
//            log.debug("plain: " + plainPassword + ", encoded: " + encodedPassword);
//        }
    }

    /**
     * Get the passwort for this MailAccount in Base64-encoding.
     *
     * @return password Base64
     */
    @XmlElement(name = "password")
    public String getEncodedPassword() {
        return encodedPassword;
    }

    /**
     * Set the passwort for this MailAccount in Base64-encoding.
     *
     * @param password password Base64 to set
     */
    public void setEncodedPassword(String password) {
        encodedPassword = password;
        byte[] decoded = Base64.decodeBase64(encodedPassword);
        plainPassword = new String(decoded);
//        if (log.isDebugEnabled()) {
//            log.debug("encoded: " + encodedPassword + ", plain: " + plainPassword);
//        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private Folder connect(int mode) throws MessagingException {
        baos.reset();
        setState(StateChangeType.MAILACCOUNT, MailBoxState.CONNECTING);
        if (!store.isConnected()) {
            store.connect(mailHost, port, userName, plainPassword);
        }
        Folder inbox = store.getDefaultFolder();
        inbox = inbox.getFolder("INBOX");
        inbox.open(mode);
        setState(StateChangeType.MAILACCOUNT, MailBoxState.CONNECTED);

        return inbox;
    }

    public String getContent(MailMessage mailMessage, boolean withHeader) throws Exception {
        try {
            StringBuilder result = new StringBuilder();
            if (!mailMessage.isCached()) {
                synchronized (this) {
                    StringBuilder content = new StringBuilder();
                    StringBuilder headers = new StringBuilder();
                    Folder inbox = connect(READ_ONLY);
                    setState(StateChangeType.MAILACCOUNT, MailBoxState.FETCHING);
                    Message message = findMessage(inbox, mailMessage);
                    if (message != null) {
                        Enumeration<String> header = ((MimeMessage) message).getAllHeaderLines();
                        while (header.hasMoreElements()) {
                            headers.append(header.nextElement()).append("\n");
                        }
                        mailMessage.setHeader(headers.toString());
                        boolean hasTextContent = false;
                        if (message.isMimeType("text/*")) {
                            content.append((String) message.getContent());
                            hasTextContent = true;
                        } else if (message.isMimeType("multipart/*")) {
                            Multipart multiPart = (Multipart) message.getContent();
                            int count = multiPart.getCount();
                            for (int c = 0; c < count; c++) {
                                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(c);
                                if (part.isMimeType("text/*")) {
                                    content.append("\nTEXTPART (").append(part.getContentType()).append(") -->\n\n");
                                    String text = (String) part.getContent();
                                    content.append(text).append("\n\n<-- END-OF-TEXTPART\n");
                                    hasTextContent = true;
                                }
                            }
                        }
                        if (!hasTextContent) {
                            content.append("NO DISPLAYABLE TEXT IN MAIL!\n");
                        }
                        mailMessage.setContent(content.toString());
                        mailMessage.setRead(true);
                        setState(StateChangeType.MAILBOX, MailBoxState.CHECKED);
                    }
                    elapsedTimeSeconds = 0;
                    inbox.close(false);
                    store.close();
                    setState(StateChangeType.MAILACCOUNT, MailBoxState.CHECKED);
                }
            }
            if (withHeader) {
                result.append(mailMessage.getHeader());
                result.append("\n\n");
            }
            result.append(mailMessage.getContent());
            return result.toString();
        } catch (MessagingException | IOException e) {
            log.error("Error loading content of mail", e);
            setState(StateChangeType.MAILACCOUNT, MailBoxState.ERROR);
            throw e;
        } finally {
            if (store.isConnected()) {
                try {
                    store.close();
                } catch (MessagingException ignore) {
                    // ignore
                }
            }
        }
    }

    private void setState(StateChangeType type, MailBoxState state) {
        this.state = state;
        super.setChanged();
        super.notifyObservers(type);
    }

    private Message findMessage(Folder folder, MailMessage toFind) throws MessagingException {
        Message[] msgs = folder.getMessages();
        if (msgs != null) {
            for (Message msg : msgs) {
                MailMessage currentMail = convertMessage(msg);
                if (currentMail.hashCode() == toFind.hashCode()) {
                    return msg;
                }
            }
        }

        return null;
    }

    public void deleteMailMessages(Collection<MailMessage> toDeleteList) throws MessagingException {
        try {
            synchronized (this) {
                Folder inbox = connect(READ_WRITE);
                setState(StateChangeType.MAILACCOUNT, MailBoxState.DELETING);
                for (MailMessage mailMessage : toDeleteList) {
                    Message message = findMessage(inbox, mailMessage);
                    if (message != null) {
                        message.setFlag(Flags.Flag.DELETED, true);
                        mailBoxSize -= message.getSize();
                    }
                    messages.remove(mailMessage.hashCode());
                    setState(StateChangeType.MAILBOX, MailBoxState.CHECKED);
                }
                inbox.close(true);
                store.close();
                elapsedTimeSeconds = 0;
                noOfMails -= toDeleteList.size();
                setState(StateChangeType.MAILACCOUNT, MailBoxState.CHECKED);
            }
        } catch (MessagingException me) {
            setState(StateChangeType.MAILACCOUNT, MailBoxState.ERROR);
            throw me;
        } finally {
            if (store.isConnected()) {
                try {
                    store.close();
                } catch (MessagingException ignore) {
                    // ignore
                }
            }
        }
    }

    public boolean hasNewMessages() {
//        if (log.isDebugEnabled()) {
//            log.debug(accountName + " has new messages: " + newMessages);
//        }
        return newMessages;
    }

    public void resetNewMessages() {
        newMessages = false;
    }

    @Override
    public String toString() {
        return getAccountName();
    }

    public boolean containsMessage(MailMessage mailMessage) {
        return messages.containsKey(Integer.valueOf(mailMessage.hashCode()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MailAccount other = (MailAccount) obj;

        return userName == null ? other.userName == null : userName.equals(other.userName)
                || userName != null && userName.equals(other.userName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (userName != null ? userName.hashCode() : 0);
        return hash;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    private MailMessage convertMessage(Message message) throws MessagingException {
        String from = decodeText(message.getFrom()[0].toString());
        String to = "";
        Address[] toAddresses = message.getRecipients(Message.RecipientType.TO);
        if (toAddresses != null) {
            StringBuilder sb = new StringBuilder();
            for (Address toAddress : toAddresses) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(decodeText(toAddress.toString()));
            }
            to = sb.toString();
        }
        String subject = decodeText(message.getSubject());

        return new MailMessage(from, to, subject, message.getSentDate(), message.getSize());
    }

    private String decodeText(String encoded) {
        try {
            return MimeUtility.decodeText(encoded);
        } catch (UnsupportedEncodingException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error decoding \"" + encoded + "\": " + e.getMessage(), e);
            }
        }

        return encoded;
    }

    public String getLastConnectionLog() {
        return baos.toString();
    }

}
