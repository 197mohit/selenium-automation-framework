package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
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

public class ApiV1ApplyPromo extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"requestId\": \""+ UUID.randomUUID().toString()+"\",\n" +
            "        \"requestTimestamp\": \""+Long.toString(System.currentTimeMillis())+"\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"\",\n" +
            "        \"promocode\": null,\n" +
            "        \"paymentOptions\": [\n" +
            "            {\n" +
            "                \"payMethod\": \"WALLET\",\n" +
            "                \"transactionAmount\": \"2\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"totalTransactionAmount\": \"2\",\n" +
            "        \"custId\": null\n" +
            "    }\n" +
            "}";

    public ApiV1ApplyPromo(String mid){
        getRequestSpecBuilder()
                .addFilters(Arrays.asList(schemaFilter))
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(APPLY_PROMO_V1)
                .addQueryParam("mid", mid)
                .setBody(request);
        setMethod(MethodType.POST);
        setContext("body.mid", mid);
    }

    public ApiV1ApplyPromo(ApplyPromoDTO applyPromoDTO)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.APPLY_PROMO_V1);
        getRequestSpecBuilder().addQueryParam("mid", applyPromoDTO.getBody().getMid());
        getRequestSpecBuilder().setBody(applyPromoDTO);
    }

    protected Filter schemaFilter = new Filter() {
        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath("json-schemas/apply-promo-schema.json"))
                            .build()
            );
            return ctx.next(requestSpec, responseSpec);
        }
    };

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
