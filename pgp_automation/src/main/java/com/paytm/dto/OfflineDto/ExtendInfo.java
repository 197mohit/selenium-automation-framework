package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by anjukumari on 04/12/18
 */
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
