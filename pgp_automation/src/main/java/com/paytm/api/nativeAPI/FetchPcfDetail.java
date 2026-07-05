package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequestWithSSO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * Created by anjukumari on 14/05/19
 */

public class FetchPcfDetail extends BaseApi {

    public FetchPcfDetail(String mid, String orderId, FetchPcfRequest fetchPcfDetail) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PCF_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(fetchPcfDetail);
    }

    public FetchPcfDetail(String mid, FetchPcfRequestWithSSO fetchPcfDetail) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PCF_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(fetchPcfDetail);
    }


    public FetchPcfDetail(FetchPcfRequest fetchPcfRequest, InitTxnDTO initTxnDTO) {
        this(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPcfRequest);
    }

    public FetchPcfDetail(FetchPcfRequest fetchPcfRequest) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PCF_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", fetchPcfRequest.getBody().getMid());
        getRequestSpecBuilder().setBody(fetchPcfRequest);
    }
}
