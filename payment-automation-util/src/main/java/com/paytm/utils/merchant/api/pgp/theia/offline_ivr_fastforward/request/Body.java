package com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Body {

    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("reqType")
    private String reqType;
    @JsonProperty("paymentMode")
    private String paymentMode;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("customerId")
    private String customerId;
    @JsonProperty("industryType")
    private String industryType;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("appIP")
    private String appIP;
    @JsonProperty("authMode")
    private String authMode;
    @JsonProperty("channel")
    private String channel;

    @JsonProperty("extendInfo")
    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public void setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
    }

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    @JsonProperty("signature")
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @JsonProperty("reqType")
    public String getReqType() {
        return reqType;
    }

    @JsonProperty("reqType")
    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("paymentMode")
    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("customerId")
    public String getCustomerId() {
        return customerId;
    }

    @JsonProperty("customerId")
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @JsonProperty("industryType")
    public String getIndustryType() {
        return industryType;
    }

    @JsonProperty("industryType")
    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("deviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("deviceId")
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @JsonProperty("appIP")
    public String getAppIP() {
        return appIP;
    }

    @JsonProperty("appIP")
    public void setAppIP(String appIP) {
        this.appIP = appIP;
    }

    @JsonProperty("authMode")
    public String getAuthMode() {
        return authMode;
    }

    @JsonProperty("authMode")
    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    @JsonProperty("channel")
    public String getChannel() {
        return channel;
    }

    @JsonProperty("channel")
    public void setChannel(String channel) {
        this.channel = channel;
    }

}
