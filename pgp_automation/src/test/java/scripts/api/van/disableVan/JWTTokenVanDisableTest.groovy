package scripts.api.van.disableVan

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import groovy.json.JsonSlurper
import io.qameta.allure.Feature
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.VANProxy.VAN_PROXY_DISABLE
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith

@Owner('Karmvir')
@Link(url = 'https://wiki.mypaytm.com/display/PGP/Disable+VANS%27s+based+on+mid')
@Feature("PGP-28151")

class JWTTokenVanDisableTest extends TestSetUp implements VanDisableTest {

    private final Filter setTokenFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = JWT.create()
                        .withIssuer('c11')
                        .withClaim('mid', root.body.mid)
                        .withClaim('requestId', root.body.requestId)
                        .sign(Algorithm.HMAC256("123456"))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/van-proxy/van-disable-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }
    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setTokenFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VAN_PROXY_DISABLE)
    }


    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Map root() {
        [
                head: [
                        version         : 'v1',
                        clientId        : 'c11',
                        channelId       : 'WEB',
                        requestTimestamp: System.currentTimeMillis().toString(),
                        tokenType       : 'JWT',
                        token           : '?',
                ],
                body: [
                        mid          : m().id,
                        requestId    : UUID.randomUUID().toString(),
                        operationType :[
                                vanDisable:true
                        ]
                ]
        ]
    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "Validate the disable van api when valid mid is provided"() {
        def root=root()
        root.body.mid=m().id
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000"))

    }


    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == false })
    void "Validate the disable van api response when MID is provided which is not having bank transfer"() {
        def root =root()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Bank Transfer not supported")).
                body("body.resultInfo.resultCode", Matchers.equalTo("2001"))
    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "Validate the response when invalid token is provided"() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Jwt Validation Failure")).
                body("body.resultInfo.resultCode", Matchers.equalTo("2003"))
    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "Validate the response when requestid id provided which is having more then 64 digit"() {
        def root = root();
        root.body.mid = m().id;
        root.body.requestId = (0..64).collect { new Random().nextInt(10) }.join('');
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("body.requestId : requestId length should be [1,64] : rejected value"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "Validate the response when mid is provided as blank"() {
        def root= root()
        root.body.mid=""
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("F")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("Incorrect mid")).
        body("body.resultInfo.resultCode", Matchers.equalTo("2004"))
    }

 //   @Override
 //   @Test(enabled = false)
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestId is not provided"() {
        throw new UnsupportedOperationException("Request id is not present in head for this API")

    }

//    @Override
//    @Test(enabled = false)
//    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestId = null"() {
        throw new UnsupportedOperationException("Request id is not present in head for this API")

    }

 //   @Override
 //   @Test(enabled = false)
 //   @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestId = ''"() {
        throw new UnsupportedOperationException("Request id is not present in head for this API")

    }

//    @Override
//    @Test(enabled = false)
 //   @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestId equals random value"() {
        throw new UnsupportedOperationException("Request id is not present in head for this API")

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestTimestamp is not provided"() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestTimestamp = null"() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestTimestamp = \'\'"() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000"))
    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head requestTimestamp equals random value"() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", Matchers.equalTo("S")).
                body("body.resultInfo.resultMsg", Matchers.equalTo("SUCCESS")).
                body("body.resultInfo.resultCode", Matchers.equalTo("0000"))

    }

//    @Override
//    @Test(enabled = false)
//    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head channelId is not provided"() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("head.channelId : channelId should not be empty"))

    }

//    @Override
//    @Test(enabled = false)
//    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head channelId = null"() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("head.channelId : channelId should not be empty"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head channelId = \'\'"() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("head.channelId : accepted values [WEB,WAP,APP,SYSTEM]"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head channelId equals random value"() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("head.channelId : accepted values [WEB,WAP,APP,SYSTEM]"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head tokenType is not provided"() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("head.tokenType : tokenType should not be empty"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head tokenType = null"() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", endsWith("rejected value [null]"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head tokenType = \'\'"() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", endsWith("rejected value []"))

    }

    @Override
    @Test
    @Merchant(edit = false, value = { it.preferences.bankTransfer.enabled == true && it.acquirings.any { it.payMode == 'bank-transfer' && it.enabled == true } })
    void "test when head tokenType equals random value"() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then().
                body("body.resultInfo.resultStatus", equalTo("F")).
                body("body.resultInfo.resultCode", equalTo("0001")).
                body("body.resultInfo.resultMsg", startsWith("head.tokenType : accepted values [JWT,CHECKSUM]"))

    }
}
