package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "payMethod",
        "payChannelOption",
        "isDisabled",
        "hasLowSuccess",
        "iconUrl",
        "balanceInfo",
        "instId",
        "instName"
})

public class PayChannelOption {

    @JsonProperty("payMethod")
    private String payMethod;
    @JsonProperty("payChannelOption")
    private String payChannelOption;
    @JsonProperty("isDisabled")
    private IsDisabled isDisabled;
    @JsonProperty("hasLowSuccess")
    private HasLowSuccess hasLowSuccess;
    @JsonProperty("iconUrl")
    private String iconUrl;
    @JsonProperty("balanceInfo")
    private Object balanceInfo;
    @JsonProperty("instId")
    private String instId;
    @JsonProperty("instName")
    private String instName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("payMethod")
    public String getPayMethod() {
        return payMethod;
    }

    @JsonProperty("payMethod")
    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    @JsonProperty("payChannelOption")
    public String getPayChannelOption() {
        return payChannelOption;
    }

    @JsonProperty("payChannelOption")
    public void setPayChannelOption(String payChannelOption) {
        this.payChannelOption = payChannelOption;
    }


    @JsonProperty("hasLowSuccess")
    public HasLowSuccess getHasLowSuccess() {
        return hasLowSuccess;
    }

    @JsonProperty("hasLowSuccess")
    public void setHasLowSuccess(HasLowSuccess hasLowSuccess) {
        this.hasLowSuccess = hasLowSuccess;
    }

    @JsonProperty("iconUrl")
    public String getIconUrl() {
        return iconUrl;
    }

    @JsonProperty("iconUrl")
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @JsonProperty("balanceInfo")
    public Object getBalanceInfo() {
        return balanceInfo;
    }

    @JsonProperty("balanceInfo")
    public void setBalanceInfo(Object balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    @JsonProperty("instId")
    public String getInstId() {
        return instId;
    }

    @JsonProperty("instId")
    public void setInstId(String instId) {
        this.instId = instId;
    }

    @JsonProperty("instName")
    public String getInstName() {
        return instName;
    }

    @JsonProperty("instName")
    public void setInstName(String instName) {
        this.instName = instName;
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