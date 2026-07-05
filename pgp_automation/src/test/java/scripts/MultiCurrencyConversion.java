package scripts;

import com.paytm.api.ProcessTransaction;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitialAmount;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import scripts.Native.checkoutjs.CheckoutJsBase;

import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class MultiCurrencyConversion extends CheckoutJsBase {

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the response of initiate txn API when currency is USD (not INR)")
    public void MCC_001() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        Assert.assertEquals((initTxnResponseDTO.getBody().getResultInfo().getResultMsg()),"Success");
        Assertions.assertThat(initTxnResponseDTO.getBody().getTxnToken().toString()).isNotEmpty().isNotNull();

    }
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the response of initiate txn API when currency is SGD when pref MCC_CURRENCY_SUPPORTED=USD,GDP,EUR")
    public void MCC_002() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100")
                .setCurrency("SGD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        Assert.assertEquals((initTxnResponseDTO.getBody().getResultInfo().getResultMsg()),"Request parameters are not valid");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the fxEngine logger when Currency = USD is passed in initiate API")
    public void MCC_003() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        String grepcmd = "com.paytm.pgplus.theia.services.impl.FxEngineServiceImpl.callFxEngineService";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("FxEngineRatesRequest payload:");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the fxEngine response when Currency = USD is passed in initiate API")
    public void MCC_004() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        String grepcmd = "com.paytm.pgplus.theia.services.impl.FxEngineServiceImpl.callFxEngineService";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("FxEngineRate API response");
        Assertions.assertThat(logs).contains("code=FX0000, message=Success");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the fxEngine request when Currency = USD is passed in initiate API")
    public void MCC_005() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.0")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        String grepcmd = "FxEngineRatesRequest payload:";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("FxEngineRatesRequest payload: HttpRequestPayload [entity={\"sourceCurrency\":\""+ initTxnDTO.getBody().getTxnAmount().getCurrency() +"\",\"destinationCurrency\":\"INR\",\"amount\":"+ initTxnDTO.getBody().getTxnAmount().getValue() +",\"requestor\":{\"type\":\"PAYTM_MERCHANT\",\"id\":\""+ Constants.MerchantType.MCC_MID.getId() +"\"},\"orderInfo\":{\"orderId\":\""+ initTxnDTO.getBody().getOrderId() +"\"},\"bizContext\":{\"operationType\":\"MCC\",\"bizType\":\"OFFLINE\",\"providerInfo\":{\"PYFX\":[{\"code\":\"ICMC\"}]}}}");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the ACQUIRING_CREATE_ORDER request when Currency = USD is passed in initiate API")
    public void MCC_006() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.0")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        String grepcmd = "ACQUIRING_CREATE_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd,"REQUEST");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("mccInfo");
        Assertions.assertThat(logs).contains("\"mccSupported\":true");
        Assertions.assertThat(logs).contains("\"foreignCurrencyCode\":\"USD\"");
        Assertions.assertThat(logs).contains("\"foreignCurrencyName\":\"U.S. DOLLAR");
        Assertions.assertThat(logs).contains("\"foreignCurrencySymbol\":\"$\"");
        Assertions.assertThat(logs).contains("\"fxProviderType\":\"PYFX\"");
        Assertions.assertThat(logs).contains("\"conversionPrecision\":2");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the FPO response when Currency = USD is passed in initiate API")
    public void MCC_007() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.0")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO=new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2=new FetchPaymentOptionV2(Constants.MerchantType.MCC_MID.getId(),initTxnDTO.getBody().getOrderId(),fetchPaymentOptionsDTO);
        JsonPath f=fetchPaymentOptionV2.execute().jsonPath();
        System.out.println(f);
        Assert.assertEquals(f.getString("body.mccPaymentInfo.foreignCurrencyName"),"U.S. DOLLAR");
        Assert.assertEquals(f.getString("body.mccPaymentInfo.foreignCurrencyCode"),"USD");
        Assert.assertEquals(f.getString("body.mccPaymentInfo.foreignCurrencySymbol"),"$");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the Order amount in FPO response when Currency = USD is passed in initiate API")
    public void MCC_008() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO=new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2=new FetchPaymentOptionV2(Constants.MerchantType.MCC_MID.getId(),initTxnDTO.getBody().getOrderId(),fetchPaymentOptionsDTO);
        JsonPath f=fetchPaymentOptionV2.execute().jsonPath();
        System.out.println(f);
        Assert.assertEquals(f.getString("body.orderAmount"),initTxnDTO.getBody().getTxnAmount().getValue());

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the ACQUIRING_PAY_ORDER request when Currency = USD is passed in initiate API")
    public void MCC_009() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.MCC_MID.getId(), txnToken, orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(new PaymentDTO().INTERNATIONAL_ICMC_CREDIT_CARD)
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd,"REQUEST");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"feeRateFactorsInfo\":\"{\"internationalCardPayment\":\"TRUE\",\"solutionWiseMdr\":\"API\",\"instId\":\"VISA\",\"mcc\":\"TRUE\"}\"");
        Assertions.assertThat(logs).contains("\"mccServiceInstId\":\"ICMCC1IN\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the FEE_BATCH_CONSULT request when Currency = USD is passed in initiate API")
    public void MCC_0010() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.MCC_MID.getId(), txnToken, orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(new PaymentDTO().INTERNATIONAL_ICMC_CREDIT_CARD)
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String grepcmd = "FEE_BATCH_CONSULT";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd,"REQUEST");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"internationalCardPayment\":\"TRUE\",\"solutionWiseMdr\":\"API\",\"instId\":\"VISA\",\"mcc\":\"TRUE\"}");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Test(description = "Verify the FEE_BATCH_CONSULT response when Currency = USD is passed in initiate API")
    public void MCC_0011() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.MCC_MID.getId(), txnToken, orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(new PaymentDTO().INTERNATIONAL_ICMC_CREDIT_CARD)
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String grepcmd = "FEE_BATCH_CONSULT";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd, "RESPONSE");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"internationalCardPayment\":\"TRUE\",\"solutionWiseMdr\":\"API\",\"instId\":\"VISA\",\"mcc\":\"TRUE\"}");
    }

  //  ----------------------------------------- UI - TEST CASES -----------------------------------------
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Parameters({"theme"})
    @Test(description = "Verify that currency is displayed as USD in the cashier page")
    public void MCC_UI_0012(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String usdCurrency = cashierPage.USDCurrency().getAttribute("innerHTML");
        Assert.assertEquals(usdCurrency, "USD ");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Parameters({"theme"})
    @Test(description = "Verify that currency is displayed as USD in the pay button")
    public void MCC_UI_0013(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payButton().waitUntilVisible();
        String buttonText = cashierPage.payButton().getText();
        Assert.assertTrue(buttonText.contains("USD"));
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Parameters({"theme"})
    @Test(description = "Verify the currency is displayed as USD in the i button")
    public void MCC_UI_0014(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.MCC_INTERNATIONAL_CREDIT_CARD);
        cashierPage.pause(2);
        cashierPage.fillExpiryMonth("11");
        cashierPage.fillExpiryYear("2050");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
        Thread.sleep(10000);
        String convFeeAmountCuurency = cashierPage.iButton_ConvFeeAmount().getText();
        Assert.assertEquals(convFeeAmountCuurency, "USD");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Parameters({"theme"})
    @Test(description = "Verify the currency is displayed as USD in the charge fee amount at PG")
    public void MCC_UI_0015(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.MCC_INTERNATIONAL_CREDIT_CARD);
        cashierPage.pause(2);
        cashierPage.fillExpiryMonth("11");
        cashierPage.fillExpiryYear("2050");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
        Thread.sleep(10000);
        String convFeeAmount = cashierPage.chargeFeeAmtAtPG().getText();
        System.out.println("convFeeAmount: " + convFeeAmount);
        Assert.assertTrue(convFeeAmount.contains("USD"));
}

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-5206/PGP-58657")
    @Parameters({"theme"})
    @Test(description = "Verify the mccPaymentInfo is displayed in the router request on Instaproxy")
    public void MCC_UI_0016(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MCC_MID)
                .setTxnValue("100.00")
                .setCurrency("USD")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.MCC_INTERNATIONAL_CREDIT_CARD);
        cashierPage.pause(2);
        cashierPage.fillExpiryMonth("11");
        cashierPage.fillExpiryYear("2050");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
     //   Thread.sleep(1000);
       // wait(2000);
        cashierPage.payButton().click();
        String grepcmd = "c.p.p.c.u.TxnPayloadLogger.log()";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("mccPaymentInfo");
    }

}