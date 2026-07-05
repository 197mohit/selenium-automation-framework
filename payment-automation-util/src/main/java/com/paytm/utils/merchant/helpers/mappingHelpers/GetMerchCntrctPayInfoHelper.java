package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetContractPaymentInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO.PayMethodFeeInfos;

/**
 * @author ankuragarwal
 * Date: 11/09/18
 */
public class GetMerchCntrctPayInfoHelper {

    private PayMethodFeeInfos payMethodFeeInfos;
    private ValidateSoftly validateSoftly;

    public GetMerchCntrctPayInfoHelper(String mid, String contractId) {
        payMethodFeeInfos = GetContractPaymentInfo.executeGetContractPayInfo(mid, contractId);
        validateSoftly = ValidateSoftly.getInstance();
    }

    public GetMerchCntrctPayInfoHelper validatePayMethod(String paymethod) {
        validateSoftly.validate(payMethodFeeInfos.getPayMethod(),
                "", "Validated Paymethod").isEqualToIgnoringCase(paymethod);
        return this;
    }

    public GetMerchCntrctPayInfoHelper validatePayMethodNotEmpty() {
        validateSoftly.validate(payMethodFeeInfos.getFeeRanges(),
                "", "Validate fee ranges list are available or not").isNotEmpty();
        return this;
    }

    public GetMerchCntrctPayInfoHelper validateFeeRateNotEmpty() {
        validateSoftly.validate(payMethodFeeInfos.getFeeRanges().get(0).getFeeRate(),
                "", "Validated fee ranges").isNotNull();
        return this;
    }

    public void assertAll() {
        validateSoftly.assertAll();
    }


}
