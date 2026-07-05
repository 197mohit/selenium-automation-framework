package com.paytm.dto.PromoDTO.ApplyPromoDTO;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "requestId",
        "requestTimestamp",
        "channelId",
        "version",
        "tokenType",
        "token"
})
public class Head
{
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("requestTimestamp")
    private String requestTimestamp;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("tokenType")
    private String tokenType;
    @JsonProperty("token")
    private String token;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 5264834651018171703L;

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Head setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    @JsonProperty("requestTimestamp")
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @JsonProperty("requestTimestamp")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Head setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("channelId")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Head setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Head setVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("tokenType")
    public String getTokenType() {
        return tokenType;
    }

    @JsonProperty("tokenType")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Head setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public com.paytm.dto.PromoDTO.ApplyPromoDTO.Head setToken(String token) {
        this.token = token;
        return this;
    }

    /*@JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/


}
