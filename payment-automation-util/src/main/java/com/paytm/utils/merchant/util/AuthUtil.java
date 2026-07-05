package com.paytm.utils.merchant.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.ServerUtil;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.auth.*;
import com.paytm.utils.merchant.dto.auth.UserAttributeRequestDTO;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by deepakkumar on 2/4/18.
 */
public class AuthUtil {

    public static synchronized String getSSOToken(String authBaseUri, String mobile, String password) {
        String authorization = "Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2";
        String body = "response_type=code&client_id=paytm-pg-client-staging&scope=paytm&username=" + mobile + "&password=" + password + "&do_not_redirect=true";
        BaseApi api = new Authorize(authBaseUri, authorization, body);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch SSO token, Exception occurred while executing Authorize API", e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();

        } catch (JsonPathException e) {
            throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Authorize API response", e, response);
        }
        String code;
        try {
            code = jsonPath.get("code");
        } catch (JsonPathException e) {
            throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Authorize API response for code", e, response);
        }
        if (code != null && !code.isEmpty()) {
            api = new Token(authBaseUri, authorization, code);
            try {
                response = api.execute();
            } catch (Throwable e) {
                throw new AuthException("Couldn't fetch SSO token, Exception occurred while executing Token API", e);
            }
            try {
                jsonPath = response.jsonPath();
            } catch (JsonPathException e) {
                throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Token API response", e, response);
            }
            String accessToken;
            try {
                accessToken = jsonPath.get("access_token");
            } catch (JsonPathException e) {
                throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Token API response for access-token", e, response);
            }
            if (accessToken == null || accessToken.isEmpty()) {
                throw new AuthException("Couldn't fetch SSO token, SSO Token is either null or empty; SSO Token = " + accessToken);
            }
            Constants.OAUTH_TOKENS.add(accessToken);
            return accessToken;
        } else {
            throw new AuthException("Couldn't fetch SSO token, Code is either null or empty; Code = " + code);
        }
    }

    public static String getTxnToken(String authBaseUri, String mobileNo, String password) {
        return getUserTokens(authBaseUri, mobileNo, password, "txn");
    }

    public static String getPaytmToken(String authBaseUri, String mobileNo, String password) {
        return getUserTokens(authBaseUri, mobileNo, password, "paytm");
    }

    public static String getWalletToken(String authBaseUri, String mobileNo, String password) {
        return getUserTokens(authBaseUri, mobileNo, password, "wallet");
    }

    private static synchronized String getUserTokens(String authBaseUri, String mobileNo, String password, String scope) {
        String debugMsg = "Couldn't fetch User token";
        String ssoToken;
        try {
            ssoToken = getSSOToken(authBaseUri, mobileNo, password);
        } catch (AuthException e) {
            throw new AuthException(debugMsg, e);
        }
        final String authorization = "Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2";
        BaseApi api = new FetchUserTokens(authBaseUri, authorization, ssoToken);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException(debugMsg, e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();

        } catch (JsonPathException e) {
            throw new AuthException(debugMsg, e, response);
        }
        String token;
        try {
            token = jsonPath.param("scope", scope).getList("tokens.findAll { tokens -> tokens.scope == scope }.access_token").get(0).toString();
        } catch (Throwable e) {
            throw new AuthException(debugMsg, e, response);
        }
        Constants.OAUTH_TOKENS.add(token);
        return token;
    }

    public static synchronized String getCustomerID(String authBaseUri, String mobile) {
        String debugMsg = "Couldn't fetch Customer Id";
        String authorization = "Basic bXVrdWwtdGVzdC1jbGllbnQ6UFVqZUdwSWt1S3ZMU0hJVW1WenIwR3NiOXU3VjhFUTk=";
        BaseApi api = new ResourceUser(authBaseUri, authorization, mobile);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException(debugMsg, e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();

        } catch (JsonPathException e) {
            throw new AuthException(debugMsg, e, response);
        }
        String custId = jsonPath.getString("userId");
        if (custId == null) {
            throw new AuthException(debugMsg, response);
        }
        return custId;
    }

    public static synchronized void logout(String authBaseUri, String ssoToken) {
        String debugMsg = "SSO token couldn't get deleted";
        String authorization = "Basic cGF5dG0tcGctY2xpZW50LXN0YWdpbmc6YTc0MjZiZTAtYTJkZC00N2NmLWExODEtYjM3YzgwMWYzNGM2";
        BaseApi api = new DeleteUserTokens(authBaseUri, authorization, ssoToken);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException(debugMsg, e);
        }
        int statusCode = response.getStatusCode();
        if (statusCode != 200) {
            throw new AuthException(debugMsg, response);
        }
    }

    public static boolean isTokenExpired(String authDBUrl, String token) {
        long count;
        try {
            String query = "SELECT COUNT(token) as count FROM expiredaccesstoken WHERE token='" + token + "';";
            List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(authDBUrl, query);
            count = (Long) result.get(0).get("count");
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch token expiry status", e);
        }
        return count != 0;
    }

    public static String getExpiredToken(String authDBUrl) {
        String query = "select token from expiredaccesstoken where scopes='wallet' limit 1;";
        List<Map<String, Object>> result;
        try {
            result = DatabaseUtil.getInstance().executeSelectQuery(authDBUrl, query);
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch expired token", e);
        }
        String expiredToken;
        if (result.size() == 0) {
            throw new AuthException("Expired token not found in DB");
        }
        expiredToken = (String) result.get(0).get("token");
        return expiredToken;
    }

    public static Response fetchUserStrategy(String authBaseUrl, String ssoToken) {
        String authorization = "Basic bWFya2V0LWFwcDo5YTA3MTc2Mi1hNDk5LTRiZDktOTE0YS00MzYxZTdjM2Y0YmM=";
        BaseApi api = new UserV2(authBaseUrl, authorization, ssoToken);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch UserV2 Api response", e);
        }
        return response;
    }

    public static Response fetchUser(String authBaseUrl, String ssoToken) {
        BaseApi api = new User(authBaseUrl, ssoToken);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch User Api response", e);
        }
        return response;
    }

    public static Response setUserAttribute(String authBaseUrl, String custId, UserAttributeRequestDTO userAttributeRequestDTO) {
        String authorization = "Basic cGF5dG0tbG9hbnMtYmFja2VuZC1xYTplNzI5OTM2NS0zZTdiLTQzOGYtODUxOC1jNmU2Mjk5ZWI0MjI=";
        BaseApi api = new UserAttributes(authBaseUrl, custId, authorization, userAttributeRequestDTO);
        Response response = null;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch UserAttribute Api response", e);
        }
        return response;
    }

    /**
     * Fetch otp from pgproxy-notification.log (staging) for provided phone number
     *
     * @param phone
     * @return
     */
    public static String getOtp(String phone) {
        try {
            Thread.sleep(3000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        phone = phone.replace("+91", "");
        String grepCmd = prepareGrepCommand(phone, "sms");
        List<String> otpStringList = getOtpStringList(grepCmd);
        if (otpStringList.isEmpty())
            throw new AuthException("Unable to find OTP from pgproxynotification.logs");
        String otpString = otpStringList.get(otpStringList.size() - 1);
        int startIndex = otpString.indexOf("{ ");
        String jsonString = otpString.substring(startIndex);
        JSONParser jsonParser = new JSONParser();

        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new PGPException("Exception occured while parsing pgproxy json object: ", e);
        }
        String content = getOtpContentString(jsonObject);
        System.out.println(content);
        Pattern pattern = Pattern.compile("[0-9]{6}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String otp = matcher.group();
            if (otp.length() == 6) {
                return otp;
            }
        }
        return "";
    }

    /**
     * Fetch otp from pgproxy-notification.log (staging) for provided email
     *
     * @param email
     * @return
     * @throws AuthException
     */
    public static String getOtpFromEmail(String email) throws AuthException {
        String otp = "";
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException var12) {
            var12.printStackTrace();
        }
        String grepCmd = prepareGrepCommand(email, "mail");
        List<String> otpStringList = getOtpStringList(grepCmd);
        if (otpStringList.isEmpty()) {
            throw new AuthException("Unable to find OTP from pgproxynotification.logs");
        } else {
            String otpString = otpStringList.get(otpStringList.size() - 1);
            int end = 0;
            Pattern pattern = Pattern.compile("(\\s+)([0-9]+)(\\.?)");
            Matcher m = pattern.matcher(otpString);
            while (m.find()) {
                otp = m.group();
                if (otp.length() > 6) {
                    System.out.println(otp.trim().substring(0, 6));
                    return otp.trim().substring(0, 6);
                }
            }
            if (otp.trim().length() < 6) {
                throw new AuthException("Not Recieved OTP on E-mail");
            }
        }
        return null;

    }

    /**
     * @param grepCmd
     * @return list of log line based on provided grap command
     */
    public static List<String> getOtpStringList(String grepCmd) {
        return Awaitility.await()
                .pollDelay(Duration.ONE_SECOND)
                .pollInterval(Duration.ONE_SECOND)
                .atMost(Duration.ONE_MINUTE)
                .until(() -> {
            List<String> otpStringList = new ArrayList<>();
            try {
                ServerUtil serverUtil = new ServerUtil();
                Session session = serverUtil.getSession(Constants.NOTIFICATION_CONNECTION_URL);
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                System.out.println("Command is " + grepCmd);
                channel.setCommand(grepCmd);
                Reporter.report.info(grepCmd);
                channel.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    otpStringList.add(temp);
                }
                channel.disconnect();
                session.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return otpStringList;
        }, otps -> !otps.isEmpty());
    }

    private static String prepareGrepCommand(String text, String otpType) {
        String command = "grep -E 'alipayplus.communication." + otpType + ".send.*ValueToSubstitue'  /paytm/logs/pgproxy-notification.log";
        String commandToExecute = command.replace("ValueToSubstitue", text);
        return commandToExecute;
    }

    private static String getOtpContentString(JSONObject jsonObject) {
        String content = "";
        try {
            JSONObject request = (JSONObject) jsonObject.get("request");
            JSONObject body = (JSONObject) request.get("body");
            content = (String) body.get("content");
        } catch (Exception e) {
            throw new PGPException("Exception occured when parsing pgproxy json object to content string", e);
        }
        return content;
    }


    public static synchronized String getSSOToken(String authBaseUri, String mobile, String password, String clientId, String SecretKey) {

        String body = "response_type=code&client_id=" + clientId + "&scope=paytm&username=" + mobile + "&password=" + password + "&do_not_redirect=true";
        BaseApi api = new Authorize(authBaseUri, clientId, SecretKey, body);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch SSO token, Exception occurred while executing Authorize API", e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();

        } catch (JsonPathException e) {
            throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Authorize API response", e, response);
        }
        String code;
        try {
            code = jsonPath.get("code");
        } catch (JsonPathException e) {
            throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Authorize API response for code", e, response);
        }
        if (code != null && !code.isEmpty()) {
            api = new Token(authBaseUri, clientId, SecretKey, code);
            try {
                response = api.execute();
            } catch (Throwable e) {
                throw new AuthException("Couldn't fetch SSO token, Exception occurred while executing Token API", e);
            }
            try {
                jsonPath = response.jsonPath();
            } catch (JsonPathException e) {
                throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Token API response", e, response);
            }
            String accessToken;
            try {
                accessToken = jsonPath.get("access_token");
            } catch (JsonPathException e) {
                throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing Token API response for access-token", e, response);
            }
            if (accessToken == null || accessToken.isEmpty()) {
                throw new AuthException("Couldn't fetch SSO token, SSO Token is either null or empty; SSO Token = " + accessToken);
            }
            Constants.OAUTH_TOKENS.add(accessToken);
            return accessToken;
        } else {
            throw new AuthException("Couldn't fetch SSO token, Code is either null or empty; Code = " + code);
        }
    }

    public static synchronized String getUserInfoWithSSOAndMid(String authBaseUri, String SSOToken,String pgmid) {

        String authorization = "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=";

        BaseApi api = new UserV2Mid(authBaseUri, authorization, SSOToken,pgmid);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new AuthException("Couldn't fetch MobileNumber, Exception occurred while executing User/V2/MID API", e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();

        } catch (JsonPathException e) {
            throw new AuthException("Couldn't fetch SSO token, Exception occurred while parsing User/V2/MID API", e, response);
        }
        String message;
        if(response.getStatusCode()==200){
            try {
                message = jsonPath.get("basicInfo").toString();
            } catch (JsonPathException e) {
                throw new AuthException("Couldn't fetch Mobile Number, Exception occurred while parsing User/V2/MID API", e, response);
            }
        }else{
            try {
                message = jsonPath.get("message").toString();
            } catch (JsonPathException e) {
                throw new AuthException("Couldn't fetch Response Message, Exception occurred while parsing User/V2/MID API", e, response);
            }
        }
        return message;
    }

    public static String getSmsContent(String phone) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException var13) {
            var13.printStackTrace();
        }

        phone = phone.replace("+91", "");
        String grepCmd = prepareGrepCommand(phone, "sms");
        List<String> otpStringList = AuthUtil.getOtpStringList(grepCmd);
        if (otpStringList.isEmpty()) {
            throw new AuthException("Unable to find OTP from pgproxynotification.logs");
        } else {
            String otpString = (String)otpStringList.get(otpStringList.size() - 1);
            int startIndex = otpString.indexOf("{ ");
            String jsonString = otpString.substring(startIndex);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = null;

            try {
                jsonObject = (JSONObject)jsonParser.parse(jsonString);
            } catch (ParseException var12) {
                throw new PGPException("Exception occured while parsing pgproxy json object: ", var12);
            }

            String content = getOtpContentString(jsonObject);
            System.out.println(content);

            return content;
        }
    }
}