package com.paytm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UPIIntentRequestDTO {
    private String instaUrl;
    private String amount;
    private String bankRRN = "1111";
    private String channelCode = "Paytm";
    private String orderId;
    private String mobileNumber = "";
    private String externalSerialNo;
    private String txnStatus;
    private String responseCode;
    private String responseMessage;
    private String mid;

    public String getInstaUrl() {
        return instaUrl;
    }

    public UPIIntentRequestDTO setInstaUrl(String instaUrl) {
        this.instaUrl = instaUrl;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public UPIIntentRequestDTO setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public String getBankRRN() {
        return bankRRN;
    }

    public UPIIntentRequestDTO setBankRRN(String bankRRN) {
        this.bankRRN = bankRRN;
        return this;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public UPIIntentRequestDTO setChannelCode(String channelCode) {
        this.channelCode = channelCode;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public UPIIntentRequestDTO setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public UPIIntentRequestDTO setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
        return this;
    }

    public String getExternalSerialNo() {
        return externalSerialNo;
    }

    public UPIIntentRequestDTO setExternalSerialNo(String externalSerialNo) {
        this.externalSerialNo = externalSerialNo;
        return this;
    }

    public String getTxnStatus() {
        return txnStatus;
    }

    public UPIIntentRequestDTO setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
        return this;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public UPIIntentRequestDTO setResponseCode(String responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public UPIIntentRequestDTO setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
        return this;
    }

    public String getMid() {
        return mid;
    }

    public UPIIntentRequestDTO setMid(String mid) {
        this.mid = mid;
        return this;
    }
}
