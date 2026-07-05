package com.paytm.dto.NativeDTO.fetchPcfDetail;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})

public class FetchPcfRequestWithSSO {

    @JsonProperty("head")
    private com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head head;

    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("head")
    public com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public FetchPcfRequestWithSSO setHead(com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public FetchPcfRequestWithSSO setBody(Body body) {
        this.body = body;
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

}