package com.paytm.utils.merchant

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.Wait
import com.paytm.utils.merchant.merchant.util.Merchant
import com.paytm.utils.merchant.user.User
import groovy.json.JsonOutput
import io.restassured.http.ContentType
import org.joda.time.DateTime

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import static com.paytm.utils.merchant.Constants.ALIPAY
import static com.paytm.utils.merchant.Constants.PGP_HOST
import static com.paytm.utils.merchant.Constants.WALLET_HOST
import static io.restassured.RestAssured.given

class GiftVouchers implements GList<GiftVoucher> {

    private final Merchant m
    private final User u

    GiftVouchers(Merchant m, User u) {
        this.m = m
        this.u = u
    }

    @Override
    Iterator<GiftVoucher> iterator() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean addAll(Collection<? extends GiftVoucher> c) {
        if (!m.editable || !u.editable) throw new UnsupportedOperationException()
        c.every {
            def root = [
                    request: [
                            body: [
                                    requestId       : new Random().nextLong().abs() as String,
                                    transactionId   : new Random().nextLong().abs() as String,
                                    templateId      : m.templateId,
                                    userId          : u.alipayId,
                                    merchantId      : m.alipayId,
                                    numberOfVouchers: 1,
                                    denomination    : it.amt as String,
                                    expiryPeriod    : '100 months',
                                    purchaseDate    : '2019-06-09 20:22:43',
                                    prodCode        : '51130100100000000001',
                                    extendInfo      : "{\"mobileNo\":\"9891344774\",\"emailId\":\"abhishek1.varshney@paytm.com\",\"acquiringId\":\"2646342173\"}"
                            ]
                    ]
            ]

            JWTCreator.Builder jwtBuilder = JWT.create()
            jwtBuilder.withClaim('ts', System.currentTimeMillis())
            jwtBuilder.withClaim('requestBodyHash', MessageDigest.getInstance('SHA-256').digest(JsonOutput.toJson(root).getBytes(StandardCharsets.UTF_8)).encodeHex().toString())
            def secretKey = 'ca75127e0beb46888d4181bbd848d6d4'
            Algorithm algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(secretKey))
            def token = jwtBuilder.sign(algorithm)

            given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).baseUri(WALLET_HOST).basePath('/fund-service/fundproxy/gv/v1/createInstance/create')
                    .headers(['clientid': '0edd99587f334295aabe9e84687a87fd', 'x-jwt-token': token])
                    .body(root).post().path('').with {
                it?.response?.result?.resultStatus == 'S'
            }
        }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!m.editable || !u.editable) throw new UnsupportedOperationException()
        c.every {
            GiftVoucher v ->
                def root = [
                        request  : [
                                head: [
                                        version     : 'fixed-a',
                                        function    : 'alipayplus.acquiring.order.createOrderAndPay',
                                        clientId    : '2016030715243903536806',
                                        clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                        reqMsgId    : new Random().nextLong().abs() as String,
                                        reqTime     : DateTime.now() as String,
                                ],
                                body: [
                                        order         : [
                                                buyer          : [
                                                        userId          : u.alipayId,
                                                        externalUserId  : System.currentTimeMillis() as String,
                                                        externalUserType: 'MERCHANT',
                                                        nickname        : '',
                                                ],
                                                orderTitle     : new Random().nextLong().abs() as String,
                                                orderAmount    : [
                                                        currency: 'INR',
                                                        value   : (v.amt * 100 as Integer) as String,
                                                ],
                                                merchantTransId: new Random().nextLong().abs() as String,
                                                createdTime    : DateTime.now() as String,
                                        ],
                                        merchantId    : m.alipayId,
                                        mcc           : 'Retail',
                                        productCode   : '51051000100000000001',
                                        envInfo       : [
                                                clientIp    : '14.143.254.210',
                                                osType      : 'JVM (Java)',
                                                terminalType: 'WEB',
                                                extendInfo  : "{\"deviceCategory\":\"Other\",\"browserName\":\"Jakarta Commons-HttpClient 4.5.2\"}",
                                        ],
                                        requestId     : new Random().nextLong().abs() as String,
                                        paymentInfo   : [
                                                payOptionBills: [
                                                        [
                                                                payOption              : 'GIFT_VOUCHER',
                                                                payMethod              : 'GIFT_VOUCHER',
                                                                transAmount            : [
                                                                        currency: 'INR',
                                                                        value   : (v.amt * 100 as Integer) as String,
                                                                ],
                                                                chargeAmount           : [
                                                                        currency: 'INR',
                                                                        value   : '0',
                                                                ],
                                                                topupAndPay            : false,
                                                                cardCacheToken         : '20200130001115f114bbbd8d88e4d3cb9f30218c501e5',
                                                                saveChannelInfoAfterPay: false,
                                                                channelInfo            : "{\"merchantDisplayName\":\"AutomationMerchant\",\"toUseDirectPayment\":\"false\",\"shippingAddr2\":\"shippingAddr2\",\"shippingAddr1\":\"shippingAddr1\",\"directPassThroughInfo\":\"eyJtZXJjaGFudERpc3BsYXlOYW1lIjoiQXV0b21hdGlvbk1lcmNoYW50IiwiY3VzdG9tZXJQaG9uZU5vIjoiNzIwNjY5MTg4NyIsImN1c3RvbWVySWQiOiIxMDAwNTE0MzI5IiwibWVyY2hhbnRMb2dvIjoiaHR0cHM6Ly9tZXJjaGFudC1zdGF0aWMucGF5dG0uY29tL21lcmNoYW50LWRhc2hib2FyZC9sb2dvL050SHlicjMxNTEzMjY1MjQyMDE3L29yZy9sb2dvIiwibWNjIjoiUmV0YWlsIiwibWVyY2hhbnRUeXBlIjoiT0ZGX1VTIiwiaXBBZGRyIjoiMTQuMTQzLjI1NC4yMTAifQ==\",\"mobileNo\":\"9876543210\",\"isEmi\":\"N\",\"merchantType\":\"Offus\",\"cardHoldName\":\"cardHoldName\",\"browserUserAgent\":\"Apache-HttpClient/4.5.2 (Java/1.8.0_222)\"}",
                                                                extendInfo             : "{\"totalTxnAmount\":\"500\",\"CUST_ID\":\"1000514329\",\"issuingBankName\":\"HDFC\",\"graceDays\":\"0\",\"PAYTM_USER_ID\":\"1000514329\",\"merchantAddress.cityName\":\"Noida\",\"merchantAddress.address1\":\"F-28\",\"isEnhancedNative\":\"false\",\"binNumber\":\"471865\",\"communicationManager\":\"false\",\"merchantName\":\"AutomationMerchant\",\"mccCode\":\"Retail\",\"linkBasedInvoicePayment\":\"false\",\"topupAndPay\":\"false\",\"userMobile\":\"XXXXXXXXXX\",\"clientIP\":\"14.143.254.210\",\"additionalInfo\":\"NOIDA\",\"peonURL\":\"https://pgp-automation.paytm.in/mockbank/peon\",\"theme\":\"enhancedweb\",\"issuingBankId\":\"HDFC\",\"alipayMerchantId\":\"216820000000718884173\",\"merchantAddress.countryName\":\"345678953\",\"merchantOnPaytm\":\"false\",\"website\":\"retail\",\"callBackURL\":\"https://pgp-qa8.paytm.in/theia/paytmCallback?ORDER_ID=4c668779d7fb45b88d0f411711547b8bd78\",\"requestType\":\"OFFLINE\",\"merchantLimitEnabled\":\"false\",\"merchantLimitUpdated\":\"false\",\"merchantAddress.zipCode\":\"201301\",\"subsRenewOrderAlreadyCreated\":\"false\",\"linkBasedNonInvoicePayment\":\"false\",\"merchantKybId\":\"1234\",\"offlineFlow\":\"false\",\"productCode\":\"51130100100000000001\",\"merchantAddress.areaName\":\"Sector 8\",\"merchantPhone\":\"91-9191919191\",\"merchantAddress.stateName\":\"100427\",\"merchantAddress.address2\":\"F Block\",\"merchantTransId\":\"4c668779d7fb45b88d0f472647b8bd78\",\"custID\":\"1000514329\",\"paytmMerchantId\":\"NtHybr31513265242017\",\"autoRenewal\":\"false\",\"autoRetry\":\"false\",\"flowType\":\"TRANSACTION\",\"enhancedNative\":\"false\"}",
                                                                templateIds            : [this.m.templateId],
                                                        ]
                                                ]
                                        ],
                                        extendInfo    : "{\"totalTxnAmount\":\"500\",\"CUST_ID\":\"1000514329\",\"issuingBankName\":\"HDFC\",\"graceDays\":\"0\",\"PAYTM_USER_ID\":\"1000514329\",\"merchantAddress.cityName\":\"Noida\",\"merchantAddress.address1\":\"F-28\",\"isEnhancedNative\":\"false\",\"communicationManager\":\"false\",\"merchantName\":\"AutomationMerchant\",\"isMerchantLimitUpdatedForPay\":\"false\",\"mccCode\":\"Retail\",\"linkBasedInvoicePayment\":\"false\",\"topupAndPay\":\"false\",\"isMerchantLimitEnabledForPay\":\"false\",\"userMobile\":\"XXXXXXXXXX\",\"clientIP\":\"14.143.254.210\",\"additionalInfo\":\"NOIDA\",\"peonURL\":\"https://pgp-automation.paytm.in/mockbank/peon\",\"theme\":\"enhancedweb\",\"issuingBankId\":\"HDFC\",\"alipayMerchantId\":\"216820000000718884173\",\"merchantAddress.countryName\":\"345678953\",\"merchantOnPaytm\":\"false\",\"website\":\"retail\",\"callBackURL\":\"https://pgp-qa8.paytm.in/theia/paytmCallback?ORDER_ID=4c669779d7fb45b88d0f411711547b8bd78\",\"requestType\":\"OFFLINE\",\"merchantLimitEnabled\":\"false\",\"merchantLimitUpdated\":\"false\",\"merchantAddress.zipCode\":\"201301\",\"subsRenewOrderAlreadyCreated\":\"false\",\"linkBasedNonInvoicePayment\":\"false\",\"merchantKybId\":\"1234\",\"offlineFlow\":\"false\",\"productCode\":\"51051000100000000001\",\"merchantAddress.areaName\":\"Sector 8\",\"merchantPhone\":\"91-9191919191\",\"merchantAddress.stateName\":\"100427\",\"merchantAddress.address2\":\"F Block\",\"merchantTransId\":\"4c668779d7fb45b88d0f472647b8bd78\",\"custID\":\"1000514329\",\"paytmMerchantId\":\"NtHybr31513265242017\",\"autoRenewal\":\"false\",\"autoRetry\":\"false\",\"flowType\":\"TRANSACTION\",\"enhancedNative\":\"false\"}",
                                        riskExtendInfo: "{\"customerType\":\"false\",\"CHANNEL_ID\":\"WEB\",\"userMerchant\":\"\"}",
                                ],
                        ],
                        signature: 'no-signature',
                ]
                given().baseUri(ALIPAY).basePath('/alipayplus/acquiring/order/createOrderAndPay.htm')
                        .body(root).post().path('').tap {
                    assert it?.response?.body?.resultInfo?.resultStatus == 'A', 'not able to remove balance from gift-vouchers'
                }.with {
                    def cashierReqId = it?.response?.body?.cashierRequestId
                    assert cashierReqId, errMsg
                    root = [
                            request  : [
                                    head: [
                                            version     : 'fixed-a',
                                            function    : 'alipayplus.payment.cashier.payresult.query',
                                            clientId    : '2016030715243903536806',
                                            clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                            reqMsgId    : new Random().nextLong().abs() as String,
                                            reqTime     : DateTime.now() as String,
                                    ],
                                    body: [
                                            cashierRequestId: cashierReqId,
                                    ]

                            ],
                            signature: 'no-signature',
                    ]
                    new Wait({ n -> 5 }, 10, 100).apply({
                        given().baseUri(ALIPAY).basePath('/alipayplus/payment/cashier/payresult/query.htm')
                                .body(root).post().path('').with {
                            it?.response?.body?.resultInfo?.resultStatus == 'S'
                        }
                    }) as Boolean
                }
        }
    }

    double getBalance() {
        def root = [
                head: [
                        "channelId": 'WEB',
                        "tokenType": 'SSO',
                        "token"    : u.tokens['sso'].id,
                ],
                body: [
                        "mid": m.id,
                ]
        ]
        given().contentType(ContentType.JSON).baseUri(PGP_HOST).basePath('/theia/api/v1/fetchPaymentOptions')
                .queryParams([mid: m.id]).body(root).post().path('').tap {
            assert it?.body?.merchantPayOption?.paymentModes?.find { it.paymentMode == 'GIFT_VOUCHER' }
        }.with {
            it?.body?.merchantPayOption?.paymentModes?.find {
                it.paymentMode == 'GIFT_VOUCHER'
            }?.payChannelOptions[0]?.balanceInfo?.accountBalance?.value
        } as Double
    }
}
