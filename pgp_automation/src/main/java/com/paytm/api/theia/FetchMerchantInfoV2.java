package com.paytm.api.theia;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.theia.FetchMerchantInfoV2DTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_MERCHANT_INFO_V2;

public class FetchMerchantInfoV2 extends BaseApi {

    public FetchMerchantInfoV2(FetchMerchantInfoV2DTO requestDto) {
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_MERCHANT_INFO_V2)
                .addQueryParam("mid", requestDto.getBody().getMid())
                .addQueryParam("orderId", requestDto.getBody().getOrderId())
                .setBody(requestDto);
        setMethod(MethodType.POST);
    }

    /** Same payload as {@link #FetchMerchantInfoV2(FetchMerchantInfoV2DTO)} built from scalar params. */
    public FetchMerchantInfoV2(String mid, String orderId, String txnToken, String ssoToken) {
        this(new FetchMerchantInfoV2DTO.Builder(mid, orderId, txnToken, ssoToken).build());
    }
}
