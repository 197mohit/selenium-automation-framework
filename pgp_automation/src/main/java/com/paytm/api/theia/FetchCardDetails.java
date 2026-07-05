package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.FetchCardDetailsDTO.FetchCardDetailsDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchCardDetails extends BaseApi {

    public FetchCardDetails(FetchCardDetailsDTO fetchCardDetailsDTO,String orderId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_CARD_DETAILS);
        getRequestSpecBuilder().addQueryParam("mid",fetchCardDetailsDTO.getBody().getMid());
        getRequestSpecBuilder().addQueryParam("orderId",orderId);

        getRequestSpecBuilder().setBody(fetchCardDetailsDTO);

    }

}
