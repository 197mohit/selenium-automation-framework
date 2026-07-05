package scripts.api.theia.fetchNbPaymentChannels


import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.Group
import com.paytm.base.test.TestSetUp
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
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import scripts.api.theia.AccessAuthenticationTest
import scripts.api.theia.CreateToken

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_NB_PAYMENT_CHANNELS
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class AccessTokenFetchNBPaymentChannelsV1Test extends TestSetUp implements FetchNBPaymentChannelsTest, AccessAuthenticationTest {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    private final ThreadLocal<String> token = new ThreadLocal<>()

    private final ThreadLocal<String> referenceId = new ThreadLocal<>()

    private Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-nb-payment-channels-v1-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_NB_PAYMENT_CHANNELS)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', UUID.randomUUID().toString())
                .addQueryParam('referenceId', referenceId.get())
    }

    private RequestSpecification req() {
        given(reqBldr().build())
    }

    private Map root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        token           : token.get(),
                        tokenType       : 'ACCESS',
                ],
                body: [
                        mid : m()?.id,
                        type: 'MERCHANT',
                ]
        ]
    }

    private static final class ResultInfo {
        static ResponseSpecification TOKEN_VALIDATION_FAILED = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1002'))
                .expectBody('resultMsg', equalTo('TOKEN_VALIDATION_FAILED'))
                .build()
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @BeforeMethod
    void setAccessToken(Method method, ITestResult testResult) {
        try {
            CreateToken api = new CreateToken()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            referenceId.set(root.body.referenceId)
            api.req().body(root).post().then()
                    .spec(results.success as ResponseSpecification)
                    .extract().path('body.accessToken').tap { if (!it) throw new SkipException('unable to generate access token') }.with { token.set(it as String) }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when query params are not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build()).post().then()
                .spec(results.mIdAndOrderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when mid in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).post().then()
                .spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when orderId in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.TOKEN_VALIDATION_FAILED)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type is not provided'() {
        def root = root()
        root.body.remove('type')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type = null'() {
        def root = root()
        root.body.type = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type = \'\''() {
        def root = root()
        root.body.type = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type equals random value'() {
        def root = root()
        root.body.type = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body type = MERCHANT'() {
        def root = root()
        root.body.type = 'MERCHANT'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.NETBANK_PCF.getId()})
    @AUser
    @Test
    void 'test when body type = ADD_MONEY'() {
        def root = root()
        root.body.type = 'ADD_MONEY'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({ !it.payModes.contains('nb') && !it.payModes.contains('ppbl') })
    @Test
    void 'test when merchant has neither nb nor ppbl configured'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', nullValue())
    }

    @Override
    @Merchant({ it.payModes.contains('nb') && !it.payModes.contains('ppbl') })
    @Test
    void "test when merchant has nb configured but not ppbl"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void "test when merchant has both nb and ppbl configured"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void "test when merchant had ppbl configured but not nb"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', emptyIterable())
    }

//    @Override
//    @Merchant({ it.acquirings.any { it.payMode == 'nb' } && it.acquirings.findAll { it.payMode == 'nb' }.every { !it.enabled } })
//    @Test(enabled = false)
    void "test when merchant has nb configured but is disabled"() {
        throw new UnsupportedOperationException('need to create merchant having the required configuration')
    }

//    @Override
//    @Merchant({ it.acquirings.any { it.payMode == 'nb' } && it.acquirings.findAll { it.payMode == 'nb' }.any { !it.enabled } })
//    @Test(enabled = false)
    void 'test when one or more nb channels is disabled'() {
        throw new UnsupportedOperationException('need to create merchant having the required configuration')
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when one or more nb channels is enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.nbPayOption.isDisabled.status', equalTo('false'))
                .body("body.nbPayOption.payChannelOptions.findAll { it.isDisabled.status == 'false' }", hasSize(greaterThan(0)))
    }

//    @Override
//    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
//    @Test(enabled = false)
    void 'test when one or more nb channels has low success rate'() {
        throw new UnsupportedOperationException('need to create merchant having the required configuration')
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when one or more nb channels has high success rate'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.nbPayOption.payChannelOptions.findAll { it.hasLowSuccess.status == 'false' }", hasSize(greaterThan(0)))
                .body("body.nbPayOption.payChannelOptions.findAll { it.hasLowSuccess.status == 'false' }.hasLowSuccess.msg", everyItem(isEmptyString()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType is not provided'() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType = null'() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType equals random value'() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @Test(groups = [Group.Status.TO_BE_FIXED])
    void 'test when head token equals expired token'() {
        //TODO need to check where the access token key is being created and then delete the key from there (Session redis to redis cluster migration issue)
        RedisAPI.deleteKey("AccessTokenRequest_${m().id}_${referenceId.get()}")
        def root = root()
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() }),
            @Merchant
    ])
    @Test
    void 'test when head token equals access token generated by different merchant'() {
        def root = root()
        root.body.mid = m(1).id
        req().body(root).post().then()
                .spec(ResultInfo.TOKEN_VALIDATION_FAILED)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head token equals access token generated for OFFUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test when head token equals access token generated for ONUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() }),
            @Merchant
    ])
    @Test
    void "test when mid provided in query params is different from mid provided in request body"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.nbPayOption', not(hasKey('displayNameRegional')),
                        'body.nbPayOption.isDisabled', not(hasKey('msgRegional')),
                        'body.nbPayOption.payChannelOptions', everyItem(not(hasKey('channelNameRegional'))),
                        'body.nbPayOption.payChannelOptions.hasLowSuccess', everyItem(not(hasKey('msgRegional'))),
                        'body.nbPayOption.payChannelOptions.isDisabled', everyItem(not(hasKey('msgRegional'))))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.nbPayOption', not(hasKey('displayNameRegional')),
                        'body.nbPayOption.isDisabled', not(hasKey('msgRegional')),
                        'body.nbPayOption.payChannelOptions', everyItem(not(hasKey('channelNameRegional'))),
                        'body.nbPayOption.payChannelOptions.hasLowSuccess', everyItem(not(hasKey('msgRegional'))),
                        'body.nbPayOption.payChannelOptions.isDisabled', everyItem(not(hasKey('msgRegional'))))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', (hasKey('resultMsgRegional')),
                        'body.nbPayOption', (hasKey('displayNameRegional')),
                        'body.nbPayOption.isDisabled', (hasKey('msgRegional')),
                        'body.nbPayOption.payChannelOptions', everyItem((hasKey('channelNameRegional'))),
                        'body.nbPayOption.payChannelOptions.hasLowSuccess', everyItem((hasKey('msgRegional'))),
                        'body.nbPayOption.payChannelOptions.isDisabled', everyItem(not(hasKey('msgRegional'))))
    }
}
