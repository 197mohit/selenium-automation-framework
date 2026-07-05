/*
package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CashierPageMerchantLow5 extends CashierPage {

    CashierPageMerchantLow5() {
        super("Cashier page merchant low 5 theme");
    }

    @Override
    public Button buttonPGPayNow() {
        return new Button(By.cssSelector(".cards-tabs:not(.hide) #btnSubmit"), getPageName(), "textBoxCVVNumber");

    }

    @Override
    public Button buttonPpblSumbit() {
        return new Button(By.id("pbSubmit"), getPageName(), "BtnPPBLSubmit");
    }

    @Override
    public Button BtnRemoveSavedCard() {
        return null;
    }

    @Override
    public Button btnRemoveConfirmYes() {
        return null;
    }

    public TextBox textBoxPassCode() {
        return new TextBox(By.id("paymentBankTxtPassCode"), getPageName(), "paymentBankTxtPassCode");
    }

    @Deprecated
    @Override
    public CheckBox walletBalanceCheck() {
        return new CheckBox(By.id("paytmCashCB"), getPageName(), "paytm wallet checkbox");
    }

    @Override
    public CheckBox checkBoxWallet() {
        return new CheckBox(By.id("paytmCashCB"), getPageName(), "paytm wallet checkbox");
    }

    @Deprecated
    @Override
    public CheckBox ppblBalanceCheck() {
        return null;
    }

    @Override
    public CheckBox checkBoxPpbl() {
        return new CheckBox(By.id("paymentBankCheckbox"), getPageName(), "paytm PPBL checkbox");
    }

    @Override
    public CheckBox checkboxPaytmCC() {
        return new CheckBox(By.id("paytmCC"), getPageName(), "checkbox paytmcc");

    }

    @Override
    public TextBox textBoxCardNumber() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) #card-number"), getPageName(), "textBoxCardNumber");
    }

    @Override
    public Select dropdownExpiryMonth() {
        return new Select(By.cssSelector(".cards-tabs:not(.hide) #ccExpMnth"), getPageName(), "dropdownExpiryMonth");
    }

    @Override
    public Select dropdownExpiryYear() {
        return new Select(By.cssSelector(".cards-tabs:not(.hide) #ccExpYr"), getPageName(), "dropdownExpiryYear");
    }

    @Override
    public TextBox textBoxCVVNumber() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) #ccCvvBox"), getPageName(), "textBoxCVVNumber");
    }

    @Override
    public TextBox textBoxSavedCardCVV() {
        return new TextBox(By.cssSelector(".cvv[style='display: block;'] input"), getPageName(), "textBoxSavedCardCVV");
    }

    @Override
    public TextBox lblSavedCardCVV() {
        return new TextBox(By.cssSelector(".cvv[style='display: block;'] label"), getPageName(), "lblSavedCardCVV");
    }


    @Override
    public RadioButton radioButtonPaytmPostpaid() {
        return new RadioButton(By.cssSelector("#paytmCC"), getPageName(), "rdbtnPostPaid");
    }

    @Override
    public Button buttonPostPaidPayNow() {
        return new Button(By.cssSelector("#postSubmit"), getPageName(), "btnPostPaidSubmit");
    }

    */
/*public RadioButton radioButtonWallet() {
        return new RadioButton(By.cssSelector("#paytmCashCB"), getPageName(), " wallet radio button");
    }*//*


    @Override
    public RadioButton radioBtnSaveCard() {
        return new RadioButton(By.cssSelector("#saveCard"), getPageName(), "radioBtnSaveCard");
    }

    @Override
    public TextBox textBoxVPA() {
        return new TextBox(By.cssSelector("#upiForm input[name='VIRTUAL_PAYMENT_ADDRESS']"), getPageName(), "textBoxVPA");
    }

    @Override
    public Button savedCardPayNow() {
        return new Button(By.cssSelector(".cvv[style='display: block;'] button[class='blue-btn']"), getPageName(), "rdbtnATMPin");
    }

    @Override
    public UIElement lblErrMsg() {
        return new UIElement(By.id("errorMsg"), getPageName(), "error msg");
    }

    public List<WebElement> savedCardNumbers() {
        List<WebElement> elements = DriverManager.getDriver().findElements(By.xpath("//input[@name ='savedCardId']/following-sibling::label"));
        return elements;
    }


    @Override
    public RadioButton radioBtnSelectSavedCard(String cardId) {
        String id = "cvv_" + cardId;
        return new RadioButton(By.id(id), "cashier page", "saved card Id:" + cardId);
    }

    @Override
    public Button rdbtnSavedCardATMPin() {
        return new Button(By.xpath("//span[text()='Use ATM PIN']/preceding-sibling::input[@class='pcb checkbox paymentIdebit fl']"), getPageName(), "rdbtnATMPin");
    }

    @Override
    public UIElement radioBtnSavedCard(String cardId) {
        String id = "cvv_" + cardId;
        return new UIElement(By.id(id), getPageName(), "saved card Id:" + cardId);
    }

    @Override
    public Button rdbtnSavedCard3DPin() {
        return new Button(By.xpath("//span[text()='Enter 3D secure PIN/OTP in the next step']/preceding-sibling::input[@class='pcb checkbox paymentIdebit fl']"), getPageName(), "rdbtnATMPin");
    }

    @Override
    public UIElement savedCardImage() {
        return new Button(By.xpath("//*[@class='cvv' and @style='display: block;']/parent::div/span"), getPageName(), "rdbtnATMPin");
    }

    @Override
    public UIElement loginModal() {
        return new UIElement(By.id("#login-stitch"), getPageName(), "loginModal");
    }

    @Override
    public Link btnLogin() {
        return new Link(By.cssSelector(".login-button.ml16.login-button-style"), getPageName(), "btnLogin");
    }

    @Override
    protected void signin(String mobileNumber, String password) {
        if (!loginModal().isDisplayed()) {
            btnLogin().waitUntilPresent();
            btnLogin().waitUntilClickable();
            btnLogin().click();
        }
        textBoxPhoneNumber().waitUntilEditable();
        textBoxPhoneNumber().clearAndType(mobileNumber);
        textBoxPassword().clearAndType(password);
        buttonSecureSignIn().click();
        waitUntilLoads();
    }

    @Override
    public UIElement paymentContainer() {
        return new UIElement(By.cssSelector(".cards-tabs"), getPageName(), "paymentContainer");
    }

    @Override
    public UIElement lblCVV() {
        return new UIElement(By.cssSelector("label[for='cvvNumber']"), getPageName(), "labelCVV");
    }

    @Override
    public UIElement lblCardNo() {
        return new UIElement(By.cssSelector("label[for='cardNumber']"), getPageName(), "labelCVV");
    }


    @Override
    public UIElement lblExpMonth() {
        return new UIElement(By.cssSelector("label[for='expiryMonth']"), getPageName(), "labelExpiryMonth");
    }

    @Override
    public UIElement insufficientBalanceIcon() {
        return new UIElement(By.cssSelector("span.insufficientIcon"), getPageName(), "Insufficient balance icon");
    }

    @Override
    public UIElement insufficientBalanceIconMsg() {
        return new UIElement(By.cssSelector("span.insufficientIcon span.tooltiptext"), pageName,
                "Insufficient balance icon text");
    }


    @Override
    public UIElement imgDefaultCVVIcon() {
        return new UIElement(By.id("defaultCvvIcon"), getPageName(), "defaultCvvIcon");
    }

    @Override
    public UIElement imgAmexCVVIcon() {
        return new UIElement(By.id("amexCvvIcon"), getPageName(), "amexCvvIcon");
    }

    @Override
    public Select dropdownNBOtherBank() {
        return new Select(By.id("nbSelect"), getPageName(), "NB dropdown");
    }

    @Override
    public Select dropdownEMIBankSelect() {
        return new Select(By.id("emiBankSelect"), getPageName(), "EMI Bank Select dropdown");
    }

    @Override
    public UIElement loginStrip() {
        return new UIElement(By.className("login-strip"), getPageName(), "login strip");
    }

    @Override
    public RadioButton radioBtnNetBankingOrATM(String bankName) {
        return new RadioButton(By.cssSelector(".input#" + bankName.toUpperCase()), getPageName(),
                "radioBtnNetBankingOrATM");
    }

    @Override
    public Button btnCheckBalancePostpaid() {
        return null;
    }

    @Override
    public UIElement lblInsufficientBalancePostpaid() {
        return null;
    }

    @Override
    public List<WebElement> lblUPIpush() {
        return DriverManager.getDriver().findElements(By.cssSelector("#UPI_PushSection label"));
    }

    @Override
    public UIElement promoCode() {
        return new UIElement(By.cssSelector(".promocode-options-msg"), pageName,
                "promo code");
    }

	@Override
	public UIElement upiPushSection() {
		
		return new UIElement(By.cssSelector("div#UPI_PushSection"), getPageName(), "UPI push section");
	}

    @Override
    public boolean isPPIChecked() {
        return checkBoxWallet().isChecked();
    }

    @Override
    public boolean isPPBLChecked() {
        return checkBoxPpbl().isChecked();
    }

    @Override
    public boolean isPaytmCCChecked() {
        return checkboxPaytmCC().isChecked();
    }

    @Override
	public List<WebElement> EMIOptions(String bankName) {
		return null;
	}
	
	@Override
	public UIElement EMIMonths(String bankName, int months) {
		return new UIElement(By.xpath("//span[@class='emi-month'][text()='"+months+" Months']"), getPageName(), bankName+" EMI month "+ months);
	}
	
	@Override
	public UIElement EMIInterest(String bankName, int interestRate) {
		float temp = interestRate;
        return new RadioButton(By.xpath("//span[@class='emi-interest'][text()='"+temp+"% p.a']"), getPageName(), "EMIInterest");
    }

}
*/
