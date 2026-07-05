package com.paytm.framework.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriverManager {

    private static List<WebDriverThread> webDriverThreadPool =
            Collections.synchronizedList(new ArrayList<WebDriverThread>());

    public static ThreadLocal<Boolean> driverStatusThread = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private static ThreadLocal<WebDriverThread> driverThread = new ThreadLocal<WebDriverThread>(){
        @Override
        protected WebDriverThread initialValue(){
            WebDriverThread webDriverThread = new WebDriverThread();
            webDriverThreadPool.add(webDriverThread);
            return webDriverThread;
        }
    };

    private static final ThreadLocal<WebDriverWait> webDriverPageWait = new ThreadLocal<WebDriverWait>(){
        @Override
        protected WebDriverWait initialValue(){
            WebDriverWait wait = new WebDriverWait(getDriver(), ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME);
            return wait;
        }
    };

    private static ThreadLocal<WebDriverWait> webDriverElementWait = new ThreadLocal<WebDriverWait>(){
        @Override
        protected WebDriverWait initialValue(){
            WebDriverWait wait = new WebDriverWait(getDriver(), ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME);
            return wait;
        }
    };

    private static ThreadLocal<Boolean> captureScreenShot = new ThreadLocal<Boolean>(){
        @Override
        protected Boolean initialValue(){
            return true;
        }
    };

    private static ThreadLocal<String> platformName = new ThreadLocal<String>();
    private static ThreadLocal<String> browserName = new ThreadLocal<String>();
    private static ThreadLocal<String> mobileEmulation = new ThreadLocal<String>();
    private static ThreadLocal<String> userAgent = new ThreadLocal<String>();

    public static WebDriver getDriver(){
        WebDriverThread webDriverThread = driverThread.get();
        if (webDriverThread.getWebDriver() != null) {
            if (!webDriverThread.getBrowser().equalsIgnoreCase(DriverManager.getBrowserName()) ||
                    !webDriverThread.getPlatform().equalsIgnoreCase(DriverManager.getPlatformName())||
                    !webDriverThread.getMobileEmulation().equalsIgnoreCase(DriverManager.getMobileEmulation())||
                    !webDriverThread.getUserAgent().equalsIgnoreCase(DriverManager.getUserAgent())) {
                    webDriverThread.quitDriver();
            }
        }
        WebDriver driver = driverThread.get().getDriver();
        if(driverStatusThread.get() == false) {
            webDriverPageWait.set(new WebDriverWait(driver, ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME));
            webDriverElementWait.set(new WebDriverWait(driver, ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME));
            driverStatusThread.set(true);
        }
        return driver;
    }


    public static WebDriver getCurrentWebDriver(){
        return driverThread.get().getWebDriver();
    }

    public static String getPlatformName() {
        return platformName.get();
    }

    public static void setPlatformName(String platform) {
        platformName.set(platform);
    }

    public static String getBrowserName() {
        return browserName.get();
    }

    public static void setBrowserName(String browser) {
        browserName.set(browser);
    }

    public static String getMobileEmulation() {
        return mobileEmulation.get();
    }

    public static String getUserAgent() {
        return userAgent.get();
    }

    public static void setMobileEmulation(String mobileEmulationValue) {
        mobileEmulation.set(mobileEmulationValue);
    }

    public static void setUserAgent(String userAgentValue) {
        userAgent.set(userAgentValue);
    }


    public static Boolean getCaptureScreenShot(){
        return captureScreenShot.get();
    }

    public static void setCaptureScreenShot(Boolean captureScreenShot){
        DriverManager.captureScreenShot.set(captureScreenShot);
    }

    public static WebDriverWait getWebDriverPageWait(){
       return webDriverPageWait.get();
    }

    public static void setWebDriverPageWait(Duration seconds){
        webDriverPageWait.set(new WebDriverWait(getDriver(), seconds));
    }

    public static WebDriverWait getWebDriverElementWait(){
        return webDriverElementWait.get();
    }

    public static void setWebDriverElementWait(Duration seconds){
        webDriverElementWait.set(new WebDriverWait(getDriver(), seconds));
    }

    public static void resetWebDriverPageWait(){
        if(driverThread.get().getWebDriver() != null)
            webDriverPageWait.set(new WebDriverWait(getDriver(), ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME));
    }

    public static void resetWebDriverElementWait(){
        if(driverThread.get().getWebDriver() != null)
            webDriverElementWait.set(new WebDriverWait(getDriver(), ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME));
    }

    public static void closeCurrentDriver() {
		driverThread.get().quitDriver();
	}

    public static void closeDriverObjects(){
        for(WebDriverThread webDriverThread: webDriverThreadPool){
            webDriverThread.quitDriver();
        }
    }
}