package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

public class FetchCardIndexNumber extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"token\": \"{checksum}\",\n" +
            "        \"tokenType\": \"CHECKSUM\"\n" +
            "    },\n" +
            "    \"body\": {\"cardNumber\":\"{cardNumber}\",\"cardExpiry\":\"{expiryMonth}/{expiryYear}\"}\n" +
            "}";

    String body = "{\"cardNumber\":\"{cardNumber}\",\"cardExpiry\":\"{expiryMonth}/{expiryYear}\"}";

    private static String createChecksum(String merchantKey,String body) {
        String checksum = "";

        try {
            checksum = PGPUtil.getChecksum(merchantKey, body);
            return checksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public String getRequest()
    {
        return request;
    }

    public FetchCardIndexNumber setRequest(Constants.MerchantType mid, String cardNumber, String expiryMonth, String expiryYear)
    {
       body = body.replace("{cardNumber}",cardNumber)
               .replace("{expiryMonth}",expiryMonth)
               .replace("{expiryYear}",expiryYear);

       System.out.println("Body : " +body);
        String signature = createChecksum(mid.getKey(),body);

        request = request
                .replace("{checksum}",signature)
                .replace("{cardNumber}",cardNumber)
                .replace("{expiryMonth}",expiryMonth)
                .replace("{expiryYear}",expiryYear);
        System.out.println("Request : " +request);

        return this;
    }

    public FetchCardIndexNumber(Constants.MerchantType mid,String cardNumber,String expiryMonth,String expiryYear) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_CARD_INDEX_NO);
        getRequestSpecBuilder().addQueryParam("mid",mid.getId());
        setRequest(mid,cardNumber,expiryMonth,expiryYear);
        getRequestSpecBuilder().setBody(getRequest());

    }

}
