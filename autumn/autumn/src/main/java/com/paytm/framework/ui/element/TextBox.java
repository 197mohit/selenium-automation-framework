package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class TextBox extends UIElement {

    @Deprecated
    public TextBox(By by, String pageName) {
        super(by, pageName);
    }

    public TextBox(By by, String pageName, String elementName) {
        super(by, pageName, elementName);
    }

    @TestStep
    public void clearAndType(CharSequence... keysToSend) {
        clear();
        type(keysToSend);
    }

    /*
    This method can be used when there is a popup with a mouse click on the text field.
    'Esc' will close the popup and will allow the set the text value.
     */
    @TestStep
    public void clearEscAndType(CharSequence... keysToSend) {
        clear();
        getWrappedElement().sendKeys(Keys.ESCAPE);
        type(keysToSend);
    }

    @TestStep
    public void type(CharSequence... keysToSend) {
        sendKeys(keysToSend);
    }
}