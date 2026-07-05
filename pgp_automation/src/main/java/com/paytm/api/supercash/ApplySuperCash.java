package com.paytm.api.supercash;

import com.paytm.LocalConfig;
import com.paytm.framework.api.BaseApi;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.APPLY_SUPERCASH_OFFERS;


public class ApplySuperCash extends BaseApi {
    String request="{\n" +
            "  \"head\": {\n" +
            "    \"token\": \"{Token}\",\n" +
            "    \"tokenType\": \"{Token_type}\",\n" +
            "    \"requestId\": \"{requestId}\",\n" +
            "    \"channelId\": \"WEB\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"userId\": \"{userId}\",\n" +
            "    \"paymodes\": [\n" +
            "      {\n" +
            "        \"paymentMode\": \"{paymode[0]}\",\n" +
            "        \"amount\": 10\n" +
            "      },\n" +
            "      {\n" +
            "        \"paymentMode\": \"{paymode[1]}\",\n" +
            "        \"amount\": 10\n" +
            "      }\n" +
            "    ],\n" +
            "    \"hybrid\": \"false\",\n" +
            "    \"promoContext\": {\n" +
            "      \"cart\": \"{\\\"items\\\":{\\\"224826563pid\\\":{\\\"price\\\":5000,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"123\\\",\\\"124\\\"]}},\\\"224826564pid\\\":{\\\"price\\\":5000,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"1\\\",\\\"2\\\"]}},\\\"224826566pid\\\":{\\\"promocode\\\":\\\"SID_TESTDISCOUNT\\\",\\\"price\\\":500,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"3\\\",\\\"4\\\"]}}}}\"\n" +
            "    },\n" +
            "    \"mid\": \"{MID}\",\n" +
            "    \"source\": \"{source}\",\n" +
            "    \"amount\": \"{Txn_amount}\"\n" +
            "  }\n" +
            "}";

    String requestWithNoPromo="{\n" +
            "  \"head\": {\n" +
            "    \"token\": \"{Token}\",\n" +
            "    \"tokenType\": \"{Token_type}\",\n" +
            "    \"requestId\": \"{requestId}\",\n" +
            "    \"channelId\": \"WEB\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"userId\": \"{userId}\",\n" +
            "    \"paymodes\": [\n" +
            "      {\n" +
            "        \"paymentMode\": \"{paymode[0]}\",\n" +
            "        \"amount\": 10\n" +
            "      },\n" +
            "      {\n" +
            "        \"paymentMode\": \"{paymode[1]}\",\n" +
            "        \"amount\": 10\n" +
            "      }\n" +
            "    ],\n" +
            "    \"hybrid\": \"false\",\n" +
            "    \"mid\": \"{MID}\",\n" +
            "    \"source\": \"{source}\",\n" +
            "    \"amount\": \"{Txn_amount}\"\n" +
            "  }\n" +
            "}";

    public String getRequest() {
        return request;
    }
    public String getRequestWithNoPromo() {
        return requestWithNoPromo;
    }

    public ApplySuperCash setRequest(String mid, String token, String tokenType, String userId, String txnAmount, String source, String []paymode, String requestID){
        request = request.replace("{Token}", token).replace("{Token_type}", tokenType).
                replace("{requestId}", requestID).replace("{userId}", userId).replace("{paymode[0]}", paymode[0])
                .replace("{paymode[1]}", paymode[1])
                .replace("{MID}", mid).replace("{source}", source).replace("{Txn_amount}", txnAmount);

        return this;
    }
    public RequestSpecification reqSpec(String mid, String token, String tokenType, String userId, String txnAmount, String source, String []paymode, String requestID){
        setRequest(mid,token,tokenType,userId,txnAmount,source,paymode,requestID);

        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequest())
                .setBasePath(APPLY_SUPERCASH_OFFERS)
                .build().log().all();

    }

    public ApplySuperCash setRequestWithoutPromo(String mid, String token, String tokenType, String userId, String txnAmount, String source, String []paymode, String requestID){
        requestWithNoPromo = requestWithNoPromo.replace("{Token}", token).replace("{Token_type}", tokenType).
                replace("{requestId}", requestID).replace("{userId}", userId).replace("{paymode[0]}", paymode[0])
                .replace("{paymode[1]}", paymode[1])
                .replace("{MID}", mid).replace("{source}", source).replace("{Txn_amount}", txnAmount);

        return this;
    }

    public RequestSpecification reqSpecWithoutPromo(String mid, String token, String tokenType, String userId, String txnAmount, String source, String []paymode, String requestID){
        setRequestWithoutPromo(mid,token,tokenType,userId,txnAmount,source,paymode,requestID);
//        setContext("body.promoContext.cart","");
        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequestWithNoPromo())
                .setBasePath(APPLY_SUPERCASH_OFFERS)
                .build().log().all();

    }
}
