package com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.extendedInfoDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedInfo {

    private String userId;
    private String merchantLimit;
    private String isMerchant;
    private String alipayMid;
    private String addProofnoPersonal;
    private String bankAccNo;
    private String businessName, additionalEmails;
    private String callbackUrlEnabled;
    private String comment;
    private String contactFname, contactLname, contactMname, contactMobile;
    private int custCommPref, custRefCommPref;
    private String eciStatus, entityId, entityKey, idProofnoPersonal, ifscCode, invoiceEmail;
    private int isApiRefundAllowed, isOtpThemeEnabled, maxAmountForComplexRefund, merchRefCommPref;
    private String isDownloaded;
    private boolean isPeonEnable, urbanAirshipEnabled;
    @JsonProperty("ONPAYTM")
    private boolean oNPAYTM;
    private boolean blocked;
    private String keySize, kycStatus;
    private String merchantName, merchantWapForcedTheme, merchantWebForcedTheme, merchCommPref, minPartialRenewalPercentage;
    private String numberOfRetry, panNoBusiness, panNoPersonal, peonRequestType, peonServiceName;
    private String platformType, primaryEmail, productCode, s2sCallbackEnabled, sap, secondaryEmail;
    private String secondaryFirstname, secondaryLastname, secondaryMobileno, secondaryPhoneno;
    private int secureStatusEnabled;
    private String signatoryName, signedTime, status, urbanAirshipHash, walletEnabled, walletRechargeRnabled, aggregatorMid, gstin, paymentInvoiceMobile, paymentInvoiceEmail;
    private String chargeBackEmails;
    private String kybId;


    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getPaymentInvoiceMobile() {
        return paymentInvoiceMobile;
    }

    public void setPaymentInvoiceMobile(String paymentInvoiceMobile) {
        this.paymentInvoiceMobile = paymentInvoiceMobile;
    }

    public String getPaymentInvoiceEmail() {
        return paymentInvoiceEmail;
    }

    public void setPaymentInvoiceEmail(String paymentInvoiceEmail) {
        this.paymentInvoiceEmail = paymentInvoiceEmail;
    }

    public boolean getBlocked() {
        return blocked;
    }

    public String getIsMerchant() {
        return isMerchant;
    }

    public String getAggregatorMid() {
        return aggregatorMid;
    }

    public String getAddProofnoPersonal() {
        return addProofnoPersonal;
    }

    public String getBankAccNo() {
        return bankAccNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getCallbackUrlEnabled() {
        return callbackUrlEnabled;
    }

    public String getComment() {
        return comment;
    }

    public String getContactFname() {
        return contactFname;
    }

    public String getContactLname() {
        return contactLname;
    }

    public String getContactMname() {
        return contactMname;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public int getCustCommPref() {
        return custCommPref;
    }

    public int getCustRefCommPref() {
        return custRefCommPref;
    }

    public String getEciStatus() {
        return eciStatus;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityKey() {
        return entityKey;
    }

    public String getIdProofnoPersonal() {
        return idProofnoPersonal;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getInvoiceEmail() {
        return invoiceEmail;
    }

    public int getIsApiRefundAllowed() {
        return isApiRefundAllowed;
    }

    public int getIsOtpThemeEnabled() {
        return isOtpThemeEnabled;
    }

    public int getMaxAmountForComplexRefund() {
        return maxAmountForComplexRefund;
    }

    public int getMerchRefCommPref() {
        return merchRefCommPref;
    }

    public String getIsDownloaded() {
        return isDownloaded;
    }

    public boolean getIsPeonEnable() {
        return isPeonEnable;
    }

    public boolean getONPAYTM() {
        return oNPAYTM;
    }

    public boolean getUrbanAirshipEnabled() {
        return urbanAirshipEnabled;
    }

    public String getKeySize() {
        return keySize;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantWapForcedTheme() {
        return merchantWapForcedTheme;
    }

    public String getMerchantWebForcedTheme() {
        return merchantWebForcedTheme;
    }

    public String getMerchCommPref() {
        return merchCommPref;
    }

    public String getMinPartialRenewalPercentage() {
        return minPartialRenewalPercentage;
    }

    public String getNumberOfRetry() {
        return numberOfRetry;
    }

    public String getPanNoBusiness() {
        return panNoBusiness;
    }

    public String getPanNoPersonal() {
        return panNoPersonal;
    }

    public String getPeonRequestType() {
        return peonRequestType;
    }

    public String getPeonServiceName() {
        return peonServiceName;
    }

    public String getPlatformType() {
        return platformType;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getS2sCallbackEnabled() {
        return s2sCallbackEnabled;
    }

    public String getSap() {
        return sap;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public String getSecondaryFirstname() {
        return secondaryFirstname;
    }

    public String getSecondaryLastname() {
        return secondaryLastname;
    }

    public String getSecondaryMobileno() {
        return secondaryMobileno;
    }

    public String getSecondaryPhoneno() {
        return secondaryPhoneno;
    }

    public int getSecureStatusEnabled() {
        return secureStatusEnabled;
    }

    public String getSignatoryName() {
        return signatoryName;
    }

    public String getSignedTime() {
        return signedTime;
    }

    public String getStatus() {
        return status;
    }

    public String getUrbanAirshipHash() {
        return urbanAirshipHash;
    }

    public String getWalletEnabled() {
        return walletEnabled;
    }

    public String getWalletRechargeRnabled() {
        return walletRechargeRnabled;
    }

    public String getAdditionalEmails() {
        return additionalEmails;
    }

    public String getAlipayMid() {
        return alipayMid;
    }

    public String getMerchantLimit() {
        return merchantLimit;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMerchantLimit(String merchantLimit) {
        this.merchantLimit = merchantLimit;
    }

    public void setAddProofnoPersonal(String addProofnoPersonal) {
        this.addProofnoPersonal = addProofnoPersonal;
    }

    public void setBankAccNo(String bankAccNo) {
        this.bankAccNo = bankAccNo;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setCallbackUrlEnabled(String callbackUrlEnabled) {
        this.callbackUrlEnabled = callbackUrlEnabled;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setContactFname(String contactFname) {
        this.contactFname = contactFname;
    }

    public void setContactLname(String contactLname) {
        this.contactLname = contactLname;
    }

    public void setContactMname(String contactMname) {
        this.contactMname = contactMname;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public void setCustCommPref(int custCommPref) {
        this.custCommPref = custCommPref;
    }

    public void setCustRefCommPref(int custRefCommPref) {
        this.custRefCommPref = custRefCommPref;
    }

    public void setEciStatus(String eciStatus) {
        this.eciStatus = eciStatus;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setEntityKey(String entityKey) {
        this.entityKey = entityKey;
    }

    public void setIdProofnoPersonal(String idProofnoPersonal) {
        this.idProofnoPersonal = idProofnoPersonal;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public void setInvoiceEmail(String invoiceEmail) {
        this.invoiceEmail = invoiceEmail;
    }

    public void setIsApiRefundAllowed(int isApiRefundAllowed) {
        this.isApiRefundAllowed = isApiRefundAllowed;
    }

    public void setIsOtpThemeEnabled(int isOtpThemeEnabled) {
        this.isOtpThemeEnabled = isOtpThemeEnabled;
    }

    public void setMaxAmountForComplexRefund(int maxAmountForComplexRefund) {
        this.maxAmountForComplexRefund = maxAmountForComplexRefund;
    }

    public void setMerchRefCommPref(int merchRefCommPref) {
        this.merchRefCommPref = merchRefCommPref;
    }

    public void setIsDownloaded(String isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public void setIsPeonEnable(boolean peonEnable) {
        isPeonEnable = peonEnable;
    }

    public void setONPAYTM(boolean oNPAYTM) {
        this.oNPAYTM = oNPAYTM;
    }

    public void setUrbanAirshipEnabled(boolean urbanAirshipEnabled) {
        this.urbanAirshipEnabled = urbanAirshipEnabled;
    }

    public void setKeySize(String keySize) {
        this.keySize = keySize;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setMerchantWapForcedTheme(String merchantWapForcedTheme) {
        this.merchantWapForcedTheme = merchantWapForcedTheme;
    }

    public void setMerchantWebForcedTheme(String merchantWebForcedTheme) {
        this.merchantWebForcedTheme = merchantWebForcedTheme;
    }

    public void setMerchCommPref(String merchCommPref) {
        this.merchCommPref = merchCommPref;
    }

    public void setMinPartialRenewalPercentage(String minPartialRenewalPercentage) {
        this.minPartialRenewalPercentage = minPartialRenewalPercentage;
    }

    public void setNumberOfRetry(String numberOfRetry) {
        this.numberOfRetry = numberOfRetry;
    }

    public void setPanNoBusiness(String panNoBusiness) {
        this.panNoBusiness = panNoBusiness;
    }

    public void setPanNoPersonal(String panNoPersonal) {
        this.panNoPersonal = panNoPersonal;
    }

    public void setPeonRequestType(String peonRequestType) {
        this.peonRequestType = peonRequestType;
    }

    public void setPeonServiceName(String peonServiceName) {
        this.peonServiceName = peonServiceName;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public void setS2sCallbackEnabled(String s2sCallbackEnabled) {
        this.s2sCallbackEnabled = s2sCallbackEnabled;
    }

    public void setSap(String sap) {
        this.sap = sap;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    public void setSecondaryFirstname(String secondaryFirstname) {
        this.secondaryFirstname = secondaryFirstname;
    }

    public void setSecondaryLastname(String secondaryLastname) {
        this.secondaryLastname = secondaryLastname;
    }

    public void setSecondaryMobileno(String secondaryMobileno) {
        this.secondaryMobileno = secondaryMobileno;
    }

    public void setSecondaryPhoneno(String secondaryPhoneno) {
        this.secondaryPhoneno = secondaryPhoneno;
    }

    public void setSecureStatusEnabled(int secureStatusEnabled) {
        this.secureStatusEnabled = secureStatusEnabled;
    }

    public void setSignatoryName(String signatoryName) {
        this.signatoryName = signatoryName;
    }

    public void setSignedTime(String signedTime) {
        this.signedTime = signedTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUrbanAirshipHash(String urbanAirshipHash) {
        this.urbanAirshipHash = urbanAirshipHash;
    }

    public void setWalletEnabled(String walletEnabled) {
        this.walletEnabled = walletEnabled;
    }

    public void setWalletRechargeRnabled(String walletRechargeRnabled) {
        this.walletRechargeRnabled = walletRechargeRnabled;
    }

    public void setAggregatorMid(String aggregatorMid) {
        this.aggregatorMid = aggregatorMid;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setAdditionalEmails(String additionalEmails) {
        this.additionalEmails = additionalEmails;
    }

    public void setIsMerchant(String isMerchant) {
        this.isMerchant = isMerchant;
    }

    public void setAlipayMid(String alipayMid) {
        this.alipayMid = alipayMid;
    }

    public String getChargeBackEmails() {
        return chargeBackEmails;
    }

    public void setChargeBackEmails(String chargeBackEmails) {
        this.chargeBackEmails = chargeBackEmails;
    }


    public String getKybId() {
        return kybId;
    }
    public void setKybId(String kybId){
        this.kybId=kybId;
    }
}