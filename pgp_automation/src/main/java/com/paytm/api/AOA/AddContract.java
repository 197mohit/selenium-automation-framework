package com.paytm.api.AOA;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class AddContract extends BaseApi {
    String request ="{  \n" +
            "  \"contractBasic\":{  \n" +
            "     \"merchantId\":\"{MID}\",\n" +
            "      \"transId\":\"aggregator11\",\n" +
            "     \"productCode\":\"51051000200000000001\",\n" +
            "     \"signedTime\":\"2020-08-12T18:44:37+05:30\",\n" +
            "     \"effectType\":\"IMMEDIATE\",\n" +
            "     \"effectTime\":\"2020-08-12T18:44:37+05:30\",\n" +
            "     \"expiryType\":\"DUE\",\n" +
            "     \"expiryTime\":\"2022-11-12T18:44:37+05:30\",\n" +
            "     \"memo\":\"AOA acquiring product\",\n" +
            "     \"externalContractId\":\"{{$guid}}\",\n" +
            "     \"createdTime\":\"2020-08-12T18:44:37+05:30\",\n" +
            "     \"modifiedTime\":\"2020-08-12T18:44:37+05:30\"\n" +
            "  },\n" +
            "  \"contractTemplate\":{  \n" +
            "     \"attachmentUrl\":\"\"\n" +
            "  },\n" +
            "  \"productCondition\":{  \n" +
            "     \"currency\":\"INR\",\n" +
            "     \"stagePaymentType\":null,\n" +
            "     \"acquiringMode\":null,\n" +
            "     \"maxAmount\":{  \n" +
            "        \"currency\":\"INR\",\n" +
            "        \"value\":100000\n" +
            "     },\n" +
            "     \"payIntegrationTypes\":[  \n" +
            "        \"API\"\n" +
            "     ],\n" +
            "     \"supportPreCreateOrder\":false,\n" +
            "     \"payMethods\":null,\n" +
            "     \"orderTimeout\":4320,\n" +
            "     \"needLogin\":\"LOGIN_OPTIONAL\",\n" +
            "     \"supportMergeOrder\":false,\n" +
            "     \"timeoutForInactiveOrder\":4320,\n" +
            "     \"supportRefund\":true,\n" +
            "     \"supportMultiRefund\":true,\n" +
            "     \"refundExpiryTime\":\"18\",\n" +
            "     \"refundCurrency\":null,\n" +
            "     \"extendInfo\":\"{\\\"isSupportHybridPayment\\\":\\\"Y\\\"}\"\n" +
            "    \n" +
            "  }\n" +
            "}";
    public AddContract() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.ADD_CONTRACT);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String aoaMid, String mid, String key) {
        this.request = request.replace("{AOAMID}",aoaMid).replace("{MERCHANT}",mid).replace("{KEY}",key);

    }
    public AddContract buildRequest(String mid) {
        setContext("contractBasic.merchantId",mid);
        return this;
    }
}
