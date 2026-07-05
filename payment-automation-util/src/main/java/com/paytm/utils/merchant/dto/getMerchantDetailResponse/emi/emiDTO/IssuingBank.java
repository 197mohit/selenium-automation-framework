package com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.emiDTO;

public class IssuingBank {
    private String issuingBankId;
    private String issuingBankName;

    public String getIssuingBankId() {
        return issuingBankId;
    }

    public void setIssuingBankId(String issuingBankId) {
        this.issuingBankId = issuingBankId;
    }

    public String getIssuingBankName() {
        return issuingBankName;
    }

    public void setIssuingBankName(String issuingBankName) {
        this.issuingBankName = issuingBankName;
    }
}
