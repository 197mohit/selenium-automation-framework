package com.paytm.utils.merchant.dto.refund;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by ankuragarwal on 13/12/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Body {

    private String mid;
    private String aggMid;
    private String orderId;
    private String refId;
    private String txnId;
    private String refundAmount;
    private String preferredDestination;
    private String txnType;
    private boolean disableMerchantDebitRetry;
    private AgentInfo agentInfo;
    private String token;
    private ExtendInfo extendInfo = null;
    @JsonIgnore
    private String comments;
    @JsonIgnore
    private SubWalletAmount subWalletAmount;

    public String getMid() {
        return mid;
    }

    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }


    public String getAggMid() {
        return aggMid;
    }

    public Body setAggMid(String aggMid) {
        this.aggMid = aggMid;
        return this;
    }
    public String getOrderId() {
        return orderId;
    }

    public Body setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getRefId() {
        return refId;
    }

    public Body setRefId(String refId) {
        this.refId = refId;
        return this;
    }

    public String getTxnId() {
        return txnId;
    }

    public Body setTxnId(String txnId) {
        this.txnId = txnId;
        return this;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public Body setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
        return this;
    }

    public String getTxnType() {
        return txnType;
    }

    public Body setTxnType(String txnType) {
        this.txnType = txnType;
        return this;
    }

    public boolean getDisableMerchantDebitRetry() {
        return disableMerchantDebitRetry;
    }

    public Body setDisableMerchantDebitRetry(Boolean disableMerchantDebitRetry) {
        this.disableMerchantDebitRetry = disableMerchantDebitRetry;
        return this;
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public Body setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Body setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public SubWalletAmount getSubWalletAmount() {
        return subWalletAmount;
    }

    public Body setSubWalletAmount(SubWalletAmount subWalletAmount) {
        this.subWalletAmount = subWalletAmount;
        return this;
    }

    public String getToken(){ return token; }

    public Body setToken(String token) {
        this.token = token;
        return this;
    }

    public String getPreferredDestination() { return preferredDestination; }

    public Body setPreferredDestination(String preferredDestination) {
        this.preferredDestination=preferredDestination;
        return this;
    }

    public ExtendInfo getExtendInfo() { return extendInfo; }

    public Body setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo=extendInfo;
        return this;
    }


}
