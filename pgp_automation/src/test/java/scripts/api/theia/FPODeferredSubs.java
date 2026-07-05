package scripts.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FPODeferredSubs extends BaseApi {

    String request="{" +
            "\"head\": " +
            "" +
            "{ \"requestTimestamp\": \"1539601338741\", " +
            "\"version\": \"v5\", " +
            "\"channelId\": \"WAP\", " +
            "\"tokenType\": \"SSO\", " +
            "\"token\": \"{sso}\"}" +
            "," +
            "\"body\":{\"mid\":\"{mid}\"," +
            "\"subscriptionTransactionRequestBody\":" +
            "{\"subscriptionAmountType\":\"VARIABLE\", " +
            "\"subscriptionStartDate\":\"{startdate}\", " +
            "\"subscriptionExpiryDate\":\"{enddate}\", " +
            "\"subscriptionFrequency\":\"0\", " +
            "\"subscriptionFrequencyUnit\":\"ONDEMAND\", " +
            "\"subscriptionGraceDays\":\"0\", " +
            "\"subscriptionEnableRetry\":\"0\", " +
            "\"subscriptionRetryCount\":\"5\", " +
            "\"mandateType\":\"E_MANDATE\", " +
            "\"subscriptionMaxAmount\":\"15000\", " +
            "\"referenceIdValue\":\"REFID1111\", " +
            "\"userInfo\":{\"custId\":\"1000877505\"}, " +
            "\"requestType\":\"NATIVE_SUBSCRIPTION\", " +
            "\"txnAmount\":" +
            "{\"value\":\"200\", " +
            "\"currency\":\"INR\"}" +
            "}"+
            "}"+
            "}";

    public String getRequest(){
        return request;
    }

    public FPODeferredSubs(String mid, String sso) {
        APIBuilder:
        {
            setMethod(MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBody(getRequest());
            getRequestSpecBuilder().addQueryParam("mid", mid);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V5);
            getRequestSpecBuilder().addHeader("Content-Type", "application/json");
            request = request.replace("{sso}", sso).replace("{mid}", mid)
                    .replace("{startdate}", "2023-07-10")
                    .replace("{enddate}", "2026-02-27");
            getRequestSpecBuilder().setBody(getRequest());
        }
    }
         public FPODeferredSubs(String mid, String sso, String startDate, String endDate)
        {
            APIBuilder:
            {
                setMethod(MethodType.POST);
                getRequestSpecBuilder().setContentType(ContentType.JSON);
                getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
                getRequestSpecBuilder().setBody(getRequest());
                getRequestSpecBuilder().addQueryParam("mid", mid);
                getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V5);
                getRequestSpecBuilder().addHeader("Content-Type","application/json");
                request = request.replace("{sso}",sso).replace("{mid}", mid)
                        .replace("{startdate}",startDate)
                        .replace("{enddate}",endDate);
                getRequestSpecBuilder().setBody(getRequest());
            }
    }
}
