package com.paytm.listeners;
import com.paytm.framework.reporting.listenerDecorators.ListenerDecorator;
import com.paytm.framework.reporting.listeners.NullListener;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

public final class StatusCounterListener extends ListenerDecorator {
    private static int total;
    private static int passed;
    private static int failed;
    private static int skipped;
    private static int pending;

    public StatusCounterListener() {
        super(new NullListener());
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
                "Started - " + result.getName()
                + "\n --------------------------------------------------- ");

    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("Completed - " + result.getName() + ", Status - Passed");
        ++passed;
        if (!result.getTestContext().getSkippedTests().getResults(result.getMethod()).isEmpty()) {
            --skipped;
        } else {
            --pending;
        }
        System.out.println("--------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed
                + "\n --------------------------------------------------- ");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!result.getTestContext().getSkippedTests().getResults(result.getMethod()).isEmpty()) {
            --skipped;
        }else{
            --pending;
        }
        result.getTestContext().getSkippedTests().removeResult(result.getMethod());
        System.out.println("onTestFailure:::::::Test Case Failed - " + result.getMethod().getMethodName());
        System.out.println("onTestFailure:::::::Completed - " + result.getName() + ", Status - Failed");
        ++failed;
        System.out.println("--------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed
                + "\n --------------------------------------------------- ");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("Completed - " + result.getName() + ", Status - Skipped");
        ++skipped;
        --pending;/*
        ++skipped;
        --pending;*/
        System.out.println("--------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed
                + "\n --------------------------------------------------- ");
    }
    @Override
    public void onFinish(ITestContext context) {
        System.out.println("onFinish ITestContext --------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed);

    }
}
