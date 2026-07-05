package com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo;

import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.extendedInfoDTO.ExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.extendedInfoDTO.ResultInfo;

public class MerchExtendedInfo {
    private ResultInfo resultInfo;
    private String merchantId;
    private ExtendedInfo extendedInfo;

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

    public ExtendedInfo getExtendedInfo() {
        return extendedInfo;
    }

    public void setExtendedInfo(ExtendedInfo extendedInfo) {
        this.extendedInfo = extendedInfo;
    }
}
