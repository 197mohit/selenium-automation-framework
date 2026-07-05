package com.paytm.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.MoreExpectedConditions;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.TextBox;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import java.io.IOException;

public class CheckoutJsCheckoutPage extends BasePage {

    private Report report;
    private boolean deleteCookie = true;
    /**
     * Currently on merchantpgpui1 url new theme is working
     * when CHECKOUTJS_LOAD_URL = https://pgp-automation.paytm.in/merchantpgpui/checkoutjs/merchants/{mid}  new theme will work
     * when CHECKOUTJS_LOAD_URL = https://pgp-automation.paytm.in/merchantpgpui1/checkoutjs/merchants/{mid} old theme will work
     */
    private static String CHECKOUTJS_LOAD_URL = "https://pgp-automation.paytm.in/merchantpgpui1/checkoutjs/merchants/{mid}";

    protected Button loadCheckoutjs() {
        return new Button(By.xpath("//button[contains(text(), 'Load Merchant JS')]"), getPageName(), "Load Merchant JS button");
    }

    protected Button initializeCheckoutJs() {
        return new Button(By.xpath("//button[contains(text(), 'Initialize CheckoutJS')]"), getPageName(), "Initialize checkoutjs button");
    }

    public void setDeleteCookie(boolean deleteCookie) {
        this.deleteCookie = deleteCookie;
    }

    protected Button invokeCheckoutJs() {
        return new Button(By.xpath("//button[contains(text(), 'Invoke CheckoutJS')]"), getPageName(), "Invoke CheckoutJS button");
    }

    protected TextBox mconfig() {
        return new TextBox(By.xpath("//textarea[@id='mconfig']"), getPageName(), "mconfig textbox");
    }

    public CheckoutJsCheckoutPage() {
        super("CheckoutJS Checkout Page");
        this.pageURL = LocalConfig.CHECKOUTJS_URL;
        this.report = Reporter.report;
    }

    public CheckoutJsCheckoutPage(String emi) {
        super("CheckoutJS EMI Checkout Page");
        this.pageURL = LocalConfig.CHECKOUTJS_EMI_URL;
        this.report = Reporter.report;
    }

    @Step("Load checkoutjs merchant configuration ")
    public MerchantConfig loadMerchantConfig(InitTxnDTO initTxnDTO, String theme) throws IOException {
        launchBrowser();
        executeJavaScript(setCheckoutjsLoadUrl(initTxnDTO, theme));
        loadCheckoutjs().click();
        DriverManager.getWebDriverElementWait().until(
                ExpectedConditions.not(ExpectedConditions.attributeToBe(By.id("mconfigContainer"), "class", "hide"))
        );
        String configString = mconfig().getText().trim();
        ObjectMapper mapper = new ObjectMapper();
        MerchantConfig config = mapper.readValue(configString, MerchantConfig.class);
        config.data.setAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .setOrderId(initTxnDTO.orderFromBody());
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName("TestingMerchant");
        return config;
    }

    @Step("Load checkoutjs merchant configuration")
    public MerchantConfig loadMerchantConfigwithoutdeletingcookie(InitTxnDTO initTxnDTO, String theme) throws IOException {
        launchBrowserwithoutdeletingcookie();
        executeJavaScript(setCheckoutjsLoadUrl(initTxnDTO, theme));
        loadCheckoutjs().click();
        DriverManager.getWebDriverElementWait().until(
                ExpectedConditions.not(ExpectedConditions.attributeToBe(By.id("mconfigContainer"), "class", "hide"))
        );
        String configString = mconfig().getText().trim();
        ObjectMapper mapper = new ObjectMapper();
        MerchantConfig config = mapper.readValue(configString, MerchantConfig.class);
        config.data.setAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .setOrderId(initTxnDTO.orderFromBody());
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName("TestingMerchant");
        return config;
    }

    @Step("Load checkoutjs merchant configuration for item based emi subvention ")
    public MerchantConfig loadMerchantConfig(String merchantid) throws IOException {
        launchBrowser();
        executeJavaScript(setCheckoutjsLoadUrl(merchantid));
        loadCheckoutjs().click();
        DriverManager.getWebDriverElementWait().until(
                ExpectedConditions.not(ExpectedConditions.attributeToBe(By.id("mconfigContainer"), "class", "hide"))
        );
        String configString = mconfig().getText().trim();
        ObjectMapper mapper = new ObjectMapper();
        MerchantConfig config = mapper.readValue(configString, MerchantConfig.class);
        config.merchant.setMid(merchantid)
                .setName("ItemBasedEMI");

        return config;
    }

    @Step("Load checkoutjs merchant configuration for amount based emi subvention ")
    public MerchantConfig loadMerchantConfigAmountStrategy(InitTxnDTO initTxnDTO, String theme) throws IOException {
        launchBrowser();
        executeJavaScript(setCheckoutjsLoadUrl(initTxnDTO, theme));
        loadCheckoutjs().click();
        DriverManager.getWebDriverElementWait().until(
                ExpectedConditions.not(ExpectedConditions.attributeToBe(By.id("mconfigContainer"), "class", "hide"))
        );
        String configString = mconfig().getText().trim();
        ObjectMapper mapper = new ObjectMapper();
        MerchantConfig config = mapper.readValue(configString, MerchantConfig.class);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName("AmountBasedEMI");
        config.emiSubvention.setstrategy("AMOUNT_BASED");
        config.emiSubvention.setitemsNullforAmountBasedTxn();
        config.emiSubvention.setsubventionAmount("20");
        return config;
    }
    @Step("Load checkoutjs merchant configuration for amount based emi subvention ")
    public MerchantConfig loadMerchantConfigAmountStrategy(String merchantid) throws IOException {
        launchBrowser();
        executeJavaScript(setCheckoutjsLoadUrl(merchantid));
        loadCheckoutjs().click();
        DriverManager.getWebDriverElementWait().until(
                ExpectedConditions.not(ExpectedConditions.attributeToBe(By.id("mconfigContainer"), "class", "hide"))
        );
        String configString = mconfig().getText().trim();
        ObjectMapper mapper = new ObjectMapper();
        MerchantConfig config = mapper.readValue(configString, MerchantConfig.class);
        config.merchant.setMid(merchantid)
                .setName("AmountBasedEMI");
        config.emiSubvention.setstrategy("AMOUNT_BASED");
        config.emiSubvention.setitemsNullforAmountBasedTxn();
        config.emiSubvention.setsubventionAmount("20");
        return config;
    }

    @Step("")
    public void createCheckoutJsOrder(MerchantConfig merchantConfig) {
        String config = merchantConfig.toString();
        int retryInitialize = 5;
        executeJavaScript("document.getElementById('mconfig').value='"+config+"'");
        pause(4);
        while((!invokeCheckoutJs().isEnabled()) && (retryInitialize >0)) {
            initializeCheckoutJs().click();
            pause(8);
            retryInitialize--;
        }
        if(!invokeCheckoutJs().isEnabled())
        {
            throw new SkipException("invokeCheckoutJs is not Enabled");
        }
        invokeCheckoutJs().waitUntilClickable();
        invokeCheckoutJs().click();
      /*
        do {
            if(invokeCheckoutJs().isEnabled())
                break;
            initializeCheckoutJs().click();
            pause(4);
        }while(!invokeCheckoutJs().isEnabled());*/
        /*DriverManager.getWebDriverElementWait().until(
                ExpectedConditions.not(ExpectedConditions.attributeToBe(By.xpath("//button[contains(text(), 'Invoke CheckoutJS')]"), "disabled", "true"))
        );*/
    }

    private String getCheckoutjsLoadUrl(String theme){
        String checkoutJsLoadUrl = LocalConfig.CHECKOUTJS_LOAD_URL;
        return checkoutJsLoadUrl;
    }

    protected String setCheckoutjsLoadUrl(InitTxnDTO initTxnDTO, String theme) {
        String mid = initTxnDTO.getBody().getMid();
        String CHECKOUTURL = getCheckoutjsLoadUrl(theme);
        CHECKOUTURL = CHECKOUTURL.replace("{mid}", mid);
        return "document.getElementById('merchantCheckout').value='" + CHECKOUTURL + "'";
    }
    private String setCheckoutjsLoadUrl(String merchantid) {
        String CHECKOUTURL = CHECKOUTJS_LOAD_URL;
        CHECKOUTURL = CHECKOUTURL.replace("{mid}", merchantid);
        return "document.getElementById('merchantCheckout').value='" + CHECKOUTURL + "'";
    }

    protected void launchBrowser() {
        DriverManager.getDriver().get(LocalConfig.AUTH_HOST + Constants.AuthAPIresource.OAUTH2);
        System.out.println("delete cookie is: " + this.deleteCookie);
        if (deleteCookie) {
            DriverManager.getDriver().manage().deleteAllCookies();
        }
        launch();
        if (deleteCookie) {
            DriverManager.getDriver().manage().deleteAllCookies();
        }
    }

    @Override
    public void waitUntilLoads() {
        DriverManager.getWebDriverPageWait().until(MoreExpectedConditions.documentIsReady());
    }

    protected void launchBrowserwithoutdeletingcookie() {
        DriverManager.getDriver().get(LocalConfig.AUTH_HOST + Constants.AuthAPIresource.OAUTH2);
        launch();
    }


}
