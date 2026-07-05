package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayerAccountDetail {

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("ifsc")
    private String ifsc;

    @JsonProperty("accountName")
    private String accountName;

    public PayerAccountDetail() {
    }

    public PayerAccountDetail(String accountNumber, String ifsc, String accountName) {
        this.accountNumber = accountNumber;
        this.ifsc = ifsc;
        this.accountName = accountName;
    }

    @JsonProperty("accountNumber")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("accountNumber")
    public PayerAccountDetail setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    @JsonProperty("ifsc")
    public String getIfsc() {
        return ifsc;
    }

    @JsonProperty("ifsc")
    public PayerAccountDetail setIfsc(String ifsc) {
        this.ifsc = ifsc;
        return this;
    }

    @JsonProperty("accountName")
    public String getAccountName() {
        return accountName;
    }

    @JsonProperty("accountName")
    public PayerAccountDetail setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }
}
