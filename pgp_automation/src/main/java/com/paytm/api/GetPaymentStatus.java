package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

public class GetPaymentStatus extends BaseApi {
    public String getPaymentStatusBody="{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"cart\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"signature\": \"\",\n" +
            "        \"token\":\"{token}\"\n" +
            "    },\n" +
            "    \"body\": {\"mid\": \"{mid}\",\"orderId\": \"{orderId}\"}\n" +
            "}";
    String checksumBody="{\"mid\": \"{MID}\",\"orderId\": \"{orderId}\"}";



    public GetPaymentStatus(GetPaymentStatusDTO body) {

        setMethod(BaseApi.MethodType.POST);
        setBodyOmitNullValueAttributes(body);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.PGPAPIResourcePath.GET_PAYMENT_STATUS);
    }
    public GetPaymentStatus(String body) {

        setMethod(BaseApi.MethodType.POST);
        setBodyOmitNullValueAttributes(body);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.PGPAPIResourcePath.GET_PAYMENT_STATUS);
    }
    public GetPaymentStatus(com.paytm.appconstants.Constants.MerchantType mid,String orderId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.PGPAPIResourcePath.GET_PAYMENT_STATUS);
        checksumBody=checksumBody.replace("{MID}",mid.getId().toString()).replace("{orderId}",orderId);
        String token= getChecksumToken(mid);
        getPaymentStatusBody=getPaymentStatusBody.replace("{mid}",mid.getId().toString()).replace("{orderId}",orderId).replace("{token}",token);
        getRequestSpecBuilder().setBody(getRequest());
    }
    public String getRequest(){
        return getPaymentStatusBody;
    }

    public void setChecksumBody(String mid,String orderId) {
        checksumBody=checksumBody.replace("{MID}",mid).replace("{orderId}",orderId);
    }
    public String getChecksumToken(com.paytm.appconstants.Constants.MerchantType mid){
        String token=PGPUtil.getChecksum(mid.getKey(),checksumBody);
        return token;
    }
    public void buildRequest(com.paytm.appconstants.Constants.MerchantType mid,String orderId){
        String token= getChecksumToken(mid);
        getPaymentStatusBody=getPaymentStatusBody.replace("{mid}",mid.getId().toString()).replace("{orderId}",orderId).replace("{token}",token);

    }

    public static String getParameterValue(String response, String key){
        String fieldsValues[]=response.split(",");
        String ans="";
        for(int i=0;i<fieldsValues.length;i++){
            if(fieldsValues[i].contains(key)){
                String ss[]=fieldsValues[i].split(":");
                ans=ss[1];
            }
        }
        int start=0;
        int end=ans.length()-1;
        while(!Character.isLetterOrDigit(ans.charAt(start)))
            start++;
        while(!Character.isLetterOrDigit(ans.charAt(end)))
            end--;
        String res=ans.substring(start,end);
        return res;

    }



}


