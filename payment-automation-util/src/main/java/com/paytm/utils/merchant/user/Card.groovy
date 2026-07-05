package com.paytm.utils.merchant.user

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.merchant.util.MerchantUser
import groovy.transform.Memoized
import groovy.transform.ToString
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.joda.time.DateTime

import static com.paytm.utils.merchant.Constants.ALIPAY
import static com.paytm.utils.merchant.Constants.SUPERGW_LITE
import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

@ToString(includePackage = false, includeNames = true, includes = ['id', 'no', 'type', 'scheme', 'successRate', 'expMo', 'expYr'])
class Card {

    final String id
    final String no
    final String expMo
    final String expYr
    final String cvv
    final String scheme
    final String successRate
    private idxNo
    private final User u
    private final MerchantUser mUser
    private final def queryReq = {
        given(
                new RequestSpecBuilder()
                        .setContentType(ContentType.JSON)
                        .setBaseUri(SUPERGW_LITE)
                        .setBasePath('/user/card/bin/query')
                        .addHeader('Authorization', 'Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=')
                        .setBody([bin: this.no[0..5]])
                        .build()
        )
    }
    private final def modifyReq = {
        given(
                new RequestSpecBuilder()
                        .setContentType(ContentType.JSON)
                        .setBaseUri(SUPERGW_LITE)
                        .setBasePath('/user/card/bin/modify')
                        .addHeader('Authorization', 'Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=')
                        .build()
        )
    }

    private final def binDetailsReq = {
        given(
                new RequestSpecBuilder()
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/mapping-service/get/bankcard/v1/bin/${this.no[0..5]}")
                        .build()
        )
    }

    Card(String no, String expMo, String expYr, String cvv, String scheme, String successRate) {
        this.no = no
        this.expMo = expMo
        this.expYr = expYr
        this.cvv = cvv
        this.scheme = scheme
        this.successRate = successRate
    }

    Card(String id, User u) {
        this.id = id
        this.u = u
    }

    Card(String id, MerchantUser mUser) {
        this.id = id
        this.mUser = mUser
    }

    Card(String id, String idxNo) {
        this.id = id
        this.idxNo = idxNo
    }

    String getIdxNo() {
        this.idxNo ?: (
                this.idxNo = given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                        .baseUri(ALIPAY)
                        .basePath('alipayplus/user/asset/cacheCard.htm')
                        .body(
                        [
                                request  : [
                                        head: [
                                                version     : '1.1.5',
                                                function    : 'alipayplus.user.asset.cacheCard',
                                                clientId    : '2016030715243903536806',
                                                reqMsgId    : new Random().nextLong().abs() as String,
                                                reqTime     : DateTime.now() as String,
                                                clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                        ],
                                        body: [
                                                cardNo         : this.no,
                                                instNetworkCode: 'ISOCARD',
                                                instNetworkType: 'ISOCARD',
                                        ],
                                ],
                                signature: new Random().nextLong().abs() as String,
                        ]
                )
                        .post().jsonPath()
                        .get('response.body.cardIndexNo')
        )
    }

    String getToken() {
        given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .baseUri(ALIPAY)
                .basePath('alipayplus/user/asset/cacheCard.htm')
                .body(
                [
                        request  : [
                                head: [
                                        version     : '1.1.5',
                                        function    : 'alipayplus.user.asset.cacheCard',
                                        clientId    : '2016030715243903536806',
                                        reqMsgId    : new Random().nextLong().abs() as String,
                                        reqTime     : DateTime.now() as String,
                                        clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                ],
                                body: [
                                        cardNo         : this.no,
                                        instNetworkCode: 'ISOCARD',
                                        instNetworkType: 'ISOCARD',
                                ],
                        ],
                        signature: new Random().nextLong().abs() as String,
                ]
        )
                .post().jsonPath()
                .get('response.body.tokenId')
    }

    boolean setBlocked(boolean blocked) {
        def root = [
                bin    : this.no[0..5],
                source : 'ADMIN',
                blocked: blocked,
        ]
        modifyReq().body(root).post().path('status') == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_${this.no[0..5]}").get().asString() == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_WITH_DISPLAY_NAME_${this.no[0..5]}").get().asString() == 'SUCCESS'
    }

    boolean isBlocked() {
        queryReq().post().path('').tap {
            assert it.status == 'SUCCESS'
        }.with { it.cardBinInfo?.blocked != "false" }
    }

    boolean setIndian(boolean indian) {
        def root = [
                bin                : this.no[0..5],
                source             : 'ADMIN',
                binConfigAttributes: [
                        INDIAN             : indian as String,
                        ZERO_SUCCESS_RATE  : zeroSuccessRate,
                        ONE_CLICK_SUPPORTED: oneClickSupported,
                ]
        ]
        modifyReq().body(root).post().path('status') == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_${this.no[0..5]}").get().asString() == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_WITH_DISPLAY_NAME_${this.no[0..5]}").get().asString() == 'SUCCESS'
    }

    boolean isIndian() {
        binDetailsReq().get().path('isIndian').tap {
            assert it instanceof Boolean, 'unable to check if card is Indian'
        } == true
    }

    boolean setOneClickSupported(boolean oneClickSupported) {
        def root = [
                bin                : this.no[0..5],
                source             : 'ADMIN',
                binConfigAttributes: [
                        INDIAN             : indian,
                        ZERO_SUCCESS_RATE  : zeroSuccessRate,
                        ONE_CLICK_SUPPORTED: oneClickSupported as String,
                ]
        ]
        modifyReq().body(root).post().path('status') == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_${this.no[0..5]}").get().asString() == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_WITH_DISPLAY_NAME_${this.no[0..5]}").get().asString() == 'SUCCESS'
    }

    boolean isOneClickSupported() {
        binDetailsReq().get().path('oneClickSupported').tap {
            assert it instanceof Boolean, 'unable to check if card has one click supported'
        } == true
    }

    boolean setZeroSuccessRate(boolean zeroSuccessRate) {
        def root = [
                bin                : this.no[0..5],
                source             : 'ADMIN',
                binConfigAttributes: [
                        INDIAN             : indian,
                        ZERO_SUCCESS_RATE  : zeroSuccessRate as String,
                        ONE_CLICK_SUPPORTED: oneClickSupported,
                ]
        ]
        modifyReq().body(root).post().path('status') == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_${this.no[0..5]}").get().asString() == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_WITH_DISPLAY_NAME_${this.no[0..5]}").get().asString() == 'SUCCESS'
    }

    boolean isZeroSuccessRate() {
        binDetailsReq().get().path('zeroSuccessRate').tap {
            assert it instanceof Boolean, 'unable to check if card has zero success rate'
        } == true
    }

    boolean isPrepaid() {
        binDetailsReq().get().path('prepaidCard').tap {
            assert it instanceof Boolean, 'unable to check if card is Prepaid card'
        } == true
    }


    boolean setPrepaid(boolean prepaid) {
        def root = [
                bin                : this.no[0..5],
                source             : 'ADMIN',
                binConfigAttributes: [
                        INDIAN             : indian,
                        ZERO_SUCCESS_RATE  : zeroSuccessRate as String,
                        ONE_CLICK_SUPPORTED: oneClickSupported,
                        PREPAID_CARD       : prepaid,
                ]
        ]
        modifyReq().body(root).post().path('status') == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_${this.no[0..5]}").get().asString() == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_WITH_DISPLAY_NAME_${this.no[0..5]}").get().asString() == 'SUCCESS'
    }

    boolean setCustomDisplayName(String displayName) {
        def root = [
                bin                : this.no[0..5],
                source             : 'ADMIN',
                binConfigAttributes: [
                        INDIAN             : indian,
                        ZERO_SUCCESS_RATE  : zeroSuccessRate as String,
                        ONE_CLICK_SUPPORTED: oneClickSupported,
                        CUSTOM_DISPLAY_NAME: displayName,
                ]
        ]
        modifyReq().body(root).post().path('status') == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_${this.no[0..5]}").get().asString() == 'SUCCESS' &&
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/BIN_DETAILS_WITH_DISPLAY_NAME_${this.no[0..5]}").get().asString() == 'SUCCESS'
    }

    @Memoized
    String getType() {
        binDetailsReq().get().path('cardType').tap {
            assert it in ['CREDIT_CARD', 'DEBIT_CARD'], 'unable to check the type of card'
        }.with {
            ['CREDIT_CARD': 'credit', 'DEBIT_CARD': 'debit'][it]
        }
    }

    String getEightDigitBinHash() {
        String hash = given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                .baseUri(SUPERGW_LITE)
                .basePath('/user/card/bin/getAlias')
                .contentType(ContentType.JSON)
                .header('Authorization', 'Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=')
                .body([cardBin: this.no[0..7]])
                .post()
                .path('cardBinDigestDetailInfo.eightDigitBinHash')
        if (!hash) throw new RuntimeException('unable to get 8 digit bin hash')
        return hash
    }

    @Override
    boolean equals(Object obj) {
        obj instanceof Card && (
                obj.id == this.id ||
                        obj.no == this.no ||
                        (this.id &&
                                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/savedcardservice/savedCardService/v1/get/savedcard/userId/${this.u?.id ?: obj.u?.id}").get().path("response.find { it.cardId == ${this.id}}")?.with {
                                    return obj.no.substring(0, 6) == it['firstSixDigit'] && obj.no.reverse().substring(0, 4) == it['lastFourDigit'].reverse()
                                }) ||
                        (obj.id &&
                                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("/savedcardservice/savedCardService/v1/get/savedcard/userId/${this.u?.id ?: obj.u?.id}").get().path("response.find { it.cardId == ${obj.id}}")?.with {
                                    return this.no.substring(0, 6) == it['firstSixDigit'] && this.no.reverse().substring(0, 4) == it['lastFourDigit'].reverse()
                                }))
    }
}
