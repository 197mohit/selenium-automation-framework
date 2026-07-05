package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetEntityOfferDetails;
import com.paytm.utils.merchant.dto.mappingService.response.EntityOfferDetailsDTO;

/**
 * Created by ankuragarwal on 13/9/18
 */
public class GetEntityOfferDetailHelper {

    private ValidateSoftly validateSoftly;
    private EntityOfferDetailsDTO entityOfferDetailsDTO;

    public GetEntityOfferDetailHelper(String mid, String channel) {
        validateSoftly = ValidateSoftly.getInstance();
        entityOfferDetailsDTO = GetEntityOfferDetails
                .executeGetEntityOffireDetail(mid, channel, "retail");
    }

    public GetEntityOfferDetailHelper validateChannel(String channel) {
        this.validateSoftly.validate(entityOfferDetailsDTO.getChannel(),
                "", "Validated channel name").isEqualTo(channel);
        return this;
    }

    public GetEntityOfferDetailHelper validateWebsiteName(String websiteName) {
        this.validateSoftly.validate(entityOfferDetailsDTO.getWebsite(),
                "", "Validated websiteName").isEqualTo(websiteName);
        return this;
    }

    public GetEntityOfferDetailHelper validateStatus(String status) {
        this.validateSoftly.validate(entityOfferDetailsDTO.getStatus(),
                "", "Validated status").isEqualTo(status);
        return this;
    }

    public GetEntityOfferDetailHelper validateMessage(String message) {
        this.validateSoftly.validate(entityOfferDetailsDTO.getMessage(),
                "", "Validated message").isEqualTo(message);
        return this;
    }

    public GetEntityOfferDetailHelper validateMID(String mid) {
        this.validateSoftly.validate(entityOfferDetailsDTO.getMid(),
                "", "Validated mid").isEqualTo(mid);
        return this;
    }

    public void assertAll() {
        this.validateSoftly.assertAll();
    }

}
