package scripts.subscription.subscriptionFirstRequest.enhancedFlows;


import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Bank;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.apphelpers.*;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.MandateAccountDetails;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.utils.merchant.util.PayMethodType;
import org.testng.Assert;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peon;
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
import java.time.Month;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static org.awaitility.Awaitility.await;



@Owner("Tarun")
public class SubscriptionViaCcDc extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String payMode;
    private ResponsePage responsePage;
    private final static String cardNotSupportedMsg = "This card is not supported for subscription payments";

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

    @Step("Validate fetchPaymentOption paymode status")
    private FetchPaymentOptResponseDTO execute_validateFetchPaymentOption(String txnToken, String mid, String orderId, String payMethod, boolean isDisabledStatus) {
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnToken, mid, orderId);
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, payMethod, isDisabledStatus))
                .as(payMethod + " paymethod is disabled or not found")
                .isTrue();
        return fetchPaymentOptResponseDTO;
    }
    @Step("Validate Success Initiate Subscription Response")
    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultCode mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;
    }

    @Parameters({"payMode"})
    @BeforeClass
    public void setPayMode(@Optional("CC") String payMode) {
        this.payMode = payMode.toUpperCase();
    }

    @Parameters({"theme"})
    @Test(description = "Validate the successful payment with txn amount > 0 and w/o saved card.", groups = {"smoke", "Sanity", "Regression"})
    public void PGP_24_ValidateSuccessfulPaymentWithoutSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        MerchantType merchantType = MerchantType.Subscription_PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme, user).build();
        SavedCardHelpers.deleteSavedCard(user);
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
    public void PGP_25_ValidateSuccessfulPaymentWithSavedCard(@Optional("enhancedweb") String theme) throws Exception {
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
    public void PGP_23_validatePayModesWithoutSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate that the Payment modes are displayed correctly on Cashier Page as per the merchant config when user have saved card")
    public void PGP_4_validatePayModesWithSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabSavedCard().assertVisible();
    }

    //Disabled HDFC testcases as it requires change in property and that will impact existing testcases
//    @Parameters("theme")
//    @Test(description = "Verify that HDFC debit cards are not supported for subscription", enabled = false)
    public void ValidateHDFC_DC_CardNotSupported(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("DC")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.DEBIT_CARD_NUMBER);

        Assertions.assertThat(cashierPage.paymentContainer().getText())
                .containsIgnoringCase(cardNotSupportedMsg);
    }

    @Parameters("theme")
    @Test(description = "Verify that PNB debit cards are not supported for subscription")
    public void ValidatePNB_DC_CardNotSupported(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("DC")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.PNB_DEBIT_CARD); //Check for PNB DC Card

        Assertions.assertThat(cashierPage.paymentContainer().getText())
                .containsIgnoringCase("This card is not supported for subscription payments");
    }

    //Disabled HDFC testcases as it requires change in property and that will impact existing testcases
//    @Parameters("theme")
//    @Test(description = "Verify that HDFC debit cards are not supported for subscription ADD N PAY", enabled = false)
    public void ValidateHDFC_DC_CardNotSupportedForAddNPay(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_ADDNPAY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.DEBIT_CARD_NUMBER); //Check for PNB DC Card

        Assertions.assertThat(cashierPage.paymentContainer().getText())
                .containsIgnoringCase(cardNotSupportedMsg);
    }

    @Parameters({"theme"})
    @Test(description = "Verify that PNB debit cards are not supported for subscription ADD N PAY")
    public void ValidatePNB_DC_CardNotSupportedForAddNPay(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_ADDNPAY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.PNB_DEBIT_CARD); //Check for PNB DC Card

        Assertions.assertThat(cashierPage.paymentContainer().getText())
                .containsIgnoringCase(cardNotSupportedMsg);
    }

    //Disabled HDFC testcases as it requires change in property and that will impact existing testcases
//    @Parameters("theme")
//    @Test(description = "Verify that Saved HDFC debit cards are not supported for subscription", enabled = false)
    public void ValidateSavedHDFC_DC_CardNotSupported(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("DC")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Assertions.assertThat(cashierPage.paymentContainer().getText())
                .containsIgnoringCase(cardNotSupportedMsg);
    }

    @Parameters("theme")
    @Test(description = "Verify that Saved PNB debit cards are not supported for subscription and " +
            "it will not be visible on cashier page")
    public void ValidateSavedPNB_DC_CardNotSupported(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.PGOnly, theme)
                .setSUBS_PAYMENT_MODE("DC")
                .setSSO_TOKEN(user.ssoToken())
                .build();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PNB_DEBIT_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().assertNotVisible();
    }

//    @Parameters({"theme"})
//    @Test(description = "Validate subscription details on cashier page when user has saved card", enabled = false)
    public void PGP_2_validateSubsDetailsOnCashierPageWhenSavedCardPresent(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme, user).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.subscriptionDetails().getText()).as("Subscription UI message change").isEqualTo("View Subscription Details");
        cashierPage.subscriptionDetails().click();

    }


    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount > 0")
    public void PGP_5_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountGtZero(@Optional("enhancedwap") String theme) throws Exception {
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
            int visaAllowedCVVLength = 4;
            String textWithLengthMoreThanAllowedInVisaCVV = "12345";
            cashierPage.textBoxCVVNumber().clearAndType(textWithLengthMoreThanAllowedInVisaCVV);
            cashierPage.textBoxCVVNumber().waitUntilContainsAttributeValue("maxlength", String.valueOf(visaAllowedCVVLength));
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "4");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate error message w.r.t card details, txn amount = 0")
    public void PGP_7_validateErrMsgsOnInvalidCardDetailsWhenTxnAmountEqualsZero(@Optional("enhancedwap") String theme) throws Exception {
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
            Assertions.assertThat(errMsg).as("Invalid error message")
                    .containsIgnoringCase(Constants.MessageAssert.EMPTY_CREDIT_CARD_NUMBER.toString());
        }

        InvalidCardNumber:
        {
            //not passing exp month, exp year, and cvv no. as null because invalid card no. is considered as maestro in which case all afore-mentioned inputs gets disabled

            cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.getAmexCardNumber());
            String errMsg = cashierPage.paymentContainer().getText();
            Assertions.assertThat(errMsg).as("Invalid card number")
                    .containsIgnoringCase(Constants.MessageAssert.INVALID_PAYMENT_DETAILS.toString());
        }
        InvalidCVV:
        {
            PaymentDTO paymentDetails = new PaymentDTO().setCvvNumber("1111");
            cashierPage.textBoxCardNumber().clearAndType(paymentDetails.getCreditCardNumber());
            cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "4");
        }

        InvalidExpiryDate:
        {
            DecimalFormat formatter = new DecimalFormat("00");
            String currentMonth = formatter.format(CommonUtils.getCurrentMonth());
            if (currentMonth.equals("00")) {
                Reporter.log("<br>Skipping the InvalidExpiry check because of January Month.");
            } else {
                PaymentDTO paymentDetails = new PaymentDTO().setExpMonth(currentMonth).setExpYear("2019");

                cashierPage.payBy(PayMode.valueOf(payMode), paymentDetails);
                String errMsg = cashierPage.paymentContainer().getText();
                Assertions.assertThat(errMsg).as("Invalid expiry date")
                        .containsIgnoringCase(Constants.MessageAssert.INVALID_EXPIRY.toString());
            }
        }
    }

    @Parameters({"theme"})
    @Test(description = "Successful payment, txn amount = 0, w/o saved card")
    public void PGP_8_ValidateSuccessfulZeroAmtPaymentWithoutSavedCard(@Optional("enhancedweb") String theme) throws Exception {
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
    public void PGP_26_ValidateSuccessfulZeroAmtPaymentWithSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("0").build();
        PaymentDTO paymentDTO = new PaymentDTO();

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
    @Test(description = "Validate Transaction should pass when txn amount > max amount")
    public void PGP_80_validateTxnAmtGreaterThanSubsMaxAmt(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setTXN_AMOUNT("101.0").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);

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

    @Parameters({"theme", "payMode"})
    @Test(description = "Validate the priority of cards on transacting with different CC/DC cards.")
    public void PGP_74_ValidateSavedCardsDisplayedInCorrectSequence(@Optional("enhancedwap") String theme, @Optional("CC") String payMode) throws Exception {
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

        if (theme.equalsIgnoreCase("enhancedweb")) {
            Assertions.assertThat(cashierPage.saveCardPosition(1)).startsWith("4718").endsWith("0336");
            Assertions.assertThat(cashierPage.saveCardPosition(2)).startsWith("4556").endsWith("3276");
            Assertions.assertThat(cashierPage.saveCardPosition(3)).startsWith("4532").endsWith("6198");
        } else {
            Assertions.assertThat(cashierPage.saveCardPosition(1)).endsWith("0336");
            Assertions.assertThat(cashierPage.saveCardPosition(2)).endsWith("3276");
            Assertions.assertThat(cashierPage.saveCardPosition(3)).endsWith("6198");
        }
    }

    @Parameters("theme")
    @Test(description = "Validate successful transaction with SSOToken login.")
    public void PGP_75_ValidateSuccessfulCCSubscriptionWithSSOTokenLogin(@Optional("enhancedweb") String theme) throws Exception {
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
    @Test(description = "Validate with call type S2S and SsoToken Invalid", groups = Group.Status.BUG)
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

    @Parameters("theme")
    @Test(description = "Validate successful transaction without SSOToken/login.")
    public void PGP_75_ValidateSuccessfulCCSubscriptionWithOutSSOTokenLogin(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .build();
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

    @Parameters("theme")
    @Test(description = "Validate payment should not be done with unsupported subscription Amex CC")
    public void PGP_76_ValidateSuccessfulCCSubscriptionWithOutSSOTokenLogin(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.getAmexCardNumber());

        Assertions.assertThat(cashierPage.paymentContainer().getText()).as("Amex Card invalid error message")
                .containsIgnoringCase("This card is not supported for subscription payments");
    }

    @Parameters("theme")
    @Test(description = "Validate payment should not be done with unsupported subscription SBI DC")
    public void PGP_77_ValidateSuccessfulDCSubscriptionWithOutSSOTokenLogin(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("DC")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.textBoxCardNumber().clearAndType("6069860037470608");

        Assertions.assertThat(cashierPage.paymentContainer().getText()).as("Amex Card invalid error message")
                .containsIgnoringCase("This card is not supported for subscription payments");
    }

    @Parameters("theme")
    @Test(description = "Validate payment done when subs_amount_type='FIX'")
    public void PGP_78_ValidatePaymentWhenFixedType(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_MAX_AMOUNT("")
                .build();
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

    //Retry flow not working
//    @Parameters("theme")
//    @Test(description = "Validate Retry Flow On CC ", enabled = false)
    public void validateRetryFlowOnCC(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SUBSCRIPTION_PGONLY_RETRY, theme)
                .setSUBS_RETRY_COUNT("1")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectPayment = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.valueOf(payMode), incorrectPayment);
        cashierPage.waitUntilLoads();
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

    @Parameters("theme")
    @Test(description = "Validate payment done with Daily frequency")
    public void validateDailyFrequencyFlow(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_GRACE_DAYS("0")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
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


    @Parameters("theme")
    @Test(description = "Validate txn should be done with any pay modes if all payment modes are visible")
    public void validateAllPaymentModes(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.DC);

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
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();

    }

    // Deepak S , cardIndexNo is not coming in peon Response, is there some kind of FF4J flag introduced, please confirm
    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test
    public void testCardIndexNoIsPresentInOutputWhenCardTokenRequiredIsTrueInOrder() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO subsOrder = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setTXN_AMOUNT("0")
                .setCardTokenRequired(true)
                .build();
        Map<String, String> subsOrderResp = PGPHelpers.executeProcessTransaction(subsOrder).jsonPath().getMap("", String.class, String.class);
        Assertions.assertThat(subsOrderResp).containsKeys("CARDINDEXNO");

        OrderDTO RenewSubsOrder = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsOrderResp.get("SUBS_ID"), "1.00")
                .build();
        PGPHelpers.executeProcessTransaction(RenewSubsOrder);

        assertion.apply(peonWait.apply(() -> peons.getAt(RenewSubsOrder.getORDER_ID()) != null));
        Peon peon = peons.getAt(RenewSubsOrder.getORDER_ID());
        assertion.apply(peon.keys().contains("cardIndexNo"));
    }

    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test
    public void testCardIndexNoIsPresentInOutputWhenCardTokenRequiredIsFalseInOrder() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO subsOrder = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setTXN_AMOUNT("0")
                .setCardTokenRequired(false)
                .build();
        Map<String, String> subsOrderResp = PGPHelpers.executeProcessTransaction(subsOrder).jsonPath().getMap("", String.class, String.class);
        Assertions.assertThat(subsOrderResp).containsKeys("CARDINDEXNO");

        OrderDTO RenewSubsOrder = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsOrderResp.get("SUBS_ID"), "1.00")
                .build();
        PGPHelpers.executeProcessTransaction(RenewSubsOrder);
    }

    // Deepak S , cardHash is not coming in peon Response, is there some kind of FF4J flag introduced, please confirm
    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test
    public void testCardHashIsPresentInOutput() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO subsOrder = new OrderFactory.SubscriptionS2S(MerchantType.Subscription_PGOnly, savedCardId, user)
                .setTXN_AMOUNT("0")
                .setCardTokenRequired(true)
                .build();
        Map<String, String> subsOrderResp = PGPHelpers.executeProcessTransaction(subsOrder).jsonPath().getMap("", String.class, String.class);
        Assertions.assertThat(subsOrderResp).containsKeys("CARDHASH");

        OrderDTO RenewSubsOrder = new OrderFactory.SubscriptionRenew(MerchantType.Subscription_PGOnly, subsOrderResp.get("SUBS_ID"), "1.00")
                .build();
        PGPHelpers.executeProcessTransaction(RenewSubsOrder);

        assertion.apply(peonWait.apply(() -> peons.getAt(RenewSubsOrder.getORDER_ID()) != null));
        Peon peon = peons.getAt(RenewSubsOrder.getORDER_ID());
        assertion.apply(peon.keys().contains("cardHash"));
    }

    @Test(description = "Verify Error Message When Subs Max Amount and Txn Amount both are zero and subscriptionAmountType is VARIABLE")
    public void verifyErrorMessageWhenMaxAmtandTxnAmtisZeroTypeVARIABLE() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, "enhancedweb")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_MAX_AMOUNT("0")
                .setTXN_AMOUNT("0").build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespMsg("SubMaxAmount cannot be zero set against the subscription")
                .validateRespCode("810")
                .assertAll();
    }

    @Test(description = "Verify Error Message When Subs Max Amount and Txn Amount both are zero and subscriptionAmountType is FIX")
    public void verifyErrorMessageWhenMaxAmtandTxnAmtisZeroTypeFIX() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, "enhancedweb")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_MAX_AMOUNT("0")
                .setTXN_AMOUNT("0").build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespMsg("SubMaxAmount cannot be zero set against the subscription")
                .validateRespCode("810")
                .assertAll();
    }


    @Owner("Eshani")
    @Parameters("theme")
    @Feature("PGP-27665")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Test(description = "Verify Enhanced DAY subscription txn is Successful with GRACE PERIOD < FREQ * FREQ_UNIT")
    public void verifyEnhancedDAYsubsWithLessGraceperiod(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf(freq -1);


        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, "enhancedweb")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setSUBS_FREQUENCY(frequency)
                .setSUBS_GRACE_DAYS(grace_days)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);

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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }


    @Owner("Eshani")
    @Feature("PGP-27665")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Test(description = "Verify Enhanced txn fails for WEEK subscription with GRACE PERIOD > FREQ * FREQ_UNIT")
    public void verifyEnhancedWEEKsubsWithMoreGraceperiod() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf((freq*7)+1);


        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, "enhancedweb")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("WEEK")
                .setSUBS_FREQUENCY(frequency)
                .setSUBS_GRACE_DAYS(grace_days)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespMsg("Grace days cannot be greater than the frequency set against the subscription")
                .validateRespCode("810")
                .assertAll();
    }


    @Owner("Eshani")
    @Parameters("theme")
    @Feature("PGP-27665")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Test(description = "Verify Enhanced MONTH subscription txn is Successful with GRACE PERIOD < FREQ * FREQ_UNIT")
    public void verifyEnhancedMONTHsubsWithLessGraceperiod(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf((freq*30) -1);


        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, "enhancedweb")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setSUBS_FREQUENCY(frequency)
                .setSUBS_GRACE_DAYS(grace_days)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);

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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }


    @Owner("Eshani")
    @Feature("PGP-27665")
    @Epic(Constants.Sprint.SPRINT36_3)
    @Test(description = "Verify Enhanced txn fails for YEAR subscription with GRACE PERIOD > FREQ * FREQ_UNIT")
    public void verifyEnhancedYEARsubsWithMoreGraceperiod() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Random ran= new Random();
        int freq= ran.nextInt(10) +1;
        String frequency= String.valueOf(freq);
        String grace_days= String.valueOf((freq*365)+1);


        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, "enhancedweb")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("YEAR")
                .setSUBS_FREQUENCY(frequency)
                .setSUBS_GRACE_DAYS(grace_days)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespMsg("Grace days cannot be greater than the frequency set against the subscription")
                .validateRespCode("810")
                .assertAll();
    }


    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "Verify that wallet is not selected by default on cashier page when Wallet amount > Transaction amount &Verify that 2nd paymode appearing on cashier page is selected for above transaction")
    public void ValidateWalletUnchecked_walletBalanceSufficient(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) + 1);
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet paymode should not be selected in subs txn").isFalse();

        Assertions.assertThat(cashierPage.payModeNextToPPILoggedIn().isEnabled()).as("Paymode next to PPI should be enabled").isTrue();

        cashierPage.checkBoxPPI().click();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet checkbox should be clickable").isTrue();

    }


    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "Verify that wallet is not selected by default on cashier page when Wallet amount < Transaction amount & Verify that 2nd paymode appearing on cashier page is selected for above transaction")
    public void ValidateWalletUnchecked_walletBalanceNotSufficient(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet paymode should not be selected").isFalse();

        Assertions.assertThat(cashierPage.payModeNextToPPILoggedIn().isEnabled()).as("Paymode next to PPI should be selected").isTrue();

    }


    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "11Verify that in ADDnPAY TXN, wallet is not selected by default on cashier page when Wallet amount < Transaction amount & Verify that 2nd paymode appearing on cashier page is selected for above transaction")
    public void ValidateADDnPAYWalletUnchecked_walletBalanceNotSufficient(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SUBSCRIPTION_ADDNPAY, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("3")
                .build();

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet paymode should not be selected").isFalse();

        Assertions.assertThat(cashierPage.payModeNextToPPILoggedIn().isEnabled()).as("Paymode next to PPI should be selected").isTrue();

        cashierPage.checkBoxPPI().click();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet checkbox should be clickable").isTrue();

    }

    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "Verify that in ADDnPAY, wallet is not selected by default on cashier page after user login & Transaction amount>wallet balanace & Verify that 2nd paymode appearing on cashier page is selected for above transaction")
    public void ValidateADDnPAYWalletUncheckedAfteruserLogin(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SUBSCRIPTION_ADDNPAY, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .build();

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet paymode should not be selected").isFalse();

        Assertions.assertThat(cashierPage.payModeNextToPPILoggedIn().isEnabled()).as("Paymode next to PPI should be selected").isTrue();

        cashierPage.checkBoxPPI().click();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet checkbox should be clickable").isTrue();

    }


    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "Verify that for normal txn, wallet is not selected by default on cashier page after user login & Transaction amount<wallet balance & Verify that 2nd paymode appearing on cashier page is selected for above transaction")
    public void ValidateWalletUncheckedAfteruserLogin(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .build();


        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) + 1.00);

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet paymode should not be selected").isFalse();

        Assertions.assertThat(cashierPage.payModeNextToPPILoggedIn().isEnabled()).as("Paymode next to PPI should be selected").isTrue();

        cashierPage.checkBoxPPI().click();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet checkbox should be clickable").isTrue();

    }


    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "Verify that in non logged & Verify that 2nd paymode appearing on cashier page is selected")
    public void ValidateIfNextPaymodeSelectedWhenNoLoginDone(@Optional("enhancedweb") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        Assertions.assertThat(cashierPage.payModeNextToPPILoggedOut().isEnabled()).as("Paymode next to PPI should be selected").isTrue();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "Subscription transaction when app is not installed but user is logged in (Balance)")
    public void PGP_29580_AutoAppInvoke_appinvokeallowed_TC_User_loggedIn_Wallet(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchantType = Constants.MerchantType.Subscription_PGOnly;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId()); theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setTXN_AMOUNT("1")
                    .build();
            checkoutPage.createOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPageURL.contains("showPaymentPage"));
            cashierPage.payBy(Constants.PayMode.WALLET);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount("1.00")
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "Subscription transaction when app is not installed but user is logged in (PPBL)")
    public void PGP_29580_AutoAppInvoke_appinvokeallowed_TC_User_loggedin_PPBL(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        Constants.MerchantType merchantType = MerchantType.Subscription_PGOnly;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId()); theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme, user).
                    setTXN_AMOUNT("100").build();
            WalletHelpers.modifyBalance(user, 0.0);
            checkoutPage.createOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("showPaymentPage"));
            cashierPage.checkBoxPPI().unCheck();
            cashierPage.pause(3);
            cashierPage.payBy(PayMode.PPBL);
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
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateGatewayName(Constants.Gateway.PPBL.toString())
                    .validateBankName(Constants.Bank.PPBL.toString())
                    .validateCheckSum(MerchantType.Subscription_PGOnly.getKey())
                    .validateResponsePageParameters()
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "Subscription transaction when app is not installed but user is logged in (Bank_MANDATE)")
    public void PGP_29580_AutoAppInvoke_appinvokeallowed_TC_User_loggedIn_Bank_Mandate(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.SUBS_BANK_MANDATE_MID;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId()); theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            String TxnMaxAmount = "10";
            String SubscriptionPurpose = "Loan Amount Payment";
            OrderDTO orderDTO = new OrderFactory.BankMandate(merchantType, theme, user)
                    .setCHANNEL_ID("WEB")
                    .setBANK_CODE("PPBL")
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
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("showPaymentPage"));
            cashierPage.payBy(PayMode.BANK_MANDATE);
            cashierPage.bankMandateSubscribe().click();
      //      cashierPage.bankMandateConfirmPay().click();
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateRespCode("3006")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "Subscription transaction when app is not installed but user is logged in (Balance)")
    public void PGP_29580_Appinvokeallowed_TC02_User_loggedIn_Wallet(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchantType = MerchantType.SUBS_PPBL_MID;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "N");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId());   theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setTXN_AMOUNT("1")
                    .build();
            checkoutPage.createOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("processTransaction"));
            cashierPage.payBy(Constants.PayMode.WALLET);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount("1.00")
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "Subscription transaction when app is not installed but user is logged in (PPBL)")
    public void PGP_29580_Appinvokeallowed_TC02_User_loggedIn_PPBL(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.PPBL);
        Constants.MerchantType merchantType = MerchantType.SUBS_PPBL_MID;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "N");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId());   theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme, user).
                    setTXN_AMOUNT("100").build();
            WalletHelpers.modifyBalance(user, 0.0);
            checkoutPage.createOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("processTransaction"));
            cashierPage.checkBoxPPI().unCheck();
            cashierPage.pause(3);
            cashierPage.payBy(PayMode.PPBL);
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
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateGatewayName(Constants.Gateway.PPBL.toString())
                    .validateBankName(Constants.Bank.PPBL.toString())
                    .validateCheckSum(MerchantType.SUBS_PPBL_MID.getKey())
                    .validateResponsePageParameters()
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "Subscription transaction when app is not installed but user is logged in (Bank_MANDATE)")
    public void PGP_29580_Appinvokeallowed_TC02_User_loggedIn_Bank_Mandate(@Optional("enhancedwap_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.SUBS_PPBL_MID;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "N");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId());  theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            String TxnMaxAmount = "10";
            String SubscriptionPurpose = "Loan Amount Payment";
            OrderDTO orderDTO = new OrderFactory.BankMandate(merchantType, theme, user)
                    .setCHANNEL_ID("WEB")
                    .setBANK_CODE("PPBL")
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
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("processTransaction"));
            cashierPage.payBy(PayMode.BANK_MANDATE);
            cashierPage.bankMandateSubscribe().click();
        //   cashierPage.bankMandateConfirmPay().click();
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateRespCode("3006")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "ASubscription transaction when app is not installed but user is logged in (Balance)")
    public void PGP_29580_AutoAppInvoke_Appinvokeallowed_TC03_User_loggedIn_Balance(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 100.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchantType = MerchantType.Subscription_PGOnly;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId()); theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                    .setTxnValue("10")
                    .setSubscriptionPaymentMode("")
                    .setSubsPPIOnly("Y")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setSubscriptionFrequency("2")
                    .setSubscriptionFrequencyUnit("WEEK")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("NATIVE_SUBSCRIPTION")
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchantType.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken)
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("showPaymentPage"));
            cashierPage.payBy(PayMode.WALLET);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount("10.00")
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "User logged in on cashier page and payment done using Balance . app not installed")
    public void PGP_29580_AutoAppInvoke_Appinvokeallowed_TC03_User_loggedIn_BalanceWallet_SSOSent(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 100.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchantType = Constants.MerchantType.Subscription_PGOnly;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId());  theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                    .setTxnValue("10")
                    .setSubscriptionPaymentMode("")
                    .setSubsPPIOnly("Y")
                    .setSsoToken(user.ssoToken())
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setSubscriptionFrequency("2")
                    .setSubscriptionFrequencyUnit("WEEK")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("NATIVE_SUBSCRIPTION")
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchantType.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken)
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("showPaymentPage"));
            cashierPage.payBy(PayMode.WALLET);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "USer already logged in using sso  app not installed payment done using PPBL")
    public void PGP_29580_AutoAppInvoke_Appinvokeallowed_TC03_User_loggedIn_PPBL(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchantType = MerchantType.Subscription_PGOnly;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId()); theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                    .setTxnValue("10")
                    .setSubscriptionPaymentMode("PPBL")
                    .setSubsPPIOnly("Y")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setSubscriptionFrequency("2")
                    .setSubscriptionFrequencyUnit("WEEK")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("")
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchantType.getId(), initTxnDTO.orderFromBody(), PayMethodType.PPBL.toString(), false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken)
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.PPBL);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "USer already logged in using sso  app not installed payment done using Bank Mandate")
    public void PGP_29580_AutoAppInvoke_Appinvokeallowed_TC03_User_loggedIn_Bank_Mandate(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = MerchantType.SUBS_BANK_MANDATE_MID;

            PGPHelpers.validate_MerchantPreference(merchant.getId(), "AUTO_APP_INVOKE_ALLOWED", "Y");
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchant.getId()); theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                    .setSubscriptionPaymentMode("BANK_MANDATE")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setTxnValue("10")
                    .setSubscriptionFrequency("1")
                    .setSubscriptionFrequencyUnit("MONTH")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("")
                    .setMandateAccountDetails(new MandateAccountDetails())
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), "BANK_MANDATE", false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                    .setTXN_AMOUNT("10")
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assert.assertTrue(cashierPageURL.contains("showPaymentPage"));
            cashierPage.payBy(PayMode.BANK_MANDATE);
            cashierPage.bankMandateSubscribe().click();
        //  cashierPage.bankMandateConfirmPay().click();

            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount("10.00")
                    .validateRespCode("3006")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "Subscription transaction when app is not installed but user is logged in (Balance) ")
    public void PGP_29580_Appinvokeallowed_TC04_User_loggedIn_Balance(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 100.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchantType = MerchantType.SUBS_PPBL_MID;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "N");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId());  theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                    .setTxnValue("10")
                    .setSubscriptionPaymentMode("")
                    .setSubsPPIOnly("Y")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setSubscriptionFrequency("2")
                    .setSubscriptionFrequencyUnit("MONTH")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("SUBSCRIBE")
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchantType.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken)
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            String cashierPageURL = DriverManager.getDriver().getCurrentUrl();
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
               Assert.assertTrue(cashierPageURL.contains("showPaymentPage"));
            cashierPage.payBy(PayMode.WALLET);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount("10.00")
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "User logged in on cashier page and payment done using Balance . app not installed ")

    public void PGP_29580_Appinvokeallowed_TC04_User_loggedIn_BalanceWallet_SSoSent(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 100.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchantType = MerchantType.SUBS_PPBL_MID;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "N");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId());  theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                    .setTxnValue("10")
                    .setSubscriptionPaymentMode("")
                    .setSubsPPIOnly("Y")
                    .setSsoToken(user.ssoToken())
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setSubscriptionFrequency("2")
                    .setSubscriptionFrequencyUnit("MONTH")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("SUBSCRIBE")
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchantType.getId(), initTxnDTO.orderFromBody(), PayMethodType.BALANCE.toString(), false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken)
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(PayMode.WALLET);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "USer already logged in using sso  app not installed payment done using PPBL")
    public void PGP_29580_AutoAppInvoke_appinvokeallowed_TC04_User_loggedIn_PPBL(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.SINGLECLICKDENROLLCARD);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchantType = MerchantType.SUBS_PPBL_MID;

            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_APP_INVOKE_ALLOWED", "N");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchantType.getId());  theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                    .setTxnValue("10")
                    .setSubscriptionPaymentMode("PPBL")
                    .setSubsPPIOnly("Y")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setSubscriptionFrequency("2")
                    .setSubscriptionFrequencyUnit("WEEK")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("")
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchantType.getId(), initTxnDTO.orderFromBody(), PayMethodType.PPBL.toString(), false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType, initTxnDTO.getBody().getOrderId(), txnToken)
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.PPBL);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .validateRespCode("01")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-29580")
    @Owner(Constants.Owner.SHUBHAM)
    @Test(description = "USer already logged in using sso  app not installed payment done using Bank Mandate")
    public void PGP_29580_Appinvokeallowed_TC04_User_loggedIn_Bank_Mandate(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = MerchantType.SUBS_PPBL_MID;

            PGPHelpers.validate_MerchantPreference(merchant.getId(), "AUTO_APP_INVOKE_ALLOWED", "N");
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "appInvokeAllowed", "Y");
        //    FF4JFlags.enableMidBased("theia.autoAppInvokePhase2", merchant.getId()); theia.autoAppInvokePhase2 Flag is ON on prod for specific Mids
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                    .setSubscriptionPaymentMode("BANK_MANDATE")
                    .setSubscriptionAmountType("VARIABLE")
                    .setSubscriptionMaxAmount("10")
                    .setTxnValue("10")
                    .setSubscriptionFrequency("1")
                    .setSubscriptionFrequencyUnit("MONTH")
                    .setSubscriptionGraceDays("1")
                    .setSubscriptionStartDate(SubscriptionStartDate)
                    .setRequestType("")
                    .setMandateAccountDetails(new MandateAccountDetails())
                    .build();
            InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
            String txnToken = initTxnResponseDTO.getBody().getTxnToken();
            execute_validateFetchPaymentOption(txnToken, merchant.getId(), initTxnDTO.orderFromBody(), "BANK_MANDATE", false);
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                    .setTXN_AMOUNT("10")
                    .build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(PayMode.BANK_MANDATE);
            cashierPage.bankMandateSubscribe().click();
       //     cashierPage.bankMandateConfirmPay().click();

            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateRespCode("3006")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();

    }
    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify that Recurring Bill Amount & Recurring Bill Frequency are displayed correctly on Cashier and Subscription Details Page where Frequency = MONTH" +"Paymode = Add n Pay")
    public void PGP_35247_TC_01_Subs_UIUX_Month(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.SUBS_ALL_PAYMODES;
        String TxnAmount = "10";
        String TxnMaxAmount = "100";


        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        String subscriptionStartDate = orderDTO.getSUBS_START_DATE();
        String subscriptionEndDate = orderDTO.getSUBS_EXPIRY_DATE();
        String[] validityStartDate = subscriptionStartDate.split("-");
        String[] validityEndDate = subscriptionEndDate.split("-");
        String month = CommonHelpers.addMonths(subscriptionStartDate, "yyyy-MM-dd",1);
        String addMonth[] = month.split("-");
        String nextPayment = addMonth[2]+" "+ Month.of(Integer.parseInt(addMonth[1])).toString().substring(0,3)+" '"+addMonth[0].substring(2,4);
        String validity = validityStartDate[2]+" "+ Month.of(Integer.parseInt(validityStartDate[1]))+" '"+validityStartDate[0].substring(2,4)+" - "+validityEndDate[2]+" "+ Month.of(Integer.parseInt(validityEndDate[1])).toString().substring(0,3)+" '"+validityEndDate[0].substring(2,4);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.toBePaidTab().assertVisible();
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.TO_BE_PAID_NOW.toString())).isEqualTo("Rs" + TxnAmount);
        cashierPage.subsRecurringBillAmount().assertVisible();
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.RECURRING_BILL_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);
        cashierPage.recurringBillFrequency().assertVisible();
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.RECURRING_BILL_FREQUENCY.toString())).isEqualTo("Every Month");
        //Subs details
        cashierPage.subscriptionDetails().click();
        cashierPage.amountToBePaid().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.AMOUNT_TO_BE_PAID_NOW.toString())).isEqualTo("Rs" + TxnAmount);
        cashierPage.subsDetailsRecurringAmount().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.RECURRING_BILL_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);
        cashierPage.subsDetailsFrequency().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.FREQUENCY.toString())).isEqualTo("Every Month");
        cashierPage.nextPayment().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.NEXT_PAYMENT.toString()).equalsIgnoreCase(nextPayment));
        cashierPage.subsDetailsValidity().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.VALIDITY.toString()).equalsIgnoreCase(validity));

    }

    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify that Recurring Bill Amount on Cashier and Subscription Details Page where Frequency = DAY")
    public void PGP_35247_TC_02_Subs_UIUX_DAY(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.PGOnly;
        String TxnAmount = "2";
        String TxnMaxAmount = "100";
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchant, theme,user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.toBePaidTab().assertVisible();
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.TO_BE_PAID_NOW.toString())).isEqualTo("Rs" + TxnAmount);
        cashierPage.subsRecurringBillAmount().assertVisible();
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.RECURRING_BILL_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);
        //Subs details
        cashierPage.subscriptionDetails().click();
        cashierPage.amountToBePaid().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.AMOUNT_TO_BE_PAID_NOW.toString())).isEqualTo("Rs" + TxnAmount);
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.VALIDITY.toString())).isNotNull();
    }

    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify that Recurring Bill Amount & Recurring Bill Frequency are displayed correctly on Cashier and Subscription Details Page where Frequency = YEAR" + "Paymode = PPI")
    public void PGP_35247_TC_03_Subs_UIUX_YEAR(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.PGOnly;
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("YEAR")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_PAYMENT_MODE("PPI")
                .build();
        checkoutPage.createOrder(orderDTO);
        String subscriptionStartDate = orderDTO.getSUBS_START_DATE();
        String subscriptionEndDate = CommonHelpers.addYears(subscriptionStartDate, "yyyy-MM-dd",1);
        String addYear[] = subscriptionEndDate.split("-");
        String nextPayment = addYear[2]+" "+ Month.of(Integer.parseInt(addYear[1])).toString().substring(0,3)+" '"+addYear[0].substring(2,4);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.paybyPaytm().getText().contains("Paytm Wallet"));
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.RECURRING_BILL_FREQUENCY.toString())).isEqualTo("Every Year");
        //Subs details
        cashierPage.subscriptionDetails().click();
        cashierPage.amountToBePaid().assertVisible();
        cashierPage.subsDetailsFrequency().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.FREQUENCY.toString())).isEqualTo("Every Year");
        cashierPage.nextPayment().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.NEXT_PAYMENT.toString()).equalsIgnoreCase(nextPayment));
        cashierPage.subsDetailsValidity().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.VALIDITY.toString())).isNotNull();
    }

    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner("VISHNU SHEKAR")
    @Test(description = "Verify that Recurring Bill Amount & Recurring Bill Frequency are displayed correctly on Cashier and Subscription Details Page where Frequency = ONDEMAND")
    public void PGP_35247_TC_04_Subs_UIUX_ONDEMAND(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.SUBS_ALL_PAYMODES;
        String TxnAmount = "10";
        String TxnMaxAmount = "100";
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.RECURRING_BILL_FREQUENCY.toString())).isEqualTo("On Demand");
        //Subs details
        cashierPage.subscriptionDetails().click();
        cashierPage.amountToBePaid().assertVisible();
        cashierPage.subsDetailsFrequency().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.FREQUENCY.toString())).isEqualTo("On Demand");
        cashierPage.subsDetailsValidity().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.VALIDITY.toString())).isNotNull();
    }

    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify that correct transaction amount should displayed against \"To be Paid Now\" when subscriptionAmountType = FIX")
    public void PGP_35247_TC_05_Subs_UIUX_FIX(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.SUBS_ALL_PAYMODES;
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.toBePaidTab().assertVisible();
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.TO_BE_PAID_NOW.toString())).contains("₹" + TxnAmount);
        Assertions.assertThat(cashierPage.tableSubUI().getRowValue(Constants.subscriptionRevamp.RECURRING_BILL_AMOUNT.toString())).isEqualTo("Rs" + TxnMaxAmount);
        //Subs details
        cashierPage.subscriptionDetails().click();
        cashierPage.amountToBePaid().assertVisible();
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.AMOUNT_TO_BE_PAID_NOW.toString())).isEqualTo("Rs" + TxnAmount);
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.RECURRING_BILL_AMOUNT.toString())).isEqualTo("Rs"+ TxnMaxAmount);
        Assertions.assertThat(cashierPage.tableSubdetailsUI().getRowValue(Constants.subscriptionDetailsUI.VALIDITY.toString())).isNotNull();
    }

    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify that UPI as paymode option is displayed on cashier page for subscription transaction")
    public void PGP_35247_TC_06_Subs_UIUX_UPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.UPI_NATIVE_SUBS;
        String TxnAmount = "2";
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_PAYMENT_MODE("UPI")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.payUPI().getText().contains("UPI")).isTrue();

    }
    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify that Saved Card as paymode option is displayed on cashier page for subscription transaction"+"PayMode=PPBL")
    public void PGP_35247_TC_07_Subs_UIUX_PPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_PAYMENT_MODE("PPBL")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.tabPPBL().isVisible());

    }

    @Parameters({"theme"})
    @Feature("PGP-35247")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify that Saved Card as paymode option is displayed on cashier page for subscription transaction"+"PayMode=CC")
    public void PGP_35247_TC_08_Subs_UIUX_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SIHUB_Subs, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_PAYMENT_MODE("CC")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.tabCreditCard().getText().contains("Credit")).isTrue();
    }
    @Parameters({"theme"})
    @Feature("PGP-39530")
    @Owner(Constants.Owner.Amanpreet)
    @Test(description = "Verify that CC/DC as paymode option is displayed on cashier page for subscription transaction for grace days=0")
    public void PGP_39530_TC_01_Subs_GD_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        //MERCHANT_UPI_PPI_CC_DC_SUBS
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SIHUB_Subs, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_PAYMENT_MODE("")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertVisible();
    }
    @Parameters({"theme"})
    @Feature("PGP-39530")
    @Owner(Constants.Owner.Amanpreet)
    @Test(description = "Verify that CC/DC as paymode option is displayed on cashier page for subscription transaction for grace days=3")
    public void PGP_39530_TC_02_Subs_GD_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        //MERCHANT_UPI_PPI_CC_DC_SUBS
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SIHUB_Subs, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("3")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_PAYMENT_MODE("")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertVisible();
    }
    @Parameters({"theme"})
    @Feature("PGP-39530")
    @Owner(Constants.Owner.Amanpreet)
    @Test(description = "Verify that CC/DC as paymode option is not displayed on cashier page for subscription transaction for grace days=4")
    public void PGP_39530_TC_03_Subs_GD_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        //MERCHANT_UPI_PPI_CC_DC_SUBS
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.SIHUB_Subs, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("4")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_PAYMENT_MODE("")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertNotVisible();

    }

    @Parameters({"theme"})
    @Feature("PGP-39935")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify text is displayed in UPI paymodes")
    public void PGP_39935_TC_SubsUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.UPI_NATIVE_SUBS;
        String TxnAmount = "2";
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payUPI().click();
        String upiText = cashierPage.upiTitle().getText();
        Assertions.assertThat(upiText).contains("This is available for supported banks for up to ₹5,000");
    }
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP-40026")
    @Parameters({"theme"})
    @Test(description = "Validate Debit Card is selected as Default Authentication Mode is Debit Card")
    public void PGP_40026_TC_BankMandateDefaultAuthMode(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("10")
                .setBankIfsc("")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();
        cashierPage.getBankMandateList().get(0).click();
        Assertions.assertThat(cashierPage.getDefaultAuthMode().isSelected()).isTrue();
        Assertions.assertThat(cashierPage.getDefaultAuthMode().getAttribute("value").equals("DEBIT_CARD"));
    }
    @Parameters({"theme"})
    @Feature("PGP-40525")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify revocable=N status when MCC=7322 and SUBSCRIPTION_REVOCABLE_FLAG is enabled")
    public void PGP_40525_TC01_UPICollectSubs(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        String TxnAmount = "2";
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
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
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .assertAll();
        String grepcmd = "grep \"Payment Request\" /paytm/logs/instaproxy.log | grep "+ orderDTO.getORDER_ID() +"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("\"revocable\""+":"+"\"N\"");

        String grepcommand = "grep \"TxnPayloadLogger\" /paytm/logs/instaproxy.log | grep "+ orderDTO.getORDER_ID() +"";
        String logsInsta = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcommand);
        Assertions.assertThat(logsInsta).contains("\\\"MCC\\\":\\\"7322\\");

    }
    @Parameters({"theme"})
    @Feature("PGP-40525")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify revocable=Y status when MCC is other than 7322 and SUBSCRIPTION_REVOCABLE_FLAG is enabled")
    public void PGP_40525_TC02_UPICollectSubs(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_UPI;
        String TxnAmount = "2";
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
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
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .assertAll();
        String grepcmd = "grep \"Payment Request\" /paytm/logs/instaproxy.log | grep " + orderDTO.getORDER_ID() + "";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("\"revocable\"" + ":" + "\"Y\"");

        String grepcommand = "grep \"TxnPayloadLogger\" /paytm/logs/instaproxy.log | grep "+ orderDTO.getORDER_ID() +"";
        String logsInsta = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcommand);
        Assertions.assertThat(logsInsta).doesNotContain("\\\"MCC\\\":\\\"7322\\");

    }


}

