package scripts.api.theia.fetchBinDetail

import com.paytm.apphelpers.PGPHelpers
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
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
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
class ChecksumFetchBinDetailV1Test extends FetchBinDetailTest {

    @Override
    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, setChecksumFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_BIN_DETAIL)
                .addQueryParam('mid', '?')
    }

    @Override
    RequestSpecification req() {
        given(reqBldr().build())
    }

    @Override
    Map root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'CHECKSUM',
                        token           : '?',
                ],
                body: [
                        bin: cards.find { it.type == 'debit' && !it.prepaid }.no[0..5],
                        mid: m()?.id,
                ]
        ]
    }

    Filter setChecksumFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = getChecksum(m().key, toJson(root.body))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
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

    private ResponseSpecification successSchema = new ResponseSpecBuilder()
            .expectBody('head', isA(Object.class))
            .expectBody('body', isA(Object.class))
            .rootPath('head')
            .expectBody('responseTimestamp', not(isEmptyOrNullString()))
            .expectBody('version', equalTo('v1'))
            .rootPath('body')
            .expectBody('resultInfo', isA(Object.class))
            .expectBody('emiChannel', anyOf(nullValue(), isA(Object.class)))
            .expectBody('errorMessage', anyOf(nullValue(), isA(String.class)))
            .expectBody('isEmiAvailable', isA(Boolean.class))
            .expectBody('binDetail', isA(Object.class))
            .expectBody('authModes', isA(List.class))
            .expectBody('hasLowSuccessRate', isA(Object.class))
            .expectBody('iconUrl', isA(String.class))
            .expectBody('isHybridDisabled', isA(Boolean.class))
            .expectBody('oneClickSupported', isA(Boolean.class))
            .expectBody('oneClickMaxAmount', isA(String.class))
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', not(isEmptyOrNullString()))
            .expectBody('resultCode', not(isEmptyOrNullString()))
            .expectBody('resultMsg', not(isEmptyOrNullString()))
            .rootPath('body.binDetail')
            .expectBody('bin', isA(String.class))
            .expectBody('issuingBank', isA(String.class))
            .expectBody('issuingBankCode', isA(String.class))
            .expectBody('paymentMode', isA(String.class))
            .expectBody('channelName', isA(String.class))
            .expectBody('channelCode', isA(String.class))
            .expectBody('cnMin', isA(String.class))
            .expectBody('cnMax', isA(String.class))
            .expectBody('cvvR', isA(String.class))
            .expectBody('cvvL', isA(String.class))
            .expectBody('expR', isA(String.class))
            .expectBody('isActive', isA(String.class))
            .expectBody('isIndian', isA(String.class))
            .rootPath('body.hasLowSuccessRate')
            .expectBody('status', isA(String.class))
            .expectBody('msg', isA(String.class))
            .build()

    private ResponseSpecification errorSchema = new ResponseSpecBuilder()
            .expectBody('', hasKey('head'))
            .expectBody('', hasKey('body'))
            .rootPath('head')
            .expectBody('', hasKey('responseTimestamp'))
            .expectBody('', hasKey('version'))
            .rootPath('body')
            .expectBody('', hasKey('resultInfo'))
            .expectBody('', not(hasKey('emiChannel')))
            .expectBody('', not(hasKey('isEmiAvailable')))
            .expectBody('', not(hasKey('binDetail')))
            .expectBody('', not(hasKey('authModes')))
            .expectBody('', not(hasKey('hasLowSuccessRate')))
            .expectBody('', not(hasKey('iconUrl')))
            .expectBody('', not(hasKey('errorMessage')))
            .expectBody('', not(hasKey('isHybridDisabled')))
            .expectBody('', not(hasKey('oneClickSupported')))
            .expectBody('', not(hasKey('oneClickMaxAmount')))
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', not(isEmptyOrNullString()))
            .expectBody('resultCode', not(isEmptyOrNullString()))
            .expectBody('resultMsg', not(isEmptyOrNullString()))
            .build()

    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test(description = "test unable to fetch bin details when head token is not provided")
    void 'test unable to fetch bin details when head token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test(description = "test unable to fetch bin details when head.token = null")
    void 'test unable to fetch bin details when token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test(description = "test unable to fetch bin details when head.token = ''")
    void "test unable to fetch bin details when token = ''"() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test(description = "test unable to fetch bin details when head.token equals to random value")
    void 'test unable to fetch bin details when token equals to random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchants([
            @Merchant(edit = true, value = { it.payModes.contains('dc') && !it.pcfEnabled }),
            @Merchant(edit = true, value = { it.payModes.contains('dc') && !it.pcfEnabled })
    ])
    @Test(description = "test unable to fetch bin details when head.token equals valid checksum generated by other merchant's key")
    void "test unable to fetch bin details when token equals valid checksum generated by other merchant's key"() {
        def root = root()
        root.head.token = getChecksum(m(1).key, toJson(root.body))
        req().body(root).post().then()
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test(description = "test unable to fetch bin details when mid in query params is not provided")
    void 'test unable to fetch bin details when mid in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(errorSchema)
                .spec(results.mIdAndOrderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true),
    ])
    @Test(description = "test unable to fetch bin details when body.mid not equals mid in query params")
    void 'test unable to fetch bin details when mid not equals mid in query params'() {
        def root = root()
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(errorSchema)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "test unable to fetch bin details when body.mid is not provided")
    void 'test unable to fetch bin details when mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "test unable to fetch bin details when body.mid = null")
    void 'test unable to fetch bin details when mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.orderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "test unable to fetch bin details when body.mid = '\"")
    void "test unable to fetch bin details when mid = '"() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "test unable to fetch bin details when body.mid equals random value")
    void 'test unable to fetch bin details when mid equals random value in body'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "test unable to fetch bin details when body.bin is not provided")
    void 'test unable to fetch bin details when bin is not provided'() {
        def root = root()
        root.body.remove('bin')
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidBinNo as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "test unable to fetch bin details when body.bin = null")
    void 'test unable to fetch bin details when bin = null'() {
        def root = root()
        root.body.bin = null
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidChecksum as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "test unable to fetch bin details when body.bin = '")
    void "test unable to fetch bin details when bin = '"() {
        def root = root()
        root.body.bin = ''
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidBinNo as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test(description = "est unable to fetch bin details when body.bin equals random alphanumeric value")
    void 'test unable to fetch bin details when bin equals random alphanumeric value'() {
        def root = root()
        root.body.bin = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(errorSchema)
                .spec(results.invalidBinNo as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test unable to fetch bin details when body.bin equals random numeric value")
    void 'test unable to fetch bin details when bin equals random numeric value'() {
        def root = root()
        root.body.bin = new Random().nextLong().abs().toString()
        req().body(root).post().then()
                .spec(successSchema)
                .spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals card no.")
    void 'test able to fetch bin details when bin equals card no'() {
        def root = root()
        root.body.bin = cards.find { !it.prepaid }.no
        req().body(root).post().then()
                .spec(successSchema)
                .spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals AMEX bin")
    void 'test able to fetch bin details when bin equals AMEX bin'() {
        def root = root()
        def card = cards.find { it.scheme == 'amex' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({ it.acquirings.any { !(it.bank in ['amex', 'bajajfn']) } && !it.pcfEnabled })
    @Test(description = "test able to fetch bin details when body.bin equals MAESTRO bin")
    void 'test able to fetch bin details when bin equals MAESTRO bin'() {
        def root = root()
        def card = cards.find { it.scheme == 'maestro' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals MASTER bin")
    void 'test able to fetch bin details when bin equals MASTER bin'() {
        def root = root()
        def card = cards.find { it.scheme == 'master' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals VISA bin")
    void 'test able to fetch bin details when bin equals VISA bin'() {
        def root = root()
        def card = cards.find { it.scheme == 'visa' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals DINERS bin")
    void 'test able to fetch bin details when bin equals DINERS bin'() {
        def root = root()
        def card = cards.find { it.scheme == 'diners' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals RUPAY bin")
    void 'test able to fetch bin details when bin equals RUPAY bin'() {
        def root = root()
        def card = cards.find { it.scheme == 'rupay' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals BAJAJFN bin")
    void 'test able to fetch bin details when bin equals BAJAJFN bin'() {
        def root = root()
        def card = cards.find { it.scheme == 'bajajfn' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals cc bin")
    void 'test able to fetch bin details when bin equals cc bin'() {
        def root = root()
        def card = cards.find { it.type == 'credit' && !it.prepaid}
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test(description = "test able to fetch bin details when body.bin equals dc bin")
    void 'test able to fetch bin details when bin equals dc bin'() {
        def root = root()
        def card = cards.find { it.type == 'debit' && !it.prepaid}
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant({it.id == Constants.MerchantType.WalletOnly.getId()})
    @Test(description = "test unable to fetch bin details when body.bin equals dc bin given merchant doesn't have dc pay method configured")
    void "test unable to fetch bin details when bin equals dc bin given merchant doesn't have dc pay method configured"() {
        def root = root()
        root.body.bin = cards.find { it.type == 'debit' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .spec(errorSchema)
                .root('body.resultInfo')
                .body(
                'resultStatus', equalTo('F'),
                'resultCode', equalTo('2011'),
                'resultMsg', endsWith('Debit card is not allowed for this payment. Please try paying using other cards/options.'))
    }

    @Merchant({it.id == Constants.MerchantType.PPBLYONLY.getId()})
    @Test(description = "test unable to fetch bin details when body.bin equals cc bin given merchant doesn't have cc pay method configured")
    void "test unable to fetch bin details when bin equals cc bin given merchant doesn't have cc pay method configured"() {
        def root = root()
        root.body.bin = cards.find { it.type == 'credit' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .spec(errorSchema)
                .root('body.resultInfo')
                .body(
                'resultStatus', equalTo('F'),
                'resultCode', equalTo('2011'),
                'resultMsg', endsWith('Credit card is not allowed for this payment. Please try paying using other cards/options.'))
    }
}
