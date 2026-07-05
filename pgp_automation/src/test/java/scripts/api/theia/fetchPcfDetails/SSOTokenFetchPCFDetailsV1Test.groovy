package scripts.api.theia.fetchPcfDetails

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
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PCF_DETAIL
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class SSOTokenFetchPCFDetailsV1Test extends TestSetUp implements FetchPCFDetailsTest, SSOTokenAuthenticationTest {

    private final Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
            if (req.getQueryParams()['mid'] == '?') {
                req.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(req.getBody())?.body?.mid ?: UUID.randomUUID().toString())
            }
            ctx.next(req, res)
        }
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-pcf-details-v1-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addFilters([setQueryParamMidFilter, schemaFilter])
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PCF_DETAIL)
                .addQueryParams([mid: '?' ?: m()?.id ?: UUID.randomUUID().toString(), orderId: UUID.randomUUID().toString()])
    }

    private final RequestSpecification req() { given(reqBldr().build()) }

    private final Map root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        mid       : m()?.id,
                        txnAmount : '1.00',
                        payMethods: [
                                [
                                        payMethod: 'DEBIT_CARD',
                                ],
                        ]
                ]
        ]
    }

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
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void "test when mid in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.FLAT_PCF.getId() }),
            @Merchant({ it.pcfEnabled })
    ])
    @AUser
    @Test
    void "test when mid in query params is different from mid sent in request body"() {
        def root = root()
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void "test when orderId in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
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
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body mid =\'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
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
                .spec(results.invalidMid as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body txnAmount is not provided'() {
        def root = root()
        root.body.remove('txnAmount')
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body txnAmount = null'() {
        def root = root()
        root.body.txnAmount = null
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body txnAmount =\'\''() {
        def root = root()
        root.body.txnAmount = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body txnAmount equals integer value'() {
        def root = root()
        //any valid value
        root.body.txnAmount = '12'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body txnAmount equals decimal value with 1 significant digit after decimal'() {
        def root = root()
        //any valid value
        root.body.txnAmount = '12.1'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body txnAmount equals decimal value with 2 significant digits after decimal'() {
        def root = root()
        //any valid value
        root.body.txnAmount = '12.12'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body txnAmount equals 0'() {
        def root = root()
        root.body.txnAmount = '0'
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods is not provided'() {
        def root = root()
        root.body.remove('payMethods')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods = null'() {
        def root = root()
        root.body.payMethods = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods payMethod equals random value'() {
        def root = root()
        root.body.payMethods = [[payMethod: UUID.randomUUID().toString()]]
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods equals empty list'() {
        def root = root()
        root.body.payMethods = []
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.Hybrid.getId()})
    @AUser
    @Test
    void "test when merchant's commission type not equals post-convenience"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.invalidMid as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod equals pay method not configured on merchant'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = NET_BANKING && body payMethods items instId != PPBL'() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : m().acquirings.find {
                                            it.payMode == 'nb' && it.bank != 'ppbl'
                                        }.bank.toUpperCase()
                                ]]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = NET_BANKING && body payMethods items instId = PPBL'() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : 'PPBL'
                                ]]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = CREDIT_CARD'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'CREDIT_CARD']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = DEBIT_CARD'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'DEBIT_CARD']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = EMI'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

//    @Override
//    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
//    @AUser
//    @Test(enabled = false)
    void 'test when body payMethods items payMethod = EMI_DC'() {
        //TODO need to create merchant with provided config.
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI_DC']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = UPI'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'UPI']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = PAYTM_DIGITAL_CREDIT'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PAYTM_DIGITAL_CREDIT']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = PPBL'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PPBL']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

//    @Override
//    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
//    @AUser
//    @Test(enabled = false)
    void 'test when body payMethods items payMethod = WALLET'() {
        //TODO need to check if tc is valid
        throw new UnsupportedOperationException()
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body payMethods items payMethod = BALANCE'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'BALANCE']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

//    @Override
//    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
//    @AUser
//    @Test(enabled = false)
    void 'test when body payMethods items payMethod = ADVANCE_DEPOSIT_ACCOUNT'() {
        //TODO need to create merchant with provided config.
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void "test when multiple pay methods are provided"() {
        def root = root()
        root.body.payMethods = [
                [payMethod: 'CREDIT_CARD'],
                [payMethod: 'DEBIT_CARD']
        ]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token equals SSO token'() {
        def root = root()
        root.head.token = user().tokens['sso'].id
        req().body(root).post().then()
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
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
    }
}
