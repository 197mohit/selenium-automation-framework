package com.paytm.utils.merchant.merchant.util

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import groovy.transform.ToString
import io.restassured.http.ContentType
import io.restassured.parsing.Parser

import static com.paytm.utils.merchant.Constants.WALLET_HOST
import static io.restassured.RestAssured.given

@ToString(includePackage = false, includeNames = true, includes = ['id', 'businessType', 'mappingType', 'qrType'], ignoreNulls = true)
class QRCode {

    final String id
    final String businessType
    final String mappingType
    final String qrType

    QRCode(String id, String qrType) {
        this.id = id
        this.qrType = qrType
    }

    QRCode() {
    }

    QRCode(String id) {
        this.id = id
    }

    QRCode(String businessType, String mappingType, String qrType) {
        this.businessType = businessType
        this.mappingType = mappingType
        this.qrType = qrType
    }

    static QRCode P2P() {
        new QRCode('P2P', 'USER', 'USER_QR_CODE')
    }

    static QRCode P2PDebit() {
        new QRCode('P2P', 'USER', 'DEBIT_QR_CODE')
    }

    static QRCode P2PDynamic() {
        new QRCode('P2P', 'USER', 'DYNAMIC_QR_CODE')
    }

    static QRCode Debit() {
        new QRCode('QR_DEBIT', 'USER', 'DEBIT_QR_CODE')
    }

    static QRCode DebitUser() {
        new QRCode('QR_DEBIT', 'USER', 'USER_QR_CODE')
    }

    static QRCode DebitDynamic() {
        new QRCode('QR_DEBIT', 'USER', 'DYNAMIC_QR_CODE')
    }

    static QRCode Merchant() {
        new QRCode('QR_MERCHANT', 'MERCHANT', 'MERCHANT_QR_CODE')
    }

    static QRCode Product() {
        new QRCode('QR_PRODUCT', 'MERCHANT', 'QR_PRODUCT')
    }

    static QRCode PricedProduct() {
        new QRCode('QR_PRICEDPRODUCT', 'MERCHANT', 'QR_PRICEDPRODUCT')
    }

    static QRCode UPI() {
        new QRCode('UPI_QR_CODE', 'MERCHANT', 'UPI_QR_CODE')
    }

    boolean isEnabled() {
        throw new UnsupportedOperationException()
    }

    boolean setEnabled(boolean activate) {
        def root = [
                request      : [
                        qrCodeId        : id,
                        activationStatus: activate,
                ],
                ipAddress    : '127.0.0.1',
                platformName : 'PayTM',
                operationType: 'QR_CODE',
        ]
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).contentType(ContentType.JSON).headers([clientid: 'a5516f104428408fb6051f833c9bb9e0', hash: 'd67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94']).baseUri(WALLET_HOST).basePath('/qrcode/v4/editQrCodeDetails').body(root).post().then().extract().jsonPath()
                .get('response.editResponse') == 'SUCCESS'
    }

}
