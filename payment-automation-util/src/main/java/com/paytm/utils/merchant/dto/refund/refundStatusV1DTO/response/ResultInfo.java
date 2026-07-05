package com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response;

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
        "resultMsg",
        "bankRetry",
        "retry"
})
public class ResultInfo {

    @JsonProperty("resultStatus")
    private String resultStatus;
    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultMsg")
    private String resultMsg;
    @JsonProperty("bankRetry")
    private Object bankRetry;
    @JsonProperty("retry")
    private Object retry;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    @JsonProperty("bankRetry")
    public Object getBankRetry() {
        return bankRetry;
    }

    @JsonProperty("bankRetry")
    public void setBankRetry(Object bankRetry) {
        this.bankRetry = bankRetry;
    }

    @JsonProperty("retry")
    public Object getRetry() {
        return retry;
    }

    @JsonProperty("retry")
    public void setRetry(Object retry) {
        this.retry = retry;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}