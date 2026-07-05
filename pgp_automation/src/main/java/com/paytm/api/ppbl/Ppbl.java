package com.paytm.api.ppbl;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.CustomRequestSpecBuilder;
import io.restassured.http.ContentType;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Created by anjukumari on 03/05/18
 */
public class Ppbl extends BaseApi{
    private String checkBalancePath = "v2/accounts/{accountNum}/balance";
    private String ppblAddMoneyPath = "v2/transactions/add-money";


    public  BaseApi CheckBalance(User user){
    String accountNum = "91"+user.mobNo();
    setMethod(MethodType.GET);
    String request_token = RandomStringUtils.randomNumeric(10);
    String checkBalancePathNew = checkBalancePath.replace("{accountNum}",accountNum);
        CustomRequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setBaseUri(LocalConfig.PPBL_URL);
        requestSpecBuilder.setBasePath(checkBalancePathNew);
        requestSpecBuilder.addHeader("Request-Token", request_token);
        return this;
    }

    public BaseApi ppbl_addMoney(User user, double amount) {
        String sourceAccount = "919250050151";
        String targetAccount = "91"+user.mobNo();
        String newAmount = String.valueOf(amount);
        String request_token = RandomStringUtils.randomNumeric(10);
        setMethod(MethodType.POST);
        CustomRequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PPBL_URL);
        requestSpecBuilder.setBasePath(ppblAddMoneyPath + "?requestToken="+request_token);
        requestSpecBuilder.addHeader("Request-Token", request_token);
        requestSpecBuilder.setContentType(ContentType.JSON);

        return this;
    }
}
