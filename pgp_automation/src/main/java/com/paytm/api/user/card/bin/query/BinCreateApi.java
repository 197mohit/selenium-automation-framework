package com.paytm.api.user.card.bin.query;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

/**
 * Created by Himanshu Sharma on 20/Nov/2021
 * */

public class BinCreateApi extends BaseApi
{
    public BinCreateApi(String bin)
    {
        String body="{\n" +
                "    \"bin\": \""+bin+"\",\n" +
                "    \"blocked\": \"{blocked_status}\",\n" +
                "    \"cardScheme\": \"VISA\",\n" +
                "    \"cardType\": \"DC\",\n" +
                "    \"countryCode\": \"IN\",\n" +
                "    \"institutionId\": \"ICICI\",\n" +
                "    \"binConfigAttributes\":  {\n" +
                "            \"INDIAN\": \"true\",\n" +
                "            \"ZERO_SUCCESS_RATE\": \"false\",\n" +
                "            \"ONE_CLICK_SUPPORTED\": \"false\",\n" +
                "            \"PREPAID_CARD\": \"false\"\n" +
                "        },\n" +
                "    \"source\": \"ADMIN\"\n" +
                "}";
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.SUPERGW_LITE);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.Alipay.BIN_CREATE);
        getRequestSpecBuilder().setBody(body);
        getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }
}