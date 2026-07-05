
package com.paytm.dto.NativeDTO.RenewSubscription;

import com.fasterxml.jackson.annotation.*;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "head",
    "body"
})
public class RenewSubscriptionDTO {

    public RenewSubscriptionDTO(Builder builder){
        this.body=new Body(builder.mid,builder.orderId,builder.subscriptionId,new TxnAmount(builder.txnAmount), builder.requestType,builder.debitDate);
        this.head=new Head(builder.version,builder.requestTimestamp,builder.channelId,builder.signature);
    }

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
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


    public static class Builder {

        private String mid;
        private String orderId;
        private String txnAmount;
        private String subscriptionId;
        private String additionalInfo;
        private String requestTimestamp;
        private String version;
        private String channelId;
        private String signature;
        private String clientId;
        private String merchantKey;
        private String requestType;
        private String debitDate;

        public Builder(String mid,String subscriptionId,String txnAmount){
            this.mid =mid;
            this.orderId= CommonHelpers.generateOrderId();
            this.subscriptionId=subscriptionId;
            this.channelId = "WEB";
            this.signature = "CH";
            this.clientId = "C11";
            this.version = "v1";
            this.txnAmount=txnAmount;
        }

        public Builder(String mid,String subscriptionId,String txnAmount,String debitDate){
            this.mid =mid;
            this.orderId= CommonHelpers.generateOrderId();
            this.subscriptionId=subscriptionId;
            this.channelId = "WEB";
            this.signature = "CH";
            this.clientId = "C11";
            this.version = "v1";
            this.txnAmount=txnAmount;
            this.debitDate=debitDate;
        }


        public String getRequestType() {
            return requestType;
        }

        public Builder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public String getClientId() {
            return clientId;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public String getMerchantKey() {
            return merchantKey;
        }

        public Builder setMerchantKey(String merchantKey) {
            this.merchantKey = merchantKey;
            return this;
        }

        public String getDebitDate() {
            return debitDate;
        }

        public Builder setDebitDate(String debitDate) {
            this.debitDate = debitDate;
            return this;
        }

        public String getSignature() {
            return signature;
        }

        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }


        public String getChannelId() {
            return channelId;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public String getRequestTimestamp() {
            return requestTimestamp;
        }

        public Builder setRequestTimestamp(String requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getMid() {
            return mid;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public String getOrderId() {
            return orderId;
        }

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public String getTxnAmount() {
            return txnAmount;
        }

        public Builder setTxnAmount(String txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public Builder setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
            return this;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }

        public Builder setAdditionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
            return this;
        }

        /*public RenewSubscriptionDTO build() {
         return new RenewSubscriptionDTO(this);
        }*/

        public RenewSubscriptionDTO build() {
            RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO(this);
            String checksum = PGPHelpers.getNativeChecksum(this.merchantKey, renewSubscriptionDTO.getBody());
            renewSubscriptionDTO.getHead().setSignature(checksum);
            return renewSubscriptionDTO;
        }

    }
}
