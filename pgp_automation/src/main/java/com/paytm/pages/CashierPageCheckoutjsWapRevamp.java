package com.paytm.pages;

import com.paytm.appconstants.Constants;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import com.paytm.utils.merchant.util.AuthUtil;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CashierPageCheckoutjsWapRevamp extends CashierPageCheckoutjsWap{

    public CashierPageCheckoutjsWapRevamp() {
        super();
        setPageName("Checkout-js-revamp-wap-theme");
    }


}


