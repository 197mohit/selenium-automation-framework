package scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;

@Owner("Deepak")
public class COD extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verify a successful COD transaction with zero wallet balance, merchant type PGOnly ")
    public void COD_S_withPGOnlyMercWithZeroBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("11.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.COD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }




    @Parameters({"theme"})
    @Test(description = "Verify a successful COD transaction with zero wallet balance.", groups = {"smoke"})
    public void COD_S_withoutBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PG2_COD, theme, user)
                .setTXN_AMOUNT("11.00").build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.COD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("COD")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("CODMOCK")
                .validateBankName("CODMOCK")
                .validateCheckSum(MerchantType.PG2_COD.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("CODMOCK")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful COD transaction when COD amount > wallet balance > 1")
    public void COD_S_withLessBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.COD, theme, user)
                .setTXN_AMOUNT("17.00").build();
        Double CODAmount = 6.00;
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - CODAmount);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.COD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("CODMOCK")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify message <You'll receive an automated call from us to confirm this order. And at the time of delivery, Rs 17.0 will have to be given to the courier boy>")
    //Hybrid COD not supported.
    public void PGP_221_validateMsgCODTxnWithSomeWalletBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String txnAmount = "17.0";
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.COD, theme, user)
                .setTXN_AMOUNT(txnAmount).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 6);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCOD().click();
        Assert.assertEquals(cashierPage.getTextCODMessage().getText(), cashierPage.getClass().getField("COD_PAY").get(cashierPage).toString().replace("{amount}", txnAmount), "COD message doesn't match");
    }

    @Parameters({"theme"})
    @Test(description = "Verify message <You'll receive an automated call from us to confirm this order. And at the time of delivery, Rs 11.0 will have to be given to the courier boy> on COD when wallet balance = 0")
    public void COD_VerifyMessageWithBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String txnAmount = "11.0";
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.COD, theme, user)
                .setTXN_AMOUNT(txnAmount).build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCOD().click();
        Assert.assertEquals(cashierPage.getTextCODMessage().getText(), cashierPage.getClass().getField("COD_PAY").get(cashierPage).toString().replace("{amount}", txnAmount), "COD message doesn't match");
    }

    @Parameters({"theme"})
    @Test(description = "Verify that user is able to complete the transaction if sufficient balance is in wallet.")
    public void PPI_S_whenCODEnabledOnMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGONLY_COD_PEON_DISABLED, theme, user)
                .setTXN_AMOUNT("11.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateBankName("WALLET")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that all enabled payment options appearing on cashier page when COD is not passed on checkout page and COD is enabled on merchant.")
    public void COD_verifyllPayModesOnCashier(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.EMI_DISCOVERY, theme, user)
                .setTXN_AMOUNT("11.00")
                .setPAYMENT_TYPE_ID("")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabNetBanking().assertVisible();
        cashierPage.tabCOD().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful COD transaction with sufficient wallet balance and then uncheck wallet and complete transaction using COD.")
    public void COD_S_withSufficientBalance(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGONLY_COD_PEON_DISABLED, theme, user)
                .setTXN_AMOUNT("11.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCOD().assertVisible();
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(Constants.PayMode.COD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("CODMOCK")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that if a user is able to perform a successful txn through Credit Card when COD is enabled for merchant")
    public void CC_S_whenCODisEnabled(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGONLY_COD_PEON_DISABLED, theme, user)
                .setTXN_AMOUNT("17.00").build();
        Double CODAmount = 6.00;
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - CODAmount;
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - CODAmount);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.parseDouble(orderDTO.getTXN_AMOUNT()) - CODAmount))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Verify that a user is able to perform a successful txn through Saved Card when COD is enabled for merchant")
    public void SC_S_whenCODisEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.COD, theme, user)
                .setTXN_AMOUNT("17.00").build();
        Double CODAmount = 6.00;
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - CODAmount;
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - CODAmount);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2024", paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFCSC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.parseDouble(orderDTO.getTXN_AMOUNT()) - CODAmount))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that COD option should not be visible to user if the transaction amount is below COD threshold value i.e 5.00")
    public void COD_F_verifyCODNotVisiblewhenTxnAmountIsBelowThresoldAmount(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.COD, theme, user)
                .setTXN_AMOUNT("1.00").build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCOD().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that COD option should be visible to user if transaction amount equals to COD threshold amount i.e. 5.00")
    public void COD_F_verifyCODVisiblewhenTxnAmountIsMoreThanThresoldAmount(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGONLY_COD_PEON_DISABLED, theme, user)
                .setTXN_AMOUNT("5.00").build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCOD().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate EMI Txn fail and retry with COD")

    public void validateEMIRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("11.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectPaymentDTO = new PaymentDTO()
                .setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN)
                .setBankName("HDFC Bank")
                .setMonth(6);
        cashierPage.payBy(Constants.PayMode.EMI, incorrectPaymentDTO);
        cashierPage.waitUntilLoads();

        cashierPage.scrollTo(0);

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate CC Txn fail and retry with COD")
    public void validateCCRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("99.95").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();

        cashierPage.scrollTo(0);

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate DC Txn fail and retry with COD")
    public void validateDCRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("99.95").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.waitUntilLoads();

        cashierPage.scrollTo(0);

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate UPI Txn fail and retry with COD")
    public void validateUPIRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("99.86").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate NB Txn fail and retry with COD")
    public void validateNBRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("99.99").build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.scrollTo(0);
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate PPBL Txn fail and retry with COD")
    public void validatePPBLRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PPBL_PAYTMCC_VPA, theme, user)
                .setTXN_AMOUNT("66.8").build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.scrollTo(0);
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Direct Bank Txn fail and retry with COD")
    public void validateDirectBankRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.COD(Constants.MerchantType.NATIVE_HDFO, theme, user)
                .setTXN_AMOUNT("11.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().click();
        directBankOTPPage.modalCancelPayment().accept();
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Postpaid Txn fail and retry with COD")
    public void validatePostpaidRetryTxnUsingCOD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC, Label.POSTPAID);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        PostpaidHelpers.updateBalance("100");
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("9.0").build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.scrollTo(0);
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.COD);
        cashierPage.waitUntilLoads();
        validateCODSuccessResponse(orderDTO);

    }

        private void validatePeon(TxnStatus txnStatus) {
        Peon peon = new Peon(txnStatus.txnStatusResponse.getORDERID());
        peon.executeUntilGetResponse();
        JsonPath jsonPath = peon.response().jsonPath();

        ObjectMapper mapper = new ObjectMapper();
        Map txnStatusMap, peonMap = null;

        txnStatusMap = mapper.convertValue(txnStatus.txnStatusResponse, Map.class);
        peonMap = jsonPath.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(peonMap.get("GATEWAYNAME").toString()).isEqualToIgnoringCase(txnStatusMap.get("GATEWAYNAME").toString());
        softly.assertThat(peonMap.get("MID")).isEqualTo(txnStatusMap.get("MID"));
        softly.assertThat(peonMap.get("ORDERID")).isEqualTo(txnStatusMap.get("ORDERID"));
        softly.assertThat(peonMap.get("PAYMENTMODE")).isEqualTo(txnStatusMap.get("PAYMENTMODE"));
        softly.assertThat(peonMap.get("RESPCODE")).isEqualTo(txnStatusMap.get("RESPCODE"));
        softly.assertThat(peonMap.get("RESPMSG")).isEqualTo(txnStatusMap.get("RESPMSG"));
        softly.assertThat(peonMap.get("STATUS")).isEqualTo(txnStatusMap.get("STATUS"));
        softly.assertThat(peonMap.get("TXNAMOUNT")).isEqualTo(txnStatusMap.get("TXNAMOUNT"));
        softly.assertThat(peonMap.get("TXNID")).isEqualTo(txnStatusMap.get("TXNID"));
        softly.assertAll();
    }

    private void validateCODSuccessResponse(OrderDTO orderDTO) {
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateCurrency("INR")
                .validateGatewayName("CODMOCK")
                .validateRespMsg("Txn Success")
                .validateBankName("CODMOCK")
                .validatePaymentMode("COD")
                .validateMid(orderDTO.getMID())
                .validateRespCode("01")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("CODMOCK")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        validatePeon(txnStatus);
    }
}
