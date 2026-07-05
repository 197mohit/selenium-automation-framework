package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "MD",
        "PaReq",
        "TermUrl",
        "extSerialNo"
})
public class Content {

    @JsonProperty("MD")
    private String mD;
    @JsonProperty("PaReq")
    private String paReq;
    @JsonProperty("TermUrl")
    private String termUrl;
    @JsonProperty("extSerialNo")
    private String extSerialNo;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("MD")
    public String getMD() {
        return mD;
    }

    @JsonProperty("MD")
    public void setMD(String mD) {
        this.mD = mD;
    }

    @JsonProperty("PaReq")
    public String getPaReq() {
        return paReq;
    }

    @JsonProperty("PaReq")
    public void setPaReq(String paReq) {
        this.paReq = paReq;
    }

    @JsonProperty("TermUrl")
    public String getTermUrl() {
        return termUrl;
    }

    @JsonProperty("TermUrl")
    public void setTermUrl(String termUrl) {
        this.termUrl = termUrl;
    }

    @JsonProperty("extSerialNo")
    public String getExtSerialNo() {
        return extSerialNo;
    }

    @JsonProperty("extSerialNo")
    public void setExtSerialNo(String extSerialNo) {
        this.extSerialNo = extSerialNo;
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