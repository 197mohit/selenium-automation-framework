package com.paytm.dto.NativeDTO.InitTxn.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankuragarwal on 17/10/18
 */
public class Body {
    private ResultInfo resultInfo;
    private boolean authenticated;
    @JsonProperty("isPromoCodeValid")
    private Boolean isPromoCodeValid;
    private String txnToken;
    @JsonProperty("subscriptionId")
    private String subscriptionId;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Boolean isPromoCodeValid() {
        return isPromoCodeValid;
    }

    public void setPromoCodeValid(Boolean promoCodeValid) {
        isPromoCodeValid = promoCodeValid;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
