package scripts.api.theia.applyPromo

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.dto.PaymentDTO
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Issue
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.SkipException
import org.testng.annotations.AfterMethod
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.APPLY_PROMO_V1
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.*
import static org.hamcrest.Matchers.*

class SSOTokenApplyPromoV1Test extends TestSetUp implements SSOTokenApplyPromoTest {

    private final static PROMOS_NOT_ADDED = "unable to add promo(s) successfully"
    private final static CARD_NOT_ADDED = "unable to add card(s) successfully"
    private final static JSON_SCHEMA = 'json-schemas/apply-promo-schema.json'

    final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(APPLY_PROMO_V1)
                .addQueryParam('mid', '?')
    }

    final RequestSpecification req() {
        given(reqBldr().build())
    }

    final Map<String, Object> root() {
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
                                        transactionAmount: '2',
                                        payMethod        : 'WALLET'
                                ]
                        ],
                        totalTransactionAmount: '2',
                        custId                : null,//O
                ]
        ]
    }

    Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private final Filter schemaFilter = new Filter() {
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

    ResponseSpecification resSpec = new ResponseSpecBuilder()
            .expectBody(matchesJsonSchemaInClasspath(JSON_SCHEMA))
            .build()

    ResponseSpecification success = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('S'))
            .expectBody('resultCodeId', nullValue())
            .expectBody('resultCode', equalTo('00000000'))
            .expectBody('resultMsg', equalTo('Success'))
            .build()

    private final static class ResultInfo {
        static ResponseSpecification TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("This transaction cannot be carried out with wallet"))
                .build()
        static ResponseSpecification PAY_TXN_AMT_ERR = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("pay transaction amount error"))
                .build()
        static ResponseSpecification PAY_TXN_AMT_CONDITION_BREACH = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("pay transaction amount condition breach"))
                .build()
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @AfterMethod(alwaysRun = true)
    void tearDown() {
        try {
            MERCHANTS.get().each {
                STATIC_REDIS_CLUSTER().del('com.paytm.pgplus.facade.paymentpromotion.models.response.SearchPaymentOffersServiceResponse_' + it.id)
            }
        } catch (Throwable e) {
            // DO NOTHING
        }
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId() })
    @AUser
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BANK_TRANSFER_MERCH.getId() })
    @AUser
    @Test
    void 'test when body mid = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.WALLETOnly_PCF.getId() })
    @AUser
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @AUser
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.WALLETOnly_PCF.getId() })
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_WALLET_MERCH.getId() })
    @AUser
    @Test
    void 'test when mid in query params is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(results.somethingWentWrong as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true)
    ])
    @AUser
    @Test
    void 'test when mid in query params is not equals to mid in request body'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.somethingWentWrong as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.UPI_INTENT.getId() })
    @AUser
    @Test
    void 'test when body promocode is not provided given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.remove('promocode')
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.NATIVE_PROMO_HYBRID.getId() })
    @AUser
    @Test
    void 'test when body promocode = null given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.promocode = null
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId() })
    @AUser
    @Test
    void 'test when body promocode = \'\' given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.promocode = ''
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BANK_TRANSFER_MERCH.getId() })
    @AUser
    @Test
    void 'test when body promocode equals random value given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.promocode = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_MERCH.getId() })
    @AUser
    @Test
    void 'test when body promocode is not provided given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('promocode')
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_PCF.getId() })
    @AUser
    @Test
    void 'test when body promocode = null given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = null
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @AUser
    @Test
    void 'test when body promocode = \'\' given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = ''
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MDR_PCF_MERCH.getId() })
    @AUser
    @Test
    void 'test when body totalTransactionAmount is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('totalTransactionAmount')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser
    @Test
    void 'test when body totalTransactionAmount = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.JIO.getId() })
    @AUser
    @Test
    void 'test when body totalTransactionAmount = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI_DC.getId() })
    @AUser
    @Test
    void 'test when body totalTransactionAmount equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.Redirectional_Native.getId() })
    @AUser
    @Test
    void 'test when body custId is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('custId')
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PAYTM_EXPRESS_ADDMONEY.getId() })
    @AUser
    @Test
    void 'test when body custId = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = null
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test
    void 'test when body custId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = ''
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI.getId() })
    @AUser
    @Test
    void 'test when body custId equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PaytmExpress_Hybrid_CCPayModeDisabled.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('paymentOptions')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.paymentOptions = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.paymentOptions = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_ONUS.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions equals empty list'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.paymentOptions = []
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.WalletOnly.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'CREDIT_CARD',
                        cardNo           : cards.find { it.type == 'credit' }.no,
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_HDFC.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals saved card id of CC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'CREDIT_CARD',
                        savedCardId      : {
                            //if (!user().savedCards.add(cards.find { it.type == 'credit' })) throw new SkipException(CARD_NOT_ADDED)
                            User user = new User(user().getMobile(),user().getPassword());
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals saved card id of DC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'CREDIT_CARD',
                        savedCardId      : {
                            //if (!user().savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
                            User user = new User(user().getMobile(),user().getPassword());
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLYONLY.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'DEBIT_CARD',
                        cardNo           : cards.find { it.type == 'debit' }.no,
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals saved card id of DC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'DEBIT_CARD',
                        savedCardId      : {
                            //if (!user().savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
                            User user = new User(user().getMobile(),user().getPassword());
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_MERCH.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals saved card id of CC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'DEBIT_CARD',
                        savedCardId      : {
                            //if (!user().savedCards.add(cards.find { it.type == 'credit' })) throw new SkipException(CARD_NOT_ADDED)
                            User user = new User(user().getMobile(),user().getPassword());
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_ALLPAYMODE_MERCH.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = NET_BANKING given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'NET_BANKING',
                        bankCode         : 'ICICI',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Issue('PGP-25524')
    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_HDFC.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = BALANCE given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'BALANCE',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = WALLET given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'WALLET',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BANK_TRANSFER_MERCH.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = PPBL given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'PPBL',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.WALLETOnly_PCF.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = PAYTM_DIGITAL_CREDIT given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'PAYTM_DIGITAL_CREDIT',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = UPI given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'UPI',
                        vpa              : 'sjsj@ICICI',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_NATIVE.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = EMI given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'EMI',
                        cardNo           : cards.find { it.type == 'credit' }.no,
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_CUSTOM_RATE.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = EMI_DC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'EMI_DC',
                        cardNo           : cards.find { it.type == 'debit' }.no,
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = COD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'COD',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = MP_COD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'MP_COD',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_HYBRID.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = HYBRID_PAYMENT given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '2'
        root.body.paymentOptions = [
                [
                        payMethod        : 'HYBRID_PAYMENT',
                        transactionAmount: '2'
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLYONLY.getId() })
    @AUser
    @Test
    void 'test when body promocode equals expired promocode'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_VALID_ANYMORE'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId() })
    @AUser
    @Test
    void 'test when promo is not valid for payMethod'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_PAY'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId() })
    @AUser
    @Test
    void 'test when promo is not valid for mid'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_VALID'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void 'test when promo is not applied by promo engine'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_APPLIED'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @AUser
    @Test
    void 'test when promo is provided for different bank for which promo is nor created'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NO_BANK'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId() })
    @AUser
    @Test
    void "test when promo is of cashback type"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'cashback'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly_Pcf.getId() })
    @AUser
    @Test
    void "test when promo is of discount type"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'discount'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_HYBRID.getId() })
    @AUser
    @Test(groups = "Security")
    void 'test when head token is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('token')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId() })
    @AUser
    @Test(groups = "Security")
    void 'test when head token = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.UPI_CONSENT_HYB.getId() })
    @AUser
    @Test(groups = "Security")
    void 'test when head token = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI.getId() })
    @AUser
    @Test(groups = "Security")
    void 'test when head token equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.FLAT_PCF.getId() })
    @AUser
    @Test(groups = "Security")
    void 'test when head token equals SSO token'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PAYTM_EXPRESS_ADDMONEY.getId() })
    @AUser
    @Test(groups = "Security")
    void 'test when head token equals WALLET token'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = user().tokens['wallet'].id
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test(groups = "Security")
    void 'test when head token equals TXN token'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = user().tokens['txn'].id
        req().body(root).post().then()
                .spec(success)
    }

/*    @Issue('PGP-26887')
    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test(enabled = false)

 */
    void "test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals CIN of CC given merchant has applicable promos"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'CREDIT_CARD',
                        savedCardId      : {
                            (user() as AlipayUser).savedCards.clear()
                            if (!(user() as AlipayUser).savedCards.add(cards.find { it.type == 'credit' })) throw new SkipException(CARD_NOT_ADDED)
                            (user() as AlipayUser).savedCards.find().idxNo
                        }(),
                        transactionAmount: root.body.totalTransactionAmount
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

/*    @Issue('PGP-26887')
    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test(enabled = false)

 */
    void "test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals CIN of DC given merchant has applicable promos"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'CREDIT_CARD',
                        savedCardId      : {
                            (user() as AlipayUser).savedCards.clear()
                            if (!(user() as AlipayUser).savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
                            (user() as AlipayUser).savedCards.find().idxNo
                        }(),
                        transactionAmount: root.body.totalTransactionAmount
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

/*    @Issue('PGP-26887')
    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test(enabled = false)

 */
    void "test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals CIN of DC given merchant has applicable promos"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'DEBIT_CARD',
                        savedCardId      : {
                            (user() as AlipayUser).savedCards.clear()
                            if (!(user() as AlipayUser).savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
                            (user() as AlipayUser).savedCards.find().idxNo
                        }(),
                        transactionAmount: root.body.totalTransactionAmount
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

/*    @Issue('PGP-26887')
    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test(enabled = false)

 */
    void "test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals CIN of CC given merchant has applicable promos"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'DEBIT_CARD',
                        savedCardId      : {
                            (user() as AlipayUser).savedCards.clear()
                            if (!(user() as AlipayUser).savedCards.add(cards.find { it.type == 'credit' })) throw new SkipException(CARD_NOT_ADDED)
                            (user() as AlipayUser).savedCards.find().idxNo
                        }(),
                        transactionAmount: root.body.totalTransactionAmount
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

 /*   @Override
    @Test(enabled = false) */
    void "test when body paymentOptions payMethod = BALANCE but user info not passed in request"() {
        throw new UnsupportedOperationException('no need to check because API will break before forwarding data to promo engine')
    }



    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BANK_TRANSFER_MERCH.getId() })
    @AUser
    @Test
    void "test when min txn amt constraint is violated"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'PAY_TXN_AMT_ERR'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.Redirectional_Native.getId() })
    @AUser
    @Test
    void "test when max txn amt constraint is violated"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'PAY_TXN_AMT_CONDITION_BREACH'
        req().body(root).post().then()
                .spec(success)
    }
}
