package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.util.PGPUtil
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class RefundsV2 implements GList<RefundV2> {

    private final Merchant m
    private final OrderV2 o
    private final List<String> refIds
    private final RefundsV2 self = this

    RefundsV2(Merchant m, OrderV2 o) {
        this.m = m
        this.o = o
        this.refIds = []
    }


    @Override
    Iterator<RefundV2> iterator() {
        new Iterator<RefundV2>() {
            private List<RefundV2> list = {
                def root = new TreeMap([MID: m.id, REFID: refIds.last()])
                root.CHECKSUMHASH = PGPUtil.generateChecksumRefund(m.key, root)
                given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                        .filters([new RequestLoggingFilter(), new ResponseLoggingFilter()]).config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/refund/HANDLER_INTERNAL/getRefundStatus').body(root).post().then().statusCode(200).extract().path('REFUND_LIST').collect {
                    new RefundV2(it['REFID'], it['TOTALREFUNDAMT'] as double, it['STATUS'], m)
                }
            }()
            private int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            RefundV2 next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends RefundV2> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        return c.every {
            if (it.id) return this.refIds.add(it.id)
            String refId = System.currentTimeMillis() as String
            refIds.add(refId)
            def body = new TreeMap([MID: m.id, ORDERID: o.id, REFID: refId, COMMENTS: 'initiate refund', REFUNDAMOUNT: it.amt as String, TXNID: o.txnId, 'TXNTYPE': 'REFUND'])
            body << [CHECKSUM: PGPUtil.generateChecksumRefund(m.key, body)]
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .filters([new RequestLoggingFilter(), new ResponseLoggingFilter()]).baseUri(PGP_HOST).basePath('/refund/HANDLER_INTERNAL/REFUND').body(body).post().path('RESPCODE') in ['601', '10']
        }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
