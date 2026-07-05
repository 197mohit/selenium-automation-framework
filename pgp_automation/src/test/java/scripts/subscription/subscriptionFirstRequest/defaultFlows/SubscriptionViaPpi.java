package scripts.subscription.subscriptionFirstRequest.defaultFlows;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group.Status;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.savecardService.SaveCard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.AKSHAT;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner("Tarun")
public class SubscriptionViaPpi extends PGPBaseTest {
    private static final double AMOUNT_TO_DEDUCT_FROM_BANK = 1.00;
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String payMode;

    private ResponsePage responsePage;

    @Parameters({"payMode"})
    @BeforeClass
    public void setPayMode(@Optional("CC") String payMode) {
        this.payMode = payMode.toUpperCase();
    }

    private List<String> getListOfPayModesOnCashierPage(CashierPage cashierPage) {
        List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
        List<String> paymethodList = new ArrayList<>();
        for (int i = 0; i < PaymodesOnPage.size(); i++) {
            paymethodList.add(PaymodesOnPage.get(i).getText().split("\n")[0]);
        }
        return paymethodList;
    }

    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount > 0, w/o saved card")
    public void PGP_36_ValidateSuccessfulPaymentWithoutSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.PG2_SUBS_NATIVE, theme).build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
    public void PGP_37_ValidateSuccessfulPaymentWithSavedCard(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
    public void PGP_27_validateSubsDetailsOnCashierPageWhenSavedCardPresent(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.txnDetailsContainer().assertContainsText("Payment Schedule");
        cashierPage.txnDetailsContainer().assertContainsText("Payment Interval");
        cashierPage.txnDetailsContainer().assertContainsText(orderDTO.getSUBS_FREQUENCY() + " " + orderDTO.getSUBS_FREQUENCY_UNIT());
        cashierPage.txnDetailsContainer().assertContainsText("Recurring Amount");
        cashierPage.txnDetailsContainer().assertContainsText("Amount Type");
        cashierPage.txnDetailsContainer().assertContainsText(": " + orderDTO.getSUBS_AMOUNT_TYPE());
        cashierPage.txnDetailsContainer().assertContainsText("Maximum Amount");
    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription details on cashier page when user doesn't has saved card")
    public void PGP_28_validateSubsDetailsOnCashierPageWhenSavedCardNotPresent(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.txnDetailsContainer().assertContainsText("Payment Schedule");
        cashierPage.txnDetailsContainer().assertContainsText("Payment Interval");
        cashierPage.txnDetailsContainer().assertContainsText(orderDTO.getSUBS_FREQUENCY() + " " + orderDTO.getSUBS_FREQUENCY_UNIT());
        cashierPage.txnDetailsContainer().assertContainsText("Recurring Amount");
        cashierPage.txnDetailsContainer().assertContainsText("Amount Type");
        cashierPage.txnDetailsContainer().assertContainsText(": " + orderDTO.getSUBS_AMOUNT_TYPE());
        cashierPage.txnDetailsContainer().assertContainsText("Maximum Amount");
    }

    @Parameters({"theme"})
    @Test(description = "Validate Login error should be displayed for incorrect credentials.")
    public void PGP_32_loginErrorWithIncorrectCredentials(@Optional("merchant") String theme) {
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login("9998765432", "abcd");
        String errMsg = cashierPage.loginContainer().getText();
        Assertions.assertThat(errMsg).as("Invalid login credentials error")
                .containsIgnoringCase(Constants.MessageAssert.INVALID_LOGIN_CREDENTIALS.toString());
    }

    @Issue("PGP-14992")
    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount > 0", groups = Status.BUG)
    public void PGP_33_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountGtZero(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
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
            Assertions.assertThat(errMsg).as("Blank card details")
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_CREDIT_CARD_NUMBER.toString())
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_EXPIRY_DATE.toString())
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_CVV_NUMBER.toString());
        }
        InvalidExpiryDate:
        {
            DecimalFormat formatter = new DecimalFormat("00");
            String currentMonth = formatter.format(CommonUtils.getCurrentMonth());
            if (currentMonth.equals("00")) {
                Reporter.log("<br>Skipping the InvalidExpiry check because of January Month.");
            } else {
                PaymentDTO paymentDetails = new PaymentDTO().setExpMonth(currentMonth).setExpYear(String.valueOf(CommonUtils.getCurrentYear()));
                cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
                String errMsg = cashierPage.paymentContainer().getText();
                Assertions.assertThat(errMsg).as("Invalid expiry date")
                        .containsIgnoringCase(Constants.MessageAssert.INVALID_EXPIRY.toString());
            }
        }
        InvalidCardNumber:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCreditCardNumber("3045751111111117");
            cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
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
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "3");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount = 0")
    public void PGP_34_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountEqualsZero(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
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
            Assertions.assertThat(errMsg).as("Blank card details")
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_CREDIT_CARD_NUMBER.toString())
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_EXPIRY_DATE.toString())
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_CVV_NUMBER.toString());
        }
        InvalidExpiryDate:
        {
            DecimalFormat formatter = new DecimalFormat("00");
            String currentMonth = formatter.format(CommonUtils.getCurrentMonth());
            if (currentMonth.equals("00")) {
                Reporter.log("<br>Skipping the InvalidExpiry check because of January Month.");
            } else {
                PaymentDTO paymentDetails = new PaymentDTO().setExpMonth(currentMonth).setExpYear(String.valueOf(CommonUtils.getCurrentYear()));
                cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
                String errMsg = cashierPage.paymentContainer().getText();
                Assertions.assertThat(errMsg).as("Invalid expiry date")
                        .containsIgnoringCase(Constants.MessageAssert.INVALID_EXPIRY.toString());
            }
        }
        InvalidCardNumber:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCreditCardNumber("3045751111111117");
            cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
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
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "3");
        }
    }

//    @Parameters({"theme"})
//    @Test(description = "Successful payment, txn amount = 0, w/o saved card", enabled = false)
//TODO need to fix the test case
    public void PGP_30_ValidateSuccessfulZeroAmtPaymentWithoutSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount = 0, with saved card")
    public void PGP_31_ValidateSuccessfulZeroAmtPaymentWithSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setTXN_AMOUNT("0")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.tabSavedCard().click();
        cashierPage.buttonPGPayNow().click();
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateSubsId(ValidationType.NON_EMPTY)
                .validateTxnId(ValidationType.NON_EMPTY);

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(PGPHelpers.getSavedCardId(subsId),user );
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate card getting saved for successful subs_PPI txn.")
    public void PGP_35_validateCardGettingSaveForSuccessfulTxn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
    public void PGP_76_validateSuccessfulPPISubsWhenWalletBalLessThanTxnAmt(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.CC);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
    public void PGP_77_validateSuccessfulPPISubsWhenWalletBalGreaterThanTxnAmt(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
    public void PGP_78_validateSuccessfulPPISubsWhenWalletBalEqualsToTxnAmt(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
    public void PGP_79_validateFailedTxnWithTxnAmntGreaterThanWalletByFraction(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
    @Test(description = "Validate retry payment amount should remain same in case of retry.")
    public void PGP_81_validateNoChangeInTxnAmtInCaseOfRetry(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI_RETRY, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        PaymentDTO paymentDetailsForRetry = new PaymentDTO()
                .setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        Assertions.assertThat(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmount().getText()))
                .isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());

    }

    @Parameters({"theme"})
    @Test(description = "Validate retrying more than merchant limit should fail the transaction.")
    public void PGP_82_validateTxnFailureForMoreThanMerchantRetryLimit(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI_RETRY, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDetailsForRetry = new PaymentDTO()
                .setCreditCardNumber("4718650100030136");
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("141")
                .validateRespMsg("Cancel Request by Customer(After page load)")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NOT_PRESENT)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful transaction with SSOToken login.")
    public void PGP_83_ValidateSuccessfulPPISubscriptionWithSSOTokenLogin(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, "", user)
                .setSUBS_PAYMENT_MODE("PPI").build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "InvalidSavedCardID", response);
    }

    @Parameters({"theme"})
    @Test(description = "Validate with 'SAVECARD_ID'= valid saved card and Call type =S2S and amount=0.")
    public void PGP_88_ValidateS2StxnWithValidSavedCardAndTxnAmntZero() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user)
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
        User user1 = userManager.getForWrite(Label.BASIC);
        User user2 = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();

        SavedCardHelpers.deleteSavedCard(user1);
        SavedCardHelpers.addCard(user1, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user1, 0);

        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user2)
                .setSUBS_PAYMENT_MODE("PPI")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Saved Card ID", response);
    }

    @Issue("PGP-18755")
    @Parameters({"theme"})
    @Test(description = "Validate with call type S2S and SsoToken Invalid",groups = Status.BUG)
    public void PGP_91_validateS2StxnWithInvalidSSOToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.SUBSCRIPTION_PPI, savedCardId, user)
                .setSUBS_PAYMENT_MODE("PPI")
                .setSSO_TOKEN("")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "InvalidToken", response);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Transaction should fail when txn amount > max amount")
    public void PGP_101_validateTxnAmtGreaterThanSubsMaxAmt(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setTXN_AMOUNT("101.0")
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("308")
                .validateRespMsg("Invalid Txn Amount")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .assertAll();
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
    public void ValidateFailedZeroAmtPaymentWith_expiredSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SaveCard saveCard = new SaveCard();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setTXN_AMOUNT("0")
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        //save credit card
        String cardId = savedCardHelpers.saveCardUserId(user.custId(), saveCard.AesEncCardNumCredit, saveCard.AesEncExp, new PaymentDTO().getCreditCardNumber()).getResponse().toString();
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId,user );


        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_userId(user);
        Assertions.assertThat(responseBase.getResponseInSaveCardResponseList().size()).isEqualTo(0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.tabSavedCard().assertNotVisible();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-27607")
    @Parameters({"theme"})
    @Test(description = "Verify isAddNPay flag when create subscription transaction is done through UPI and payment flow is ADDANDPAY in enhanced flow")
    public void VerifyisAddNPayFlagForCreateSubsUsingAddnPayThroughUPIEnhancedFlow(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - AMOUNT_TO_DEDUCT_FROM_BANK);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .assertAll();
        String tsnId = responsePage.textTxnID().getText();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
        String grepcmd = "grep \"" + tsnId + "\" /paytm/logs/instaproxy.log | " +
                "grep \"extendInfo\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        String [] ar = theiaFacadeLogs.split("passThroughExtendInfo");
        String [] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("\"isAddNPay\":\"true\"");
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-29588")
    @Parameters({"theme"})
    @Test(description = "Verify Successfull PPI Subscription Txn when SubscriptionFrequencyUnit is FORTNIGHT")
    public void verifySuccessfulCreateSubscriptionforFreqFortnight(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.PG2_SUBS_NATIVE, theme)
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("FORTNIGHT")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Owner(AKSHAT)
    @Test(description = "Verify that subs txn. fails when pyamode is blank & PPI_Only 'N' ")
    public void TC_001_PPIN_txnFailed_forBlank_Paymode(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY;
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchant, theme)
                .setSUBS_PAYMENT_MODE("")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(orderDTO.getMID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCurrency("INR")
                .validateStatus("TXN_FAILURE")
                .validateRespCode("2022")
                .validateRespMsg("Invalid Subs payment mode")
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Owner(AKSHAT)
    @Test(description = "Verify that Wallet is displayed on cashier page for PPI-N txn' ")
    public void TC_002_Wallet_Displayed_for_PPIN(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY;
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchant, theme).build();

        Double walletAmount = 5.0;
        WalletHelpers.modifyBalance(user, walletAmount);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assertions.assertThat(paymethodList.contains("Paytm Balance"));
        Assertions.assertThat(paymethodList).doesNotContain("CREDIT CARD");
        Assertions.assertThat(paymethodList).doesNotContain("DEBIT CARD");
//        Assert.assertTrue(paymethodList.contains("Paytm Balance"));
//        Assert.assertFalse(paymethodList.contains("CREDIT CARD"));
//        Assert.assertFalse(paymethodList.contains("DEBIT CARD"));

    }
}
