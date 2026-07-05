package com.paytm.framework.reportportal.service.launchManager;

import com.paytm.framework.reportportal.api.GetExistingLaunchInfo;
import com.paytm.framework.reportportal.api.StartLaunchRequest;
import com.paytm.framework.reportportal.service.dto.LaunchInfo;
import io.restassured.response.Response;

public class FreshLaunch extends LaunchHandler {
    @Override
    public String startLaunch() {
        StartLaunchRequest startLaunchRequest = new StartLaunchRequest();
        startLaunchRequest.createRequest();
        LaunchInfo.getInstance().setLaunchRequest(startLaunchRequest);
        Response respStartLaunch = startLaunchRequest.execute();
        assert respStartLaunch.statusCode() == 200 : "ATLAS launch is not successfull";
        GetExistingLaunchInfo getExistingLaunchInfo = new GetExistingLaunchInfo();
        Response respGetLaunch = getExistingLaunchInfo.execute();
        assert respGetLaunch.statusCode() == 200 : "ATLAS launch is not successfull";
        String launchId = respGetLaunch.jsonPath().getString("launch_id");
        return launchId;
    }
}
