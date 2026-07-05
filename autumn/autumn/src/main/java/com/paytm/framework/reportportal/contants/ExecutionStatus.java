package com.paytm.framework.reportportal.contants;


public final class ExecutionStatus {
    public static final String PASSED;
    public static final String FAILED;
    public static final String SKIPPED;

    private ExecutionStatus() {
    }

    static {
        PASSED = ItemStatusEnum.PASSED.name();
        FAILED = ItemStatusEnum.FAILED.name();
        SKIPPED = ItemStatusEnum.SKIPPED.name();
    }
}
