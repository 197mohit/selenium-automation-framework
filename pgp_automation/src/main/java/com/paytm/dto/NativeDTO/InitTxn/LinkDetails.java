
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
        "amount",
        "linkDescription",
        "linkNotes",
        "paymentFormId",
        "linkPaymentRiskInfo",
        "paymentFormDetails",
        "subRequestType",
        "linkId",
        "invoiceId",
        "longUrl",
        "shortUrl",
        "linkName",
        "resellerId",
        "resellerName"
})
public class LinkDetails implements Serializable {

    @JsonProperty("amount")
    private Integer amount;
    @JsonProperty("linkDescription")
    private String linkDescription;
    @JsonProperty("linkNotes")
    private Object linkNotes;
    @JsonProperty("paymentFormId")
    private Object paymentFormId;
    @JsonProperty("linkPaymentRiskInfo")
    private LinkPaymentRiskInfo linkPaymentRiskInfo;
    @JsonProperty("paymentFormDetails")
    private PaymentFormDetails paymentFormDetails;
    @JsonProperty("subRequestType")
    private String subRequestType;
    @JsonProperty("linkId")
    private String linkId;
    @JsonProperty("invoiceId")
    private String invoiceId;
    @JsonProperty("longUrl")
    private String longUrl;
    @JsonProperty("shortUrl")
    private String shortUrl;
    @JsonProperty("linkName")
    private String linkName;
    @JsonProperty("resellerId")
    private Object resellerId;
    @JsonProperty("resellerName")
    private Object resellerName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 7384990747666481200L;

    @JsonProperty("amount")
    public Integer getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @JsonProperty("linkDescription")
    public String getLinkDescription() {
        return linkDescription;
    }

    @JsonProperty("linkDescription")
    public void setLinkDescription(String linkDescription) {
        this.linkDescription = linkDescription;
    }

    @JsonProperty("linkNotes")
    public Object getLinkNotes() {
        return linkNotes;
    }

    @JsonProperty("linkNotes")
    public void setLinkNotes(Object linkNotes) {
        this.linkNotes = linkNotes;
    }

    @JsonProperty("paymentFormId")
    public Object getPaymentFormId() {
        return paymentFormId;
    }

    @JsonProperty("paymentFormId")
    public void setPaymentFormId(Object paymentFormId) {
        this.paymentFormId = paymentFormId;
    }

    @JsonProperty("linkPaymentRiskInfo")
    public LinkPaymentRiskInfo getLinkPaymentRiskInfo() {
        return linkPaymentRiskInfo;
    }

    @JsonProperty("linkPaymentRiskInfo")
    public void setLinkPaymentRiskInfo(LinkPaymentRiskInfo linkPaymentRiskInfo) {
        this.linkPaymentRiskInfo = linkPaymentRiskInfo;
    }

    @JsonProperty("paymentFormDetails")
    public PaymentFormDetails getPaymentFormDetails() {
        return paymentFormDetails;
    }

    @JsonProperty("paymentFormDetails")
    public void setPaymentFormDetails(PaymentFormDetails paymentFormDetails) {
        this.paymentFormDetails = paymentFormDetails;
    }

    @JsonProperty("subRequestType")
    public String getSubRequestType() {
        return subRequestType;
    }

    @JsonProperty("subRequestType")
    public void setSubRequestType(String subRequestType) {
        this.subRequestType = subRequestType;
    }

    @JsonProperty("linkId")
    public String getLinkId() {
        return linkId;
    }

    @JsonProperty("linkId")
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    @JsonProperty("invoiceId")
    public String getInvoiceId() {
        return invoiceId;
    }

    @JsonProperty("invoiceId")
    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    @JsonProperty("longUrl")
    public String getLongUrl() {
        return longUrl;
    }

    @JsonProperty("longUrl")
    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    @JsonProperty("shortUrl")
    public String getShortUrl() {
        return shortUrl;
    }

    @JsonProperty("shortUrl")
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    @JsonProperty("linkName")
    public String getLinkName() {
        return linkName;
    }

    @JsonProperty("linkName")
    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    @JsonProperty("resellerId")
    public Object getResellerId() {
        return resellerId;
    }

    @JsonProperty("resellerId")
    public void setResellerId(Object resellerId) {
        this.resellerId = resellerId;
    }

    @JsonProperty("resellerName")
    public Object getResellerName() {
        return resellerName;
    }

    @JsonProperty("resellerName")
    public void setResellerName(Object resellerName) {
        this.resellerName = resellerName;
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
        return new ToStringBuilder(this).append("amount", amount).append("linkDescription", linkDescription).append("linkNotes", linkNotes).append("paymentFormId", paymentFormId).append("linkPaymentRiskInfo", linkPaymentRiskInfo).append("paymentFormDetails", paymentFormDetails).append("subRequestType", subRequestType).append("linkId", linkId).append("invoiceId", invoiceId).append("longUrl", longUrl).append("shortUrl", shortUrl).append("linkName", linkName).append("resellerId", resellerId).append("resellerName", resellerName).append("additionalProperties", additionalProperties).toString();
    }

}
