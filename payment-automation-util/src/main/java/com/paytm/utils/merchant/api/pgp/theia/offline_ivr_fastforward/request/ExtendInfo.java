package com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtendInfo {

    @JsonProperty("key")
    private String key;

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

}
