package scripts.api.savedcardservice.getsavedcardbyssotoken

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
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

@Owner('Deepak')
abstract class GetSavedCardBySSOTokenTest extends TestSetUp implements GetSavedCardsOnUserIdTest {

    final static String SC_FETCH_FROM_PLATFORM_PERCENTAGE_FEATURE = 'scFetchFromPlatformPercentageFeature'

    final RequestSpecification req(String ssoToken = user()?.tokens?.getAt('sso')?.id) {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath("/savedcardservice/savedcardOpenAPIService/get/savecard/ssoToken/${ssoToken}")
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
            .expectBody('cardNumber', everyItem(anyOf(containsString('X' * 6), containsString('X' * 7), containsString('X' * 8), containsString('X' * 9), containsString('X' * 10))))
            .expectBody('cardScheme', everyItem(isIn('CC', 'DC')))
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

    final ResponseSpecification errorSchema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .addResponseSpecification(headersResSpec)
            .expectBody('responseStatus', equalTo('FAILURE'))
            .expectBody('httpCode', equalTo('500'))
            .expectBody('httpSubCode', equalTo('500'))
            .expectBody('codeDetail', equalTo('System Error'))
            .expectBody('response', emptyIterable())
            .build()

    @Test
    abstract void "test unable to fetch saved card(s) when ssoToken equals random value"()

    @Test
    abstract void "test able to fetch saved card(s) when ssoToken = paytm token"()

    @Test
    abstract void "test able to fetch saved card(s) when ssoToken = txn token"()

    @Test
    abstract void "test able to fetch saved card(s) when ssoToken = wallet token"()
}
