package com.paytm.listeners;

import com.paytm.base.test.Group.Status;
import com.paytm.framework.reporting.listenerDecorators.ListenerDecorator;
import com.paytm.framework.reporting.listeners.NullListener;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import com.paytm.utils.merchant.util.exception.walletException.WalletException;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.util.Arrays;
import java.util.List;

public class FailureHandlingListener extends ListenerDecorator {

    public FailureHandlingListener() {
        super(new NullListener());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable e = result.getThrowable();
        if (e instanceof WalletException)
            handleWalletException(result);
        if (e instanceof AuthException)
            handleAuthException(result);
        if (e.getMessage() != null && Arrays.asList("no new entities available to be given to the thread", "resource not acquired in the specified time").contains(e.getMessage().toLowerCase()))
            handleResourceAllocationException(result);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            List<String> groups = Arrays.asList(testResult.getMethod().getGroups());
            if(testResult.getStatus() == ITestResult.FAILURE){
                if (groups.contains(Status.BUG)) {
                    testResult.setStatus(3);
                    testResult.setThrowable(new SkipException("It's a known bug."));
                }
            }
        }
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            List<String> groups = Arrays.asList(testResult.getMethod().getGroups());
            if (groups.contains(Status.TO_BE_FIXED)) {
                throw new SkipException("Test case needs to be run manually");
            }
//            if (groups.contains(Status.BUG)) {
//                throw new SkipException("It's a known bug.");
//            }
        }
    }

    private void handleWalletException(ITestResult result) {
        result.setStatus(ITestResult.SKIP);
    }

    private void handleAuthException(ITestResult result) {
        result.setStatus(ITestResult.SKIP);
    }

    private void handleResourceAllocationException(ITestResult result) {
        result.setStatus(ITestResult.SKIP);
    }

}
