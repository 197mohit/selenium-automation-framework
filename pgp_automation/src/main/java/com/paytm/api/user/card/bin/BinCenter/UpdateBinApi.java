package com.paytm.api.user.card.bin.BinCenter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;


import java.io.UnsupportedEncodingException;

public class UpdateBinApi extends BaseApi{

    public String jwtCreate(String ts) throws UnsupportedEncodingException {

        String jwtToken = JWT.create().withIssuer("PAYTM_BIN_CENTER")
                .withClaim("ts",ts)
                .withClaim("sub","PAYTM_BIN_CENTER_QA")
                .sign(Algorithm.HMAC256("7Bn2db6F5DZIFY97"));
        return jwtToken;
    }
    public UpdateBinApi(UpdateBinRequest requestBody)  {
        long currentTimestamp = System.currentTimeMillis();
        String currentTimestampString = String.valueOf(currentTimestamp);
        String Signature="";
        try{
        Signature=jwtCreate(currentTimestampString);}
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.BinCenter.MODIFY_BIN);
        getRequestSpecBuilder().addHeader("signature",Signature);
        getRequestSpecBuilder().addHeader("timestamp",currentTimestampString);
        getRequestSpecBuilder().addHeader("clientId","PAYTM_BIN_CENTER_QA");
        getRequestSpecBuilder().addHeader("requestid","faa6dec9-46eb-44bc-80ae-e8d590dce7ff");
        getRequestSpecBuilder().addQueryParam("updatedBy","Heisenberg");
        getRequestSpecBuilder().addQueryParam("fileName","BB");
        getRequestSpecBuilder().setBody(requestBody);
    }

}
