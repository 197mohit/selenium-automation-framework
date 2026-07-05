package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ResendNotificationLink extends BaseApi {
    String request= "{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"78yds87ty7ds\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"JWT\",\n" +
            "    \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzZW5kRW1haWwiOiJ0cnVlIiwic2VuZFNtcyI6InRydWUiLCJsaW5rSWQiOiIxNTIzIiwiaXNzIjoidHMiLCJtaWQiOiJhbmtvZmY4ODM3MjM0MjQzMTUyNyJ9.ZUEY-14PiBENeoRkIr57fEzPvD4waZ4NExSrQBVX5rI\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"mid\": \"RUPAYB58539831709103\",\n" +
            "    \"linkId\": 221275,\n" +
            "    \"sendSms\": \"true\",\n" +
            "    \"sendEmail\": \"true\",\n" +
            "     \"notifyContact\":{\n" +
            "          \"customerName\": \"Ravindra lal mishra pandey prakash\",\n" +
            "          \"customerEmail\": \"ravindralalmishrapandeyprakash@paytm.com\",\n" +
            "          \"customerMobile\": \"7014107741\"\n" +
            "}\n" +
            "  }\n" +
            "}";
    public ResendNotificationLink() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.RESENDNOTIFICATION_LINK);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest(){
        return request;
    }
    public  ResendNotificationLink buildRequest(String mid, String linkId){
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        setContext("body.notifyContact.customerName","Nirottam Singh");
        setContext("body.notifyContact.customerEmail","nirottam.singh@paytm.com");
        setContext("body.notifyContact.customerMobile","7014107741");
        return this;
    }
}
