package com.paytm.framework.reportportal.service;

import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.service.launchManager.FreshLaunch;
import com.paytm.framework.reportportal.service.launchManager.LaunchHandler;
import com.paytm.framework.reportportal.service.launchManager.RerunLaunch;

public class LoadFactory {

    public static LaunchHandler getLaunchHandler() {
        if (ReporterConfig.RP_RERUN.equalsIgnoreCase("true"))
            return new RerunLaunch();
        return new FreshLaunch();
    }


}
