package com.paytm.api.subscription;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class HDFCIntentPay extends BaseApi {
    String hdfcIntentPayRequest= "{\n" +
            "\"instaUrl\":\"https://pgp-ite.paytm.in/instaproxy/bankresponse/HDFC/UPI/RESP\",\n" +
            "\"deeplink\":\"upi://mandate?pa=paytm01@hdfcbank&pn=Chandraeesssss&mc=6012&tid=HDFC4061025691482614785&tr=PAYTMSUBS4061025691482614785&tn=Amount%20to%20be%20paid%20now%20is%20Rs%201.00&am=10.00&cu=INR&mode=04&purpose=14&orgid=000000&validitystart=10062024&validityend=28052025&amrule=MAX&recur=MONTHLY&recurvalue=10&recurtype=AFTER&rev=Y&fam=1.00&txnType=CREATE&block=N\",\n" +
            "\"orderId\":\"{{orderId}}\"\n" +
            "}";

    public HDFCIntentPay setInstaURL(String instaUrl) {
        setContext("instaUrl",instaUrl);
        return this;
    }

    public HDFCIntentPay setDeeplink(String deeplink){
        setContext("deeplink",deeplink);
        return this;
    }

    public HDFCIntentPay setOrderId(String orderId) {
        setContext("orderId",orderId);
        return this;
    }

    public HDFCIntentPay() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.SubscriptionService.HDFC_INTENT_PAY);
    }

    public String getRequest() {return hdfcIntentPayRequest;}
}
