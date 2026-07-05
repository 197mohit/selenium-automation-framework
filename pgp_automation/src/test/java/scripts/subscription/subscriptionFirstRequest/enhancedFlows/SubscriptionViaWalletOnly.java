package scripts.subscription.subscriptionFirstRequest.enhancedFlows;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ResponseCode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.savecardService.SaveCard;

import java.util.Date;

import static com.paytm.base.test.Group.Status.BUG;


/**
 * Created by sureshgupta on 26/09/17.
 *//*
 */
@Owner("Tarun")
public class SubscriptionViaWalletOnly extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    public void successfulTxnForInsufficientBalance(@Optional("enhancedweb") String theme, PayMode payMode) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(payMode);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();

    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction using wallet only")
    public void PGP_180_verifySuccessfulTxnUsingWalletOnly(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme).build();
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.WALLET);

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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }

    @Issue("PGP-16085")
    @Parameters({"theme"})
    @Test(groups = BUG,description = "To verify successful transaction for Insufficient wallet amount via CC")
    public void PGP_181_verifySuccessfulTxnForInsufficientBalanceViaCC(@Optional("enhancedweb") String theme) throws Exception {
        successfulTxnForInsufficientBalance(theme, PayMode.CC);
    }


//    @Parameters({"theme"})
//    @Test(description = "To verify successful transaction for Insufficient wallet amount via UPI", enabled = false)
    public void PGP_183_verifySuccessfulTxnForInsufficientBalanceViaUPI(@Optional("enhancedweb") String theme) throws Exception {
        successfulTxnForInsufficientBalance(theme, PayMode.UPI);

    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction  for Insufficient wallet amount via NB")
    public void PGP_184_verifySuccessfulTxnForInsufficientBalanceViaNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        PaymentDTO nbCredentials = new PaymentDTO()
                .setBankName("ICICI");
        cashierPage.payBy(PayMode.NB, nbCredentials);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();

    }

    @Issue("PGP-16085")
    @Parameters({"theme"})
    @Test(groups = BUG,description = "To verify card is not getting saved for successful transaction")
    public void PGP_186_verifyCardSaveAfterSuccessfulTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.CC);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }

    @Issue(("PGP-16085"))
    @Parameters({"theme"})
    @Test(groups = BUG,description = "To verify transaction using saved card")
    public void PGP_187_verifySuccessfulTxnUsingSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.SAVED_CARD);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }

    @Parameters({"theme", "CC"})
    @Test(description = "To verify transaction when cancelled at bank End via payment mode other than wallet")
    public void PGP_188_verifyTxnCancellationAtBank(@Optional("enhancedweb") String theme, @Optional("CC") String payMode) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setTXN_AMOUNT("99.99")
                .setSUBS_MAX_AMOUNT("100")
                .build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.valueOf(payMode));

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode())
                .validateRespMsg(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespMsg())
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify transaction when cancelled at bank End via saved card")
    public void PGP_189_verifyTxnCancellationAtBankWithSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setTXN_AMOUNT("99.99")
                .setSUBS_MAX_AMOUNT("100")
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.SAVED_CARD);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode())
                .validateRespMsg(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespMsg())
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
    }

//    @Issue(("PGP-16085"))
//    @Parameters({"theme"})
//    @Test(description = "To verify successful transaction through UPI", enabled = false)
    public void PGP_190_verifyVPA_GettingSavedForSuccessfulTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.UPI);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }

//    @Parameters({"theme"})
//    @Test(description = "To verify transaction using saved VPA", enabled = false)
    public void PGP_191_verifySuccessfulTxnViaSavedVPA(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.UPI);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();

        //Txn Using Saved VPA:

        orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.SAVED_UPI);

        new ResponsePage().waitUntilLoads();
        txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
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
    }

    @Parameters({"theme"})
    @Test(description = "To verify failed transaction using expired saved card")
    public void PGP_verifyFailedSubs_FromExpiredSaveCard(@Optional("enhancedweb") String theme) throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCard saveCard = new SaveCard();
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        String cardId = savedCardHelpers.saveCardUserId(user.custId(), saveCard.AesEncCardNumCredit, saveCard.AesEncExp, new PaymentDTO().getCreditCardNumber()).getResponse().toString();
        SavedCardHelpers.validateSaveCardDB_ByCardID(cardId);
        //change save card expiry date
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId, user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.tabSavedCard().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate the response if subs_Payment_Mode=EMPTY but SUBS_PPI_Only is selected as 'Y'")
    public void validateIfSubsPaymentModeIsEmptyAndPPIOnlyIsNot(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme).setSUBS_PAYMENT_MODE("").build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) +1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.buttonWalletPayNow().click();

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
    @Test(description = "Validate successful wallet only txn with insufficient balance with SUBS_TYPE = 'FIX'")
    public void PGP_181_verifyWalletOnlyWithFixedSubsType(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_MAX_AMOUNT("")
                .build();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.WALLET);

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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }


    @Parameters({"theme"})
    @Test(description = "Validate payment done with Daily frequency")
    public void PGP_182_verifySubsDailyFrequencyFlow(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setSUBS_GRACE_DAYS("0")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.WALLET);

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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }

    @Issue("PGP-16085")
    @Parameters({"theme"})
    @Test(groups = BUG,description = "Validate txn should be done with any pay modes if all payment modes are visible")
    public void validateAllPayModes(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL,Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT())-1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
      //To be Confirmed
       /* if (!cashierPage.checkBoxPPI().isChecked()){
                  cashierPage.checkBoxPPI().check();
        } */
        cashierPage.checkBoxPPI().click();
        cashierPage.payBy(PayMode.DC);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }



    //PGP-21763 Verify Different Subscription Error Messages

    @Parameters({"theme"})
    @Test(description = "Verify that Invalid Frequency Unit message is correctly displayed")
    public void PGP_21763_verifyInvalidFrequencyUnitMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("cjkjsabbc")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("810")
                .validateRespMsg("Invalid Frequency Unit")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that Invalid Subscription Amount Type messages correctly displayed")
    public void PGP_21763_verifyInvalidSubsAmountTypeMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.PG2_SUBS_NATIVE, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setSUBS_AMOUNT_TYPE("")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("810")
                .validateRespMsg("Invalid Subscription Amount Type")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that Invalid Max. Amount message is correctly displayed")
    public void PGP_21763_verifyInvalidMaxSubsAmountMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.PG2_SUBS_NATIVE, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setSUBS_MAX_AMOUNT("")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("810")
                .validateRespMsg("Invalid Max Amount")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify if txn amount>max amount, txn should still be successful")
    public void PGP_21763_verifyTxnAmountCanBeGreaterthanMaxSubsAmountMessage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmt = "101";
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmt));
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(txnAmt)
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.WALLET);

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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that Grace days cannot be greater than the frequency set against the subscription message is correctly displayed")
    public void PGP_21763_verifyGraceDaysCannotBeGreaterthanFrequencyMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setSUBS_GRACE_DAYS("32")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("810")
                .validateRespMsg("Grace days cannot be greater than the frequency set against the subscription")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that \"Invalid Subs Payment mode\" message is displayed correctly")
    public void PGP_21763_verifyInvalidSubsPaymodeMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.PG2_SUBS_NATIVE, theme)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("N")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("2022")
                .validateRespMsg("Invalid Subs payment mode")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that \"Paymode selected is not enabled for your merchant account. Please reach out to support teams to enable\n" +
            "\" message is displayed correctly")
    public void PGP_21763_verifyInvalidPaymodeSelectedMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_PAYMENT_MODE("NB")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("2022")
                .validateRespMsg("Paymode selected is not enabled for your merchant account. Please reach out to support teams to enable")
                .assertAll();
    }
    //Add N Pay is not supported for Subscription and for Wallet only txn ppi-y only is supported
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify that 'Add and Pay Payments are not allowed for this subscription' message is displayed correctly")
    public void PGP_21763_verifyAddnPayNotAllowedMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_WALLET_LIMIT, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("4006")
                .validateRespMsg("Add and Pay Payments are not allowed for this subscription")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that 'Max amount is greater than the permissible amount limit for available paymodes' message is displayed correctly")
    public void PGP_21763_verifyMaxAmtGreaterThanPermissibleAmtMessage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_LIMIT, theme)
                .setSUBS_MAX_AMOUNT("5100")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("4004")
                .validateRespMsg("Max amount is greater than the permissible amount limit for available paymodes")
                .assertAll();
    }



    @Parameters({"theme"})
    @Test(description = "Verify that Grace days value is mandatory")
    public void PGP_21763_verifyGraceDaysMandatoryMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSUBS_FREQUENCY("1")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setSUBS_GRACE_DAYS("")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("810")
                .validateRespMsg("Grace days value is mandatory")
                .assertAll();
    }

    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "Verify that for Wallet only merchant - wallet should be selected on cashier page ")
    public void ValidateWalletChecked_walletOnlyMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet paymode should be selected").isTrue();

    }




    @Owner("ESHANI")
    @Parameters({"theme"})
    @Feature("PGP-29089")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Test(description = "Verify a successful wallet transaction after otp login-wallet should be selected ")
    public void ValidateWalletChecked_walletOnlyMerchantUserLoginatCashierPage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.PG2_SUBS_NATIVE, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet paymode should be selected").isTrue();

    }
    @Owner("Ashish")
    @Feature("PGP-35774")
    @Parameters({"theme"})
    @Test(description = "To verify a successful Add N Pay transaction when 2fa for subs on wallet is disabled for Merchant")
    public void PGP_35774_Validate_2fa_disabled_wallet_subscription_tc3(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.WALLET_2FA_DISABLED, theme)
                .setTXN_AMOUNT("200")
                .build();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
       // cashierPage.payBy(PayMode.CC,new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_CREDIT_CARD_NUMBER));
        cashierPage.payBy(PayMode.PPBL);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
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

    @Owner("Ashish")
    @Feature("PGP-35774")
    @Parameters({"theme"})
    @Test(description = "Create a subscription with paymode PPI in PTC call when 2fa for subs on wallet is disabled for merchant")
    public void PGP_35774_Validate_2fa_disabled_wallet_subscription_Tc1(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.WALLET_2FA_DISABLED, theme)
                .setTXN_AMOUNT("100")
                .build();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.WALLET);

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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();

    }

    @Owner("Ashish")
    @Feature("PGP-35774")
    @Parameters({"theme"})
    @Test(description = "Create a subscription with paymode PPI while Initiating the txn when 2fa for subs on wallet is disabled for merchant")
    public void PGP_35774_Validate_2fa_disabled_wallet_subscription_Tc2(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.WALLET_2FA_DISABLED, theme)
                .setSUBS_PAYMENT_MODE("")
                .setTXN_AMOUNT("100")
                .build();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.WALLET);

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
                .validateSubsid(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNull();

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-48530")
    @Parameters({"theme"})
    @Test(description = "To verify Bank Transaction Id returned for successful wallet transaction")
    public void WalletSubs_BankTxnId_returned(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBS_Prenotify_MID, theme).build();
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
      //  WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

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
                .validateSubsid(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-54581")
    @Parameters({"theme"})
    @Test(description = "Verify that user is not able to create subscription with wallet as paymode")
    public void walletNotSupporting_subsCreation(@Optional("enhancedweb_revamp") String theme) throws Exception {

        // ff4j theia.disable.balance.forSubscriptionEligibility should be ON

        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBS_Prenotify_MID, theme)
                .setTXN_AMOUNT("3")
                .build();
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("2022")
                .validateRespMsg("Invalid Subs payment mode")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .assertAll();

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-54581")
    @Parameters({"theme"})
    @Test(description = "Verify that user is not able to create subscription with PPBL as paymode")
    public void ppblNotSupporting_subsCreation(@Optional("enhancedweb_revamp") String theme) throws Exception {

      // ff4j theia.disable.ppbl.forSubscriptionEligibility should be ON

        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBS_Prenotify_MID, theme)
                .setTXN_AMOUNT("3")
                .setSUBS_PAYMENT_MODE("PPBL")
                .build();
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("2022")
                .validateRespMsg("Invalid Subs payment mode")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .assertAll();

    }
}

