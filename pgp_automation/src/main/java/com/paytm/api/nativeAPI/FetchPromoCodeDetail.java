package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchPromoCode.FetchPromoCodeDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchPromoCodeDetail extends BaseApi {

    public FetchPromoCodeDetail(String mid, String orderId, FetchPromoCodeDTO fetchPromoCodeDTO) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PROMO_DETAIL);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(fetchPromoCodeDTO);

    }

    public FetchPromoCodeDetail(FetchPromoCodeDTO fetchPromoCodeDTO, InitTxnDTO initTxnDTO) {
        this(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPromoCodeDTO);
    }
}
