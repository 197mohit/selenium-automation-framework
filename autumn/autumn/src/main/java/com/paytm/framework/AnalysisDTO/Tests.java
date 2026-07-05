package com.paytm.framework.AnalysisDTO;

public class Tests {
    private String testCaseName;
    private String testCaseDescription;
    private String testCaseExp;
    private String failureMsg;
    private String comments;

    public String getTestCaseName() {
        return testCaseName;
    }

    public Tests setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
        return this;
    }

    public String getTestCaseDescription() {
        return testCaseDescription;
    }

    public Tests setTestCaseDescription(String testCaseDescription) {
        this.testCaseDescription = testCaseDescription;
        return this;
    }

    public String getTestCaseExp() {
        return testCaseExp;
    }

    public Tests setTestCaseExp(String testCaseExp) {
        this.testCaseExp = testCaseExp;
        return this;
    }

    public String getFailureMsg() {
        return failureMsg;
    }

    public Tests setFailureMsg(String failureMsg) {
        this.failureMsg = failureMsg;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Tests setComments(String comments) {
        this.comments = comments;
        return this;
    }

}
