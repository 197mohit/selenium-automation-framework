package scripts.flows

import com.paytm.api.TxnStatus
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.dto.OrderDTO
import com.paytm.framework.conditions.SoftAssertion
import com.paytm.pages.CashierPage
import com.paytm.pages.CashierPageFactory
import com.paytm.pages.CheckoutPage
import com.paytm.pages.responsePage.ResponsePage
import com.paytm.utils.ff4j.FF4JFlags
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.Card
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.testng.SkipException
import org.testng.annotations.Test
import scripts.api.theia.FetchCardIndexNumber

import java.text.DecimalFormat

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.*
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

@Owner('Deepak | Somesh')
class NativeTest extends TestSetUp implements FlipkartPromoTest {

    private final static String THEME = 'enhancedweb_revamp'
    private final static int INSTANT_DISCOUNT_PERCENTAGE = 5
    private final static CARD_NOT_ADDED = "unable to add card(s) successfully"
    private final static String PROMOS_NOT_ADDED = "unable to add promo(s) successfully"
    private final static String PROMO_NOT_APPLIED = "promo not applied successfully"
    private static final String TXN_TOKEN_NOT_GENERATED = 'unable to generate txn token'
    private final static String FAILURE_PROMO_CODE = 'failure'
    private final static String PG_DISCOUNT_PROMO_CODE = 'discount'
    private final static String PG_CASHBACK_PROMO_CODE = 'cashback'
    private final static String PAYTM_CASHBACK_PROMO_CODE = 'paytm_cashback'
    private static final ThreadLocal<String> token = new ThreadLocal<>()
    private static final ThreadLocal<String> orderId = new ThreadLocal<>()
    private static final ThreadLocal<String> mid = new ThreadLocal<>()
    private static final ThreadLocal<String> custId = new ThreadLocal<>()
    private static final ThreadLocal<Map> paymentOffer = new ThreadLocal<>()
    ResponsePage responsePage = new ResponsePage()

    private static class Status {
        static final String SUCCESS = 'TXN_SUCCESS'
        static final String FAILURE = 'TXN_FAILURE'
        static final String PENDING = 'PENDING'
    }

    private static class RespCode {
        static final String SUCCESS = '01'
        static final String FAILURE = '810'
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
                        paymentOffersApplied: '?'
                ]
        ]
    }

    final def ptcReq = {
        given(
                new RequestSpecBuilder()
                        .addFilters([pasteInitTxnDataToPTCFilter, setPTCQueryParamMidFilter, setPTCQueryParamOrderIdFilter, new RequestLoggingFilter()])
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.URLENC)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(PROCESS_TXN)
                        .addQueryParam('mid', '?')
                        .addQueryParam('orderId', '?')
                        .build()
        )
    }

    final def ptcRoot = {
        [
                mid             : '?',
                orderId         : '?',
                REQUEST_TYPE    : 'NATIVE',
                INDUSTRY_TYPE_ID: 'retail',
                channelId       : 'WEB',
                authMode        : 'USRPWD',
                paymentMode     : null,//M
                WEBSITE         : 'retail',
                txnToken        : '?',
        ]
    }

    OrderDTO.Builder orderBuilder() {
        new OrderDTO.Builder()
                .setMID(mid.get())
                .setORDER_ID(orderId.get())
                .setTXN_TOKEN(token.get())
                .setPaymentMode(null)
                .setREQUEST_TYPE('NATIVE')
                .setPaymentFlow('NONE')
                .setINDUSTRY_TYPE_ID("retail")
                .setWEBSITE("retail")
                .setCHANNEL_ID("WEB")
                .setAUTH_MODE("USRPWD")
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
            def root = requestSpec.getFormParams().collectEntries { [(it.key): it.value] }
            if (root?.txnToken == '?') root?.txnToken = token.get()
            if (root?.orderId == '?') root?.orderId = orderId.get()
            if (root?.mid == '?') root?.mid = mid.get()
            if (root?.custId == '?') root?.custId = custId.get()
            root.each { requestSpec.removeFormParam(it.key as String) }
            requestSpec.formParams(root.findAll { it.value != null } as Map<String, String>)
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

    private final Filter setPTCQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', requestSpec.getFormParams()?.mid ?: m()?.id ?: UUID.randomUUID().toString())
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

    private final Filter setPTCQueryParamOrderIdFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['orderId'] == '?') {
                requestSpec.removeQueryParam('orderId').queryParam('orderId', requestSpec.getFormParams()?.orderId ?: new Random().nextLong().abs() as String)
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
            OrderDTO order = orderBuilder()
                    .setPAYMENT_TYPE_ID('BALANCE')
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser
    @Test
    void "test when order payment is done by cc given paytm_cashback promo is applied"() {
        final double txnAmt = 1D
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
            OrderDTO order = orderBuilder()
                    .setPAYMENT_TYPE_ID('CREDIT_CARD')
                    .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
        }
    }
    @Owner(Constants.Owner.ESHANI)
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser
    @Test
    void "test if tncUrl not present in apply promo response if ff4j flag OFF"() {
        try{
        final double txnAmt = 1D
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        FF4JFlags.disable("theia.applyPromoSendResponseTncUrl");
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.totalTransactionAmount = txnAmt
            root.body.paymentOptions = [
                    [
                            transactionAmount: txnAmt,
                            payMethod        : 'CREDIT_CARD',
                            cardNo           : card.no,
                    ]
            ]
            if (applyPromoReq().body(root).post().path('body.resultInfo.resultStatus') != 'S') throw new SkipException(PROMO_NOT_APPLIED)
            String tncUrl = applyPromoReq().body(root).post().path("body.paymentOffer.tncUrl");
            Assertions.assertThat(tncUrl)
                    .as("TncUrl present in apply promo response despite flag being OFF")
                    .isNull();
        }
        INIT_TXN:
        {
            def root = initTxnRoot()
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            OrderDTO order = orderBuilder()
                    .setPAYMENT_TYPE_ID('CREDIT_CARD')
                    .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()


        }
        }
        finally {
            FF4JFlags.disable("theia.applyPromoSendResponseTncUrl");
        }
    }



//    @Override
//    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['amex', 'bajajfn']) } })
//    @AUser(edit = true)
//    @Test(enabled = false)
    //Somesh to update(Getting error msg - CheckoutPaymentPromo req builder validation failed - in logs)
    void "test when order payment is done by saved card CIN given paytm_cashback promo is applied"() {
        final double txnAmt = 1D
        String cardIdxNo = null
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        (user() as AlipayUser).savedCards.clear()
        def card = cards.find { it.type == 'credit' && !(it.scheme in ['amex', 'bajajfn']) }
        if (!(user() as AlipayUser).savedCards.add(card)) throw new SkipException(CARD_NOT_ADDED)
        APPLY_PROMO:
        {
            def root = applyPromoRoot()
            root.body.promocode = PAYTM_CASHBACK_PROMO_CODE
            root.body.totalTransactionAmount = txnAmt
            root.body.paymentOptions = [
                    [
                            transactionAmount: txnAmt,
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
            OrderDTO order = orderBuilder()
                    .setPAYMENT_TYPE_ID('CREDIT_CARD')
                    .setCardInfo("$cardIdxNo||$card.cvv|" as String)
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
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
            OrderDTO order = orderBuilder()
                    .setPAYMENT_TYPE_ID('CREDIT_CARD')
                    .setCardInfo("${user().savedCards.find().id}||$card.cvv|" as String)
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
        }
    }

    @Override
    @Merchant(edit = true, value = { it.id == 'SIgKmW85235084384561' })
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
            OrderDTO order = orderBuilder()
                    .setPaymentFlow('NONE')
                    .setPAYMENT_TYPE_ID('BALANCE')
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
            cashierPage.payBy(Constants.PayMode.CC)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
        }
    }

    @Override
    @Merchant(edit = true, value = { it.id == 'SIgKmW85235084384561' })
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
            OrderDTO order = orderBuilder()
                    .setPaymentFlow('NONE')
                    .setPAYMENT_TYPE_ID('BALANCE')
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
            cashierPage.payBy(Constants.PayMode.CC)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
        }
    }

    @Override
    @Merchant(edit = true, value = { it.id == 'SIgKmW85235084384561' })
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
            user().wallets['main'].balance = txnAmt - 1D
            OrderDTO order = orderBuilder()
                    .setPaymentFlow('NONE')
                    .setPAYMENT_TYPE_ID('BALANCE')
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
            cashierPage.payBy(Constants.PayMode.CC)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
        }
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('ppi') })
    @AUser(edit = true)
    @Test
    void "test when paytm_cashback promo is applied but discount promo details are passed while initiating order"() {
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
            paymentOffer.get().offerBreakup[0].promocodeApplied = FAILURE_PROMO_CODE
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            user().wallets['main'].balance = 1
            OrderDTO order = orderBuilder()
                    .setPAYMENT_TYPE_ID('BALANCE')
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.FAILURE),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.FAILURE)
            )
            sAssert.eval()
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
        final double promoAmt = 1
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
            root.body.txnAmount.value = promoAmt + 1
            if (!initTxnReq().body(root).post().path('body.txnToken')) throw new SkipException(TXN_TOKEN_NOT_GENERATED)
        }
        PTC:
        {
            OrderDTO order = orderBuilder()
                    .setPAYMENT_TYPE_ID('CREDIT_CARD')
                    .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                    .build()
            new CheckoutPage().createNativeOrder(order, false)
            pageWait.apply(responsePage.hasLoaded())
            SoftAssertion sAssert = new SoftAssertion()
            sAssert.apply(
                    responsePage.get(ResponsePage.Attribute.STATUS).equals(Status.SUCCESS),
                    responsePage.get(ResponsePage.Attribute.RESPCODE).equals(RespCode.SUCCESS)
            )
            sAssert.eval()
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus(Status.SUCCESS)
                    .validateRespCode(RespCode.SUCCESS)
                    .AssertAll()
        }
    }
}