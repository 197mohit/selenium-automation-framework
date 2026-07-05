package scripts.api.theia.DeferredSubsAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ValidateVPADeferredSubs extends BaseApi{

    String request = "{" +
            "\"head\": " +
            "" +
            "{ \"requestTimestamp\": \"1539601338741\", " +
            "\"version\": \"v1\", " +
            "\"channelId\": \"WAP\", " +
            "\"tokenType\": \"ACCESS\", " +
            "\"token\": \"{accessTokenValue}\"}, "+
            "\"body\":{\"mid\":\"{mid}\"," +
            "\"vpa\":\"9999661503@paytm\", " +
            "\"orderId\":\"{orderId}\", " +
            "}"+
            "}";

    public String getRequest(){
        return request;
    }

    public ValidateVPADeferredSubs(String MID, String OrderID, String accessTokenValue) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.VPA_VALIDATE);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("referenceId", "REFID1111");
        request = request.replace("{mid}",MID).replace("{orderId}",OrderID).replace("{accessTokenValue}",accessTokenValue);
        getRequestSpecBuilder().setBody(getRequest());
    }

}
