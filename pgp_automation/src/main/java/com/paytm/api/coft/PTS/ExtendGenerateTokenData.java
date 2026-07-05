package com.paytm.api.coft.PTS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 06/01/22 05:02 PM
 * @Version 1.0
 */
public class ExtendGenerateTokenData extends BaseApi {

    String request = "{\"head\":{\"version\":\"v1\",\"requestTimestamp\":\"1544614590000\",\"requestId\":\"merchre000001\",\"signature\":\"d7186f022c5a44cc8915bf74e4e8c9f61534487392716\",\"signatureType\":\"CHECKSUM\",\"clientId\":\"PAYTM_THEIA\"},\"body\":{\"paymentType\":\"ECOM\",\"tokenIndexNumber\":\"618ce84aaf898b5b8a99b295\",\"userInfo\":{\"custId\":\"cus001\",\"userId\":\"\",\"firstName\":\"\",\"lastName\":\"\",\"mobileNumber\":\"\"}}}";

    public ExtendGenerateTokenData(String mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.EXTEND_GENERATE_TOKEN_DATA);
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("requestId", UUID.randomUUID().toString());
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public String getRequest() {
        return request;
    }

    public ExtendGenerateTokenData buildRequest(String paymentType, String tokenIndexNumber, String custId, String userId) {
        setContext("body.paymentType", paymentType);
        setContext("body.tokenIndexNumber", tokenIndexNumber);
        setContext("body.userInfo.custId", custId);
        setContext("body.userInfo.userId", userId);
        return this;
    }

    public ExtendGenerateTokenData generateChecksum(String key) {
        Gson gson = new Gson();
        String requestBody = gson.toJson(getRequestBody());
        JsonObject tokenizeCardRequest = gson.fromJson(requestBody, JsonObject.class);
        String bodyForChecksum = tokenizeCardRequest.get("body").toString();
        String checksum = PGPUtil.getChecksum(key, bodyForChecksum);
        setContext("head.signature", checksum);
        setContext("head.signatureType", "CHECKSUM");
        return this;
    }
}
