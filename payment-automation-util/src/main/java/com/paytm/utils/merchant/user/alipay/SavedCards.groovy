package com.paytm.utils.merchant.user.alipay

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.user.Card
import org.joda.time.DateTime

import static com.paytm.utils.merchant.Constants.*
import static io.restassured.RestAssured.given

class SavedCards implements GList<Card> {

    private final AlipayUser u

    SavedCards(AlipayUser u) {
        this.u = u
    }

    @Override
    Iterator<Card> iterator() {
        Closure<Iterator> closure = {
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .baseUri(SUPERGW_LITE)
                    .basePath('user/assets/queryByFilter')
                    .header('Authorization', 'Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=')
                    .body(
                    [
                            userId        : this.u.id,
                            contactBizType: 'PAYMENT_ASSET',
                            includeExpired: true,
                    ]
            )
                    .post()
                    .path('assetInfos.collect { it.value }.flatten()')
                    .collect { new Card(it.bindingId as String, it.cardIndexNo as String) }
                    .iterator()
        }
        return closure()
    }

    @Override
    boolean addAll(Collection<? extends Card> c) {
        if (!this.u.paytmUser.editable) throw new UnsupportedOperationException()
        c.every {
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .baseUri(ALIPAY)
                    .basePath('alipayplus/user/asset/bindAsset.htm')
                    .body(
                    [
                            request  : [
                                    head: [
                                            version     : 'fixed-a',
                                            function    : 'alipayplus.user.asset.bindAsset',
                                            clientId    : '2016030715243903536806',
                                            reqMsgId    : new Random().nextLong().abs() as String,
                                            reqTime     : DateTime.now() as String,
                                            clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                    ],
                                    body: [
                                            userId                : this.u.id,
                                            instNetworkType       : 'ISOCARD',
                                            cardIndexNo           : it.idxNo,
                                            bizType               : 'PAYMENT_ASSET',
                                            lastSuccessfulUsedTime: '2020-02-19 09:04:37.000',
                                    ],
                            ],
                            signature: 'signature',
                    ]
            )
                    .post().jsonPath()
                    .get('response.body.resultInfo.resultCode')?.toString() in ['SUCCESS', 'ASSET_ALREADY_EXIST']
        }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!this.u.paytmUser.editable) throw new UnsupportedOperationException()
        c.every {
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .baseUri(SUPERGW_LITE)
                    .basePath('user/asset/delete')
                    .headers(['Authorization': 'Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw='])
                    .body(
                    [
                            cardIndexNo: it.idxNo,
                            userId     : u.id,
                            envInfo    : [terminalType: 'WEB'],
                    ]
            )
                    .post().jsonPath()
                    .get('status') == 'SUCCESS'
        }
    }
}
