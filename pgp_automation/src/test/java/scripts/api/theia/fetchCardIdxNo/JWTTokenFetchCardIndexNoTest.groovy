package scripts.api.theia.fetchCardIdxNo

import com.paytm.LocalConfig
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import static org.hamcrest.Matchers.*
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_CARD_INDEX_NO
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath

class JWTTokenFetchCardIndexNoTest extends TestSetUp implements FetchCardIndexNoTest{


    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_CARD_INDEX_NO)
                .addQueryParam('client', 'IN')
                .addQueryParam('referenceId', UUID.randomUUID().toString())
    }

    final def req = { given(reqBldr().build()) }


    final def root = {
        [
                head: [
                        clientId        : 'WEB',
                        version         : 'v1',
                        requestTimestamp: 'Time',
                        channelId       : 'WEB',
                        tokenType       : 'JWT',
                        token           : {
                            def map = [
                                    bankAccountNumber: '919814716299'
                            ]
                            PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, LocalConfig.FETCH_CARD_INDEX_JWT_KEY)
                        }(),
                ],
                body: [
                        bankAccountNumber   : '919814716299',
                        bankIfsc            : 'SBI123'
                ],
        ]
    }


    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-card-idx-no-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    @Override
    @Test
    void "test when reference id in header"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)

    }

    @Override
    @Test
    void "test when without reference id in headers"() {

        def root = root()
        given(reqBldr().removeQueryParam('referenceId').build()).body(root)
                .post().then().root("body.resultInfo")
                .body("resultMsg",equalTo("Request param validation failed"),
                "resultStatus",equalTo("F"),"resultCode",equalTo("1007"))

    }


    @Override
    @Test
    void "test when with incomplete bank account number"() {

        def root = root()
        root.body.bankAccountNumber = '1122123'
        def map=[ bankAccountNumber: '1122123']
        root.head.token = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, LocalConfig.FETCH_CARD_INDEX_JWT_KEY)
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)


    }

    @Override
    @Test
    void "test when with incomplete IFSC number"() {



        def root = root()
        root.body.bankIfsc = 'SBI'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)

    }

    @Override
    @Test
    void "test bank account number set as blank"() {


        def root = root()
        root.body.bankAccountNumber = ''
        req().body(root)
                .post().then().root("body.resultInfo")
                .body("resultStatus",equalTo("F"),"resultCode",
                        equalTo("2021"),"resultMsg",equalTo("Invalid token type"))


    }

    @Override
    @Test
    void "test with bankifsc number set as blank"() {

        def root = root()
        root.body.bankIfsc = ''
        req().body(root)
                .post().then().root("body.resultInfo")
                .body("resultStatus",equalTo("U"),"resultCode",equalTo("00000900"),
                        "resultMsg",equalTo("SYSTEM_ERROR"))

    }

    @Override
    @Test
    void "test with bank, ifsc and cardnumber"() {

        def root = root()
        root.body << [cardNumber:'1123223517842311']
        req().body(root)
                .post().then().root("body.resultInfo")
                .body("resultMsg",equalTo("Request parameters are not valid")
        ,"resultCode",equalTo("1001"),"resultStatus",equalTo("F"))
    }
}
