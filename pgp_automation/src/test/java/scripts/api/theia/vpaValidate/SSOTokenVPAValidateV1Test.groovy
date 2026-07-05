package scripts.api.theia.vpaValidate

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
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
import scripts.api.theia.SSOTokenAuthenticationTest

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.VPA_VALIDATE
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class SSOTokenVPAValidateV1Test extends TestSetUp implements VPAValidateTest, SSOTokenAuthenticationTest {

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilter(setQueryParamMidFilter)
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
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id
                ],
                body: [
                        vpa: '9999661503@paytm',
                        mid: m()?.id,
                ]
        ]
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when query params are not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when mid in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when orderId in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @AUser
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
    @AUser
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
    @AUser
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
    @AUser
    @Test
    void 'test unable to validate vpa when body vpa equals random alphabetical value'() {
        def root = root()
        root.body.vpa = 'a' * 10
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.invalidUpiId as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
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
    @AUser
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
    @AUser
    @Test
    void 'test unable to validate vpa when body vpa equals vpa whose psp is not registered'() {
        def root = root()
        root.body.vpa = 'invalid@invalid'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.unsuccessfulPaymentRequestTryAgain as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test able to validate vpa when body vpa equals paytm vpa'() {
        def root = root()
        root.body.vpa = 'srivastavaprateek@paytm'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @AUser
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.couldNotVerifyUpiIdTryAgainLater as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.MLV.getId()})
    @AUser
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @AUser
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head tokenType is not provided'() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head tokenType = null'() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head tokenType equals random value'() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token equals SSO token'() {
        def root = root()
        root.head.token = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token equals WALLET token'() {
        def root = root()
        root.head.token = user().tokens['wallet'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token equals TXN token'() {
        def root = root()
        root.head.token = user().tokens['txn'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(edit = true)
    @Test
    void 'test when head token equals expired token'() {
        def root = root()
        def token = user().tokens['sso'].id
        user().tokens.clear()
        root.head.token = token
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant({it.id == Constants.MerchantType.PCF_ONUS.getId()}),
            @Merchant
    ])
    @AUser
    @Test
    void "test when mid provided in query params is different from mid provided in request body"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }
}
