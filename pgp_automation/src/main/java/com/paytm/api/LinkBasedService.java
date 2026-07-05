package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import io.restassured.http.ContentType;

import java.util.Map;


public class LinkBasedService extends BaseApi {
    String request = "{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"UMP\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"AES\",\n" +
            "    \"signature\": \"\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "\"merchantRequestId\": \"test123\",\n" +
            "    \"mid\": \"{MERCHANT_ID}\",\n" +
            "    \"linkName\": \"InvoiceNew1\",\n" +
            "    \"linkDescription\": \"Hello's S\",\n" +
            "    \"linkType\": \"GENERIC\",\n" +
            "\"invoiceId\": \"1568895363\",\n" +
            "\"expiryDate\": \"05/04/2022\",\n" +
            "\"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            " \"sendSms\": \"true\",\n" +
            "    \"sendEmail\": \"true\",\n" +
            "\"customerContact\": {\n" +
            "          \"customerName\": \"Rahul\",\n" +
            "          \"customerEmail\": \"rahul.chugh@paytm.com\",\n" +
            "          \"customerMobile\": \"9654269965\"\n" +
            "},\n" +
            " \"statusCallbackUrl\": \"\",\n" +
            "     \"invoicePhoneNo\": \"01145509664\",\n" +
            "    \"invoiceEmail\": \"dipendra.choudhary@paytm.com\",\n" +
            "      \"invoiceDetails\": [{\n" +
            "            \"productName\": \"Test prod \",\n" +
            "            \"productCode\": \"P101\",\n" +
            "            \"noOfUnits\": \"1\",\n" +
            "            \"perUnitAmount\": 1.0,\n" +
            "            \"perUnitTax\": [{\n" +
            "                    \"taxName\": \"SGST\",\n" +
            "                    \"taxAmount\": \"0.0\"\n" +
            "                }]\n" +
            "        },\n" +
            "        {\n" +
            "            \"productName\": \"Test prod \",\n" +
            "            \"productCode\": \"P101\",\n" +
            "            \"noOfUnits\": \"2\",\n" +
            "            \"perUnitAmount\": 1.0,\n" +
            "            \"perUnitTax\": [{\n" +
            "                    \"taxName\": \"SGST\",\n" +
            "                    \"taxAmount\": \"0.0\"\n" +
            "                }]\n" +
            "        }],\n" +
            "    \"maxPaymentsAllowed\" : \"10\"\n" +
            "  }\n" +
            "  \n" +
            "}";



    public String getRequest()
    {return request;}

    public LinkBasedService setRequest(String mid,String txnAmount)
    {
        request = request.replace("{MERCHANT_ID}",mid).replace("{TRANSACTION_AMOUNT}",txnAmount);
        return this;
    }

    public LinkBasedService(String mid,String txnAmount) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        setRequest(mid,txnAmount);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
    }

    public LinkBasedService(String mId, String mKey, String txnAmount) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        this.setRequest(mId,txnAmount);
        Map request = (Map) new JsonSlurper().parseText(this.getRequest());
        ((Map) request.get("head")).put("signature", PGPHelpers.getNativeChecksum(mKey, request.get("body")));
        getRequestSpecBuilder().setBody(JsonOutput.toJson(request));
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
    }
}