package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.GList
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.joda.time.DateTime

import static com.paytm.utils.merchant.Constants.ALIPAY
import static io.restassured.RestAssured.given

class Commissions implements GList<Commission> {

    private final Merchant m

    Commissions(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<Commission> iterator() {
        new Iterator<Commission>() {
            List list = {
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(ALIPAY).basePath('alipayplus/boss/contract/items/query.htm').body(
                        [
                                request  : [
                                        head: [
                                                version : '1.1.3.1',
                                                function: 'alipayplus.boss.contract.items.query',
                                                clientId: '2016030715243903536806',
                                                reqMsgId: new Random().nextLong().abs() as String,
                                                reqTime : DateTime.now() as String,
                                        ],
                                        body: [
                                                merchantId: m.alipayId,
                                                pageNum   : '1',
                                                pageSize  : '20',
                                        ],
                                ],
                                signature: 'signature'
                        ]
                ).post().then().defaultParser(Parser.JSON).extract().jsonPath().get("response.body.contractBasics.find { it.productCode == '51051000100000000002' }.contractId").tap {
                    assert it, 'exception occurred while fetching details from alipay'
                }.with {
                    given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(ALIPAY).basePath('alipayplus/boss/contract/details/query.htm').body(
                            [
                                    request  : [
                                            head: [
                                                    version : 'fixed-a',
                                                    function: 'alipayplus.boss.contract.details.query',
                                                    clientId: '2016030715243903536806',
                                                    reqMsgId: new Random().nextLong().abs() as String,
                                                    reqTime : DateTime.now() as String,
                                            ],
                                            body: [contractId: it],
                                    ],
                                    signature: 'signature'
                            ]
                    ).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('response.body.productCondition.feeItems[0].payMethodFeeInfos').tap {
                        assert it, 'exception occurred while fetching details from alipay'
                    }.with {
                        it.collect {
                            String payMode = ['NET_BANKING': 'nb', 'UPI': 'upi', 'CREDIT_CARD': 'cc', 'DEBIT_CARD': 'dc', 'EMI': 'emi', 'PAYTM_DIGITAL_CREDIT': 'pdc', 'EMI_DC': 'emidc', 'MP_COD': 'cod', 'BALANCE' : 'ppi'][it.payMethod]
                            it.feeRanges.collect {
                                [feeRate: it.feeRate, fixedFee: it.fixedFeeAmount.value, maxFee: it.maxFeeAmount.value, minAmt: it.lowerFeeValue.value, maxAmt: it.upperFeeValue.value, payMode: payMode]
                            }
                        }.flatten().collect {
                            new Commission(it.feeRate as double, (it.fixedFee as double) / 100, (it.maxFee as double) / 100, (it.minAmt as double) / 100, ((it.maxAmt as double) / 100).with {
                                it < 0 ? Double.MAX_VALUE : it
                            }, it.payMode)
                        }
                    }
                }
            }()
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Commission next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Commission> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
