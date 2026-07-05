package com.paytm.dto.mappingService.GetBrandEmiDetail.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "paytmResultInfo",
        "response"
})
public class FetchBrandEmiDetailDTO {

    @JsonProperty("paytmResultInfo")
    private PaytmResultInfo paytmResultInfo;
    @JsonProperty("response")
    private Response response;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("paytmResultInfo")
    public PaytmResultInfo getPaytmResultInfo() {
        return paytmResultInfo;
    }

    @JsonProperty("paytmResultInfo")
    public void setPaytmResultInfo(PaytmResultInfo paytmResultInfo) {
        this.paytmResultInfo = paytmResultInfo;
    }

    public FetchBrandEmiDetailDTO withPaytmResultInfo(PaytmResultInfo paytmResultInfo) {
        this.paytmResultInfo = paytmResultInfo;
        return this;
    }

    @JsonProperty("response")
    public Response getResponse() {
        return response;
    }

    @JsonProperty("response")
    public void setResponse(Response response) {
        this.response = response;
    }

    public FetchBrandEmiDetailDTO withResponse(Response response) {
        this.response = response;
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

    public FetchBrandEmiDetailDTO withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
