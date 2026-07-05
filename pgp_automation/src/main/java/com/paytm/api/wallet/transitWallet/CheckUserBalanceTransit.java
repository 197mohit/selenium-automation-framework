package com.paytm.api.wallet.transitWallet;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CheckUserBalanceTransit extends BaseApi {

    String request = "{\n" +
            "    \"request\": {\n" +
            "        \"isDetailInfo\": \"yes\",\n" +
            "        \"isClubSubwalletsRequired\": \"no\",\n" +
            "        \"computeAddableAmount\": \"true\"  \n" +
            "          }\n" +
            "}";

    public String getRequest()
    {
        return request;
    }

    public CheckUserBalanceTransit setRequest(String request)
    {
        this.request = request;
        return this;
    }

    public CheckUserBalanceTransit(User user)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("ssotoken",user.ssoToken());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        getRequestSpecBuilder().setBasePath(Constants.WalletAPIResourcePath.TRANSIT_CHECKBALANCE);

        getRequestSpecBuilder().setBody(getRequest());
    }

}
