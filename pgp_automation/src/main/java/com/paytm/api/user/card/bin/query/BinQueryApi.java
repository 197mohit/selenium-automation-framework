package com.paytm.api.user.card.bin.query;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class BinQueryApi extends BaseApi {

    public BinQueryApi(String bin){
        String request = "{\"bin\":\""+bin+"\", \"fetchBlocked\":\"false\"}";
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_QUERY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }
    public BinQueryApi(String bin,String binStatus)
    {
        String request = "{ \"bin\":\""+bin+"\", \"fetchBlocked\":\""+binStatus+"\" }";
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_QUERY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }
}
