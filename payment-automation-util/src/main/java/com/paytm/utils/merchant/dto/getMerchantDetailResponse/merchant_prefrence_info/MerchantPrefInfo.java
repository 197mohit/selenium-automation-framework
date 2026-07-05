package com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info;

import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.merchantPrefDTO.MerchantPreferenceInfos;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.merchantPrefDTO.ResultInfo;

import java.util.List;

public class MerchantPrefInfo {

    private ResultInfo resultInfo;
    private String merchantId;
    private List<MerchantPreferenceInfos> merchantPreferenceInfos;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public List<MerchantPreferenceInfos> getMerchantPreferenceInfos() {
        return merchantPreferenceInfos;
    }

    public void setMerchantPreferenceInfos(List<MerchantPreferenceInfos> merchantPreferenceInfos) {
        this.merchantPreferenceInfos = merchantPreferenceInfos;
    }
}
