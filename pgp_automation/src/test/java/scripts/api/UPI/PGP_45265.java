package scripts.api.UPI;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.paytm.appconstants.Constants.MerchantType.*;


public class PGP_45265 extends PGPBaseTest {
    //Test case sheet - https://docs.google.com/spreadsheets/d/1rWV0NwiPEo9x5CjGkE43mw1wjH7M6jRLMthYx2pUJLg/edit#gid=1708201262
    private final CheckoutPage checkoutPage = new CheckoutPage();


    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "test With valid Vpa in pre Login flow")
    public void TEST_01(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payWithOtherUpiAppsPreLogin().click();
        cashierPage.textBoxVPA().sendKeys("test@paytm");
        cashierPage.proceedButton().click();
        Thread.sleep(100);
        cashierPage.verifiedVpaID().isDisplayed();

    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify invalid vpa in pre login flow ")
    public void TEST_02(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payWithOtherUpiAppsPreLogin().click();
        cashierPage.textBoxVPA().sendKeys("invali@paytm");
        cashierPage.proceedButton().click();
        Thread.sleep(100);
        String msg=cashierPage.errorTextsInUPIFlow().getText();
        Assert.assertEquals(msg,Constants.MessageAssert.INVALID_VPA.toString());

    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify collect with valid upi number in pre login flow")
    public void TEST_03(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payWithOtherUpiAppsPreLogin().click();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.proceedButton().click();
        Thread.sleep(100);
        cashierPage.verifyUpiNumericID().isDisplayed();

    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify Logos are displayed for Paytm and UPI in pre login flow")
    public void TEST_04(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.paytmLogoNew().isDisplayed();
        cashierPage.upiLogoNew().isDisplayed();
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify Enter Mobile No text is displayed in Pay with Paytm UPI section in pre login flow")
    public void TEST_05(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        String placeholderText=cashierPage.payWithPaytmInputBoxText().getAttribute("placeholder");
        Assert.assertEquals("Enter Mobile No / UPI Number",placeholderText);
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify Proceed button  is displayed on cashier page in CTA in pre login flow")
    public void TEST_06(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.proceedButton().isDisplayed();
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify Err Msg for blank vpa id  in pre login flow")
    public void TEST_07(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payWithOtherUpiAppsPreLogin().click();
        cashierPage.textBoxVPA().sendKeys("");
        cashierPage.proceedButton().click();
        Thread.sleep(100);

        String msg=cashierPage.errorTextsInUPIFlow().getText();
        Assert.assertEquals(msg,"Please enter a valid VPA of the form username@bank");
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Successfull Txn in pre login flow")
    public void TEST_08(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxUpiMobileNumber().sendKeys("8006006993");
        cashierPage.proceedButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(UPI_ERROR_MSG.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Successfull Txn in pre login flow")
    public void TEST_09(@Optional("enhancedwab_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payWithOtherUpiAppsPreLogin().click();
        cashierPage.textBoxVPA().sendKeys("test@paytm");
        cashierPage.proceedButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(UPI_ERROR_MSG.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify upi collect with phone number  linked with paytm in pre login flow")
    public void TEST_010(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxUpiMobileNumber().sendKeys("8183878457");
        cashierPage.proceedButton().click();
        Thread.sleep(100);
        cashierPage.verifyUpiNumericID().isDisplayed();
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify upi collect with phone number not linked with paytm in pre login flow" )
    public void TEST_011(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxUpiMobileNumber().sendKeys("9972403805");
        cashierPage.proceedButton().click();
        String msg=cashierPage.errorTextsInUPIFlow().getText();
        Assert.assertEquals(msg,"Phone number does not exist in Paytm, Try Again");
    }


    // Post Login flow Cases
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify E2E Successfull Txns with valid vpa in post login flow")
    public void TEST_012(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .setSSO_TOKEN(user.ssoToken()).build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().sendKeys("test@paytm");
        cashierPage.proceedButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(UPI_ERROR_MSG.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify E2E Successfull Txns with valid Numeric Id in post login flow")
    public void TEST_013(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .setSSO_TOKEN(user.ssoToken()).build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.proceedButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(UPI_ERROR_MSG.getKey())
                .validateResponsePageParameters()
                .assertAll();


    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Collect with blank VPA ID in Post Login Flow")
    public void TEST_014(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .setSSO_TOKEN(user.ssoToken()).build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().sendKeys("");
        cashierPage.proceedButton().click();
        Thread.sleep(100);
        String msg=cashierPage.errorTextsInUPIFlow().getText();
        Assert.assertEquals(msg,"Please enter a valid VPA of the form username@bank");

    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify text is changed from Pay to Proceed in post login flow")
    public void TEST_015(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.proceedButton().isDisplayed();
    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify text is changed from Pay to Proceed in post login flow")
    public void TEST_016(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.proceedButton().isDisplayed();
    }

    //pre login
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Pay using Paytm UPI and Pay using other UPI App screen should be visible in the place of OTPStrip in pre login flow")
    public void TEST_017(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payWithOtherUpiAppsPreLogin().isDisplayed();
        cashierPage.tabUPI().isDisplayed();
    }

    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify Login OTP/QR section is removed for merchants with only UPI Enabled in preLogin flow")
    public void TEST_018(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.QRCode().assertNotVisible();
        cashierPage.loginOtpBox().assertNotVisible();

    }

    //post login
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Number is automatically checked on clicking Proceed in Post Login flow")
    public void TEST_019(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.proceedButton().click();
        Thread.sleep(100);
       cashierPage.verifyUpiNumericID().isDisplayed();

    }
    @Feature("PGP-45265")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify procedd button is there with saved vpa")
    public void TEST_020(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_ERROR_MSG, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.proceedButton().isDisplayed();

    }


}
