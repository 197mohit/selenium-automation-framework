package scripts.api.theia.DeferredSubsAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SendOTPDeferredSubs extends BaseApi{
    String request = "{" +
            "\"head\": " +
            "" +
            "{ \"tokenType\": \"ACCESS\", " +
            "\"token\": \"{accessTokenValue}\"}, "+
            "\"body\":{\"mobileNumber\":\"{mob}\"," +
            "}"+
            "}";

    public String getRequest(){
        return request;
    }

    public SendOTPDeferredSubs(String MID, String accessTokenValue, String user) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.SEND_OTP);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("referenceId", "REFID1111");
        request = request.replace("{accessTokenValue}",accessTokenValue).replace("{mob}", user);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
