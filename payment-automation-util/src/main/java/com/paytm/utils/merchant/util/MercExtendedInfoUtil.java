package com.paytm.utils.merchant.util;

import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import org.assertj.core.api.SoftAssertions;

public class MercExtendedInfoUtil {
    private GetMerchantHelper getMerchantHelper;
    private SoftAssertions softly = new SoftAssertions();
    private MerchExtendedInfo merchExtendedInfo;

    public MercExtendedInfoUtil(GetMerchantHelper getMerchantHelper) {
        this.getMerchantHelper = getMerchantHelper;
        merchExtendedInfo = getMerchantHelper.getMerchantExtendedInfo();
    }

    public MercExtendedInfoUtil(GetMerchantHelper getMerchantHelper, SoftAssertions softly) {
        this.getMerchantHelper = getMerchantHelper;
        this.softly = softly;
        merchExtendedInfo = getMerchantHelper.getMerchantExtendedInfo();
    }

    public MercExtendedInfoUtil verifyRetryCount() {
        int actualRetryCount = Integer.parseInt(merchExtendedInfo.getExtendedInfo().getNumberOfRetry());
        if(actualRetryCount > 0) {
            Reporter.report.info("Retry count is greater than 0");
        }
        return this;
    }

    public MercExtendedInfoUtil setSoftly(SoftAssertions softly) {
        this.softly = softly;
        return this;
    }

    public SoftAssertions getSoftly() {
        return softly;
    }

    public MercExtendedInfoUtil AssertAll() {
        this.softly.assertAll();
        return this;
    }

}
