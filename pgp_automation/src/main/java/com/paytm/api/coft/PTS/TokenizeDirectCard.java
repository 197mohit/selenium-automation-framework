package com.paytm.api.coft.PTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CoftHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.dto.coft.CardData;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static com.paytm.apphelpers.CoftHelper.*;

/**
 * @Author mayankbharshiv
 * @Date 20/05/22 11:52 AM
 * @Version 1.0
 */
public class TokenizeDirectCard extends BaseApi {
    String requestId = UUID.randomUUID().toString();

    String cardData= "{\"expiryMonth\":\"10\",\"securityCode\":\"123\",\"expiryYear\":\"2022\",\"cardNumber\":\"4761360075860386\"}";

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"1544614590000\",\n" +
            "        \"requestId\": \"" + requestId + "\",\n" +
            "        \"signature\": \"P1Kn+wLulDpLhYIAr5gnj4+2txd1M2TNbbTjMbK69+7bmXEJHEK3yUVx0kOA734f0/XEtVyrmhC0yQDKMbm4Wf1QsjjvoXhweM/IOUtuw6E=\",\n" +
            "           \"signatureType\": \"CHECKSUM\",\n" +
            "        \"clientId\": \"PAYTM_THEIA\"\n" +
            "    },\n" +
            "    \"body\": {\"authRefId\":\"123456\",\"cardSource\":\"CARD_ON_FILE\",\"encryptedCardData\":\"AH7yQYAhW2BCkcJ9Z9ZtkXHS3dhIpxsc+GY3ejXRRY8x+Fnl4idFktX56/YfEuErzci1rsx4VhgKBj9Qv8fHCN9Sp7mmnZzewrgJeFtASjODjSqlfwpS315w1XhKlNtRd/F95Ej/wJHF3+3OV5VBo1fNzGzMiLUue1NGr8pXsfggvBaIIwdfAo8P0KTSMKpgjAa5Hmno99mQ+g/acfbE3F8baXDwM9ZXJTwefXVniGNOclELg7JckyyNHfHhogkCxRkkk4Cc1iMQ1MyO6DebHe9AWb1P50VOENRwfTtRQjpBk15SoZH8wF6xbRnFKVzGPSwS5FzxJE6QU+v6d0uiBw==\",\"userInfo\":{\"custId\":\"1002293943\",\"userId\":\"100229394,\",\"firstName\":\"Abhishek\",\"lastName\":\"Verma\",\"mobileNumber\":\"9899096972\"},\"tokenizationConsent\":{\"userConsent\":\"1\",\"createdAt\":\"Jul22,202102:46:54PM\",\"userConsentId\":\"1234\",\"language\":\"en\",\"platform\":\"APP\",\"os\":\"androidapp\",\"appVersion\":\"9.10.2\",\"ip\":\"157.35.79.240\"}}\n" +
            "}";

    public TokenizeDirectCard(String mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.TOKENIZE_DIRECT_CARD);
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("requestId", requestId);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public String getRequest() {
        return request;
    }


    public TokenizeDirectCard buildRequest(String encryptedCardData, String custId, String userConsent, String authRefId, String cardSource, String userId) {
        setContext("body.encryptedCardData", encryptedCardData);
        setContext("body.userInfo.custId", custId);
        setContext("body.tokenizationConsent.userConsent", userConsent);
        setContext("body.authRefId", authRefId);
        setContext("body.cardSource", cardSource);
        setContext("body.userInfo.userId", userId);
        return this;
    }

    public TokenizeDirectCard buildRequest(String cardNumber, String expiryMonth, String expiryYear, String cvv, String custId, String userConsent, String authRefId, String cardSource, String userId, boolean isSaveOnUser, boolean isSaveOnMidCustId) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        encryptCardData(cardNumber,expiryMonth,expiryYear,cvv);
        setContext("body.userInfo.custId", custId);
        setContext("body.tokenizationConsent.userConsent", userConsent);
        setContext("body.authRefId", authRefId);
        setContext("body.cardSource", cardSource);
        setContext("body.userInfo.userId", userId);
        setContext("body.isSaveAssetForUser", isSaveOnUser);
        setContext("body.isSaveAssetForMerchant", isSaveOnMidCustId);
        return this;
    }
    public TokenizeDirectCard buildRequest(String custId, String userConsent, String authRefId, String cardSource, String userId, boolean isSaveOnUser, boolean isSaveOnMidCustId)  {
        setContext("body.userInfo.custId", custId);
        setContext("body.tokenizationConsent.userConsent", userConsent);
        setContext("body.authRefId", authRefId);
        setContext("body.cardSource", cardSource);
        setContext("body.userInfo.userId", userId);
        setContext("body.isSaveAssetForUser", isSaveOnUser);
        setContext("body.isSaveAssetForMerchant", isSaveOnMidCustId);
        return this;
    }

    public TokenizeDirectCard buildRequest(String mid,String custId, String userConsent, String authRefId, String cardSource, String userId, boolean isSaveOnUser, boolean isSaveOnMidCustId)  {
        setContext("body.userInfo.custId", custId);
        setContext("body.tokenizationConsent.userConsent", userConsent);
        setContext("body.authRefId", authRefId);
        setContext("body.cardSource", cardSource);
        setContext("body.userInfo.userId", userId);
        setContext("body.isSaveAssetForUser", isSaveOnUser);
        setContext("body.isSaveAssetForMerchant", isSaveOnMidCustId);
        return this;
    }

    public TokenizeDirectCard generateChecksum(String key) {
        Gson gson = new Gson();
        String requestBody = gson.toJson(getRequestBody());
        JsonObject tokenizeCardRequest = gson.fromJson(requestBody, JsonObject.class);
        String bodyForChecksum = tokenizeCardRequest.get("body").toString();
        String checksum = PGPUtil.getChecksum(key, bodyForChecksum);
        setContext("head.signature", checksum);
        setContext("head.signatureType", "CHECKSUM");
        return this;
    }

    public TokenizeDirectCard generateJwt(String mid)
    {
        String requestTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        String jwtToken = SavedCardHelpersNew.jwtSignatureTokenizeMidCustId(requestId,mid,requestTimeStamp);
        setContext("head.requestId",requestId);
        setContext("head.signature", jwtToken);
        setContext("head.signatureType", "JWT");
        setContext("head.requestTimestamp",requestTimeStamp);
        return this;
    }
    public TokenizeDirectCard encryptCardData(String cardNumber, String expiryMonth, String expiryYear, String cvv)
            throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        CardData cardDataObject = new CardData();
        cardDataObject.setCardData(cardNumber, expiryMonth, expiryYear, cvv);
        String encryptedCardData = CoftHelper.encryptCardData(cardDataObject);
        setContext("body.encryptedCardData", encryptedCardData);
        return this;
    }

}
