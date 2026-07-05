package com.paytm.api.boss;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class MerchantDetails extends BaseApi {

    public MerchantDetails(String phoneNumber, String walletToken,String jwt,String client_id){
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        //getRequestSpecBuilder().addHeader("x-sso-token",walletToken);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("x-client-token",jwt);
        getRequestSpecBuilder().addHeader("x-client-id",client_id);
        getRequestSpecBuilder().setBaseUri(LocalConfig.BOSS_PANEL);
        String basePath = Constants.BossAPI.MERCHANT_DETAILS
                .replace("{phoneNumber}",phoneNumber);
        getRequestSpecBuilder().addQueryParam("searchBy","phoneNo");
        getRequestSpecBuilder().addQueryParam("searchValue",phoneNumber);
        getRequestSpecBuilder().setBasePath(basePath);


    }
}
