package com.paytm.api.boss;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class BlockUnblockStatus extends BaseApi {
    String request="{\"mid\":\"AUTOME80347438632342\",\"status\":\"INACTIVE\",\"tag\":\"Duplicate\",\"comment\":\"\"}";
    public BlockUnblockStatus(String mid,String walletToken){
        setMethod(MethodType.PUT);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("x-sso-token",walletToken);
        getRequestSpecBuilder().setBaseUri(LocalConfig.BOSS_PANEL);
        String basePath = Constants.BossAPI.BLOCK_UNBLOCK_STATUS;
        getRequestSpecBuilder().setBasePath(basePath);
        getRequestSpecBuilder().setBody(getRequest());
    }
    public String getRequest() {return request;}
    public void buildRequest(String mid){
        setContext("mid",mid);
        return;
    }
}
