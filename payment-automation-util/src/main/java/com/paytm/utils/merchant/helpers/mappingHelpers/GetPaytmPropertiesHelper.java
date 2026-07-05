package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetPaytmProperties;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Created by ankuragarwal on 13/9/18
 */
public class GetPaytmPropertiesHelper {

    private ValidateSoftly validateSoftly;
    private JsonPath jsonPath;

    public GetPaytmPropertiesHelper(String propertyName) {
        Response response = GetPaytmProperties.executeGetPaytmProperties(propertyName);
        jsonPath = response.jsonPath();
        validateSoftly = ValidateSoftly.getInstance();
    }

    public GetPaytmPropertiesHelper validateName(String propertyName) {
        this.validateSoftly.validate(jsonPath.getString("name"),
                "", "Validated Name in response").isEqualTo(propertyName);
        return this;
    }

    public  GetPaytmPropertiesHelper validatePropertyValueNotEmpty() {
        this.validateSoftly.validate(jsonPath.getString("value"),
                "", "Validated non empty property value").isNotEmpty();
        return this;
    }

    public void assertAll() {
        this.validateSoftly.assertAll();
    }

}
