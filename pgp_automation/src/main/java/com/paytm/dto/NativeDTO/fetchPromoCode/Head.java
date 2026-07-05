
package com.paytm.dto.NativeDTO.fetchPromoCode;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "requestTimestamp",
        "channelId",
        "txnToken"
})
public class Head {

    @JsonProperty("version")
    private String version;
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("txnToken")
    private String txnToken;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Head(String version, String requestTimestamp, String channelId, String txnToken) {
        this.version = version;
        this.requestTimestamp = requestTimestamp;
        this.channelId = channelId;
        this.txnToken = txnToken;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("requestTimestamp")
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("channelId")
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @JsonProperty("txnToken")
    public String getTxnToken() {
        return txnToken;
    }

    @JsonProperty("txnToken")
    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
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
