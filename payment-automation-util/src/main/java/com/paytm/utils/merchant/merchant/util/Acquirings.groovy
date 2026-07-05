package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import io.restassured.parsing.Parser
import org.joda.time.DateTime

import static com.paytm.utils.merchant.Constants.ALIPAY
import static io.restassured.RestAssured.given

class Acquirings implements GList<Acquiring> {

    private final map = ['nb': 'NET_BANKING', 'upi': 'UPI', 'cc': 'CREDIT_CARD', 'dc': 'DEBIT_CARD', 'emi': 'EMI', 'pdc': 'PAYTM_DIGITAL_CREDIT', 'emidc': 'EMI_DC', 'cod': 'MP_COD', 'bank-transfer': 'BANK_TRANSFER']
    private final defaultGateways = ['nb': 'icici', 'upi': 'icici', 'cc': 'hdfc', 'dc': 'hdfc', 'emi': 'hdfc', 'pdc': 'paytmcc']
    private final gateways = ['hdfc', 'icici', 'icie', 'icio', 'icicidirect', 'codmock', 'paytmcc', 'axis', 'ppbl', 'amex', 'bajajfn', 'iciciidebit', 'icicipay', 'sbifss', 'bobfss', 'hdfo', 'hddo', 'citidirect', 'hdfs']
    private final Merchant m
    private List<Acquiring> acquirings

    Acquirings(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<Acquiring> iterator() {
        return new Iterator<Acquiring>() {
            List<Acquiring> list = acquirings ?: (acquirings = {
                Map body = [
                        request  : [
                                head: [
                                        version     : '1.2',
                                        function    : 'alipayplus.merchant.config.queryAcquirings',
                                        reqTime     : DateTime.now() as String,
                                        clientId    : '2016030715243903536806',
                                        clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                        reqMsgId    : System.currentTimeMillis() as String,
                                ],
                                body: [
                                        merchantId: m.alipayId,
                                ]
                        ],
                        signature: 'signature'
                ]
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(ALIPAY).basePath('/alipayplus/merchant/config/queryAcquirings.htm').body(body).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('response.body.acquiringConfigInfos').collect {
                    new Acquiring(it.recordId, map.collectEntries {
                        [(it.value): it.key]
                    }[it.payMethod], it.serviceInstId.toLowerCase(), ['true': true, 'false': false][it.enableStatus])
                }
            }())
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Acquiring next() {
                list[index++]
            }
        }

    }

    @Override
    boolean addAll(Collection<? extends Acquiring> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        acquirings = null
        c.every {
            assert it.payMode.toLowerCase() in map.keySet()
            assert it.bank.toLowerCase() in gateways
            Map body = [
                    request  : [
                            head: [
                                    version     : '1.2',
                                    function    : 'alipayplus.merchant.config.addAcquiring',
                                    reqTime     : DateTime.now() as String,
                                    clientId    : '2016030715243903536806',
                                    clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                    reqMsgId    : System.currentTimeMillis() as String,
                            ],
                            body: [
                                    merchantId   : m.alipayId,
                                    mcc          : 'Retail',
                                    payMethod    : map[it.payMode.toLowerCase()].toUpperCase(),
                                    serviceInstId: it.bank.toUpperCase(),
                                    enableStatus : true,
                            ]
                    ],
                    signature: 'signature'
            ]
            given().baseUri(ALIPAY).basePath('/alipayplus/merchant/config/addAcquiring.htm').body(body).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('response.body.resultInfo.resultCode') in ['SUCCESS', 'ACQUIRING_CONFIG_EXIST']
        }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        acquirings = null
        c.findAll { this.contains(it) }.collect { acqr -> this.find { it == acqr } }.every {
            assert it.payMode.toLowerCase() in map.keySet()
            assert it.bank.toLowerCase() in gateways
            Map body = [
                    request  : [
                            head: [
                                    version     : '1.2',
                                    function    : 'alipayplus.merchant.config.deleteAcquiring',
                                    reqTime     : DateTime.now() as String,
                                    clientId    : '2016030715243903536806',
                                    clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                    reqMsgId    : System.currentTimeMillis() as String,
                            ],
                            body: [
                                    recordId  : it.id,
                                    merchantId: m.alipayId,
                            ]
                    ],
                    signature: 'signature'
            ]
            given().baseUri(ALIPAY).basePath('/alipayplus/merchant/config/deleteAcquiring.htm').body(body).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('response.body.resultInfo.resultStatus') == 'S'
        }
    }
}
