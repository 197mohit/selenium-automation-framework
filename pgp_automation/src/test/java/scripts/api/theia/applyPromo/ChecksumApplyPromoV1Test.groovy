package scripts.api.theia.applyPromo

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.User
import com.paytm.dto.PaymentDTO
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
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
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.nullValue

class ChecksumApplyPromoV1Test extends TestSetUp implements ChecksumApplyPromoTest {

    private final static PROMOS_NOT_ADDED = "unable to add promo(s) successfully"
    private final static CARD_NOT_ADDED = "unable to add card(s) successfully"
    private final static JSON_SCHEMA = 'json-schemas/apply-promo-schema.json'

    final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, setChecksumFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(APPLY_PROMO_V1)
                .addQueryParam('mid', '?')
    }

    final RequestSpecification req() {
        given(reqBldr().build())
    }

    final def root = {
        [
                head: [
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'CHECKSUM',
                        token           : '?',
                ],
                body: [
                        mid                   : m()?.id,
                        custId                : m()?.users?.get(0)?.id,//O
                        promocode             : null,//O
                        paymentOptions        : [
                                [
                                        transactionAmount: '1',
                                        payMethod        : 'WALLET',
                                ]
                        ],
                        totalTransactionAmount: '1',
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

    Filter setChecksumFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body.findAll { it.value != null }))
            }
            requestSpec.body(root)
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
        static ResponseSpecification NEW_TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET"))
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_HDFC.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PAYTM_EXPRESS_ADDMONEY.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_ALLPAYMODE_MERCH.getId() })
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.NATIVE_PROMO_HYBRID.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_ONUS.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PaytmExpress_Hybrid_CCPayModeDisabled.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BANK_TRANSFER_MERCH.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_HDFC.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly_Pcf.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_PCF.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @AUser
    @Test
    void 'test when body custId is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('custId')
        req().body(root).post().then()
                .spec(results.custIdCantBeBlankForTokenTypeChecksum as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly_Retry.getId() })
    @AUser
    @Test
    void 'test when body custId = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = null
        req().body(root).post().then()
                .spec(results.custIdCantBeBlankForTokenTypeChecksum as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PaytmExpress_Hybrid_CCPayModeDisabled.getId() })
    @AUser
    @Test
    void 'test when body custId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = ''
        req().body(root).post().then()
                .spec(results.custIdCantBeBlankForTokenTypeChecksum as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId() })
    @AUser
    @Test
    void 'test when body custId equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = UUID.randomUUID().toString().replaceAll('-', '')
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ZEST_MONEY.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_MERCH.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'CREDIT_CARD',
                        cardNo           : cards.find { it.type == 'credit' }.no,
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_MERCH.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals saved card id of CC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'CREDIT_CARD',
                        savedCardId      : {
                            //if (!user().savedCards.add(cards.find { it.type == 'credit' })) throw new SkipException(CARD_NOT_ADDED)
                            User user = new User(user().getMobile(),user().getPassword());
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_WALLET_MERCH.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals saved card id of DC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'CREDIT_CARD',
                        savedCardId      : {
                            User user = new User(user().getMobile(),user().getPassword());
                            //user().savedCards.clear()
                            SavedCardHelpers.deleteSavedCard(user);
                           // if (!user().savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                ]
        ]
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_HYBRID.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'DEBIT_CARD',
                        cardNo           : cards.find { it.type == 'debit' }.no,
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_HDFC.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals saved card id of DC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'DEBIT_CARD',
                        savedCardId      : {
                            User user = new User(user().getMobile(),user().getPassword());
                          //  user().savedCards.clear()
                            SavedCardHelpers.deleteSavedCard(user);
                            //if (!user().savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MDR_PCF_MERCH.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals saved card id of CC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'DEBIT_CARD',
                        savedCardId      : {
                            User user = new User(user().getMobile(),user().getPassword());
                            //user().savedCards.clear()
                            SavedCardHelpers.deleteSavedCard(user);
                            //if (!user().savedCards.add(cards.find { it.type == 'credit' })) throw new SkipException(CARD_NOT_ADDED)
                            PaymentDTO paymentDTO = new PaymentDTO();
                            paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
                            if (!SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber())) throw new SkipException(CARD_NOT_ADDED)
                            user().savedCards.find().id
                        }(),
                ]
        ]
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_HYBRID.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = NET_BANKING given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'NET_BANKING',
                        bankCode         : 'ICICI',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Issue('PGP-25524')
    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.FLAT_PCF.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = BALANCE given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'BALANCE',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ_HDFC.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = WALLET given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'WALLET',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_ALLPAYMODE_MERCH.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = PPBL given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'PPBL',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }


    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = PAYTM_DIGITAL_CREDIT given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'PAYTM_DIGITAL_CREDIT',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.Redirectional_Native.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = UPI given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'UPI',
                        vpa              : 'sjsj@ICICI',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = EMI given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'EMI',
                        cardNo           : cards.find { it.type == 'credit' }.no,
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_ONUS.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = EMI_DC given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'EMI_DC',
                        cardNo           : cards.find { it.type == 'debit' }.no,
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_PCF.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = COD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'COD',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_WALLET_MERCH.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = MP_COD given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'MP_COD',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PAYTM_EXPRESS_ADDMONEY.getId() })
    @AUser
    @Test
    void 'test when body paymentOptions payMethod = HYBRID_PAYMENT given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '2'
        root.body.paymentOptions = [
                [
                        transactionAmount: '1',
                        payMethod        : 'HYBRID_PAYMENT',
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MIGRATIONDETAIL.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.WalletOnly.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI_DC.getId() })
    @AUser
    @Test
    void 'test when head token is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('token')
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLYONLY.getId() })
    @AUser
    @Test
    void 'test when head token = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = null
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ZEST_MONEY.getId() })
    @AUser
    @Test
    void 'test when head token = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = ''
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.NETBANK_PCF.getId() })
    @AUser
    @Test
    void 'test when head token equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true)
    ])
    @AUser
    @Test
    void "test unable to apply promo when checksum is created using different merchant's key"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = PGPUtil.getChecksum(m(1).key, JsonOutput.toJson(root.body.findAll { it.value != null }))
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
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

//    @Issue('PGP-26887')
//    @Override
//    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
//    @AUser(edit = true)
//    @Test(enabled = false)
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

//    @Issue('PGP-26887')
//    @Override
//    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
//    @AUser(edit = true)
//    @Test(enabled = false)
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

//    @Issue('PGP-26887')
//    @Override
//    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
//    @AUser(edit = true)
//    @Test(enabled = false)
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

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId() })
    @Test
    void "test when body paymentOptions payMethod = BALANCE but user info not passed in request"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        transactionAmount: root.body.totalTransactionAmount,
                        payMethod        : 'BALANCE',
                ]
        ]
        root.body.promocode = 'TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MDR_PCF_MERCH.getId() })
    @Test
    void "test when min txn amt constraint is violated"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'PAY_TXN_AMT_ERR'
        req().body(root).post().then()
                .spec(success)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMISubvention.getId() })
    @Test
    void "test when max txn amt constraint is violated"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'PAY_TXN_AMT_CONDITION_BREACH'
        req().body(root).post().then()
                .spec(success)
    }
}