package com.paytm.framework.reportportal.service;

import com.paytm.framework.reportportal.contants.ItemStatusEnum;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

public interface RPTestNGServiceImpl {

    public static final String RP_ID = "rp_id";
    public static final String RP_RETRY = "rp_retry";
    public static final String RP_METHOD_TYPE = "rp_method_type";
    public static final String RP_TEST_DESC = "rp_test_desc";
    public static final String RP_TEST_UID = "rp_test_uid";
    public static final String RP_SUITE_ID = "rp_suite_id";

    /**
     * Start current launch
     */
    void startLaunch();

    /**
     * Finish current launch
     */
    void finishLaunch();

    /**
     * Start test suite event handler
     *
     * @param suite TestNG's suite
     */
    void startTestSuite(ISuite suite);

    /**
     * Finish test suite event handler
     *
     * @param suite TestNG's suite
     */
    void finishTestSuite(ISuite suite);

    /**
     * Start test event handler
     *
     * @param testContext TestNG's test context
     */
    void startTest(ITestContext testContext);

    /**
     * Finish test event handler
     *
     * @param testContext TestNG's test context
     */
    void finishTest(ITestContext testContext);

    /**
     * Start test method event handler
     *
     * @param testResult TestNG's test result
     */
    void startTestMethod(ITestResult testResult);

    /**
     * Finish test method event handler
     *
     * @param status     Status (PASSED/FAILED)
     * @param testResult TestNG's test result
     * @see com.epam.reportportal.listeners.Statuses
     * @deprecated
     */
    @Deprecated
    void finishTestMethod(String status, ITestResult testResult);

    /**
     * Finish test method event handler
     *
     * @param status     Status (PASSED/FAILED)
     * @param testResult TestNG's test result
     * @see ItemStatusEnum
     */
    void finishTestMethod(ItemStatusEnum status, ITestResult testResult);

    /**
     * Start configuration method(any before of after method)
     *
     * @param testResult TestNG's test result
     */
    void startConfiguration(ITestResult testResult);

    void sendReportPortalMsg(ITestResult testResult);

}
