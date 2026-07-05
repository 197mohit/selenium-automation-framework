package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mode",
        "channels",
        "banks",
        "bins"
})
public class DisablePaymentMode {

    @JsonProperty("mode")
    private String mode;
    @JsonProperty("channels")
    private Object[] channels = null;
    @JsonProperty("banks")
    private String[] banks = null;
    @JsonProperty("bins")
    private String[] bins = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public DisablePaymentMode(String[] channels, String mode) {
        this.channels = channels;
        this.mode = mode;
    }

    public DisablePaymentMode() {
    }

    @JsonProperty("mode")
    public String getMode() {
        return mode;
    }

    @JsonProperty("mode")
    public DisablePaymentMode setMode(String mode) {
        this.mode = mode;
        return this;
    }

    @JsonProperty("channels")
    public Object[] getChannels() {
        return channels;
    }

    @JsonProperty("channels")
    public DisablePaymentMode setChannels(Object[] channels) {
        this.channels = channels;
        return this;
    }

    @JsonProperty("banks")
    public Object[] getBanks() {
        return banks;
    }

    @JsonProperty("banks")
    public DisablePaymentMode setBanks(String[] banks) {
        this.banks = banks;
        return this;
    }

    @JsonProperty("bins")
    public Object[] getBins() {
        return bins;
    }

    @JsonProperty("bins")
    public DisablePaymentMode setBins(String[] bins) {
        this.bins = bins;
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
}
