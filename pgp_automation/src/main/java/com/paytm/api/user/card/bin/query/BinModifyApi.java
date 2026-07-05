package com.paytm.api.user.card.bin.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

/**
 * Implemented in incorrect manner sourav.singh working on it.
 */

@Deprecated
public class BinModifyApi extends BaseApi {

    public BinModifyApi(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian, Boolean PREPAID_CARD, String CORPORATE_CARD) throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("bin", bin.substring(0,6));
        obj.put("blocked", "false");
        obj.put("cardScheme", cardScheme);
        obj.put("cardType", cardType);
        obj.put("countryCode", countryCode);
        obj.put("institutionId", institutionId);

        Map<String, Object> binConfigAttributes = new HashMap<String, Object>();
        binConfigAttributes.put("INDIAN", Indian);
        binConfigAttributes.put("ZERO_SUCCESS_RATE", "false");
        binConfigAttributes.put("PREPAID_CARD", PREPAID_CARD);
        binConfigAttributes.put("CORPORATE_CARD", CORPORATE_CARD);

        obj.put("binConfigAttributes", binConfigAttributes);
        obj.put("source", "ADMIN");

        String request = new ObjectMapper().writeValueAsString(obj);

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_MODIFY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }

    public BinModifyApi(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian, String CORPORATE_CARD) throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("bin", bin.substring(0,6));
        obj.put("blocked", "false");
        obj.put("cardScheme", cardScheme);
        obj.put("cardType", cardType);
        obj.put("countryCode", countryCode);
        obj.put("institutionId", institutionId);

        Map<String, Object> binConfigAttributes = new HashMap<String, Object>();
        binConfigAttributes.put("INDIAN", Indian);
        binConfigAttributes.put("ZERO_SUCCESS_RATE", "false");
        binConfigAttributes.put("CORPORATE_CARD", CORPORATE_CARD);

        obj.put("binConfigAttributes", binConfigAttributes);
        obj.put("source", "ADMIN");

        String request = new ObjectMapper().writeValueAsString(obj);

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_MODIFY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }


    public BinModifyApi(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian, Boolean PREPAID_CARD) throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("bin", bin.substring(0,6));
        obj.put("blocked", "false");
        obj.put("cardScheme", cardScheme);
        obj.put("cardType", cardType);
        obj.put("countryCode", countryCode);
        obj.put("institutionId", institutionId);

        Map<String, Object> binConfigAttributes = new HashMap<String, Object>();
        binConfigAttributes.put("INDIAN", Indian);
        binConfigAttributes.put("ZERO_SUCCESS_RATE", "false");
        binConfigAttributes.put("PREPAID_CARD", PREPAID_CARD);

        obj.put("binConfigAttributes", binConfigAttributes);
        obj.put("source", "ADMIN");

        String request = new ObjectMapper().writeValueAsString(obj);

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_MODIFY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }

    public BinModifyApi(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian) throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("bin", bin.substring(0,6));
        obj.put("blocked", "false");
        obj.put("cardScheme", cardScheme);
        obj.put("cardType", cardType);
        obj.put("countryCode", countryCode);
        obj.put("institutionId", institutionId);

        Map<String, Object> binConfigAttributes = new HashMap<String, Object>();
        binConfigAttributes.put("INDIAN", Indian);
        binConfigAttributes.put("ZERO_SUCCESS_RATE", "false");

        obj.put("binConfigAttributes", binConfigAttributes);
        obj.put("source", "ADMIN");

        String request = new ObjectMapper().writeValueAsString(obj);

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_MODIFY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }

    public BinModifyApi(String bin, String blockedStatus)
    {
        String request = "{\n" +
                "    \"bin\": \""+bin+"\",\n" +
                "    \"source\": \"ADMIN\",\n" +
                "           \"cardType\": \"CC\",\n" +
                "    \"blocked\": \""+blockedStatus+"\",\n" +
                "}";
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_MODIFY);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }

}
