package com.paytm.dto.NativeDTO.fetchBinDetails.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 23/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Body {

    private boolean isEmiAvailable;
    private String iconUrl;
    private EmiChannel emiChannel;
    private ResultInfo resultInfo;
    private BinDetail binDetail;
    private String[] authModes;
    private HasLowSuccessRate hasLowSuccessRate;

    public boolean isEmiAvailable() {
        return isEmiAvailable;
    }

    public void setEmiAvailable(boolean emiAvailable) {
        isEmiAvailable = emiAvailable;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public EmiChannel getEmiChannel() {
        return emiChannel;
    }

    public void setEmiChannel(EmiChannel emiChannel) {
        this.emiChannel = emiChannel;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public BinDetail getBinDetail() {
        return binDetail;
    }

    public void setBinDetail(BinDetail binDetail) {
        this.binDetail = binDetail;
    }

    public String[] getAuthModes() {
        return authModes;
    }

    public void setAuthModes(String[] authModes) {
        this.authModes = authModes;
    }

    public HasLowSuccessRate getHasLowSuccessRate() {
        return hasLowSuccessRate;
    }

    public void setHasLowSuccessRate(HasLowSuccessRate hasLowSuccessRate) {
        this.hasLowSuccessRate = hasLowSuccessRate;
    }
}
