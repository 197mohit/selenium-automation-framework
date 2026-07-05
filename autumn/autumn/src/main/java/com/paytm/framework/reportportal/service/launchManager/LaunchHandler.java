package com.paytm.framework.reportportal.service.launchManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.reportportal.api.FinishLaunchRequest;
import com.paytm.framework.reportportal.api.StartLaunchRequest;
import com.paytm.framework.reportportal.service.dto.LaunchInfo;

import java.util.Map;

public abstract class LaunchHandler {

    public abstract String startLaunch();

    public void finishLaunch(String launchId, String status) {
        FinishLaunchRequest finishLaunchRequest = new FinishLaunchRequest(launchId)
                .createRequest(status);
//        finishLaunchRequest.setContext("description", System.lineSeparator() + "launchId: " + launchId);
        finishLaunchRequest.execute();
    }

}
