package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.util.PGPUtil
import groovy.transform.ToString
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

@ToString(includePackage = false, includeNames = true)
class RefundV2 {

    final String id
    final double amt
    final String status
    private final Merchant m

    RefundV2(String id, double amt, String status, Merchant m) {
        this.id = id
        this.amt = amt
        this.status = status
        this.m = m
    }

    RefundV2(double amt) {
        this.amt = amt
    }

    RefundV2(String id) {
        this.id = id
    }

    String getStatus() {
        def root = new TreeMap([MID: m.id, REFID: id])
        root.CHECKSUMHASH = PGPUtil.generateChecksumRefund(m.key, root)
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/refund/HANDLER_INTERNAL/getRefundStatus').body(root).post().then().statusCode(200).extract().path('REFUND_LIST').find {
            it['REFID'] == id
        }['STATUS'] as String
    }
}