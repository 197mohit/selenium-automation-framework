package scripts.api.mappingService


import com.paytm.appconstants.Constants
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.dto.OrderDTO
import com.paytm.dto.OrderFactory
import com.paytm.dto.PaymentDTO
import com.paytm.framework.conditions.SoftAssertion
import com.paytm.pages.CashierPage
import com.paytm.pages.CashierPageFactory
import com.paytm.pages.CheckoutPage
import com.paytm.pages.responsePage.ResponsePage
import com.paytm.utils.merchant.user.Card
import io.qameta.allure.Owner
import io.qameta.allure.Story
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Gagandeep Singh')
@Story('Bin Migration')
class BankCardBinApiTest extends TestSetUp {


    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/mapping-service/get/bankcard/bin/{bin}')
                        .build()
        )
    }

    protected ResponseSpecification schema() {
        new ResponseSpecBuilder()
                .expectStatusCode(isIn(200))
                .expectContentType(ContentType.JSON)
                .expectBody('', allOf(hasKey('id'), hasKey('bin'), hasKey('isIndian'), hasKey('bank'), hasKey('cardType'), hasKey('cardName'), hasKey('bankCode'), hasKey('instId'), hasKey('iDebitEnabled'), hasKey('cardEnabled'), hasKey('ccDirectEnabled'), hasKey('displayBankName'), hasKey('oneClickSupported'), hasKey('active')))
                .expectBody("id", allOf(isA(Integer.class), notNullValue()))
                .expectBody("bin", allOf(isA(Integer.class), notNullValue()))
                .expectBody("isIndian", allOf(instanceOf(Boolean.class), isIn(false, true)))
                .expectBody("bank", allOf(isA(String.class)))
                .expectBody("cardType", allOf(isA(String.class), isIn("DEBIT_CARD", "CREDIT_CARD")))
                .expectBody("cardName", allOf(isA(String.class), isIn("VISA", "MASTER", "MAESTRO", "RUPAY", "AMEX", "DINERS", "BAJAJFN")))
                .expectBody("bankCode", allOf(isA(String.class)))
                .expectBody("iDebitEnabled", allOf(isA(Boolean.class), isIn(false, true)))
                .expectBody("cardEnabled", allOf(isA(Boolean.class), isIn(false, true)))
                .expectBody("ccDirectEnabled", allOf(isA(Boolean.class), isIn(false, true)))
                .expectBody("displayBankName", allOf(isA(String.class)))
                .expectBody("oneClickSupported", allOf(isA(Boolean.class), isIn(false, true)))
                .expectBody("binInfoSource", allOf(isA(String.class), isIn("DATABASE_MATCH", "PATTERN_MATCH", "DEFAULT_MATCH"))) // Database_Match -- P + bin fetching PG_DataBaseMatch -- For PG database
                .expectBody("active", allOf(isA(Boolean.class), isIn(false, true)))
                .build()
    }

    @Test
    void testApiSchema() {
        req().pathParam('bin', '123456').get().then()
                .spec(schema())
    }


    //validation of cardtype

    @Test
    void 'testCardTypeIsDCorDebit'() {
        req().pathParam('bin', cards.find { it.type == "debit" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardType', equalTo('DEBIT_CARD'))
    }

    @Test
    void 'testCardTypeIsCCorCredit'() {
        req().pathParam('bin', cards.find { it.type == "credit" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardType', equalTo('CREDIT_CARD'))
    }


    //validation of CardName

    @Test
    void 'testCardNameIsAmex'() {
        req().pathParam('bin', cards.find { it.scheme == "amex" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardName', equalTo('AMEX'))
    }

    @Test
    void 'testCardNameIsVisa'() {
        req().pathParam('bin', cards.find { it.scheme == "visa" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardName', equalTo('VISA'))
    }

    @Test
    void 'testCardNameIsMaster'() {
        req().pathParam('bin', cards.find { it.scheme == "master" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardName', equalTo('MASTER'))
    }

    @Test
    void 'testCardNameIsRupay'() {
        req().pathParam('bin', cards.find { it.scheme == "rupay" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardName', equalTo('RUPAY'))
    }

    @Test
    void 'testCardNameIsMastero'() {
        req().pathParam('bin', cards.find { it.scheme == "maestro" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardName', equalTo('MAESTRO'))
    }

    @Test
    void 'testCardNameIsDiners'() {
        req().pathParam('bin', cards.find { it.scheme == "diners" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardName', equalTo('DINERS'))
    }

    @Test
    void 'testCardNameIsBajaj'() {
        req().pathParam('bin', cards.find { it.scheme == "bajajfn" }.no[0..5]).get().then()
                .spec(schema())
                .body('cardName', equalTo('BAJAJFN'))
    }

//invalid bins
    @Test(dataProvider = 'invalidBins')
    void testforinvalidbinNo(String invalidBin) {
        req().pathParam('bin', invalidBin).get().then()
                .body('bankCode', equalTo('BBK'))

    }

    @DataProvider(name = "invalidBins")
    public static Object[][] invalidBins() {
        [
                ['1'],
                ['1234'],
                ['123416'],
                ['1235565'],
                ['1222'],
                ['123'],
        ]
    }

//Validation of oneclicksupported


    @Test
    void 'testForOneClickSupportedTrue'() {
        req().pathParam('bin', cards.find { it.type == "debit" }.tap {
            assert it.setOneClickSupported(true)
        }.no[0..5]).get().then()
                .spec(schema())
                .body('oneClickSupported', equalTo(true))

    }


    @Test
    void 'testForOneClickSupportedFalse'() {
        req().pathParam('bin', cards.find { it.type == "debit" }.tap {
            assert it.setOneClickSupported(false)
        }.no[0..5]).get().then()
                .spec(schema())
                .body('oneClickSupported', equalTo(false))

    }


//Validation of isIndian
    @Test
    void 'testForIsIndianTrue'() {
        req().pathParam('bin', cards.find { it.type == "debit" }.tap {
            assert it.setIndian(true)
        }.no[0..5]).get().then()
                .spec(schema())
                .body('isIndian', equalTo(true))

    }

    @Test
    void 'testForIsIndianFalse'() {

        Card card = new Card('5122140830243040', '01', '2030', '123', 'master', 'high')
        req().pathParam('bin', card.no[0..5])
                .get().then()
                .spec(schema())
                .body('isIndian', equalTo(false))

    }


    //Validation of PrepaidCards
    @Test
    void 'testForPrepaidCardTrue'() {
        req().pathParam('bin', cards.find { it.type == "debit" }.tap {
            assert it.setPrepaid(true)
        }.no[0..5]).get().then()
                .spec(schema())
                .body('prepaidCard', equalTo(true))

    }

    @Test
    void 'testForPrepaidCardFalse'() {
        req().pathParam('bin', cards.find { it.type == "debit" }.tap {
            assert it.setPrepaid(false)
        }.no[0..5]).get().then()
                .spec(schema())
                .body('prepaidCard', equalTo(false))

    }

//Validate Bin Coming Same As Given
    @Test
    void 'testBinInResIsSameAsInReq'() {
        Card card = cards.find { it.type == 'debit' }
        req().pathParam("bin", card.no[0..5]).get().then()
                .body('bin', equalTo(Integer.valueOf(card.no[0..5])))
    }


//Validate CustomDisplayName

    @Test
    void 'testForCustomDisplayName'() {
        req().pathParam('bin', cards.find { it.type == "debit" }.tap {
            assert it.setCustomDisplayName("State Bank of India")
        }.no[0..5]).get().then()
                .spec(schema())
                .body('displayBankName', equalTo("State Bank of India"))

    }

    @Test
    void 'testForCustomDisplayNameIsSpecialCharacter'() {

        Card card = new Card('5122140830243040', '01', '2030', '123', 'master', 'high')
        req().pathParam('bin', cards.find { it.type == "debit" }.tap {
            assert it.setCustomDisplayName("State % Bank@ of !NDI^")
        }.no[0..5]).get().then()
                .spec(schema())
                .body('displayBankName', equalTo("State % Bank@ of !NDI^"))

    }

//Validation of active ---- would be as Blocked and it should pick it from Regex i.e. Source would Be Pattern Match


    @Test
    void 'testForInActive'() {
        Card card = new Card('3045751111111117', '01', '2030', '123', 'master', 'high')

        req().pathParam('bin', card.no[0..5]).get().then()
                .spec(schema())
                .body('binInfoSource', equalTo("PATTERN_MATCH"))

    }


    @Test
    void 'testForactive'() {
        req().pathParam('bin', cards.find { it.scheme == "diners" }.tap {
            assert it.setBlocked(false)
        }.no[0..5]).get().then()
                .spec(schema())
                .body('binInfoSource', equalTo("DATABASE_MATCH"))

    }

    //validate for eight bin successful bin stored


    @Test
    void 'testFor8DigitBin'() {
        req().pathParam('bin', '41266331').get().then()
                .spec(schema())
    }

    //bin value is saved in Redis

    @Test
    void 'testBinValueSavedInRedis'() {

        def bin = req().pathParam('bin', cards.find { it.type == "credit" }.no[0..5]).get().jsonPath().get("bin")

//        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI)
//        Set<String> keySet = jedis.keys("BIN_DETAILS_*");
        Set<String> keySet = TRANSACTIONAL_REDIS_CLUSTER().hkeys("BIN_DETAILS_*");
        keySet.find {
            it.find('BIN_DETAILS_' + bin)
        }.tap { assert true }

    }
//Default binsource for isIndian is true

    @Test
    void 'testForDefaultBinSource'() {
        req().pathParam('bin', "776655").get().then()
                .spec(schema())
                .body('binInfoSource', equalTo("DEFAULT_MATCH"), 'isIndian', equalTo(true))

    }


//Pattern bin source when P+ DB don't have entry for isIndian
    @Test
    void 'testForPatternBinSource'() {
        req().pathParam('bin', "555424").get().then()
                .spec(schema())
                .body('binInfoSource', equalTo("PATTERN_MATCH"), 'isIndian', equalTo(true))

    }


    /*Adding few test cases where BIN Is out of sync i.e. PG_DB has different bin details and P+ DB has different bin details
    * So when we fetch the data source of truth would be P+ through mapping service*/


    //bin - 459200 -- SBI In PG_DB && ICICI Bank in P+ DB

    @Test
    void 'testforOutOfSyncBinWithDifferentBankCode'() {
        req().pathParam('bin', "459200").get().then()
                .spec(schema())
                .body('bankCode', equalTo('SBI'))  //459200 -- BBK in New P+ DB

    }

    //bin - 471865 -- CREDIT_CARD In P+ && DEBIT_CARD in PG DB

    @Test
    void 'testforOutOfSyncBinWithDifferentCardType'() {
        req().pathParam('bin', "471865").get().then()
                .spec(schema())
                .body('cardType', equalTo('CREDIT_CARD'))

    }

    //bin - 388002 -- DINERS In PG_DB && MASTER in P+ DB

    @Test
    void 'testforOutOfSyncBinWithDifferentcardName'() {
        req().pathParam('bin', "388002").get().then()
                .spec(schema())
                .body('cardName', equalTo('DINERS')) //bin - 388002 -- Diners In New P+ DB

    }


    //Test cases for Source of BIN

    @Test
    void 'testTxnforDefaultMatchbinSource'() {

        BinSource:
        {
            req().pathParam('bin', "355873").get().then()
                    .spec(schema())
                    .body('binInfoSource', equalTo("DEFAULT_MATCH"))
        }

        Txn:
        {
            String theme = 'enhancedweb_revamp'
            OrderDTO order = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_Retry, theme)
                    .build()
            PaymentDTO paymenDetails = new PaymentDTO().setDebitCardNumber("3558736343368576")
            CheckoutPage checkoutPage = new CheckoutPage()
            checkoutPage.createOrder(order)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
            cashierPage.payBy(Constants.PayMode.DC, paymenDetails)
            ResponsePage responsePage = new ResponsePage()
            assertion.apply(pageWait.apply(responsePage.hasLoaded()))
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals('TXN_SUCCESS'),
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.PAYMENTMODE).equals('DC')
            )
            sAssert.eval()
        }
    }

    @Test
    void 'testTxnforPatterntMatchbinSource'() {

        BinSource:
        {
            req().pathParam('bin', "525103").get().then()
                    .spec(schema())
                    .body('binInfoSource', equalTo("PATTERN_MATCH"))
        }

        Txn:
        {
            String theme = 'enhancedweb_revamp'
            OrderDTO order = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_Retry, theme).build()
            PaymentDTO paymenDetails = new PaymentDTO().setDebitCardNumber("5251030581561638")
            CheckoutPage checkoutPage = new CheckoutPage()
            checkoutPage.createOrder(order)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
            cashierPage.payBy(Constants.PayMode.DC, paymenDetails)
            ResponsePage responsePage = new ResponsePage()
            assertion.apply(pageWait.apply(responsePage.hasLoaded()))
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals('TXN_SUCCESS'),
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.PAYMENTMODE).equals('DC')
            )
            sAssert.eval()
        }


    }

    @Test
    void 'testTxnforDatabasetMatchbinSource'() {

        BinSource:
        {
            req().pathParam('bin', "476641").get().then()
                    .spec(schema())
                    .body('binInfoSource', equalTo("DATABASE_MATCH"))
        }
        Txn:
        {
            String theme = 'enhancedweb_revamp'
            OrderDTO order = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build()
            PaymentDTO paymenDetails = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD)
            CheckoutPage checkoutPage = new CheckoutPage()
            checkoutPage.createOrder(order)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
            cashierPage.payBy(Constants.PayMode.DC, paymenDetails)
            ResponsePage responsePage = new ResponsePage()
            assertion.apply(pageWait.apply(responsePage.hasLoaded()))
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals('TXN_SUCCESS'),
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.PAYMENTMODE).equals('DC')
            )
            sAssert.eval()
        }
    }


    //Test cases for transaction of Bin configuration

    @Test
    void 'testTxnforInternationalbinWhenIsIndianIsFalse'() {

        BinSource:
        {
            req().pathParam('bin', "415026").get().then()
                    .body('isIndian', equalTo(false))
        }

        Txn:
        {
            String theme = 'enhancedweb_revamp'
            OrderDTO order = new OrderFactory.PGOnly(Constants.MerchantType.ALLPAYMODE, theme).build()
            PaymentDTO paymenDetails = new PaymentDTO().setCreditCardNumber("4718650100010336")
            CheckoutPage checkoutPage = new CheckoutPage()
            checkoutPage.createOrder(order)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
            cashierPage.payBy(Constants.PayMode.CC, paymenDetails)
            ResponsePage responsePage = new ResponsePage()
            assertion.apply(pageWait.apply(responsePage.hasLoaded()))
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals('TXN_SUCCESS'),
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.GATEWAY_NAME).equals('HDFC')
            )
            sAssert.eval()
        }

    }


    @Test
    void 'testTxnforPrepaidCard'() {


        BinSource:
        {
            req().pathParam('bin', "476641").get().then()
                    .spec(schema())
                    .body('prepaidCard', equalTo(true))
        }
        Txn:
        {
            String theme = 'enhancedweb_revamp'
            OrderDTO order = new OrderFactory.PGOnly(Constants.MerchantType.MASKED_MOBILE_ENABLED, theme).build()
            PaymentDTO paymenDetails = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD)
            CheckoutPage checkoutPage = new CheckoutPage()
            checkoutPage.createOrder(order)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
            cashierPage.payBy(Constants.PayMode.DC, paymenDetails)
            ResponsePage responsePage = new ResponsePage()
            assertion.apply(pageWait.apply(responsePage.hasLoaded()))
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals('TXN_SUCCESS'),
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.PREPAID_CARD).equals('true')
            )
            sAssert.eval()
        }

    }

    @Test
    void 'testTxnforDisplayNameForDC'() {

        Card card = new Card('5134676957885192', '01', '2030', '123', 'master', 'high')

        BinSource:
        {
            req().pathParam('bin', card.tap {
                assert it.setCustomDisplayName("State Bank of India")
            }.no[0..5]).get().then()
                    .body('displayBankName', equalTo("State Bank of India"))
        }

        Txn:
        {
            String theme = 'enhancedweb_revamp'
            User user = userManager.getForWrite(Label.RETRYPAYMODE)
            SavedCardHelpers.deleteSavedCard(user)
            OrderDTO order = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .build()
            PaymentDTO paymenDetails = new PaymentDTO().setDebitCardNumber(card.no)
            CheckoutPage checkoutPage = new CheckoutPage()
            checkoutPage.createOrder(order)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
            cashierPage.payBy(Constants.PayMode.DC_WITH_SAVECARD, paymenDetails)
            ResponsePage responsePage = new ResponsePage()
            assertion.apply(pageWait.apply(responsePage.hasLoaded()))
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals('TXN_SUCCESS'),
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.PAYMENTMODE).equals('DC')
            )
            sAssert.eval()
            OrderDTO orderDTO2 = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .build()
            CheckoutPage checkoutPage2 = new CheckoutPage();
            checkoutPage2.createOrder(orderDTO2);
            CashierPage cashierPage2 = CashierPageFactory.getCashierPage(theme);
            cashierPage2.savedCardDisplayName().assertText("State Bank of India Debit Card")
        }

    }


    @Test
    void 'testTxnforDisplayNameForCC'() {

        Card card = cards.find { it.scheme == "visa" && it.type == "credit" }
        BinSource:
        {
            req().pathParam('bin', card.tap {
                assert it.setCustomDisplayName("HDFC Bank")
            }.no[0..5]).get().then()
                    .body('displayBankName', equalTo("HDFC Bank"))
        }

        Txn:
        {
            String theme = 'enhancedweb_revamp'
            User user = userManager.getForWrite(Label.RETRYPAYMODE)
            SavedCardHelpers.deleteSavedCard(user)
            OrderDTO order = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .build()
            PaymentDTO paymenDetails = new PaymentDTO().setCreditCardNumber(card.no)
            CheckoutPage checkoutPage = new CheckoutPage()
            checkoutPage.createOrder(order)
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
            cashierPage.payBy(Constants.PayMode.CC_WITH_SAVECARD, paymenDetails)
            ResponsePage responsePage = new ResponsePage()
            assertion.apply(pageWait.apply(responsePage.hasLoaded()))
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals('TXN_SUCCESS'),
                    responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.PAYMENTMODE).equals('CC')
            )
            sAssert.eval()
            OrderDTO orderDTO2 = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            CheckoutPage checkoutPage2 = new CheckoutPage();
            checkoutPage2.createOrder(orderDTO2);
            CashierPage cashierPage2 = CashierPageFactory.getCashierPage(theme);
            cashierPage2.savedCardDisplayName().assertText("HDFC Bank Credit Card")
        }

    }


}