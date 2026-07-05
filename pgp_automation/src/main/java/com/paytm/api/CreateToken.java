package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

public class CreateToken extends BaseApi {
    String mid;
    String token;

    String body = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"C11\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"{TOKEN}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\":\"{MID}\",\n" +
            "        \"referenceId\":\"{REFERENCEID}\"\n" +
            "    }\n" +
            "}";

String bodyforchecksum="{" + "\"mid\":\"{MID}\"," + "\"referenceId\":\"{REFERENCEID}\"" + "}";


    public String getRequest()
    {
        return body;
    }


    public  String setMidReferenceID( Constants.MerchantType mid, String referenceId){
        bodyforchecksum=bodyforchecksum.replace("{MID}", mid.getId()).replace("{REFERENCEID}",referenceId);
return bodyforchecksum;
    }

public  void setRequest(String token, Constants.MerchantType mid, String referenceId) {
    body = body.replace("{TOKEN}", token).replace("{MID}", mid.getId()).replace("{REFERENCEID}", referenceId);

    }



    public CreateToken(Constants.MerchantType mid, String referenceId)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/api/v1/token/create");
        String bodychecksum=setMidReferenceID(mid,referenceId);
        String token=PGPUtil.getChecksum(mid.getKey(),bodychecksum);
        setRequest(token,mid,referenceId);
        getRequestSpecBuilder().setBody(getRequest());
    }


}
