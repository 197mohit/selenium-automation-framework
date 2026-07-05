package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * Created by nikunjkumar on 15/09/17.
 */
public class AddMoney extends BaseApi {

    public AddMoney(String ssoToken) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("ssotoken", ssoToken);
        getRequestSpecBuilder().addHeader("is_admin", "false");
        getRequestSpecBuilder().addHeader("tokentype", "OAUTH");
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        getRequestSpecBuilder().setBasePath("/wallet-web/addMoneyCash");
    }

}
