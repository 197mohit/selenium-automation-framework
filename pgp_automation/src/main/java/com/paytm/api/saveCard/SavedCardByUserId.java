package com.paytm.api.saveCard;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SavedCardByUserId extends BaseApi {

    String body = "{\n" +
            "  \"head\": {\n" +
            "    \"clientId\": \"C11\",\n" +
            "    \"version\": \"v1\",\n" +
            "    \"requestTimestamp\": \"Time\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"JWT\",\n" +
            "    \"token\": \"{JWTTOKEN}\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"userId\": \"{USERID}\",\n" +
            "        \"isCardIndexNumberRequired\":\"{INDEX}\"\n" +
            "  }\n" +
            "}";

    public String getRequest()
    {
            return body;
    }

    public void setRequest(String JWTtoken,String userId , String isCardIndexNumberRequired)
    {
          body =  body.replace("{JWTTOKEN}",JWTtoken).replace("{USERID}",userId)
                    .replace("{INDEX}",isCardIndexNumberRequired);
    }


    public SavedCardByUserId(String JWTtoken, String userId, String isCardIndexNumberRequired )
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/savedcardservice/savedcardOpenAPIService/v1/savedcardsByUserId");

        setRequest(JWTtoken,userId,isCardIndexNumberRequired);
        getRequestSpecBuilder().setBody(getRequest());
    }



}
