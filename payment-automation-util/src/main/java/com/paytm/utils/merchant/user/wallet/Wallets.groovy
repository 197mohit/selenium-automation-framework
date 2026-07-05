package com.paytm.utils.merchant.user.wallet

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.utils.merchant.GList
import com.paytm.utils.merchant.user.User
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.WALLET_HOST
import static io.restassured.RestAssured.given

class Wallets implements GList<Wallet> {

    private final User user

    Wallets(User user) {
        this.user = user
    }

    Wallet getAt(String wallet) {
        assert wallet in ['main', 'food', 'gift']
        this.find { it.name == wallet }
    }

//    Wallet sum() {
    //this returning wallet should respresent aggregrator of all wallets e.g
    //if we do wallets.sum().remove(5) when main -> 2, food -> 4, gift -> 1 then it it should be main -> 0, food -> 1, gift -> 1
    //if we do wallets .sum.add(5) when main -> max - 2, food -> max - 4, gift -> 1 then main -> max, food -> max - 1, gift -> 1
//    }

    @Override
    Iterator<Wallet> iterator() {
        return new Iterator<Wallet>() {
            def list = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).baseUri(WALLET_HOST).basePath('/service/checkUserBalance').contentType(ContentType.JSON).headers(['ssotoken': user.tokens.collect().find {
                it.name == 'sso'
            }.id])
                    .body(
                    [
                            request: [
                                    is_detailinfo: "yes"
                            ]
                    ]
            )
                    .post()
                    .path('response.subWalletDetailsList.subWalletName')*.toLowerCase().collect {
                ['main': new MainWallet(user), 'food': new FoodWallet(user)][it]
            }
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Wallet next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Wallet> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }
}
