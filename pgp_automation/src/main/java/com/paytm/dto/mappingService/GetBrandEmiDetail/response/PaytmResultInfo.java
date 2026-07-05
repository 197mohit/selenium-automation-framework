package com.paytm.dto.mappingService.GetBrandEmiDetail.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultCode",
        "resultStatus",
        "messaage"
})
public class PaytmResultInfo {

    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultStatus")
    private String resultStatus;
    @JsonProperty("messaage")
    private String messaage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("resultCode")
    public String getResultCode() {
        return resultCode;
    }

    @JsonProperty("resultCode")
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public PaytmResultInfo withResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    @JsonProperty("resultStatus")
    public String getResultStatus() {
        return resultStatus;
    }

    @JsonProperty("resultStatus")
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public PaytmResultInfo withResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
        return this;
    }

    @JsonProperty("messaage")
    public String getMessaage() {
        return messaage;
    }

    @JsonProperty("messaage")
    public void setMessaage(String messaage) {
        this.messaage = messaage;
    }

    public PaytmResultInfo withMessaage(String messaage) {
        this.messaage = messaage;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public PaytmResultInfo withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
