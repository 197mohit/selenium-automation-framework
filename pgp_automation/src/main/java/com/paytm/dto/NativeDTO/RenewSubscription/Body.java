
package com.paytm.dto.NativeDTO.RenewSubscription;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mid",
    "orderId",
    "txnAmount",
    "subscriptionId",
    "additionalInfo",
        "requestType",
        "debitDate"
})
public class Body {

    public Body(String mid,String orderId,String subscriptionId,TxnAmount txnAmount, String requestType,String debitDate){
        this.mid=mid;
        this.orderId=orderId;
        this.subscriptionId=subscriptionId;
        this.txnAmount = txnAmount;
        this.requestType = requestType;
        this.debitDate = debitDate;
    }
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("txnAmount")
    private TxnAmount txnAmount;
    @JsonProperty("subscriptionId")
    private String subscriptionId;
    @JsonProperty("additionalInfo")
    private String additionalInfo;
    @JsonProperty("requestType")
    private String requestType;
    @JsonProperty("debitDate")
    private String debitDate;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("requestType")
    public String getRequestType() {
        return requestType;
    }

    @JsonProperty("requestType")
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("txnAmount")
    public TxnAmount getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(TxnAmount txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("subscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @JsonProperty("subscriptionId")
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @JsonProperty("debitDate")
    public String getDebitDate() {
        return debitDate;
    }

    @JsonProperty("debitDate")
    public void setDebitDate(String debitDate) {
        this.debitDate = debitDate;
    }


    @JsonProperty("additionalInfo")
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @JsonProperty("additionalInfo")
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
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
