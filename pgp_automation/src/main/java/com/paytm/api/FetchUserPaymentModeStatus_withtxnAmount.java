package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.List;

public class FetchUserPaymentModeStatus_withtxnAmount extends BaseApi{
    String body="{\n" +
            "    \"head\": {\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"{TOKEN}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"mobileNo\": \"{MobNo}\",\n" +
            "        \"txnAmount\": \"{Txnamount}\",\n" +
            "        \"paymentMode\": [\n" +
            "            \"PAYTM_DIGITAL_CREDIT\"\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    String bodyforchecksum="{" + "\"mid\":\"{MID}\"," + "\"mobileNo\":\"{MobNo}\"," + "\"txnAmount\":\"{Txnamount}\"," + "\"paymentMode\":[\"PAYTM_DIGITAL_CREDIT\"]" + "}";

    public String getRequest()
    {
        return body;
    }


    public  String setBody(Constants.MerchantType mid, String mobNo,String Txnamount ){
        bodyforchecksum=bodyforchecksum.replace("{MID}", mid.getId()).replace("{MobNo}", mobNo).replace("{Txnamount}", Txnamount);
        return bodyforchecksum;
    }

    public  void setRequest(String token, Constants.MerchantType mid, String mobNo,String Txnamount) {
        body = body.replace("{TOKEN}", token).replace("{MID}", mid.getId()).replace("{MobNo}", mobNo).replace("{Txnamount}", Txnamount);

    }
    public FetchUserPaymentModeStatus_withtxnAmount(Constants.MerchantType mid, String mobNo,String Txnamount) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_USER_PAYMENT_MODES);
        getRequestSpecBuilder().addQueryParam("mid",mid.getId());
        String bodychecksum=setBody(mid,mobNo,Txnamount);
        String token= PGPUtil.getChecksum(mid.getKey(),bodychecksum);
        setRequest(token,mid,mobNo,Txnamount);
        getRequestSpecBuilder().setBody(getRequest());
    }

}