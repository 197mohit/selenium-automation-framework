package com.paytm.api.wallet.transitWallet;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WithdrawTransit extends BaseApi{

String request ="{\n" +
        "    \"request\": {\n" +
        "        \"orderId\": \"{ORDERID}\",\n" +
        "        \"txnAmount\": {AMOUNT},\n" +
        "        \"currencyCode\": \"INR\",\n" +
        "        \"metaInfo\": {\n" +
        "            \"globalBalance\": 50,\n" +
        "            \"merchantName\": \"Dominos\",\n" +
        "            \"testKey\": \"TestValue\",\n" +
        "            \"maskedCardNo\": \"{CARDNO}\",\n" +
        "            \"rrn\": \"eewrter345645\",\n" +
        "            \"txnType\": \"retail\",\n" +
        "            \"merchantAddress\": \"test address\",\n" +
        "            \"lat\": \"21.4\",\n" +
        "            \"long\": \"21.7\",\n" +
        "            \"PPBLacquiredmerchantflag\": \"true\"\n" +
        "        },\n" +
        "        \"txnDateTime\": \"1606368506\",\n" +
        "        \"mid\": \"{MID}\",\n" +
        "        \"mccCode\": \"77\"\n" +
        "    }\n" +
        "}";

    public String getRequest()
    {
        return request;
    }

    public WithdrawTransit setRequest(String orderId, String amount, String cardNo, String mid)
    {
        this.request= request
                .replace("{ORDERID}",orderId)
                .replace("{AMOUNT}",amount)
                .replace("{CARDNO}",cardNo)
                .replace("{MID}",mid);



        return this;
    }

    public WithdrawTransit(String orderId, String amount, String cardNo, Constants.MerchantType mid, User user) throws NoSuchAlgorithmException {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        getRequestSpecBuilder().setBasePath(Constants.WalletAPIResourcePath.TRANSIT_WITHDRAW);
        setRequest(orderId,amount,cardNo,mid.getId());

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("ssoid",user.custId());
        tokenMap.put("clientId", "a5516f104428408fb6051f833c9bb9e0");
        tokenMap.put("requestHash",CommonHelpers.toHexString(CommonHelpers.getSHA(getRequest())));
        tokenMap.put("ts", String.valueOf(CommonHelpers.getCurrentEpochTime()));
       
        JWTCreator.Builder builder = JWT.create();
        String jwtToken = null;
        Iterator var3 = tokenMap.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var3.next();
            builder.withClaim((String)entry.getKey(), (String)entry.getValue());
        }

        try {
            jwtToken = builder.sign(Algorithm.HMAC256(LocalConfig.PG_JWT_KEY));
        } catch (JWTCreationException | UnsupportedEncodingException | IllegalArgumentException var5) {
            var5.printStackTrace();
        }

        getRequestSpecBuilder().addHeader("x-jwt-token",jwtToken);

        getRequestSpecBuilder().setBody(getRequest());
    }

}
