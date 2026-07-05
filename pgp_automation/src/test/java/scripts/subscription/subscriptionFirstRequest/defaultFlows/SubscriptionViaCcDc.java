package scripts.subscription.subscriptionFirstRequest.defaultFlows;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Bank;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.*;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.savecardService.SaveCard;

import java.text.DecimalFormat;
import java.util.Date;

import static org.awaitility.Awaitility.await;

@Owner("Tarun")
public class SubscriptionViaCcDc extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String payMode;

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

    private void verifyS2SSuccessResponse(OrderDTO orderDTO, String expectedRespMsg, Response response) {
        JsonPath jsonPath = response.jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat((String) jsonPath.get("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat((String) jsonPath.get("TXNAMOUNT")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        softly.assertThat(jsonPath.getString("RESPMSG")).isEqualToIgnoringCase(expectedRespMsg);
        softly.assertThat((String) jsonPath.get("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat((String) jsonPath.get("STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertAll();
    }

    @Parameters({"payMode"})
    @BeforeClass
    public void setPayMode(@Optional("CC") String payMode) {
        this.payMode = payMode.toUpperCase();
    }

    @Parameters({"theme"})
    @Test(description = "Validate the successful payment with txn amount > 0 and w/o saved card.", groups = {"smoke", "Sanity", "Regression"})
    public void PGP_24_ValidateSuccessfulPaymentWithoutSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user); // login flow no more applicable for subscription
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
       Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate the successful payment with txn amount > 0 and with saved card.")
    public void PGP_25_ValidateSuccessfulPaymentWithSavedCard(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Validate that the Payment modes are displayed correctly on Cashier Page as per the merchant config when user doesn't have saved card")
    public void PGP_23_validatePayModesWithoutSavedCard(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.tabCreditCard().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate that the Payment modes are displayed correctly on Cashier Page as per the merchant config when user have saved card")
    public void PGP_4_validatePayModesWithSavedCard(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       // cashierPage.login(user);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabSavedCard().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription details on cashier page when user has saved card")
    public void PGP_2_validateSubsDetailsOnCashierPageWhenSavedCardPresent(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
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
    public void PGP_22_validateSubsDetailsOnCashierPageWhenSavedCardNotPresent(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.txnDetailsContainer().assertContainsText("Payment Schedule");
        cashierPage.txnDetailsContainer().assertContainsText("Payment Interval");
        cashierPage.txnDetailsContainer().assertContainsText(orderDTO.getSUBS_FREQUENCY() + " " + orderDTO.getSUBS_FREQUENCY_UNIT());
        cashierPage.txnDetailsContainer().assertContainsText("Recurring Amount");
        cashierPage.txnDetailsContainer().assertContainsText("Amount Type");
        cashierPage.txnDetailsContainer().assertContainsText(": " + orderDTO.getSUBS_AMOUNT_TYPE());
        cashierPage.txnDetailsContainer().assertContainsText("Maximum Amount");
    }


//    @Parameters({"theme"})
//    @Test(description = "Validate cashier page when user login with incorrect credentials", enabled = false)
    public void PGP_3_validateCashierPageWhenLoginWithIncorrectCredentials(@Optional("merchant") String theme) throws Exception {
        throw  new RuntimeException("login flow is depricated for subscription");
        /*OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        User user = userManager.getForRead(Label.BASIC);
        cashierPage.login(user.mobNo(), "abcd");
        String errMsg = cashierPage.loginContainer().getText();
        Assertions.assertThat(errMsg).as("Invalid login credentials error")
                .containsIgnoringCase(ERROR.INVALID_LOGIN_CREDENTIALS);*/
    }

    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount > 0")
    public void PGP_5_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountGtZero(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SavedCardHelpers.deleteSavedCard(user);
        //cashierPage.login(user);

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
            cashierPage.notificationContainer().waitUntilVisible();
            String errMsg = cashierPage.notificationContainer().getText();
            Assertions.assertThat(errMsg).as("Invalid card number")
                    .containsIgnoringCase(Constants.MessageAssert.INVALID_PAYMENT_DETAILS.toString());
        }
        InvalidCVV:
        {
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().waitUntilEditable();
            String visaCardNumber = "4718650100010336";
            cashierPage.textBoxCardNumber().clearAndType(visaCardNumber);
            int visaAllowedCVVLength = 3;
            String textWithLengthMoreThanAllowedInVisaCVV = "12345";
            cashierPage.textBoxCVVNumber().clearAndType(textWithLengthMoreThanAllowedInVisaCVV);
            cashierPage.textBoxCVVNumber().waitUntilContainsAttributeValue("maxlength", String.valueOf(visaAllowedCVVLength));
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "3");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount = 0")
    public void PGP_7_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountEqualsZero(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("0")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        SavedCardHelpers.deleteSavedCard(user);

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
            //not passing exp month, exp year, and cvv no. as null because invalid card no. is considered as maestro in which case all afore-mentioned inputs gets disabled
            PaymentDTO paymentDetails = new PaymentDTO()
                    .setCreditCardNumber("111111111111")
                    .setExpMonth(null)
                    .setExpYear(null)
                    .setCvvNumber(null);
            cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
            String errMsg = cashierPage.paymentContainer().getText();
            Assertions.assertThat(errMsg).as("Invalid card number")
                    .containsIgnoringCase(Constants.MessageAssert.INVALID_CARD_NUMBER.toString());
        }
        InvalidCVV:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCvvNumber("1111");
            cashierPage.textBoxCardNumber().clearAndType(paymentDetails.getCreditCardNumber());
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "3");
        }
    }

    @Flaky
    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount = 0, w/o saved card")
    public void PGP_8_ValidateSuccessfulZeroAmtPaymentWithoutSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setTXN_AMOUNT("0")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.valueOf(payMode));
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        //Refund may sometimes take time to be performed so adding wait condition
        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.TWO_MINUTES).untilAsserted(() -> Assertions.assertThat(txnStatus.execute().as(TxnStatusResponse.class).getREFUNDAMT()).as("Refund Amount is not as expected").isIn("1.0", "1.00"));
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("1.0")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("1.0")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount = 0, with saved card")
    public void PGP_26_ValidateSuccessfulZeroAmtPaymentWithSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("0").build();
        PaymentDTO paymentDTO = new PaymentDTO().setCvvNumber("");

        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate retry payment amount should remain same in case of retry.")
    public void PGP_70_validateNoChangeInTxnAmtInCaseOfRetry(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SUBSCRIPTION_PGONLY_RETRY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDetailsForRetry = new PaymentDTO()
                .setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        Assertions.assertThat(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmount().getText()))
                .isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());

    }

    @Parameters({"theme"})
    @Test(description = "Validate Transaction should fail when txn amount > max amount")
    public void PGP_80_validateTxnAmtGreaterThanSubsMaxAmt(@Optional("merchant") String theme) {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setTXN_AMOUNT("101.0").build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId("")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("308")
                .validateRespMsg("Invalid amount.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT());
    }

    @Parameters("theme")
    @Test(description = "Validate retrying more than merchant limit should fail the transaction.")
    public void PGP_72_validateTxnFailureForMoreThanMerchantRetryLimit(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SUBSCRIPTION_PGONLY_RETRY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDetailsForRetry = new PaymentDTO()
                .setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDetailsForRetry);
        new ResponsePage().waitUntilLoads();
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

    @Parameters({"theme", "payMode"})
    @Test(description = "Validate the priority of cards on transacting with different CC/DC cards.")
    public void PGP_74_ValidateSavedCardsDisplayedInCorrectSequence(@Optional("merchant4") String theme, @Optional("CC") String payMode) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "05", "2022", "4532119585746198");
        Thread.sleep(2000);
        SavedCardHelpers.addCard(user, "05", "2022", "4556635133953276");
        Thread.sleep(2000);
        SavedCardHelpers.addCard(user, "05", "2022", "4718650100010336");
        OrderDTO orderDTO1 = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        checkoutPage.createOrder(orderDTO1);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.tabSavedCard().click();
        cashierPage.savedCardBlock(3).assertAttribute("value", "4532 XXXX XXXX 6198");
        cashierPage.savedCardBlock(2).assertAttribute("value", "4556 XXXX XXXX 3276");
        cashierPage.savedCardBlock(1).assertAttribute("value", "4718 XXXX XXXX 0336");
    }

    @Parameters("theme")
    @Test(description = "Validate successful transaction with SSOToken login.")
    public void PGP_75_ValidateSuccessfulCCSubscriptionWithSSOTokenLogin(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
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
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Test(description = "Validate Txn with 'SAVECARD_ID'= BLANK and Call type =S2S.")
    public void PGP_60_ValidateS2StxnWithSavedCardBlank() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, "", user).build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "InvalidSavedCardID", response);
    }

    @Test(description = "Validate with 'SAVECARD_ID'= valid saved card and Call type =S2S and amount=0.")
    public void PGP_62_ValidateS2StxnWithValidSavedCardAndTxnAmntZero() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
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

    @Test(description = "Validate with 'SAVECARD_ID'= valid saved card and Call type =S2S and amount>0.")
    public void PGP_84_ValidateS2STxnWithValidSavedCardAndTxnAmtGreaterThanZero() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setTXN_AMOUNT("1.00").build();
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

    @Test(description = "Validate with 'SAVECARD_ID'= valid saved card of another user and Call type =S2S.")
    public void PGP_63_ValidateS2StxnWithValidSavedCardIDOfAnotherUser() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        User user1 = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.deleteSavedCard(user1);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user1, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user1, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user).build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Saved Card ID", response);
    }

    @Issue("PGP-18755")
    @Test(description = "Validate with call type S2S and SsoToken Invalid",groups = Group.Status.BUG)
    public void PGP_85_validateS2StxnWithInvalidSSOToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setSSO_TOKEN("17057c26-efc4-4b77-a39c-e131d800f54d").build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Token", response);
    }

    @Test(description = "Validate with call type S2S and SSOToken Blank.")
    public void PGP_86_validateS2StxnWithBlankSSOToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setSSO_TOKEN("").build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "InvalidToken", response);
    }

    @Test(description = "Validate response if the subscription is using CC but the saved card ID of DC Card.")
    public void PGP_67_ValidateTxnWithDCSavedCardWhenPayModeIsCC() throws Exception {
        String randDCNo = "4386243463604084";
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(randDCNo);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setSUBS_PAYMENT_MODE("CC")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Saved Card ID", response);
    }

    @Test(description = "Validate response if the subscription is using DC but the saved card ID of CC Card.")
    public void PGP_100_alidateTxnWithCCSavedCardWhenPayModeIsDC() throws Exception {
        String randCCNo = "4718650100010336";
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(randCCNo);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setSUBS_PAYMENT_MODE("DC")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Saved Card ID", response);
    }

    @Test(description = "Validate failed subs request for expired saved card")
    public void Validate_ExpiredCardNotVisible_ForSubs() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCard saveCard = new SaveCard();
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String cardId = savedCardHelpers.saveCardUserId(user.custId(), saveCard.AesEncCardNumCredit, saveCard.AesEncExp, new PaymentDTO().getCreditCardNumber()).getResponse().toString();
        SavedCardHelpers.validateSaveCardDB_ByCardID(cardId);
        //change save card expiry date
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId, user);
        //String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, cardId, user)
                .setSUBS_PAYMENT_MODE("CC")
                .build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        verifyS2SFailureResponse(orderDTO, "Invalid Saved Card ID", response);
    }


}
