package com.paytm.utils.merchant.DatabaseDTO.paytmpgdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ankuragarwal on 10/10/18
 */
public class EntityInfoDTO {

    @JsonProperty("ID")
    private Object id;
    @JsonProperty("KYB_ID")
    private Object kyb_id;
    @JsonProperty("INDUSTRY_TYPE_ID")
    private Object industryTypeId;
    @JsonProperty("REQUEST_ID")
    private Object requestId;
    @JsonProperty("MID")
    private Object mid;
    @JsonProperty("ACTOR_TYPE_ID")
    private Object actorTypeId;
    @JsonProperty("MERCHANT_NAME")
    private Object merchantName;
    @JsonProperty("CURRENCY")
    private Object currency;
    @JsonProperty("KYC_APPROVAL_ID")
    private Object kycApprovalId;
    @JsonProperty("STATUS")
    private Object status;
    @JsonProperty("CREATED_BY")
    private Object createdBy;
    @JsonProperty("CREATED_DATE")
    private Object createdDate;
    @JsonProperty("MODIFIED_BY")
    private Object modifiedBy;
    @JsonProperty("MODIFIED_DATE")
    private Object modifiedDate;
    @JsonProperty("ORDER_ID_FLAG")
    private Object orderIdFlag;
    @JsonProperty("NUMBER_OF_RETRY")
    private Object numberOfRetry;
    @JsonProperty("ISPEONENABLE")
    private Object ispeonenable;
    @JsonProperty("KEY_SIZE")
    private Object keySize;
    @JsonProperty("WALLET_ENABLED")
    private Object walletEnabled;
    @JsonProperty("WALLET_RECHARGE_ENABLED")
    private Object walletRechargeEnabled;
    @JsonProperty("PROFILE_ID")
    private Object profileId;
    @JsonProperty("ALERT_FLAG")
    private Object alertFlag;
    @JsonProperty("GUID_ENABLED")
    private Object guidEnabled;
    @JsonProperty("CALLBACK_URL_ENABLED")
    private Object callbackUrlEnabled;
    @JsonProperty("IS_FLUCTUATION_ENABLED")
    private Object isFluctuationEnabled;
    @JsonProperty("ISAGGREGATOR")
    private Object isAggregator;
    @JsonProperty("PARENT_ENTITY_ID")
    private Object parentEntityId;
    @JsonProperty("PREFORMAT")
    private Object preformat;
    @JsonProperty("SAP")
    private Object sap;
    @JsonProperty("IS_BLOCKED")
    private Object isBlocked;
    @JsonProperty("COMMENT")
    private Object comment;
    @JsonProperty("S2S_CALLBACK_ENABLED")
    private Object s2sCallback_Enabled;
    @JsonProperty("SSO_ID")
    private Object ssoId;
    @JsonProperty("POST_CONV_THRESHOLD")
    private Object postConvThreshold;
    @JsonProperty("PENDING_TO_SUCCESS_REFUND_TIME")
    private Object pendingToSuccessRefundTime;
    @JsonProperty("IS_API_REFUND_ALLOWED")
    private Object isApiRefundAllowed;
    @JsonProperty("IS_OTP_Theme_Enabled")
    private Object isOtpThemeEnabled;
    @JsonProperty("SAP_CODE")
    private Object sapCode;
    @JsonProperty("REF_AMT_LIMIT_ON")
    private Object refAmtLimitOn;
    @JsonProperty("REF_AMT_LIMIT")
    private Object refAmtLimit;
    @JsonProperty("PPI_LIMITED_MERCHANT")
    private Object ppiLimitedMerchant;
    @JsonProperty("SECURE_LIMITED_MERCHANT")
    private Object secureLimitedMerchant;
    @JsonProperty("SECURE_STATUS_ENABLED")
    private Object secureStatusEnabled;
    @JsonProperty("MERCHANT_QR_TAG")
    private Object merchantQRTag;
    @JsonProperty("ENABLED_QR_TAG")
    private Object enabledQRTag;
    @JsonProperty("IS_TXN_ALLOWED")
    private Object isTxnAllowed;
    @JsonProperty("PAYOUT_DAYS")
    private Object payoutDays;


    public Object getId() {
        return id;
    }

    public Object getKyb_id() {
        return kyb_id;
    }

    public Object getIndustryTypeId() {
        return industryTypeId;
    }

    public Object getRequestId() {
        return requestId;
    }

    public Object getMid() {
        return mid;
    }

    public Object getActorTypeId() {
        return actorTypeId;
    }

    public Object getMerchantName() {
        return merchantName;
    }

    public Object getCurrency() {
        return currency;
    }

    public Object getKycApprovalId() {
        return kycApprovalId;
    }

    public Object getStatus() {
        return status;
    }

    public Object getCreatedBy() {
        return createdBy;
    }

    public Object getCreatedDate() {
        return createdDate;
    }

    public Object getModifiedBy() {
        return modifiedBy;
    }

    public Object getModifiedDate() {
        return modifiedDate;
    }

    public Object getOrderIdFlag() {
        return orderIdFlag;
    }

    public Object getNumberOfRetry() {
        return numberOfRetry;
    }

    public Object getIspeonenable() {
        return ispeonenable;
    }

    public Object getKeySize() {
        return keySize;
    }

    public Object getWalletEnabled() {
        return walletEnabled;
    }

    public Object getWalletRechargeEnabled() {
        return walletRechargeEnabled;
    }

    public Object getProfileId() {
        return profileId;
    }

    public Object getAlertFlag() {
        return alertFlag;
    }

    public Object getGuidEnabled() {
        return guidEnabled;
    }

    public Object getCallbackUrlEnabled() {
        return callbackUrlEnabled;
    }

    public Object getIsFluctuationEnabled() {
        return isFluctuationEnabled;
    }

    public Object getIsAggregator() {
        return isAggregator;
    }

    public Object getParentEntityId() {
        return parentEntityId;
    }

    public Object getPreformat() {
        return preformat;
    }

    public Object getSap() {
        return sap;
    }

    public Object getIsBlocked() {
        return isBlocked;
    }

    public Object getComment() {
        return comment;
    }

    public Object getS2sCallback_Enabled() {
        return s2sCallback_Enabled;
    }

    public Object getSsoId() {
        return ssoId;
    }

    public Object getPostConvThreshold() {
        return postConvThreshold;
    }

    public Object getPendingToSuccessRefundTime() {
        return pendingToSuccessRefundTime;
    }

    public Object getIsApiRefundAllowed() {
        return isApiRefundAllowed;
    }

    public Object getIsOtpThemeEnabled() {
        return isOtpThemeEnabled;
    }

    public Object getSapCode() {
        return sapCode;
    }

    public Object getRefAmtLimitOn() {
        return refAmtLimitOn;
    }

    public Object getRefAmtLimit() {
        return refAmtLimit;
    }

    public Object getPpiLimitedMerchant() {
        return ppiLimitedMerchant;
    }

    public Object getSecureLimitedMerchant() {
        return secureLimitedMerchant;
    }

    public Object getSecureStatusEnabled() {
        return secureStatusEnabled;
    }

    public Object getMerchantQRTag() {
        return merchantQRTag;
    }

    public Object getEnabledQRTag() {
        return enabledQRTag;
    }

    public Object getIsTxnAllowed() {
        return isTxnAllowed;
    }

    public Object getPayoutDays() {
        return payoutDays;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setKyb_id(Object kyb_id) {
        this.kyb_id = kyb_id;
    }

    public void setIndustryTypeId(Object industryTypeId) {
        this.industryTypeId = industryTypeId;
    }

    public void setRequestId(Object requestId) {
        this.requestId = requestId;
    }

    public void setMid(Object mid) {
        this.mid = mid;
    }

    public void setActorTypeId(Object actorTypeId) {
        this.actorTypeId = actorTypeId;
    }

    public void setMerchantName(Object merchantName) {
        this.merchantName = merchantName;
    }

    public void setCurrency(Object currency) {
        this.currency = currency;
    }

    public void setKycApprovalId(Object kycApprovalId) {
        this.kycApprovalId = kycApprovalId;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public void setCreatedBy(Object createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(Object createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedBy(Object modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public void setModifiedDate(Object modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setOrderIdFlag(Object orderIdFlag) {
        this.orderIdFlag = orderIdFlag;
    }

    public void setNumberOfRetry(Object numberOfRetry) {
        this.numberOfRetry = numberOfRetry;
    }

    public void setIspeonenable(Object ispeonenable) {
        this.ispeonenable = ispeonenable;
    }

    public void setKeySize(Object keySize) {
        this.keySize = keySize;
    }

    public void setWalletEnabled(Object walletEnabled) {
        this.walletEnabled = walletEnabled;
    }

    public void setWalletRechargeEnabled(Object walletRechargeEnabled) {
        this.walletRechargeEnabled = walletRechargeEnabled;
    }

    public void setProfileId(Object profileId) {
        this.profileId = profileId;
    }

    public void setAlertFlag(Object alertFlag) {
        this.alertFlag = alertFlag;
    }

    public void setGuidEnabled(Object guidEnabled) {
        this.guidEnabled = guidEnabled;
    }

    public void setCallbackUrlEnabled(Object callbackUrlEnabled) {
        this.callbackUrlEnabled = callbackUrlEnabled;
    }

    public void setIsFluctuationEnabled(Object isFluctuationEnabled) {
        this.isFluctuationEnabled = isFluctuationEnabled;
    }

    public void setIsAggregator(Object isAggregator) {
        this.isAggregator = isAggregator;
    }

    public void setParentEntityId(Object parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public void setPreformat(Object preformat) {
        this.preformat = preformat;
    }

    public void setSap(Object sap) {
        this.sap = sap;
    }

    public void setIsBlocked(Object isBlocked) {
        this.isBlocked = isBlocked;
    }

    public void setComment(Object comment) {
        this.comment = comment;
    }

    public void setS2sCallback_Enabled(Object s2sCallback_Enabled) {
        this.s2sCallback_Enabled = s2sCallback_Enabled;
    }

    public void setSsoId(Object ssoId) {
        this.ssoId = ssoId;
    }

    public void setPostConvThreshold(Object postConvThreshold) {
        this.postConvThreshold = postConvThreshold;
    }

    public void setPendingToSuccessRefundTime(Object pendingToSuccessRefundTime) {
        this.pendingToSuccessRefundTime = pendingToSuccessRefundTime;
    }

    public void setIsApiRefundAllowed(Object isApiRefundAllowed) {
        this.isApiRefundAllowed = isApiRefundAllowed;
    }

    public void setIsOtpThemeEnabled(Object isOtpThemeEnabled) {
        this.isOtpThemeEnabled = isOtpThemeEnabled;
    }

    public void setSapCode(Object sapCode) {
        this.sapCode = sapCode;
    }

    public void setRefAmtLimitOn(Object refAmtLimitOn) {
        this.refAmtLimitOn = refAmtLimitOn;
    }

    public void setRefAmtLimit(Object refAmtLimit) {
        this.refAmtLimit = refAmtLimit;
    }

    public void setPpiLimitedMerchant(Object ppiLimitedMerchant) {
        this.ppiLimitedMerchant = ppiLimitedMerchant;
    }

    public void setSecureLimitedMerchant(Object secureLimitedMerchant) {
        this.secureLimitedMerchant = secureLimitedMerchant;
    }

    public void setSecureStatusEnabled(Object secureStatusEnabled) {
        this.secureStatusEnabled = secureStatusEnabled;
    }

    public void setMerchantQRTag(Object merchantQRTag) {
        this.merchantQRTag = merchantQRTag;
    }

    public void setEnabledQRTag(Object enabledQRTag) {
        this.enabledQRTag = enabledQRTag;
    }

    public void setIsTxnAllowed(Object isTxnAllowed) {
        this.isTxnAllowed = isTxnAllowed;
    }

    public void setPayoutDays(Object payoutDays) {
        this.payoutDays = payoutDays;
    }
}
