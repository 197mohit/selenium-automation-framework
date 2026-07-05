package com.paytm.framework.ui.base.page;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.MoreExpectedConditions;
import com.paytm.framework.ui.element.UIElement;
import org.apache.commons.io.FileUtils;
import org.fest.assertions.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class BasePage {

    protected String pageName;
    protected String pageURL;
    private Report report = com.paytm.framework.reporting.Reporter.report;

    public BasePage(String pageName) {
        this.pageName = pageName;
    }

    public BasePage(String pageName, Report report) {
        this.pageName = pageName;
        this.report = report;
    }

    public static synchronized Object executeJavaScript(String javaScript, Object... args) {
        com.paytm.framework.reporting.Reporter.report.info("Execute javascript [" + javaScript + "]");
        WebDriver driver = DriverManager.getDriver();
        try {
            return ((JavascriptExecutor) driver).executeScript(javaScript, args);
        } catch (Throwable e) {
            throw (RuntimeException) e;
        }
    }

    public String getPageURL() {
        return this.pageURL;
    }

    public String getPageName() {
        return this.pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public void launch() {
        this.report.info("Launch URL [{}]", pageURL);
        DriverManager.getDriver().get(this.pageURL);
    }

    public void assertContainsText(String text) {
        this.report.info("Assert [{}] contains text [{}]", pageName, text);
        Assertions.assertThat(DriverManager.getDriver().findElement(By.tagName("body")).getText()).containsIgnoringCase(text);
    }

    public void assertDoesNotContainText(String text) {
        this.report.info("Assert [{}] doesn't contain text [{}]", pageName, text);
        Assertions.assertThat(DriverManager.getDriver().findElement(By.tagName("body")).getText()).doesNotContain(text);
    }

    public void assertContainsTitle(String title) {
        this.report.info("Assert [{}] title contains [{}]", pageName, title);
        Assertions.assertThat(DriverManager.getDriver().getTitle()).containsIgnoringCase(title);
    }

    public void waitUntilLoads() {
        this.report.info("Wait until [{}] loads ", pageName);
        Duration timeToWaitTillDocumentStartsLoading = Duration.ofSeconds(5);
        Duration maxPageLoad=ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME;
        try {
            // It is important to first wait till the document has started loading otherwise tests fail intermittently
            // when the execution is fast on some browser/environment. e.g. if clicking on a link results in a
            // page load & we have a waitForPageLoad after click then if the document hasn't started to load, it will
            // assume that the document is ready
            DriverManager.setWebDriverPageWait(timeToWaitTillDocumentStartsLoading);
            DriverManager.getWebDriverPageWait().until(MoreExpectedConditions.documentIsLoading());
//            DriverManager.setWebDriverPageWait(maxPageLoad.minus(timeToWaitTillDocumentStartsLoading));
//            DriverManager.setWebDriverPageWait(ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME - timeToWaitTillDocumentStartsLoading);
//            DriverManager.getWebDriverPageWait().until(MoreExpectedConditions.documentIsReady());
        } catch (Throwable e) {
            // Do nothing since we don't want to fail a test if the page hasn't loaded completely
        } finally {
            DriverManager.setWebDriverPageWait(maxPageLoad);
        }
    }

    public void waitUntilContainsTitle(final String title) {
        this.report.info("Wait until [{}] contains title [{}]", pageName, title);
        DriverManager.getWebDriverPageWait().until(ExpectedConditions.titleContains(title));
    }

    public void waitUntilContainsText(final String text) {
        this.report.info("Wait until [{}] contains text [{}]", pageName, text);
        DriverManager.getWebDriverPageWait().until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), text));
    }

    public void waitUntilDoesNotContainText(final String text) {
        this.report.info("Wait until [{}] doesn't contain text [{}]", pageName, text);
        DriverManager.getWebDriverPageWait().until(ExpectedConditions.not
                (ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), text)));
    }

    public void waitUntilAllAJAXCallsFinish() {
        this.report.info("Wait until all ajax requests complete on [{}]", pageName);
        WebDriverWait wait = DriverManager.getWebDriverPageWait();

        long timeOutInMilliSeconds = ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME.toMillis();
//        long timeOutInMilliSeconds = ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME * 1000;
        long startTime = System.currentTimeMillis();
        try {
            if (timeOutInMilliSeconds > 0) {
                wait.withTimeout(Duration.ofMillis(5000)).until(MoreExpectedConditions.jQueryAJAXCallsHaveCompleted());
//                wait.withTimeout(timeOutInMilliSeconds, TimeUnit.MILLISECONDS).until(MoreExpectedConditions.jQueryAJAXCallsHaveCompleted());
            }
            pause(1);

            timeOutInMilliSeconds = timeOutInMilliSeconds - (System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();
            if (timeOutInMilliSeconds > 0) {
                wait.withTimeout(Duration.ofMillis(timeOutInMilliSeconds)).until(MoreExpectedConditions.jQueryAJAXCallsHaveCompleted());
            }
            pause(1);

            timeOutInMilliSeconds = timeOutInMilliSeconds - (System.currentTimeMillis() - startTime);
            if (timeOutInMilliSeconds > 0) {
                wait.withTimeout(Duration.ofMillis(timeOutInMilliSeconds)).until(MoreExpectedConditions.jQueryAJAXCallsHaveCompleted());
            }

        } catch (Throwable e) {
            // Do nothing since we don't want to fail a test case in case of ongoing request
        }
    }

    public void waitUntilAngularProcessingFinish() {
        this.report.info("Wait until angular js has finished processing on [{}]", pageName);
        DriverManager.getWebDriverPageWait().until(MoreExpectedConditions.angularHasFinishedProcessing());
    }

    public void waitUntilFrameAppearsAndSwitchToIt(String frameLocator) {
        this.report.info("Wait until frame with locator [" + frameLocator + "] appears and switch to it on [{}]", pageName);
        DriverManager.getWebDriverPageWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    public void waitUntilFrameAppearsAndSwitchToIt(UIElement frameElement) {
        this.report.info("Wait until frame element [" + frameElement.getElementName() + "] appears and switch to it on [{}]", pageName);
        DriverManager.getWebDriverPageWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameElement.getWrappedElement()));
    }

    public void pause(int seconds) {
        this.report.info("Pause for [" + seconds + "] seconds");
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Reporter.report.error("Couldn't sleep "+e.getMessage());
        }
    }

    public void assertPageURL(String url) {
        this.report.info("Assert [" + pageName + "] URL [" + url + "]");
        Assertions.assertThat(DriverManager.getDriver().getCurrentUrl()).isEqualToIgnoringCase(url);
    }

    public void assertPageContainsURL(String url) {
        this.report.info("Assert [" + pageName + "] URL contains [" + url + "]");
        Assertions.assertThat(DriverManager.getDriver().getCurrentUrl()).containsIgnoringCase(url);
    }

    public void assertPageURL() {
        String url = DriverManager.getDriver().getCurrentUrl();
        this.report.info("Assert [" + pageName + "] URL [" + url + "]");
        Assertions.assertThat(DriverManager.getDriver().getCurrentUrl()).isEqualToIgnoringCase(getPageURL());
    }

    public void assertPageContainsURL() {
        String url = DriverManager.getDriver().getCurrentUrl();
        this.report.info("Assert [" + pageName + "] URL contains [" + url + "]");
        Assertions.assertThat(DriverManager.getDriver().getCurrentUrl()).containsIgnoringCase(getPageURL());
    }

    public void refresh() {
        this.report.info("Refresh [" + pageName + "]");
        DriverManager.getDriver().navigate().refresh();
        waitUntilLoads();
    }
    
    public void navigateBack() {
    	this.report.info("Navigate back [" + pageName + "]");
    	DriverManager.getDriver().navigate().back();
    	waitUntilLoads();
    }
    
    public void navigateForward() {
    	this.report.info("Navigate forward [" + pageName + "]");
    	DriverManager.getDriver().navigate().forward();
    	waitUntilLoads();
    }

    public static synchronized void takeScreenshot(String pageName) {
        final WebDriver driver = DriverManager.getCurrentWebDriver();
        if (driver != null) {

            final DateFormat timeFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss_SSS");
            final String aShotFileName = org.testng.Reporter.getCurrentTestResult().getMethod().getMethodName() + "_" + timeFormat.format(new Date()) + ".png";
            final String defaultFileName = org.testng.Reporter.getCurrentTestResult().getMethod().getMethodName() + "_" + timeFormat.format(new Date()) + "_default" + ".png";
            String outputDir = org.testng.Reporter.getCurrentTestResult().getTestContext().getOutputDirectory();
            outputDir = outputDir.substring(0, outputDir.lastIndexOf(File.separator)) + "/html";
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
                Reporter.report.info("Exception occurred while saving screenshot to file", e);
            }
            Reporter.report.attachImage(aShotFile, pageName);
            Reporter.report.attachImage(defaultFile, pageName);
        } else {
            Reporter.report.info("Couldn't capture screen-shot as WebDriver is null.");
        }
    }

}