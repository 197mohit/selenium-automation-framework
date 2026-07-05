
package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.NativeDTO.SubwalletAmount;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "udf1",
        "udf2",
        "udf3",
        "udf4",
        "udf5",
        "mercUnqRef",
        "comments",
        "subwalletAmount",
        "orderAdditionalInfo"

})
public class ExtendInfo {

    @JsonProperty("udf1")
    private String udf1;
    @JsonProperty("udf2")
    private String udf2;
    @JsonProperty("udf3")
    private String udf3;
    @JsonProperty("udf4")
    private String udf4;
    @JsonProperty("udf5")
    private String udf5;
    @JsonProperty("mercUnqRef")
    private String mercUnqRef;
    @JsonProperty("comments")
    private String comments;
    @JsonProperty("subwalletAmount")
    private SubwalletAmount subwalletAmount;
    @JsonProperty("orderAdditionalInfo")
    private OrderAdditionalInfo orderAdditionalInfo;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("feeDetails")
    private FeeDetails feeDetails;

    @JsonProperty("feeDetails")
    public FeeDetails getFeeDetails() {
        return feeDetails;
    }

    @JsonProperty("feeDetails")
    public void setFeeDetails(FeeDetails feeDetails) {
        this.feeDetails = feeDetails;
    }

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
    public ExtendInfo setUdf2(String udf2) {
        this.udf2 = udf2;
        return this;
    }

    @JsonProperty("udf3")
    public String getUdf3() {
        return udf3;
    }

    @JsonProperty("udf3")
    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    @JsonProperty("udf4")
    public String getUdf4() {
        return udf4;
    }

    @JsonProperty("udf4")
    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    @JsonProperty("udf5")
    public String getUdf5() {
        return udf5;
    }

    @JsonProperty("udf5")
    public void setUdf5(String udf5) {
        this.udf5 = udf5;
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

    public ExtendInfo() {
        this.udf2 = "test";
        this.udf1 = "test";
        this.udf5 = "test";
        this.udf4 = "test";
        this.udf3 = "test";
    }

}
