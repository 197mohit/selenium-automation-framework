package scripts.JSCheckout;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import static com.paytm.appconstants.Constants.MerchantType.EmiInfo_COP;
import static com.paytm.appconstants.Constants.Owner.GAURAV;
import static com.paytm.appconstants.Constants.Owner.ROHIT_SHARMA;
import static com.paytm.appconstants.Constants.PayMode.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Feature("PGP-32471")
@Owner(Constants.Owner.PRIYANSHI)
@Parameters({"theme"})
public class CheckoutJSonEnhanced extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();


    @Test(description = "Validate CC TXN in case of CheckoutJS domain on Enhanced")
    public void JSCheckoutDomainOnEnhanced_CCTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();

    }

    @Test(description = "Validate DC TXN in case of CheckoutJS domain on Enhanced")
    public void JSCheckoutDomainOnEnhanced_DCTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();

    }

    @Test(description = "VValidate UPI TXN in case of CheckoutJS domain on Enhanced")
    public void JSCheckoutDomainOnEnhanced_UPITxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.UPI);
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
                .assertAll();
    }


    @Parameters("theme")
    @Test(description = "Validate Subs TXN done with Daily frequency")
    public void JSCheckoutDomainOnEnhanced_SUBS_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,100.00);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(Constants.MerchantType.SUBS_UI_TEXT, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("5")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().click();
        cashierPage.payBy(WALLET);
        cashierPage.buttonPGPayNow().click();
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();
    }


    @Test(description = "VValidate NB TXN in case of CheckoutJS domain on Enhanced")
    public void JSCheckoutDomainOnEnhanced_NB_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();
    }

//    @Parameters({"theme"})
//    @Test(description = "Validate ADDNPAY TXN in case of CheckoutJS domain on Enhanced",enabled = false)
    public void JSCheckoutDomainOnEnhanced_ADD_N_PAY_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.JS_Checkout_Paytm_Domain, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();
    }

//    @Parameters({"theme"})
//    @Test(description = "Validate HYBRID TXN in case of CheckoutJS domain on Enhanced", enabled = false)
    public void JSCheckoutDomainOnEnhanced_HYBRID_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.HYBRID_MID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Retry Message after reaching to the limit")
    public void JSCheckoutDomainOnEnhanced_Exhausted_Retry_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.IDEMPOTENT, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        cashierPage.retryBtnPopupClosedByUser().isDisplayed();
        cashierPage.retryBtnPopupClosedByUser();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();
    }
    @Parameters("theme")
    @Test(description = "Verify successful Promo Cashback with checkoutJS On EnhancedUI ")
    public void JSCheckoutDomainOnEnhanced_BANK_OFFER(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.EMI_DISCOVERY;
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(merchant.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.clickPgOverlay();
        PaymentDTO nbPaymentDTO = new PaymentDTO();
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        cashierPage.tabNetBanking().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful EMI Transaction")
    public void JSCheckoutDomainOnEnhanced_EMI_Txn(@Optional("checkoutjs_web_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("100.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO.setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate HDFO TXN in case of CheckoutJS domain on Enhanced")
    public void JSCheckoutDomainOnEnhanced_DIRECT_BANK_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDFO, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(CC);
        String parent=DriverManager.getDriver().getWindowHandle();
        Set<String>s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        DriverManager.getDriver().switchTo().window(parent);

        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFO")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-38023")
    @Parameters({"theme"})
    @Test(description = "In FPO response, merchant name (Raha's) is showing apostrophe character")
    public void verifyMerchantname(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" " + LocalConfig.THEIA_LOGS
                + " | grep \"fetchPaymentOptions\" | " + "grep \"response\"";
        String theiaLogs=getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);

        Assertions.assertThat(theiaLogs).contains("merchantName=******");//merchant name and display name is encrypted
    }

    @Owner(GAURAV)
    @Feature("PGP-39260")
    @Parameters({"theme"})
    @Test(description = "Validate successful AddMoney Transaction")
    public void AddMoneyOnJsCheckoutOnEnhancedWithCC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.AddMoney_CheckoutOnEnhanced, theme,user)
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        DriverManager.getDriver().switchTo().window(parent);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();
        WalletHelpers.validateBalance(user,3);
    }

    @Owner(GAURAV)
    @Feature("PGP-39260")
    @Parameters({"theme"})
    @Test(description = "Validate successful AddMoney Transaction")
    public void AddMoneyOnJsCheckoutOnEnhancedWithPPBL(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.AddMoney_CheckoutOnEnhanced, theme,user)
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateGatewayName("PPBL")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .assertAll();
        WalletHelpers.validateBalance(user,3);
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-41553")
    @Parameters({"theme"})
    @Test(description = "Verify Postpaid pay option as default on cashier page in case of AddNPay")
    public void verifyPostpaidAsDefaultforAddNPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        WalletHelpers.modifyBalance(user, 10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaid().isSelected();

    }
    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-41553")
    @Parameters({"theme"})
    @Test(description = "Verify Postpaid pay option as not default on cashier page in case of NONE Flow")
    public void verifyPostpaidAsNOTdefaultforNoneFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        WalletHelpers.modifyBalance(user, 10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.CHECKOUT_ON_REDIRECTION, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().isSelected();

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-41553")
    @Parameters({"theme"})
    @Test(description = "Verify Postpaid txn for AddNPay ")
    public void verifyPostpaidtxnforAddNPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        WalletHelpers.modifyBalance(user, 10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("1.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when only UPI and PPI are enabled")
    public void PGP_41787_TC01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT_JS;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setPAYMENT_MODE_ONLY("YES")
                .setPAYMENT_TYPE_ID("UPI,PPI")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm or Any UPI App");
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when UPI and PPI are disabled")
    public void PGP_41787_TC02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT_JS;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setPAYMENT_MODE_DISABLE("UPI,PPI")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" "+LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }
    private String getColorCode(String color) {
        String[] hexValue = color.replace("rgba", "").replace(")", "").replace("(", "").split(",");
        hexValue[0] = hexValue[0].trim();
        int hexValue1 = Integer.parseInt(hexValue[0]);
        hexValue[1] = hexValue[1].trim();
        int hexValue2 = Integer.parseInt(hexValue[1]);
        hexValue[2] = hexValue[2].trim();
        int hexValue3 = Integer.parseInt(hexValue[2]);
        return String.format("#%02x%02x%02x", hexValue1, hexValue2, hexValue3);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify Scan with any UPI App, is shown for the Qr rendered")
    public void verifyScanQrRevamp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
        .setTXN_AMOUNT("10.00")
        .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String Qrtext =cashierPage.qrCodeCheckoutJSTextNew().getText();
        Assertions.assertThat(Qrtext).contains("Scan with any UPI App");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that enabled Paymodes text colour is changes to black,for the Qr rendered")
    public void verifyEnabledPaymodeColourRevamp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String enabledColour = getColorCode(cashierPage.enabledPaymodes().getCssValue("color"));
        System.out.println(enabledColour);
        Assertions.assertThat(enabledColour).contains("#101010");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that or Scan with any UPI app in grey with UPI images is shown for the Qr rendered")
    public void verifyorScanwithUPItextRevamp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String qrSubText = cashierPage.qrSubTextNew().getText();
        Assertions.assertThat(qrSubText).contains("& more");
        String qrSubTestColour = getColorCode(cashierPage.qrSubTextNew().getCssValue("color"));
        System.out.println(qrSubTestColour);
       // Assertions.assertThat(qrSubTestColour).contains("#0f0f0f");
        cashierPage.qrCodeImgNew().assertVisible();
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-46447")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Upi paymode should contain  UPI app images with & more text in non-logged in flow", enabled=false) //As discussed with dev , this case is not for redirection flow
    public void verifyUpiPaymodeUpiAppImgNonLoggedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.upiPaymodeUpiAppImg().assertVisible();
        Assertions.assertThat(cashierPage.upiPaymodeUpiAppText().getText()).contains("& more");
    }
//     @Owner(ROHIT_SHARMA)
//     @Feature("PGP-46447")
//     @Parameters({"theme"})
//     @Test(description = "Verify that Upi paymode should contain  UPI app images with & more text in logged in flow", enabled=false) // As discussed with dev , this case is not for redirection flow
    public void verifyUpiPaymodeUpiAppImgLoggedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("10.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.upiPaymodeUpiAppImg().assertVisible();
        Assertions.assertThat(cashierPage.upiPaymodeUpiAppText().getText()).contains("& more");
    }

    @Owner(GAURAV)
    @Feature("PGP-46539")
    @Parameters({"theme"})
    @Test(description = "Verify new UPI polling page with a successful transaction")
    public void verifyNewUpiPollingPageAndSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.upiPollingPageMobileLogo().isDisplayed();
        cashierPage.upiPollingPageInfoText().isDisplayed();
        cashierPage.upiPollingPageWarningText().isDisplayed();
        Assertions.assertThat(cashierPage.upiPollingPageTxnAmount().getText()).contains(orderDTO.getTXN_AMOUNT().replace(".00",""));
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
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-47696")
    @Parameters({"theme"})
    @Test(description = "Verify that for deactive postpaid user account msg Your Postpaid account is not active. Please use other Payment option. should be displayed")
    public void postpaidDeactiveUimsg(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_Pcf, theme)
                .setTXN_AMOUNT("24.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.POSTPAID_ONBOARDING);
        cashierPage.waitUntilLoads();
        String txt = cashierPage.postpaidContainer().getText();
        Assertions.assertThat(txt).contains("Your Postpaid account is not active. Please use other Payment option");
    }

    public void kfs(CashierPage cashierPage , String cardNo) throws Exception {
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(cardNo);
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        PaymentDTO emidetails = new PaymentDTO();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(emidetails.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(emidetails.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(emidetails.getCvvNumber());
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn of ICICI emi dc using new kfs changes")
    public void kfs_icici_dc(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI)
                        .setBankName("ICICI");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(EMI_DC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validatePaymentMode("EMI_DC")
                .validateGatewayName("ICIE")
                .validateBankName("ICICI Bank")
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that I adhere to Key Fact Statement and digital lending consent should be displayed on ui for icici emi-dc")
    public void kfs_icici_dc_adhere_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText().getText();
        Assertions.assertThat(adhere).contains("I adhere to Key Fact Statement and digital lending consent");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact Statement and digital lending consent should be displayed on ui for icici emi-dc and it is clickable as link")
    public void kfs_icici_dc_adhere_text_link(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertClickable();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for icici emi dc")
    public void kfs_icici_dc_kfs_upper_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsUpperText().getText();
        Assertions.assertThat(kfsUpper).contains("Key Fact statement - EMI on Debit Card");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for icici emi dc")
    public void kfs_icici_dc_kfs_bank_date_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsBankDateText().getText();
        Assertions.assertThat(kfsUpper).contains("Name of Regulated entity - ICICI");
        Assertions.assertThat(kfsUpper).contains("Date:");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text should contain all the parameters for icici emi dc")
    public void kfs_icici_dc_kfs_table_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("Parameter");
        Assertions.assertThat(kfsUpper).contains("Details");
        Assertions.assertThat(kfsUpper).contains("Loan Amount (amount disbursed / to be disbursed to the borrower) in Rupees");
        Assertions.assertThat(kfsUpper).contains("Total interest charge during the entire tenor of the loan (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Other up-front charges, if any");
        Assertions.assertThat(kfsUpper).contains("a. GST applicable on Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("b. Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Net Disbursed Amount (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Total Amount to be paid by the borrower (sum of (i),(ii) and (iii)) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate - Effective annualised interest rate (in percentage) computed on net disbursed amount using IRR approach and reducing balance method (APR is exclusive of GST on processing fees)");
        Assertions.assertThat(kfsUpper).contains("Tenure of the loan in months");
        Assertions.assertThat(kfsUpper).contains("Repayment frequency");
        Assertions.assertThat(kfsUpper).contains("No. of instalments of repayment");
        Assertions.assertThat(kfsUpper).contains("Amount of each instalment of repayment in Rupees");
        Assertions.assertThat(kfsUpper).contains("Monthly");
        Assertions.assertThat(kfsUpper).contains("Details about Contingent Charges");
        Assertions.assertThat(kfsUpper).contains("Rate of annualised penal charges in case of delayed payments");
        Assertions.assertThat(kfsUpper).contains("Rate of annualized other penal charges");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period during which borrower shall not be charged any penalty on prepayment of loan");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/digital lending related complaints/issues");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower*");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS Disclaimer text should be displayed on KFS page for icici emi dc")
    public void kfs_icici_dc_kfs_disclaimer_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsDisclaimerText().getText();
        Assertions.assertThat(kfsUpper).contains("Note: There could be a difference in the amount to be paid by the borrower mentioned in (v) and that in repayment schedule shared because of any rounding off of the instalment amount in the repayment schedule.");
        Assertions.assertThat(kfsUpper).contains("*No recovery agent is assigned for loan account. In case any recovery agent is assigned, then particulars of such recovery agent will be communicated before the recovery agent contacts for recovery.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS loan text should be displayed on KFS page for icici emi dc")
    public void kfs_icici_dc_kfs_loan_consent_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsLoanConsentText().getText();
        Assertions.assertThat(kfsUpper).contains("I Read And Agreed");
        Assertions.assertThat(kfsUpper).contains("I/We expressly authorize and give consent to ICICI Bank to, disclose,transfer or part with any of my/our information, (including location), or an other device information when ICICI Bank considers such disclosure as necessary,with:");
        Assertions.assertThat(kfsUpper).contains("Agents of ICICI Bank, ICICI Bank's group entities in any jurisdiction");
        Assertions.assertThat(kfsUpper).contains("Auditors, credit rating agencies/credit bureaus, statutory/regulatory authorities,governmental/administrative authorities,Central Know Your Customer(CKYC) registery or SEBI Know Your Client registration agency,having jurisdiction over ICICI Bank or its group entities;");
        Assertions.assertThat(kfsUpper).contains("Service providers,or such person with whom ICICI Bank contracts or puproses to contract;");
        Assertions.assertThat(kfsUpper).contains("(Collectively referred to as \"Permitted Persons\")");
        Assertions.assertThat(kfsUpper).contains("For the purposes of:");
        Assertions.assertThat(kfsUpper).contains("Provision of the facility and completion of non-onboarding formalities; or");
        Assertions.assertThat(kfsUpper).contains("Complying with KYC requirements; or");
        Assertions.assertThat(kfsUpper).contains("Compliance with applicable laws or any order (judicial or otherwise),statutory/regulatory requirement or;");
        Assertions.assertThat(kfsUpper).contains("for credit reveiew of facilities availed; or");
        Assertions.assertThat(kfsUpper).contains("Authentication or verification; or");
        Assertions.assertThat(kfsUpper).contains("research or analysis, credit reporting and scoring,risk management,participation in any telecommunication; or");
        Assertions.assertThat(kfsUpper).contains("electronic clearing network and for use or processing of the said information/data");
        Assertions.assertThat(kfsUpper).contains("Disclosing any default in payment, for the purposes of recovering such amounts.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS Table Data for icici emi dc")
    public void kfs_icici_dc_kfs_table_Verifivation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfscloanAmount().getText();
        Assertions.assertThat(kfsUpper).contains("2000");
        String kfstenure = cashierPage.kfsctenure().getText();
        Assertions.assertThat(kfstenure).contains("3");
        String kfsinstallments = cashierPage.kfsinstallments().getText();
        Assertions.assertThat(kfsinstallments).contains("3");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that I adhere to Key Fact Statement should be displayed on ui for hdfc emi-dc")
    public void kfs_hdfc_dc_adhere_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText().getText();
        Assertions.assertThat(adhere).contains("I adhere to Key Fact Statement");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact Statement should be displayed on ui for hdfc emi-dc and it is clickable as link")
    public void kfs_hdfc_dc_adhere_text_link(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
            User user = userManager.getForRead(Label.UPIPUSHPG2);
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                    .setTXN_AMOUNT("2000.00")
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertClickable();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for hdfc dc emi")
    public void kfs_hdfc_dc_kfs_upper_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsUpperText().getText();
        Assertions.assertThat(kfsUpper).contains("Key Fact statement - EMI on Debit Card");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for hdfc emi dc")
    public void kfs_hdfc_dc_kfs_bank_date_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsBankDateText().getText();
        Assertions.assertThat(kfsUpper).contains("Name of Regulated entity - HDFC");
        Assertions.assertThat(kfsUpper).contains("Date:");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text should contain all the parameters for hdfc emi dc")
    public void kfs_hdfc_dc_kfs_table_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("Parameter");
        Assertions.assertThat(kfsUpper).contains("Details");
        Assertions.assertThat(kfsUpper).contains("Loan Amount (amount disbursed / to be disbursed to the borrower) in Rupees");
        Assertions.assertThat(kfsUpper).contains("Total interest charge during the entire tenor of the loan (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Other up-front charges, if any");
        Assertions.assertThat(kfsUpper).contains("a. GST applicable on Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("b. Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Net Disbursed Amount (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Total Amount to be paid by the borrower (sum of (i),(ii) and (iii)) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate - Effective annualised interest rate (in percentage) computed on net disbursed amount using IRR approach and reducing balance method (APR is exclusive of GST on processing fees)");
        Assertions.assertThat(kfsUpper).contains("Tenure of the loan in months");
        Assertions.assertThat(kfsUpper).contains("Repayment frequency");
        Assertions.assertThat(kfsUpper).contains("No. of instalments of repayment");
        Assertions.assertThat(kfsUpper).contains("Amount of each instalment of repayment in Rupees");
        Assertions.assertThat(kfsUpper).contains("Monthly");
        Assertions.assertThat(kfsUpper).contains("Details about Contingent Charges");
        Assertions.assertThat(kfsUpper).contains("Rate of annualised penal charges in case of delayed payments");
        Assertions.assertThat(kfsUpper).contains("Rate of annualized other penal charges");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period during which borrower shall not be charged any penalty on prepayment of loan");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/digital lending related complaints/issues");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower*");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS Disclaimer text should be displayed on KFS page hdfc emi dc")
    public void kfs_hdfc_dc_kfs_disclaimer_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsDisclaimerText().getText();
        Assertions.assertThat(kfsUpper).contains("Note: There could be a difference in the amount to be paid by the borrower mentioned in (v) and that in repayment schedule shared because of any rounding off of the instalment amount in the repayment schedule.");
        Assertions.assertThat(kfsUpper).contains("*No recovery agent is assigned for loan account. In case any recovery agent is assigned, then particulars of such recovery agent will be communicated before the recovery agent contacts for recovery.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS Table Data for hdfc emi dc")
    public void kfs_hdfc_dc_kfs_table_Verifivation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfscloanAmount().getText();
        Assertions.assertThat(kfsUpper).contains("2000");
        String kfstenure = cashierPage.kfsctenure().getText();
        Assertions.assertThat(kfstenure).contains("3");
        String kfsinstallments = cashierPage.kfsinstallments().getText();
        Assertions.assertThat(kfsinstallments).contains("3");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify paybutton is  in disabled state if kfs consent is not clicked for ICICI emi dc using new kfs changes")
    public void kfs_icici_dc_disabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4572741654006328");
        cashierPage.buttonPGPayNow().assertNotClickable();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify paybutton is  in disabled state if kfs consent is not clicked for HDFC emi dc using new kfs changes")
    public void kfs_hdfc_dc_disabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        cashierPage.buttonPGPayNow().assertNotClickable();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify  txn of HDFC emi dc using new kfs changes")
    public void kfs_hdfc_dc(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.KFS_HDFC, theme)
                .setTXN_AMOUNT("2000.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfs(cashierPage,"4444333322221111");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    }

