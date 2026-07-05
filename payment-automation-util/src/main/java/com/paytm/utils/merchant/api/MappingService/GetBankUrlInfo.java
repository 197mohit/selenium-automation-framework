package com.paytm.utils.merchant.api.MappingService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.mappingService.response.BankUrlInfoDTO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * Created by ankuragarwal on 27/9/18
 */
public class GetBankUrlInfo extends BaseApi {

    private String getBankUrlInfoUrl = Constants.MappingService.GET_BANK_URL_INFO;

    private GetBankUrlInfo(String bankId, String payMethodId, String channelId) {

        getBankUrlInfoUrl = getBankUrlInfoUrl.replace("{bankId}", bankId)
                .replace("{payMethodId}", payMethodId)
                .replace("{channelId}", channelId);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getBankUrlInfoUrl);
    }

    public static BankUrlInfoDTO executeBankUrlInfo (String bankId, String payMethodId, String channelId) {
        Response response = new GetBankUrlInfo(bankId, payMethodId, channelId).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Status Code is: "+statusCode).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        BankUrlInfoDTO bankUrlInfoDTO = null;
        try {
            bankUrlInfoDTO = mapper.readValue(jsonObject.toJSONString(), BankUrlInfoDTO.class);
        } catch (IOException e) {
            Assertions.fail("Change in bankUrlInfo DTO", e);
        }
        return bankUrlInfoDTO;
    }

}
