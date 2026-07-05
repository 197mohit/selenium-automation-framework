package com.paytm.dto.NativeDTO.fetchBinDetails.response;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.HasLowSuccess;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.IsDisabled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "isDisabled",
        "hasLowSuccess",
        "iconUrl",
        "emiChannelInfos",
        "minAmount",
        "maxAmount",
        "emiType",
        "isHybridDisabled",
        "channelCode",
        "channelName"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiChannel {

    @JsonProperty("isDisabled")
    private IsDisabled isDisabled;
    @JsonProperty("hasLowSuccess")
    private HasLowSuccess hasLowSuccess;
    @JsonProperty("iconUrl")
    private String iconUrl;
    @JsonProperty("emiChannelInfos")
    private List<EmiChannelInfo> emiChannelInfos = null;
    @JsonProperty("minAmount")
    private MinAmount minAmount;
    @JsonProperty("maxAmount")
    private MaxAmount maxAmount;
    @JsonProperty("emiType")
    private String emiType;
    @JsonProperty("isHybridDisabled")
    private Boolean isHybridDisabled;
    @JsonProperty("channelCode")
    private String channelCode;
    @JsonProperty("channelName")
    private String channelName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("isDisabled")
    public IsDisabled getIsDisabled() {
        return isDisabled;
    }

    @JsonProperty("isDisabled")
    public void setIsDisabled(IsDisabled isDisabled) {
        this.isDisabled = isDisabled;
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

    @JsonProperty("emiChannelInfos")
    public List<EmiChannelInfo> getEmiChannelInfos() {
        return emiChannelInfos;
    }

    @JsonProperty("emiChannelInfos")
    public void setEmiChannelInfos(List<EmiChannelInfo> emiChannelInfos) {
        this.emiChannelInfos = emiChannelInfos;
    }

    @JsonProperty("minAmount")
    public MinAmount getMinAmount() {
        return minAmount;
    }

    @JsonProperty("minAmount")
    public void setMinAmount(MinAmount minAmount) {
        this.minAmount = minAmount;
    }

    @JsonProperty("maxAmount")
    public MaxAmount getMaxAmount() {
        return maxAmount;
    }

    @JsonProperty("maxAmount")
    public void setMaxAmount(MaxAmount maxAmount) {
        this.maxAmount = maxAmount;
    }

    @JsonProperty("emiType")
    public String getEmiType() {
        return emiType;
    }

    @JsonProperty("emiType")
    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    @JsonProperty("isHybridDisabled")
    public Boolean getIsHybridDisabled() {
        return isHybridDisabled;
    }

    @JsonProperty("isHybridDisabled")
    public void setIsHybridDisabled(Boolean isHybridDisabled) {
        this.isHybridDisabled = isHybridDisabled;
    }

    @JsonProperty("channelCode")
    public String getChannelCode() {
        return channelCode;
    }

    @JsonProperty("channelCode")
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    @JsonProperty("channelName")
    public String getChannelName() {
        return channelName;
    }

    @JsonProperty("channelName")
    public void setChannelName(String channelName) {
        this.channelName = channelName;
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
