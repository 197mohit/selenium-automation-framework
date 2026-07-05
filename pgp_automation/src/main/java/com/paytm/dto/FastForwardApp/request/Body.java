package com.paytm.dto.FastForwardApp.request;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.processTransactionV1.ExtendInfo;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "authMode",
        "channel",
        "currency",
        "customerId",
        "deviceId",
        "extendInfo",
        "industryType",
        "orderId",
        "paymentMode",
        "reqType",
        "txnAmount"
})
public class Body {

    @JsonProperty("authMode")
    private String authMode;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("customerId")
    private String customerId;
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;
    @JsonProperty("industryType")
    private String industryType;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("paymentMode")
    private String paymentMode;
    @JsonProperty("reqType")
    private String reqType;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("authMode")
    public String getAuthMode() {
        return authMode;
    }

    @JsonProperty("authMode")
    public Body setAuthMode(String authMode) {
        this.authMode = authMode;
        return this;
    }

    @JsonProperty("channel")
    public String getChannel() {
        return channel;
    }

    @JsonProperty("channel")
    public Body setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public Body setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @JsonProperty("customerId")
    public String getCustomerId() {
        return customerId;
    }

    @JsonProperty("customerId")
    public Body setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    @JsonProperty("deviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("deviceId")
    public Body setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    @JsonProperty("extendInfo")
    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public Body setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }

    @JsonProperty("industryType")
    public String getIndustryType() {
        return industryType;
    }

    @JsonProperty("industryType")
    public Body setIndustryType(String industryType) {
        this.industryType = industryType;
        return this;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public Body setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("paymentMode")
    public Body setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
        return this;
    }

    @JsonProperty("reqType")
    public String getReqType() {
        return reqType;
    }

    @JsonProperty("reqType")
    public Body setReqType(String reqType) {
        this.reqType = reqType;
        return this;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public Body setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Body setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
