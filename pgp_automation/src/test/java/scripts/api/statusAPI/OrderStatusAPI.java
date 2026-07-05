package scripts.api.statusAPI;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.api.BaseApi;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Deepak")
public class OrderStatusAPI extends PGPBaseTest {

    private static OrderDTO orderDTO;
//    private static final String theme = "enhancedweb";
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Test(description = "PG Only transaction for Order/Merchant Status check API")
    @Parameters({"theme"})
    private void PGOnlyTransaction(@Optional("enhancedweb_revamp") String theme) {
        orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
    }

    @Test(description = "Valid Merchant key with valid Order ID of different merchant", dependsOnMethods = "PGOnlyTransaction")
    @Parameters({"theme"})
    public void transactionStatusTC001_differentOrderId(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO1 = new OrderFactory.AddnPay(Constants.MerchantType.AddMoney, theme).build();
        TxnStatus txnStatus = new TxnStatus(orderDTO1.getMID(), orderDTO.getORDER_ID(), null, true).executeUntilNotPending();
        txnStatus.validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateMid(orderDTO1.getMID())
                .AssertAll();
    }

    @Test(description = "Invalid Merchant key with valid Order ID", dependsOnMethods = "PGOnlyTransaction")
    public void transactionStatusTC002_invalidMerchantKey() throws Exception {
        String merchantKey = "CiW9PcdA9VjuhWi";
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), merchantKey, true).executeUntilNotPending();
        txnStatus.validateStatus(Constants.ValidationType.NOT_PRESENT)
                .validateRespCode("330")
                .validateRespMsg("checksum is not valid")
                .validateOrderid(Constants.ValidationType.NOT_PRESENT)
                .validateMid(Constants.ValidationType.NOT_PRESENT)
                .AssertAll();
    }

    @Test(description = "Valid Merchant key with valid Order ID and no checksum", dependsOnMethods = "PGOnlyTransaction")
    public void transactionStatusTC003_noChecksum() throws Exception {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), null, false).executeUntilNotPending();
        txnStatus.validateStatus(Constants.ValidationType.NOT_PRESENT)
                .validateRespCode("330")
                .validateRespMsg("checksum is not valid")
                .validateOrderid(Constants.ValidationType.NOT_PRESENT)
                .validateMid(Constants.ValidationType.NOT_PRESENT)
                .AssertAll();
    }
}