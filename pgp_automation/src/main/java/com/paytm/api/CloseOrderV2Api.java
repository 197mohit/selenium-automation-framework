/**
 * @desc This class is used to write all the logic for close order V2 API
 */

package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.CloseOrderV2.CloseOrderDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.paytm.appconstants.Constants.PGPAPIResourcePath.CLOSE_ORDER_V2;

public class CloseOrderV2Api extends BaseApi {

    public CloseOrderV2Api(String tokenType, String merchantId,
                           String orderId, String userToken){
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setOrderId(orderId)
                .setMid(merchantId)
                .setToken(userToken)
                .setTokenType(tokenType)
                .build();
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setBody(closeOrderDTO);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CLOSE_ORDER_V2);
        getRequestSpecBuilder().addQueryParam("mid", merchantId);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
    }

    /**
     * Constructor with extra forceClose param to close the order
     * @param tokenType
     * @param merchantId
     * @param orderId
     * @param userToken
     * @param forceClose
     */
    public CloseOrderV2Api(String tokenType, String merchantId,
                           String orderId, String userToken, String forceClose){
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO.Builder()
                .setOrderId(orderId)
                .setMid(merchantId)
                .setToken(userToken)
                .setTokenType(tokenType)
                .setForceClose(forceClose)
                .build();
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setBody(closeOrderDTO);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CLOSE_ORDER_V2);
        getRequestSpecBuilder().addQueryParam("mid", merchantId);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
    }
}
