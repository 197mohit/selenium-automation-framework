package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner(Constants.Owner.TAMANA_TATHAN)
//FF4j Flag = theia.subscriptionQrEnabled
//Merchant pref = ENABLE_SUBSCRIPTION_QR_FLOW
public class UpiQrFlow extends PGPBaseTest {

    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    @Step()
    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }

    public String requestMethod(Constants.MerchantType merchant , String txnAmount , String amountType , String maxAmount , String freqUnit , String graceDays, String requestType){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue(txnAmount)
                .setSubscriptionAmountType(amountType)
                .setSubscriptionMaxAmount(maxAmount)
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit(freqUnit)
                .setSubscriptionGraceDays(graceDays)
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType(requestType)
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String subscriptionId = initTxnResponseDTO.getBody().getSubscriptionId();

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), subscriptionId, maxAmount, "QR_SUBSCRIPTION")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        JsonPath upiPspRepsonse = staticQrUpiPSP.execute().jsonPath();
        validateUpiPspResponse(upiPspRepsonse, Arrays.asList("body.resultCode", "body.resultCodeId", "body.orderId", "body.txnAmount", "body.debitAmount"), Arrays.asList("SUCCESS", "001", subscriptionId, maxAmount, txnAmount));
        return orderId;
    }

    public void validateUpiPspResponse(JsonPath response , List<String> fieldsToValidate , List<String> expectedValue ){
        for(int i=0; i<fieldsToValidate.size(); i++)
            Assert.assertTrue(response.getString(fieldsToValidate.get(i)).equals(expectedValue.get(i)), "Validating the response of UPI PSP API, Field Checked -> "+fieldsToValidate.get(i)+" expected -> "+expectedValue.get(i));
    }

    @Parameters({"theme"})
    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Verify the UI for UPI - QR Flow when ff4j(theia.subscriptionQrEnabled) flag is on and merchant pref(ENABLE_SUBSCRIPTION_QR_FLOW) is set to yes")
    public void TC01_ValidateUI(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.QR_Subscription_Merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSUBS_MAX_AMOUNT("5.65")
                .setSUBS_GRACE_DAYS("0")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.imgScanPayQRCode().isDisplayed(), "Validating the QR is displayed or not, when ff4j is on and pref is set to yes");
        Assert.assertTrue(cashierPage.enabledPaymodes().getText().contains("Scan and Pay to set-up subscription via UPI Autopay"), "Validating the displayed message on cashier page");
    }

    @Parameters({"theme"})
    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Verify the UI for UPI - QR Flow when ff4j(theia.subscriptionQrEnabled) flag is on and merchant pref(ENABLE_SUBSCRIPTION_QR_FLOW) is set to No")
    public void TC02_ValidateUI(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.UPI_NATIVE_SUBS, theme)
                .setCHANNEL_ID("WEB")
                .setSUBS_MAX_AMOUNT("5.65")
                .setSUBS_GRACE_DAYS("0")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertFalse(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR is displayed or not when ff4j flag is on and pref is set to N");
    }

    @Parameters({"theme"})
    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Verify the UI for UPI - QR Flow when ff4j(theia.subscriptionQrEnabled) flag is on and merchant pref(ENABLE_SUBSCRIPTION_QR_FLOW) is set to yes")
    public void TC03_ValidateUI_CheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR is displayed or not, when ff4j is on and pref is set to yes");
        Assert.assertTrue(cashierPage.enabledPaymodes().getText().contains("Scan and Pay to set-up subscription via UPI Autopay"), "Validating the displayed message on cashier page");
    }


    @Parameters({"theme"})
    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Verify the UI for UPI - QR Flow when ff4j(theia.subscriptionQrEnabled) flag is on and merchant pref(ENABLE_SUBSCRIPTION_QR_FLOW) is set to yes for checkoutJS")
    public void TC04_ValidateUI_CheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UPI_NATIVE_SUBS)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertFalse(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR is displayed or not when ff4j flag is on and pref is set to N");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR detail field and deeplink field is present in FPO response")
    public void TC05_ValidateTheNewFieldsInFPO(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body").contains("qrDetail"), "Validating the QR Detail is present in FPO response or not");
        if(fetchPaymentOptionsJson.getString("body").contains("qrDetail")){
            String qrDetail = fetchPaymentOptionsJson.getString("body.qrDetail");
            List<String> qrDetailExpected = Arrays.asList("dataUrl", "pageTimeout", "displayMessage", "enabled", "prn", "upiQR");
            for(int i=0; i< qrDetailExpected.size(); i++)
                Assert.assertTrue(qrDetail.contains(qrDetailExpected.get(i)), "Validating sub fields of qr detail");
        }
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "Validating the Deep Link is present in FPO response or not");
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body.deepLink").isEmpty());
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR detail field and deeplink field is present or not in FPO response when deeplink param is false")
    public void TC06_ValidateTheNewFieldInFPOWhendeepLinkisFALSE(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(false)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("qrDetail"), "Validating the QR Detail is not present in FPO response when deeplink param is false");
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "Validating the Deep link is not present in FPO response when deeplink param is false");
    }


    @Parameters({"theme"})
    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the new field in UPI psp response")
    public void TC07_ValidateTheNewFieldInUpiPsp(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String subscriptionId = initTxnResponseDTO.getBody().getSubscriptionId();

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), subscriptionId, "5.58", "QR_SUBSCRIPTION")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        JsonPath upiPspRepsonse = staticQrUpiPSP.execute().jsonPath();

        List<String> validateNewField = Arrays.asList("subscriptionId", "debitAmount", "callbackUrl", "callbackUrl");
        for(int i=0 ; i< validateNewField.size();i++)
            Assert.assertTrue(upiPspRepsonse.getString("body").contains(validateNewField.get(i)));
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the success response from UPI psp API")
    public void TC08_ValidateSuccessFromUPIpspAPI() {
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        String maxAmount = "5.58";
        String txnAmount = "2.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue(txnAmount)
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(maxAmount)
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String subscriptionId = initTxnResponseDTO.getBody().getSubscriptionId();

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), subscriptionId, maxAmount, "QR_SUBSCRIPTION")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        JsonPath upiPspRepsonse = staticQrUpiPSP.execute().jsonPath();
        validateUpiPspResponse(upiPspRepsonse, Arrays.asList("body.resultCode", "body.resultCodeId", "body.orderId", "body.txnAmount", "body.debitAmount"), Arrays.asList("SUCCESS", "001", subscriptionId, maxAmount, txnAmount));
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp API when Order Id is invalid")
    public void TC09_ValidateResponseWhenOrderIdIsInvalid() {
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.8")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), "1234", "5.58", "QR_SUBSCRIPTION")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        JsonPath upiPspRepsonse = staticQrUpiPSP.execute().jsonPath();
        validateUpiPspResponse(upiPspRepsonse, Arrays.asList("body.resultCode", "body.resultCodeId", "body.resultMsg"), Arrays.asList("FAIL", "005", "Order doesn't exist"));
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp API when Request Type is null")
    public void TC10_ValidateResponseWhenRequestTypeinNULL() {
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.8")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String subscriptionId = initTxnResponseDTO.getBody().getSubscriptionId();

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(" ", merchant.getId(), subscriptionId, "5.58", "QR_SUBSCRIPTION")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        JsonPath upiPspRepsonse = staticQrUpiPSP.execute().jsonPath();
        validateUpiPspResponse(upiPspRepsonse, Arrays.asList("body.resultCode", "body.resultCodeId", "body.resultMsg"), Arrays.asList("FAIL", "009", "payment Failure"));
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for FPO when requestType is 'NATIVE_MF_SIP' in create Subscription")
    public void TC11_ValidateFPOResponseForNATIVE_MF_SIP() {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body").contains("qrDetail"), "Validating the QR Detail is  present in FPO response when deeplink param is true");
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "Validating the Deep link is  present in FPO response when deeplink param is true");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp when requestType is 'NATIVE_MF_SIP' in create Subscription")
    public void TC12_ValidateSuccessResponseForNATIVE_MF_SIP() {
        requestMethod(Constants.MerchantType.UPI_MUTUAL_MF,"2.00", "VARIABLE", "5.8", "MONTH", "0", "NATIVE_MF_SIP");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp when Amount Type is 'FIX' in create Subscription")
    public void TC13_ValidateResponseWhenAmountTypeIsFix() {
        requestMethod(Constants.MerchantType.QR_Subscription_Merchant, "2.00", "FIX", "2.00", "MONTH", "0", "NATIVE_SUBSCRIPTION");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp when Amount Type is 'Variable' , Frequncy Type is 'WEEK' and grace days is '5' in create Subscription")
    public void TC14_ValidateResponseWhenAmountTypeIsVaraiableandForWeek() {
        requestMethod(Constants.MerchantType.QR_Subscription_Merchant, "2.00", "VARIABLE", "5.8", "WEEK", "5", "NATIVE_SUBSCRIPTION");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp when Amount Type is 'Variable' , Frequncy Type is 'QUARTER' in create Subscription")
    public void TC15_ValidateResponseWhenAmountTypeIsVaraiableandForQuarter() {
        requestMethod(Constants.MerchantType.QR_Subscription_Merchant, "2.00", "VARIABLE", "5.8", "QUARTER", "0", "NATIVE_SUBSCRIPTION");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp when Amount Type is 'Variable' , Frequncy Type is 'WEEK' , grace days is '5' ,in create Subscription")
    public void TC16_ValidateResponseWhenAmountTypeIsVaraiableForNATIVE_MF_SIP() {
        requestMethod(Constants.MerchantType.UPI_MUTUAL_MF, "2.00", "VARIABLE", "5.8", "WEEK", "5", "NATIVE_MF_SIP");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for UPI psp when Amount Type is 'Variable' , Frequncy Type is 'QUARTER' in create Subscription")
    public void TC17_ValidateResponseWhenAmountTypeIsVaraiableandForQuarter_NATIVE_MF_SIP() {
        requestMethod(Constants.MerchantType.UPI_MUTUAL_MF, "2.00", "VARIABLE", "5.8", "QUARTER", "0", "NATIVE_MF_SIP");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deep link parameters ")
    public void TC18_ValidateDeepLinkParameters(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.8")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String deeplink = fetchPaymentOptionsJson.getString("body.deepLink");
        Assert.assertTrue(deeplink.contains("recur=MONTHLY"));
        Assert.assertTrue(deeplink.contains("am=5.80"));
        Assert.assertTrue(deeplink.contains("mam=2.00"));
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deep link parameters when Amount Type = 'VARIABLE' and Freq Unit is 'ONDEMAND' ")
    public void TC19_ValidateDeepLinkParametersForOndemandSubs(){
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("2")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.8")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String deeplink = fetchPaymentOptionsJson.getString("body.deepLink");
        Assert.assertTrue(deeplink.contains("recur=ASPRESENTED"));
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deep link parameters when Amount Type = 'Fix' and Freq Unit is 'ONDEMAND' ")
    public void TC20_ValidateDeepLinkParametersForOndemandSubs_1(){
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("2")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("2")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String deeplink = fetchPaymentOptionsJson.getString("body.deepLink");
        Assert.assertTrue(deeplink.contains("recur=ASPRESENTED"));
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the product code when requestType = 'SEAMLESS_3D_FORM' for NATIVE_SUBSCRIPTION")
    public void TC21_ValidateProductCode() throws InterruptedException {
        String orderId = requestMethod(Constants.MerchantType.QR_Subscription_Merchant, "2.00", "VARIABLE", "5.8", "MONTH", "0", "NATIVE_SUBSCRIPTION");
        String command = " grep \"" + orderId + "\" /paytm/logs/theia_facade.log" + " | grep \"ACQUIRING_PAY_ORDER\"";
        String cashierPayRequest = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, command);
        Assert.assertTrue(cashierPayRequest.contains("51051000100000000004"), "Product code should be of subscription , as SEAMLESS_3D_FORM is sent as request Type in UPI psp request ");
        Assert.assertFalse(cashierPayRequest.contains("51051000100000000001"), "Negative Test , Validating 001 should not be sent in cashier pay request");
    }

    @Feature("PAPR-3361")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the product code when requestType = 'SEAMLESS_3D_FORM' for 'NATIVE_MF_SIP")
    public void TC22_ValidateProductCodeForNATIVE_MF_SIP() throws InterruptedException {
        String orderId = requestMethod(Constants.MerchantType.UPI_MUTUAL_MF, "2.00", "VARIABLE", "5.8", "MONTH", "0", "NATIVE_MF_SIP");
        String command = " grep \"" + orderId + "\" /paytm/logs/theia_facade.log" + " | grep \"ACQUIRING_PAY_ORDER\"";
        String cashierPayRequest = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, command);
        Assert.assertTrue(cashierPayRequest.contains("51051000100000000043"), "Product code should be of NATIVE MF SIP , as SEAMLESS_3D_FORM is sent as request Type in UPI psp request ");
        Assert.assertFalse(cashierPayRequest.contains("51051000100000000001"), "Negative Test , Validating 001 should not be sent in cashier pay request");
    }

    @Parameters({"theme"})
    @Feature("PGP-42885")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR is not displayed on when UPI is not added as paymode")
    public void TC01_ValidateQrIsNotDsiplayed(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.NON_UPI_MERCHANT, theme)
                .setCHANNEL_ID("WEB")
                .setSUBS_MAX_AMOUNT("5.65")
                .setSUBS_GRACE_DAYS("0")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertFalse(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR should not be displayed - as UPI is not added on merchant");
    }

    @Parameters({"theme"})
    @Feature("PGP-42885")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR is not displayed on when UPI is not added as paymode")
    public void TC02_ValidateQRisNotDisplayed_UPI_INACTIVE(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.EMI_DC_MERCHANT, theme)
                .setCHANNEL_ID("WEB")
                .setSUBS_MAX_AMOUNT("5.65")
                .setSUBS_GRACE_DAYS("0")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertFalse(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR should not be displayed - as UPI is not active on merchant");
    }

    @Parameters({"theme"})
    @Feature("PGP-42885")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR is not displayed on when UPI is not addeed on merchant")
    public void TC03_ValidateQRisNotDisplayed(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NON_UPI_MERCHANT)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertFalse(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR should not be displayed - as UPI is not added on merchant");

    }

    @Parameters({"theme"})
    @Feature("PGP-42885")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR is not displayed on when UPI added as paymode but not active")
    public void TC04_ValidateQRisNotDisplayed_UPI_INACTIVE(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI_DC_MERCHANT)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertFalse(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR should not be displayed - as UPI is not active on merchant");
    }

    @Feature("PGP-42885")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR is not displayed on when UPI is not addeed on merchant")
    public void TC05_ValidateQRisNotDisplayed_NATIVE(){
        Constants.MerchantType merchant = Constants.MerchantType.NON_UPI_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("qrDetail"), "Validating the QR Detail is not present in FPO response when UPI is not added as a paymode");
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "Validating the Deep link is not present in FPO response when UPI is not added as a paymode");
    }

    @Feature("PGP-42885")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the QR is not displayed on when UPI is not addeed on merchant")
    public void TC06_ValidateQRisNotDisplayed_UPI_INACTIVE_NATIVE(){
        Constants.MerchantType merchant = Constants.MerchantType.EMI_DC_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("qrDetail"), "Validating the QR Detail is not present in FPO response when UPI is inactive on merchant");
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "Validating the Deep link is not present in FPO response when UPI is inactive on merchant");
    }

    @Feature("PGP-48730")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deep link should not come for non subs flow ")
    public void TC01_PGP_48730ForNonSubsFlow(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "For 001 product code , deeplink should not get returned in FPO");
    }

    @Feature("PGP-48730")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the fields are coming for 004 product code")
    public void TC02_PGP_48730ForSubsFlow(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "Validating the Deep Link is present in FPO response or not");
        Assert.assertFalse(fetchPaymentOptionsJson.getString("body.deepLink").isEmpty());
    }

    @Feature("PGP-48730")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the response for FPO when requestType is 'NATIVE_MF_SIP' in create Subscription")
    public void TC03_PGP_48730ForSIPFlow() {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_MF_SIP")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body").contains("deepLink"), "Validating the Deep link is  present in FPO response");
    }

    @Feature("PGP-48381")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deeplink field for Subscription dqr flow")
    public void TC01_PGP_48381(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("deepLink")) {
            String deeplink = fetchPaymentOptionsJson.getString("body.deepLink");
            Assert.assertTrue(deeplink.contains("fam=1.00"));
            Assert.assertTrue(deeplink.contains("txnType=CREATE"));
        }
    }

    @Feature("PGP-48381")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deeplink field for pennydrop Subscription dqr flow")
    public void TC02_PGP_48381(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("deepLink")) {
            String deeplink = fetchPaymentOptionsJson.getString("body.deepLink");
            Assert.assertTrue(deeplink.contains("fam=1.00"));
            Assert.assertTrue(deeplink.contains("txnType=CREATE"));
        }
    }

    @Feature("PGP-48381")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the upi psp response after deeplink parameter changes")
    public void TC03_PGP_48381(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("deepLink")) {
            String deeplink = fetchPaymentOptionsJson.getString("body.deepLink");
            Assert.assertTrue(deeplink.contains("fam=1.00"));
            Assert.assertTrue(deeplink.contains("txnType=CREATE"));
        }

        String subscriptionId = initTxnResponseDTO.getBody().getSubscriptionId();

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), subscriptionId, "100", "QR_SUBSCRIPTION")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        JsonPath upiPspRepsonse = staticQrUpiPSP.execute().jsonPath();
        validateUpiPspResponse(upiPspRepsonse, Arrays.asList("body.resultCode", "body.resultCodeId", "body.orderId", "body.txnAmount", "body.debitAmount"), Arrays.asList("SUCCESS", "001", subscriptionId, "100", "1.00"));
    }

    @Feature("PGP-48381")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the upi psp response after deeplink parameter changes for pennydrop")
    public void TC04_PGP_48381(){
        Constants.MerchantType merchant = Constants.MerchantType.QR_Subscription_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setTxnValue("0")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId(Boolean.toString(true))
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if(fetchPaymentOptionsJson.getString("body").contains("deepLink")) {
            String deeplink = fetchPaymentOptionsJson.getString("body.deepLink");
            Assert.assertTrue(deeplink.contains("fam=1.00"));
            Assert.assertTrue(deeplink.contains("txnType=CREATE"));
        }

        String subscriptionId = initTxnResponseDTO.getBody().getSubscriptionId();

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM", merchant.getId(), subscriptionId, "100", "QR_SUBSCRIPTION")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        JsonPath upiPspRepsonse = staticQrUpiPSP.execute().jsonPath();
        validateUpiPspResponse(upiPspRepsonse, Arrays.asList("body.resultCode", "body.resultCodeId", "body.orderId", "body.txnAmount", "body.debitAmount"), Arrays.asList("SUCCESS", "001", subscriptionId, "100", "1.00"));
    }

    @Parameters({"theme"})
    @Feature("PGP-48381")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deeplink paramete for intent flow")
    public void TC05_PGP_48381(@Optional("enhancedwap_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.QR_Subscription_Merchant, theme)
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_GRACE_DAYS("0")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.upiIntentPayButton().click();
        cashierPage.waitUntilLoads();
        String orderId = orderDTO.getORDER_ID();

        LogsValidationHelper logsValidation = new LogsValidationHelper();
        String logs = logsValidation.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy , orderId, "deeplink");
        Assertions.assertThat(logs).contains("fam=2.00&txnType=CREATE");
    }

    @Parameters({"theme"})
    @Feature("PGP-48381")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Validate the deeplink paramete for intent flow for pennydrop")
    public void TC06_PGP_48381(@Optional("enhancedwap_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.QR_Subscription_Merchant, theme)
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_GRACE_DAYS("0")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.upiIntentPayButton().click();
        cashierPage.waitUntilLoads();
        String orderId = orderDTO.getORDER_ID();

        LogsValidationHelper logsValidation = new LogsValidationHelper();
        String logs = logsValidation.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy , orderId, "deeplink");
        Assertions.assertThat(logs).contains("fam=1.00&txnType=CREATE");
    }
}


