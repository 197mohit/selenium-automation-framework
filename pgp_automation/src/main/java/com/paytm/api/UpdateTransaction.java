package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.UpdateTransaction.UpdateTransactionDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class UpdateTransaction extends BaseApi {

    String endPoint = "/theia/api/v1/updateTransactionDetail";

    public UpdateTransaction(UpdateTransactionDTO updateTransactionDTO, String mid, String orderId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBody(updateTransactionDTO);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
    }

}