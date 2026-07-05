package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

public class InstaproxyPtybliResponse extends BaseApi {

    public static final String INSTAPROXY_PTYL_UPI_PUSH_RESP = "/instaproxy/secureresponse/PTYL/UPI/PUSH/RESP";

    String request = "{\n" +
            "    \"header\": {\n" +
            "        \"requestTimestamp\": \"1739790918577\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"signature\": \"{JWTTOKEN}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"messageDesc\": \"\",\n" +
            "        \"payerName\": \"Chakshu Singhal\",\n" +
            "        \"payeeVpa\": \"paytm-9759417321@ptybl\",\n" +
            "        \"payerVpa\": \"paytmTest@ptys\",\n" +
            "        \"payerBank\": \"Punjab National Bank\",\n" +
            "        \"payerIfsc\": \"PUNB\",\n" +
            "        \"payerMaskedAccount\": \"\",\n" +
            "        \"channelCode\": \"PGPTM\",\n" +
            "        \"externalSerialNo\": \"{ESN}\",\n" +
            "        \"txnStatus\": \"SUCCESS\",\n" +
            "        \"bankRRN\": \"{bankRrn}\",\n" +
            "        \"amount\": \"{TXNAMOUNT}\",\n" +
            "        \"responseCode\": \"0\",\n" +
            "        \"responseMessage\": \"Transaction is successful\",\n" +
            "        \"settlementType\": \"DEFERRED_SETTLEMENT\",\n" +
            "        \"paymentInstrument\": \"{paymentInstrument}\",\n" +
            "        \"creditCardInfo\": {\n" +
            "            \"binNumber\": \"\",\n" +
            "            \"creditAccountReferenceNumber\": \" \",\n" +
            "            \"cardType\": \"\"\n" +
            "        }\n" +
            "    }\n" +
            "}" ;

    String requestUpiCcBin = "{\n" +
            "    \"header\": {\n" +
            "        \"requestTimestamp\": \"1739790918577\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"signature\": \"{JWTTOKEN}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"messageDesc\": \"\",\n" +
            "        \"payerName\": \"Chakshu Singhal\",\n" +
            "        \"payeeVpa\": \"paytm-9759417321@ptybl\",\n" +
            "        \"payerVpa\": \"paytmTest@ptys\",\n" +
            "        \"payerBank\": \"Punjab National Bank\",\n" +
            "        \"payerIfsc\": \"PUNB\",\n" +
            "        \"payerMaskedAccount\": \"\",\n" +
            "        \"channelCode\": \"PGPTM\",\n" +
            "        \"externalSerialNo\": \"{ESN}\",\n" +
            "        \"txnStatus\": \"SUCCESS\",\n" +
            "        \"bankRRN\": \"{bankRrn}\",\n" +
            "        \"amount\": \"{TXNAMOUNT}\",\n" +
            "        \"responseCode\": \"0\",\n" +
            "        \"responseMessage\": \"Transaction is successful\",\n" +
            "        \"settlementType\": \"DEFERRED_SETTLEMENT\",\n" +
            "        \"paymentInstrument\": \"{paymentInstrument}\",\n" +
            "        \"creditCardInfo\": {\n" +
            "            \"binNumber\": \"652925\",\n" +
            "            \"creditAccountReferenceNumber\": \" 652925000XXXXXXXX007640639\",\n" +
            "            \"cardType\": \"Platinum1\"\n" +
            "        }\n" +
            "    }\n" +
            "}" ;


    public String getRequest()
    {
        return request;
    }

    public String getRequestUpiCcBin()
    {
        return requestUpiCcBin;
    }

    public  String CreateJwtToken(String txnAmount,String externalSerialNo , String bankRrn , String paymentInstrument ){
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("settlementType", "DEFERRED_SETTLEMENT");
        tokenMap.put("amount", txnAmount);
        tokenMap.put("payerBank", "Punjab National Bank");
        tokenMap.put("payeeVpa", "paytm-9759417321@ptybl");
        tokenMap.put("responseCode", "0");
        tokenMap.put("txnStatus", "SUCCESS");
        tokenMap.put("payerIfsc", "PUNB");
        tokenMap.put("messageDesc", "");
        tokenMap.put("bankRRN", bankRrn);
        tokenMap.put("responseMessage", "Transaction is successful");
        tokenMap.put("externalSerialNo", externalSerialNo);
        tokenMap.put("channelCode", "PGPTM");
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.JWT_KEY);
        return jwt;
    }

    public  void setRequest( String jwtToken , String txnAmount,String externalSerialNo , String bankRrn , String paymentInstrument) {
        request = request.replace("{JWTTOKEN}", jwtToken).replace("{TXNAMOUNT}", txnAmount).replace("{ESN}", externalSerialNo).replace("{bankRrn}", bankRrn).replace("{paymentInstrument}", paymentInstrument);

    }

    public  void setRequestUpiCcBin( String jwtToken , String txnAmount,String externalSerialNo , String bankRrn , String paymentInstrument) {
        requestUpiCcBin = requestUpiCcBin.replace("{JWTTOKEN}", jwtToken).replace("{TXNAMOUNT}", txnAmount).replace("{ESN}", externalSerialNo).replace("{bankRrn}", bankRrn).replace("{paymentInstrument}", paymentInstrument);

    }

    public InstaproxyPtybliResponse( String txnAmount, String externalSerialNo , String bankRrn, String paymentInstrument)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INSTAPROXY_SECURERESPONSE_PTYBLI_UPI_PUSH_RESP);
        String jwtToken=CreateJwtToken( txnAmount, externalSerialNo, bankRrn , paymentInstrument);
        setRequest( jwtToken, txnAmount, externalSerialNo, bankRrn , paymentInstrument);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public InstaproxyPtybliResponse( String txnAmount, String externalSerialNo , String bankRrn, String paymentInstrument, String upiCcBin)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INSTAPROXY_SECURERESPONSE_PTYBLI_UPI_PUSH_RESP);
        String jwtToken=CreateJwtToken( txnAmount, externalSerialNo, bankRrn , paymentInstrument);
        setRequestUpiCcBin( jwtToken, txnAmount, externalSerialNo, bankRrn , paymentInstrument);
        getRequestSpecBuilder().setBody(getRequestUpiCcBin());
    }

    public InstaproxyPtybliResponse setSecureResponsePath(String basePath) {
        getRequestSpecBuilder().setBasePath(basePath);
        return this;
    }
}
