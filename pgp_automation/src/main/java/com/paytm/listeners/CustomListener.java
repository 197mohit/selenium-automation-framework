package com.paytm.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import com.paytm.framework.reporting.listenerDecorators.ListenerDecorator;
import com.paytm.framework.reporting.listeners.NullListener;
import org.testng.*;
import org.testng.annotations.ITestAnnotation;

public class CustomListener extends ListenerDecorator {
    private static int total;
    private static int passed;
    private static int failed;
    private static int skipped;
    private static int pending;
    private static final int MAX_RETRY_COUNT = 2;
    private int retryCount = 0;
    private Set<ITestResult> retriedTests = new HashSet<>();

    public CustomListener() {
        super(new NullListener());
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(CustomListener.class);
    }

    @Override
    public boolean retry(ITestResult iTestResult) {

        if (!iTestResult.isSuccess()) {                                     //Check if test not succeed
            if (retryCount < MAX_RETRY_COUNT) {                               //Check if maxtry count is reached
                retryCount++;                                               //Increase the maxTry count by 1
                iTestResult.setStatus(ITestResult.FAILURE);                 //Mark test as failed
                retriedTests.add(iTestResult);
                System.out.println("Retrying Test method : "+iTestResult.getName() + " for " + retryCount +" times. ");
                return true;                                                //Tells TestNG to re-run the test
            } else {
                iTestResult.setStatus(ITestResult.FAILURE);                 //If maxCount reached,test marked as failed
            }
        } else {
            iTestResult.setStatus(ITestResult.SUCCESS);                     //If test passes, TestNG marks it as passed
        }
        return false;
    }

    @Override
    public void onStart(ISuite suite) {
        total = suite.getAllMethods().size();
        pending = total;
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (!result.getTestContext().getSkippedTests().getResults(result.getMethod()).isEmpty()) {
            --skipped;
            ++pending;
        }
        System.out.println("--------------------------------------------------- \n" +
                "Started - " + result.getName() +
                "\n --------------------------------------------------- ");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (!retriedTests.contains(result)) {
            ++passed;
            --pending;
        }
        System.out.println("Completed - " + result.getName() + ", Status - Passed");
        System.out.println("--------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed +
                "\n --------------------------------------------------- ");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!retriedTests.contains(result)) {
            ++failed;
            --pending;
        }
        result.getTestContext().getSkippedTests().removeResult(result.getMethod());
        System.out.println("onTestFailure:::::::Test Case Failed - " + result.getMethod().getMethodName());
        System.out.println("onTestFailure:::::::Completed - " + result.getName() + ", Status - Failed");
        System.out.println("--------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed +
                "\n --------------------------------------------------- ");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ++skipped;
        --pending;
        System.out.println("Completed - " + result.getName() + ", Status - Skipped");
        System.out.println("--------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed +
                "\n --------------------------------------------------- ");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("onFinish ITestContext --------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed);
    }


}
