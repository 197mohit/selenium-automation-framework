package com.paytm.dto.NativeDTO.fetchPromoCode;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

public class FetchPromoCodeDTO {


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"body", "head"})
    //@JsonPropertyOrder({"head"})

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("head")
    private com.paytm.dto.NativeDTO.fetchPromoCode.Head head;
    @JsonProperty("body")
    private com.paytm.dto.NativeDTO.fetchPromoCode.Body body;

    private FetchPromoCodeDTO(Builder builder) {
        this.body = new Body(builder.txnType, builder.cardNumber, builder.bankCode, builder.isEnhancedFlow);
        this.head = new Head(builder.version, builder.requestTimestamp, builder.channelId, builder.txnToken);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("head")
    public com.paytm.dto.NativeDTO.fetchPromoCode.Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(com.paytm.dto.NativeDTO.fetchPromoCode.Head head) {
        this.head = head;
    }

    @JsonProperty("body")
    public com.paytm.dto.NativeDTO.fetchPromoCode.Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(com.paytm.dto.NativeDTO.fetchPromoCode.Body body) {
        this.body = body;
    }

    public static class Builder {

        private String channelId;
        private String txnToken;
        private String cardNumber;
        private String txnType;
        private String requestTimestamp;
        private String version;
        private String bankCode;
        private String isEnhancedFlow;


        public Builder(String txnToken, String trxType) {
            this.channelId = "WEB";
            this.requestTimestamp = "Time";
            this.txnToken = txnToken;
            this.version = "v1";
            this.txnType = trxType;
        }

        public String getVersion() {
            return version;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public Builder setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public String getbankCode() {
            return this.bankCode;
        }

        public Builder setBankCode(String bankCode) {
            this.bankCode = bankCode;
            return this;
        }

        public String getIsEnhancedFlow() {
            return isEnhancedFlow;
        }

        public Builder setIsEnhancedFlow(String isEnhancedFlow) {
            this.isEnhancedFlow = isEnhancedFlow;
            return this;
        }

        public String getTxnType() {
            return txnType;
        }

        public Builder setTxnType(String txnType) {
            this.txnType = txnType;
            return this;
        }

        public String getRequestTimestamp() {
            return requestTimestamp;
        }

        public Builder setRequestTimestamp(String requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        public String getChannelId() {
            return channelId;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public String getTxnToken() {
            return txnToken;
        }

        public Builder setTxnToken(String txnToken) {
            this.txnToken = txnToken;
            return this;
        }

        public FetchPromoCodeDTO build() {
            return new FetchPromoCodeDTO(this);
        }
    }

}
