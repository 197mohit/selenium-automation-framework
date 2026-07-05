package scripts.UI;


import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


@Feature("PGP-39945")
public class CorrectingNbDisplayNames extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "validate Paytm Net Banking is displayed on cashier page.")
    public void validatePaytmNetBanking(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO NBorderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PPBL_NB, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(NBorderDTO);
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        String bankName = cashierPage.firstBank().getText();
        Assertions.assertThat(bankName).contains("Paytm");
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "validate SBI Net Banking is displayed on cashier page.")
    public void validateSbiNetBanking(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO NBorderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PPBL_NB, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(NBorderDTO);
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        String bankName = cashierPage.sbiBank().getText();
        Assertions.assertThat(bankName).contains("SBI");
    }


    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "validate SBI Net Banking is displayed on cashier page.")
    public void validateKotakNetBanking(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO NBorderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PPBL_NB, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(NBorderDTO);
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        String bankName = cashierPage.kotakBank().getText();
        Assertions.assertThat(bankName).contains("Kotak");
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "validate SBI Net Banking is displayed on cashier page.")
    public void validateAxisNetBanking(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO NBorderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PPBL_NB, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(NBorderDTO);
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        String bankName = cashierPage.axisBank().getText();
        Assertions.assertThat(bankName).contains("AXIS");
    }

}
