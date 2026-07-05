
package com.paytm.dto.NativeDTO.CancelSubscription;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "head",
    "body"
})
public class CancelSubscriptionDTO {

    public CancelSubscriptionDTO(Builder builder){
        this.body = new Body(builder.mid,builder.subscriptionId,builder.ssoToken);
        this.head = new Head(builder.version,builder.requestTimestamp,builder.tokenType,
                        builder.clientId,builder.signature);
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

    public static class Builder{

        public Builder(String mid,String subscriptionId,String ssoToken,String signature){
            this.mid =mid;
            this.subscriptionId=subscriptionId;
            this.signature = signature;
            this.clientId = "C11";
            this.version = "v1";
            this.ssoToken=ssoToken;
            this.tokenType="SSO";
            this.requestTimestamp="Time";
        }

        private String tokenType;
        private String ssoToken;
        private String mid;
        private String subscriptionId;
        private String requestTimestamp;
        private String version;
        private String signature;
        private String clientId;


        public String getTokenType() {
            return tokenType;
        }

        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }


        public String getSsoToken() {
            return ssoToken;
        }

        public Builder setSsoToken(String ssoToken) {
            this.ssoToken = ssoToken;
            return this;
        }

        public String getMid() {
            return mid;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public Builder setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
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

        public String getSignature() {
            return signature;
        }

        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public String getClientId() {
            return clientId;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public CancelSubscriptionDTO build(){
            return new CancelSubscriptionDTO(this);
        }

    }

}
