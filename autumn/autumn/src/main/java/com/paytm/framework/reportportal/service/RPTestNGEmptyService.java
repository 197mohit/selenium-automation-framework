package com.paytm.framework.reportportal.service;

import com.epam.reportportal.listeners.Statuses;
import com.paytm.framework.reportportal.contants.ItemStatusEnum;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

public class RPTestNGEmptyService implements RPTestNGServiceImpl {
    /**
     * Start current launch
     */
    @Override
    public void startLaunch() {

    }

    /**
     * Finish current launch
     */
    @Override
    public void finishLaunch() {

    }

    /**
     * Start test suite event handler
     *
     * @param suite TestNG's suite
     */
    @Override
    public void startTestSuite(ISuite suite) {

    }

    /**
     * Finish test suite event handler
     *
     * @param suite TestNG's suite
     */
    @Override
    public void finishTestSuite(ISuite suite) {

    }

    /**
     * Start test event handler
     *
     * @param testContext TestNG's test context
     */
    @Override
    public void startTest(ITestContext testContext) {

    }

    /**
     * Finish test event handler
     *
     * @param testContext TestNG's test context
     */
    @Override
    public void finishTest(ITestContext testContext) {

    }

    /**
     * Start test method event handler
     *
     * @param testResult TestNG's test result
     */
    @Override
    public void startTestMethod(ITestResult testResult) {

    }

    /**
     * Finish test method event handler
     *
     * @param status     Status (PASSED/FAILED)
     * @param testResult TestNG's test result
     * @see Statuses
     * @deprecated
     */
    @Override
    @Deprecated
    public void finishTestMethod(String status, ITestResult testResult) {

    }

    /**
     * Finish test method event handler
     *
     * @param status     Status (PASSED/FAILED)
     * @param testResult TestNG's test result
     * @see ItemStatusEnum
     */
    @Override
    public void finishTestMethod(ItemStatusEnum status, ITestResult testResult) {

    }

    /**
     * Start configuration method(any before of after method)
     *
     * @param testResult TestNG's test result
     */
    @Override
    public void startConfiguration(ITestResult testResult) {

    }

    @Override
    public void sendReportPortalMsg(ITestResult testResult) {

    }
}
