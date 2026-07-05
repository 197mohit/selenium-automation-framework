package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

public class FetchPaymentOptionsLite extends BaseApi {

    String mid;
    String token;

    String body = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"C11\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"signature\": \"CH\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"{TOKEN}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"requestType\": \"{REQUESTTYPE}\"\n" +
            "    }\n" +
            "}";

    String bodyforchecksum="{" + "\"mid\":\"{MID}\"," + "\"requestType\":\"{REQUESTTYPE}\"" + "}";


    public String getRequest()
    {
        return body;
    }


    public  String setMidRequestType(Constants.MerchantType mid, String requestType){
        bodyforchecksum=bodyforchecksum.replace("{MID}", mid.getId()).replace("{REQUESTTYPE}",requestType);
        return bodyforchecksum;
    }

    public  void setRequest(String token, Constants.MerchantType mid, String requestType) {
        body = body.replace("{TOKEN}", token).replace("{MID}", mid.getId()).replace("{REQUESTTYPE}", requestType);

    }

    public FetchPaymentOptionsLite(Constants.MerchantType mid, String requestType)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_LITE);
        String bodychecksum=setMidRequestType(mid,requestType);
        String token= PGPUtil.getChecksum(mid.getKey(),bodychecksum);
        setRequest(token,mid,requestType);
        getRequestSpecBuilder().setBody(getRequest());
    }

}
