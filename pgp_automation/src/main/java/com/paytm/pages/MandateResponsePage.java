package com.paytm.pages;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.ui.element.UIElement;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;

public class MandateResponsePage extends ResponsePage {

    private SoftAssertions softly;

    public MandateResponsePage() {
        super();
        this.softly = getSoftly();
        this.pageURL = LocalConfig.PGP_RESP_HOST + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH;
    }

    public MandateResponsePage validateResponseField(ParamName paramName, String expectedValue) {
        String locator = getLocator(paramName.toString());
        UIElement element = new UIElement(By.xpath(locator), "MandateResponsePage", locator);
        softly.assertThat(element.getText())
                .as(locator + " mismatch")
                .isEqualToIgnoringCase(expectedValue);
        return this;
    }

    public MandateResponsePage assertAll() {
        this.softly.assertAll();
        return this;
    }

    private String getLocator(String paramName) {
        String locator = "//table/tbody//*[text()=\""+paramName+"\"]/../td[2]";
        return locator;
    }

    public enum ParamName {

        orderId("orderId"),
        paymentMode("paymentMode"),
        merchantCustId("merchantCustId"),
        mid("mid"),
        responseMessage("responseMessage"),
        responseStatus("responseStatus"),
        subscriptionId("subscriptionId"),
        txnDate("txnDate"),
        responseCode("responseCode")
        ;

        private String paramName;
        ParamName(String paramName) {
            this.paramName = paramName;
        }

        @Override
        public String toString() {
            return this.paramName;
        }
    }


}
