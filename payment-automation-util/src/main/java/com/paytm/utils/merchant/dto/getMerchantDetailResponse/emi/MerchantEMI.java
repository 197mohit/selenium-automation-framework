package com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi;

import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.emiDTO.EmiConfigInfos;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.emiDTO.ResultInfo;

import java.util.List;

public class MerchantEMI {
    private ResultInfo resultInfo;
    private List<EmiConfigInfos> emiConfigInfos;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public List<EmiConfigInfos> getEmiConfigInfos() {
        return emiConfigInfos;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public void setEmiConfigInfos(List<EmiConfigInfos> emiConfigInfos) {
        this.emiConfigInfos = emiConfigInfos;
    }
}
