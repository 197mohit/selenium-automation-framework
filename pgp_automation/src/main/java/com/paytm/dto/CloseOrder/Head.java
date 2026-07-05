package com.paytm.dto.CloseOrder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "sigature",
        "channelId"
})
public class Head {

    @JsonProperty("version")
    private String version;
    @JsonProperty("sigature")
    private String sigature;
    @JsonProperty("channelId")
    private String channelId;

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public Head setVersion(String version) {
        this.version = version;
        return this;
    }

    @JsonProperty("sigature")
    public String getSigature() {
        return sigature;
    }

    @JsonProperty("sigature")
    public Head setSigature(String sigature) {
        this.sigature = sigature;
        return this;
    }

    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("channelId")
    public Head setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

}