package scripts.api.savedcardservice.getcard

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import groovy.json.JsonSlurper
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import scripts.api.savedcardservice.GetSavedCardsOnMidAndCustIdTest
import scripts.api.savedcardservice.GetSavedCardsOnUserIdTest

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

abstract class GetCardTest extends TestSetUp implements GetSavedCardsOnMidAndCustIdTest, GetSavedCardsOnUserIdTest {

    final static String SC_FETCH_FROM_PLATFORM_FOR_MID = 'scFetchFromPlatformForMid'
    final static INPUT_PARAM_MID_OR_CHECKSUM_MISSING = 'Input param mid or checksum missing'
    final static INVALID_CHECKSUM = 'Invalid checksum'
    final static SYSTEM_ERROR = 'System Error'

    final RequestSpecification req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .addFilter(checksumFilter)
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/merchant/v1/get/card")
                        .build()
        )
    }

    final Map<String, Object> root() {
        [
                CUSTID      : m()?.users?.getAt(0)?.id,
                SSO_TOKEN   : user()?.tokens?.getAt('sso')?.id,
                MID         : m()?.id,
                REQUEST_TYPE: 'DEFAULT',
                CHECKSUM    : '?',
        ]
    }

    final Filter checksumFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.with {
                if (it?.CHECKSUM == '?') it.CHECKSUM = getChecksum(m().key, root.findAll { it.key != 'CHECKSUM' && !(it.value in [null, '']) } as TreeMap)
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    final ResponseSpecification headersResSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectHeader('Content-Length', not(isEmptyOrNullString()))
            .expectHeader('Date', not(isEmptyOrNullString()))
            .expectHeaders([
                    "Server"                      : "Apache-Coyote/1.1",
//                    "Content-Security-Policy"     : "default-src 'self' https://*.paytm.com https://*.paytm.in; connect-src 'self' https://*.paytm.com https://*.paytm.in wss://*.paytm.in ; frame-src 'self' https://*.paytm.com https://*.paytm.in; img-src 'self' data: https://*.paytm.in; script-src 'unsafe-eval' 'unsafe-inline' https://*.paytm.in ; style-src 'unsafe-inline' https://*.paytm.in https://maxcdn.bootstrapcdn.com 'self' data: https://*.paytm.in https://themes.googleusercontent.com ;",
                    "Access-Control-Allow-Origin" : "*",
                    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
                    "Access-Control-Allow-Headers": "DNT, X-CustomHeader, Keep-Alive, User-Agent, X-Requested-With, If-Modified-Since, Cache-Control, Content-Type"
            ])
            .build()

    final ResponseSpecification successSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .addResponseSpecification(headersResSpec)
            .expectBody('responseStatus', equalTo('SUCCESS'))
            .expectBody('httpCode', equalTo('200'))
            .expectBody('httpSubCode', equalTo('200'))
            .expectBody('codeDetail', equalTo('Success'))
            .expectBody('response', not(emptyIterable()))
            .rootPath('response')
            .expectBody('savedCardId', everyItem(anyOf(isA(Integer.class), not(isEmptyOrNullString()))))
            .expectBody('cardFirstSixDigits', everyItem(not(isEmptyOrNullString())))
            .expectBody('cardLastFourDigits', everyItem(not(isEmptyOrNullString())))
            .expectBody('cardType', everyItem(isIn('DEBIT_CARD', 'CREDIT_CARD')))
            .expectBody('issuerDisplayName', everyItem(not(isEmptyOrNullString())))
            .expectBody('issuerCode', everyItem(not(isEmptyOrNullString())))
            .expectBody('cardScheme', everyItem(not(isEmptyOrNullString())))
            .build()

    final ResponseSpecification errorSchema = new ResponseSpecBuilder()
            .expectStatusCode(isIn(200, 500))
            .addResponseSpecification(headersResSpec)
            .expectBody('responseStatus', equalTo('FAILURE'))
            .expectBody('httpCode', equalTo('500'))
            .expectBody('httpSubCode', equalTo('500'))
            .expectBody('codeDetail', equalTo('System Error'))
            .expectBody('response', isIn(INPUT_PARAM_MID_OR_CHECKSUM_MISSING, INVALID_CHECKSUM, SYSTEM_ERROR))
            .build()

    final ResponseSpecification noContentSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .addResponseSpecification(headersResSpec)
            .expectBody('responseStatus', equalTo('SUCCESS'))
            .expectBody('httpCode', equalTo('200'))
            .expectBody('httpSubCode', equalTo('204'))
            .expectBody('codeDetail', equalTo('Card does not exist for given parameters'))
            .expectBody('response', emptyIterable())
            .build()

    final ResponseSpecification invalidDataSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .addResponseSpecification(headersResSpec)
            .expectBody('responseStatus', equalTo('FAILURE'))
            .expectBody('httpCode', equalTo('400'))
            .expectBody('httpSubCode', equalTo('406'))
            .expectBody('codeDetail', equalTo('Invalid data entered by user'))
            .expectBody('response', emptyIterable())
            .build()

    final ResponseSpecification emptySchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .addResponseSpecification(headersResSpec)
            .expectBody('responseStatus', equalTo('SUCCESS'))
            .expectBody('httpCode', equalTo('200'))
            .expectBody('httpSubCode', equalTo('200'))
            .expectBody('codeDetail', equalTo('Success'))
            .expectBody('response', emptyIterable())
            .build()

    @Test
    abstract void 'test able to fetch saved card(s) when REQUEST_TYPE is not provided'()

    @Test
    abstract void 'test able to fetch saved card(s) when REQUEST_TYPE = null'()

    @Test
    abstract void "test able to fetch saved card(s) when REQUEST_TYPE = ''"()

    @Test
    abstract void 'test unable to fetch saved card(s) when REQUEST_TYPE equals random value'()

    @Test
    abstract void 'test unable to fetch saved card(s) when MID is not provided'()

    @Test
    abstract void 'test unable to fetch saved card(s) when MID = null'()

    @Test
    abstract void "test unable to fetch saved card(s) when MID = ''"()

    @Test
    abstract void "test unable to fetch saved card(s) when MID equals random value"()

    @Test
    abstract void 'test unable to fetch saved card(s) when CHECKSUM is not provided'()

    @Test
    abstract void 'test unable to fetch saved card(s) when CHECKSUM = null'()

    @Test
    abstract void "test unable to fetch saved card(s) when CHECKSUM = ''"()

    @Test
    abstract void "test unable to fetch saved card(s) when CHECKSUM equals random value"()

    @Test
    abstract void 'test no saved cards are returned when CUSTID && SSO_TOKEN are not provided'()

    @Test
    abstract void 'test no saved cards are returned when CUSTID = null && SSO_TOKEN = null'()

    @Test
    abstract void 'test no saved cards are returned when CUSTID is not provided and no card is saved on userId'()

    @Test
    abstract void 'test no saved cards are returned when SSO_TOKEN is not provided and no card is saved on custId and mId'()

    @Test
    abstract void 'test no saved cards are returned when CUSTID = null and no card is saved on userId'()

    @Test
    abstract void 'test no saved cards are returned when SSO_TOKEN = null and no card is saved on custId and mId'()

    @Test
    abstract void "test no saved cards are returned when CUSTID = '' and no card is saved on userId"()

    @Test
    abstract void "test no saved cards are returned when SSO_TOKEN = '' and no card is saved on custId and mId"()

    @Test
    abstract void 'test no saved cards are returned when CUSTID equals random value and no card is saved on userId'()

    @Test
    abstract void 'test no saved cards are returned when SSO_TOKEN equals random value and no card is saved on custId and mId'()

    @Test
    abstract void 'test able to fetch saved card when CUSTID and SSO_TOKEN are provided given card is saved on custId and mId but not on userId'()

    @Test
    abstract void 'test able to fetch saved card when CUSTID and SSO_TOKEN are provided given card is saved on userId but not on custId and mId'()

    @Test
    abstract void 'test able to fetch saved card(s) when cards are saved on userId and custId and mId'()

    @Test
    abstract void 'test cardLastFourDigits in response is as expected when last 4th digit of card num is 0'()

    @Test
    abstract void 'test able to fetch saved card(s) when same card is saved on both custId & mId and userId'()

    @Test
    abstract void 'test unable to fetch saved card(s) when card is saved on userId given corresponding acquiring is not configured on merchant'()
}
