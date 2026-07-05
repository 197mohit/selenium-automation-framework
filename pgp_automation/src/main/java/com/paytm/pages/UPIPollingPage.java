package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UPIPollingPage extends BasePage {

    private static HashMap<String, List<String>> webMap = new HashMap<>();

    static {
        webMap.put("@ybl", Arrays.asList("Go to the PhonePe", "phonepe"));
        webMap.put("@upi", Arrays.asList("Go to the BHIM", "bhim"));
        webMap.put("@ok*", Arrays.asList("Go to the Google Pay", "google"));
        webMap.put("@paytm", Arrays.asList("Go to the Paytm", "paytm.png"));
        webMap.put("@default", Arrays.asList("Go to the UPI Linked Bank/ UPI", "upi_default"));
    }

    public UPIPollingPage() {
        super("upi-polling-page");
    }

    public HashMap<String, List<String>> getAppDetailsMap() {
        return webMap;
    }

    public UIElement getTextUPIAddress() {
        return new UIElement(By.xpath(".//*[starts-with(.,'Entered UPI Address')]"), getPageName(), "upi-address-text") {
            @Override
            public String getText() {
                return super.getWrappedElement().getText().split("\\:")[1].trim();
            }
        };
    }
    public UIElement getTextGoToApp() {
        return new UIElement(By.xpath("//span[@id='upi_appName'] | //span[contains(.,'Go to the')] | .//*[contains(.,'Go to the')]"), getPageName(), "upi-go-to-app-text");
    }

    public UIElement getImageGoToApp(String imageName) {
        return new UIElement(By.xpath("//img[contains(@src,'images/"+imageName+"')]|//img[contains(@src,'"+imageName+"')]"), getPageName(), "upi-go-to-app-image");
    }

    public UIElement getMinutesDisplayed() {
        return new UIElement(By.xpath("//div[contains(@class,'timer-global')]/div[1]"), getPageName(), "upi-minutes-text");
    }
}