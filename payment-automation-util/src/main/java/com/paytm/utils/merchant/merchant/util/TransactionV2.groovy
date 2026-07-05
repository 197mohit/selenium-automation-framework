package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import groovy.json.JsonOutput
import groovy.transform.Memoized
import groovy.transform.ToString
import io.restassured.builder.RequestSpecBuilder

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum

import static io.restassured.RestAssured.given

@ToString(includePackage = false, includeNames = true)
class TransactionV2 {

    final String id
    final String token
    final TransactionsV2 children
    private final Merchant m
    private final OrderV2 o
    private def reqSpec = {
        new RequestSpecBuilder()
                .setBaseUri(PGP_HOST)
                .setBasePath('/merchant-status/getTxnStatus')
                .addQueryParam('JsonData', {
            def jsonData = new TreeMap([MID: m.id, ORDERID: o.id])
            jsonData << [CHECKSUMHASH: getChecksum(m.key, jsonData)]
            JsonOutput.toJson(jsonData)
        }()).build()
    }
    private String status

    TransactionV2(String id, Merchant m, OrderV2 o, String token) {
        this.id = id
        this.m = m
        this.o = o
        this.token = token
        this.children = new TransactionsV2(m, o)
    }

    TransactionV2(Merchant m, OrderV2 o, String token) {
        this('', m, o, token)
    }

    @Memoized
    String getId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec()).get().path('TXNID')
    }

    String getStatus() {
        status in ['PENDING', null] ? given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec()).get().path('STATUS').tap { status = it } : status
    }
}
