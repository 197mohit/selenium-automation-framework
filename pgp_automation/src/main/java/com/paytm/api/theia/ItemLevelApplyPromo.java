package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
import com.paytm.dto.PromoDTO.ItemLevelPromoDTO.ItemLevelPromoDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Arrays;
import java.util.UUID;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.APPLY_PROMO_V1;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class ItemLevelApplyPromo extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"requestId\": \""+ UUID.randomUUID().toString()+"\",\n" +
            "        \"requestTimeStamp\": \""+Long.toString(System.currentTimeMillis())+"\",\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"version\": \"v1\", \n" +
            "\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"a91c39a4-426f-4ab3-8cf8-cd5ec0b18100\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"AUTOME19638290371021\",\n" +
            "        \"custId\": \"126767691010\", \n" +
            "        \"promocode\": \"discount\",\n" +
            "        \"paymentOptions\": [ \n" +
            "            {\n" +
            "                \"transactionAmount\": \"100.00\", \n" +
            "                \"payMethod\": \"CREDIT_CARD\",\n" +
            "                \"bankCode\": \"HDFC\", \n" +
            "                \"cardNo\": \"4718650100010336\", \n" +
            "                \"savedCardId\": \"\", \n" +
            "                \"vpa\": null \n" +
            "            }\n" +
            "        ],\n" +
            "        \"totalTransactionAmount\": \"100.00\" ,\n" +
            "    \"promoContext\": {\n" +
            "            \"cart\": \"{\\\"items\\\":{\\\"224826563pid\\\":{\\\"price\\\":5000,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"123\\\",\\\"124\\\"]}},\\\"224826564pid\\\":{\\\"price\\\":5000,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"1\\\",\\\"2\\\"]}},\\\"224826566pid\\\":{\\\"price\\\":500,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"3\\\",\\\"4\\\"]}}}}\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public ItemLevelApplyPromo(ItemLevelPromoDTO applyPromoDTO)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.ITEM_APPLY_PROMO_V2);
        getRequestSpecBuilder().addQueryParam("mid", applyPromoDTO.getBody().getMid());
        getRequestSpecBuilder().setBody(applyPromoDTO);
    }

    public final static class ResultInfo {
        public static ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath("body.resultInfo")
                .expectBody("resultStatus", equalTo("S"))
                .expectBody("resultCodeId", nullValue())
                .expectBody("resultCode", equalTo("00000000"))
                .expectBody("resultMsg", equalTo("Success"))
                .build();
        public static ResponseSpecification TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET = new ResponseSpecBuilder()
                .rootPath("body.resultInfo")
                .expectBody("resultStatus", equalTo("F"))
                .expectBody("resultCode", equalTo("9999"))
                .expectBody("resultMsg", equalTo("This transaction cannot be carried out with wallet"))
                .build();
        public static ResponseSpecification PAY_TXN_AMT_ERR = new ResponseSpecBuilder()
                .rootPath("body.resultInfo")
                .expectBody("resultStatus", equalTo("F"))
                .expectBody("resultCode", equalTo("9999"))
                .expectBody("resultMsg", equalTo("pay transaction amount error"))
                .build();
        public static ResponseSpecification PAY_TXN_AMT_CONDITION_BREACH = new ResponseSpecBuilder()
                .rootPath("body.resultInfo")
                .expectBody("resultStatus", equalTo("F"))
                .expectBody("resultCode", equalTo("9999"))
                .expectBody("resultMsg", equalTo("pay transaction amount condition breach"))
                .build();
        public static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath("body.resultInfo")
                .expectBody("resultStatus", equalTo("F"))
                .expectBody("resultCode", equalTo("1001"))
                .expectBody("resultMsg", equalTo("Request parameters are not valid"))
                .build();
    }

}
