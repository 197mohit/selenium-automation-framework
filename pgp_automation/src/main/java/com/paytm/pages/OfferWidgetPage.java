package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author satwiksharma
 * This is a Offer Discovery Widget Page
 */
public class OfferWidgetPage extends BasePage {

    public OfferWidgetPage() {
        super("Offer Widget Page");
    }

    public TextBox offerJsUrl() {
        return new TextBox(By.xpath("//textarea[@id='merchantCheckout']"), getPageName(), "Offer JS URL");
    }

    public Button buttonLoadJS() {
        return new Button(By.id("loadMerchantJS"), getPageName(), "buttonLoadJS");
    }

    public Button initialiseOfferJs() {
        return new Button(By.id("btn"), getPageName(), "Initialize Offer JS");
    }

    public Button invokeOfferJs() {
        return new Button(By.id("start"), getPageName(), "Invoke Offer JS");
    }

    public TextBox offerJsConfiguration() {
        return new TextBox(By.xpath("//textarea[@id='mconfig']"), getPageName(), "Offer JS URL");
    }

    public Link linkShowAllOffers() {
        return new Link(By.xpath("//div[contains(text(),'View all Offers')]"), getPageName(), "linkShowAllOffers");
    }

    public UIElement payMethodCC() {
        return new UIElement(By.xpath("//div[contains(@class, 'tab') and contains(@class, 'tab-item') and contains(text(), 'Credit Card EMI')]"), getPageName(), "Bank & EMI Offers");
    }

    public UIElement payMethodDC() {
        return new UIElement(By.xpath("//div[contains(@class, 'tab') and contains(@class, 'tab-item') and contains(text(), 'Debit Card EMI')]"), getPageName(), "Bank & EMI Offers");
    }

    public UIElement payInFull() {
        return new UIElement(By.xpath("//div[contains(@class, 'tab') and contains(@class, 'tab-item') and contains(text(), 'Pay In Full')]"), getPageName(), "Bank & EMI Offers");
    }

    public UIElement payNoCostEmi() {
        return new UIElement(By.xpath("//div[contains(@class, 'tab') and contains(@class, 'tab-item') and contains(text(), 'No Cost EMI')]"), getPageName(), "Bank & EMI Offers");
    }

    public UIElement FirstBenefitText() {
        return new UIElement(By.xpath("//div[@class='paytm-banner']//div[@class='list-content pos-r'][1]"), getPageName(), "First Benefit Text on widget");
    }

    public UIElement SecondBenefitText() {
        return new UIElement(By.xpath("//div[@class='paytm-banner']//div[@class='list-content pos-r'][2]//div[@class='list-text']"), getPageName(), "Second Benefit Text on widget");
    }

    public Button ComparePlans() {
        return new Button(By.xpath("//div[@class='pg-offer-modal']//button[contains(text(), 'Compare Plans')]"), getPageName(), "Compare plans");
    }

    public UIElement SelectBanksDropdown() {
        return new UIElement(By.xpath("//div[@class='pg-offer-modal']//div[contains(text(), 'Select Banks')]"), getPageName(), "Select Banks - Compare plans");
    }

    public UIElement SelectTenuresDropdown() {
        return new UIElement(By.xpath("//div[@class='pg-offer-modal']//div[contains(text(), 'Select Tenure')]"), getPageName(), "Select Tenures - Compare plans");
    }

    public List<UIElement> BankOptions() {
        return UIElements.getMultiple(By.xpath("//div[@class='pg-offer-modal']//div[contains(@class, 'bank-field')]//ul/li"), getPageName(), "Bank Options - Compare plans");
    }

    public List<UIElement> TenureOptions() {
        return UIElements.getMultiple(By.xpath("//div[@class='pg-offer-modal']//div[contains(@class, 'tenure-field')]//ul/li"), getPageName(), "Tenure Options - Compare plans");
    }

    public Button Apply() {
        return new Button(By.xpath("//div[@class='pg-offer-modal']//button[contains(text(), 'Apply')]"), getPageName(), "apply button - Compare plans");
    }

    public UIElement ComparePlansTable() {
        return new UIElement(By.xpath("//div[@class='pg-offer-modal']//div[contains(@class, 'comp-tabular-data')]"), getPageName(), "Compare plans Table");
    }


    public void launchLoginPage(String pageUrl) {
        super.pageURL = pageUrl;
        launch();
    }

    public void enterJSUrl(String url) {
        offerJsUrl().clear();
        offerJsUrl().sendKeys(url);
    }

    public void clickButtonLoadJS() {
        buttonLoadJS().click();
    }

    public void doConfiguration(String body) {
        offerJsConfiguration().clear();
        offerJsConfiguration().sendKeys(body);
    }

    public void clickInitialiseOfferJS() {
        waitUntilLoads();
        initialiseOfferJs().click();
    }

    public void clickInvokeOfferJS() {
        waitUntilLoads();
        invokeOfferJs().click();
    }

    public void clickViewAllOffers() {
        waitUntilAllAJAXCallsFinish();
        linkShowAllOffers().click();
    }

    public void openComparePlans() {
        waitUntilLoads();
        ComparePlans().click();
        waitUntilLoads();
        SelectBanksDropdown().click();
        BankOptions().get(0).click();
        if (BankOptions().size() > 1){
            BankOptions().get(1).click();
        }
        if (BankOptions().size() > 2){
            BankOptions().get(2).click();
        }
        SelectTenuresDropdown().click();
        TenureOptions().get(0).click();
        TenureOptions().get(1).click();
        TenureOptions().get(2).click();
        Apply().click();
        waitUntilLoads();
    }

    public void invokeWidget(String json) {
        clickButtonLoadJS();
        doConfiguration(json);
        waitUntilLoads();
        clickButtonLoadJS();
        doConfiguration(json);
        waitUntilLoads();
        clickInitialiseOfferJS();
        clickInvokeOfferJS();
    }

    public void assertAllPayModeArePresent() {
        Assert.assertTrue(payMethodCC().isElementPresent());
        Assert.assertTrue(payMethodDC().isElementPresent());
        Assert.assertTrue(payInFull().isElementPresent());
        Assert.assertTrue(payNoCostEmi().isElementPresent());
    }

    public void assertComparePlansTable() {
        Assert.assertTrue(ComparePlansTable().isElementPresent());
        UIElement table = ComparePlansTable();
        Assert.assertTrue(!table.findElements(By.xpath("//div[contains(@class, 'thead')]")).isEmpty());
        Assertions.assertThat(table.findElement(By.xpath("//div[contains(@class, 'thead')]/div[contains(text(), 'Bank')]")).getText()).isEqualTo("Bank");
        Assertions.assertThat(table.findElement(By.xpath("//div[contains(@class, 'thead')]/div[contains(text(), 'Interest Rate (p.a.)')]")).getText()).isEqualTo("Interest Rate (p.a.)");
        Assertions.assertThat(table.findElement(By.xpath("//div[contains(@class, 'thead')]/div[contains(text(), 'EMI Per Month')]")).getText()).isEqualTo("EMI Per Month");
        Assertions.assertThat(table.findElement(By.xpath("//div[contains(@class, 'thead')]/div[contains(text(), 'EMI Offer')]")).getText()).isEqualTo("EMI Offer");
        Assertions.assertThat(table.findElement(By.xpath("//div[contains(@class, 'thead')]/div[contains(text(), 'Additional Offer')]")).getText()).isEqualTo("Additional Offer");
        Assertions.assertThat(table.findElement(By.xpath("//div[contains(@class, 'thead')]/div[contains(text(), 'Effective Price')]")).getText()).isEqualTo("Effective Price");
        List<WebElement> tenures = table.findElements(By.xpath("//div[@class='table-wrapper']/div"));
        for (WebElement tenure : tenures) {
            Assertions.assertThat(tenure.findElement(By.className("tenure")).getText()).matches("TENURE: (\\d+) MONTHS");
        }
        List<WebElement> tablerow = table.findElements(By.xpath("//div[contains(@class, 'xsd-tr')]/div"));
        for (WebElement row : tablerow){
            Assertions.assertThat(row.findElement(By.xpath("//div[contains(@class, 'table')]//div[contains(@class, 'row-body')]/div/div[2]")).getText()).matches("^([\\w\\s]+) Bank (Credit|Debit) Card$");
            Assertions.assertThat(row.findElement(By.xpath("//div[contains(@class, 'table')]//div[contains(@class, 'row-body')]/div/div[3]")).getText()).matches("^(\\b\\d{1,2}\\b)%$");
            Assertions.assertThat(row.findElement(By.xpath("//div[contains(@class, 'table')]//div[contains(@class, 'row-body')]/div/div[4]")).getText()).matches("₹(?:\\d{1,3}(?:,\\d{2})*(?:,\\d{3})*|\\d+)(?:\\.\\d{2})?");
            Assertions.assertThat(row.findElement(By.xpath("//div[contains(@class, 'table')]//div[contains(@class, 'row-body')]/div/div[5]")).getText()).matches("(₹((?:\\d{1,3}(?:,\\d{2})*(?:,\\d{3})*|\\d+)(?:\\.\\d{2})?)\\s+(?:No|Low) Cost EMI)|(-)");
            Assertions.assertThat(row.findElement(By.xpath("//div[contains(@class, 'table')]//div[contains(@class, 'row-body')]/div/div[6]")).getText()).matches("(₹(?:\\d{1,3}(?:,\\d{2})*(?:,\\d{3})*|\\d+)(?:\\.\\d{2})?)|(-)");
            Assertions.assertThat(row.findElement(By.xpath("//div[contains(@class, 'table')]//div[contains(@class, 'row-body')]/div/div[7]")).getText()).matches("₹(?:\\d{1,3}(?:,\\d{2})*(?:,\\d{3})*|\\d+)(?:\\.\\d{2})?");
        }
    }


}
