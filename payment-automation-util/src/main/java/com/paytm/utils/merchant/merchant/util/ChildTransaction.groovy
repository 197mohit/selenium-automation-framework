package com.paytm.utils.merchant.merchant.util

import groovy.json.JsonOutput
import groovy.transform.Memoized
import groovy.transform.ToString
import io.restassured.builder.RequestSpecBuilder

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static io.restassured.RestAssured.given

@ToString(includePackage = false, includeNames = true)
class ChildTransaction {

    final int index
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

    ChildTransaction(int index, Merchant m, OrderV2 o) {
        this.index = index
        this.m = m
        this.o = o
    }

    @Memoized
    String getId() {
        given().spec(reqSpec()).get().path("CHILDTXNLIST[$index].TXNID")
    }

    String getStatus() {
        status in ['PENDING', null] ? given().spec(reqSpec()).get().path("CHILDTXNLIST[$index].STATUS").tap {
            status = it
        } : status
    }
}
