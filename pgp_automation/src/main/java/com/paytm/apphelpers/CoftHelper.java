package com.paytm.apphelpers;

import com.google.gson.Gson;
import com.paytm.LocalConfig;
import com.paytm.api.coft.saveCard.SavedCardByUserId;
import com.paytm.dto.coft.CardData;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CoftHelper {

    public static PublicKey getPublicKey(String base64PublicKey){
        PublicKey publicKey = null;
        try{
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static String encrypt(String data, String publicKey)
    {
        byte [] encryptedData=null;
        try{Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
        encryptedData = cipher.doFinal(data.getBytes());}
        catch (Exception e)
        {
            e.printStackTrace();
        }
        String str = Base64.getEncoder().encodeToString(encryptedData);
        return str;
    }

    public static String encryptCardData(CardData cardDataObject)
    {
        String cardData;
        Gson gson = new Gson();
        cardData = gson.toJson(cardDataObject);
        String encryptedCardData = encrypt(cardData, LocalConfig.COFT_ENCRYPTION_KEY);
        System.out.println("encrypted data is: " + encryptedCardData);
        return encryptedCardData;
    }

    public static String getAccessToken(String userId)
    {
        SavedCardByUserId savedCardByUserId=new SavedCardByUserId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", userId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.CLIENTID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("RISK_VERIFIER_PG","JWT",jwt,userId);
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertAll();
        return savedCardByUserIdResponse.getString("accessToken");
    }
}
