package com.paytm.apphelpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.paytm.api.coft.PTS.FetchTokenDetails;
import com.paytm.api.coft.PTS.ModifyTokenStatus;
import com.paytm.api.coft.PTS.TokenizeCard;
import com.paytm.api.coft.saveCard.DeleteCardByMidCustId;
import com.paytm.api.coft.saveCard.HandlerInternalDeleteBinUser;
import com.paytm.api.coft.saveCard.HanlerInternalBinInfo;
import com.paytm.api.coft.saveCard.SavedCardByMidCustId;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.paytm.LocalConfig.JWT_CLIENT_SECRET_COFT_THEIA;

public class SavedCardHelpersNew {
    public static final String visaEncryptedCardData = PaymentDTO.VISA_ENCRYPTED_CARD_DATA;
    public static final String masterEncryptedCardData = PaymentDTO.MASTER_ENCRYPTED_CARD_DATA;
    static TokenizeCard tokenizeCard;
    public static final String authRefId = RandomStringUtils.randomAlphabetic(10);

    public static String jwtSignature(String requestId, String timeStamp) throws UnsupportedEncodingException {
        String token = JWT.create()
                .withClaim("version", "v1")
                .withClaim("requestId", requestId)
                .withClaim("requestTimestamp", timeStamp)
                .sign(Algorithm.HMAC256(JWT_CLIENT_SECRET_COFT_THEIA));

        return token;
    }

    public static String jwtSignatureTokenizeMidCustId(String requestId, String mid) throws UnsupportedEncodingException {
        String token = JWT.create()
                .withClaim("version", "v1")
                .withClaim("requestId", requestId)
                .withClaim("requestTimestamp", Instant.now().toEpochMilli())
                .withClaim("mid", mid)
                .sign(Algorithm.HMAC256(JWT_CLIENT_SECRET_COFT_THEIA));
        return token;
    }
    public static String jwtSignatureTokenizeMidCustId(String requestId, String mid, String requestTimeStamp) {
        String token = null;
        try {
            token = JWT.create()
                    .withClaim("version", "v1")
                    .withClaim("requestId", requestId)
                    .withClaim("requestTimestamp", requestTimeStamp)
                    .withClaim("mid", mid)
                    .sign(Algorithm.HMAC256(JWT_CLIENT_SECRET_COFT_THEIA));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    public static String createJsonWebTokenToSavedCard(String tokenType, String mid, String custId, String filterTokenCards,String jwtKey) {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType", tokenType);
        tokenMap.put("mid", mid);
        tokenMap.put("custId", custId);
        tokenMap.put("filterTokenCards", filterTokenCards);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, jwtKey);
        return jwt;
    }

    public static String createJsonWebTokenToDeleteCard(String tokenType, String mid, String custId, String savedCardId,String jwtKey) {
        Map<String, String> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("mid", mid);
        map.put("custId", custId);
        map.put("cardId", savedCardId);
        map.put("requestedBy", "CARDHOLDER");
        map.put("reason", "Customer wants to delete the token");
        map.put("reasonCode", "CUSTOMER_CONFIRMED");
        String jwtDelete = PGPHelpers.createJsonWebToken(map, PGPHelpers.ISSUER.ts, jwtKey);
        return jwtDelete;
    }

    public static JsonPath savedCardbyMidCustId(String jwt, String mid, String custId) {
        SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(mid, custId, false, "JWT", jwt);
        JsonPath savedCardResponse = savedCards.execute().jsonPath();
        return savedCardResponse;
    }

    public static void deleteAllCardsMidCustId(JsonPath savedCardResponse, String jwtDelete, String mid, String custId) {
        List<Object> list = savedCardResponse.getList("response");
        int size = list.size();
        System.out.println("size is" + size);
        //deleting cards if exist
        while (size != 0) {
            String savedCardId = savedCardResponse.getString("response.savedCardId[" + (size - 1) + "]");
            DeleteCardByMidCustId deleteCard = new DeleteCardByMidCustId(mid, custId).buildRequest(mid, custId, savedCardId, "JWT", jwtDelete);
            deleteCard.execute();
            System.out.println(savedCardId + " Deleted");
            size--;
        }
    }

    public static void deleteAllCardsUserId(String sso) {
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso,true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        System.out.println("Size is "+ size);
        while (size !=0)
        {
            String savedCardId= binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
            HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,savedCardId);
            JsonPath delBinResp = deleteBinUser.execute().jsonPath();
            int noOfCardsDeleted = Integer.parseInt(delBinResp.getString("NUMBER_OF_RECORDS"));
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(noOfCardsDeleted).isNotEqualTo(0);
            System.out.println("No of cards deleted "+noOfCardsDeleted);
            System.out.println(savedCardId+ " Deleted");
            binInfoResponse = binInfo.execute().jsonPath();
//            size = Integer.parseInt(binInfoResponse.getString("SIZE"));
            size=size-1;
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(size).isEqualTo(0);
        softly.assertAll();
    }

    public static void tokenizeCardMidCustId(String mid, String custId) throws UnsupportedEncodingException {
        String requestId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
        String signtaure = jwtSignatureTokenizeMidCustId(requestId, mid);
        TokenizeCard tokenizeCard = new TokenizeCard(mid, Constants.CardScheme.VISA.get(), requestId).buildRequest(visaEncryptedCardData, custId, Constants.TokenizationConsent.YES.get(), authRefId, Constants.CardSource.CARD_ON_FILE.get(), "", false, true, signtaure, "JWT", requestId);
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertAll();

        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(
                () ->
                {
                    FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(mid, tokenizeCardResponse.getString("body.tokenIndexNumber"), tokenizeCardResponse.getString("head.requestId"));
                    JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();
                    Boolean status = fetchTokenDetailsResponse.getString("body.tokenInfo.tokenStatus").equals("ACTIVE");
                    return status;
                }
        );
        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(mid, tokenizeCardResponse.getString("body.tokenIndexNumber"), tokenizeCardResponse.getString("head.requestId"));
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("ACTIVE");
        softly.assertAll();
    }


    public static void modifyTokenMidCustId(String mid, String tokenIndexNumber, String custId, String tokenStatus) {
        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(mid).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), tokenStatus, tokenIndexNumber, custId, "").createJwt(mid);
        JsonPath modifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(modifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(modifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(modifyTokenStatusResponse.getString("body.tokenStatus")).isEqualTo(tokenStatus);
        softly.assertAll();
    }

    public static JsonPath tokenizeCard(Constants.MerchantType merchant, User user, String cardScheme) {
        if (cardScheme.equalsIgnoreCase("VISA")) {
            tokenizeCard = new TokenizeCard(merchant.getId(), Constants.CardScheme.VISA.get())
                    .buildRequest(visaEncryptedCardData, user.custId(), Constants.TokenizationConsent.YES.get(), authRefId, Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, false)
                    .generateChecksum(merchant.getKey());
        } else if (cardScheme.equalsIgnoreCase("MASTERCARD")) {
            tokenizeCard = new TokenizeCard(merchant.getId(), Constants.CardScheme.MASTERCARD.get())
                    .buildRequest(masterEncryptedCardData, user.custId(), Constants.TokenizationConsent.YES.get(), authRefId, Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, false)
                    .generateChecksum(merchant.getKey());
        }

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertAll();

        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(
                () ->
                {
                    FetchTokenDetails fetchTokenDetails = new FetchTokenDetails("PAYTM197", tokenizeCardResponse.getString("body.tokenIndexNumber"), tokenizeCardResponse.getString("head.requestId"));
                    JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();
                    Boolean status = fetchTokenDetailsResponse.getString("body.tokenInfo.tokenStatus").equals("ACTIVE");
                    return status;
                }
        );
        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails("PAYTM197", tokenizeCardResponse.getString("body.tokenIndexNumber"), tokenizeCardResponse.getString("head.requestId"));
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("ACTIVE");
        softly.assertAll();
        return fetchTokenDetailsResponse;
    }
    public static void modifyTokenUserId(User user,String tokenIndexNumber,String tokenStatus)
    {
        ModifyTokenStatus modifyTokenStatus = new ModifyTokenStatus("PAYTM197").buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), tokenStatus,tokenIndexNumber,null,user.custId()).createJwt("PAYTM197");
        JsonPath ModifyTokenStatusResponse = modifyTokenStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.tokenStatus")).isEqualTo(Constants.TokenStatus.DEAD.get());
        softly.assertAll();
    }

    public static void modifyTokenUserIdInOtherVault(User user,String tokenIndexNumber,String tokenStatus,String mid)
    {
        ModifyTokenStatus modifyTokenStatus = new ModifyTokenStatus(mid).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), tokenStatus,tokenIndexNumber,null,user.custId()).createJwt(mid);
        JsonPath ModifyTokenStatusResponse = modifyTokenStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.tokenStatus")).isEqualTo(Constants.TokenStatus.DEAD.get());
        softly.assertAll();
    }
}
