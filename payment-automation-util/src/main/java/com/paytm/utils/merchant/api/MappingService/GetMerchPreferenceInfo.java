package com.paytm.utils.merchant.api.MappingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.MerchantPrefInfo;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

public class GetMerchPreferenceInfo extends BaseApi {

    private String merchPrefInfoUrl = Constants.MappingService.GET_MERCH_PREFERENCE_INFO;

    public GetMerchPreferenceInfo(String mid) {
        merchPrefInfoUrl = merchPrefInfoUrl.replace("{mid}", mid);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(merchPrefInfoUrl);
    }

    public static MerchantPrefInfo executeGetMercPreferenceInfo(String mid) {
        GetMerchPreferenceInfo getMerchPreferenceInfo = new GetMerchPreferenceInfo(mid);
        Response response = getMerchPreferenceInfo.execute();
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        MerchantPrefInfo merchantPrefInfo = null;
        try {
            merchantPrefInfo = mapper.readValue(jsonObject.toJSONString(), MerchantPrefInfo.class);
        } catch (IOException e) {
            Assertions.fail("Change in MerchantPrefInfo DTO", e);
        }
        return merchantPrefInfo;
    }

}
