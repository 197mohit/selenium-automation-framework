package scripts.notification;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.wallet.WalletPaymentConfirmation;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.RefundStatusHelper;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.POOJA;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;

public class RefundNotify extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();

    @Feature("PGP-43237")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundNotify generated for CC refund")
    public void verifyRefundNotify_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);

        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");


    }


    @Feature("PGP-43237")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundSuccessNotify generated for CC refund")
    public void verifyRefundSuccessNotify_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                    .assertAll();
        }

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundSuccessNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundSuccessNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }

    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundNotify generated for DC refund")
    public void verifyRefundNotify_DC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
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
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);

        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }


    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundSuccessNotify generated for DC refund")
    public void verifyRefundSuccessNotify_DC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
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
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                    .assertAll();
        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundSuccessNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundSuccessNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }

    @Feature("PGP-43237")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundNotify generated for Wallet refund")
    public void verifyRefundNotify_Wallet(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.NATIVE_HYBRID.getId());
        }
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.NATIVE_HYBRID, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
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
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();

        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);

        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }

    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundSuccessNotify generated for Wallet refund")
    public void verifyRefundSuccessNotify_Wallet(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.NATIVE_HYBRID.getId());
        }
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.NATIVE_HYBRID, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
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
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();

        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateGATEWAY(txnStatus.getResponse().getGATEWAYNAME(), 0)
                    .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                    .assertAll();
        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundSuccessNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.RefundSuccessNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }

    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundNotify generated for UPI refund")
    public void verifyRefundNotify_UPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PPBLYONLY.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PPBLYONLY, theme).build();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
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
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);

        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundNotify generated for UPI refund")
    public void verifyRefundSuccessNotify_UPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PPBLYONLY.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PPBLYONLY, theme).build();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
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
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundSuccessNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.RefundSuccessNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }


    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundNotify generated for PPBL refund")
    public void verifyRefundNotify_PPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PPBLYONLY.getId());
        }
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBLYONLY, theme, user).
                setTXN_AMOUNT("100.0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkboxPPBL().check();
        cashierPage.textBoxPPBLPassCode().clearAndType(new PaymentDTO().getPasscode());
        cashierPage.buttonPpblSumbit().click();
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "");

        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }

    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundSuccessNotify generated for PPBL refund")
    public void verifyRefundSuccessNotify_PPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PPBLYONLY.getId());
        }
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBLYONLY, theme, user).
                setTXN_AMOUNT("100.0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkboxPPBL().check();
        cashierPage.textBoxPPBLPassCode().clearAndType(new PaymentDTO().getPasscode());
        cashierPage.buttonPpblSumbit().click();
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
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "");
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus("TXN_SUCCESS", 0)
                    .assertAll();
        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundSuccessNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.RefundSuccessNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundNotify generated for AddNPay refund")
    public void verifyRefundNotify_AddNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {

            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.AddnPay_refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay_refund, theme, user)
                .setTXN_AMOUNT("3.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);

        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.refundNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");

    }
    @Feature("PGP-43237")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify successful refundSuccessNotify generated for AddNPay refund")
    public void verifyRefundSuccessNotify_AddNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {

            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.AddnPay_refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay_refund, theme, user)
                .setTXN_AMOUNT("3.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        String orderId = orderDTO.getORDER_ID();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus("TXN_SUCCESS", 0)
                    .assertAll();
        }
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,orderDTO.getORDER_ID(),"Type=RefundSuccessNotify");
        Assertions.assertThat(logs).contains("oldPG.acquiring.refund.RefundSuccessNotify");
        Assertions.assertThat(logs).contains("\"resultCodeId\":\"00000000\"");
        Assertions.assertThat(logs).contains("\"resultCode\":\"SUCCESS\"");
    }

}