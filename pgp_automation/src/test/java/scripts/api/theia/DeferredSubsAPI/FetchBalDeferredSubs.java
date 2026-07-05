package scripts.api.theia.DeferredSubsAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchBalDeferredSubs extends BaseApi {
    String request = "{" +
            "\"head\": " +
            "" +
            "{ \"tokenType\": \"ACCESS\", " +
            "\"version\": \"v1\", " +
            "\"channelId\": \"WEB\", " +
            "\"requestTimestamp\": \"Time\", " +
            "\"token\": \"{accessTokenValue}\"}, "+
            "\"body\":{\"mid\":\"{mid}\"," +
            "\"paymentMode\": \"BALANCE\", " +
            "}"+
            "}";

    public String getRequest(){
        return request;
    }

    public FetchBalDeferredSubs(String MID, String accessTokenValue) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_BALANCE);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("referenceId", "REFID1111");
        request = request.replace("{accessTokenValue}",accessTokenValue).replace("{mid}", MID);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
