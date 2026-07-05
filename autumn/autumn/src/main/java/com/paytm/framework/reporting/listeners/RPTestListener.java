package com.paytm.framework.reporting.listeners;

import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.base.RPBaseListener;
import com.paytm.framework.reportportal.contants.FeaturesEvaluator;
import com.paytm.framework.reportportal.service.RPTestNGEmptyService;
import com.paytm.framework.reportportal.service.RPTestNGService;
import com.paytm.framework.reportportal.service.RPTestNGServiceImpl;
import com.paytm.framework.reportportal.service.RPTestNGService_ExecHandler;

public class RPTestListener extends RPBaseListener {

    private final static RPTestNGServiceImpl rpTestNGService;

    static {
//        rpTestNGService = ReporterConfig.mandatoryPropCheck ? new RPTestNGService() : new RPTestNGEmptyService();
        rpTestNGService = getRpTestNGService();
    }

    private static RPTestNGServiceImpl getRpTestNGService() {
        if (FeaturesEvaluator.FEATURE_ATLAS.equals(FeaturesEvaluator.FEATURE_VALUE.ENABLED)) {
            if (FeaturesEvaluator.FEATURE_ATLAS_EXISTING_LAUNCH.equals(FeaturesEvaluator.FEATURE_VALUE.ENABLED))
                return new RPTestNGService_ExecHandler();
            else
                return new RPTestNGService();
        } else
            return new RPTestNGEmptyService();
    }

    public RPTestListener() {
        super(rpTestNGService);
    }
}
