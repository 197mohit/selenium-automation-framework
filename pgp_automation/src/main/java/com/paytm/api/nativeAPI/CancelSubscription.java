package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.CancelSubscription.CancelSubscriptionDTO;
//import com.paytm.dto.NativeDTO.InitTxn.InitSubscriptionDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CancelSubscription extends BaseApi {


    public CancelSubscription(CancelSubscriptionDTO cancelSubscriptionDTO){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.CANCEL_SUBSCRIPTION);
        //getRequestSpecBuilder().addQueryParam("mid", mid);
        //getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(cancelSubscriptionDTO);
    }

}
