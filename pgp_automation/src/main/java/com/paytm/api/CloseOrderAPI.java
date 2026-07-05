package com.paytm.api;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CloseOrderAPI extends BaseApi {

    private final String endPoint = Constants.PGPAPIResourcePath.CLOSE_ORDER_V1;

    public CloseOrderAPI(CloseOrderDTO closeOrderDTO) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBody(closeOrderDTO);
    }

}
