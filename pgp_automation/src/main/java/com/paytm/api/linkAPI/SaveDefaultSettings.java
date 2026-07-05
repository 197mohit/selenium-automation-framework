package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.framework.api.BaseApi;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;


@Owner("Shashank Gupta")
public class SaveDefaultSettings extends BaseApi {

    public static String getExpiryDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String output=CommonHelpers.addDays(date,"dd/MM/yyyy",5);
        return output;
    }

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"bwdedc94737648890192\",\n" +
            "        \"reminderDetails\": {\n" +
            "            \"removeDefaultReminder\": true,\n" +
            "            \"daysAfterIssueDate\": [1,2],\n" +
            "            \"daysBeforeExpiry\": [3],\n" +
            "            \"channels\": [\"PUSH\"]\n" +
            "        }\n" +
            "    }\n" +
            "}";


    public SaveDefaultSettings() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.Save_Default_Settings);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {return request;}


    public  SaveDefaultSettings buildRequest(String mid, String removeDefaultReminder, ArrayList<Integer> daysAfterIssueDate, ArrayList<Integer> daysBeforeExpiry, ArrayList<String> channels) {
      setContext("body.mid",mid);
      setContext("body.reminderDetails.removeDefaultReminder",removeDefaultReminder);
      setContext("body.reminderDetails.daysAfterIssueDate",daysAfterIssueDate);
      setContext("body.reminderDetails.daysBeforeExpiry",daysBeforeExpiry);
      setContext("body.reminderDetails.channels",channels);
      return this;
    }


}