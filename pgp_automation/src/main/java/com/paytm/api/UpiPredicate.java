package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * We can use this API to apply mock UPI data based on any custid
 */
public class UpiPredicate extends BaseApi {

    Response response;

    public UpiPredicate(String condition, String response) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath(Constants.Mockbank.UPI_PREDICATE_URL);
        getRequestSpecBuilder().setBody("{\"condition\":" + condition +",\"response\":{"+ response +"}}");
    }

    public UpiPredicate(String custId){
        String condition = "\"{request -> request.getParameter('cust-id')=='"+custId+"'}\"";
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath(Constants.Mockbank.UPI_PREDICATE_URL);
        getRequestSpecBuilder().setBody("{\"condition\":" + condition +",\"response\":{"+ upiProfileData +"}}");
    }

    private final String upiProfileData = "\"status\": \"SUCCESS\",\n" +
            "        \"seqNo\": \"9800907760114504bf4756b9606a62cepgpsandbox101paytmlocal\",\n" +
            "        \"respMessage\": \"\",\n" +
            "        \"respCode\": \"0\",\n" +
            "        \"respDetails\": {\n" +
            "            \"profileDetail\": {\n" +
            "                \"vpaDetails\": [\n" +
            "                    {\n" +
            "                        \"name\": \"ankitarora26@paytm\",\n" +
            "                        \"defaultCreditAccRefId\": \"10673\",\n" +
            "                        \"defaultDebitAccRefId\": \"10673\",\n" +
            "                        \"isPrimary\": true\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"bankAccounts\": [\n" +
            "                    {\n" +
            "                        \"bank\": \"Mypsp2\",\n" +
            "                        \"ifsc\": \"AABF0876543\",\n" +
            "                        \"accRefId\": \"10673\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0125\",\n" +
            "                        \"accountType\": \"UOD\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"P2M\",\n" +
            "                        \"warningMessage\": \"Unsecured Overdraft Account can only be used to make payments to merchants\",\n" +
            "                        \"pgBankCode\": \"PPBL\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"100000\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"GREEN\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https://static.paytmbank.com/upi/images/bank-logo/500004.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"MYPSP\",\n" +
            "                        \"ifsc\": \"AABC0876543\",\n" +
            "                        \"accRefId\": \"10679\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0127\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"ATMPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"HDFC\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"10\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"GREEN\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500001.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"Mybank\",\n" +
            "                        \"ifsc\": \"AABD0876543\",\n" +
            "                        \"accRefId\": \"10761\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0123\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"N\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"NHAI\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"10\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"RED\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"The Bank is experiencing downtime.Please select another payment option\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500007.png\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"profileStatus\": \"ACTIVE\",\n" +
            "                \"upiLinkedMobileNumber\": \"919999161601\",\n" +
            "                \"isDeviceBinded\": false\n" +
            "            },\n" +
            "            \"metaDetails\": {\n" +
            "                \"banksDown\": [],\n" +
            "                \"npciHealthCategory\": \"GREEN\",\n" +
            "                \"npciHealthMsg\": \"\",\n" +
            "                \"txnAction\": null\n" +
            "            }\n" +
            "        }";
}