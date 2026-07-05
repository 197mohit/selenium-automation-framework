package scripts.api.theia

import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.filter.Filter
import io.restassured.specification.ResponseSpecification
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Factory
import org.testng.annotations.Test
import scripts.api.theia.vpaValidate.SSOTokenVPAValidateV1Test
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV1Test
import scripts.api.theia.fetchBalance.SSOTokenFetchBalanceInfoV1Test
import scripts.api.theia.fetchBinDetail.SSOTokenFetchBinDetailV1Test
import scripts.api.theia.fetchEmiDetail.SSOTokenFetchEmiDetailV1Test
import scripts.api.theia.fetchNbPaymentChannels.SSOTokenFetchNBPaymentChannelsV1Test

import java.lang.reflect.Method

import static com.paytm.appconstants.Constants.MerchantType.AMEX_PCF
import static com.paytm.appconstants.Constants.MerchantType.NATIVE_PROMO_HYBRID
import static com.paytm.appconstants.Constants.promoCode.WALLET_PROMO
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

@Owner("Deepak")
class OfflineCompatibilityTest extends TestSetUp {

    def filter1
    def filter2

    def orderId = new ThreadLocal<String>()

    @Factory(dataProvider = 'dataMethod')
    OfflineCompatibilityTest(filter1, filter2) {
        this.filter1 = filter1
        this.filter2 = filter2
    }

    @DataProvider(name = "dataMethod")
    static Object[][] dataMethod() {
        def primaryFilter = [filter: { requestSpec, responseSpec, ctx -> ctx.next(requestSpec, responseSpec) }] as Filter
        def secondaryFilter = [filter: { requestSpec, responseSpec, ctx ->
            requestSpec.basePath(requestSpec.getBasePath().replace('theia', 'theia1'))
            ctx.next(requestSpec, responseSpec)
        }] as Filter
        return [
                [0, 1],
                [1, 0],
        ].collect {
            it.collect { it ? secondaryFilter : primaryFilter }
        }
    }

    @BeforeMethod
    void testFetchPayOptions(Method method, ITestResult testResult) {
        try {
            def api = new SSOTokenFetchPayOptionsV1Test()
            def root = api.root()
            root.head.tokenType = 'SSO'
            root.head.token = user().tokens['sso'].id
            root.head.txnToken = null
            root.body.generateOrderId = 'true'
            orderId.set(given(api.reqBldr().removeQueryParam('orderId').build()).body(root).filter(filter1).post().then().spec(results.success as ResponseSpecification).extract().path('body.orderId'))
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Merchant({it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId()})
    @AUser
    @Test
    void testFetchNBPaymentChannels() {
        def api = new SSOTokenFetchNBPaymentChannelsV1Test()
        def root = api.root()
        root.head.token = user().tokens['sso'].id
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void testFetchBinDetail() {
        def api = new SSOTokenFetchBinDetailV1Test()
        def root = api.root()
        root.head.token = user().tokens['sso'].id
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly_Pcf.getId()})
    @AUser
    @Test
    void testFetchBalanceInfo() {
        def api = new SSOTokenFetchBalanceInfoV1Test()
        def root = api.root()
        root.head.token = user().tokens['sso'].id
        given(api.reqBldr().removeQueryParam('mid').removeQueryParam('orderId').addQueryParams([mid: m().id, orderId: orderId.get()]).build())
                .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void testFetchEmiDetail() {
        def api = new SSOTokenFetchEmiDetailV1Test()
        def root = api.root()
        root.head.token = user().tokens['sso'].id
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @AUser
    @Test
    void testFetchMerchantUserInfo() {
        def api = new FetchMerchantUserInfo()
        def root = api.root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens['sso'].id
        root.head.txnToken = null
        root.body.orderId = orderId.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

//    @Merchant(edit = true, value = { it.id == AMEX_PCF.id })
//    @AUser
//    @Test(enabled = false)
//TODO getting null-pointer-exception when authenticated by sso token
    void testFetchPcfDetails() {
        def api = new FetchPcfDetails()
        def root = api.root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens['sso'].id
        root.head.txnToken = null
        root.body.orderId = orderId.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == NATIVE_PROMO_HYBRID.id })
    @AUser
    @Test(enabled = true)
    void testFetchPromoCodeDetail() {
        def api = new FetchPromoCodeDetail()
        def root = api.root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens['sso'].id
        root.head.txnToken = null
        root.body.txnType = 'BALANCE'
        root.body.promoCode = WALLET_PROMO.toString()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter2).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('SUCCESS'),
                'resultCode', equalTo('01'),
                'resultMsg', equalTo('Valid payment mode'))
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly_Retry.getId()})
    @AUser
    @Test
    void testValidateVpa() {
        def api = new SSOTokenVPAValidateV1Test()
        def root = api.root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens['sso'].id
        root.head.txnToken = null
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

//    @Merchant(edit = true, value = { it.payModes.contains('ppi') })
//    @AUser(edit = true)
//    @Test(enabled = false)
//TODO getting some exception
    void testProcessTransaction() {
        def api = new ProcessTransactionAPI()
        def root = api.root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens['sso'].id
        root.head.txnToken = null
        root.body.orderId = orderId.get()
        root.body.paymentMode = 'BALANCE'
        root.body << [
                txnAmount: [
                        value   : '1',
                        currency: 'INR'
                ]
        ]
        user().wallets['main'].balance = 2
        api.req(orderId.get()).body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.WalletOnly.getId()})
    @AUser
    @Test
    void testFetchPaymentInstruments() {
        def api = new FetchPaymentInstruments()
        def root = api.root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens['sso'].id
        root.head.txnToken = null
        root.body.orderId = orderId.get()
        api.req().body(root).filter(filter2).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                'resultCodeId', equalTo('0000'),
                'resultCode', equalTo('SUCCESS'),
                'resultMsg', equalTo('Success'))
    }

    @Merchant(edit = true, value = { it.payModes.contains('ppi') })
    @AUser
    @Test
    void testFetchQRPaymentDetails() {
        def api = new FetchQRPaymentDetailsAPITest()
        def root = api.root()
        root.head.tokenType = 'SSO'
        root.head.token = user().tokens['sso'].id
        root.head.txnToken = null
        root.body.orderId = orderId.get()
        api.req().body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
    }
}
