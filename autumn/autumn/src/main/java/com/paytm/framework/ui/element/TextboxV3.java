package com.paytm.framework.ui.element;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

public class TextboxV3 extends InputV3 {

    public TextboxV3(By locator, String name) {
        super(locator, name);
    }

    @TestStep("Enter %s in %e")
    public void enter(CharSequence... keysToSend) {
        if (keysToSend == null) return;
        this.wait.apply(this.isClickable());
        new Actions(DriverManager.getDriver()).moveToElement(this.element()).sendKeys(keysToSend).perform();
    }

    @TestStep("Clear %e")
    public void clear() {
        this.wait.apply(this.isClickable());
        this.element().clear();
    }

}
