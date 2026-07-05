package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.ArrayList;

public class SaveOrUpdateLinkDefaultSettingsApi extends BaseApi {

    String request="{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"78yds87ty7ds\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"AES\",\n" +
            "    \"signature\": \"jhddyt87td87vd\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"mid\": \"Online97162038260814\",\n" +
            "     \"reminderDetails\":{\n" +
            "      \"daysAfterIssueDate\":[1],\n" +
            "      \"daysBeforeExpiry\":[1],\n" +
            "      \"channels\":[\"SMS\"]\n" +
            "   },\n" +
            "    \"captureCustomerDetails\" : true\n" +
            "  }\n" +
            "}";

    public SaveOrUpdateLinkDefaultSettingsApi() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.SAVE_OR_UPDATE_LINK);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest(){
        return request;
    }

    public SaveOrUpdateLinkDefaultSettingsApi buildRequest(String mid, ArrayList<Integer> daysAfterIssueDate,ArrayList<Integer> daysBeforeExpiry,ArrayList<String> channels){
        setContext("body.mid",mid);
        setContext("body.reminderDetails.daysAfterIssueDate",daysAfterIssueDate);
        setContext("body.reminderDetails.daysBeforeExpiry",daysBeforeExpiry);
        setContext("body.reminderDetails.channels",channels);
        return this;
    }
}
