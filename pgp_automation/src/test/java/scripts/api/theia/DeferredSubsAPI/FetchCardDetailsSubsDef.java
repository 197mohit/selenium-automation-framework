package scripts.api.theia.DeferredSubsAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.bouncycastle.jcajce.provider.symmetric.Grainv1;

public class FetchCardDetailsSubsDef extends BaseApi{

    String request = "{" +
            "\"head\": " +
            "" +
            "{ \"requestTimestamp\": \"1539601338741\", " +
            "\"version\": \"v1\", " +
            "\"channelId\": \"WAP\", " +
            "\"tokenType\": \"ACCESS\", " +
            "\"token\": \"{accessTokenValue}\"}, "+
            "\"body\":{\"mid\":\"{mid}\"," +
            "\"cardNumber\":\"4761360075860519\", " +
            "\"eightDigitBinRequired\":\"false\", " +
            "}"+
            "}";

    public String getRequest(){
        return request;
    }

    public FetchCardDetailsSubsDef(String MID, String accessTokenValue) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_CARD_DETAILS);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("referenceId", "REFID1111");
        request = request.replace("{mid}",MID).replace("{accessTokenValue}",accessTokenValue);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
