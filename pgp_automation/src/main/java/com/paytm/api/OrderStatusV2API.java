package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderStatusV2.OrderStatusV2DTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class OrderStatusV2API extends BaseApi {

    private final String endPoint = "/merchant-status/v2/order/status";

    public OrderStatusV2API(OrderStatusV2DTO orderStatusV2DTO) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBody(orderStatusV2DTO);
    }
}
