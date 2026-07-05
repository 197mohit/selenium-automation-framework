package scripts.contractValidation

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.core.DriverManager
import com.paytm.utils.merchant.merchant.util.OrderV2
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
class TransactionStatusApi extends TestSetUp implements ContractTest {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/order/status')
                        .build()
        )
    }

    final def root = {
        [
                MID         : null,
                ORDERID     : null,
                CHECKSUMHASH: null,
                TXNTYPE     : null,
        ]
    }

    List<String> resAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/transaction-status-api/')
        resAttr = DriverManager.getDriver().findElements(By.cssSelector('#child > div > div > div > div.txn-wrapper-module--api-wrapper--3iZr5 > div > div:nth-child(12) > table > tbody > tr > td > strong')).collect {
            it.getText()
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
        root.MID = m().id
        root.ORDERID = m().orders.last().id
        root.CHECKSUMHASH = getChecksum(m().key, toJson(root))
        req().body(root).post().then().statusCode(200).extract().path('').with {
            assert this.resAttr.containsAll(it.keySet())
        }
    }
}
