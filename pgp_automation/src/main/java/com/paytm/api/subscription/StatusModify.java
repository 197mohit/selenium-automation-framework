package com.paytm.api.subscription;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class StatusModify extends BaseApi {
    String statusModifyRequest="{\n" +
            "    \"head\":{\n" +
            "            \"signature\" : \"B8IN+UNxXQXjdvs08ngT5r+PAgoGSOKoqXhJYTr8FPc/fDQabKHt2zr3gV/wWxUTf4RFKFhtPagLLtzPfVO3by4c6HvFTVGBSsbs12Yp+S0=\",\n" +
            "            \"tokenType\" : \"AES\",\n" +
            "            \"token\"  : \"43663b36-9beb-4397-a467-b96ce1154600\"\n" +
            "        },\n" +
            "         \"body\": {\n" +
            "           \"status\":\"{STATUS}\"," +
            "           \"mid\":\"{MID}\" , " +
            "           \"subsId\" : \"{SUBS_ID}\" }\n" +
            "}";

    public StatusModify setStatus(String status) {
        setContext("body.status",status);
        return this;
    }

    public StatusModify setMID(String mid){
        setContext("body.mid",mid);
        return this;
    }

    public StatusModify setSubsId(String subsId) {
        setContext("body.subsId",subsId);
        return this;
    }

    public StatusModify() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.SubscriptionService.SUBSCRIPTION_STATUS_MODIFY);
    }

    public String getRequest() {return statusModifyRequest;}

}
