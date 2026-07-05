package com.paytm.api.MappingService;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.ArrayList;


public class UpdateEMI extends BaseApi {
    String request = "{\n" +
            "    \"externalMerchantId\" : \"{MID}\",\n" +
            "    \"emiConfigInfos\": [\n" +
            "            \n" +
            "            {\n" +
            "                \"issuingBank\": {\n" +
            "                    \"issuingBankId\": \"{BANKID}\",\n" +
            "                    \"issuingBankName\": \"{BANKNAME}\"\n" +
            "                },\n" +
            "                \"recordId\": \"120100000009487584449\",\n" +
            "                \"emiInfo\": {\n" +
            "                    \"emiPlanId\": null,\n" +
            "                    \"emiTenureId\": null,\n" +
            "                    \"cardAcquiringMode\": \"ONUS\",\n" +
            "                    \"emiMinAmount\": {\n" +
            "                        \"currency\": \"INR\",\n" +
            "                        \"value\": \"8900\",\n" +
            "                        \"amount\": \"8900\",\n" +
            "                        \"amountInRs\": \"89\"\n" +
            "                    },\n" +
            "                    \"emiMaxAmount\": {\n" +
            "                        \"currency\": \"INR\",\n" +
            "                        \"value\": \"79000\",\n" +
            "                        \"amount\": \"79000\",\n" +
            "                        \"amountInRs\": \"790\"\n" +
            "                    },\n" +
            "                    \"emiMonths\": 17,\n" +
            "                    \"emiInterestRate\": 9.37,\n" +
            "                    \"cardType\": \"CC\"\n" +
            "                },\n" +
            "                \"merchantId\": \"216820000007224195441\"\n" +
            "            }\n" +
            "                   \n" +
            "        \n" +
            "        ]\n" +
            "    \n" +
            "    \n" +
            "}";

    public UpdateEMI() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.UPDATE_EMI);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public String getRequest() {
        return request;
    }

    public UpdateEMI buildRequest(String mid, String bankID,String bankName) {
        setContext("externalMerchantId", mid);
        setContext("emiConfigInfos[0].issuingBank.issuingBankId", bankID);
        setContext("emiConfigInfos[0].issuingBank.issuingBankName", bankName);

        return this;


    }
}