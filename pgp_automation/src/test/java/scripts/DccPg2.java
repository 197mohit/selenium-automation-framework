package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.CloseOrderAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.CloseOrder.Body;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.CloseOrder.Head;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;


public class DccPg2 extends PGPBaseTest {

    private DynamicCurrencyConversionPage dccPage = new DynamicCurrencyConversionPage();
    private DynamicCurrencyConversionBankPage dccBankPage = new DynamicCurrencyConversionBankPage();
    private CheckoutPage checkoutPage = new CheckoutPage();
    CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();

    private static void verifyPgProxyLogs(String orderId, String logSearch) throws InterruptedException {
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log | " +
                "grep \""+logSearch+"\"";
        Awaitility.await().atMost(4, TimeUnit.MINUTES).
                until(()->LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd).contains(logSearch));
        String logs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
        Assertions.assertThat(logs).contains("dccPaymentInfo");
        Assertions.assertThat(logs).contains("dccSupported");
        Assertions.assertThat(logs).contains("foreignCurrencyName");
        Assertions.assertThat(logs).contains("exchangeRateSourceName");
        Assertions.assertThat(logs).contains("expirationTimestamp");
        Assertions.assertThat(logs).contains("foreignCurrencySymbol");
        Assertions.assertThat(logs).contains("dccId");
        Assertions.assertThat(logs).contains("foreignMarkupAmount");
        Assertions.assertThat(logs).contains("dccOffered");
        Assertions.assertThat(logs).contains("foreignPayableAmount");
        Assertions.assertThat(logs).contains("isoForeignCurrencyCode");
        Assertions.assertThat(logs).contains("foreignCurrencyCode");
        Assertions.assertThat(logs).contains("foreignMarkupRatePercentage");
        Assertions.assertThat(logs).contains("foreignPaymentAmount");
        Assertions.assertThat(logs).contains("amountPerUnitForeignAmount");
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PG2-6651 & PG2-6880")
    @Test(description = "Verify Successful transaction for MDR merchant with credit card")
    public void successfulTransactionForMdrCC() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_PG2_MDR).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_PG2_MDR.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        dccPage.selectCurrencyAndValidateConvenienceFee("USD");
        dccBankPage.clickSuccessButton();
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.DCC_PG2_MDR.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("IICP").AssertAll();
        verifyPgProxyLogs(orderId,"alipayplus.acquiring.order.paymentNotify");
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PG2-6651 & PG2-6880")
    @Test(description = "Verify Unsuccessful transaction for MDR merchant with credit card")
    public void unsuccessfulTransactionForMdrCC() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_PG2_MDR).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_PG2_MDR.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        dccPage.selectCurrencyAndValidateConvenienceFee("USD");
        Thread.sleep(5000);
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        Body body = new Body();
        body.setMid(Constants.MerchantType.DCC_PG2_MDR.getId());
        body.setOrderId(orderId);
        body.setIsForceClose(true);
        closeOrderDTO.setBody(body);
        Head head = new Head();
        head.setChannelId("WAP");
        head.setVersion("v1");
        head.setSigature("");
        closeOrderDTO.setHead(head);
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeResponse.getBody().asString().contains("SUCCESS"));
        verifyPgProxyLogs(orderId,"alipayplus.acquiring.order.closeNotify");
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PG2-6651 & PG2-6880")
    @Test(description = "Verify Successful transaction for PCF merchant with credit card")
    public void successfulTransactionForPcfCC() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_PG2_PCF).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_PG2_PCF.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        dccPage.selectCurrencyAndValidateConvenienceFee("USD");
        dccBankPage.clickSuccessButton();
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.DCC_PG2_PCF.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("IICP").AssertAll();
        verifyPgProxyLogs(orderId,"alipayplus.acquiring.order.paymentNotify");
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PG2-6651 & PG2-6880")
    @Test(description = "Verify Successful transaction with INR for PCF merchant with debit card")
    public void successfulTransactionWithINRForPcfDC() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_PG2_PCF).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_PG2_PCF.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        dccPage.selectCurrencyAndValidateConvenienceFee("INR");
        dccBankPage.clickSuccessButton();
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.DCC_PG2_PCF.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("IICP").AssertAll();
        verifyPgProxyLogs(orderId,"alipayplus.acquiring.order.paymentNotify");
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PG2-6651 & PG2-6880")
    @Test(description = "Verify Merchant status contains Dcc payment info")
    public void dccPg2MerchantStatusValidation() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_PG2_PCF).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_PG2_PCF.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        dccPage.selectCurrencyAndValidateConvenienceFee("USD");
        dccBankPage.clickSuccessButton();
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.DCC_PG2_PCF.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("IICP").AssertAll();
        String body = txnStatus.execute().asString();
        Assertions.assertThat(body).contains("dccPaymentDetail");
        Assertions.assertThat(body).contains("dccId");
        Assertions.assertThat(body).contains("amountPerUnitForeignAmount");
        Assertions.assertThat(body).contains("foreignCurrencyCode");
        Assertions.assertThat(body).contains("foreignCurrencySymbol");
        Assertions.assertThat(body).contains("foreignPayableAmount");
        Assertions.assertThat(body).contains("foreignPaymentAmount");
        Assertions.assertThat(body).contains("foreignMarkupAmount");
        Assertions.assertThat(body).contains("foreignMarkupRatePercentage");
        Assertions.assertThat(body).contains("expirationTimestamp");
        Assertions.assertThat(body).contains("exchangeRateSourceName");
        Assertions.assertThat(body).contains("isoForeignCurrencyCode");
        Assertions.assertThat(body).contains("dccOffered");
        Assertions.assertThat(body).contains("foreignCurrencyName");
    }

/*    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PG2-6651 & PG2-6880")
    @Parameters({"theme"})
    @Test(description = "Verify successful pg2 dcc transaction with enhanced flow", enabled = false) */
    public void pg2DccEnhancedFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.DCC_PG2_MDR, theme)
                .setTXN_AMOUNT("100.00").build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD);
        System.out.println(orderDTO.getORDER_ID());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        dccPage.selectCurrencyAndValidateConvenienceFee("INR");
        dccBankPage.clickSuccessButton();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("IICP").AssertAll();
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PG2-6651 & PG2-6880")
    @Parameters({"theme"})
    @Test(description = "Verify successful pg2 dcc transaction with checkoutjs flow")
    public void pg2DccChekoutJsFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_PG2_MDR)
                .build();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        String[] windows = DriverManager.getDriver().getWindowHandles().toArray(new String[0]);
        DriverManager.getDriver().switchTo().window(windows[1]);
        dccPage.selectCurrencyAndValidateConvenienceFee("USD");
        dccBankPage.clickSuccessButton();
        DriverManager.getDriver().switchTo().window(windows[0]);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.DCC_PG2_MDR.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("IICP").AssertAll();
        verifyPgProxyLogs(orderId,"alipayplus.acquiring.order.paymentNotify");
    }
}