package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "arpc",
})
public class UpiLiteResponseData {

    @JsonProperty("arpc")
    private String arpc;

    @JsonProperty("arpc")
    public String getArpc() {
        return arpc;
    }

    @JsonProperty("arpc")
    public void setArpc(String arpc) {
        this.arpc = arpc;
    }

}