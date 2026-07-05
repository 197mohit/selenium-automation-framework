package com.paytm.api.MappingService;

import java.io.IOException;

import com.paytm.apphelpers.PGPHelpers;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.dto.mappingService.response.EntityBankDetailsResponse;
import com.paytm.framework.api.BaseApi;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author rajeshkumar
 *
 * 
 */
public class GetEntityBankDetails extends BaseApi {

	private String getEntityBankDetailUrl=Constants.MappingService.GET_BANK_DETAILS;
	
    private GetEntityBankDetails(String bankCode) {
    	
    	getEntityBankDetailUrl = getEntityBankDetailUrl.replace("{bankCode}", bankCode);
          setMethod(BaseApi.MethodType.GET);
          RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
          requestSpecBuilder.setContentType(ContentType.JSON);
          requestSpecBuilder.setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
          requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST);
          requestSpecBuilder.setBasePath(getEntityBankDetailUrl);
		
	}
    // GetEntityBankDetails Method ends here 
    
    @SuppressWarnings("unchecked")
	public static EntityBankDetailsResponse executeEntityBankDetails(String bankCode) {
    	GetEntityBankDetails getEntityBankDetails = new GetEntityBankDetails(bankCode);
        Response response = getEntityBankDetails.execute();
        Assertions.assertThat(response.getStatusCode()).as("Response code mismatch").isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        EntityBankDetailsResponse entityBankDetailsResponse = null;
        try {
        	entityBankDetailsResponse = mapper.readValue(jsonObject.toJSONString(), EntityBankDetailsResponse.class);
        } catch (IOException e) {
            Assertions.fail("Change in EntityUrlInfoResponse DTO", e);
        }
        return entityBankDetailsResponse;
    }
	
	
}
