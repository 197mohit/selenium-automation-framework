package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.NativeDTO.InitTxn.OrderAdditionalInfo;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "udf1",
        "udf2",
        "udf3",
        "mercUnqRef",
        "comments",
        "additionalInfo",
        "orderAdditionalInfo"
})

public class ExtendInfo {

    @JsonProperty("udf1")
    private String udf1 = "vivek1";
    @JsonProperty("udf2")
    private String udf2 = "vivek2";
    @JsonProperty("udf3")
    private String udf3 = "vivek3";
    @JsonProperty("mercUnqRef")
    private String mercUnqRef = "vivek4";
    @JsonProperty("comments")
    private String comments = "vivek5";
    @JsonProperty("sdkType")
    private String sdkType = null;
    @JsonProperty("additionalInfo")
    private String additionalInfo;
    @JsonProperty("merchantUniqueReference")
    private String merchantUniqueReference = "2810050501011BBRF2ET3Y18";
    @JsonProperty("orderAdditionalInfo")
    private OrderAdditionalInfo orderAdditionalInfo = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
    public void setMercUnqRef(String mercUnqRef) {
        this.mercUnqRef = mercUnqRef;
    }

    @JsonProperty("comments")
    public String getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(String comments) {
        this.comments = comments;
    }

    @JsonProperty("sdkType")
    public String getSdkType() {
        return sdkType;
    }

    @JsonProperty("sdkType")
    public void setSdkType(String sdkType) {
        this.sdkType = sdkType;
    }

    @JsonProperty("additionalInfo")
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @JsonProperty("additionalInfo")
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @JsonProperty("merchantUniqueReference")
    public String getMerchantUniqueReference() {
        return merchantUniqueReference;
    }

    @JsonProperty("merchantUniqueReference")
    public void setMerchantUniqueReference(String merchantUniqueReference) {
        this.merchantUniqueReference = merchantUniqueReference;
    }

    @JsonProperty("orderAdditionalInfo")
    public OrderAdditionalInfo getOrderAdditionalInfo() {
        return orderAdditionalInfo;
    }

    @JsonProperty("orderAdditionalInfo")
    public ExtendInfo setOrderAdditionalInfo(OrderAdditionalInfo orderAdditionalInfo) {
        this.orderAdditionalInfo = orderAdditionalInfo;
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