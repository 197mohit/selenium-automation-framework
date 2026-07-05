package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ankuragarwal on 16/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentModes {
    private String displayName;
    private String paymentMode;
    private List<PayChannelOptions> payChannelOptions;
    private IsDisabled isDisabled;
    private boolean onboarding;

    public boolean getOnboarding() {
        return onboarding;
    }

    public void setOnboarding(boolean onboarding) {
        this.onboarding = onboarding;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<PayChannelOptions> getPayChannelOptions() {
        return payChannelOptions;
    }

    public void setPayChannelOptions(List<PayChannelOptions> payChannelOptions) {
        this.payChannelOptions = payChannelOptions;
    }

    public IsDisabled getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(IsDisabled isDisabled) {
        this.isDisabled = isDisabled;
    }
}
