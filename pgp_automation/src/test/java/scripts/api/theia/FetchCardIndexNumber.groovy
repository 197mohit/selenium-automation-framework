package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonSlurper
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.*
import com.paytm.appconstants.Constants
import java.text.DecimalFormat

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_CARD_INDEX_NO
import static com.paytm.base.test.Group.Status.BUG
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
class FetchCardIndexNumber extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, setChecksumFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_CARD_INDEX_NO)
                .addQueryParam('mid', '?' ?: m()?.id ?: UUID.randomUUID().toString())
                .addQueryParam('orderId', UUID.randomUUID().toString())
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        version         : 'v1',
                        channelId       : 'WEB',
                        requestTimestamp: System.currentTimeMillis() as String,
                        tokenType       : 'CHECKSUM',
                        token           : '?',
                ],
                body: [
                        cardNumber: cards.find { it.scheme == 'visa' }.no,
                        cardExpiry: cards.find { it.scheme == 'visa' }.with { "$it.expMo/$it.expYr" } as String,
                ]
        ]
    }

    Filter setChecksumFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = PGPUtil.getChecksum(m().key, toJson(root.body))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private ResponseSpecification successSchema = new ResponseSpecBuilder()
            .expectBody('head', isA(Object.class))
            .expectBody('body', isA(Object.class))
            .rootPath('head')
            .expectBody('responseTimestamp', not(isEmptyOrNullString()))
            .expectBody('version', equalTo('v1'))
            .rootPath('body')
            .expectBody('resultInfo', isA(Object.class))
            .expectBody('cardIndexNumber', not(isEmptyOrNullString()))
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', not(isEmptyOrNullString()))
            .expectBody('resultCode', not(isEmptyOrNullString()))
            .expectBody('resultMsg', not(isEmptyOrNullString()))
            .build()

    private ResponseSpecification errorSchema = new ResponseSpecBuilder()
            .expectBody('', hasKey('head'))
            .expectBody('', hasKey('body'))
            .rootPath('head')
            .expectBody('', hasKey('responseTimestamp'))
            .expectBody('', hasKey('version'))
            .rootPath('body')
            .expectBody('', hasKey('resultInfo'))
            .expectBody('', not(hasKey('cardIndexNumber')))
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', not(isEmptyOrNullString()))
            .expectBody('resultCode', not(isEmptyOrNullString()))
            .expectBody('resultMsg', not(isEmptyOrNullString()))
            .build()

    private final static class ResultInfo {
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardNumber is not provided")
    void 'test unable to fetch card index no cardNumber is not provided in body'() {
        def root = root()
        root.body.remove('cardNumber')
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardNumber = null")
    void 'test unable to fetch card index no when cardNumber = null'() {
        def root = root()
        root.body.cardNumber = null
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardNumber = ''")
    void "test unable to fetch card index no when cardNumber = ''"() {
        def root = root()
        root.body.cardNumber = ''
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no when body.cardNumber equals random value")
    void 'test unable to fetch card index no when cardNumber equals random value'() {
        def root = root()
        root.body.cardNumber = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardExpiry is not provided")
    void 'test unable to fetch card index no when cardExpiry is not provided'() {
        def root = root()
        root.body.remove('cardExpiry')
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardExpiry = null")
    void 'test unable to fetch card index no when cardExpiry = null'() {
        def root = root()
        root.body.cardExpiry = null
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardExpiry = ''")
    void "test unable to fetch card index no when cardExpiry = ''"() {
        def root = root()
        root.body.cardExpiry = ''
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardExpiry equals random value")
    void 'test unable to fetch card index no when cardExpiry equals random value'() {
        def root = root()
        root.body.cardExpiry = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Issue('PGP-24698')
    @Merchant
    @Test(groups = BUG, description = "test unable to fetch card index no. when body.cardExpiry equals past value")
    void 'test unable to fetch card index no when cardExpiry equals past value'() {
        def root = root()
        root.body.cardExpiry = new Date().with {
            toMonth().value.with { new DecimalFormat('00').format(it) } + '/' + toYear().minus(1).value
        }
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when body.cardExpiry is not in expected format")
    void 'test unable to fetch card index no when cardExpiry is not in expected format'() {
        def root = root()
        root.body.cardExpiry = new Date().with {
            toMonth().value.with { new DecimalFormat('00').format(it) } + toYear().minus(1).value
        }
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when head.token is not provided")
    void 'test unable to fetch card index no when token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when head.token = null")
    void 'test unable to fetch card index no when token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when head.token = ''")
    void "test unable to fetch card index no when token = ''"() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when head.token equals random value")
    void 'test unable to fetch card index no when token equals random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true),
    ])
    @Test(description = "test unable to fetch card index no. when head.token equals checksum generated by other merchant's key")
    void "test unable to fetch card index no when token equals checksum generated by other merchant's key"() {
        def root = root()
        root.head.token = getChecksum(m(1).key, toJson(root.body))
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when query params are not provided")
    void "test unable to fetch card index no when query params are not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build()).body(root).post().then()
                .spec(errorSchema)
                .spec(results.mIdAndOrderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch card index no. when mid in query params is not provided")
    void "test unable to fetch card index no when mid in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(errorSchema)
                .spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch card index no. when orderId in query params is not provided")
    void "test able to fetch card index no when orderId in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(successSchema)
                .spec(results.success as ResponseSpecification)
    }
//
//    @Issue('PGP-24699')
//    @Merchant
//    @Test(enabled = false, description = "test unable to fetch card index no. when body.mid is not provided")
    //Pulkit needs to confirm if tc is valid
    void 'test unable to fetch card index no when mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post()
    }

//    @Issue('PGP-24699')
//    @Merchant
//    @Test(enabled = false, description = "test unable to fetch card index no. when body.mid = null")
    //Pulkit needs to confirm if tc is valid
    void 'test unable to fetch card index no when mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post()
    }

//    @Issue('PGP-24699')
//    @Merchant
//    @Test(enabled = false, description = "test unable to fetch card index no. when body.mid = ''")
    //Pulkit needs to confirm if tc is valid
    void "test unable to fetch card index no when mid = ''"() {
        def root = root()
        root.body.mid = ''
        req().body(root).post()
    }

//    @Issue('PGP-24699')
//    @Merchant
//    @Test(enabled = false, description = "test unable to fetch card index no. when body.mid equals random value")
    //Pulkit needs to confirm if tc is valid
    void 'test unable to fetch card index no when mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post()
    }

//    @Issue('PGP-24699')
//    @Merchants([
//            @Merchant(edit = true),
//            @Merchant(edit = true),
//    ])
//    @Test(enabled = false, description = "test unable to fetch card index no. when body.mid equals not mid in query params")
    //Pulkit needs to confirm if tc is valid
    void 'test unable to fetch card index no when mid equals not mid in query params'() {
        def root = root()
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post()
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch card index no. when body.cardNumber equals AMEX card no.")
    void 'test able to fetch card index no when cardNumber equals AMEX card no'() {
        def root = root()
        def card = cards.find { it.scheme == 'amex' }
        root.body.cardNumber = card.no
        req().body(root).post().then()
                .spec(successSchema)
                .body('body.cardIndexNumber', equalTo(card.idxNo))
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch card index no. when body.cardNumber equals MAESTRO card no.")
    void 'test able to fetch card index no when cardNumber equals MAESTRO card no'() {
        def root = root()
        def card = cards.find { it.scheme == 'maestro' }
        root.body.cardNumber = card.no
        req().body(root).post().then()
                .spec(successSchema)
                .body('body.cardIndexNumber', equalTo(card.idxNo))
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch card index no. when body.cardNumber equals MASTER card no.")
    void 'test able to fetch card index no when cardNumber equals MASTER card no'() {
        def root = root()
        def card = cards.find { it.scheme == 'master' }
        root.body.cardNumber = card.no
        req().body(root).post().then()
                .spec(successSchema)
                .body('body.cardIndexNumber', equalTo(card.idxNo))
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch card index no. when body.cardNumber equals VISA card no.")
    void 'test able to fetch card index no when cardNumber equals VISA card no'() {
        def root = root()
        def card = cards.find { it.scheme == 'visa' }
        root.body.cardNumber = card.no
        req().body(root).post().then()
                .spec(successSchema)
                .body('body.cardIndexNumber', equalTo(card.idxNo))
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch card index no. when body.cardNumber equals DINERS card no.")
    void 'test able to fetch card index no when cardNumber equals DINERS card no'() {
        def root = root()
        def card = cards.find { it.scheme == 'diners' }
        root.body.cardNumber = card.no
        req().body(root).post().then()
                .spec(successSchema)
                .body('body.cardIndexNumber', equalTo(card.idxNo))
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch card index no. when body.cardNumber equals RUPAY card no.")
    void 'test able to fetch card index no when cardNumber equals RUPAY card no'() {
        def root = root()
        def card = cards.find { it.scheme == 'rupay' }
        root.body.cardNumber = card.no
        req().body(root).post().then()
                .spec(successSchema)
                .body('body.cardIndexNumber', equalTo(card.idxNo))
    }
}