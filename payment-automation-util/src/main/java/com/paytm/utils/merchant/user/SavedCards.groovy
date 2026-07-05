package com.paytm.utils.merchant.user

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder
import com.paytm.framework.conditions.Wait
import com.paytm.utils.merchant.GList
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification

import java.util.function.Predicate

import static com.paytm.utils.merchant.Constants.PGP_HOST
import static io.restassured.RestAssured.given

class SavedCards implements GList<Card> {

    private static RequestSpecification reqSpec = new RequestSpecBuilder()
            .setBaseUri(PGP_HOST)
            .setContentType(ContentType.JSON)
            .build()

    private final User user

    SavedCards(User user) {
        this.user = user
    }

    @Override
    Iterator<Card> iterator() {
        new Iterator<Card>() {
            List list = given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec).get("/savedcardservice/savedCardService/v1/get/savedcard/userId/${ user.id}").path('response').collect {
                new Card(it.cardId as String, user)
            }
            int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Card next() {
                list[index++]
            }
        }
    }

    @Override
    boolean addAll(Collection<? extends Card> c) {
        if (!user.editable) throw new UnsupportedOperationException()
        c?.every() &&
                c.every { card ->
                    def root = [
                            userId    : user.id,
                            cardNumber: card.no,
                            expiryDate: card.expMo + card.expYr,
                            tokenType : 'JWT'
                    ]
                    root.token = JWT.create()
                            .withIssuer('ts')
                            .tap { token ->
                                root.each {
                                    token.withClaim(it.key, it.value)
                                }
                            }
                            .sign(Algorithm.HMAC256('hUOB1Us6TIv5zAgpDwIcs4QBvGxh0VeR'))

                    given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec).body(root).post('/savedcardservice/savedCardService/v1/add/savedcard/trustedCard').path('responseStatus').with {
                        if (it != 'SUCCESS') return false
                        new Wait({ n -> 200 }, 10, 1).apply({
                            this.contains(card)
                        })
                        return this.contains(card)
                    }
                }
    }

    @Override
    boolean removeAll(Collection<?> c) {
        if (!user.editable) throw new UnsupportedOperationException()
        c?.every() &&
                c.every {
                    given().config(new CurlLoggingRestAssuredConfigBuilder().build()).spec(reqSpec).delete("/savedcardservice/savedCardService/v1/delete/savedcard/userId/cardId/$user.id/${it.id}").path('responseStatus') == 'SUCCESS'
                }
    }

    boolean add(Closure<Boolean> predicate) {
        this.add(new Cards().find(predicate))
    }

    boolean add(Predicate<Card> predicate) {
        this.add({ predicate.test(it) })
    }

    boolean addAll(Closure<Boolean> predicate, int min, int max) {
        assert min > 0 && max >= min
        new Cards().findAll(predicate).with {
            it.size() >= min && this.addAll(it[0..(min - 1)])
        }
    }

    boolean addAll(Predicate<Card> predicate, int min, int max) {
        this.addAll({ predicate.test(it) }, min, max)
    }
}
