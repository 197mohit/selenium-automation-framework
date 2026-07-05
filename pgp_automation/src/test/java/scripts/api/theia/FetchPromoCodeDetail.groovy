package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MerchantType.*
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PROMO_DETAIL
import static com.paytm.appconstants.Constants.promoCode.WALLET_PROMO
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchPromoCodeDetail extends TestSetUp {

    def txnToken = new ThreadLocal<String>()
    def orderId = new ThreadLocal<String>()

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PROMO_DETAIL)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: orderId.get() ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        txnToken: txnToken.get(),
                ],
                body: [
                        txnType       : 'BALANCE',
                        mid           : m()?.id,//M for offline flow
                        bankCode      : null,//O
                        promoCode     : null,//O
                        cardNumber    : null,//O
                        isEnhancedFlow: null,//O
                        userDetailsBiz: null,//O
                ]
        ]
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
        txnToken.set(api.req(orderId.get()).body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken') as String)
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Merchant(edit = true, value = { it.id == NATIVE_PROMO_HYBRID.getId() })
    @AUser
    @Test
    void testSuccess() {
        def root = root()
        req().body(root).post().then()
                .body('head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body('requestId', nullValue(),
                'responseTimestamp', not(isEmptyOrNullString()),
                'version', equalTo('v1'))
                .root('body')
                .body('extraParamsMap', nullValue(),
                'resultInfo', isA(Object.class),
                'promoCodeDetail', isA(Object.class),
                'checkPromoValidityURL', nullValue(),
                'paymentModes', nullValue(),
                'nbBanks', nullValue())
                .root('body.resultInfo')
                .body('resultStatus', equalTo('SUCCESS'),
                'resultCode', equalTo('01'),
                'resultMsg', equalTo('Valid payment mode'))
                .root('body.promoCodeDetail')
                .body('promocodeId', isA(Integer.class),
                'promoCode', equalTo(WALLET_PROMO.toString()),
                'paymentModes', isA(List.class),
                'cardBins', isA(Object.class),
                'nbBanks', isA(List.class),
                'merchants', isA(List.class),
                'startDate', not(isEmptyOrNullString()),
                'endDate', not(isEmptyOrNullString()),
                'promocodeType', isA(Long.class),
                'promocodeTypeName', isIn('CASHBACK', 'DISCOUNT'),
                'promoMsg', not(isEmptyOrNullString()),
                'promoErrorMsg', not(isEmptyOrNullString()),
                'promoCardType', isA(List.class),
                '', hasKey('savedCardDate'),
                'txnLimitMap', isA(Object.class),
                'maxCount', isA(Integer.class),
                'maxAmount', isA(Float.class),
                'minAmount', isA(Float.class),
                'promoOnSavedCard', isA(Boolean.class),
                'validatePromoWithWallet', isA(Boolean.class),
                'cardLimit', isA(Boolean.class),
                'custLimit', isA(Boolean.class))

    }
}
