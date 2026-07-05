package com.paytm.utils.merchant.dto.mappingService.response;

/**
 * Created by ankuragarwal on 13/9/18
 */
public class EntityOfferDetailsDTO {

    private String channel;
    private String website;
    private String status;
    private String message;
    private String merchantId;
    private String createdDate;
    private String modifiedDate;
    private String validFrom;
    private String validTo;
    private String mid;

    public String getChannel() {
        return channel;
    }

    public String getWebsite() {
        return website;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public String getMid() {
        return mid;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
