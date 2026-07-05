package com.paytm.dto.NativeDTO.InitTxn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "linkName",
        "linkDescription",
        "linkCreationTime",
        "merchantLimit",
        "linkType",
        "requestType",
        "linkOpenTime",
        "linkAmount",
        "linkId",
        "linkPaymentRequest"
})
public class LinkPaymentRiskInfo implements Serializable {

    @JsonProperty("linkName")
    private String linkName;
    @JsonProperty("linkDescription")
    private String linkDescription;
    @JsonProperty("linkCreationTime")
    private Integer linkCreationTime;
    @JsonProperty("merchantLimit")
    private Integer merchantLimit;
    @JsonProperty("linkType")
    private String linkType;
    @JsonProperty("requestType")
    private String requestType;
    @JsonProperty("linkOpenTime")
    private String linkOpenTime;
    @JsonProperty("linkAmount")
    private String linkAmount;
    @JsonProperty("linkId")
    private String linkId;
    @JsonProperty("linkPaymentRequest")
    private Boolean linkPaymentRequest;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 1758283353169922963L;

    @JsonProperty("linkName")
    public String getLinkName() {
        return linkName;
    }

    @JsonProperty("linkName")
    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    @JsonProperty("linkDescription")
    public String getLinkDescription() {
        return linkDescription;
    }

    @JsonProperty("linkDescription")
    public void setLinkDescription(String linkDescription) {
        this.linkDescription = linkDescription;
    }

    @JsonProperty("linkCreationTime")
    public Integer getLinkCreationTime() {
        return linkCreationTime;
    }

    @JsonProperty("linkCreationTime")
    public void setLinkCreationTime(Integer linkCreationTime) {
        this.linkCreationTime = linkCreationTime;
    }

    @JsonProperty("merchantLimit")
    public Integer getMerchantLimit() {
        return merchantLimit;
    }

    @JsonProperty("merchantLimit")
    public void setMerchantLimit(Integer merchantLimit) {
        this.merchantLimit = merchantLimit;
    }

    @JsonProperty("linkType")
    public String getLinkType() {
        return linkType;
    }

    @JsonProperty("linkType")
    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    @JsonProperty("requestType")
    public String getRequestType() {
        return requestType;
    }

    @JsonProperty("requestType")
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @JsonProperty("linkOpenTime")
    public String getLinkOpenTime() {
        return linkOpenTime;
    }

    @JsonProperty("linkOpenTime")
    public void setLinkOpenTime(String linkOpenTime) {
        this.linkOpenTime = linkOpenTime;
    }

    @JsonProperty("linkAmount")
    public String getLinkAmount() {
        return linkAmount;
    }

    @JsonProperty("linkAmount")
    public void setLinkAmount(String linkAmount) {
        this.linkAmount = linkAmount;
    }

    @JsonProperty("linkId")
    public String getLinkId() {
        return linkId;
    }

    @JsonProperty("linkId")
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    @JsonProperty("linkPaymentRequest")
    public Boolean getLinkPaymentRequest() {
        return linkPaymentRequest;
    }

    @JsonProperty("linkPaymentRequest")
    public void setLinkPaymentRequest(Boolean linkPaymentRequest) {
        this.linkPaymentRequest = linkPaymentRequest;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("linkName", linkName).append("linkDescription", linkDescription).append("linkCreationTime", linkCreationTime).append("merchantLimit", merchantLimit).append("linkType", linkType).append("requestType", requestType).append("linkOpenTime", linkOpenTime).append("linkAmount", linkAmount).append("linkId", linkId).append("linkPaymentRequest", linkPaymentRequest).append("additionalProperties", additionalProperties).toString();
    }

}
