package com.paytm.dto.HANDLERINTERNALTXNSTATUS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "key"
})
public class ExtendInfo {

    public  ExtendInfo(){
        this.key = "value";
    }

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