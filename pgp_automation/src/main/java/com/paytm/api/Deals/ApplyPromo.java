package com.paytm.api.Deals;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.luaj.vm2.ast.Str;

import java.util.HashMap;
import java.util.Map;

public class ApplyPromo extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"requestId\": \"Native660f6daf8814e6\",\n" +
            "        \"requestTimestamp\": \"1673792191475\",\n" +
            "        \"token\": \"{{sso}}\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"version\": \"1.0\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "      \"custId\": \"1000885542\",\n" +
            "      \"mid\": \"{{mid}}\",\n" +
            "      \"paymentOptions\": [\n" +
            "        {\n" +
            "          \"bankCode\": \"ICICI\",\n" +
            "          \"cardNo\": \"4375512441465005\",\n" +
            "          \"payMethod\": \"CREDIT_CARD\",\n" +
            "          \"transactionAmount\": \"190.0\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"promoContext\": {\n" +
            "        \"cart\": \"{\\\"items\\\":{\\\"1349568965421\\\":{\\\"price\\\":190,\\\"product\\\":{\\\"id\\\":1234580979,\\\"merchant_id\\\":63795704,\\\"brand_id\\\":571366,\\\"vertical_id\\\":1218,\\\"category_ids\\\":[166091]}}}}\",\n" +
            "        \"affordabilityInfo\": \"{\\\"paymentFlow\\\":\\\"DEAL_FLOW\\\"}\"\n" +
            "      },\n" +
            "      \"totalTransactionAmount\": \"190.00\"\n" +
            "    }\n" +
            "}";

    String requestWithPromoCode="{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"requestId\": \"Native660f6daf8814e69\",\n" +
            "        \"requestTimestamp\": \"1673792191475\",\n" +
            "        \"token\": \"{{sso}}\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"version\": \"1.0\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "      \"custId\": \"1001840919\",\n" +
            "      \"mid\": \"{{mid}}\",\n" +
            "      \"promocode\":\"MLPOFFER\",\n" +
            "        \"paymentOptions\": [\n" +
            "        {\n" +
            "            \"payMethod\": \"BALANCE\",\n" +
            "            \"transactionAmount\": \"40.00\"\n" +
            "        }\n" +
            "        ],\n" +
            " \n" +
            "      \"promoContext\": {\n" +
            "        \"cart\": \"{\\\"items\\\":{\\\"1349568965421\\\":{\\\"price\\\":40,\\\"product\\\":{\\\"id\\\":1234580979,\\\"merchant_id\\\":63795704,\\\"brand_id\\\":571366,\\\"vertical_id\\\":1218,\\\"category_ids\\\":[166091]}}}}\",\n" +
            "        \"affordabilityInfo\": \"{\\\"paymentFlow\\\":\\\"DEAL_FLOW\\\"}\"\n" +
            "\n" +
            "      },\n" +
            "      \"totalTransactionAmount\": \"40.00\"\n" +
            "    }\n" +
            "}";

    public String getCart() {
        return cart;
    }

    public void setCart(String cart) {
        this.cart = cart;
    }

    String cart = "{\"items\":{\"1349568965421\":{\"price\":{amount},\"product\":{\"id\":1234580979,\"merchant_id\":63795704,\"brand_id\":571366,\"vertical_id\":1218,\"category_ids\":[166091]}}}}";

    public String getRequest() {
        return request;
    }
    public String getRequestWithPromoCode(){return requestWithPromoCode;}

    public void setRequest(String request) {
        this.request = request;
    }
    public void setRequestWithPromoCode(String requestWithPromoCode){this.requestWithPromoCode=requestWithPromoCode;}
    public ApplyPromo() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.ITEM_APPLY_PROMO_V2);
    }

    public ApplyPromo buildWithParameters(String mid, String token,String amount) {

        PaymentDTO paymentDTO = new PaymentDTO();
        setContext("head.token",token);
        setContext("body.mid",mid);
        setContext("body.totalTransactionAmount",amount);
        setContext("body.paymentOptions[0].transactionAmount",amount);
        cart = cart.replace("{amount}",amount);
        setContext("body.promoContext.cart",getCart());
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(getRequest());
        return this;
    }
    public ApplyPromo buildWithParameters(String mid,String custID, String token,String amount) {

        PaymentDTO paymentDTO = new PaymentDTO();
        setContext("head.token",token);
        setContext("body.mid",mid);
        setContext("body.totalTransactionAmount",amount);
        setContext("body.custId",custID);
        setContext("body.paymentOptions[0].transactionAmount",amount);
        cart = cart.replace("{amount}",amount);
        setContext("body.promoContext.cart",getCart());
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(getRequest());
        return this;
    }

    public ApplyPromo buildWithParameters(String mid,String custID, String token,String amount,String promoCode) {

        PaymentDTO paymentDTO = new PaymentDTO();
        setContext("head.token",token);
        setContext("body.mid",mid);
        setContext("body.totalTransactionAmount",amount);
        setContext("body.custId",custID);
        setContext("body.paymentOptions[0].transactionAmount",amount);
        setContext("body.promocode",promoCode);
        cart = cart.replace("{amount}",amount);
        setContext("body.promoContext.cart",getCart());
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(getRequestWithPromoCode());
        return this;
    }
}
