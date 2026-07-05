package com.paytm.pages;

import com.paytm.appconstants.Constants;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Add override function in this class when locator us different in case of web.
 */
public class CashierPageCheckoutjsWebRevamp extends CashierPageCheckoutjsWapRevamp {

    public CashierPageCheckoutjsWebRevamp() {
        super();
        setPageName("Checkoutjs cashier page web");
    }



}