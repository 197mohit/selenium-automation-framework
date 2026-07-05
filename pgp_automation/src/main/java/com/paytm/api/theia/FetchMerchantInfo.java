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
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_MERCHANT_INFO_V1;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class FetchMerchantInfo extends BaseApi {

    String request = "{\n" +
            " \"head\":{\n" +
            "     \"version\":\"v1\",\n" +
            "     \"requestTimestamp\":\"Time\",\n" +
            "     \"ssoToken\":\"\",\n" +
            "     \"txnToken\":\"\"\n" +
            " },\n" +
            " \"body\":{\n" +
            "    \"mid\":\"\",\n" +
            "    \"orderId\":\"\"\n" +
            "  }\n" +
            "}";

    public FetchMerchantInfo(String mid,String orderId, String txnToken,String sso){
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_MERCHANT_INFO_V1)
                .addQueryParam("mid", mid)
                .addQueryParam("orderId",orderId)
                .setBody(request);
        setMethod(MethodType.POST);
        setContext("body.mid", mid);
        setContext("body.orderId", orderId);
        setContext("head.txnToken", txnToken);
        setContext("head.ssoToken", sso);
    }

}
