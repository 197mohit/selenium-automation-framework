package scripts.api.theia

import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
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
import scripts.api.theia.applyPromo.SSOTokenApplyPromoV1Test
import scripts.api.theia.fetchPayOptions.TxnTokenFetchPayOptionsV1Test
import scripts.api.theia.fetchBalance.TxnTokenFetchBalanceInfoV1Test
import scripts.api.theia.fetchBinDetail.TxnTokenFetchBinDetailV1Test
import scripts.api.theia.fetchEmiDetail.TxnTokenFetchEmiDetailV1Test
import scripts.api.theia.fetchMerchantInfo.TxnTokenFetchMerchantInfoV1Test
import scripts.api.theia.fetchNbPaymentChannels.TxnTokenFetchNBPaymentChannelsV1Test
import scripts.api.theia.validateAndFetchMerchantInfo.JWTTokenValidateAndFetchMerchantInfoV1Test
import scripts.api.theia.vpaValidate.TxnTokenVPAValidateV1Test
import scripts.or.FetchCardDetailsAPI

import java.lang.reflect.Method

import static com.paytm.appconstants.Constants.MerchantType.AMEX_PCF
import static com.paytm.appconstants.Constants.MerchantType.NATIVE_PROMO_HYBRID
import static com.paytm.appconstants.Constants.promoCode.WALLET_PROMO
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

@Owner("Deepak")
class CompatibilityTest extends TestSetUp {

    def filter1


    def filter2
    def filter3

    def txnToken = new ThreadLocal<String>()
    def orderId = new ThreadLocal<String>()

    @Factory(dataProvider = 'dataMethod')
    CompatibilityTest(filter1, filter2, filter3) {
        this.filter1 = filter1
        this.filter2 = filter2
        this.filter3 = filter3
    }

    @DataProvider(name = "dataMethod")
    static Object[][] dataMethod() {
        def primaryFilter = [filter: { requestSpec, responseSpec, ctx -> ctx.next(requestSpec, responseSpec) }] as Filter
        def secondaryFilter = [filter: { requestSpec, responseSpec, ctx ->
            requestSpec.basePath(requestSpec.getBasePath().replace('theia', 'theia1'))
            ctx.next(requestSpec, responseSpec)
        }] as Filter
        return [
                [0, 0, 1],
                [0, 1, 0],
                [1, 0, 0],
        ].collect {
            it.collect { it ? secondaryFilter : primaryFilter }
        }
    }

    @BeforeMethod
    void testInitTxn(Method method, ITestResult testResult) {
        try {
        def api = new InitiateTransaction()
        def root = api.root()
        root.body.paytmSsoToken = user().tokens['sso'].id
        root.body.txnAmount.value = 1
        root.body.promoCode = WALLET_PROMO.toString()
        root.head.signature = getChecksum(m().key, toJson(root.body))
        orderId.set(root.body.orderId)
        txnToken.set(api.req(orderId.get()).body(root).filter(filter1).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken'))
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @BeforeMethod(dependsOnMethods = 'testInitTxn')
    void testFetchPayOptions(Method method, ITestResult testResult) {
        try {
            def api = new TxnTokenFetchPayOptionsV1Test()
            def root = api.root()
            root.head.token = txnToken.get()
            given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                    .body(root).filter(filter2).post().then().spec(results.success as ResponseSpecification)
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Merchant(edit = true, value = { it.payModes.contains('ppi') })
    @AUser
    @Test
    void testFetchBalanceInfo() {
        def api = new TxnTokenFetchBalanceInfoV1Test()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        given(api.reqBldr().removeQueryParam('mid').removeQueryParam('orderId').addQueryParams([mid: m().id, orderId: orderId.get()]).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.payModes.contains('dc') })
    @AUser
    @Test
    void testFetchBinDetail() {
        def api = new TxnTokenFetchBinDetailV1Test()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        root.body.bin = cards.find { it.type == 'debit' && !it.prepaid }.no[0..5]
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void testFetchNBPaymentChannels() {
        def api = new TxnTokenFetchNBPaymentChannelsV1Test()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

/*    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test(enabled = false)
//TODO no support for TXN_TOKEN
    void testApplyPromo() {
        def api = new SSOTokenApplyPromoV1Test()
        def root = api.root()
        root.body.paymentOptions << [transactionAmount: root.body.totalTransactionAmount, payMethod: 'WALLET']
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        api.req().body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test(enabled = false)
//TODO no support for TXN_TOKEN
    void testFetchAllPaymentOffers() {
        def api = new FetchAllPaymentOffers()
        def root = api.root()
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        api.req().body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test(enabled = false)
//TODO no support for TXN_TOKEN
    void testFetchCardIndexNumber() {
        def api = new FetchCardIndexNumber()
        def root = api.root()
        root.head.tokenType = 'CHECKSUM'
        root.head.token = getChecksum(m().key, toJson(root.body))
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

 */

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testFetchCardDetailsAPI() {
        def api = new FetchCardDetailsAPI()
        def root = api.root()
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        root.body.cardNumber = cards.find { it.scheme == 'visa' }.no
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test
    void testSentOtp() {
        def api = new SendOtp()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('SUCCESS'),
                'resultCode', equalTo('01'),
                'resultMsg', equalTo('Otp sent to phone'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testValidateOtp() {
        pre:
        {
            def api = new SendOtp()
            def root = api.root()
            root.head.txnToken = txnToken.get()
            given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                    .body(root).filter(filter3).post().then()
                    .root('body.resultInfo')
                    .body('resultStatus', equalTo('SUCCESS'),
                    'resultCode', equalTo('01'),
                    'resultMsg', equalTo('Otp sent to phone'))
        }
        def api = new ValidateOtp()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //root.body.otp = AuthUtil.getOtp(user().mobile)
        root.body.otp = '123456'
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then()
                .body('body.authenticated', equalTo(true))
                .root('body.resultInfo')
                .body('resultStatus', equalTo('SUCCESS'),
                'resultCode', equalTo('01'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testFetchEmiPaymentChannels() {
        def api = new FetchEmiPaymentChannels()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMISubvention.getId() })
    @AUser
    @Test
    void testFetchEmiDetail() {
        def api = new TxnTokenFetchEmiDetailV1Test()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testFetchMerchantUserInfo() {
        def api = new FetchMerchantUserInfo()
        def root = api.root()
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        root.body.orderId = orderId.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testFetchVpaDetails() {
        def api = new FetchVpaDetails()
        def root = api.root()
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testFetchMerchantInfo() {
        def api = new TxnTokenFetchMerchantInfoV1Test()
        def root = api.root()
        root.head.ssoToken = user().tokens['sso'].id
        root.head.txnToken = txnToken.get()
        root.body.orderId = orderId.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == AMEX_PCF.id })
    @AUser
    @Test
    void testFetchPcfDetails() {
        def api = new FetchPcfDetails()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        root.body.orderId = orderId.get()
        root.body.payMethods = [[payMethod: 'NET_BANKING', instId: m().acquirings.find {
            it.payMode == 'nb'
        }.bank.toUpperCase()]]
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == NATIVE_PROMO_HYBRID.id })
    @AUser
    @Test
    void testFetchPromoCodeDetail() {
        def api = new FetchPromoCodeDetail()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        root.body.txnType = 'BALANCE'
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('SUCCESS'),
                'resultCode', equalTo('01'),
                'resultMsg', equalTo('Valid payment mode'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testUpdateTransactionDetail() {
        def api = new UpdateTransactionDetail()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        root.head.signature = getChecksum(m().key, toJson(root.body))
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

//    @Merchant(edit = true)
//    @AUser
//    @Test(enabled = false)
//TODO getting some exception
    void testShowPaymentPage() {
        def api = new ShowPaymentPage()
        def root = api.root()
        root.txnToken = txnToken.get()
        root.orderId = orderId.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void testSubmitKYC() {
        def api = new SubmitKYC()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('0001'),
                'resultMsg', equalTo('We could not validate your ID. Please try again with a different document ID'))
    }

    @Merchant({it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId()})
    @AUser
    @Test
    void testValidateVpa() {
        def api = new TxnTokenVPAValidateV1Test()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        given(api.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build())
                .body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

//    @Merchants([@Merchant(edit = true), @Merchant(edit = true)])
//    @AUser
//    @Test(enabled = false)
//TODO need to know how to link this api to particular order as neither txnToken nor orderId is passed in this api
    void testValidateAndFetchMerchantInfo() {
        def api = new JWTTokenValidateAndFetchMerchantInfoV1Test()
        def root = api.root()
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        root.body.mids = [m(0).id, m(1).id]
        api.req().body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

//    @Merchant(edit = true, value = { !it.emis.empty })
//    @AUser
//    @Test(enabled = false)
//TODO txn token not allowed
    void testCheckEmiEligibility() {
        def api = new CheckEmiEligibility()
        def root = api.root()
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        api.req().body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

//    @Merchant(edit = true)
//    @AUser
//    @Test(enabled = false)
//TODO accepts only JWT token
    void testFetchPaymentPromotionAttributes() {
        def api = new FetchPaymentPromotionAttributes()
        def root = api.root()
        root.head.tokenType = 'TXN_TOKEN'
        root.head.token = txnToken.get()
        api.req().body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @AUser(edit = true)
    @Test
    void testProcessTransaction() {
        def api = new ProcessTransactionAPI()
        def root = api.root()
        root.head.txnToken = txnToken.get()
        root.body.orderId = orderId.get()
        root.body.paymentMode = 'BALANCE'
        user().wallets['main'].balance = 2
        api.req(orderId.get()).body(root).filter(filter3).post().then().spec(results.success as ResponseSpecification)
    }
}
