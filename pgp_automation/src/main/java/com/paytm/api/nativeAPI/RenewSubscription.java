package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class RenewSubscription extends BaseApi {

    public RenewSubscription(RenewSubscriptionDTO renewSubscriptionDTO, String mid, String orderId){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.RENEW_SUBSCRIPTION);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(renewSubscriptionDTO);
    }

    public RenewSubscription(RenewSubscriptionDTO renewSubscriptionDTO){
        this(renewSubscriptionDTO,renewSubscriptionDTO.getBody().getMid(),renewSubscriptionDTO.getBody().getOrderId());
    }

}
