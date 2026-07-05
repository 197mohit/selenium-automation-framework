package com.paytm.pages;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.MoreExpectedConditions;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.UIElement;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Arrays;

import static com.paytm.appconstants.Constants.Theme.MERCHANT4;

public class CheckoutPage extends BasePage {

    private Report report;
    private boolean deleteCookie = true;

    public CheckoutPage() {
        super("Checkout Page");
        this.pageURL = LocalConfig.THEIA_URL;
        this.report = Reporter.report;
    }

    public boolean isDeleteCookie() {
        return deleteCookie;
    }

    public void setDeleteCookie(boolean deleteCookie) {
        this.deleteCookie = deleteCookie;
    }

    private Button pay() {
        return new Button(By.cssSelector("form#target input[value='Pay']"), getPageName(), "pay");
    }
    private Button customProcessTransactionPay() {
        return new Button(By.cssSelector("form#WrapperForm input[value='Pay']"), getPageName(), "pay");
    }

    private Button nativePay() {
        return new Button(By.cssSelector("form#nativeForm button[value='Pay']"), getPageName(), "Native Pay");

    }

    private Button nativeHoldPay() {
        return new Button(By.cssSelector("form#nativeForm button[value='Pay Hold']"), getPageName(), "Native Hold Pay");
    }

    private UIElement nativeHoldResponseTable() {
        return new UIElement(By.id("NativeResponseTable"), getPageName(), "Native Hold response table");
    }

    public UIElement nativeHoldHtmlResponseBox() {
        return new UIElement(By.id("Native_OLD_RESPONSE"), getPageName(), "Native hold Html resposne"){
            @Override
            public String getText() {
                return this.getAttribute("value");
            }
        };
    }

    public Button postNativeHtmlResponse() {
        return new Button(By.xpath("//*[contains(text(), 'Post HTML')]"), getPageName(), "postNativeHtml button");
    }

    private Button appInvokePay() {
        return new Button(By.id("appInvokePay"), getPageName(), "App Invoke Pay");
    }

    private Button nativePlusAutoPay() {
        return new Button(By.id("nativePlusAutoPay"), getPageName(), "Native+ Auto Pay");
    }

    private Button encryptedPay() {
        return new Button(By.xpath("//form[@id='SbiForm']//input[@value='Pay']"), getPageName(), "SbiForm Pay");
    }

    private String fillEncryptedTxnDetails(OrderDTO checkoutDTO) {
        return "document.getElementById('MID_ENC').value='" + checkoutDTO.getMID() + "';" +
                "document.getElementById('ENC_DATA').value='" + checkoutDTO.getENC_DATA() + "';"
                ;
    }

    public Button payButtonConvineance() {
        return new Button(By.xpath("//button[@id='payButton1']"), getPageName(), "Pay Button");
    }

    private String fillTransactionDetails(OrderDTO checkoutDTO) {
        return "document.getElementById('orderid').value='" + checkoutDTO.getORDER_ID() + "';" +
                "document.getElementsByName('AUTH_MODE')[0].value='" + checkoutDTO.getAUTH_MODE() + "';" +
                //"document.getElementsByName('TOKEN_TYPE')[0].value='" + checkoutDTO.getTOKEN_TYPE() + "';" +
                "document.getElementsByName('TXN_AMOUNT')[0].value='" + checkoutDTO.getTXN_AMOUNT() + "';" +
                "document.getElementsByName('REQUEST_TYPE')[0].value='" + checkoutDTO.getREQUEST_TYPE() + "';" +
                "document.getElementsByName('planId')[0].value='" + checkoutDTO.getPlanId() + "';" +
                "document.getElementsByName('EMI_OPTIONS')[0].value='" + checkoutDTO.getEMI_OPTIONS() + "';" +
                "document.getElementsByName('subwalletAmount')[0].value='" + checkoutDTO.getSubwallet_Details() + "';" +
                "document.getElementsByName('IS_SAVED_CARD')[0].value='" + checkoutDTO.getIsSaveCard() + "';" +
                "document.getElementsByName('cardTokenRequired')[0].value='" + checkoutDTO.getCardTokenRequired() + "';" +
                "document.getElementsByName('MOBILE_NO')[0].value='" + checkoutDTO.getContactNumber() + "';" +
                "document.getElementsByName('MERC_UNQ_REF')[0].value='" + checkoutDTO.getMERC_UNQ_REF() + "';" +
                "document.getElementById('templateId').value='" + checkoutDTO.getTemplateId() + "';" +
                "document.getElementsByName('bId')[0].value='" + checkoutDTO.getbId() + "';" +
                "document.getElementsByName('corporateCustId')[0].value='" + checkoutDTO.getCorporateCustId() + "';"+
                "document.getElementsByName('appVersion')[0].value='" + checkoutDTO.getAppVersion() + "';" +
                "document.getElementsByName('GOODS_INFO')[0].value='" + checkoutDTO.getGOODS_INFO() + "';" +
                "document.getElementsByName('UDF_2')[0].value='" + checkoutDTO.getUDF2() + "';" +
                "document.getElementsByName('LINK_NAME')[0].value='" + checkoutDTO.getLinkName() + "';" +
                "document.getElementsByName('SHORT_URL')[0].value='" + checkoutDTO.getShortUrl() + "';" +
                "document.getElementsByName('LONG_URL')[0].value='" + checkoutDTO.getLongUrl() + "';" +
                "document.getElementsByName('LINK_DESCRIPTION')[0].value='" + checkoutDTO.getLinkDescription() + "';" +
                "document.getElementsByName('LINK_ID')[0].value='" + checkoutDTO.getLINKID() + "';" +
                "document.getElementsByName('CALLBACK_URL')[0].value='" + checkoutDTO.getCallBackURL() + "';" +
                "document.getElementsByName('ultimateBeneficiaryName')[0].value='" + checkoutDTO.getUltimateBeneficiaryName()+ "';"
                ;
    }

    private String fillNativeTransactionDetails(OrderDTO checkoutDTO) {

        return "document.getElementsByName('mid')[0].value='" + checkoutDTO.getNative_mid() + "';" +
                "document.getElementsByName('orderId')[0].value='" + checkoutDTO.getNative_orderId() + "';" +
                "document.getElementsByName('channelId')[0].value='" + checkoutDTO.getNative_channelId() + "';" +
                "document.getElementsByName('txnToken')[0].value='" + checkoutDTO.getNative_txnToken() + "';" +
                "document.getElementsByName('paymentMode')[0].value='" + checkoutDTO.getNative_paymentMode() + "';" +
                "document.getElementsByName('cardInfo')[0].value='" + checkoutDTO.getNative_cardInfo() + "';" +
                "document.getElementsByName('authMode')[0].value='" + checkoutDTO.getNative_authMode() + "';" +
                "document.getElementsByName('channelCode')[0].value='" + checkoutDTO.getNative_channelCode() + "';";
    }

    private String fillUserDetails(OrderDTO checkoutDTO) {

        return "document.getElementById('CUST_ID').value='" + checkoutDTO.getCUST_ID() + "';" +
                "document.getElementById('MSISDN').value='" + checkoutDTO.getMobileNumber() + "';" +
                "document.getElementsByName('SSO_TOKEN')[0].value='" + checkoutDTO.getSSO_TOKEN() + "';" +
                "document.getElementsByName('PAYTM_TOKEN')[0].value='" + checkoutDTO.getPAYTM_TOKEN() + "';" +
                "document.getElementsByName('accountNumber')[0].value='" + checkoutDTO.getAccountNumber() + "';" +
                "document.getElementsByName('PROMO_CAMP_ID')[0].value='" + checkoutDTO.getPROMO_CAMP_ID() + "';" +
                "document.getElementById('EMAIL').value='" + checkoutDTO.getEMAIL() + "';";

    }

    private String fillMerchantDetails(OrderDTO checkoutDTO) {
        return "document.getElementById('MID').value='" + checkoutDTO.getMID() + "';" +
                "document.getElementById('merchantKey').value='" + checkoutDTO.getMerchantKey() + "';" +
                "document.getElementById('INDUSTRY_TYPE_ID').value='" + checkoutDTO.getINDUSTRY_TYPE_ID() + "';" +
                "document.getElementById('WEBSITE').value='" + checkoutDTO.getWEBSITE() + "';" +
                "document.getElementById('THEME').value='" + checkoutDTO.getTHEME() + "';" +
                "document.getElementById('CHANNEL_ID').value='" + checkoutDTO.getCHANNEL_ID() + "';";
    }


    private String fillPaymentDetails(OrderDTO checkoutDTO) {
        return "document.getElementsByName('BANK_CODE')[0].value='" + checkoutDTO.getBANK_CODE() + "';" +
                "document.getElementsByName('PAYMENT_DETAILS')[0].value='" + checkoutDTO.getPAYMENT_DETAILS() + "';" +
                "document.getElementsByName('STORE_CARD')[0].value='" + checkoutDTO.getSTORE_CARD() + "';" +
                "document.getElementsByName('addMoney')[0].value='" + checkoutDTO.getAddMoney() + "';" +
                "document.getElementsByName('WALLET_AMOUNT')[0].value='" + checkoutDTO.getWALLET_AMOUNT() + "';" +
                "document.getElementsByName('CC_BILL_NO')[0].value='" + checkoutDTO.getCC_BILL_NO() + "';" +
                "document.getElementsByName('PAYMENT_TYPE_ID')[0].value='" + checkoutDTO.getPAYMENT_TYPE_ID() + "';" +
                "document.getElementById('selbank').value='" + checkoutDTO.getPAYMENT_MODE_DISABLE() + "';" +
                "document.getElementsByName('PAYMENT_MODE_ONLY')[0].value='" + checkoutDTO.getPAYMENT_MODE_ONLY() + "';" +
                "document.getElementsByName('extendInfo')[0].value='" + checkoutDTO.getExtendInfo() + "';" +
                "document.getElementsByName('splitSettlementInfo')[0].value='" + checkoutDTO.getSplitSettlementInfo() + "';"+
                "document.getElementsByName('PAYMENT_MODE_DISABLE')[0].value='" + checkoutDTO.getPAYMENT_MODE_DISABLE() + "';"
                ;
    }

    private String fillSubscriptionDetails(OrderDTO checkoutDTO) {
        return "document.getElementsByName('SUBS_START_DATE')[0].value='" + checkoutDTO.getSUBS_START_DATE() + "';" +
                "document.getElementsByName('SUBS_EXPIRY_DATE')[0].value='" + checkoutDTO.getSUBS_EXPIRY_DATE() + "';" +
                "document.getElementsByName('SUBS_PPI_ONLY')[0].value='" + checkoutDTO.getSUBS_PPI_ONLY() + "';" +
                "document.getElementsByName('SUBS_AMOUNT_TYPE')[0].value='" + checkoutDTO.getSUBS_AMOUNT_TYPE() + "';" +
                "document.getElementsByName('SUBS_MAX_AMOUNT')[0].value='" + checkoutDTO.getSUBS_MAX_AMOUNT() + "';" +
                "document.getElementsByName('SUBS_FREQUENCY')[0].value='" + checkoutDTO.getSUBS_FREQUENCY() + "';" +
                "document.getElementsByName('SUBS_FREQUENCY_UNIT')[0].value='" + checkoutDTO.getSUBS_FREQUENCY_UNIT() + "';" +
                "document.getElementsByName('SUBS_GRACE_DAYS')[0].value='" + checkoutDTO.getSUBS_GRACE_DAYS() + "';" +
                "document.getElementsByName('SUBS_ENABLE_RETRY')[0].value='" + checkoutDTO.getSUBS_ENABLE_RETRY() + "';" +
                "document.getElementsByName('SUBS_RETRY_COUNT')[0].value='" + checkoutDTO.getSUBS_RETRY_COUNT() + "';" +
                "document.getElementsByName('SUBS_PAYMENT_MODE')[0].value='" + checkoutDTO.getSUBS_PAYMENT_MODE() + "';" +
                "document.getElementsByName('SAVED_CARD_ID')[0].value='" + checkoutDTO.getSAVED_CARD_ID() + "';" +
                "document.getElementsByName('SUBS_FREQUENCY_UNIT')[0].value='" + checkoutDTO.getSUBS_FREQUENCY_UNIT() + "';" +
                "document.getElementsByName('ACCOUNT_REF_ID')[0].value='" + checkoutDTO.getACCOUNT_REF_ID() + "';" +
                "document.getElementsByName('USER_NAME')[0].value='" + checkoutDTO.getUSER_NAME() + "';" +
                "document.getElementsByName('bankIfsc')[0].value='" + checkoutDTO.getBANK_IFSC() + "';" +
                "document.getElementsByName('subscriptionPurpose')[0].value='" + checkoutDTO.getSubscriptionPurpose() + "';" +
                "document.getElementsByName('CONNECTION_TYPE')[0].value='" + checkoutDTO.getCONNECTION_TYPE() + "';";
    }

    private String fillNativeDetails(OrderDTO checkoutDTO) {
        return "document.getElementsByName('mid')[0].value='" + checkoutDTO.getMID() + "';" +
                "document.getElementsByName('aggMid')[0].value='" + checkoutDTO.getAggrMid() + "';" +
                "document.getElementsByName('orderId')[0].value='" + checkoutDTO.getORDER_ID() + "';" +
                "document.getElementsByName('channelId')[0].value='" + checkoutDTO.getCHANNEL_ID() + "';" +
                "document.getElementsByName('txnToken')[0].value='" + checkoutDTO.getTXN_TOKEN() + "';" +
                "document.getElementsByName('paymentMode')[0].value='" + checkoutDTO.getPAYMENT_TYPE_ID() + "';" +
                "document.getElementsByName('cardInfo')[0].value='" + checkoutDTO.getCardInfo() + "';" +
                "document.getElementsByName('authMode')[0].value='" + checkoutDTO.getAUTH_MODE() + "';" +
                "document.getElementsByName('channelCode')[0].value='" + checkoutDTO.getChannelCode() + "';" +
                "document.getElementsByName('saveForFuture')[0].value='" + checkoutDTO.getSTORE_CARD() + "';" +
                "document.getElementsByName('paymentFlow')[0].value='" + checkoutDTO.getPaymentFlow() + "';" +
                "document.getElementsByName('payerAccount')[0].value='" + checkoutDTO.getPayerAccount() + "';" +
                "document.getElementsByName('planId')[1].value='" + checkoutDTO.getPlanId() + "';" +
                "document.getElementsByName('account_number')[0].value='" + checkoutDTO.getAccount_number() + "';" +
                "document.getElementsByName('accountNumber')[1].value='" + checkoutDTO.getAccountNumber() + "';" +
                "document.getElementsByName('encCardInfo')[0].value='" + checkoutDTO.getEncCardInfo() + "';" +
                "document.getElementsByName('REQUEST_TYPE')[1].value='" + checkoutDTO.getREQUEST_TYPE() + "';" +
                "document.getElementsByName('mpin')[0].value='" + checkoutDTO.getMpin() + "';" +
                "document.getElementsByName('SUBSCRIPTION_ID')[1].value='" + checkoutDTO.getSUBSCRIPTION_ID() + "';" +
                "document.getElementsByName('bankIfsc')[1].value='" + checkoutDTO.getBankIfsc() + "';" +
                "document.getElementsByName('ACCOUNT_TYPE')[0].value='" + checkoutDTO.getACCOUNT_TYPE() + "';" +
                "document.getElementById('WEBSITE').value='" + checkoutDTO.getWEBSITE() + "';" +
                "document.getElementsByName('USER_NAME')[1].value='" + checkoutDTO.getUSER_NAME() + "';" +
                "document.getElementsByName('templateId')[1].value='" + checkoutDTO.getTemplateID() + "';" +
                "document.getElementsByName('riskExtendInfo')[1].value='" + checkoutDTO.getRiskExtendInfo() + "';"+
                "document.getElementsByName('storeInstrument')[0].value='" + checkoutDTO.getStoreInstrument() + "';" +
                "document.getElementsByName('MERC_UNQ_REF')[1].value='" + checkoutDTO.getMERC_UNQ_REF() + "';" +
                "document.getElementsByName('emiType')[0].value='" + checkoutDTO.getEMI_TYPE() + "';" +
                "document.getElementsByName('extendInfo')[1].value='" + checkoutDTO.getExtendInfo() + "';" +
                "document.getElementsByName('risk_extended_info')[0].value='" + checkoutDTO.getRisk_extended_info() + "';"
                ;
    }

    /*
     * Author  : Rajesh Kumar
     * Date : 12/12/2018
     */
    private String fillMutualFundNative(OrderDTO checkoutDTO) {
        return "document.getElementsByName('mid')[0].value='" + checkoutDTO.getMID() + "';" +
                "document.getElementsByName('orderId')[0].value='" + checkoutDTO.getORDER_ID() + "';" +
                "document.getElementsByName('channelId')[0].value='" + checkoutDTO.getCHANNEL_ID() + "';" +
                "document.getElementsByName('txnToken')[0].value='" + checkoutDTO.getTXN_TOKEN() + "';" +
                "document.getElementsByName('paymentMode')[0].value='" + checkoutDTO.getPAYMENT_TYPE_ID() + "';" +
                "document.getElementsByName('cardInfo')[0].value='" + checkoutDTO.getCardInfo() + "';" +
                "document.getElementsByName('authMode')[0].value='" + checkoutDTO.getAUTH_MODE() + "';" +
                "document.getElementsByName('channelCode')[0].value='" + checkoutDTO.getChannelCode() + "';" +
                "document.getElementsByName('saveForFuture')[0].value='" + checkoutDTO.getSTORE_CARD() + "';" +
                "document.getElementsByName('paymentFlow')[0].value='" + checkoutDTO.getPaymentFlow() + "';" +
                "document.getElementsByName('payerAccount')[0].value='" + checkoutDTO.getPayerAccount() + "';" +
                "document.getElementsByName('planId')[0].value='" + checkoutDTO.getPlanId() + "';" +
                "document.getElementsByName('encCardInfo')[0].value='" + checkoutDTO.getEncCardInfo() + "';" +
                "document.getElementsByName('aggMid')[0].value='" + checkoutDTO.getAggrMid() + "';" +
                "document.getElementsByName('account_number')[0].value='" + checkoutDTO.getAccountNumber() + "';" +
                "document.getElementsByName('mpin')[0].value='" + checkoutDTO.getMpin() + "';";

    }//  fillMutualFundNative ends here

    private String fillAppInvoke(OrderDTO checkoutDTO) {
        return "document.getElementsByName('mid')[1].value='" + checkoutDTO.getMID() + "';" +
                "document.getElementsByName('orderId')[1].value='" + checkoutDTO.getORDER_ID() + "';" +
                "document.getElementsByName('txnToken')[1].value='" + checkoutDTO.getTXN_TOKEN() + "';" +
                "document.getElementsByName('fetchAllPaymentOffers')[0].value='" + checkoutDTO.getFetchAllPaymentOffers() + "';" +
                "document.getElementsByName('applyPaymentOffer')[0].value='" + checkoutDTO.getApplyPaymentOffer() + "';" ;
    }

    private String fillCustomTransactionDetails(OrderDTO checkoutDTO) {
        return "document.getElementById('msg').value='" + checkoutDTO.getMsg() + "';" +
                "document.getElementById('WRAPPERMID').value='" + checkoutDTO.getMID() + "';" +
                "document.getElementById('merchantCode').value='" + checkoutDTO.getMerchantCode() + "';" +
                "document.getElementById('ORDER_ID').value='" + checkoutDTO.getORDER_ID() + "';";
    }

    @Step("Initiate txn")
    public void createOrder(OrderDTO order) {
//        ExtentTestManager.getTest().pass("initiate txn");
        launchBrowser(order);
//        ExtentTestManager.getTest().info(MarkupHelper.createCodeBlock(order.toString(), CodeLanguage.JSON));
//        ExtentTestManager.getTest().info(MarkupHelper.createCodeBlock(order.asQuery()));
        executeJavaScript(
                fillTransactionDetails(order) +
                        fillUserDetails(order) +
                        fillMerchantDetails(order) +
                        fillPaymentDetails(order) +
                        fillSubscriptionDetails(order) +
                        fillNativeTransactionDetails(order)
        );
        pay().click();
        this.waitUntilLoads();
    }

    @Step("Initiate Encrypted request")
    public void createEncryptedOrder(OrderDTO order) {
        launchBrowser(order);
        executeJavaScript(fillEncryptedTxnDetails(order));
        encryptedPay().click();
        this.waitUntilLoads();
    }

    @Deprecated
    @Step("Process txn")
    public void createNativeOrder(OrderDTO checkoutDTO) {
        createNativeOrder(checkoutDTO, false);
    }

    @Step("Process txn")
    public void createNativeOrder(OrderDTO checkoutDTO, Boolean isNativePlus) {
        launchBrowser(checkoutDTO);
        this.report.info("Fill txn details");
        System.out.println(fillNativeDetails(checkoutDTO));
        executeJavaScript(fillNativeDetails(checkoutDTO));
        if (isNativePlus) {
            nativePlusAutoPay().click();
        } else {
            nativePay().click();
        }
    }

    public void createNHoldNativeOrder(OrderDTO checkoutDTO, Boolean isNativePlus) {
        launchBrowser(checkoutDTO);
        this.report.info("Fill txn details");
        System.out.println(fillNativeDetails(checkoutDTO));
        executeJavaScript(fillNativeDetails(checkoutDTO));
        if (isNativePlus) {
            nativePlusAutoPay().click();
        } else {
            nativeHoldPay().click();
            DriverManager.getWebDriverElementWait().until(
                    ExpectedConditions.not(ExpectedConditions.attributeToBe(By.id("NativeResponseTable"), "hidden", "true"))
            );
        }
    }

    // adding create order function for Mutual fund Checkout
    // Added on: 12/12/18 - Rajesh
    public void createMFOrder(OrderDTO checkoutDTO) {
        launchBrowser(checkoutDTO);
        executeJavaScript(fillMutualFundNative(checkoutDTO));
        nativePay().click();
    }

    @Step()
    public void createAppInvokeOrder(OrderDTO checkoutDto) {
        launchBrowser(checkoutDto);
        this.report.info("Fill AppInvoke txn details");
        executeJavaScript(fillAppInvoke(checkoutDto));
        appInvokePay().click();
    }



    private void launchBrowser(OrderDTO orderDTO) {
        if (Arrays.asList(MERCHANT4).contains(orderDTO.getTHEME())) {
            this.pageURL = LocalConfig.THEIA_SECONDARY_URL;
        } else {
            if (orderDTO.getORDER_ID().startsWith("T2-theia") || orderDTO.getORDER_ID().startsWith("T1-Retry")) {
                this.pageURL = LocalConfig.THEIA_URL;
            } else if (orderDTO.getORDER_ID().startsWith("T1-theia") || orderDTO.getORDER_ID().startsWith("T2-Retry")) {
                this.pageURL = LocalConfig.THEIA_SECONDARY_URL;
            } else {
                this.pageURL = LocalConfig.THEIA_URL;
            }

        }
        if ("hi-IN".equals(orderDTO.getLocale())) {
            this.pageURL = LocalConfig.THEIA_HINDI_URL;
        }
        DriverManager.getDriver().get(LocalConfig.AUTH_HOST + Constants.AuthAPIresource.OAUTH2);
        System.out.println("delete cookie is: " + this.deleteCookie);
        if (deleteCookie) {
            Reporter.report.info("deleting cookie for: " + LocalConfig.AUTH_HOST + Constants.AuthAPIresource.OAUTH2);
            DriverManager.getDriver().manage().deleteAllCookies();
        }
        DriverManager.getDriver().get(LocalConfig.PGP_HOST);
        if(deleteCookie){
            Reporter.report.info("deleting cookie for: " + LocalConfig.PGP_HOST);
            DriverManager.getDriver().manage().deleteAllCookies();
        }
        launch();
        if (deleteCookie) {
            Reporter.report.info("deleting cookie for: " + this.pageURL);
            DriverManager.getDriver().manage().deleteAllCookies();
        }
    }

    @Step("Wait for Page to Load")
    @Override
    public void waitUntilLoads() {
        DriverManager.getWebDriverPageWait().until(MoreExpectedConditions.documentIsReady());
    }



    @Step("Initiate txn")
    public void createCustomPTCOrder(OrderDTO order) {
        launchBrowser(order);
        executeJavaScript(
                fillCustomTransactionDetails(order));
        customProcessTransactionPay().click();
        this.waitUntilLoads();
    }
}
