package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import groovy.json.JsonSlurper
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import com.paytm.appconstants.Constants

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.INIT_TXN
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Link('https://wiki.mypaytm.com/pages/viewpage.action?spaceKey=PGP&title=Native+API+Contract+Details+II#NativeAPIContractDetailsII-/api/v1/initiateTransaction?mid=%3Cmid%3E&orderId=%3CorderId%3E')
class InitiateTransaction extends TestSetUp {

    final def req = { orderId = null ->
        given(
                new RequestSpecBuilder()
                        .addFilters([setSignatureFilter, setQueryParamOrderIdFilter])
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(INIT_TXN)
                        .addQueryParams(([mid: m().id, orderId: orderId ?: '?']))
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        version  : '',
                        channelId: 'WEB',
                        signature: '?',//M
                ],
                body: [
                        mid             : m().id,
                        orderId         : new Random().nextLong().abs() as String,
                        requestType     : 'Payment',
                        websiteName     : 'retail',
                        txnAmount       : [
                                currency: 'INR',
                                value   : 1
                        ],
                        userInfo        : [
                                custId: System.currentTimeMillis() as String
                        ],
                        paytmSsoToken   : user()?.tokens?.find { it.name == 'sso' }?.id,//O
                        promoCode       : null,//O
                        offlineFlow     : null,
                        orderPricingInfo: null,
                        aggMid          : m().id,//O
                        aggType         : 'ORDER_CREATOR',//O
                ]
        ]
    }

    def setSignatureFilter = [filter: { FilterableRequestSpecification req, res, ctx ->
        def root = new JsonSlurper().parseText(req.getBody())
        root?.head?.with {
            if (it?.signature == '?') it.signature = getChecksum(m().key, toJson(root.body))
        }
        req.body(root)
        ctx.next(req, res)
    }] as Filter

    def setQueryParamOrderIdFilter = [filter: { req, res, ctx ->
        if (req.getQueryParams()['orderId'] == '?') {
            req.removeQueryParam('orderId').queryParam('orderId', new JsonSlurper().parseText(req.getBody())?.body?.orderId ?: new Random().nextLong().abs() as String)
        }
        ctx.next(req, res)
    }] as Filter

    private final ResponseSpecification rootSchema = new ResponseSpecBuilder()
            .expectStatusCode(isIn(200))
            .expectContentType(ContentType.JSON)
            .expectBody('head', instanceOf(Object.class))
            .expectBody('body', instanceOf(Object.class))
            .build()

    private final ResponseSpecification headSchema = new ResponseSpecBuilder()
            .rootPath('head')
            .expectBody('responseTimestamp', instanceOf(String.class))
            .expectBody('version', instanceOf(String.class))
            .build()

    private final ResponseSpecification bodySchema = new ResponseSpecBuilder()
            .rootPath('body')
            .expectBody('resultInfo', instanceOf(Object.class))
            .build()

    private final ResponseSpecification resultInfoSchema = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', isOneOf('S', 'F', 'U'))
            .expectBody('resultCode', isOneOf('0000', '00000900', '1007', '1008', '2004', '2007', '2009', '2013', '2014'))
            .expectBody('resultMsg', isOneOf('Duplicate request, with same orderId is already in progres', 'Mid in the query param doesn’t match with the Mid send in the request', 'Missing mandatory element', 'OrderId in the query param doesn’t match with the OrderId send in the request', 'Pipe character is not allowed, SSO Token is invalid', 'System error', 'Txn amount is invalid', 'Success'))
            .build()

    final ResponseSpecification success = new ResponseSpecBuilder()
            .rootPath('head')
            .expectBody('signature', instanceOf(String.class))
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('S'))
            .expectBody('resultCode', equalTo('0000'))
            .expectBody('resultMsg', equalTo('Success'))
            .rootPath('body')
            .expectBody('txnToken', allOf(instanceOf(String.class), not(isEmptyString())))
            .expectBody('isPromoCodeValid', instanceOf(Boolean.class))
            .build()

    private final ResponseSpecification systemError = new ResponseSpecBuilder()
            .rootPath('head')
            .expectBody('signature', nullValue())
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('U'))
            .expectBody('resultCode', equalTo('00000900'))
            .expectBody('resultMsg', equalTo('System error'))
            .build()

    private final ResponseSpecification missingMandatoryParams = new ResponseSpecBuilder()
            .rootPath('head')
            .expectBody('signature', nullValue())
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('F'))
            .expectBody('resultCode', equalTo('1007'))
            .expectBody('resultMsg', equalTo('Missing mandatory element'))
            .build()

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test success when existent mid is supplied')
    void testWhenRealMidPassed() {
        def root = root()
        root.body.mid = m().id
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.NON_MUTUAL_FUND.getId()})
    @Test(description = 'test head attr is mandatory')
    void testHeadIsRequired() {
        def root = root()
        root.remove('head')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(systemError)
    }

    @Merchant({it.id == Constants.MerchantType.NON_MUTUAL_FUND.getId()})
    @Test(description = 'test body attr is mandatory')
    void testBodyIsRequired() {
        def root = root()
        root.remove('body')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(systemError)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test version attr is optional')
    void testVersionIsOptional() {
        def root = root()
        root.head.remove('version')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.NON_MUTUAL_FUND.getId()})
    @Test(description = 'test channel id attr is optional')
    void testChannelIdIsOptional() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test requestTimestamp attr is optional')
    void testRequestTimestampIsOptional() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.NON_MUTUAL_FUND.getId()})
    @Test(description = 'test clientId attr is optional')
    void testClientIdIsOptional() {
        def root = root()
        root.head.remove('clientId')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test signature attr is mandatory')
    void testSignatureIsRequired() {
        def root = root()
        root.head.remove('signature')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(systemError)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test requestType attr is mandatory')
    void testRequestTypeIsMandatory() {
        def root = root()
        root.body.remove('requestType')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(missingMandatoryParams)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test mid attr is mandatory')
    void testMidIsMandatory() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(missingMandatoryParams)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test orderId attr is mandatory')
    void testOrderIdIsMandatory() {
        def root = root()
        root.body.remove('orderId')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(missingMandatoryParams)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test websiteName attr is mandatory')
    void testWebsiteNameIsMandatory() {
        def root = root()
        root.body.remove('websiteName')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(missingMandatoryParams)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test txnAmount attr is mandatory')
    void testTxnAmountIsMandatory() {
        def root = root()
        root.body.remove('txnAmount')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(missingMandatoryParams)
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test userInfo attr is mandatory')
    void testUserInfoIsMandatory() {
        def root = root()
        root.body.remove('userInfo')
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .spec(systemError)
    }

    @Merchant(value = { it.id == Constants.MerchantType.Redirectional_Native.getId() },edit = true)
    @Test(description = 'test isPromoCodeValid == false when invalid promo is supplied')
    void testWhenNonExistentPromoCodeIsPassed() {
        def root = root()
        m().promos.clear()
        root.body.promoCode = 'qwwqwq'
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .body('body.isPromoCodeValid', equalTo(false))
    }

    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_PG.getId()})
    @Test(description = 'test txn < 1 not allowed')
    void testTxnOfLessThan1NotAllowed() {
        def root = root()
        root.body.txnAmount.value = 0
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema).spec(bodySchema).spec(resultInfoSchema)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('2007'),
                'resultMsg', equalTo('Txn amount is invalid')
        )
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when agg mid key is not passed')
    void testWhenAggMidKeyIsNotPassed() {
        def root = root()
        root.body.remove('aggMid')
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when agg mid key is passed as null and merchant not having mlv')
    void testWhenAggMidKeyIsPassedAsNullForMerchantNotHavingMLVConfigured() {
        def root = root()
        root.body.aggMid = null
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId()})
    @Test(description = 'test when agg mid key is passed as null and merchant having mlv')
    void testWhenAggMidKeyIsPassedAsNullForMerchantHavingMLVConfigured() {
        def root = root()
        root.body.aggMid = null
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when non existent mid is passed as agg mid')
    void testWhenNonExistentMidIsPassedAsAggMid() {
        def root = root()
        root.body.aggMid = [(0..5).collect {
            ['a'..'z', 'A'..'Z'].flatten().with { it[new Random().nextInt(it.size())] }
        }, (0..15).collect {
            (0..9).with { it[new Random().nextInt(it.size())] }
        }].flatten().join()
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('2006'),
                'resultMsg', equalTo('Mid is invalid'))
    }

    @Merchants([@Merchant(edit = true), @Merchant()])
    @Test(description = 'test when existent mid is passed as agg mid and checksum is created using child merchant key')
    void testWhenExistentMidIsPassedAsAggMidAndChecksumIsCreatedUsingChildMerchantKey() {
        def root = root()
        root.body.aggMid = m(1).id
        root.head.signature = getChecksum(m().key, toJson(root.body))
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('2005'),
                'resultMsg', equalTo('Checksum provided is invalid'))
    }

    @Merchants([@Merchant(edit = true), @Merchant()])
    @Test(description = 'test when existent mid is passed as agg mid and checksum created using agg merchant key')
    void testWhenExistentMidIsPassedAsAggMidAndChecksumCreatedUsingAggMerchantKey() {
        def root = root()
        root.body.aggMid = m(1).id
        root.head.signature = getChecksum(m(1).key, toJson(root.body))
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when offline flow key is not passed')
    void testWhenOfflineFlowKeyIsNotPassed() {
        def root = root()
        root.body.remove('offlineFlow')
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when offline flow key is passed as null')
    void testWhenOfflineFlowKeyIsPassedAsNull() {
        def root = root()
        root.body.offlineFlow = null
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when offline flow key is passed as false')
    void testWhenOfflineFlowKeyIsPassedAsFalse() {
        def root = root()
        root.body.offlineFlow = false
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when offline flow key is passed as true')
    void testWhenOfflineFlowKeyIsPassedAsTrue() {
        def root = root()
        root.body.offlineFlow = true
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when agg type key is not passed')
    void testWhenAggTypeKeyIsNotPassed() {
        def root = root()
        root.body.remove('aggType')
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when agg type key is passed as null')
    void testWhenAggTypeKeyIsPassedAsNull() {
        def root = root()
        root.body.aggType = null
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when agg type key is passed as ORDER_CREATOR')
    void testWhenAggTypeKeyIsPassedAsORDER_CREATOR() {
        def root = root()
        root.body.aggType = 'ORDER_CREATOR'
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when order pricing info is not passed')
    void testWhenOrderPricingInfoIsNotPassed() {
        def root = root()
        root.body.remove('orderPricingInfo')
        req().body(root).post().then()
                .spec(success)
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(description = 'test when order pricing info is passed')
    void testWhenOrderPricingInfoIsPassed() {
        def root = root()
        def orderTotalAmount = 10D
        root.body.orderPricingInfo = [
                orderTotalAmount: [value: orderTotalAmount as String, currency: 'INR'],
                amountInfoList  : [
                        [
                                amountType: 'MLV_AMOUNT',
                                amount    : [value: (orderTotalAmount - (root.body.txnAmount.value as Double)) as String, currency: 'INR'],
                        ]
                ]
        ]
        req().body(root).post().then()
                .spec(success)
    }


}
