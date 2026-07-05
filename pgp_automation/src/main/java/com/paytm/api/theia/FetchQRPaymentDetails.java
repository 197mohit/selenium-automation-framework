package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchQRPaymentDetails extends BaseApi {


    public FetchQRPaymentDetails(FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS);
        getRequestSpecBuilder().addQueryParam("appVersion","8.3.2");
        getRequestSpecBuilder().addQueryParam("client","android");

        getRequestSpecBuilder().setBody(fetchQRPaymentDetailsDTO);

    }

    public FetchQRPaymentDetails(FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO ,String version) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        getRequestSpecBuilder().addQueryParam("appVersion","8.3.2");
        getRequestSpecBuilder().addQueryParam("client","android");

        getRequestSpecBuilder().setBody(fetchQRPaymentDetailsDTO);

    }

}
