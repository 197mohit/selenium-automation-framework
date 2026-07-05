package com.paytm.api.ImeiValidation;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ImeiValidation extends BaseApi{
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"WEB\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "         \"mid\": \"{mid}\",\n" +
            "        \"action\": \"{action}\",\n" +
            "        \"orderId\": \"{orderid}\",\n" +
            "        \"skuCode\": \"{skucode}\",\n" +
            "        \"dealerCode\": \"{dealercode}\",\n" +
            "        \"stateCode\": \"{statecode}\",\n" +
            "        \"brandId\": \"{brandId}\",\n" +
            "        \"categoryId\": \"{categoryId}\",\n" +
            "        \"imei\": \"{Imei}\"\n" +
            "    }\n" +
            "}";

    public ImeiValidation setRequest(Constants.MerchantType mid, String OrderId, String action , String SkuCode , String DealerCode, String StateCode, String BrandId, String CategoryId, String Imei){
        request = request.replace("{mid}", mid.getId())
                .replace("{orderid}",OrderId)
                .replace("{action}",action)
                .replace("{skucode}",SkuCode)
                .replace("{dealercode}",DealerCode)
                .replace("{statecode}",StateCode)
                .replace("{brandId}",BrandId)
                .replace("{categoryId}",CategoryId)
                .replace("{Imei}",Imei);
        return this;
    }

    public String getRequest() {
        return request;
    }

    public ImeiValidation(Constants.MerchantType mid, String OrderId,String action, String Skucode, String Dealercode, String Statecode, String BrandId, String CategoryId, String Imei){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.IMEI);
        getRequestSpecBuilder().addQueryParam("mid", mid.getId());
        setRequest(mid,OrderId,action,Skucode,Dealercode,Statecode,BrandId,CategoryId,Imei);
        getRequestSpecBuilder().setBody(getRequest());
    }
    public void setRequest(String request) {
        this.request = request;}
}
