package com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.merchantPrefDTO;

public class MerchantPreferenceInfos {

    private String prefStatus;
    private String prefType;
    private String prefValue;

    public String getPrefStatus() {
        return prefStatus;
    }

    public void setPrefStatus(String prefStatus) {
        this.prefStatus = prefStatus;
    }

    public String getPrefType() {
        return prefType;
    }

    public void setPrefType(String prefType) {
        this.prefType = prefType;
    }

    public String getPrefValue() {
        return prefValue;
    }

    public void setPrefValue(String prefValue) {
        this.prefValue = prefValue;
    }
}
