package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.GList
import groovy.json.JsonSlurper
import static io.restassured.RestAssured.given

import static com.paytm.utils.merchant.Constants.PGP_HOST

class Contracts implements GList<Contract> {

    private final Merchant m
    private List<Contract> contracts

    Contracts(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<Contract> iterator() {
        return new Iterator<Contract>() {
            List<Contract> list = contracts ?: (contracts = {
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/query/merchant/migration/details/$m.id/true").get().then().extract().jsonPath().get('')
                        .findAll{it.key.toString().startsWith('CONTRACT-DETAIL')}
                        .values().contractBasic.collect {
                    new Contract(it.contractId, it.productCode, it.productName, it.contractStatus)
                }
            }())
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Contract next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Contract> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}