package com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.emiDTO;

public class EmiConfigInfos {
    private IssuingBank issuingBank;
    private String recordId;
    private EmiInfo emiInfo;
    private String merchantId;

    public IssuingBank getIssuingBank() {
        return issuingBank;
    }

    public String getRecordId() {
        return recordId;
    }

    public EmiInfo getEmiInfo() {
        return emiInfo;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setIssuingBank(IssuingBank issuingBank) {
        this.issuingBank = issuingBank;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public void setEmiInfo(EmiInfo emiInfo) {
        this.emiInfo = emiInfo;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}
