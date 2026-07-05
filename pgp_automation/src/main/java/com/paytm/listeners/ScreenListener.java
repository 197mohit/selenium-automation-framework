package com.paytm.listeners;

import com.paytm.framework.core.DriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class ScreenListener extends TestListenerAdapter {

    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);

        if (DriverManager.getCurrentWebDriver() != null && DriverManager.getCaptureScreenShot()) {
            Throwable e = result.getThrowable();
            if (!(e instanceof SQLException || e instanceof IOException)) {
                byte[] screenshot = captureScreenshot();
                attachScreenshotToAllure(screenshot);
            }
        }
    }

    @Attachment(value = "Failure Screenshot", type = "image/png")
    public byte[] captureScreenshot() {
        byte[] screenshot = null;
        try {
            screenshot = ((TakesScreenshot) DriverManager.getCurrentWebDriver()).getScreenshotAs(OutputType.BYTES);
        } catch (WebDriverException e) {
            e.printStackTrace();
        }
        return screenshot;
    }

    public void attachScreenshotToAllure(byte[] screenshot) {
        if (screenshot != null) {
            Allure.addAttachment("Failure Screenshot", new ByteArrayInputStream(screenshot));
        }
    }
}
