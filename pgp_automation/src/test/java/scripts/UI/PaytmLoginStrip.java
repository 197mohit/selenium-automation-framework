package scripts.UI;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Gagandeep")
public class PaytmLoginStrip  extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verify that login Strip is coming or not on cashier page when DISABLED_LOGIN_STRIP is set to Y")
    public void loginStripDisabledWhenPropLoginStripisY(@Optional("enhancedweb") String theme) throws Exception {


        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.LOGIN_STRIP_DISABLED, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that login Strip is Enabled on cashier page when DISABLED_LOGIN_STRIP is set to Null")
    public void loginStripVisableWhenPropLoginStripisNull(@Optional("enhancedweb") String theme) throws Exception {


        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.WalletOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that login Strip is coming on cashier page when DISABLED_LOGIN_STRIP is set to N")
    public void loginStripEnaableWhenPropLoginStripisN(@Optional("enhancedweb_revamp") String theme) throws Exception {


        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertVisible();
    }


    @Parameters({"theme"})
    @Test(description = "Verify that login Strip is disabled on cashier page when DISABLED_LOGIN_STRIP is set to N for Retry case")
    public void loginStripDisableWhenPropLoginStripisNonRetry(@Optional("enhancedweb") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.EMI_DISCOVERY, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectcc = new PaymentDTO();
        incorrectcc.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC,incorrectcc);
        cashierPage.waitUntilLoads();
        cashierPage.loginStrip().assertNotVisible();
    }
    @Parameters({"theme"})
    @Test(description = "Verify that when user mobile number entered already (checkout page) and prefrance set to Y")
    public void loginStripDisableWhenMobileNumberAlreadyEntered(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.LOGIN_STRIP_DISABLED, theme).setMSISDN("9812334455")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertNotVisible();
    }
    @Parameters({"theme"})
    @Test(description = "Verify that login strip is enabled when DISABLED_LOGIN_STRIP property is not present ")
    public void loginStripEnabledWhenEntryIsNotInDB(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertVisible();
    }


}
