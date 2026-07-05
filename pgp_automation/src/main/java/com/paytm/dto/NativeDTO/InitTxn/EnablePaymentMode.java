package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mode",
        "cardExpiry",
        "channels",
        "banks",
        "bins"
})
public class EnablePaymentMode {

    @JsonProperty("mode")
    private String mode;
    @JsonProperty("cardExpiry")
    private String cardExpiry;
    @JsonProperty("channels")
    private Object[] channels = null;
    @JsonProperty("banks")
    private String[] banks = null;
    @JsonProperty("bins")
    private String[] bins = null;
    @JsonProperty("subTypes")
    private String[] subTypes = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public EnablePaymentMode(String[] channels, String mode) {
        this.channels = channels;
        this.mode = mode;
    }

    public EnablePaymentMode(String[] channels, String mode, String[] banks) {
        this.channels = channels;
        this.mode = mode;
        this.banks = banks;
    }

    public EnablePaymentMode(String[] channels, String mode, String[] banks,String[] subTypes) {
        this.channels = channels;
        this.mode = mode;
        this.banks = banks;
        this.subTypes = subTypes;
    }

    public EnablePaymentMode(String mode,String cardExpiry){
        this.mode = mode;
        this.cardExpiry=cardExpiry;
    }

    public EnablePaymentMode() {
    }

    @JsonProperty("mode")
    public String getMode() {
        return mode;
    }

    @JsonProperty("mode")
    public EnablePaymentMode setMode(String mode) {
        this.mode = mode;
        return this;
    }

    @JsonProperty("cardExpiry")
    public String getcardExpiry() {
        return cardExpiry;
    }
    @JsonProperty("cardExpiry")
    public EnablePaymentMode setcardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
        return this;
    }

    @JsonProperty("channels")
    public Object[] getChannels() {
        return channels;
    }

    @JsonProperty("channels")
    public EnablePaymentMode setChannels(Object[] channels) {
        this.channels = channels;
        return this;
    }

    @JsonProperty("banks")
    public Object[] getBanks() {
        return banks;
    }

    @JsonProperty("banks")
    public EnablePaymentMode setBanks(String[] banks) {
        this.banks = banks;
        return this;
    }

    @JsonProperty("subTypes")
    public Object[] getSubTypes() {
        return subTypes;
    }

    @JsonProperty("subTypes")
    public EnablePaymentMode setSubTypes(String[] subTypes) {
        this.subTypes = subTypes;
        return this;
    }

    @JsonProperty("bins")
    public Object[] getBins() {
        return bins;
    }

    @JsonProperty("bins")
    public EnablePaymentMode setBins(String[] bins) {
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
