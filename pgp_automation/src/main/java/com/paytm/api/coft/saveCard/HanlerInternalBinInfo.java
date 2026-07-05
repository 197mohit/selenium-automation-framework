package com.paytm.api.coft.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class HanlerInternalBinInfo extends BaseApi{
    public HanlerInternalBinInfo(String ssoToken,Boolean returnToken ) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_HANDLER_INTERNAL_BIN_INFO);
        getRequestSpecBuilder().addQueryParam("JsonData", "{\"SSOToken\":\"" + ssoToken + "\"}");
        getRequestSpecBuilder().addQueryParam("returnToken",returnToken);
    }

    public HanlerInternalBinInfo(String ssoToken) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_HANDLER_INTERNAL_BIN_INFO);
        getRequestSpecBuilder().addQueryParam("JsonData", "{\"SSOToken\":\"" + ssoToken + "\"}");
        getRequestSpecBuilder().addQueryParam("returnToken");
    }

    public HanlerInternalBinInfo(String ssoToken,Boolean returnToken,String vaultIdentifier ) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_HANDLER_INTERNAL_BIN_INFO);
        getRequestSpecBuilder().addQueryParam("JsonData", "{\"SSOToken\":\"" + ssoToken + "\"}");
        getRequestSpecBuilder().addQueryParam("returnToken",returnToken);
        getRequestSpecBuilder().addQueryParam("vaultIdentifier",vaultIdentifier);
    }

    public HanlerInternalBinInfo(String ssoToken,Boolean returnToken,String vaultIdentifier,String X_APP_RID ) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_HANDLER_INTERNAL_BIN_INFO);
        getRequestSpecBuilder().addQueryParam("JsonData", "{\"SSOToken\":\"" + ssoToken + "\"}");
        getRequestSpecBuilder().addQueryParam("returnToken",returnToken);
        getRequestSpecBuilder().addQueryParam("vaultIdentifier",vaultIdentifier);
        getRequestSpecBuilder().addHeader("X-APP-RID",X_APP_RID);

    }
}
