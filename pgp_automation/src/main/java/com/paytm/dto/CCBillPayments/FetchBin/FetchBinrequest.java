package com.paytm.dto.CCBillPayments.FetchBin;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class FetchBinrequest extends BaseApi {
    public  FetchBinrequest(FetchBin body){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/billproxy/api/v1/cc/fetchBin/request");
        getRequestSpecBuilder().setBody(body);
    }

}