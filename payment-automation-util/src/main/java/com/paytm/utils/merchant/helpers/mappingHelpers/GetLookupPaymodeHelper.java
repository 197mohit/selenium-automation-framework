package com.paytm.utils.merchant.helpers.mappingHelpers;

import com.paytm.framework.Assertion.ValidateSoftly;
import com.paytm.utils.merchant.api.MappingService.GetLookupPaymode;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author ankuragarwal
 * Date: 11/09/18
 */
public class GetLookupPaymodeHelper {

    private Response response;
    private JsonPath jsonPath;
    private ValidateSoftly validateSoftly;

    public GetLookupPaymodeHelper(String paymode) {
        response = GetLookupPaymode.GetLookupPaymode(paymode);
        jsonPath = response.jsonPath();
        validateSoftly = ValidateSoftly.getInstance();
    }

    public GetLookupPaymodeHelper getid() {
        this.jsonPath.getString("id");
        return this;
    }

    public GetLookupPaymodeHelper getName() {
        this.jsonPath.getString("name");
        return this;
    }

    public GetLookupPaymodeHelper validateName(String paymodeName) {
        validateSoftly.validate(this.jsonPath.getString("name"),
                "", "Validated PAYMODE name").isEqualTo(paymodeName);
        return this;
    }

    public GetLookupPaymodeHelper validateID(String id) {
        validateSoftly.validate(this.jsonPath.getString("id"),
                "", "Validated Lookup id").isEqualTo(id);
        return this;
    }

    public GetLookupPaymodeHelper validateCategory(String category) {
        validateSoftly.validate(this.jsonPath.getString("category"),
                "", "Validated Lookup category").isEqualTo(category);
        return this;
    }

    public void assertAll() { this.validateSoftly.assertAll();}

}
