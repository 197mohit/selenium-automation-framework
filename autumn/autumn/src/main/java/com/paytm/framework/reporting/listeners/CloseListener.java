package com.paytm.framework.reporting.listeners;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.DatabaseUtil;
import org.testng.IExecutionListener;

public class CloseListener implements IExecutionListener {

    private void closeAllBrowsers() {
        System.out.println("!!!!!!!!!!!!! BROWSER CLOSE STARTED !!!!!!!!!!!!!!!!");
        DatabaseUtil.getInstance().closeAllConnections();
        DriverManager.closeDriverObjects();
    }

    /**
     * Invoked before the TestNG run starts.
     */
    @Override
    public void onExecutionStart() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        closeAllBrowsers();
                    }
                })
        );
        System.out.println("!!!!!!!!!!!!! BROWSER CLOSE REGISTERED !!!!!!!!!!!!!!!!");
    }

    /**
     * Invoked once all the suites have been run.
     */
    @Override
    public void onExecutionFinish() {
        closeAllBrowsers();
    }
}
