package com.paytm.api.Deals;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchPaymentOptions extends BaseApi {

    private String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"requestTimestamp\": \"2023-01-12T07:10:29.378Z\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"{{sso}}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{{mid}}\",\n" +
            "        \"goods\": [\n" +
            "            {\n" +
            "                \"merchantGoodsId\": 1234580979\n" +
            "            }\n" +
            "        ],\n" +
            "        \"subwalletAmount\": \"{\\\"FOOD\\\":\\\"0\\\"}\",\n" +
            "        \"disablePaymentMode\": [\n" +
            "            {\n" +
            "                \"mode\": \"BANK_MANDATE\",\n" +
            "                \"channels\": [],\n" +
            "                \"banks\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"mode\": \"PAY_AT_COUNTER\",\n" +
            "                \"channels\": [],\n" +
            "                \"banks\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"mode\": \"COD\",\n" +
            "                \"channels\": [],\n" +
            "                \"banks\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"mode\": \"ESCROW\",\n" +
            "                \"channels\": [],\n" +
            "                \"banks\": []\n" +
            "            }\n" +
            "        ],\n" +
            "        \"cardHashRequired\": \"true\",\n" +
            "        \"applyPaymentOffer\": \"true\",\n" +
            "        \"fetchAllPaymentOffers\": \"true\",\n" +
            "        \"orderAmount\": 12,\n" +
            "        \"txnAmount\": {\n" +
            "            \"value\": 12,\n" +
            "            \"currency\": \"INR\"\n" +
            "        },\n" +
            "        \"applyItemOffers\": {\n" +
            "            \"promoContext\": {\n" +
            "                \"cart\": \"{\\\"items\\\":{\\\"{pid1234580979config{price20\\\":{\\\"price\\\":2000,\\\"product\\\":{\\\"id\\\":1234580979,\\\"merchant_id\\\":63795704,\\\"brand_id\\\":571366,\\\"vertical_id\\\":1218,\\\"category_ids\\\":[166091]}}}}\",\n" +
            "                \"affordabilityInfo\": \"{\\\"paymentFlow\\\":\\\"DEAL_FLOW\\\"}\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public FetchPaymentOptions() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public FetchPaymentOptions buildRequest(String mid, String amount, String sso) {
        setContext("head.token", sso);
        setContext("body.mid", mid);
        setContext("body.orderAmount", amount);
        setContext("body.txnAmount", amount);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        return this;
    }
}
