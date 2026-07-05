package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;


/**
 * Created by anjukumari on 26/03/18
 */
public class ValidatePassCode extends BaseApi{

    private static String loginSecret = "Wiso8e3HPMIAf9JhMP0BvSeQpX18XziUxCHcR91zqGZgS1jNCeWu/R33rJLwmaVS0SHPRN3YH8qrBzUVItvdPg==";

    public  ValidatePassCode(User user, String scope) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(LocalConfig.AUTH_HOST);
        getRequestSpecBuilder().addParam("grant_type", "password");
        getRequestSpecBuilder().addParam("login_id", user.mobNo());
        getRequestSpecBuilder().addParam("login_secret", loginSecret);
        getRequestSpecBuilder().addParam("scope", scope);
        getRequestSpecBuilder().addParam("login_id_type", "phone");
        getRequestSpecBuilder().addParam("login_secret_type", "passcode");
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("authorization", "Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2");
        getRequestSpecBuilder().setBasePath("/oauth2/token");
    }


    public static String accessTokenFromPasscode(ValidatePassCode validatePassCode){
        Response ValidatePassCodeResponse = validatePassCode.execute();
        JsonPath path = ValidatePassCodeResponse.jsonPath();
        String PasscodeToken = path.get("access_token").toString();
        return PasscodeToken;
    }


}
