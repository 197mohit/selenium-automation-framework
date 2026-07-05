package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.fest.assertions.api.Assertions;
import org.openqa.selenium.By;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;

public class UPISavedVPA extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction when VPA VAULT PREFRENCE = LOCAL_CUSTOMERID")
    public void savedVPA_localCustomerId001(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.OTP_INJECT)
                .setCustId("VPA_11005")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.OTP_INJECT.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "SaveVPACommand request body:");
        Assertions.assertThat(logs_req).contains("\"subAssetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa8mid79337851791661\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"VPA_11005\"");
        Assertions.assertThat(logs_req).contains("\"supportedTransactionTypes\":[\"PAYMENT\",\"SUBSCRIPTION\"]");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"*****61503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "SaveVPACommand response:");
        Assertions.assertThat(logs_resp).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(logs_resp).contains("\"responseCode\":\"BE1400001\"");
        Assertions.assertThat(logs_resp).contains("\"message\":\"SUCCESS\"");

    }


    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction with Saved VPA when VPA VAULT PREFRENCE = LOCAL_CUSTOMERID")
    public void savedVPA_localCustomerId002(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.OTP_INJECT)
                .setCustId("VPA_11005")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.savedVPAList().get(0).click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.OTP_INJECT.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "FETCH_SAVED_UPI_ASSET");
        Assertions.assertThat(logs_req).contains("\"localUserIdentifier\":\"VPA_11005\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa8mid79337851791661\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "FETCH_SAVED_UPI_ASSET");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa8mid79337851791661\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"9999661503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"VPA_11005\"");
        Assertions.assertThat(logs_req).contains("\"supportedTransactionTypes\":[\"PAYMENT\",\"SUBSCRIPTION\"]");
        Assertions.assertThat(logs_req).contains("\"upiNoInfos\":[]");


    }


    @Parameters({"theme"})
    @Test(description = "Verfiy that saved VPA can be deleted when VPA VAULT PREFRENCE = LOCAL_CUSTOMERID")
    public void savedVPA_localCustomerId003(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.OTP_INJECT)
                .setCustId("VPA_11005")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.deleteSavedVPAList().get(0).click();
        cashierPage.buttonOk().click();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa8mid79337851791661\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"9999661503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"VPA_11005\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs_resp).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(logs_resp).contains("\"responseCode\":\"BE1400001\"");
        Assertions.assertThat(logs_resp).contains("\"message\":\"SUCCESS\"");


    }


    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction when VPA VAULT PREFRENCE = LOCAL_MOBILENO")
    public void savedVPA_localMobile001(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard)
                .setMobile("8586004363")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard.getKey())
                .validateVPA("9999661503@paytm")
                .validateResponsePageParameters()
                .assertAll();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "SaveVPACommand request body:");
        Assertions.assertThat(logs_req).contains("\"subAssetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa142369358662555707\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"85XXXX4363\"");
        Assertions.assertThat(logs_req).contains("\"supportedTransactionTypes\":[\"PAYMENT\",\"SUBSCRIPTION\"]");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"*****61503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "SaveVPACommand response:");
        Assertions.assertThat(logs_resp).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(logs_resp).contains("\"responseCode\":\"BE1400001\"");
        Assertions.assertThat(logs_resp).contains("\"message\":\"SUCCESS\"");

    }


    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction with Saved VPA when VPA VAULT PREFRENCE = LOCAL_MOBILENO")
    public void savedVPA_localMobile002(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard)
                .setMobile("8586004363")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.savedVPAList().get(0).click();

        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard.getKey())
                .validateVPA("9999661503@paytm")
                .validateResponsePageParameters()
                .assertAll();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "FETCH_SAVED_UPI_ASSET");
        Assertions.assertThat(logs_req).contains("\"localUserIdentifier\":\"8586004363\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa142369358662555707\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "FETCH_SAVED_UPI_ASSET");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa142369358662555707\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"9999661503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"8586004363\"");
        Assertions.assertThat(logs_req).contains("\"supportedTransactionTypes\":[\"PAYMENT\",\"SUBSCRIPTION\"]");
        Assertions.assertThat(logs_req).contains("\"upiNoInfos\":[]");

    }


    @Parameters({"theme"})
    @Test(description = "Verfiy that saved VPA can be deleted  when VPA VAULT PREFRENCE = LOCAL_MOBILENO")
    public void savedVPA_localMobile003(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard)
                .setMobile("8586004363")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.deleteSavedVPAList().get(0).click();
        cashierPage.buttonOk().click();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa142369358662555707\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"9999661503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"LOCAL\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"8586004363\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs_resp).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(logs_resp).contains("\"responseCode\":\"BE1400001\"");
        Assertions.assertThat(logs_resp).contains("\"message\":\"SUCCESS\"");

    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction when VPA VAULT PREFRENCE = GLOBAL")
    public void savedVPA_GLOBAL001(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH)
                .setMobile("8586004360")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getKey())
                .validateVPA("9999661503@paytm")
                .validateResponsePageParameters()
                .assertAll();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "SaveVPACommand request body:");
        Assertions.assertThat(logs_req).contains("\"subAssetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa12id70232557209005\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"85XXXX4360\"");
        Assertions.assertThat(logs_req).contains("\"supportedTransactionTypes\":[\"PAYMENT\",\"SUBSCRIPTION\"]");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"GLOBAL\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"*****61503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "SaveVPACommand response:");
        Assertions.assertThat(logs_resp).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(logs_resp).contains("\"responseCode\":\"BE1400001\"");
        Assertions.assertThat(logs_resp).contains("\"message\":\"SUCCESS\"");

    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction when VPA VAULT PREFRENCE = GLOBAL")
    public void savedVPA_GLOBAL002(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH)
                .setMobile("8586004360")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.savedVPAList().get(0).click();

        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getKey())
                .validateVPA("9999661503@paytm")
                .validateResponsePageParameters()
                .assertAll();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "FETCH_SAVED_UPI_ASSET");
        Assertions.assertThat(logs_req).contains("\"globalUserIdentifier\":\"8586004360\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa12id70232557209005\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"GLOBAL\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "FETCH_SAVED_UPI_ASSET");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa12id70232557209005\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"GLOBAL\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"9999661503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"8586004360\"");
        Assertions.assertThat(logs_req).contains("\"supportedTransactionTypes\":[\"PAYMENT\",\"SUBSCRIPTION\"]");
        Assertions.assertThat(logs_req).contains("\"upiNoInfos\":[]");

    }

    @Parameters({"theme"})
    @Test(description = "Verfiy that saved VPA can be deleted when VPA VAULT when VPA VAULT PREFRENCE = GLOBAL")
    public void savedVPA_GLOBAL003(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH)
                .setMobile("8586004360")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.deleteSavedVPAList().get(0).click();
        cashierPage.buttonOk().click();

        String logs_req = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs_req).contains("\"merchantId\":\"qa12id70232557209005\"");
        Assertions.assertThat(logs_req).contains("\"assetValue\":\"9999661503@paytm\"");
        Assertions.assertThat(logs_req).contains("\"assetType\":\"VPA\"");
        Assertions.assertThat(logs_req).contains("\"targetType\":\"GLOBAL\"");
        Assertions.assertThat(logs_req).contains("\"userIdentifier\":\"8586004360\"");

        String logs_resp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs_resp).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(logs_resp).contains("\"responseCode\":\"BE1400001\"");
        Assertions.assertThat(logs_resp).contains("\"message\":\"SUCCESS\"");

    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction when VPA VAULT PREFRENCE = GLOBAL")
    public void savedVPA_GLOBAL004(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH)
                .setMobile("8586004363")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.deleteSavedVPAList().get(0).click();
        cashierPage.buttonDismiss().click();
        cashierPage.deleteSavedVPAList().get(0).isDisplayed();

    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI transaction when VPA VAULT PREFRENCE = GLOBAL")
    public void savedVPA_GLOBAL005(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH)
                .setMobile("8586004363")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.clickPgOverlay();
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.deleteSavedVPAList().get(0).click();
        cashierPage.displayDeleteVPA().getText().equals("Deleting this UPI ID will remove it from your saved options.");

    }


}