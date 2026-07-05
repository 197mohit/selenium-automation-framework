
package com.paytm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "instaUrl",
        "amount",
        "bankRRN",
        "channelCode",
        "orderId",
        "mobileNumber",
        "externalSerialNo",
        "txnStatus",
        "responseCode",
        "responseMessage",
        "mid"
})
public class MockbankUPIIntentPayDTO {

    @JsonProperty("instaUrl")
    private String instaUrl;
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("bankRRN")
    private String bankRRN;
    @JsonProperty("channelCode")
    private String channelCode;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("mobileNumber")
    private String mobileNumber;
    @JsonProperty("externalSerialNo")
    private String externalSerialNo;
    @JsonProperty("txnStatus")
    private String txnStatus;
    @JsonProperty("responseCode")
    private String responseCode;
    @JsonProperty("responseMessage")
    private String responseMessage;
    @JsonProperty("mid")
    private String mid;

    public MockbankUPIIntentPayDTO(String amount, String orderId, String externalSerialNo, String mid){
        this.instaUrl = "https://pgp-automation.paytm.in";
        this.amount = amount;
        this.bankRRN = "1111";
        this.channelCode = "Paytm";
        this.orderId = orderId;
        this.mobileNumber = "8006006993";
        this.externalSerialNo = externalSerialNo;
        this.txnStatus = "SUCCESS";
        this.responseCode = "001";
        this.responseMessage = "success";
        this.mid = mid;
    }

    @JsonProperty("instaUrl")
    public String getInstaUrl() {
        return instaUrl;
    }

    @JsonProperty("instaUrl")
    public void setInstaUrl(String instaUrl) {
        this.instaUrl = instaUrl;
    }

    @JsonProperty("amount")
    public String getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(String amount) {
        this.amount = amount;
    }

    @JsonProperty("bankRRN")
    public String getBankRRN() {
        return bankRRN;
    }

    @JsonProperty("bankRRN")
    public void setBankRRN(String bankRRN) {
        this.bankRRN = bankRRN;
    }

    @JsonProperty("channelCode")
    public String getChannelCode() {
        return channelCode;
    }

    @JsonProperty("channelCode")
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("mobileNumber")
    public String getMobileNumber() {
        return mobileNumber;
    }

    @JsonProperty("mobileNumber")
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @JsonProperty("externalSerialNo")
    public String getExternalSerialNo() {
        return externalSerialNo;
    }

    @JsonProperty("externalSerialNo")
    public void setExternalSerialNo(String externalSerialNo) {
        this.externalSerialNo = externalSerialNo;
    }

    @JsonProperty("txnStatus")
    public String getTxnStatus() {
        return txnStatus;
    }

    @JsonProperty("txnStatus")
    public void setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
    }

    @JsonProperty("responseCode")
    public String getResponseCode() {
        return responseCode;
    }

    @JsonProperty("responseCode")
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    @JsonProperty("responseMessage")
    public String getResponseMessage() {
        return responseMessage;
    }

    @JsonProperty("responseMessage")
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

}