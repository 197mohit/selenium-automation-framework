package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.pages.TxnStatusResponse;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.List;

public class Handlerinternaltxnstatus extends BaseApi {

    public Handlerinternaltxnstatus txnStatusResponse;

    String body= "{\n" +
            "    \"head\": {\n" +
            "        \"mid\": \"{MID}\",\n" +
            "        \"version\": \"V1\",\n" +
            "        \"requestTimestamp\": 1672735268138,\n" +
            "        \"requestId\": \"5402749feac86bff1672735268139\",\n" +
            "        \"clientId\": \"market-app\",\n" +
            "        \"token\": \"{sso}\",\n" +
            "        \"tokenType\": \"SSO\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"ORDERID\": \"{orderId}\"\n" +
            "    }\n" +
            "}";

    public String getRequest()
    {
        return body;
    }


    public  void setRequest(Constants.MerchantType mid, String ORDERID,String token) {
        body = body.replace("{MID}", mid.getId()).replace("{orderId}", ORDERID).replace("{sso}", token);
        }

    public Handlerinternaltxnstatus(Constants.MerchantType mid, String ORDERID,String token)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.TXN_STATUS_APP);
        setRequest(mid,ORDERID,token);
        getRequestSpecBuilder().setBody(getRequest());
    }


}
