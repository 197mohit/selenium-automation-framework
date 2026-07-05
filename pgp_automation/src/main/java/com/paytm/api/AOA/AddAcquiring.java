package com.paytm.api.AOA;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.ArrayList;

public class AddAcquiring extends BaseApi {
    String request ="{\n" +
            "    \"acquiringConfigInfo\": {\n" +
            "        \"merchantId\": \"{MID}\",\n" +
            "        \"mcc\": \"Retail\",\n" +
            "        \"payMethod\": \"{PAYMETHOD}\",\n" +
            "\t    \"serviceInstId\": \"{SERVICEINSTID}\",\n" +
            "        \"serviceInstName\": \"ICICI\",\n" +
            "        \"enableStatus\": true,\n" +
            "        \"preference\": 1,\n" +
            "        \"subServiceInstIds\": [\"VISA\",\"MASTER\",\"MAESTRO\"],\n" +
            "\n" +
            "        \"unsupportedSchemes\":[\"{unsupportedScheme}\"],\n" +
            "        \"unsupportedCardSubTypes\":[\"{unsupportedCardSubTypes}\"],\n" +
            "        \"unsupportedIssuingBanks\":[\"{unsupportedIssuingBanks}\"],\n" +
            "        \"unsupportedUpiPayMethods\":[\"{unsupportedUpiPayMethods}\"]\n" +
            "\n" +
            "\n" +
            "\n" +
            "    }\n" +
            "}";
    public AddAcquiring() {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.AOA_ADD_ACQUIRING);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }

    public AddAcquiring buildRequest(String mid,String paymode,String serviceInstId ) {
        ArrayList<String> unsupportedScheme = new ArrayList<String>();
        ArrayList<String> unsupportedCardSubTypes = new ArrayList<String>();
        ArrayList<String> unsupportedIssuingBanks = new ArrayList<String>();
        ArrayList<String> unsupportedUpiPayMethods = new ArrayList<String>();

        setContext("acquiringConfigInfo.merchantId", mid);
        setContext("acquiringConfigInfo.payMethod",paymode);
        setContext("acquiringConfigInfo.serviceInstId",serviceInstId);
        setContext("acquiringConfigInfo.unsupportedSchemes",unsupportedScheme);
        setContext("acquiringConfigInfo.unsupportedCardSubTypes" ,unsupportedCardSubTypes );
        setContext("acquiringConfigInfo.unsupportedIssuingBanks" , unsupportedIssuingBanks);
        setContext("acquiringConfigInfo.unsupportedUpiPayMethods" , unsupportedUpiPayMethods);
        return this;
    }

    public AddAcquiring buildRequest(String mid, String paymode, String serviceInstId, ArrayList<String> unsupportedScheme, ArrayList<String> unsupportedCardSubTypes , ArrayList<String>  unsupportedIssuingBanks , ArrayList<String> unsupportedUpiPayMethods) {
        setContext("acquiringConfigInfo.merchantId", mid);
        setContext("acquiringConfigInfo.payMethod",paymode);
        setContext("acquiringConfigInfo.serviceInstId",serviceInstId);
        setContext("acquiringConfigInfo.unsupportedSchemes",unsupportedScheme);
        setContext("acquiringConfigInfo.unsupportedCardSubTypes" ,unsupportedCardSubTypes );
        setContext("acquiringConfigInfo.unsupportedIssuingBanks" , unsupportedIssuingBanks);
        setContext("acquiringConfigInfo.unsupportedUpiPayMethods" , unsupportedUpiPayMethods);

        return this;
    }
}
