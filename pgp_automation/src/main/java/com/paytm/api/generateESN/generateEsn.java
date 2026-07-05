package com.paytm.api.generateESN;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.appconstants.Constants.NativeAPIResourcePath.THEIA_V1_GENERATEESN;

public class generateEsn extends BaseApi {

    String request = "{\n" +
            "    \"header\": {\n" +
            "        \"clientId\": \"C11\",\n" +
            "        \"requestTimestamp\": \"1559202961528\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestMsgId\": \"asdasdasd\",\n" +
            "        \"signature\": \"{jwtToke}\"},\n" +
            "    \"body\": {\n" +
            "        \"externalSerialNo\": \"{extNo}\",\n" +
            "        \"isMandateFlow\": \"{isMandateFlow}\"\n" +
            "    }\n" +
            "}";

    public String getRequest() {
        return request;
    }


    public generateEsn setRequest(String extNo,String isMandateFlow) {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","ts");
        tokenMap.put("externalSerialNo",extNo);
        String jwtToke=PGPHelpers.createJsonWebToken(tokenMap,PGPHelpers.ISSUER.ts,"z6zzVeIMRrRuuPBWA1LO");
        request = request.replace("{extNo}", extNo).replace("{jwtToke}",jwtToke).replace("{isMandateFlow}",isMandateFlow);

        return this;
    }

    public RequestSpecification reqSpec(String extNo,String isMandateFlow) {
        setRequest(extNo,isMandateFlow);

        return new RequestSpecBuilder()
                .setBaseUri(LocalConfig.PGP_HOST)
                .setAccept(ContentType.ANY)
                .setContentType(ContentType.JSON)
                .setBody(getRequest())
                .setBasePath(THEIA_V1_GENERATEESN)
                .build().log().all();

    }
}

