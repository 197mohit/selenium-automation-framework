package scripts;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.wallet.transitWallet.AddFundsToSubWalletTransit;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peon;
import io.qameta.allure.Owner;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.base.test.Group.Status;

/**
 * Created by deepakkumar on 4/12/17.
 */
@Owner("Deepak")
public class AddMoneyMP extends PGPBaseTest implements IAddMoney {

    private static final String DUPLICATE_ORDER_ID = "Duplicate order id";

    @Parameters("theme")
    @Test(description = "Verify successful txn when payment is done via CC")
    public void PGP_383_verifySuccessfulTxnUsingCC(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user).build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

//    @Parameters("theme")
//    @Test(description = "Verify that the proper message is displayed when wallet AddMoney limit exceeds.", enabled = false)
    public void PGP_382_validateProperMsgDisplayedWhenWalletAddMoneyLimitExceeds(@Optional("merchant4") String theme) throws Exception {
        throw new SkipException("wallet limits scenarios testing pending");
    }

    @Parameters("theme")
    @Test(description = "Verify Txn failure when user account has insufficient balance.", groups = {"debug"})
    public void PGP_381_VerifyFailureWhenBankHasInsufficientFunds(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT("99.94")//txn amt set to 99.94 to trigger insufficient amt condition in mock
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

//    @Parameters("theme")
//    @Test(description = "Verify that transaction is allowed when valid sso token is passed.", enabled = false)
    public void PGP_380_validateTxnIsAllowedWhenValidSSOTokenIsPassed(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

 //   @Parameters({"theme"})
 //   @Test(description = "Verify transaction is not allowed when duplicate OrderID is passed.", enabled = false)
    public void PGP_384_validateTxnNotAllowedWhenDuplicateOrderIdIsPassed(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme).build();
        PGPHelpers.executeProcessTransaction(orderDTO);
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage("Error message duplicate order id should be displayed.").containsIgnoringCase(DUPLICATE_ORDER_ID);
    }

    @Issue("PGP-18736")
    @Parameters({"theme"})
    @Test(description = "Verify that if SSO token is expired then exception must occur", groups = Status.BUG)
    public void PGP_385_validateTxnNotAllowedWhenExpiredSSoTokenIsPassed(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String activeSSOToken = user.ssoToken();
        try {
            AuthHelpers.logout(activeSSOToken);
        } finally {
            user.purge();
        }
        String expiredSSOToken = activeSSOToken;
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme)
                .setSSO_TOKEN(expiredSSOToken)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("SSO Token is invalid");
        }

    private void validateFailureResponsePage(String theme) {
        ResponsePage responsePage = new ResponsePage();
        if (theme.equalsIgnoreCase("merchant4")) {
            LostInSpacePage lostInSpacePage = new LostInSpacePage();
            lostInSpacePage.imgLostInSpace().assertVisible();
        } else if (theme.equalsIgnoreCase("enhancedweb")) {
            responsePage.validateStatus("TXN_FAILURE");
        } else if (theme.equalsIgnoreCase("enhancedwap")) {
            responsePage.validateStatus("TXN_FAILURE");
        }
    }

    @Issue("PGP-14961")
    @Parameters({"theme"})
    @Test(description = "Verify that if channel is invalid then exception must occur", groups = Status.BUG)
    public void PGP_386_validateTxnNotAllowedWhenInvalidChannelIsPassed(@Optional("enhancedweb") String theme) {
        String invalidChannel = "WWW";
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme)
                .setCHANNEL_ID(invalidChannel)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        new LostInSpacePage().imgLostInSpace().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that only active acquiring of merchant must display as payment mode at cashier page")
    public void PGP_387_verifyOnlyActiveAcquiringsAreShownAsPayModes(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        if(theme.equalsIgnoreCase("merchant4"))
            cashierPage.tabNetBanking().assertVisible();
        else
        cashierPage.tabNetBanking().assertVisible();//txnAmount is less than 2000
        //cashierPage.tabUPI().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabATM().assertNotVisible();
        cashierPage.tabCOD().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the successful saved card transaction.", groups = {"debug"})
    public void PGP_389_verifyTxnSuccessfullyProcessedWhenPaymentViaSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC_ONLY.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that Txn details are getting reflected correctly on the response page")
    public void PGP_390_verifyTxnDetailsShownInResponsePageAreAsExpected(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify transaction is allowed when correct MID is passed.")
    public void PGP_378_verifyTxnIsAllowedWhenCorrectMidIsPassed(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.validate();
    }

    @Parameters("theme")
    @Test(description = "Verify NB tab not displayed for Addmoney when txnAmount < 2000")
    public void PGP_394_1_verifyNBtabNotDisplayed(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT("1999")
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabNetBanking().assertNotVisible();
    }

        @Parameters("theme")
    @Test(description = "Verify successful NB Transaction.")
    public void PGP_394_verifySuccessfulTxnWhenPaymentViaNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT("2000")
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

//    @Parameters("theme")
//    @Test(description = "Verify txn is successful when payment is done via UPI", enabled = false)
    public void PGP_395_verifySuccessfulTxnWhenPaymentViaUPI(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT("11")
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                //.validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(ValidationType.NOT_PRESENT)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters("theme")
    @Test(description = "Verify txn is successful when payment is done via DC")
    public void PGP_396_verifySuccessfulTxnWhenPaymentViaDC(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

 //   @Owner("Surbhi Pathak")
 //   @Parameters({"theme"})
  //  @Test(description = "Validate saved card should not displayed for if theme is merchantLow|ccdc", enabled = false)
    public void checkSavedCardFormerchantLowccdc(@Optional("merchantLow|ccdc") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP,theme,user).
                build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertSavedCardNotVisible();
    }

  //  @Owner("Surbhi Pathak")
  //  @Parameters({"theme"})
  //  @Test(description = "Verify if user is able to save credit card when theme passed is merchantLow|ccdc.", enabled = false)
    public void checkUserAbleToSaveCC(@Optional("merchantLow|ccdc") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP,theme,user).
                build();
        String custId = orderDTO.getCUST_ID();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        OrderDTO orderDTO1 = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme,user).
                setCUST_ID(custId).
                build();
        new CheckoutPage().createOrder(orderDTO1);
        cashierPage.waitUntilLoads();
        cashierPage.assertSavedCardNotVisible();
    }

//    @Owner("Surbhi Pathak")
//    @Parameters({"theme"})
//    @Test(description = "Verify if user is able to save debit card when theme passed is merchantLow|ccdc.", enabled = false)
    public void checkUserAbleToSaveDC(@Optional("merchantLow|ccdc") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP,theme,user).
                build();
        String custId = orderDTO.getCUST_ID();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.DC);
        OrderDTO orderDTO1 = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme,user).
                setCUST_ID(custId).
                build();
        new CheckoutPage().createOrder(orderDTO1);
        cashierPage.waitUntilLoads();
        cashierPage.assertSavedCardNotVisible();
    }

//    @Owner("Surbhi Pathak")
//    @Parameters({"theme"})
//    @Test(description = "Validate saved card should be displayed for if theme is merchantLow", enabled = false)
    public void checkSavedCardForOthertheme(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP,theme,user).
                build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertSavedCardVisibility();
    }

    @Parameters("theme")
    @Test(description = "test txn < 1 not allowed")
    public void testTxnOfLessThan1NotAllowed(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchant = MerchantType.AddMoneyMP;
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(merchant, theme, user)
                .setTXN_AMOUNT("0.99")
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("308")
                .validateRespMsg("Invalid Txn Amount")
                .validateCheckSum(merchant.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .AssertAll();
    }

    @Parameters("theme")
    @Test(description = "Verify successful ADD Money txn when payment is done via NB --IDBI")
    public void verifySuccessfulTxnUsingNBIDBI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user).setREQUEST_TYPE("ADD_MONEY").setTXN_AMOUNT("2000").build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("IDBI");
        cashierPage.payBy(PayMode.NB,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("IDBI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("IDBI")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));

    }

    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test
    public void testCardIndexNoIsPresentInOutputWhenCardTokenRequiredIsTrueInOrder(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO order = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setCardTokenRequired(true)
                .build();
        new CheckoutPage().createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals("TXN_SUCCESS"),
                responsePage.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO)
        );
        sAssert.eval();
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NON_EMPTY).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        // This test case was to assert cardIndexNo but due to an prod bug now cardIndexNo does not comes in any scenario in peon
        assertion.apply(peon.keys().contains("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE"));
    }

    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test
    public void testCardIndexNoIsAbsentInOutputWhenCardTokenRequiredIsFalseInOrder(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO order = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setCardTokenRequired(false)
                .build();
        new CheckoutPage().createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals("TXN_SUCCESS"),
                responsePage.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not()
        );
        sAssert.eval();
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NOT_PRESENT).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not());
    }

    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test
    public void testCardIndexNoIsAbsentInOutputWhenCardTokenRequiredParamIsNotPassedInOrder(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO order = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        new CheckoutPage().createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals("TXN_SUCCESS"),
                responsePage.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not()
        );
        sAssert.eval();

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NOT_PRESENT).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not());
    }

    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Parameters({"theme"})
    @Test
    public void testCardHashIsPresentInOutput(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO order = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .build();
        new CheckoutPage().createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS).equals("TXN_SUCCESS"),
                responsePage.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_HASH)
        );
        sAssert.eval();

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardHash(ValidationType.NON_EMPTY).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_HASH));
    }


    public void enableNewFlow()
    {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID='theia_addMoneyFeeWalletConsult'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_theia_addMoneyFeeWalletConsult");
    }

    public void disableNewFlow()
    {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='0' WHERE FEAT_UID='theia_addMoneyFeeWalletConsult'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_theia_addMoneyFeeWalletConsult");
    }

    @Override
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Parameters({"theme"})
    public void validateFullKYCWalletCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String txnAmount = "10001";
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.setZeroBalance(user);
        Response response = WalletHelpers.checkWalletLimit(user,txnAmount,"MAIN","","","CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().getString("response.feeDetails[0].feeAmount")).isNotEmpty().isNotNull();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();


    }

    @Override
    @Test
    @Owner("Tarun")
    @Parameters({"theme"})
    @Feature("PGP-19696")
    public void validateFullKYCWalletSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String txnAmount = "10001";
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        Response response = WalletHelpers.checkWalletLimit(user,txnAmount,"MAIN","","","CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().getString("response.feeDetails[0].feeAmount")).isNotEmpty().isNotNull();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();

    }

  /*  @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Parameters({"theme"})
    @Issue("PGP-24839")
    @Override */
    public void validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String txnAmount = "10001";
        User user = userManager.getForRead(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }


  /*  @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Parameters({"theme"})
    @Issue("PGP-24839")
    @Override */
    public void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String txnAmount = "10001";
        User user = userManager.getForRead(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

  /*  @Test(enabled = false)
    @Override */
    public void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
       //No need
    }
   /*
    @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Parameters({"theme"})
    @Issue("PGP-24839")
    @Override */
    public void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String txnAmount = "10001";
        User user = userManager.getForRead(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";

        String txnAmount = "10001";
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Parameters({"theme"})
    @Override
    public void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";

        String txnAmount = "10001";
        User user = userManager.getForRead(Label.LOGIN);

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Parameters({"theme"})
    @Override
    public void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";

        String txnAmount = "10";
        User user = userManager.getForRead(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();

    }
  /*  @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Parameters({"theme"})
    @Override */
    public void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";

        String txnAmount = "10001";
        User user = userManager.getForRead(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }
 /*   @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Parameters({"theme"})
    @Override */
    public void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";

        String txnAmount = "10001";
        User user = userManager.getForRead(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";

        String txnAmount = "10001";
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }
 /*   @Test(enabled = false)
    @Override
    */
    public void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //No Need
    }
 /*   @Test(enabled = false)
    @Override */
    public void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //No Need
    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-26893")
    @Test(description = "To verify add money success txn and addMoneyDestination is TRANSIT_BLOCKED wallet")
    public void verifyAddMoneySuccess(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.AddMoneyMP;
        String txnAmount = "5.0";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"329915210\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String orderId = CommonHelpers.generateOrderId();
        PaymentDTO paymentDTO = new PaymentDTO();

        AddFundsToSubWalletTransit addFundsToSubWalletTransit = new AddFundsToSubWalletTransit(orderId,txnAmount,paymentDTO.getCreditCardNumber(),merchantType,user);
        addFundsToSubWalletTransit.execute();

        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .setORDER_ID(orderId)
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"PAYMODE_DECISION_MAKER_TASK\" | grep \"REQUEST\"";
        String payModeDecisionMaker = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(payModeDecisionMaker).as("Add Money Destination is incorrect").contains("TRANSIT_BLOCKED_WALLET");

    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-26893")
    @Test(description = "verify add money txn when txn amount>transit wallet max limit amount(2000)")
    public void verifyMaxLimit2000(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.AddMoneyMP;
        String txnAmount = "2001.0";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"329915210\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String orderId = CommonHelpers.generateOrderId();
        PaymentDTO paymentDTO = new PaymentDTO();

        AddFundsToSubWalletTransit addFundsToSubWalletTransit = new AddFundsToSubWalletTransit(orderId,txnAmount,paymentDTO.getCreditCardNumber(),merchantType,user);
        addFundsToSubWalletTransit.execute();

        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .setORDER_ID(orderId)
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"PAYMODE_DECISION_MAKER_TASK\" | grep \"REQUEST\"";
        String payModeDecisionMaker = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(payModeDecisionMaker).as("Add Money Destination is incorrect").contains("TRANSIT_BLOCKED_WALLET");


    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-26893")
    @Test(description = "Verify txn should be success when add money success txn for basic+ user(limit 10000)")
    public void verifyLimitExact(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.AddMoneyMP;
        String txnAmount = "5000.0";
        String goodsInfo = "[  {  \"merchantGoodsId\":\"329915210\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String orderId = CommonHelpers.generateOrderId();
        PaymentDTO paymentDTO = new PaymentDTO();

        AddFundsToSubWalletTransit addFundsToSubWalletTransit = new AddFundsToSubWalletTransit(orderId,txnAmount,paymentDTO.getCreditCardNumber(),merchantType,user);
        addFundsToSubWalletTransit.execute();

        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(merchantType, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .setORDER_ID(orderId)
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"PAYMODE_DECISION_MAKER_TASK\" | grep \"REQUEST\"";
        String payModeDecisionMaker = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(payModeDecisionMaker).as("Add Money Destination is incorrect").contains("TRANSIT_BLOCKED_WALLET");



    }

    @Parameters({"theme"})
    @Test(description = "test PPBL pay mode is disabled when user has insufficient PPBL balance")
    public void testPPBLPayModeIsDisabledWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        Assertions.assertThat(cashierPage.checkboxPPBL().isEnabled()).as("PPBL paymode is disabled").isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "test err msg is displayed when user has insufficient PPBL balance")
    public void testErrMsgIsDisplayedWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        Assertions.assertThat(cashierPage.tabPPBL().content().toString()).as("Getting insufficient PPBL balance msg").contains("You do not have enough balance for this payment");
    }

    @Parameters({"theme"})
    @Test(description = "test next pay mode is selected when user has insufficient PPBL balance")
    public void testNextPayModeIsSelectedWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddMoneyMP(MerchantType.AddMoneyMP, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        WebElement payModeNextToPPBL = DriverManager.getDriver().findElement(By.cssSelector("section[id=ptm-ppb]"));
        Assertions.assertThat(payModeNextToPPBL.getAttribute("class").contains("active")).as("Paymode next to PPBL is selected").isTrue();
    }
}
