package com.paytm.dto.instaproxy.upipayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// AI-Generated: 2026-04-07 - DTO: Instaproxy PG2 UPI payment request.htm request body
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpiPaymentRequestDTO {

    private static final String DEFAULT_EXTEND_INFO =
            "{\"isDeepLinkReq\":\"true\",\"passThroughExtendInfo\":\"eyJuYXRpdmVKc29uUmVxdWVzdCI6InRydWUiLCJvZmZsaW5lIjoiWSIsIm1lcmNoYW50RWxpZ2libGVVUElDQyI6InRydWUiLCJ1cGlTdWJUeXBlTWVyY2hhbnRFbGlnaWJpbGl0eSI6IltdIiwicHBibE5iRGlyZWN0RmxvdyI6InRydWUifQ==\",\"directPassThroughInfo\":\"eyJwYXl0bU1lcmNoYW50T3JkZXJJZCI6IjIwMjYwNDA5MjIxMTAwMDA2NiIsImNhbGxCYWNrVVJMIjoiaHR0cHM6Ly9wZ3AtcWExNC5wYXl0bS5pbi90aGVpYS9saW5rUGF5bWVudFJlZGlyZWN0IiwiaXNTYXZlZENhcmQiOiJmYWxzZSIsIm1jYyI6IlJldGFpbCIsImlwQWRkciI6IjEyNS4yMi42Ny4xNjIiLCJjb2Z0Q29uc2VudENhY2hlS2V5RGlyZWN0IjoiY29mdENvbnNlbnRDYWNoZUtleURpcmVjdF9xYTEyaWQ0MDAxMDgxMzIzNzU0Ml8yMDI2MDQwOTIyMTEwMDAwNjYiLCJtZXJjaGFudERpc3BsYXlOYW1lIjoiQUVST05VVFJJWCBTUE9SVFMgUFJPRFVDVFMiLCJ0eG5Ub2tlbiI6IjgzNjBiOWEwZGFlYTQyMzU4N2YwZjNjNTlmM2U3MjgwMTc3NTc1Mjg2MDE2MyIsInZhcmlhYmxlTGVuZ3RoT3RwU3VwcG9ydGVkIjoiZmFsc2UiLCJjdXN0SWQiOiJOLkEiLCJwYXl0bU1lcmNoYW50SWQiOiJxYTEyaWQ0MDAxMDgxMzIzNzU0MiIsImlzT2ZmbGluZVR4biI6ImZhbHNlIiwibWVyY2hhbnRUeXBlIjoiT0ZGX1VTIiwibmF0aXZlSnNvblJlcXVlc3QiOiJ0cnVlIn0=\",\"verificationType\":\"\"}";

    private static final String DEFAULT_LIMIT_EXTEND_INFO =
            "{\"UPI_PPIWALLET\":\"{\\\"status\\\":\\\"NOT_EXCEED\\\",\\\"velocityType\\\":null}\",\"UPI_CREDITLINE\":\"{\\\"status\\\":\\\"NOT_EXCEED\\\",\\\"velocityType\\\":\\\"TXN_AMOUNT\\\"}\",\"dtc\":\"0\",\"PL_UPI\":\"{\\\"status\\\":\\\"NOT_EXCEED\\\",\\\"velocityType\\\":null}\",\"UPI_CC\":\"{\\\"status\\\":\\\"NOT_EXCEED\\\",\\\"velocityType\\\":null}\",\"PL_UPI_CC\":\"{\\\"status\\\":\\\"NOT_EXCEED\\\",\\\"velocityType\\\":null}\",\"PL_PER_MID\":\"{\\\"status\\\":\\\"NOT_EXCEED\\\",\\\"velocityType\\\":null}\",\"PL_UPI_CREDITLINE\":\"{\\\"status\\\":\\\"NOT_EXCEED\\\",\\\"velocityType\\\":null}\"}";

    private static final String DEFAULT_BROWSER_UA =
            "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Mobile Safari/537.36";

    @JsonProperty("version")
    private String version;
    @JsonProperty("appId")
    private String appId;
    @JsonProperty("reqTime")
    private String reqTime;
    @JsonProperty("function")
    private String function;
    @JsonProperty("transId")
    private String transId;
    @JsonProperty("extSerialNo")
    private String extSerialNo;
    @JsonProperty("cashierRequestId")
    private String cashierRequestId;
    @JsonProperty("merchantInfo")
    private UpiPaymentMerchantInfo merchantInfo;
    @JsonProperty("virtualPaymentAddr")
    private String virtualPaymentAddr;
    @JsonProperty("ipAddress")
    private String ipAddress;
    @JsonProperty("exchangeCurrency")
    private String exchangeCurrency;
    @JsonProperty("exchangeAmount")
    private String exchangeAmount;
    @JsonProperty("createdTime")
    private String createdTime;
    @JsonProperty("payTime")
    private String payTime;
    @JsonProperty("terminalType")
    private String terminalType;
    @JsonProperty("extendInfo")
    private String extendInfo;
    @JsonProperty("browserUserAgent")
    private String browserUserAgent;
    @JsonProperty("limitExtendInfo")
    private String limitExtendInfo;
    @JsonProperty("mbidConfigurationDetails")
    private UpiMbidConfigurationDetails mbidConfigurationDetails;
    @JsonProperty("transactionRetried")
    private String transactionRetried;
    @JsonProperty("oldPGMerchantId")
    private String oldPGMerchantId;

    public UpiPaymentRequestDTO() {
    }

    private UpiPaymentRequestDTO(Builder b) {
        this.version = b.version;
        this.appId = b.appId;
        this.reqTime = b.reqTime;
        this.function = b.function;
        this.transId = b.transId;
        this.extSerialNo = b.extSerialNo;
        this.cashierRequestId = b.cashierRequestId;
        this.merchantInfo = b.merchantInfo;
        this.virtualPaymentAddr = b.virtualPaymentAddr;
        this.ipAddress = b.ipAddress;
        this.exchangeCurrency = b.exchangeCurrency;
        this.exchangeAmount = b.exchangeAmount;
        this.createdTime = b.createdTime;
        this.payTime = b.payTime;
        this.terminalType = b.terminalType;
        this.extendInfo = b.extendInfo;
        this.browserUserAgent = b.browserUserAgent;
        this.limitExtendInfo = b.limitExtendInfo;
        this.mbidConfigurationDetails = b.mbidConfigurationDetails;
        this.transactionRetried = b.transactionRetried;
        this.oldPGMerchantId = b.oldPGMerchantId;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getExtSerialNo() {
        return extSerialNo;
    }

    public void setExtSerialNo(String extSerialNo) {
        this.extSerialNo = extSerialNo;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public UpiPaymentMerchantInfo getMerchantInfo() {
        return merchantInfo;
    }

    public void setMerchantInfo(UpiPaymentMerchantInfo merchantInfo) {
        this.merchantInfo = merchantInfo;
    }

    public String getVirtualPaymentAddr() {
        return virtualPaymentAddr;
    }

    public void setVirtualPaymentAddr(String virtualPaymentAddr) {
        this.virtualPaymentAddr = virtualPaymentAddr;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getExchangeCurrency() {
        return exchangeCurrency;
    }

    public void setExchangeCurrency(String exchangeCurrency) {
        this.exchangeCurrency = exchangeCurrency;
    }

    public String getExchangeAmount() {
        return exchangeAmount;
    }

    public void setExchangeAmount(String exchangeAmount) {
        this.exchangeAmount = exchangeAmount;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getBrowserUserAgent() {
        return browserUserAgent;
    }

    public void setBrowserUserAgent(String browserUserAgent) {
        this.browserUserAgent = browserUserAgent;
    }

    public String getLimitExtendInfo() {
        return limitExtendInfo;
    }

    public void setLimitExtendInfo(String limitExtendInfo) {
        this.limitExtendInfo = limitExtendInfo;
    }

    public UpiMbidConfigurationDetails getMbidConfigurationDetails() {
        return mbidConfigurationDetails;
    }

    public void setMbidConfigurationDetails(UpiMbidConfigurationDetails mbidConfigurationDetails) {
        this.mbidConfigurationDetails = mbidConfigurationDetails;
    }

    public String getTransactionRetried() {
        return transactionRetried;
    }

    public void setTransactionRetried(String transactionRetried) {
        this.transactionRetried = transactionRetried;
    }

    public String getOldPGMerchantId() {
        return oldPGMerchantId;
    }

    public void setOldPGMerchantId(String oldPGMerchantId) {
        this.oldPGMerchantId = oldPGMerchantId;
    }

    public static class Builder {
        private String version = "1.1.2";
        private String appId = "PTYLC1IN07";
        private String reqTime;
        private String function = "pg.router.paytm.upi.payment.request";
        private String transId = "20260409111760000249123579967889569";
        private String extSerialNo;
        private String cashierRequestId = "202604090dbe01deeec014c6fb68b0a2f3542a782theia8697778fccwcjgq0007013";
        private UpiPaymentMerchantInfo merchantInfo;
        private String virtualPaymentAddr = "dummyvpa@upi";
        private String ipAddress = "125.22.67.162";
        private String exchangeCurrency = "INR";
        private String exchangeAmount = "5000";
        private String createdTime;
        private String payTime;
        private String terminalType = "WAP";
        private String extendInfo = DEFAULT_EXTEND_INFO;
        private String browserUserAgent = DEFAULT_BROWSER_UA;
        private String limitExtendInfo = DEFAULT_LIMIT_EXTEND_INFO;
        private UpiMbidConfigurationDetails mbidConfigurationDetails;
        private String transactionRetried = "false";
        private String oldPGMerchantId = "qa12id40010813237542";

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setReqTime(String reqTime) {
            this.reqTime = reqTime;
            return this;
        }

        public Builder setFunction(String function) {
            this.function = function;
            return this;
        }

        public Builder setTransId(String transId) {
            this.transId = transId;
            return this;
        }

        public Builder setExtSerialNo(String extSerialNo) {
            this.extSerialNo = extSerialNo;
            return this;
        }

        public Builder setCashierRequestId(String cashierRequestId) {
            this.cashierRequestId = cashierRequestId;
            return this;
        }

            public Builder setMerchantInfo(UpiPaymentMerchantInfo merchantInfo) {
            this.merchantInfo = merchantInfo;
            return this;
        }

        public Builder setVirtualPaymentAddr(String virtualPaymentAddr) {
            this.virtualPaymentAddr = virtualPaymentAddr;
            return this;
        }

        public Builder setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder setExchangeCurrency(String exchangeCurrency) {
            this.exchangeCurrency = exchangeCurrency;
            return this;
        }

        public Builder setExchangeAmount(String exchangeAmount) {
            this.exchangeAmount = exchangeAmount;
            return this;
        }

        public Builder setCreatedTime(String createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder setPayTime(String payTime) {
            this.payTime = payTime;
            return this;
        }

        public Builder setTerminalType(String terminalType) {
            this.terminalType = terminalType;
            return this;
        }

        public Builder setExtendInfo(String extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public Builder setBrowserUserAgent(String browserUserAgent) {
            this.browserUserAgent = browserUserAgent;
            return this;
        }

        public Builder setLimitExtendInfo(String limitExtendInfo) {
            this.limitExtendInfo = limitExtendInfo;
            return this;
        }

        public Builder setMbidConfigurationDetails(UpiMbidConfigurationDetails mbidConfigurationDetails) {
            this.mbidConfigurationDetails = mbidConfigurationDetails;
            return this;
        }

        public Builder setTransactionRetried(String transactionRetried) {
            this.transactionRetried = transactionRetried;
            return this;
        }

        public Builder setOldPGMerchantId(String oldPGMerchantId) {
            this.oldPGMerchantId = oldPGMerchantId;
            return this;
        }

        public UpiPaymentRequestDTO build() {
            if (mbidConfigurationDetails == null) {
                mbidConfigurationDetails = new UpiMbidConfigurationDetails.Builder().build();
            }
            return new UpiPaymentRequestDTO(this);
        }
    }
}
