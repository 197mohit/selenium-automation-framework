package scripts.merchantStatus;

import com.paytm.api.OrderStatus;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.AJEESH;

/**
 * E2E merchant-status tests that exercise promo/cashback-enabled merchants.
 *
 * Pre-conditions:
 *   These tests require a promo-enabled merchant configured in the environment.
 *   Set the following system properties before enabling:
 *     -Dpromo.mid=<MID>
 *     -Dpromo.merchant.key=<KEY>
 *
 * All tests are disabled until a promo merchant is provisioned in staging.
 */
public class PromoMerchantStatusTests extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();

    /**
     * MS-NEW-24
     * Verify getTxnStatus includes promo/discount fields for a CC transaction
     * where a promo code was applied at checkout.
     *
     * Disabled until a promo-enabled merchant and discount code are available in staging.
     */
    @Owner(AJEESH)
    @Feature("MS-PROMO-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify getTxnStatus returns expected fields for a CC txn with promo applied.",
            enabled = false)
    public void txnStatus_PromoApplied_Success(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String promoMid = System.getProperty("promo.mid", "");
        String promoMerchantKey = System.getProperty("promo.merchant.key", "");

        Reporter.report.info("Testing getTxnStatus for promo merchant mid=" + promoMid);

        // Build and place order with promo merchant
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("STATUS")).as("STATUS").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("TXNID")).as("TXNID").isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).as("ORDERID").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).as("TXNAMOUNT").isNotEmpty();
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).as("PAYMENTMODE").isEqualToIgnoringCase("CC");
        // TODO: add promo-specific field assertions once the promo merchant response schema is confirmed
        softly.assertAll();
    }

    /**
     * MS-NEW-25
     * Verify v2/order/status returns the correct paymentMode and status fields
     * for a CC transaction on a promo-enabled merchant.
     *
     * Disabled until a promo-enabled merchant is available in staging.
     */
    @Owner(AJEESH)
    @Feature("MS-PROMO-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify v2/order/status returns expected fields for a CC txn on a promo merchant.",
            enabled = false)
    public void txnStatus_PromoApplied_v2OrderStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String promoMid = System.getProperty("promo.mid", "");

        Reporter.report.info("Testing v2/order/status for promo merchant mid=" + promoMid);

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("body.txnStatus").toString()).as("txnStatus").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().get("body.paymentMode").toString()).as("paymentMode").isEqualToIgnoringCase("CC");
        softly.assertThat(response.jsonPath().get("body.txnId").toString()).as("txnId").isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.orderId").toString()).as("orderId").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("resultStatus").isEqualToIgnoringCase("S");
        // TODO: add promo-specific field assertions once the promo merchant response schema is confirmed
        softly.assertAll();
    }
}