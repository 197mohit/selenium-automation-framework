package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetMerchPreferenceInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.MerchantPrefInfo;

/**
 * @author ankuragarwal
 * Date: 12/09/18
 */
public class GetMerchPreferenceInfoHelper {

    private ValidateSoftly validateSoftly;
    private MerchantPrefInfo merchantPrefInfo;

    public GetMerchPreferenceInfoHelper(String mid) {
        merchantPrefInfo = GetMerchPreferenceInfo.executeGetMercPreferenceInfo(mid);
        validateSoftly = ValidateSoftly.getInstance();
    }

    public GetMerchPreferenceInfoHelper validateStatuCode() {
        this.validateSoftly.validate(merchantPrefInfo.getResultInfo().getMessaage(),
                "", "Validated Success Message").isEqualTo("Success");
        return this;
    }

    public GetMerchPreferenceInfoHelper validateMerchantId(String mid) {
        this.validateSoftly.validate(merchantPrefInfo.getMerchantId(),
                "", "Validated MID in reponse body").isEqualTo(mid);
        return this;
    }

    public GetMerchPreferenceInfoHelper validatePreferenceInfoListNotEmty() {
        this.validateSoftly.validate(merchantPrefInfo.getMerchantPreferenceInfos(),
                "", "Validated preference info list not empty").isNotEmpty();
        return this;
    }

    public void assertAll() {
        validateSoftly.assertAll();
    }
}
