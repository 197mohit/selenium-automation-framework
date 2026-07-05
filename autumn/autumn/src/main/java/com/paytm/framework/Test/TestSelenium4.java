package com.paytm.framework.Test;

import com.paytm.framework.core.ExecutionConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.util.Arrays;

public class TestSelenium4 {

    public static ChromeOptions choptions = new ChromeOptions();

        public static void main(String[] args) {
//            choptions.setCapability("goog:loggingPrefs", "{\"browser\": \"ALL\"}");
//            choptions.setCapability("chrome.switches", Arrays.asList("--no-default-browser-check"));

            choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
                                put("profile.password_manager_enabled", false);
                           }});

            choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
                                put("profile.default_content_settings.popups", 0);
                            }});
                    choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
                                put("download.prompt_for_download", false);
                            }});

//                    choptions.setExperimentalOption("prefs", new java.util.HashMap<String, Object>() {{
//                                put("download.default_directory", ExecutionConfig.TEMP_DATA_PATH);
//                            }});

            choptions.addArguments("--test-type");
                    choptions.addArguments("start-maximized");
                    choptions.addArguments("--disable-web-security");
                    choptions.addArguments("--disable-dev-shm-usage");
                    choptions.addArguments("--no-sandbox");
                    choptions.addArguments("--disable-extensions");
                    choptions.addArguments("--allow-running-insecure-content");

            // Set the path to the ChromeDriver executable
//            System.setProperty("webdriver.chrome.driver", "/Users/rahulgulati/.cache/selenium/chromedriver/mac64/114.0.5735.90/chromedriver");

            // Create an instance of the ChromeDriver
//            WebDriverManager.chromedriver().clearDriverCache().setup();

            WebDriverManager.chromedriver().setup();
            WebDriver driver = new ChromeDriver(choptions);

            // Navigate to a website
            driver.get("https://www.google.com");

            // Perform actions on the website
            // ...

            // Close the browser
            driver.quit();
        }


}
