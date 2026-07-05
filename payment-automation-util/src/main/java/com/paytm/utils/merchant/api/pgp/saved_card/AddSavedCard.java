package com.paytm.utils.merchant.api.pgp.saved_card;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class AddSavedCard extends BaseApi {

    // /etc/payment_engine/key/test/pg_jwt.key in savedcardService
    private final static String pgJWTKey= "hUOB1Us6TIv5zAgpDwIcs4QBvGxh0VeR";


    private static String getJWTToken(Map<String, String> jwtClaims, String issuer, String jwtKey)
    {
        JWTCreator.Builder builder = JWT.create().withIssuer(issuer);
        String jwtToken = null;
        Iterator var3 = jwtClaims.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var3.next();
            builder.withClaim((String)entry.getKey(), (String)entry.getValue());
        }

        try {
            jwtToken = builder.sign(Algorithm.HMAC256(jwtKey));
        } catch (JWTCreationException | IllegalArgumentException | UnsupportedEncodingException var5) {
            var5.printStackTrace();
        }
        return jwtToken;
    }

    public AddSavedCard(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/savedcardservice/savedCardService/v1/add/savedcard/trustedCard");
        getRequestSpecBuilder().setBody(body);
    }

    public AddSavedCard(String pgpUrl, String custId, String cardNumber, String expiryDate) {
        this(pgpUrl, createBody(custId, cardNumber, expiryDate));
    }

    private static String createBody(String custId, String cardNumber, String expiryDate){
        TreeMap<String, String> body = new TreeMap<>();

        body.put("tokenType","JWT");
        body.put("userId", custId);
        body.put("cardNumber", cardNumber);
        body.put("expiryDate", expiryDate);
        String jwtToken = getJWTToken(body,"ts",pgJWTKey);
        body.put("token",jwtToken);

        JSONObject obj = new JSONObject(body);
        return obj.toString();
    }


}
