package com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.request.RequestBody;
import io.restassured.http.ContentType;

public class OfflineIvrFastForward extends BaseApi {


    public OfflineIvrFastForward(RequestBody body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/HANDLER_IVR/CLW_APP_PAY/APP");
        getRequestSpecBuilder().setBody(body);
    }

}
