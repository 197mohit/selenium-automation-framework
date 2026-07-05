package com.paytm.apphelpers;

import com.paytm.LocalConfig;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.test.BaseTest;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.auth.FetchUserTokens;
import com.paytm.utils.merchant.util.AuthUtil;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import com.paytm.appconstants.Constants.*;
import java.util.HashSet;
import java.util.Set;

import static com.paytm.framework.reporting.Reporter.report;

public class AuthHelpers extends BaseApi {
    public static Set<String> tokens = new HashSet<>();
    @Step("Get SSO Token for mobile: {0}")
    public static synchronized String getSSOToken(String mobile, String password) throws AuthException {
        try {
            DriverManager.setCaptureScreenShot(false);
            String ssoToken = AuthUtil.getSSOToken(LocalConfig.AUTH_HOST, mobile, password);
//            report.info("Customer ID is: " + ssoToken);
            tokens.add(ssoToken);
            tokens.addAll(Constants.OAUTH_TOKENS);
            return ssoToken;
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static Response getUserTokens(String authBaseUri, String ssoToken){
        String authorization = "Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2";
        FetchUserTokens fetchUserTokens = new FetchUserTokens(LocalConfig.AUTH_HOST,authorization,ssoToken);
        Response response = fetchUserTokens.execute();
        return response;
    }

    @Step("Get Customer Id for mobile: {0}")
    public static synchronized String getCustomerID(String mobile) throws AuthException {
        try {
//            report.info("Get Customer Id for mobile: " + mobile);
            DriverManager.setCaptureScreenShot(false);
            String custID = AuthUtil.getCustomerID(LocalConfig.AUTH_HOST, mobile);
//            report.info("Customer ID is: " + custID);
            return custID;
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    @Step("Log out of session: {0}")
    public static synchronized void logout(String ssoToken) throws AuthException {
        try {
//            report.info("Log out of session: " + ssoToken);
            DriverManager.setCaptureScreenShot(false);
            AuthUtil.logout(LocalConfig.AUTH_HOST, ssoToken);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static String getTxnToken(String mobile, String password) throws AuthException {
        try {
            DriverManager.setCaptureScreenShot(false);
            return AuthUtil.getTxnToken(LocalConfig.AUTH_HOST, mobile, password);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }

    }

    public static String getPaytmToken(String mobile, String password) throws AuthException {
        try {
            DriverManager.setCaptureScreenShot(false);
            return AuthUtil.getPaytmToken(LocalConfig.AUTH_HOST, mobile, password);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static String getWalletToken(String mobile, String password) throws AuthException {
        try {
            DriverManager.setCaptureScreenShot(false);
            return AuthUtil.getWalletToken(LocalConfig.AUTH_HOST, mobile, password);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }


    public static boolean isTokenExpired(String token) throws AuthException {
        try {
            report.info("Check if token: " + token + "  has expired");
            DriverManager.setCaptureScreenShot(false);
            return AuthUtil.isTokenExpired(LocalConfig.AUTH_DB_CONNECTION_URL, token);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static String getExpiredToken() throws AuthException {
        try {
            DriverManager.setCaptureScreenShot(false);
            return AuthUtil.getExpiredToken(LocalConfig.AUTH_DB_CONNECTION_URL);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }


    @Step("Get SSO Token for mobile: {0}")
    public static synchronized String getSSOToken(String mobile, String password,String clientId,String SecretKey) throws AuthException {
        try {
            DriverManager.setCaptureScreenShot(false);
             return AuthUtil.getSSOToken(LocalConfig.AUTH_HOST, mobile, password,clientId,SecretKey);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }


    @Step("Get SSO Token for mobile: {0}")
    public static synchronized  String UserInfoWithSSOAndMid(String SSOToken,String Mid) throws AuthException {
        try {
            DriverManager.setCaptureScreenShot(false);
            return AuthUtil.getUserInfoWithSSOAndMid(LocalConfig.AUTH_HOST, SSOToken, Mid);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    // register new user in auth
    private String authRegisterUser(String mobileNumber, String password) {

        String authorization = "Basic YXdhbmlzaHNhbmRib3hvbmUxOlpVR2xhUWF1cVoxOVllSEtHTXE2M2NNa01NRzROd0w5";

        String body = "{ \"email\": \"\",\n" + "  \"mobile\": \"" + mobileNumber + "\",\n" + "  \"loginPassword\": \""
                + password + "\",\n" + "  \"doNotRedirect\": \"true\",\n" + "  \"clientId\": \"awanishsandboxone1\",\n"
                + "  \"scope\": \"paytm\",\n" + "  \"state\": \"null\",\n" + "  \"responseType\": \"code\",\n"
                + "  \"theme\": \"mp-web\",\n" + "  \"dob_agreement\": \"true\"}";
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("Authorization", authorization);
        getRequestSpecBuilder().setBaseUri(LocalConfig.AUTH_HOST);
        getRequestSpecBuilder().setBasePath(AuthAPIresource.REGISTERUSER);
        getRequestSpecBuilder().setBody(body);
        Response response = execute();
        String status = response.jsonPath().getString("status");
        Assert.assertEquals(status, "SUCCESS");
        String signupToken = response.jsonPath().get("signupToken");
        // System.out.println("code--"+signupToken);
        return signupToken;

    }

    public void authCreateNewUser(String mobileNumber, String password) {

        String signuptoken = this.authRegisterUser(mobileNumber, password);
        System.out.println("token--" + signuptoken);
        String body = "{\"otp\": \"888888\",\n" + " \"signupToken\": \"" + signuptoken + "\",\n"
                + " \"responseCode\": \"01\",\n" + " \"userData\": {\n" + " \"gender\": \"male\"\n" + "  }\n" + "}";
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.AUTH_HOST);
        getRequestSpecBuilder().setBasePath(AuthAPIresource.VALIDATEREGISTERUSER);
        getRequestSpecBuilder().setBody(body);
        Response response = execute();
        response.prettyPrint();
        String status = response.jsonPath().getString("status");
        Assert.assertEquals(status, "SUCCESS");
    }

}

