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
import static io.restassured.RestAssured.given

@Owner("Deepak")
class FetchPaymentOptionAPI extends TestSetUp implements ContractTest {

    final def req = { mId, orderId ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath('/fetchPaymentOptions')
                        .addQueryParams([mid: mId, orderId: orderId])
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        txnToken : null,
                ],
        ]
    }

    List<String> headAttr
    List<String> bodyAttr
    List<String> payOptionAttr
    List<String> payMethodAttr
    List<String> payChannelBaseAttr
    List<String> statusInfoAttr
    List<String> merchantDetailsAttr
    List<String> promoCodeDataAttr
    List<String> savedInstrumentsAttr
    List<String> cardDetailsAttr
    List<String> resultInfoAttr

    @BeforeClass
    void setUp() {
        DriverManager.getDriver().get('https://developer.paytm.com/docs/fetch-payment-options-api/')
        headAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Head']/following-sibling::div[1]/table[1]//strong")).collect {
            it.text.trim()
        }
        bodyAttr = DriverManager.getDriver().findElements(By.xpath("//h2[text() = 'Response Attributes']/following-sibling::h3[text()='Body']/following-sibling::div[1]/table[1]//strong")).collect {
            it.text.trim()
        }
        payOptionAttr = DriverManager.getDriver().findElements(By.cssSelector("#payOption table strong")).collect {
            it.text.trim()
        }
        payMethodAttr = DriverManager.getDriver().findElements(By.cssSelector("#payMethod table strong")).collect {
            it.text.trim()
        }
        payChannelBaseAttr = DriverManager.getDriver().findElements(By.cssSelector("#payChannelBase table strong")).collect {
            it.text.trim()
        }
        statusInfoAttr = DriverManager.getDriver().findElements(By.cssSelector("#statusInfo table strong")).collect {
            it.text.trim()
        }
        merchantDetailsAttr = DriverManager.getDriver().findElements(By.cssSelector("#merchantDetails table strong")).collect {
            it.text.trim()
        }
        promoCodeDataAttr = DriverManager.getDriver().findElements(By.cssSelector("#promoCodeData table strong")).collect {
            it.text.trim()
        }
        savedInstrumentsAttr = DriverManager.getDriver().findElements(By.cssSelector("#savedInstruments table strong")).collect {
            it.text.trim()
        }
        cardDetailsAttr = DriverManager.getDriver().findElements(By.cssSelector("#cardDetails table strong")).collect {
            it.text.trim()
        }
        resultInfoAttr = DriverManager.getDriver().findElements(By.cssSelector("#resultInfo table strong")).collect {
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
        root.head.txnToken = m().orders.last().transaction.token
        req(m().id, m().orders.last().id).body(root).post().then().statusCode(200).extract().path('').with {
            head.with {
                assert this.headAttr.containsAll(keySet())
            }
            body.with {
                assert this.bodyAttr.containsAll(keySet())
                [merchantPayOption, addMoneyPayOption]*.with {
                    assert this.payOptionAttr.containsAll(keySet())
                    paymentModes.each {
                        assert this.payMethodAttr.containsAll(keySet())
                        isDisabled.with {
                            assert this.statusInfoAttr.containsAll(keySet())
                        }
                        payChannelOptions.each {
                            assert this.payChannelBaseAttr.containsAll(keySet())
                            [isDisabled, hasLowSuccess]*.with {
                                assert this.statusInfoAttr.containsAll(keySet())
                            }
                        }
                    }
                    savedInstruments.each {
                        assert this.savedInstrumentsAttr.containsAll(keySet())
                        [isDisabled, hasLowSuccess]*.with {
                            assert this.statusInfoAttr.containsAll(keySet())
                        }
                        cardDetails.with {
                            assert this.cardDetailsAttr.containsAll(keySet())
                        }
                    }
                }
                [merchantDetails, addMoneyMerchantDetails]*.with {
                    assert this.merchantDetailsAttr.containsAll(keySet())
                }
                promoCodeData.with {
                    assert this.promoCodeDataAttr.containsAll(keySet())
                }
                resultInfo.with {
                    assert this.resultInfoAttr.containsAll(keySet())
                }
            }


        }
    }
}
