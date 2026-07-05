package com.paytm.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Base64;
import java.util.Map;

public class CallBackApi extends BaseApi {


    @Override
    public Response execute(){
        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().convertValue(getRequestBody(),Map.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String signature = Jwts.builder().setIssuer(PGPHelpers.ISSUER.PAYTMBANK.toString())
                .addClaims(map)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString("7nsFaObLRz-gQ7VH1anTD-qY8iiWeUc4lvVTHdfObTc=".getBytes()))
                .compact();
        getRequestSpecBuilder().addHeader("Authorization",signature);
        return super.execute();
    }

    public CallBackApi(String body) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.VANProxy.VAN_PROXY_PAYMENT);
        getRequestSpecBuilder().setBody(body);
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
}

