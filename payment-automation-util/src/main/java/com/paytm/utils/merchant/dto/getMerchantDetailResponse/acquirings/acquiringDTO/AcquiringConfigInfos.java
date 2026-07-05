package com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings.acquiringDTO;

import java.util.List;

public class AcquiringConfigInfos {
    private boolean enableStatus;
    private String fromAoaMerchant;
    private String mcc;
    private String merchantId;
    private String payMethod;
    private String preference;
    private String recordId;
    private String serviceInstId;
    private String serviceInstName;
    private String subServiceInstIds;
    private List<String> supportRegions;


    public boolean isEnableStatus() {
        return enableStatus;
    }

    public String getFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public String getMcc() {
        return mcc;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public String getPreference() {
        return preference;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getServiceInstId() {
        return serviceInstId;
    }

    public String getServiceInstName() {
        return serviceInstName;
    }

    public String getSubServiceInstIds() {
        return subServiceInstIds;
    }

    public List<String> getSupportRegions() {
        return supportRegions;
    }

    public void setEnableStatus(boolean enableStatus) {
        this.enableStatus = enableStatus;
    }

    public void setFromAoaMerchant(String fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public void setServiceInstId(String serviceInstId) {
        this.serviceInstId = serviceInstId;
    }

    public void setServiceInstName(String serviceInstName) {
        this.serviceInstName = serviceInstName;
    }

    public void setSubServiceInstIds(String subServiceInstIds) {
        this.subServiceInstIds = subServiceInstIds;
    }

    public void setSupportRegions(List<String> supportRegions) {
        this.supportRegions = supportRegions;
    }
}
