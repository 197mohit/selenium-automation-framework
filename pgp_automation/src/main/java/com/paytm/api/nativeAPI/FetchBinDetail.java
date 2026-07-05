package com.paytm.api.nativeAPI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.FetchBinDetailResponse;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

import static com.paytm.appconstants.Constants.BillProxy.FETCH_BIN_DETAIL;

/**
 * Created by anjukumari on 15/10/18
 */

public class FetchBinDetail extends BaseApi{

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"1544614590000\",\n" +
            "        \"requestId\": \"test123\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"{tokenValue}\"\n" +
            "    },\n" +
            "    \"body\": {BODY}\n" +
            "}";

    String body = "{\n" +
            "       \"bin\": \"{binNo}\",\n" +
            "       \"mid\": \"{mid}\",\n" +
            "       \"requestType\": \"NATIVE_SUBSCRIPTION\"\n" +
            "}";

    String NativeOTPRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ANDROID\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\",\n" +
            "        \"tokenType\": \"TXN_TOKEN\",\n"+
            "        \"token\": \"{TXN_TOKEN}\"\n"+
            "    },\n" +
            "    \"body\": {\n" +
            "        \"bin\": \"{bin}\",\n" +
            "        \"nativeOTPDetailRequired\": \"{nativeOTPDetailRequired}\",\n" +
            "        \"mid\": \"{mid}\",\n" +
            "        \"OrderId\": \"{OrderId}\",\n" +
            "    }\n" +
            "}";

    public FetchBinDetail(String txnToken , String mid, String OrderId, String bin, String nativeOTPDetailRequired){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(FETCH_BIN_DETAIL);
        getRequestSpecBuilder().addHeader("TXN_TOKEN", txnToken);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId",OrderId);
        NativeOTPRequest = NativeOTPRequest.replace("{bin}",bin).replace("{mid}",mid).replace("{nativeOTPDetailRequired}",nativeOTPDetailRequired).replace("{OrderId}",OrderId).replace("{TXN_TOKEN}",txnToken);
        getRequestSpecBuilder().setBody(getNativeOTPRequest());
    }

    public String getNativeOTPRequest(){
        return NativeOTPRequest;
    }
    public void setNativeOTPRequest(String NativeOTPRequest){
        this.NativeOTPRequest = NativeOTPRequest;
    }

    private static String createChecksum(String merchantKey, String body) {
        String checksum = "";
        body = body.replaceAll("\\s", "");
        try {
            checksum = PGPUtil.getChecksum(merchantKey, body);
            return checksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public FetchBinDetail setRequest(Constants.MerchantType mid, String binNumber){
        body = body.replace("{binNo}", binNumber).replace("{mid}", mid.getId());
        String checksum = createChecksum(mid.getKey(), body);
        request = request.replace("{tokenValue}", checksum).replace("{BODY}", body);
        return this;
    }

    public String getRequest() {
        return request;
    }

    public FetchBinDetail(Constants.MerchantType mid, String OrderId, String binNumber){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid.getId());
        getRequestSpecBuilder().addQueryParam("orderId", OrderId);
        setRequest(mid, binNumber);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public FetchBinDetail(FetchBinDetailsRequest fetchBinDetailsRequest, String mid, String orderId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(fetchBinDetailsRequest);
    }

    public static FetchBinDetailResponse executeFetchBinDetails(FetchBinDetailsRequest fetchBinDetailsRequest, String mid, String orderId) {
        Response response = new FetchBinDetail(fetchBinDetailsRequest, mid, orderId).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Status Code is: "+statusCode).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        FetchBinDetailResponse fetchBinDetailResponse = null;try {
            fetchBinDetailResponse = mapper.readValue(jsonObject.toJSONString(), FetchBinDetailResponse.class);
        } catch (IOException e) {
            Assertions.fail("Change in InitTxnResponse Json", e);
        }
        return fetchBinDetailResponse;
    }

    public FetchBinDetail(FetchBinDetailsRequest fetchBinDetailsRequest, String mid, Boolean isGuest) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        if(isGuest) {
            getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL_GUEST);
        }else{
            getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL);
        }
        if (mid != null) {
            getRequestSpecBuilder().addQueryParam("mid", mid);
        }
        getRequestSpecBuilder().setBody(fetchBinDetailsRequest);
    }

    public FetchBinDetail(String mid, FetchBinDetailsRequest fetchBinDetailsRequest, String referenceId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("referenceId", referenceId);
        this.setContext("body.referenceId", referenceId);
        getRequestSpecBuilder().setBody(fetchBinDetailsRequest);
    }

}