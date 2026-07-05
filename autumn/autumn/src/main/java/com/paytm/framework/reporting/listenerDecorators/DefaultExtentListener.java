package com.paytm.framework.reporting.listenerDecorators;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.paytm.framework.reporting.Extent;
import com.paytm.framework.reporting.listeners.ExtentListener;
import org.testng.ITestResult;

/**
 * Created by deepakkumar on 17/3/18.
 */
public final class DefaultExtentListener extends ListenerDecorator {

    private final ThreadLocal<ExtentTest> test = Extent.TEST;
    private final ExtentReports extent = Extent.REPORT;

    public DefaultExtentListener() {
        super(new DefaultListener(new ExtentListener()));
    }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);
        this.test.get().assignCategory(result.getMethod().getXmlTest().getName());
//        this.test.get().assignCategory(result.getTestContext().getSuite().getName());
//        this.test.get().assignCategory(result.getTestClass().getXmlClass().getName());
//        for (String group : result.getMethod().getGroups()) {
//            this.test.get().assignCategory(group);
//        }
    }
}
