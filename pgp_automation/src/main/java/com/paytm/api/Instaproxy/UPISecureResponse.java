package com.paytm.api.Instaproxy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class UPISecureResponse extends BaseApi {

    String request = "{\n"
        + "    \"header\": {\n"
        + "        \"requestTimestamp\": \"1739790918577\",\n"
        + "        \"version\": \"v1\",\n"
        + "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzZXR0bGVtZW50VHlwZSI6IkRFRkVSUkVEX1NFVFRMRU1FTlQiLCJhbW91bnQiOiI1MDUuOTAiLCJwYXllckJhbmsiOiJQdW5qYWIgTmF0aW9uYWwgQmFuayIsInBheWVlVnBhIjoicGF5ZWV2cGFAdGVzdCIsImlzcyI6InRzIiwicmVzcG9uc2VDb2RlIjoiMCIsInR4blN0YXR1cyI6IlNVQ0NFU1MiLCJwYXllcklmc2MiOiJQVU5CIiwibWVzc2FnZURlc2MiOiIiLCJiYW5rUlJOIjoiMTIxNjE1NDI1NjQiLCJyZXNwb25zZU1lc3NhZ2UiOiJUcmFuc2FjdGlvbiBpcyBzdWNjZXNzZnVsIiwiZXh0ZXJuYWxTZXJpYWxObyI6IjUwNTAxMjM3MzI0NDE1OTc5NTMiLCJjaGFubmVsQ29kZSI6IlBHUFRNIn0.WPLQ0QPByeUyN-5-HQqmlSYalv8yx0qErt3afBzU28U\"\n"
        + "    },\n"
        + "    \"body\": {\n"
        + "        \"messageDesc\": \"\",\n"
        + "        \"payerName\": \"Abhishek Verma\",\n"
        + "        \"payeeVpa\": \"paytm-956932428@ptybl\",\n"
        + "        \"payerVpa\": \"paytmTest@ptys\",\n"
        + "        \"payerBank\": \"Punjab National Bank\",\n"
        + "        \"payerIfsc\": \"PUNB\",\n"
        + "        \"channelCode\": \"PGPTM\",\n"
        + "        \"externalSerialNo\": \"5050123732441597953\",\n"
        + "        \"txnStatus\": \"SUCCESS\",\n"
        + "        \"bankRRN\": \"12161542564\",\n"
        + "        \"amount\": \"505.90\",\n"
        + "        \"responseCode\": \"0\",\n"
        + "        \"responseMessage\": \"Transaction is successful\",\n"
        + "        \"settlementType\": \"DEFERRED_SETTLEMENT\",\n"
        + "        \"paymentInstrument\": \"PPI_WALLET\",\n"
        + "        \"CreditCardInfo\": {\n"
        + "            \"binNumber\": \"\",\n"
        + "            \"creditAccountReferenceNumber\": \" \",\n"
        + "            \"cardType\": \"\"\n"
        + "        }\n"
        + "    }\n"
        + "}";
    public UPISecureResponse buildRequest(String payerName,String payerVpa,String payeeVpa,String payerBank,String payerIfsc,String externalSerialNo,String txnStatus,String bankRRN, String amount,String responseCode,String responseMessage, String settlementType,String payerPaymentInstrument,String bankCode) {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/instaproxy/secureresponse/"+bankCode+"/UPI/PUSH/RESP");
        setContext("body.payerName",payerName);
        setContext("body.payeeVpa",payeeVpa);
        setContext("body.payerVpa",payerVpa);
        setContext("body.payerBank",payerBank);
        setContext("body.payerIfsc",payerIfsc);
        setContext("body.externalSerialNo",externalSerialNo);
        setContext("body.txnStatus",txnStatus);
        setContext("body.bankRRN",bankRRN);
        setContext("body.amount",amount);
        setContext("body.responseCode",responseCode);
        setContext("body.responseMessage",responseMessage);
        setContext("body.settlementType",settlementType);
        deleteContext("body.paymentInstrument");
        deleteContext("body.CreditCardInfo");
        setContext("header.requestTimestamp",System.currentTimeMillis());
        setContext("header.signature", generateJwt(payerBank, payeeVpa, amount, responseCode, txnStatus, payerIfsc, bankRRN, responseMessage, externalSerialNo, settlementType));
        getRequestSpecBuilder().setBody(getRequest());
        return this;
    }
    public UPISecureResponse buildRequest(String payerName,String payerVpa,String payeeVpa,String payerBank,String payerIfsc,String externalSerialNo,String txnStatus,String bankRRN, String amount,String responseCode,String responseMessage, String settlementType,String paymentInstrument) {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP");
        setContext("body.payerName",payerName);
        setContext("body.payeeVpa",payeeVpa);
        setContext("body.payerVpa",payerVpa);
        setContext("body.payerBank",payerBank);
        setContext("body.payerIfsc",payerIfsc);
        setContext("body.externalSerialNo",externalSerialNo);
        setContext("body.txnStatus",txnStatus);
        setContext("body.bankRRN",bankRRN);
        setContext("body.amount",amount);
        setContext("body.responseCode",responseCode);
        setContext("body.responseMessage",responseMessage);
        setContext("body.settlementType",settlementType);
        setContext("body.paymentInstrument",paymentInstrument);
        setContext("header.requestTimestamp",System.currentTimeMillis());
        setContext("header.signature", generateJwt(payerBank, payeeVpa, amount, responseCode, txnStatus, payerIfsc, bankRRN, responseMessage, externalSerialNo, settlementType));
        getRequestSpecBuilder().setBody(getRequest());
        return this;
    }

    public UPISecureResponse buildRequest(String payerName,String payerVpa,String payeeVpa,String payerBank,String payerIfsc,String externalSerialNo,String txnStatus,String bankRRN, String amount,String responseCode,String responseMessage, String settlementType,String paymentInstrument,String binNumber,String creditAccountReferenceNumber, String cardType) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP");
        setContext("body.payerName",payerName);
        setContext("body.payeeVpa",payeeVpa);
        setContext("body.payerVpa",payerVpa);
        setContext("body.payerBank",payerBank);
        setContext("body.payerIfsc",payerIfsc);
        setContext("body.externalSerialNo",externalSerialNo);
        setContext("body.txnStatus",txnStatus);
        setContext("body.bankRRN",bankRRN);
        setContext("body.amount",amount);
        setContext("body.responseCode",responseCode);
        setContext("body.responseMessage",responseMessage);
        setContext("body.settlementType",settlementType);
        setContext("body.paymentInstrument",paymentInstrument);
        setContext("body.CreditCardInfo.binNumber",binNumber);
        setContext("body.CreditCardInfo.creditAccountReferenceNumber",creditAccountReferenceNumber);
        setContext("body.CreditCardInfo.cardType",cardType);
        setContext("header.requestTimestamp",System.currentTimeMillis());
        setContext("header.signature", generateJwt(payerBank, payeeVpa, amount, responseCode, txnStatus, payerIfsc, bankRRN, responseMessage, externalSerialNo, settlementType));
        getRequestSpecBuilder().setBody(getRequest());
        return this;
    }

    private String generateJwt(String payerBank, String payeeVpa, String amount, String responseCode, 
                             String txnStatus, String payerIfsc, String bankRRN, String responseMessage, 
                             String externalSerialNo, String settlementType) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(LocalConfig.JWT_KEY);
            return JWT.create()
                .withClaim("settlementType", settlementType)
                .withClaim("amount", amount)
                .withClaim("payerBank", payerBank)
                .withClaim("payeeVpa", payeeVpa)
                .withClaim("iss", "ts")
                .withClaim("responseCode", responseCode)
                .withClaim("txnStatus", txnStatus)
                .withClaim("payerIfsc", payerIfsc)
                .withClaim("messageDesc", "")
                .withClaim("bankRRN", bankRRN)
                .withClaim("responseMessage", responseMessage)
                .withClaim("externalSerialNo", externalSerialNo)
                .withClaim("channelCode", "PGPTM")
                .withIssuedAt(new Date())
                .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public String getRequest() {
        return request;
    }
} 