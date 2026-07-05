package com.paytm.dto.NativeDTO.getEMIDetails.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "emiDetails",
        "resultInfo",
        "brandEmiDetails"
})
public class Body {

    @JsonProperty("emiDetails")
    private List<EmiDetail> emiDetails = null;
    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;
    @JsonProperty("brandEmiDetails")
    private List<BrandEmiDetails> brandEmiDetails = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("emiDetails")
    public List<EmiDetail> getEmiDetails() {
        return emiDetails;
    }

    @JsonProperty("emiDetails")
    public void setEmiDetails(List<EmiDetail> emiDetails) {
        this.emiDetails = emiDetails;
    }

    public Body withEmiDetails(List<EmiDetail> emiDetails) {
        this.emiDetails = emiDetails;
        return this;
    }

    @JsonProperty("resultInfo")
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    @JsonProperty("resultInfo")
    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public Body withResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
        return this;
    }

    @JsonProperty("brandEmiDetails")
    public List<BrandEmiDetails> getBrandEmiDetails() {
        return brandEmiDetails;
    }

    @JsonProperty("brandEmiDetails")
    public void setBrandEmiDetails(List<BrandEmiDetails> brandEmiDetails) {
        this.brandEmiDetails = brandEmiDetails;
    }

    public Body withBrandEmiDetails(List<BrandEmiDetails> brandEmiDetails) {
        this.brandEmiDetails = brandEmiDetails;
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

    public Body withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}