package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;



public class FetchUPIOptions extends BaseApi {
String mid;
    String token;
    String body = "{\n" +
            "    \"head\": {\n" +
            "        \"refId\": \"\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"{TOKEN}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\":\"{MID}\",\n" +
            "    }\n" +
            "}";

    String bodyforchecksum="{" + "\"mid\":\"{MID}\"" + "}";
    public String getRequest()
    {
        return body;
    }


    public  String setMid(Constants.MerchantType mid) {
        bodyforchecksum = bodyforchecksum.replace("{MID}", mid.getId());
        return bodyforchecksum;
    }

    public  void setRequest(String token, Constants.MerchantType mid) {
        body = body.replace("{TOKEN}", token).replace("{MID}", mid.getId());

    }

    public FetchUPIOptions(Constants.MerchantType mid)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FETCH_UPI_OPTIONS);
        String bodychecksum=setMid(mid);
        String token= PGPUtil.getChecksum(mid.getKey(),bodychecksum);
        setRequest(token,mid);
        getRequestSpecBuilder().setBody(getRequest());
    }
    public FetchUPIOptions(Constants.MerchantType mid,String token)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FETCH_UPI_OPTIONS);
        setRequest(token,mid);
        getRequestSpecBuilder().setBody(getRequest());
    }



}
