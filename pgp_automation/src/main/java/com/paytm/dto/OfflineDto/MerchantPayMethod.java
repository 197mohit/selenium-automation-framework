package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anjukumari on 18/02/19
 */


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "payMethod",
        "displayName",
        "isDisabled",
        "PayChannelOption",
        "onboarding"
})
public class MerchantPayMethod {

    @JsonProperty("payMethod")
    private String payMethod;
    @JsonProperty("displayName")
    private String displayName;
    @JsonProperty("isDisabled")
    private IsDisabled isDisabled;
    @JsonProperty("PayChannelOption")
    private List<PayChannelOption> payChannelOptions = null;
    @JsonProperty("onboarding")
    private Boolean onboarding;
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

    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("displayName")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("isDisabled")
    public IsDisabled getIsDisabled() {
        return isDisabled;
    }

    @JsonProperty("isDisabled")
    public void setIsDisabled(IsDisabled isDisabled) {
        this.isDisabled = isDisabled;
    }

    @JsonProperty("PayChannelOption")
    public List<PayChannelOption> getPayChannelOptions() {
        return payChannelOptions;
    }

    @JsonProperty("PayChannelOption")
    public void setPayChannelOptions(List<PayChannelOption> payChannelOptions) {
        this.payChannelOptions = payChannelOptions;
    }

    @JsonProperty("onboarding")
    public Boolean getOnboarding() {
        return onboarding;
    }

    @JsonProperty("onboarding")
    public void setOnboarding(Boolean onboarding) {
        this.onboarding = onboarding;
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