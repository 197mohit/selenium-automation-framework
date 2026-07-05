package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import groovy.transform.ToString
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

@ToString(includePackage = false, includeNames = true, excludes = ['transaction'], includeFields = true)
class OrderV2 {

    final String id
    final double amt
    final TransactionV2 transaction
    final RefundsV2 refunds
    private final String txnToken
    final String ssoToken
    final String custId
    private final Merchant m

    OrderV2(String id, double amt, String txnToken, String ssoToken, Merchant m, String custId) {
        this.id = id
        this.amt = amt
        this.txnToken = txnToken
        this.ssoToken = ssoToken
        this.custId = custId
        this.m = m
        this.transaction = new TransactionV2(m, this, txnToken)
        this.refunds = new RefundsV2(m, this)
    }

    OrderV2(double amt) {
        this(amt, null, null)
    }

    OrderV2(double amt, String ssoToken, String custId) {
        this(new Random().nextLong().abs() as String, amt, null, ssoToken, null, custId)
    }

    void payUsingNB() {
        def root = [
                head: [
                        channelId: 'WEB',
                        txnToken : txnToken,
                ],
                body: [
                        mid        : m.id,
                        orderId    : id,
                        paymentMode: 'NET_BANKING',
                        channelCode: 'ICICI',
                        requestType: 'NATIVE',
                ]
        ]
        def res = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/theia/api/v1/processTransaction').queryParams([mid: m.id, orderId: id]).body(root).post().then().statusCode(200).extract().path('')
        def bankFromHtml = given().filters([new RequestLoggingFilter(), new ResponseLoggingFilter()]).contentType(ContentType.URLENC).formParams(res.body.bankForm.redirectForm.content).post(res.body.bankForm.redirectForm.actionUrl as String).then().statusCode(200).extract().asString()
        String instaUrl = bankFromHtml.split("\"")[7]
        String ES = bankFromHtml.split("\"")[29]
        given().filters([new RequestLoggingFilter(), new ResponseLoggingFilter()]).contentType(ContentType.URLENC).baseUri(instaUrl).formParam('ES', ES).post()
    }
}