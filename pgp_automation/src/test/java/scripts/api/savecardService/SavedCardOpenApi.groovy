package scripts.api.savecardService

import com.paytm.api.saveCard.SavedCardApi
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.CommonHelpers
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.dto.saveCard.SaveCardResponseBase
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.user.Card
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.util.PGPUtil
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.BeforeTest
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.savedCard.GET_PG_SAVEDCARD_ON_CUSTID_MID
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Ankur")
//TODO: For now commented new user() due to issue at suite.xml
class SavedCardOpenApi extends TestSetUp {

    ThreadLocal<Constants.MerchantType> mid = new ThreadLocal<Constants.MerchantType>(){
        @Override
        protected Constants.MerchantType initialValue() {
            return Constants.MerchantType.ALLPAYMODE;
        }
    };


    SavedCardHelpers sch = new SavedCardHelpers()
    private static String GET_SAVEDCARD_CUSTID_MID_CHECKSUM = "/savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum/{custId}/{mid}/{checksum}"
//    private static String PG_GET_SAVEDCARD_CUSTID_MID = "/savedcardservice/savedCardService/pg/get/savedcard/custId/mId/{custid}/{mid}"


    private RequestSpecification reqSpec1 = new RequestSpecBuilder()
            .addRequestSpecification(reqSpec())
            .setContentType(ContentType.JSON)
            .setBaseUri(PGP_HOST)
            .build()

    @BeforeTest
    void setMerchant() {
        mid.set(Constants.MerchantType.ALLPAYMODE)
    }

    def MERCHANT_V1_GET_SAVEDCARD_BODY() {
        [
                'CUSTID'      : null,
//                'SSO_TOKEN'   : null,
                'MID'         : null,
                'REQUEST_TYPE': null
        ]
    }

    @DataProvider(name = "CardTypes")
    Object[][] d1() {
        [
                ['5166400031031058', '01', '2030', 'DEBIT_CARD', 'MASTER'],
                ['6799990100000000019', '01', '2030', 'DEBIT_CARD', 'MAESTRO'],
                ['379863297651006', '01', '2030', 'CREDIT_CARD', 'AMEX'],
                ['6073180505920479', '01', '2030', 'DEBIT_CARD', 'RUPAY'],
                ['30569309025904', '01', '2030', 'CREDIT_CARD', 'DINERS'],
//                    ['2030400200341578', '01', '2030', 'CREDIT_CARD', 'BAJAJFN'], //TODO: Acquiring needs to be apply for BAJAJFM on mid
                ['4012888888881881', '01', '2030', 'CREDIT_CARD', 'VISA']
        ]
    }

//    @AUser(edit = true)
    //@Deprecated
    //@Test(enabled = false, dataProvider = "CardTypes", description = "Verify /savedcardservice/merchant/v1/get/card API for different card types on mid/custid")
    void t1(String cardNo, String expMon, String expyear, String cardType, String cardScheme) {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        String custId = CommonHelpers.generateOrderId()

        root.CUSTID = custId
        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        String checksum = PGPUtil.getChecksum(mid.get().key, (root as TreeMap<String, String>))
        root.CHECKSUM = checksum

        String cardFirstSixDigits = cardNo.substring(0, 6)
        String cardLastFourDigits = cardNo.substring(cardNo.length() - 4)

        sch.saveCard_custId_mId(cardNo, custId, mid.get().id, expMon + expyear)

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", not(empty()),
                        "response.savedCardId", everyItem(notNullValue()),
                        "response.cardFirstSixDigits", hasItem(cardFirstSixDigits),
                        "response.cardLastFourDigits", hasItem(cardLastFourDigits),
                        "response.cardType", hasItem(cardType),
                        "response.issuerDisplayName", everyItem(notNullValue()),
                        "response.issuerCode", everyItem(not(empty())),
                        "response.cardScheme", hasItem(cardScheme))
    }

    @DataProvider(name = "CardType_ssoToken")
    Object[][] d2() {
        [
                [cards.find { it.scheme == 'amex' }, "AMEX"],
                [cards.find { it.scheme == 'maestro' }, "MAESTRO"],
                [cards.find { it.scheme == 'master' }, "MASTER"],
                [cards.find { it.scheme == 'visa' }, "VISA"],
                [cards.find { it.scheme == 'diners' }, "DINERS"],
                [cards.find { it.scheme == 'rupay' }, "RUPAY"]
        ]
    }

//    @AUser(edit = true)
    @Test(dataProvider = "CardType_ssoToken", description = "Verify /savedcardservice/merchant/v1/get/card API for different card types on userId")
    void t2(Card card, String scheme) {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()
        String custId = CommonHelpers.generateOrderId()

        user.savedCards.add(card)

        root.CUSTID = custId
        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        String checksum = PGPUtil.getChecksum(mid.get().key, (root as TreeMap<String, String>))
        root.CHECKSUM = checksum

        String cardFirstSixDigits = card.no.substring(0, 6)
        String cardLastFourDigits = card.no.substring(card.no.length() - 4)

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", not(empty()),
                        "response.savedCardId", everyItem(notNullValue()),
                        "response.cardFirstSixDigits", hasItem(cardFirstSixDigits),
                        "response.cardLastFourDigits", hasItem(cardLastFourDigits),
                        "response.issuerDisplayName", everyItem(notNullValue()),
                        "response.issuerCode", everyItem(not(empty())),
                        "response.cardScheme", hasItem(scheme))
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API when no card is saved on sso')
    void t3() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()
        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        String checksum = PGPUtil.getChecksum(mid.get().key, ((root as TreeMap<String, String>)).findAll {
            it.value != null
        })
        root.CHECKSUM = checksum

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("204"),
                        "codeDetail", equalToIgnoringCase("Card does not exist for given parameters"),
                        "response", empty())
    }


//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API when no card is saved on sso')
    void t4() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()
        String custId = CommonHelpers.generateOrderId()

        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        root.CUSTID = custId
        String checksum = PGPUtil.getChecksum(mid.get().key, ((root as TreeMap<String, String>)).findAll {
            it.value != null
        })
        root.CHECKSUM = checksum

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("SUCCESS"),
                        "response", empty())
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API when same card saved on mid_custid and userId')
    void t5_1() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        String custId = CommonHelpers.generateOrderId()
        root.CUSTID = custId
        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        String checksum = PGPUtil.getChecksum(mid.get().key, (root as TreeMap<String, String>))
        root.CHECKSUM = checksum

        Card materCard = cards.find { it.scheme == 'master' }

        user.savedCards.clear();

        //card on user Id
        user.savedCards.add(materCard)
        String cardFirstSixDigits2 = materCard.no.substring(0, 6)
        String cardLastFourDigits2 = materCard.no.substring(materCard.no.length() - 4)

        //card on mid
        sch.saveCard_custId_mId(materCard.no, custId, mid.get().id, materCard.expMo + materCard.expYr)

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", hasSize(1),
                        "response.savedCardId", everyItem(notNullValue()),
                        "response.cardFirstSixDigits", hasItem(cardFirstSixDigits2),
                        "response.cardLastFourDigits", hasItem(cardLastFourDigits2))
    }

//    @AUser(edit = true)
   // @Deprecated
  //  @Test(enabled = false, description = 'Verify /savedcardservice/merchant/v1/get/card API when card is saved on mid/custId and user id')
    void t5() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear();
        String custId = CommonHelpers.generateOrderId()

        root.CUSTID = custId
        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        String checksum = PGPUtil.getChecksum(mid.get().key, (root as TreeMap<String, String>))
        root.CHECKSUM = checksum

        //card saved on mid
        Card amexCard = cards.find { it.scheme == 'amex' }
        String cardFirstSixDigits1 = amexCard.no.substring(0, 6)
        String cardLastFourDigits1 = amexCard.no.substring(amexCard.no.length() - 4)
        sch.saveCard_custId_mId(amexCard.no, custId, mid.get().id, amexCard.expMo + amexCard.expYr)

        //card saved on user id
        Card materCard = cards.find { it.scheme == 'master' }
        user.savedCards.add(materCard)
        String cardFirstSixDigits2 = materCard.no.substring(0, 6)
        String cardLastFourDigits2 = materCard.no.substring(materCard.no.length() - 4)

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", hasSize(2),
                        "response.savedCardId", everyItem(notNullValue()),
                        "response.cardFirstSixDigits", hasItems(cardFirstSixDigits1, cardFirstSixDigits2),
                        "response.cardLastFourDigits", hasItems(cardLastFourDigits1, cardLastFourDigits2))
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API when cust id and user id are passed blank')
    void t6() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()

        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        String checksum = PGPUtil.getChecksum(mid.get().key, ((root as TreeMap<String, String>)).findAll {
            it.value != null
        })
        root.CHECKSUM = checksum

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("SUCCESS"),
                        "response", empty())
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API mid is not passed')
    void t7() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()

        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        root.REQUEST_TYPE = 'DEFAULT'
        String checksum = PGPUtil.getChecksum(mid.get().key, ((root as TreeMap<String, String>)).findAll {
            it.value != null
        })
        root.CHECKSUM = checksum

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("FAILURE"),
                        "httpCode", equalToIgnoringCase("500"),
                        "httpSubCode", equalToIgnoringCase("500"),
                        "codeDetail", equalToIgnoringCase("System Error"),
                        "response", equalToIgnoringCase('Input param mid or checksum missing'))
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API checksum is not passed')
    void t8() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()

        root.MID = mid.get().id
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        root.REQUEST_TYPE = 'DEFAULT'

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("FAILURE"),
                        "httpCode", equalToIgnoringCase("500"),
                        "httpSubCode", equalToIgnoringCase("500"),
                        "codeDetail", equalToIgnoringCase("System Error"),
                        "response", equalToIgnoringCase('Input param mid or checksum missing'))
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API checksum is invalid')
    void t9() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()

        root.MID = mid.get().id
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        root.REQUEST_TYPE = 'DEFAULT'
        root.CHECKSUM = '1234'

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("FAILURE"),
                        "httpCode", equalToIgnoringCase("500"),
                        "httpSubCode", equalToIgnoringCase("500"),
                        "codeDetail", equalToIgnoringCase("System Error"),
                        "response", equalToIgnoringCase('Invalid checksum'))
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API when debit card is not applied on MID')
    void t10() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()
        Card debitCard = cards.find { it.type == 'debit' }
        user.savedCards.add(debitCard)

        Constants.MerchantType merchant = Constants.MerchantType.WalletOnly

        root.MID = merchant.id
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        root.REQUEST_TYPE = 'DEFAULT'
        String checksum = PGPUtil.getChecksum(merchant.key, ((root as TreeMap<String, String>)).findAll {
            it.value != null
        })
        root.CHECKSUM = checksum

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", empty())
    }

    @DataProvider(name = "CardTypes_MID_CUSTID")
    Object[][] d3() {
        [
                ['5166400031031058', '01', '2030', 'DC'],
                ['6799990100000000019', '01', '2030', 'DC'],
                ['379863297651006', '01', '2030', 'CC'],
                ['6073180505920479', '01', '2030', 'DC'],
                ['30569309025904', '01', '2030', 'CC'],
                ['2030400200341578', '01', '2030', 'CC'],
                ['4012888888881881', '01', '2030', 'CC']
        ]
    }

    @Test(dataProvider = "CardTypes_MID_CUSTID", description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum API for different card types on mid/custid")
    void t11(String cardNo, String expMon, String expyear, String cardType) {
        String custId = CommonHelpers.generateOrderId()

        String cardFirstSixDigits = cardNo.substring(0, 6)
        String cardLastFourDigits = cardNo.substring(cardNo.length() - 4)

        sch.saveCard_custId_mId(cardNo, custId, mid.get().id, expMon + expyear)

        SavedCardApi.getSaveCard_byMid_custId(mid.get().id, mid.get().key, custId)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase('Success'),
                        "response", not(empty()),
                        "response.cardId", everyItem(notNullValue()),
                        "response.cardNumber", everyItem(containsString(cardFirstSixDigits)),
                        "response.cardNumber", everyItem(containsString(cardLastFourDigits)),
                        "response.cardScheme", hasItem(cardType))

    }

    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum API for expiry card")
    void t12() {

        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        SaveCardResponseBase obj = sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(String.valueOf(obj.response),mid.get().id,custId)

        SavedCardApi.getSaveCard_byMid_custId(mid.get().id, mid.get().key, custId)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("204"),
                        "codeDetail", equalToIgnoringCase('Card does not exist for given parameters'),
                        "response", empty())
    }

    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum API when no card saved")
    void t13() {

        String custId = CommonHelpers.generateOrderId()

        SavedCardApi.getSaveCard_byMid_custId(mid.get().id, mid.get().key, custId)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("204"),
                        "codeDetail", equalToIgnoringCase('Card does not exist for given parameters'),
                        "response", empty())
    }

//    @AUser(edit = true)
    @Test(dataProvider = "CardType_ssoToken", description = "Verify //  savedcardservice/savedcardOpenAPIService/get/savecard/ssoToken/ API for different card types on userId")
    void t14(Card card, String scheme) {
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()

        user.savedCards.add(card)
        String cardFirstSixDigits = card.no.substring(0, 6)
        String cardLastFourDigits = card.no.substring(card.no.length() - 4)

        String SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id

        SavedCardApi.getSavedCard_bySsotoken(SSO_TOKEN)
                .then()
                .body('responseStatus', equalToIgnoringCase('SUCCESS'),
                        'httpCode', equalToIgnoringCase('200'),
                        'httpSubCode', equalToIgnoringCase('200'),
                        'codeDetail', equalToIgnoringCase('Success'),
                        'response', not(empty()),
                        'response.cardId', everyItem(notNullValue()),
                        'response.cardNumber', hasItem(containsString(cardFirstSixDigits)),
                        'response.cardNumber', hasItem(containsString(cardLastFourDigits)),
                        'response.cardNumber', hasItem(containsString('XXXXXX')))
    }

//    @AUser(edit = true)
    @Test(description = "Verify //  savedcardservice/savedcardOpenAPIService/get/savecard/ssoToken/ API when no card saved on userId")
    void t15() {
        User user = userManager.getForWrite(Label.BASIC)
                .tap { SavedCardHelpers.deleteSavedCard(it) }
                .with { new User(it.mobNo(), it.password(), true) }

        String SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id

        SavedCardApi.getSavedCard_bySsotoken(SSO_TOKEN)
                .then()
                .body('responseStatus', equalToIgnoringCase('SUCCESS'),
                        'httpCode', equalToIgnoringCase('200'),
                        'httpSubCode', equalToIgnoringCase('204'),
                        'codeDetail', equalToIgnoringCase('Card does not exist for given parameters'),
                        'response', empty())
    }

//    @AUser(edit = true)
    @Test(description = 'Verify /savedcardservice/merchant/v1/get/card API when same card is saved on mid/custId and user id')
    void t16() {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }
        user.savedCards.clear();
        String custId = CommonHelpers.generateOrderId()

        root.CUSTID = custId
        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        root.SSO_TOKEN = user.tokens.find { it.name == 'sso' }.id
        String checksum = PGPUtil.getChecksum(mid.get().key, (root as TreeMap<String, String>))
        root.CHECKSUM = checksum

        //card saved on mid
        Card masterCard = cards.find { it.scheme == 'master' }
        sch.saveCard_custId_mId(masterCard.no, custId, mid.get().id, masterCard.expMo + masterCard.expYr)

        //card saved on user id
        user.savedCards.add(masterCard)

        SavedCardApi.getSavedCard(root)
                .then()
                .body('responseStatus', equalToIgnoringCase('SUCCESS'),
                        'httpCode', equalToIgnoringCase('200'),
                        'httpSubCode', equalToIgnoringCase('200'),
                        'codeDetail', equalToIgnoringCase('Success'),
                        'response', hasSize(1),
                        'response.savedCardId', everyItem(notNullValue()))
    }


    @Test(description = "Verify //  savedcardservice/savedcardOpenAPIService/get/savecard/ssoToken/ API when sso is not passed")
    void t17() {

        SavedCardApi.getSavedCard_bySsotoken("")
                .then()
                .defaultParser(Parser.JSON)
                .body('responseStatus', equalToIgnoringCase('FAILURE'),
                        'httpCode', equalToIgnoringCase('500'),
                        'httpSubCode', equalToIgnoringCase('500'),
                        'codeDetail', equalToIgnoringCase('System Error'),
                        'response', equalToIgnoringCase('System Error'))
    }

    @Test(description = "Verify //  savedcardservice/savedcardOpenAPIService/get/savecard/ssoToken/ API when sso passed is invalid")
    void t18() {

        SavedCardApi.getSavedCard_bySsotoken("122312423523453423423542")
                .then()
                .body("responseStatus", equalToIgnoringCase("FAILURE"),
                        "httpCode", equalToIgnoringCase("500"),
                        "httpSubCode", equalToIgnoringCase("500"),
                        "codeDetail", equalToIgnoringCase("System Error"));
    }

    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum when checksum is blank")
    void t19() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)
        String basePath = GET_SAVEDCARD_CUSTID_MID_CHECKSUM.replace("{custId}", custId)
                .replace("{mid}", mid.get().id).replace("{checksum}", "")
        given().spec(reqSpec1).basePath(basePath).get()
                .then()
                .body("responseStatus", equalToIgnoringWhiteSpace("FAILURE"))
                .body("httpCode", equalToIgnoringWhiteSpace("500"))
                .body("httpSubCode", equalToIgnoringWhiteSpace("500"))
                .body("codeDetail", equalToIgnoringWhiteSpace("System Error"))
                .body("response", empty())
    }

    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum when mid is blank")
    void t20() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)
        TreeMap<String, String> map = new TreeMap<>();
        map.put("CUSTID", custId);
        String checksum = PGPUtil.getChecksum(mid.get().key, map);

        String basePath = GET_SAVEDCARD_CUSTID_MID_CHECKSUM.replace("{custId}", custId)
                .replace("{mid}/", "").replace("{checksum}", checksum)

        given().spec(reqSpec1).basePath(basePath).get()
                .then()
                .body("responseStatus", equalToIgnoringWhiteSpace("FAILURE"))
                .body("httpCode", equalToIgnoringWhiteSpace("500"))
                .body("httpSubCode", equalToIgnoringWhiteSpace("500"))
                .body("codeDetail", equalToIgnoringWhiteSpace("System Error"))
                .body("response", empty())
    }

    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum when custid is blank")
    void t21() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)
        TreeMap<String, String> map = new TreeMap<>();
        map.put("MID", mid.get().id);
        String checksum = PGPUtil.getChecksum(mid.get().key, map);

        String basePath = GET_SAVEDCARD_CUSTID_MID_CHECKSUM.replace("{custId}/", "")
                .replace("{mid}", mid.get().id).replace("{checksum}", checksum)

        given().spec(reqSpec1).basePath(basePath).get()
                .then()
                .body("responseStatus", equalToIgnoringWhiteSpace("FAILURE"))
                .body("httpCode", equalToIgnoringWhiteSpace("500"))
                .body("httpSubCode", equalToIgnoringWhiteSpace("500"))
                .body("codeDetail", equalToIgnoringWhiteSpace("System Error"))
                .body("response", empty())
    }


    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum mid is invalid")
    void t22() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        TreeMap<String, String> map = new TreeMap<>();
        map.put("MID", mid.get().id);
        map.put("CUSTID", custId);

        String checksum = PGPUtil.getChecksum(mid.get().key, map);

        String basePath = GET_SAVEDCARD_CUSTID_MID_CHECKSUM.replace("{custId}", custId)
                .replace("{mid}", mid.get().id + "abcd").replace("{checksum}", checksum)

        given().spec(reqSpec1).basePath(basePath).get()
                .then()
                .body("responseStatus", equalToIgnoringWhiteSpace("FAILURE"))
                .body("httpCode", equalToIgnoringWhiteSpace("500"))
                .body("httpSubCode", equalToIgnoringWhiteSpace("500"))
                .body("codeDetail", equalToIgnoringWhiteSpace("System Error"))
                .body("response", empty())
    }

    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum checksum is invalid")
    void t23() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        String basePath = GET_SAVEDCARD_CUSTID_MID_CHECKSUM.replace("{custId}", custId)
                .replace("{mid}", mid.get().id).replace("{checksum}", "aaaa")

        given().spec(reqSpec1).basePath(basePath).get()
                .then()
                .body("responseStatus", equalToIgnoringWhiteSpace("FAILURE"))
                .body("httpCode", equalToIgnoringWhiteSpace("500"))
                .body("httpSubCode", equalToIgnoringWhiteSpace("500"))
                .body("codeDetail", equalToIgnoringWhiteSpace("System Error"))
                .body("response", empty())
    }

    @Test(description = "Verify /savedcardservice/savedcardOpenAPIService/get/savedcard/custId/mId/checkSum custId with special character")
    void t24() {
        String custId = CommonHelpers.generateOrderId() + "%^&*"

        Card card = cards.find { it.type == 'debit' }
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        String cardFirstSixDigit = card.no.substring(0, 6)
        String cardLastFourDigit = card.no.substring(card.no.length() - 4)

        TreeMap<String, String> map = new TreeMap<>()
        map.put("CUSTID", custId)
        map.put("MID", mid.get().id)

        String checksum = PGPUtil.getChecksum(mid.get().key, map)

        String basePath = GET_SAVEDCARD_CUSTID_MID_CHECKSUM.replace("{custId}", custId)
                .replace("{mid}", mid.get().id).replace("{checksum}", checksum)

        given().spec(reqSpec1).basePath(basePath).get()
                .then()
                .body("responseStatus", equalToIgnoringWhiteSpace("SUCCESS"))
                .body("httpCode", equalToIgnoringWhiteSpace("200"))
                .body("httpSubCode", equalToIgnoringWhiteSpace("200"))
                .body("codeDetail", equalToIgnoringWhiteSpace("Success"))
                .body("response", not(empty()))
                .body("response.cardId", not(empty()))
                .body("response.cardNumber", hasItem(containsString(cardFirstSixDigit)),
                        "response.cardNumber", hasItem(containsString(cardLastFourDigit)),
                        "response.cardScheme", hasItem(equalToIgnoringWhiteSpace("DC")))
    }

    //TODO: Card does not exist is displayed every time
   // @Test(dataProvider = "CardTypes", description = "Verify /savedcardservice/savedCardService/pg/get/savedcard/custId/mId for different card types", enabled = false)
    void t25(String cardNo, String expMon, String expyear, String cardType, String cardScheme) {
        String custId = CommonHelpers.generateOrderId()

        sch.saveCard_custId_mId(cardNo, custId, mid.get().id, expMon + expyear)

        given().spec(reqSpec1).get(PG_GET_SAVEDCARD_CUSTID_MID, custId, mid.get().id)
                .then()
    }

    def BININFO_REQ_PARAM() {
        [
                "SSOToken": null
        ]
    }

//    @AUser(edit = true)
    @Test(dataProvider = "CardType_ssoToken", description = "Verify /HANDLER_INTERNAL/BININFO for different card")
    void t26(Card card, String scheme) {
        User user = userManager.getForWrite(Label.BASIC).with { new User(it.mobNo(), it.password(), true) }

        user.savedCards.clear()
        user.savedCards.add(card)

        String cardFirstSixDigit = card.no.substring(0, 6)
        String cardLastFourDigit = card.no.substring(card.no.length() - 4)

        def requestparam = BININFO_REQ_PARAM()
        requestparam.SSOToken = user.tokens.find { it.name == 'sso' }.id

        def path = given().spec(reqSpec1).formParam("JsonData", requestparam)
                .basePath("/savedcardservice/HANDLER_INTERNAL/BIN_INFO").post()
                .then().defaultParser(Parser.JSON).extract().body().path("")

        SoftAssertions softly = new SoftAssertions()
        softly.assertThat(path.STATUS).isEqualTo("SUCCESS")
        softly.assertThat(path.SIZE).isNotNull()
        softly.assertThat(path.BIN_DETAILS).asList().isNotEmpty()
        softly.assertThat(path.BIN_DETAILS.SAVE_CARD_ID).asList().isNotEmpty()
        softly.assertThat(path.BIN_DETAILS.CARDBIN).asList().contains(cardFirstSixDigit)
        softly.assertThat(path.BIN_DETAILS.CARDLASTDIGIT).asList().contains(cardLastFourDigit)
        softly.assertAll()
    }

    //@Deprecated
    //@Test(enabled = false, description = "Validate success of /savedcardservice/savedCardService/pg/get/savedcard/custId/mId api for different card Types", dataProvider = "CardType_ssoToken")
    void t27(Card card, String scheme) {
        String custId = CommonHelpers.generateOrderId()

        String cardNo = card.no
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        given().spec(reqSpec1)
                .basePath(GET_PG_SAVEDCARD_ON_CUSTID_MID)
                .pathParam("custid", custId)
                .pathParam("mid", mid.get().id)
                .get()
                .then()
                .defaultParser(Parser.JSON)
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", hasSize(1),
                        "response", not(empty()))
                .root("response")
                .body("cardId", notNullValue(),
                        "cardNumber", notNullValue(),
                        "cardType", notNullValue(),
                        "expiryDate", notNullValue(),
                        "firstSixDigit", hasItem(cardNo.substring(0, 6)),
                        "lastFourDigit", hasItem(cardNo.substring(cardNo.length() - 4)),
                        "status", contains(1),
                        "updated_on", notNullValue(),
                        "created_on", notNullValue(),
                        "mId", hasItem(mid.get().id),
                        "custId", hasItem(custId),
                        "cardScheme", notNullValue(),
                        "bankName", notNullValue())
    }

    @Test(description = "Validate /savedcardservice/savedCardService/pg/get/savedcard/custId/mId api when mid is blank")
    void t28() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        String cardNo = card.no
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        given().spec(reqSpec1)
                .basePath(GET_PG_SAVEDCARD_ON_CUSTID_MID)
                .pathParam("custid", custId)
                .pathParam("mid", "")
                .get()
                .then()
                .defaultParser(Parser.JSON)
                .body("responseStatus", equalToIgnoringCase("FAILURE"),
                        "httpCode", equalToIgnoringCase("500"),
                        "httpSubCode", equalToIgnoringCase("500"),
                        "codeDetail", equalToIgnoringCase("System Error"),
                        "response", equalToIgnoringCase("System Error"))

    }

    @Test(description = "Validate /savedcardservice/savedCardService/pg/get/savedcard/custId/mId api when custid is blank")
    void t29() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        String cardNo = card.no
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        given().spec(reqSpec1)
                .basePath(GET_PG_SAVEDCARD_ON_CUSTID_MID)
                .pathParam("custid", "")
                .pathParam("mid", mid.get().id)
                .get()
                .then()
                .defaultParser(Parser.JSON)
                .body("responseStatus", equalToIgnoringCase("FAILURE"),
                        "httpCode", equalToIgnoringCase("500"),
                        "httpSubCode", equalToIgnoringCase("500"),
                        "codeDetail", equalToIgnoringCase("System Error"),
                        "response", equalToIgnoringCase("System Error"))

    }

    @Test(description = "Validate /savedcardservice/savedCardService/pg/get/savedcard/custId/mId api when mid is invalid")
    void t30() {
        String custId = CommonHelpers.generateOrderId()

        Card card = cards.find { it.type == 'debit' }
        String cardNo = card.no
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        given().spec(reqSpec1)
                .basePath(GET_PG_SAVEDCARD_ON_CUSTID_MID)
                .pathParam("custid", custId)
                .pathParam("mid", "1234")
                .get()
                .then()
                .defaultParser(Parser.JSON)
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("204"),
                        "codeDetail", equalToIgnoringCase("Card does not exist for given parameters"),
                        "response", empty())
    }

    //@Deprecated
    //@Test(enabled = false, description = "Validate /savedcardservice/savedCardService/pg/get/savedcard/custId/mId api when Cust id contains special characters")
    void t31() {
        String custId = CommonHelpers.generateOrderId() + "@##@#@"

        Card card = cards.find { it.type == 'debit' }
        String cardNo = card.no
        sch.saveCard_custId_mId(card.no, custId, mid.get().id, card.expMo + card.expYr)

        given().spec(reqSpec1)
                .basePath(GET_PG_SAVEDCARD_ON_CUSTID_MID)
                .pathParam("custid", custId)
                .pathParam("mid", mid.get().id)
                .get()
                .then()
                .defaultParser(Parser.JSON)
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", hasSize(1),
                        "response", not(empty()))
                .root("response")
                .body("cardId", notNullValue(),
                        "cardNumber", notNullValue(),
                        "cardType", notNullValue(),
                        "expiryDate", notNullValue(),
                        "firstSixDigit", hasItem(cardNo.substring(0, 6)),
                        "lastFourDigit", hasItem(cardNo.substring(cardNo.length() - 4)),
                        "status", contains(1),
                        "updated_on", notNullValue(),
                        "created_on", notNullValue(),
                        "mId", hasItem(mid.get().id),
                        "custId", hasItem(custId),
                        "cardScheme", notNullValue(),
                        "bankName", notNullValue())
    }

    @DataProvider(name = "lastFourDigitSingleNumber")
    Object[][] d4() {
        [
                ['5166400031030008', '01', '2030', 'DEBIT_CARD', 'MASTER'],
                ['6799990100000000009', '01', '2030', 'DEBIT_CARD', 'MAESTRO'],
                ['379863297650006', '01', '2030', 'CREDIT_CARD', 'AMEX'],
                ['6073180505920009', '01', '2030', 'DEBIT_CARD', 'RUPAY'],
                ['30569309020004', '01', '2030', 'CREDIT_CARD', 'DINERS'],
                ['4012888888880001', '01', '2030', 'CREDIT_CARD', 'VISA']
        ]
    }

//    @AUser(edit = true)
//    @Deprecated
//    @Test(enabled = false, dataProvider = "lastFourDigitSingleNumber", description = "Verify last for digit if we have first 3 values equal to 0 in /savedcardservice/merchant/v1/get/card API")
    void t32(String cardNo, String expMon, String expyear, String cardType, String cardScheme) {
        def root = MERCHANT_V1_GET_SAVEDCARD_BODY()
        String custId = CommonHelpers.generateOrderId()

        root.CUSTID = custId
        root.MID = mid.get().id
        root.REQUEST_TYPE = 'DEFAULT'
        String checksum = PGPUtil.getChecksum(mid.get().key, (root as TreeMap<String, String>))
        root.CHECKSUM = checksum

        String cardFirstSixDigits = cardNo.substring(0, 6)
        String cardLastFourDigits = cardNo.substring(cardNo.length() - 4)

        sch.saveCard_custId_mId(cardNo, custId, mid.get().id, expMon + expyear)

        SavedCardApi.getSavedCard(root)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", not(empty()),
                        "response.savedCardId", everyItem(notNullValue()),
                        "response.cardFirstSixDigits", hasItem(cardFirstSixDigits),
                        "response.cardLastFourDigits", hasItem(cardLastFourDigits),
                        "response.cardType", hasItem(cardType),
                        "response.issuerDisplayName", everyItem(notNullValue()),
                        "response.issuerCode", everyItem(not(empty())),
                        "response.cardScheme", hasItem(cardScheme))
    }


}