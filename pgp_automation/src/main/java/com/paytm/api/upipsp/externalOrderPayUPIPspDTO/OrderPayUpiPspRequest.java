package com.paytm.api.upipsp.externalOrderPayUPIPspDTO;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import java.io.UnsupportedEncodingException;


public class OrderPayUpiPspRequest extends BaseApi {

   public  OrderPayUpiPspRequest(OrderPayUpiPsp orderPayUpiPsp, String jwtToken){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
       getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/upi-psp-processor/external/order/pay/upipsp");
        orderPayUpiPsp.getHeader().setSignature(jwtToken);
        getRequestSpecBuilder().setBody(orderPayUpiPsp);
    }
    public static String createJwtToken(OrderPayUpiPsp orderPayUpiPsp) throws UnsupportedEncodingException {
        String token = JWT.create()
                .withClaim("bankCode",orderPayUpiPsp.getBody().getBankCode())
                .withClaim("txnStatus", orderPayUpiPsp.getBody().getTxnStatus())
                .withClaim("orderId", orderPayUpiPsp.getBody().getOrderId())
                .withClaim("payeeVpa", orderPayUpiPsp.getBody().getPayeeVpa())
                .withClaim("iss", "ts")
                .withClaim("mid", orderPayUpiPsp.getBody().getMid())
                .withClaim("refId", orderPayUpiPsp.getBody().getRefId())
                .withClaim("txnAmount", orderPayUpiPsp.getBody().getTxnAmount())
                .sign(Algorithm.HMAC256("UGFE11N4n1nxoJU="));
        return token;
    }
    public static JsonPath orderPayUpiPsp(OrderPayUpiPsp orderPayUpiPsp) throws UnsupportedEncodingException {
        String jwtToken= createJwtToken(orderPayUpiPsp);
        return new OrderPayUpiPspRequest(orderPayUpiPsp, jwtToken).execute().jsonPath();
    }
}
