package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class MerchantPGPUITheme extends BaseApi {

    public MerchantPGPUITheme(Constants.MerchantType mid,Boolean V2)
    {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        if(V2==true) {
            getRequestSpecBuilder().setBasePath(Constants.merchantpgpui.GET_MERCHANTPGPUI_THEME_V2
                    .replace("{mid}", mid.getId()));
        }
        else if(V2==false)
        {
            getRequestSpecBuilder().setBasePath(Constants.merchantpgpui.GET_MERCHANTPGPUI_THEME
                    .replace("{mid}", mid.getId()));
        }
    }
}

