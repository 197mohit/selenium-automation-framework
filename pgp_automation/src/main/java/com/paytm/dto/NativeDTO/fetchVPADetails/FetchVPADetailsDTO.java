package com.paytm.dto.NativeDTO.fetchVPADetails;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

/*
@author Rahul Mendiratta
 */

public class FetchVPADetailsDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"body", "head"})
    //@JsonPropertyOrder({"head"})

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    private FetchVPADetailsDTO(Builder builder) {
        this.body = new Body();
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

    public static class Builder {

        private String version;
        private String requestTimestamp;
        private String channelId;
        private String txnToken;

        public Builder(String txnToken) {
            this.channelId = "WEB";
            this.requestTimestamp = "Time";
            this.txnToken = txnToken;
            this.version = "v1";
        }

        public String getVersion() {
            return version;
        }

        public Builder setVersion(String version) {
            this.version = version;
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

        public FetchVPADetailsDTO build() {
            return new FetchVPADetailsDTO(this);
        }

    }
}
