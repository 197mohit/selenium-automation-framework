package scripts.api.theia.fetchPayOptions

import com.paytm.apphelpers.PGPHelpers
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
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

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTION_V1
import static com.paytm.appconstants.Constants.Owner.PULKIT
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class SSOTokenFetchPayOptionsV1Test extends FetchPayOptionsV1Test {

    @Override
    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PAYMENT_OPTION_V1)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', UUID.randomUUID().toString())
    }

    @Override
    Map<String, Object> root() {
        [
                "head": [
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis() as String,
                        requestId       : UUID.randomUUID().toString(),
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                "body": [
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

    private final static class ResultInfo {
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void "test unable to fetch pay options details when mid is not provided in query params"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token = \'\''() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token equals random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.invalidMid as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true)
    ])
    @AUser
    @Test
    void "test unable to fetch pay options details when mid in query params is different from mid in body"() {
        def root = root()
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant
    @AUser
    @Test
    void 'test able to fetch body userDetails when token has user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(body_userDetails_schema)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body loginInfo userLoggedIn == true when token has user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.loginInfo.userLoggedIn', equalTo(true))
    }

    @Override
    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser(edit = true)
    @Test
    void 'test body paymentFlow when m hybrid == true && token contains user authentication'() {
        def root = root()
        user().wallets['main'].balance = 1D//any balance > 0
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentFlow', equalTo('HYBRID'))
    }

    @Override
    @Merchant({  it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void 'test body paymentFlow when m addnpay == true && token contains user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentFlow', equalTo('ADDANDPAY'))
    }

    @Owner(PULKIT)
    @Merchant({  it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void 'Verify that merchant limit list is returned in the response for merchantLimit is 1'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantLimitInfo.merchantPaymodesLimits', notNullValue())
    }


    @Owner(PULKIT)
    @Merchant(value = { it.limit == 2 })
    @AUser
    @Test
    void 'Verify that merchant limit list is not returned in the response for merchantLimit is not 1'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantLimitInfo', not(hasKey("merchantPaymodesLimits")))
    }

    @Owner(PULKIT)
    @Merchant({ it.payModes.containsAll(['emi', 'emidc']) && it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test emi type CREDIT_CARD added in enable paymode for EMI pay method'()
    {
        def root = root()
        root.body.enablePaymentMode = [[mode: 'EMI', emiType: 'CREDIT_CARD']]
        root.body.generateOrderId = 'true'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes', hasSize(1))
                .body('body.merchantPayOption.paymentModes[0].displayName', equalTo("EMI"))
                .body('body.merchantPayOption.paymentModes.payChannelOptions.emiType', everyItem(hasItem(equalTo("CREDIT_CARD"))))
    }

    @Owner(PULKIT)
    @Merchant({ it.payModes.containsAll(['emi', 'emidc']) && it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test emi type DEBIT_CARD added in enable paymode for EMI pay method'()
    {
        def root = root()
        root.body.enablePaymentMode = [[mode: 'EMI', emiType: 'DEBIT_CARD']]
        root.body.generateOrderId = 'true'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes', hasSize(1))
                .body('body.merchantPayOption.paymentModes[0].displayName', equalTo('EMI'))
                .body('body.merchantPayOption.paymentModes.payChannelOptions.emiType', everyItem(hasItem(equalTo("DEBIT_CARD"))))
    }

    @Owner(PULKIT)
    @Merchant({ it.payModes.containsAll(['emi', 'emidc']) && it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test enable paymode for EMI pay method when EMI_TYPE is not passed'()
    {
        def root = root()
        root.body.enablePaymentMode = [[mode: 'EMI', emiType: null]]
        root.body.generateOrderId = 'true'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes', hasSize(1))
                .body('body.merchantPayOption.paymentModes[0].displayName', equalTo('EMI'))
                .body('body.merchantPayOption.paymentModes.payChannelOptions.emiType', everyItem(hasItems('CREDIT_CARD','DEBIT_CARD')))
    }


    @Merchant({ it.payModes.containsAll(['emi', 'emidc']) && it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test disable paymode for EMI pay method when EMI_TYPE is not passed'()
    {
        def root = root()
        root.body.disablePaymentMode = [[mode: 'EMI', emiType: null]]
        root.body.generateOrderId = 'true'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes.paymentMode', not(hasItem("EMI")))
    }
}
