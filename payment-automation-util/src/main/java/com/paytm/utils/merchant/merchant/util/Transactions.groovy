package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.CInteger
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import io.restassured.builder.RequestSpecBuilder

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class Transactions {

    private String mId
    private String mKey
    private String orderId
    private def reqSpec = {
        new RequestSpecBuilder()
                .setBaseUri(PGP_HOST)
                .setBasePath('/merchant-status/getTxnStatus')
                .addQueryParam('JsonData', {
            def jsonData = new TreeMap([MID: mId, ORDERID: orderId])
            jsonData << [CHECKSUMHASH: PGPUtil.getChecksum(mKey, jsonData)]
            JsonOutput.toJson(jsonData)
        }()).build()
    }

    Transactions(String mId, String mKey, String orderId) {
        assert mId && mKey && orderId, 'constructor params are not as expected'
        this.mId = mId
        this.mKey = mKey
        this.orderId = orderId
    }

    Transaction getAt(int index) {
        def respBody = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec()).get().path('')
        List txns = respBody['CHILDTXNLIST']
        if (!respBody['TXNID']) throw new IndexOutOfBoundsException("no txns available for order($orderId)")
        else if (!txns && index > 0) throw new IndexOutOfBoundsException()
        else if (!txns) new Transaction(mId, mKey, orderId, respBody['TXNID'], respBody['TXNAMOUNT'])
        else if (!txns[index]) throw new IndexOutOfBoundsException()
        else new Transaction(mId, mKey, orderId, txns[index]['TXNID'], txns[index]['TXNAMOUNT'])
    }

    Transaction get(int index) {
        this[index]
    }

    CInteger size() {
        def respBody = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec()).get().path('')
        def size = {
            if (!respBody.CHILDTXNLIST && !respBody.TXNID) return 0
            else if (!respBody.CHILDTXNLIST) return 1
            else if (respBody.CHILDTXNLIST) return respBody.CHILDTXNLIST.size()
        }()
        new CInteger(size) {
            @Override
            String toString() {
                "(no. of transactions)"
            }
        }

    }
}
