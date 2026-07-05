package scripts.coft.theia;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class CoftConsentBox extends PGPBaseTest{
    public static Constants.MerchantType onusMid = Constants.MerchantType.CCB_ONUS;
    public static Constants.MerchantType offusGVEnabled = Constants.MerchantType.CCB_OFFUS_GVE;
    public static Constants.MerchantType offusTridConfigured = Constants.MerchantType.CCB_OFFUS_TRID_CONFIGURED;
    public static Constants.MerchantType offusTridNotConfigured = Constants.MerchantType.CCB_OFFUS_TRID_NOT_CONFIGURED;

    public static String bin1 = PaymentDTO.CCB_SUCCESS;
    public static String bin2 = PaymentDTO.CCB_FAILURE1;
    public static String bin3 = PaymentDTO.CCB_FAILURE2;

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is visible for bin with OnPaytm = true, isEligibleForCoft=true, IS_COFT_BIN_ELIGIBLE= true")
    public void onusSuccess() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(onusMid, "enhancedweb_revamp")
                .setSSO_TOKEN(user.ssoToken()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin1);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxSaveCard().isElementPresent()).isTrue();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is not visible for bin with OnPaytm = true, isEligibleForCoft=true, IS_COFT_BIN_ELIGIBLE= false")
    public void onusFailure1() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(onusMid, "enhancedweb_revamp")
                .setSSO_TOKEN(user.ssoToken()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin2);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxSaveCard().assertNotVisible();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is not visible for bin with OnPaytm = true, isEligibleForCoft=false, IS_COFT_BIN_ELIGIBLE= true")
    public void onusFailure2() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(onusMid, "enhancedweb_revamp")
                .setSSO_TOKEN(user.ssoToken()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin3);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxSaveCard().assertNotVisible();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is visible for bin with OnPaytm = false,GLOBAL_VAULT_COFT : Y, isEligibleForCoft=true, IS_COFT_BIN_ELIGIBLE= true")
    public void offusGVEnabledSuccess() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(offusGVEnabled, "enhancedweb_revamp")
                .setSSO_TOKEN(user.ssoToken()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin1);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxSaveCard().isElementPresent()).isTrue();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is not visible for bin with OnPaytm = false,GLOBAL_VAULT_COFT : Y, isEligibleForCoft=true, IS_COFT_BIN_ELIGIBLE= false")
    public void offusGVEnabledFailure1() throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(offusGVEnabled, "enhancedweb_revamp").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin2);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxSaveCard().assertNotVisible();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is not visible for bin with OnPaytm = false, GLOBAL_VAULT_COFT : Y isEligibleForCoft=false, IS_COFT_BIN_ELIGIBLE= true")
    public void offusGVEnabledFailure2() throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(offusGVEnabled, "enhancedweb_revamp").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin3);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxSaveCard().assertNotVisible();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is visible for bin with OnPaytm = false, isEligibleForCoft=true, IS_COFT_BIN_ELIGIBLE= true and trid is configured")
    public void offustridConfiguredSuccess() throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(offusTridConfigured, "enhancedweb_revamp").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin1);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxSaveCard().isElementPresent()).isTrue();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is not visible for bin with OnPaytm = false, isEligibleForCoft=true, IS_COFT_BIN_ELIGIBLE= false and and trid is configured")
    public void offustridConfiguredFailure1() throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(offusTridConfigured, "enhancedweb_revamp").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin2);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxSaveCard().assertNotVisible();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is not visible for bin with OnPaytm = false, isEligibleForCoft=false, IS_COFT_BIN_ELIGIBLE= true and and trid is configured")
    public void offustridConfiguredFailure2() throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(offusTridConfigured, "enhancedweb_revamp").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin3);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxSaveCard().assertNotVisible();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-43454")
    @Test(description = "Verify save card checkbox is not visible for bin with OnPaytm = false, isEligibleForCoft=true, IS_COFT_BIN_ELIGIBLE= true and and trid is not configured")
    public void offustridNotConfiguredFailure() throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(offusTridNotConfigured, "enhancedweb_revamp").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(bin1);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxSaveCard().assertNotVisible();
    }

}
