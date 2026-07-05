package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 23/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceInfo {
    private boolean payerAccountExists;
    private AccountBalance accountBalance;

    public boolean isPayerAccountExists() {
        return payerAccountExists;
    }

    public void setPayerAccountExists(boolean payerAccountExists) {
        this.payerAccountExists = payerAccountExists;
    }

    public AccountBalance getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(AccountBalance accountBalance) {
        this.accountBalance = accountBalance;
    }
}
