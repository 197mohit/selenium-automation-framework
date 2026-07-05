package scripts.UI;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.emiSubvention.ApiV1InitTxn.EmiSubventionInitTXN;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.utils.PropertyUtil;
import com.paytm.pages.*;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Idebit;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.HIMANSHU;

@Owner("Gagandeep")
public class UITests extends PGPBaseTest {

    private final static String IDEBIT_CARD_NUMBER = "5166400031031058";
    private final static String IDEBIT_CVV = "918";
    private final static String IDEBIT_EXP_MONTH = "07";
    private final static String IDEBIT_EXP_YEAR = "2022";

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final PropertyUtil propertyUtil = PropertyUtil.getInstance();
    private static String getMerchantId(String merchantType) {
        return propertyUtil.getValue(merchantType);
    }

    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();

//    @Parameters({"browser", "platform", "mobileEmulation"})
//    @BeforeMethod
//    public void setBrowserBeforeMethods(@Optional("") String browser, @Optional("") String platform, @Optional("") String mobileEmulation) {
//        super.setBrowserBeforeMethods(browser, platform, "Nexus 5");
//    }

    @BeforeClass
    public void loadMerchants() {
        propertyUtil.load("merchant.properties");
    }


    @Parameters({"theme"})
    @Test(description = "Verify wallet is appearing and default selected on page.", groups = {"merchantLow5", "merchant4"})
    public void TC_001_verifyWalletAppearingAndEnabled(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user).build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().assertEnabled();
        Assertions.assertThat(cashierPage.isPPIChecked()).isTrue().as("Wallet is not checked by default");
    }

    @Parameters({"theme"})
    @Test(description = "Verify Wallet and PPBL are appearing and PPI, PPBL are default selected on page load", groups = {"merchantLow5", "merchant4"})
    public void TC_002_verifyPpiAndPpblSelected(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL"), theme, user)
                .setTXN_AMOUNT("2")
                .build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().assertEnabled();
        Assertions.assertThat(cashierPage.isPPIChecked()).isTrue().as("Wallet is not checked by default");
        Assertions.assertThat(cashierPage.checkboxPPBL().isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.isPPBLChecked()).isTrue().as("PPBL is not selected by default");

    }

    @Parameters({"theme"})
    @Test(description = "Verify Postpaid is showing and default selected when PPI balance+PPBL balance < txn amount Postpaid Limit > txn amount", groups = {"merchantLow5", "merchant4"})
    public void TC_003_verifyPostpaidDefaultSelected(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_POSTPAID"), theme, user)
                .setTXN_AMOUNT("80001")
                .build();
        WalletHelpers.modifyBalance(user, 1.00);
        PostpaidHelpers.updateBalance("85000.00");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.isPaytmCCChecked()).isTrue().as("Post paid is not checked by default");
    }

    @Parameters({"theme"})
    @Test(description = "Verify wallet is not showing when wallet balance is 0", groups = {"merchantLow5"})
    public void TC_004_verifyWalletNotShowingForWalletBalance0(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_POSTPAID"), theme, user).build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Pay button should remain disabled, until pass code is blank or pass code length < 4 for PPBL", groups = {"merchantLow5"})
    public void TC_10_verifyPPBLPayButtonDisabled(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_POSTPAID"), theme, user)
                .setTXN_AMOUNT("2")
                .build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.checkboxPPBL().isDisplayed()).isTrue();
        cashierPage.buttonPpblSumbit().assertDisabled();
    }

    //TODO: need to update verification
    @Parameters({"theme"})
    @Test(description = "Verify without CVV transaction is not getting submit", groups = {"merchantLow5", "merchant4"})
    public void TC_049_verifyTxnNotAllowedWithoutCVVForCC(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCvvNumber(null);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        switch (theme.toLowerCase()) {
            case Constants.Theme
                    .MERCHANT4:
                Assertions.assertThat(cashierPage.lblCVV().getCssValue("color")).isEqualTo("rgba(51, 51, 51, 1)");
                break;
            case Constants.Theme
                    .MERCHANTLOW5:
                Assertions.assertThat(cashierPage.lblCVV().getCssValue("color")).isEqualTo("rgba(255, 0, 0, 1)");
                break;
        }
    }

    //TODO: need to update verification
    @Parameters({"theme"})
    @Test(description = "Verify without expiry transaction is not getting submit", groups = {"merchantLow5", "merchant4"})
    public void TC_50_verifyTxnNotAllowedWithoutExpDateForCC(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setExpMonth(null).setExpYear(null);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        switch (theme.toLowerCase()) {
            case Constants.Theme
                    .MERCHANT4:
                Assertions.assertThat(cashierPage.lblExpMonth().getCssValue("color")).isEqualTo("rgba(51, 51, 51, 1)");
                break;
            case Constants.Theme
                    .MERCHANTLOW5:
                Assertions.assertThat(cashierPage.lblExpMonth().getCssValue("color")).isEqualTo("rgba(255, 0, 0, 1)");
                break;
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify Pay button should only be enabled after entering 4 digit passcode", groups = {"merchantLow5"})
    public void TC_011_verifyPPBLPayButtonEnabled(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPBL_ONLY"), theme, user).setTXN_AMOUNT("2").build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxPPBLPassCode().sendKeys("1234");
        cashierPage.buttonPpblSumbit().assertEnabled();
    }

    @Parameters({"theme"})
    @Test(description = "Verify invalid card number message is showing when card number < 15 for AMEX", groups = {"merchantLow5", "merchant4"})
    public void TC_051_verifyInvalidCardNoMsgDisplayedForAmexWhenCardNoLengthLessThan15(
            @Optional("merchant4") String theme) {
        invalidCardScenario(theme, "37108520776634");
    }

    @Parameters({"theme"})
    @Test(description = "Verify invalid card number message is showing when card number < 16 for VISA", groups = {"merchantLow5", "merchant4"})
    public void TC_052_verifyInvalidCardNoMsgDisplayedForVisaWhenCardNoLengthLessThan16(
            @Optional("merchant4") String theme) {
        invalidCardScenario(theme, "471865010001033");
    }

    @Parameters({"theme"})
    @Test(description = "Verify invalid card number message is showing when card number < 16 for MasterCard", groups = {"merchantLow5", "merchant4"})
    public void TC_052_verifyInvalidCardNoMsgDisplayedForMasterCardWhenCardNoLengthLessThan16(@Optional("merchantLow5") String theme) {
        invalidCardScenario(theme, "538348435013482");
    }

    @Parameters({"theme"})
    @Test(description = "Verify PPBL is selected when wallet balance is less than transaction amount", groups = {"merchantLow5", "merchant4"})
    public void TC_006_verifyPpblSelected(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_POSTPAID"), theme, user).setTXN_AMOUNT("10.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkboxPPBL().assertEnabled();
        Assertions.assertThat(cashierPage.isPPBLChecked()).isTrue().as("PPBl is not selected by default");
    }

    @Parameters({"theme"})
    @Test(description = "Verify PPBL is disabled and grayed out", groups = {"merchantLow5", "merchant4"})
    public void TC_007_verifyPpblDisabled(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPBL_ONLY"), theme, user).setTXN_AMOUNT("60000").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //       cashierPage.checkboxPPBL().assertNotVisible();
        Assertions.assertThat(cashierPage.isPPBLChecked()).isFalse().as("PPBL is enabled");
        cashierPage.validateInsufficientIcon(theme).AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Wallet and VPA is selected on page load", groups = {"merchantLow5"})
    public void TC_100_verifyWalletVPASelected(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user)
                .setTXN_AMOUNT("22.00").build();
        WalletHelpers.modifyBalance(user, 20.00);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.isPPIChecked()).isTrue().as("Wallet is not checked by default");
        WebElement upiPushChkbox = cashierPage.checkboxVPAList().get(0);
        Assertions.assertThat(upiPushChkbox.isSelected()).as("is UPI push selected");
        Assertions.assertThat(upiPushChkbox.getAttribute("checked")).as("UPI push has attribute checked");
    }

    private void invalidCardScenario(String theme, String invalidCardNo) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(invalidCardNo);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String errMsg = cashierPage.paymentContainer().getText();
        Assertions.assertThat(errMsg)
                .contains(Constants.MessageAssert.INVALID_CARD_NUMBER.toString());
    }

    //TODO: need to fix test case
    @Parameters({"theme"})
//	@Test(description = "Verify Postpaid is disabled and grayed out when txn amnt is greater than postpaid limit")
    public void verifyPostpaidDisabled(@Optional("merchantLow5") String theme) throws Exception {
        PostpaidHelpers.updateBalance("50");
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PPBLYONLY, theme, user).setTXN_AMOUNT("60000").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        // TODO: assertion needs to be applied for postpaid
        // uncomment @Test
        cashierPage.ppblBalanceCheck().assertDisabled();
        cashierPage.validateInsufficientIcon(theme).AssertAll();
    }

    @Parameters({"theme"})
//	@Test(description = "Verify same payment method should be selected after cancel transaction from bank page")
    public void verifySamePayMthdSelected(@Optional("merchantLow5") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
        //TODO: Bank page not yet retrieved so test case not successful
        // uncomment @Test
    }

    @Parameters({"theme"})
    @Test(description = "Verify invalid card number message is showing when card number < 16 for Diners Club", groups = {"merchantLow5", "merchant4"})
    public void TC_052_verifyInvalidCardNoMsgDisplayedForDinersClubWhenCardNoLengthLessThan16(
            @Optional("merchant4") String theme) {
        invalidCardScenario(theme, "300528663023774");
    }

    @Parameters({"theme"})
    @Test(description = "Verify invalid card number message is showing when card number < 16 for Rupay", groups = {"merchantLow5", "merchant4"})
    public void TC_052_verifyInvalidCardNoMsgDisplayedForRupayWhenCardNoLengthLessThan16(
            @Optional("merchantLow5") String theme) {
        invalidCardScenario(theme, PaymentDTO.INVALID_CARD);
    }

    private void invalidUPIScenario(String theme, String invalidVpa) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setVpa(invalidVpa);
        cashierPage.payBy(Constants.PayMode.UPI, paymentDTO);
        String errMsg = cashierPage.paymentContainer().getText();
        errMsg = errMsg.replace("\n", "");
        switch (theme.toLowerCase()) {
            case Constants.Theme
                    .MERCHANT4:
                Assertions.assertThat(errMsg.contains("Enter your Virtual Payment Address")).isTrue().as("Validation message failed");
                break;
            case Constants.Theme
                    .MERCHANTLOW5:
                Assertions.assertThat(errMsg)
                        .contains("You have entered an invalid VPA. Please check and try again.");
                break;
        }
    }

    private void invalidUPIScenario(String theme, String invalidVpa, String textToValidate) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setVpa(invalidVpa);
        cashierPage.payBy(Constants.PayMode.UPI, paymentDTO);
        String errMsg = cashierPage.paymentContainer().getText();
        errMsg = errMsg.replace("\n", "");
        errMsg = errMsg.replace(" ", "");
        textToValidate = textToValidate.replace(" ", "");
        Assertions.assertThat(errMsg)
                .contains(textToValidate);
    }

    @Parameters({"theme"})
    @Test(description = "Verify without VPA page is not getting submit", groups = {"merchantLow5", "merchant4"})
    public void TC_053_verifyTxnNotAllowedWithoutVPA(@Optional("merchant4") String theme) {
        String merchant4ErrorText = "Enter your Virtual Payment Address (VPA)";
        switch (theme.toLowerCase()) {
            case Constants.Theme
                    .MERCHANT4:
                invalidUPIScenario(theme, "", merchant4ErrorText);
                break;
            case Constants.Theme
                    .MERCHANTLOW5:
                invalidUPIScenario(theme, "");
                break;
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify CVV number is 4 for AMEX", groups = {"merchantLow5", "merchant4"})
    public void TC_047_verifyOnly4digitsCVVAllowedForAMEX(@Optional("merchantLow5") String theme) {
        validateCVVlength(theme, "378282246310005", 4);
    }

    @Parameters({"theme"})
    @Test(description = "Verify CVVnumber is 3 digit for VISA", groups = {"merchantLow5", "merchant4"})
    public void TC_048_2_verifyOnly3digitsCVVAllowedForVISA(@Optional("merchantLow5") String theme) {
        validateCVVlength(theme, "4718650100010336", 3);
    }

    @Parameters({"theme"})
    @Test(description = "Verify CVVnumber is 3 digit for MasterCard", groups = {"merchantLow5", "merchant4"})
    public void TC_048_0_verifyOnly3digitsCVVAllowedForMasterCard(@Optional("merchantLow5") String theme) {
        validateCVVlength(theme, "5317527521624998", 3);
    }

    @Parameters({"theme"})
    @Test(description = "Verify CVVnumber is 3 digit for Diners Club", groups = {"merchantLow5", "merchant4"})
    public void TC_048_1_verifyOnly3digitsCVVAllowedForDinersClub(@Optional("merchantLow5") String theme) {
        validateCVVlength(theme, "30569309025904", 3);
    }

    private void validateCVVlength(String theme, String cardNo, int expectedCvvLength) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(cardNo);
        Assertions.assertThat(cashierPage.textBoxCVVNumber().getAttribute("maxlength")).isEqualTo(String.valueOf(expectedCvvLength));
    }

    @Parameters({"theme"})
    @Test(description = "Verify Card number length according to card type AMEX=15", groups = {"merchantLow5", "merchant4"})
    public void TC_046_verifyMaxCardLengthAllowedForAMEXIs15(@Optional("merchant4") String theme) {
        validateMaxCardLengthAllowed(theme, "378282246310005", 18);
    }

    @Parameters({"theme"})
    @Test(description = "Verify Card number length according to card type VISA=16", groups = {"merchantLow5", "merchant4"})
    public void TC_046_verifyMaxCardLengthAllowedForVISAIs16(@Optional("merchant4") String theme) {
        validateMaxCardLengthAllowed(theme, "4718650100010336", 19);
    }

    @Parameters({"theme"})
    @Test(description = "Verify Card number length according to card type Maestro=19", groups = {"merchantLow5", "merchant4"})
    public void TC_046_verifyMaxCardLengthAllowedForMaestroIs19(@Optional("merchant4") String theme) {
        validateMaxCardLengthAllowed(theme, "6799990100000000019", 23);
    }

    private void validateMaxCardLengthAllowed(String theme, String cardNo, int expectedMaxLengthAllowedForCardNoField) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(cardNo);
        Assertions.assertThat(cashierPage.textBoxCardNumber().getAttribute("maxlength")).isEqualTo(String.valueOf(expectedMaxLengthAllowedForCardNoField));
    }

    @Parameters({"theme"})
    @Test(description = "Verify amex CVV help icon shown for Amex", groups = {"merchantLow5"})
    public void TC_045_verifyAmexCvvIconShownWhenAmexCardEntered(@Optional("merchantLow5") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.AMEX_CARD_NUMBER);
        Assertions.assertThat(cashierPage.imgAmexCVVIcon().isDisplayed()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify default CVV help icon shown for Visa", groups = {"merchantLow5"})
    public void TC_045_verifydefaultCvvIconShownWhenVisaCardEntered(@Optional("merchantLow5") String theme) {
        validateDefaultCvvIconDisplayed(theme, "4718650100010336");
    }

    @Parameters({"theme"})
    @Test(description = "Verify default CVV help icon shown for Master Card", groups = {"merchantLow5"})
    public void TC_045_verifydefaultCvvIconShownWhenMasterCardEntered(@Optional("merchantLow5") String theme) {
        validateDefaultCvvIconDisplayed(theme, "5317527521624998");
    }

    @Parameters({"theme"})
    @Test(description = "Verify default CVV help icon shown for Diners Club", groups = {"merchantLow5"})
    public void TC_045_verifydefaultCvvIconShownWhenDinersClubCardEntered(@Optional("merchantLow5") String theme) {
        validateDefaultCvvIconDisplayed(theme, "30569309025904");
    }

    private void validateDefaultCvvIconDisplayed(String theme, String cardno) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(cardno);
        Assertions.assertThat(cashierPage.imgDefaultCVVIcon().isDisplayed()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify pay button is disabled if bank is not selected in net banking", groups = {"merchantLow5"})
    public void TC_081_verifyPayBtnIsDisabledWhenBankNotSelected(@Optional("merchantlow5") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.buttonPGPayNow().assertDisabled();
    }

    @Parameters({"theme"})
    @Test(description = "Verify low success rate message is showing just above to pay button for Net banking", groups = {"merchantLow5"})
    public void TC_080_verifyLowSuccessRateMsgShownForNB(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNBOtherBank().selectByVisibleText("Allahabad Bank");
        Assertions.assertThat(cashierPage.lblErrMsg().isDisplayed()).isTrue().as("Low success rate message not display");
        Assertions.assertThat(cashierPage.lblErrMsg().getText()).isEqualTo("Allahabad Bank is not available due to maintenance activity. If possible, pay using a different payment option or try after sometime.");
    }

    @Parameters({"theme"})
    @Test(description = "Verify CVV box is not showing for Baja Finserv EMI", groups = {"merchantLow5", "merchant4"})
    public void TC_070_verifyCvvFieldNotDisplayedForBajajFinservEMI(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText("Bajaj Finserv Ltd.");
        cashierPage.textBoxCVVNumber().assertNotVisible();
    }

    //  TODO: Getting failure as response while adding cards
    @Parameters({"theme"})
    @Test(description = "Verify for add & pay merchants saved cards and paymodes should be shown on cashier page when wallet option is checked", groups = {"merchantLow5"})
    public void TC_127_verifyPayModesShownForUserForAddnPayMerchantWhenWalletChecked(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.ADD_N_PAY_WITHOUT_CC_DC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabCreditCard().click();
        cashierPage.tabSavedCard().assertVisible();
    }

    //  TODO: Getting failure as response while adding cards
    @Parameters({"theme"})
    @Test(description = "Verify for add & pay merchants saved cards and pay modes should not be shown on cashier page when wallet option is unchecked")
    public void verifyPayModesNotShownForUserForAddnPayMerchantWhenWalletUnchecked(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.ADD_N_PAY_WITHOUT_CC_DC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.walletBalanceCheck().unCheck();
        cashierPage.tabCreditCard().assertNotVisible();
        cashierPage.tabDebitCard().assertNotVisible();
        cashierPage.tabSavedCard().assertNotVisible();
    }

    // TODO: need to verify testcase again
    @Parameters({"theme"})
    @Test(description = "Verify for wallet only merchants login strip will not show on cashier page", groups = {"merchantLow5"})
    public void TC_123_verifyForWalletOnlyMerchantSignInScreenIsShown(@Optional("merchantLow5") String theme) {
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginContainer().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify for all acquiring on merchant login strip will show on cashier page", groups = {"merchantLow5"})
    public void TC_124_verifyLoginStripShownForNonWalletOnlyMerchant(@Optional("merchantLow5") String theme) {
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.loginStrip().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify page is not getting submit without '@' in VPA", groups = {"merchantLow5", "merchant4"})
    public void TC_054_verifyVPATxnNotAllowedWithoutAtSymbol(@Optional("merchantLow5") String theme) {
        invalidUPIScenario(theme, "automationpaytm.com");
    }


    @Parameters({"theme"})
    @Test(description = "Verify CVV box is disabled for maestro saved card", groups = {"merchantLow5", "merchant4"})
    public void TC_055_verifyCVVBoxDisabledForMaestro(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        String maestroCardNo = "5081597197482876";
        cashierPage.textBoxCardNumber().clearAndType(maestroCardNo);
        cashierPage.textBoxCVVNumber().assertDisabled();
    }

    // TODO: Need to update iDebit properties in /etc/appconf/project/project-theia.properties
    @Parameters({"theme"})
    @Test(description = "Verify ATM PIN and CVV option is visible and ATM PIN option selected for saved idebit card when uesr is loged in")
    public void TC_135_verifyATMPinOptionSelectedForiDebit(@Optional("merchantlow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .build();
        Double txnAmount = Double.valueOf(orderDTO.getTXN_AMOUNT());
        WalletHelpers.modifyBalance(user, txnAmount);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.rdbtnSavedCardATMPin().assertVisible();
        cashierPage.rdbtnSavedCard3DPin().assertVisible();
        Assertions.assertThat(cashierPage.rdbtnSavedCardATMPin().isSelected()).isTrue().as("ATM Pin option not selected by default.");
    }

    //not applicable any more

    @Parameters({"theme"})
    @Test(description = "Verify saved detail is showing when user is not login and card and VPA is saved on MID and cusID")
    public void TC_134_verifySavedDetailsOnCustID(@Optional("merchant4") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .AssertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabSavedCard().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Promo message is showing", groups = {"merchnatLow5", "merchant4"})
    public void TC_130_verifyPromoMsgDisplayed(@Optional("merchant4") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPROMO_CAMP_ID("SALE20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.promoCode().assertText("SALE20");
    }

    //  TODO: Getting page not found for promocode SALE20 on hotfix
    @Parameters({"theme"})
    @Test(description = "Verify All paymethods is showing for which PROMO code is configured", groups = {"merchantLow5"})
    public void TC_131_verifyAllPaymethodsForPromoDisplayed(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setPROMO_CAMP_ID("SALE20")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.promoCode().assertText("SALE20");
        cashierPage.checkBoxPPI().assertVisible();
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabNetBanking().assertVisible();
        cashierPage.tabSavedCard().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify promo is getting failed but transaction is getting success if " +
            "promo code is not applicable on paymethod", groups = {"merchantLow5", "merchant4"})
    public void TC_133_verifyPromoFailedButTxnPassed(@Optional("merchant4") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPROMO_CAMP_ID("SALE20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.promoCode().assertText("SALE20");
        cashierPage.payBy(PayMode.UPI);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA is getting check and uncheck on click on checkbox", groups = {"merchantLow5"})
    public void TC_101_test1(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.checkboxVPAList().get(0).isSelected()) {
            cashierPage.checkboxVPAList().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isFalse();
            cashierPage.checkboxVPAList().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isTrue();
        } else {
            cashierPage.checkboxVPAList().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isTrue();
            cashierPage.checkboxVPAList().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isFalse();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA text is tappable", groups = {"merchantLow5"})
    public void TC_091_test2(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.checkboxVPAList().get(0).isSelected()) {
            cashierPage.lblUPIpush().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isFalse();
            cashierPage.lblUPIpush().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isTrue();
        } else {
            cashierPage.lblUPIpush().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isTrue();
            cashierPage.lblUPIpush().get(0).click();
            Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isFalse();
        }
    }

    // TODO: Getting failure as response for add cards
    @Parameters({"theme"})
    @Test(description = "Verify saved detail is showing if promo code is configured for CC and DC", groups = {"merchantLow5"})
    public void TC_132_verifySavedDetailsDisplayedForCCPromoConfiguured(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setPROMO_CAMP_ID("SALE30")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.promoCode().assertText("SALE30");
        cashierPage.textBoxSavedCardCVV().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify PPI and VPA is selected on page reload when failure form bank", groups = {"merchantLow5"})
    public void TC_099_verifyPPIAndVPASelectedOnPageReload(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH, Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.promoCode().assertText("SALE30");
        cashierPage.textBoxSavedCardCVV().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verification of PPI, UPI push, PPBL, postpaid locations", groups = {"merchantLow5"})
    public void verify_PaymodeLocation(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user)
                .setTXN_AMOUNT("22.00").build();
        WalletHelpers.modifyBalance(user, 20.00);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.verifyPaymodeLocations().AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify PPBL is getting selected when uncheck VPA", groups = {"merchantLow5"})
    public void TC_105_verify_PPBLslctd_onVPAunchk(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user)
                .setTXN_AMOUNT("2.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.isPaymodeModeSelected(PayMode.WALLET))
            cashierPage.togglePaymodeCheckbox(PayMode.WALLET);
        if (cashierPage.isPaymodeModeSelected(PayMode.PPBL))
            cashierPage.togglePaymodeCheckbox(PayMode.PPBL);
        if (cashierPage.isPaymodeModeSelected(PayMode.PAYTM_DIGITAL_CARD))
            cashierPage.togglePaymodeCheckbox(PayMode.PAYTM_DIGITAL_CARD);

        if (!cashierPage.isPaymodeModeSelected(PayMode.UPI_PUSH)) {
            cashierPage.togglePaymodeCheckbox(PayMode.UPI_PUSH);
            cashierPage.togglePaymodeCheckbox(PayMode.UPI_PUSH);
        } else
            cashierPage.togglePaymodeCheckbox(PayMode.UPI_PUSH);
        Assertions.assertThat(cashierPage.isPPBLChecked()).as("PPBL check box is not checked").isTrue();
        cashierPage.textBoxPPBLPassCode().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Postpaid is getting selected when uncheck PPBL", groups = {"merchantLow5"})
    public void TC_106_TC_097_verify_PostPaidslctd_onPPBLunchk(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        PostpaidHelpers.updateBalance("100.00");        // applicable when mock is working
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user)
                .setTXN_AMOUNT("22.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.isPaymodeModeSelected(PayMode.WALLET))
            cashierPage.togglePaymodeCheckbox(PayMode.WALLET);
        if (cashierPage.isPaymodeModeSelected(PayMode.UPI_PUSH))
            cashierPage.togglePaymodeCheckbox(PayMode.UPI_PUSH);
        if (cashierPage.isPaymodeModeSelected(PayMode.PAYTM_DIGITAL_CARD))
            cashierPage.togglePaymodeCheckbox(PayMode.PAYTM_DIGITAL_CARD);
        if (!cashierPage.isPaymodeModeSelected(PayMode.PPBL)) {
            cashierPage.togglePaymodeCheckbox(PayMode.PPBL);
            cashierPage.togglePaymodeCheckbox(PayMode.PPBL);
        } else
            cashierPage.togglePaymodeCheckbox(PayMode.PPBL);
        Assertions.assertThat(cashierPage.isPaytmCCChecked()).isTrue().as("Post paid is not checked by default");
    }

    @Parameters({"theme"})
    @Test(description = "Verify first saved card/vpa is getting selected when uncheck Postpaid", groups = {"merchantLow5"})
    public void TC_098_TC_107_verify_SavdCardslctd_onPPunchk(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        PostpaidHelpers.updateBalance("100.00");        // applicable when mock is working
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("22.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.isPaymodeModeSelected(PayMode.WALLET))
            cashierPage.togglePaymodeCheckbox(PayMode.WALLET);
        if (cashierPage.isPaymodeModeSelected(PayMode.UPI_PUSH))
            cashierPage.togglePaymodeCheckbox(PayMode.UPI_PUSH);
        if (cashierPage.isPaymodeModeSelected(PayMode.PPBL))
            cashierPage.togglePaymodeCheckbox(PayMode.PPBL);
        if (cashierPage.isPaymodeModeSelected(PayMode.PAYTM_DIGITAL_CARD))
            cashierPage.togglePaymodeCheckbox(PayMode.PAYTM_DIGITAL_CARD);

        cashierPage.radioBtnSelectSavedCard(cardId).assertSelected();
    }

    @Parameters({"theme"})
    @Test(description = "Verify PPI and postpaid is selected at same time for hybrid transaction only", groups = {"merchantLow5", "merchant4"})
    public void TC_108_verify_PPI_PP_selected(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user)
                .setTXN_AMOUNT("22.00").build();
        WalletHelpers.modifyBalance(user, 10.00);
        PostpaidHelpers.updateBalance("100.00");        // applicable when mock is working

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.isPPIChecked()).isTrue().as("Wallet not checked");
        if (!cashierPage.isPaymodeModeSelected(PayMode.PAYTM_DIGITAL_CARD))
            cashierPage.togglePaymodeCheckbox(PayMode.PAYTM_DIGITAL_CARD);
        Assertions.assertThat(cashierPage.isPaytmCCChecked()).isTrue().as("Postpaid is not checked by default");
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA (@paytm) is displayed in upper section just below to wallet when Merchant has UPI Push and User has VPA (paytm handler)")
    public void TC_090(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_UPIPUSH"), theme, user).build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.lblUPIpush().get(0).getLocation().getY())
                .isGreaterThan(cashierPage.walletBalanceCheck().getLocation().getY());
    }

 //   @Parameters({"theme"})
//    @Test(description = "Verify VPA is showing when wallet is unchecked and merchant has UPI push but scw merchant dont have UPI PUSH for add & pay transaction. Merchant has PPBL UPI PUSH as accquiring. User has PPBL account and VPA (paytm handler)", enabled = false)
    public void TC_111(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).setSSO_TOKEN(user.ssoToken()).build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.walletBalanceCheck().unCheck();
        Assertions.assertThat(cashierPage.lblUPIpush().get(0).isDisplayed()).isTrue();
        throw new SkipException("need to create AddnPay merchant having UPI push");
    }

    // Test case is disabled as UPI__PUSH is applied on SCW merchant
/*    @Parameters({"theme"})
    @Test(description = "Verify VPA is not showing when wallet is checked and merchant has UPI push " +
            "but scw merchant dont have UPI PUSH for add & pay transaction. Merchant has " +
            "PPBL UPI PUSH as accquiring. User has PPBL account and VPA (paytm handler)", enabled = false) */
    public void TC_112(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.AddnPay(getMerchantId("ADDNPAY_UPIPUSH"), theme).setSSO_TOKEN(user.ssoToken()).build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        Assertions.assertThat(cashierPage.lblUPIpush().size()).isZero().as("Possible failure reasons could be UPI is applied on merchant");
        throw new SkipException("need to create AddnPay merchant having UPI push");
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA is not showing when merchant has not UPI push but user have VPA")
    public void TC_113(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1.00").build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.walletBalanceCheck().unCheck();
        Assertions.assertThat(cashierPage.lblUPIpush().size()).isZero();
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA is not showing when user have not VPA but merchant has UPI PUSH accquiring")
    public void TC_114(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.lblUPIpush().size()).isZero();
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA (paytm handler) is showing in Saved detail")
    public void TC_092(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).build();
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);

        OrderDTO orderDTO1 = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).build();
        checkoutPage.createOrder(orderDTO1);
        cashierPage.tabSavedCard().click();
        cashierPage.verifyUPISavedDisplayed();
        throw new SkipException("create hybrid merchant not having UPI push");
    }


    @Test(description = "Verify first saved card is selected", groups = {"merchantLow5"})
    @Parameters({"theme"})
    public void TC_013_FirstSavedCardSelected(@Optional("merchantlow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PGOnly, theme, user)
                .setTXN_AMOUNT("8.00")
                .build();
        WalletHelpers.modifyBalance(user, 10.00);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
        Assertions.assertThat(cashierPage.savedCardPayNow().isDisplayed()).isTrue();
    }

    @Test(description = "Verify first VPA is selected also verify UPI logo ")
    @Parameters({"theme"})
    public void TC_013_01_FirstSavedVPASelected(@Optional("merchantLow5") String theme) throws Exception {

        User user  = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PGOnly, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PGOnly, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
        Assertions.assertThat(cashierPage.savedCardNumbers().get(0).getText()).containsIgnoringCase("@");
        Assertions.assertThat(cashierPage.savedCardPayNow().isDisplayed()).isTrue();
        throw new RuntimeException("Will work only when mock");
    }

    @Test(description = "Verify first saved card  is selected when merchant has acquring(ppbl,PPI,Postpaid)")
    @Parameters({"theme"})
    public void TC_014_SavedCardSelected(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("HYBRID"), theme, user)
                .build();

        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
        Assertions.assertThat(cashierPage.textBoxSavedCardCVV().isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.savedCardPayNow().isDisplayed()).isTrue();
    }

    @Test(description = " Verify Wallet and first saved card is selected")
    @Parameters({"theme"})
    public void TC_015_SavedCardSelected(@Optional("merchantlow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.isPPIChecked()).isTrue();
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.textBoxSavedCardCVV().isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.savedCardPayNow().isDisplayed()).isTrue();
    }

    @Test(description = "Verify saved debit card have ATM PIN and CVV option only for iDebit channel")
    @Parameters({"theme"})
    public void TC_016_SavedCardIDebit(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        Idebit idebitObj = new Idebit();
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.addCard(user, IDEBIT_EXP_MONTH, IDEBIT_EXP_YEAR, IDEBIT_CARD_NUMBER);
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
        Assertions.assertThat(cashierPage.rdbtnSavedCardATMPin().isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.rdbtnSavedCard3DPin().isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.savedCardPayNow().isDisplayed()).isTrue();
    }

    @Test(description = "Verify ATM PIN option is select for saved debit card option only for iDebit channel")
    @Parameters({"theme"})
    public void TC_017_SavedCardIDebit(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
        Assertions.assertThat(cashierPage.rdbtnSavedCardATMPin().isSelected()).isTrue();
        Assertions.assertThat(cashierPage.rdbtnSavedCard3DPin().isSelected()).isFalse();
        Assertions.assertThat(cashierPage.savedCardPayNow().isDisplayed()).isTrue();
    }


    @Test(description = "Verify Pay button should appear just below selected saved card/VPA")
    @Parameters({"theme"})
    public void TC_019_SavedCardIDebit(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.NOPPBL);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.HYBPEON, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
        Assertions.assertThat(cashierPage.savedCardPayNow().isDisplayed()).isTrue();

    }


    @Test(description = "Verify Card type logo is showing in saved detail of Visa  credit card")
    @Parameters({"theme"})
    public void TC_018_SavedCard(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.NOPPBL);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.HYBPEON, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
        cashierPage.validateSavedCardImage("visa");
    }

    @Test(description = "Verify user is able to click Pay button without expiry and " +
            "CVV for maestro card saved card. No client side error message should appear.", groups = {"merchantLow5", "merchant4"})
    @Parameters({"theme"})
    public void TC_022_Pay_WoutCVV_Maestro(@Optional("merchant4") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.NOPPBL);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.HYBPEON, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER);
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.textBoxSavedCardCVV().isEnabled()).isFalse();
        String initialUrl = getPageURL();
        cashierPage.savedCardPayNow().click();
        String finalUrl = getPageURL();
        Assertions.assertThat(initialUrl).isNotEqualTo(finalUrl);
    }

    private String getPageURL() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    private void validateBinImage(String cardType, String cardNumber, String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        //       WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.HYBPEON, theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(cardNumber);
        cashierPage.validateBinImage(cardType);
    }


    @Test(description = "Verify card type logo should be appear when enter BIN number of Visa")
    @Parameters({"theme"})
    public void TC_044_00_VerifyImage_BasedOn_BinNumber(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        validateBinImage("visa", paymentDTO.getCreditCardNumber(), theme);
    }

    @Test(description = "Verify card type logo should be appear when enter BIN number of Rupay")
    @Parameters({"theme"})
    public void TC_044_01_VerifyImage_BasedOn_BinNumber(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        validateBinImage("rupay", PaymentDTO.RUPAY_CARD_NUMBER, theme);
    }

    @Test(description = "Verify card type logo should be appear when enter BIN number of Amex")
    @Parameters({"theme"})
    public void TC_044_02_VerifyImage_BasedOn_BinNumber(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        validateBinImage("amex", PaymentDTO.AMEX_CARD_NUMBER, theme);
    }

    @Test(description = "Verify card type logo should be appear when enter BIN number of Maestro")
    @Parameters({"theme"})
    public void TC_044_03_VerifyImage_BasedOn_BinNumber(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        validateBinImage("maestro", PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER, theme);
    }

    @Test(description = "Verify card type logo should be appear when enter BIN number of Master")
    @Parameters({"theme"})
    public void TC_044_04_VerifyImage_BasedOn_BinNumber(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        validateBinImage("master", PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER, theme);
    }

    @Test(description = "Verify card type logo should be appear when enter BIN number of Diners")
    @Parameters({"theme"})
    public void TC_044_05_VerifyImage_BasedOn_BinNumber(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        validateBinImage("diners", PaymentDTO.DINERS_CARD_NUMBER, theme);
    }


    @Test(description = "Verify Successfull Add N Pay transaction when wallet balance=0")
    @Parameters({"theme"})
    public void TC_087_VerifyAddNPay_PPIBalance_0(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL);

        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }


    @Test(description = " Verify Postpaid is getting selected when uncheck ppbl")
    @Parameters({"theme"})
    public void TC_097_PostPaidSelect_whenUncheckPPBL(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_PPBL_POSTPAID"), theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        PostpaidHelpers.updateBalance("10.00");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.checkboxPPBL().isChecked()).isTrue();
        cashierPage.checkboxPPBL().check();
        Assertions.assertThat(cashierPage.checkboxPaytmCC().isSelected()).isTrue();
    }


    @Test(description = "Verify first saved card is getting selected when uncheck postpaid")
    @Parameters({"theme"})
    public void TC_098_SavedcardSelected_WhenUncheckPostpaid(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("HYBRID"), theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.isPaymodeModeSelected(PayMode.UPI_PUSH))
            cashierPage.togglePaymodeCheckbox((PayMode.UPI_PUSH));
        if (cashierPage.isPaymodeModeSelected(PayMode.PPBL))
            cashierPage.togglePaymodeCheckbox(PayMode.PPBL);

        cashierPage.checkboxPaytmCC().click();
        Assertions.assertThat(cashierPage.radioBtnSavedCard(savedCardId).isSelected()).isTrue();
    }

    @Test(description = "Verify first VPA is getting selected when uncheck postpaid", groups = "merchantLow5")
    @Parameters({"theme"})
    public void TC_098_02_VerifyFirstVPA_Selected(@Optional("merchantLow5") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(getMerchantId("PPI_UPIPUSH_POSTPAID"), theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkboxPaytmCC().click();
        Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isTrue();
    }

    @Test(description = " Verify PPI and VPA is selected on page load", groups = "merchantLow5")
    @Parameters({"theme"})
    public void TC_088_VPASelected_onPageLoad(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.HYBPEON, theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).isTrue();
        cashierPage.verifyFirstVPASelected();
    }

    @Test(description = "Verify VPA is getting selected when uncheck wallet", groups = "merchantLow5")
    @Parameters({"theme"})
    public void TC_096_VPASelected_WhenUncheckWallet(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(PropertyUtil.getInstance().getValue("PPI_PPBL_UPIPUSH_POSTPAID"), theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isSelected()).isTrue();
    }

    @Test(description = "Verify Postpaid is not showing for ADD&Pay merchant", groups = {"merchant4", "merchantLow5"})
    @Parameters({"theme"})
    public void TC_122_Postpaid_NotVisible_AddnpayMerchant(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.checkboxPaytmCC().isDisplayed()).isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "Verify CVV box should only accept numeric values", groups = {"merchant4", "merchantLow5"})
    public void TC_024(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCVVNumber().clearAndType("~`!@#$%^&*()_+[]{}|;:',<.>/?qwerty123");
        switch (theme.toLowerCase()) {
            case Constants.Theme
                    .MERCHANT4:
                Assertions.assertThat(cashierPage.textBoxCVVNumber().getAttribute("value")).isEqualTo("123");
                break;
            case Constants.Theme
                    .MERCHANTLOW5:
                Assertions.assertThat(cashierPage.textBoxCVVNumber().getAttribute("value")).isEqualTo("~`!");
                break;
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify that for saved card and VPA the complete area is tappable")
    public void TC_021(@Optional("merchantLow5") String theme) throws Exception {//TODO
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.AMEX_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.DINERS_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).build();
        WalletHelpers.modifyBalance(user, 0.0);
        PostpaidHelpers.updateBalance("0.00");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.sectionSavedCard(PaymentDTO.AMEX_CARD_NUMBER.substring(0, 6)).assertEnabled();
        throw new SkipException("need to add code for VPA");
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA is showing in upper section when wallet is check. Merchant do not have UPI PUSH. SCW merchant have UPI PUSH.", groups = {"merchantLow5"})
    public void TC_093(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.AddnPay(getMerchantId("ADDNPAY"), theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 1.0);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        Assertions.assertThat(cashierPage.lblUPIpush().get(0).isDisplayed()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA is showing in saved detail section when wallet is uncheck. Merchant do not have UPI PUSH. SCW merchant have UPI PUSH. User have saved VPA (Paytm handler)", groups = {"merchant4", "merchantLow5"})
    public void TC_094(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);

        OrderDTO orderDTO = new OrderFactory.AddnPay(getMerchantId("ADDNPAY"), theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);

        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO1 = new OrderFactory.AddnPay(getMerchantId("ADDNPAY"), theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO1);
        //TODO: when clicking on wallet checkbox saved UPI_PUSH is also not displayed
        cashierPage.checkBoxPPI().check();
        cashierPage.verifyUPISavedDisplayed();
    }

    @Parameters({"theme"})
    @Test(description = "Verify VPA is not showing when wallet is uncheck. Merchant do not have UPI PUSH. SCW merchant have UPI PUSH.", groups = {"merchantLow5"})
    public void TC_095(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.AddnPay(getMerchantId("ADDNPAY"), theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 1.0);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        Assertions.assertThat(cashierPage.lblUPIpush().size()).isZero();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Wallet and VPA is selected on page load when wallet balance = 0 for AddnPay txn. SCW Merchant has PPBL UPI PUSH as accquiring. User has PPBL account and VPA (paytm handler)", groups = {"merchantLow5"})
    public void TC_115(@Optional("merchantLow5") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSH);
        OrderDTO orderDTO1 = new OrderFactory.AddnPay(getMerchantId("ADDNPAY"), theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 1.0);

        checkoutPage.createOrder(orderDTO1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.isPPIChecked()).isTrue();
        Assertions.assertThat(cashierPage.checkboxVPAList().get(0).isDisplayed()).isTrue();
    }


    @Parameters({"theme"})
    @Test(description = "Verify all the configured EMI options should be available on PG page.", groups = {"merchant4", "merchantLow5"})
    public void TC_067(@Optional("merchant4") String theme) throws Exception {
        GetMerchantHelper getMerchantHelper = new GetMerchantHelper();
        getMerchantHelper.fetchMetchantDetails(MerchantType.EMI.getId())
                .verifyMrchntEMIResultStatus();
        List<String> emiBankName = getMerchantHelper.getIssuingEmiBankNames();
        emiBankName.add("Select");
        User user = userManager.getForWrite(Label.UPIPUSH, Label.BASIC, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.EMI, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.verifyPaymentModeDisplayed(PayMode.EMI);
        cashierPage.tabEMI().click();
        List<WebElement> emiOptionsActual = cashierPage.dropdownEmiBanks().getOptions();
        for (WebElement element : emiOptionsActual) {
            String temp = element.getText();
            Assertions.assertThat(emiBankName).contains(temp).as(temp + " emi option is not available in list");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify all configured tenure is available in EMI section", groups = {"merchant4", "merchantLow5"})
    public void TC_068(@Optional("merchantLow5") String theme) throws Exception {//TODO correct locators
        GetMerchantHelper getMerchantHelper = new GetMerchantHelper();
        getMerchantHelper.fetchMetchantDetails(MerchantType.EMI.getId())
                .verifyMrchntEMIResultStatus();
        List<String> emiBankName = getMerchantHelper.getIssuingEmiBankNames();

        HashSet<String> tempSet = new HashSet<>();        //Temporary operation to remove duplicates from list
        tempSet.addAll(emiBankName);
        emiBankName.clear();
        emiBankName.addAll(tempSet);

        User user = userManager.getForWrite(Label.UPIPUSH, Label.BASIC, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.EMI, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.verifyPaymentModeDisplayed(PayMode.EMI);
        cashierPage.tabEMI().click();

        for (String bankName : emiBankName) {
            cashierPage.dropdownEmiBanks().selectByVisibleText(bankName);
            List<Float> expectedEmiIntRate = getMerchantHelper.getEmiInterestRateByBankName(bankName);
            List<Integer> expectedEmiMonths = getMerchantHelper.getEmiMonthsByBankName(bankName);
            String bankId = getMerchantHelper.getIssuingBankyId_ByName(bankName);
            for (int month : expectedEmiMonths) {
                cashierPage.EMIMonths(bankId, month).assertVisible();
            }
            for (float intRate : expectedEmiIntRate) {
                int temp = (int) intRate;
                cashierPage.EMIInterest(bankId, temp).assertVisible();
            }
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify browser back button on cashier page", groups = {"merchant4", "merchantLow5"})
    public void TC_082_verifyBrowserBack(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL, Label.UPIPUSH);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.EMI, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        cashierPage.navigateBack();
        cashierPage.assertContainsTitle("My Merchant Check Out Page");
    }

    @Owner(HIMANSHU)
    @Feature("PGP-36410")
    @Parameters({"theme"})
    @Test(description="Issue in VPA response: Check if correct VPA comes after retrying failed payment from another vpa")
    public void vpaIssue(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        //Try payment of rs 99.99 via upi (it should fail and give an option to retry)
        Constants.MerchantType merchant = MerchantType.UPI_RETRY_ENABLED;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant,theme)
                .setTXN_AMOUNT("99.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.ErrorRetryButton().click();
        //Pay via specific vpa (txn should be successful)
        PaymentDTO correctVPA = new PaymentDTO();
        correctVPA.setVpa(PaymentDTO.PASS_VPA);
        cashierPage.payBy(Constants.PayMode.UPI, correctVPA);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        //Check which VPA is coming on response page (it should be the one with which payment was successful)
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .validateVPA(PaymentDTO.PASS_VPA)
                .assertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-37147")
    @Parameters({"theme"})
    @Test(description="Login issue in EMI subvention flow: Check if in EMI Subvention flow, user is able to login using phone no at cashier page")
    public void PGP_37147_LoginIssueEmiSubvention(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        SoftAssertions softly = new SoftAssertions();
        Constants.MerchantType merchant = MerchantType.EMI;
        User user = userManager.getForWrite(Label.POSTPAID);
        String odrerId= CommonHelpers.generateOrderId();
        EmiSubventionInitTXN api = new EmiSubventionInitTXN(merchant.getId(), odrerId);
        Response response = api.execute();
        String txnToken = response.jsonPath().getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,odrerId,txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user.mobNo(),"123456");
        cashierPage.waitUntilLoads();
        if(cashierPage.checkBoxPPI().isChecked())
        {
            cashierPage.checkBoxPPI().unCheck();
        }
        softly.assertThat(cashierPage.tabEMI().isDisplayed());
        softly.assertAll();
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-37934")
    @Parameters({"theme"})
    @Test(description = "Verify the UI Text for the Subscription Flow when values in MerchantStaticConfig DB updated")
    public  void PGP_37934_Verify_Subs_UI_Text(@Optional("enhancedweb_revamp") String theme) throws Exception{
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBS_UI_TEXT, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        UIElement paidNow= new UIElement(By.xpath("//*[text() ='To be paid now_ddd']"), "cashier-page", "to-be-paid-now");
        paidNow.assertVisible();
        UIElement billAmnt= new UIElement(By.xpath("//*[text() ='Recurring Bill Amount_xxx']"), "cashier-page", "recurring-amount");
        billAmnt.assertVisible();
        UIElement billFreq= new UIElement(By.xpath("//*[text() ='Recurring Bill Frequency_yyy']"), "cashier-page", "recurring-freq");
        billFreq.assertVisible();
        UIElement selectOption= new UIElement(By.xpath("//*[text() ='Select an option to setup your subscriptions_ddd']"), "cashier-page", "select-option-subs");
        selectOption.assertVisible();
        cashierPage.subscriptionDetails().click();
        UIElement infoRecurringAmnt= new UIElement(By.xpath("//*[text() ='Recurring Bill Amount_xxx*']"), "cashier-page", "info-recurring-amnt");
        infoRecurringAmnt.assertVisible();
        UIElement SubsDetails= new UIElement(By.xpath("//*[text() ='Subscription Details_eee']"), "cashier-page", "subs-details");
        SubsDetails.assertVisible();
        UIElement amntPaidNow= new UIElement(By.xpath("//*[text() ='Amount to be Paid Now_eee']"), "cashier-page", "amnt-paid-now");
        amntPaidNow.assertVisible();
        cashierPage.closeSubsDetailsTab().click();
        cashierPage.tabCreditCard().click();
        String text= cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(text).contains("Pay_aaa Rs 2 to Subscribe_bbb");
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-37934")
    @Parameters({"theme"})
    @Test(description = "Verify the UI Text for the Subscription Flow when values in MerchantStaticConfig DB updated")
    public void PGP_37934_Verify_Subs_UI_Text_forBM(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        String SubscriptionPurpose = "Loan Payments";
        Constants.MerchantType merchant = MerchantType.SUBS_UI_TEXT;
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setPaymentMode("BANK_MANDATE")
                .setCHANNEL_ID("WEB")
                .setWEBSITE("retail")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose("")
                .setAccountNumber("915445500424")
                .setACCOUNT_TYPE("Savings")
                .setBANK_IFSC("PYTM0000001")
                .setUSER_NAME("Akshat Sharma")
                .setSUBS_FREQUENCY("0")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setCallBack_URL("")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.BankMandateRadioButton().click();
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.tabBankMandate().click();
        cashierPage.proceedBtn().click();
        String bmText= cashierPage.bankMandateConfirmPay().getText();
        Assertions.assertThat(bmText).contains("Activate Subscriptions_fff");
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-37934")
    @Parameters({"theme"})
    @Test(description = "Verify the UI Text for the Subscription Flow when values in MerchantStaticConfig DB updated")
    public  void PGP_37934_Verify_Subs_UI_Text_1(@Optional("enhancedwap_revamp") String theme) throws Exception{
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBS_UI_TEXT, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        UIElement paidNow= new UIElement(By.xpath("//*[text() ='To be paid now_ddd']"), "cashier-page", "to-be-paid-now");
        paidNow.assertVisible();
        UIElement billAmnt= new UIElement(By.xpath("//*[text() ='Recurring Bill Amount_xxx']"), "cashier-page", "recurring-amount");
        billAmnt.assertVisible();
        UIElement billFreq= new UIElement(By.xpath("//*[text() ='Recurring Bill Frequency_yyy']"), "cashier-page", "recurring-freq");
        billFreq.assertVisible();
        UIElement selectOption= new UIElement(By.xpath("//*[text() ='Select an option to setup your subscriptions_ddd']"), "cashier-page", "select-option-subs");
        selectOption.assertVisible();
        cashierPage.subscriptionDetails().click();
        UIElement infoRecurringAmnt= new UIElement(By.xpath("//*[text() ='Recurring Bill Amount_xxx*']"), "cashier-page", "info-recurring-amnt");
        infoRecurringAmnt.assertVisible();
        UIElement SubsDetails= new UIElement(By.xpath("//*[text() ='Subscription Details_eee']"), "cashier-page", "subs-details");
        SubsDetails.assertVisible();
        UIElement amntPaidNow= new UIElement(By.xpath("//*[text() ='Amount to be Paid Now_eee']"), "cashier-page", "amnt-paid-now");
        amntPaidNow.assertVisible();
        cashierPage.closeSubsDetailsTab().click();
        cashierPage.tabCreditCard().click();
        String text= cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(text).contains("Pay_aaa Rs 2 to Subscribe_bbb");
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-41305")
    @Parameters({"theme"})
    @Test(description = "Verify the UI Text for the Subscription Flow when values in MerchantStaticConfig DB updated")
    public void VERIFY_Rupee_SpecialCharacter_Rendering_onUI(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.WALLET_2FA_DISABLED, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        UIElement paidNow = new UIElement(By.xpath("//*[text() ='₹1 will be deducted now for account verification']"), "cashier-page", "recurring-amount");
        paidNow.assertVisible();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-45666")
    @Parameters({"theme"})
    @Test(description = "Verify only UPI Collect is shown on cashier page if merchant has wallet disabled and only UPI Collect enabled")
    public void verifyWalletNotPresent_UPICollectOnlyMerchant_Enhanced(@Optional("enhancedwap_revamp")String theme) throws Exception
    {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.WALLET_DISABLED_UPI_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.wallet().assertNotVisible();
        cashierPage.tabUPI().assertVisible();

    }

    @Owner(HIMANSHU)
    @Feature("PGP-45666")
    @Parameters({"theme"})
    @Test(description = "Verify only UPI Collect is shown on cashier page if merchant has wallet disabled and only UPI Collect enabled")
    public void verifyWalletNotPresent_UPICollectOnlyMerchant_CheckoutJS(@Optional("checkoutjs_wap_revamp") String theme) throws Exception
    {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        MerchantType merchant=MerchantType.WALLET_DISABLED_UPI_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.wallet().assertNotVisible();
        cashierPage.UPICollectTab().assertVisible();

    }
    @Owner(HIMANSHU)
    @Feature("PGP-45666")
    @Parameters({"theme"})
    @Test(description = "Verify only UPI Collect is shown on cashier page if merchant has wallet disabled and only UPI Collect enabled for AppInvoke flow")
    public void verifyWalletNotPresent_UPICollectOnlyMerchant_AppInvoke(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        MerchantType merchantType=MerchantType.WALLET_DISABLED_UPI_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType,initTxnDTO.getBody().getOrderId(),txnToken)
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.wallet().assertNotVisible();
        cashierPage.tabUPI().assertVisible();
    }
    @Owner("Meenakshi")
    @Feature("PGP-54467")
    @Parameters({"theme"})
    @Test(description = "Login at last, Pay with UPI Text and Saved Card text removal on login Strip")
    public void ValidateLoginStrip(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType mid = MerchantType.HIGH_PRIORITY_SMS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
       InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.savedcardsandbalance().assertNotVisible();
        cashierPage.scrollTo(0);
        cashierPage.loginStrip().assertVisible();
       cashierPage.TextUpi().assertContainsText("Pay with UPI");
    }

    @Owner("Meenakshi")
    @Feature("PGP-54467")
    @Parameters({"theme"})
    @Test(description = "Login Component Reordering on Login QR strip and Payment options should be displayed")
    public void ValidateTextPaymentOptions(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType mid = MerchantType.MID_FETCHLOGOFROMBOSSPANEL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.wallet().assertNotVisible();
        cashierPage.tabUPI().assertVisible();
        cashierPage.savedcardsandbalance().assertNotVisible();
        cashierPage.paymentOptions().assertContainsText("Payment Options");
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56667")
    @Parameters({"theme"})
    @Test(description = "Verify BHIM Upi icon comes next to paytm app in upi intent option  when ff4j: ui.promoteBhim is on theme : mweb")
    public void verifyBHIMUpiPromotion_ff4j_on(@Optional("enhancedwap_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = MerchantType.UPI_INTENT_MWEB;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        Assertions.assertThat(cashierPage.UPIIntentApps().contains("BHIM"));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56667")
    @Parameters({"theme"})
    @Test(description = "Verify BHIM Upi icon doesn't come next to paytm app in upi intent option  when ff4j: ui.promoteBhim is off theme : mweb")
    public void verifyBHIMUpiPromotion_ff4j_off(@Optional("enhancedwap_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = MerchantType.UPI_PUSH_INTENT;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid,theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        Assertions.assertThat(cashierPage.UPIIntentApps()).doesNotContain("BHIM");

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56667")
    @Parameters({"theme"})
    @Test(description = "Verify BHIM Upi icon comes next to paytm app in upi intent option  when ff4j: ui.promoteBhim is on theme : checkoutjs wap")
    public void verifyBHIMUpiPromotion_ff4j_on_checkoutJs(@Optional("checkoutjswap_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = MerchantType.UPI_INTENT_MWEB;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        Assertions.assertThat(cashierPage.UPIIntentApps().contains("BHIM"));
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56667")
    @Parameters({"theme"})
    @Test(description = "Verify BHIM Upi icon doesn't come next to paytm app in upi intent option  when ff4j: ui.promoteBhim is off theme : checkoutjs mweb")
    public void verifyBHIMUpiPromotion_ff4j_off_checkoutJs(@Optional("checkoutjswap_revamp") String theme) throws Exception
    {
        Constants.MerchantType mid = MerchantType.UPI_PUSH_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads(); cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        Assertions.assertThat(cashierPage.UPIIntentApps()).doesNotContain("BHIM");

    }
}