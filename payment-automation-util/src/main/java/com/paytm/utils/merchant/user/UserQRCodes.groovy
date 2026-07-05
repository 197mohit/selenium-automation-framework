package com.paytm.utils.merchant.user

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.QRCodes
import com.paytm.utils.merchant.merchant.util.QRCode
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.WALLET_HOST
import static io.restassured.RestAssured.*

class UserQRCodes implements QRCodes {

    private final User u

    UserQRCodes(User u) {
        this.u = u
    }

    @Override
    Iterator<QRCode> iterator() {
        new Iterator<QRCode>() {
            private List<QRCode> list = { User u ->
                def root = [
                        request      : [
                                mappingId  : u.id,
                                mappingType: 'P2P_QR',
                                status     : '1',
                                key        : 'cts',
                                order      : 'ASC',
                        ],
                        ipAddress    : '127.0.0.1',
                        platformName : 'PayTM',
                        operationType: 'QR_CODE',
                ]
                def res = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(WALLET_HOST).basePath('/qrcode/v4/fetchQrCodeDetails').headers(([clientid: 'a5516f104428408fb6051f833c9bb9e0', hash: 'd67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94'])).contentType(ContentType.JSON).body(root).post().path('').with {
                    assert statusCode == '200', "unable to fetch QR Codes for $u.id"
                    it.response
                }
                res.collect {
                    new QRCode(it.qrCodeId, it.qrType)
                }
            }(u)

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
        if (!u.editable) throw new UnsupportedOperationException()
        c.collect {
            def root = [
                    request      : [
                            createRequest: [
                                    businessType: it.businessType ?: 'P2P',
                            ],
                            mapRequest   : [
                                    mappingType : 'USER',
                                    typeOfQrCode: it.qrType ?: 'USER_QR_CODE',
                                    phoneNo     : u.mobile,
                                    deepLink    : new Random().nextLong().abs() as String,
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
        if (!u.editable) throw new UnsupportedOperationException()
        QRCodes.super.removeAll(c)
    }
}
