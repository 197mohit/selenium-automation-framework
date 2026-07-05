package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;


public class VersionChange extends BaseApi {

  public VersionChange(){

  }
    public VersionChange ChangeVersion(String NewVersion, String mid){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.merchantpgpui.CHANGE_VERSION
                .replace("{num}",NewVersion));
        getRequestSpecBuilder().setBody("[\""+ mid + "\"]");
        return this;

    }

        public VersionChange ValidateVersion(String NewVersion){
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.merchantpgpui.GET_VERSION
                .replace("{num}",NewVersion));
        return this;

    }


}
