package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class MerchantPGPUILocale extends BaseApi {

    public MerchantPGPUILocale() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("x-real-ip","127.0.0.1");
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.merchantpgpui.LOCALE_UPDATE);
    }

    protected String requestTemplatePath() {
        return "merchantpgpui_locale/localeEnglish.json";
    }

}
