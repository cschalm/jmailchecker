package org.schalm.mailcheck;

import java.util.Date;

/**
 * E-Mail including text-content and headers.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailMessage.java 137 2014-01-24 12:32:06Z cschalm $
 */
public class MailMessage {
    private MailAccount mailAccount;
    private final String from;
    private final String to;
    private final String subject;
    private final Date date;
    private final int size;
    private String content;
    private String header;
    private boolean read;
    private final int hashCode;

    public MailMessage(String from, String to, String subject, Date receivedDate, int size) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.size = size;
        if (receivedDate == null) {
            receivedDate = new Date(0L);
        }
        this.date = receivedDate;
        this.content = null;
        this.read = false;
        this.hashCode = from.hashCode() + to.hashCode() + subject.hashCode() + receivedDate.hashCode() + size;
    }

    public Date getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getSize() {
        return size;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isCached() {
        return this.content != null;
    }

    public MailAccount getMailAccount() {
        return this.mailAccount;
    }

    public void setMailAccount(MailAccount mailAccount) {
        this.mailAccount = mailAccount;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MailMessage other = (MailMessage) obj;
        return this.hashCode == other.hashCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MailMessage [from=");
        builder.append(this.from);
        builder.append(", subject=");
        builder.append(this.subject);
        builder.append("]");
        return builder.toString();
    }

}
