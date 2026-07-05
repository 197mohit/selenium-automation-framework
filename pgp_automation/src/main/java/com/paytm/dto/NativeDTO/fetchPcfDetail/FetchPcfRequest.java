package com.paytm.dto.NativeDTO.fetchPcfDetail;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anjukumari on 14/05/19
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})

public class FetchPcfRequest {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public FetchPcfRequest() {}

    private FetchPcfRequest(Builder builder) {
        this.body = new Body().setPayMethods(builder.payMethods)
                .setMid(builder.mid)
                .setTxnAmount(builder.txnAmount);
        this.head = new Head().setTxnToken(builder.txnToken)
                .setChannelId(builder.channelId)
                .setToken(builder.token)
                .setTokenType(builder.tokenType)
                .setVersion(builder.version);
    }

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public FetchPcfRequest setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public FetchPcfRequest setBody(Body body) {
        this.body = body;
        return this;
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
        private String txnToken;
        private String token;
        private String tokenType;
        private String version;
        private String channelId;
        private String mid;
        private String txnAmount;
        private List<PayMethod> payMethods;

        public Builder() {
            this.version = "8.13.2";
            this.channelId = "APP";
        }

        public Builder(String token, String tokenType){
            this();
            this.token = token;
            this.tokenType = tokenType;
        }

        public Builder setTxnToken(String txnToken) {
            this.txnToken = txnToken;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setTxnAmount(String txnAmount) {
            this.txnAmount = txnAmount;
            return this;
        }

        public Builder setPayMethods(List<PayMethod> payMethods) {
            this.payMethods = payMethods;
            return this;
        }

        public FetchPcfRequest build() {
            return new FetchPcfRequest(this);
        }
    }


    public FetchPcfRequest setPaymethodsInBody(List<PayMethod> paymethodList) {
        this.getBody().setPayMethods(paymethodList);
        return this;
    }

    public FetchPcfRequest setTxnTokenInHead(String token) {
        this.getHead().setTxnToken(token);
        return this;
    }

}