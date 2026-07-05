package com.paytm.base.test;

import com.paytm.utils.merchant.util.exception.authException.AuthException;

/**
 * Created by deepakkumar on 1/3/18.
 */
public class CachedUser extends User {

    private String ssoToken;
    private String paytmToken;
    private String walletToken;
    private String custId;

    public CachedUser(String mobNo, String password) {
        super(mobNo, password);
    }

    @Override
    public synchronized String ssoToken() throws AuthException {
        if (ssoToken == null) {
            ssoToken = super.ssoToken();
        }
        return ssoToken;
    }

    @Override
    public String paytmToken() throws AuthException {
        if (paytmToken == null) {
            paytmToken = super.paytmToken();
        }
        return paytmToken;
    }

    @Override
    public String walletToken() throws AuthException {
        if (walletToken == null) {
            walletToken = super.walletToken();
        }
        return walletToken;
    }

    @Override
    public String custId() throws AuthException {
        if (custId == null) {
            custId = super.custId();
        }
        return custId;
    }

    @Override
    public void purge() {
        ssoToken = null;
        paytmToken = null;
        walletToken = null;
    }
}
