package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * Created by anjukumari on 30/10/18
 */

public class FetchEMIDetail extends BaseApi{
    public FetchEMIDetail(FetchEMIDetailRequest fetchEMIDetailRequest, String mid, String orderId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_EMI_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(fetchEMIDetailRequest);
    }

    public FetchEMIDetail(FetchEMIDetailRequest fetchEMIDetailRequest, String mid) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_EMI_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(fetchEMIDetailRequest);
    }

}
