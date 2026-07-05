package com.paytm.api.upipsp.externalOrderPayUPIPspDTO;


import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "header",
            "body"
    })
    @Generated("jsonschema2pojo")
    public class OrderPayUpiPsp {

        @JsonProperty("header")
        private Header header;
        @JsonProperty("body")
        private Body body;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        @JsonProperty("header")
        public Header getHeader() {
            return header;
        }

        @JsonProperty("header")
        public void setHeader(Header header) {
            this.header = header;
        }

        @JsonProperty("body")
        public Body getBody() {
            return body;
        }

        @JsonProperty("body")
        public void setBody(Body body) {
            this.body = body;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

        OrderPayUpiPsp(Builder builder){
             this.header= new Header().setRequestTimeStamp(builder.requestTimeStamp).setSignature(builder.signature);
             this.body= new Body().setBankCode(builder.bankCode).setPosId(builder.posId).setTxnStatus(builder.txnStatus).setTxnDate(builder.txnDate).setOrderId(builder.orderId).setPayeeVpa(builder.payeeVpa).setMid(builder.mid).setBankTxnId(builder.bankTxnId).setRefId(builder.refId).setPayerVpa(builder.payerVpa).setTxnAmount(builder.txnAmount);
        }

        public static class Builder{
            private String requestTimeStamp;
            private String signature;
            private String bankCode;
            private String posId;
            private String txnStatus;
            private String txnDate;
            private String orderId;
            private String payeeVpa;
            private String mid;
            private String bankTxnId;
            private String refId;
            private String payerVpa;
            private String txnAmount;

            public Builder setRequestTimeStamp(String requestTimeStamp) {
                this.requestTimeStamp = requestTimeStamp;
                return this;
            }

            public Builder setSignature(String signature) {
                this.signature = signature;
                return this;
            }

            public Builder setBankCode(String bankCode) {
                this.bankCode = bankCode;
                return this;
            }

            public Builder setPosId(String posId) {
                this.posId = posId;
                return this;
            }

            public Builder setTxnStatus(String txnStatus) {
                this.txnStatus = txnStatus;
                return this;
            }

            public Builder setTxnDate(String txnDate) {
                this.txnDate = txnDate;
                return this;
            }

            public Builder setOrderId(String orderId) {
                this.orderId = orderId;
                return this;
            }

            public Builder setPayeeVpa(String payeeVpa) {
                this.payeeVpa = payeeVpa;
                return this;
            }

            public Builder setMid(String mid) {
                this.mid = mid;
                return this;
            }

            public Builder setBankTxnId(String bankTxnId) {
                this.bankTxnId = bankTxnId;
                return this;
            }

            public Builder setRefId(String refId) {
                this.refId = refId;return this;

            }

            public Builder setPayerVpa(String payerVpa) {
                this.payerVpa = payerVpa;
                return this;
            }

            public Builder setTxnAmount(String txnAmount) {
                this.txnAmount = txnAmount;return this;

            }

            public OrderPayUpiPsp build(){
                return new OrderPayUpiPsp(this);
            }
        }

    }

