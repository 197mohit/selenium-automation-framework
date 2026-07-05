package com.paytm.framework.utils.email;

import javax.mail.NoSuchProviderException;
import javax.mail.Store;

public interface StoreSetup {

    Store getStore(String emailAddress, String password) throws NoSuchProviderException;
}
