package scripts.api.theia.DeferredSubsAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ValidateOTPDeferredSubs extends BaseApi{

    String request = "{" +
            "\"head\": " +
            "" +
            "{ \"tokenType\": \"ACCESS\", " +
            "\"version\": \"v1\", " +
            "\"channelId\": \"WAP\", " +
            "\"requestTimestamp\": \"1688654481579\", " +
            "\"Content-Type\": \"application\\/json\", " +
            "\"token\": \"{accessTokenValue}\"}, "+
            "\"body\":{\"mid\":\"{mid}\"," +
            "\"otp\": \"{otp}\", " +
            "}"+
            "}";

    public String getRequest(){
        return request;
    }

    public ValidateOTPDeferredSubs(String MID, String accessTokenValue, String otp) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.VALIDATE_OTP);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("referenceId", "REFID1111");
        request = request.replace("{accessTokenValue}",accessTokenValue).replace("{mid}", MID).replace("{otp}", otp);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
