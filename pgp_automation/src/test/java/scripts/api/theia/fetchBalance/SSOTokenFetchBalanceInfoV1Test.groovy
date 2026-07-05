package scripts.api.theia.fetchBalance

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.ASubWallets
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Issue
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.junit.Assert
import org.testng.annotations.Test
import scripts.api.theia.HeadTest
import scripts.api.theia.SSOTokenAuthenticationTest
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_BALANCE
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class SSOTokenFetchBalanceInfoV1Test extends TestSetUp implements FetchBalanceInfoTest, SSOTokenAuthenticationTest, HeadTest {

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilter(setQueryParamMidFilter)
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_BALANCE)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', UUID.randomUUID().toString())
    }

    RequestSpecification req() {
        given(reqBldr().build())
    }

    Map root() {
        [
                head: [
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        mid        : m()?.id,
                        paymentMode: 'BALANCE',
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
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-balance-info-v1-schema.json'))
            .build()

    private final static class ResultInfo {
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
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
    void 'test when query params are not provided'() {
        def root = root()
        Response r=given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build())
                .body(root).post().then().extract().response();
               // .spec(resSpec)
                //.spec(results.mIdAndOrderIdMandatoryInQueryParams as ResponseSpecification)
        JsonPath res=r.jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultMsg"),"Mid is mandatory in query parameter");
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when mid is not provided in query params'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build())
                .body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when orderId is not provided in query params'() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build())
                .body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true)
    ])
    @AUser
    @Test
    void 'test when mid provided in query params is different from mid provided in request body'() {
        def root = root()
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build())
                .body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token = \'\''() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token equals random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
    }

    @Override
    @Merchant
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
    @Merchant
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
    @Merchant
    @AUser
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.operationNotSupported as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body paymentMode is not provided'() {
        def root = root()
        root.body.remove('paymentMode')
        req().body(root).post().then()
                .spec(resSpec)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body paymentMode = null'() {
        def root = root()
        root.body.paymentMode = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body paymentMode = \'\''() {
        def root = root()
        root.body.paymentMode = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body paymentMode equals random value'() {
        def root = root()
        root.body.paymentMode = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body paymentMode equals BALANCE given merchant and user both have PPI configured'() {
        def root = root()
        root.body.paymentMode = 'BALANCE'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId()})
    @AUser(kyc = 'any', subWallets = @ASubWallets(main = 'false'))
    @Test
    void 'test when body paymentMode equals BALANCE given merchant and user both do not have PPI configured'() {
        def root = root()
        root.body.paymentMode = 'BALANCE'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.operationNotSupported as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(kyc = 'any', subWallets = @ASubWallets(main = 'false'))
    @Test
    void 'test when body paymentMode equals BALANCE given merchant has PPI configured but user does not'() {
        def root = root()
        root.body.paymentMode = 'BALANCE'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.operationNotSupported as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BAJAJFINEMI.getId()})
    @AUser
    @Test
    void 'test when body paymentMode equals BALANCE given user has PPI configured but merchant does not'() {
        def root = root()
        root.body.paymentMode = 'BALANCE'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.operationNotSupported as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(ppbl = 'true')
    @Test
    void 'test when body paymentMode equals PPBL given merchant and user both have PPBL configured'() {
        def root = root()
        root.body.paymentMode = 'PPBL'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

//    @Override
//    @Merchant({ !it.payModes.contains('ppbl') })
//    @AUser(ppbl = 'false')
//    @Test(enabled = false)
    void 'test when body paymentMode equals PPBL given merchant and user both do not have PPBL configured'() {
        //TODO need to mock response returned from check PPBL balance API(PPBL system API) when user doesn't have ppbl account
        def root = root()
        root.body.paymentMode = 'PPBL'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

//    @Issue('PGP-20262')
//    @Override
//    @Merchant({ it.payModes.contains('ppbl') })
//    @AUser(ppbl = 'false')
//    @Test(enabled = false)
    void 'test when body paymentMode equals PPBL given merchant has PPBL configured but user does not'() {
        //TODO need to mock response returned from check PPBL balance API(PPBL system API) when user doesn't have ppbl account
        def root = root()
        root.body.paymentMode = 'PPBL'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Issue('PGP-20261')
    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser(ppbl = 'true')
    @Test
    void 'test when body paymentMode equals PPBL given user has PPBL configured but merchant does not'() {
        def root = root()
        root.body.paymentMode = 'PPBL'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(paytmcc = 'true')
    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given merchant and user both have PAYTM_DIGITAL_CREDIT configured'() {
        def root = root()
        root.body.paymentMode = 'PAYTM_DIGITAL_CREDIT'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({ !it.payModes.contains('pdc') })
    @AUser(paytmcc = 'false')
    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given merchant and user both do not have PAYTM_DIGITAL_CREDIT configured'() {
        def root = root()
        root.body.paymentMode = 'PAYTM_DIGITAL_CREDIT'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Issue('PGP-20263')
    @Override
    @Merchant({ it.payModes.contains('pdc') })
    @AUser(paytmcc = 'false')
    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given merchant has PAYTM_DIGITAL_CREDIT configured but user does not'() {
        def root = root()
        root.body.paymentMode = 'PAYTM_DIGITAL_CREDIT'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Issue('PGP-20263')
    @Override
    @Merchant({ !it.payModes.contains('pdc') })
    @AUser(paytmcc = 'true')
    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given user has PAYTM_DIGITAL_CREDIT configured but merchant does not'() {
        def root = root()
        root.body.paymentMode = 'PAYTM_DIGITAL_CREDIT'
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
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void 'test when head token equals TXN token'() {
        def root = root()
        root.head.token = user().tokens['txn'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
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



}
