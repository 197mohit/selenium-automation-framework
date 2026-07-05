package com.paytm.utils.merchant.util;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.api.wallet.AddMoneyAPI;
import com.paytm.utils.merchant.api.wallet.BlockDisputeTxn;
import com.paytm.utils.merchant.api.wallet.CheckBalance;
import com.paytm.utils.merchant.api.wallet.Withdraw;
import com.paytm.utils.merchant.api.wallet.dto.RequestDTO;
import com.paytm.utils.merchant.api.wallet.dto.WalletDTO;
import com.paytm.utils.merchant.util.exception.walletException.WalletException;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;
import java.util.Random;


public class WalletUtil {

    private static final String DEFAULT_MERCHANT_GUID = "125FD26C-4D98-11E2-B20C-E89A8FF309EA";
    private static final String DEFAULT_MERCHANT_KEY = "C#txuHrwuO0ghDfv";


    public static double getWalletBalance(String walletBaseUri, String ssoToken) {
        String debugMsg = "Wallet balance could not be fetched successfully";
        BaseApi api = new CheckBalance(walletBaseUri, ssoToken);
        double walletBalance = 0;
        int count = 0;
        int maxRetryCount = 3;
        Response response = null;
        while (count < maxRetryCount) {
            try {
                response = api.execute();
            } catch (Throwable e) {
                throw new WalletException(debugMsg, e);
            }
            JsonPath jsonPath;
            try {
                jsonPath = response.jsonPath();
            } catch (JsonPathException e) {
                throw new WalletException(debugMsg, e, response);
            }
            if ("SUCCESS".equalsIgnoreCase(jsonPath.get("statusCode"))) {
                walletBalance = jsonPath.getDouble("response.amount");
                break;
            }
            count++;
        }
        if (count == maxRetryCount) {
            throw new WalletException(debugMsg, response);
        }
        return walletBalance;
    }

    public static void withdrawAmount(String walletBaseUri, String walletSsoToken, double amount) {
        withdrawAmount(walletBaseUri, walletSsoToken, amount, DEFAULT_MERCHANT_GUID, DEFAULT_MERCHANT_KEY);
    }

    public static void withdrawAmount(String walletBaseUri, String walletSsoToken, double amount, String merchantGuid, String merchantKey) {
        String debugMsg = "Amount could not be withdrawn from wallet successfully";
        String merchantOrderId = Long.toString(System.currentTimeMillis());
        String body = "{ \"request\": { \"skipRefill\": true, \"totalAmount\": \"" + amount + "\", \"currencyCode\": \"INR\", \"merchantGuid\": \"" + merchantGuid + "\", \"industryType\":\"Retail\", \"merchantOrderId\": \"" + merchantOrderId + "\", \"pgTxnId\": \"" + merchantOrderId + "\",\"subWalletAmount\":{ \"FOOD\":\"0\",\"GIFT\":\"0\",\"TOLL\":\"0\"}}, \"platformName\": \"PayTM\", \"ipAddress\": \"192.168.40.11\", \"operationType\": \"WITHDRAW_MONEY\" }";
        String checksumHash = PGPUtil.getChecksum(merchantKey, body);
        BaseApi api = new Withdraw(walletBaseUri, walletSsoToken, merchantGuid, checksumHash, body);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new WalletException(debugMsg, e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();
        } catch (JsonPathException e) {
            throw new WalletException(debugMsg, e, response);
        }
        if (!"SUCCESS".equalsIgnoreCase(jsonPath.get("statusCode"))) {
            throw new WalletException(debugMsg, response);
        }
    }

    public static void addAmount(String walletBaseUri, String mobileNumber, String walletToken, double amount) {
        addAmount(walletBaseUri, mobileNumber, walletToken, amount, DEFAULT_MERCHANT_GUID, DEFAULT_MERCHANT_KEY);
    }
    public static void addAmountCC(String walletBaseUri, String mobileNumber, String walletToken, double amount) {
        addAmountCC(walletBaseUri, mobileNumber, walletToken, amount, DEFAULT_MERCHANT_GUID, DEFAULT_MERCHANT_KEY);
    }


    public static void addAmount(String walletBaseUri, String mobileNumber, String walletToken, double amount, String merchantGuid, String merchantKey) {
        String debugMsg = "Amount could not be added to wallet successfully";
        Random random = new Random();
        WalletDTO body = new WalletDTO()
                .setIpAddress("219.65.43.2")
                .setMetadata("INR")
                .setOperationType("ADD_MONEY_VIA_MERCHANT")
                .setPlatformName("PayTM")
                .setRequest(
                        new RequestDTO()
                                .setBankName("HDFC Bank")
                                .setBankTxnId(String.valueOf(Math.abs(random.nextInt())))
                                .setDestination("MAIN")
                                .setMerchantGuid("125FD26C-4D98-11E2-B20C-E89A8FF309EA")
                                .setMerchantOrderId(String.valueOf(Math.abs(random.nextInt())))
                                .setPaymentMethod("DC")
                                .setPaymentMode("DC")
                                .setPgTxnId(String.valueOf(Math.abs(random.nextInt())))
                                .setRequestType("WEB")
                                .setTxnAmount(String.valueOf(amount))
                                .setTxnCurrency("INR")
                                .setTxnStatus("Success")
                );
        BaseApi api = new AddMoneyAPI(walletToken, body);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new WalletException(debugMsg, e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();
        } catch (JsonPathException e) {
            throw new WalletException(debugMsg, e, response);
        }
        if (!"SUCCESS".equalsIgnoreCase(jsonPath.get("statusCode"))) {
            throw new WalletException(debugMsg, response);
        }
    }
    public static void addAmountCC(String walletBaseUri, String mobileNumber, String walletToken, double amount, String merchantGuid, String merchantKey) {
        String debugMsg = "Amount could not be added to wallet successfully";
        Random random = new Random();
        WalletDTO body = new WalletDTO()
                .setIpAddress("219.65.43.2")
                .setMetadata("INR")
                .setOperationType("ADD_MONEY_VIA_MERCHANT")
                .setPlatformName("PayTM")
                .setRequest(
                        new RequestDTO()
                                .setBankName("HDFC Bank")
                                .setBankTxnId(String.valueOf(Math.abs(random.nextInt())))
                                .setDestination("MAIN")
                                .setMerchantGuid("125FD26C-4D98-11E2-B20C-E89A8FF309EA")
                                .setMerchantOrderId(String.valueOf(Math.abs(random.nextInt())))
                                .setPaymentMethod("CC")
                                .setPaymentMode("CC")
                                .setPgTxnId(String.valueOf(Math.abs(random.nextInt())))
                                .setRequestType("WEB")
                                .setTxnAmount(String.valueOf(amount))
                                .setTxnCurrency("INR")
                                .setTxnStatus("Success")
                );
        BaseApi api = new AddMoneyAPI(walletToken, body);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new WalletException(debugMsg, e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();
        } catch (JsonPathException e) {
            throw new WalletException(debugMsg, e, response);
        }
        if (!"SUCCESS".equalsIgnoreCase(jsonPath.get("statusCode"))) {
            throw new WalletException(debugMsg, response);
        }
    }


    public static void blockAmount(String walletBaseUri, String walletToken, String customerId, double amount, String merchantKey) {
        String debugMsg = "Wallet Amount couldn't get blocked successfully";
        String adminGuid = "83b30930-8732-11e3-b653-000c292554b0";
        String body = "{\"request\":{\"txnGuid\":null,\"custId\":\"" + customerId + "\",\"adminId\":20004,\"refrenceNo\":\"qweqwe\",\"totalAmount\":" + amount + ",\"blockingReason\":\"suspicious transaction\",\"blockingAmount\":" + amount + ",\"requestType\":\"WALLET_BLOCK\",\"adminGuid\":\"" + adminGuid + "\",\"currencyCode\":\"INR\",\"bankGateway\":\"-\",\"requestWithoutTxn\":true},\"ipAddress\":\"10.0.129.22\",\"platformName\":\"PayTM\",\"operationType\":\"DISPUTED_BLOCK_TRANSFER\",\"deviceId\":null,\"metadata\":null,\"channel\":null,\"version\":null,\"mode\":null}";
        String checksumHash = PGPUtil.getChecksum(merchantKey, body);
        BaseApi api = new BlockDisputeTxn(walletBaseUri, walletToken, adminGuid, checksumHash, body);
        Response response;
        try {
            response = api.execute();
        } catch (Throwable e) {
            throw new WalletException(debugMsg, e);
        }
        JsonPath jsonPath;
        try {
            jsonPath = response.jsonPath();
        } catch (JsonPathException e) {
            throw new WalletException(debugMsg, e, response);
        }
        if (!"SUCCESS".equalsIgnoreCase(jsonPath.get("statusCode"))) {
            throw new WalletException(debugMsg, response);
        }
    }

    public static void modifyBalanceCC(String walletBaseUri, String mobileNumber, String walletToken, Double amountToBeRetainedInWallet) {
        String debugMsg = "Wallet balance could not be modified successfully";
        double currentBalance = 0;
        try {
            currentBalance = getWalletBalance(walletBaseUri, walletToken);
            double amount = amountToBeRetainedInWallet - currentBalance;
            UpdatingBalance:
            {
                if (amount > 0 && amount < 1) {
                    addAmountCC(walletBaseUri, mobileNumber, walletToken, 1 + amount);
                    withdrawAmount(walletBaseUri, walletToken, 1.00);
                } else if (amount >= 1) {
                    addAmountCC(walletBaseUri, mobileNumber, walletToken, amount);
                } else if (amount < 0) {
                    withdrawAmount(walletBaseUri, walletToken, -amount);
                }
            }
            if (amount != 0.00) {
                currentBalance = getWalletBalance(walletBaseUri, walletToken);
            }
        } catch (WalletException e) {
            throw new WalletException(debugMsg, e);
        }
        if (currentBalance != amountToBeRetainedInWallet) {
            throw new WalletException(debugMsg);
        }
    }

    public static void modifyBalance(String walletBaseUri, String mobileNumber, String walletToken, Double amountToBeRetainedInWallet) {
        String debugMsg = "Wallet balance could not be modified successfully";
        double currentBalance = 0;
        try {
            currentBalance = getWalletBalance(walletBaseUri, walletToken);
            double amount = amountToBeRetainedInWallet - currentBalance;
            UpdatingBalance:
            {
                if (amount > 0 && amount < 1) {
                    addAmount(walletBaseUri, mobileNumber, walletToken, 1 + amount);
                    withdrawAmount(walletBaseUri, walletToken, 1.00);
                } else if (amount >= 1) {
                    addAmount(walletBaseUri, mobileNumber, walletToken, amount);
                } else if (amount < 0) {
                    withdrawAmount(walletBaseUri, walletToken, -amount);
                }
            }
            if (amount != 0.00) {
                currentBalance = getWalletBalance(walletBaseUri, walletToken);
            }
        } catch (WalletException e) {
            throw new WalletException(debugMsg, e);
        }
        if (currentBalance != amountToBeRetainedInWallet) {
            throw new WalletException(debugMsg);
        }
    }


    public static void validateBalance(String walletBaseUri, String walletToken, double expectedWalletBal) {
        double currentBalance = 0.0;
        try {
            ValidatingBalance:
            {
                for (int i = 0; i < 40; i++) {
                    currentBalance = getWalletBalance(walletBaseUri, walletToken);
                    if (currentBalance == expectedWalletBal) {
                        break;
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (WalletException e) {
            throw new WalletException("Wallet balance could not be validated", e);
        }
        if (currentBalance != expectedWalletBal) {
            throw new WalletException("Wallet balance is not as expected");
        }
    }

    public static void breachAddMoneyLimit(String walletDBConnectionUrl, String custId) {
        String newConfig = "{\"dayLimit\":{\"limitCounters\":{\"20\":[{\"a\":200000000000,\"c\":1,\"r\":0,\"o\":1},{\"a\": 200000000000 ,\"c\":1,\"r\":0,\"o\":0}],\"1\":[{\"a\":-4.2,\"c\":3,\"r\":0,\"o\":1}],\"36\":[{\"a\":3.2,\"c\":3,\"r\":0,\"o\":0}]}},\"monthLimit\":{\"limitCounters\":{\"20\":[{\"a\":2000000000,\"c\":1,\"r\":0,\"o\":1},{\"a\":200000000000 ,\"c\":1,\"r\":0,\"o\":0}],\"1\":[{\"a\":-4.2,\"c\":3,\"r\":0,\"o\":1}],\"36\":[{\"a\":3.2,\"c\":3,\"r\":0,\"o\":0}]}},\"yearlyLimit\":{\"limitCounters\":{\"20\":[{\"a\":2000000000,\"c\":1,\"r\":0,\"o\":1},{\"a\": 200000000000 ,\"c\":1,\"r\":0,\"o\":0}],\"1\":[{\"a\":-4.2,\"c\":3,\"r\":0,\"o\":1}],\"36\":[{\"a\":3.2,\"c\":3,\"r\":0,\"o\":0}]}}}";
        try {
            updateCreditThroughputLimit(walletDBConnectionUrl, custId, newConfig);
        } catch (WalletException e) {
            throw new WalletException("Couldn't breach Add Money limit", e);
        }
    }

    public static void breachLimitViaBalanceModification(String walletDBConnectionUrl, String custId) throws WalletException {
        String newConfig = "{\"dayLimit\":{\"limitCounters\":{\"20\":[{\"a\":200000000000,\"c\":1,\"r\":0,\"o\":1},{\"a\": 200000000000 ,\"c\":1,\"r\":0,\"o\":0}],\"1\":[{\"a\":-4.2,\"c\":3,\"r\":0,\"o\":1}],\"36\":[{\"a\":3.2,\"c\":3,\"r\":0,\"o\":0}]}},\"monthLimit\":{\"limitCounters\":{\"20\":[{\"a\":2000000000,\"c\":1,\"r\":0,\"o\":1},{\"a\":200000000000 ,\"c\":1,\"r\":0,\"o\":0}],\"1\":[{\"a\":-4.2,\"c\":3,\"r\":0,\"o\":1}],\"36\":[{\"a\":3.2,\"c\":3,\"r\":0,\"o\":0}]}},\"yearlyLimit\":{\"limitCounters\":{\"20\":[{\"a\":2000000000,\"c\":1,\"r\":0,\"o\":1},{\"a\": 200000000000 ,\"c\":1,\"r\":0,\"o\":0}],\"1\":[{\"a\":-4.2,\"c\":3,\"r\":0,\"o\":1}],\"36\":[{\"a\":3.2,\"c\":3,\"r\":0,\"o\":0}]}}}";
        String queryForLimitConfig = "select * from limit_config where wallet_rbi_type='PAYTM_PRIME_WALLET' and pan_verified=0 and trust_factor=0  and limit_name='RBI_BALANCE';";
        String amount = "";
        try {
            updateCreditThroughputLimit(walletDBConnectionUrl, custId, newConfig);
        } catch (WalletException e) {
            throw new WalletException("Couldn't breach Add Money limit", e);
        }
    }


    public static void setLimitAuditInfoDefault(String walletDBConnectionUrl, String custId) {
        String Config = "{\"dayLimit\":{\"limitCounters\":{\"20\":[{\"a\":2,\"c\":1,\"r\":0,\"o\":1}]}},\"monthLimit\":{\"limitCounters\":{\"20\":[{\"a\":2,\"c\":1,\"r\":0,\"o\":1}]}},\"yearlyLimit\":{\"limitCounters\":{\"20\":[{\"a\":2,\"c\":1,\"r\":0,\"o\":1}]}}}";
        try {
            updateCreditThroughputLimit(walletDBConnectionUrl, custId, Config);
        } catch (WalletException e) {
            throw new WalletException("Couldn't set Audit Info limit to default", e);
        }
    }

    private static void updateCreditThroughputLimit(String walletDBConnectionUrl, String customerId, String config) {
        String query = "update limit_audit_info set limit_info = '" + config + "' where cust_id = '" + customerId + "'";
        try {
            DatabaseUtil.getInstance().executeUpdateQuery(walletDBConnectionUrl, query);
        } catch (Throwable e) {
            throw new WalletException("Credit throughput limit update failed. Exception occurred while running query: " + query, e);
        }
    }

    private static void getRBI_BALANCE_LIMIT_Value(String walletDBConnectionUrl, String config) throws WalletException {
        String query = "select * from limit_config where wallet_rbi_type='PAYTM_PRIME_WALLET' and pan_verified=0 and trust_factor=0  and limit_name='RBI_BALANCE';";
        try {
            DatabaseUtil.getInstance().executeSelectQuery(walletDBConnectionUrl, query);
        } catch (Throwable e) {
            throw new WalletException("Credit throughput limit update failed. Exception occurred while running query: " + query, e);
        }
    }

    private static String getCreditThroughputAmountLimitForPrimeWallet(String walletDBConnectionUrl) throws WalletException {
        String query = "select config from limit_config where limit_name = 'CREDIT_THROUGHPUT_LIMIT'  " +
                "and trust_factor = 0 and pan_verified = 0 and wallet_rbi_type = 'PAYTM_PRIME_WALLET'";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(walletDBConnectionUrl, query);
        if (result.size() == 0) {
            throw new WalletException("No result found for query: " + query);
        }
        String config = result.get(0).get("config").toString();
        String limitAmount;
        try {
            limitAmount = new JsonPath(config).getString("periodLimits.monthPeriodLimit.amount");
        } catch (JsonPathException e) {
            throw new WalletException("Couldn't fetch credit throughput amount limit. Exception occurred while parsing string: " + config, e);
        }
        if (limitAmount == null) {
            throw new WalletException("Couldn't fetch credit throughput amount limit.");
        }
        return limitAmount;
    }


}
