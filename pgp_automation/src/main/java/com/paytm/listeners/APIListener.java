package com.paytm.listeners;

import com.paytm.framework.reporting.listenerDecorators.ListenerDecorator;
import com.paytm.framework.reporting.listeners.NullListener;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.lang.reflect.InvocationTargetException;

public class APIListener extends ListenerDecorator {

    public APIListener() {
        super(new NullListener());
    }

    @Override
    public void onTestStart(ITestResult result) {
        try {
            result.getTestClass().getRealClass().getMethod("prepareAPIBody").invoke(result.getInstance());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            result.setStatus(ITestResult.SKIP);
            throw new SkipException("couldn't call prepareAPIBody() before API test", e);
        }
    }
}
