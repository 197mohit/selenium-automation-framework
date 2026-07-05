package com.paytm.utils.merchant.dto.mappingService.response;

/**
 * Created by ankuragarwal on 12/9/18
 */
public class EntityUrlInfoResponse {

    private String merchantId;
    private String postBackurl;
    private String urlTypeId;
    private String status;
    private String comments;
    private long createdOn;
    private long modifiedOn;
    private String websiteName;
    private String requestName;
    private String notificationStatusUrl;
    private String imageName;
    private String imageData;
    private String mid;


    public String getMerchantId() {
        return merchantId;
    }

    public String getPostBackurl() {
        return postBackurl;
    }

    public String getUrlTypeId() {
        return urlTypeId;
    }

    public String getStatus() {
        return status;
    }

    public String getComments() {
        return comments;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public long getModifiedOn() {
        return modifiedOn;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public String getRequestName() {
        return requestName;
    }

    public String getNotificationStatusUrl() {
        return notificationStatusUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageData() {
        return imageData;
    }

    public String getMid() {
        return mid;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setPostBackurl(String postBackurl) {
        this.postBackurl = postBackurl;
    }

    public void setUrlTypeId(String urlTypeId) {
        this.urlTypeId = urlTypeId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public void setModifiedOn(long modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public void setNotificationStatusUrl(String notificationStatusUrl) {
        this.notificationStatusUrl = notificationStatusUrl;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
