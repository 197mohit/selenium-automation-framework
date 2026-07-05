package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.CString
import com.paytm.utils.merchant.util.PGPUtil
import com.paytm.utils.merchant.util.exception.pgpException.PGPException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class Refund {

    private final String mId
    private final String mKey
    private final String orderId
    private final String refId

    Refund(String mId, String mKey, String orderId, String refId) {
        this.mId = mId
        this.mKey = mKey
        this.orderId = orderId
        this.refId = refId
    }

    CString getStatus() {
        def rootJson = """
{
   "head":{
      "signature":""
   },
   "body":{
      "mid": "$mId",
      "orderId": "$orderId",
      "refId": "$refId"
   }
}
"""
        def root = new JsonSlurper().parseText(rootJson)
        root.head.signature = PGPUtil.getChecksum(mKey, JsonOutput.toJson(root.body))
        def status = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath('/refund/api/v1/refundStatus').body(root).post().path('body.resultInfo.resultStatus')
        if (!status) throw new PGPException("unable to fetch refund status of $this")
        new CString(status) {
            @Override
            String toString() {
                "${Refund.this} status"
            }
        }
    }


    @Override
    String toString() {
        "refund($refId)"
    }
}
