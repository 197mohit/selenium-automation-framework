package com.paytm.framework.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

class WebDriverThread {

    public WebDriver webDriver;
//    private DesiredCapabilities capabilities;
    private FirefoxOptions ffoptions = new FirefoxOptions();
    private  ChromeOptions choptions = new ChromeOptions();
    private  SafariOptions sfoptions = new SafariOptions();
    private  InternetExplorerOptions ieoptions = new InternetExplorerOptions();
    private String browser;
    private String platform;
    private String mobileEmulation;
    private String userAgent;


    private void setPlatform() {
        if (ExecutionConfig.EXECUTION_ENVIRONMENT.equalsIgnoreCase("remote")) {
            switch (platform.toUpperCase()) {
                case "LINUX":
                    ffoptions.setCapability("platformName", "Linux");
                    choptions.setCapability("platformName", "Linux");
                    sfoptions.setCapability("platformName", "Linux");
                    ieoptions.setCapability("platformName", "Linux");
//                    capabilities.setPlatform(Platform.LINUX);
                    break;
                case "MAC":
                    ffoptions.setCapability("platformName", "macOS");
                    choptions.setCapability("platformName", "macOS");
                    sfoptions.setCapability("platformName", "macOS");
                    ieoptions.setCapability("platformName", "macOS");
//                    capabilities.setPlatform(Platform.MAC);
                    break;
                case "WINDOWS":
                    ffoptions.setCapability("platformName", "Windows");
                    choptions.setCapability("platformName", "Windows");
                    sfoptions.setCapability("platformName", "Windows");
                    ieoptions.setCapability("platformName", "Windows");
//                    capabilities.setPlatform(Platform.WINDOWS);
                    break;
                default:
                    throw new RuntimeException("Invalid execution environment: " + platform);
            }
        }
    }


    public WebDriver getDriver() {

        if (null == webDriver || ((RemoteWebDriver) webDriver).getSessionId() == null) {

            browser = DriverManager.getBrowserName();
            if (browser==null){
                DriverManager.setBrowserName("HEADLESS_CHROME");
//                ExecutionConfig.BROWSER = "CHROME";
                browser = DriverManager.getBrowserName();
            }
            platform = DriverManager.getPlatformName();
            mobileEmulation = DriverManager.getMobileEmulation();
            userAgent = DriverManager.getUserAgent();

            switch (browser.toUpperCase()) {

                case "FIREFOX":
//                    capabilities = DesiredCapabilities.firefox();
                    setPlatform();
                    FirefoxProfile profile = new FirefoxProfile();


                    profile.setAcceptUntrustedCertificates(true);
                    profile.setPreference("browser.download.folderList", 2);
                    profile.setPreference("browser.download.manager.showWhenStarting", false);
                    profile.setPreference("browser.download.dir", ExecutionConfig.TEMP_DATA_PATH);
                    profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;"
                                    + "application/pdf;"
                                    + "application/vnd.openxmlformats-officedocument.wordprocessingml.document;"
                                    + "text/plain;"
                                    + "text/csv"
                                    + "application/zip");
                    ffoptions.setProfile(profile);
                    ffoptions.setAcceptInsecureCerts(true);
                    ffoptions.setProfile(profile);
//                    capabilities.setAcceptInsecureCerts(true);
//                    capabilities.setCapability(FirefoxDriver.PROFILE, profile);
                    if (!ExecutionConfig.EXECUTION_ENVIRONMENT.equalsIgnoreCase("remote")) {
                        WebDriverManager.firefoxdriver().setup();
//                        webDriver = new FirefoxDriver(capabilities);
                        webDriver = new FirefoxDriver(ffoptions);
                    }
                    break;

                case "HEADLESS_CHROME":
                    setPlatform();
                    choptions.addArguments("--headless");
                    choptions.addArguments("--disable-gpu");
                    choptions.addArguments("--no-sandbox");
                    choptions.addArguments("--disable-dev-shm-usage");
                    WebDriverManager.chromedriver().setup();
                    webDriver = new ChromeDriver(choptions);
                    break;
                case "CHROME":
//                    capabilities = DesiredCapabilities.chrome();
                    setPlatform();
                    //choptions
//                    LoggingPreferences logPrefs = new LoggingPreferences();
//                    java.util.logging.LogManager.getLogManager().reset();
//                    logPrefs.enable(LogType.PERFORMANCE, Level.INFO);

//                    choptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
//      Important              choptions.setCapability("goog:loggingPrefs", "{\"browser\": \"ALL\"}");

//      Important                     choptions.setCapability("chrome.switches", Arrays.asList("--no-default-browser-check"));
//
//                    HashMap<String, String> chromePreferences = new HashMap<String, String>();
//                    chromePreferences.put("profile.password_manager_enabled", "false");
//                    chromePreferences.put("profile.default_content_settings.popups", "0");
//                    chromePreferences.put("download.prompt_for_download", "false");
//                    chromePreferences.put("download.default_directory", ExecutionConfig.TEMP_DATA_PATH);
                    //                    choptions.setCapability("chrome.prefs", chromePreferences);
//                    ChromeOptions options = new ChromeOptions();
                    //---------------Test commenting --------------------//
                    choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
                                put("profile.password_manager_enabled", false);
                            }});
                    choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
                                put("profile.default_content_settings.popups", 0);
                            }});
                    choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
                                put("download.prompt_for_download", false);
                            }});
//
//                    choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
//                                put("download.default_directory", ExecutionConfig.TEMP_DATA_PATH);
//                            }});
//
//
                    choptions.addArguments("--test-type");
                    choptions.addArguments("start-maximized");
                    choptions.addArguments("--disable-web-security");
                    choptions.addArguments("--disable-dev-shm-usage");
                    choptions.addArguments("--no-sandbox");
                    choptions.addArguments("--disable-extensions");
                    choptions.addArguments("--allow-running-insecure-content");
                    if(browser.toUpperCase().equals("HEADLESS_CHROME")) {
                        choptions.addArguments("--headless");
                    }
                    //options.addArguments("disable-infobars");
                    if (!mobileEmulation.isEmpty()) {
                        Map<String, String> mobileEmulationMap = new HashMap<String, String>();
                        mobileEmulationMap.put("deviceName", mobileEmulation);
                        choptions.setExperimentalOption("mobileEmulation", mobileEmulationMap);
                    }
                    if(!userAgent.isEmpty())
                        choptions.addArguments("--user-agent=" + userAgent);
//                    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                    if (!ExecutionConfig.EXECUTION_ENVIRONMENT.equalsIgnoreCase("remote")) {
                        WebDriverManager.chromedriver().setup();
                        webDriver = new ChromeDriver(choptions);
//                        webDriver = new ChromeDriver();
                    }
                    break;

                case "SAFARI":
//                    capabilities = DesiredCapabilities.safari();
                    setPlatform();
                    if (!ExecutionConfig.EXECUTION_ENVIRONMENT.equalsIgnoreCase("remote")) {
                        webDriver = new SafariDriver(sfoptions);
                    }
                    break;

                case "IE":
                    setPlatform();

//                    capabilities = DesiredCapabilities.internetExplorer();
                    ieoptions.setCapability("cleanSession", true);
//                    ieoptions.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
                    ieoptions.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
                    ieoptions.setCapability("requireWindowFocus", true);
                    if (!ExecutionConfig.EXECUTION_ENVIRONMENT.equalsIgnoreCase("remote")) {
                        WebDriverManager.iedriver().setup();
                        webDriver = new InternetExplorerDriver(ieoptions);
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid browser: " + browser);
            }

            if (ExecutionConfig.EXECUTION_ENVIRONMENT.equalsIgnoreCase("remote")) {
                try {
                    webDriver = new RemoteWebDriver(new URL(ExecutionConfig.HUB_NODE_URL), choptions);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Malformed HUB_URL", e);
                }
            }
            webDriver.manage().timeouts().pageLoadTimeout(ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME.toMillis(), TimeUnit.MILLISECONDS);
            webDriver.manage().timeouts().setScriptTimeout(ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME.toMillis(), TimeUnit.MILLISECONDS);
            webDriver.manage().timeouts().implicitlyWait(ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME.toMillis(), TimeUnit.MILLISECONDS);
            if (!browser.equalsIgnoreCase("chrome")) {
                webDriver.manage().window().maximize();
            }
        }
        return webDriver;
    }

    public void quitDriver() {
        if (null != webDriver) {
            webDriver.quit();
            webDriver = null;
            DriverManager.driverStatusThread.set(false);
        }
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    public String getBrowser() {
        return browser;
    }

    public String getPlatform() {
        return platform;
    }

    public String getMobileEmulation() {
        return mobileEmulation;
    }

    public String getUserAgent() {
        return userAgent;
    }
}