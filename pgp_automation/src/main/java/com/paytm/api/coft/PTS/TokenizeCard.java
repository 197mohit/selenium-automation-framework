package com.paytm.api.coft.PTS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CoftHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.coft.CardData;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import org.apache.commons.lang.RandomStringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 30/12/21 11:52 AM
 * @Version 1.0
 */
public class TokenizeCard extends BaseApi {

    String requestId= RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
    String request = "{\"head\":{\"version\":\"v1\",\"requestTimestamp\":\"1544614590000\",\"requestId\":\"fetchBinVISA\",\"signature\":\"d7186f022c5a44cc8915bf74e4e8c9f61534487392716\",\"signatureType\":\"CHECKSUM\",\"clientId\":\"PAYTM_THEIA\"},\"body\":{\"authRefId\":\"501123338\",\"cardSource\":\"CARD_ON_FILE\",\"encryptedCardData\":\"EKMa2lhPuGeYSSBU92vzv1/gNIL27oin5cWRYyzopThHF6CLDoYzYzLbprZfCb2mq568yUFJaTNfR+EUS//FDocFCQSJON/7EB7wqnG6lxYcrgMOKzKYKh0HR9fLCvuPA7VEsBPMlUq88315owdzcbZKckyDKUR0YDXaQb4/HKT2/GIfusaB6ZV1TgG2YZNWMmYvpcS8xfYYCC7TkzBmHLBKPTe4sF6igQytEhFCAJkBbXBK/gIh3zJDZOl15fuA3rmoWSF82i4MoSNuOzYmxrb9Zhn2c5/GuvCVwp/OdibP3d69hEmb54+ly9VIBvv9psW8ixEUgYbWykX41xyhOw==\",\"userInfo\":{\"custId\":\"bulkTokenTest\",\"userId\":\"\",\"firstName\":\"\",\"lastName\":\"\",\"mobileNumber\":\"\"},\"tokenizationConsent\":{\"userConsent\":1,\"createdAtuser\":\"Jul22,202102:46:54PM\",\"createdAt\":\"test\",\"userConsentId\":\"1234\"}}}";
    Gson gson = new Gson();

    public TokenizeCard(String mid, String cardScheme) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.TOKENIZE_CARD);
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("cardScheme", cardScheme);
        getRequestSpecBuilder().addQueryParam("requestId", requestId);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public TokenizeCard(String mid, String cardScheme, String requestId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.TOKENIZE_CARD);
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("cardScheme", cardScheme);
        getRequestSpecBuilder().addQueryParam("requestId", requestId);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public String getRequest() {
        return request;
    }


    public TokenizeCard buildRequest(String encryptedCardData, String custId, String userConsent, String authRefId, String cardSource, String userId, boolean isSaveOnUser, boolean isSaveOnMidCustId) {
        setContext("body.encryptedCardData", encryptedCardData);
        setContext("body.userInfo.custId", custId);
        setContext("body.tokenizationConsent.userConsent", userConsent);
        setContext("body.authRefId", authRefId);
        setContext("body.cardSource", cardSource);
        setContext("body.userInfo.userId", userId);
        setContext("body.isSaveAssetForUser", isSaveOnUser);
        setContext("body.isSaveAssetForMerchant", isSaveOnMidCustId);
        return this;
    }

    public TokenizeCard buildRequest(String encryptedCardData, String custId, String userConsent, String authRefId, String cardSource, String userId) {
        setContext("body.encryptedCardData", encryptedCardData);
        setContext("body.userInfo.custId", custId);
        setContext("body.tokenizationConsent.userConsent", userConsent);
        setContext("body.authRefId", authRefId);
        setContext("body.cardSource", cardSource);
        setContext("body.userInfo.userId", userId);
        return this;
    }

    public TokenizeCard buildRequest(String encryptedCardData, String custId, String userConsent, String authRefId,String cardSource, String userId,boolean isSaveOnUser,boolean isSaveOnMidCustId, String signature,String signatureType,String requestId) {
        setContext("body.encryptedCardData", encryptedCardData);
        setContext("body.userInfo.custId", custId);
        setContext("body.tokenizationConsent.userConsent", userConsent);
        setContext("body.authRefId", authRefId);
        setContext("body.cardSource", cardSource);
        setContext("body.userInfo.userId", userId);
        setContext("body.isSaveAssetForUser",isSaveOnUser);
        setContext("body.isSaveAssetForMerchant",isSaveOnMidCustId);
        setContext("head.signature",signature);
        setContext("head.signatureType",signatureType);
        setContext("head.requestId",requestId);
        return this;
    }

    public TokenizeCard generateChecksum(String key) {
        Gson gson = new Gson();
        String requestBody = gson.toJson(getRequestBody());
        JsonObject tokenizeCardRequest = gson.fromJson(requestBody, JsonObject.class);
        String bodyForChecksum = tokenizeCardRequest.get("body").toString();
        String checksum = PGPUtil.getChecksum(key, bodyForChecksum);
        setContext("head.signature",checksum);
        setContext("head.signatureType","CHECKSUM");
        return this;
    }

    public TokenizeCard encryptCardData(String cardNumber, String expiryMonth, String expiryYear, String cvv)
            throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        CardData cardDataObject = new CardData();
        cardDataObject.setCardData(cardNumber, expiryMonth, expiryYear, cvv);
        String encryptedCardData = CoftHelper.encryptCardData(cardDataObject);
        setContext("body.encryptedCardData", encryptedCardData);
        return this;
    }
}
