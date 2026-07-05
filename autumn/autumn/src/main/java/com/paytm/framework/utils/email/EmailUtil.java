package com.paytm.framework.utils.email;

import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.reports.Report;
import org.fest.assertions.api.Assertions;
import org.testng.Reporter;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;

public class EmailUtil {

    private final static Report report = com.paytm.framework.reporting.Reporter.report;

    public static EmailMessage[] getAllEmails(String emailAddress, String password) {

        Message messages[] = null;
        Folder inbox = null;
        EmailMessage emailMessages[] = null;
        Store store = null;

        try {
            store = StoreType.valueOf(getDomain(emailAddress).toUpperCase()).getStore(emailAddress, password);
            store.connect(emailAddress, password);
            inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);
            messages = inbox.getMessages();
            if (messages.length == 0) {
                throw new RuntimeException("Inbox empty.");
            } else {
                emailMessages = new EmailMessage[messages.length];
                int i = 0;
                for (int j = messages.length - 1; j >= 0; j--) {
                    emailMessages[i] = new EmailMessage();
                    emailMessages[i].setSubject(getEmailSubjectTemp(messages[j]));
                    emailMessages[i].setFrom(getEmailFromTemp(messages[j]));
                    emailMessages[i].setTo(getEmailToTemp(messages[j]));
                    emailMessages[i].setCc(getEmailCcTemp(messages[j]));
                    emailMessages[i].setPlainText(getEmailPlainTextTemp(messages[j]));
                    emailMessages[i].setHtmlText(getEmailHTMLTextTemp(messages[j]));
                    i++;
                }
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        } finally {
            try {
                if (inbox != null)
                    inbox.close(true);
                if (store != null)
                    store.close();
            } catch (Throwable e1) {
                throw (RuntimeException) e1;
            }
        }
        return emailMessages;
    }

    public static void sendEmail(String from, String to, String cc, String bcc, String subject, String body,
                                 String attachmentPath) {

        report.info("Send email:");
        report.info("From: " + from);
        report.info("to: " + to);
        report.info("cc: " + cc);
        report.info("bcc: " + bcc);
        report.info("subject: " + subject);
        report.info("body: " + body);
        report.info("attachment: " + attachmentPath);

        System.out.println("Send email:");
        System.out.println("\tFrom: " + from);
        System.out.println("\tTo: " + to);
        System.out.println("\tcc: " + cc);
        System.out.println("\tbcc: " + bcc);
        System.out.println("\tsubject: " + subject);
        System.out.println("\tbody: " + body);
        System.out.println("\tattachment: " + attachmentPath);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", ExecutionConfig.SMTP_HOSTNAME);
        props.put("mail.smtp.port", ExecutionConfig.SMTP_PORT);
        props.put("mail.smtp.timeout", "30000");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(ExecutionConfig.SMTP_USERNAME, ExecutionConfig.SMTP_PASSWORD);
                    }
                });

        try {
            Multipart multipart = new MimeMultipart();
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from, null));
            message.addRecipients(Message.RecipientType.TO, to);
            if (null == cc) {
                cc = "";
            }
            message.addRecipients(RecipientType.CC, cc);
            if (null == bcc) {
                bcc = "";
            }
            message.addRecipients(RecipientType.BCC, bcc);
            message.setSubject(subject);

            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setContent(body, "text/html");
            multipart.addBodyPart(textBodyPart);

            if (attachmentPath != null || attachmentPath.trim().isEmpty()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachmentPath);
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(new File(attachmentPath).getName());
                multipart.addBodyPart(attachmentBodyPart);
            }

            message.setContent(multipart);

            Transport.send(message);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteAllEmails(String emailAddress, String password) {

        report.info("Delete all emails [" + emailAddress + "]");
        delete(emailAddress, password, 1, null, -1);
    }

    public static void deleteEmail(String emailAddress, String password, String emailSubject) {
        report.info("Delete email with subject [" + emailSubject + "]");
        delete(emailAddress, password, 2, emailSubject, -1);
    }

    public static void deleteEmail(String emailAddress, String password, int emailPosition) {
        report.info("Delete email at position [" + emailPosition + "]");
        delete(emailAddress, password, 3, null, emailPosition);
    }

    private static String getDomain(String emailAddress) {
        String abc = emailAddress.split("@")[1];
        abc = abc.split("\\.")[0];
        return abc; // abc@def.gh.ij returns "def"
    }

    private static void delete(String emailAddress, String password, int type, String emailSubject, int emailPosition) {

        Store store = null;
        try {
            store = StoreType.valueOf(getDomain(emailAddress).toUpperCase()).getStore(emailAddress, password);
            store.connect(emailAddress, password);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        Folder inbox = null;
        boolean emailPresent = false;
        try {
            inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            Message messages[] = inbox.getMessages();
            if (messages.length != 0) {
                if (type == 1) {
                    emailPresent = true;
                    for (Message m : messages) {
                        m.setFlag(Flags.Flag.DELETED, true);
                    }
                } else if (type == 2) {
                    for (Message m : messages) {
                        if (m.getSubject().equalsIgnoreCase(emailSubject)) {
                            emailPresent = true;
                            m.setFlag(Flags.Flag.DELETED, true);
                            break;
                        }
                    }
                } else {
                    if (emailPosition >= 0 && emailPosition < messages.length) {
                        emailPresent = true;
                        messages[emailPosition].setFlag(Flags.Flag.DELETED, true);
                    }
                }
            } else {
                // do nothing
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        } finally {
            try {
                if (inbox != null)
                    inbox.close(true);
                if (store != null)
                    store.close();
            } catch (Throwable e1) {
                throw (RuntimeException) e1;
            }
        }
    }

    public static EmailMessage getEmail(String emailAddress, String password, int emailPosition) {
        report.info("Read email at position [" + emailPosition + "] from inbox [" + emailAddress + "]");
        EmailMessage messages[] = getAllEmails(emailAddress, password);
        if (emailPosition < 0 || emailPosition >= messages.length) {
            throw new RuntimeException("Email not present.");
        }
        return messages[emailPosition];
    }

    public static EmailMessage getEmail(EmailMessage messages[], String emailSubject) {
        report.info(" Get email with subject [" + emailSubject + "]");
        int emailPosition = getEmailPosition(messages, emailSubject);
        return messages[emailPosition];
    }

    public static EmailMessage getEmail(String emailAddress, String password, String emailSubject) {
        report.info("Read email with subject [" + emailSubject + "] from inbox [" + emailAddress + "]");
        EmailMessage messages[] = getAllEmails(emailAddress, password);
        return messages[getEmailPosition(messages, emailSubject)];
    }

    private static int getEmailPosition(EmailMessage messages[], String emailSubject) {
        int emailPosition = -1;
        try {
            if (messages.length != 0) {
                for (int i = 0; i <= messages.length; i++) {
                    if (messages[i].getSubject().equalsIgnoreCase(emailSubject)) {
                        emailPosition = i;
                        break;
                    }
                }
            }
            if (emailPosition == -1) {
                throw new RuntimeException("Email not present.");
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
        return emailPosition;
    }

    private static String getEmailSubjectTemp(Message message) {

        String emailSubject = null;
        try {
            emailSubject = message.getSubject();
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
        return emailSubject;
    }

    private static String getEmailFromTemp(Message message) {

        Address from[];
        String emailFrom = null;
        try {
            from = message.getFrom();
            emailFrom = from[0].toString();
            if (emailFrom.contains("<")) {
                emailFrom = emailFrom.substring(emailFrom.indexOf("<") + 1, emailFrom.indexOf(">"));
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
        return emailFrom;
    }

    private static String[] getEmailToTemp(Message message) {

        Address to[];
        String emailTo[] = null;
        int i = 0;
        try {
            to = message.getRecipients(RecipientType.TO);
            if (to != null) {
                emailTo = new String[to.length];
                for (Address a : to) {
                    emailTo[i] = a.toString();
                    if (emailTo[i].contains("<")) {
                        emailTo[i] = emailTo[i].substring(emailTo[i].indexOf("<") + 1, emailTo[i].indexOf(">"));
                        i++;
                    }
                }
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
        return emailTo;
    }

    private static String[] getEmailCcTemp(Message message) {

        Address cc[];
        String emailCc[] = null;
        int i = 0;
        try {
            cc = message.getRecipients(RecipientType.CC);
            if (cc != null) {
                emailCc = new String[cc.length];
                for (Address a : cc) {
                    emailCc[i] = a.toString();
                    if (emailCc[i].contains("<")) {
                        emailCc[i] = emailCc[i].substring(emailCc[i].indexOf("<") + 1, emailCc[i].indexOf(">"));
                        i++;
                    }
                }
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
        return emailCc;
    }

    private static String getEmailPlainTextTemp(Message message) {

        String emailText = null;
        try {
            if (message.getContent() instanceof String) {
                emailText = message.getContent().toString();
            } else {
                Multipart multipart = (Multipart) message.getContent();
                for (int x = 0; x < multipart.getCount(); x++) {
                    BodyPart bodyPart = multipart.getBodyPart(x);
                    String disposition = bodyPart.getDisposition();
                    if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                    } else {
                        if (bodyPart.getContentType().toUpperCase().contains("TEXT/PLAIN")) {
                            emailText = bodyPart.getContent().toString();
                            break;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
        return emailText;
    }

    private static String getEmailHTMLTextTemp(Message message) {

        String emailText = null;
        try {
            if (message.getContent() instanceof String) {
                emailText = message.getContent().toString();
            } else {
                Multipart multipart = (Multipart) message.getContent();
                for (int x = 0; x < multipart.getCount(); x++) {
                    BodyPart bodyPart = multipart.getBodyPart(x);
                    String disposition = bodyPart.getDisposition();
                    if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                    } else {
                        if (bodyPart.getContent() instanceof String) {
                            if (bodyPart.getContentType().toUpperCase().contains("TEXT/HTML")) {
                                emailText = bodyPart.getContent().toString();
                                break;
                            }
                        } else {
                            Multipart multipart1 = (Multipart) bodyPart.getContent();
                            BodyPart bodyPart1 = multipart1.getBodyPart(0);
                            if (bodyPart1.getContent() instanceof String) {
                                if (bodyPart1.getContentType().toUpperCase().contains("TEXT/HTML")) {
                                    emailText = bodyPart1.getContent().toString();
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
        return emailText;
    }

    public static void assertEmailSubject(EmailMessage message, String expectedEmailSubject) {
        report.info("Assert email subject [" + expectedEmailSubject + "]");
        String emailSubject = message.getSubject();
        Assertions.assertThat(emailSubject).isEqualToIgnoringCase(expectedEmailSubject.trim());
    }

    public static void assertEmailSubjectContains(EmailMessage message, String expectedEmailSubject) {
        report.info("Assert email subject contains [" + expectedEmailSubject + "]");
        String emailSubject = message.getSubject();
        Assertions.assertThat(emailSubject).containsIgnoringCase(expectedEmailSubject.trim());
    }

    public static void assertEmailFrom(EmailMessage message, String expectedEmailFrom) {
        report.info("Assert email from [" + expectedEmailFrom + "]");
        String emailFrom = message.getFrom();
        Assertions.assertThat(emailFrom.toLowerCase()).isEqualTo(expectedEmailFrom.toLowerCase().trim());
    }

    public static void assertEmailTo(EmailMessage message, String... expectedEmailTo) {
        report.info("Assert email to [" + expectedEmailTo + "]");
        String emailTo[] = message.getTo();
        emailTo = convertStringArrayCase(emailTo, false);
        Arrays.sort(emailTo);

        expectedEmailTo = trimStringArray(expectedEmailTo);
        expectedEmailTo = convertStringArrayCase(expectedEmailTo, false);
        Arrays.sort(expectedEmailTo);

        Assertions.assertThat(emailTo).isEqualTo(expectedEmailTo);
    }

    private static String[] convertStringArrayCase(String stringArray[], boolean toUpper) {
        if (toUpper) {
            for (int i = 0; i < stringArray.length; i++) {
                stringArray[i] = stringArray[i].toUpperCase();
            }
        } else {
            for (int i = 0; i < stringArray.length; i++) {
                stringArray[i] = stringArray[i].toLowerCase();
            }
        }
        return stringArray;
    }

    private static String[] trimStringArray(String stringArray[]) {
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = stringArray[i].trim();
        }
        return stringArray;
    }

    public static void assertEmailCc(EmailMessage message, String... expectedEmailCc) {
        report.info("Assert email cc [" + expectedEmailCc + "]");
        String emailCc[] = message.getCc();
        emailCc = convertStringArrayCase(emailCc, false);
        Arrays.sort(emailCc);

        expectedEmailCc = trimStringArray(expectedEmailCc);
        expectedEmailCc = convertStringArrayCase(expectedEmailCc, false);
        Arrays.sort(expectedEmailCc);
        Assertions.assertThat(emailCc).isEqualTo(expectedEmailCc);
    }

    public static void assertEmailBcc(EmailMessage message, String... expectedEmailBcc) {
        report.info("Assert email bcc [" + expectedEmailBcc + "]");
        String emailBcc[] = message.getBcc();
        emailBcc = convertStringArrayCase(emailBcc, false);
        Arrays.sort(emailBcc);

        expectedEmailBcc = trimStringArray(expectedEmailBcc);
        expectedEmailBcc = convertStringArrayCase(expectedEmailBcc, false);
        Arrays.sort(expectedEmailBcc);
        Assertions.assertThat(emailBcc).isEqualTo(expectedEmailBcc);
    }

    public static void replyEmail(String from, String password, String subject, String body) {
        report.info("Reply to email");
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", ExecutionConfig.SMTP_HOSTNAME);
        props.put("mail.smtp.port", ExecutionConfig.SMTP_PORT);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(ExecutionConfig.SMTP_USERNAME, ExecutionConfig.SMTP_PASSWORD);
                    }
                });

        Message messages[];
        Folder inbox = null;
        Message replyToMessage = null;
        Store store = null;

        try {
            store = StoreType.valueOf(getDomain(from).toUpperCase()).getStore(from, password);
            store.connect(from, password);
            inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);
            messages = inbox.getMessages();
            if (messages.length == 0) {
                throw new RuntimeException("Inbox empty.");
            } else {
                for (Message message : messages) {
                    if (message.getSubject().contains(subject)) {
                        replyToMessage = message;
                        break;
                    }
                }
            }

            String messageFrom = InternetAddress.toString(replyToMessage.getFrom());
            String messageTo = InternetAddress.toString(replyToMessage.getRecipients(Message.RecipientType.TO));
            String messageCC = InternetAddress.toString(replyToMessage.getRecipients(Message.RecipientType.CC));
            String messageText = getEmailHTMLTextTemp(replyToMessage);

            if (null == messageCC || messageCC.isEmpty()) {
                messageCC = messageTo;
            } else {
                messageCC = messageCC + "," + messageTo;
            }

            String[] ccList = messageCC.split(",");
            messageCC = "";
            for (String cc : ccList) {
                if (!cc.toLowerCase().contains(from.toLowerCase())) {
                    messageCC = messageCC + cc + ", ";
                }
            }

            MimeMessage message2 = new MimeMessage(session);
            MimeMessage message3 = (MimeMessage) replyToMessage.reply(true);
            message2.setFrom(new InternetAddress(from));
            message2.addRecipients(RecipientType.TO, messageFrom);
            message2.addRecipients(RecipientType.CC, messageCC);
            message2.setSubject(message3.getSubject());
            message2.setContent(body + "<br/><br/>" + messageText, "text/html");

            Transport.send(message2);

        } catch (Exception e) {
            Reporter.log("Couldn't replyEmail "+e.getMessage());
            throw (RuntimeException) e;
        } finally {
            try {
                if (inbox != null) {
                    inbox.close(true);
                }
                if (store != null) {
                    store.close();
                }
            } catch (Throwable e1) {
                throw (RuntimeException) e1;
            }
        }
    }
}