package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class OrderStatus extends BaseApi {
    Response response;

    public Response getOrderStatus(final String mid, final String orderId) {
        String request = "{\n" +
                "    \"head\": {\n" +
                "        \"version\": \"V1\",\n" +
                "        \"requestTimestamp\": 1557395034318,\n" +
                "        \"requestId\": \""+ CommonHelpers.generateOrderId()+"\",\n" +
                "        \"clientId\": \"upi-app\",\n" +
                "        \"token\": \"{jwtToken}\",\n" +
                "        \"tokenType\": \"JWT\"\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "        \"mid\": \""+mid+"\",\n" +
                "        \"orderId\": \""+orderId+"\"\n" +
                "    }\n" +
                "}";

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","upi-app");
        tokenMap.put("BODY", "{\"mid\":\""+mid+"\",\"orderId\":\""+orderId+"\"}");

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,LocalConfig.JWT_KEY);
        request = request.replace("{jwtToken}",jwtToken);
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.ORDER_STATUS);

        getRequestSpecBuilder().setBody(request);
        int timeOutInSeconds = 60;
        long startTime = System.currentTimeMillis();

        while(System.currentTimeMillis() - startTime < (long)(timeOutInSeconds * 1000)) {
            response = this.execute();
            if (response.statusCode() != 200) {
                throw new PGPException("Exception in Txn Status Order Status Merchant API");
            }

            if (response.jsonPath().getString("body.txnResponseCode") != null) {
                if (response.jsonPath().getString("body.txnResponseCode").equals("01")) {
                    return response;
                }

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException var11) {
                    var11.printStackTrace();
                }
            }
        }

        return response;
    }
}

