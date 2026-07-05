package scripts;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group.Status;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.base.test.Group.Feature;
import static com.paytm.base.test.Group.Theme;

@Test(groups = Feature.IDEBIT, enabled = false)
@Owner("Deepak")
public class Idebit extends PGPBaseTest {

    private final static String IDEBIT_CARD_NUMBER = "5166400031031058";
    private final static String IDEBIT_CVV = "918";
    private final static String IDEBIT_EXP_MONTH = "07";
    private final static String IDEBIT_EXP_YEAR = "2022";

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final ICICIBankPage iciciBankPage = new ICICIBankPage();

    @Parameters({"theme"})
    @Test(description = "Check CVV field is not displayed for saved IDebit card when ATM pin is selected",
            groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},enabled = false)
    public void checkCVVFieldNotDisplayedForSavedIDebitCardWhenATMPinSelected(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, IDEBIT_EXP_MONTH, IDEBIT_EXP_YEAR, IDEBIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICICI_IDEBIT_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.rdbtnATMPin().select();
        cashierPage.textBoxSavedCardCVV().assertNotVisible();
    }


    @Parameters({"theme"})
    @Test(description = "Check CVV field is displayed for saved IDebit card when 3D secure option is selected",
            groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},enabled = false)
    public void checkCVVFieldDisplayedForSavedIDebitCardWhen3DSecureOptionSelected(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, IDEBIT_EXP_MONTH, IDEBIT_EXP_YEAR, IDEBIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICICI_IDEBIT_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.pause(1);//waiting for the animation in enhancedwap to complete so that rdbtn3DSecurePin is selected instead of textBoxSavedCardCVV getting clicked
        cashierPage.rdbtn3DSecurePin().select();
        cashierPage.textBoxSavedCardCVV().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Check Atm Pin is not displayed for saved IDebit card when IDebit channel is not configured on merchant",
            groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},enabled = false)
    public void checkAtmPinNotDisplayedForSavedIDebitCardWhenIDebitChannelNotConfiguredOnMerchant(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, IDEBIT_EXP_MONTH, IDEBIT_EXP_YEAR, IDEBIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.rdbtnATMPin().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Check ATM pin option visible on ICICI Bank page when txn initiated with saved IDebit card using Atm pin",
            groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},enabled = false)
    public void checkPaymentWithSavedIDebitCardUsingAtmPin(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, IDEBIT_EXP_MONTH, IDEBIT_EXP_YEAR, IDEBIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICICI_IDEBIT_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.rdbtnATMPin().select();
        cashierPage.buttonPGPayNow().click();
        iciciBankPage.waitUntilLoads();
        iciciBankPage.fieldAtmPin().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Check payment with saved IDebit card using 3D secure",
            groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB, Status.TO_BE_FIXED},enabled = false) //TODO Nikunj will update icici urls in db
    public void checkPaymentWithSavedIDebitCardUsing3DSecure(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, IDEBIT_EXP_MONTH, IDEBIT_EXP_YEAR, IDEBIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICICI_IDEBIT_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.pause(1);//waiting for the animation in enhancedwap to complete so that rdbtn3DSecurePin is selected instead of textBoxSavedCardCVV getting clicked
        cashierPage.rdbtn3DSecurePin().select();
        cashierPage.textBoxSavedCardCVV().clearAndType(IDEBIT_CVV);
        cashierPage.buttonPGPayNow().click();
        iciciBankPage.waitUntilLoads();
        iciciBankPage.fieldOtp().assertVisible();
    }
}
