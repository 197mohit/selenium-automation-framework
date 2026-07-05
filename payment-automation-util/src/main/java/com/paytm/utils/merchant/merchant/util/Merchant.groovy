package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.QRCodes
import com.paytm.utils.merchant.intersections.MerchantUserIntersection
import com.paytm.utils.merchant.merchant.util.alipay.AlipayMerchant
import com.paytm.utils.merchant.merchant.util.names.MerchantNames
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.util.PGPUtil
import com.paytm.utils.merchant.util.exception.pgpException.PGPException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Memoized
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.joda.time.DateTime

import static com.paytm.utils.merchant.Constants.ALIPAY
import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class Merchant implements Comparable<Merchant> {
    private final String id
    private String key
    final Acquirings acquirings = new Acquirings(this)
    final Preferences preferences = new Preferences(this)
    final EMIs emis = new EMIs(this)
    final Contracts contracts = new Contracts(this)
    final OrdersV2 orders = new OrdersV2(this)
    boolean editable
    private int limit
    private final def reqSpec = { vpa, accountNumber, ifscCode, bankName ->
        def rootJson = """
{
  "head": {
    "requestTimestamp": "${System.currentTimeMillis()}",
    "version": "v1",
    "channelId": "WEB",
    "signature": null
  },
  "body": {
    "mid": "$id",
    "requestId": "${System.currentTimeMillis()}",
    "vpa": "$vpa",
    "accountNumber": "$accountNumber",
    "ifscCode": "$ifscCode",
    "bankName": "$bankName",
  }
}
"""
        def root = new JsonSlurper().parseText(rootJson)
        root.head.signature = PGPUtil.getChecksum(this.key, JsonOutput.toJson(root.body))
        new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath('/refund/api/v1/account/validate')
                .addQueryParams([mid: id, requestId: root.body.requestId])
                .setBody(root)
                .build()
    }

    Merchant(String id, String key, boolean editable) {
        this.id = id
        this.key = key
        this.editable = editable
    }

    Merchant(String id, boolean editable) {
        this(id, null, editable)
    }

    Merchant(String id) {
        this(id, false)
    }

    String getId() {
        return id
    }

    String getKey() {
        this.key ?: (this.key = {
            String entityId = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/query/merchant/migration/details/$id/true").get().path('MERCHANT-EXTENDED-INFO.extendedInfo.entityId')
            return given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/admin/app/v1/merchants/$entityId/key").header('x-auth-ump', 'zxcs-9098-kls-qw90-xcd').get().path('result')
        }())
    }

    @Memoized
    String getAlipayId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check alipay id of $this")
        }.path('extendedInfo.alipayMid')
    }
    @Memoized
    String getEntityId() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check alipay id of $this")
        }.path('extendedInfo.entityId')
    }




    @Memoized
    String getTemplateId() {
        def root = [
                'request'  : [
                        'head': [
                                'clientId'    : '2016030715243903536806',
                                'clientSecret': 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                'function'    : 'alipayplus.promotion.template.queryTemplateByMerchantId',
                                'reserve'     : ['source': 'end2end'],
                                'reqTime'     : DateTime.now() as String,
                                'accessToken' : '234567a',
                                'reqMsgId'    : new Random().nextLong().abs() as String,
                                'version'     : 'fixed-a',
                        ],
                        'body': [
                                'promotionTool': 'GIFT_VOUCHER',
                                'merchantId'   : this.alipayId,
                        ],
                ],
                'signature': 'no-signature',
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(ALIPAY).basePath('/alipayplus/promotion/template/queryTemplateByMerchantId.htm').body(root).post()
                .then().defaultParser(Parser.JSON).extract().jsonPath().with {
            it?.response?.body?.templates?.find()?.id
        }
    }

    @Memoized
    int getRetryCount() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check retry count of $this")
        }.path('extendedInfo.numberOfRetry') as int
    }

    @Memoized
    def getExtendedInfo() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to fetch extended info for $this")
        }.jsonPath().get('extendedInfo')
    }

    @Memoized
    List<String> getPayModes() {
        def map = [NET_BANKING: 'nb', UPI: 'upi', 'UPI_PUSH': 'upip', 'UPI_PUSH_EXPRESS': 'upipe', CREDIT_CARD: 'cc', DEBIT_CARD: 'dc', EMI: 'emi', PAYTM_DIGITAL_CREDIT: 'pdc', EMI_DC: 'emidc', COD: 'cod', MP_COD: 'cod', BALANCE: 'ppi', PPBL: 'ppbl', 'ADVANCE_DEPOSIT_ACCOUNT': 'ada', 'GIFT_VOUCHER': 'gv']
        def root = [
                'request'  : [
                        'head': [
                                'version'     : 'fixed-a',
                                'function'    : 'alipayplus.payment.cashier.litepayview.consult',
                                'clientId'    : '2016030715243903536806',
                                'reqMsgId'    : new Random().nextLong().abs() as String,
                                'reqTime'     : DateTime.now() as String,
                                'clientSecret': new Random().nextLong().abs() as String,
                        ],
                        'body': [
                                'productCode'              : '51051000100000000001',
                                'envInfo'                  : ['terminalType': 'WEB'],
                                'merchantId'               : this.alipayId,
                                'payerUserId'              : null,
                                'excludeChannelAccountInfo': true,
                        ],
                ],
                'signature': 'no-signature'
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(ALIPAY).basePath('/alipayplus/payment/cashier/litepayview/consult.htm').body(root).post().then().defaultParser(Parser.JSON).extract()
                .jsonPath().get('').with {
            it.response.body.payMethodViews.findAll {
                !(it.payMethod in ['UPI', 'NET_BANKING'])
            }?.payChannelOptionViews?.payMethod?.collect {
                it.find()
            } + it.response.body.payMethodViews.find { it.payMethod == 'UPI' }?.payChannelOptionViews?.payOption +
                    it.response.body.payMethodViews.find {
                        it.payMethod == 'NET_BANKING'
                    }?.payChannelOptionViews?.find { it.instId != 'PPBL' }?.payMethod +
                    it.response.body.payMethodViews.find {
                        it.payMethod == 'NET_BANKING'
                    }?.payChannelOptionViews?.find { it.instId == 'PPBL' }?.instId
        }?.findAll().collect { map[it as String] ?: it }
    }

    String validate(String accountNumber, String ifscCode, String bankName) {
        String token = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec('', accountNumber, ifscCode, bankName)).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('body.token')
        if (!token) throw new PGPException("$this was unable to validate account")
    }

    String validate(String vpa) {
        String token = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec(vpa, '', '', '')).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('body.token')
        if (!token) throw new PGPException("$this was unable to validate vpa")
    }

    @Memoized
    Promos getPromos() {
        return new Promos(this)
    }

    @Memoized
    QRCodes getQrCodes() {
        new MerchantQRCodes(this)
    }

    @Memoized
    MerchantUsers getUsers() {
        new MerchantUsers(this)
    }

    @Memoized
    MerchantNames getNames() {
        new MerchantNames(this)
    }

    @Memoized
    Commissions getCommissions() {
        new Commissions(this)
    }

    @Memoized
    boolean isOnus() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check if $this is onus or offus")
        }.path('extendedInfo.ONPAYTM')
    }

    @Memoized
    boolean isPeonEnabled() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check if peon is enabed for $this")
        }.path('extendedInfo.isPeonEnable')
    }

    @Memoized
    int getKeySize() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check keySize of $this")
        }.path('extendedInfo.keySize') as int
    }

    @Memoized
    boolean isActive() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/merchant/get/extended/info/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check keySize of $this")
        }.path('extendedInfo.status') == 'ACTIVE'
    }

    @Memoized
    String getLogoUrl() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/get/merchantlogoinfo/v2/$id").get().thenReturn().tap {
            if (it.statusCode != 200) throw new PGPException("unable to check logo url of $this")
        }.path('merchantImageName') as String
    }

    @Memoized
    boolean isPcfEnabled() {
        this.preferences.any { it.name == 'pcf-fee-info' && it.enabled }
    }

    @Memoized
    boolean isAggregator() {
        this.id == 'cuu5PN33033033550100'
    }

    @Memoized
    boolean isOneClickSupported() {
        this.preferences.oneClickSupported.enabled && this.acquirings.any { it.bank == 'hdfs' }
    }

    @Memoized
    boolean isTokenTxnSupported() {
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
                                        merchantId: this.alipayId,
                                        pageNum   : '1',
                                        pageSize  : '20',
                                ],
                        ],
                        signature: 'signature'
                ]
        ).post().then().defaultParser(Parser.JSON).extract().jsonPath().get("response.body.contractBasics.find { it.productCode in ['51051000100000000001', '51051000100000000101'] }.contractId").tap {
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
            ).post().then().defaultParser(Parser.JSON).extract().jsonPath().get('response.body.productCondition.tokenTransactionSupport').tap {
                assert it in ['true', 'false', null], 'exception occurred while fetching details from alipay'
            }.with {
                it != null ? (['true': true, 'false': false][it]) : false
            }
        }
    }

    boolean isDefault() {
        !this.preferences.addnpay.enabled && !this.preferences.hybrid.enabled
    }

    boolean isAddNPay() {
        this.preferences.addnpay.enabled
    }

    boolean isHybrid() {
        this.preferences.hybrid.enabled
    }

    boolean isLocaleEnabled() {
        this.preferences.isLocaleEnabled().enabled
    }

    int getLimit() {
        this.limit ?: (this.limit = {
            return given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/mapping-service/query/merchant/migration/details/$id/true").get().path('MERCHANT-EXTENDED-INFO.extendedInfo.merchantLimit')
        }())
    }

    @Memoized
    MerchantLimits getLimits() {
        return new MerchantLimits(this)
    }

    @Memoized
    MerchantUserIntersection intersect(User u) {
        return new MerchantUserIntersection(this, u)
    }

    @Override
    boolean equals(Object obj) {
        obj instanceof Merchant && (obj as Merchant).id == this.id
    }

    @Override
    int compareTo(Merchant o) {
        return 0
    }

    String dump() {
        """
[
                id         : $id,
                key        : ${this.getKey()},
                acquirings : $acquirings,
                preferences: $preferences,
                emis       : $emis,
                contracts  : $contracts,
]
"""
    }

    @Memoized
    Object asType(Class<?> clazz) {
        if (clazz == AlipayMerchant) return new AlipayMerchant(this)
        else return super.asType(clazz)
    }

    @Override
    String toString() {
        "merchant($id)"
    }
}
