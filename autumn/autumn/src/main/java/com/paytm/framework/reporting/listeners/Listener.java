package com.paytm.framework.reporting.listeners;

import org.testng.*;

/**
 * Created by deepakkumar on 16/3/18.
 */
public interface Listener extends ITestListener, ISuiteListener, IInvokedMethodListener, IRetryAnalyzer,
        IAnnotationTransformer, IConfigurationListener, IExecutionListener {
}
