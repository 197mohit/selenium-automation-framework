package scripts.api.theia

import com.paytm.ServerConfigProvider
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.LogsValidationHelper
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.Group
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.QRCode
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.config.RestAssuredConfig
import io.restassured.config.SSLConfig
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.testng.annotations.DataProvider
import org.testng.annotations.Optional
import org.testng.annotations.Parameters
import org.testng.annotations.Test
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV1Test
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV2Test


import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS
import static com.paytm.appconstants.Constants.Owner.PULKIT
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchQRPaymentDetailsAPITest extends TestSetUp {
    private static final Constants.MerchantType FTECH_QR = Constants.MerchantType.PGOnly
    private static final Constants.MerchantType MLV_ENABLED = Constants.MerchantType.MLV
    final def reqBldr = {
        new RequestSpecBuilder()
                .addFilters([setQueryParamOrderIdFilter])
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_QR_PAYMENT_DETAILS)
                .addQueryParams([appVersion: '8.3.2', client: 'dd', orderId: '?'])
    }

    final RequestSpecification req() {
        given(reqBldr().build()) }

    final Map<String, Object> root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        qrCodeId       : m().with {
                            it ? it.qrCodes?.add(QRCode.MerchantUPI())?.id.tap { assert it } : null
                        },
                        mlvSupported   : null,//O
                        generateOrderId: false,
                        orderId        : new Random().nextLong().abs() as String,
                ]
        ]
    }

    def setQueryParamOrderIdFilter = [filter: { req, res, ctx ->
        if (req.getQueryParams()['orderId'] == '?') {
            req.removeQueryParam('orderId').queryParam('orderId', new JsonSlurper().parseText(req.getBody())?.body?.orderId ?: new Random().nextLong().abs() as String)
        }
        ctx.next(req, res)
    }] as Filter

    Matcher paymentDetailsPresent = new BaseMatcher() {
        @Override
        boolean matches(Object o) {
            o?.body?.paymentOptions
        }

        @Override
        void describeTo(Description description) {
            description.appendText('payment details is present')
        }
    }

    Matcher qrDetailsPresent = new BaseMatcher() {
        @Override
        boolean matches(Object o) {
            o?.body?.qrInfo?.response
        }

        @Override
        void describeTo(Description description) {
            description.appendText('qr details is present')
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

    @DataProvider(name = "mandatoryFormParams")
    static Object[][] mandatoryFormParams() {
        [['head'], ['tokenType', 'token']].combinations() +
                [['body'], ['qrCodeId']].combinations()
    }

    @DataProvider(name = "optionalFormParams")
    static Object[][] optionalFormParams() {
        [['head'], ['version', 'requestId', 'requestTimestamp', 'channelId']].combinations()
    }

    @DataProvider(name = "mandatoryQueryParams")
    static Object[][] mandatoryQueryParams() {
        [['appVersion'], ['client']]
    }

    @DataProvider(name = "channelIdValues")
    static Object[][] channelIdValues() {
        [['APP'], ['WAP'], ['WEB'], ['SYSTEM']]
    }

    @DataProvider(name = "versionValues")
    static Object[][] versionValues() {
        [['v1']]
    }

    @DataProvider(name = "tokenValues")
    static Object[][] tokenValues() {
        [['paytm'], ['wallet']]
    }

    @DataProvider(name = "clientValues")
    static Object[][] clientValues() {
        [['android'], ['ios']]
    }

    @Issue('PGP-20566')
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test(dataProvider = 'mandatoryFormParams', description = 'test form param is mandatory', groups = Group.Status.BUG)
    void testFormParamIsMandatory(String paramBasePath, String param) {
        def root = root()
        paramBasePath.split('\\.').inject(root) { parent, parameter -> parent[parameter] }[param] = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test(dataProvider = 'optionalFormParams', description = 'test form param is optional')
    void testFormParamIsOptional(String paramBasePath, String param) {
        def root = root()
        paramBasePath.split('\\.').inject(root) { parent, parameter -> parent[parameter] }[param] = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(value = {it.id == Constants.MerchantType.WALLETOnly_PCF.getId() }, edit = true)
    @AUser
    @Test(dataProvider = 'mandatoryQueryParams', description = 'test query param is mandatory')
    void testQueryParamIsMandatory(String queryParam) {
        def root = root()
        given(reqBldr().removeQueryParam(queryParam).build()).body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                        'resultCode', equalTo('1007'),
                        'resultMsg', equalTo('AppVersion and client is mandatory in query parameter'))
    }

    @Merchant(value = { it.id == Constants.MerchantType.WALLETOnly_PCF.getId() }, edit = true)
    @AUser
    @Test(dataProvider = 'channelIdValues', description = 'test channel id value supported')
    void testChannelIdValueSupported(String channelId) {
        def root = root()
        root.head.channelId = channelId
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(value = { it.payModes.containsAll(['ppi', 'nb', 'cc', 'dc', 'emi', 'upi', 'ppbl']) && it.id == FTECH_QR.getId() }, edit = true)
    @AUser(edit = true)
    @Test(description = 'test fetchQR priority')
    void testFetchQR() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.paymentOptions.merchantPayOption.paymentModes')
                .body('paymentMode', hasItems('BALANCE','COD', 'CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'EMI', 'UPI', 'PPBL'),
                        'priority', hasItems('1', '6','7','8','4','5','3','2'))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId() })
    @AUser
    @Test(dataProvider = 'versionValues', description = 'test version value supported')
    void testVersionValueSupported(String version) {
        def root = root()
        root.head.version = version
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @AUser
    @Parameters('invalidQRCode')
    @Test(description = 'test when invalid q r code id is supplied')
    void testWhenInvalidQRCodeIdIsSupplied(@Optional('abdagdj3y1y381') String invalidQRCode) {
        def root = root()
        root.body.qrCodeId = invalidQRCode
        req().body(root).post().then()
                .spec(results.qrCodeNotRecognised as ResponseSpecification)
    }

    @Merchant(value = {it.id == Constants.MerchantType.COD.getId()}, edit = true)
    @AUser
    @Test(dataProvider = 'tokenValues', description = 'test api is accessible through token')
    void testApiIsAccessibleThroughToken(String tokenType) {
        def root = root()
        root.head.token = user().tokens[tokenType].id
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Merchant(value = { it.id == Constants.MerchantType.AMEX_PCF.getId() }, edit = true)
    @AUser
    @Test(description = 'test api is not accessible through oauth txn token')
    void testApiIsNotAccessibleThroughOauthTxnToken() {
        def root = root()
        root.head.token = user().tokens['txn'].id
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                        'resultCode', equalTo('2004'),
                        'resultMsg', equalTo('SSO Token is invalid'))
    }

    @Merchant(value = { it.id == Constants.MerchantType.NATIVE_PROMO_HYBRID.getId() }, edit = true)
    @Parameters('invalidToken')
    @Test(description = 'test api is not accessible through invalid token')
    void testApiIsNotAccessibleThroughInvalidToken(@Optional('abdagdj3y1y381') String invalidToken) {
        def root = root()
        root.head.token = invalidToken
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                        'resultCode', equalTo('2004'),
                        'resultMsg', equalTo('SSO Token is invalid'))
    }

    @Merchant(value = { it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId() }, edit = true)
    @AUser
    @Test(description = 'test payment details fetched for p2m qr code')
    void testPaymentDetailsFetchedForP2MQRCode() {
        def root = root()
        root.body.qrCodeId = m().qrCodes.add(QRCode.MerchantUPI())?.id.tap { assert it }
        req().body(root).post().then()
                .body('', paymentDetailsPresent)
    }

    @AUser(edit = true)
    @Test(description = 'test payment details not fetched for p2p qr code')
    void testPaymentDetailsNotFetchedForP2PQRCode() {
        def root = root()
        root.body.qrCodeId = user().qrCodes.add(QRCode.P2P())?.id.tap { assert it }
        req().body(root).post().then()
                .body('', not(paymentDetailsPresent))
    }

    @AUser(edit = true)
    @Test(description = 'test payment details not fetched for debit qr code')
    void testPaymentDetailsNotFetchedForDebitQRCode() {
        def root = root()
        root.body.qrCodeId = user().qrCodes.add(QRCode.Debit())?.id.tap { assert it }
        req().body(root).post().then()
                .body('', not(paymentDetailsPresent))
    }

    @AUser(edit = true)
    @Test(description = 'test qr details fetched for p2p qr code')
    void testQRDetailsFetchedForP2PQRCode() {
        def root = root()
        root.body.qrCodeId = user().qrCodes.add(QRCode.P2P())?.id.tap { assert it }
        req().body(root).post().then()
                .body('', qrDetailsPresent)
    }

    @Merchant(value = {it.id == Constants.MerchantType.MDR_PCF_MERCH.getId()}, edit = true)
    @AUser
    @Test(description = 'test qr details fetched for p2m qr code')
    void testQRDetailsFetchedForP2MQRCode() {
        def root = root()
        root.body.qrCodeId = m().qrCodes.add(QRCode.MerchantUPI())?.id.tap { assert it }
        req().body(root).post().then()
                .body('', qrDetailsPresent)
    }

    @Merchant(value = {it.id == Constants.MerchantType.EMI.getId()}, edit = true)
    @AUser
    @Test(dataProvider = 'versionValues', description = 'test api is accessible from allowed clients')
    void testApiIsAccessibleFromAllowedClients(String client) {
        def root = root()
        req().queryParam('client', client).body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

//    @Merchant(value = { it.payModes.contains('ppi') }, edit = true)
//    @AUser(edit = true)
//    @Test(enabled = false, description = 'test saved cards fetched given user has saved cards')
//TODO test for different types of merchant like ['hybrid', 'pgonly', 'pcf] etc
    void testSavedCardsFetchedGivenUserHasSavedCards() {
        def root = root()
        user().savedCards.clear()
        user().savedCards.addAll(cards.findAll())
        req().body(root).post().then()
                .body('body.paymentOptions.merchantPayOption.savedInstruments', hasSize(user().savedCards.size()))
    }

//    @Merchant(value = { it.payModes.contains('ppi') }, edit = true)
//    @AUser
//    @Test(enabled = false, description = 'test when expired qr code is supplied')
//TODO need way to get expired qrcode
    void testWhenExpiredQRCodeIsSupplied() {
        def root = root()
        root.body.qrCodeId = m().qrCodes.last().id
        req().body(root).post().thenReturn()
    }

    @Merchant(value = { it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId() }, edit = true)
    @AUser
    @Test(description = 'test when inactive merchant qr code is supplied')
    void testWhenInactiveMerchantQRCodeIsSupplied() {
        def root = root()
        root.body.qrCodeId = m().qrCodes.add(QRCode.MerchantUPI()).tap { assert it }.tap { assert it.setEnabled(false) }.id
        req().body(root).post().then().
                spec(results.qrCodeNotRecognised as ResponseSpecification)
    }

    @AUser(edit = true)
    @Test(description = 'test when inactive user qr code is supplied')
    void testWhenInactiveUserQRCodeIsSupplied() {
        def root = root()
        root.body.qrCodeId = user().qrCodes.add(QRCode.P2P()).tap { assert it }.tap { assert it.setEnabled(false) }.id
        req().body(root).post().then()
                .spec(results.qrCodeNotRecognised as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_ALLPAYMODE_MERCH.getId() })
    @AUser
    @Test
    void 'test when non-mlv-supported qrCodeId is passed'() {
        def root = root()
        root.body.qrCodeId = m().qrCodes.add(QRCode.MerchantUPI()).tap { assert it }.id
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .root('body.qrInfo.response')
                .body('', not(hasKey('kybId')),
                        '', not(hasKey('shopId')))
    }

    @Owner(Constants.Owner.JAI)
    @Merchant(value = { it.preferences.mlv.enabled == true && it.payModes.contains('ppi') && it.id == MLV_ENABLED.getId() }, edit = true)
    @AUser
    @Test(description = 'test query params are present in FETCH_CHANNEL_DETAILS Request in theia_facade logs')
    void 'PGP-28418 test Queryparams in logs when mlv-supported qrCodeId is passed'() {
        def root = root()
        root.body.mlvSupported = 'true'
        root.body.qrCodeId = m().qrCodes.add(QRCode.UPI()).tap { assert it }.id
        given(reqBldr()
                .addQueryParam("deviceIdentifier","samsung-SM-A505F-3fc3ca68dc0d3614")
                .addQueryParam("deviceManufacturer","samsung")
                .addQueryParam("locale","en-IN")
                .addQueryParam("networkType", "WIFI")
                .addQueryParam("deviceName","SM-A505F")
                .build()).body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                        'resultCode', equalTo('0000'),
                        'resultMsg', equalTo('Success'))
        String grepcmd = "grep \"" + root.body.orderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + m().id +"\" | grep \"FETCH_CHANNEL_DETAILS\" | grep \"REQUEST\"";
        String theiafacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd)
        Assertions.assertThat(theiafacadelogs)
                .contains("deviceIdentifier=[samsung-SM-A505F-3fc3ca68dc0d3614]")
                .contains("deviceManufacturer=[samsung]")
                .contains("locale=[en-IN]")
                .contains("networkType=[WIFI]")
                .contains("deviceName=[SM-A505F]")

    }

    @Merchant(value = { it.preferences.mlv.enabled == true && it.payModes.contains('ppi') && it.id == MLV_ENABLED.getId() }, edit = true)
    @AUser
    @Test
    void 'test when mlv-supported qrCodeId is passed'() {
        def root = root()
        root.body.qrCodeId = m().qrCodes.add(QRCode.UPI()).tap { assert it }.id
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .root('body.qrInfo.response')
                .body('', hasKey('kybId'),
                        '', hasKey('shopId'))
    }

    @Merchant(value = { it.id == Constants.MerchantType.PGOnly.getId()}, edit = true)
    @AUser
    @Test
    void 'test when mlvSupported == false given merchant supports mlv'() {
        def root = root()
        root.body.mlvSupported = [false, "false"].get(new Random().nextInt(2))
        root.body.qrCodeId = m().qrCodes.add(QRCode.UPI()).tap { assert it }.id
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('channelPaymentDetails')))
    }

    @Merchant(value = { it.preferences.mlv.enabled == true && it.payModes.contains('ppi') && it.id == MLV_ENABLED.getId()}, edit = true)
    @AUser
    @Test
    void 'test when mlvSupported key is not passed given merchant supports mlv'() {
        def root = root()
        root.body.remove('mlvSupported')
        root.body.qrCodeId = m().qrCodes.add(QRCode.UPI()).tap { assert it }.id
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('channelPaymentDetails')))
    }


    //Mocked channel API ( PGP-22076)

    @Merchant(value = { it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId() }, edit = true)
    @AUser
    @Test
    void 'test when mlvSupported == true given merchant does not support mlv'() {
        def root = root()
        root.body.mlvSupported = [true, "true"].get(new Random().nextInt(2))
        root.body.qrCodeId = m().qrCodes.add(QRCode.MerchantUPI()).tap { assert it }.id
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('channelPaymentDetails')))
    }

    @Merchant(value = { it.preferences.mlv.enabled == true && it.payModes.contains('ppi') && it.id == MLV_ENABLED.getId()}, edit = true)
    @AUser
    @Test
    void 'test when mlvSupported == true given merchant supports mlv'() {
        def root = root()
        root.body.mlvSupported = [true, "true"].get(new Random().nextInt(2))
        root.body.qrCodeId = m().qrCodes.add(QRCode.UPI()).tap { assert it }.id
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .body('body', hasKey('channelPaymentDetails'))
    }

    @Merchant(value = { it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId() }, edit = true)
    @AUser(edit = true)
    @Test
    public void 'verify order is created through fetch QR float in FPO and PTC for successful txn'() {
        String orderId = UUID.randomUUID() as String
        FETCH_QR:
        {
            def root = root()
            root.body.generateOrderId = 'false'
            root.body.orderId = orderId
            req().body(root).post().then().spec(results.success as ResponseSpecification)
                    .body('body.paymentOptions.orderId', equalTo(orderId))
        }
        FPO:
        {
            def api = new SSOTokenFetchPayOptionsV1Test()
            def root = api.root()
            root.body.generateOrderId = 'false'
            given(api.reqBldr().removeQueryParam('orderId')
                    .addQueryParam('orderId', orderId).build())
                    .body(root).post().then().spec(results.success as ResponseSpecification)
                    .body('body.orderId', equalTo(orderId))
        }
        PTC:
        {
            def api = new ProcessTransactionAPI()
            def root = api.root()
            root.head.remove("txnToken")
            root.head.tokenType = 'SSO'
            root.head.token = user().tokens['sso'].id
            root.body.paymentMode = 'BALANCE'
            root.body.put("custId", new Random().nextLong().abs() as String)
            root.body.orderId = orderId
            root.body.txnAmount = [value: '1', currency: 'INR']
            user().wallets['main'].balance = 1
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
    }


    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_WALLET_MERCH.getId() })
    @AUser(edit = true)
    @Test
    public void 'verify generateorder flag is true for fetch QR the response order in FPO and PTC will float for successful txn'() {
        String orderId = null
        FETCH_QR:
        {
            def root = root()
            root.body.generateOrderId = 'true'
            root.body.remove("orderId")
            Response response = req().body(root).post()
            orderId = response.jsonPath().get('body.paymentOptions.orderId')

        }
        FPO:
        {
            def api = new SSOTokenFetchPayOptionsV1Test()
            def root = api.root()
            root.body.generateOrderId = 'false'
            given(api.reqBldr().removeQueryParam('orderId')
                    .addQueryParam('orderId', orderId).build())
                    .body(root).post().then().spec(results.success as ResponseSpecification)
                    .body('body.orderId', equalTo(orderId))
        }
        PTC:
        {
            def api = new ProcessTransactionAPI()
            def root = api.root()
            root.head.remove("txnToken")
            root.head.tokenType = 'SSO'
            root.head.token = user().tokens['sso'].id
            root.body.paymentMode = 'BALANCE'
            root.body.put("custId", new Random().nextLong().abs() as String)
            root.body.orderId = orderId
            root.body.txnAmount = [value: '1', currency: 'INR']
            user().wallets['main'].balance = 1
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
    }


    @Merchant(value = { it.id == Constants.MerchantType.PGOnly.getId() }, edit = true)
    @AUser(edit = true)
    @Test(description = "verify when FPO generateOrderFlag is true than Order is treated as seperate order w.r.t FetchQR payment order")
    public void 'verify when FPO generateOrderFlag is true than Order is treated as seperate order wrt FetchQR payment order'() {
        String orderId = new Random().nextLong().abs() as String
        FETCH_QR:
        {
            def root = root()
            root.body.generateOrderId = 'false'
            root.body.orderId = orderId
            req().body(root).post().then().spec(results.success as ResponseSpecification)
                    .body('body.paymentOptions.orderId', equalTo(orderId))
        }
        FPO:
        {
            def api = new SSOTokenFetchPayOptionsV1Test()
            def root = api.root()
            root.head.remove("txnToken")
            root.body.generateOrderId = 'true'
            orderId = given(api.reqBldr().removeQueryParam('orderId')
                    .addQueryParam('orderId', orderId).build())
                    .body(root).post().then().body('body.orderId', not(orderId)).extract().path('body.orderId')

        }
        PTC:
        {
            def api = new ProcessTransactionAPI()
            def root = api.root()
            root.head.remove("txnToken")
            root.head.tokenType = 'SSO'
            root.head.token = user().tokens['sso'].id
            root.body.paymentMode = 'BALANCE'
            root.body.put("custId", new Random().nextLong().abs() as String)
            root.body.orderId = orderId
            root.body.txnAmount = [value: '1', currency: 'INR']
            user().wallets['main'].balance = 1
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }


    }


    @Merchant(value = { it.id == Constants.MerchantType.EMI.getId() }, edit = true)
    @AUser(edit = true)
    @Test
    public void 'verify after fetchQRPayment FetchPayV2 is hit and Txn is Successful through PTC'() {
        String orderId = new Random().nextLong().abs() as String
        FETCH_QR:
        {
            def root = root()
            root.body.generateOrderId = 'false'
            root.body.orderId = orderId
            req().body(root).post().then().spec(results.success as ResponseSpecification)
                    .body('body.paymentOptions.orderId', equalTo(orderId))
        }
        FPO:
        {
            def api = new SSOTokenFetchPayOptionsV2Test()
            def root = api.root()
            root.body.generateOrderId = 'false'
            given(api.reqBldr().removeQueryParam('orderId')
                    .addQueryParam('orderId', orderId).build())
                    .body(root).post().then().spec(results.success as ResponseSpecification)
                    .body('body.orderId', equalTo(orderId))
        }
        PTC:
        {
            def api = new ProcessTransactionAPI()
            def root = api.root()
            root.head.remove("txnToken")
            root.head.tokenType = 'SSO'
            root.head.token = user().tokens['sso'].id
            root.body.paymentMode = 'BALANCE'
            root.body.put("custId", new Random().nextLong().abs() as String)
            root.body.orderId = orderId
            root.body.txnAmount = [value: '1', currency: 'INR']
            user().wallets['main'].balance = 1
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @Test
    void 'Validate that saved vpa should be fetched when device id is passed in query param'() {
        def user = new User("7073388117", "Paytm@123")
        def root = root()
        root.head.channelId = 'APP'
        root.body.mlvSupported = 'true'
        root.head.token = user.tokens['sso'].id
        given(reqBldr().addQueryParam("language", "en-IN").addQueryParam("deviceManufacturer", "Apple").
                addQueryParam("osVersion", "13.6").addQueryParam("version", "8.11.0").
                addQueryParam("deviceIdentifier", "Apple-iPhone-C00D255E-8125-49DB-BB35-F0E1C7E553CD").addQueryParam("locale", "en-IN").
                addQueryParam("deviceName", "iPhone%208%20(iOS%2013.6)").
                addQueryParam("networkType", "WiFi").
                addQueryParam("device-id", "C00D255E-8125-49DB-BB35-F0E1C7E553CD").build()).body(root).post().then().
                body("body.paymentOptions.merchantPayOption.userProfileSarvatra.status", equalTo("success")).
                body("body.paymentOptions.merchantPayOption.userProfileSarvatra.response.vpaDetails", Matchers.notNullValue());

    }

    @Owner(PULKIT)
    @Merchant(value = { it.limit == 1 }, edit = true)
    @AUser
    @Test
    void 'Verify that merchant limit list is returned in the response for merchantLimit is 1'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOptions.merchantLimitInfo.merchantPaymodesLimits', notNullValue())
    }

    @Owner(PULKIT)
    @Merchant(value = { it.limit == 2 }, edit = true)
    @AUser
    @Test
    void 'Verify that merchant limit list is not returned in the response for merchantLimit is 2'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOptions.merchantLimitInfo', not(hasKey("merchantPaymodesLimits")))
    }



}