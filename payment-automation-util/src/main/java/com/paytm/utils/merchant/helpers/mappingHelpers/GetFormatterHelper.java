package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetFormatter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Created by ankuragarwal on 27/9/18
 */
public class GetFormatterHelper {

    private ValidateSoftly validateSoftly;
    private JsonPath jsonPath;

    public GetFormatterHelper(String bankCode, String payMethod) {
        Response response = GetFormatter.executeGetFormatter(bankCode, payMethod);
        jsonPath = response.jsonPath();
        validateSoftly = ValidateSoftly.getInstance();
    }

    public GetFormatterHelper validateId(int id) {
        this.validateSoftly.validate(jsonPath.getInt("id"),
                "", "Validated id in response").isEqualTo(id);
        return this;
    }

    public  GetFormatterHelper validateBankCode(String bankCode) {
        this.validateSoftly.validate(jsonPath.getString("bankCode"),
                "", "Validated bankCode in response").isEqualTo(bankCode);
        return this;
    }

    public  GetFormatterHelper validatePayMethod(String payMethod) {
        this.validateSoftly.validate(jsonPath.getString("payMethod"),
                "", "Validated payMethod in response").isEqualTo(payMethod);
        return this;
    }

    public  GetFormatterHelper validateFormatterName(String formatterName) {
        this.validateSoftly.validate(jsonPath.getString("formatterName"),
                "", "Validated formatterName in response").isEqualTo(formatterName);
        return this;
    }

    public  GetFormatterHelper validateStatus(boolean status) {
        this.validateSoftly.validate(jsonPath.getBoolean("status"),
                "", "Validated status in response").isEqualTo(status);
        return this;
    }

    public  GetFormatterHelper validateParams(String params) {
        this.validateSoftly.validate(jsonPath.getString("params"),
                "", "Validated params in response").isEqualTo(params);
        return this;
    }

    public void assertAll() {
        this.validateSoftly.assertAll();
    }
}
