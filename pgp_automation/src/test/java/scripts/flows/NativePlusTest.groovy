package scripts.flows

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer
import com.paytm.RefundSucessNotifyPeon
import com.paytm.ServerConfigProvider
import com.paytm.api.CloseOrderAPI
import com.paytm.api.GetPaymentStatus
import com.paytm.api.GetTokenRangeAPI
import com.paytm.api.ModifyTokenAPI
import com.paytm.api.RedisAPI
import com.paytm.apphelpers.NativeHelpers
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.PGPBaseTest
import com.paytm.appconstants.Constants
import com.paytm.base.test.TestSetUp
import com.paytm.dto.CloseOrder.CloseOrderDTO
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO
import com.paytm.dto.GetPaymentStatusResponse.GetPaymentStatusResponseDTO
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO
import com.paytm.dto.OrderDTO
import com.paytm.dto.OrderFactory
import com.paytm.framework.conditions.SoftAssertion
import com.paytm.pages.CheckoutPage
import com.paytm.pages.ResponsePage
import com.paytm.utils.merchant.Peon
import com.paytm.utils.ff4j.FF4JFlags
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.Card
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import com.paytm.utils.merchant.util.PayMethodType
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Feature
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.testng.SkipException
import org.testng.annotations.Test
import scripts.api.theia.FetchCardIndexNumber
import java.text.DecimalFormat
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.Owner.ARSH
import static com.paytm.appconstants.Constants.Owner.GAGANDEEP
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.*
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*
import static com.paytm.appconstants.Constants.MappingService.TOKEN_INFO

@Owner('Deepak | Somesh')
class NativePlusTest extends TestSetUp implements FlipkartPromoTest, AuthorizationTokenTest, ProcessTxnEventLinkIdTest {

    private final static int INSTANT_DISCOUNT_PERCENTAGE = 5
    private final static CARD_NOT_ADDED = "unable to add card(s) successfully"
    private final static String PROMOS_NOT_ADDED = "unable to add promo(s) successfully"
    private final static String PROMO_NOT_APPLIED = "promo not applied successfully"
    private static final String TXN_TOKEN_NOT_GENERATED = 'unable to generate txn token'
    private final static String FAILURE_PROMO_CODE = 'failure'
    private final static String PG_DISCOUNT_PROMO_CODE = 'discount'
    private final static String PG_CASHBACK_PROMO_CODE = 'cashback'
    private final static String PAYTM_CASHBACK_PROMO_CODE = 'paytm_cashback'
    private final static String DEFAULT_TOKEN = '465010000'
    private final static String BANK_NULL_TOKEN = '800000003'
    private static final ThreadLocal<String> token = new ThreadLocal<>()
    private static final ThreadLocal<String> orderId = new ThreadLocal<>()
    private static final ThreadLocal<String> mid = new ThreadLocal<>()
    private static final ThreadLocal<String> custId = new ThreadLocal<>()
    private static final ThreadLocal<Map> paymentOffer = new ThreadLocal<>()

    private static class HasValidChecksum extends TypeSafeMatcher<Map> {

        private final String mKey

        HasValidChecksum(String mKey) {
            this.mKey = mKey
        }

        @Override
        protected boolean matchesSafely(Map txnInfo) {
            return PGPUtil.isChecksumValid(this.mKey, txnInfo.findAll { it.key != 'CHECKSUMHASH' } as TreeMap, txnInfo.CHECKSUMHASH as String)
        }

        @Override
        void describeTo(Description description) {
            description.appendText("is a valid checksum")
        }

        static Matcher<Map> hasValidChecksum(String mKey) {
            new HasValidChecksum(mKey)
        }
    }

    private String GetJSONStringBody(Response response) throws IOException {
        ObjectMapper object = new ObjectMapper()
        GetPaymentStatusResponseDTO getPaymentStatusResponseDTO = object.readValue(response.getBody().asString(), GetPaymentStatusResponseDTO.class)
        Gson gson = new Gson()
        return gson.toJson(getPaymentStatusResponseDTO.getBody())
    }

    private final RequestSpecBuilder applyPromoReqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, applyPromoSchemaFilter, copyApplyPromoDataFilter, new RequestLoggingFilter()])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(APPLY_PROMO_V1)
                .addQueryParam('mid', '?')
    }

    private final RequestSpecification applyPromoReq() {
        given(applyPromoReqBldr().build())
    }

    private final Map applyPromoRoot() {
        [
                head: [
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id
                ],
                body: [
                        mid                   : m()?.id,
                        promocode             : null,//O
                        paymentOptions        : [
                                [
                                        transactionAmount: '1',
                                        payMethod        : 'WALLET'
                                ]
                        ],
                        totalTransactionAmount: '1',
                        custId                : null,//O
                ]
        ]
    }

    final def initTxnReq = {
        given(
                new RequestSpecBuilder()
                        .addFilters([pasteApplyPromoDataToInitTxnFilter, setSignatureFilter, setQueryParamMidFilter, setQueryParamOrderIdFilter, copyInitTxnDataFilter, new RequestLoggingFilter()])
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(INIT_TXN)
                        .addQueryParam('mid', '?')
                        .addQueryParam('orderId', '?')
                        .build()
        )
    }

    protected def getTokenReq = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(TOKEN_INFO)
                        .build()
        )
    }

    final def initTxnRoot = {
        [
                head: [
                        version  : 'v1',
                        channelId: 'WEB',
                        signature: '?',
                ],
                body: [
                        mid                 : m()?.id ?: UUID.randomUUID().toString(),
                        orderId             : UUID.randomUUID().toString(),
                        requestType         : 'Payment',
                        websiteName         : 'retail',
                        txnAmount           : [
                                currency: 'INR',
                                value   : 1
                        ],
                        payableAmount       : [
                                currency: 'INR',
                                value   : 1
                        ],
                        userInfo            : [
                                custId: UUID.randomUUID().toString(),
                        ],
                        paytmSsoToken       : user()?.tokens?.find { it.name == 'sso' }?.id,
                        promoCode           : null,//O,
                        paymentOffersApplied: '?',
                        isNativeAddMoney    : "false"
                ]
        ]
    }

    final RequestSpecBuilder ptcReqBldr() {
        new RequestSpecBuilder()
                .addFilters([pasteInitTxnDataToPTCFilter, setQueryParamMidFilter, setQueryParamOrderIdFilter, new RequestLoggingFilter()])
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(PROCESS_TXN)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', '?')
    }

    final RequestSpecification ptcReq() {
        given(ptcReqBldr().build())
    }

    final def ptcRoot = {
        [
                head: [
                        channelId: 'WEB',
                        txnToken : '?',
                ],
                body: [
                        mid        : '?',
                        orderId    : '?',
                        paymentMode: null,//M
                        channelCode: null,//O
                        custId     : '?',
                        cardInfo   : null,//O
                        paymentFlow: 'NONE',//O
                        mpin       : null,//O
                        website    : "retail",
                        extendInfo : ["udf1"      : "vivek1",
                                      "udf2"      : "vivek2",
                                      "udf3"      : "vivek3",
                                      "mercUnqRef": "vivek4",
                                      "comments"  : "vivek5"
                        ]
                ]
        ]
    }

    final def ecomTokenInfo = {
        ["ecomToken"              : "4650100000000839",
         "expiryMonth"            : "12",
         "expiryYear"             : "2031",
         "authenticationValue"    : "qE5juRwDzAUFBAkEHuWW9PiBkWv=",
         "firstSixDigits"         : "133244",//o
         "lastFourDigits"         : "2341",//o
         "issuingBank"            : "HDFC",//o
         "networkTokenRequestorId": null,//O
        ]
    }

    private final Filter copyApplyPromoDataFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            Response res = ctx.next(requestSpec, responseSpec)
            paymentOffer.set(res.getBody().path('body.paymentOffer'))
            return res
        }
    }

    private final Filter pasteApplyPromoDataToInitTxnFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            if (root?.body?.paymentOffersApplied == '?') root?.body?.paymentOffersApplied = paymentOffer.get()
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter copyInitTxnDataFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            orderId.set(root?.body?.orderId)
            mid.set(root?.body?.mid)
            custId.set(root?.body?.userInfo?.custId)
            Response res = ctx.next(requestSpec, responseSpec)
            token.set(res.getBody().path('body.txnToken'))
            return res
        }
    }

    private final Filter pasteInitTxnDataToPTCFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            if (root?.head?.txnToken == '?') root?.head?.txnToken = token.get()
            if (root?.body?.orderId == '?') root?.body?.orderId = orderId.get()
            if (root?.body?.mid == '?') root?.body?.mid = mid.get()
            if (root?.body?.custId == '?') root?.body?.custId = custId.get()
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }
    private final Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter setQueryParamOrderIdFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['orderId'] == '?') {
                requestSpec.removeQueryParam('orderId').queryParam('orderId', new JsonSlurper().parseText(requestSpec.getBody())?.body?.orderId ?: new Random().nextLong().abs() as String)
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter setSignatureFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.signature == '?') it.signature = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter applyPromoSchemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/apply-promo-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private final static class ResultInfo {
        static ResponseSpecification PROMO_AMT_CANNOT_BE_GREATER_THAN_TXN_AMT = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1012'))
                .expectBody('resultMsg', equalTo("Promo amount cannot be more than transaction amount"))
                .build()
    }

    String[] getTokenRange(String token) {
        JsonPath jsonPath = new GetTokenRangeAPI().setContext('bin', token).execute().jsonPath()
//default token is 465010000
        String binMin = jsonPath.getString("binInfo.binMin")
        String binMax = jsonPath.getString("binInfo.binMax")
        return [binMin, binMax]
    }

    void setTokenAsNormal(String[] binRange) {
        JsonPath jsonPath = new ModifyTokenAPI(binRange[0], binRange[1]).execute().jsonPath()
        Assertions.assertThat(jsonPath.getString("responseCode")).isEqualTo("BE1400001").as("response code mismatch")
//success response code
    }

    void setTokenAsNormalVISACC(String[] binRange) {
        JsonPath jsonPath = new ModifyTokenAPI(binRange[0], binRange[1]).setContext('cardType', 'CC').setContext('binConfigAttributes.CATEGORY', 'PLATINUM').execute().jsonPath()
        Assertions.assertThat(jsonPath.getString("responseCode")).isEqualTo("BE1400001").as("response code mismatch")
//success response code
    }

    void setTokenAsPrepaid(String[] binRange) {
        JsonPath jsonPath = new ModifyTokenAPI(binRange[0], binRange[1]).setContext('binConfigAttributes.PREPAID_CARD', true).execute().jsonPath()
        Assertions.assertThat(jsonPath.getString("responseCode")).isEqualTo("BE1400001").as("response code mismatch")
//success response code
    }

    void setTokenAsCorporate(String[] binRange) {
        JsonPath jsonPath = new ModifyTokenAPI(binRange[0], binRange[1]).setContext('binConfigAttributes.CORPORATE_CARD', true).setContext('cardType', 'CC').setContext('binConfigAttributes.CATEGORY', 'PLATINUM').execute().jsonPath()
        Assertions.assertThat(jsonPath.getString("responseCode")).isEqualTo("BE1400001").as("response code mismatch")
//success response code
    }

    void setTokenAsCorporateDC(String[] binRange) {
        JsonPath jsonPath = new ModifyTokenAPI(binRange[0], binRange[1]).setContext('binConfigAttributes.CORPORATE_CARD', true).execute().jsonPath()
        Assertions.assertThat(jsonPath.getString("responseCode")).isEqualTo("BE1400001").as("response code mismatch")
//success response code
    }

    void setTokenAsBankNull(String[] binRange) {
        JsonPath jsonPath = new ModifyTokenAPI(binRange[0], binRange[1]).setContext('institutionId', null).execute().jsonPath()
        Assertions.assertThat(jsonPath.getString("responseCode")).isEqualTo("BE1400001").as("response code mismatch")
//success response code
    }

    void setTokenAsBankNullCorporateCard(String[] binRange) {
        JsonPath jsonPath = new ModifyTokenAPI(binRange[0], binRange[1]).setContext('institutionId', null).setContext('binConfigAttributes.CORPORATE_CARD', true).setContext('cardType', 'CC').setContext('binConfigAttributes.CATEGORY', 'PLATINUM').execute().jsonPath()
        Assertions.assertThat(jsonPath.getString("responseCode")).isEqualTo("BE1400001").as("response code mismatch")
//success response code
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('ppi') })
    @AUser(edit = true)
    @Test
    void "test when order payment is done by ppi given paytm_cashback promo is applied"() {
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.paymentOptions = [
                    [
                            transactionAmount: '1',
                            payMethod        : 'BALANCE'
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            user().wallets['main'].balance = 1
            def root = ptcRoot()
            root.body.paymentMode = 'BALANCE'
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }


    @Owner(Constants.Owner.ESHANI)
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('ppi') })
    @AUser(edit = true)
    @Test
    void "test if tncUrl present in apply promo response if ff4j flag ON"() {
        try {
            if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
            FF4JFlags.enable("theia.applyPromoSendResponseTncUrl");
            APPLY_PROMO:
            {
                def root = applyPromoRoot()
                root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
                root.body.paymentOptions = [
                        [
                                transactionAmount: '1',
                                payMethod        : 'BALANCE'
                        ]
                ]
                if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
                String tncUrl = applyPromoReq().body(root).post().path("body.paymentOffer.tncUrl");
                Assertions.assertThat(tncUrl)
                        .as("TncUrl not present in apply promo response despite flag being ON")
                        .isNotNull();
            }
            INIT_TXN:
            {
                def root = initTxnRoot()
                if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
            }
            PTC:
            {
                user().wallets['main'].balance = 1
                def root = ptcRoot()
                root.body.paymentMode = 'BALANCE'
                ptcReq().body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('S'))
            }
        }
        finally {
            FF4JFlags.disable("theia.applyPromoSendResponseTncUrl");
        }

    }


    @Owner(Constants.Owner.JAI)
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('ppi') && it.id == 'SIgKmW85235084384561'})
    @AUser(edit = true)
    @Test
    void "PGP 28967 test merchant name in theia facade logs after paytm_cashback promo txn"() {
        String orderId;
        String mid;
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.paymentOptions = [
                    [
                            transactionAmount: '1',
                            payMethod        : 'BALANCE'
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            user().wallets['main'].balance = 1
            def root = ptcRoot()
            root.body.paymentMode = 'BALANCE'
            Response response = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S')).extract().body()
            orderId = response.jsonPath().get("body.txnInfo.ORDERID")
            mid = response.jsonPath().get("body.txnInfo.MID")
        }
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + mid + "\" | grep \"PAYMENT_PROMO_SERVICE\" | grep \"REQUEST\"";
        String theiafacadelogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiafacadelogs).contains("\"merchantDisplayName\":\"AutomationMerchant00\"");
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser
    @Test
    void "test when order payment is done by cc given paytm_cashback promo is applied"() {
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.paymentOptions = [
                    [
                            transactionAmount: '1',
                            payMethod        : 'CREDIT_CARD',
                            cardNo           : card.no,
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }

   // @Override
    //@Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['amex', 'bajajfn']) } })
    //@AUser(edit = true)
    //@Test(enabled = false)
    //Somesh to update(Getting error msg - CheckoutPaymentPromo req builder validation failed - in logs)
    void "test when order payment is done by saved card CIN given paytm_cashback promo is applied"() {
        String cardIdxNo = null
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        (user() as AlipayUser).savedCards.clear()
        def card = cards.find { it.type == 'credit' && !(it.scheme in ['amex', 'bajajfn']) }
        if (!(user() as AlipayUser).savedCards.add(card)) throw new SkipException(CARD_NOT_ADDED)
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.paymentOptions = [
                    [
                            transactionAmount: '1',
                            payMethod        : 'CREDIT_CARD',
                            savedCardId      : card.idxNo,
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        FETCH_CARD_INDEX_NO:
        {
            FetchCardIndexNumber fetchCardIndexNumberApi = new FetchCardIndexNumber()
            def fetchCardIndexNumberRoot = fetchCardIndexNumberApi.root()
            fetchCardIndexNumberRoot.body.cardNumber = card.no
            cardIdxNo = given(fetchCardIndexNumberApi.reqBldr().removeQueryParam('orderId').addQueryParam('orderId', orderId.get()).build()).body(fetchCardIndexNumberRoot).post().path('body.cardIndexNumber')
            if (!cardIdxNo) throw new SkipException('unable to fetch card index no.')
        }
        PTC:
        {
            def root = ptcRoot()
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.cardInfo = "$cardIdxNo||$card.cvv|" as String
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser(edit = true)
    @Test
    void "test when order payment is done by saved card id given paytm_cashback promo is applied"() {
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        user().savedCards.clear()
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        if (!user().savedCards.add(card)) throw new SkipException(CARD_NOT_ADDED)
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.paymentOptions = [
                    [
                            transactionAmount: '1',
                            payMethod        : 'CREDIT_CARD',
                            savedCardId      : user().savedCards.find().id,
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.cardInfo = "${user().savedCards.find().id}||$card.cvv|" as String
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.preferences.addnpay().enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser(edit = true)
    @Test
    void "test when order payment is done by add n pay cc given paytm_cashback promo is applied"() {
        final double txnAmt = 2D
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.totalTransactionAmount = txnAmt
            root.body.paymentOptions = [
                    [
                            transactionAmount: txnAmt,
                            payMethod        : 'BALANCE',
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            user().wallets['main'].balance = txnAmt - 1D
            def root = ptcRoot()
            root.body.paymentFlow = 'NONE'
            root.body.paymentMode = 'BALANCE'
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.preferences.addnpay().enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser(edit = true)
    @Test
    void "test when order payment is done by add n pay cc given cashback promo is applied"() {
        final double txnAmt = 2D
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PG_CASHBACK_PROMO_CODE
            root.body.totalTransactionAmount = txnAmt
            root.body.paymentOptions = [
                    [
                            transactionAmount: txnAmt,
                            payMethod        : 'BALANCE',
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            user().wallets['main'].balance = txnAmt - 1D
            def root = ptcRoot()
            root.body.paymentFlow = 'NONE'
            root.body.paymentMode = 'BALANCE'
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.preferences.addnpay().enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser(edit = true)
    @Test
    void "test when order payment is done by add n pay cc given discount promo is applied"() {
        final double txnAmt = 3D
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PG_DISCOUNT_PROMO_CODE
            root.body.totalTransactionAmount = txnAmt
            root.body.paymentOptions = [
                    [
                            transactionAmount: txnAmt,
                            payMethod        : 'BALANCE',
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = new DecimalFormat('0.00').format(txnAmt * (1 - INSTANT_DISCOUNT_PERCENTAGE / 100))
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            user().wallets['main'].balance = 1D
            def root = ptcRoot()
            root.body.paymentFlow = 'NONE'
            root.body.paymentMode = 'BALANCE'
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('ppi') })
    @AUser(edit = true)
    @Test
    void "test when paytm_cashback promo is applied but discount promo details are passed while initiating order"() {
        final double txnAmt = 1
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.totalTransactionAmount = txnAmt
            root.body.paymentOptions = [
                    [
                            transactionAmount: txnAmt,
                            payMethod        : 'BALANCE'
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            paymentOffer.get().offerBreakup[0].promocodeApplied = FAILURE_PROMO_CODE
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            user().wallets['main'].balance = txnAmt
            def root = ptcRoot()
            root.body.paymentFlow = 'NONE'
            root.body.paymentMode = 'BALANCE'
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser
    @Test
    void "test when promo amt is greater than txn amt"() {
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        final double promoAmt = 2
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.paymentOptions = [
                    [
                            transactionAmount: promoAmt,
                            payMethod        : 'CREDIT_CARD',
                            cardNo           : card.no,
                    ]
            ]
            root.body.totalTransactionAmount = promoAmt
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = promoAmt - 1
            initTxnReq().body(root).post().then()
                    .spec(ResultInfo.PROMO_AMT_CANNOT_BE_GREATER_THAN_TXN_AMT)
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser
    @Test
    void "test when promo amt is lesser than txn amt"() {
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        final double promoAmt = 1D
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.paymentOptions = [
                    [
                            transactionAmount: promoAmt,
                            payMethod        : 'CREDIT_CARD',
                            cardNo           : card.no,
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = promoAmt + 1D
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
    }

    @Owner(GAGANDEEP)
    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for CC pay mode in case of risk pass'() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = 1
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
        }
    }

    @Owner(GAGANDEEP)
    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('dc') })
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for DC pay mode in case of risk pass'() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = 1
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            def card = cards.find { it.type == 'debit' && !it.prepaid }.tap { assert it }
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
        }
    }

    @Owner(GAGANDEEP)
    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('nb') })
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for NB pay mode in case of risk pass'() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = 1
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.body.paymentMode = 'NET_BANKING'
            root.body.channelCode = 'ICICI'
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
        }
    }

    @Owner(GAGANDEEP)
    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('dc') })
    @AUser
    @Test
    void "test eventLinkId is not coming in response in case of risk reject"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = 1.2
            root.body.payableAmount.value = 1.2
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            def card = cards.find { it.type == 'debit' }.tap { assert it }
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body("body.riskContent.eventLinkId", isEmptyOrNullString())
        }
    }

    @Owner(GAGANDEEP)
    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('nb') })
    @AUser
    @Test
    void "test eventLinkId is not coming in response in case of risk verify"() {

        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = 1.4
            root.body.payableAmount.value = 1.4
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.body.paymentMode = 'NET_BANKING'
            root.body.channelCode = 'ICICI'
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body("body.riskContent.eventLinkId", isEmptyOrNullString())
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify success transaction with ECOM token with all mandatory and optional fields in PTC"() {
        String txnAmt = '10.00' //setting a random transaction amount
        String ecom, firstSixDigits, lastFourDigits
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormal(tokenRange)//setting the token as normal

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
            firstSixDigits = root.body.ecomTokenInfo.firstSixDigits
            lastFourDigits = root.body.ecomTokenInfo.lastFourDigits
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                    .body('body.txnInfo.BANKTXNID', not(isEmptyOrNullString()))
                    .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                    .body('body.txnInfo.CURRENCY', equalTo('INR'))
                    .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                    .body('body.txnInfo.MID', equalTo(m().id))
                    .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                    .body('body.txnInfo.PAYMENTMODE', equalTo('DC'))
                    .body('body.txnInfo.RESPCODE', equalTo('01'))
                    .body('body.txnInfo.RESPMSG', equalTo('Txn Success'))
                    .body('body.txnInfo.STATUS', equalTo('TXN_SUCCESS'))
                    .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                    .body('body.txnInfo.TXNDATE', notNullValue())
                    .body('body.txnInfo.TXNID', notNullValue())
                    .body('body.callBackUrl', notNullValue())
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
        PEON:
        {
            assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
            Peon peon = peons.getAt(orderId.get())
            SoftAssertion softAssertion = new SoftAssertion()
            softAssertion.apply(
                    peon.gatewayName().equals("AXIS"),
                    peon.payMode().equals("DC"),
                    peon.txnDate().equals("").not(),
                    peon.txnDateTime().equals("").not(),
                    peon.custId().equals("").not(),
                    peon.status().equals("TXN_SUCCESS"),
                    peon.mId().equals(m().id),
                    peon.orderId().equals(orderId.get()),
                    peon.currency().equals("INR"),
                    peon.txnId().equals("").not(),
                    peon.txnAmt().equals(txnAmt),
                    peon.bankTxnId().equals("").not(),
                    peon.bankName().equals("HDFC Bank"),
                    peon.respMsg().equals("Txn Success"),
                    peon.respCode().equals("01"),
                    peon.mercUnqRef().equals(""),
                    peon.maskedEcomToken().contains(ecom + "******"),
                    peon.isChecksumValid()
            )
            softAssertion.eval()
        }
        MERCHANT_STATUS:
        {
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
            Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
            JsonPath jsonPath = response.jsonPath()
            String MerchantResBody = GetJSONStringBody(response)
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS")
            Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
            Assertions.assertThat(jsonPath.get("body.maskedCardNo")).isEqualTo(firstSixDigits + "******" + lastFourDigits).as("Masked card number is not coming")
            Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
            Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "******").as("Masked ecom token not coming");
            Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
            Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify success transaction with ECOM token for MDR+PCF merchant"() {
        String txnAmt = '10.00' //setting a random transaction amount
        String ecom, firstSixDigits, lastFourDigits
        double chargeAmountPCF
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        PGPBaseTest pgpBaseTest = new PGPBaseTest()
        chargeAmountPCF = pgpBaseTest.convenienceFeeCalculator(txnAmt.toDouble(), 1.2, 1.0, "")
        println chargeAmountPCF.toString()

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormal(tokenRange)//setting the token as normal

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
            firstSixDigits = root.body.ecomTokenInfo.firstSixDigits
            lastFourDigits = root.body.ecomTokenInfo.lastFourDigits
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                    .body('body.txnInfo.BANKTXNID', not(isEmptyOrNullString()))
                    .body('body.txnInfo.CHARGEAMOUNT', equalTo(format.format(chargeAmountPCF)))
                    .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                    .body('body.txnInfo.CURRENCY', equalTo('INR'))
                    .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                    .body('body.txnInfo.MID', equalTo(m().id))
                    .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                    .body('body.txnInfo.PAYMENTMODE', equalTo('DC'))
                    .body('body.txnInfo.RESPCODE', equalTo('01'))
                    .body('body.txnInfo.RESPMSG', equalTo('Txn Success'))
                    .body('body.txnInfo.STATUS', equalTo('TXN_SUCCESS'))
                    .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                    .body('body.txnInfo.TXNDATE', notNullValue())
                    .body('body.txnInfo.TXNID', notNullValue())
                    .body('body.callBackUrl', notNullValue())
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
        PEON:
        {
            assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
            Peon peon = peons.getAt(orderId.get())
            SoftAssertion softAssertion = new SoftAssertion()
            softAssertion.apply(
                    peon.gatewayName().equals("AXIS"),
                    peon.payMode().equals("DC"),
                    peon.txnDate().equals("").not(),
                    peon.txnDateTime().equals("").not(),
                    peon.custId().equals("").not(),
                    peon.status().equals("TXN_SUCCESS"),
                    peon.mId().equals(m().id),
                    peon.orderId().equals(orderId.get()),
                    peon.currency().equals("INR"),
                    peon.txnId().equals("").not(),
                    peon.txnAmt().equals(txnAmt),
                    peon.bankTxnId().equals("").not(),
                    peon.bankName().equals("HDFC Bank"),
                    peon.respMsg().equals("Txn Success"),
                    peon.respCode().equals("01"),
                    peon.mercUnqRef().equals(""),
                    peon.maskedEcomToken().contains(ecom + "******"),
                    peon.isChecksumValid()
            )
            softAssertion.eval()
        }
        MERCHANT_STATUS:
        {
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
            Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
            JsonPath jsonPath = response.jsonPath()
            String MerchantResBody = GetJSONStringBody(response)
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS")
            Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
            Assertions.assertThat(jsonPath.getString("body.chargeAmount")).isEqualTo(format.format(chargeAmountPCF))
            Assertions.assertThat(jsonPath.get("body.maskedCardNo")).isEqualTo(firstSixDigits + "******" + lastFourDigits).as("Masked card number is not coming")
            Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
            Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "******").as("Masked ecom token not coming");
            Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
            Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify failure transaction with ECOM token with all mandatory and optional fields in PTC"() {
        String txnAmt = '223.13' //setting a random transaction amount
        String ecom, firstSixDigits, lastFourDigits
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormal(tokenRange)//setting the token as normal

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.networkTokenRequestorId = 'abcd'
            //explicitly setting a value to fail the transaction at bank end
            ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
            firstSixDigits = root.body.ecomTokenInfo.firstSixDigits
            lastFourDigits = root.body.ecomTokenInfo.lastFourDigits
            //merchant with retry count=1
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.retry', equalTo(true))

            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.retry', equalTo(false))
                    .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                    .body('body.txnInfo.BANKTXNID', notNullValue())
                    .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                    .body('body.txnInfo.CURRENCY', equalTo('INR'))
                    .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                    .body('body.txnInfo.MID', equalTo(m().id))
                    .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                    .body('body.txnInfo.PAYMENTMODE', equalTo('DC'))
                    .body('body.txnInfo.RESPCODE', equalTo('232'))
                    .body('body.txnInfo.RESPMSG', equalTo('Payment failed due to a technical error. Try after some time.'))
                    .body('body.txnInfo.STATUS', equalTo('TXN_FAILURE'))
                    .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                    .body('body.txnInfo.TXNDATE', notNullValue())
                    .body('body.txnInfo.TXNID', notNullValue())
                    .body('body.callBackUrl', notNullValue())
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
        CLOSE_ORDER:
        {
            CloseOrderDTO closeOrderDTO = new CloseOrderDTO()
            closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                    .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderId.get()).setMid(m().id))
            CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO)
            Response response1 = closeOrderAPI.execute()
            String resultMsg = response1.path("body.resultInfo.resultMsg")
            Assertions.assertThat(resultMsg).isEqualTo("SUCCESS")
        }
        PEON:
        {
            assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
            Peon peon = peons.getAt(orderId.get())
            SoftAssertion softAssertion = new SoftAssertion()
            softAssertion.apply(
                    peon.gatewayName().equals("AXIS"),
                    peon.payMode().equals("DC"),
                    peon.txnDate().equals("").not(),
                    peon.txnDateTime().equals("").not(),
                    peon.custId().equals("").not(),
                    peon.status().equals("TXN_FAILURE"),
                    peon.mId().equals(m().id),
                    peon.orderId().equals(orderId.get()),
                    peon.currency().equals("INR"),
                    peon.txnId().equals("").not(),
                    peon.txnAmt().equals(txnAmt),
                    peon.bankTxnId().equals("").not(),
                    peon.bankName().equals("HDFC Bank"),
                    peon.respMsg().equals("Payment failed due to a technical error. Try after some time."),
                    peon.respCode().equals("232"),
                    peon.mercUnqRef().equals(""),
                    peon.maskedEcomToken().contains(ecom + "******"),
                    peon.isChecksumValid()
            )
            softAssertion.eval()
        }
        MERCHANT_STATUS:
        {
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
            Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
            JsonPath jsonPath = response.jsonPath()
            String MerchantResBody = GetJSONStringBody(response)
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE")
            Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
            Assertions.assertThat(jsonPath.get("body.maskedCardNo")).isEqualTo(firstSixDigits + "******" + lastFourDigits).as("Masked card number is not coming")
            Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
            Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "******").as("Masked ecom token not coming");
            Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
            Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify failure transaction with ECOM token with all mandatory and optional fields in PTC for MDR+PCF merchant"() {
        String txnAmt = '223.13' //setting a random transaction amount
        String ecom, firstSixDigits, lastFourDigits
        double chargeAmountPCF
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        PGPBaseTest pgpBaseTest = new PGPBaseTest();
        chargeAmountPCF = pgpBaseTest.convenienceFeeCalculator(txnAmt.toDouble(), 1.2, 1.0, "")
        println chargeAmountPCF.toString()

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormal(tokenRange)//setting the token as normal

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.networkTokenRequestorId = 'abcd'
            //explicitly setting a value to fail the transaction at bank end
            ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
            firstSixDigits = root.body.ecomTokenInfo.firstSixDigits
            lastFourDigits = root.body.ecomTokenInfo.lastFourDigits
            //merchant with retry count=1
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.retry', equalTo(true))

            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.retry', equalTo(false))
                    .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                    .body('body.txnInfo.BANKTXNID', notNullValue())
                    .body('body.txnInfo.CHARGEAMOUNT', equalTo(format.format(chargeAmountPCF)))
                    .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                    .body('body.txnInfo.CURRENCY', equalTo('INR'))
                    .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                    .body('body.txnInfo.MID', equalTo(m().id))
                    .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                    .body('body.txnInfo.PAYMENTMODE', equalTo('DC'))
                    .body('body.txnInfo.RESPCODE', equalTo('232'))
                    .body('body.txnInfo.RESPMSG', equalTo('Payment failed due to a technical error. Try after some time.'))
                    .body('body.txnInfo.STATUS', equalTo('TXN_FAILURE'))
                    .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                    .body('body.txnInfo.TXNDATE', notNullValue())
                    .body('body.txnInfo.TXNID', notNullValue())
                    .body('body.callBackUrl', notNullValue())
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))

        }
        CLOSE_ORDER:
        {
            CloseOrderDTO closeOrderDTO = new CloseOrderDTO()
            closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                    .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderId.get()).setMid(m().id))
            CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO)
            Response response1 = closeOrderAPI.execute()
            String resultMsg = response1.path("body.resultInfo.resultMsg")
            Assertions.assertThat(resultMsg).isEqualTo("SUCCESS")
        }
        PEON:
        {
            assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
            Peon peon = peons.getAt(orderId.get())
            SoftAssertion softAssertion = new SoftAssertion()
            softAssertion.apply(
                    peon.gatewayName().equals("AXIS"),
                    peon.payMode().equals("DC"),
                    peon.txnDate().equals("").not(),
                    peon.txnDateTime().equals("").not(),
                    peon.custId().equals("").not(),
                    peon.status().equals("TXN_FAILURE"),
                    peon.mId().equals(m().id),
                    peon.orderId().equals(orderId.get()),
                    peon.currency().equals("INR"),
                    peon.txnId().equals("").not(),
                    peon.txnAmt().equals(txnAmt),
                    peon.bankTxnId().equals("").not(),
                    peon.bankName().equals("HDFC Bank"),
                    peon.respMsg().equals("Payment failed due to a technical error. Try after some time."),
                    peon.respCode().equals("232"),
                    peon.mercUnqRef().equals(""),
                    peon.maskedEcomToken().contains(ecom + "******"),
                    peon.isChecksumValid()
            )
            softAssertion.eval()
        }
        MERCHANT_STATUS:
        {
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
            Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
            JsonPath jsonPath = response.jsonPath()
            String MerchantResBody = GetJSONStringBody(response)
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE")
            Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
            Assertions.assertThat(jsonPath.getString("body.chargeAmount")).isEqualTo(format.format(chargeAmountPCF))
            Assertions.assertThat(jsonPath.get("body.maskedCardNo")).isEqualTo(firstSixDigits + "******" + lastFourDigits).as("Masked card number is not coming")
            Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
            Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "******").as("Masked ecom token not coming");
            Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
            Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.tokenTxnSupported == false && it.getPreferences().nativeJsonRequest.enabled })
    @Test
    void "Verify that PTC API fails for merchant not having token support preference for transaction with ECOM token"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('EcomToken transaction is not supported in merchant contract'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify success transaction with CC ECOM token with only mandatory fields in PTC"() {
        String txnAmt = '20.00' //setting a random transaction amount
        String ecom
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount = txnAmt
            root.body.payableAmount = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormalVISACC(tokenRange)//setting the token as normal CC

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.remove('firstSixDigits')
            root.body.ecomTokenInfo.remove('lastFourDigits')
            root.body.ecomTokenInfo.remove('issuingBank')
            root.body.ecomTokenInfo.remove('networkTokenRequestorId')
            ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                    .body('body.txnInfo.BANKTXNID', not(isEmptyOrNullString()))
                    .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                    .body('body.txnInfo.CURRENCY', equalTo('INR'))
                    .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                    .body('body.txnInfo.MID', equalTo(m().id))
                    .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                    .body('body.txnInfo.PAYMENTMODE', equalTo('CC'))
                    .body('body.txnInfo.RESPCODE', equalTo('01'))
                    .body('body.txnInfo.RESPMSG', equalTo('Txn Success'))
                    .body('body.txnInfo.STATUS', equalTo('TXN_SUCCESS'))
                    .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                    .body('body.txnInfo.TXNDATE', notNullValue())
                    .body('body.txnInfo.TXNID', notNullValue())
                    .body('body.callBackUrl', notNullValue())
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
        PEON:
        {
            assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
            Peon peon = peons.getAt(orderId.get())
            SoftAssertion softAssertion = new SoftAssertion()
            softAssertion.apply(
                    peon.gatewayName().equals("AXIS"),
                    peon.payMode().equals("CC"),
                    peon.txnDate().equals("").not(),
                    peon.txnDateTime().equals("").not(),
                    peon.custId().equals("").not(),
                    peon.status().equals("TXN_SUCCESS"),
                    peon.mId().equals(m().id),
                    peon.orderId().equals(orderId.get()),
                    peon.currency().equals("INR"),
                    peon.txnId().equals("").not(),
                    peon.txnAmt().equals(txnAmt),
                    peon.bankTxnId().equals("").not(),
                    peon.bankName().equals("HDFC Bank"),
                    peon.respMsg().equals("Txn Success"),
                    peon.respCode().equals("01"),
                    peon.mercUnqRef().equals(""),
                    peon.maskedEcomToken().contains(ecom + "******"),
                    peon.isChecksumValid()
            )
            softAssertion.eval()
        }
        MERCHANT_STATUS:
        {
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
            Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
            JsonPath jsonPath = response.jsonPath()
            String MerchantResBody = GetJSONStringBody(response)
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS")
            Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
            Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
            Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "******").as("Masked ecom token not coming");
            Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
            Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616'})
    @Test
    void "Verify that transaction with prepaid card ECOM token should fail"() {
  //      FF4JFlags.enable("prepaidCard")
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsPrepaid(tokenRange)//setting the token as prepaid

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', DEFAULT_TOKEN).get().then()
                    .body('prepaidCard', equalTo(true))
        }
        try {
            PTC:
            {
                def root = ptcRoot()
                root.head.channelId = 'SYSTEM'
                root.body.paymentMode = 'DEBIT_CARD'
                root.body.requestType = 'NATIVE'
                root.body.emiType = null
                root.body.authMode = null
                root.body.ecomTokenInfo = ecomTokenInfo()
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('F'))
                        .body('body.resultInfo.resultCode', equalTo('0001'))
                        .body('body.resultInfo.resultMsg', equalTo('Prepaid Card is not allowed for this transaction, kindly use some other payment mode'))
                        .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
            }
        }
        finally {
            setTokenAsNormal(tokenRange)

            RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        //    FF4JFlags.disable("prepaidCard")
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616'})
    @Test
    void "Verify that transaction with prepaid card ECOM token should fail for MDR+PCF merchant"() {
   //     FF4JFlags.enable("prepaidCard")
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsPrepaid(tokenRange)//setting the token as prepaid

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', DEFAULT_TOKEN).get().then()
                    .body('prepaidCard', equalTo(true))
        }
        try {
            PTC:
            {
                def root = ptcRoot()
                root.head.channelId = 'SYSTEM'
                root.body.paymentMode = 'DEBIT_CARD'
                root.body.requestType = 'NATIVE'
                root.body.emiType = null
                root.body.authMode = null
                root.body.ecomTokenInfo = ecomTokenInfo()
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('F'))
                        .body('body.resultInfo.resultCode', equalTo('0001'))
                        .body('body.resultInfo.resultMsg', equalTo('Prepaid Card is not allowed for this transaction, kindly use some other payment mode'))
                        .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
            }
        }
        finally {
            setTokenAsNormal(tokenRange)

            RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

         //   FF4JFlags.disable("prepaidCard")
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify the transaction with corporate card ECOM token is successful"() {
        String txnAmt = "10.00" //setting a random transaction amount
        String ecom
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount = txnAmt
            root.body.payableAmount = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsCorporate(tokenRange)//setting the token as corporate

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', DEFAULT_TOKEN).get().then()
                    .body('corporateCard', equalTo(true))
        }
        try {
            PTC:
            {
                def root = ptcRoot()
                root.head.channelId = 'SYSTEM'
                root.body.paymentMode = 'DEBIT_CARD'
                root.body.requestType = 'NATIVE'
                root.body.emiType = null
                root.body.authMode = null
                root.body.ecomTokenInfo = ecomTokenInfo()
                ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('S'))
                        .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                        .body('body.txnInfo.BANKTXNID', not(isEmptyOrNullString()))
                        .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                        .body('body.txnInfo.CURRENCY', equalTo('INR'))
                        .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                        .body('body.txnInfo.MID', equalTo(m().id))
                        .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                        .body('body.txnInfo.PAYMENTMODE', equalTo('CC'))
                        .body('body.txnInfo.RESPCODE', equalTo('01'))
                        .body('body.txnInfo.RESPMSG', equalTo('Txn Success'))
                        .body('body.txnInfo.STATUS', equalTo('TXN_SUCCESS'))
                        .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                        .body('body.txnInfo.TXNDATE', notNullValue())
                        .body('body.txnInfo.TXNID', notNullValue())
                        .body('body.callBackUrl', notNullValue())
                        .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
            }
            PEON:
            {
                assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
                Peon peon = peons.getAt(orderId.get())
                SoftAssertion softAssertion = new SoftAssertion()
                softAssertion.apply(
                        peon.gatewayName().equals("AXIS"),
                        peon.payMode().equals("CC"),
                        peon.txnDate().equals("").not(),
                        peon.txnDateTime().equals("").not(),
                        peon.custId().equals("").not(),
                        peon.status().equals("TXN_SUCCESS"),
                        peon.mId().equals(m().id),
                        peon.orderId().equals(orderId.get()),
                        peon.currency().equals("INR"),
                        peon.txnId().equals("").not(),
                        peon.txnAmt().equals(txnAmt),
                        peon.bankTxnId().equals("").not(),
                        peon.bankName().equals("HDFC Bank"),
                        peon.respMsg().equals("Txn Success"),
                        peon.respCode().equals("01"),
                        peon.mercUnqRef().equals(""),
                        peon.feeRateFactors().equals("{\"corporateCard\":\"TRUE\"}"),
                        peon.maskedEcomToken().contains(ecom + "******"),
                        peon.isChecksumValid()
                )
                softAssertion.eval()
            }
            MERCHANT_STATUS:
            {
                GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
                Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
                JsonPath jsonPath = response.jsonPath()
                String MerchantResBody = GetJSONStringBody(response)
                Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS")
                Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
                Assertions.assertThat(jsonPath.getString("body.feeRateFactors.corporateCard")).isEqualTo("TRUE").as("Expected corporate fee rate factor not coming")
                Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
                Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "*****").as("Masked ecom token not coming");
                Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
                Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
            }
        }
        finally {
            setTokenAsNormal(tokenRange)

            RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)
        }
    }


    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify the transaction with corporate card ECOM token is successful for MDR+PCF merchant"() {
        String txnAmt = "77.39" //setting a random transaction amount
        Double chargeAmountPCF
        String ecom
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount = txnAmt
            root.body.payableAmount = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsCorporate(tokenRange)//setting the token as corporate

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', DEFAULT_TOKEN).get().then()
                    .body('corporateCard', equalTo(true))
        }

        PGPBaseTest pgpBaseTest = new PGPBaseTest()
        chargeAmountPCF = pgpBaseTest.convenienceFeeCalculator(txnAmt.toDouble(), 0.5, 0, "CC")
        println chargeAmountPCF.toString()

        try {
            PTC:
            {
                def root = ptcRoot()
                root.head.channelId = 'SYSTEM'
                root.body.paymentMode = 'DEBIT_CARD'
                root.body.requestType = 'NATIVE'
                root.body.emiType = null
                root.body.authMode = null
                root.body.ecomTokenInfo = ecomTokenInfo()
                ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('S'))
                        .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                        .body('body.txnInfo.BANKTXNID', not(isEmptyOrNullString()))
                        .body('body.txnInfo.CHARGEAMOUNT', equalTo(format.format(chargeAmountPCF)))
                        .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                        .body('body.txnInfo.CURRENCY', equalTo('INR'))
                        .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                        .body('body.txnInfo.MID', equalTo(m().id))
                        .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                        .body('body.txnInfo.PAYMENTMODE', equalTo('CC'))
                        .body('body.txnInfo.RESPCODE', equalTo('01'))
                        .body('body.txnInfo.RESPMSG', equalTo('Txn Success'))
                        .body('body.txnInfo.STATUS', equalTo('TXN_SUCCESS'))
                        .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                        .body('body.txnInfo.TXNDATE', notNullValue())
                        .body('body.txnInfo.TXNID', notNullValue())
                        .body('body.callBackUrl', notNullValue())
                        .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
            }
            PEON:
            {
                assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
                Peon peon = peons.getAt(orderId.get())
                SoftAssertion softAssertion = new SoftAssertion()
                softAssertion.apply(
                        peon.gatewayName().equals("AXIS"),
                        peon.payMode().equals("CC"),
                        peon.txnDate().equals("").not(),
                        peon.txnDateTime().equals("").not(),
                        peon.custId().equals("").not(),
                        peon.status().equals("TXN_SUCCESS"),
                        peon.mId().equals(m().id),
                        peon.orderId().equals(orderId.get()),
                        peon.currency().equals("INR"),
                        peon.txnId().equals("").not(),
                        peon.txnAmt().equals(txnAmt),
                        peon.bankTxnId().equals("").not(),
                        peon.bankName().equals("HDFC Bank"),
                        peon.respMsg().equals("Txn Success"),
                        peon.respCode().equals("01"),
                        peon.mercUnqRef().equals(""),
                        peon.feeRateFactors().equals("{\"corporateCard\":\"TRUE\"}"),
                        peon.maskedEcomToken().contains(ecom + "******"),
                        peon.isChecksumValid()
                )
                softAssertion.eval()
            }
            MERCHANT_STATUS:
            {
                GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
                Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
                JsonPath jsonPath = response.jsonPath()
                String MerchantResBody = GetJSONStringBody(response)
                Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS")
                Assertions.assertThat(jsonPath.getString("body.chargeAmount")).isEqualTo(format.format(chargeAmountPCF))
                Assertions.assertThat(jsonPath.getString("body.feeRateFactors.corporateCard")).isEqualTo("TRUE").as("Expected corporate fee rate factor not coming")
                Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
                Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "*****").as("Masked ecom token not coming")
                Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
                Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
            }
        }
        finally {
            setTokenAsNormal(tokenRange)

            RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that PTC API fails with ECOM token when mandatory parameter ecomtoken is missing"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.remove('ecomToken')
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Request parameters are not valid'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that PTC API fails with ECOM token when mandatory parameter expiryMonth is missing"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.remove('expiryMonth')
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Request parameters are not valid'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that PTC API fails with ECOM token when mandatory parameter expiryYear is missing"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.remove('expiryYear')
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Request parameters are not valid'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616'})
    @Test
    void "Verify that PTC API fails with ECOM token when mandatory parameter authenticationValue is missing"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.remove('authenticationValue')
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Request parameters are not valid'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that in case of mismatch of paymentMode and issuingBank between merchant and our DB, our DB credentials are picked"() {
        String txnAmt = '23.01' //setting a random transaction amount
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormal(tokenRange)//setting the token as normal

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.issuingBank = 'ICICI Bank'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .body('body.txnInfo.BANKNAME', equalTo('HDFC Bank'))
                    .body('body.txnInfo.PAYMENTMODE', equalTo('DC'))
        }
        PEON:
        {
            assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
            Peon peon = peons.getAt(orderId.get())
            SoftAssertion softAssertion = new SoftAssertion()
            softAssertion.apply(
                    peon.gatewayName().equals("AXIS"),
                    peon.payMode().equals("DC"),
                    peon.txnDate().equals("").not(),
                    peon.txnDateTime().equals("").not(),
                    peon.custId().equals("").not(),
                    peon.status().equals("TXN_SUCCESS"),
                    peon.mId().equals(m().id),
                    peon.orderId().equals(orderId.get()),
                    peon.currency().equals("INR"),
                    peon.txnId().equals("").not(),
                    peon.txnAmt().equals(txnAmt),
                    peon.bankTxnId().equals("").not(),
                    peon.bankName().equals("HDFC Bank"),
                    peon.respMsg().equals("Txn Success"),
                    peon.respCode().equals("01"),
                    peon.mercUnqRef().equals(""),
                    peon.maskedEcomToken().contains("******"),
                    peon.isChecksumValid()
            )
            softAssertion.eval()
        }
        MERCHANT_STATUS:
        {
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
            Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
            JsonPath jsonPath = response.jsonPath()
            String MerchantResBody = GetJSONStringBody(response)
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS")
            Assertions.assertThat(jsonPath.getString('body.bankName')).isEqualTo("HDFC Bank").as("Bank name details do not match")
            Assertions.assertThat(jsonPath.getString('body.paymentMode')).isEqualTo("DC").as("Card details do not match")
            Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
            Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that the PTC API fails when expiryMonth field has value more than 12"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.expiryMonth = '13'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Invalid Payment Details'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that the PTC API fails when expiryMonth field has only single digit value"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.expiryMonth = '3'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Invalid Payment Details'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify transaction with VISA CC ECOM token"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormalVISACC(tokenRange)//setting the token as normal CC VISA

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.txnInfo.PAYMENTMODE', equalTo("CC"))
                    .body('body.txnInfo.RESPCODE', equalTo("01"))
                    .body('body.txnInfo.RESPMSG', equalTo("Txn Success"))
                    .body('body.txnInfo.TXNID', not(isEmptyOrNullString()))
                    .body('body.txnInfo.GATEWAYNAME', equalTo("AXIS"))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    @Issue('PGP-27965')
//Now, there is no check on past expiry year for ecom token transaction but still needs to be confirmed by product
    void "Verify that the PTC API fails when expiryYear is of past"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.expiryMonth = '12'
            root.body.ecomTokenInfo.expiryYear = '2019'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Card details entered by the user is/are invalid.')) //Error according to current handling
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test()
    @Issue('PGP-27965')
//Now, there is no check on card length for ecom token transaction but still needs to be confirmed by product
    void "Verify that the PTC API fails when ECOM token digits are less than 16"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.ecomToken = '465010000000083'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same')) ////Error according to current handling
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify transaction should fail when CC ECOM token transaction is initiated when CC acquiring is not present on MID"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormalVISACC(tokenRange)//setting the token as normal CC

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Credit Card is not allowed for this transaction, kindly use some other payment mode'))
                    .body('body.txnInfo.RESPCODE', equalTo("317"))
                    .body('body.txnInfo.RESPMSG', equalTo("Invalid payment mode"))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify transaction should fail for prepaid ECOM token transaction is initiated when Prepaid acquiring is not present on MID"() {
  //      FF4JFlags.enable("prepaidCard")
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsPrepaid(tokenRange)//setting the token as prepaid

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', '465010000').get().then()
                    .body('prepaidCard', equalTo(true))
        }
        try {
            PTC:
            {
                def root = ptcRoot()
                root.head.channelId = 'SYSTEM'
                root.body.paymentMode = 'DEBIT_CARD'
                root.body.requestType = 'NATIVE'
                root.body.emiType = null
                root.body.authMode = null
                root.body.ecomTokenInfo = ecomTokenInfo()
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('F'))
                        .body('body.resultInfo.resultCode', equalTo('0001'))
                        .body('body.resultInfo.resultMsg', equalTo('Prepaid Card is not allowed for this transaction, kindly use some other payment mode'))
                        .body('body.txnInfo.RESPCODE', equalTo("317"))
                        .body('body.txnInfo.RESPMSG', equalTo("Invalid payment mode"))
                        .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
            }
        }
        finally {
            setTokenAsNormal(tokenRange)

            RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

   //         FF4JFlags.disable("prepaidCard")
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test()
    void "Verify that the PTC API fails when EMI transaction is initiated with ECOM token"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'EMI'
            root.body.requestType = 'NATIVE'
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.emiType = 'DEBIT_CARD'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.resultCode', equalTo('0001'))
                    .body('body.resultInfo.resultMsg', equalTo('Request parameters are not valid'))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616'})
    @Test
    void "Verify successful refund of ECOM token transaction"() {
        String txnAmt = '20.00'
        String txnId
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount = txnAmt
            root.body.payableAmount = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsNormal(tokenRange)//setting the token as normal

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            Response response = given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post()
            txnId = response.getBody().jsonPath().getString('body.txnInfo.TXNID')
            JsonPath js = new JsonPath(response.asString())
            String txnStatus = js.get('body.resultInfo.resultStatus')
            Assertions.assertThat(txnStatus, equalTo('S')).as('Transaction not successful, refund cannot be initiated')
        }
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(m().id);
        }
        Refund:
        {
            PGPHelpers.validate_MerchantPreference(m().id, "REFUND_SUCCESS_PEON_ENABLED", "Y");
            PGPHelpers.initiateAsyncRefund(m().id, m().key, orderId.get(), orderId.get(), txnId, txnAmt, "REFUND", "InitiateRefund", null)
            PGPHelpers.getRefundStatusV1(m().id, orderId.get(), orderId.get(), m().key, true)
                    .validateSuccessRefund()
                    .validateMid(m().id)
                    .validateRefundAmount(txnAmt)
                    .validateTotalRefundAmount(txnAmt)
                    .asserAll()

            RefundSucessNotifyPeon refundNotify = new RefundSucessNotifyPeon(orderId.get(), m().id)
            refundNotify.validateBasicDetails(m().id, orderId.get())
        }
    }

    //@Owner(ARSH)
    //@Override
    //@Feature("PGP-23214")
   // @Test(enabled = false)
//TODO need to check with the dev why HDFC formatter is getting picked during callback
    void "Verify success transaction with normal card with cybersource gateway"() {
        Constants.MerchantType merchantType = Constants.MerchantType.JIO
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(Constants.MerchantType.JIO.getId())
                .setMerchantKey(Constants.MerchantType.JIO.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO)
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).setCardInfo("|4012009999900011|099|042025")
                .setAUTH_MODE("otp")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage()
        checkoutPage.createNativeOrder(orderDTO, true)
        ResponsePage responsePage = new ResponsePage()
        responsePage.waitUntilLoads()
        responsePage.validateStatus("TXN_SUCCESS")
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify transaction should fail for corporate ECOM token transaction is initiated when Corporate acquiring is not present on MID"() {
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        String[] tokenRange = getTokenRange(DEFAULT_TOKEN)//fetching the token range
        setTokenAsCorporateDC(tokenRange)//setting the token as corporate DC

        RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', '465010000').get().then()
                    .body('corporateCard', equalTo(true))
        }
        try {
            PTC:
            {
                def root = ptcRoot()
                root.head.channelId = 'SYSTEM'
                root.body.paymentMode = 'DEBIT_CARD'
                root.body.requestType = 'NATIVE'
                root.body.emiType = null
                root.body.authMode = null
                root.body.ecomTokenInfo = ecomTokenInfo()
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('F'))
                        .body('body.resultInfo.resultCode', equalTo('0001'))
                        .body('body.resultInfo.resultMsg', equalTo('Corporate Card is not allowed for this transaction, kindly use some other payment mode'))
                        .body('body.txnInfo.RESPCODE', equalTo("317"))
                        .body('body.txnInfo.RESPMSG', equalTo("Invalid payment mode"))
                        .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
            }
        }
        finally {
            setTokenAsNormal(tokenRange)
            RedisAPI.deleteKey("TOKEN_DETAILS_" + DEFAULT_TOKEN)
        }
    }

    //@Owner(ARSH)
   // @Override
    //@Feature("PGP-23214")
   // @Merchant({ it.id == 'normid27075651332616' })
   // @Test(enabled = false)
//TODO Unavailability of test ECOM Tokens and 2D transaction mock
    void "Verify transaction with MASTER CC ECOM token"() {
        //Cannot run end to end transaction because of non-availability of token/mock.
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.ecomToken = '9990129440000001'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.txnInfo.PAYMENTMODE', equalTo("CC"))
                    .body('body.txnInfo.RESPCODE', equalTo("227"))
                    .body('body.txnInfo.RESPMSG', equalTo("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"))
                    .body('body.txnInfo.TXNID', not(isEmptyOrNullString()))
                    .body('body.txnInfo.GATEWAYNAME', equalTo("AXIS"))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

//    @Owner(ARSH)
//    @Override
//    @Feature("PGP-23214")
//    @Merchant({ it.id == 'normid27075651332616' })
//    @Test(enabled = false)
    //TODO Unavailability of test ECOM Tokens and 2D transaction mock
    void "Verify transaction with MASTER DC ECOM token"() {
        //Cannot run end to end transaction because of non-availability of token/mock.
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo = '3234569009888288'
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.txnInfo.PAYMENTMODE', equalTo("DC"))
                    .body('body.txnInfo.RESPCODE', equalTo("227"))
                    .body('body.txnInfo.RESPMSG', equalTo("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"))
                    .body('body.txnInfo.TXNID', not(isEmptyOrNullString()))
                    .body('body.txnInfo.GATEWAYNAME', equalTo("AXIS"))
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that transaction does not fail at theia end when ECOM token passed has bank as null in get token API"() {
        //could not test end to end success because of unavailability of test token/mock
        String txnAmt = '10.00' //setting a random transaction amount
        String ecom, firstSixDigits, lastFourDigits
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(BANK_NULL_TOKEN)//fetching the token range
        setTokenAsBankNull(tokenRange)//setting the token with bank=null

        RedisAPI.deleteKey("TOKEN_DETAILS_" + BANK_NULL_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', BANK_NULL_TOKEN).get().then()
                    .body('bank', equalTo(null)) //jira : PGP-31833, bank=null use case
                    .body('cardType', not(isEmptyOrNullString()))
                    .body('cardName', not(isEmptyOrNullString()))
        }
        PTC:
        {
            def root = ptcRoot()
            root.head.channelId = 'SYSTEM'
            root.body.paymentMode = 'DEBIT_CARD'
            root.body.requestType = 'NATIVE'
            root.body.emiType = null
            root.body.authMode = null
            root.body.ecomTokenInfo = ecomTokenInfo()
            root.body.ecomTokenInfo.ecomToken = BANK_NULL_TOKEN + '0000083'
            ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
            firstSixDigits = root.body.ecomTokenInfo.firstSixDigits
            lastFourDigits = root.body.ecomTokenInfo.lastFourDigits
            //merchant with retry count=1
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.resultInfo.retry', equalTo(true))
            given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .body('body.txnInfo.BANKTXNID', equalTo(""))
                    .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                    .body('body.txnInfo.CURRENCY', equalTo('INR'))
                    .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                    .body('body.txnInfo.MID', equalTo(m().id))
                    .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                    .body('body.txnInfo.PAYMENTMODE', equalTo('DC'))
                    .body('body.txnInfo.RESPCODE', equalTo('227'))
                    .body('body.txnInfo.RESPMSG', equalTo('Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same'))
                    .body('body.txnInfo.STATUS', equalTo('TXN_FAILURE'))
                    .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                    .body('body.txnInfo.TXNDATE', notNullValue())
                    .body('body.txnInfo.TXNID', notNullValue())
                    .body('body.callBackUrl', notNullValue())
                    .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
        }
        CLOSE_ORDER:
        {
            CloseOrderDTO closeOrderDTO = new CloseOrderDTO()
            closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                    .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderId.get()).setMid(m().id))
            CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO)
            Response response1 = closeOrderAPI.execute()
            String resultMsg = response1.path("body.resultInfo.resultMsg")
            Assertions.assertThat(resultMsg).isEqualTo("SUCCESS")
        }
        PEON:
        {
            assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
            Peon peon = peons.getAt(orderId.get())
            SoftAssertion softAssertion = new SoftAssertion()
            softAssertion.apply(
                    peon.gatewayName().equals("AXIS"),
                    peon.payMode().equals("DC"),
                    peon.txnDate().equals("").not(),
                    peon.txnDateTime().equals("").not(),
                    peon.custId().equals("").not(),
                    peon.status().equals("TXN_FAILURE"),
                    peon.mId().equals(m().id),
                    peon.orderId().equals(orderId.get()),
                    peon.currency().equals("INR"),
                    peon.txnId().equals("").not(),
                    peon.txnAmt().equals(txnAmt),
                    peon.bankTxnId().equals("").not(),
                    peon.respMsg().equals("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"),
                    peon.respCode().equals("227"),
                    peon.mercUnqRef().equals(""),
                    peon.maskedEcomToken().contains(ecom + "******"),
                    peon.isChecksumValid()
            )
            softAssertion.eval()
        }
        MERCHANT_STATUS:
        {
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
            Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
            JsonPath jsonPath = response.jsonPath()
            String MerchantResBody = GetJSONStringBody(response)
            Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE")
            Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
            Assertions.assertThat(jsonPath.get("body.maskedCardNo")).isEqualTo(firstSixDigits + "******" + lastFourDigits).as("Masked card number is not coming")
            Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
            Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "******").as("Masked ecom token not coming");
            Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
            Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
        }
    }

    @Owner(ARSH)
    @Override
    @Feature("PGP-23214")
    @Merchant({ it.id == 'normid27075651332616' })
    @Test
    void "Verify that corporate card transaction does not fail at theia end when ECOM token passed has bank as null in get token API"() {
        //could not test end to end success because of unavailability of test token/mock
        String txnAmt = '10.00' //setting a random transaction amount
        String ecom, firstSixDigits, lastFourDigits
        INIT_TXN:
        {
            def root = initTxnRoot()
            root.body.txnAmount.value = txnAmt
            root.body.payableAmount.value = txnAmt
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }

        String[] tokenRange = getTokenRange(BANK_NULL_TOKEN)//fetching the token range
        setTokenAsBankNullCorporateCard(tokenRange)//setting the corporate card token with bank=null

        RedisAPI.deleteKey("TOKEN_DETAILS_" + BANK_NULL_TOKEN)

        GET_TOKEN_INFO:
        {
            def root = getTokenReq()
            root.pathParam('token', BANK_NULL_TOKEN).get().then()
                    .body('bank', equalTo(null)) //jira : PGP-31833, bank=null use case
                    .body('cardType', not(isEmptyOrNullString()))
                    .body('cardName', not(isEmptyOrNullString()))
        }
        try {
            PTC:
            {
                def root = ptcRoot()
                root.head.channelId = 'SYSTEM'
                root.body.paymentMode = 'DEBIT_CARD'
                root.body.requestType = 'NATIVE'
                root.body.emiType = null
                root.body.authMode = null
                root.body.ecomTokenInfo = ecomTokenInfo()
                root.body.ecomTokenInfo.ecomToken = BANK_NULL_TOKEN + '0000083'
                ecom = root.body.ecomTokenInfo.ecomToken.subSequence(0, 6).toString()
                firstSixDigits = root.body.ecomTokenInfo.firstSixDigits
                lastFourDigits = root.body.ecomTokenInfo.lastFourDigits
                //merchant with retry count=1
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('F'))
                        .body('body.resultInfo.retry', equalTo(true))
                given(ptcReqBldr().addHeader("User-Agent", "").build()).body(root).post().then()
                        .body('body.resultInfo.resultStatus', equalTo('F'))
                        .body('body.txnInfo.BANKTXNID', equalTo(""))
                        .body('body.txnInfo.CHECKSUMHASH', notNullValue())
                        .body('body.txnInfo.CURRENCY', equalTo('INR'))
                        .body('body.txnInfo.GATEWAYNAME', equalTo('AXIS'))
                        .body('body.txnInfo.MID', equalTo(m().id))
                        .body('body.txnInfo.ORDERID', equalTo(orderId.get()))
                        .body('body.txnInfo.PAYMENTMODE', equalTo('CC'))
                        .body('body.txnInfo.RESPCODE', equalTo('227'))
                        .body('body.txnInfo.RESPMSG', equalTo('Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same'))
                        .body('body.txnInfo.STATUS', equalTo('TXN_FAILURE'))
                        .body('body.txnInfo.TXNAMOUNT', equalTo(txnAmt))
                        .body('body.txnInfo.TXNDATE', notNullValue())
                        .body('body.txnInfo.TXNID', notNullValue())
                        .body('body.callBackUrl', notNullValue())
                        .body('body.txnInfo', HasValidChecksum.hasValidChecksum(m().key))
            }
            CLOSE_ORDER:
            {
                CloseOrderDTO closeOrderDTO = new CloseOrderDTO()
                closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                        .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderId.get()).setMid(m().id))
                CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO)
                Response response1 = closeOrderAPI.execute()
                String resultMsg = response1.path("body.resultInfo.resultMsg")
                Assertions.assertThat(resultMsg).isEqualTo("SUCCESS")
            }
            PEON:
            {
                assertion.apply(peonWait.apply({ -> peons.getAt(orderId.get()) != null }))
                Peon peon = peons.getAt(orderId.get())
                SoftAssertion softAssertion = new SoftAssertion()
                softAssertion.apply(
                        peon.gatewayName().equals("AXIS"),
                        peon.payMode().equals("CC"),
                        peon.txnDate().equals("").not(),
                        peon.txnDateTime().equals("").not(),
                        peon.custId().equals("").not(),
                        peon.status().equals("TXN_FAILURE"),
                        peon.mId().equals(m().id),
                        peon.orderId().equals(orderId.get()),
                        peon.currency().equals("INR"),
                        peon.txnId().equals("").not(),
                        peon.txnAmt().equals(txnAmt),
                        peon.bankTxnId().equals("").not(),
                        peon.respMsg().equals("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"),
                        peon.respCode().equals("227"),
                        peon.mercUnqRef().equals(""),
                        peon.maskedEcomToken().contains(ecom + "******"),
                        peon.isChecksumValid()
                )
                softAssertion.eval()
            }
            MERCHANT_STATUS:
            {
                GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(orderId.get(), m()).build()
                Response response = new GetPaymentStatus(getPaymentStatusDTO).execute()
                JsonPath jsonPath = response.jsonPath()
                String MerchantResBody = GetJSONStringBody(response)
                Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE")
                Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmt)
                Assertions.assertThat(jsonPath.get("body.maskedCardNo")).isEqualTo(firstSixDigits + "******" + lastFourDigits).as("Masked card number is not coming")
                Assertions.assertThat(jsonPath.getString("body.feeRateFactors.ecomToken")).isEqualTo("TRUE").as("Fee rate factor not coming")
                Assertions.assertThat(jsonPath.getString("body.maskedEcomToken")).contains(ecom + "******").as("Masked ecom token not coming");
                Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(m().key, MerchantResBody, jsonPath.getString("head.signature"))
                Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue()
            }
        }
        finally {
            setTokenAsBankNull(tokenRange)

            RedisAPI.deleteKey("TOKEN_DETAILS_" + BANK_NULL_TOKEN)
        }
    }
}