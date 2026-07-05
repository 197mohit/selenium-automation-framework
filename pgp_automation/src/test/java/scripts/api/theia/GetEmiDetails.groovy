package scripts.api.theia

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.ServerConfigProvider
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.LogsValidationHelper
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import groovy.json.JsonSlurper
import io.qameta.allure.Epic
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.qameta.allure.Story
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.testng.Assert
import org.testng.annotations.Test

import static com.paytm.LocalConfig.JWT_EMI_KEY
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.GET_EMI_DETAILS
import static com.paytm.base.test.Group.Status.BUG
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Epic(Constants.Sprint.SPRINT32_2)
@Story('PGP-20178')
@Owner('Deepak')
@Owners(author = 'Deepak', qa = 'Vidhi')
@Test(groups = ['emi-subvention'])
class GetEmiDetails extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addFilters([setQueryParamMidFilter, setSignatureFilter])
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_EMI_DETAILS)
                .addQueryParam('mid', '?')
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        clientId : 'subvention',
                        signature: '?',
                ],
                body: [
                        mid: m()?.id ?: new Random().nextLong().abs() as String,
                        productCode:'',

                ],
        ]
    }

    private final def setQueryParamMidFilter = [filter: { req, res, ctx ->
        if (req.getQueryParams()['mid'] == '?') {
            req.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(req.getBody())?.body?.mid ?: m().id)
        }
        ctx.next(req, res)
    }] as Filter

    private final def setSignatureFilter = [filter: { FilterableRequestSpecification req, res, ctx ->
        def root = new JsonSlurper().parseText(req.getBody())
        root?.head?.with {
            if (it?.signature == '?') it.signature = JWT.create().withIssuer('subvention').
                    withClaim('mid', root.body.mid ?: m().id).
                    withClaim('productCode', root.body.productCode).
                    sign(Algorithm.HMAC256(JWT_EMI_KEY))
        }
        req.body(root)
        ctx.next(req, res)
    }] as Filter

    private final static class ResultInfo {
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void actual_testWhenMidInQueryParamsIsNotSupplied() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

//    @Issue('PGP-23850')
//    @Merchant
//    @Test(enabled = false)
    void expected_testWhenMidInQueryParamsIsNotSupplied() {
        def root = root()
        Response res=given(reqBldr ().removeQueryParam('mid').build()).body(root).post().then().extract().response();
                //.spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
        JsonPath response= res.jsonPath();
        Assert.assertEquals(response.getString("body.resultInfo.resultMsg"),"Request parameters are not valid");
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void testWhenSignatureIsNotSupplied() {
        def root = root()
        root.head.remove('signature')
        req().body(root).post().then()
                .spec(results.tokenValidationFailed as ResponseSpecification)
    }

    @Merchants([@Merchant, @Merchant(edit = true)])
    @Test
    void testWhenSignatureOfOtherMerchantIsUsed() {
        def root = root()
        root.head.signature = JWT.create().withIssuer('subvention').withClaim('mid', m(1).id).sign(Algorithm.HMAC256(JWT_EMI_KEY))
        req().body(root).post().then()
                .spec(results.tokenValidationFailed as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId()})
    @Test
    void actual_testWhenMidIsNotSupplied() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

//    @Issue('PGP-23849')
//    @Merchant
//    @Test(enabled = false)
//    void expected_testWhenMidIsNotSupplied() {
//        def root = root()
//        root.body.remove('mid')
//        req().body(root).post().then()
//                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
//    }


    @Test
    void testWhenNonExistentMidIsSupplied() {
        def root = root()
        root.body.mid = [(0..5).collect {
            ['a'..'z', 'A'..'Z'].flatten().with { it[new Random().nextInt(it.size())] }
        }, (0..15).collect {
            (0..9).with { it[new Random().nextInt(it.size())] }
        }].flatten().join()
        req().body(root).post().then()
                .spec(results.invalidMid as ResponseSpecification)
    }

    @Issue('PGP-23851')
    @Merchants([@Merchant, @Merchant(edit = true)])
    @Test
    void testWhenMidInFormParamAndInQueryParamAreDifferent() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant({it.id == Constants.MerchantType.PaytmExpress_Hybrid_CCPayModeDisabled.getId()})
    @Test
    void testWhenEmiIsNotConfiguredOnMerchant() {
        def root = root()
        req().body(root).post().then()
                .spec(results.emiNotConfiguredOnMerchant as ResponseSpecification)
                .body('body.emiDetails', nullValue())
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void testWhenEmiIsConfiguredOnMerchant() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails', not(emptyIterable()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.size")
    void 'test size in emiDetails in Body'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails', iterableWithSize(m().emis.bank.code.unique().size()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.size")
    void 'test emiDetails in Body'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.emiDetails')
                .body('channelCode', everyItem(isA(String.class)),
                'channelName', everyItem(isA(String.class)),
                'emiType', everyItem(isA(String.class)),
                'iconUrl', everyItem(isA(String.class)),
                'emiChannelInfos', everyItem(isA(List.class)),
                'multiItemEmiSupported', everyItem(isA(Boolean.class)))
    }

    @Merchant({it.id == Constants.MerchantType.BAJAJFINEMI.getId()})
    @Test(description = "test body.emiDetails.channelCode for bajajfn emi'")
    void 'test channelCode for bajajfn emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.channelCode', hasItem('BAJAJFN'))
    }

    @Merchant({it.id == Constants.MerchantType.PG2_AMEX_EMI.getId()})
    @Test(description = "test body.emiDetails.channelCode for amex emi")
    void 'test channelCode for amex emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.channelCode', hasItem('AMEX'))
    }

    @Merchant({it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @Test(description = "test body.emiDetails.channelCode for zest money emi" )
    void 'test channelCode for zest money emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.channelCode', hasItem('ZEST'))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.channelCode for any bank emi")
    void 'test channelCode for any bank emi in emiDetails'() {
        def root = root()
        def bankEmiChannelCodes = m().emis.bank.code*.toUpperCase()
        def emiChannelCodes = req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .extract()
                .path('body.emiDetails.channelCode')
        emiChannelCodes.each {
            assert it in bankEmiChannelCodes
        }
    }

    @Merchant({it.id == Constants.MerchantType.BAJAJFINEMI.getId()})
    @Test (description = "test body.emiDetails.channelName for bajajfn emi")
    void 'test channelName for bajajfn emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.channelName', hasItem('Bajaj Finserv EMI Card'))
    }

    @Merchant({it.id == Constants.MerchantType.PG2_AMEX_EMI.getId()})
    @Test(description = "test body.emiDetails.channelName for amex emi")
    void 'test channelName for amex emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.channelName', hasItem(containsString('American Express')))
    }

    @Merchant({it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @Test(description = "test body.emiDetails.channelName for zest money emi")
    void 'test channelName for zest money emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.channelName', hasItem('ZestMoney'))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.channelName for any bank emi")
    void 'test channelName for any bank emi in emiDetails'() {
        def root = root()
        List channelNames = req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .extract().path('body.emiDetails.channelName')
        m().emis.findAll {
            !(it.bank.code in ['amex', 'bajajfn', 'zest'])
        }.bank.name.unique().each { emi -> assert channelNames*.toLowerCase().any { it.contains(emi) } }
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiType for cc emi")
    void 'test emiType for cc emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiType', hasItem('CREDIT_CARD'))
    }

    @Issue('PGP-24397')
    @Merchant({it.id == Constants.MerchantType.EMI.getId()})
    @Test( description = "test body.emiDetails.emiType for dc emi")
    void 'test emiType for dc emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiType', hasItem('DEBIT_CARD'))
    }

    @Merchant({it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @Test(description = "test body.emiDetails.emiType for zest money emi")
    void 'test emiType for zest money emi in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiType', hasItem('NBFC'))

    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.iconUrl")
    void 'test iconUrl in emiDetails'() {
        def root = root()
        def iconUrls = req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .extract().path('body.emiDetails.iconUrl')
        iconUrls.every {
            assert it ==~ /https?:\/\/.+\..+\..+\/.+\.png/
        }
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos")
    void 'test emiChannelInfos in emiDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.emiDetails.emiChannelInfos.flatten()')
                .body('emiId', everyItem(isA(String.class)),
                'planId', everyItem(isA(String.class)),
                'interestRate', everyItem(isA(String.class)),
                'ofMonths', everyItem(isA(String.class)),
                'minAmount', everyItem(isA(Object.class)),
                'maxAmount', everyItem(isA(Object.class)),
                'bankId', everyItem(isA(String.class)))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos.interestRate")
    void 'test interestRate in emiChannelInfos'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiChannelInfos.interestRate.flatten()', containsInAnyOrder(*m().emis.interest*.toString()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos.bankId")
    void 'test bankId in emiChannelInfos'() {
        String nullValue="null";
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiChannelInfos.bankId',not(null))
                .body('body.emiDetails.emiChannelInfos.bankId',not(nullValue));

    }

    @Merchant({it.id == Constants.MerchantType.EMI.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos.ofMonths")
    void 'test ofMonths in emiChannelInfos'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiChannelInfos.ofMonths.flatten()',notNullValue() )
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos.minAmount")
    void 'test minAmount in emiChannelInfos'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.emiDetails.emiChannelInfos.minAmount')
                .body('currency.flatten()', everyItem(equalTo('INR')),
                'value.flatten()', containsInAnyOrder(*m().emis.minAmt*.toString()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos.maxAmount")
    void 'test maxAmount in emiChannelInfos'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.emiDetails.emiChannelInfos.maxAmount')
                .body('currency.flatten()', everyItem(equalTo('INR')),
                'value.flatten()', containsInAnyOrder(*m().emis.maxAmt*.toString()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos.emiId")
    void 'test emiId in emiChannelInfos'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiChannelInfos.emiId.flatten()', containsInAnyOrder(*m().emis.id*.toString()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos.planId")
    void 'test planId in emiChannelInfos'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails.emiChannelInfos.planId.flatten()', containsInAnyOrder(*m().emis.collect {
            it.bank.code.toUpperCase() + '|' + it.months
        }*.toString()))
    }

    @Merchant({it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @Test(description = "test body.emiDetails.emiChannelInfos for zest money emi")
    void 'test emiChannelInfos for zest money emi in emiDetails'() {
        def root = root()
        def zestEmis = m().emis.findAll { it.bank.code == 'zest' }
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.emiDetails.find { it.channelCode == 'ZEST' }.emiChannelInfos")
                .body('interestRate', containsInAnyOrder(*zestEmis.interest*.toString()),
                'minAmount.currency', everyItem(equalTo('INR')),
                'minAmount.value', containsInAnyOrder(*zestEmis.minAmt*.toString()),
                'bankId', containsInAnyOrder(*zestEmis.bank.id*.toString()),
                'ofMonths', containsInAnyOrder(*zestEmis.months*.toString()),
                'planId', containsInAnyOrder(*zestEmis.collect {
            it.bank.code.toUpperCase() + '|' + it.months
        }*.toString()),
                'maxAmount.currency', everyItem(equalTo('INR')),
                'maxAmount.value', containsInAnyOrder(*zestEmis.maxAmt*.toString()),
                'emiId', containsInAnyOrder(*zestEmis.id))
    }
    //TODO test body.emiDetails.multiItemEmiSupported
    //TODO test body.emiDetails.emiChannelInfos.size


    @Merchant({it.id == Constants.MerchantType.EDC_EMI_MERCH.getId()})
    @Test
    void 'testThatEMIdetailsShouldBefetchedForProductCode51051000100000000001'(){
        String productcode = '51051000100000000001'
        def root = root()
        root.body.productCode = productcode
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails', not(emptyIterable()))
        String cmdToFetchLitePayViewConsultAPIRequest = "grep '${root.body.mid}' /paytm/logs/theia_facade.log | grep 'PAYMENT_CASHIER_LITEPAYVIEW_CONSULT' | grep 'REQUEST'"
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchLitePayViewConsultAPIRequest)
        Assertions.assertThat(theiaFacadeLogs).contains('"productCode":"' + productcode + '"')
    }

//    @Issue('PGP-29503')
//    @Merchant(edit = true, value = { it.acquirings.any { it.payMode == 'emidc' && it.bank == 'axed' } })
//   @Test(enabled = false)
//    void 'testThatEMIdetailsShouldBefetchedForProductCode51051000100000000014'(){
//        String productcode = '51051000100000000014'
//        def root = root()
//        root.body.productCode = productcode
//        req().body(root).post().then()
//                .spec(results.success as ResponseSpecification)
//                .body('body.emiDetails', not(emptyIterable()))
//        String cmdToFetchLitePayViewConsultAPIRequest = "grep '${root.body.mid}' /paytm/logs/theia_facade.log | grep 'PAYMENT_CASHIER_LITEPAYVIEW_CONSULT' | grep 'REQUEST'"
//        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchLitePayViewConsultAPIRequest)
//        Assertions.assertThat(theiaFacadeLogs).contains('"productCode":"' + productcode + '"')
//    }

    @Merchant({it.id == Constants.MerchantType.EDC_EMI_MERCH.getId()})
    @Test
    void 'testThatEMIdetailsShouldBefetchedFor51051000100000000001ProductCodeWhenProductCodeIsNotProvidedInrequest'(){
        String productcode = '51051000100000000001'
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.emiDetails', not(emptyIterable()))
        String cmdToFetchLitePayViewConsultAPIRequest = "grep '${root.body.mid}' /paytm/logs/theia_facade.log | grep 'PAYMENT_CASHIER_LITEPAYVIEW_CONSULT' | grep 'REQUEST'"
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchLitePayViewConsultAPIRequest)
        Assertions.assertThat(theiaFacadeLogs).contains('"productCode":"' + productcode + '"')
    }
}
