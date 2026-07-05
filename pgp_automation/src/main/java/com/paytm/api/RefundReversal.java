package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.time.Instant;

import static com.paytm.appconstants.Constants.Refund.REFUND_REVERSAL;

public class RefundReversal extends BaseApi {

        String request = "{\n" +
                "    \"head\": {\n" +
                "        \"version\": \"V1\",\n" +
                "        \"requestTimestamp\": \"{requestTimestamp}\",\n" +
                "        \"clientId\": \"C11\",\n" +
                "        \"channelId\": \"WEB\",\n" +
                "        \"signature\": \"{signature}\"\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "        \"mid\": \"{mid}\",\n" +
                "        \"orderId\": \"{oid}\"\n" +
                "    }\n" +
                "}";
        String body = "{\"mid\":\"{mid}\",\"orderId\":\"{oid}\"}";

        public RefundReversal(String mid, String oid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(REFUND_REVERSAL);
        setRequest(mid, oid);
        getRequestSpecBuilder().setBody(getRequest());
    }

        public String getRequest() {
                return request;
        }

        public void setRequest(String mid, String oid) {

                body = body.replace("{mid}", mid).replace("{oid}", oid);
                String requestTimestamp = String.valueOf(Instant.now().toEpochMilli());
                String token = createChecksum(Constants.MerchantType.getByMid(mid).getKey(), body);
                request = request.replace("{mid}", mid)
                        .replace("{oid}", oid)
                        .replace("{signature}", token)
                        .replace("{requestTimestamp}", requestTimestamp);
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
