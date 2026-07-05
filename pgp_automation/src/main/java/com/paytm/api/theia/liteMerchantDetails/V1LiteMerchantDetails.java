package com.paytm.api.theia.liteMerchantDetails;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.assertj.core.api.SoftAssertions;


public class V1LiteMerchantDetails extends BaseApi {
     public static String bodyWithAll="{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"10.41.0\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"requestId\": \"1721127727\",\n" +
            "        \"requestTimestamp\": \"1544614590000\",\n" +
            "        \"tokenType\": \"JWT\",\n" +
            "        \"token\": \"{token}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{mid}\",\n" +
            "        \"orderId\": \"{orderId}\",\n" +
            "        \"merchantVpa\": \"{merchantVpa}\"\n" +
            "    }\n" +
            "}";

    public void setRequest(Constants.MerchantType mid, String orderId ,String merchantVpa ,String token)
    {
        bodyWithAll = bodyWithAll.replace("{mid}", mid.getId()).replace("{orderId}",orderId).replace("{merchantVpa}",merchantVpa).replace("{token}",token);
    }

public V1LiteMerchantDetails(String body)
{
    setMethod(BaseApi.MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.V1_Lite_MerchantDetails);
    getRequestSpecBuilder().setBody(body);
    getRequestSpecBuilder().setBody(getRequest());
}

    public String getRequest()
    {
        return bodyWithAll;
    }
}
