package com.paytm;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.PGPAPIResourcePath.CREATE_TOKEN;

public class CreateToken extends BaseApi {


    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"{TimeStamp}\",\n" +
            "        \"token\": \"{SIGNATURE}\",\n" +
            "        \"tokenType\": \"CHECKSUM\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"referenceId\": \"{REFID}\",\n" +
            "        \"paytmSsoToken\": \"{SSOTOKEN}\"\n" +
            "    }\n" +
            "}";


    String body = "{" + "\"mid\":\"{MID}\"," + "\"referenceId\":\"{REFID}\"," + "\"paytmSsoToken\":\"{SSOTOKEN}\"" + "}";


    public CreateToken(Constants.MerchantType mid, String ssoToken, String refId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CREATE_TOKEN);
        setRequest(mid, ssoToken, refId);
        getRequestSpecBuilder().setBody(getRequest());

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

    public String getRequest() {
        return request;
    }

    public CreateToken setRequest(Constants.MerchantType mid, String sso, String refId) {

        body = body.replace("{MID}", mid.getId())
                .replace("{REFID}", refId)
                .replace("{SSOTOKEN}", sso);

        String Signature = createChecksum(mid.getKey(), body);

        request = request.replace("{MID}", mid.getId())
                .replace("{REFID}", refId)
                .replace("{TimeStamp}", (String.valueOf(System.currentTimeMillis())))
                .replace("{SSOTOKEN}", sso)
                .replace("{SIGNATURE}", Signature);
        return this;
    }

}
