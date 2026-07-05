package scripts.contractValidation

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.core.DriverManager
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.RefundV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.openqa.selenium.By
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given

@Owner("Deepak")
class OldRefundStatusAPI extends TestSetUp implements ContractTest {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/refund/status')
                        .build()
        )
    }

    final def root = {
        [
                MID         : null,
                ORDERID     : null,
                REFID       : null,
                CHECKSUMHASH: null,
        ]
    }

    List<String> resAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/refund-status-api/')
        resAttr = DriverManager.getDriver().findElements(By.cssSelector('#child > div > div.full-container.dual-wrapper.wrapper2 div:nth-child(12) > table tr  strong')).collect {
            it.text.trim()
        }
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        DriverManager.closeCurrentDriver()
    }


    @Merchant
    @Test
    void testResponseAttributesAreAsExpected() {
        def root = root()
        assert m().orders.add(new OrderV2(1))
        m().orders.last().payUsingNB()
        assertion.apply(txnStatusWait.apply({ m().orders.last().transaction.status == 'TXN_SUCCESS' }))
        assert m().orders.last().refunds.add(new RefundV2(m().orders.last().amt))
        root << [MID: m().id, ORDERID: m().orders.last().id, REFID: m().orders.last().refunds.last().id]
        root.CHECKSUM = getChecksum(m().key, toJson(root))
        req().body(root).post().then().statusCode(200).extract().path('').with {
            assert this.resAttr.containsAll(it.keySet())
        }
    }
}
