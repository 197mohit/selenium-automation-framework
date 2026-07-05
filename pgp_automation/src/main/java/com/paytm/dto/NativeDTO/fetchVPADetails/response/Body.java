package com.paytm.dto.NativeDTO.fetchVPADetails.response;


public class Body {

    private boolean isEmiAvailable;
    private String iconUrl;
    private String emiChannel;
    private ResultInfo resultInfo;
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

    public String getEmiChannel() {
        return emiChannel;
    }

    public void setEmiChannel(String emiChannel) {
        this.emiChannel = emiChannel;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
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
