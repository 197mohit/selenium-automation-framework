package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

public class FetchMerchantConfig extends BaseApi {

        String request = "{\n" +
                "    \"head\": {\n" +
                "        \"tokenType\": \"JWT\",\n" +
                "        \"token\": \"{jwtToken}\",\n" +
                "        \"clientId\": \"aoa-client\"\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "        \"mid\": \"{mid}\",\n" +
                "    }\n" +
                "}";

        String txnTokenRequest = "{\n" +
                "    \"head\": {\n" +
                "        \"version\": \"v1\",\n" +
                "        \"requestTimestamp\": \"\",\n" +
                "        \"channelId\": \"WEB\",\n" +
                "        \"token\": \"\",\n" +
                "        \"tokenType\": \"TXN_TOKEN\",\n" +
                "        \"workFlow\": \"checkout\",\n" +
                "        \"type\": \"TXN_TOKEN\"\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "        \"mid\": \"\"\n" +
                "    }\n" +
                "}";
        

        public FetchMerchantConfig(String mid) {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","aoa-client");
        tokenMap.put("mid",mid);
        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"12345");
        request = request.replace("{jwtToken}",jwtToken).replace("{mid}",mid);
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FETCH_MERCHANT_CONFIG);
        getRequestSpecBuilder().setBody(request);
    }

    // AI-Generated: 2025-01-04 - Refactoring: Using setContext method following codebase pattern
    public FetchMerchantConfig(String mid, String txnToken) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(LocalConfig.PGP_HOST)
                .setBasePath(Constants.PGPAPIResourcePath.FETCH_MERCHANT_CONFIG + "?mid=" + mid)
                .setBody(txnTokenRequest);
        
        // Set context values using the proper codebase pattern
        setContext("head.requestTimestamp", String.valueOf(System.currentTimeMillis()));
        setContext("head.token", txnToken);
        setContext("body.mid", mid);
        
        // Add additional headers
        getRequestSpecBuilder()
                .addHeader("accept", "*/*")
                .addHeader("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
                .addHeader("priority", "u=1, i")
                .addHeader("referer", LocalConfig.PGP_HOST + "/checkoutjs/1585/assets/iframes/dummy-frame.html")
                .addHeader("sec-ch-ua", "\"Not;A=Brand\";v=\"99\", \"Google Chrome\";v=\"139\", \"Chromium\";v=\"139\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
    }
}

