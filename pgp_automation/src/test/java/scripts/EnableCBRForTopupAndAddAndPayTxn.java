package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ResponseCode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.*;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;


public class EnableCBRForTopupAndAddAndPayTxn extends PGPBaseTest {

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Validate successful add & pay via CC Txn and hit routing engine and validate addAndPay=true.", groups = {"smoke"})
    public void addnPay_CC_S(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
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
        WalletHelpers.validateBalance(user, 0.00);

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify successful add & pay transaction using saved card and hit routing engine and validate addAndPay=true.")
    public void addnPay_SC_S(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify add & pay transaction using NB payment options and hit routing engine and validate addAndPay=true.")
    public void addnPay_NB_S(@Optional("enhancedweb_revamp") String theme) throws Exception {
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

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "To verify successful add and pay transaction using DC payment options and hit routing engine and validate addAndPay=true.")
    public void addnPay_DC_S(@Optional("enhancedweb_revamp") String theme) throws Exception {
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

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Validate successful transaction with SSOToken login and hit routing engine and validate addAndPay=true.")
    public void addnPay_CC_S_withSSOToken(@Optional("enhancedweb_revamp") String theme) throws Exception {
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

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

/*    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify Txn failure in case of Credit Card Cancel on Cashier Page and hit routing engine and validate addAndPay=true", enabled = false) */
    public void addnPay_CC_F_CashierPageCancel(@Optional("enhancedweb_revamp") String theme) throws Exception {
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

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

/*    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify Txn failure in case of Debit Card Cancel on Cashier Page and hit routing engine and validate addAndPay=true", enabled = false) */
    public void addnPay_DC_F_CashierPageCancel(@Optional("enhancedweb_revamp") String theme) throws Exception {
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

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify successful sufficient wallet balance transaction and hit routing engine and validate addAndPay=true.")
    public void addnPay_PPI_S_sufficientBal(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
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

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify successful express addmoney txn when payment is done via CC and hit routing engine and validate addAndPay=true")
    public void successfulAddMoneyCC1() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "CC", user, paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify successful express addmoney txn when payment is done via DC and hit routing engine and validate addAndPay=true")
    public void successfulAddMoneyDC1() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "DC", user, new PaymentDTO()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36640")
    @Parameters({"theme"})
    @Test(description = "Verify successful express addmoney txn when payment is done via mastero card and hit routing engine and validate addAndPay=true")
    public void successfulAddMoney_Maestro1() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "DC", user, paymentDTO).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" |grep \"addAndPay=true\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        System.out.println(theialogsrequest);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }


    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify Received Hit To Process Routing Engine from theia to routing-engine in TopUp CC flow")
    public void RequestToProcessRoutingEngineTopUpCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.CBR_MID, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();

        String theiaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString());
        Assertions.assertThat(theiaLogs).contains("Received Hit To Process Routing Engine");

        String theiaLogsCBRRequest=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString(),"Received Hit To Process Routing Engine" );
        Assertions.assertThat(theiaLogs).contains("FetchCostAcquirerInfoRequestBody");
    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify cost effective channel response from Routing to theia in TopUp CC flow")
    public void ResponseToProcessRoutingEngineTopUp(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.CBR_MID, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();

        String theiaLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString());
        Assertions.assertThat(theiaLogs).contains("Response returned from routing Engine");
        Assertions.assertThat(theiaLogs).contains("FetchCostAcquirerInfoResponseBody");
    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify channelPreference is sent in FUND_ORDER_PAY request for TopUp CC flow")
    public void channelPreferenceInFUND_ORDER_PAYRequestTopUpFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.CBR_MID, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();

        String theiaFacadeLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID().toString(),"FUND_ORDER_PAY");
        Assertions.assertThat(theiaFacadeLogs).contains("channelPreference");

    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify Received Hit To Process Routing Engine from theia to routing-engine in TopUp DC flow")
    public void RequestToProcessRoutingEngineDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.CBR_MID, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        String theiaLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString());
        Assertions.assertThat(theiaLogs).contains("Received Hit To Process Routing Engine");

        String theiaLogsCBRRequest=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString(),"Received Hit To Process Routing Engine" );
        Assertions.assertThat(theiaLogsCBRRequest).contains("FetchCostAcquirerInfoRequestBody");
    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify Received Hit To Process Routing Engine from theia to routing-engine in AddMoneyExpress CC flow")
    public void RequestToProcessRoutingEngineAddMoneyExpressCC() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(MerchantType.CBR_MID, "CC", user, paymentDTO)
                .setTHEME("enhancedweb_revamp")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        checkoutPage.payButtonConvineance().click();
        ResponsePage responsePage = new ResponsePage();

        String theiaLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString());
        Assertions.assertThat(theiaLogs).contains("Received Hit To Process Routing Engine");

        String theiaLogsCBRRequest=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString(),"Received Hit To Process Routing Engine" );
        Assertions.assertThat(theiaLogsCBRRequest).contains("FetchCostAcquirerInfoRequestBody");
    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify cost effective channel response from Routing to theia in AddMoneyExpress Flow")
    public void ResponseToProcessRoutingEngineAddMoneyExpress() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(MerchantType.CBR_MID, "CC", user, paymentDTO)
                .setTHEME("enhancedweb_revamp")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        checkoutPage.payButtonConvineance().click();
        ResponsePage responsePage = new ResponsePage();

        String theiaLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString());
        Assertions.assertThat(theiaLogs).contains("Response returned from routing Engine");
        Assertions.assertThat(theiaLogs).contains("FetchCostAcquirerInfoResponseBody");
    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify channelPreference is sent in FUND_ORDER_PAY request for AddMoneyExpress flow")
    public void channelPreferenceInFUND_ORDER_PAYRequestAddMoneyExpressFlow() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(MerchantType.CBR_MID, "CC", user, paymentDTO)
                .setTHEME("enhancedweb_revamp")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        checkoutPage.payButtonConvineance().click();
        ResponsePage responsePage = new ResponsePage();

        String theiaFacadeLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID().toString(),"FUND_ORDER_PAY");
        Assertions.assertThat(theiaFacadeLogs).contains("channelPreference");

    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify Received Hit To Process Routing Engine from theia to routing-engine in AddNPay CC flow")
    public void RequestToProcessRoutingEngineAddNPayCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.CBR_MID, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();

        String theiaLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString());
        Assertions.assertThat(theiaLogs).contains("Received Hit To Process Routing Engine");

        String theiaLogsCBRRequest=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString(),"Received Hit To Process Routing Engine" );
        Assertions.assertThat(theiaLogsCBRRequest).contains("FetchCostAcquirerInfoRequestBody");
    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify cost effective channel response from Routing to theia in AddNPay CC Flow")
    public void ResponseToProcessRoutingEngineAddNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.CBR_MID, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();

        String theiaLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID().toString());
        Assertions.assertThat(theiaLogs).contains("Response returned from routing Engine");
        Assertions.assertThat(theiaLogs).contains("FetchCostAcquirerInfoResponseBody");
    }

    @Owner("Pooja")
    @Feature("PGP-45369")
    @Parameters({"theme"})
    @Test(description = "Verify channelPreference is sent in ACQUIRING_PAY_ORDER request for AddNPay CC flow")
    public void channelPreferenceInPAYMENT_CASHIER_PAYRequestAddNPayFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.CBR_MID, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();

        String theiaFacadeLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID().toString(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theiaFacadeLogs).contains("channelPreference");

    }
}
