package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "accountHolderName",
        "channelCode",
        "accountNumber",
        "ifsc",
        "accountType"
})
public class MandateAccountDetails {

    @JsonProperty("accountHolderName")
    private String accountHolderName;
    @JsonProperty("channelCode")
    private String channelCode;
    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("ifsc")
    private String ifsc;
    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("accountHolderName")
    public String getAccountHolderName() {
        return accountHolderName;
    }

    @JsonProperty("accountHolderName")
    public MandateAccountDetails setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
        return this;
    }

    @JsonProperty("channelCode")
    public String getChannelCode() {
        return channelCode;
    }

    @JsonProperty("channelCode")
    public MandateAccountDetails setChannelCode(String channelCode) {
        this.channelCode = channelCode;
        return this;
    }

    @JsonProperty("accountNumber")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("accountNumber")
    public MandateAccountDetails setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    @JsonProperty("ifsc")
    public String getIfsc() {
        return ifsc;
    }

    @JsonProperty("ifsc")
    public MandateAccountDetails setIfsc(String ifsc) {
        this.ifsc = ifsc;
        return this;
    }

    @JsonProperty("accountType")
    public String getAccountType() {
        return accountType;
    }

    @JsonProperty("accountType")
    public MandateAccountDetails setAccountType(String accountType) {
        this.accountType = accountType;
        return this;
    }

    public MandateAccountDetails(){
        this.accountHolderName = "AkshatSharma";
        this.channelCode = "PPBL";
        this.accountNumber = "915445500424";
        this.ifsc = "PYTM0000001";
        this.accountType = "Savings";
    }
    public MandateAccountDetails(String IFSC){
        this.accountHolderName = "AjeeshNair";
        this.channelCode = "";
        this.accountNumber = "915445500424";
        this.ifsc = IFSC;
        this.accountType = "Savings";
    }

}