package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.By;

public class RadioButtonV3 extends InputV3 {

    public RadioButtonV3(By locator, String name) {
        super(locator, name);
    }

    @TestStep
    public void check() {
        if (!this.isSelected().getAsBoolean()) {
            this.click();
        }
    }

}
