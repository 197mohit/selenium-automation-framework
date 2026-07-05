
package com.paytm.dto.autorefund;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "txnTimestamp",
    "reversalAmount",
    "orderId",
    "reversalId",
    "mid",
    "txnAmount",
    "reversalDetailInfoList"
})
public class Body implements Serializable
{

    @JsonProperty("txnTimestamp")
    private String txnTimestamp;
    @JsonProperty("reversalAmount")
    private String reversalAmount;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("reversalId")
    private String reversalId;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("ifscCode")
    private String ifscCode;
    @JsonProperty("reversalDetailInfoList")
    private List<ReversalDetailInfoList> reversalDetailInfoList = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -1449165808872401848L;

    @JsonProperty("txnTimestamp")
    public String getTxnTimestamp() {
        return txnTimestamp;
    }

    @JsonProperty("txnTimestamp")
    public void setTxnTimestamp(String txnTimestamp) {
        this.txnTimestamp = txnTimestamp;
    }

    @JsonProperty("reversalAmount")
    public String getReversalAmount() {
        return reversalAmount;
    }

    @JsonProperty("reversalAmount")
    public void setReversalAmount(String reversalAmount) {
        this.reversalAmount = reversalAmount;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("reversalId")
    public String getReversalId() {
        return reversalId;
    }

    @JsonProperty("reversalId")
    public void setReversalId(String reversalId) {
        this.reversalId = reversalId;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    @JsonProperty("ifscCode")
    public String getIfscCode() {
        return ifscCode;
    }

    @JsonProperty("ifscCode")
    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    @JsonProperty("reversalDetailInfoList")
    public List<ReversalDetailInfoList> getReversalDetailInfoList() {
        return reversalDetailInfoList;
    }

    @JsonProperty("reversalDetailInfoList")
    public void setReversalDetailInfoList(List<ReversalDetailInfoList> reversalDetailInfoList) {
        this.reversalDetailInfoList = reversalDetailInfoList;
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
