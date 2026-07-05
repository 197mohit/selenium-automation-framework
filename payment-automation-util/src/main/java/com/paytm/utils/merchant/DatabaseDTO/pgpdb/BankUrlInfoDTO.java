package com.paytm.utils.merchant.DatabaseDTO.pgpdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankuragarwal on 10/10/18
 */
public class BankUrlInfoDTO {

    @JsonProperty("ID")
    private Object id;
    @JsonProperty("BANK_ID")
    private Object bankId;
    @JsonProperty("PAY_METHOD_ID")
    private Object payMethodId;
    @JsonProperty("CHANNEL_ID")
    private Object channelId;
    @JsonProperty("WEB_PAY_URL")
    private Object webPayUrl;
    @JsonProperty("S2S_PAY_URL")
    private Object s2sPayUrl;
    @JsonProperty("STATUS_QRY_URL")
    private Object statusQryUrl;
    @JsonProperty("REFUND_URL")
    private Object refundUrl;
    @JsonProperty("REFUND_STATUS_URL")
    private Object refundStatusUrl;
    @JsonProperty("WEB_RESPONSE_URL")
    private Object webResponseUrl;
    @JsonProperty("URL")
    private Object url;
    @JsonProperty("URL_TYPE")
    private Object urlType;
    @JsonProperty("CREATED_ON")
    private Object createdOn;
    @JsonProperty("CREATED_BY")
    private Object createdBy;
    @JsonProperty("UPDATED_ON")
    private Object updatedOn;
    @JsonProperty("UPDATED_BY")
    private Object updatedBy;


    public Object getId() {
        return id;
    }

    public Object getBankId() {
        return bankId;
    }

    public Object getPayMethodId() {
        return payMethodId;
    }

    public Object getChannelId() {
        return channelId;
    }

    public Object getWebPayUrl() {
        return webPayUrl;
    }

    public Object getS2sPayUrl() {
        return s2sPayUrl;
    }

    public Object getStatusQryUrl() {
        return statusQryUrl;
    }

    public Object getRefundUrl() {
        return refundUrl;
    }

    public Object getRefundStatusUrl() {
        return refundStatusUrl;
    }

    public Object getWebResponseUrl() {
        return webResponseUrl;
    }

    public Object getUrl() {
        return url;
    }

    public Object getUrlType() {
        return urlType;
    }

    public Object getCreatedOn() {
        return createdOn;
    }

    public Object getCreatedBy() {
        return createdBy;
    }

    public Object getUpdatedOn() {
        return updatedOn;
    }

    public Object getUpdatedBy() {
        return updatedBy;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setBankId(Object bankId) {
        this.bankId = bankId;
    }

    public void setPayMethodId(Object payMethodId) {
        this.payMethodId = payMethodId;
    }

    public void setChannelId(Object channelId) {
        this.channelId = channelId;
    }

    public void setWebPayUrl(Object webPayUrl) {
        this.webPayUrl = webPayUrl;
    }

    public void setS2sPayUrl(Object s2sPayUrl) {
        this.s2sPayUrl = s2sPayUrl;
    }

    public void setStatusQryUrl(Object statusQryUrl) {
        this.statusQryUrl = statusQryUrl;
    }

    public void setRefundUrl(Object refundUrl) {
        this.refundUrl = refundUrl;
    }

    public void setRefundStatusUrl(Object refundStatusUrl) {
        this.refundStatusUrl = refundStatusUrl;
    }

    public void setWebResponseUrl(Object webResponseUrl) {
        this.webResponseUrl = webResponseUrl;
    }

    public void setUrl(Object url) {
        this.url = url;
    }

    public void setUrlType(Object urlType) {
        this.urlType = urlType;
    }

    public void setCreatedOn(Object createdOn) {
        this.createdOn = createdOn;
    }

    public void setCreatedBy(Object createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedOn(Object updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedBy(Object updatedBy) {
        this.updatedBy = updatedBy;
    }
}
