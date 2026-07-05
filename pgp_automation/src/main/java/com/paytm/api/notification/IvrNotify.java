package com.paytm.api.notification;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class IvrNotify extends BaseApi {

    String request = "{\n" +
            "    \"request\": {\n" +
            "        \"actions\": [\n" +
            "            {\n" +
            "                \"actionType\": \"TXN_HISTORY_SMS\",\n" +
            "                \"actionFilter\": {\n" +
            "                    \"posId\": null,\n" +
            "                    \"txnCount\": 1\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"actionType\": \"BALANCE_SMS\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"actionType\": \"SETTLEMENT_SMS\",\n" +
            "                \"actionFilter\": {\n" +
            "                    \"posId\": null,\n" +
            "                    \"txnCount\": 2\n" +
            "\n" +
            "                }\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"platformName\": \"PayTM\",\n" +
            "    \"ipAddress\": \"127.0.0.1\",\n" +
            "    \"operationType\": \"MERCHANT_TXN_HISTORY\",\n" +
            "    \"channel\": \"WEB\",\n" +
            "    \"version\": \"1.0\"\n" +
            "}";



    public IvrNotify(String mobileNumer) {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.IVR_NOTIFY);
        getRequestSpecBuilder().setBody(getRequest());
        if(mobileNumer != ""){
            getRequestSpecBuilder().addHeader("PHONE",mobileNumer.substring(0,10));
        }
        getRequestSpecBuilder().addHeader("HASH","sampleclienhash");
        getRequestSpecBuilder().addHeader("CLIENTID","notification");
        getRequestSpecBuilder().addHeader("Content-Type","application/json");

    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}




