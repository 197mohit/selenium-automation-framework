package com.paytm;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.BillProxy.FETCH_BIN;
import static com.paytm.appconstants.Constants.BillProxy.FETCH_BIN_DETAIL;

public class FetchBin extends BaseApi{

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ANDROID\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"binNumber\": \"1111\"\n" +
            "    }\n" +
            "}";

    String NativeOTPRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ANDROID\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\",\n" +
            "        \"tokenType\": \"SSO\",\n"+
            "        \"token\": \"{ssoToken}\"\n"+
            "    },\n" +
            "    \"body\": {\n" +
            "        \"bin\": \"{bin}\",\n" +
            "        \"nativeOTPDetailRequired\": \"{nativeOTPDetailRequired}\",\n" +
            "        \"mid\": \"{mid}\",\n" +
            "        \"OrderId\": \"{OrderId}\",\n" +
            "    }\n" +
            "}";

    public  FetchBin(String ssoToken){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(FETCH_BIN);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("ssoToken", ssoToken);
    }

    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }

    public FetchBin(String ssoToken , String mid, String OrderId, String bin, String nativeOTPDetailRequired){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(FETCH_BIN_DETAIL);
        getRequestSpecBuilder().addHeader("ssoToken", ssoToken);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId",OrderId);
        NativeOTPRequest = NativeOTPRequest.replace("{bin}",bin).replace("{mid}",mid).replace("{nativeOTPDetailRequired}",nativeOTPDetailRequired).replace("{OrderId}",OrderId).replace("{ssoToken}",ssoToken);
        getRequestSpecBuilder().setBody(getNativeOTPRequest());
    }

    public String getNativeOTPRequest(){
        return NativeOTPRequest;
    }
    public void setNativeOTPRequest(String NativeOTPRequest){
        this.NativeOTPRequest = NativeOTPRequest;
    }
}
