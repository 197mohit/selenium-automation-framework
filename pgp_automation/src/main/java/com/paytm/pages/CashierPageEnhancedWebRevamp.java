package com.paytm.pages;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

//TODO: add scrollToElement at common level instead of test cases
public class CashierPageEnhancedWebRevamp extends CashierPageEnhancedWeb{



    public CashierPageEnhancedWebRevamp() {
        super();
        this.pageName = "cashier-page-enhanced-web-new-theme";
    }



}
