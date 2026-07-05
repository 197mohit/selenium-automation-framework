package com.paytm.api.subscription;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class PreNotifyStatus extends BaseApi  {

    String preNotifyStatusRequest="{ \"head\": { \"version\": \"v1\", \"signature\": \"aesdg123456789jhdfgrtyuaaaa=\", \"tokenType\": \"AES\", \"clientId\": \"1234\" }, \"body\": { \"subsId\": \"{SUBS_ID}\", \"mid\": \"{MID}\", \"referenceId\": \"{REFERENCE_ID}\" } }";

    public PreNotifyStatus setMID(String mid){
        setContext("body.mid",mid);
        return this;
    }

    public PreNotifyStatus setSubsId(String subsId) {
        setContext("body.subsId",subsId);
        return this;
    }

    public PreNotifyStatus setReferenceId(String referenceId) {
        setContext("body.referenceId",referenceId);
        return this;
    }

    public PreNotifyStatus() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.SubscriptionService.SUBSCRIPTION_PRENOTIFY_STATUS);
    }

    public String getRequest() {return preNotifyStatusRequest;}
}
