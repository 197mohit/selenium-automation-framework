package com.paytm.api.nativeAPI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

public class FetchPaymentOption extends BaseApi {

    public FetchPaymentOption(String mid, String orderId, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        this(mid,fetchPaymentOptionsDTO);
       getRequestSpecBuilder().addQueryParam("orderId", orderId);
    }

    public FetchPaymentOption(String mid, String orderId, String deviceType,FetchPaymentOptionsDTO fetchPaymentOptionsDTO,String appVersion)
    {
        this(mid,fetchPaymentOptionsDTO);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        if(deviceType.equalsIgnoreCase("ios"))
        {
            getRequestSpecBuilder().addQueryParam("skip_prevalidation=1&signature=2WR/wvD eX9TMJPDcTuPRxHW Vk=&locTime=1695734802.0&language=en-IN&source=paytm-app&payment_info_version=2&locale=en-IN&show_bank_offers=1&deviceName=iPhone XR (iOS 16.6)&version="+appVersion+"&long=79.78570719970486&deviceIdentifier=Apple-iPhone-2DC5760B-3313-4720-A9B8-E70AA8C103CF&osVersion=16.6&add_money_fee_applied=1&native_withdraw=1&client=iosapp&promoV2=1&payment_info=1&networkType=2G&deviceManufacturer=Apple&lat=18.855941772460938&timestamp=1695734804");
        }
        else if(deviceType.equalsIgnoreCase("android"))
        {
            getRequestSpecBuilder().addQueryParam("deviceIdentifier=OnePlus-EB2101-8d6c2345-d82a-4857-b055-693130615cb4&deviceManufacturer=OnePlus&deviceName=EB2101&client=androidapp&version="+appVersion+"&playStore=true&lat=28.7012426&long=77.4323668&language=en&networkType=WIFI&osVersion=12");
        }
        else
        {
            new FetchPaymentOption(mid,orderId,fetchPaymentOptionsDTO);
        }
    }

    public FetchPaymentOption(String mid, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        if(fetchPaymentOptionsDTO.getHead().getTokenType() == "GUEST") {
            getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.GUEST_FETCH_PAYMENT_OPTION_V1);
        }
        else {
            getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTION_V1);
        }
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(fetchPaymentOptionsDTO);
    }

    public static FetchPaymentOptResponseDTO executeFetchPaymtOption(String mid, String orderId,
                                                                     FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        Response response =  new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Status Code is: "+statusCode).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = null;
        try {
            fetchPaymentOptResponseDTO = mapper.readValue(jsonObject.toJSONString(), FetchPaymentOptResponseDTO.class);
        } catch (IOException e) {
            Assertions.fail("Change in FetchPaymentOption DTO", e);
        }
        return fetchPaymentOptResponseDTO;
    }
    public FetchPaymentOption(String mid,String orderId,String Client,String UserAgent, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        this(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().addQueryParam("client",Client);
        getRequestSpecBuilder().addHeader("user-agent",UserAgent);

    }
    public FetchPaymentOption(String mid,String orderId,String UserAgent, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        this(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().addHeader("user-agent",UserAgent);
    }
}

