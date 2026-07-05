package com.paytm.api.sts;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FundTransfer extends BaseApi {

    String orderId;

    private String request = "{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"version\": \"1.0.0\",\n" +
            "            \"reqTime\": \"2021-05-10T11:37:51\",\n" +
            "            \"reqMsgId\": \"1aa40865-32cc-4d53-aa97-502403c004b1\",\n" +
            "            \"signature\": \"\",\n" +
            "            \"sourceId\": \"settlement\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"exchangeAmount\": \"5000500\",\n" +
            "            \"payoutId\": \"{requestAndPayoutId}\",\n" +
            "            \"requestId\": \"{requestAndPayoutId}\",\n" +
            "            \"aliPayId\": \"\",\n" +
            "            \"paytmId\": \"{MID}\",\n" +
            "            \"transferType\": \"m2b\",\n" +
            "            \"extendInfo\": \"{\\\"key\\\":\\\"value\\\"}\",\n" +
            "            \"bbId\": \"\",\n" +
            "            \"merchantName\": \"dummymerchantName\",\n" +
            "            \"billDateTime\": \"9872985729\",\n" +
            "            \"settleBillId\": \"875982758\",\n" +
            "            \"isManualM2B\": true,\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"settleStrategy\":\"ONLINE_SETTLEMENT\",\n" +
            "            \"merchantSolutionType\":\"ONLINE\" \n" +
            "        }\n" +
            "    }\n" +
            "}";

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
    public FundTransfer(){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.STS_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.STS_FUND_TRANSFER);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public FundTransfer buildRequest(String mid, String amount, Boolean manualM2B) {
        setOrderId(CommonHelpers.generateOrderId());
        setContext("request.body.paytmId", mid);
        setContext("request.body.exchangeAmount", amount);
        setContext("request.body.isManualM2B", manualM2B);
        setContext("request.body.payoutId", getOrderId());
        setContext("request.body.requestId", getOrderId());
        return this;
    }
}
