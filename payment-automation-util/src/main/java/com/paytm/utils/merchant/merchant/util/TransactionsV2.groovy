package com.paytm.utils.merchant.merchant.util

import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import io.restassured.RestAssured

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class TransactionsV2 implements GList<TransactionV2> {

    private final Merchant m
    private final OrderV2 o

    TransactionsV2(Merchant m, OrderV2 o) {
        this.m = m
        this.o = o
    }

    @Override
    Iterator<TransactionV2> iterator() {
        return new Iterator<TransactionV2>() {
            private List<TransactionV2> list = {
                def jsonData = new TreeMap([MID: m.id, ORDERID: o.id])
                jsonData << [CHECKSUMHASH: PGPUtil.getChecksum(m.key, jsonData)]
                given().baseUri(PGP_HOST).basePath('/merchant-status/getTxnStatus').queryParam('JsonData', JsonOutput.toJson(jsonData)).get().path('CHILDTXNLIST').with {
                    it != null ? it.indexed().collect { new ChildTransaction(it.key, m, o) } : []
                }
            }()
            private int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            TransactionV2 next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends TransactionV2> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
