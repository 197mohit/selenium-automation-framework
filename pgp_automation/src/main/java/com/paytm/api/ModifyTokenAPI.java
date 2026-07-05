package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ModifyTokenAPI extends BaseApi{

    public ModifyTokenAPI(String binMin,String binMax) {
        //Deprecated
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setBaseUri(LocalConfig.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(Constants.Alipay.USER_TOKEN_MODIFY);   //user/token/bin/modify
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("Authorization", "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
        getRequestSpecBuilder().setBody("{\"binMin\":"+binMin+",\"binMax\":"+binMax+",\"cardType\":\"DC\",\"cardScheme\":\"VISA\",\"institutionId\":\"HDFC\",\"blocked\":\"false\",\"binConfigAttributes\":{\"CATEGORY\":\"GOLD\",\"INDIAN\":\"true\",\"CORPORATE_CARD\":false,\"PREPAID_CARD\":false},\"source\":\"administrator\",\"countryCode\":\"IN\"}");
    }
}