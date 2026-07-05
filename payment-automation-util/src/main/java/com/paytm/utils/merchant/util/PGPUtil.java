package com.paytm.utils.merchant.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.ui.element.*;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pg.crypto.SecurityException;
import com.paytm.pg.crypto.*;
import com.paytm.pg.merchant.CheckSumServiceHelper;
import com.paytm.utils.merchant.DbQueries;
import com.paytm.utils.merchant.api.pgp.bo.PGPlusBO;
import com.paytm.utils.merchant.api.pgp.merchant_status.TransactionStatus;
import com.paytm.utils.merchant.api.pgp.merchant_status.TransactionStatusList;
import com.paytm.utils.merchant.api.pgp.merchant_status.TransactionStatusListSecured;
import com.paytm.utils.merchant.api.pgp.refund.*;
import com.paytm.utils.merchant.api.pgp.saved_card.AddSavedCard;
import com.paytm.utils.merchant.api.pgp.saved_card.DeleteSavedCard;
import com.paytm.utils.merchant.api.pgp.saved_card.GetSavedCard;
import com.paytm.utils.merchant.api.pgp.theia.ProcessTransaction;
import com.paytm.utils.merchant.api.pgp.theia.paytm_express.GetCardToken;
import com.paytm.utils.merchant.api.pgp.theia.paytm_express.GetCardTokenBillPayment;
import com.paytm.utils.merchant.dto.bo.PGPlusBODTO;
import com.paytm.utils.merchant.dto.cachecardtoken.request.CacheCardDTO;
import com.paytm.utils.merchant.dto.cachecardtoken.request.Name;
import com.paytm.utils.merchant.dto.refund.*;
import com.paytm.utils.merchant.util.exception.pgpException.NoResultFoundException;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Optional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;

import static com.paytm.framework.reporting.Reporter.report;

public class PGPUtil {

    private static Button pay() {
        return new Button(By.xpath("//input[@value='Pay']"), "Checkout Page", "pay");
    }

    private static Link tabCreditCard() {
        return new Link(By.linkText("Credit Card"), "Cashier Page", "tabCreditCard");
    }

    private static TextBox textBoxCardNumber() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active .cd input:first-child"), "Cashier Page", "textBoxCardNumber");
    }

    private static Select dropdownExpiryMonth() {
        return new Select(By.cssSelector(".cards-tabs:not(.hide) .content.active .mb10 .fl:not(.img) select[name='expiryMonth']"), "Cashier Page", "dropdownExpiryMonth");
    }

    private static Select dropdownExpiryYear() {
        return new Select(By.cssSelector(".cards-tabs:not(.hide) .content.active .mb10 .fl:not(.img) select[name='expiryYear']"), "Cashier Page", "dropdownExpiryYear");
    }

    private static TextBox textBoxCVVNumber() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active .cvv-block input[name='cvvNumber']"), "Cashier Page", "textBoxCVVNumber");
    }

    private static Button buttonPGPayNow() {
        return new Button(By.cssSelector(".cards-tabs:not(.hide) .content.active button[type='submit']"), "Cashier Page", "buttonPGPayNow");
    }

    private static UIElement textOrderID() {
        return new UIElement(By.xpath("//table/tbody//*[text()=\"ORDERID\"]/../td[2]"), "Response Page", "textOrderID");
    }

    public static String renewSubscription(String pgpUrl, String orderId, String mid, double txnAmount, String subsId) {
        String body = "REQUEST_TYPE=RENEW_SUBSCRIPTION&ORDER_ID=" + orderId + "&MID=" + mid + "&TXN_AMOUNT=" + txnAmount + "&SUBS_ID=" + subsId;
        BaseApi api = new ProcessTransaction(pgpUrl, body);
        Response response = api.execute();
        JsonPath jsonPath = response.jsonPath();
        return jsonPath.get("RESPMSG");
    }

    /**
     * This will trigger refund using /refund/api/v1/async/refund api.
     *
     * @param mid
     * @param merchantKey
     * @param orderId
     * @param refId
     * @param txnId
     * @param refundAmount
     * @param txnType
     * @param comments
     * @param subWalletAmount
     * @return
     */
    public static Response asyncRefundUntilPending(String pgpUrl, String mid, String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("comments") String comments, SubWalletAmount subWalletAmount) throws PGPException {
        Body body = new Body()
                .setMid(mid)
                .setOrderId(orderId)
                .setRefId(refId)
                .setTxnId(txnId)
                .setRefundAmount(refundAmount)
                .setTxnType(txnType)
                .setComments(comments)
                .setSubWalletAmount(subWalletAmount);
        AsyncRefundDTO asyncRefundDTO = null;
        try {
            asyncRefundDTO = new AsyncRefundDTO(merchantKey, body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new RefundAsync(pgpUrl, mapper.writeValueAsString(asyncRefundDTO));
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting AsyncRefundDTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in Async Refund");
        }
        return response;
    }


    public static Response asyncRefundWithExtendInfo(String pgpUrl, String mid, String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("comments") String comments, SubWalletAmount subWalletAmount, ExtendInfo extendInfo) throws PGPException {
        Body body = new Body()
                .setMid(mid)
                .setOrderId(orderId)
                .setRefId(refId)
                .setTxnId(txnId)
                .setRefundAmount(refundAmount)
                .setTxnType(txnType)
                .setComments(comments)
                .setSubWalletAmount(subWalletAmount)
                .setExtendInfo(extendInfo);
        AsyncRefundDTO asyncRefundDTO = null;
        try {
            asyncRefundDTO = new AsyncRefundDTO(merchantKey, body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new RefundAsync(pgpUrl, mapper.writeValueAsString(asyncRefundDTO));
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting AsyncRefundDTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in Async Refund");
        }
        return response;
    }


    public static Response asyncRefundJWT(String pgpUrl, String mid, String orderId, String refId, String txnId, String refundAmount, String txnType, Boolean disableMerchantDebitRetry, String comments, String jwtToken) throws PGPException {
        String client = "client123";
        Body body = new Body()
                .setMid(mid)
                .setOrderId(orderId)
                .setRefId(refId)
                .setTxnId(txnId)
                .setRefundAmount(refundAmount)
                .setDisableMerchantDebitRetry(disableMerchantDebitRetry)
                .setTxnType(txnType)
                .setComments(comments)
                .setAgentInfo(new AgentInfo().setEmployeeId("ASQERDFDSFDFDSFDSFDFDASQERDFDSFDFDSFDSFDFDF").setName("1234567890")
                        .setPhoneNo("87653578996213214353252").setEmail("xyz"));

        AsyncRefundDTO asyncRefundDTO = null;

        asyncRefundDTO = new AsyncRefundDTO(jwtToken, "jwt", body, client);

        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new RefundAsync(pgpUrl, mapper.writeValueAsString(asyncRefundDTO));
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting AsyncRefundDTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in Async Refund");
        }
        return response;
    }


    public static Response syncRefundJWT(String pgpUrl, String mid, String orderId, String refId, String txnId, String refundAmount, String txnType, Boolean disableMerchantDebitRetry, String comments, String jwtToken) throws PGPException {
        String client = "client123";
        Body body = new Body()
                .setMid(mid)
                .setOrderId(orderId)
                .setRefId(refId)
                .setTxnId(txnId)
                .setRefundAmount(refundAmount)
                .setDisableMerchantDebitRetry(disableMerchantDebitRetry)
                .setTxnType(txnType)
                .setComments(comments)
                .setAgentInfo(new AgentInfo().setEmployeeId("ASQERDFDSFDFDSFDSFDFDASQERDFDSFDFDSFDSFDFDF").setName("1234567890")
                        .setPhoneNo("87653578996213214353252").setEmail("xyz"));

        AsyncRefundDTO syncRefundDTO = null;

        syncRefundDTO = new AsyncRefundDTO(jwtToken, "jwt", body, client);

        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new RefundAsync().getSyncRefund(pgpUrl, mapper.writeValueAsString(syncRefundDTO));

        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting AsyncRefundDTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in Async Refund");
        }
        return response;
    }

    //Async Refund With Agg MID
    public static Response asyncRefundWithAggMidUntilPending(String pgpUrl, String mid, String aggMid, String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("comments") String comments, SubWalletAmount subWalletAmount) throws PGPException {
        Body body = new Body()
                .setMid(mid)
                .setAggMid(aggMid)
                .setOrderId(orderId)
                .setRefId(refId)
                .setTxnId(txnId)
                .setRefundAmount(refundAmount)
                .setTxnType(txnType)
                .setComments(comments)
                .setSubWalletAmount(subWalletAmount);
        AsyncRefundDTO asyncRefundDTO = null;
        try {
            asyncRefundDTO = new AsyncRefundDTO(merchantKey, body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new RefundAsync(pgpUrl, mapper.writeValueAsString(asyncRefundDTO));
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting AsyncRefundDTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in Async Refund");
        }
        return response;
    }


    /**
     * This will trigger impsrefund using /refund/api/v1/async/refund api.
     *
     * @param mid
     * @param merchantKey
     * @param orderId
     * @param refId
     * @param txnId
     * @param refundAmount
     * @param txnType
     * @param comments
     * @param token
     * @param preferredDestination
     * @return
     */
    public static Response asyncRefundIMPSUntilPending(String pgpUrl, String mid, String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("comments") String comments, String token, String preferredDestination) throws PGPException {
        Body body = new Body()
                .setMid(mid)
                .setOrderId(orderId)
                .setRefId(refId)
                .setTxnId(txnId)
                .setRefundAmount(refundAmount)
                .setTxnType(txnType)
                .setComments(comments)
                .setToken(token).setPreferredDestination(preferredDestination);
        AsyncRefundDTO asyncRefundDTO = null;
        try {
            asyncRefundDTO = new AsyncRefundDTO(merchantKey, body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new RefundAsync(pgpUrl, mapper.writeValueAsString(asyncRefundDTO));
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting AsyncRefundDTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in Async Refund");
        }
        return response;
    }


    /**
     * This will trigger CacheCardToken using /refund/api/v1/account/validate api.
     *
     * @param mid
     * @param merchantKey
     * @param vpa
     * @param accountNumber
     * @param ifscCode
     * @param name
     * @param mobileNo
     * @param requestId
     * @return
     */
    public static Response cacheCardToken(String pgpUrl, String mid, String merchantKey, String vpa, String accountNumber, String ifscCode, Name name, String mobileNo, String requestId) throws PGPException {

        com.paytm.utils.merchant.dto.cachecardtoken.request.Body body = new com.paytm.utils.merchant.dto.cachecardtoken.request.Body()
                .setMid(mid)
                .setAccountNumber(accountNumber)
                .setIfscCode(ifscCode)
                .setMobileNo(mobileNo)
                .setRequestId(requestId)
                .setName(name)
                .setVpa(vpa);

        CacheCardDTO cacheCardDTO = null;
        try {
            cacheCardDTO = new CacheCardDTO(merchantKey, body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new CacheCardToken(pgpUrl, mapper.writeValueAsString(cacheCardDTO));
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting CacheCardDTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in CacheCardToken");
        }
        return response;
    }


    /**
     * This will trigger PGPlusBO using /pg-plus-bo/search/refund.
     *
     * @param pgPlusBODTO
     * @return
     */
    public static Response pgPlusBO(String pgpUrl, PGPlusBODTO pgPlusBODTO) throws PGPException {


        ObjectMapper mapper = new ObjectMapper();
        BaseApi api = null;
        try {
            api = new PGPlusBO(pgpUrl, mapper.writeValueAsString(pgPlusBODTO));
        } catch (JsonProcessingException e) {
            throw new PGPException("Exception Occured while converting PGPlusBODTO to String", e);
        }
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in PGPlusBO");
        }
        return response;
    }

    /**
     * This will trigger refund using /refund/HANDLER_INTERNAL/REFUND api.
     *
     * @param mid
     * @param merchantKey
     * @param orderId
     * @param refId
     * @param refundAmount
     * @param txnId
     * @param postConvFlag defining transactionType in Initiate Refund API
     * @return
     */
    public static Response executeRefund_checksum(String pgpUrl, String mid, String merchantKey, String orderId, String refId, String refundAmount, String txnId, String postConvFlag) throws PGPException {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        treemap.put("REFID", refId);
        treemap.put("COMMENTS", "COMMENTS");
        treemap.put("REFUNDAMOUNT", refundAmount);
        treemap.put("TXNID", txnId);

        if (postConvFlag == null || postConvFlag.isEmpty()) {
            treemap.put("TXNTYPE", "REFUND");

        } else {
            treemap.put("TXNTYPE", postConvFlag);
        }
        String checksum = PGPUtil.generateChecksumRefund(merchantKey, treemap);
        treemap.put("CHECKSUM", checksum);
        BaseApi api = new Refund(pgpUrl, new JSONObject(treemap).toString());
        Response response = api.execute();
        if (response.statusCode() != 200) {
            throw new PGPException("Exception in Refund");
        }
        return response;
    }

    /**
     * Execute SECURE_REFUND_STATUS with checksum if isSecure is true.
     * End point: /refund/HANDLER_INTERNAL/getMasterRefundStatus <br>
     * <p>
     * Execute REFUND_STATUS if isSecure is false
     * End point: /refund/HANDLER_INTERNAL/REFUND_STATUS
     *
     * @param mid
     * @param merchantKey
     * @param refId
     * @return
     */
    public static Response executeRefundStatus(String pgpUrl, String mid, String merchantKey, String refId, boolean isSecure) throws PGPException {
        BaseApi api;
        if (isSecure) {
            TreeMap<String, String> treemap = new TreeMap<>();
            treemap.put("MID", mid);
            treemap.put("REFID", refId);
            String checksum = getChecksum(merchantKey, treemap);
            api = new RefundStatusSecured(pgpUrl, mid, refId, checksum);
        } else {
            api = new RefundStatus(pgpUrl, mid, refId);
        }
        int timeOutInSeconds = 60;
        long startTime = System.currentTimeMillis();
        Response response = null;

        response = Awaitility.with().pollInSameThread()
                .await()
                .pollInterval(Duration.FIVE_SECONDS)
                .atMost(Duration.ONE_MINUTE)
                .until(refundStatusCallable(api), Objects::nonNull);

//        while ((System.currentTimeMillis() - startTime) < timeOutInSeconds * 1000) {
//            response = api.execute();
//            if (response.statusCode() != 200) {
//                throw new PGPException("Exception in Refund");
//            }
//            if (response.jsonPath().get("REFUND_LIST.STATUS") == null)       // To avoid exception during jsonPath.getList() in case of null response in REFUND_LIST
//                continue;
//            if (response.jsonPath().getList("REFUND_LIST.STATUS").get(0).toString().equals("PENDING")) {
//                try {
//                    Thread.sleep(5 * 1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } else
//                return response;
//        }
        return response;
    }

    private static Callable<Response> refundStatusCallable(BaseApi api) {
        return new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response response = api.execute();
                if (200 != response.statusCode() &&
                        null == response.jsonPath().getString("REFUND_LIST[0].STATUS") &&
                        "PENDING".equals(response.jsonPath().getString("REFUND_LIST[0].STATUS")))
                    return null;
                return response;
            }
        };
    }

    @Deprecated
    public static Response refundStatusUntilPending(String pgpUrl, String mid, String orderId, String refId, String refundAmount, String txnId, String postConvFlag) {
        String body;
        if (postConvFlag == null || postConvFlag.isEmpty()) {
            body = "{\"MID\": \"" + mid + "\", \"ORDERID\": \"" + orderId + "\", \"REFID\": \"" + refId + "\", \"CHECKSUM\": \"0\", \"COMMENTS\":\"Initiate Refund.....\", \"TXNTYPE\": \"REFUND\", \"REFUNDAMOUNT\": \"" + refundAmount + "\",\"TXNID\":\"" + txnId + "\"}";
        } else {
            body = "{\"MID\": \"" + mid + "\", \"ORDERID\": \"" + orderId + "\", \"REFID\": \"" + refId + "\", \"CHECKSUM\": \"0\", \"COMMENTS\":\"Initiate Refund.....\", \"TXNTYPE\": \"" + postConvFlag + "\", \"REFUNDAMOUNT\": \"" + refundAmount + "\",\"TXNID\":\"" + txnId + "\"}";
        }
        BaseApi api = new Refund(pgpUrl, body);
        Response response = api.execute();
        if ("501".equals(response.jsonPath().get("RESPCODE"))) {
            return response;
        }
        api = new RefundStatus(pgpUrl, mid, refId);
        int timeOutInSeconds = 90;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeOutInSeconds * 1000) {
            response = api.execute();
            if (response.jsonPath().get("REFUND_LIST.STATUS") != null && !response.jsonPath().get("REFUND_LIST.STATUS").equals("PENDING")) {
                break;
            }
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public static Response getTxnStatusResponse(String pgpUrl, String mid, String orderId) {
        BaseApi api = new TransactionStatus(pgpUrl, mid, orderId);
        Response response = api.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    public static String getChecksum(String key, TreeMap<String, String> treeMap) {
        CheckSumServiceHelper checkSumServiceHelper = CheckSumServiceHelper.getCheckSumServiceHelper();
        String checksum = null;
        try {
            checksum = checkSumServiceHelper.genrateCheckSum(key, treeMap);
        } catch (Exception e) {
            report.info("<br>Exception occurred in generating checksum.");
        }
        return checksum;
    }

    public static String getChecksum(String key, String paramap) {
        StringBuilder response = new StringBuilder(paramap);
        response.append("|");
        String checkSumValue = null;

        try {
            Encryption encryption = EncryptionFactory.getEncryptionInstance("AES");
            String randomNo = CryptoUtils.generateRandomString(4);
            response.append(randomNo);
            String checkSumHash;
            checkSumHash = CryptoUtils.getSHA256(response.toString());
            checkSumHash = checkSumHash.concat(randomNo);
            System.out.println("-----------------------------");
            System.out.println(checkSumHash);
            System.out.println("-----------------------------");
            checkSumValue = encryption.encrypt(checkSumHash, key);
            if (checkSumValue != null) {
                checkSumValue = checkSumValue.replaceAll("\r\n", "");
                checkSumValue = checkSumValue.replaceAll("\r", "");
                checkSumValue = checkSumValue.replaceAll("\n", "");
            }
        } catch (SecurityException var8) {
            var8.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return checkSumValue;
    }

    public static List<String> getEMIDetailsFromDB(String mid, String bankCode, String col) {
        List<String> resultList = new ArrayList<>();
        List<Map<String, Object>> valueList = DbQueriesUtil.selectFromPaytmPGDB(DbQueries.GET_EMI_DETAILS(mid, bankCode, col));
        if (valueList.isEmpty()) {
            Reporter.report.info("No EMI Details found for this " + mid);
            return null;
        } else {
            for (int i = 0; i < valueList.size(); i++) {
                String var9 = valueList.get(i).get(col).toString();
                resultList.add(var9);
            }
            return resultList;
        }
    }

    public static StringBuilder getCheckSumStringForRefund(TreeMap<String, String> paramMap) throws Exception {
        Set<String> keys = paramMap.keySet();
        StringBuilder checkSumStringBuffer = new StringBuilder("");
        TreeSet<String> parameterSet = new TreeSet();
        Iterator var6 = keys.iterator();
        String paramName;
        while (var6.hasNext()) {
            paramName = (String) var6.next();
            if (!"CHECKSUMHASH".equalsIgnoreCase(paramName)) {
                parameterSet.add(paramName);
            }
        }
        var6 = parameterSet.iterator();
        while (var6.hasNext()) {
            String value;
            paramName = (String) var6.next();
            value = (String) paramMap.get(paramName);
            if (value == null || value.trim().equalsIgnoreCase("NULL")) {
                value = "";
            }
            checkSumStringBuffer.append(value).append("|");
        }
        return checkSumStringBuffer;
    }


    public static String generateChecksumRefund(String Key, TreeMap<String, String> paramap) {
        StringBuilder response = null;
        try {
            response = getCheckSumStringForRefund(paramap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String checkSumValue = null;

        try {
            Encryption encryption = EncryptionFactory.getEncryptionInstance("AES");
            String randomNo = CryptoUtils.generateRandomString(4);
            response.append(randomNo);
            String checkSumHash = null;
            try {
                checkSumHash = CryptoUtils.getSHA256(response.toString());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            checkSumHash = checkSumHash.concat(randomNo);
            try {
                checkSumValue = encryption.encrypt(checkSumHash, Key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkSumValue != null) {
                checkSumValue = checkSumValue.replaceAll("\r\n", "");
                checkSumValue = checkSumValue.replaceAll("\r", "");
                checkSumValue = checkSumValue.replaceAll("\n", "");
            }
        } catch (java.lang.SecurityException var8) {
            var8.printStackTrace();
        }
        return checkSumValue;
    }

    public void validateChecksumFromPaytmString(String paytmCheckSumString, String Key, String ChecksumSendToMerchant) {
        try {
            Encryption encryption = EncryptionFactory.getEncryptionInstance("AES");
            String checksumHashSendToMerchant = encryption.decrypt(ChecksumSendToMerchant, Key);
            String OldrandomNo = checksumHashSendToMerchant.substring(checksumHashSendToMerchant.length() - 4, checksumHashSendToMerchant.length());
            paytmCheckSumString = paytmCheckSumString.replaceAll("\r", "");
            paytmCheckSumString = paytmCheckSumString.replaceAll("\n", "");

            paytmCheckSumString = paytmCheckSumString.concat(OldrandomNo);
            String checkSumHashFromRequest;
            checkSumHashFromRequest = CryptoUtils.getSHA256(paytmCheckSumString);
            String checksumHashSendToMerchantWithoutRandom = checksumHashSendToMerchant.substring(0, checksumHashSendToMerchant.length() - 4);
            Assertions.assertThat(checkSumHashFromRequest).isEqualTo(checksumHashSendToMerchantWithoutRandom);
        } catch (SecurityException var8) {
            var8.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Executes getTxnStatusList api with checksum
     *
     * @param mid
     * @param merchantKey
     * @param orderId
     * @param txnType
     * @return
     */
    public static Response executeTxnStatusList(String pgpUrl, String mid, String merchantKey, String orderId, String txnType) {
        report.info("<br>Executing Txn Status List API");
        DriverManager.setCaptureScreenShot(false);
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("MID", mid);
        treeMap.put("ORDERID", orderId);
        treeMap.put("TXNTYPE", txnType);
        String checkSum = getChecksum(merchantKey, treeMap);
        BaseApi api = new TransactionStatusListSecured(pgpUrl, mid, orderId, txnType, checkSum);
        Response response = api.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        DriverManager.setCaptureScreenShot(true);
        return response;
    }

    public static Response getTxnStatusListResponse(String pgpUrl, String mid, String orderId, String txnType) {
        BaseApi api = new TransactionStatusList(pgpUrl, mid, orderId, txnType);
        Response response = api.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    public static String getEncryptedPaymentDetails(String merchantKey, String cardNumber, String expiryMonth, String expiryYear, String cvv) throws NoResultFoundException {
        try {
            report.info("<br>Fetching encrypted payment details");
            DriverManager.setCaptureScreenShot(false);
            String encryptedDetails = EncryptionFactory.getEncryptionInstance(EncryptConstants.ALGTHM_TYPE_AES).encrypt(getFormattedPaymentDetails(cardNumber, expiryMonth, expiryYear, cvv), merchantKey);
            report.info("<br>Encrypted payment details: " + encryptedDetails);
            return encryptedDetails;
        } catch (Exception e) {
            throw new NoResultFoundException("card details encryption failed", e);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static String getEncryptedPaymentDetails(String merchantKey, String savedCardId, String cvv) throws NoResultFoundException {
        try {
            report.info("<br>Fetching encrypted payment details");
            DriverManager.setCaptureScreenShot(false);
            String encryptedDetails = EncryptionFactory.getEncryptionInstance(EncryptConstants.ALGTHM_TYPE_AES).encrypt(getFormattedPaymentDetails(savedCardId, cvv), merchantKey);
            report.info("<br>Encrypted payment details: " + encryptedDetails);
            return encryptedDetails;
        } catch (Exception e) {
            throw new NoResultFoundException("card details encryption failed", e);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static String getFormattedPaymentDetails(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        report.info("<br>Fetching formatted payment details");
        DriverManager.setCaptureScreenShot(false);
        String formattedDetails = cardNumber + "|" + cvv + "|" + expiryMonth + expiryYear;
        report.info("<br>Formatted payment details: " + formattedDetails);
        DriverManager.setCaptureScreenShot(true);
        return formattedDetails;
    }

    public static String getFormattedPaymentDetails(String savedCardId, String cvv) {
        report.info("<br>Fetching formatted payment details");
        DriverManager.setCaptureScreenShot(false);
        String formattedDetails = savedCardId + "|" + cvv;
        report.info("<br>Formatted payment details: " + formattedDetails);
        DriverManager.setCaptureScreenShot(true);
        return formattedDetails;
    }


    public static String getEncryptedPaymentDetailsForExpress(String pgpUrl, String cardNumber, String expiryMonth, String expiryYear, String cvv, String custId, String mid) {
        report.info("<br>Fetching encrypted payment details");
        DriverManager.setCaptureScreenShot(false);
        BaseApi api = new GetCardToken(pgpUrl, mid, custId, cardNumber, expiryMonth, expiryYear, cvv);
        Response response = api.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat((String) jsonPath.get("STATUS")).isEqualToIgnoringCase("SUCCESS");
        String token = jsonPath.get("TOKEN");
        report.info("<br>Encrypted payment details: " + token);
        DriverManager.setCaptureScreenShot(true);
        return token;
    }

    public static String getEncryptedPaymentDetailsForExpress(String pgpUrl, String custId, String mid, String savedCardId, String cvv) {
        report.info("<br>Fetching encrypted payment details");
        DriverManager.setCaptureScreenShot(false);
        BaseApi api = new GetCardToken(pgpUrl, mid, custId, savedCardId, cvv);
        Response response = api.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat((String) jsonPath.get("STATUS")).isEqualToIgnoringCase("SUCCESS");
        String token = jsonPath.get("TOKEN");
        report.info("<br>Encrypted payment details: " + token);
        DriverManager.setCaptureScreenShot(true);
        return token;
    }


    private static String getAcquirementId(String PGPDBUrl, String subsId, String orderId) {
        report.info("<br>Fetching acquirement Id");
        DriverManager.setCaptureScreenShot(false);
        String query = "SELECT * FROM SUBS_ACC.subscription_payment_details WHERE subscription_id=" + subsId + " and order_id='" + orderId + "';";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
        if (result.size() == 0) {
            DriverManager.setCaptureScreenShot(true);
            return null;
        } else {
            String acquirementId = (String) result.get(0).get("acquirement_id");
            report.info("<br>Acquirement Id: " + acquirementId);
            DriverManager.setCaptureScreenShot(true);
            return acquirementId;
        }
    }

    public static String executeUntilAcquirementIdNotFound(String PGPDBUrl, String subsId, String orderId) {
        String acquirementId;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        acquirementId = getAcquirementId(PGPDBUrl, subsId, orderId);
        if (acquirementId == null || acquirementId.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                acquirementId = getAcquirementId(PGPDBUrl, subsId, orderId);
                if (acquirementId != null && !acquirementId.isEmpty()) {
                    return acquirementId;
                }
            }
        }
        return acquirementId;
    }

    public static String getSavedCardId(String PGPDBUrl, String subsId) throws NoResultFoundException {
        report.info("<br>Fetching Saved Card Id");
        DriverManager.setCaptureScreenShot(false);
        String query = "SELECT * FROM SUBS_ACC.subscription_contract_v2 WHERE subscription_id=" + subsId + "";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
        if (result.size() == 0) {
            throw new NoResultFoundException("Record not found");
        }
        Object savedCardIdTemp = result.get(0).get("saved_card_id");
        String savedCardId = (savedCardIdTemp == null ? null : savedCardIdTemp.toString());
        report.info("<br>Saved Card Id: " + savedCardId);
        DriverManager.setCaptureScreenShot(true);
        return savedCardId;
    }


    public static String getSubsDate(String PGPDBUrl, String subsId) throws NoResultFoundException {
        report.info("<br>Fetching Saved Card Id");
        DriverManager.setCaptureScreenShot(false);
        String query = "SELECT * FROM SUBS_ACC.subscription_contract_v2 WHERE subscription_id=" + subsId + "";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
        if (result.size() == 0) {
            throw new NoResultFoundException("Record not found");
        }
        Object subDueDateTemp = result.get(0).get("sub_due_date");
        String subDueDate = (subDueDateTemp == null ? null : subDueDateTemp.toString());
        report.info("<br>Subs Due Date: " + subDueDate);
        DriverManager.setCaptureScreenShot(true);
        return subDueDate;
    }


    public static String getExpiredMID(String PAYTMPGDBUrl) throws NoResultFoundException {
        String query = "SELECT MID FROM `ENTITY_INFO` WHERE `ID` IN( SELECT `ENTITY_ID` FROM ENTITY_LICENSE_INFO WHERE `VALID_TILL`< NOW()) LIMIT 1";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PAYTMPGDBUrl, query);
        if (result.size() == 0) {
            throw new NoResultFoundException("Record not found");
        }
        String mID = (String) result.get(0).get("MID");
        if (mID == null || mID.isEmpty()) {
            throw new NoResultFoundException("MID is either null or empty: " + mID);
        }
        return mID;
    }

    public static void modifySubscriptionStartDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubscriptionStartDate) {
        String query = "UPDATE SUBS_ACC.subscription_contract_v2 SET subcription_start_date='" + newSubscriptionStartDate + "' where subscription_id = '" + subscriptionId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionEndDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubscriptionEndDate) {
        String query = "UPDATE SUBS_ACC.subscription_contract_v2 SET subcription_end_date='" + newSubscriptionEndDate + "' where subscription_id = '" + subscriptionId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionDueDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubscriptionDueDate) {
        String query = "UPDATE SUBS_ACC.subscription_contract_v2 SET sub_due_date='" + newSubscriptionDueDate + "' where subscription_id = '" + subscriptionId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionPaymentCreateDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubscriptionCreateDate) {
        String query = "UPDATE SUBS_ACC.subscription_payment_details SET created_date='" + newSubscriptionCreateDate + "' where subscription_id = '" + subscriptionId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionPaymentUpdateDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubscriptionUpdateDate) {
        String query = "UPDATE SUBS_ACC.subscription_payment_details SET updated_date='" + newSubscriptionUpdateDate + "' where subscription_id = '" + subscriptionId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionPreNotifyDate(String PGPDBUrl, long paytmRefId, LocalDateTime newNotifyDate) {
        String query = "UPDATE SUBS_ACC.subscription_prenotify SET notify_date='" + newNotifyDate + "' where id = '" + paytmRefId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionPreNotifyStatus(String PGPDBUrl, long paytmRefId, String newNotifyStatus) {
        String query = "UPDATE SUBS_ACC.subscription_prenotify SET status='" + newNotifyStatus + "' where id = '" + paytmRefId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionPreNotifyTxnDate(String PGPDBUrl, long paytmRefId, LocalDateTime newNotifyTxnDate) {
        String query = "UPDATE SUBS_ACC.subscription_prenotify SET txn_date='" + newNotifyTxnDate + "' where id = '" + paytmRefId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }


    public static void modifySubscriptionUpidetailCreateTime(String PGPDBUrl, long subscriptionId, LocalDateTime newSubsUPICreateDate) {
        String query = "UPDATE SUBS_ACC.subscription_upi_details SET create_timestamp='" + newSubsUPICreateDate + "' where subscription_id = '" + subscriptionId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionUpidetailUpdateTime(String PGPDBUrl, long subscriptionId, LocalDateTime newSubsUPIUpdateDate) {
        String query = "UPDATE SUBS_ACC.subscription_upi_details SET update_timestamp='" + newSubsUPIUpdateDate + "' where subscription_id = '" + subscriptionId + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionPaymentCreateDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubsUPIUpdateDate, String paymentType) {
        String query = "UPDATE SUBS_ACC.subscription_payment_details SET created_date='" + newSubsUPIUpdateDate + "' where subscription_id = '" + subscriptionId + "' and payment_type ='" + paymentType + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }

    public static void modifySubscriptionPaymentUpdateDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubscriptionUpdateDate, String paymentType) {
        String query = "UPDATE SUBS_ACC.subscription_payment_details SET updated_date='" + newSubscriptionUpdateDate + "' where subscription_id = '" + subscriptionId + "' and payment_type ='" + paymentType + "';";
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }


    public static String getCCBillPaymentToken(String pgpHost, String cardNumber, String custId, String mid) {
        BaseApi api = new GetCardTokenBillPayment(pgpHost, mid, custId, cardNumber);
        Response response = api.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat((String) jsonPath.get("STATUS")).isEqualToIgnoringCase("SUCCESS");
        String token = jsonPath.get("TOKEN");
        return token;
    }

    public static void initiatePGOnlyCCTxn(String checkOutPageUrl, String mId, String cardNumber, String expMonth, String expYear, String cvv) {
        DriverManager.getDriver().get(checkOutPageUrl);
        executeJavaScript(
                fillTransactionDetails(((Long) RandomUtils.nextLong(0, Long.MAX_VALUE)).toString(), "3D", "OAUTH", "1.00", "DEFAULT") +
                        fillUserDetails("afaq101", "", "", "", "") +
                        fillMerchantDetails(mId, "", "retail", "retail", "merchant4", "WEB")
        );
        pay().click();

        tabCreditCard().click();
        textBoxCardNumber().clearAndType(cardNumber);
        dropdownExpiryMonth().selectByVisibleText(expMonth);
        dropdownExpiryYear().selectByVisibleText(expYear);
        textBoxCVVNumber().clearAndType(cvv);
        buttonPGPayNow().click();
        textOrderID().waitUntilVisible();
    }


    public static void initiateHybridCCTxn(String checkOutPageUrl, String mId, String ssoToken, String cardNumber, String expMonth, String expYear, String cvv) {
        DriverManager.getDriver().get(checkOutPageUrl);
        executeJavaScript(
                fillTransactionDetails(((Long) RandomUtils.nextLong(0, Long.MAX_VALUE)).toString(), "3D", "OAUTH", "2.0", "DEFAULT") +
                        fillUserDetails("afaq101", "", ssoToken, "", "") +
                        fillMerchantDetails(mId, "", "retail", "retail", "merchant4", "WEB")
        );
        pay().click();

        tabCreditCard().click();
        textBoxCardNumber().clearAndType(cardNumber);
        dropdownExpiryMonth().selectByVisibleText(expMonth);
        dropdownExpiryYear().selectByVisibleText(expYear);
        textBoxCVVNumber().clearAndType(cvv);
        buttonPGPayNow().click();
        textOrderID().waitUntilVisible();
    }

    private static synchronized Object executeJavaScript(String javaScript, Object... args) {
        Reporter.report.info("Execute javascript [" + javaScript + "]");
        WebDriver driver = DriverManager.getDriver();

        try {
            return ((JavascriptExecutor) driver).executeScript(javaScript, args);
        } catch (Throwable var4) {
            throw var4;
        }
    }

    private static String fillTransactionDetails(String orderId, String authMode, String tokenType, String txnAmt, String requestType) {
        return "document.getElementById('orderid').value='" + orderId + "';" +
                "document.getElementsByName('AUTH_MODE')[0].value='" + authMode + "';" +
                "document.getElementsByName('TOKEN_TYPE')[0].value='" + tokenType + "';" +
                "document.getElementsByName('TXN_AMOUNT')[0].value='" + txnAmt + "';" +
                "document.getElementsByName('REQUEST_TYPE')[0].value='" + requestType + "';";
    }

    private static String fillUserDetails(String custId, String mobNo, String ssoToken, String paytmToken, String email) {

        return "document.getElementById('CUST_ID').value='" + custId + "';" +
                "document.getElementById('MSISDN').value='" + mobNo + "';" +
                "document.getElementsByName('SSO_TOKEN')[0].value='" + ssoToken + "';" +
                "document.getElementsByName('PAYTM_TOKEN')[0].value='" + paytmToken + "';" +
                "document.getElementById('EMAIL').value='" + email + "';";
    }

    private static String fillMerchantDetails(String mId, String mKey, String industryTypeId, String website, String theme, String channelId) {
        return "document.getElementById('MID').value='" + mId + "';" +
                "document.getElementsByName('merchantKey')[0].value='" + mKey + "';" +
                "document.getElementById('INDUSTRY_TYPE_ID').value='" + industryTypeId + "';" +
                "document.getElementById('WEBSITE').value='" + website + "';" +
                "document.getElementById('THEME').value='" + theme + "';" +
                "document.getElementById('CHANNEL_ID').value='" + channelId + "';";
    }

    private static JsonPath saveCardDetails(String pgpUrl, String custId, String cardNumber, String expiryDate) {
        report.info("Save card where custId: " + custId + " cardNumber: " + cardNumber + " expiryDate: " + expiryDate);
        DriverManager.setCaptureScreenShot(false);
        BaseApi api = new AddSavedCard(pgpUrl, custId, cardNumber, expiryDate);
        Response response = api.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat((String) jsonPath.get("responseStatus")).isEqualToIgnoringCase("SUCCESS");
        DriverManager.setCaptureScreenShot(true);
        return jsonPath;
    }

    private static ArrayList<Integer> fetchSaveCardList(String pgpUrl, String custId) {
        report.info("Fetch saved card list for custId: " + custId);
        DriverManager.setCaptureScreenShot(false);
        BaseApi api = new GetSavedCard(pgpUrl, custId);
        Response response = api.execute();
        JsonPath jsonPath = response.jsonPath();
        ArrayList<Integer> cards;
        cards = jsonPath.get("response.cardId");
        report.info("Cards list: " + cards.toString());
        DriverManager.setCaptureScreenShot(true);
        return cards;
    }

    private static void deleteSavedCard(String pgpUrl, String custId, String saveCardId) {
        report.info("Delete saved card where custId: " + custId + " savedCardId: " + saveCardId);
        DriverManager.setCaptureScreenShot(false);
        BaseApi api = new DeleteSavedCard(pgpUrl, custId, saveCardId);
        Response response = api.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat((String) jsonPath.get("responseStatus")).isEqualToIgnoringCase("SUCCESS");
        DriverManager.setCaptureScreenShot(true);
    }

    public synchronized static JsonPath addCard(String pgpUrl, String custId, String expiryMonth, String expiryYear, String cardNumber) {
        report.info("Add card: " + cardNumber);
        DriverManager.setCaptureScreenShot(false);
        String expiryDate = expiryMonth.concat(expiryYear);
        JsonPath jsonPath = saveCardDetails(pgpUrl, custId, cardNumber, expiryDate);
        DriverManager.setCaptureScreenShot(true);
        return jsonPath;
    }

    public static void deleteSavedCard(String pgpUrl, String custId) {
        report.info("Delete saved cards for custId: " + custId);
        DriverManager.setCaptureScreenShot(false);
        ArrayList<Integer> cardIDs = fetchSaveCardList(pgpUrl, custId);
        for (int cardID : cardIDs) {
            deleteSavedCard(pgpUrl, custId, String.valueOf(cardID));
        }
        DriverManager.setCaptureScreenShot(true);
    }

    public static void validateSavedCardPresence(String pgpUrl, String custId) {
        report.info("Validate saved card's presence for custId: " + custId);
        DriverManager.setCaptureScreenShot(false);
        try {
            int MaxNoOfAttemptsToBeMade = 10;
            int totalNoOfAttemptsMade = 0;
            for (; totalNoOfAttemptsMade < MaxNoOfAttemptsToBeMade; totalNoOfAttemptsMade++) {
                if (fetchSaveCardList(pgpUrl, custId).size() != 0) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (totalNoOfAttemptsMade == MaxNoOfAttemptsToBeMade) {
                throw new AssertionError("User is expected to have saved cards but has not");
            }
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static String getSavedCardId(String pgpUrl, String custId, int index) {
        ArrayList<Integer> saveCardList = fetchSaveCardList(pgpUrl, custId);
        String savedCardId = Integer.toString(saveCardList.get(index));
        return savedCardId;
    }

    public static void validateSavedCardAbsence(String pgpUrl, String custId) {
        int MaxNoOfAttemptsToBeMade = 10;
        int totalNoOfAttemptsMade = 0;
        for (; totalNoOfAttemptsMade < MaxNoOfAttemptsToBeMade; totalNoOfAttemptsMade++) {
            if (fetchSaveCardList(pgpUrl, custId).size() == 0) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (totalNoOfAttemptsMade == MaxNoOfAttemptsToBeMade) {
            throw new AssertionError("User is expected to have no saved cards but has");
        }
    }

    public static String getSavedCardIdForSubscription(String pgpDBUrl, String subsId) throws NoResultFoundException {
        report.info("Get saved card Id for subs Id: " + subsId);
        DriverManager.setCaptureScreenShot(false);
        String query = "SELECT * FROM SUBS_ACC.subscription_contract_v2 WHERE subscription_id=" + subsId + "";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(pgpDBUrl, query);
        if (result.size() == 0) {
            throw new NoResultFoundException("Record not found");
        }
        String savedCardId = (String) result.get(0).get("saved_card_id");
        if (savedCardId == null || savedCardId.isEmpty()) {
            throw new NoResultFoundException("Saved Card Id is either null or empty: " + savedCardId);
        }
        report.info("Saved Card Id: " + savedCardId);
        DriverManager.setCaptureScreenShot(true);
        return savedCardId;
    }

    private String fillPaymentDetails(String bankCode, String paymentDetails, String storeCard, String addMoney, String walletAmt, String ccBillNo, String paymentTypeId) {
        return "document.getElementsByName('BANK_CODE')[0].value='" + bankCode + "';" +
                "document.getElementsByName('PAYMENT_DETAILS')[0].value='" + paymentDetails + "';" +
                "document.getElementsByName('STORE_CARD')[0].value='" + storeCard + "';" +
                "document.getElementsByName('addMoney')[0].value='" + addMoney + "';" +
                "document.getElementsByName('WALLET_AMOUNT')[0].value='" + walletAmt + "';" +
                "document.getElementsByName('CC_BILL_NO')[0].value='" + ccBillNo + "';" +
                "document.getElementsByName('PAYMENT_TYPE_ID')[0].value='" + paymentTypeId + "';";
    }

    private String fillSubscriptionDetails(String startDate, String expiryDate, String ppiOnly, String amtType, String maxAmt, String frequency, String graceDays, String enableRetry, String retryCount, String paymentMode, String savedCardId, String connectionType) {
        return "document.getElementsByName('SUBS_START_DATE')[0].value='" + startDate + "';" +
                "document.getElementsByName('SUBS_EXPIRY_DATE')[0].value='" + expiryDate + "';" +
                "document.getElementsByName('SUBS_PPI_ONLY')[0].value='" + ppiOnly + "';" +
                "document.getElementsByName('SUBS_AMOUNT_TYPE')[0].value='" + amtType + "';" +
                "document.getElementsByName('SUBS_MAX_AMOUNT')[0].value='" + maxAmt + "';" +
                "document.getElementsByName('SUBS_FREQUENCY')[0].value='" + frequency + "';" +
                "document.getElementsByName('SUBS_GRACE_DAYS')[0].value='" + graceDays + "';" +
                "document.getElementsByName('SUBS_ENABLE_RETRY')[0].value='" + enableRetry + "';" +
                "document.getElementsByName('SUBS_RETRY_COUNT')[0].value='" + retryCount + "';" +
                "document.getElementsByName('SUBS_PAYMENT_MODE')[0].value='" + paymentMode + "';" +
                "document.getElementsByName('SAVED_CARD_ID')[0].value='" + savedCardId + "';" +
                "document.getElementsByName('CONNECTION_TYPE')[0].value='" + connectionType + "';";
    }

    public static boolean isChecksumValid(String merchantKey, TreeMap<String, String> paramsMap, String checksum) throws PGPException {
        try {
            return CheckSumServiceHelper.getCheckSumServiceHelper().verifycheckSum(merchantKey, paramsMap, checksum);
        } catch (Exception e) {
            throw new PGPException("Exception occurred while verifying checksum", e);
        }
    }

    public static boolean isChecksumValid(String merchantKey, String paramsJson, String checksum) throws PGPException {
        try {
            return CheckSumServiceHelper.getCheckSumServiceHelper().verifycheckSumQueryStr(merchantKey, paramsJson, checksum);
        } catch (Exception e) {
            throw new PGPException("Exception occurred while verifying checksum", e);
        }
    }

}

