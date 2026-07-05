package com.paytm.base.test;

import com.paytm.apphelpers.AuthHelpers;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.util.exception.authException.AuthException;

/**
 * Created by deepakkumar on 27/2/18.
 */
public class User implements Comparable<User> {

    private final String mobNo;
    private final String password;

    public User(String mobNo, String password) {
        this.mobNo = mobNo;
        this.password = password;
    }

    public String mobNo() {
        return mobNo;
    }

    public String password() {
        return password;
    }

    public String ssoToken() throws AuthException {
        return AuthHelpers.getSSOToken(mobNo,password);
    }

    public String paytmToken() throws AuthException {
        return AuthHelpers.getPaytmToken(mobNo, password);
    }

    public String walletToken() throws AuthException {
        String token = AuthHelpers.getWalletToken(mobNo, password);
        Reporter.report.info("Token for ["+mobNo+"] is ["+token+"]");
        return token;
    }

    public String txnToken() throws AuthException {
        String token = AuthHelpers.getTxnToken(mobNo, password);
        Reporter.report.info("Token for ["+mobNo+"] is ["+token+"]");
        return token;
    }

    public String custId() throws AuthException {
        return AuthHelpers.getCustomerID(mobNo);
    }

    public void purge() {
    }

    @Override
    public String toString() {
        return mobNo;
    }

    @Override
    public int compareTo(User o) {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && ((User) obj).mobNo.equals(this.mobNo);
    }

    public String ssoToken(String clientId,String SecretKey) throws AuthException {
        return AuthHelpers.getSSOToken(mobNo,password,clientId,SecretKey);
    }

    public String getUserInfoWithSSOAndMid(String SSOToken,String Mid) throws AuthException {
        return AuthHelpers.UserInfoWithSSOAndMid(SSOToken,Mid);
    }

}
