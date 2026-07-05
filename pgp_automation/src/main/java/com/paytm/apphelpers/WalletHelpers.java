package com.paytm.apphelpers;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.api.wallet.CheckBalance;
import com.paytm.utils.merchant.util.WalletUtil;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import com.paytm.utils.merchant.util.exception.walletException.WalletException;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;

import java.text.DecimalFormat;

import static com.paytm.framework.reporting.Reporter.report;

public class WalletHelpers {

    public static double getWalletBalance(User user) throws WalletException, AuthException {
        report.info("Get wallet balance for user: " + user);
        double walletBalance = 0;
        try {
            String ssoToken = user.ssoToken();
            DriverManager.setCaptureScreenShot(false);
            walletBalance = WalletUtil.getWalletBalance(LocalConfig.WALLET_HOST, ssoToken);
            report.info("Wallet balance is: " + walletBalance);
            return walletBalance;
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static String getWalletType(User user) {
        String debugMsg = "Wallet balance could not be fetched successfully";
        CheckBalance checkBalance = new CheckBalance(LocalConfig.WALLET_HOST, user.ssoToken());
        String walletGrade = null;
        int count = 0;
        int maxRetryCount = 3;

        Response response;
        for (response = null; count < maxRetryCount; ++count) {
            try {
                response = checkBalance.execute();
            } catch (Throwable ex) {
                throw new WalletException(debugMsg, ex);
            }

            JsonPath jsonPath;
            try {
                jsonPath = response.jsonPath();
            } catch (JsonPathException ex) {
                throw new WalletException(debugMsg, ex, response);
            }

            if ("SUCCESS".equalsIgnoreCase(jsonPath.get("statusCode"))) {
                walletGrade = jsonPath.getString("response.walletGrade");
                break;
            }
        }

        if (count == maxRetryCount) {
            throw new WalletException(debugMsg, response);
        } else {
            return walletGrade;
        }
    }

    public static void blockAmount(User user, double amount, String merchantKey) throws WalletException, AuthException {
        report.info("Block amount: " + amount + " for user: " + user);
        DriverManager.setCaptureScreenShot(false);
        WalletUtil.blockAmount(LocalConfig.WALLET_HOST, user.walletToken(), user.custId(), amount, merchantKey);
        report.info("Amount blocked successfully");
        DriverManager.setCaptureScreenShot(true);
    }

    @Step("Modify wallet balance of {0} with amount {1}")
    public static void modifyBalance(User user, Double amountToBeRetainedInWallet) throws WalletException, AuthException {
        report.info("Set wallet balance to: " + amountToBeRetainedInWallet + " for user " + user);
        DriverManager.setCaptureScreenShot(false);
        double currentBalance = getWalletBalance(user);
        String mobile = user.mobNo();
        String walletScopeToken = user.walletToken();
        WalletUtil.modifyBalance(LocalConfig.WALLET_HOST, mobile, walletScopeToken, amountToBeRetainedInWallet);
        report.info("Wallet balance is set successfully");
        DriverManager.setCaptureScreenShot(true);
    }

    private static void addBalanceInGV(User user, Double amountToBeRetainedInWallet) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/service/checkUserBalance");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().addHeader("ssotoken", user.ssoToken());
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"is_detailinfo\":\"yes\" } }");
        Response response = baseApi.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Double subWalletBalance = 0.0d;
        try {
            subWalletBalance = Double.parseDouble(response.jsonPath().getString("response.subWalletDetailsList.findAll{subWalletDetailsList->subWalletDetailsList.subWalletName == 'GIFT_VOUCHER'}.balance[0]"));
        } catch (NullPointerException e) {
            //throw new WalletException("GV Balance is 0", response);
            System.out.println("GV Balance is 0");
        }
        WalletHelpers.addAmtToGiftVoucher(user, amountToBeRetainedInWallet);
        //   WalletUtil.modifyBalance(LocalConfig.WALLET_HOST,user.mobNo(),user.walletToken(),amountToBeRetainedInWallet);

    }

    public void addAmountInGV(String walletBaseUri, String mobileNumber, String walletToken, Double amountToBeRetainedInWallet) {

    }

    public static void validateBalance(User user, double expectedWalletBal) throws WalletException, AuthException {
        report.info("Validating wallet balance for user " + user + " is equals to: " + expectedWalletBal);
        DriverManager.setCaptureScreenShot(false);
        String walletScopeToken = user.walletToken();
        try {
            WalletUtil.validateBalance(LocalConfig.WALLET_HOST, walletScopeToken, expectedWalletBal);
        } catch (WalletException e) {
            if (StringUtils.equalsIgnoreCase("Wallet balance is not as expected", e.getMessage())) {
                throw new AssertionError(e.getMessage());
            } else {
                throw e;
            }
        }
        report.info("Validation successful, actual wallet balance is equal to expected balance");
        DriverManager.setCaptureScreenShot(true);
    }

    @Step
    public static void  setZeroBalance(User user) throws WalletException, AuthException {
        modifyBalance(user, 0.0);
    }

    @Step
    public static void breachAddMoneyLimit(User user) throws WalletException, AuthException {
        report.info("Breach add money limit for user: " + user);
        WalletUtil.breachAddMoneyLimit(Constants.DBConnectionURL.WALLET_DB_CONNECTION_URL, user.custId());
    }

    @Step
    public static void setLimitAuditInfoDefault(User user) throws WalletException, AuthException {
        report.info("Set Limit Audit Info to default for user: " + user);
        WalletUtil.setLimitAuditInfoDefault(Constants.DBConnectionURL.WALLET_DB_CONNECTION_URL, user.custId());
    }

    public static double getFoodWalletBalance(User user) throws AuthException {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/service/checkUserBalance");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().addHeader("ssotoken", user.ssoToken());
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"is_detailinfo\":\"yes\" } }");
        Response response = baseApi.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        return Double.parseDouble(response.jsonPath().getString("response.subWalletDetailsList.findAll{subWalletDetailsList->subWalletDetailsList.subWalletName == 'FOOD'}.balance[0]"));
    }


    public static double getGVBalance(User user) throws AuthException {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/service/checkUserBalance");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().addHeader("ssotoken", user.ssoToken());
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"is_detailinfo\":\"yes\" } }");
        Response response = baseApi.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Double gvBalance = 0.0d;
        try {
            gvBalance = Double.parseDouble(response.jsonPath().getString("response.subWalletDetailsList.findAll{subWalletDetailsList->subWalletDetailsList.subWalletName == 'GIFT_VOUCHER'}.balance[0]"));
        } catch (NullPointerException e) {
            System.out.println("GV Balance is zero");
        }
        return gvBalance;
    }



    public static void updateFoodWalletBalance(User user, double amount) throws AuthException {
        double currentBalance = getFoodWalletBalance(user);
        double amt = amount - currentBalance;
        DecimalFormat df = new DecimalFormat("#.##");
        amt = Double.valueOf(df.format(amt));
        if (amt > 0.0D && amt < 1.0D) {
            addAmtToFoodWallet(user, 1.0D + amt);
            withdrawAmtFromFoodWallet(user, 1.0D);
        } else if (amt >= 1.0D) {
            addAmtToFoodWallet(user, amt);
        } else if (amt < 0.0D) {
            withdrawAmtFromFoodWallet(user, -amt);
        }
        if (amt != 0.0D) {
            currentBalance = getFoodWalletBalance(user);
            if (Double.compare(currentBalance, amount) != 0)
                throw new SkipException("update to food wallet balance to: " + amount + "is failed, current food wallet balance is: " + currentBalance);
        }
    }


    public static void updateGVBalance(User user, double amount) throws AuthException {
        double currentBalance = getGVBalance(user);
        double amt = amount - currentBalance;
        DecimalFormat df = new DecimalFormat("#.##");
        amt = Double.valueOf(df.format(amt));
        if (amt > 0.0D && amt < 1.0D) {
            addBalanceInGV(user, 1.0D + amt);
            withdrawAmtFromGV(user, 1.0D);
        } else if (amt >= 1.0D) {
            addBalanceInGV(user, amt);
        } else if (amt < 0.0D) {
            withdrawAmtFromGV(user, -amt);
        }
        if (amt != 0.0D) {
            currentBalance = getGVBalance(user);
           /* if (Double.compare(currentBalance, amount) != 0)
                throw new SkipException("update to Gift voucher balance to: " + amount + "is failed, current GV wallet balance is: " + currentBalance);*/
        }

        if(currentBalance!=amount){
            throw new WalletException("GV Wallet amount could not be updated.");
        }
    }



    private static void addAmtToFoodWallet(User user, double amount) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/wallet-web/addFundsToSubWallet");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
//        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"merchantGuid\": \"7AA1F178-90B4-48A6-8F03-CFDA4B820950\", \"merchantOrderId\": \""+CommonHelpers.generateOrderId()+"\", \"merchantSubWalletGuid\":\"D6665D35-5ADD-4953-8227-C33E328C3071\", \"payeeEmailId\": \"\", \"payeeSsoId\": \"\", \"payeePhoneNumber\":\""+user.mobNo()+"\", \"activationDate\":null, \"expiryDate\":null, \"amount\":"+amount+", \"currencyCode\": \"INR\", \"userSubWalletType\":\"FOOD\" }, \"metadata\": \"Testing\", \"ipAddress\": \"127.0.0.1\", \"platformName\": \"PayTM\", \"operationType\": \"SALES_TO_USER_CREDIT\" }");
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"merchantGuid\": \"125FD26C-4D98-11E2-B20C-E89A8FF309EA\", " +
                "\"merchantOrderId\": \"" + CommonHelpers.generateOrderId() + "\", \"merchantSubWalletGuid\":\"6E05B037-21CC-4919-87C8-E531DD08E33D\", \"" +
                "payeeEmailId\": \"\", \"payeeSsoId\": \"\", \"payeePhoneNumber\":\"" + user.mobNo() + "\", \"activationDate\":null, \"expiryDate\":null, \"amount\":" + amount + ", \"currencyCode\": \"INR\", \"userSubWalletType\":\"FOOD\" }, \"metadata\": \"Testing\", \"ipAddress\": \"127.0.0.1\", \"platformName\": \"PayTM\", \"operationType\": \"SALES_TO_USER_CREDIT\" }");
        Response response = baseApi.execute();
        if (response.statusCode() != 200) throw new SkipException("add amount to food wallet failed");
    }

    private static void addAmtToGiftVoucher(User user, double amount) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/wallet-web/addFundsToSubWallet");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
//        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"merchantGuid\": \"7AA1F178-90B4-48A6-8F03-CFDA4B820950\", \"merchantOrderId\": \""+CommonHelpers.generateOrderId()+"\", \"merchantSubWalletGuid\":\"D6665D35-5ADD-4953-8227-C33E328C3071\", \"payeeEmailId\": \"\", \"payeeSsoId\": \"\", \"payeePhoneNumber\":\""+user.mobNo()+"\", \"activationDate\":null, \"expiryDate\":null, \"amount\":"+amount+", \"currencyCode\": \"INR\", \"userSubWalletType\":\"FOOD\" }, \"metadata\": \"Testing\", \"ipAddress\": \"127.0.0.1\", \"platformName\": \"PayTM\", \"operationType\": \"SALES_TO_USER_CREDIT\" }");
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"merchantGuid\": \"125FD26C-4D98-11E2-B20C-E89A8FF309EA\", " +
                "\"merchantOrderId\": \"" + CommonHelpers.generateOrderId() + "\", \"merchantSubWalletGuid\":\"6E05B037-21CC-4919-87C8-E531DD08E33D\", \"" +
                "payeeEmailId\": \"\", \"payeeSsoId\": \"\", \"payeePhoneNumber\":\"" + user.mobNo() + "\", \"activationDate\":null, \"expiryDate\":null, \"amount\":" + amount + ", \"currencyCode\": \"INR\", \"userSubWalletType\":\"GIFT_VOUCHER\" }, \"metadata\": \"Testing\", \"ipAddress\": \"127.0.0.1\", \"platformName\": \"PayTM\", \"operationType\": \"SALES_TO_USER_CREDIT\" }");
        Response response = baseApi.execute();
        if (response.statusCode() != 200) throw new SkipException("add amount to food wallet failed");
    }


    private static void withdrawAmtFromFoodWallet(User user, double amount) throws AuthException {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/wallet-web/withdraw");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().addHeader("is_admin", "false");
        baseApi.getRequestSpecBuilder().addHeader("tokentype", "OAUTH");
        baseApi.getRequestSpecBuilder().addHeader("ssotoken", user.walletToken());
//        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"skipRefill\": true, \"totalAmount\": \""+amount+"\", \"currencyCode\": \"INR\", \"merchantGuid\": \"FFAA3EC5-B722-43A2-8459-901EABA43F4D\", \"industryType\":\"PVT_LTD\", \"merchantOrderId\": \"1533738084\", \"pgTxnId\":\"153373238084\", \"itemDetails\": [], \"subWalletAmount\":{ \"FOOD\":10, \"GIFT\":10, \"TOLL\":0, \"CLOSED_LOOP_WALLET\":0, \"CLOSED_LOOP_SUB_WALLET\":0, \"FUEL\":10, \"CASHBACK\":0 } }, \"platformName\": \"PayTM\", \"ipAddress\": \"192.168.40.11\", \"operationType\": \"WITHDRAW_MONEY\" }");//TODO add proper json param values after discussing API func. with Wallet team
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"skipRefill\": true, \"totalAmount\": \"" + amount + "\", \"currencyCode\": \"INR\", " +
                "\"merchantGuid\": \"125FD26C-4D98-11E2-B20C-E89A8FF309EA\", \"industryType\":\"PVT_LTD\", \"merchantOrderId\": \"" + CommonHelpers.getRandomWithSize(8) + "\"," +
                " \"pgTxnId\":\"" + CommonHelpers.getRandomWithSize(8) + "\", \"itemDetails\": [], " +
                "\"subWalletAmount\":{ \"FOOD\":" + amount + "} }, \"platformName\": \"PayTM\", \"ipAddress\": \"192.168.40.11\", \"operationType\": \"WITHDRAW_MONEY\" }");
        Response response = baseApi.execute();
        if (response.statusCode() != 200) throw new SkipException("withdraw amount from food wallet failed");
    }


    private static void withdrawAmtFromGV(User user, double amount) throws AuthException {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/wallet-web/withdraw");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().addHeader("is_admin", "false");
        baseApi.getRequestSpecBuilder().addHeader("tokentype", "OAUTH");
        baseApi.getRequestSpecBuilder().addHeader("ssotoken", user.walletToken());
//        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"skipRefill\": true, \"totalAmount\": \""+amount+"\", \"currencyCode\": \"INR\", \"merchantGuid\": \"FFAA3EC5-B722-43A2-8459-901EABA43F4D\", \"industryType\":\"PVT_LTD\", \"merchantOrderId\": \"1533738084\", \"pgTxnId\":\"153373238084\", \"itemDetails\": [], \"subWalletAmount\":{ \"FOOD\":10, \"GIFT\":10, \"TOLL\":0, \"CLOSED_LOOP_WALLET\":0, \"CLOSED_LOOP_SUB_WALLET\":0, \"FUEL\":10, \"CASHBACK\":0 } }, \"platformName\": \"PayTM\", \"ipAddress\": \"192.168.40.11\", \"operationType\": \"WITHDRAW_MONEY\" }");//TODO add proper json param values after discussing API func. with Wallet team
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"skipRefill\": true, \"totalAmount\": \"" + amount + "\", \"currencyCode\": \"INR\", " +
                "\"merchantGuid\": \"125FD26C-4D98-11E2-B20C-E89A8FF309EA\", \"industryType\":\"PVT_LTD\", \"merchantOrderId\": \"" + CommonHelpers.getRandomWithSize(8) + "\"," +
                " \"pgTxnId\":\"" + CommonHelpers.getRandomWithSize(8) + "\", \"itemDetails\": [], " +
                "\"subWalletAmount\":{ \"GIFT_VOUCHER\":\" "+getGVBalance(user)+"\"} }, \"platformName\": \"PayTM\", \"ipAddress\": \"192.168.40.11\", \"operationType\": \"WITHDRAW_MONEY\" }");
        Response response = baseApi.execute();
        if (response.statusCode() != 200) throw new SkipException("withdraw amount from GV failed");
    }


    public static void withdrawOnlyFromMainWallet(User user, double amount) throws WalletException, AuthException {
        if (amount == 0.0D) {
            return;
        }
        WalletHelpers.getWalletBalance(user);
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/wallet-web/withdraw");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().addHeader("is_admin", "false");
        baseApi.getRequestSpecBuilder().addHeader("tokentype", "OAUTH");
        baseApi.getRequestSpecBuilder().addHeader("ssotoken", user.walletToken());
        baseApi.getRequestSpecBuilder().setBody("{ \"request\": { \"skipRefill\": true, \"totalAmount\": \"" + amount + "\", \"currencyCode\": \"INR\", " +
                "\"merchantGuid\": \"125FD26C-4D98-11E2-B20C-E89A8FF309EA\", \"industryType\":\"PVT_LTD\", \"merchantOrderId\": \"" + CommonHelpers.getRandomWithSize(8) + "\"," +
                " \"pgTxnId\":\"" + CommonHelpers.getRandomWithSize(8) + "\", \"itemDetails\": [], " +
                "\"subWalletAmount\":{ \"FOOD\":\"0\",\"GIFT\":\"0\",\"TOLL\":\"0\"} }, \"platformName\": \"PayTM\", \"ipAddress\": \"192.168.40.11\", \"operationType\": \"WITHDRAW_MONEY\" }");
        Response response = baseApi.execute();
        if (response.statusCode() != 200) throw new SkipException("withdraw amount from food wallet failed");
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualToIgnoringCase("SUCCESS");
    }

    public static Response checkWalletLimit(User user , String txnAmount, String addMoneyDestination,String cardHash, String cardIndexNumber,String payMode )
    {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/wallet-web/walletLimits");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().setBody("{\n" +
                "    \"request\": {\n" +
                "        \"walletOperationTypeBasedAmountList\": [\n" +
                "            \""+txnAmount+"\"\n" +
                "        ],\n" +
                "        \"ssoId\": \""+user.custId()+"\",\n" +
                "        \"ignoreAddMoneyDestination\": false,\n" +
                "        \"addMoneyDestination\": \""+addMoneyDestination+"\",\n" +
                "        \"walletOperationTypeList\": [\n" +
                "            \"ADD_MONEY\"\n" +
                "        ],\n" +
                "        \"cardHash\":\""+cardHash+"\",\n" +
                "        \"paymentMode\": \""+payMode+"\",\n" +
                "        \"cardIndexNo\":\""+cardIndexNumber+"\"\n" +
                "    },\n" +
                "    \"metadata\": \"INR\",\n" +
                "    \"ipAddress\": \"INR\",\n" +
                "    \"operationType\": \"WALLET_LIMIT\",\n" +
                "    \"platformName\": \"PayTM\"\n" +
                "}");
        Response response = baseApi.execute();
        return response;

    }

    @Step
    public static void breachLimitByAddMoney(User user) {
        report.info("Breach add money limit for prime user by updating user balance: " + user);
        String query = "select config from limit_config where limit_name = \"WALLET_AGGREGATE_BALANCE_LIMIT\" and pan_verified='0' and wallet_rbi_type='PAYTM_PRIME_WALLET';\n ";
        String dbString = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.WALLET_DB_CONNECTION_URL, query)
                .get(0).toString();
        String jsonString  = dbString.split("=")[1].trim().replaceFirst("}","");
        JsonPath jPath = new JsonPath(jsonString);
        String balance = jPath.getString("balance");
        WalletHelpers.modifyBalance(user, Double.parseDouble(balance));
    }


    public static Response setWalletType(String userID, String walletState)
    {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.POST);
        baseApi.getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        baseApi.getRequestSpecBuilder().setBasePath("/wallet-web/upgradeWallet");
        baseApi.getRequestSpecBuilder().setContentType(ContentType.JSON);
        baseApi.getRequestSpecBuilder().setBody("{\n" +
                "    \"request\": {\n" +
                "      \"ssoId\":\""+ userID +"\",\n" +
                "      \"walletState\":\""+walletState+"\",\n" +
                "       \"rbiMigration\": true\n" +
                "    },\n" +
                "  \"ipAddress\": \"127.0.0.1\",\n" +
                "  \"platformName\": \"PayTM\",\n" +
                "  \"operationType\": \"WALLET_UPGRADE\"\n" +
                "}");
        Response response = baseApi.execute();
        return response;
    }
}
