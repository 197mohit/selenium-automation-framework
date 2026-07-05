package scripts.api.theia

import com.paytm.api.RedisAPI
import com.paytm.apphelpers.PG2LogsValidationHelper
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Description
import io.qameta.allure.Feature
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.isA
import static org.hamcrest.Matchers.isIn
import static org.hamcrest.Matchers.stringContainsInOrder

@Owner("Deepak")
@Link(name = 'doc', value = 'https://wiki.mypaytm.com/display/PGP/API+Contracts')
class FetchBinAppDetails extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath('/theia/bin/fetchBinAppDetails')
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis() as String,
                        requestId       : new Random().nextLong().abs() as String,
                        mid             : m().id,
                        tokenType       : 'SSO',
                        token           : user().tokens['sso'].id,
                ],
                body: [
                        bin        : cards.find().no[0..5],
                        orderId    : new Random().nextLong().abs() as String,
                        channelId  : 'WEB',
                        requestType: 'DEFAULT',
                ]
        ]
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void testSuccess() {
        def root = root()
        def card = cards.find()
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .rootPath('head')
                .body('responseTimestamp', isA(String.class),
                'requestId', equalTo(root.head.requestId),
                'mid', equalTo(root.head.mid),
                'version', equalTo(root.head.version))
                .root('body')
                .body('oneClickSupported', isA(Boolean.class),
                'oneClickMaxAmount', equalTo('2000'),
                'binDetail', isA(Object.class),
                'hasLowSuccessRate', isA(Object.class),
                'iconUrl', isA(String.class),
                'authModes', isA(List.class))
                .root('body.binDetail')
                .body('issuingBank', isA(String.class),
                'paymentMode', isA(String.class),
                'bin', equalTo(root.body.bin),
                'isIndian', equalTo(card.indian as String),
                'channelName', equalTo(card.scheme.toUpperCase()),
                'issuingBankCode', isA(String.class),
                'isActive', isIn('true', 'false'))
                .root('body.hasLowSuccessRate')
                .body('msg', isA(String.class),
                'status', isIn('true', 'false'))
    }


    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void testFailure() {
        def root = root()
        def card = cards.find()
        root.body.bin = card.no[0..5]
        root.head.token=null
        req().body(root).post().then()
                .rootPath('head')
                .body('responseTimestamp', isA(String.class),
                        'requestId', equalTo(root.head.requestId),
                        'version', equalTo(root.head.version))
                .root('body.resultInfo')
                .body('resultStatus', equalTo("TXN_FAILURE"),
                        'resultCode', equalTo("CMER-JSE101"),
                        'resultMsg', equalTo("InvalidToken"))
    }


    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Feature("PGP-55308")
    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Description("Automation Of PGP-55308")
    @AUser
    @Test(description = "Validate bin client is not called instead bin api call is going for fetching bin from bin-center",enabled = false)
    void verifyBinCenterApiCallIsHappening() {

        def root = root()
        def card = cards.find()
        root.body.bin = card.no[0..8]
        RedisAPI.deleteKey("BIN_DB_CACHE:"+card.no[0..8]);
        RedisAPI.deleteKey("BIN_DB_DIGITAL_ASSETS_CACHE:"+card.no[0..8]);
        req().body(root).post().then()
                .root('body.binDetail')
                .body(
                        'bin', equalTo(card.no[0..5]),
                )

        def orderId = root.body.orderId
        SoftAssertions softAssertions= new SoftAssertions();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId , "BIN_CENTER");
        System.out.println(theia_facade);
        softAssertions.assertThat(theia_facade).contains("https://qa-pg-int.paytm.com/bin-center/v1/bin/"+card.no[0..8]+"/query");
        softAssertions.assertAll();

    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Feature("PGP-55308")
    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Description("Automation Of PGP-55308")
    @AUser
    @Test(description = "Validate data is picked from cache and no hit is going on bin-center",enabled = false)
    void ValidateDataIsPickedFromCache() {

        def root = root()
        def card = cards.find()
        root.body.bin = card.no[0..8]
        RedisAPI.deleteKey("BIN_DB_CACHE:"+card.no[0..8]);
        RedisAPI.deleteKey("BIN_DB_DIGITAL_ASSETS_CACHE:"+card.no[0..8]);
        req().body(root).post().then()
                .root('body.binDetail')
                .body(
                        'bin', equalTo(card.no[0..5]),

                )
        def orderId = root.body.orderId
        System.out.println("First OrderId is: "+orderId)
        orderId=new Random().nextLong().abs() as String;
        root.body.orderId=orderId;
        req().body(root).post().then()
                .root('body.binDetail')
                .body(
                        'bin', equalTo(card.no[0..5]),
                )

        orderId = root.body.orderId
        System.out.println("Second orderId is: "+ orderId)
        SoftAssertions softAssertions= new SoftAssertions();
        String theia = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderId, " binInfoMap from cache :: ");
        System.out.println("Thiea Logs: "+theia);
        softAssertions.assertThat(theia).contains(card.no[0..8]);
        softAssertions.assertAll();

    }

}
