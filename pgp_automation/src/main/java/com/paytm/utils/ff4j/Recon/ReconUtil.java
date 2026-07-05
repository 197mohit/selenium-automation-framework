package com.paytm.utils.ff4j.Recon;

import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.ff4j.FF4JFlags;
import io.restassured.response.Response;

public class ReconUtil {

        FF4JFlags ff4JFlags = new FF4JFlags();
        User user;
        OrderDTO orderDTO;


    public boolean doRecon(User user, OrderDTO orderDTO, Response fromPlatform, Response fromService)
    {
        this.user= null!=user ? user:null;
        String userId = null!=user?user.custId():null;
        this.orderDTO=orderDTO;
        if (ff4JFlags.fetchSavedCardFromService(userId,orderDTO.getMID(),orderDTO.getCUST_ID())) {
            Reporter.report.info("Short circuit flag is disabled on userId: " + userId + " mid:  " +orderDTO.getMID()  + " custId: " +orderDTO.getCUST_ID() + " so, recon will happen");
            return isReconSuccess(fromPlatform,fromService);
        }
        Reporter.report.info("Short circuit flag is enabled on userId: " + userId + " mid:  " +orderDTO.getMID()  + " custId: " +orderDTO.getCUST_ID()+" so, no recon will happen");
        return false;
    }

    private boolean isReconSuccess(Response fromPlatform, Response fromService)
    {

        return true;
    }

}
