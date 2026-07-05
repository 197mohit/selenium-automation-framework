package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ResponseCode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner("Deepak")
public class AddNPay extends PGPBaseTest implements IAddMoney {

    private static final String ADDnPAY_LIMIT_MSG = "With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges.";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    @Parameters({"theme"})
    @Test(description = "Validate successful add & pay via CC Txn.", groups = {"smoke"})
    public void addnPay_CC_S(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful add & pay transaction when there is zero balance in wallet.")
    public void addnPay_CC_S_ZeroBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, 0.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify txn failure in case of cancellation of add and pay transaction at 3D secure page.")
    public void addnPay_CC_F_3DPageCancel(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).setTXN_AMOUNT("100.99").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 99.99);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify Txn failure in case of Credit Card Cancel on Cashier Page", enabled = false)
    public void addnPay_CC_F_CashierPageCancel(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.linkPGCancel().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify Txn failure in case of Debit Card Cancel on Cashier Page", enabled = false)
    public void addnPay_DC_F_CashierPageCancel(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.linkPGCancel().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that save cards are getting saved using add and pay functionality.")
    public void addnPay_CC_S_CheckforCardSaved(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC_WITH_SAVECARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
        orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        cashierPage.assertSavedCardVisibility();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful add & pay transaction using saved card.")
    public void addnPay_SC_S(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful sufficient wallet balance transaction.")
    public void addnPay_PPI_S_sufficientBal(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Verify add & pay transaction using NB payment options.")
    public void addnPay_NB_S(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful add and pay transaction using DC payment options.")
    public void addnPay_DC_S(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful transaction with SSOToken login.")
    public void addnPay_CC_S_withSSOToken(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("2.00")
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful transaction without log in using PG payment modes available for merchant.")
    public void addnPay_CC_S_loginOnCashier(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("1.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify failed transaction having zero as transaction amount")
    public void addnPay_CC_F_ZeroAmount(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("0.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId("")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("308")
                .validateRespMsg("Invalid amount.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT());
    }

    @Parameters({"theme"})
    @Test(description = "Verify failed transaction with negative or invalid transaction amount")
    public void addnPay_CC_F_NegativeAmt(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("-1.00")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId("")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("308")
                .validateRespMsg("Invalid amount.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT());
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful transaction by loging as another user.")
    public void addnPay_DC_S_LoginWithDiffUserOnCashierPage(@Optional("enhancedweb") String theme) throws Exception {
        User user1 = userManager.getForWrite(Label.BASIC);
        User user2 = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user1);
        cashierPage.btnLogout().click();
        user1.purge();
        WalletHelpers.modifyBalance(user2, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        cashierPage.login(user2);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
    }

    @Parameters({"theme"})
    @Test(description = "Verify wallet balance after successful payment of add n Pay")
    public void addnPay_DC_S_VerifyWalletBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Verify the payment mode on cashier page before login")
    public void addnPay_verifyPaymentModeOnCashierPageBeforeLogin(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabNetBanking().assertVisible();
        cashierPage.tabUPI().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the successful add n Pay payment without login")
    public void addnPay_DC_S_withoutLogin(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner("Deepak")
    @Issue("PGP-9532")
    @Parameters({"theme"})
    @Test(description = "Verify cancellation of transaction when retry count reached for merchant")
    public void addnPay_Retry_F_WhenRetryCountBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("100.99")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 99.99);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.DC);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.DC);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner("Deepak")
    @Issue("PGP-9532")
    @Parameters({"theme"})
    @Test(description = "Verify number of transaction retry on the basis of retrial counts configured for merchant")
    public void addnPay_Retry_VerifyRetry(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("100.99")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.checkBoxPPI().isSelected()).isTrue();
        cashierPage.payBy(PayMode.DC);
        cashierPage.waitUntilLoads();
        /*As retry count is set as 2 so, after first txn denial from bank, user is redirected to cashier page so, retry info msg should be displayed*/
            /*cashierPage.notificationContainer().assertContainsText("Your payment has been declined by your bank. Please contact your bank for" +
                    " any queries. If money has been deducted from your account, your bank will inform us within" +
                    " 48 hrs and we will refund the same");*///TODO uncomment once mock is modified to mimic the scenario where this notification msg will be displayed
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        Assertions.assertThat(cashierPage.checkBoxPPI().isSelected()).isTrue();
        cashierPage.payBy(PayMode.DC);
        cashierPage.waitUntilLoads();
        /*As retry count is set as 2 so, after first txn denial from bank, user is redirected to cashier page so, retry info msg should be displayed*/
            /*cashierPage.notificationContainer().assertContainsText("Your payment has been declined by your bank. Please contact your bank for" +
                    " any queries. If money has been deducted from your account, your bank will inform us within" +
                    " 48 hrs and we will refund the same");*/
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        Assertions.assertThat(cashierPage.checkBoxPPI().isSelected()).isTrue();
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        /*As retry count is set as 2 so, after second txn denial from bank, user should be redirected to response page and txn should fail*/
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_FAILURE")
                .validateGatewayName("WALLET")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful withdraw transaction using wallet with insufficient wallet balance in case of wallet only.")
    public void PGP_114_successfulAddNPayWithWalletPaymentModeInCheckoutPage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setPAYMENT_TYPE_ID("WALLET")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Verify the Online browser response sent back to a merchant when add n pay is failed")
    public void addnPay_CC_F_verifyResponsePage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("100.98")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 99.98);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textRespCode().getText()).isEqualTo("227");
        Assertions.assertThat(responsePage.textRespMsg().getText()).isEqualTo("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(responsePage.textTXNAMOUNT().getText()).isEqualTo(orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the Online browser response sent back to a merchant when add n pay is pending.")
    public void addnPay_CC_P_verifyResponsePage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).setTXN_AMOUNT("100.84").build();
        WalletHelpers.modifyBalance(user, 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("pending")
                .validatePaymentMode("PPI")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the response of TXNSTATUS_LIST API for add n pay success")
    public void addnPay_DC_S_verifyTxnStatusList(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay_PG2_RTDD, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        Response txnStatusListResp = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), "ALL");
        JsonPath jsonPath = txnStatusListResp.jsonPath();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNID[0]")).isNotEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.BANKTXNID[0]")).isNotEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.ORDERID[0]")).isNotEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNAMOUNT[0]")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.STATUS[0]")).isEqualToIgnoringCase("TXN_SUCCESS");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNTYPE[0]")).isEqualToIgnoringCase("SALE");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.GATEWAYNAME[0]")).isEqualToIgnoringCase("WALLET");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.RESPCODE[0]")).isEqualToIgnoringCase("01");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.RESPMSG[0]")).isEqualToIgnoringCase("Txn Successful.");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.BANKNAME[0]")).isEqualToIgnoringCase("WALLET");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.MID[0]")).isEqualToIgnoringCase(orderDTO.getMID());
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.PAYMENTMODE[0]")).isEqualToIgnoringCase("PPI");
        softAssertions.assertThat(jsonPath.getDouble("TXN_LIST.REFUNDAMT[0]")).isZero();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNDATE[0]")).isNotEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.REFUNDID[0]")).isEmpty();
        softAssertions.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the response of TXNSTATUS_LIST API for add n pay failure")
    public void addnPay_DC_F_verifyTxnStatusList(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).setTXN_AMOUNT("100.99").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 99.99);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        Response txnStatusListResp = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), "ALL");
        JsonPath jsonPath = txnStatusListResp.jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNID[0]")).isNotEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.BANKTXNID[0]")).isEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.ORDERID[0]")).isNotEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNAMOUNT[0]")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.STATUS[0]")).isEqualToIgnoringCase("TXN_FAILURE");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNTYPE[0]")).isEqualToIgnoringCase("SALE");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.GATEWAYNAME[0]")).isEqualToIgnoringCase("WALLET");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.RESPCODE[0]")).isEqualToIgnoringCase("810");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.RESPMSG[0]")).isEqualToIgnoringCase("ORDER IS CLOSE.");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.BANKNAME[0]")).isEqualToIgnoringCase("WALLET");
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.MID[0]")).isEqualToIgnoringCase(orderDTO.getMID());
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.PAYMENTMODE[0]")).isEqualToIgnoringCase("PPI");
        softAssertions.assertThat(validateRefundAmount(jsonPath.getString("TXN_LIST.REFUNDAMT[0]"), "0.00"));
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.TXNDATE[0]")).isNotEmpty();
        softAssertions.assertThat(jsonPath.getString("TXN_LIST.REFUNDID[0]")).isEmpty();
        softAssertions.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful add n pay transaction using UPI.")
    public void addnPay_UPI_S(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify VPA is getting saved for successful txn using UPI.", enabled = false)
    public void PGP_124_validateSaveUPI(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
        orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).build();
        new CheckoutPage().createOrder(orderDTO);
        cashierPage.login(user);
        cashierPage.tabSavedCard().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "To verify saved card list appearing for merchant after login")
    public void VerifySaveCardListOnCashierAfterLogin(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String card1 = "4109139965359183";
        String card2 = "5507032420388415";
        SavedCardHelpers.addCard(user, "06", "2022", card1);
        SavedCardHelpers.addCard(user, "07", "2023", card2);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.verifyCardDisplayed(card1, card2);
    }

    private Boolean validateRefundAmount(String actualRefundAmt, String expectedRefundAmt) {
        return Double.parseDouble(actualRefundAmt) == Double.parseDouble(expectedRefundAmt);
    }

    @Parameters({"theme"})
    @Test(description = "Validate add & pay txn should not be allowed when limit is already reached before user logged in", groups = {"regression", Group.Status.TO_BE_FIXED})
    public void addnPay_DC_F_whenlimitBreachedBeforeLogin(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        try {
            OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
            new CheckoutPage().createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            WalletHelpers.breachAddMoneyLimit(user);
            cashierPage.notificationContainer().assertContainsText("Transaction not allowed using Paytm Wallet for this amount due to RBI guidelines. Please pay using other payment modes.");
        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate add & pay Txn Failure, when limit is already reached before clicking paynow button", groups = {"smoke", "regression", Group.Status.TO_BE_FIXED})
    public void addnPay_DC_F_whenlimitBreachedBeforePay(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        try {
            OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
            new CheckoutPage().createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            WalletHelpers.breachAddMoneyLimit(user);
            cashierPage.payBy(PayMode.DC);
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_FAILURE")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("227")
                    .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                    .validateBankName("WALLET")
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }
    }

    @Parameters({"theme"})
    @Test(description = "To verify Add n Pay with Login for PPBL")
    public void addnPay_PPBL_S(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).setTXN_AMOUNT("10").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 4.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL);
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
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Add n pay transaction with IDBI NB")
    public void transactionwithIDBINB(@Optional("enhancedweb") String theme) throws Exception {

        double txnAmount = 5.00;
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchant = MerchantType.AddnPay;
        WalletHelpers.modifyBalance(user, txnAmount - 2.00);
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchant, theme, user).setTXN_AMOUNT(String.valueOf(txnAmount)).build();
        new CheckoutPage().createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("IDBI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
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
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Epic(Constants.Sprint.SPRINT32_2)
    @Story("PGP-21561")
    @Parameters({"theme"})
    @Test(description = "test txn amt in callback and txn status has 2 decimal precision")
    public void testTxnAmtInCallbackAndTxnStatusHas2DecimalPrecision(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();

        String txnAmt = "100";
        String formattedTxnAmt = new DecimalFormat("0.00").format(Double.parseDouble(txnAmt));
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmt)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        new CheckoutPage().createOrder(orderDTO);
        cashierPage.payBy(PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        pageWait.apply(responsePage.hasLoaded());
        Assertions.assertThat(responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.TXNAMOUNT).getValue()).isEqualTo(formattedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.getTXNAMOUNT()).isEqualTo(formattedTxnAmt);
    }

    ////////////////// Fee on ADD Money  //////////////////

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    @Parameters({"theme"})
    public void validateFullKYCWalletCC(@Optional("enhancedweb") String theme) throws Exception {
        String txnAmount = "10002";
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(PayMode.CC,paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Override
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCWalletSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10002";
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.modifyBalance(user, 1.0);

        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "100";
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


/*    @Test(enabled = false)
    @Override */
    public void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //Already covered
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10002";
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();

        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10002";
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(PayMode.CC,paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10002";
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

/*    @Test(enabled = false)
    @Override */
    public void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
      //Already covered
    }


    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10002";
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO1 = new PaymentDTO();
        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO1.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10002";
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }
 /*
    @Test(enabled = false)
    @Override */
    public void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //No need
    }
 /*   @Test(enabled = false)
    @Override */
    public void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //No Need
    }


    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10002";
        User user = userManager.getForWrite(Label.BASICTOKYC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Feature("PGP-20695")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-25565")
    @Test(description = "To verify wallet is disabled and txn should get success with other paymode for add n pay txn when wallet limit is breached")
    public void verifyAddNPayWalletLimitBreached(@Optional("enhancedweb") String theme) throws Exception {

        String txnAmount = "100000001"; // Wallet Limit Breached
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay,theme , user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().assertUnChecked();//Wallet should be unchecked
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success") //Payment should be success with other payMode
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

 ///////////////////////////Add N Pay Saved Card ///////////////////////////// theia.sendMerchantIdInAddNPayLitePayViewTask///

    @Parameters({"theme"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "PG Side : For addNPay Flow : when we login at cashierPage, mid cards are displayed first, after login all cards of userid and custid are displayed, if flag theia.sendMerchantIdInAddNPayLitePayViewTask is enabled")
    public void loginAtCashierPageToCheckMIDUserIdCardsPGSide(@Optional("enhancedweb") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        MerchantType addNPayMerchant = MerchantType.AddnPay;
        SavedCardHelpers.assertStoreCardPrefEnabled(addNPayMerchant);

        prerequisite: {

            //MID/CustId
            FF4JFlags.disable("shortCircuitSavedCardServiceReadForMidCustId");
            FF4JFlags.disable("fetchSavedcardFromPlatformForMidCustId");
            FF4JFlags.disable("returnSavedCardsFromPlatformForMidCustId");

            //UserId
            FF4JFlags.disable("shortCircuitSavedCardServiceReadForUserId");
            FF4JFlags.disable("fetchSavedcardFromPlatformForUserId");
            FF4JFlags.disable("returnSavedCardsFromPlatformForUserId");

        }

        User user = userManager.getForWrite(Label.SAVECARDMIGRATION,Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(addNPayMerchant, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Deleting for user on PG side
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for user on PG side
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Adding for MID/CustId on PG side
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(),orderDTO.getCUST_ID(),addNPayMerchant.getId(),paymentDTO.getExpMonth()+paymentDTO.getExpYear());

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.login(user);
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();//Should be visible even after login

        cashierPage.checkBoxPPI().unCheck();

        //After unchecking wallet, saved cards still should be visible
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

        cashierPage.checkBoxPPI().check();

        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO,paymentDTO.getCreditCardNumber()); //Paying through saved card on MID CustId
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);

    }

    @Parameters({"theme"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "P + Side Enhanced: For addNPay Flow : when we login at cashierPage, mid cards are displayed first, after login all cards of userid and custid are displayed, if flag theia.sendMerchantIdInAddNPayLitePayViewTask is enabled")
    public void loginAtCashierPageToCheckMIDUserIdCardsAlipay(@Optional("enhancedweb") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        MerchantType addNPayMerchant = MerchantType.AddnPay;
        SavedCardHelpers.assertStoreCardPrefEnabled(addNPayMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();

        User user = userManager.getForWrite(Label.SAVECARDMIGRATION, Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(addNPayMerchant, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(addNPayMerchant.getId(),custId);

        //Adding for MID/CustId on P+ side
        SavedCardHelpers.addCardAlipay(addNPayMerchant.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.login(user);
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();//Should be visible even after login

        cashierPage.checkBoxPPI().unCheck();

        //After unchecking wallet, saved cards still are visible
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

        cashierPage.checkBoxPPI().check();

        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO,paymentDTO.getCreditCardNumber()); //Paying through saved card on MID CustId
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);

    }

    @Parameters({"theme"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "P+ Enhanced : For addNPay Flow : Check the cards displayed when logged in flow and pref is off")
    public void storePrefOffAddPay(@Optional("enhancedweb") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        MerchantType addNPayMerchantDisabledStoreCard = MerchantType.FOOD_MERCHANT_ADDNPAY;
        SavedCardHelpers.assertStoreCardPrefDisabled(addNPayMerchantDisabledStoreCard);

        SavedCardHelpers.enableAllSavedCardFlags();

        User user = userManager.getForWrite(Label.SAVECARDMIGRATION, Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(addNPayMerchantDisabledStoreCard, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(addNPayMerchantDisabledStoreCard.getId(),custId);

        //Adding for MID/CustId on P+ side
        SavedCardHelpers.addCardAlipay(addNPayMerchantDisabledStoreCard.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertNotVisible();
        cashierPage.login(user);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertNotVisible();
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO,paymentDTO.getDebitCardNumber()); //Paying through saved card on UserId
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }


    @Parameters({"theme"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "P+ Enhanced : For addNPay Flow : Check the cards on logging out on cashier page")
    public void logoutCashierPage(@Optional("enhancedweb") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        MerchantType addNPayMerchantDisabledStoreCard = MerchantType.AddnPay;
        SavedCardHelpers.assertStoreCardPrefEnabled(addNPayMerchantDisabledStoreCard);

        SavedCardHelpers.enableAllSavedCardFlags();

        User user = userManager.getForWrite(Label.SAVECARDMIGRATION, Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(addNPayMerchantDisabledStoreCard, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(addNPayMerchantDisabledStoreCard.getId(),custId);

        //Adding for MID/CustId on P+ side
        SavedCardHelpers.addCardAlipay(addNPayMerchantDisabledStoreCard.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.login(user);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

        cashierPage.logout(user);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertNotVisible();

    }

    @Parameters({"theme"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "P+ Enhanced : For addNPay Flow : Check the cards on case of retry txn")
    public void retryAddPayPPlus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        MerchantType addNPayMerchantDisabledStoreCard = MerchantType.AddnPay_Retry;
        SavedCardHelpers.assertStoreCardPrefEnabled(addNPayMerchantDisabledStoreCard);

        SavedCardHelpers.enableAllSavedCardFlags();

        User user = userManager.getForWrite(Label.SAVECARDMIGRATION, Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(addNPayMerchantDisabledStoreCard, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(addNPayMerchantDisabledStoreCard.getId(),custId);

        //Adding for MID/CustId on P+ side
        SavedCardHelpers.addCardAlipay(addNPayMerchantDisabledStoreCard.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

        cashierPage.login(user);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);

        cashierPage.payBy(PayMode.CC,paymentDTO);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();
        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

    }



    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success ADDPAY CC Corporate card on Corporate Card ADDNPAY Merchant",groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateAddPayCCBinCorporateMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType corporateMerchant = MerchantType.AddnPay;
        CorporateHelpers.assertCorporateCardCC(corporateMerchant.getId());

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 2.0;

        WalletHelpers.modifyBalance(user,txnAmount-1.0);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0,6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC,paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO,corporateMerchant,"PPI", "WALLET");

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO,"PPI", "WALLET");

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO,"PPI", "WALLET", "WALLET");
    }


    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success ADDPAY DC Corporate card on Corporate Card ADDNPAY Merchant",groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateAddPayDCBinCorporateMerchant(@Optional("enhancedweb") String theme) throws Exception {

        MerchantType corporateMerchant = MerchantType.AddnPay;
        CorporateHelpers.assertCorporateCardDC(corporateMerchant.getId());

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 2.0;

        WalletHelpers.modifyBalance(user,txnAmount-1.0);

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC,paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO,corporateMerchant,"PPI", "WALLET");

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO,"PPI", "WALLET");

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO,"PPI", "WALLET", "WALLET");
    }

    @Parameters({"theme"})
    @Test(description = "test PPBL pay mode is disabled when user has insufficient PPBL balance")
    public void testPPBLPayModeIsDisabledWhenUserHasInsufficientPPBLBalance(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
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
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        Assertions.assertThat(cashierPage.tabPPBL().content().toString()).as("Getting insufficient PPBL balance msg").contains("You do not have enough balance for this payment");
    }

 //   @Issue("PGP-29758")
 //   @Parameters({"theme"})
 //   @Test(enabled = false, description = "test next pay mode is selected when user has insufficient PPBL balance")
    public void testNextPayModeIsSelectedWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        WebElement payModeNextToPPBL = DriverManager.getDriver().findElement(By.cssSelector("section.p-option[data-key=ppb] + section"));
        Assertions.assertThat(payModeNextToPPBL.getAttribute("class").contains("active")).as("Paymode next to PPBL is selected").isTrue();
    }

    @Feature("PGP-35219")
    @Owner(Constants.Owner.GAURAV)
    @Parameters({"theme"})
    @Test(description = "Validate dynamic limit of addnPay via CC")
    public void validateAddnPayViaCCDynamicLimit(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("15000")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        Assertions.assertThat(cashierPage.getAddnPayLimitError().getText()).contains(ADDnPAY_LIMIT_MSG);

        String paymentDecisionMaker =  "grep \"PAYMODE_DECISION_MAKER_TASK" +"\" /paytm/logs/theia_facade.log" + " | grep \"RESPONSE\"" + " | grep \"rejectMsg\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, paymentDecisionMaker);
        Assertions.assertThat(theiaFacadeLogs).contains(ADDnPAY_LIMIT_MSG);
    }

    @Feature("PGP-35219")
    @Owner(Constants.Owner.GAURAV)
    @Parameters({"theme"})
    @Test(description = "Validate dynamic limit of addnPay via CC and do successful txn")
    public void validateAddnPayViaCCDynamicLimit_successTxn(@Optional("enhancedwap_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("15000")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        Assertions.assertThat(cashierPage.getAddnPayLimitError().getText()).contains(ADDnPAY_LIMIT_MSG);

        String paymentDecisionMaker =  "grep \"PAYMODE_DECISION_MAKER_TASK" +"\" /paytm/logs/theia_facade.log" + " | grep \"RESPONSE\"" + " | grep \"rejectMsg\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, paymentDecisionMaker);
        Assertions.assertThat(theiaFacadeLogs).contains(ADDnPAY_LIMIT_MSG);

        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.validateRespCode("200");
        responsePage.validateRespMsg("Txn Success");
    }

    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm postpaid and wallet using merchant NOT having postpaid and wallet(SCW) merchant having postpaid paymode ")
    public void SuccessfulAddNPayPostpaidTxnForMIDNotHavingPostapidPaymode(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        PostpaidHelpers.updateBalance("2");
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PG2_COP, theme, user)
                .setTXN_AMOUNT("3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Postpaid paymode should not be available since merchant doesn't have postapid acquiring and wallet merchant has postpaid")
    public void postpaidNotAvailableWhenDeselectedWallet(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("2");
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("2")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        wallet isn't autometically selected jira: https://jira.mypaytm.com/browse/PGP-41941
//        cashierPage.checkBoxPPI().unCheck();
        Assertions.assertThat(cashierPage.isPPIChecked()).isFalse().as("Wallet not checked");
        cashierPage.radioButtonPaytmPostpaid().assertNotVisible();
    }


    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm postpaid and wallet using merchant having postpaid and wallet(SCW) merchant having postpaid paymode ")
    public void SuccessfulAddNPayPostpaidTxnForMIDAndWalletMIDHavingPostapidPaymode(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("2");
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
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
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Owner(MAYURI)
    @Feature("PGP-33928")
    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm postpaid when deselecting wallet")
    public void SuccessfulPostpaidTxnForWalletDeselected(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("10");
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("2")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        Wallet isn't autometically selected JIRA: https://jira.mypaytm.com/browse/PGP-41941
//        cashierPage.checkBoxPPI().unCheck();
        Assertions.assertThat(cashierPage.isPPIChecked()).isFalse().as("Wallet not checked");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PAYTMCC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("Paytm Postpaid")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.ROHIT)
    @Feature("PGP-33592")
    @Parameters({"theme"})
    @Test(description = "for add and pay txn parameters should be passed in metadata in the bank request isAddPayService, addPayServicMerchantOrderID, addPayServicMerchantID, title")
        public void verifyParametersPassesInMetadata(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).setTXN_AMOUNT("2").build();
        WalletHelpers.modifyBalance(user,1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.radioButtonWallet().select();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmd = "grep 'Request String For Digital Credit Payment for customerId' /paytm/logs/instaproxy.log | grep "+orderDTO.getORDER_ID()+"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("metadata");
        // As confirmed with Insta dev changes have been done in PGP-37201 and now ADD_PAY will come instead of isAddPayService
        Assertions.assertThat(logs).contains("ADD_PAY");
        Assertions.assertThat(logs).contains("addPayServicMerchantOrderID");
        Assertions.assertThat(logs).contains("addPayServicMerchantID");
        Assertions.assertThat(logs).contains("title");

    }
    @Owner(Constants.Owner.ROHIT)
    @Feature("PGP-33592")
    @Parameters({"theme"})
    @Test(description = "for normal postpaid txn no parameters should be passed in bank request ")
    public void verifyParametersnotPassedInMetadata(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).setTXN_AMOUNT("2").build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateBankName("PAYTMCC")
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmd = "grep 'Request String For Digital Credit Payment for customerId' /paytm/logs/instaproxy.log | grep "+orderDTO.getORDER_ID()+"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("metadata\":null");

    }

    @Owner(GAURAV)
    @Feature("PGP-36144")
    @Parameters({"theme"})
    @Test(description = "Verify successful addnPay txn via saved VPA upto 2k")
    public void verifyAddnPayviaSavedVPA2k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        WalletHelpers.modifyBalance(user, 50.00);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.UPICollectSavedVPA, theme, user).setTXN_AMOUNT("2050").build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
        Assertions.assertThat(cashierPage.upiPushSection().isElementPresent()).isTrue();
        cashierPage.payBy(PayMode.SAVED_UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateBankName("WALLET")
                .validateTxnAmount("2050")
                .validateRespCode("01")
                .assertAll();
    }

    @Owner(GAURAV)
    @Feature("PGP-36144")
    @Parameters({"theme"})
    @Test(description = "Verify BHIM UPI not shown for addnPay txn via saved VPA greater 2k but shown when wallet is not selected")
    public void verifyAddnPayviaSavedVPAgreater2k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        WalletHelpers.modifyBalance(user, 50.00);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.UPICollectSavedVPA, theme, user).setTXN_AMOUNT("2100").build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.upiPushSection().isElementPresent()).isFalse();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.checkBoxPPI().click();
        cashierPage.checkBoxPPI().assertUnChecked();
        cashierPage.tabUPI().assertVisible();
        Assertions.assertThat(cashierPage.upiPushSection().isElementPresent()).isTrue();
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateTxnAmount("2100")
                .validateRespCode("01")
                .assertAll();
    }
    @Owner(Amanpreet)
    @Feature("PGP-40763")
    @Parameters({"theme"})
    @Test(description = "verify for AddnPay merchants the remaining pay options are available when user has insufficient wallet balance")
    public void PGP_40763_TC_05_validateWallet_NotSelected(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        double txnAmt = 100;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmt))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        double balance = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 10.00;
        WalletHelpers.modifyBalance(user, balance);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WebElement walletBalance = DriverManager.getDriver().findElement(By.xpath("//*[@type='checkbox']"));
        WebElement payModeNextToPPBL = DriverManager.getDriver().findElement(By.cssSelector("section[id=ptm-ppb]"));
        Assertions.assertThat(walletBalance.isSelected()).isTrue();
        Assertions.assertThat(payModeNextToPPBL.getAttribute("class").contains("active")).as("Paymode next to PPBL is selected").isTrue();

    }
    @Owner("Shubham Soni")
    @Feature("PGP-40704")
    @Parameters({"theme"})
    @Test(description = "Validate successful add & pay via CC Txn greater than wallet limit specified.")
    public void addnPay_CC_Limit(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            //FF4J flag is on : theia.enableAddMoneyFeeOnAddnPayTxn
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.ADDNPAY_LIMIT.getId(), "disableLimitCCAddNPay", "Y");
        }
        User user = userManager.getForWrite(Label.WALLETLIMIT);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.ADDNPAY_LIMIT, theme, user).setTXN_AMOUNT("11000").build();
        WalletHelpers.modifyBalance(user, 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
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
                .assertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.ADDNPAY_LIMIT.getId() + "\" | grep \"RESPONSE\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\"msg\":\"Your bank/payment network charges a fee on using Credit card to add money to Wallet. Please use UPI or Debit card option to add money free of cost. To know more, visit https://www.paytmbank.com/ratesCharges.\",\"rejectMsg\":\"With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card\"");
        int count = 0;
        String str = "https://wallet-pgp.paytm.in/wallet-web/walletLimits";

        if (theiaFacadeLogs.isEmpty() || str.isEmpty()) {
            count = 0;
        }
        int index = 0;
        while (true) {
            index = theiaFacadeLogs.indexOf(str, index);
            if (index != -1) {
                count++;
                index += str.length();
            } else {
                break;
            }
        }
        Assertions.assertThat(count).isEqualTo(1);
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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }
    @Owner(PAREEKSHITH)
    @Feature("PGP-42947")
    @Parameters({"theme"})
    @Test(description = "Verify successful payment for <1 rs of Add N Pay transaction")
    public void successfullLessthan1rupeeAddNPayTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.AddnPay_PG2_Refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay_PG2_Refund, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 0.50);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.50);
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-42947")
    @Parameters({"theme"})
    @Test(description = "Verify add money <1 rupee UI message")
    public void verifyUiMessageLessthan1rupeeAddNPayTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.AddnPay_PG2_Refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay_PG2_Refund, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 0.50);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.addnpayInfoIcon().click();
        Assertions.assertThat(cashierPage.addnpayInfoText().getText()).isEqualTo("As bank allows minimum ₹1 payment, to complete the payment ₹1 need to be added to your Paytm wallet.");
        cashierPage.addnpayInfoButton().click();
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.50);
    }
}
