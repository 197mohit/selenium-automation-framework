package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.merchant.util.names.BusinessName
import com.paytm.utils.merchant.merchant.util.names.DisplayName
import com.paytm.utils.merchant.merchant.util.names.MerchantName
import io.restassured.RestAssured

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class MerchantLimits implements GList<MerchantLimit> {

    private final Merchant m

    MerchantLimits(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<MerchantLimit> iterator() {
        def map = [NET_BANKING: 'nb', UPI: 'upi', 'UPI_PUSH': 'upip', 'UPI_PUSH_EXPRESS': 'upipe', CREDIT_CARD: 'cc', DEBIT_CARD: 'dc', EMI: 'emi', PAYTM_DIGITAL_CREDIT: 'pdc', EMI_DC: 'emidc', COD: 'cod', MP_COD: 'cod', BALANCE: 'ppi', PPBL: 'ppbl', 'ADVANCE_DEPOSIT_ACCOUNT': 'ada', 'GIFT_VOUCHER': 'gv']
        List<MerchantLimit> limits = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/get/limit/merchantType/PPI_LIMIT_${m.limit}").get().path('merchantLimits').collect {
            new MerchantLimit(map[it.payMode as String], it.limit as Double)
        }
        (map.values() - limits*.payMode).each { limits.add(new MerchantLimit(it, -1)) }
        limits.iterator()
    }

    @Override
    boolean addAll(Collection<? extends MerchantLimit> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}