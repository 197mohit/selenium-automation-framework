package com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.utils.merchant.dto.refund.AgentInfo;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "agentInfo",
        "orderId",
        "userCreditInitiateStatus",
        "mid",
        "merchantRefundRequestTimestamp",
        "source",
        "requestedRefundDetailInfo",
        "resultInfo",
        "txnTimestamp",
        "acceptRefundTimestamp",
        "acceptRefundStatus",
        "refundDetailInfoList",
        "userCreditInitiateTimestamp",
        "totalRefundAmount",
        "refId",
        "txnAmount",
        "refundId",
        "txnId",
        "rrn",
        "refundAmount",
        "txnDate",
        "merchantRefundRequestDate",
        "refundReason",
        "maskedVpa",
        "maxRefundRetryTimeStamp"
})
public class Body {

    @JsonProperty("agentInfo")
    private AgentInfo agentInfo;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("userCreditInitiateStatus")
    private String userCreditInitiateStatus;
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("merchantRefundRequestTimestamp")
    private String merchantRefundRequestTimestamp;
    @JsonProperty("source")
    private String source;
    @JsonProperty("requestedRefundDetailInfo")
    private RequestedRefundDetailInfo requestedRefundDetailInfo;
    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;
    @JsonProperty("txnTimestamp")
    private String txnTimestamp;
    @JsonProperty("acceptRefundTimestamp")
    private String acceptRefundTimestamp;
    @JsonProperty("acceptRefundStatus")
    private String acceptRefundStatus;
    @JsonProperty("refundDetailInfoList")
    private List<RefundDetailInfoList> refundDetailInfoList = null;
    @JsonProperty("userCreditInitiateTimestamp")
    private String userCreditInitiateTimestamp;
    @JsonProperty("totalRefundAmount")
    private String totalRefundAmount;
    @JsonProperty("refId")
    private String refId;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("refundId")
    private String refundId;
    @JsonProperty("txnId")
    private String txnId;
    @JsonProperty("refundAmount")
    private String refundAmount;
    @JsonProperty("merchantRefundRequestDate")
    private String merchantRefundRequestDate;
    @JsonProperty("txnDate")
    private String txnDate;
    @JsonProperty("rrn")
    private String rrn;
    @JsonProperty("refundReason")
    private String refundReason;
    @JsonProperty("maskedVpa")
    private String maskedVpa;
    @JsonProperty("maxRefundRetryTimeStamp")
    private String maxRefundRetryTimeStamp;

    @JsonProperty("agentInfo")
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    @JsonProperty("agentInfo")
    public Body setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
        return this;
    }

    @JsonProperty("refundReason")
    public String getRefundReason() {
        return refundReason;
    }

    @JsonProperty("refundReason")
    public Body setRefundReason(String refundReason) {
        this.refundReason = refundReason;
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


    @JsonProperty("userCreditInitiateStatus")
    public String getUserCreditInitiateStatus() {
        return userCreditInitiateStatus;
    }

    @JsonProperty("userCreditInitiateStatus")
    public Body setUserCreditInitiateStatus(String userCreditInitiateStatus) {
        this.userCreditInitiateStatus = userCreditInitiateStatus;
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


    @JsonProperty("merchantRefundRequestTimestamp")
    public String getMerchantRefundRequestTimestamp() {
        return merchantRefundRequestTimestamp;
    }

    @JsonProperty("merchantRefundRequestTimestamp")
    public Body setMerchantRefundRequestTimestamp(String merchantRefundRequestTimestamp) {
        this.merchantRefundRequestTimestamp = merchantRefundRequestTimestamp;
        return this;
    }

    @JsonProperty("txnDate")
    public String getTxnDate() {
        return txnDate;
    }

    @JsonProperty("txnDate")
    public Body setTxnDate(String txnDate) {
        this.txnDate = txnDate;
        return this;
    }
    @JsonProperty("merchantRefundRequestDate")
    public String getMerchantRefundRequestDate() {
        return merchantRefundRequestDate;
    }

    @JsonProperty("merchantRefundRequestDate")
    public Body setMerchantRefundRequestDate(String merchantRefundRequestDate) {
        this.merchantRefundRequestDate = merchantRefundRequestDate;
        return this;
    }


    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    public Body setSource(String source) {
        this.source = source;
        return this;
    }

    @JsonProperty("requestedRefundDetailInfo")
    public RequestedRefundDetailInfo getRequestedRefundDetailInfo() {
        return requestedRefundDetailInfo;
    }

    @JsonProperty("requestedRefundDetailInfo")
    public Body setRequestedRefundDetailInfo(RequestedRefundDetailInfo requestedRefundDetailInfo) {
        this.requestedRefundDetailInfo = requestedRefundDetailInfo;
        return this;
    }

    @JsonProperty("resultInfo")
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    @JsonProperty("resultInfo")
    public Body setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
        return this;
    }


    @JsonProperty("txnTimestamp")
    public String getTxnTimestamp() {
        return txnTimestamp;
    }

    @JsonProperty("txnTimestamp")
    public Body setTxnTimestamp(String txnTimestamp) {
        this.txnTimestamp = txnTimestamp;
        return this;
    }


    @JsonProperty("acceptRefundTimestamp")
    public String getAcceptRefundTimestamp() {
        return acceptRefundTimestamp;
    }

    @JsonProperty("acceptRefundTimestamp")
    public Body setAcceptRefundTimestamp(String acceptRefundTimestamp) {
        this.acceptRefundTimestamp = acceptRefundTimestamp;
        return this;
    }


    @JsonProperty("acceptRefundStatus")
    public String getAcceptRefundStatus() {
        return acceptRefundStatus;
    }

    @JsonProperty("acceptRefundStatus")
    public Body setAcceptRefundStatus(String acceptRefundStatus) {
        this.acceptRefundStatus = acceptRefundStatus;
        return this;
    }

    @JsonProperty("refundDetailInfoList")
    public List<RefundDetailInfoList> getRefundDetailInfoList() {
        return refundDetailInfoList;
    }

    @JsonProperty("refundDetailInfoList")
    public Body setRefundDetailInfoList(List<RefundDetailInfoList> refundDetailInfoList) {
        this.refundDetailInfoList = refundDetailInfoList;
        return this;
    }

    @JsonProperty("userCreditInitiateTimestamp")
    public String getUserCreditInitiateTimestamp() {
        return userCreditInitiateTimestamp;
    }

    @JsonProperty("userCreditInitiateTimestamp")
    public Body setUserCreditInitiateTimestamp(String userCreditInitiateTimestamp) {
        this.userCreditInitiateTimestamp = userCreditInitiateTimestamp;
        return this;
    }

    @JsonProperty("totalRefundAmount")
    public String getTotalRefundAmount() {
        return totalRefundAmount;
    }

    @JsonProperty("totalRefundAmount")
    public Body setTotalRefundAmount(String totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
        return this;
    }

    @JsonProperty("refId")
    public String getRefId() {
        return refId;
    }

    @JsonProperty("refId")
    public Body setRefId(String refId) {
        this.refId = refId;
        return this;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public Body setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    @JsonProperty("refundId")
    public String getRefundId() {
        return refundId;
    }

    @JsonProperty("refundId")
    public Body setRefundId(String refundId) {
        this.refundId = refundId;
        return this;
    }

    @JsonProperty("txnId")
    public String getTxnId() {
        return txnId;
    }

    @JsonProperty("txnId")
    public Body setTxnId(String txnId) {
        this.txnId = txnId;
        return this;
    }

    @JsonProperty("refundAmount")
    public String getRefundAmount() {
        return refundAmount;
    }

    @JsonProperty("refundAmount")
    public Body setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
        return this;
    }

    @JsonProperty("rrn")
    public String getRrn() {
        return rrn;
    }

    @JsonProperty("rrn")
    public Body setRrn(String rrn) {
        this.rrn = rrn;
        return this;
    }

    @JsonProperty("maskedVpa")
    public String getMaskedVpa() {
        return maskedVpa;
    }

    @JsonProperty("maskedVpa")
    public void setMaskedVpa(String maskedVpa) {
        this.maskedVpa = maskedVpa;
    }

    @JsonProperty("maxRefundRetryTimeStamp")
    public String getMaxRefundRetryTimeStamp() {
        return maxRefundRetryTimeStamp;
    }

    @JsonProperty("maxRefundRetryTimeStamp")
    public void setMaxRefundRetryTimeStamp(String maxRefundRetryTimeStamp) {

        this.maxRefundRetryTimeStamp = maxRefundRetryTimeStamp;
    }
}