package com.paytm.dto.masterRefund;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.utils.merchant.dto.refund.SubWalletAmount;

/**
 * Created by anjukumari on 24/01/19
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MasterRefundBody {
    @JsonProperty("MID")
    private String mid;
    @JsonProperty("ORDERID")
    private String orderId;
    @JsonProperty("REFID")
    private String refId;
    @JsonProperty("TXNID")
    private String txnId;
    @JsonProperty("REFUNDAMOUNT")
    private String refundAmount;
    @JsonProperty("TXNTYPE")
    private String txnType;
    private String comments;
    @JsonProperty("subwalletAmount")
    private SubWalletAmount subWalletAmount;
    @JsonProperty("CHECKSUM")
    private String checksum;

    public String getMid() {
        return mid;
    }

    public MasterRefundBody setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public MasterRefundBody setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getRefId() {
        return refId;
    }

    public MasterRefundBody setRefId(String refId) {
        this.refId = refId;
        return this;
    }

    public String getTxnId() {
        return txnId;
    }

    public MasterRefundBody setTxnId(String txnId) {
        this.txnId = txnId;
        return this;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public MasterRefundBody setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
        return this;
    }

    public String getTxnType() {
        return txnType;
    }

    public MasterRefundBody setTxnType(String txnType) {
        this.txnType = txnType;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public MasterRefundBody setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public SubWalletAmount getSubWalletAmount() {
        return subWalletAmount;
    }

    public MasterRefundBody setSubWalletAmount(SubWalletAmount subWalletAmount) {
        this.subWalletAmount = subWalletAmount;
        return this;
    }

    public String getChecksum() {
        return checksum;
    }

    public MasterRefundBody setChecksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "mid='" + mid + '\'' +
                ", orderId='" + orderId + '\'' +
                ", refId='" + refId + '\'' +
                ", txnId='" + txnId + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                ", txnType='" + txnType + '\'' +
                ", comments='" + comments + '\'' +
                ", subWalletAmount=" + subWalletAmount +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}

