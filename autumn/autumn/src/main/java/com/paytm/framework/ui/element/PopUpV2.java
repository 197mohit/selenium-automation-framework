package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.By;

public abstract class PopUpV2 extends UIElement {

    public PopUpV2(By by, String elementName) {
        super(by, elementName);
    }

    @TestStep
    public abstract void accept();

    @TestStep
    public abstract void close();

}
