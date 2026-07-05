package scripts.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class InitiateSubsDeferred extends BaseApi {

    String request="{" +
            "\"head\": " +
            "" +
            "{ \"requestTimestamp\": \"1539601338741\", " +
            "\"version\": \"v1\", " +
            "\"channelId\": \"WAP\", " +
            "\"clientId\": \"C11\", " +
            "\"signature\": \"\"}, " +
            "\"body\":{\"mid\":\"{mid}\"," +
            "\"orderId\":\"{orderId}\", " +
            "\"websiteName\":\"retail\", " +
            "\"txnAmount\":" +
            "{\"value\":\"300\", " +
            "\"currency\":\"INR\"}, " +
            "\"subscriptionPaymentMode\":\"\", " +
            "\"subscriptionAmountType\":\"VARIABLE\", " +
            "\"subscriptionStartDate\":\"{startDate}\", " +
            "\"subscriptionExpiryDate\":\"2029-02-27\", " +
            "\"subscriptionFrequency\":\"1\", " +
            "\"subscriptionFrequencyUnit\":\"MONTH\", " +
            "\"subscriptionGraceDays\":\"0\", " +
            "\"subscriptionEnableRetry\":\"0\", " +
            "\"subscriptionRetryCount\":\"5\", " +
            "\"mandateType\":\"E_MANDATE\", " +
            "\"subscriptionMaxAmount\":\"15000\", " +
            "\"referenceIdValue\":\"REFID1111\", " +
            "\"accessTokenValue\":\"{accessTokenValue}\", " +
            "\"userInfo\":{\"custId\":\"1000877505\"}, " +
            "\"paytmSsoToken\":\"{sso}\"," +
            "\"mandateAccountDetails\":" +
            "{\"accountHolderName\":\"Akshat Sharma\", " +
            "\"channelCode\":\"PPBL\"," +
            "\"accountNumber\":\"915445500424\"," +
            "\"ifsc\":\"PYTM0000001\"," +
            "\"accountType\":\"Savings\"}" +
            "}"+
            "}";

    public String getRequest(){
        return request;
    }
    public InitiateSubsDeferred(String MID, String OrderID, String accessTokenValue, String startDate, String sso) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_SUBSCRIPTION);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("orderId", OrderID);
        request = request.replace("{mid}",MID).replace("{orderId}",OrderID).replace("{accessTokenValue}",accessTokenValue).replace("{startDate}",startDate).replace("{sso}",sso);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
