package scripts.api.theia.fetchBinDetail

import com.paytm.apphelpers.PGPHelpers
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL
import static io.restassured.RestAssured.given

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
class TxnTokenFetchBinDetailV1Test extends FetchBinDetailTest {

    @Override
    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_BIN_DETAIL)
                .addQueryParams([mid: m().id, orderId: m().orders.last().id])
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
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        txnToken        : {
                            assert m().orders.add(new OrderV2(2))
                            m().orders.last().transaction.token
                        }(),
                ],
                body: [
                        mid        : m().id,
                        bin        : cards.find { it.type == 'debit' && !it.prepaid }.no[0..5],
                        paymentMode: 'DC'
                ]
        ]
    }

    @Merchants([
            @Merchant(edit = true, value = { !!it.payModes.contains('dc') && !it.pcfEnabled }),
            @Merchant(edit = true, value = { !!it.payModes.contains('dc') && !it.pcfEnabled })
    ])
    @Test(description = 'test when txn token of other merchant used')
    final void testWhenTxnTokenOfOtherMerchantUsed() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).queryParam('mid', m(1).id).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }


    @Merchant(edit = true, value = {it.id == Constants.MerchantType.PCF_ONUS.getId()})
    @Test
    final void testWhenTxnTokenOfOtherOrderUsed() {
        def root = root()
        assert m().orders.add(new OrderV2(2))
        given(reqBldr().removeQueryParam('orderId').build()).queryParam('orderId', m().orders.last().id).body(root).post().then()
                .spec(results.orderIdInQueryParamNotMatchingWithOrderIdInRequest as ResponseSpecification)
    }
}
