package com.paytm.framework.core;

import com.google.common.io.Files;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.framework.utils.email.EmailUtil;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.*;
import org.testng.annotations.ITestAnnotation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


public class CustomListener implements ITestListener, ISuiteListener, IInvokedMethodListener, IRetryAnalyzer,
        IAnnotationTransformer, IConfigurationListener, IExecutionListener {

    private static int pass;
    private static int fail;
    private static int skip;
    private static int totalPass;
    private static int totalFail;
    private static int totalSkip;

    /**
     * Invoked each time before a test will be invoked.
     * The <code>ITestResult</code> is only partially filled with the references to
     * class, method, start millis and status.
     *
     * @param result the partially filled <code>ITestResult</code>
     * @see ITestResult#STARTED
     */
    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("Running..... "+result.getName());
        System.out.print("Total:"+(totalPass+totalFail+totalSkip));
        System.out.print(", Passed: "+totalPass);
        System.out.print(", Failures: "+totalFail);
        System.out.println(", Skipped: "+totalSkip);
    }



    /**
     * Invoked each time a test succeeds.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SUCCESS
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        result.getTestContext().getSkippedTests().removeResult(result.getMethod());
        totalPass++;
    }

    /**
     * Invoked each time a test fails.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#FAILURE
     */
    @Override
    public void onTestFailure(ITestResult result) {
        totalFail++;
        result.getTestContext().getSkippedTests().removeResult(result.getMethod());
        System.out.println("Test Case Failed - " + result.getMethod().getMethodName());
        if (DriverManager.getCurrentWebDriver() != null) {
            if (DriverManager.getCaptureScreenShot() == true) {
                Throwable e = result.getThrowable();
                if (e instanceof SQLException || e instanceof IOException) {
                    // Do nothing
                } else {
                    createScreenshot(result);
                }
            }
        }

    }

    /**
     * Invoked each time a test is skipped.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SKIP
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        totalSkip++;
    }

    /**
     * Invoked each time a method fails but has been annotated with
     * successPercentage and this failure still keeps it within the
     * success percentage requested.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SUCCESS_PERCENTAGE_FAILURE
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

    /**
     * Invoked after the test class is instantiated and before
     * any configuration method is called.
     *
     * @param context
     */
    @Override
    public void onStart(ITestContext context) {
    }

    /**
     * Invoked after all the tests have run and all their
     * Configuration methods have been called.
     *
     * @param context
     */
    @Override
    public void onFinish(ITestContext context) {
        System.out.println("hello");
        pass = context.getPassedTests().size();
        fail = context.getFailedTests().size();
        skip = context.getSkippedTests().size();

    }

    /**
     * This method is invoked before the SuiteRunner starts.
     *
     * @param suite
     */
    @Override
    public void onStart(ISuite suite) {
        System.out.println("Total test case: "+suite.getAllMethods().size());
    }

    /**
     * This method is invoked after the SuiteRunner has run all
     * the test suites.
     *
     * @param suite
     */
    @Override
    public void onFinish(ISuite suite) {
    }

    @Override


    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {}

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {}

    private void createScreenshot(final ITestResult result) {
        final WebDriver driver = DriverManager.getCurrentWebDriver();
        if (driver != null) {
            final DateFormat timeFormat = new SimpleDateFormat("MM.dd.yyyy HH-mm-ss");
            final String fileName = result.getMethod().getMethodName() + "_" + timeFormat.format(new Date()) + ".png";
            try {
                File scrFile;

                if (driver.getClass().equals(RemoteWebDriver.class)) {
                    scrFile = ((TakesScreenshot) new Augmenter().augment(driver))
                            .getScreenshotAs(OutputType.FILE);
                } else {
                    scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                }

                String outputDir = result.getTestContext().getOutputDirectory();
                outputDir = outputDir.substring(0, outputDir.lastIndexOf(File.separator)) + "/html";
                final File saved = new File(outputDir, fileName);
                FileUtils.copyFile(scrFile, saved);
                Reporter.log("<a href=\"" + fileName + "\" target=\"_blank\"><b>Screenshot</b></a>");
            } catch (IOException e) {
                //TODO
            }
        } else {
            Reporter.log("Couldn't capture screen-shot as WebDriver is null.");
        }
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {}

    @Override
    public void onConfigurationSkip(ITestResult iTestResult) {}

    @Override
    public void onConfigurationSuccess(ITestResult result) {}

    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        Class<? extends IRetryAnalyzer> retry = annotation.getRetryAnalyzerClass();
        if (retry == null) {
            annotation.setRetryAnalyzer(CustomListener.class);
        }

    }

    private static int MAX_RETRY_COUNT = ExecutionConfig.TEST_CASE_RETRY_COUNT;
    AtomicInteger count = new AtomicInteger(MAX_RETRY_COUNT);

    @Override
    public boolean retry(ITestResult result) {
        boolean retry = false;
        if (count.intValue() > 0) {
            Reporter.log("Test Case Failed : " + result.getMethod().getMethodName() + ", Retrying " + (MAX_RETRY_COUNT - count.intValue() + 1) + " out of " + MAX_RETRY_COUNT);
            System.out.println("Test Case Failed : " + result.getMethod().getMethodName() + ", Retrying " + (MAX_RETRY_COUNT - count.intValue() + 1) + " out of " + MAX_RETRY_COUNT);
            retry = true;
            count.decrementAndGet();
        }
        return retry;
    }

    @Override
    public void onExecutionStart() {}

    @Override
    public void onExecutionFinish() {
        if (ExecutionConfig.SEND_EXEC_REPORT_EMAIL.equalsIgnoreCase("true")) {
            renameJsFileExtensionsToTxtForAttachingInEmail();
            String reportParentDirPath = System.getProperty("REPORT_PARENT_DIRECTORY");
            CommonUtils.zip(reportParentDirPath);
            EmailUtil.sendEmail(ExecutionConfig.SMTP_USERNAME, ExecutionConfig.EXEC_REPORT_EMAIL_RECEIVER, "", "",
                    "Test Execution Report", "Pass: " + pass + ", Fail: " + fail + ", Skip: " + skip,
                    reportParentDirPath + ".zip");
        }
    }

    private void renameJsFileExtensionsToTxtForAttachingInEmail() {
        System.out.println("Renaming reportng.js -> reportng.txt, sorttable.js -> sorttable.txt to be able to able to send " +
                "execution report as as attachment over email");
        String reportMainDirectoryPath = System.getProperty("REPORT_MAIN_DIRECTORY");
        File file1 = new File(reportMainDirectoryPath + File.separator + "html" + File.separator + "reportng.js");
        File file2 = new File(reportMainDirectoryPath + File.separator + "html" + File.separator + "reportng.txt");
        try {
            Files.move(file1, file2);
        } catch (IOException e) {
            Reporter.log("Couldn't renameJsFileExtensionsToTxtForAttachingInEmail "+e.getMessage());
        }
        file1 = new File(reportMainDirectoryPath + File.separator + "html" + File.separator + "sorttable.js");
        file2 = new File(reportMainDirectoryPath + File.separator + "html" + File.separator + "sorttable.txt");
        try {
            Files.move(file1, file2);
        } catch (IOException e) {
            Reporter.log("Couldn't renameJsFileExtensionsToTxtForAttachingInEmail "+e.getMessage());
        }
    }



}
