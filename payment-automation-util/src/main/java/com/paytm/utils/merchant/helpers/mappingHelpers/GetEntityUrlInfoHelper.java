package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetEntityUrlInfo;
import com.paytm.utils.merchant.dto.mappingService.response.EntityUrlInfoResponse;

/**
 * Created by ankuragarwal on 12/9/18
 */
public class GetEntityUrlInfoHelper {

    private ValidateSoftly validateSoftly;
    private EntityUrlInfoResponse entityUrlInfoResponse;

    public GetEntityUrlInfoHelper(String mid, String requestType) {
        validateSoftly = ValidateSoftly.getInstance();
        entityUrlInfoResponse = GetEntityUrlInfo.executeEntityUrlInfo(mid, requestType);
    }

    public GetEntityUrlInfoHelper validateMerchantId(String merchantId) {
        this.validateSoftly.validate(entityUrlInfoResponse.getMerchantId(),
                "", "Validated merchantId").isEqualTo(merchantId);
        return this;
    }

    public GetEntityUrlInfoHelper validatePostBackUrl(String postBackurl) {
        this.validateSoftly.validate(entityUrlInfoResponse.getPostBackurl(),
                "", "Validated post back url").isEqualTo(postBackurl);
        return this;
    }

    public GetEntityUrlInfoHelper validateUrlTypeId(String urlTypeId) {
        this.validateSoftly.validate(entityUrlInfoResponse.getUrlTypeId(),
                "", "Validated urlTypeId").isEqualTo(urlTypeId);
        return this;
    }

    public GetEntityUrlInfoHelper validateStatus(String status) {
        this.validateSoftly.validate(entityUrlInfoResponse.getStatus(),
                "", "Validated status").isEqualTo(status);
        return this;
    }

    public GetEntityUrlInfoHelper validateWebsiteName(String websiteName) {
        this.validateSoftly.validate(entityUrlInfoResponse.getWebsiteName(),
                "", "Validated websiteName").isEqualTo(websiteName);
        return this;
    }

    public GetEntityUrlInfoHelper validateNotificationUrl(String notificationStatusUrl) {
        this.validateSoftly.validate(entityUrlInfoResponse.getNotificationStatusUrl(),
                "", "Validated notificationStatusUrl").isEqualTo(notificationStatusUrl);
        return this;
    }

    public GetEntityUrlInfoHelper validateMID(String mid) {
        this.validateSoftly.validate(entityUrlInfoResponse.getMid(),
                "", "Validated MID").isEqualTo(mid);
        return this;
    }

    public void assertAll() {
        this.validateSoftly.assertAll();
    }

}
