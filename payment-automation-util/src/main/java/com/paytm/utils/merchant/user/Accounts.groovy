package com.paytm.utils.merchant.user

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.Constants
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.util.exception.pgpException.PGPException
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class Accounts implements GList<Account> {

    private final User u

    Accounts(User u) {
        this.u = u
    }

    @Override
    Iterator<Account> iterator() {
        new Iterator<Account>() {
            private List<Account> list = {
                def root = [
                        head: [
                                requestId       : System.currentTimeMillis() as String,
                                requestTimestamp: System.currentTimeMillis() as String,
                                token           : u.tokens['sso'].id,
                                tokenType       : 'SSO',
                        ],
                        body: [
                                channelId: 'WEB'
                        ]
                ]
                return given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath('/savedcardservice/refund/account/query').contentType(ContentType.JSON).body(root).post().path('').with {
                    if (it?.body?.resultInfo?.resultCodeId == '0000') return it.body.refundAccounts.assetId
                    else if (it?.body?.resultInfo?.resultCodeId == '2007') return []
                    else throw new PGPException("not able to fetch account details of ${this.u}")
                }
            }().collect { new Account([id: it]) }
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Account next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Account> c) {
        if (!u.editable) throw new UnsupportedOperationException()
        c.every {
            def root = [
                    head: [
                            requestTimestamp: System.currentTimeMillis() as String,
                            requestId       : System.currentTimeMillis() as String,
                            token           : u.tokens['sso'].id,
                            tokenType       : 'SSO',
                    ],
                    body: [
                            accountNumber: it.no,
                            ifsc         : it.ifsc,
                            bankName     : it.bank,
                            upiAccountId : it.vpa,
                            channelId    : 'WEB',
                            holderName   : [
                                    firstName: it.firstName,
                                    lastName : it.lastName,
                            ]
                    ]
            ]
            try {
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath('/savedcardservice/refund/account/add').contentType(ContentType.JSON).body(root).post().path('body.resultInfo.resultCode') in ['SUCCESS', 'DUPLICATE_REQUEST_FOR_ADD_ASSET'] &&
                        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/ASSET_ADD_${u.tokens['sso'].id}").get().asString() == 'SUCCESS'
            } catch (Exception e) {
                return false
            }
        }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!u.editable) throw new UnsupportedOperationException()
        c.every {
            def root = [
                    head: [
                            requestId       : System.currentTimeMillis() as String,
                            requestTimestamp: System.currentTimeMillis() as String,
                            token           : u.tokens['sso'].id,
                            tokenType       : 'SSO',
                    ],
                    body: [
                            assetId  : it.id,
                            channelId: 'WEB'
                    ]
            ]
            try {
                given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath('/savedcardservice/refund/account/remove').contentType(ContentType.JSON).body(root).post().path('body.resultInfo.resultStatus') == 'S' &&
                        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(PGP_HOST).basePath("mapping-service/common/v1/remove/ASSET_ADD_${u.tokens['sso'].id}").get().asString() == 'SUCCESS'
            } catch (Exception e) {
                return false
            }
        }
    }
}
