package scripts.subscription.subscriptionFirstRequest.enhancedFlows;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.savecardService.SaveCard;

import java.text.DecimalFormat;
import java.util.Date;

import static com.paytm.apphelpers.CommonHelpers.addDays;
import static com.paytm.apphelpers.CommonHelpers.getDate;

@Owner("Tarun")
public class SubscriptionViaPpi extends PGPBaseTest {
    private static final double AMOUNT_TO_DEDUCT_FROM_BANK = 1.00;
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String payMode;

    @Parameters({"payMode"})
    @BeforeClass
    public void setPayMode(@Optional("CC") String payMode) {
        this.payMode = payMode.toUpperCase();
    }

    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount > 0, w/o saved card")
    public void PGP_36_ValidateSuccessfulPaymentWithoutSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme).build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount > 0, with saved card")
    public void PGP_37_ValidateSuccessfulPaymentWithSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.SAVED_CARD, paymentDTO);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription details on cashier page when user has saved card")
    public void PGP_27_validateSubsDetailsOnCashierPageWhenSavedCardPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.subsDetailsInfo().click();
        //Assertions.assertThat(cashierPage.subscriptionDetails().getText()).as("Subscription UI message change").isEqualTo("View Subscription Details");
        Assertions.assertThat(cashierPage.subDetailsPage().getText()).as("Subscription UI message change").isEqualTo("Amount to be Paid Now");
    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription details on cashier page when user doesn't has saved card")
    public void PGP_28_validateSubsDetailsOnCashierPageWhenSavedCardNotPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.subsDetailsInfo().click();
        //Assertions.assertThat(cashierPage.subscriptionDetails().getText()).as("Subscription UI message change").isEqualTo("View Subscription Details");
        Assertions.assertThat(cashierPage.subDetailsPage().getText()).as("Subscription UI message change").isEqualTo("Amount to be Paid Now");
    }

    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount > 0")
    public void PGP_33_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountGtZero(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       cashierPage.login(user);
        EmptyCardDetails:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCreditCardNumber("").setExpMonth("").setExpYear("").setCvvNumber("");
            cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
            String errMsg = cashierPage.paymentContainer().getText();
            Assertions.assertThat(errMsg).as("Invalid error message")
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_CREDIT_CARD_NUMBER.toString());
        }
        InvalidExpiryDate:
        {
            DecimalFormat formatter = new DecimalFormat("00");
            String currentMonth = formatter.format(CommonUtils.getCurrentMonth());
            if (currentMonth.equals("00")) {
                Reporter.log("<br>Skipping the InvalidExpiry check because of January Month.");
            } else {
                PaymentDTO paymentDetails = new PaymentDTO().setExpMonth(currentMonth).setExpYear("19");
                cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
                String errMsg = cashierPage.paymentContainer().getText();
                Assertions.assertThat(errMsg).as("Invalid expiry date")
                        .containsIgnoringCase(Constants.MessageAssert.INVALID_EXPIRY.toString());
            }
        }
        InvalidCardNumber:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCreditCardNumber("3045751111111117");
            cashierPage.textBoxCardNumber().clearAndType(paymentDetails.getCreditCardNumber());
            String errMsg = cashierPage.notificationContainer().getText();
            Assertions.assertThat(errMsg).as("Invalid card number")
                    .containsIgnoringCase(Constants.MessageAssert.INVALID_PAYMENT_DETAILS.toString());
        }
        InvalidCVV:
        {
            PaymentDTO paymentDetails = new PaymentDTO();
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().clearAndType(paymentDetails.getCreditCardNumber());
            cashierPage.pause(3);
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "4");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount = 0")
    public void PGP_34_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountEqualsZero(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        EmptyCardDetails:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCreditCardNumber("").setExpMonth("").setExpYear("").setCvvNumber("");
            cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
            String errMsg = cashierPage.paymentContainer().getText();

            Assertions.assertThat(errMsg).as("Invalid error message")
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_CREDIT_CARD_NUMBER.toString());
        }
        InvalidExpiryDate:
        {
            DecimalFormat formatter = new DecimalFormat("00");
            String currentMonth = formatter.format(CommonUtils.getCurrentMonth());
            if (currentMonth.equals("00")) {
                Reporter.log("<br>Skipping the InvalidExpiry check because of January Month.");
            } else {
                PaymentDTO paymentDetails = new PaymentDTO().setExpMonth(currentMonth).setExpYear("19");
                cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
                String errMsg = cashierPage.paymentContainer().getText();
                Assertions.assertThat(errMsg).as("Invalid expiry date")
                        .containsIgnoringCase(Constants.MessageAssert.INVALID_EXPIRY.toString());
            }
        }
        InvalidCardNumber:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCreditCardNumber("3045751111111117");
            cashierPage.textBoxCardNumber().clearAndType(paymentDetails.getCreditCardNumber());
            String errMsg = cashierPage.notificationContainer().getText();
            Assertions.assertThat(errMsg).as("Invalid card number")
                    .containsIgnoringCase(Constants.MessageAssert.INVALID_PAYMENT_DETAILS.toString());
        }
        InvalidCVV:
        {
            PaymentDTO paymentDetails = new PaymentDTO();
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().clearAndType(paymentDetails.getCreditCardNumber());
            cashierPage.pause(3);
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "4");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount = 0, w/o saved card")
    public void PGP_30_ValidateSuccessfulZeroAmtPaymentWithoutSavedCard(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       // cashierPage.payBy(PayMode.valueOf(payMode));
        cashierPage.payBy(PayMode.DC);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Card details are required For zero rs subscription payment, " +
            "when wallet amount is zero or less than 1 rs, PPIN Flow")
    public void VerifyZeroAmtPaymentWithWalletBalanceLessThanOnePPIN(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("0")
                .build();
        Double walletAmount = 0.50;
        WalletHelpers.modifyBalance(user, walletAmount);
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().assertDisabled();
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        if(theme.equals("enhancedweb")) {
            Assertions.assertThat(payButtonText).isEqualTo("ADD Rs " + "0.50" + " TO PAY");
        }
        else
            Assertions.assertThat(payButtonText).isEqualTo("ADD Rs " + "0.50" + " to PAY");
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Card details are required For zero rs subscription payment, " +
            "when wallet amount is zero or less than 1 rs, PPIY Flow")
    public void VerifyZeroAmtPaymentWithWalletBalanceLessThanOnePPIY(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.Subscription_Pg2_MID2, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("0")
                .build();
        WalletHelpers.modifyBalance(user, 0.50);
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().assertDisabled();
        cashierPage.payBy(PayMode.DC);
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        if(theme.equals("enhancedweb")) {
            Assertions.assertThat(payButtonText).isEqualTo("Pay Rs " + "1" + " to Subscribe");
        }
        else
            Assertions.assertThat(payButtonText).isEqualTo("Pay Rs " + "1" + " to Subscribe");

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Card details are required For zero rs subscription payment, " +
            "when wallet amount is zero or less than 1 rs, PPIY Flow with SubsOnWallet preference Enabled")
    public void VerifyZeroAmtPaymentWithWalletBalanceLessThanOnePPIYSubsonWalletEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_LIMIT, theme)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("0")
                .build();
        WalletHelpers.modifyBalance(user, 0.50);
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.checkBoxPPI().assertDisabled();
        cashierPage.tabDebitCard().click();
      String payButtonText = cashierPage.buttonPGPayNow().getText();
        if(theme.equals("enhancedweb")) {
            Assertions.assertThat(payButtonText).isEqualTo("Pay Rs " + "1" + " to Subscribe");
        }
        else
            Assertions.assertThat(payButtonText).isEqualTo("Pay Rs " + "1" + " to Subscribe");

       /* if(theme.equals("enhancedweb")) {
            Assertions.assertThat(payButtonText).isEqualTo("ADD Rs " + "0.50" + " TO PAY");
        }
        else
            Assertions.assertThat(payButtonText).isEqualTo("ADD Rs " + "0.50" + " to PAY");*/
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.00")
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }


    // Issue Exception while querying attribute/get/subscription/detail , mapping id can't be null
    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount = 0, with saved card")
    public void PGP_31_ValidateSuccessfulZeroAmtPaymentWithSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("5")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateSubsId(ValidationType.NON_EMPTY)
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(PGPHelpers.getSavedCardId(subsId),user );
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate card getting saved for successful subs_PPI txn.")
    public void PGP_35_validateCardGettingSaveForSuccessfulTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("2.00")
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
        SavedCardHelpers.validateSavedCardPresence(user);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PPI Subscription when Wallet Balance < Transaction Amount")
    public void PGP_76_validateSuccessfulPPISubsWhenWalletBalLessThanTxnAmt(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.CC);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PPI Subscription when Wallet Balance > Transaction Amount")
    public void PGP_77_validateSuccessfulPPISubsWhenWalletBalGreaterThanTxnAmt(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PPI Subscription when Wallet Balance = Transaction Amount")
    public void PGP_78_validateSuccessfulPPISubsWhenWalletBalEqualsToTxnAmt(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Payment should be failed when Transaction Amount = (wallet balance +0.5)")
    public void PGP_79_validateFailedTxnWithTxnAmntGreaterThanWalletByFraction(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setTXN_AMOUNT("2.5")
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 0.5);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));

        String errorMsg = cashierPage.notificationContainer().getText();
        Assertions.assertThat(errorMsg).isEqualToIgnoringCase(Constants.MessageAssert.SOMETHING_WENT_WRONG.toString());
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful transaction with SSOToken login.")
    public void PGP_83_ValidateSuccessfulPPISubscriptionWithSSOTokenLogin(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Txn with 'SAVECARD_ID'= BLANK and Call type =S2S.")
    public void PGP_87_ValidateS2StxnWithSavedCardBlank() throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, "", user)
                .setSUBS_PAYMENT_MODE("PPI").build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "InvalidSavedCardID", response);
    }

    @Parameters({"theme"})
    @Test(description = "Validate with 'SAVECARD_ID'= valid saved card and Call type =S2S and amount=0.")
    public void PGP_88_ValidateS2StxnWithValidSavedCardAndTxnAmntZero() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        JsonPath jsonPath = response.jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
        softly.assertThat(jsonPath.getString("RESPCODE")).isEqualTo("01");
        softly.assertThat(jsonPath.getString("RESPMSG")).isEqualToIgnoringCase("Subscription registered successfully");
        softly.assertThat(jsonPath.getString("MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("SUBS_ID")).isNotEmpty();
        softly.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate with 'SAVECARD_ID'= valid saved card and Call type =S2S and amount>0.")
    public void PGP_89_ValidateS2STxnWithValidSavedCardAndTxnAmtGreaterThanZero() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .setTXN_AMOUNT("2.0")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        JsonPath jsonPath = response.jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
        softly.assertThat(jsonPath.getString("RESPCODE")).isEqualTo("308");
        softly.assertThat(jsonPath.getString("RESPMSG")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT() + " is not a valid value for TxnAmount");
        softly.assertThat(jsonPath.getString("MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate with 'SAVECARD_ID'= valid saved card of another user and Call type =S2S.")
    public void PGP_90_ValidateS2StxnWithValidSavedCardIDOfAnotherUser() throws Exception {
        User user1 = userManager.getForWrite(Label.LOGIN);
        User user2 = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();

        SavedCardHelpers.deleteSavedCard(user1);
        SavedCardHelpers.addCard(user1, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user1, 0);

        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_Pg2_MID1, savedCardId, user2)
                .setSUBS_PAYMENT_MODE("PPI")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Saved Card ID", response);
    }

    @Issue("PGP-18755")
    @Parameters({"theme"})
    @Test(description = "Validate with call type S2S and SsoToken Invalid",groups = Group.Status.BUG)
    public void PGP_91_validateS2StxnWithInvalidSSOToken() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .setSSO_TOKEN("17057c26-efc4-4b77-a39c-e131d800f54d")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Token", response);
    }

    @Parameters({"theme"})
    @Test(description = "Validate with call type S2S and SsoToken Blank.")
    public void PGP_92_validateS2StxnWithBlankSSOToken() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_Pg2_MID1, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .setSSO_TOKEN("")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "InvalidToken", response);
    }

    @Issue("PGP-16706")
    @Parameters({"theme"})
    @Test(groups = Group.Status.BUG,description = "Validate Transaction should not fail when txn amount > max amount")
    public void PGP_101_validateTxnAmtGreaterThanSubsMaxAmt(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setTXN_AMOUNT("101.0")
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.payBy(PayMode.CC);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    private void verifyS2SFailureResponse(OrderDTO orderDTO, String expectedRespMsg, Response response) {
        JsonPath jsonPath = response.jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat((String) jsonPath.get("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat((String) jsonPath.get("TXNAMOUNT")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        softly.assertThat(jsonPath.getString("RESPMSG")).isEqualToIgnoringCase(expectedRespMsg);
        softly.assertThat((String) jsonPath.get("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat((String) jsonPath.get("STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Failed payment, txn amount = 0, with expired saved card")
    public void ValidateFailedZeroAmtPaymentWith_expiredSavedCard(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        SaveCard saveCard = new SaveCard();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setTXN_AMOUNT("0")
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        //save credit card
        String cardId = savedCardHelpers.saveCardUserId(user.custId(), saveCard.AesEncCardNumCredit, saveCard.AesEncExp, new PaymentDTO().getCreditCardNumber()).getResponse().toString();
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId,user);


        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_userId(user);
        Assertions.assertThat(responseBase.getResponseInSaveCardResponseList().size()).isEqualTo(0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.tabSavedCard().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription tray should contain renew date")
    public void validateStartDateMoreThanTodayDate(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        String startDate = getDate(addDays(new Date(), 1), "yyyy-MM-dd");
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_START_DATE(startDate)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsDetailsInfo().click();
        //cashierPage.validateSubscriptionTrayText();
//        Assertions.assertThat(cashierPage.subscriptionTray().getText()).as("Subscription Tray does'nt contain Renew Date").containsIgnoringCase("Next payment due on");
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.subscriptionTray().getText()).containsIgnoringCase("Next payment");

    }

    @Parameters("theme")
    @Test(description = "Validate payment done when subs_amount_type='FIX' with insufficient wallet balance")
    public void PGP_78_ValidatePaymentWhenFixedType(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_MAX_AMOUNT("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters("theme")
    @Test(description = "Verify after login is successful, status is changed in contract_v2 table from INIT to ACTIVE after payment")
    public void PGP_18282_ValidateStatusAfterLogin(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        String subsStatus=null;
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_LIMIT, theme).build();
        double txnAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        WalletHelpers.modifyBalance(user, txnAmt);
        checkoutPage.createOrder(orderDTO);
        //Verify INIT status in contract_v2 table
        subsStatus = PGPHelpers.getSubsStatus(orderDTO.getORDER_ID());
        Assertions.assertThat(subsStatus).isEqualTo("INIT");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        String subsmetadata = PGPHelpers.getSubsMetadata(orderDTO.getORDER_ID());
        Assertions.assertThat(subsmetadata).contains("isOtpAuthorized\":true");
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(MerchantType.SUBSCRIPTION_WALLET_LIMIT.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

        //Verify ACTIVE status in contract_v2 table
        subsStatus = PGPHelpers.getSubsStatus(orderDTO.getORDER_ID());
        Assertions.assertThat(subsStatus).isEqualTo("ACTIVE");
    }

    @Parameters("theme")
    @Test(description = "Validate payment done with Daily frequency with sufficient wallet balance")
    public void PGP_80_ValidateSubsForDailyFrequency(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setSUBS_GRACE_DAYS("0")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();

    }

    @Parameters("theme")
    @Test(description = "Validate txn should be done with any pay modes if all payment modes are visible")
    public void validatePPBLTxnIfAllPaymodesAreAvailable(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("PPBL")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();

    }

    @Parameters({"theme"})
    @Test(description = "Verify Auth Modes are displayed in Subscription Bank Mandate")
    public void PGP_25155_verifyAuthModesinSubscriptionBankMandate(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.BANK_MANDATE, theme)
                .setSSO_TOKEN("")
                .setSUBS_PAYMENT_MODE("BANK_MANDATE")
                .setSUBS_PPI_ONLY("")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();
        cashierPage.selectBankMandateBank("HDFC").click();
        cashierPage.bankmandateAuthMode("Net Banking").assertVisible();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.login(user);
        cashierPage.tabBankMandate().click();
        cashierPage.selectBankMandateBank("HDFC").click();
        cashierPage.pause(2);
        cashierPage.bankmandateAuthMode("Net Banking").assertVisible();
    }

//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Auth Modes are displayed in Subscription Bank Mandate with second user after logging out from the first")
    public void PGP_25155_verifyAuthModesinSubscriptionBankMandatewithSecondUser(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user1 = userManager.getForWrite(Label.LOGIN);
        User user2 = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.BANK_MANDATE, theme)
                .setSSO_TOKEN("")
                .setSUBS_PAYMENT_MODE("BANK_MANDATE")
                .setSUBS_PPI_ONLY("")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user1);
        cashierPage.tabBankMandate().click();
        cashierPage.selectBankMandateBank("HDFC").click();
        cashierPage.bankmandateAuthMode("Net Banking").assertVisible();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.logout(user1);
        cashierPage.login(user2);
        cashierPage.tabBankMandate().click();
        cashierPage.selectBankMandateBank("HDFC").click();
        cashierPage.bankmandateAuthMode("Net Banking").assertVisible();
    }

    //Card Txn is not supported after SI HUB Release
//    @Parameters({"theme"})
//    @Epic(Constants.Sprint.SPRINT34_2)
//    @Feature("PGP_24623")
//    @Owner("Tarun")
//    @Test(enabled = false, description = "P+ Enhanced Subscription : 1st request for PPI only( no paymode specified), check cards when wallet balance insufficient")
    public void checkSavedCardsWhenInsufficientBalance(@Optional("enhancedweb") String theme) throws Exception {
       MerchantType subscriptionMerchant = MerchantType.SUBSCRIPTION_ADDNPAY;
       SavedCardHelpers.assertStoreCardPrefEnabled(subscriptionMerchant);
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.enableAllSavedCardFlags();
        String custId = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(subscriptionMerchant, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(subscriptionMerchant.getId(),custId);

        //Adding for MID/CustId on P+ side
        SavedCardHelpers.addCardAlipay(subscriptionMerchant.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.login(user);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO,paymentDTO.getCreditCardNumber());

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "test PPBL pay mode is disabled when user has insufficient PPBL balance")
    public void testPPBLPayModeIsDisabledWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        WalletHelpers.setZeroBalance(user);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        Assertions.assertThat(cashierPage.checkboxPPBL().isEnabled()).as("PPBL paymode is disabled").isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "test err msg is displayed when user has insufficient PPBL balance")
    public void testErrMsgIsDisplayedWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        WalletHelpers.setZeroBalance(user);
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
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        WalletHelpers.setZeroBalance(user);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        WebElement payModeNextToPPBL = DriverManager.getDriver().findElement(By.cssSelector("section[id=ptm-ppb]"));
        Assertions.assertThat(payModeNextToPPBL.getAttribute("class").contains("active")).as("Paymode next to PPBL is selected").isTrue();

    }

    @Owner("AJEESH")
    @Feature("PGP-33733")
    @Parameters({"theme"})
    @Test(description = "Pre-fill the Bank IFSC code and account holder name using the saved mandate object details")
    public void TC_001_Verify_Fields_are_noneditable(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.BANK_MANDATE, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setBANK_CODE("PPBL")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setACCOUNT_TYPE("Savings")
                .setAccountNumber("915445500424")
                .setBANK_IFSC("PYTM0000001")
                .setUSER_NAME("Ajeesh Nair")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();

        Assert.assertTrue(cashierPage.IfscDetails().getAttribute("value").contains("PYTM0000001"));
        Assert.assertTrue(cashierPage.UserBankName().getAttribute("value").contains("Ajeesh Nair"));
        Assert.assertTrue(cashierPage.BankDetails().getAttribute("value").contains("915445500424"));


        Assert.assertFalse(cashierPage.IfscDetails().isEnabled());  // verify button is non-editable
        Assert.assertFalse(cashierPage.UserBankName().isEnabled()); // verify button is non-editable
        Assert.assertFalse(cashierPage.BankDetails().isEnabled());  // verify button is non-editable

    }

    @Owner("AJEESH")
    @Feature("PGP-33733")
    @Parameters({"theme"})
    @Test(description = "Pre-fill the Bank IFSC code and account holder name using the saved mandate object details")
    public void TC_002_Verify_Fields_are_editable(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.BANK_MANDATE, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setBANK_CODE("PPBL")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setACCOUNT_TYPE("Savings")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();

        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Ajeesh Nair");
        cashierPage.BankDetails().sendKeys("915445500424");

        Assert.assertTrue(cashierPage.IfscDetails().getAttribute("value").contains("PYTM0000001"));
        Assert.assertTrue(cashierPage.UserBankName().getAttribute("value").contains("Ajeesh Nair"));
        Assert.assertTrue(cashierPage.BankDetails().getAttribute("value").contains("915445500424"));


    }


    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= VARIABLE and FrequencyUnit =MONTH")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InVariable_and_Month(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setTXN_AMOUNT("399")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setORDER_ID(order_id)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_MAX_AMOUNT("399")
                .setSubscriptionPurpose("LoanPayments")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().waitUntilVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        cashierPage.toBePaidTab().assertVisible();

    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= FIX and FrequencyUnit =MONTH")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InFix_and_Month(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setTXN_AMOUNT("0")
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setORDER_ID(order_id)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_MAX_AMOUNT("399")
                .setSubscriptionPurpose("LoanPayments")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().waitUntilVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        cashierPage.toBePaidTab().assertVisible();
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= FIX and FrequencyUnit =ONDEMAND")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InFix_and_Ondemand(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setTXN_AMOUNT("20")
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setORDER_ID(order_id)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_MAX_AMOUNT("399")
                .setSubscriptionPurpose("LoanPayments")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().waitUntilVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        cashierPage.toBePaidTab().assertVisible();
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify if Renewal Amount is named as Recurring Amount on Checkout JS and ribbon text for monthType= VARIABLE and FrequencyUnit =ONDEMAND")
    public void toVerifyRecurringAmountAndRibbonTextLabel_InVariable_and_Ondemand(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String order_id = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setTXN_AMOUNT("10.50")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setORDER_ID(order_id)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_MAX_AMOUNT("399")
                .setSubscriptionPurpose("LoanPayments")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subsLabelDueInfo().assertVisible();
        cashierPage.subscriptionDetails().assertClickable();
        cashierPage.subscriptionDetails().click();
        cashierPage.clickPgOverlay();
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).isEqualTo("Recurring Bill Amount*");
        cashierPage.toBePaidTab().assertVisible();
    }

    @Owner("AJEESH")
    @Feature("PGP-33689")
    @Parameters({"theme"})
    @Test(description = "Perform End to End transaction with Channel Code received via IFSC Code")
    public void TC004_Verify_ChannelCode_is_retrieved_bySending_IFSC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";
        String DBQuery = "SELECT * FROM PAYTMPGDB.IFSC_CODES ic WHERE IFSC_CODE ='HDFC0009386'";
        String ExpectedChannelCode = DbQueriesUtil.selectFromPGPDB(DBQuery, "BANK_CODE"); // Getting Value from DB

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.BANK_MANDATE, theme)
                .setSUBS_PAYMENT_MODE("")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setACCOUNT_TYPE("Savings")
                .setAccountNumber("915445500424")
                .setBANK_IFSC("HDFC0009386")
                .setUSER_NAME("Ajeesh Nair")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();

        Assert.assertTrue(cashierPage.IfscDetails().getAttribute("value").contains("HDFC0009386"));
        Assert.assertTrue(cashierPage.UserBankName().getAttribute("value").contains("Ajeesh Nair"));
        Assert.assertTrue(cashierPage.BankDetails().getAttribute("value").contains("915445500424"));


        Assert.assertFalse(cashierPage.IfscDetails().isEnabled());  // verify button is non-editable
        Assert.assertFalse(cashierPage.UserBankName().isEnabled()); // verify button is non-editable
        Assert.assertFalse(cashierPage.BankDetails().isEnabled());  // verify button is non-editable
        String ActualchannelCode= cashierPage.getPushAppData().getString("mandateAccountDetails.channelCode");

        Assert.assertEquals(ActualchannelCode,ExpectedChannelCode);

    }
    @Owner("AJEESH")
    @Feature("PGP-33732")
    @Parameters({"theme"})
    @Test(description = "Update logic for subscription related error cases")
    public void VerifyErrorMsgisshownforUPIotherthanPaytmOrBhim(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";
        String Expected_Err_Msg = "This UPI ID does not support subscription payments. Please try with another UPI ID.";
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_Pg2_MID2, theme)
                .setSUBS_PAYMENT_MODE("UPI")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();

        cashierPage.textBoxVPA().clearAndType("pgp33732mock@ybl");
        cashierPage.verifyVPALinkText().click();
        String Err_Msg =  cashierPage.invalidVpaText().getText();
        Assert.assertEquals(Err_Msg,Expected_Err_Msg);

        cashierPage.textBoxVPA().clear();
        cashierPage.closeCcDcDetailBtn().click();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateTxnDate(new Date())
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }
    @Owner("AJEESH")
    @Feature("PGP-33732")
    @Parameters({"theme"})
    @Test(description = "Update logic for subscription related error cases")
    public void VerifyErrorMsgisshownforUPInotSupportingSubscription(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";
        String Expected_Err_Msg = "Linked bank does not support subscription payments";
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("UPI")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();

        cashierPage.textBoxVPA().clearAndType("pgp33732mock@paytm");
        cashierPage.verifyVPALinkText().click();
        String Err_Msg =  cashierPage.invalidVpaText().getText();
        Assert.assertEquals(Err_Msg,Expected_Err_Msg);

        cashierPage.textBoxVPA().clear();
        cashierPage.closeCcDcDetailBtn().click();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateTxnDate(new Date())
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }
    @Owner("AJEESH")
    @Feature("PGP-33732")
    @Parameters({"theme"})
    @Test(description = "Update logic for subscription related error cases")
    public void VerifyErrorMsgisshownforInvalidUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";
        String Expected_Err_Msg = "Invalid VPA ID, Try Again";
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_Pg2_MID1, theme)
                .setSUBS_PAYMENT_MODE("UPI")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();

        cashierPage.textBoxVPA().clearAndType("invalidupi@paytm");
        cashierPage.verifyVPALinkText().click();
        String Err_Msg =  cashierPage.invalidVpaText().getText();
        Assert.assertEquals(Err_Msg,Expected_Err_Msg);

        cashierPage.textBoxVPA().clear();
        cashierPage.closeCcDcDetailBtn().click();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateTxnDate(new Date())
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }
    @Owner("AJEESH")
    @Feature("PGP-35618")
    @Parameters({"theme"})
    @Test(description = "Verify that User is Navigated to Dynamic URL and DB value is also updated")
    public void PGP_35618_VerifythatUserisNavigatedtoDynamicURL(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";
        String callBackURL = "www.google.com";
        OrderDTO orderDTO = new OrderFactory.BankMandate(MerchantType.BANK_MANDATE, theme, user)
                .setSUBS_PAYMENT_MODE("BANK_MANDATE")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setACCOUNT_TYPE("Savings")
                .setAccountNumber("915445500424")
                .setBANK_IFSC("HDFC0009386")
                .setUSER_NAME("Ajeesh Nair")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setCallBack_URL(callBackURL)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String subsId = cashierPage.getPushAppData().getString("subscriptionDetail.subsId");
        cashierPage.tabBankMandate().click();
        cashierPage.proceedBtn().click();
        cashierPage.buttonPGPayNow().click();

        Assertions.assertThat((DriverManager.getCurrentWebDriver().getCurrentUrl()).contains(callBackURL));
        String query = "SELECT callback_url  FROM PGPDB.bank_mandate_info bmi WHERE subscription_id ='" + subsId + "'";
        Assertions.assertThat((DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).toString()).contains(callBackURL));


    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-37658")
    @Parameters({"theme"})
    @Test(description = "Verify that User is Navigated to Dynamic URL for Bank Mandate Txn")
    public void PGP_37658_VerifythatUserisNavigatedtoDynamicURL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        String TxnMaxAmount = "200";
        String SubscriptionPurpose = "Loan Payments";
        String callBackURL = "www.google.com";
        OrderDTO orderDTO = new OrderFactory.BankMandate(MerchantType.BANK_MANDATE_BM, theme, user)
                .setSUBS_PAYMENT_MODE("BANK_MANDATE")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setACCOUNT_TYPE("Savings")
                .setAccountNumber("915445500424")
                .setBANK_IFSC("PYTM0000001")
                .setUSER_NAME("Akshat Sharma")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setCallBack_URL(callBackURL)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
       /* cashierPage.tabBankMandate().click();
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.proceedBtn().click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.waitUntilLoads();*/

        Assertions.assertThat((DriverManager.getCurrentWebDriver().getCurrentUrl()).contains(callBackURL));

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-37658")
    @Parameters({"theme"})
    @Test(description = "Validate User is redirecting to the BM URL for Bank Mandate Txn")
    public void PGP_37658_ValidateUserRedirectingtoBMURL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        String SubscriptionPurpose = "Loan Payments";
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_BM;
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setPaymentMode("BANK_MANDATE")
                .setCHANNEL_ID("WEB")
                .setWEBSITE("retail")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
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
        cashierPage.buttonPGPayNow().click();
        cashierPage.waitUntilLoads();

        Assertions.assertThat((DriverManager.getCurrentWebDriver().getCurrentUrl()).contains("https://www.spacex.com/"));

    }

}


