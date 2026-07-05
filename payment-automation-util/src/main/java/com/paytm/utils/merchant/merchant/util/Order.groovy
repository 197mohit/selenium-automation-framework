package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.CString
import com.paytm.framework.conditions.Condition
import com.paytm.utils.merchant.util.PGPUtil
import com.paytm.utils.merchant.util.exception.pgpException.PGPException
import groovy.json.JsonOutput
import io.restassured.builder.RequestSpecBuilder

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class Order {

    String id
    private String mid
    private String mKey
    private Refunds refunds
    private def reqSpec = {
        new RequestSpecBuilder()
                .setBaseUri(PGP_HOST)
                .setBasePath('/merchant-status/getTxnStatus')
                .addQueryParam('JsonData', {
            def jsonData = new TreeMap([MID: mid, ORDERID: id])
            jsonData << [CHECKSUMHASH: PGPUtil.getChecksum(mKey, jsonData)]
            JsonOutput.toJson(jsonData)
        }()).build()
    }

    Order(String mid, String mKey) {
        this(System.currentTimeMillis(), mid, mKey)
    }

    Order(String id, String mid, String mKey) {
        assert id && mid && mKey, 'constructor params are not as expected'
        this.id = id
        this.mid = mid
        this.mKey = mKey
    }

    void init() {}

    Order plus(String theme) {}

    Order add(String theme) { this + theme }

    CString status() {
        String status = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec()).get().path('STATUS')
        if (!status) throw new PGPException("couldn't fetch status for $this")
        new CString(status) {
            @Override
            String toString() {
                "${Order.this} status"
            }
        }
    }

    void minus(Object amt) {
        this.refund(amt)
    }

    void refund(Object amt) {
        def resp = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec()).get().path('')
        new Transaction(mid, mKey, id, resp['TXNID'] as String, resp['TXNAMOUNT'] as String) - amt
    }

    Refunds getRefunds() {
        refunds ?: (refunds = new Refunds(mid, mKey, id))
    }

    Condition isSuccess() {
        this.status().equals('TXN_SUCCESS')
    }

    Condition isFailure() {
        this.status().equals('TXN_FAILURE')
    }

    Condition isPending() {
        this.status().equals('PENDING')
    }

    Transactions getSubTransactions() {
        new Transactions(mid, mKey, id)
    }

    String toString() {
        "order($id)"
    }

}
