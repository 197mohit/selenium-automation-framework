package com.paytm.dto.emiSubvention.ApiV1InitTxn;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import static com.paytm.LocalConfig.*;
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.INIT_TXN;

public class EmiSubventionInitTXN extends BaseApi
{
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"C11\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"requestType\": \"NATIVE\",\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"orderId\": \"{OrderID}\",\n" +
            "        \"websiteName\": \"retail\",\n" +
            "        \"cardTokenRequired\": \"true\",\n" +
            "        \"txnAmount\": {\n" +
            "            \"value\": \"1000.00\",\n" +
            "            \"currency\": \"INR\"\n" +
            "        },\n" +
            "        \"simplifiedSubvention\": {\n" +
            "            \"customerId\": \"1234\",\n" +
            "            \"subventionAmount\": \"470.555\",\n" +
            "            \"selectPlanOnCashierPage\": true\n" +
            "        },\n" +
            "        \"userInfo\": {\n" +
            "            \"custId\": \"1107196087\",\n" +
            "            \"mobile\": \"7404186250\",\n" +
            "            \"email\": \"arzoo.batra@.com\",\n" +
            "            \"firstName\": \"arzoo\",\n" +
            "            \"lastName\": \"batra\"\n" +
            "        },\n" +
            "        \"disablePaymentMode\": [],\n" +
            "        \"enablePaymentMode\": [],\n" +
            "        \"callbackUrl\": \"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\",\n" +
            "        \"goods\": [\n" +
            "            {\n" +
            "                \"merchantGoodsId\": \"0001\",\n" +
            "                \"merchantShippingId\": \"0101\",\n" +
            "                \"snapshotUrl\": \"[http://snap.url.com]\",\n" +
            "                \"description\": \"SummerDress\",\n" +
            "                \"category\": \"travelling\",\n" +
            "                \"quantity\": \"1\",\n" +
            "                \"unit\": \"Kg\",\n" +
            "                \"price\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"1\"\n" +
            "                },\n" +
            "                \"extendInfo\": {\n" +
            "                    \"udf1\": \"U1\",\n" +
            "                    \"udf2\": \"U2\",\n" +
            "                    \"udf3\": \"U3\",\n" +
            "                    \"udf4\": \"U4\",\n" +
            "                    \"udf5\": \"U5\"\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"shippingInfo\": [\n" +
            "            {\n" +
            "                \"merchantShippingId\": \"0001\",\n" +
            "                \"trackingNo\": \"00101\",\n" +
            "                \"carrier\": \"FederalExpress\",\n" +
            "                \"chargeAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"1\"\n" +
            "                },\n" +
            "                \"countryName\": \"JP\",\n" +
            "                \"stateName\": \"GA\",\n" +
            "                \"cityName\": \"Atlanta\",\n" +
            "                \"address1\": \"137WSanBernardino\",\n" +
            "                \"address2\": \"4114Sepulveda\",\n" +
            "                \"firstName\": \"Jim\",\n" +
            "                \"lastName\": \"Li\",\n" +
            "                \"mobileNo\": \"8376979170\",\n" +
            "                \"zipCode\": \"310001\",\n" +
            "                \"email\": \"arzoobatra04@gmail.com\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"extendInfo\": {\n" +
            "            \"udf1\": \"\",\n" +
            "            \"udf2\": \"\",\n" +
            "            \"udf3\": \"\",\n" +
            "            \"mercUnqRef\": \"\",\n" +
            "            \"orderAdditionalInfo\": {\n" +
            "                \"mid\": \"mid123\",\n" +
            "                \"mName\": \"merchantName\",\n" +
            "                \"mLogo\": \"merchantLogoUrl\",\n" +
            "                \"mcc\": \"mccCode\"\n" +
            "            },\n" +
            "            \"comments\": \"\"\n" +
            "        }\n" +
            "    }\n" +
            "}";
    public EmiSubventionInitTXN(String MID,String OrderID)
    {
        request=request.replace("{MID}",MID)
                .replace("{OrderID}",OrderID);
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(MOCK_HOST);
        getRequestSpecBuilder().setBasePath(INIT_TXN);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("orderId", OrderID);
        getRequestSpecBuilder().setBody(request);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }
}
