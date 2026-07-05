package com.paytm.api.boss;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class staticPrefUpdateApi extends BaseApi {
    String request="{\n" +
            "    \"prefName\": \"BIN_IN_RESPONSE\",\n" +
            "    \"prefValue\": \"N\",\n" +
            "    \"mid\": \"qa8PG294377944191275\",\n" +
            "    \"prefType\": \"PREFERENCE_TYPE\",\n" +
            "    \"pg2CreatedReq\": false,\n" +
            "    \"updatedBy\": \"1107228639\"\n" +
            "}";
    public staticPrefUpdateApi() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.BOSS_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.STATIC_PREFERENCE_API);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("x-client-token", getJWT());   //jwt
        getRequestSpecBuilder().addHeader("x-client-id",getClientId() );       //client_id
        System.out.println("2");
    }
    public String getRequest(){
        return request;
    }
    public static String createJwtHMAC(String clientId, String key) {

        byte[] decodedKey = Base64.getMimeDecoder().decode(key);

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("client-id", clientId);
        String token = Jwts.builder().setIssuedAt(new Date()).addClaims(claims)
                .signWith(SignatureAlgorithm.HS512, decodedKey).compact();
        return token;
    }
    public String getClientId(){
        String clientId="66c02d3e-ebc0-4117-ba85-7f523ac8d424";
        return clientId;
    }
    public String getJWT(){
        String jwt=createJwtHMAC(getClientId(),"WidnixnDo2780hILxdvvQXu9shJ9tIZnSsX4aEe9aKoOg5n7CtkijYqCb0ijNe7SE1qOu38JVU+gfx8G89oWvQ==");
        return jwt;
    }
    public void buildRequest(String prefName, String prefValue,  String mid){
        setContext("prefName",prefName);
        setContext("prefValue",prefValue);
        setContext("mid",mid);
    }
    public void buildRequestStaticPref(String prefName, String prefValue,  String mid){
        setContext("prefName",prefName);
        setContext("prefValue",prefValue);
        setContext("prefType","PREFERENCE_STATIC_TYPE");
        setContext("mid",mid);
    }
}
