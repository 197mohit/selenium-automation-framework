package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Optional
import org.testng.annotations.Parameters
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_ALL_PAYMENT_OFFERS
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchAllPaymentOffers extends TestSetUp {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(FETCH_ALL_PAYMENT_OFFERS)
                        .addQueryParam('mid', m().id)
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        requestId       : new Random().nextLong().abs() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user().tokens['sso'].id
                ],
                body: [
                        mid: m().id,
                ]
        ]
    }

    private final ResponseSpecification rootSchema = new ResponseSpecBuilder()
            .expectStatusCode(isIn(200, 401))
            .expectContentType(ContentType.JSON)
            .expectBody('head', instanceOf(Object.class))
            .expectBody('body', instanceOf(Object.class))
            .build()

    private final ResponseSpecification headSchema = new ResponseSpecBuilder()
            .rootPath('head')
            .expectBody('requestId', instanceOf(String.class))
            .expectBody('responseTimestamp', instanceOf(String.class))
            .expectBody('version', instanceOf(String.class))
            .build()

    private final ResponseSpecification bodySchema = new ResponseSpecBuilder()
            .rootPath('body')
            .expectBody('paymentOffers', anyOf(nullValue(), instanceOf(List.class)))
            .expectBody('resultInfo', instanceOf(Object.class))
            .build()

    private final ResponseSpecification resultInfoSchema = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', instanceOf(String.class))
            .expectBody('resultCode', instanceOf(String.class))
            .expectBody('resultMsg', instanceOf(String.class))
            .build()

    private final ResponseSpecification paymentOffersSchema = new ResponseSpecBuilder()
            .rootPath('body.paymentOffers')
            .expectBody('promocode', everyItem(instanceOf(String.class)))
            .expectBody('offer', everyItem(instanceOf(Object.class)))
            .expectBody('termsUrl', everyItem(instanceOf(String.class)))
            .expectBody('termsTitle', everyItem(instanceOf(String.class)))
//            .expectBody('validFrom', everyItem(instanceOf(String.class)))
//            .expectBody('validUpto', everyItem(instanceOf(String.class)))
            .expectBody('isPromoVisible', everyItem(isIn('true', 'false')))
            .build()

    private final ResponseSpecification offerSchema = new ResponseSpecBuilder()
            .rootPath('body.paymentOffers.offer')
            .expectBody('title', everyItem(instanceOf(String.class)))
            .expectBody('text', everyItem(instanceOf(String.class)))
            .expectBody('icon', everyItem(instanceOf(String.class)))
            .build()

    private final ResponseSpecification invalidSSoToken = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('F'))
            .expectBody('resultCode', equalTo('2004'))
            .expectBody('resultMsg', equalTo('SSO Token is invalid'))
            .build()

    private final ResponseSpecification invalidReqParams = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('F'))
            .expectBody('resultCode', equalTo('1001'))
            .expectBody('resultMsg', equalTo('Request parameters are not valid'))
            .build()

    private final ResponseSpecification somethingWentWrong = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('F'))
            .expectBody('resultCode', equalTo('9999'))
            .expectBody('resultMsg', equalTo('Something went wrong'))
            .build()

    private final def expiredPromo = { new Promo(true, false) }
    private final def usedPromo = { new Promo(false, true) }
    private final def freshPromo = { new Promo(false, false) }

    @Merchant(edit = true,value={it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test(description = 'test no promos returned when no promos are active')
    void testWhenMerchantHasNoPromos() {
        def root = root()
        m().promos.clear()
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .body('body.paymentOffers', hasSize(0))
    }

    @Merchant(edit = true,value={it.id == Constants.MerchantType.Hybrid.getId()})
    @AUser
    @Test(description = 'test all active promos are returned')
    void testWhenMerchantHasPromos() {
        def root = root()
        m().promos.clear()
        assert m().promos.addAll(freshPromo(), freshPromo(), freshPromo(), freshPromo())
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(paymentOffersSchema).spec(offerSchema).spec(resultInfoSchema)
                .body('body.paymentOffers', hasSize(m().promos.size() as Integer))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Parameters('nonExistentToken')
    @Test(description = 'test err msg when non-existent token is supplied')
    void testWhenNonExistentTokenPassed(@Optional('dqwedqwe123e32e3') String nonExistentToken) {
        def root = root()
        root.head.token = nonExistentToken
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(invalidSSoToken)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test(description = 'test err msg when expired token is supplied')
    void testWhenExpiredSSOTokenPassed() {
        def root = root()
        def token = user().tokens['sso'].id
        user().tokens.clear()
        root.head.token = token
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(invalidSSoToken)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'test err msg when sso token is not supplied')
    void testWhenSSOTokenNotPassed() {
        def root = root()
        root.head.remove('token')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(invalidReqParams)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Parameters('nonExistentMid')
    @Test(description = 'test err msg when non-existent mid is supplied')
    void testWhenNonExistentMidPassed(@Optional('dqwedqwe123e32e3') String nonExistentMid) {
        def root = root()
        root.body.mid = nonExistentMid
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(somethingWentWrong)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'test err msg when mid is not supplied')
    void testWhenMidNotPassed() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(invalidReqParams)
    }

//    @Merchant(edit = true)
//    @AUser
//    @Test(enabled = false)
//TODO need to confirm mock behaviour in this case
    void testWhenMerchantHasExpiredPromos() {
        def root = root()
        m().promos.clear()
        assert m().promos.addAll(freshPromo(), expiredPromo())
        req().body(root).post()
                .then()
                .body('body.paymentOffers.promocode', everyItem(not(isIn(m().promos.findAll { it.expired }*.name))))
    }

//    @Merchant(edit = true)
//    @AUser
//    @Test(enabled = false)
//TODO need to confirm mock behaviour in this case
    void testWhenMerchantHasUsedPromos() {
        def root = root()
        m().promos.clear()
        assert m().promos.addAll(freshPromo(), usedPromo())
        req().body(root).post()
                .then()
                .body('body.paymentOffers', everyItem(not(isIn(m().promos.findAll { it.used }))))
    }

}