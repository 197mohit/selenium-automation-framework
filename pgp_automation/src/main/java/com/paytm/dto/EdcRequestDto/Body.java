package com.paytm.dto.EdcRequestDto;

import com.fasterxml.jackson.annotation.*;
import com.paytm.dto.processTransactionV1.ExtendInfo;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "productType",
        "productId",
        "posId",
        "payAndConfirmRequest",
        "orderId",
        "orderDetails",
        "mid",
        "invoiceDetails",
        "imageRequired",
        "expiryDate",
        "displayName",
        "contactPhoneNo",
        "businessType",
        "amount",
        "extendInfo"
})
public class Body {

    @JsonProperty("productType")
    private String productType;
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("posId")
    private String posId;
    @JsonProperty("payAndConfirmRequest")
    private String payAndConfirmRequest;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("orderDetails")
    private String orderDetails;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("invoiceDetails")
    private String invoiceDetails;
    @JsonProperty("imageRequired")
    private String imageRequired;
    @JsonProperty("expiryDate")
    private String expiryDate;
    @JsonProperty("displayName")
    private String displayName;
    @JsonProperty("contactPhoneNo")
    private String contactPhoneNo;
    @JsonProperty("businessType")
    private String businessType;
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("productType")
    public String getProductType() {
        return productType;
    }

    @JsonProperty("productType")
    public Body setProductType(String productType) {
        this.productType = productType;
        return this;
    }

    @JsonProperty("productId")
    public String getProductId() {
        return productId;
    }

    @JsonProperty("productId")
    public Body setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    @JsonProperty("posId")
    public String getPosId() {
        return posId;
    }

    @JsonProperty("posId")
    public Body setPosId(String posId) {
        this.posId = posId;
        return this;
    }

    @JsonProperty("payAndConfirmRequest")
    public String getPayAndConfirmRequest() {
        return payAndConfirmRequest;
    }

    @JsonProperty("payAndConfirmRequest")
    public Body setPayAndConfirmRequest(String payAndConfirmRequest) {
        this.payAndConfirmRequest = payAndConfirmRequest;
        return this;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public Body setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @JsonProperty("orderDetails")
    public String getOrderDetails() {
        return orderDetails;
    }

    @JsonProperty("orderDetails")
    public Body setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
        return this;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("invoiceDetails")
    public String getInvoiceDetails() {
        return invoiceDetails;
    }

    @JsonProperty("invoiceDetails")
    public Body setInvoiceDetails(String invoiceDetails) {
        this.invoiceDetails = invoiceDetails;
        return this;
    }

    @JsonProperty("imageRequired")
    public String getImageRequired() {
        return imageRequired;
    }

    @JsonProperty("imageRequired")
    public Body setImageRequired(String imageRequired) {
        this.imageRequired = imageRequired;
        return this;
    }

    @JsonProperty("expiryDate")
    public String getExpiryDate() {
        return expiryDate;
    }

    @JsonProperty("expiryDate")
    public Body setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("displayName")
    public Body setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @JsonProperty("contactPhoneNo")
    public String getContactPhoneNo() {
        return contactPhoneNo;
    }

    @JsonProperty("contactPhoneNo")
    public Body setContactPhoneNo(String contactPhoneNo) {
        this.contactPhoneNo = contactPhoneNo;
        return this;
    }

    @JsonProperty("businessType")
    public String getBusinessType() {
        return businessType;
    }

    @JsonProperty("businessType")
    public Body setBusinessType(String businessType) {
        this.businessType = businessType;
        return this;
    }

    @JsonProperty("amount")
    public String getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public Body setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    @JsonProperty("extendInfo")
    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public Body setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }


    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Body setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
