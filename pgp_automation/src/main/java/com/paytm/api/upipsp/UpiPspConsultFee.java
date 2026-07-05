package com.paytm.api.upipsp;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

public class UpiPspConsultFee extends BaseApi {

    String request = "{\n" +
            "  \"head\": {\n" +
            "    \"clientId\": \"OCIL\",\n" +
            "    \"requestTimestamp\": \"1709561770681\",\n" +
            "    \"version\": \"v1\",\n" +
            "    \"requestMsgId\": \"AXI37c39f11f22d4f79a25ebb24e388ebe1\",\n" +
            "    \"signature\": \"{JWTTOKEN}\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"mid\": \"{MID}\",\n" +
            "    \"txnAmount\": \"{TXNAMOUNT}\",\n" +
            "    \"paymentFee\": \"{PAYMENTFEE}\",\n" +
            "    \"payerVpa\": \"payer@ptys\",\n" +
            "    \"payeeVpa\": \"{PAYEEVPA}\",\n" +
            "    \"mcc\": \"7322\",\n" +
            "    \"externalSerialNo\": \"{ESN}\",\n" +
            "    \"payerPaymentInstrument\": \"{PAYERPAYMENTINSTRUMENT}\",\n" +
            "    \"subAccountType\": \"{CREDITLINESUBACCOUNTTYPE}\",\n" +
            "    \"merchantName\": \"Prabhas\",\n" +
            "    \"npciTxnId\": \"T9836223598\",\n" +
            "    \"extendInfo\": {\n" +
            "      \"additionalInfo\": \"comment:UPI\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public String getRequest()
    {
        return request;
    }


    public  String CreateJwtToken(String mid, String txnAmount,String payeeVPA, String externalSerialNo , String payerPaymentInstrument ){
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("mid", mid);
        tokenMap.put("txnAmount", txnAmount);
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("externalSerialNo", externalSerialNo);
        tokenMap.put("payerPaymentInstrument", payerPaymentInstrument);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.UPI_PSP_PAYMENT_STATUS_JWT_KEY);
        return jwt;
    }

    public  void setRequest(String JwtToken, String mid, String txnAmount,String paymentFee , String payeeVPA, String externalSerialNo , String payerPaymentInstrument, String subAccountTypeValue) {
        request = request.replace("{JWTTOKEN}", JwtToken).replace("{MID}", mid).replace("{TXNAMOUNT}", txnAmount).replace("{PAYMENTFEE}", paymentFee).replace("{PAYEEVPA}", payeeVPA).replace("{ESN}", externalSerialNo).replace("{PAYERPAYMENTINSTRUMENT}", payerPaymentInstrument).replace("{CREDITLINESUBACCOUNTTYPE}", subAccountTypeValue);

    }

    public UpiPspConsultFee( String mid, String txnAmount,String paymentFee , String payeeVPA, String externalSerialNo , String payerPaymentInstrument, String subAccountTypeValue)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.UPI_PSP_PROCESSOR_API_V1_CONSULT_FEE);
        String jwtToken=CreateJwtToken( mid,txnAmount, payeeVPA, externalSerialNo, payerPaymentInstrument);
        setRequest( jwtToken, mid, txnAmount, paymentFee , payeeVPA, externalSerialNo , payerPaymentInstrument, subAccountTypeValue);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
