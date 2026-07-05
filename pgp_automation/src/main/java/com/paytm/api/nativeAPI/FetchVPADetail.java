package com.paytm.api.nativeAPI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchVPADetails.FetchVPADetailsDTO;
import com.paytm.dto.NativeDTO.fetchVPADetails.response.FetchVPADetailResponse;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;

    /*
    @author Rahul Mendiratta
    */

public class FetchVPADetail extends BaseApi {

    public FetchVPADetail(FetchVPADetailsDTO fetchVPADetailsDTO, String mid, String orderId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_VPA_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(fetchVPADetailsDTO);
    }

    public FetchVPADetail(FetchVPADetailsDTO fetchVPADetailsDTO, InitTxnDTO initTxnDTO) {
        this(fetchVPADetailsDTO, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
    }


    public static FetchVPADetailResponse executeFetchBinDetails(FetchVPADetailsDTO fetchVPADetailsDTO, String mid, String orderId) {
        Response response = new FetchVPADetail(fetchVPADetailsDTO, mid, orderId).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Status Code is: " + statusCode).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        FetchVPADetailResponse fetchVPADetailResponse = null;
        try {
            fetchVPADetailResponse = mapper.readValue(jsonObject.toJSONString(), FetchVPADetailResponse.class);
        } catch (IOException e) {
            Assertions.fail("Change in InitTxnResponse Json", e);
        }
        return fetchVPADetailResponse;
    }


}
