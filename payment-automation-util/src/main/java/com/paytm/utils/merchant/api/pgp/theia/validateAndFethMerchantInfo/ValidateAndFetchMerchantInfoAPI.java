package com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo.request.RequestBody;
import io.restassured.http.ContentType;

public class ValidateAndFetchMerchantInfoAPI extends BaseApi {


    public ValidateAndFetchMerchantInfoAPI(RequestBody body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/api/v1/validateAndFetchMerchantInfo");
        getRequestSpecBuilder().setBody(body);
    }

}
