package com.paytm.api.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.saveCard.SaveCardRequestGeneric;
import com.paytm.dto.saveCard.SavedcardOpenAPIServiceCardTypeSsoToken.SavedcardOpenAPIServiceCardTypeSsoTokenRequest;
import com.paytm.dto.saveCard.saveTrustedCardRequest;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.framework.api.CustomRequestSpecBuilder;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.paytm.appconstants.Constants.savedCard;

/**
 * Created by anjukumari on 21/08/18
 */
public class SavedCardApi extends BaseApi {


    public Response getAllSavedCardsByUserId(String userID) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.GET);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        getRequestSpecBuilder().setBasePath(savedCard.GET_SAVED_CARD_BY_USER_ID.replace("{userId}", userID));
        return baseApi.execute();
    }

    public void deactivateSavedCard(Long savedCardId, String userID) {

    }


    public static Response saveTrustedCard(saveTrustedCardRequest requestBody) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.POST);
        CustomRequestSpecBuilder specBuilder = baseApi.getRequestSpecBuilder();
        specBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        specBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        specBuilder.setBasePath(savedCard.SAVE_TRUSTED_CARD_DETAIL);
        specBuilder.setContentType(ContentType.JSON);
        specBuilder.setBody(requestBody);
        return baseApi.execute();
    }

    public static Response saveCardDetailCache(SaveCardRequestGeneric saveCardRequestGeneric) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.SAVE_CARD_DETAILS_IN_CACHE);
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setBody(saveCardRequestGeneric);
        return baseApi.execute();
    }

    public static Response saveCardDetailUserId(SaveCardRequestGeneric saveCardRequestGeneric) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.SAVE_CARD_BY_USER_ID);
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setBody(saveCardRequestGeneric);
        return baseApi.execute();
    }

    public static Response getCardDetailForSavedcardOpenAPIServiceBycardTypeSsoToken(SavedcardOpenAPIServiceCardTypeSsoTokenRequest savedcardOpenAPIServiceCardTypeSsoTokenRequest) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST);
        requestSpecBuilder.setBasePath(savedCard.SAVEDCARD_OPEN_API_SERVICE_BY_CARDTYPE_SSOTOKEN);
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setBody(savedcardOpenAPIServiceCardTypeSsoTokenRequest);
        return baseApi.execute();
    }

    public static Response saveCardDetailCustIdMid(SaveCardRequestGeneric saveCardRequestGeneric) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.SAVE_CARD_BY_MID_CUSTID);
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setBody(saveCardRequestGeneric);
        return baseApi.execute();
    }


    public static Response saveCardDetail_ToDB_FromCache(SaveCardRequestGeneric saveCardRequestGeneric) {
        BaseApi baseApi = new BaseApiV2();
        CustomRequestSpecBuilder specBuilder = baseApi.getRequestSpecBuilder();
        baseApi.setMethod(MethodType.POST);
        specBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        specBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        specBuilder.setBasePath(savedCard.SAVE_TRANSACTION_CARD_DETAIL);
        specBuilder.setContentType(ContentType.JSON);
        specBuilder.setBody(saveCardRequestGeneric);
        return baseApi.execute();
    }

    public static Response getSaveCardByUserId(String userId) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.GET_SAVED_CARD_BY_USER_ID.replace("{userId}", userId));
        return baseApi.execute();
    }

    public static Response getSaveCardByCardId(String userId, String cardId) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.GET_SAVED_CARD_BY_CARD_ID.replace("{userId}", userId).replace("{cardId}", cardId));
        return baseApi.execute();
    }


    public static Response getSaveCardBy_User_Status(String userId, String status) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.GET_SAVED_CARD_BY_USER_ID_AND_STATUS.replace("{userId}", userId).replace("{status}", status));
        return baseApi.execute();
    }

    public static Response getSaveCardBy_mId_custId_userId(String userId, String custId, String mid) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.GET_MID_CUSTID_USERID_CARD_DETAIL.replace("{userId}", userId).replace("{custId}", custId).replace("{mId}", mid));
        return baseApi.execute();
    }


    public static Response getSaveCardBy_mId_custId_userId_cardId(String userId, String custId, String mid, String cardId) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.GET_SAVEDCARD_BY_MID_CUSTID_USERID_AND_CARDID.replace("{userId}", userId).replace("{custId}", custId).replace("{mId}", mid).replace("{cardId}", cardId));
        return baseApi.execute();
    }

    public static Response deleteSave_cardId_mId_custId_userId(String userId, String cardId, String custId, String mid) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.DELETE);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.DELETE_SAVEDCARD_BY_CARDID_USERID_MID_CUSTID.replace("{userId}", userId).replace("{custId}", custId).replace("{mId}", mid).replace("{cardId}", cardId));
        return baseApi.execute();
    }

    public static Response deleteSaveCard_userId_cardId(String userId, String cardId) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.DELETE);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.DELETE_SAVED_CARD_BY_USERID_AND_CARD_ID.replace("{userId}", userId).replace("{cardId}", cardId));
        return baseApi.execute();
    }

    public static Response deleteSaveCard_fromCache(String txnId) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.DELETE);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_SERVICE_BASE_URL_V1);
        requestSpecBuilder.setBasePath(savedCard.DELETE_CACHE_CARD_DETAIL.replace("{transactionId}", txnId));
        return baseApi.execute();
    }


    //saved card open APIs

    public static Response getSavedCard(Object request) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setBody(request);
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST);
        requestSpecBuilder.setBasePath(Constants.savedCard.MERCHANT_V1_GET_SAVEDCARD);
        return baseApi.execute();
    }

    public static Response getMerchantCardAPI(Constants.MerchantType mid, User user)
    {
        TreeMap<String,String> obj = new TreeMap<>();
        obj.put("MID",mid.getId());
        obj.put("REQUEST_TYPE","DEFAULT");
        obj.put("CUSTID", CommonHelpers.generateOrderId());
        obj.put("SSO_TOKEN", user.ssoToken());

        obj.put("CHECKSUM", PGPUtil.getChecksum(mid.getKey(),obj));

        return getSavedCard(obj);

    }

    public static Response getMerchantCardAPI(Constants.MerchantType mid,String custId)
    {
        TreeMap<String,String> obj = new TreeMap<>();
        obj.put("MID",mid.getId());
        obj.put("REQUEST_TYPE","DEFAULT");
        obj.put("CUSTID", custId);
        obj.put("CHECKSUM", PGPUtil.getChecksum(mid.getKey(),obj));

        return getSavedCard(obj);

    }


    public static Response getSavedCardFromOldPg(String ssoToken) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.addQueryParam("JsonData", "{\"SSOToken\":\"" + ssoToken + "\"}");
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST);
        requestSpecBuilder.setBasePath(savedCard.HANDLER_INTERNAL_BIN_INFO);
        return baseApi.execute();
    }

    public static Response getSavedCard_bySsotoken(String ssoToken) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_OPEN_API_SERVICE);
        requestSpecBuilder.setBasePath(savedCard.GET_SAVEDCARD_ON_SSOTOKEN.replace("{ssoToken}", ssoToken));
        return baseApi.execute();
    }

    public BaseApi getSavedCardTokenTypeJWT(String userId) {

        this.setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = this.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + savedCard.SAVEDCARD_OPEN_API_SERVICE);
        requestSpecBuilder.setBasePath(savedCard.GETSAVEDCARDBYTOKENTYPE);
        requestSpecBuilder.setContentType(ContentType.JSON);
        Map<String,String> jwtClaims = new HashMap<>();
        jwtClaims.put("tokenType","JWT");
        jwtClaims.put("userId",userId);
        String JWTToken = PGPHelpers.createJsonWebToken(jwtClaims,PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        requestSpecBuilder.setBody("{\n" +
                "  \"head\": {\n" +
                "    \"clientId\": \"C11\",\n" +
                "    \"version\": \"v1\",\n" +
                "    \"requestTimestamp\": \"Time\",\n" +
                "    \"channelId\": \"WEB\",\n" +
                "    \"tokenType\": \"JWT\",\n" +
                "    \"token\": \""+JWTToken+"\"\n" +
                "  },\n" +
                "  \"body\": {\n" +
                "    \"userId\": \""+userId+"\",\n" +
                "    \"isCardIndexNumberRequired\": true\n" +
                "  }\n" +
                "}");

        return this;
    }


    public static Response getSaveCard_byMid_custId(String mid, String merchantKey, String custId) throws UnsupportedEncodingException {
        BaseApi baseApi = new BaseApiV2();
        TreeMap<String, String> checksumMap = new TreeMap<>();
        checksumMap.put("CUSTID", custId);
        checksumMap.put("MID", mid);
        String checkSum = PGPUtil.getChecksum(merchantKey, checksumMap);
        baseApi.setMethod(BaseApi.MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + Constants.savedCard.SAVEDCARD_OPEN_API_SERVICE);
        requestSpecBuilder.setBasePath(Constants.savedCard.GET_SAVEDCARD_ON_CUSTID_MID.replace("{custId}", custId).replace("{mId}", mid).replace("{checkSum}", checkSum));
        return baseApi.execute();
    }


    public static Response getSaveCard_byMid_custId_token(String mid, String merchantKey, String custId, String token) {
        BaseApi baseApi = new BaseApiV2();
        TreeMap<String, String> checksumMap = new TreeMap<>();
        checksumMap.put("mId", mid);
        checksumMap.put("custId", custId);
        checksumMap.put("ssoToken", token);
        String checkSum = PGPUtil.getChecksum(merchantKey, checksumMap);
        baseApi.setMethod(BaseApi.MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + Constants.savedCard.SAVEDCARD_OPEN_API_SERVICE);
        requestSpecBuilder.setBasePath(savedCard.GET_SAVEDCARD_ON_MID_CUSTID_SSOTOKEN.replace("{custId}", custId).replace("{mId}", mid).replace("{checkSum}", checkSum).replace("{ssoToken}", token));
        return baseApi.execute();
    }


    public static Response deleteSaveCard_On_custId_mid_cardId(String mid, String merchantKey, String custId, String cardId) {
        BaseApi baseApi = new BaseApiV2();
        TreeMap<String, String> checksumMap = new TreeMap<>();
        checksumMap.put("mId", mid);
        checksumMap.put("custId", custId);
        checksumMap.put("cardId", cardId);
        String checkSum = PGPUtil.getChecksum(merchantKey, checksumMap);
        baseApi.setMethod(BaseApi.MethodType.DELETE);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + Constants.savedCard.SAVEDCARD_OPEN_API_SERVICE);
        requestSpecBuilder.setBasePath(savedCard.DELETE_SAVEDCARD_ON_CUSTID_MID_CARDID.replace("{custId}", custId).replace("{mId}", mid).replace("{checkSum}", checkSum).replace("{cardId}", cardId));
        return baseApi.execute();
    }

    public static Response deleteSaveCard_On_ssoToken_cardId(String cardId, String ssotoken) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.DELETE);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST + Constants.savedCard.SAVEDCARD_OPEN_API_SERVICE);
        requestSpecBuilder.setBasePath(savedCard.DELETE_SAVEDCARD_ON_SSOTOKEN_CARDID.replace("{cardId}", cardId).replace("{ssoToken}", ssotoken));
        return baseApi.execute();
    }

}
