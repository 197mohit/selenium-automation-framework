package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetLinkInfo extends BaseApi {
    public GetLinkInfo(String linkId, String linkName) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.GET_LINK_INFO);
        getRequestSpecBuilder().addPathParam("linkId",linkId);
        getRequestSpecBuilder().addPathParam("linkName",linkName);
    }
}
