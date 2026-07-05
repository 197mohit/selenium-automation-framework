package scripts.subscription.subscriptionFirstRequest.defaultFlows;

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
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.savecardService.SaveCard;

import java.util.Date;


/**
 * Created by sureshgupta on 26/09/17.
 *//*
*/

@Owner("Tarun")
public class SubscriptionViaWalletOnly extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private void successfulTxnForInsufficientBalance(@Optional("merchant4") String theme, PayMode payMode) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction using wallet only")
    public void PGP_180_verifySuccessfulTxnUsingWalletOnly(@Optional("merchant4") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme).build();
        User user = userManager.getForWrite(Label.BASIC);
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
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction for Insufficient wallet amount via CC")
    public void PGP_181_verifySuccessfulTxnForInsufficientBalanceViaCC(@Optional("merchant4") String theme) throws Exception {
        successfulTxnForInsufficientBalance(theme, PayMode.CC);
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction for Insufficient wallet amount via DC")
    public void PGP_182_verifySuccessfulTxnForInsufficientBalanceViaDC(@Optional("merchant4") String theme) throws Exception {
        successfulTxnForInsufficientBalance(theme, PayMode.DC);
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction for Insufficient wallet amount via UPI")
    public void PGP_183_verifySuccessfulTxnForInsufficientBalanceViaUPI(@Optional("merchant4") String theme) throws Exception {
        successfulTxnForInsufficientBalance(theme, PayMode.UPI);

    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction  for Insufficient wallet amount via NB")
    public void PGP_184_verifySuccessfulTxnForInsufficientBalanceViaNB(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
    }

    @Parameters({"theme"})
    @Test(description = "To verify transaction with wallet balance equal to zero and transaction amount equal to zero")
    public void PGP_185_verifySuccessfulTxnForZeroTxnAmtAndZeroWalletBalance(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setTXN_AMOUNT("0.0")
                .build();
        WalletHelpers.modifyBalance(user, 0.0);
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
    }

    @Parameters({"theme"})
    @Test(description = "To verify card is getting saved for successful transaction")
    public void PGP_186_verifyCardSaveAfterSuccessfulTxn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "To verify transaction using saved card")
    public void PGP_187_verifySuccessfulTxnUsingSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

    @Parameters({"theme", "CC"})
    @Test(description = "To verify transaction when cancelled at bank End via payment mode other than wallet")
    public void PGP_188_verifyTxnCancellationAtBank(@Optional("merchant4") String theme, @Optional("CC") String payMode) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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
    public void PGP_189_verifyTxnCancellationAtBankWithSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction through UPI")
    public void PGP_190_verifyVPA_GettingSavedForSuccessfulTxn(@Optional("merchant4") String theme) throws Exception {//skip
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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();
    }

//    @Parameters({"theme"})
//    @Test(description = "To verify transaction using saved VPA", enabled = false)
    public void PGP_191_verifySuccessfulTxnViaSavedVPA(@Optional("merchant4") String theme) throws Exception {
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
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).isNotNull().isNotEmpty();

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
    public void PGP_verifyFailedSubs_FromExpiredSaveCard(@Optional("merchant4") String theme) throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCard saveCard = new SaveCard();
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        String cardId = savedCardHelpers.saveCardUserId(user.custId(), saveCard.AesEncCardNumCredit, saveCard.AesEncExp, new PaymentDTO().getCreditCardNumber()).getResponse().toString();
        SavedCardHelpers.validateSaveCardDB_ByCardID(cardId);
        //change save card expiry date
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId, user);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = new CashierPageMerchant4();
        cashierPage.login(user);
        cashierPage.tabSavedCard().assertNotVisible();
    }

    @Parameters({"theme"})
    @Feature("PGP-37902")
    @Owner(Constants.Owner.PUSPA)
    @Test(description = "Verify for closed wallet user RBI guidelines error message should be shown")
    public void verify_closed_wallet_user_error_message_SubsFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.DEACTIVATEDUSER);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.DEACTIVATED_WALLET, theme)
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
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("Your wallet has been deactivated as mandated by RBI.");
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("As per RBI guidelines, all wallet accounts with no transactions in the past one year have been deactivated");

    }


}


