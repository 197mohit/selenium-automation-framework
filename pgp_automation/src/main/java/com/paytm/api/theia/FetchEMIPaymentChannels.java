package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchEMIPaymentChannels extends BaseApi {

    public FetchEMIPaymentChannels(String merchantId, String orderId, String txnToken){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_EMI_PAYMENT_CHANNELS);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addQueryParam("mid", merchantId);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody("{\"head\":{\"version\":\"v1\",\"requestTimestamp\":\"Time\",\"channelId\":\"WEB\",\"txnToken\":"+txnToken+"},\"body\":{\"type\":\"merchant\"}}");
    }
}
