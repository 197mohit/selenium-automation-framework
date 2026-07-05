package scripts.Native.checkoutjs;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.MAYURI;

public class AllInOneNegativePreference extends CheckoutJsBase {

    @Owner(MAYURI)
    @Feature("PGP-30610")
    @Parameters({"theme"})
    @Test(description = "Verfiy QR code visible when Negative Preference OFF in Checkout js flow")
    public void ValidateQRDetailsForNegativePreferenceOFF(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //qr code should get visible
        cashierPage.imgScanPayQRCode().assertVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-30610")
    @Parameters({"theme"})
    @Test(description = "Verfiy QR code visible when Negative Preference ON in Checkout js flow")
    public void ValidateQRDetailsForNegativePreferenceON(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.QR_NEGATIVE_PREF_ON)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //qr code should get visible
        cashierPage.imgScanPayQRCode().assertNotVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-30610")
    @Parameters({"theme"})
    @Test(description = "Verfiy PRN Flag in FPO in Checkout js flow")
    public void ValidatePRNFlagInFPO(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String PRNFlagExpected = "true";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        //PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String PRNFlagFromFPO = fetchPaymentOptionsJson.getString("body.qrDetail.prn");
        //QR code should get visible if FPO has deeplink
        //v5 fpo is not calling because it is validating via Fpo Api
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.qrDetail.dataUrl")).isNotNull();
        Assertions.assertThat(PRNFlagFromFPO).describedAs("PRN Flag is false").isEqualTo(PRNFlagExpected);
    }

    @Owner(MAYURI)
    @Feature("PGP-30610")
    @Parameters({"theme"})
    @Test(description = "Verfiy QR details and Perform Successful txn using CC in Checkout js flow")
    public void ValidateQRDetailsAndPRNSuccessTxnUsingCC(@Optional("checkoutjs_web") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.imgScanPayQRCode().assertVisible();
        cashierPage.pause(2);
        User user = userManager.getForRead(Label.BASIC);
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId(), "SSO", user.ssoToken(),fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"),fetchQRResponse.getString("body.qrInfo.response.TXN_AMOUNT"))
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validatePRN(Constants.ValidationType.NON_EMPTY)
                .validateMERC_UNQ_REF("vivek4")
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Feature("PGP-30610")
    @Parameters({"theme"})
    @Test(description = "Verfiy QR details and Perform Successful txn using UPI in Checkout js flow")
    public void ValidateQRDetailsAndUPIQRSuccessTxnUsingUPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       // PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //QR code should get visible
        cashierPage.imgScanPayQRCode().assertVisible();
        cashierPage.pause(2);
        User user = userManager.getForRead(Label.BASIC);
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId(), "SSO", user.ssoToken(),fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"), "1")
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(MAYURI)
    @Feature("PGP-30610")
    @Parameters({"theme"})
    @Test(description = "Verfiy QR details and Perform Successful txn using Wallet in Checkout js flow")
    public void ValidateQRDetailsAndSuccessTxnUsingWallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF)
                .setTxnValue("2")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
      //  PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //QR code should get visible
        cashierPage.imgScanPayQRCode().assertVisible();
        cashierPage.pause(2);
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId(), "SSO", user.ssoToken(),fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"),fetchQRResponse.getString("body.qrInfo.response.TXN_AMOUNT"))
                .setPaymentMode("PPI")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET");
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validatePRN(Constants.ValidationType.NON_EMPTY)
                .validateStatusAPIParameters();
    }

    @Owner(MAYURI)
    @Feature("PGP-30610")
    @Parameters({"theme"})
    @Test(description = "Verfiy QR details and Perform Failed txn using CC in Checkout js flow")
    public void ValidateQRDetailsAndFailedTxnUsingCC(@Optional("checkoutjs_web") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF)
                .setTxnValue("99.98")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.data.setAmount("99.98");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.imgScanPayQRCode().assertVisible();
        cashierPage.pause(2);
        User user = userManager.getForRead(Label.BASIC);
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.PG2_QR_NEGATIVE_PREF_OFF.getId(), "SSO", user.ssoToken(),fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"),"99.98")
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();
    }


}
