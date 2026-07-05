package com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings;

import com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings.acquiringDTO.AcquiringConfigInfos;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings.acquiringDTO.ResultInfo;

import java.util.List;

public class MerchantAcquiring {

    private ResultInfo resultInfo;
    private List<AcquiringConfigInfos> acquiringConfigInfos;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public List<AcquiringConfigInfos> getAcquiringConfigInfos() {
        return acquiringConfigInfos;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public void setAcquiringConfigInfos(List<AcquiringConfigInfos> acquiringConfigInfos) {
        this.acquiringConfigInfos = acquiringConfigInfos;
    }
}
