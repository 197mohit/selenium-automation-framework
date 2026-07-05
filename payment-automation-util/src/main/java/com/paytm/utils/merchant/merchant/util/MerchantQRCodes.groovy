package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.QRCodes
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.WALLET_HOST
import static io.restassured.RestAssured.given

class MerchantQRCodes implements QRCodes {

    private final Merchant m

    MerchantQRCodes(Merchant m) {
        this.m = m
    }

    @Override
    Iterator<QRCode> iterator() {
        new Iterator<QRCode>() {
            private List<QRCode> list = { Merchant m ->
                def root = [
                        request      : [
                                mappingId  : m.id,
                                mappingType: 'P2M_QR',
                                status     : '1',
                                key        : 'cts',
                                order      : 'ASC',
                        ],
                        ipAddress    : '127.0.0.1',
                        platformName : 'PayTM',
                        operationType: 'QR_CODE',
                ]
                def res = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(WALLET_HOST).basePath('/qrcode/v4/fetchQrCodeDetails').headers(([clientid: 'a5516f104428408fb6051f833c9bb9e0', hash: 'd67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94'])).contentType(ContentType.JSON).body(root).post().path('').with {
                    assert statusCode == '200', "unable to fetch QR Codes for $m.id"
                    it.response
                }
                res.collect {
                    new QRCode(it.qrCodeId, it.qrType)
                }
            }(m)
            private int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            QRCode next() {
                list[index++]
            }
        }
    }

    @Override
    List<QRCode> addAll(Collection<? extends QRCode> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        c.collect { QRCode it ->
            def root = [
                    request      : [
                            createRequest: [
                                    businessType: it.businessType ?: 'QR_MERCHANT',
                            ],
                            mapRequest   : [
                                    mappingType   : 'MERCHANT',
                                    typeOfQrCode  : it.qrType ?: 'MERCHANT_QR_CODE',
                                    merchantMid   : this.m.id,
                                    deepLink      : new Random().nextLong().abs() as String,
                                    additionalInfo: [
                                            shopId: new Random().nextLong().abs() as String,
                                            kybId : new Random().nextLong().abs() as String,
                                    ]
                            ],
                            operationType: ['CREATE', 'MAP']
                    ],
                    ipAddress    : '127.0.0.1',
                    platformName : 'PayTM',
                    operationType: 'QR_CODE',
            ]
            given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).headers([clientid: 'a5516f104428408fb6051f833c9bb9e0', hash: 'd67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94']).baseUri(WALLET_HOST).basePath('/qrcode/v4/generateQrCode').body(root).post().then().extract().jsonPath()
                    .get('response?.qrCodeId?.find()')
        }.with {
            !it.contains(null) ? it.collect { id -> new QRCode(id) } : []
        }
    }

    @Override
    List<QRCode> removeAll(Collection<?> c) {
        if (!m.editable) throw new UnsupportedOperationException()
        QRCodes.super.removeAll(c)
    }
}
