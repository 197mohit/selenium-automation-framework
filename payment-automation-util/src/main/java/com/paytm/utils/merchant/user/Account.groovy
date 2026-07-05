package com.paytm.utils.merchant.user

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.merchant.util.Merchant
import groovy.transform.MapConstructor
import groovy.transform.ToString
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.parsing.Parser

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given

@ToString(includePackage = false, ignoreNulls = true, excludes = ['token'])
@MapConstructor
class Account {

    String id
    final String no
    final String ifsc
    final String bank
    final String firstName
    final String lastName
    final String vpa

    Account(String no, String ifsc, String bank, String firstName, String lastName) {
        this.no = no
        this.ifsc = ifsc
        this.bank = bank
        this.firstName = firstName
        this.lastName = lastName
    }

    Account(String no, String ifsc, String bank) {
        this.no = no
        this.ifsc = ifsc
        this.bank = bank
    }

    Account(String vpa) {
        this.vpa = vpa
    }

    String getToken() {
        Merchant m = new Merchant('WXnohK26855499163808', false)
        def root = [
                head: [
                        requestTimestamp: System.currentTimeMillis() as String,
                        version         : 'v1',
                        channelId       : 'WEB',
                        signature       : null,
                ],
                body: [
                        mid          : m.id,
                        requestId    : System.currentTimeMillis() as String,
                        vpa          : vpa,
                        accountNumber: no,
                        ifscCode     : ifsc,
                        bankName     : bank,

                ]
        ]
        root.head.signature = getChecksum(m.key, toJson(root.body))
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).filters([new RequestLoggingFilter(), new ResponseLoggingFilter()]).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/refund/api/v1/account/validate').queryParams([mid: m.id, requestId: root.body.requestId]).body(root).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('body.token')
    }
}
