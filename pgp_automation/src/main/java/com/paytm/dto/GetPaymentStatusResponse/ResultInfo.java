package com.paytm.dto.GetPaymentStatusResponse;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultStatus",
        "resultCode",
        "resultMsg"
})
public class ResultInfo {

    @JsonProperty("resultStatus")
    private String resultStatus;
    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultMsg")
    private String resultMsg;

    @JsonProperty("resultStatus")
    public String getResultStatus() {
        return resultStatus;
    }

    @JsonProperty("resultStatus")
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    @JsonProperty("resultCode")
    public String getResultCode() {
        return resultCode;
    }

    @JsonProperty("resultCode")
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @JsonProperty("resultMsg")
    public String getResultMsg() {
        return resultMsg;
    }

    @JsonProperty("resultMsg")
    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

}