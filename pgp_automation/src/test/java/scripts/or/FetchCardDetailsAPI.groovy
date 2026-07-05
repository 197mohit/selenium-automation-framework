package scripts.or

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.appconstants.Constants
import com.paytm.dto.PaymentDTO
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.Card
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.user.annotations.AUsers
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.*
import scripts.api.theia.fetchPayOptions.FetchPayOptionsV1Test
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV1Test

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_CARD_DETAILS
import static com.paytm.base.test.Group.Feature
import static com.paytm.base.test.Group.PUBLIC_API
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Test(groups = [Feature.OR, PUBLIC_API])
class FetchCardDetailsAPI extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_CARD_DETAILS)
                .addQueryParam('mid', m().id)
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                "head": [
                        "requestId"       : new Random().nextLong().abs() as String,
                        "requestTimestamp": System.currentTimeMillis() as String,
                        "channelId"       : 'WAP',
                        "version"         : 'v1',
                        "tokenType"       : 'SSO',
                        "token"           : user().tokens['sso'].id,
                ],
                "body": [
                        "mid"                  : m().id,
                        "cardNumber"           : cards.find { it.scheme == 'visa' }.no,//M
                        "savedCardId"          : null,//M
                        "eightDigitBinRequired": true,
                ]
        ]
    }

    private final ResponseSpecification respSpecSuccess = new ResponseSpecBuilder()
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", equalTo("S"))
            .expectBody("resultCode", equalTo("0000"))
            .expectBody("resultMsg", equalTo("Success"))
            .rootPath("body")
            .expectBody("cardHash", notNullValue())
            .build()

    private final ResponseSpecification respSpecInvalidRequestParams = new ResponseSpecBuilder()
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", equalTo("F"))
            .expectBody("resultCode", equalTo("1001"))
            .expectBody("resultMsg", equalTo("Request parameters are not valid"))
            .build()

    private final ResponseSpecification respSpecSystemError = new ResponseSpecBuilder()
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", equalTo("U"))
            .expectBody("resultCode", equalTo("00000900"))
            .expectBody("resultMsg", equalTo("System error"))
            .build()

    private final ResponseSpecification respSpecFailed = new ResponseSpecBuilder()
            .rootPath("body.resultInfo")
            .expectBody("resultStatus", equalTo("F"))
            .expectBody("resultCode", equalTo("0001"))
            .expectBody("resultMsg", equalTo("FAILED"))
            .build()

    @BeforeMethod(dependsOnMethods = ['setMerchant'])
    void executeFetchPayOptions(Method method, ITestResult testResult) {
        try {
            SSOTokenFetchPayOptionsV1Test fpo = new SSOTokenFetchPayOptionsV1Test()
            def root = fpo.root()
            root.head.token = user().tokens['sso'].id
            root.body.mid = m().id
            root.body.enablePaymentMode = null
            fpo.req().body(root).post()
                    .then().body('body.resultInfo.resultStatus', equalTo('S'))
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Parameters('invalidToken')
    @Test(description = 'assert Failure When Invalid Token Passed')
    void assertFailureWhenInvalidTokenPassed(@Optional('invalid') String invalidToken) {
        def root = root()
        root.head << [token: invalidToken]
        root.body.cardNumber = PaymentDTO.AMEX_CARD_NUMBER
        req().body(root).post()
                .then().spec(respSpecFailed)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When Blank Token Passed')
    void assertFailureWhenBlankTokenPassed() {
        def root = root()
        root.head << [token: ""]
        root.body.cardNumber = PaymentDTO.AMEX_CARD_NUMBER
        req().body(root).post()
                .then().spec(respSpecInvalidRequestParams)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When Token Not Passed')
    void assertFailureWhenTokenNotPassed() {
        def root = root()
        root.head << [token: null]
        root.body.cardNumber = PaymentDTO.AMEX_CARD_NUMBER
        req().body(root).post()
                .then().spec(respSpecInvalidRequestParams)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Parameters('invalidTokenType')
    @Test(description = 'assert Failure When Invalid Token Type Passed')
    void assertFailureWhenInvalidTokenTypePassed(@Optional('invalid') String invalidTokenType) {
        def root = root()
        root.head << [token: user().tokens['sso'].id, tokenType: invalidTokenType]
        root.body.cardNumber = PaymentDTO.AMEX_CARD_NUMBER
        req().body(root).post()
                .then().spec(respSpecSystemError)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When Blank Token Type Passed')
    void assertFailureWhenBlankTokenTypePassed() {
        def root = root()
        root.head << [token: user().tokens['sso'].id, tokenType: '']
        root.body.cardNumber = PaymentDTO.AMEX_CARD_NUMBER
        req().body(root).post()
                .then().spec(respSpecSystemError)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When Token Type Not Passed')
    void assertFailureWhenTokenTypeNotPassed() {
        def root = root()
        root.head << [token: user().tokens['sso'].id, tokenType: null]
        root.body.cardNumber = PaymentDTO.AMEX_CARD_NUMBER
        req().body(root).post()
                .then().spec(respSpecFailed)
    }

    @Merchant
    @AUser
    @Parameters('invalidMid')
    @Test(description = 'assert Failure When Invalid Mid Passed')
    void assertFailureWhenInvalidMidPassed(@Optional('hsakjdh2e2e') String invalidMid) {
        def root = root()
        root.head << [token: user().tokens['sso'].id]
        root.body << [mid: invalidMid, cardNumber: PaymentDTO.AMEX_CARD_NUMBER]
        given(reqBldr().removeQueryParam('mid').build()).queryParam('mid', invalidMid).body(root).post()
                .then().spec(respSpecFailed)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When mid not Passed')
    void assertFailureWhenMidNotPassed() {
        def root = root()
        root.head << [token: user().tokens['sso'].id]
        root.body << [mid: null, cardNumber: PaymentDTO.AMEX_CARD_NUMBER]
        req().body(root).post()
                .then()
                .root("body.resultInfo")
                .body("resultStatus", equalTo("F"),
                "resultCode", equalTo("2013"),
                "resultMsg", equalTo("Mid in the query param doesn't match with the Mid send in the request"))
    }

//    @Merchant
//    @AUser
//    @Parameters('cardNumberWithNonExistentBIN')
//    @Test(description = 'assert Failure When Invalid Mid Passed', enabled = false)
    void assertFailureWhenCardWithNonExistentBINPassed(String cardNumberWithNonExistentBIN) { //TODO discuss with Mansi
        def root = root()
        root.head << [token: user().tokens['sso'].id]
        root.body.cardNumber = cardNumberWithNonExistentBIN
        req().body(root).post()
                .thenReturn()
    }

    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test(description = 'assert Valid Response For Different Card Types', dataProvider = "CardDetails")
    void assertValidResponseForDifferentCardTypes(Card card, String channelName, String channelCode, String cnMin, String cnMax, String cvvL) {
        def root = root()
        root.head << [token: user().tokens['sso'].id]
        root.body.cardNumber = card.no
        req().body(root).post()
                .then()
                .spec(respSpecSuccess)
                .root("body.cardDetails.binDetail")
                .body("bin", anyOf(equalTo(card.no.substring(0, 8)), equalTo(card.eightDigitBinHash)),
                "channelName", equalTo(channelName),
                "channelCode", equalTo(channelCode),
                "cnMin", equalTo(cnMin),
                "cnMax", equalTo(cnMax),
                "cvvL", equalTo(cvvL))
    }

    @DataProvider(name = "CardDetails")
    Object[][] CardDetails() {
        [
                [cards.find { it.scheme == 'amex' }, "AMEX", "AMEX", "15", "15", "4"],
                [cards.find { it.scheme == 'maestro' }, "MAESTRO", "MAESTRO", "16", "19", "0"],
                [cards.find { it.scheme == 'master' }, "MASTER", "MASTER", "16", "16", "3"],
                [cards.find { it.scheme == 'visa' }, "VISA", "VISA", "13", "16", "3"],
                [cards.find { it.scheme == 'diners' }, "DINERS", "DINERS", "14", "14", "3"],
                [cards.find { it.scheme == 'rupay' }, "RUPAY", "RUPAY", "16", "16", "3"],
                [cards.find { it.scheme == 'bajajfn' }, "BAJAJFN", "BAJAJFN", "16", "16", "0"],
        ]
    }

    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser(edit = true)
    @Test(description = 'assert Valid Response For Different Saved Card Types', dataProvider = "CardDetails")
    void assertValidResponseForDifferentSavedCardTypes(Card card, String channelName, String channelCode, String cnMin, String cnMax, String cvvL) {
        def root = root()
        user().savedCards.clear()
        if (!user().savedCards.add(card)) throw new SkipException('unable to add card')
        root.head << [token: user().tokens['sso'].id]
        root.body.cardNumber = null
        root.body.savedCardId = user().savedCards[0].id
        req().body(root).post()
                .then()
                .spec(respSpecSuccess)
                .root("body.cardDetails.binDetail")
                .body("bin", anyOf(equalTo(card.no.substring(0, 8)), equalTo(card.eightDigitBinHash)),
                "channelName", equalTo(channelName),
                "channelCode", equalTo(channelCode),
                "cnMin", equalTo(cnMin),
                "cnMax", equalTo(cnMax),
                "cvvL", equalTo(cvvL))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When Empty Card Number Passed')
    void assertFailureWhenEmptyCardNumberPassed() {
        def root = root()
        root.body.cardNumber = ''
        root.body.savedCardId = null
        req().body(root).post()
                .then().spec(respSpecInvalidRequestParams)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When Card Number And Saved Card Id Not Passed')
    void assertFailureWhenCardNumberAndSavedCardIdNotPassed() {
        def root = root()
        root.body.removeAll { it.key in ['cardNumber', 'savedCardId'] }
        req().body(root).post()
                .then().spec(respSpecInvalidRequestParams)
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Failure When Blank Saved Card Id Passed')
    void assertFailureWhenBlankSavedCardIdPassed() {
        def root = root()
        root.body.savedCardId = ''
        root.body.cardNumber = null
        req().body(root).post()
                .then().spec(respSpecInvalidRequestParams)
    }

    @Merchant({it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @AUser(edit = true)
    @Test(description = 'assert Response When Both Card Number And Saved Card Id Passed')
    void assertResponseWhenBothCardNumberAndSavedCardIdPassed() {
        def root = root()
        //user().savedCards.clear()
       // assert user().savedCards.add(cards.find { it.scheme == 'rupay' })
        User user = new User(user().getMobile(),user().getPassword());



        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.RUPAY_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        root.body << [cardNumber: cards.find { it.scheme == 'visa' }.no, savedCardId: user().savedCards[0].id]
        req().body(root).post()
                .then()
                .spec(respSpecSuccess)
                .body("body.cardDetails.binDetail.channelCode", equalTo("RUPAY"))
    }

    @Merchant
    @AUsers([@AUser(edit = true), @AUser(edit = true)])
    @Test(description = 'assert Failure When Saved Card Id Of Another User Passed')
    void assertFailureWhenSavedCardIdOfAnotherUserPassed() {
        def root = root()
        user(0).savedCards.clear()
        user(1).savedCards.clear()
        if (!user(1).savedCards.add(cards.find())) throw new SkipException('unable to add card')
        root.body.savedCardId = user(1).savedCards[0].id
        root.body.cardNumber = null
        req().body(root).post()
                .then().spec(respSpecFailed)
    }

    @Merchant
    @AUser(edit = true)
    @Test(description = 'assert Success When Saved Card Id Passed')
    void assertSuccessWhenSavedCardIdPassed() {
        def root = root()
        user().savedCards.clear()
        if (!user().savedCards.add(cards.find())) throw new SkipException('unable to add card')
        root.body.savedCardId = user().savedCards[0].id
        root.body.cardNumber = null
        req().body(root).post()
                .then().spec(respSpecSuccess)
    }

    @Merchant
    @AUser
    @Test(description = 'assert Bin Length 6 When Eight Digit Bin Required False')
    void assertBinLength6WhenEightDigitBinRequiredFalse() {
        def root = root()
        def card = cards.find()
        root.body << [cardNumber: card.no, eightDigitBinRequired: false]
        req().body(root).post()
                .then()
                .spec(respSpecSuccess)
                .body("body.cardDetails.binDetail.bin", anyOf(equalTo(card.no[0..5]), equalTo(card.eightDigitBinHash)))
    }

    @Merchant
    @AUser
    @Test(description = 'assert Bin Length 8 When Eight Digit Bin Required True')
    void assertBinLength8WhenEightDigitBinRequiredTrue() {
        def root = root()
        def card = cards.find()
        root.body << [cardNumber: card.no, eightDigitBinRequired: true]
        req().body(root).post()
                .then()
                .spec(respSpecSuccess)
                .body("body.cardDetails.binDetail.bin", anyOf(equalTo(card.no.substring(0, 8)), equalTo(card.eightDigitBinHash)))
    }

//    @Parameters('invalidBinCardNumber')
//    @Merchant
//    @AUser
//    @Test(description = 'assert Bin Not Valid Resp When Invalid Bin Provided', enabled = false)
    void assertBinNotValidRespWhenInvalidBinProvided(@Optional('0000300033334444') String invalidBinCardNumber) {
//TODO provide invalid bin
        def root = root()
        root.body << [cardNumber: invalidBinCardNumber, eightDigitBinRequired: false]
        req().body(root).post()
                .then()
                .root("body.resultInfo")
                .body("resultStatus", equalTo("F"),
                "resultCode", equalTo("1003"),
                "resultMsg", equalTo("Bin number is not valid"))
    }

    @Parameters('lowSuccessRateCardNumber')
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test(description = 'assert Correct Resp When Low Success Rate Card Provided')
    void assertCorrectRespWhenLowSuccessRateCardProvided(@Optional(PaymentDTO.LOW_SUCCESS_RATE_CARD_NUMBER) String lowSuccessRateCardNumber) {
        def root = root()
        root.body << [cardNumber: lowSuccessRateCardNumber, eightDigitBinRequired: false]
        req().body(root).post()
                .then()
                .spec(respSpecSuccess)
                .root('body.cardDetails')
                .body('', hasKey('hasLowSuccessRate'))
                .body('hasLowSuccessRate.status', equalTo("true"))
    }

}
