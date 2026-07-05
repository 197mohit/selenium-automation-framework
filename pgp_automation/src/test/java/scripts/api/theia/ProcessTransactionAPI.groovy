package scripts.api.theia

import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.apphelpers.PostpaidHelpers
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.dto.PaymentDTO
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.Card
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.SkipException
import org.testng.annotations.Test
import scripts.api.theia.applyPromo.SSOTokenApplyPromoV1Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.PROCESS_TXN
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Link('https://wiki.mypaytm.com/display/PGP/Native+API+Contract+Details+II#NativeAPIContractDetailsII-/api/v1/processTransaction?mid=<mid>&orderId=<orderId>')
class ProcessTransactionAPI extends TestSetUp {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    final def req = { orderId = null ->
        given(
                new RequestSpecBuilder()
                        .addFilter(setQueryParamOrderIdFilter)
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(PROCESS_TXN)
                        .addQueryParams([mid: m().id, orderId: orderId ?: '?'])
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        txnToken : {
                            assert m().orders.add(new OrderV2(2, user()?.tokens?.find {
                                it.name == 'sso'
                            }?.id, m().users.find().id))
                            m().orders.last().transaction.token
                        }(),
                ],
                body: [
                        mid        : m().id,
                        orderId    : m().orders.last().id,
                        paymentMode: null,//M
                        channelCode: null,//O
                        custId     : UUID.randomUUID() as String,
                        cardInfo   : null,//O
                        paymentFlow: null,//O
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

    def setQueryParamOrderIdFilter = [filter: { req, res, ctx ->
        if (req.getQueryParams()['orderId'] == '?') {
            req.removeQueryParam('orderId').queryParam('orderId', new JsonSlurper().parseText(req.getBody())?.body?.orderId ?: new Random().nextLong().abs() as String)
        }
        ctx.next(req, res)
    }] as Filter

    final ResponseSpecification success = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('S'))
            .expectBody('resultCode', equalTo('0000'))
            .expectBody('resultMsg', equalTo('Success'))
            .build()

    final SSOTokenApplyPromoV1Test ap = new SSOTokenApplyPromoV1Test()

    @AUser(edit = true)
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.PGOnly_Retry.getId()})
    @Test(description = 'test successful res for ppi')
    void testPaymentByPPI() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'BALANCE']
        ap.req().body(apRoot).post().then().spec(ap.success)
        user().wallets['main'].balance = m().orders.last().amt
        root.body.paymentMode = 'BALANCE'
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @AUser(edit = true, ppbl = 'true')
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly_Retry.getId()})
    @Test(description = 'test successful res for ppbl')
//TODO need to debug why it is failing
    void testPaymentByPPBL() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'PPBL']
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'PPBL', mpin: new PaymentDTO().passcode]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    //TODO, never got passed
    @AUser(edit = true, paytmcc = 'true')
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = 'test successful res for pdc')
    void testPaymentByPDC() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'PAYTM_DIGITAL_CREDIT']
        ap.req().body(apRoot).post().then().spec(ap.success)
        PostpaidHelpers.updateBalance('2')
        root.body << [paymentMode: 'PAYTM_DIGITAL_CREDIT', mpin: new PaymentDTO().passcode]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @AUser
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId()})
    @Test(description = 'test successful res for dc')
    void testPaymentsByDC() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        Card card = cards.find {
            it.successRate == 'high' && it.type == 'debit' && !it.prepaid
        }.tap { assert it }
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'DEBIT_CARD', cardNo: card.no]
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'DEBIT_CARD', cardInfo: "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @AUser
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly_Retry.getId()  })
    @Test(description = 'test successful res for cc')
    void testPaymentsByCC() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'CREDIT_CARD', cardNo: card.no]
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'CREDIT_CARD', cardInfo: "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }


    @AUser
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test successful res for nb')
    void testPaymentsByNB() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'NET_BANKING', bankCode: 'ICICI']
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'NET_BANKING', channelCode: 'ICICI']
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @AUser(edit = true)
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = 'test successful res for saved dc')
    void testPaymentsBySavedDC() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        //user().savedCards.clear()
        Card card = cards.find {
            it.successRate == 'high' && it.type == 'debit' && !it.prepaid
        }
        SavedCardHelpers.addCard(user, card.expMo, card.expYr, card.no);
        //assert user().savedCards.add(card)
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'DEBIT_CARD', savedCardId: user().savedCards.find().id]
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'DEBIT_CARD', cardInfo: "${user().savedCards.find().id}||$card.cvv|" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @AUser(edit = true)
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test successful res for saved cc')
    void testPaymentsBySavedCC() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
       // user().savedCards.clear()
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        //assert user().savedCards.add(card)
        SavedCardHelpers.addCard(user, card.expMo, card.expYr, card.no);
        //PaymentDTO paymentDTO = new PaymentDTO();
        //paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        //SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());


        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'CREDIT_CARD', savedCardId: user().savedCards.find().id]
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'CREDIT_CARD', cardInfo: "${user().savedCards.find().id}||$card.cvv|" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @AUser(edit = true)
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @Test(description = 'test successful res for addnpay req by cc')
    void testAddnPayPaymentByCC() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        user().wallets['main'].balance = 1D
        Card card = cards.find {
            it.successRate == 'high' && it.type == 'credit' && it.scheme == 'visa'
        }.tap { assert it }
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'CREDIT_CARD', cardNo: card.no]
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'CREDIT_CARD', cardInfo: "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String, paymentFlow: 'ADDANDPAY']
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @AUser(edit = true)
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.NATIVE_PROMO_HYBRID.getId()})
    @Test(description = 'test successful res for hybrid req by cc')
    void testHybridPaymentByCC() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        user().wallets['main'].balance = 1D
        Card card = cards.find {
            it.successRate == 'high' && it.type == 'credit' && it.scheme == 'visa'
        }.tap { assert it }
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'CREDIT_CARD', cardNo: card.no]
        ap.req().body(apRoot).post().then().spec(ap.success)
        root.body << [paymentMode: 'CREDIT_CARD', cardInfo: "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String, paymentFlow: 'HYBRID']
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

 /*   @Epic(Constants.Sprint.SPRINT_THEMATIC)
    @Feature("PGP-18661")
    @AUser(edit = true)
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(enabled = false)

  */
    void 'test order is successful when payment is done by saved cc using CIN'() {
        def root = root()
        //(user() as AlipayUser).savedCards.clear()
        //def card = cards.find { it.type == 'credit' && !(it.scheme in ['amex', 'bajajfn']) }
       // assert (user() as AlipayUser).savedCards.add(card)
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        FetchCardIndexNumber fetchCardIndexNumberApi = new FetchCardIndexNumber()
        def fetchCardIndexNumberRoot = fetchCardIndexNumberApi.root()
        fetchCardIndexNumberRoot.body.cardNumber = card.no
        def cardIdxNo = fetchCardIndexNumberApi.req().body(fetchCardIndexNumberRoot).post().path('body.cardIndexNumber')
        if (!cardIdxNo) throw new SkipException('unable to fetch card index no.')
        root.body << [paymentMode: 'CREDIT_CARD', cardInfo: "$cardIdxNo||$card.cvv|" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

 /*   @Epic(Constants.Sprint.SPRINT_THEMATIC)
    @Feature("PGP-18661")
    @AUser(edit = true)
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(enabled=false) */
    void 'test retried order is successful when payment is done by saved cc using CIN'() {
        def root = root()
        //(user() as AlipayUser).savedCards.clear()
        //def card = cards.find { it.type == 'credit' && !(it.scheme in ['amex', 'bajajfn']) }
        //assert (user() as AlipayUser).savedCards.add(card)
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        FetchCardIndexNumber fetchCardIndexNumberApi = new FetchCardIndexNumber()
        def fetchCardIndexNumberRoot = fetchCardIndexNumberApi.root()
        fetchCardIndexNumberRoot.body.cardNumber = card.no
        def cardIdxNo = fetchCardIndexNumberApi.req().body(fetchCardIndexNumberRoot).post().path('body.cardIndexNumber')
        if (!cardIdxNo) throw new SkipException('unable to fetch card index no.')
        root.body << [paymentMode: 'CREDIT_CARD', cardInfo: "|${PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN}|$card.cvv|${card.with { expMo + expYr }}" as String]
        req().body(root).post().then()
                .body('body.txnInfo.STATUS', equalTo('TXN_FAILURE'))
        root.body << [paymentMode: 'CREDIT_CARD', cardInfo: "$cardIdxNo||$card.cvv|" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

  /*  @Epic(Constants.Sprint.SPRINT_THEMATIC)
    @Feature("PGP-18661")
    @AUser(edit = true)
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(enabled = false)

   */
    void 'test order is successful when payment is done by saved dc using CIN'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        def card = cards.find { it.type == 'debit' && !(it.scheme in ['amex', 'bajajfn']) }
        assert (user() as AlipayUser).savedCards.add(card)
        FetchCardIndexNumber fetchCardIndexNumberApi = new FetchCardIndexNumber()
        def fetchCardIndexNumberRoot = fetchCardIndexNumberApi.root()
        fetchCardIndexNumberRoot.body.cardNumber = card.no
        def cardIdxNo = fetchCardIndexNumberApi.req().body(fetchCardIndexNumberRoot).post().path('body.cardIndexNumber')
        if (!cardIdxNo) throw new SkipException('unable to fetch card index no.')
        root.body << [paymentMode: 'DEBIT_CARD', cardInfo: "$cardIdxNo||$card.cvv|" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

/*    @Epic(Constants.Sprint.SPRINT_THEMATIC)
    @Feature("PGP-18661")
    @AUser(edit = true)
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('dc') && it.retryCount > 0 && it.acquirings.any { it.payMode == 'dc' && !(it.bank in ['amex', 'bajajfn']) } })
    @Test(enabled=false) */
    void 'test retried order is successful when payment is done by saved dc using CIN'() {
        def root = root()
        //(user() as AlipayUser).savedCards.clear()
        //def card = cards.find { it.type == 'debit' && !(it.scheme in ['amex', 'bajajfn']) }
        //assert (user() as AlipayUser).savedCards.add(card)
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        FetchCardIndexNumber fetchCardIndexNumberApi = new FetchCardIndexNumber()
        def fetchCardIndexNumberRoot = fetchCardIndexNumberApi.root()
        fetchCardIndexNumberRoot.body.cardNumber = card.no
        def cardIdxNo = fetchCardIndexNumberApi.req().body(fetchCardIndexNumberRoot).post().path('body.cardIndexNumber')
        if (!cardIdxNo) throw new SkipException('unable to fetch card index no.')
        root.body << [paymentMode: 'DEBIT_CARD', cardInfo: "|${PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN}|$card.cvv|${card.with { expMo + expYr }}" as String]
        req().body(root).post().then()
                .body('body.txnInfo.STATUS', equalTo('TXN_FAILURE'))
        root.body << [paymentMode: 'DEBIT_CARD', cardInfo: "$cardIdxNo||$card.cvv|" as String]
        req().body(root).post().then()
                .spec(success)
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @Merchant(edit = true)
    @Test(groups = "Security")
    void testwithPutMethodType() {
        def root = root()
        req().body(root).put().then().statusCode(405);
    }

    @Merchant(edit = true)
    @Test(groups = "Security")
    void testwithDeleteMethodType() {
        def root = root()
        req().body(root).delete().then().statusCode(405);
    }

    @Merchant(edit = true,value={it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(groups = "Security")
    void testwithIncorrectContentType() {
        def root = root(), apRoot = ap.root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        apRoot.body.totalTransactionAmount = '2'
        apRoot.body.paymentOptions << [transactionAmount: '2', payMethod: 'BALANCE']
        ap.req().body(apRoot).post().then().spec(ap.success)
        user().wallets['main'].balance = m().orders.last().amt
        root.body.paymentMode = 'BALANCE'
        given(reqBldr().setContentType(ContentType.URLENC)).body(root).post().then().statusCode(405);
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId()})
    @AUser(edit = true)
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        user().wallets['main'].balance = m().orders.last().amt
        root.body.paymentMode = 'BALANCE'
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(success)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.Hybrid.getId() })
    @AUser(edit = true)
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        user().wallets['main'].balance = m().orders.last().amt
        root.body.paymentMode = 'BALANCE'
        req().body(root).post().then()
                .spec(success)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.Hybrid.getId() })
    @AUser(edit = true)
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        user().wallets['main'].balance = m().orders.last().amt
        root.body.paymentMode = 'BALANCE'
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(success)
                .body('body.resultInfo', hasKey('resultMsgRegional'))
    }
}
