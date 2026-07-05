package com.paytm.utils.merchant.api.MappingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.contractDTO.PayMethodFeeInfos;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * @author ankuragarwal
 * Date: 11/09/18
 */
public class GetContractPaymentInfo extends BaseApi {

    String getContractPaymentInfo = Constants.MappingService.GET_CONTRACT_PAYMENT_INFO;

    private GetContractPaymentInfo(String mid, String contractId) {
        getContractPaymentInfo = getContractPaymentInfo.replace("{mid}", mid);
        getContractPaymentInfo = getContractPaymentInfo.replace("{contractID}", contractId);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getContractPaymentInfo);
    }

    public static PayMethodFeeInfos executeGetContractPayInfo(String mid, String contractID) {
        GetContractPaymentInfo getContractPaymentInfo = new GetContractPaymentInfo(mid, contractID);
        Response response = getContractPaymentInfo.execute();
        Assertions.assertThat(response.getStatusCode()).as("Response code mismatch").isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get("response"));

        ObjectMapper mapper = new ObjectMapper();
        PayMethodFeeInfos payMethodFeeInfos = null;
        try {
            payMethodFeeInfos = mapper.readValue(jsonObject.toJSONString(), PayMethodFeeInfos.class);
        } catch (IOException e) {
            Assertions.fail("Change in PayMethodFeeInfos DTO", e);
        }

        return payMethodFeeInfos;
    }

}
