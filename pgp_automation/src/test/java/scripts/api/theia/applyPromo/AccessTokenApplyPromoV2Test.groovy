package scripts.api.theia.applyPromo

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.dto.PaymentDTO
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
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import scripts.api.theia.CreateToken
import com.paytm.appconstants.Constants
import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.APPLY_PROMO_V2
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class AccessTokenApplyPromoV2Test extends TestSetUp implements AccessTokenApplyPromoTest {

    private final static PROMOS_NOT_ADDED = "unable to add promo(s) successfully"
    private final static CARD_NOT_ADDED = "unable to add card(s) successfully"

    private final ThreadLocal<String> token = new ThreadLocal<>()
    private final ThreadLocal<String> referenceId = new ThreadLocal<>()

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(APPLY_PROMO_V2)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', UUID.randomUUID().toString())
                .addQueryParam('referenceId', referenceId.get())
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Map root() {
        [
                head: [
                        version         : 'v2',
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'ACCESS',
                        token           : token.get(),
                ],
                body: [
                        mid                   : m()?.id,
                        custId                : m()?.users?.get(0)?.id,//O
                        promocode             : null,//O
                        paymentOptions        : [
                                [
                                        transactionAmount: '2',
                                        payMethod        : 'WALLET'
                                ]
                        ],
                        totalTransactionAmount: '2',
                ]
        ]
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

    private final static class ResultInfo {
        static ResponseSpecification MID_OR_ORDERID_DOES_NOT_MATCH = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("Mid or OrderId doesn't match"))
                .build()
        static ResponseSpecification CUST_ID_PAYTM_USER_ID_CAN_NOT_BE_BLANK_FOR_TOKEN_TYPE_ACCESS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("CustId and paytm user id can't be blank for tokenType ACCESS"))
                .build()
        static ResponseSpecification TOKEN_VALIDATION_FAILED = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1002'))
                .expectBody('resultMsg', equalTo("TOKEN_VALIDATION_FAILED"))
                .build()
        static ResponseSpecification MISSING_MANDATORY_ELEMENT = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1007'))
                .expectBody('resultMsg', equalTo("Missing mandatory element."))
                .build()
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
        static ResponseSpecification NEW_PAY_TXN_AMT_ERR = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("PAY_TXN_AMT_ERR"))
                .build()
        static ResponseSpecification PAY_TXN_AMT_CONDITION_BREACH = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("pay transaction amount condition breach"))
                .build()
        static ResponseSpecification NEW_PAY_TXN_AMT_CONDITION_BREACH = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("PAY_TXN_AMT_CONDITION_BREACH"))
                .build()
        static ResponseSpecification NOT_VALID_ANYMORE = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("The offer you are trying to redeem is not valid anymore"))
                .build()
        static ResponseSpecification NEW_NOT_VALID_ANYMORE = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("NOT_VALID_ANYMORE"))
                .build()
        static ResponseSpecification NOT_PAY = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("not pay"))
                .build()
        static ResponseSpecification NEW_NOT_PAY = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("NOT_PAY"))
                .build()
        static ResponseSpecification NOT_VALID = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("The offer you are trying to redeem is not valid"))
                .build()
        static ResponseSpecification NEW_NOT_VALID = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("NOT_VALID"))
                .build()
        static ResponseSpecification NOT_APPLIED = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("The offer was not applied. Please try again"))
                .build()
        static ResponseSpecification NEW_NOT_APPLIED = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("NOT_APPLIED"))
                .build()
        static ResponseSpecification NO_BANK = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("no bank"))
                .build()
        static ResponseSpecification NEW_NO_BANK = new ResponseSpecBuilder()
                .rootPath('body')
                .expectBody('resultInfo.resultStatus', equalTo('S'))
                .expectBody('resultInfo.resultCode', equalTo('00000000'))
                .expectBody('paymentOffer.offerBreakup[0].promocodeApplied', equalTo("NO_BANK"))
                .build()
        static ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('S'))
                .expectBody('', hasKey('resultCodeId'))
                .expectBody('resultCodeId', nullValue())
                .expectBody('resultCode', equalTo('00000000'))
                .expectBody('resultMsg', equalTo('Success'))
                .build()
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @BeforeMethod
    void setAccessToken(Method method, ITestResult testResult) {
        try {
            CreateToken api = new CreateToken()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            referenceId.set(root.body.referenceId)
            api.req().body(root).post().then()
                    .spec(results.success as ResponseSpecification)
                    .extract().path('body.accessToken').tap { if (!it) throw new SkipException('unable to generate access token') }.with { token.set(it as String) }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.WalletOnly.getId() })
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @Test
    void 'test when body mid = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.FLAT_PCF.getId() })
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.TOKEN_VALIDATION_FAILED)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BANK_TRANSFER_MERCH.getId() })
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
    @Test
    void 'test when mid in query params is not equals to mid in request body'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.somethingWentWrong as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.NETBANK_PCF.getId() })
    @Test
    void 'test when body promocode is not provided given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.remove('promocode')
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body promocode = null given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.promocode = null
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_ONUS.getId() })
    @Test
    void 'test when body promocode = \'\' given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.promocode = ''
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLYONLY.getId() })
    @Test
    void 'test when body promocode equals random value given merchant has no promos'() {
        def root = root()
        m().promos.clear()
        root.body.promocode = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.noPromoCodeActive as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ZEST_MONEY.getId() })
    @Test
    void 'test when body promocode is not provided given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('promocode')
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_PCF.getId() })
    @Test
    void 'test when body promocode = null given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = null
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PaytmExpress_Hybrid_CCPayModeDisabled.getId() })
    @AUser
    @Test
    void 'test when body promocode = \'\' given merchant has applicable promos'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = ''
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.JIO.getId() })
    @Test
    void 'test when body totalTransactionAmount is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('totalTransactionAmount')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.UPI_INTENT.getId() })
    @Test
    void 'test when body totalTransactionAmount = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_MERCH.getId() })
    @Test
    void 'test when body totalTransactionAmount = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMISubvention.getId() })
    @Test
    void 'test when body totalTransactionAmount equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId() })
    @Test
    void 'test when body custId is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('custId')
        req().body(root).post().then()
                .spec(ResultInfo.CUST_ID_PAYTM_USER_ID_CAN_NOT_BE_BLANK_FOR_TOKEN_TYPE_ACCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MDR_PCF_MERCH.getId() })
    @Test
    void 'test when body custId = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = null
        req().body(root).post().then()
                .spec(ResultInfo.CUST_ID_PAYTM_USER_ID_CAN_NOT_BE_BLANK_FOR_TOKEN_TYPE_ACCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test
    void 'test when body custId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = ''
        req().body(root).post().then()
                .spec(ResultInfo.CUST_ID_PAYTM_USER_ID_CAN_NOT_BE_BLANK_FOR_TOKEN_TYPE_ACCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.UPI_CONSENT_PG.getId() })
    @Test
    void 'test when body custId equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.custId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.NATIVE_PROMO_HYBRID.getId() })
    @Test
    void 'test when body paymentOptions is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('paymentOptions')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId() })
    @Test
    void 'test when body paymentOptions = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.paymentOptions = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId() })
    @Test
    void 'test when body paymentOptions = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.paymentOptions = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @Test
    void 'test when body paymentOptions equals empty list'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.paymentOptions = []
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.NETBANK_PCF.getId() })
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
                .spec(ResultInfo.SUCCESS)
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
                        payMethod        : 'CREDIT_CARD',
                        savedCardId      : {
                          //  if (!user().savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_CUSTOM_RATE.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_HYBRID.getId() })
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
                           // if (!user().savedCards.add(cards.find { it.type == 'debit' })) throw new SkipException(CARD_NOT_ADDED)
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
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
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.UPI_INTENT.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_CUSTOM_RATE.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_MERCH.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_ONUS.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.JIO.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly_Retry.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.LINK_TRANSACTION.getId() })
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_HDFC.getId() })
    @Test
    void 'test when body promocode equals expired promocode'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_VALID_ANYMORE'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_NOT_VALID_ANYMORE)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId() })
    @Test
    void 'test when promo is not valid for payMethod'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_PAY'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_NOT_PAY)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId() })
    @Test
    void 'test when promo is not valid for mid'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_VALID'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_NOT_VALID)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly_Retry.getId() })
    @Test
    void 'test when promo is not applied by promo engine'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NOT_APPLIED'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_NOT_APPLIED)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @Test
    void 'test when promo is provided for different bank for which promo is nor created'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'NO_BANK'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_NO_BANK)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @AUser
    @Test
    void "test when promo is of cashback type"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'cashback'
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PCF_CUSTOM_RATE.getId() })
    @AUser
    @Test
    void "test when promo is of discount type"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'discount'
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MLV.getId() })
    @Test
    void 'test when head token is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('token')
        req().body(root).post().then()
                .spec(ResultInfo.MISSING_MANDATORY_ELEMENT)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId() })
    @Test
    void 'test when head token = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = null
        req().body(root).post().then()
                .spec(ResultInfo.MISSING_MANDATORY_ELEMENT)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId() })
    @Test
    void 'test when head token = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = ''
        req().body(root).post().then()
                .spec(ResultInfo.MISSING_MANDATORY_ELEMENT)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI.getId() })
    @Test
    void 'test when head token equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.UPI_CONSENT_HYB.getId() })
    @Test
    void "test when head requestId is not provided"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.UPI_CONSENT_PG.getId() })
    @Test
    void "test when head requestId = null"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestId = null
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId() })
    @Test
    void "test when head requestId equals random value"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    void "test when head requestTimestamp is not provided"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.FLAT_PCF.getId() })
    @Test
    void "test when head requestTimestamp = null"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_PCF.getId() })
    @Test
    void "test when head requestTimestamp equals random value"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @Test
    void "test when head channelId is not provided"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_NATIVE.getId() })
    @Test
    void "test when head channelId = null"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.channelId = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EMI_DC.getId() })
    @Test
    void "test when head channelId equals random value"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.ADD_MONEY_ALLPAYMODE_MERCH.getId() })
    @Test
    void "test when head tokenType is not provided"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.Redirectional_Native.getId() })
    @Test
    void "test when head tokenType = null"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BIN_IN_RESPONSE_HYBRID.getId() })
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT.getId() })
    @Test
    void "test when head tokenType equals random value"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant
    ])
    @Test
    void "test when head token equals access token generated by different merchant"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = m(1).id
        req().body(root).post().then()
                .spec(ResultInfo.TOKEN_VALIDATION_FAILED)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.NATIVE_PROMO_HYBRID.getId() })
    @Test
    void "test when head token equals access token generated for OFFUS user"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PAYTM_EXPRESS_ADDMONEY.getId() })
    @AUser
    @Test
    void "test when head token equals access token generated for ONUS user"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        req().body(root).post().then()
                .spec(ResultInfo.SUCCESS)
    }

//    @Issue('PGP-26887')
//    @Override
//    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
//    @AUser(edit = true)
//    @Test(enabled = false)
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
                .spec(ResultInfo.SUCCESS)
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
                .spec(ResultInfo.SUCCESS)
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
                .spec(ResultInfo.SUCCESS)
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
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PAYTM_EXPRESS_ADDMONEY.getId() })
    @Test
    void "test when body paymentOptions payMethod = BALANCE but user info not passed in request"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.totalTransactionAmount = '1'
        root.body.paymentOptions = [
                [
                        payMethod        : 'BALANCE',
                        transactionAmount: root.body.totalTransactionAmount
                ]
        ]
        root.body.promocode = 'TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_TXN_CANNOT_BE_CARRIED_OUT_WITH_WALLET)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @Test
    void "test when min txn amt constraint is violated"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'PAY_TXN_AMT_ERR'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_PAY_TXN_AMT_ERR)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ_HDFC.getId() })
    @Test
    void "test when max txn amt constraint is violated"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.promocode = 'PAY_TXN_AMT_CONDITION_BREACH'
        req().body(root).post().then()
                .spec(ResultInfo.NEW_PAY_TXN_AMT_CONDITION_BREACH)
    }
}
