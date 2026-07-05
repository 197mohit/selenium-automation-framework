package com.paytm.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int maxRetryCount = 2;

    @Override
    public boolean retry(ITestResult iTestResult) {

        if (!iTestResult.isSuccess()) {                                     //Check if test not succeed
            if (retryCount < maxRetryCount) {                               //Check if maxtry count is reached
                retryCount++;                                               //Increase the maxTry count by 1
                iTestResult.setStatus(ITestResult.FAILURE);                 //Mark test as failed
                System.out.println("Retrying Test method : "+iTestResult.getName() + " for " + retryCount +" times. ");
                return true;                                                //Tells TestNG to re-run the test
            } else {
                iTestResult.setStatus(ITestResult.FAILURE);                 //If maxCount reached,test marked as failed
            }
        } else {
            iTestResult.setStatus(ITestResult.SUCCESS);                     //If test passes, TestNG marks it as passed
        }
        return false;
    }
}
