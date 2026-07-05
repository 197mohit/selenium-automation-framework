package com.paytm.listeners;

import com.paytm.base.test.PGPBaseTest;
import org.testng.IExecutionListener;

public class ExecutionListener implements IExecutionListener {

    /**
     * Invoked before the TestNG run starts.
     */
    @Override
    public void onExecutionStart() {
        System.out.println("________ Executing onExecutionStart ________");
        PGPBaseTest.executeLogCheckPrerequisites();
        PGPBaseTest.getMerchants();
        PGPBaseTest.setupUsers();
        PGPBaseTest.setEnvironmentDetails();
        PGPBaseTest.savedCardsDataReset();
        PGPBaseTest.FF4JFlags();
    }

    /**
     * Invoked once all the suites have been run.
     */
    @Override
    public void onExecutionFinish() {
        System.out.println("________ Executing onExecutionFinish ________");
        PGPBaseTest.copyProperties();
    }
}
