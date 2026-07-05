package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankuragarwal on 16/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayChannelOptions {
    private IsDisabled isDisabled;
    private HasLowSuccess hasLowSuccess;
    private String iconUrl;
    private BalanceInfo balanceInfo;
    @JsonProperty("isHybridDisabled")
    private boolean isHybridDisabled;
    private String channelCode;
    private String channelName;

    @JsonProperty("isHybridDisabled")
    public boolean getIsHybridDisabled() {
        return isHybridDisabled;
    }

    @JsonProperty("isHybridDisabled")
    public void setIsHybridDisabled(boolean hybridDisabled) {
        isHybridDisabled = hybridDisabled;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public IsDisabled getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(IsDisabled isDisabled) {
        this.isDisabled = isDisabled;
    }

    public HasLowSuccess getHasLowSuccess() {
        return hasLowSuccess;
    }

    public void setHasLowSuccess(HasLowSuccess hasLowSuccess) {
        this.hasLowSuccess = hasLowSuccess;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public BalanceInfo getBalanceInfo() {
        return balanceInfo;
    }

    public void setBalanceInfo(BalanceInfo balanceInfo) {
        this.balanceInfo = balanceInfo;
    }
}
