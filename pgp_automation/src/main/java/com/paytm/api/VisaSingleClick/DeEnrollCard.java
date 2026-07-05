package com.paytm.api.VisaSingleClick;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

public class DeEnrollCard extends BaseApi {

    String body = "{\n" +
            "    \"head\": {\n" +
            "        \"requestId\":\"123\",\n" +
            "        \"token\": \"{TOKEN}\",\n" +
            "        \"tokenType\": \"{TOKENTYPE}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "    \t\"deEnrollContent\":\n" +
            "    \t\t{\n" +
            "       \"bin\":\"{BIN}\",\n" +
            "       \"custId\":\"{CUSTID}\",\n" +
            "       \"cardAlias\":\"{CARDALIAS}\",\n" +
            "       \"appId\":\"{APPID}\"\n" +
            "    \t\t}\n" +
            "  }\n" +
            "}";


    String bodyforchecksum="{\"deEnrollContent\":{\"bin\":\"{BIN}\",\"custId\":\"{CUSTID}\",\"cardAlias\":\"{CARDALIAS}\",\"appId\":\"{APPID}\"}}";

    public String getRequest()
    {
        return body;
    }

public String setBinCustIdCardAlias(String bin, String custId, String cardAlias, String appId){
        bodyforchecksum= bodyforchecksum.replace("{BIN}", bin)
                .replace("{CUSTID}",custId ).replace("{CARDALIAS}", cardAlias).replace("{APPID}",appId);
        return bodyforchecksum;
}

    public  void setRequest(String token, String tokenType, String bin, String custId, String cardAlias , String appId){

        body = body.replace("{TOKEN}", token).replace("{TOKENTYPE}", tokenType).replace("{BIN}", bin)
                .replace("{CUSTID}",custId ).replace("{CARDALIAS}", cardAlias).replace("{APPID}",appId);
    }

    public DeEnrollCard(String token, String tokenType, String bin, String custId, String cardAlias, Constants.MerchantType mid, String referenceId, String orderId, String appId){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/api/v1/card/deEnroll");
        if (token=="" || token==null) {
            String bodychecksum = setBinCustIdCardAlias(bin, custId, cardAlias,appId);
            System.out.println("bodyforchecksum="+bodychecksum);
            String token1 = PGPUtil.getChecksum(mid.getKey(), bodychecksum);
            setRequest(token1,tokenType,bin,custId,cardAlias,appId);
        }
        else {
            setRequest(token, tokenType, bin, custId, cardAlias,appId);
        }
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addQueryParam("mid", mid.getId());
        getRequestSpecBuilder().addQueryParam("referenceId", referenceId);
        if (orderId!="" || orderId!=null) {
            getRequestSpecBuilder().addQueryParam("orderId", orderId);
        }

    }
    }




