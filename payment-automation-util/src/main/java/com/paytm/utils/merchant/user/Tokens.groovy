package com.paytm.utils.merchant.user

import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.Wait
import com.paytm.utils.merchant.GList
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType

import static com.paytm.utils.merchant.Constants.AUTH_HOST
import static io.restassured.RestAssured.given

class Tokens implements GList<Token>{

    private final User user
    private final List<Token> tokens = [new SSOToken(user), new PaytmToken(user), new WalletToken(user), new TxnToken(user)]

    Tokens(User user) {
        this.user = user
    }

    Token getAt(String token) {
        assert token in ['sso', 'paytm', 'txn', 'wallet']
        this.find { it.name == token }
    }

    @Override
    Iterator<Token> iterator() {
        new Iterator<Token>() {
            private def list = tokens
            private int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Token next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Token> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    void clear() {
        if (!user.editable) throw new UnsupportedOperationException()
        def token = this['sso'].id
        this.collect().each { it.id = null }
        given().config(new CurlLoggingRestAssuredConfigBuilder().build()).filters([new RequestLoggingFilter(), new ResponseLoggingFilter()]).contentType(ContentType.JSON).baseUri(AUTH_HOST).basePath('oauth2/usertokens').header('authorization', 'Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2').param('accessTokenId', this['sso'].id).delete()
                .with {
            assert statusCode == 200, 'unable to delete tokens'
        }
        new Wait({ n -> 200 }, 10, 1).apply({
            this['sso'].id = null
            this['sso'].id != token
        })
        assert this['sso'].id != token, 'unable to delete tokens'
    }
}