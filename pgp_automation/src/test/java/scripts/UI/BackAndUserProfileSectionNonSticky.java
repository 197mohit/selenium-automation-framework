package scripts.UI;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Karmvir")
@Feature("PGP-15780")
@Story(Constants.Sprint.SPRINT27_1)
public class BackAndUserProfileSectionNonSticky extends PGPBaseTest{
    @Parameters({"theme"})
    @Test(description = "User details and back button should also scroll when user scroll down cashier page")
    public void backButtonAndUserProfileNineSticky(@Optional("enhancedwap") String theme) throws Exception {
     User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO= new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme ,user).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       cashierPage.waitUntilLoads();
       cashierPage.getUserPrfile().assertVisible();
       Thread.sleep(3000);
       cashierPage.scrollToElement(cashierPage.tabNetBanking());;
       Thread.sleep(2000);
       Assertions.assertThat(cashierPage.getUserPrfile().isDisplayed() == false);

    }

}
