package com.paytm.utils.merchant.api.MappingService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.mappingService.response.EntityOfferDetailsDTO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * Created by ankuragarwal on 13/9/18
 */
public class GetEntityOfferDetails extends BaseApi {

    private String getEntityOfferDetails_URL = Constants.MappingService.GET_ENTITY_OFFER_DETAIL;

    private GetEntityOfferDetails(String mid, String channel, String websiteName) {
        getEntityOfferDetails_URL = getEntityOfferDetails_URL.replace("{mid}", mid)
                .replace("{channel}", channel)
                .replace("{websiteName}", websiteName);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getEntityOfferDetails_URL);
    }

    public static EntityOfferDetailsDTO executeGetEntityOffireDetail(String mid, String channel, String websiteName) {
        Response response = new GetEntityOfferDetails(mid, channel,websiteName).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Status Code is: "+statusCode).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        EntityOfferDetailsDTO entityOfferDetailsDTO = null;
        try {
            entityOfferDetailsDTO = mapper.readValue(jsonObject.toJSONString(), EntityOfferDetailsDTO.class);
        } catch (IOException e) {
            Assertions.fail("Change in EntityOfferDetailsDTO DTO", e);
        }
        return entityOfferDetailsDTO;
    }

}
