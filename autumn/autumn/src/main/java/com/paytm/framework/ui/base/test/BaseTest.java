package com.paytm.framework.ui.base.test;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.listeners.CloseListener;
import com.paytm.framework.utils.DatabaseUtil;
import org.testng.IExecutionListener;
import org.testng.annotations.*;

@Listeners({CloseListener.class})
public abstract class BaseTest {

    @Parameters({"browser", "platform","mobileEmulation","userAgent"})
    @BeforeSuite (alwaysRun = true)
    public void setBrowserBeforeSuite(@Optional("") String browser, @Optional("") String platform,  @Optional("") String mobileEmulation,  @Optional("") String userAgent) {
        setEnv(browser, platform, mobileEmulation,userAgent);
    }

    @Parameters({"browser", "platform","mobileEmulation","userAgent"})
    @BeforeTest(alwaysRun = true)
    public void setBrowserBeforeTest(@Optional("") String browser, @Optional("") String platform,  @Optional("") String mobileEmulation,  @Optional("") String userAgent) {
        setEnv(browser, platform, mobileEmulation,userAgent);
    }

    @Parameters({"browser", "platform","mobileEmulation","userAgent"})
    @BeforeClass(alwaysRun = true)
    public void setBrowserBeforeClass(@Optional("") String browser, @Optional("") String platform,  @Optional("") String mobileEmulation,  @Optional("") String userAgent) {
        setEnv(browser, platform, mobileEmulation,userAgent);
    }

    @Parameters({"browser", "platform","mobileEmulation","userAgent"})
    @BeforeMethod(alwaysRun = true)
    public void setBrowserBeforeMethods(@Optional("") String browser, @Optional("") String platform, @Optional("") String mobileEmulation,  @Optional("") String userAgent) {
        setEnv(browser, platform, mobileEmulation,userAgent);
    }

    @AfterMethod(alwaysRun = true)
    public void setScreenCaptureTrue() {
        DriverManager.setCaptureScreenShot(true);
    }

    @AfterMethod(alwaysRun = true)
    public void resetElementWait() {
        DriverManager.resetWebDriverElementWait();
    }

    @AfterMethod(alwaysRun = true)
    public void resetPageLoadWait() {
        DriverManager.resetWebDriverPageWait();
    }

    private void setEnv(String browser, String platform, String mobileEmulation, String userAgent) {
        if(browser == null || browser.isEmpty()){
            DriverManager.setBrowserName(ExecutionConfig.BROWSER);
        }else{
            DriverManager.setBrowserName(browser);
        }

        if(platform == null || platform.isEmpty()){
            DriverManager.setPlatformName(ExecutionConfig.PLATFORM);
        }else{
            DriverManager.setPlatformName(platform);
        }

        if(mobileEmulation == null || mobileEmulation.isEmpty() ){
            DriverManager.setMobileEmulation(ExecutionConfig.MOBILE_EMULATION);
        }else{
            DriverManager.setMobileEmulation(mobileEmulation);
        }

        if(userAgent == null || userAgent.isEmpty() ){
            DriverManager.setUserAgent(ExecutionConfig.USER_AGENT);
        }else{
            DriverManager.setUserAgent(userAgent);
        }
    }
}