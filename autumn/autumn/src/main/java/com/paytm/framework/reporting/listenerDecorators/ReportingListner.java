package com.paytm.framework.reporting.listenerDecorators;

import com.paytm.framework.AnalysisDTO.Tests;
import com.paytm.framework.exception.CustomException;
import com.paytm.framework.exception.KnownException;
import com.paytm.framework.reporting.listeners.NullListener;
import com.paytm.framework.utils.AnalysisUtils;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReportingListner extends ListenerDecorator {

    private AnalysisUtils analysisUtils;
    private List<Tests> objList = new ArrayList<>();
    private Tests[] tests;

    public ReportingListner() {
        super(new NullListener());
        analysisUtils = AnalysisUtils.getinstance();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testCaseName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        String description = Optional.ofNullable(result.getMethod().getDescription()).orElse(testCaseName).replace("\n", "");
        String failureMsg = "";
        String throwable = result.getThrowable().toString().replace("\n", "");
        try {
            failureMsg = result.getThrowable().getMessage().replace("\n", "");
        } catch (Exception e) {
        }

        Tests test = new Tests();
        if (analysisUtils.getTestDetails().containsKey(testCaseName)) {

            test = analysisUtils.getTestDetails().get(testCaseName);
            boolean condition = failureMsg.equalsIgnoreCase(test.getFailureMsg());
            if (condition) {
                if (test.getComments().equalsIgnoreCase("")) {

                } else
                    result.setThrowable(new KnownException("Known Issue", new CustomException(test.getComments(),
                            result.getThrowable(), true, true), true, false));

            } else
                test.setFailureMsg(failureMsg)
                        .setTestCaseExp(throwable)
                        .setComments("");

        } else
            test.setTestCaseName(testCaseName)
                    .setTestCaseDescription(description)
                    .setFailureMsg(failureMsg)
                    .setTestCaseExp(throwable)
                    .setComments("");

        super.onTestFailure(result);
        objList.add(test);
    }

    @Override
    public void onExecutionStart() {
        analysisUtils.loadAnalysis();
        super.onExecutionStart();
    }

    @Override
    public void onExecutionFinish() {

        tests = new Tests[objList.size()];
        for (int i = 0; i < tests.length; i++) {
            tests[i] = objList.get(i);
        }

        analysisUtils.writeAnalysis(tests);
        super.onExecutionFinish();
    }


}
