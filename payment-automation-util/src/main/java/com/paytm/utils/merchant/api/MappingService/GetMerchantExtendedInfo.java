package com.paytm.utils.merchant.api.MappingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * @author ankuragarwal
 * Date: 10/10/18
 */
public class GetMerchantExtendedInfo extends BaseApi {

    String mappingExtendedInfoUrl = Constants.MappingService.MERCHANT_EXTENDED_INFO;

    private GetMerchantExtendedInfo(String mid) {
        mappingExtendedInfoUrl = mappingExtendedInfoUrl.replace("{mid}", mid);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(mappingExtendedInfoUrl);
    }

    public static MerchExtendedInfo executeMercExtendedInfo(String mid) {
        GetMerchantExtendedInfo getMerchantExtendedInfo = new GetMerchantExtendedInfo(mid);
        Response response = getMerchantExtendedInfo.execute();
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        MerchExtendedInfo merchExtendedInfo = null;
        try {
            merchExtendedInfo = mapper.readValue(jsonObject.toJSONString(), MerchExtendedInfo.class);
        } catch (IOException e) {
            Assertions.fail("Change in MerchExtendedInfo DTO", e);
        }
        return merchExtendedInfo;
    }

}
