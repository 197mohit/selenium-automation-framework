package scripts.api.theia;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.appconstants.Constants;

@Feature("PGP-38536")
public class OptOutPreferenceForQrLogin extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify only QR code is enabled on cashier page.")
    public void validateQRandLoginOnMerchant_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.QR_LOGIN_PREFERENCE_N, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.waitUntilLoads();
        String QR =cashierPage.QRCode().getText();
        Assertions.assertThat(QR).contains("Scan QR using Paytm or any UPI App");

    }

    @Owner("Himanshu Arora")
    @Test(description = "Verify both QR code and login strip is enabled on cashier page.")
    public void validateQRandLoginOnMerchant_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.QR_LOGIN_PREFERENCE_Y, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.waitUntilLoads();
        String QR =cashierPage.QRCode().getText();
        Assertions.assertThat(QR).contains("Scan QR using Paytm or any UPI App");
        String LOGINSTRIP = cashierPage.loginStripWithQR().getText();
        Assertions.assertThat(LOGINSTRIP).contains("Pay with Paytm");
    }

}
