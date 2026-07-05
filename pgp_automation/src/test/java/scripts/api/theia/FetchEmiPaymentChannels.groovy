package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.OrderV2
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_EMI_PAYMENT_CHANNELS
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchEmiPaymentChannels extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_EMI_PAYMENT_CHANNELS)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: m()?.orders?.last()?.id ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        txnToken        : m()?.orders?.with {
                            assert it.add(new OrderV2(1, user()?.tokens?.getAt('sso')?.id, null))
                            it.last()?.transaction?.token
                        },
                ],
                body: [:]
        ]
    }

    @Merchant(edit = true, value = {it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void testName() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body('requestId', nullValue(),
                'responseTimestamp', not(isEmptyOrNullString()),
                'version', equalTo('v1'))
                .root('body')
                .body('extraParamsMap', nullValue(),
                'emiPayOption', isA(Object.class))
                .root('body.emiPayOption')
                .body(
                'displayName', nullValue(),
                'isDisabled', nullValue(),
                'payChannelOptions', isA(List.class),
                'feeAmount', nullValue(),
                'taxAmount', nullValue(),
                'totalTransactionAmount', nullValue(),
                'priority', nullValue(),
                'onboarding', isA(Boolean.class),
                'paymentMode', nullValue(),
                'isHybridDisabled', isA(Boolean.class))
                .root('body.emiPayOption.payChannelOptions')
                .body(
                'isDisabled', everyItem(nullValue()),
                'hasLowSuccess', everyItem(nullValue()),
                'emiChannelInfos', everyItem(isA(List.class)),
                'isHybridDisabled', everyItem(isA(Boolean.class)))
                .root('body.emiPayOption.payChannelOptions.emiChannelInfos')
                .body('planId', everyItem(everyItem(not(isEmptyOrNullString()))),
                'interestRate', everyItem(everyItem(not(isEmptyOrNullString()))),
                'ofMonths', everyItem(everyItem(not(isEmptyOrNullString()))),
                'minAmount', everyItem(everyItem(allOf(hasKey('currency'), hasKey('value')))),
                'maxAmount', everyItem(everyItem(allOf(hasKey('currency'), hasKey('value')))),
                'emiAmount', everyItem(everyItem(allOf(hasKey('currency'), hasKey('value')))),
                'totalAmount', everyItem(everyItem(allOf(hasKey('currency'), hasKey('value')))))
    }
}
