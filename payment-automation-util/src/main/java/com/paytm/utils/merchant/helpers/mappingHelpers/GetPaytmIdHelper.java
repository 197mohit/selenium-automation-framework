package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetPaytmId;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Created by ankuragarwal on 12/10/18
 */
public class GetPaytmIdHelper {

    private ValidateSoftly validateSoftly;
    private JsonPath jsonPath;

    public GetPaytmIdHelper(String alipayId) {
        Response response = GetPaytmId.executeGetPaytmId(alipayId);
        jsonPath = response.jsonPath();
        validateSoftly = ValidateSoftly.getInstance();
    }

    public GetPaytmIdHelper validateId(String id) {
        this.validateSoftly.validate(jsonPath.getString("id"),
                "", "Validated id in response").isEqualTo(id);
        return this;
    }

    public GetPaytmIdHelper validatePaytmId(String paytmId) {
        this.validateSoftly.validate(jsonPath.getString("paytmId"),
                "", "Validated paytmId in response").isEqualTo(paytmId);
        return this;
    }

    public GetPaytmIdHelper validateWalletId(String paytmWalletId) {
        this.validateSoftly.validate(jsonPath.getString("paytmWalletId"),
                "", "Validated paytmWalletId in response").isEqualTo(paytmWalletId);
        return this;
    }

    public GetPaytmIdHelper validateAlipayId(String alipayId) {
        this.validateSoftly.validate(jsonPath.getString("alipayId"),
                "", "Validated alipayId in response").isEqualTo(alipayId);
        return this;
    }

    public GetPaytmIdHelper validateAlipayWalletId(String alipayWalletId) {
        this.validateSoftly.validate(jsonPath.getString("alipayWalletId"),
                "", "Validated alipayWalletId in response").isEqualTo(alipayWalletId);
        return this;
    }

    public GetPaytmIdHelper validateContractPayload(String contractPayload) {
        this.validateSoftly.validate(jsonPath.getString("contractPayload"),
                "", "Validated contractPayload in response").isEqualTo(contractPayload);
        return this;
    }

    public GetPaytmIdHelper validateOfficalName(String officialName) {
        this.validateSoftly.validate(jsonPath.getString("officialName"),
                "", "Validated officialName in response").isEqualTo(officialName);
        return this;
    }

    public GetPaytmIdHelper validateIndustryTypeId(String industryTypeId) {
        this.validateSoftly.validate(jsonPath.getString("industryTypeId"),
                "", "Validated industryTypeId in response").isEqualTo(industryTypeId);
        return this;
    }

    public void assertAll() {
        this.validateSoftly.assertAll();
    }
}
