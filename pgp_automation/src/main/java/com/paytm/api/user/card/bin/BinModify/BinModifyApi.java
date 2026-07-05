package com.paytm.api.user.card.bin.BinModify;


import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;



/**
 * Owner - Sourav Singh
 * Date - 10/07/2022
 **/


public class BinModifyApi extends BaseApi {

    public BinModifyApi(String bin, String blockedStatus)
    {
        String request = "{\n" +
                "    \"bin\": \""+bin+"\",\n" +
                "    \"source\": \"ADMIN\",\n" +
                "           \"cardType\": \"CC\",\n" +
                "    \"blocked\": \""+blockedStatus+"\",\n" +
                "}";
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_MODIFY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }

    public BinModifyApi(BinModifyRequest requestBody) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_MODIFY);
        getRequestSpecBuilder().setBody(requestBody);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");

    }
}
