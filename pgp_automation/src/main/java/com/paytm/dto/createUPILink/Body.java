package com.paytm.dto.createUPILink;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "txnToken",
        "accountNumber",
        "channelId",
        "orderId",
        "refUrl",
        "txnNote",
        "extendInfo",
        "aggMid",
        "amount"
})
public class Body {

    public Body () {
        this.channelId = "WEB";
        this.refUrl = "";
        this.txnNote = "test";
        this.extendInfo = new ExtendInfo();
    }

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("txnToken")
    private String txnToken;
    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("refUrl")
    private String refUrl;
    @JsonProperty("txnNote")
    private String txnNote;
    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;
    @JsonProperty("aggMid")
    private String aggMid;
    @JsonProperty("amount")
    private String amount;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("txnToken")
    public String getTxnToken() {
        return txnToken;
    }

    @JsonProperty("txnToken")
    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    @JsonProperty("accountNumber")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("accountNumber")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("channelId")
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("refUrl")
    public String getRefUrl() {
        return refUrl;
    }

    @JsonProperty("refUrl")
    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    @JsonProperty("txnNote")
    public String getTxnNote() {
        return txnNote;
    }

    @JsonProperty("txnNote")
    public void setTxnNote(String txnNote) {
        this.txnNote = txnNote;
    }

    @JsonProperty("extendInfo")
    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public void setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
    }

    @JsonProperty("aggMid")
    public String getAggMid() {
        return aggMid;
    }

    @JsonProperty("aggMid")
    public void setAggMid(String aggMid) {
        this.aggMid = aggMid;
    }

    @JsonProperty("amount")
    public String getAmount() {
        return amount;
    }

    @JsonProperty("aggMid")
    public void setAmount(String amount) {
        this.amount = amount;
    }



    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}