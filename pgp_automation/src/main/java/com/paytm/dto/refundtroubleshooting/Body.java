package com.paytm.dto.refundtroubleshooting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "orderId",
        "refId",
        "txnId",
        "refundAmount",
        "txnType",
        "comments",
        "preferredDestination",
        "token"
})
public class Body {

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("refId")
    private String refId;
    @JsonProperty("txnId")
    private String txnId;
    @JsonProperty("refundAmount")
    private String refundAmount;
    @JsonProperty("txnType")
    private String txnType="REFUND";
    @JsonProperty("comments")
    private String comments="InitiateRefund";
    @JsonProperty("preferredDestination")
    private String preferredDestination="TO_INSTANT";
    @JsonProperty("token")
    private String token;

    /**
     * No args constructor for use in serialization
     *
     */
    public Body() {
    }

    /**
     *
     * @param txnType
     * @param refundAmount
     * @param token
     * @param preferredDestination
     * @param refId
     * @param mid
     * @param comments
     * @param txnId
     * @param orderId
     */
    public Body(String mid, String orderId, String refId, String txnId, String refundAmount, String txnType, String comments, String preferredDestination, String token) {
        super();
        this.mid = mid;
        this.orderId = orderId;
        this.refId = refId;
        this.txnId = txnId;
        this.refundAmount = refundAmount;
        this.txnType = txnType;
        this.comments = comments;
        this.preferredDestination = preferredDestination;
        this.token = token;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    public Body withMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Body withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @JsonProperty("refId")
    public String getRefId() {
        return refId;
    }

    @JsonProperty("refId")
    public void setRefId(String refId) {
        this.refId = refId;
    }

    public Body withRefId(String refId) {
        this.refId = refId;
        return this;
    }

    @JsonProperty("txnId")
    public String getTxnId() {
        return txnId;
    }

    @JsonProperty("txnId")
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public Body withTxnId(String txnId) {
        this.txnId = txnId;
        return this;
    }

    @JsonProperty("refundAmount")
    public String getRefundAmount() {
        return refundAmount;
    }

    @JsonProperty("refundAmount")
    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Body withRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
        return this;
    }

    @JsonProperty("txnType")
    public String getTxnType() {
        return txnType;
    }

    @JsonProperty("txnType")
    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public Body withTxnType(String txnType) {
        this.txnType = txnType;
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

    public Body withComments(String comments) {
        this.comments = comments;
        return this;
    }

    @JsonProperty("preferredDestination")
    public String getPreferredDestination() {
        return preferredDestination;
    }

    @JsonProperty("preferredDestination")
    public void setPreferredDestination(String preferredDestination) {
        this.preferredDestination = preferredDestination;
    }

    public Body withPreferredDestination(String preferredDestination) {
        this.preferredDestination = preferredDestination;
        return this;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    public Body withToken(String token) {
        this.token = token;
        return this;
    }

}
