package scripts.api.savedcardservice.pggetsavedcardbyuseridtokenidtokentype

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import groovy.transform.NamedParam
import groovy.transform.NamedVariant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import scripts.api.savedcardservice.GetSavedCardsOnUserIdTest

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

abstract class PGGetSavedCardByUserIdTokenIdTokenTypeTest extends TestSetUp implements GetSavedCardsOnUserIdTest {

    final static String SC_FETCH_FROM_PLATFORM_FOR_USER_ID = 'scFetchFromPlatformForUserId'

    interface TokenType {
        String OAUTH = 'oauth'
    }

    @NamedVariant
    final RequestSpecification req(@NamedParam String userId = user().id, @NamedParam String tokenId = user().tokens['sso'].id, @NamedParam String tokenType = TokenType.OAUTH) {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedCardService/pg/get/savedcard/userId/${userId}/tokenId/${tokenId}/tokenType/${tokenType}")
                        .build()
        )
    }

    final ResponseSpecification headersResSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectHeader('Content-Length', not(isEmptyOrNullString()))
            .expectHeader('Date', not(isEmptyOrNullString()))
            .expectHeaders([
                    "Server"                      : "Apache-Coyote/1.1",
//                    "Content-Security-Policy"     : "default-src 'self' https://*.paytm.com https://*.paytm.in; connect-src 'self' https://*.paytm.com https://*.paytm.in wss://*.paytm.in ; frame-src 'self' https://*.paytm.com https://*.paytm.in; img-src 'self' data: https://*.paytm.in; script-src 'unsafe-eval' 'unsafe-inline' https://*.paytm.in ; style-src 'unsafe-inline' https://*.paytm.in https://maxcdn.bootstrapcdn.com ;font-src 'self' data: https://*.paytm.in https://themes.googleusercontent.com ;",
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
            .expectBody('cardId', everyItem(anyOf(isA(Integer.class), not(isEmptyOrNullString()))))
            .expectBody('cardNumber', everyItem(not(isEmptyOrNullString())))
            .expectBody('cardType', everyItem(isA(Integer.class)))
            .expectBody('expiryDate', everyItem(not(isEmptyOrNullString())))
            .expectBody('firstSixDigit', everyItem(not(isEmptyOrNullString())))
            .expectBody('lastFourDigit', everyItem(not(isEmptyOrNullString())))
            .expectBody('status', everyItem(isIn(0, 1)))
            .expectBody('updated_on', everyItem(isA(Long.class)))
            .expectBody('created_on', everyItem(isA(Long.class)))
            .expectBody('mId', everyItem(isEmptyOrNullString()))
            .expectBody('custId', everyItem(isEmptyOrNullString()))
            .expectBody('cardScheme', everyItem(isIn('DEBIT_CARD', 'CREDIT_CARD')))
            .expectBody('bankName', everyItem(not(isEmptyOrNullString())))
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

    @Test
    abstract void "test unable to fetch saved card(s) when userId equals random value"()

    @Test
    abstract void "test unable to fetch saved card(s) when tokenId equals random value"()

    @Test
    abstract void "test unable to fetch saved card(s) when tokenType equals random value"()
}
