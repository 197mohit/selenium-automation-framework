package com.paytm.api.nativeAPI;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;

public class FetchPaymentOptionV5 extends FetchPaymentOptionV2{
    public FetchPaymentOptionV5(String mid, String orderId, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V5);
    }

    public FetchPaymentOptionV5(String mid, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V5);
    }

    public FetchPaymentOptionV5(String mid,String orderId,String Client,String UserAgent, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V5);
        getRequestSpecBuilder().addQueryParam("client",Client);
        getRequestSpecBuilder().addHeader("user-agent",UserAgent);
    }
    public FetchPaymentOptionV5(String mid,String orderId,String UserAgent, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V5);
        getRequestSpecBuilder().addHeader("user-agent",UserAgent);
    }
}

