package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

/**
 * Created by anjukumari on 17/01/19
 */
@Owner("Tarun")
public class FoodWalletTxns extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate Successfull txn from Food wallet when food wallet has sufficient txn balance also validate balance has been deducted from food wallet when txn is success", groups = {"smoke"})
    public void PGP_successfulFoodWalletTxnWithSufficientFoodBal(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.FOODWALLET);
        WalletHelpers.updateFoodWalletBalance(user, 2);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setSubwallet_Details("{\"FOOD\":2}")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        WalletHelpers.getFoodWalletBalance(user);
        new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble("0"));
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successfull txn from Food wallet when food wallet has insufficient txn balance also validate balance has been deducted from food wallet when txn is success", groups = {"smoke"})
    public void PGP_successfulFoodWalletTxn_gtInsuffFoodBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.FOODWALLET);
        WalletHelpers.updateFoodWalletBalance(user, 1);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setSubwallet_Details("{\"FOOD\":2}")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble("0"));
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.0);
    }

    @Feature("PGP-38887")
    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "validate PaymentRequestBean in theia logs & verify that MismatchedInputException error is not coming in logs.")
    public void mismatchedInputExceptionInSubwalletDetails(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.MISMATCHED_INPUT, theme, user)
                .setSubwallet_Details("{\"FOOD\":\"0\"}")
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +
                "grep \"" + orderDTO.getORDER_ID() + "\"  ";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).isNotEmpty();
        Assertions.assertThat(theiaLogs).doesNotContain("Error: com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `java.util.LinkedHashMap");
    }
}
