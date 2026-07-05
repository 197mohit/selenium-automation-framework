package com.paytm.utils.merchant.merchant.util.alipay

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.user.Card
import org.joda.time.DateTime

import static com.paytm.utils.merchant.Constants.*
import static io.restassured.RestAssured.given

class SavedCards implements GList<Card> {

    private final MerchantUser u

    SavedCards(MerchantUser user) {
        this.u = user
    }

    @Override
    Iterator<Card> iterator() {
        Closure<Iterator> closure = {
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .baseUri(SUPERGW_LITE)
                    .basePath('merchant/asset/query/customerAssets')
                    .headers(['Authorization': 'Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw='])
                    .body(
                    [
                            merchantId    : this.u.m.id,
                            externalUserId: this.u.id,
                            contactBizType: 'MERCHANT_CUSTOMER_ASSET',
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
        if (!this.u.m.paytmMerchant.editable) throw new UnsupportedOperationException()
        c.every {
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .baseUri(ALIPAY)
                    .basePath('alipayplus/merchant/asset/bindAsset.htm')
                    .body(
                    [
                            request  : [
                                    head: [
                                            version     : 'fixed-a',
                                            function    : 'alipayplus.merchant.asset.bindAsset',
                                            clientId    : '2016030715243903536806',
                                            reqMsgId    : new Random().nextLong().abs() as String,
                                            reqTime     : DateTime.now() as String,
                                            clientSecret: 'ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5',
                                    ],
                                    body: [
                                            merchantId            : u.m.id,
                                            externalUserId        : u.id,
                                            instNetworkType       : 'ISOCARD',
                                            cardIndexNo           : it.idxNo,
                                            bizType               : 'MERCHANT_CUSTOMER_ASSET',
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
        if (!this.u.m.paytmMerchant.editable) throw new UnsupportedOperationException()
        c.every {
            given().config(new CurlLoggingRestAssuredConfigBuilder().build())
                    .baseUri(SUPERGW_LITE)
                    .basePath('merchant/asset/delete/customerAsset')
                    .headers(['Authorization': 'Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw='])
                    .body(
                    [
                            cardIndexNo   : it.idxNo,
                            merchantId    : u.m.id,
                            externalUserId: u.id,
                            envInfo       : [terminalType: 'WEB'],
                    ]
            )
                    .post().jsonPath()
                    .get('status') == 'SUCCESS'
        }
    }
}
