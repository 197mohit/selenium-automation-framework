package scripts;

import com.paytm.api.SMSPrimary;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peon;
import io.qameta.allure.Owner;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

@Owner("Deepak")
public class Hybrid extends PGPBaseTest {
    private final static int TIME_FOR_UPI_MOCK_TO_RESPOND = 22;
    private final UPIpage upiPage = new UPIpage();
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate successful Hybrid transaction using CC.", groups = {"smoke"})
    public void hybrid_CC_S(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.HYBRID_MID, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.HYBRID_MID.getKey())
                .validateChildTxnsPresent()
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful Hybrid transaction using UPI.",enabled = false)
    public void hybrid_UPI_S(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_Txn, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.UPI);
        upiPage.waitUntilLoads();
        upiPage.pause(TIME_FOR_UPI_MOCK_TO_RESPOND);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "UPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.ICICI.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful Hybrid transaction using UPI with sufficient wallet balance",enabled = false)
    public void hybrid_UPI_S_sufficientBal(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT());
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    // Marking as disabled because InvalidPin scenario is not valid for UPI
   // @Parameters({"theme"})
   // @Test(description = "Validate fail Hybrid transaction using UPI with insufficient wallet balance and invalid pin", enabled = false)
    public void hybrid_UPI_F_withInvalidPin(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("100.94").build();
        double amountToBeRetainedInWallet = 1;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("227").assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

   // @Parameters({"theme"})
   // @Test(description = "Validate Txn failure on Cancellation on Bank Authorisation Page", enabled = false)
    public void hybrid_CC_F_cancelOn3Dpage(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("100.99").build();
        double amountToBeRetainedInWallet = Double.valueOf("1.00");
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(ValidationType.EMPTY)
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful txn with decimal amount > 1")
    public void hybrid_CC_S_withDecimalAmount(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.10").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

  //  @Parameters({"theme"})
  //  @Test(description = "Verify Txn failure on Credit Card Cancel on Cashier Page", enabled = false)
    public void hybrid_CC_F_cancelCashierPage(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.linkPGCancel().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(ValidationType.EMPTY)
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateBankName(ValidationType.EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(ValidationType.EMPTY)
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

  //  @Parameters({"theme"})
  //  @Test(description = "Verify Txn failure on Debit Card Cancel on Cashier Page.", enabled = false)
    public void hybrid_DC_F_cancelCashierPage(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.linkPGCancel().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(ValidationType.EMPTY)
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateBankName(ValidationType.EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(ValidationType.EMPTY)
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Issue("PGP-13109")
    @Parameters({"theme"})
    @Test(description = "Verify successful transaction via Wallet only")
    public void hybrid_PPI_S_sufficientBal(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.buttonWalletPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01").assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
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
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isZero();
    }


    @Parameters({"theme"})
    @Test(description = "Verify that on successful txn card should be saved.")
    public void hybrid_CC_S_validateCardSaved(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavingCard:
        {
            OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_WithoutEMI, theme, user)
                    .setTXN_AMOUNT("2.00").build();
            double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
            WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
            SavedCardHelpers.deleteSavedCard(user);
            WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
//            cashierPage.checkBoxPPI().check();
            cashierPage.payBy(PayMode.CC);
            new ResponsePage().waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("HYBRID")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateChildTxnsPresent();

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                    .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                    .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                    .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                    .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                    .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                    .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                    .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                    .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                    .AssertAll();
        }
        Validation:
        {
            OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                    .setTXN_AMOUNT("2.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            System.out.println(SavedCardHelpers.getSavedCardId(user, 0));
            cashierPage.assertSavedCardVisibility();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful Hybrid transaction without saving card")
    public void hybrid_CC_S_validateCardNotSaved(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SavedCardHelpers.deleteSavedCard(user);
        SavingCard:
        {
            OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_WithoutEMI, theme, user)
                    .setTXN_AMOUNT("11.00").build();
            SavedCardHelpers.deleteSavedCard(user);
            double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
            WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
            checkoutPage.createOrder(orderDTO);
            PaymentDTO paymentDTO = new PaymentDTO();
            cashierPage.checkBoxPPI().check();
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().type(paymentDTO.getCreditCardNumber());
            cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
            cashierPage.fillExpiryYear(paymentDTO.getExpYear());
            cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
            cashierPage.radioBtnSaveCard().click();
            //cashierPage.checkbox_wallet();

            cashierPage.buttonPGPayNow().click();

            new ResponsePage().waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("HYBRID")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateChildTxnsPresent();

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                    .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                    .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                    .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                    .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                    .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                    .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                    .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                    .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                    .AssertAll();
        }
        Validation:
        {
            OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_WithoutEMI, theme, user)
                    .setTXN_AMOUNT("2.00").build();
            checkoutPage.createOrder(orderDTO);
            cashierPage.pause(3);
            cashierPage.tabSavedCard().assertNotVisible();
        }
    }

    @Parameters({"theme"})
    @Test(description = "To verify wallet balance on the cashier page when user logged in")
    public void hybrid_verifyWalletBalanceAfterLogin(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        Double amntToBeRetained = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1;
        WalletHelpers.modifyBalance(user, amntToBeRetained);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String uiUserWalletBalance = String.format("%.2f", new BigDecimal(cashierPage.walletBalanceCheck().getText().replace("Rs", "").trim()));
        String actualWalletBalance = String.format("%.2f", new BigDecimal(Double.toString(amntToBeRetained)));
        Assertions.assertThat(uiUserWalletBalance).as("Wallet balance is not as expected").isEqualToIgnoringCase(actualWalletBalance);
    }

    @Parameters({"theme"})
    @Test(description = "To verify all payments mode highlighted correctly in accordance with the payment")
    public void hybrid_verifyAllPayModeOnCashier(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        Double amntToBeRetained = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1;
        WalletHelpers.modifyBalance(user, amntToBeRetained);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabNetBanking().assertVisible();
    }

  //  @Parameters({"theme"})
  //  @Test(description = "To verify response of the successful wallet withdraw transaction having sufficient wallet balance", enabled = false)
    public void hybrid_PPI_S_sufficientBalanceValidateWalletBalance(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        Double amntToBeRetained = Double.valueOf(orderDTO.getTXN_AMOUNT());
        WalletHelpers.modifyBalance(user, amntToBeRetained);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
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
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isZero();
    }

    @Parameters({"theme"})
    @Test(description = "To verify saved card list appearing for merchant after login")
    public void hybrid_verifySavedCardList(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        String card1 = "4109139965359183";
        String card2 = "5507032420388415";
        SavedCardHelpers.addCard(user, "06", "2022", card1);
        SavedCardHelpers.addCard(user, "07", "2023", card2);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (!Arrays.asList("enhancedwap", "enhancedweb").contains(theme)) {
            cashierPage.tabSavedCard().click();
        }
        cashierPage.verifyCardDisplayed(card1, card2);
    }

    @Parameters({"theme"})
    @Test(description = "To verify response of the successful Hybrid transaction using saved card")
    public void hybrid_SC_S(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.SAVED_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFCSC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Owner("Deepak")
 //   @Issues({@Issue("PGP-9532"), @Issue("PGP-15522")})
    @Parameters({"theme"})
    @Test(description = "To verify Txn Failure on cancellation of transaction when retry count reached for merchant")
    public void hybrid_CC_F_retryCountBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_Retry, theme, user)
                .setTXN_AMOUNT("2.00").build();
        Double amountToBeRetainedInWallet = 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode())
                .validateRespMsg(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .AssertAll();
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();
    }

    @Owner("Deepak")
 //   @Issues({@Issue("PGP-9532"), @Issue("PGP-15522")})
    @Parameters({"theme"})
    @Test(description = "To verify Txn Success on cancellation of transaction when retry count not reached for merchant")
    public void hybrid_CC_retry_S(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_Retry, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG only transaction if the wallet balance is zero")
    public void hybrid_CC_S_walletBalanceZero(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("1.00").build();
        WalletHelpers.modifyBalance(user, 0.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
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
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify response of the successful Hybrid transaction using DC payment mode")
    public void hybrid_DC_S(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()

                .validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS")

                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify response of the successful Hybrid transaction using NB payment mode when sufficient wallet balance")
    public void hybrid_NB_S_uncheckWalletwithSufficientBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT());
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(PayMode.NB, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify response of the successful Hybrid transaction using NB payment mode")
    public void hybrid_NB_S(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_WithoutEMI, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(PayMode.NB, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "NB")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.ICICI.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.ICICI.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify Save card for Hybrid Txn using DC")
    public void hybrid_DC_S_verifyCardSaved(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavingCard:
        {
            OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_WithoutEMI, theme, user)
                    .setTXN_AMOUNT("2.00").build();
            SavedCardHelpers.deleteSavedCard(user);
            double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
            WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(PayMode.DC_WITH_SAVECARD);
            new ResponsePage().waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("HYBRID")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateChildTxnsPresent();

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                    .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                    .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                    .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                    .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                    .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                    .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                    .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                    .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                    .AssertAll();
        }
        Validation:
        {
            OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                    .setTXN_AMOUNT("2.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.assertSavedCardVisibility();
        }
    }

    @Flaky
    @Parameters({"theme"})
    @Test(description = "To verify successful Hybrid Txn using DC saved card", groups = {"debug"})
//TODO same card gets saved as CC and sometimes as DC
    public void hybrid_SC_S_withDCCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();

        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.SAVED_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFCSC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

 //   @Parameters({"theme"})
 //   @Test(description = "To verify Txn Failure on DC Txn cancellation at 3D secure page", enabled = false)
    public void hybrid_DC_F_cancelOn3Dpage(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("100.99").build();
        double amountToBeRetainedInWallet = Double.valueOf("1.00");
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(ValidationType.EMPTY)
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Error Message when invalid CVV number, invalid Expiry Date is passed")
    public void hybrid_CC_F_invalid_Cvv_Expiry(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("3.00").build();
        Double amountToBeRetainedInWallet = 1.00;
        WalletHelpers.modifyBalance(user, Double.valueOf(amountToBeRetainedInWallet));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCvvNumber("9999").setExpMonth("21").setExpYear("2121");
        cashierPage.fillInvalidCCDetails(paymentDTO);
        cashierPage.buttonPGPayNow().click();
        Assert.assertEquals("Invalid Expiry Date", cashierPage.error_invalidExpiryDate().getText());
        cashierPage.closeCcDcDetailBtn().click();
        paymentDTO.setExpMonth("12").setExpYear("2222");
        cashierPage.fillInvalidCCDetails(paymentDTO);
        cashierPage.buttonPGPayNow().click();
        Assert.assertEquals("CVV is Invalid", cashierPage.getError_invalidCVV().getText());
    }

    @Parameters({"theme"})
    @Test(description = "Verify the Txn Failure when invalid OTP number is passed")
    public void hybrid_CC_F_invalidOTP(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        Double amountToBeRetainedInWallet = 1.00;
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("100.95").build();
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode())
                .validateRespMsg(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();
    }

 //   @Parameters({"theme"})
 //   @Test(enabled = false, description = "Verify the Txn Failure when invalid expiry date is passed")
    public void hybrid_CC_F_invalidExpiry(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        String txnAmount = "100.96";
        Double amountToBeRetainedInWallet = 1.00;
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT(txnAmount).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(amountToBeRetainedInWallet));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify Txn Failure for passing incorrect CVV with saved cards")
    public void hybrid_SC_F_invalidCvv(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        PaymentDTO paymentDTO = new PaymentDTO().setCvvNumber("123");
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        String txnAmount = "100.97";
        String walletBalance = "1.00";
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT(txnAmount).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(walletBalance));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - Double.valueOf(walletBalance)))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFCSC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, walletBalance)
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();
    }


 //   @Parameters({"theme"})
 //   @Test(description = "To verify Txn Failure using NB when txn is cancelled at cashier page", enabled = false)
    public void hybrid_NB_F_cancelOnCashier(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.tabNetBanking().click();
        cashierPage.linkPGCancel().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

 //   @Parameters({"theme"})
 //   @Test(description = "Verify the Txn Failure when browser is closed while doing the payment", enabled = false)
    public void hybrid_F_whenBrowserClosed(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().quit();
        Thread.sleep(600000);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateBankName(ValidationType.EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(ValidationType.EMPTY)
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

  //  @Parameters({"theme"})
  //  @Test(description = "To verify successful Hybrid Txn using saved VPA", enabled = false)
    public void PGP_336_validateHybridTxnUsingSavedUPI(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.payBy(PayMode.UPI);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "UPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.ICICI.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();


        //Next Hybrid Txn Using Saved UPI

        orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_UPI);
        new ResponsePage().waitUntilLoads();
        txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "UPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.ICICI.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

    }


    @Parameters({"theme"})
    @Test(description = "Validate successful Hybrid transaction when user logs in at cashier page")
    public void hybrid_CC_S_loginOnCashier(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN("")
                .build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.payBy(PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "To verify Successful Hybrid transaction for PPBL")
    public void hybrid_PPBL_S(@Optional("enhancedweb") String theme) throws Exception {
        String trxAmount = "1010";
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).
                setTXN_AMOUNT(trxAmount).build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1000.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.PPBL);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "NB")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.PPBL.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.PPBL.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET,
                        CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Hybrid EMI transaction flow using HDFC Card")
    public void validateHybridTxnUsingEMI(@Optional("enhancedweb") String theme) throws Exception {
        String hdfcDropdownBankName = "merchant4".equalsIgnoreCase(theme) ? "HDFC Bank" : "HDFC Bank Credit Card";
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setBankName(hdfcDropdownBankName)
                .setMonth(3);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "EMI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET,
                        CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    //This test case is specific to Enhanced web theme only
    @Parameters({"theme"})
    @Test(description = "Validate Error messages in Hybrid RETRY with EMI, pay it with other mode", groups = {"smoke"})
    public void hybridErrorMessageAndPayUsingAnotherMode(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_Retry, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        EMIErrorMessage:
        {
            cashierPage.tabEMI().click();
            Assertions.assertThat(cashierPage.notificationBar().getText()).as("Error message incorrect")
                    .containsIgnoringCase("Paytm Balance and EMI cannot be used together for this payment");
            cashierPage.notificationBarOK().click();
        }
        cashierPage.tabCreditCard().click();
        cashierPage.checkBoxPPI().check();
        PaymentDTO incorrectPaymentDTO = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.CC,incorrectPaymentDTO);
        cashierPage.waitUntilLoads();

        RetryErrorMessage:
        {
            Assertions.assertThat(cashierPage.notificationBar().getText()).as(" OTP Error message incorrect")
                    .containsIgnoringCase("Looks like OTP entered was incorrect. Please try again.");
            cashierPage.notificationBarOK().click();

        }

        cashierPage.payBy(PayMode.UPI);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.Hybrid_Retry.getKey())
                .validateChildTxnsPresent()
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "UPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.ICICI.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

    }





    @Parameters({"theme"})
    @Test(description = "To verify Successful Hybrid transaction for IDBI NB")
    public void hybrid_IDBI_NB(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).
                setTXN_AMOUNT("2").build();

        WalletHelpers.modifyBalance(user, 1.00);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("IDBI");
        cashierPage.payBy(PayMode.NB, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success Hybrid CC Corporate card on Corporate Card Hybrid Merchant",groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateHybridCCBinCorporateMerchant(@Optional("enhancedweb") String theme) throws Exception {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateMerchant.getId());

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 2.0;

        WalletHelpers.modifyBalance(user,txnAmount-1.0);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0,6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.Hybrid(corporateMerchant, theme,user)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(corporateMerchant.getKey())
                .validateChildTxnsPresent()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .AssertAll();

        Assertions.assertThat(txnStatus.getApiResponse().jsonPath().getString("CHILDTXNLIST[0].FEERATEFACTORS.CORPORATECARD")).isEqualTo("TRUE");

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "feeRateFactors","MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHILDTXNLIST", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals("HDFC Bank"),
                peon.payMode().equals("HYBRID"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.childTxnList().equals("").not(),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not()
        );
        sAssert.eval();
    }


    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success Hybrid DC Corporate card on Corporate Card Hybrid Merchant",groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateHybridDCBinCorporateMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardDC(corporateMerchant.getId());

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 2.0;

        WalletHelpers.modifyBalance(user,txnAmount-1.0);

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.Hybrid(corporateMerchant, theme,user)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(corporateMerchant.getKey())
                .validateChildTxnsPresent()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .AssertAll();

        Assertions.assertThat(txnStatus.getApiResponse().jsonPath().getString("CHILDTXNLIST[0].FEERATEFACTORS.CORPORATECARD")).isEqualTo("TRUE");

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "feeRateFactors","MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHILDTXNLIST", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals(Bank.AXIS.toString()),
                peon.payMode().equals("HYBRID"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.childTxnList().equals("").not(),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not()
        );
        sAssert.eval();
    }

@Parameters({"theme"})
@Test(description = "To test SMS is not sent on a specific number")
@Feature("PGP-28069")
@Owner(Constants.Owner.TARUN)
public void smsNotSent(@Optional("enhancedweb") String theme) throws Exception {
    User user = userManager.getForWrite(Label.SMSNOTSENT);
    MerchantType merchantType = MerchantType.Hybrid;
    OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType,theme,user)
            .build();

    checkoutPage.createOrder(orderDTO);
    CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

    cashierPage.payBy(PayMode.CC);
    ResponsePage responsePage = new ResponsePage();
    responsePage.waitUntilLoads();
    SMSPrimary smsPrimary = new SMSPrimary(orderDTO.getORDER_ID());
    Response smsResponse = smsPrimary.execute();

    Assertions.assertThat(smsResponse.jsonPath().getString("mobileNo")).doesNotContain(user.mobNo());
    Assertions.assertThat(smsResponse.jsonPath().getString("message")).doesNotContain(user.mobNo()).contains(orderDTO.getORDER_ID());

}


}
