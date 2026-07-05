package com.paytm.api.nativeAPI;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;

public class FetchPaymentOptionV2 extends FetchPaymentOption{
    public FetchPaymentOptionV2(String mid, String orderId, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2);
    }

    public FetchPaymentOptionV2(String mid, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2);
    }

    public FetchPaymentOptionV2(String mid,String orderId,String Client,String UserAgent, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2);
        getRequestSpecBuilder().addQueryParam("client",Client);
        getRequestSpecBuilder().addHeader("user-agent",UserAgent);
    }
    public FetchPaymentOptionV2(String mid,String orderId,String UserAgent, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        super(mid, orderId, fetchPaymentOptionsDTO);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2);
        getRequestSpecBuilder().addHeader("user-agent", UserAgent);
    }
}
