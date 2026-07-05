package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetMerchantExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.extendedInfoDTO.ExtendedInfo;

/**
 * @author ankuragarwal
 * Date: 11/09/18
 */
public class GetMerchExtndInfoHelper {

    private MerchExtendedInfo merchExtendedInfo;
    private ValidateSoftly validateSoftly;
    private ExtendedInfo extendedInfo;

    public GetMerchExtndInfoHelper(String mid) {
        merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(mid);
        validateSoftly = ValidateSoftly.getInstance();
        extendedInfo = merchExtendedInfo.getExtendedInfo();
    }

    public GetMerchExtndInfoHelper validateSuccessMessage() {
        validateSoftly.validate(merchExtendedInfo.getResultInfo().getMessaage(),
                "", "Validated Success message").isEqualToIgnoringCase("Success");
        return this;
    }

    public GetMerchExtndInfoHelper validateMerchantId(String mid) {
        validateSoftly.validate(merchExtendedInfo.getMerchantId(),
                "", "Validated MID").isEqualTo(mid);
        return this;
    }

    public GetMerchExtndInfoHelper validateExtendedInfoStatus(String status) {
        validateSoftly.validate(extendedInfo.getStatus(),
                "", "Validated extended info status").isEqualToIgnoringCase(status);
        return this;
    }

    public GetMerchExtndInfoHelper validateContractId(String contractId) {
        validateSoftly.validate(extendedInfo.getProductCode(),
                "", "Validated Default contract id in Extended Info").containsIgnoringCase(contractId);
        return this;
    }

    public String fetchMobileNumber()
    {
        return this.merchExtendedInfo.getExtendedInfo().getContactMobile();
    }


    public void assertAll() {
        validateSoftly.assertAll();
    }

}
