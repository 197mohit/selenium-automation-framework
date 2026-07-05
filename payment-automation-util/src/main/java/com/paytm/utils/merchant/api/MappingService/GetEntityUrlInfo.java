package com.paytm.utils.merchant.api.MappingService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.mappingService.response.EntityUrlInfoResponse;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * Created by ankuragarwal on 12/9/18
 */
public class GetEntityUrlInfo extends BaseApi {

    private String getEntiryUrlInfoUrl = Constants.MappingService.GET_ENTITY_URL_INFO;

    private GetEntityUrlInfo(String mid, String requestType, String websiteName) {

        getEntiryUrlInfoUrl = getEntiryUrlInfoUrl.replace("{mid}", mid)
                .replace("{reqType}", requestType)
                .replace("{websiteName}", websiteName);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getEntiryUrlInfoUrl);
    }

    public static EntityUrlInfoResponse executeEntityUrlInfo(String mid, String requestType) {
        GetEntityUrlInfo getEntityUrlInfo = new GetEntityUrlInfo(mid, requestType, "retail");
        Response response = getEntityUrlInfo.execute();
        Assertions.assertThat(response.getStatusCode()).as("Response code mismatch").isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        EntityUrlInfoResponse entityUrlInfoResponse = null;
        try {
            entityUrlInfoResponse = mapper.readValue(jsonObject.toJSONString(), EntityUrlInfoResponse.class);
        } catch (IOException e) {
            Assertions.fail("Change in EntityUrlInfoResponse DTO", e);
        }
        return entityUrlInfoResponse;
    }

}
