package com.paytm.pages;

import com.jcraft.jsch.Logger;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.PagePath;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.framework.api.CustomRequestSpecBuilder;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.*;
import com.paytm.utils.merchant.util.AuthUtil;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;


import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.paytm.apphelpers.CommonHelpers.emiCalc;
public class DynamicCurrencyConversionPage extends BasePage {

    private Report report;
    private boolean deleteCookie = true;

    public DynamicCurrencyConversionPage() {
        super("Dcc Page");
    }

    public boolean isDeleteCookie() {
        return deleteCookie;
    }

    public void setDeleteCookie(boolean deleteCookie) {
        this.deleteCookie = deleteCookie;
    }

    public UIElement usdCurrencyCheckMark() {
        return new RadioButton(By.xpath("//span[@id='frn-curr']"), getPageName(),
                "Currency-CheckMark");
    }

    public UIElement usdPriceBreakUpText() {
        return new UIElement(By.xpath("//span[@id='price-breakup-label']"), getPageName(),"usd Price Break Up Text");
    }

    public UIElement usdPriceBreakUp() {
        return new Link(By.xpath("//em[@id='pb-icon']"), getPageName(),"USD-PriceBreakUp");
    }

    public UIElement inrCurrencyCheckMark() {
        return new RadioButton(By.xpath("//label[@id='inr-option']/span[@class='ptm-checkmark']"), getPageName(),
                "Currency-CheckMark");
    }

    public UIElement inrPriceBreakUpText() {
        return new UIElement(By.xpath("//span[@id='inr-price-breakup-label']"), getPageName(),"inr Price Break Up Text");
    }

    public UIElement inrPriceBreakUp() {
        return new Link(By.xpath("//*[@id='inr-pb-icon']"), getPageName(),
                "USD-PriceBreakUp");
    }

    public UIElement usdConvenienceAmount() {
        return new UIElement(By.xpath("//span[@Id='convenience-label']/following-sibling::span[@id='frn-conv-amount']"), getPageName(), "UsdConvenience-Amount");
    }

    public UIElement inrConvenienceAmount() {
        return new UIElement(By.xpath("//span[@Id='inr-conv-label']/following-sibling::span[@id='inr-conv-amount']"), getPageName(), "InrConvenience-Amount");
    }

    public UIElement payInUsd() {
        return new Button(By.xpath("//button[@id='frn-btn']"), getPageName(), "PayInUsd-Button");
    }

    public UIElement payInInr() {
        return new Button(By.xpath("//button[@id='inr-btn']"), getPageName(), "PayInInr-Button");
    }

    public void selectCurrencyAndValidateConvenienceFee(String currency) {
        if(currency.equals("USD")) {
            waitUntilLoads();
            usdCurrencyCheckMark().click();
            usdPriceBreakUp().click();
            Assert.assertNotNull(usdConvenienceAmount().getText());
            payInUsd().click();
        }else if(currency.equals("INR")){
            waitUntilLoads();
            inrCurrencyCheckMark().click();
            inrPriceBreakUp().click();
            Assert.assertNotNull(inrConvenienceAmount().getText());
            payInInr().click();
        }
    }
}
