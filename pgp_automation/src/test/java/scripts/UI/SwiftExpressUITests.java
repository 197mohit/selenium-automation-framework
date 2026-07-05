package scripts.UI;

import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.DCCExpressPage;
import com.paytm.pages.DynamicCurrencyConversionPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class SwiftExpressUITests extends PGPBaseTest {

    DCCExpressPage dccExpressPage = new DCCExpressPage();
    DynamicCurrencyConversionPage dynamicCurrencyConversionPage = new DynamicCurrencyConversionPage();

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6911")
    @Test(description = "Verify the Swift express UI when FF4J theia.enableNewLogicForDccPageSelection true,Pref swiftDccCurrency-Y and SWIFT_DCC_ROLLOUT-100")
    public void SwiftExpressUI_001() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_SWIFT_MID).setTxnValue("5000").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_SWIFT_MID.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        String dccHeadingText = dccExpressPage.dccHeading().getText();
        Assert.assertTrue(dccHeadingText.contains("Select currency to pay"));
        dccExpressPage.PayInINR().assertNotVisible();
        dynamicCurrencyConversionPage.usdPriceBreakUpText().assertNotVisible();
        dynamicCurrencyConversionPage.inrPriceBreakUpText().assertNotVisible();
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6911")
    @Test(description = "Verify the DCC express UI when FF4J Pref swiftDccCurrency-N and SWIFT_DCC_ROLLOUT-0 and DccExpress Pref are ON")
    public void SwiftExpressUI_002() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_MID).setTxnValue("5000").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_MID.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        String dccHeadingText = dccExpressPage.dccHeading().getText();
        Assert.assertTrue(dccHeadingText.contains("Select currency to pay"));
        Assert.assertTrue(dccExpressPage.PayInINR().getText().contains("Make Payment in INR"));
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6911")
    @Test(description = "Verify the OLD/Standard UI when FF4J Pref swiftDccCurrency-N and SWIFT_DCC_ROLLOUT-0 and DccExpress Pref are OFF")
    public void SwiftExpressUI_003() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_PG2_MDR).setTxnValue("5000").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_PG2_MDR.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());
        String dccHeadingText = dccExpressPage.dccHeading().getText();
        Assert.assertTrue(dccHeadingText.contains("Select currency to pay"));
        Assert.assertTrue(dynamicCurrencyConversionPage.usdPriceBreakUpText().getText().contains("Price Breakup"));
        Assert.assertTrue(dynamicCurrencyConversionPage.inrPriceBreakUpText().getText().contains("Price Breakup"));   
     }

     @Owner(Constants.Owner.VIDHI)
     @Feature("PAPR-6911")
     @Test(description = "Verify the USD heading when foreign currency is selected on Swift express UI")
     public void SwiftExpressUI_004() throws Exception {
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_SWIFT_MID).setTxnValue("5000").build();
         InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
         String orderId = initTxnDTO.getBody().getOrderId();
         ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                 .Builder(Constants.MerchantType.DCC_SWIFT_MID.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
                 .setPaymentMode("DEBIT_CARD")
                 .setCardNum(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)
                 .setAuthMode("otp")
                 .build();
         ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
         NativeHelpers.submitJsonFormInBrowser(response.toString());
        Assert.assertTrue(dccExpressPage.dccHeading().getText().contains("Select currency to pay"));

//----------------------------------------- START to verify the SWIFT UI ------------------------------
        dccExpressPage.PayInINR().assertNotVisible();
        dynamicCurrencyConversionPage.usdPriceBreakUpText().assertNotVisible();
        dynamicCurrencyConversionPage.inrPriceBreakUpText().assertNotVisible();
// ----------------------------------------- END to verify the SWIFT UI ------------------------------
         
// ASSERTIONS of test case 
         Assert.assertTrue(!(dccExpressPage.frncurrency().getText()).equals("INDIAN RUPEE"));
         Assert.assertTrue(!(dccExpressPage.frncurrencyAmount().getText()).contains("INR"));
     }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1992/PGPUI-2172")
    @Test(description = "Verify that txn is AutoSubmit on timer countdown completion on Swift express UI")
    public void verifyPayCall_AutoSubmit_onTimerCompletion() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DCC_SWIFT_MID).setTxnValue("5000").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.DCC_SWIFT_MID.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(response.toString());

        dccExpressPage.waitUntilLoads();
        String dccHeadingText = dccExpressPage.dccHeading().getText();
        Assert.assertTrue(dccHeadingText.contains("Select currency to pay"));
        dccExpressPage.PayInINR().assertNotVisible();
        dynamicCurrencyConversionPage.usdPriceBreakUpText().assertNotVisible();
        dynamicCurrencyConversionPage.inrPriceBreakUpText().assertNotVisible();

        TimeUnit.SECONDS.sleep(80);

        String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_PAY_ORDER");
        System.out.println("Acquiring Pay Order Logs: " + acquiringPayOrderLogs); 
        Assertions.assertThat(acquiringPayOrderLogs).as("theia_facade ACQUIRING_PAY_ORDER logs").isNotBlank();
        Assertions.assertThat(acquiringPayOrderLogs).contains("ACQUIRING_PAY_ORDER");
        
    }

}
