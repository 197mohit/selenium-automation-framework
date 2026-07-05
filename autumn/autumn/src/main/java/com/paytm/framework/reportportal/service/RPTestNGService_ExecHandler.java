package com.paytm.framework.reportportal.service;

import com.paytm.framework.reportportal.api.GetExistingLaunchInfo;
import com.paytm.framework.reportportal.service.dto.LaunchInfo;
import io.restassured.response.Response;

public class RPTestNGService_ExecHandler extends RPTestNGService {

    @Override
    public void startLaunch() {
        GetExistingLaunchInfo getExistingLaunchInfo = new GetExistingLaunchInfo();
        Response response = getExistingLaunchInfo.execute();
        assert response.statusCode() == 200 : "Unable to get launch-id from auto_exec_handler";
        String launchId = response.jsonPath().getString("launch_id");
        LaunchInfo.getInstance().setLaunchId(launchId);
    }

    @Override
    public void finishLaunch() { }


}
