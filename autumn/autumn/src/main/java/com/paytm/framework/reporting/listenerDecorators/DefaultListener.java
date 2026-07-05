package com.paytm.framework.reporting.listenerDecorators;

import com.google.common.io.Files;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.reporting.Reporter;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by deepakkumar on 16/3/18.
 */
public class DefaultListener extends ListenerDecorator {

    private static int pass;
    private static int fail;
    private static int skip;

    private static int MAX_RETRY_COUNT = ExecutionConfig.TEST_CASE_RETRY_COUNT;
    private final Report report = com.paytm.framework.reporting.Reporter.report;
    AtomicInteger count = new AtomicInteger(MAX_RETRY_COUNT);

    public DefaultListener(Listener decoratedListener) {
        super(decoratedListener);
    }

    public DefaultListener() {
        super(new NullListener());
    }

    @Override
    public void onStart(ISuite suite) {
        super.onStart(suite);
    }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        super.onTestSuccess(result);
        result.getTestContext().getSkippedTests().removeResult(result.getMethod());
    }



    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);
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

    @Override
    public void onTestSkipped(ITestResult result) {
        super.onTestSkipped(result);
    }

    @Override
    public void onFinish(ITestContext context) {
        super.onFinish(context);
    }

    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        super.transform(annotation, testClass, testConstructor, testMethod);
        Class<? extends IRetryAnalyzer> retry = annotation.getRetryAnalyzerClass();
        if (retry == null) {
            annotation.setRetryAnalyzer(DefaultListener.class);
        }
    }

    @Override
    public boolean retry(ITestResult result) {
        boolean retry = super.retry(result);
        if (count.intValue() > 0) {
            this.report.info("Test Case Failed : " + result.getMethod().getMethodName() + ", Retrying " + (MAX_RETRY_COUNT - count.intValue() + 1) + " out of " + MAX_RETRY_COUNT);
            retry = true;
            count.decrementAndGet();
        }
        return retry;
    }

    @Override
    public void onExecutionFinish() {
        super.onExecutionFinish();
        if (ExecutionConfig.SEND_EXEC_REPORT_EMAIL.equalsIgnoreCase("true")) {
            renameJsFileExtensionsToTxtForAttachingInEmail();
            String reportParentDirPath = System.getProperty("REPORT_PARENT_DIRECTORY");
            CommonUtils.zip(reportParentDirPath);
            EmailUtil.sendEmail(ExecutionConfig.SMTP_USERNAME, ExecutionConfig.EXEC_REPORT_EMAIL_RECEIVER, "", "",
                    "Test Execution Report", "Pass: " + pass + ", Fail: " + fail + ", Skip: " + skip,
                    reportParentDirPath + ".zip");
        }
    }

    private void createScreenshot(final ITestResult result) {
        final WebDriver driver = DriverManager.getCurrentWebDriver();
        if (driver != null) {
            final DateFormat timeFormat = new SimpleDateFormat("MM.dd.yyyy HH-mm-ss");
            final String aShotFileName = StringUtils.removeAll(result.getMethod().getMethodName() + "_" + timeFormat.format(new Date()) + ".png", " ");
            final String defaultFileName = StringUtils.removeAll(result.getMethod().getMethodName() + "_" + timeFormat.format(new Date()) + "_default" + ".png", " ");
            String outputDir = result.getTestContext().getOutputDirectory();
            outputDir = outputDir.substring(0, outputDir.lastIndexOf(File.separator)) + "/html";
            File outDirPath = new File(outputDir);
            if(!outDirPath.exists()){
                outDirPath.mkdirs();
            }
            final File aShotFile = new File(outputDir, aShotFileName);
            final File defaultFile = new File(outputDir, defaultFileName);
            File tempFile;
            AShot aShot = new AShot()
                    .shootingStrategy(ShootingStrategies.viewportPasting(100));
            Screenshot screenshot;
            if (driver.getClass().equals(RemoteWebDriver.class)) {
                screenshot = aShot.takeScreenshot(new Augmenter().augment(driver));
                tempFile = ((TakesScreenshot) new Augmenter().augment(driver))
                        .getScreenshotAs(OutputType.FILE);
            } else {
                screenshot = aShot.takeScreenshot(driver);
                tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            }
            try {
                ImageIO.write(screenshot.getImage(), "PNG", aShotFile);
                FileUtils.copyFile(tempFile, defaultFile);
                FileUtils.deleteQuietly(tempFile);
            } catch (IOException e) {
                this.report.info("Exception occurred while saving screenshot to file", e);
            }
            this.report.attachImage(aShotFile, "FinalPage");
            this.report.attachImage(defaultFile, "FinalPage");
        } else {
            this.report.info("Couldn't capture screen-shot as WebDriver is null.");
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
            Reporter.report.error("Couldn't move file "+e.getMessage());
        }
        file1 = new File(reportMainDirectoryPath + File.separator + "html" + File.separator + "sorttable.js");
        file2 = new File(reportMainDirectoryPath + File.separator + "html" + File.separator + "sorttable.txt");
        try {
            Files.move(file1, file2);
        } catch (IOException e) {
            Reporter.report.error("Couldn't move file "+e.getMessage());
        }
    }

}
