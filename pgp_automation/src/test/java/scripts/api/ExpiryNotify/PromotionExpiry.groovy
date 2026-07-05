package scripts.api.ExpiryNotify

import com.paytm.api.SMSPrimary
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.joda.time.DateTime
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NotificationService.GIFTVOUCHER_EXPIRY
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

class PromotionExpiry extends TestSetUp {


    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GIFTVOUCHER_EXPIRY)
                        .build()
        )
    }

    final def root = {
        [
                'request'  : [
                        'head': [
                                'version' : '1.1.1',
                                'function': 'MGV_EXPIRY',
                                'clientId': '20171213',
                                'reqTime' : DateTime.now() as String,
                                'reqMsgId': new Random().nextLong().abs() as String,
                        ],
                        'body': [
                                'merchantId': '',
                                'expireCard':
                                        [
                                                'voucherId'        : '',
                                                'gvAmountAvailable': [
                                                        'currency': 'INR',
                                                        'value'   : '100',
                                                ],
                                                'gvAmountExpire'   : [
                                                        'currency': 'INR',
                                                        'value'   : '100',
                                                ],
                                                'purchaseDate'     : '2019-12-27T12:12:12'
                                        ],
                                'mobileNum' : '',
                                'expiryDate': '2020-01-28T12:12:12',
                                "emailId"   : "abhishek7.verma@paytm.com",
                                "expiryType": "",
                                "extendInfo": ""
                        ]
                ],
                'signature': 'no-signature',
        ]
    }

    final ResponseSpecification success = new ResponseSpecBuilder()
            .rootPath('response.body.resultInfo')
            .expectBody('resultStatus', equalTo('S'))
            .expectBody('resultCodeId', equalTo('00000000'))
            .expectBody('resultCode', equalTo('SUCCESS'))
            .expectBody('resultMsg', equalTo('Success'))
            .build()


    @Merchant
    @AUser
    @Test(description = "test User Receive SMS If Phone No. Provided for PRE_EXPIRY")
    void 'test User Receive SMS If Phone No Provided for PRE_EXPIRY'() {
        def voucherId = new Random().nextLong().abs() as String
        def root = root()
        root.request.body.expireCard.voucherId = voucherId
        root.request.body.merchantId = m().alipayId
        root.request.body.mobileNum = user().mobile
        root.request.body.expiryType='PRE_EXPIRY'
        req().body(root).post().then()
                .spec(success)

        SMSPrimary smsPrimary = new SMSPrimary(voucherId);

        txnStatusWait.apply( {smsPrimary.execute().jsonPath().getString("mobileNo").contains(user().mobile)})

        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("mobileNo"))
                .as("SMS is going to wrong mobile number").contains(user().mobile)
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("message"))
                .as( "message is incorrect").contains("Gift Voucher(Id = $voucherId) " +
                "purchased on Dec 27, 2019 worth Rs. 1.00 is going to expire on Jan 28, 2020. Shop now!")

    }



    @Merchant
    @AUser
    @Test(description = "test if Mobile No. not provided no msg will be sent to user for PRE_EXPIRY")
    void 'test if Mobile No not provided no msg will be sent to user for PRE_EXPIRY'() {
        def voucherId = new Random().nextLong().abs() as String
        def root = root()
        root.request.body.expireCard.voucherId = voucherId
        root.request.body.merchantId = m().alipayId
        root.request.body.expiryType='PRE_EXPIRY'
        req().body(root).post().then()
                .spec(success)
        SMSPrimary smsPrimary = new SMSPrimary(voucherId);
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("mobileNo"))
                .as("SMS is still going").isEqualTo("No mobile number found")
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("message"))
                .as( "message is incorrect").contains("Did n't get any logs in comm gateway, please check comm " +
                "gateway for orderid : "+voucherId)

    }


    @Merchant
    @AUser
    @Test(description = "test User Receive SMS If Phone No. Provided for POST_EXPIRY")
    void 'test User Receive SMS If Phone No Provided for POST_EXPIRY'() {
        def voucherId = new Random().nextLong().abs() as String
        def root = root()
        root.request.body.expireCard.voucherId = voucherId
        root.request.body.merchantId = m().alipayId
        root.request.body.mobileNum = user().mobile
        root.request.body.expiryType='POST_EXPIRY'
        req().body(root).post().then()
                .spec(success)

        SMSPrimary smsPrimary = new SMSPrimary(voucherId);

        txnStatusWait.apply( {smsPrimary.execute().jsonPath().getString("mobileNo").contains(user().mobile)})

        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("mobileNo"))
                .as("SMS is going to wrong mobile number").contains(user().mobile)
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("message"))
                .as( "message is incorrect").contains("Gift Voucher (Id = $voucherId)" +
                " purchased on Dec 27, 2019 worth Rs. 1.00 has expired on Jan 28, 2020.")

    }



    @Merchant
    @AUser
    @Test(description = "test if Mobile No. not provided no msg will be sent to user for POST_EXPIRY")
    void 'test if Mobile No not provided no msg will be sent to user for POST_EXPIRY'() {
        def voucherId = new Random().nextLong().abs() as String
        def root = root()
        root.request.body.expireCard.voucherId = voucherId
        root.request.body.merchantId = m().alipayId
        root.request.body.expiryType='POST_EXPIRY'
        req().body(root).post().then()
                .spec(success)
        SMSPrimary smsPrimary = new SMSPrimary(voucherId);
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("mobileNo"))
                .as("SMS is still going").isEqualTo("No mobile number found")
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("message"))
                .as( "message is incorrect").contains("Did n't get any logs in comm gateway, please check comm " +
                "gateway for orderid : "+voucherId)

    }

}