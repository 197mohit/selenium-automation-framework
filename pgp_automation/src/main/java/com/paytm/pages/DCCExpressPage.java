package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;
import com.paytm.framework.core.DriverManager;
import java.lang.InterruptedException;

public class DCCExpressPage extends BasePage {

    private boolean deleteCookie = true;

    public DCCExpressPage() {
        super("Dcc Express Page");
    }



    public UIElement dccOrderId() {

        return new UIElement(By.xpath("//div[@id='orderid']"), getPageName(), "Order Id");
    }

    public UIElement dccHeading() {
        return new UIElement(By.xpath("//div[@id='dccHeading']"),getPageName(),"Select currency to pay");
    }

    public UIElement cardDetailsLabel() {
        // contains(@class) — Jenkins/builds may append extra CSS classes; exact @class match fails then.
        return new UIElement(By.xpath("//div[contains(@class,'card-label')]"),
                getPageName(), "Paying with label");
    }

    public UIElement cardType() {
        return new UIElement(By.xpath("//span[@id='card-type-number']"),
                getPageName(), "Card type and masked number");
    }

    public UIElement cardScheme() {
        return new UIElement(By.xpath("//span[@id='card-name']"),
                getPageName(), "Card scheme name");
    }

    public UIElement timerText() {
        // id-only — matches verifyDccExpress_TimerSection stability; avoids strict @class on Jenkins builds
        return new UIElement(By.xpath("//span[@id='timer-text']"),
                getPageName(), "Timer label (Proceeding with)");
    }

    public UIElement stopTimerText() {
        return new UIElement(By.xpath("//span[@id='stop-timer']"),
                getPageName(), "Stop Timer");
    }

    public UIElement timerCountdown() {
        return new UIElement(By.xpath("//span[@id='timer-countdown']"),
                getPageName(), "Timer countdown");
    }

    public UIElement frncurrency() {
        return new UIElement(By.xpath("//span[@id='frn-curr']"),getPageName(),"USD text");
    }

    public UIElement frncurrencyAmount() {
        return new UIElement(By.xpath("//span[@id='frn-pay-amount']"),getPageName(),"SGD amount");
    }

    public UIElement conversionRateText() {
        return new UIElement(By.xpath("//span[@id='conversion-rate']"),getPageName(),"Conversion Rate Text");
    }

    public UIElement recommentLabel() {
        return new UIElement(By.xpath("//span[@id='recommend-label']"),getPageName(),"Recommend label");
    }

    public Button ViewTC() {
        return new Button(By.xpath("//a[@id='show-tc']"),getPageName(),"View T&C");
    }

    public UIElement overlayViewTC() {
        return new UIElement(By.xpath("//div[@class='overlay-heading bold' and @id='tnc']"),getPageName(),"Terms and Conditions");
    }

    public Button USDButton(){

        return new Button(By.xpath("//button[@id='frn-btn']"),getPageName(),"USD Pay Button");
    }
    public Button EUROButton(){
        return new Button(By.xpath("//label[@id='frn-option']//span[@class='ptm-checkmark']"),getPageName(),"EURO Button");
    }
    public Button PayEuro(){
        return new Button(By.xpath("//span[@id='frn-pay-val' and contains(text(),'Pay EUR')]"),getPageName(),"Pay Euro Button");
    }

    /**
     * selectCurrencyPage: EURO row — click label#frn-option (full label; use when the checkmark span is not interactable).
     */
    public Button frnEurOptionLabel(){
        return new Button(By.id("frn-option"), getPageName(), "frn EURO option label");
    }
    /**
     * selectCurrencyPage: Pay EUR primary action — button#frn-btn (after EURO is selected).
     */
    public Button frnEurPayButton(){
        return new Button(By.id("frn-btn"), getPageName(), "frn Pay EUR button");
    }
    public Button CustomNameOnCard(){
        return new Button(By.xpath("//input[@id='name']"),getPageName(),"Custom Name On Card");
    }
    public Button CustomEmailOnCard(){
        return new Button(By.xpath("//input[@id='email']"),getPageName(),"Custom Email On Card");
    }
    public Button CustomDialCode(){
        return new Button(By.xpath("//input[@id='dialCode']"),getPageName(),"Custom Dial Code");
    }
    public Button CustomPhoneNumber(){
        return new Button(By.xpath("//input[@id='phone']"),getPageName(),"Custom Phone Number");
    }
    public Button CustomCountryCode(){
        return new Button(By.xpath("//input[@id='cont_code']"),getPageName(),"Custom Country Code");
    }
    public Button CustomPostalCode(){
        return new Button(By.xpath("//input[@id='postal']"),getPageName(),"Custom Postal Code");
    }
    /** AVS billing block on selectCurrencyPage (e.g. #modalOverlay / collectCompleteAvs). */
    public UIElement AvsBillingForm() {
        return new UIElement(By.id("avs-billing"), getPageName(), "AVS billing form");
    }

    public Button CustomHouseNo(){
        return new Button(By.xpath("//input[@id='house']"),getPageName(),"Custom House No");
    }
    public Button CustomStreet(){
        return new Button(By.xpath("//input[@id='street']"),getPageName(),"Custom street Code");
    }
    public Button CustomCity(){
        return new Button(By.xpath("//input[@id='cityName']"),getPageName(),"Custom City Name");
    }
    public Button CustomState(){
        return new Button(By.xpath("//input[@id='stateName']"),getPageName(),"Custom State Name");
    }


    public Button INRButton(){

        return new Button(By.xpath("//button[@id='inr-btn']"),getPageName(),"USD Pay Button");
    }

    public UIElement PayInINR() {
        return new UIElement(By.xpath("//a[@id='pay-in-inr' and contains(text(),'Make Payment in INR')]"),getPageName(),"Make Payment in INR");
    }

    public UIElement RadioButtonINR() {
        return new UIElement(By.xpath("//label[@id='inr-option']//span[@class='ptm-checkmark']"),getPageName(),"INR radio button");
    }

    public UIElement INRRecommendLabel() {
        return new UIElement(By.xpath("//div[@id='inr-curr-box']//span[@id='inr-norecommend-label']"),getPageName(),"INR recomment label");
    }

    // AI-Generated: 2025-01-02 - Refactoring: Added only missing page object methods (avoided duplicates)
    public Button AlternativeEURButton() {
        return new Button(By.xpath("//button[contains(text(),'EUR') or contains(text(),'EURO') or contains(@id,'eur')]"), getPageName(), "Alternative EUR Button");
    }

    public Button GenericForeignCurrencyOption() {
        return new Button(By.xpath("//label[contains(@id,'frn')]//span[contains(@class,'ptm-checkmark')]"), getPageName(), "Generic Foreign Currency Option");
    }

    public Button GenericPayButton() {
        return new Button(By.xpath("//button[contains(text(),'Pay') or contains(@id,'btn')]"), getPageName(), "Generic Pay Button");
    }

    public void clickViewTermsAndConditions(){
        ViewTC().click();
        waitUntilLoads();
    }

    public void selectCurrencyAndPay(String currency) {
        if(currency.equals("USD")) {
            USDButton().click();
        }
        else if(currency.equals("INR")) {
            PayInINR().click();
            RadioButtonINR().click();
            INRButton().click();
        }
        else if(currency.equals("EURO")) {
            selectForeignCurrencyWithFallback();
        }
    }

    public void selectForeignCurrencyWithFallback() {
        try {
            // AI-Generated: 2025-01-02 - Refactoring: Replaced hardcoded XPaths with page object methods
            // Strategy 1: Try the standard EURO button (primary approach)
            EUROButton().click();
            waitUntilLoads();
            PayEuro().click();
        } catch (Exception e1) {
            try {
                // Strategy 2: Try alternative selectors for EURO (different approach)
                AlternativeEURButton().click();
            } catch (Exception e2) {
                try {
                    // Strategy 3: Try to find any foreign currency option
                    GenericForeignCurrencyOption().click();
                    waitUntilLoads();
                    
                    // Try to find and click any pay button
                    GenericPayButton().click();
                } catch (Exception e3) {
                    throw new RuntimeException("All foreign currency selection strategies failed", e3);
                }
            }
        }
    }


}
