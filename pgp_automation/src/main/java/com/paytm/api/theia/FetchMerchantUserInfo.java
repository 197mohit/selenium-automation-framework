package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.time.Instant;

import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_MERCHANT_USER_INFO;

/**
 * AI-Generated: 2025-01-04 - FetchMerchantUserInfo API class
 * This class provides methods to call the fetchMerchantUserInfo API endpoint
 * Following the codebase pattern for API classes
 */
public class FetchMerchantUserInfo extends BaseApi {

    // Default configuration constants
    private static final String DEFAULT_CHANNEL_ID = "WEB";
    private static final String DEFAULT_TOKEN_TYPE = "SSO";
    private static final String DEFAULT_VERSION = "v1";
    
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"{version}\",\n" +
            "        \"requestTimestamp\": \"{requestTimestamp}\",\n" +
            "        \"channelId\": \"{channelId}\",\n" +
            "        \"tokenType\": \"{tokenType}\",\n" +
            "        \"token\": \"{token}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{mid}\",\n" +
            "        \"orderId\": \"{orderId}\"\n" +
            "    }\n" +
            "}";

    /**
     * Constructor for FetchMerchantUserInfo API
     * 
     * @param mid Merchant ID
     * @param orderId Order ID
     * @param ssoToken SSO token for authentication
     */
    public FetchMerchantUserInfo(String mid, String orderId, String ssoToken) {
        this(mid, orderId, ssoToken, null, DEFAULT_CHANNEL_ID, DEFAULT_TOKEN_TYPE, DEFAULT_VERSION);
    }

    /**
     * Constructor for FetchMerchantUserInfo API with custom parameters
     * 
     * @param mid Merchant ID
     * @param orderId Order ID
     * @param ssoToken SSO token for authentication
     * @param requestTimestamp Request timestamp (optional, will use current time if null)
     * @param channelId Custom channel ID (optional, will use default if null)
     * @param tokenType Custom token type (optional, will use default if null)
     * @param version Custom version (optional, will use default if null)
     */
    public FetchMerchantUserInfo(String mid, String orderId, String ssoToken, 
                                String requestTimestamp, String channelId, 
                                String tokenType, String version) {
        
        // Set default values if not provided
        if (requestTimestamp == null || requestTimestamp.isEmpty()) {
            requestTimestamp = Instant.now().toString();
        }
        
        String finalChannelId = (channelId != null && !channelId.isEmpty()) ? channelId : DEFAULT_CHANNEL_ID;
        String finalTokenType = (tokenType != null && !tokenType.isEmpty()) ? tokenType : DEFAULT_TOKEN_TYPE;
        String finalVersion = (version != null && !version.isEmpty()) ? version : DEFAULT_VERSION;
        
        // Replace placeholders in request template
        String finalRequest = request
                .replace("{version}", finalVersion)
                .replace("{requestTimestamp}", requestTimestamp)
                .replace("{channelId}", finalChannelId)
                .replace("{tokenType}", finalTokenType)
                .replace("{token}", ssoToken)
                .replace("{mid}", mid)
                .replace("{orderId}", orderId);
        
        // Configure the API request
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(LocalConfig.PGP_HOST)
                .setBasePath(FETCH_MERCHANT_USER_INFO)
                .setBody(finalRequest);
        
        setMethod(MethodType.POST);
        
        // Set context for potential use in tests
        setContext("body.mid", mid);
        setContext("body.orderId", orderId);
        setContext("head.token", ssoToken);
        setContext("head.version", finalVersion);
        setContext("head.channelId", finalChannelId);
        setContext("head.tokenType", finalTokenType);
    }
}
