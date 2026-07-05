package com.paytm.framework.reporting.listenerDecorators;

import com.paytm.framework.reporting.listeners.Listener;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by deepakkumar on 16/3/18.
 */
public abstract class ListenerDecorator implements Listener {

    protected Listener decoratedListener;

    public ListenerDecorator(Listener decoratedListener) {
        this.decoratedListener = decoratedListener;
    }

  @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        this.decoratedListener.transform(annotation, testClass, testConstructor, testMethod);
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        this.decoratedListener.onConfigurationSuccess(itr);
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        this.decoratedListener.onConfigurationFailure(itr);
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        this.decoratedListener.onConfigurationSkip(itr);
    }

    @Override
    public void onExecutionStart() {
        this.decoratedListener.onExecutionStart();
    }

    @Override
    public void onExecutionFinish() {
        this.decoratedListener.onExecutionFinish();
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        this.decoratedListener.beforeInvocation(method, testResult);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        this.decoratedListener.afterInvocation(method, testResult);
    }

    @Override
    public boolean retry(ITestResult result) {
        return false;
    }

    @Override
    public void onStart(ISuite suite) {
        this.decoratedListener.onStart(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        this.decoratedListener.onFinish(suite);
    }

    @Override
    public void onTestStart(ITestResult result) {
        this.decoratedListener.onTestStart(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        this.decoratedListener.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        this.decoratedListener.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        this.decoratedListener.onTestSkipped(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        this.decoratedListener.onTestFailedButWithinSuccessPercentage(result);
    }

    @Override
    public void onStart(ITestContext context) {
        this.decoratedListener.onStart(context);
    }

    @Override
    public void onFinish(ITestContext context) {
        this.decoratedListener.onFinish(context);
    }
}
