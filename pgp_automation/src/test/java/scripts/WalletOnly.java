package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

import static com.paytm.appconstants.Constants.Owner.Amanpreet;

@Owner("Deepak")
public class WalletOnly extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final CheckoutJsCheckoutPage checkoutPage1 = new CheckoutJsCheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate successful Txn with amount in whole number >= 1", groups = {"smoke"})
    //Wallet is not supported
    public void PGP_158_successfulWalletOnlyTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(MerchantType.WalletOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        if (LocalConfig.PERFORM_RESPONSE_PAGE_UI_VALIDATION) {
            responsePage = new ResponsePage();
            responsePage.textStatus().waitUntilDoesNotContainText("PENDING");
            responsePage
                    .validateBankTxnId("")
                    .validateCurrency("")
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successful Txn when amount in fraction > 1")
    //Wallet is not supported
    public void PGP_159_successfulWalletOnlyTxnInFractionGTOne(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme, user)
                .setTXN_AMOUNT("10.50")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Issue("PGP-13104")
    @Parameters({"theme"})
    @Test(description = "Validate Txn failure when amount = 0")
    public void PGP_160_failureWalletOnlyTxnWithZeroAmount(@Optional("merchant4") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme)
                .setTXN_AMOUNT("0.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textRespMsg().getText()).isIn("Invalid Txn Amount", "Txn amount is invalid");
        Assertions.assertThat(responsePage.textRespCode().getText()).isIn("308", "501");
    }

    @Issue("PGP-13104")
    @Parameters({"theme"})
    @Test(description = "Validate Txn failure for -ve amount")
    public void PGP_161_failureWalletOnlyTxnWithNegativeAmount(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme)
                .setTXN_AMOUNT("-2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textRespMsg().getText()).isIn("Invalid Txn Amount", "Txn amount is invalid");
        Assertions.assertThat(responsePage.textRespCode().getText()).isIn("308", "501");
    }

    @Issue("PGP-13104")
    @Parameters({"theme"})
    @Test(description = "Validate Txn failure when 0 < amount < 1")
    public void PGP_162_failureWalletOnlyTxnWithFractionAmountLTOne(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme)
                .setTXN_AMOUNT("0.50")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textRespMsg().getText()).isIn("Invalid Txn Amount", "Txn amount is invalid");
        Assertions.assertThat(responsePage.textRespCode().getText()).isIn("308", "501");
    }

    @Parameters({"theme"})
    @Test(description = "validate Txn success - sign in as a different user")
    public void PGP_164_successfulWalletOnlyTxnWithOtherUser(@Optional("enhancedwap") String theme) throws Exception {
        User user1 = userManager.getForWrite(Label.BASIC);
        User user2 = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user1);
        cashierPage.btnLogout().click();
        user1.purge();
        WalletHelpers.modifyBalance(user2, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        cashierPage.login(user2);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

   @Parameters({"theme"})
   @Test(description = "Verify Txn Failure in case of Txn Cancellation at cashier page.")
    public void PGP_163_cancelWalletOnlyTxn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.linkWalletCancel().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        //responsePage.verifyUIResponse(Constants.ResponseCode.USER_DROP_141);
    }

    @Parameters({"theme"})
    @Test(description = "Verify Mobile Number should be auto populated in login frame")
    public void PGP_166_walletOnlyTxnWithMobileAutoPopulated(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme)
                .setMSISDN(user.mobNo())
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilContainsText("Login");
        if (!theme.equals("merchant4") && !cashierPage.loginModal().isDisplayed()) {
            cashierPage.btnLogin().waitUntilClickable();
            cashierPage.btnLogin().click();
            cashierPage.loginModal().waitUntilVisible();
        }
        cashierPage.switchToLoginFrame();
        //cashierPage.loginFrame().switchToFrame();
        cashierPage.textBoxPhoneNumber().assertValue(user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description = "Verify Email Id should be auto populated in Login frame")
    public void PGP_167_walletOnlyTxnWithEmailIDAutoPopulated(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme)
                .setEMAIL("testautumn@paytm.com")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilContainsText("Login");
        if (!theme.equals("merchant4") && !cashierPage.loginModal().isDisplayed()) {
            cashierPage.btnLogin().waitUntilClickable();
            cashierPage.btnLogin().click();
            cashierPage.loginModal().waitUntilVisible();
        }
        cashierPage.switchToLoginFrame();
        cashierPage.textBoxPhoneNumber().assertValue(orderDTO.getEMAIL());
    }

    @Parameters({"theme"})
    @Test(description = "Verify Mobile Number should be auto populated in Login frame if both email and mobile number provided")
    public void PGP_168_walletOnlyTxnWithMobileAutoPopulatedInCaseOfBothPresent(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme)
                .setMSISDN(user.mobNo())
                .setEMAIL("testautumn@paytm.com")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilContainsText("Login");
        if (!theme.equals("merchant4") && !cashierPage.loginModal().isDisplayed()) {
            cashierPage.btnLogin().waitUntilClickable();
            cashierPage.btnLogin().click();
            cashierPage.loginModal().waitUntilVisible();
        }
        cashierPage.switchToLoginFrame();
        cashierPage.textBoxPhoneNumber().assertValue(user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description = "Validate Txn failure - wallet blocked case")
    public void PGP_165_failedWalletOnlyTxnWithBlockedTxnAmnt(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        double txnAmt = 2;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmt))
                .build();
        double blockedAmt = txnAmt;
        Double amtToBeRetained = txnAmt + 1;
        WalletHelpers.modifyBalance(user, amtToBeRetained);
        WalletHelpers.blockAmount(user, blockedAmt, orderDTO.getMerchantKey());
        System.out.println("current balance is:" + WalletHelpers.getWalletBalance(user));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String expectedBalance = CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amtToBeRetained - blockedAmt);
        String actualBalance = String.format("%.2f", new BigDecimal(cashierPage.walletBalanceCheck().getText().replace("Rs", "").trim()));
        Assertions.assertThat(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(actualBalance)).as("Wallet balance is not as expected").isEqualToIgnoringCase(expectedBalance);
    }

    @Epic(Constants.Sprint.SPRINT32_2)
    @Story("PGP-21561")
    @Test(description = "test txn amt in callback and txn status has 2 decimal precision")
    public void testTxnAmtInCallbackAndTxnStatusHas2DecimalPrecision(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();

        String txnAmt = "100";
        String formattedTxnAmt = new DecimalFormat("0.00").format(Double.parseDouble(txnAmt));
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.EMI_DISCOVERY, theme, user)
                .setTXN_AMOUNT(txnAmt)
                .build();
       // WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        cashierPage.backBtn().click();
        cashierPage.cancelPaymentYes().click();
        pageWait.apply(responsePage.hasLoaded());
        Assertions.assertThat(responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.TXNAMOUNT).getValue()).isEqualTo(formattedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.getTXNAMOUNT()).isEqualTo(formattedTxnAmt);
    }

    @Owner(Amanpreet)
    @Feature("PGP-40763")
    @Parameters({"theme"})
    @Test(description = "verify for Wallet only merchants that wallet is not selected when user has insufficient wallet balance")//wallet is not supported
    public void PGP_40763_TC_01_validateWalletNotSelected(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        double txnAmt = 100;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmt))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        double balance = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 10.00;
        WalletHelpers.modifyBalance(user, balance);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WebElement walletBalance = DriverManager.getDriver().findElement(By.xpath("//*[@id='checkbox']"));
        Assertions.assertThat(walletBalance.isSelected()).isFalse();
    }

    @Owner(Amanpreet)
    @Feature("PGP-40763")
    @Parameters({"theme"})
    @Test(description = "verify for Wallet only merchants that wallet is selected when user has sufficient wallet balance")
    //Wallet is nor Supported
    public void PGP_40763_TC_02_validateWallet_Selected(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        double txnAmt = 1;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmt))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        double balance = Double.valueOf(orderDTO.getTXN_AMOUNT()) + 10.00;
        WalletHelpers.modifyBalance(user, balance);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WebElement walletBalance = DriverManager.getDriver().findElement(By.xpath("//*[@id='checkbox']"));
        Assertions.assertThat(walletBalance.isSelected()).isTrue();
    }

    @Owner("Shubham Soni")
    @Feature("PG2-10050")
    @Description("Automation JIRA : PGP-46672")
    @Test(description = "To verify Payment Flow As NONE when wallet balance is zero and DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE preference is Y for enhanced Flow")
    public void ZERO_WALLET_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.ZERO_WALLET_BALANCE.getId(), "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE", "Y");
        }

        MerchantType corporateMerchant = MerchantType.ZERO_WALLET_BALANCE;

        User user = userManager.getForWrite(Label.ZEROWALLET);
        Double txnAmount = 2.0;

        WalletHelpers.modifyBalance(user,0.0);


        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

       boolean flag = cashierPage.checkBoxPPI().isEnabled();
        Assertions.assertThat(flag).isEqualTo(false);


        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,orderDTO.getORDER_ID(),"fetchPaymentOptions");
        System.out.println(response);
        Assertions.assertThat(response).contains("paymentFlow=NONE");

    }
    @Owner("Shubham Soni")
    @Feature("PG2-10050")
    @Description("Automation JIRA : PGP-46672")
    @Test(description = "To verify Payment Flow As NONE when wallet balance is zero and DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE preference is Y for Native Flow")
    public void ZERO_WALLET_02(@Optional("false") Boolean isNativePlus) throws Exception {
        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.ZERO_WALLET_BALANCE.getId(), "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE", "Y");
        }
        Constants.MerchantType merchant = Constants.MerchantType.ZERO_WALLET_BALANCE;
        User user = userManager.getForWrite(Label.ZEROWALLET);
        WalletHelpers.modifyBalance(user,0.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("40")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentFlow")).isEqualTo("NONE");
    }
    @Owner("Shubham Soni")
    @Feature("PG2-10050")
    @Description("Automation JIRA : PGP-46672")
    @Test(description = "To verify Payment Flow As NONE when wallet balance is zero and DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE preference is Y for Native Flow, v5 FPO version")
    public void ZERO_WALLET_03(@Optional("false") Boolean isNativePlus) throws Exception {
        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.ZERO_WALLET_BALANCE.getId(), "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE", "Y");
        }
        Constants.MerchantType merchant = Constants.MerchantType.ZERO_WALLET_BALANCE;
        User user = userManager.getForWrite(Label.ZEROWALLET);
        WalletHelpers.modifyBalance(user,0.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("40")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentFlow")).contains("NONE");
    }

    @Owner("Shubham Soni")
    @Feature("PG2-10050")
    @Description("Automation JIRA : PGP-46672")
    @Test(description = "To verify Payment Flow As NONE when wallet balance is zero and DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE preference is Y for CheckoutJs Flow")
    public void ZERO_WALLET_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.ZERO_WALLET_BALANCE.getId(), "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE", "Y");
        }
        Constants.MerchantType merchant = Constants.MerchantType.ZERO_WALLET_BALANCE;
        User user = userManager.getForWrite(Label.ZEROWALLET);
        WalletHelpers.modifyBalance(user,0.0);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue("40")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage1.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage1.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        boolean flag = cashierPage.checkBoxPPI().isSelected();
        Assertions.assertThat(flag).isEqualTo(false);
        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,initTxnDTO.getBody().getOrderId(),"fetchPaymentOptions");
        System.out.println(response);
        Assertions.assertThat(response).contains("paymentFlow=NONE");
    }

    @Owner("Shubham Soni")
    @Feature("PG2-10050")
    @Description("Automation JIRA : PGP-46672")
    @Test(description = "To verify Payment Flow As NONE when wallet balance is zero and DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE preference is Y for enhanced Flow, WalletOnly Merchant")
    //wallet and PPI is not supported
    public void ZERO_WALLET_05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.WALLET_ONLY_MER.getId(), "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE", "Y");
        }

        MerchantType corporateMerchant = MerchantType.WALLET_ONLY_MER;

        User user = userManager.getForWrite(Label.ZEROWALLET);
        Double txnAmount = 2.0;

      //  WalletHelpers.modifyBalance(user,0.0);


        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        boolean flag = cashierPage.checkBoxPPI().isEnabled();
        Assertions.assertThat(flag).isEqualTo(false);


        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,orderDTO.getORDER_ID(),"fetchPaymentOptions");
        System.out.println(response);
        Assertions.assertThat(response).contains("paymentFlow=NONE");

    }

    @Owner("Shubham Soni")
    @Feature("PG2-10050")
    @Description("Automation JIRA : PGP-46672")
    @Test(description = "To verify Payment Flow As NONE when wallet balance is zero and DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE preference is Y for enhanced Flow, WalletOnly Merchant")//wallet is not supported
    public void ZERO_WALLET_06(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.ZERO_WALLET_BALANCE.getId(), "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE", "Y");
        }

        MerchantType corporateMerchant = MerchantType.ZERO_WALLET_BALANCE;

        User user = userManager.getForWrite(Label.ZEROWALLET);
        Double txnAmount = 2.0;

        WalletHelpers.modifyBalance(user,1.0);


        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        boolean flag = cashierPage.checkBoxPPI().isEnabled();
        Assertions.assertThat(flag).isEqualTo(true);


        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,orderDTO.getORDER_ID(),"fetchPaymentOptions");
        System.out.println(response);
        Assertions.assertThat(response).contains("paymentFlow=ADDANDPAY");

    }

    @Owner("Shubham Soni")
    @Feature("PG2-10050")
    @Description("Automation JIRA : PGP-46672")
    @Test(description = "To verify Payment Flow As ADDANDPAY when wallet balance is zero and DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE preference is N for enhanced Flow, WalletOnly Merchant")
    public void ZERO_WALLET_07(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.ZERO_WALL_BAL.getId(), "DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE", "N");
        }

        MerchantType corporateMerchant = MerchantType.ZERO_WALL_BAL;

        User user = userManager.getForWrite(Label.ZEROWALLET);
        Double txnAmount = 2.0;

        WalletHelpers.modifyBalance(user,0.0);


        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,orderDTO.getORDER_ID(),"fetchPaymentOptions");
        System.out.println(response);
        Assertions.assertThat(response).contains("paymentFlow=ADDANDPAY");

    }

}
