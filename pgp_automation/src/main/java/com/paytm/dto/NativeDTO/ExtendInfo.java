
package com.paytm.dto.NativeDTO;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "udf1",
    "udf2",
    "udf3",
    "mercUnqRef",
    "comments",
    "subwalletAmount"
})

@Deprecated
public class ExtendInfo {

    @JsonProperty("udf1")
    private String udf1;
    @JsonProperty("udf2")
    private String udf2;
    @JsonProperty("udf3")
    private String udf3;
    @JsonProperty("mercUnqRef")
    private String mercUnqRef;
    @JsonProperty("comments")
    private String comments;
    @JsonProperty("subwalletAmount")
    private SubwalletAmount subwalletAmount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("udf1")
    public String getUdf1() {
        return udf1;
    }

    @JsonProperty("udf1")
    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    @JsonProperty("udf2")
    public String getUdf2() {
        return udf2;
    }

    @JsonProperty("udf2")
    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    @JsonProperty("udf3")
    public String getUdf3() {
        return udf3;
    }

    @JsonProperty("udf3")
    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    @JsonProperty("mercUnqRef")
    public String getMercUnqRef() {
        return mercUnqRef;
    }

    @JsonProperty("mercUnqRef")
    public ExtendInfo setMercUnqRef(String mercUnqRef) {
        this.mercUnqRef = mercUnqRef;
        return this;
    }

    @JsonProperty("comments")
    public String getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(String comments) {
        this.comments = comments;
    }

    @JsonProperty("subwalletAmount")
    public SubwalletAmount getSubwalletAmount() {
        return subwalletAmount;
    }

    @JsonProperty("subwalletAmount")
    public void setSubwalletAmount(SubwalletAmount subwalletAmount) {
        this.subwalletAmount = subwalletAmount;
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
