package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import groovy.json.JsonSlurper
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
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
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PCF_DETAIL
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchPcfDetails extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addFilter(setQueryParamMidFilter)
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PCF_DETAIL)
                .addQueryParams([mid: '?' ?: m()?.id ?: UUID.randomUUID().toString(), orderId: m()?.orders?.last()?.id ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        txnToken: m()?.orders?.with {
                            assert it.add(new OrderV2(1, user()?.tokens?.getAt('sso')?.id, null))
                            it.last()?.transaction?.token
                        },
                ],
                body: [
                        mid       : m().id,
                        payMethods: [
                                [
                                        payMethod: 'DEBIT_CARD',
                                ],
                        ]
                ]
        ]
    }

    Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
            if (req.getQueryParams()['mid'] == '?') {
                req.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(req.getBody())?.body?.mid ?: UUID.randomUUID().toString())
            }
            ctx.next(req, res)
        }
    }

    ResponseSpecification successSchema(String paymode) {
        new ResponseSpecBuilder()
                .expectBody('head', isA(Object.class))
                .expectBody('body', isA(Object.class))
                .rootPath('head')
                .expectBody('requestId', nullValue())
                .expectBody('responseTimestamp', not(isEmptyOrNullString()))
                .expectBody('version', equalTo('v1'))
                .rootPath('body')
                .expectBody('extraParamsMap', nullValue())
                .expectBody('resultInfo', isA(Object.class))
                .expectBody('consultDetails', isA(Object.class))
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', not(isEmptyOrNullString()))
                .expectBody('resultCode', not(isEmptyOrNullString()))
                .expectBody('resultMsg', not(isEmptyOrNullString()))
                .rootPath('body.consultDetails')
                .expectBody('', hasKey(paymode))
                .rootPath("body.consultDetails.$paymode")
                .expectBody('payMethod', not(isEmptyOrNullString()))
                .expectBody('baseTransactionAmount', allOf(hasKey('currency'), hasKey('value')))
                .expectBody('feeAmount', allOf(hasKey('currency'), hasKey('value')))
                .expectBody('taxAmount', allOf(hasKey('currency'), hasKey('value')))
                .expectBody('totalConvenienceCharges', allOf(hasKey('currency'), hasKey('value')))
                .expectBody('totalTransactionAmount', allOf(hasKey('currency'), hasKey('value')))
                .expectBody('text', not(isEmptyOrNullString()))
                .expectBody('displayText', not(isEmptyOrNullString()))
                .build()
    }

    private final static class ResultInfo {
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when head.txnToken is not provided")
    void "test unable to fetch pcf details when txnToken is not provided in head"() {
        def root = root()
        root.head.remove('txnToken')
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Issue('PGP-24526')
    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test (description = "test unable to fetch pcf details when body.mid is not provided")
    void "test unable to fetch pcf details when mid is not provided in body"() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Issue('PGP-24526')
    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when body.mid = null")
    void "test unable to fetch pcf details when mid equals null in body"() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Issue('PGP-24526')
    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when body.mid equals empty string")
    void "test unable to fetch pcf details when mid equals empty string in body"() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Issue('PGP-24525')
    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when body.mid equals random value")
    void "test unable to fetch pcf details when mid equals random value in body"() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Issue('PGP-24527')
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when mid in query params is not provided")
    void "test unable to fetch pcf details when mid in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() }),
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() }),
    ])
    @Test(description = "test unable to fetch pcf details when mid in query params is different from mid sent in request body")
    void "test unable to fetch pcf details when mid in query params is different from mid sent in request body"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Issue('PGP-24528')
    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when orderId in query params is not provided")
    void "test unable to fetch pcf details when orderId in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(results.orderIdInQueryParamNotMatchingWithOrderIdInRequest as ResponseSpecification)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when body.payMethods is not provided")
    void "test unable to fetch pcf details when payMethods is not provided in body"() {
        def root = root()
        root.body.remove('payMethods')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when body.payMethods = null")
    void "test unable to fetch pcf details when payMethods = null in body"() {
        def root = root()
        root.body.payMethods = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test(description = "test unable to fetch pcf details when body.payMethods equals empty list")
    void "test unable to fetch pcf details when payMethods equals empty list in body"() {
        def root = root()
        root.body.payMethods = []
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test unable to fetch pcf details when body.payMethods.payMethod equals random value")
    void "test unable to fetch pcf details when payMethod equals random value in body"() {
        def root = root()
        root.body.payMethods = [[payMethod: UUID.randomUUID().toString()]]
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Merchant(edit = true, value = { !it.pcfEnabled && it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = "test unable to fetch pcf details when merchant's commission type not equals post-convenience")
    void "test unable to fetch pcf details when merchant's commission type not equals post-convenience"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.invalidMid as ResponseSpecification)
    }

    @Merchant(edit = true, value = {  !it.payModes.contains('ada') && it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test(description = "test unable to fetch pcf details when body.payMethods.payMethod equals pay method not configured on merchant")
    void "test unable to fetch pcf details when payMethod equals pay method not configured on merchant"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant(edit = true, value = {   it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId != PPBL')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsNotPPBL() {
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

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId != PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsNotPPBL')
    void 'test schema of fetch pcf details is as expected when payMethod = NET_BANKING && instId != PPBL'() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : m().acquirings.find {
                                            it.payMode == 'nb' && it.bank != 'ppbl'
                                        }.bank.toUpperCase()
                                ]]
        req().body(root).post().then()
                .spec(successSchema('NET_BANKING'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description ="test body.consultDetails.NET_BANKING.payMethod == 'NET_BANKING' when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId != PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsNotPPBL')
    void "test payMethod == 'NET_BANKING' in cosult and when payMethod = NET_BANKING && instId != PPBL"() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : m().acquirings.find {
                                            it.payMode == 'nb' && it.bank != 'ppbl'
                                        }.bank.toUpperCase()
                                ]]
        req().body(root).post().then()
                .body('body.consultDetails.NET_BANKING.payMethod', equalTo('NET_BANKING'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.NET_BANKING.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId != PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsNotPPBL')
    void "test instId != PPBL"() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : m().acquirings.find {
                                            it.payMode == 'nb' && it.bank != 'ppbl'
                                        }.bank.toUpperCase()
                                ]]
        req().body(root).post().then()
                .body('body.consultDetails.NET_BANKING.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "body.consultDetails.NET_BANKING.displayText == 'Net Banking' when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId != PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsNotPPBL')
    void "testinstId != PPBL"() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : m().acquirings.find {
                                            it.payMode == 'nb' && it.bank != 'ppbl'
                                        }.bank.toUpperCase()
                                ]]
        req().body(root).post().then()
                .body('body.consultDetails.NET_BANKING.displayText', equalTo('Net Banking'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId = PPBL')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsPPBL() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : 'PPBL'
                                ]]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('ppbl') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId = PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsPPBL')
    void 'test schema of fetch pcf details is as expected when payMethod = NET_BANKING && instId = PPBL'() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : 'PPBL'
                                ]]
        req().body(root).post().then()
                .spec(successSchema('PPBL'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('ppbl') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PPBL.payMethod == 'NET_BANKING' when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId = PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsPPBL')
    void "test when payMethod = NET_BANKING && instId = PPBL"() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : 'PPBL'
                                ]]
        req().body(root).post().then()
                .body('body.consultDetails.PPBL.payMethod', equalTo('PPBL'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('ppbl') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PPBL.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId = PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsPPBL')
    void "test instId = PPBL in payMethod"() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : 'PPBL'
                                ]]
        req().body(root).post().then()
                .body('body.consultDetails.PPBL.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('ppbl') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PPBL.displayText == 'Paytm Payments Bank' when body.payMethods.payMethod = NET_BANKING && body.payMethods.instId = PPBL",dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsNET_BANKINGAndInstIdEqualsPPBL')
    void "test displayText == 'Paytm Payments Bank' when payMethod = NET_BANKING in payMethods && instId = PPBL in payMethods"() {
        def root = root()
        root.body.payMethods = [[
                                        payMethod: 'NET_BANKING',
                                        instId   : 'PPBL'
                                ]]
        req().body(root).post().then()
                .body('body.consultDetails.PPBL.displayText', equalTo('Paytm Payments Bank'))
    }

    @Merchant(edit = true, value = {   it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = CREDIT_CARD')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsCREDIT_CARD() {
        def root = root()
        root.body.payMethods = [[payMethod: 'CREDIT_CARD']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('cc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = CREDIT_CARD", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsCREDIT_CARD')
    void 'test schema of fetch pcf details is as expected when payMethod = CREDIT_CARD in Body'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'CREDIT_CARD']]
        req().body(root).post().then()
                .spec(successSchema('CREDIT_CARD'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('cc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.CREDIT_CARD.payMethod == 'NET_BANKING' when body.payMethods.payMethod = CREDIT_CARD", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsCREDIT_CARD')
    void "test payMethod == 'NET_BANKING' when payMethod = CREDIT_CARD"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'CREDIT_CARD']]
        req().body(root).post().then()
                .body('body.consultDetails.CREDIT_CARD.payMethod', equalTo('CREDIT_CARD'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('cc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test pcf detail for cc", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsCREDIT_CARD')
    void "test pcf detail for cc"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'CREDIT_CARD']]
        req().body(root).post().then()
                .body('body.consultDetails.CREDIT_CARD.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('cc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test consultDetails displayText and payMethod for CREDIT_CARD", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsCREDIT_CARD')
    void "test consultDetails displayText and payMethod for CREDIT_CARD"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'CREDIT_CARD']]
        req().body(root).post().then()
                .body('body.consultDetails.CREDIT_CARD.displayText', equalTo('Credit Card'))
    }

    @Merchant(edit = true, value = {   it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = DEBIT_CARD')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsDEBIT_CARD() {
        def root = root()
        root.body.payMethods = [[payMethod: 'DEBIT_CARD']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('dc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = DEBIT_CARD", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsDEBIT_CARD')
    void 'test schema of fetch pcf details is as expected when payMethod = DEBIT_CARD'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'DEBIT_CARD']]
        req().body(root).post().then()
                .spec(successSchema('DEBIT_CARD'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('dc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.DEBIT_CARD.payMethod == 'DEBIT_CARD' when body.payMethods.payMethod = DEBIT_CARD", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsDEBIT_CARD')
    void "test when payMethod = DEBIT_CARD"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'DEBIT_CARD']]
        req().body(root).post().then()
                .body('body.consultDetails.DEBIT_CARD.payMethod', equalTo('DEBIT_CARD'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('dc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.DEBIT_CARD.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = DEBIT_CARD", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsDEBIT_CARD')
    void "test text when payMethod = DEBIT_CARD in body"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'DEBIT_CARD']]
        req().body(root).post().then()
                .body('body.consultDetails.DEBIT_CARD.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('dc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.DEBIT_CARD.displayText == 'Debit Card' when body.payMethods.payMethod = DEBIT_CARD", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsDEBIT_CARD')
    void "test displayText == 'Debit Card' in consult when payMethod = DEBIT_CARD in body"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'DEBIT_CARD']]
        req().body(root).post().then()
                .body('body.consultDetails.DEBIT_CARD.displayText', equalTo('Debit Card'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = EMI')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emi') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = EMI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI')
    void 'test schema of fetch pcf details is as expected when payMethod = EMI'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI']]
        req().body(root).post().then()
                .spec(successSchema('EMI'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emi') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.EMI.payMethod == 'EMI' when body.payMethods.payMethod = EMI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI')
    void "testp ayMethod = EMI"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI']]
        req().body(root).post().then()
                .body('body.consultDetails.EMI.payMethod', equalTo('EMI'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emi') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.EMI.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = EMI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI')
    void "test text in consultDetails EMI whenpayMethod = EMI"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI']]
        req().body(root).post().then()
                .body('body.consultDetails.EMI.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emi') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.EMI.displayText == 'EMI' when body.payMethods.payMethod = EMI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI')
    void "test displayText == 'EMI' payMethod = EMI"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI']]
        req().body(root).post().then()
                .body('body.consultDetails.EMI.displayText', equalTo('EMI'))
    }

//    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emidc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = 'test able to fetch pcf details when body.payMethods.payMethod = EMI_DC')
//TODO need to create merchant with provided config.
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI_DC() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI_DC']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

//    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emidc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = EMI_DC", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI_DC')
    void 'test schema of fetch pcf details is as expected when payMethod = EMI_DC'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI_DC']]
        req().body(root).post().then()
                .spec(successSchema('EMI_DC'))
    }

//    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emidc') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "test body.consultDetails.EMI_DC.payMethod == 'EMI_DC' when body.payMethods.payMethod = EMI_DC", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI_DC')
    void "test when payMethod = EMI_DC"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI_DC']]
        req().body(root).post().then()
                .body('body.consultDetails.EMI_DC.payMethod', equalTo('EMI_DC'))
    }

//    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('emidc')&& it.id == Constants.MerchantType.AMEX_PCF.getId() })
//    @Test(enabled = false, description = "test body.consultDetails.EMI_DC.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = EMI_DC", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI_DC')
    void "test text when payMethod = EMI_DC"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI_DC']]
        req().body(root).post().then()
                .body('body.consultDetails.EMI_DC.text', allOf(startsWith('Rs. '), endsWith(" + GST as applicable.")))
    }

//    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "test body.consultDetails.EMI_DC.displayText == 'EMI DC' when body.payMethods.payMethod = EMI_DC", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsEMI_DC')
    void "test displayText == 'EMI DC' when payMethod = EMI_DC"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'EMI_DC']]
        req().body(root).post().then()
                .body('body.consultDetails.EMI_DC.displayText', equalTo('EMI DC'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = UPI')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsUPI() {
        def root = root()
        root.body.payMethods = [[payMethod: 'UPI']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = UPI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsUPI')
    void 'test schema of fetch pcf details is as expected when payMethod = UPI'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'UPI']]
        req().body(root).post().then()
                .spec(successSchema('UPI'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.UPI.payMethod == 'UPI' when body.payMethods.payMethod = UPI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsUPI')
    void "test when payMethod = UPI"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'UPI']]
        req().body(root).post().then()
                .body('body.consultDetails.UPI.payMethod', equalTo('UPI'))
    }

    @Merchant(edit = true, value = { it.pcfEnabled && it.payModes.contains('upi') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.UPI.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = UPI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsUPI')
    void "test text when payMethod = UPI"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'UPI']]
        req().body(root).post().then()
                .body('body.consultDetails.UPI.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.UPI.displayText == 'BHIM UPI' when body.payMethods.payMethod = UPI", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsUPI')
    void "test displayText == 'BHIM UPI' when payMethod = UPI"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'UPI']]
        req().body(root).post().then()
                .body('body.consultDetails.UPI.displayText', equalTo('BHIM UPI'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = PAYTM_DIGITAL_CREDIT')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsPAYTM_DIGITAL_CREDIT() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PAYTM_DIGITAL_CREDIT']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = PAYTM_DIGITAL_CREDIT", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPAYTM_DIGITAL_CREDIT')
    void 'test schema of fetch pcf details is as expected when payMethod = PAYTM_DIGITAL_CREDIT'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PAYTM_DIGITAL_CREDIT']]
        req().body(root).post().then()
                .spec(successSchema('PAYTM_DIGITAL_CREDIT'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PAYTM_DIGITAL_CREDIT.payMethod == 'PAYTM_DIGITAL_CREDIT' when body.payMethods.payMethod = PAYTM_DIGITAL_CREDIT", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPAYTM_DIGITAL_CREDIT')
    void "test payMethod == 'PAYTM_DIGITAL_CREDIT'"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PAYTM_DIGITAL_CREDIT']]
        req().body(root).post().then()
                .body('body.consultDetails.PAYTM_DIGITAL_CREDIT.payMethod', equalTo('PAYTM_DIGITAL_CREDIT'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PAYTM_DIGITAL_CREDIT.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = PAYTM_DIGITAL_CREDIT", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPAYTM_DIGITAL_CREDIT')
    void "test text when payMethod = PAYTM_DIGITAL_CREDIT"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PAYTM_DIGITAL_CREDIT']]
        req().body(root).post().then()
                .body('body.consultDetails.PAYTM_DIGITAL_CREDIT.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PAYTM_DIGITAL_CREDIT.displayText == 'Paytm Postpaid' when body.payMethods.payMethod = PAYTM_DIGITAL_CREDIT", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPAYTM_DIGITAL_CREDIT')
    void "test displayText == 'Paytm Postpaid' when payMethod = PAYTM_DIGITAL_CREDIT"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PAYTM_DIGITAL_CREDIT']]
        req().body(root).post().then()
                .body('body.consultDetails.PAYTM_DIGITAL_CREDIT.displayText', equalTo('Paytm Postpaid'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = PPBL')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsPPBL() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PPBL']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPPBL')
    void 'test schema of fetch pcf details is as expected when payMethod = PPBL'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PPBL']]
        req().body(root).post().then()
                .spec(successSchema('PPBL'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PPBL.payMethod == 'PPBL' when body.payMethods.payMethod = PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPPBL')
    void "test when payMethod = PPBL"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PPBL']]
        req().body(root).post().then()
                .body('body.consultDetails.PPBL.payMethod', equalTo('PPBL'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PPBL.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = PPBL", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPPBL')
    void "test PPBL text when payMethod = PPBL"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PPBL']]
        req().body(root).post().then()
                .body('body.consultDetails.PPBL.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.PPBL.displayText == 'Paytm Payments Bank' when body.payMethods.payMethod = PPBL, dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsPPBL")
    void "test displayText == 'Paytm Payments Bank' when payMethod = PPBL"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'PPBL']]
        req().body(root).post().then()
                .body('body.consultDetails.PPBL.displayText', equalTo('Paytm Payments Bank'))
    }

//    @Merchant(edit = true, value = {    it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "test able to fetch pcf details when body.payMethods.payMethod = WALLET")
    //TODO need to check if tc is valid
    void 'test able to fetch pcf details when payMethod = WALLET'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'WALLET']]
        req().body(root).post().then()
                .spec(successSchema('WALLET'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test able to fetch pcf details when body.payMethods.payMethod = BALANCE')
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsBALANCE() {
        def root = root()
        root.body.payMethods = [[payMethod: 'BALANCE']]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = BALANCE", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsBALANCE')
    void 'test schema of fetch pcf details is as expected when payMethod = BALANCE'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'BALANCE']]
        req().body(root).post().then()
                .spec(successSchema('BALANCE'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.BALANCE.payMethod == 'BALANCE' when body.payMethods.payMethod = BALANCE", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsBALANCE')
    void "test payMethod == 'BALANCE' when payMethod = BALANCE"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'BALANCE']]
        req().body(root).post().then()
                .body('body.consultDetails.BALANCE.payMethod', equalTo('BALANCE'))
    }

    @Merchant(edit = true, value = {  it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = "test body.consultDetails.BALANCE.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = BALANCE", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsBALANCE')
    void "test consultDetails BALANCE text when payMethod = BALANCE"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'BALANCE']]
        req().body(root).post().then()
                .body('body.consultDetails.BALANCE.text', allOf(startsWith('Convenience fee of Rs. '), endsWith("is applicable.")))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test(description = "test body.consultDetails.BALANCE.displayText == 'Paytm Balance' when body.payMethods.payMethod = BALANCE", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsBALANCE')
    void "test displayText == 'Paytm Balance' when payMethod = BALANCE"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'BALANCE']]
        req().body(root).post().then()
                .body('body.consultDetails.BALANCE.displayText', equalTo('Paytm Balance'))
    }

//    @Merchant(edit = true, value = {  it.payModes.contains('ada') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = 'test able to fetch pcf details when body.payMethods.payMethod = ADVANCE_DEPOSIT_ACCOUNT')
//TODO need to create merchant with provided config.
    void testAbleToFetchPCFDetailsWhenPayMethodEqualsADVANCE_DEPOSIT_ACCOUNT() {
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .spec(successSchema('ADVANCE_DEPOSIT_ACCOUNT'))
    }

//    @Merchant(edit = true, value = {  it.payModes.contains('ada') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "test schema of fetch pcf details is as expected when body.payMethods.payMethod = ADVANCE_DEPOSIT_ACCOUNT", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsADVANCE_DEPOSIT_ACCOUNT')
    void 'test schema of fetch pcf details is as expected when payMethod = ADVANCE_DEPOSIT_ACCOUNT'() {
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .spec(successSchema('ADVANCE_DEPOSIT_ACCOUNT'))
    }

//    @Merchant(edit = true, value = {  it.payModes.contains('ada') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "test body.consultDetails.ADVANCE_DEPOSIT_ACCOUNT.payMethod == 'ADVANCE_DEPOSIT_ACCOUNT' when body.payMethods.payMethod = ADVANCE_DEPOSIT_ACCOUNT", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsADVANCE_DEPOSIT_ACCOUNT')
    void "test payMethod == 'ADVANCE_DEPOSIT_ACCOUNT'"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .body('body.consultDetails.ADVANCE_DEPOSIT_ACCOUNT.payMethod', equalTo('ADVANCE_DEPOSIT_ACCOUNT'))
    }

//    @Merchant(edit = true, value = {  it.payModes.contains('ada') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "\"test body.consultDetails.ADVANCE_DEPOSIT_ACCOUNT.text ==~ /Rs. ... GST as applicable./ when body.payMethods.payMethod = ADVANCE_DEPOSIT_ACCOUNT\"", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsADVANCE_DEPOSIT_ACCOUNT')
    void "test consultDetails ADVANCE_DEPOSIT_ACCOUNT text when payMethod = ADVANCE_DEPOSIT_ACCOUNT"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .body('body.consultDetails.ADVANCE_DEPOSIT_ACCOUNT.text', allOf(startsWith('Rs. '), endsWith(" + GST as applicable.")))
    }

//    @Merchant(edit = true, value = {  it.payModes.contains('ada') && it.id == Constants.MerchantType.AMEX_PCF.getId()})
//    @Test(enabled = false, description = "test body.consultDetails.ADVANCE_DEPOSIT_ACCOUNT.displayText == 'ADVANCE_DEPOSIT_ACCOUNT' when body.payMethods.payMethod = ADVANCE_DEPOSIT_ACCOUNT", dependsOnMethods = 'testAbleToFetchPCFDetailsWhenPayMethodEqualsADVANCE_DEPOSIT_ACCOUNT')
    void "test consultDetails ADVANCE_DEPOSIT_ACCOUNT displayText == 'ADVANCE_DEPOSIT_ACCOUNT' when payMethod = ADVANCE_DEPOSIT_ACCOUNT"() {
        def root = root()
        root.body.payMethods = [[payMethod: 'ADVANCE_DEPOSIT_ACCOUNT']]
        req().body(root).post().then()
                .body('body.consultDetails.ADVANCE_DEPOSIT_ACCOUNT.displayText', equalTo('ADVANCE_DEPOSIT_ACCOUNT'))
    }

    @Merchant(edit = true, value = {   it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test(description = "test able to fetch pcf details when multiple payMethods are provided")
    void 'test able to fetch pcf details when multiple payMethods are provided'() {
        def root = root()
        root.body.payMethods = [
                [payMethod: 'CREDIT_CARD'],
                [payMethod: 'DEBIT_CARD']
        ]
        req().body(root).post().then()
                .spec(successSchema('CREDIT_CARD'))
                .spec(successSchema('DEBIT_CARD'))
    }

//TODO add tc's for LOYALTY_POINT and PREPAID_CARD
}
