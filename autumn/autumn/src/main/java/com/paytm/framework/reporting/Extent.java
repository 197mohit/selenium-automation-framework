package com.paytm.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

/**
 * Created by anjukumari on 16/03/18
 */
public class Extent {
    public static ThreadLocal<ExtentTest> TEST = new ThreadLocal<ExtentTest>();
    public static ExtentReports REPORT =  new ExtentReports();
}
