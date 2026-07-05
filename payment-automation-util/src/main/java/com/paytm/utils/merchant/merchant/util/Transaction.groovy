package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.util.PGPUtil
import com.paytm.utils.merchant.util.exception.pgpException.PGPException
import lombok.experimental.PackagePrivate

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

@PackagePrivate
class Transaction {
    String mid
    String mKey
    String orderId
    String id
    String amt

    Transaction(String mid, String mKey, String orderId, String id, String amt) {
        assert mid && mKey && orderId && id && amt, 'constructor params are not as expected'
        this.mid = mid
        this.mKey = mKey
        this.orderId = orderId
        this.id = id
        this.amt = amt
    }

    void minus(Object amt) {
        this.refund(amt)
    }

    void refund(Object amt) {
        assert amt && (amt instanceof String || amt instanceof Double || amt instanceof Integer), 'method params are not as expected'
        def body = new TreeMap([MID: mid, ORDERID: orderId, REFID: System.currentTimeMillis() as String, COMMENTS: 'initiate refund', REFUNDAMOUNT: amt as String, TXNID: id, 'TXNTYPE': 'REFUND'])
        body << [CHECKSUM: PGPUtil.generateChecksumRefund(mKey, body)]
        if (!['601', '10'].contains(given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath('/refund/HANDLER_INTERNAL/REFUND').body(body).post().path('RESPCODE'))) throw new PGPException("unable to refund $this")
    }

    void previous() {
        this.refund()
    }

    void refund() {
        this.refund(amt)
    }

    @Override
    String toString() {
        "transaction($id)"
    }
}
