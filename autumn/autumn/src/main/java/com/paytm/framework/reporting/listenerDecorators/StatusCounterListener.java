package com.paytm.framework.reporting.listenerDecorators;

import com.google.common.io.Files;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.listeners.Listener;
import com.paytm.framework.reporting.listeners.NullListener;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.framework.utils.email.EmailUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.IRetryAnalyzer;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by deepakkumar on 16/3/18.
 */
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
        //System.out.println("Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed + ", FailedButWithinSuccessPercentage: " + failedButWithinSuccessPercentage);
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

    /*@Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        super.onTestFailedButWithinSuccessPercentage(result);
        ++failedButWithinSuccessPercentage;
        --pending;
        System.out.println("Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed + ", FailedButWithinSuccessPercentage: " + failedButWithinSuccessPercentage);
    }*/

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("onFinish ITestContext --------------------------------------------------- \n" +
                "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed);

    }


/*    @Override
    public boolean retry(ITestResult result) {
        boolean retry = super.retry(result);
        if (count.intValue() > 0) {
//            Reporter.log("Test Case Failed : " + result.getMethod().getMethodName() + ", Retrying " + (MAX_RETRY_COUNT - count.intValue() + 1) + " out of " + MAX_RETRY_COUNT);
            this.report.info("Test Case Failed : " + result.getMethod().getMethodName() + ", Retrying " + (MAX_RETRY_COUNT - count.intValue() + 1) + " out of " + MAX_RETRY_COUNT);
//            System.out.println("Test Case Failed : " + result.getMethod().getMethodName() + ", Retrying " + (MAX_RETRY_COUNT - count.intValue() + 1) + " out of " + MAX_RETRY_COUNT);
            retry = true;
            count.decrementAndGet();
           *//* if (result.getStatus() == 2) {
                --failed;
                ++pending;
            }
            if (result.getStatus() == 3) {
                --skipped;
                ++pending;
            }*//*
                ++skipped;
                System.out.println("--------------------------------------------------- \n" +
                        "Total: " + total + ", Pending: " + pending + ", Failed: " + failed + ", Skipped: " + skipped + ", Passed: " + passed + ", FailedButWithinSuccessPercentage: " + failedButWithinSuccessPercentage
                        + "\n --------------------------------------------------- ");
            }
        return retry;
    }*/

}
