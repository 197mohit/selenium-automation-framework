package com.paytm.utils.merchant.dto.alipay.request;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientId",
        "function",
        "reserve",
        "clientSecret",
        "reqTime",
        "accessToken",
        "version",
        "reqMsgId"
})
public class Head {

    @JsonProperty("clientId")
    private String clientId = "2016030715243903536806";
    @JsonProperty("function")
    private String function = "alipayplus.dataservice.bizorder.search";
    @JsonProperty("reserve")
    private Reserve reserve = new Reserve();
    @JsonProperty("clientSecret")
    private String clientSecret = "ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5";
    @JsonProperty("reqTime")
    private String reqTime = "2016-08-23T14:43:00+05:30";
    @JsonProperty("accessToken")
    private String accessToken = "234567a";
    @JsonProperty("version")
    private String version = "fixed-a";
    @JsonProperty("reqMsgId")
    private String reqMsgId = "c45adea8b6a7465e8f9751227de7a1da";
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @JsonProperty("function")
    public String getFunction() {
        return function;
    }

    @JsonProperty("function")
    public void setFunction(String function) {
        this.function = function;
    }

    @JsonProperty("reserve")
    public Reserve getReserve() {
        return reserve;
    }

    @JsonProperty("reserve")
    public void setReserve(Reserve reserve) {
        this.reserve = reserve;
    }

    @JsonProperty("clientSecret")
    public String getClientSecret() {
        return clientSecret;
    }

    @JsonProperty("clientSecret")
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @JsonProperty("reqTime")
    public String getReqTime() {
        return reqTime;
    }

    @JsonProperty("reqTime")
    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }

    @JsonProperty("accessToken")
    public String getAccessToken() {
        return accessToken;
    }

    @JsonProperty("accessToken")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("reqMsgId")
    public String getReqMsgId() {
        return reqMsgId;
    }

    @JsonProperty("reqMsgId")
    public void setReqMsgId(String reqMsgId) {
        this.reqMsgId = reqMsgId;
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
