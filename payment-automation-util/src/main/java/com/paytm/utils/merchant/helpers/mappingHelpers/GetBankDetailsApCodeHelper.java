package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetBankDetailsAPCode;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Created by ankuragarwal on 12/10/18
 */
public class GetBankDetailsApCodeHelper {

    private ValidateSoftly validateSoftly;
    private JsonPath jsonPath;

    public GetBankDetailsApCodeHelper(String alipayCode) {
        Response response = GetBankDetailsAPCode.executeGetBankDetailsAPCode(alipayCode);
        jsonPath = response.jsonPath();
        validateSoftly = ValidateSoftly.getInstance();
    }

    public GetBankDetailsApCodeHelper validateBankId(String bankId) {
        this.validateSoftly.validate(jsonPath.getString("bankId"),
                "", "Validated bankId in response").isEqualTo(bankId);
        return this;
    }

    public GetBankDetailsApCodeHelper validateBankName(String bankName) {
        this.validateSoftly.validate(jsonPath.getString("bankName"),
                "", "Validated bankName in response").isEqualTo(bankName);
        return this;
    }

    public GetBankDetailsApCodeHelper validateBankCode(String bankCode) {
        this.validateSoftly.validate(jsonPath.getString("bankCode"),
                "", "Validated bankCode in response").isEqualTo(bankCode);
        return this;
    }

    public GetBankDetailsApCodeHelper validateBankDisplayName(String bankDisplayName) {
        this.validateSoftly.validate(jsonPath.getString("bankDisplayName"),
                "", "Validated bankDisplayName in response").isEqualTo(bankDisplayName);
        return this;
    }

    public GetBankDetailsApCodeHelper validateBankKey(String bankKey) {
        this.validateSoftly.validate(jsonPath.getString("bankKey"),
                "", "Validated bankKey in response").isEqualTo(bankKey);
        return this;
    }

    public GetBankDetailsApCodeHelper validateAlipayBankCode(String alipayBankCode) {
        this.validateSoftly.validate(jsonPath.getString("alipayBankCode"),
                "", "Validated alipayBankCode in response").isEqualTo(alipayBankCode);
        return this;
    }

    public GetBankDetailsApCodeHelper validateBankWebLogo(String bankWebLogo) {
        this.validateSoftly.validate(jsonPath.getString("bankWebLogo"),
                "", "Validated bankWebLogo in response").isEqualTo(bankWebLogo);
        return this;
    }

    public GetBankDetailsApCodeHelper validateBankWapLogo(String bankWapLogo) {
        this.validateSoftly.validate(jsonPath.getString("bankWapLogo"),
                "", "Validated bankWapLogo in response").isEqualTo(bankWapLogo);
        return this;
    }

    public GetBankDetailsApCodeHelper validateStatus(String status) {
        this.validateSoftly.validate(jsonPath.getString("status"),
                "", "Validated status in response").isEqualTo(status);
        return this;
    }

    public void assertAll() {
        this.validateSoftly.assertAll();
    }
}
