package scripts.api.theia.vpaValidate

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.http.Method
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.Assert
import org.testng.annotations.Test
import scripts.api.theia.ChecksumAuthenticationTest

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.VPA_VALIDATE
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class ChecksumVPAValidateV1Test extends TestSetUp implements VPAValidateTest, ChecksumAuthenticationTest {

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, setChecksumFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VPA_VALIDATE)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', UUID.randomUUID().toString())
    }

    RequestSpecification req() {
        given(reqBldr().build())
    }

    Map root() {
        [
                head: [
                        version         : "v1",
                        requestTimestamp: System.currentTimeMillis().toString(),
                        requestId       : UUID.randomUUID().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'CHECKSUM',
                        token           : '?'
                ],
                body: [
                        vpa: '9999661503@paytm',
                        mid: m()?.id,
                ]
        ]
    }

    Filter setChecksumFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body.findAll { it.value != null }))
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

    ResponseSpecification resSpec = new ResponseSpecBuilder()
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/vpa-validate-v1-schema.json'))
            .build()

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when query params are not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when mid in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when orderId in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test unable to validate vpa when body vpa is not provided'() {
        def root = root()
        root.body.remove('vpa')
        req().body(root).post().then()
                .spec(resSpec)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                        'resultCode', equalTo('0'),
                        'resultMsg', equalTo('System Error, invalid param'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test unable to validate vpa when body vpa = null'() {
        def root = root()
        root.body.vpa = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.systemErrorInvalidParam as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test unable to validate vpa when body vpa = \'\''() {
        def root = root()
        root.body.vpa = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.systemErrorInvalidParam as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Test
    void 'test unable to validate vpa when body vpa equals random alphabetical value'() {
        def root = root()
        root.body.vpa = 'a' * 10
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.invalidUpiId as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test unable to validate vpa when body vpa equals random numerical value'() {
        def root = root()
        root.body.vpa = '1' * 10
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.invalidUpiId as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Test
    void 'test unable to validate vpa when body vpa equals value having pattern equals upiHandler@psp'() {
        def root = root()
        root.body.vpa = 'a@a'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.unsuccessfulPaymentRequestTryAgain as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Test
    void 'test unable to validate vpa when body vpa equals vpa whose psp is not registered'() {
        def root = root()
        root.body.vpa = 'test@google'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.unsuccessfulPaymentRequestTryAgain as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Test
    void 'test able to validate vpa when body vpa equals paytm vpa'() {
        def root = root()
        root.body.vpa = 'srivastavaprateek@paytm'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        Response res=req().body(root).post().then().extract().response();
                //.spec(resSpec)
                //.spec(results.invalidChecksum as ResponseSpecification)
        JsonPath response=res.jsonPath();
        Assert.assertEquals(response.getString("body.resultInfo.resultMsg"),"System Error, invalid param");
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mid = null'() {
        String errMsg="System Error, invalid param";
        def root = root()
        root.body.mid = null
       Response res=req().body(root).post().then().extract().response();
               // .spec(resSpec as ResponseSpecification);
                //.spec(results.sysErr as ResponseSpecification)
        JsonPath response=res.jsonPath();
        Assert.assertEquals(response.getString("body.resultInfo.resultMsg"),errMsg);
        Assert.assertEquals(response.getString("body.resultInfo.resultStatus"),"F");
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        Response res=req().body(root).post().then().extract().response()
                //.spec(resSpec)
                //.spec(results.invalidChecksum as ResponseSpecification)
        JsonPath response=res.jsonPath();
        Assert.assertEquals(response.getString("body.resultInfo.resultMsg"),"System Error, invalid param")
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head tokenType is not provided'() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head tokenType = null'() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head tokenType equals random value'() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant({it.id == Constants.MerchantType.UPI_INTENT.getId()}),
            @Merchant(edit = true)
    ])
    @Test
    void 'test when head token equals checksum generated using different merchant\'s key'() {
        def root = root()
        root.head.token = PGPUtil.getChecksum(m(1).key, JsonOutput.toJson(root.body))
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant({it.id == Constants.MerchantType.PGOnly.getId()}),
            @Merchant
    ])
    @Test
    void "test when mid provided in query params is different from mid provided in request body"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.MDR_PCF.getId()})
    @Test(groups = "Security")
    void testWithPutMethodType() {
        def root = root()
        root.head.token = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
        given(reqBldr().removeQueryParam('orderId').build()).body(root).put()
                .then().statusCode(405);
    }

    @Merchant({it.id == Constants.MerchantType.WITHOUT_REQ_TYPE.getId()})
    @Test(groups = "Security")
    void testWithDeleteMethodType() {
        def root = root()
        root.head.token = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
        given(reqBldr().removeQueryParam('orderId').build()).body(root).delete()
                .then().statusCode(501);
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT.getId()})
    @Test
    void testWithTraceMethodType() {
        def root = root()
        root.head.token = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
        given(reqBldr().removeQueryParam('orderId').build()).body(root).request(Method.TRACE)
                .then().statusCode(403);
    }
}
