package scripts.api.theia.DeferredSubsAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchNBChannelsDefSubs extends BaseApi {

    String request = "{" +
            "\"head\": " +
            "" +
            "{ \"requestTimestamp\": \"1539601338741\", " +
            "\"version\": \"v1\", " +
            "\"channelId\": \"WAP\", " +
            "\"tokenType\": \"ACCESS\", " +
            "\"token\": \"{accessTokenValue}\"}, "+
            "\"body\":{\"mid\":\"{mid}\"," +
            "\"type\":\"MERCHANT\", " +
            "}"+
            "}";

    public String getRequest(){
        return request;
    }

    public FetchNBChannelsDefSubs(String MID, String accessTokenValue) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_NB_PAYMENT_CHANNELS);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("referenceId", "REFID1111");
        request = request.replace("{mid}",MID).replace("{accessTokenValue}",accessTokenValue);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
