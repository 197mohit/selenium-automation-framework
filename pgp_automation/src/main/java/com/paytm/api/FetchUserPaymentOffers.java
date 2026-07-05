package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;

/**
 * Classs created to execute API to fetch
 * user payment offers
 */
@Owner(Constants.Owner.ABHISHEK_TEWARI)
public class FetchUserPaymentOffers  extends BaseApi {

    String request = "{\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"mobileNo\": \"{MOBILE}\"\n" +
            "    },\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"CHECKSUM\",\n" +
            "        \"token\": \"{CHECKSUM}\"\n" +
            "    }\n" +
            "}";
    String body = "{\"mid\":\"{MID}\",\"mobileNo\":\"{MOBILE}\"}";

    public FetchUserPaymentOffers(Constants.MerchantType mid, String mobile){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_USER_PAYMENT_OFFERS);
        setRequest(mid, mobile);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addQueryParam("mid", mid.getId());
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(Constants.MerchantType mid, String mobile) {

        body = body.replace("{MID}", mid.getId())
                .replace("{MOBILE}", mobile);
        String token = createChecksum(mid.getKey(), body);
        request = request.replace("{MID}", mid.getId())
                .replace("{MOBILE}", mobile)
                .replace("{CHECKSUM}", token);
    }

    private static String createChecksum(String merchantKey, String body) {
        String checksum = "";

        try {
            checksum = PGPUtil.getChecksum(merchantKey, body);
            return checksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
