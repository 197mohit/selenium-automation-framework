package com.paytm.framework.utils.email;

import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public enum StoreType implements StoreSetup {

    GMAIL{
        public Store getStore(String emailAddress, String password) throws NoSuchProviderException {
            Properties properties = new Properties();
            properties.setProperty("mail.host", "imap.gmail.com");
            properties.setProperty("mail.port", "993");
            properties.setProperty("mail.transport.protocol", "imaps");
            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailAddress, password);
                        }
                    });
            return  session.getStore("imaps");
        }
    },
    YAHOO{
        public Store getStore(String emailAddress, String password) throws NoSuchProviderException {
            Properties properties = new Properties();
            properties.setProperty("mail.host", "imap.mail.yahoo.com");
            properties.setProperty("mail.port", "993");
            properties.setProperty("mail.transport.protocol", "imaps");
            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailAddress, password);
                        }
                    });
            return session.getStore("imaps");
        }
    },

    OUTLOOK{
        public Store getStore(String emailAddress, String password) throws NoSuchProviderException {
            Properties properties = new Properties();
            properties.setProperty("mail.host", "imap-mail.outlook.com");
            properties.setProperty("mail.port", "993");
            properties.setProperty("mail.transport.protocol", "imaps");
            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailAddress, password);
                        }
                    });
            return session.getStore("imaps");
        }
    };
}
