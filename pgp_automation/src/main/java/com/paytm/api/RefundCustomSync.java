package com.paytm.api;


import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static com.paytm.appconstants.Constants.Refund.CUSTOM_REFUND;


public class RefundCustomSync extends BaseApi {

    String requestJWT = "{\n" +
            "    \"head\": {\n" +
            "         \"version\": \"v1\",\n" +
            "         \"requestTimestamp\": \"{requestTimestamp}\"," +
            "        \"clientId\":\"{client}\",\"tokenType\":\"JWT\",\"token\":\"{JWTToken}\"\n" +
            "    },\n" +
            "    \"body\":{\"mid\":\"{MID}\",\"txnType\":\"REFUND\",\"orderId\":\"{OrderId}\",\"txnId\":\"{TXNId}\",\"refId\":\"{RefId}\",\"refundAmount\":\"{RefundAmount}\"}\n" +
            "}";

    public String getRequestJWT() {
        return requestJWT;
    }

    public void setRequestJWT(String mid ,String orderId,String txnId,String refundAmount, String jwtKey,String client) {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","payments");
        tokenMap.put("mid", mid);
        String refId = "Ref" + orderId;
        tokenMap.put("refId",refId);
        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,jwtKey);
        String requestTimestamp = String.valueOf(Instant.now().toEpochMilli());
        requestJWT = requestJWT.replace("{MID}", mid)
                .replace("{OrderId}", orderId)
                .replace("{JWTToken}", jwtToken)
                .replace("{requestTimestamp}", requestTimestamp)
                .replace("{TXNId}", txnId)
                .replace("{RefId}", refId)
                .replace("{RefundAmount}", refundAmount)
                .replace("{client}",client);
    }

    public RefundCustomSync(String mid ,String orderId,String txnId,String refundAmount, String jwtKey,String client) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CUSTOM_REFUND);
        setRequestJWT(mid, orderId,txnId,refundAmount,jwtKey,client);
        getRequestSpecBuilder().setBody(getRequestJWT());
    }
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"\",\n" +
            "         \"version\": \"v1\",\n" +
            "         \"requestTimestamp\": \"Time\",\n" +
            "        \"signature\":\"{CHECKSUM}\"\n" +
            "    },\n" +
            "    \"body\":{\"mid\":\"{MID}\",\"txnType\":\"REFUND\",\"orderId\":\"{OrderId}\",\"txnId\":\"{TxnId}\",\"refId\":\"{RefId}\",\"refundAmount\":\"{refundAmount}\"}\n" +
            "    \n" +
            "}";
    String body = "{\"mid\":\"{MID}\",\"txnType\":\"REFUND\",\"orderId\":\"{OrderId}\",\"txnId\":\"{TxnId}\",\"refId\":\"{RefId}\",\"refundAmount\":\"{refundAmount}\"}";

    public String getRequest() {
        return request;
    }

    public void setRequest(String mid ,String orderId,String txnId,String refundAmount,String merchantKey) {
        String refId = "Ref" + orderId;
        body = body.replace("{MID}", mid)
                .replace("{OrderId}", orderId)
                .replace("{TxnId}", txnId)
                .replace("{RefId}", refId)
                .replace("{refundAmount}", refundAmount);
        String requestTimestamp = String.valueOf(Instant.now().toEpochMilli());
        String token = PGPUtil.getChecksum(merchantKey, body);
        request = request.replace("{MID}", mid)
                .replace("{OrderId}", orderId)
                .replace("{CHECKSUM}", token)
                .replace("{requestTimestamp}", requestTimestamp)
                .replace("{TxnId}", txnId)
                .replace("{RefId}", refId)
                .replace("{refundAmount}", refundAmount);
    }

    public RefundCustomSync(String mid ,String orderId,String txnId,String refundAmount, String merchantKey) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CUSTOM_REFUND);
        setRequest(mid, orderId,txnId,refundAmount,merchantKey);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
