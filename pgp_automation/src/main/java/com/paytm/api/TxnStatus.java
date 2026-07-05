package com.paytm.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.PGPAPIResourcePath;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.encryptdecrypt.Aes256EncryptionDecryption;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.TxnStatusResponse;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.Matchers.hasKey;

/**
 * Created  by  sureshgupta  on  15/09/17.
 */
public class TxnStatus extends BaseApi {

    public static String NOT_PRESENT = "NOT_PRESENT";
    public static String EMPTY = "EMPTY";
    public static String NON_EMPTY = "NON_EMPTY";
    private static String STATUS_TXN_SUCCESS = "TXN_SUCCESS";
    private static String STATUS_TXN_FAILURE = "TXN_FAILURE";
    private static String RESPMSG_TXN_SUCCESS = "Txn Success";
    private static String RESPMSG_TXN_FAILURE = "ORDER IS CLOSE.";
    private static String RESPCODE_FAILURE = "810";
    private static String RESPCODE_SUCCESS = "01";
    private static String TXNTYPE_SALE = "SALE";
    private static String TXNTYPE_ADDMONEY = "ADDMONEY";
    private static String REFUND_AMT = "0.00";
    private static String ACCNUMVARSUCCESS = "true";    //  Variable  used  to  validate  the  account  number  sent  in  the  request  and  the  acccount  number  used  while  payment.

    public TxnStatusResponse txnStatusResponse;
    private SoftAssertions softly = new SoftAssertions();

    Set<String> parameters = new ConcurrentSkipListSet<>();
    Response response;

    private String getMerchantKey(String mid) {
        return PGPHelpers.getMerchantKey(mid);
    }

    public TxnStatus()
    {

    }

    public TxnStatus(final String mid, final String orderId) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        String checksum = PGPUtil.getChecksum(getMerchantKey(mid), treemap);

        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.TXNSTATUS_CHECKSUM);
        getRequestSpecBuilder().addParam("JsonData", "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId + "\",\"CHECKSUMHASH\":\"" + checksum + "\"}");

    }

    public TxnStatus getNativeStatus(final String mid, final String orderId) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        String checksum = PGPUtil.getChecksum(getMerchantKey(mid), treemap);

        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.NATIVE_TXNSTATUS);
        getRequestSpecBuilder().addParam("JsonData", "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId + "\",\"CHECKSUMHASH\":\"" + checksum + "\"}");
        return this;
    }

    public TxnStatus(final String mid, final String orderId, final String merchantKey, final boolean checksumFlag) {
        String checksum = null;
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        if (checksumFlag == true) {
            checksum = PGPUtil.getChecksum(merchantKey != null ? merchantKey : getMerchantKey(mid), treemap);
        }

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.TXNSTATUS_CHECKSUM);
        getRequestSpecBuilder().addParam("JsonData", "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId + "\",\"CHECKSUMHASH\":\"" + checksum + "\"}");
    }


    public TxnStatus getNativeStatus(final String mid, final String orderId, final String merchantKey, final boolean checksumFlag) {
        String checksum = null;
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        if (checksumFlag == true) {
            checksum = PGPUtil.getChecksum(merchantKey != null ? merchantKey : getMerchantKey(mid), treemap);
        }

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.NATIVE_TXNSTATUS);
        getRequestSpecBuilder().addParam("JsonData", "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId + "\",\"CHECKSUMHASH\":\"" + checksum + "\"}");
        return this;
    }

    @Override
    public Response execute() {
        Response response = super.execute();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);

        try {
            this.txnStatusResponse = mapper.readValue(jsonObject.toJSONString(), TxnStatusResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        this.txnStatusResponse = response.as(TxnStatusResponse.class);
        this.response = response;//intitializing the global variable
        return response;
    }

    public TxnStatus executeEncrypted(String merchantKey) throws PGPException{
        Response response = super.execute().then()
                .statusCode(200)
                .body("", hasKey("encParams"))
                .extract().response();
        String encParams = response.jsonPath().getString("encParams");
        String decryptedResponse = Aes256EncryptionDecryption.decrypt(encParams, merchantKey);

        JSONObject jo = new JSONObject();
        try {
            for (String s : decryptedResponse.split("\\|")) {
                String k = s.substring(0, s.indexOf("="));
                String v = s.substring(s.indexOf("=") + 1);

                if (k.equalsIgnoreCase("CHILDTXNLIST")) {
                    JSONParser p = new JSONParser();
                    if (v.startsWith("[")) {
                        jo.put(k, (JSONArray) p.parse(v));
                    } else {
                        jo.put(k, (JSONObject) p.parse(v));
                    }
                } else
                    jo.put(k, v);
            }
            System.out.println("txnStatus response:===== " + jo.toJSONString());
            Reporter.report.info("txnStatus response:===== " + jo.toJSONString());

        } catch (Exception e) {
            throw new PGPException("Exception occurred while decrypting request");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);

        try {
            this.txnStatusResponse = mapper.convertValue(jo, TxnStatusResponse.class);
            this.response = response;
            return this;
        } catch (IllegalArgumentException e) {
            throw new PGPException("Exception occurred when parsing to txnStatusResponse: ", e);
        }

    }


    public TxnStatus executeUntilNotPending(Duration duration) {
        try {
        with().pollInSameThread().await().pollInterval(Duration.FIVE_SECONDS).atMost(duration).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("STATUS")).isNotEqualToIgnoringCase("PENDING"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was not to be :- PENDING  but found to be "+this.execute().jsonPath().getString("STATUS"));
        }
        return this;
    }


    public TxnStatus executeUntilNotPending() {
        try {
       with().pollInSameThread().await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("STATUS")).isNotEqualToIgnoringCase("PENDING"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was not to be :- PENDING  but found to be "+this.execute().jsonPath().getString("STATUS"));
        }
        return this;
    }

    public TxnStatus executeUntilPending() {
        try {
        with().pollInSameThread().await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("STATUS")).isEqualToIgnoringCase("PENDING"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was :- PENDING  but found to be "+this.execute().jsonPath().getString("STATUS"));
        }
        return this;
    }

    public TxnStatus validateStatusAPIParameters() {

        Set<String> mismatchSet = new ConcurrentSkipListSet<>(parameters);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        String cardIndexNo="cardIndexNo";
        String bankResultInfo="bankResultInfo";
        String RRNCODE="RRNCODE";
        for (Object param : jsonObject.keySet()) {
            if(!cardIndexNo.equals(param.toString()) && !bankResultInfo.equals(param.toString()) && !RRNCODE.equals(param.toString())) {
                //mismatchSet contains expected values
                //param are values coming from merchant status response
                if (!mismatchSet.add(param.toString())) //add method will return false if expected values already contains status response parameters
                {
                    mismatchSet.remove(param.toString());
                }
            }
        }

        this.softly.assertThat(mismatchSet).as(mismatchSet + " is/are not getting compared").isEmpty();
        return this;
    }

    @Step
    public TxnStatus validateSuccessResponse() {
        validateStatus(STATUS_TXN_SUCCESS);
        validateRespCode(RESPCODE_SUCCESS);
        validateRespMsg(RESPMSG_TXN_SUCCESS);
        return this;
    }

    @Step
    public TxnStatus validateFailureResponse(String respCode_expected, String failureMsg_expected) {
        validateStatus(STATUS_TXN_FAILURE);
        validateRespCode(respCode_expected);
        validateRespMsg(failureMsg_expected);
        return this;
    }

    public TxnStatus validateCardIndexNo_isEmpty() {
        this.softly.assertThat(txnStatusResponse.getCardIndexNo()).isNullOrEmpty();
        return this;
    }


    public TxnStatus validateCardIndexNo_isNotEmpty() {
        this.softly.assertThat(txnStatusResponse.getCardIndexNo()).isNotEmpty();
        return this;
    }

    public TxnStatus validateAccNumVarSuccess(String ACCNUMVARSUCCESS) {
        this.softly.assertThat(txnStatusResponse.getACCNUMVARSUCCESS()).as("ACCNUMVARSUCCESS").isEqualToIgnoringCase(ACCNUMVARSUCCESS);
        return this;
    }

    public TxnStatus validateMid(String expectedMid) {
        validateExpectedAndActual("MID", txnStatusResponse.getMID(), expectedMid);
        return this;
    }

    public TxnStatus validateFinalPaymentAmount(String expectedFinalPaymentAmount) {
        String actual = txnStatusResponse.getFinalPaymentAmount();
        boolean missing = actual == null || actual.trim().isEmpty();
        if (missing) {
            this.softly.assertThat(missing)
                    .as("finalPaymentAmount missing in txn status API (null/empty); expected [%s]. "
                            + "Remove this validation for flows that do not return finalPaymentAmount.",
                            expectedFinalPaymentAmount)
                    .isFalse();
        } else {
            validateExpectedAndActual("finalPaymentAmount", Double.parseDouble(actual), Double.parseDouble(expectedFinalPaymentAmount));
        }
        return this;
    }

    public TxnStatus validateMid(ValidationType validationType) {
        validate("MID", txnStatusResponse.getMID(), validationType);
        return this;
    }

    public TxnStatus validateCardIndexNo(String expected) {
        validateExpectedAndActual("cardIndexNo", txnStatusResponse.getCardIndexNo(), expected);
        return this;
    }

    public TxnStatus validateCardIndexNo(ValidationType validationType) {
        validate("cardIndexNo", txnStatusResponse.getCardIndexNo(), validationType);
        return this;
    }

    public TxnStatus validateCardHash(String expected) {
        validateExpectedAndActual("cardHash", txnStatusResponse.getCardHash(), expected);
        return this;
    }

    public TxnStatus validateCardHash(ValidationType validationType) {
        validate("cardHash", txnStatusResponse.getCardHash(), validationType);
        return this;
    }

    public TxnStatus validateOrderid(String expectedOrderid) {

        validateExpectedAndActual("ORDERID", txnStatusResponse.getORDERID(), expectedOrderid);
        return this;
    }

    public TxnStatus validateOrderid(ValidationType validationType) {
        validate("ORDERID", txnStatusResponse.getORDERID(), validationType);
        return this;
    }
    public TxnStatus validateTxnId(ValidationType validationType) {
        validate("TXNID", txnStatusResponse.getTXNID(), validationType);
        return this;
    }

    public TxnStatus validateBankTxnId(String bankTxnId) {
        this.softly.assertThat(txnStatusResponse.getBANKTXNID()).as("BANKTXNID mismatch").isEqualToIgnoringCase(bankTxnId);
        return this;
    }

    public TxnStatus validateBankTxnId(ValidationType validationType) {
        validate("BANKTXNID", txnStatusResponse.getBANKTXNID(), validationType);
        return this;
    }

    @Step
    public TxnStatus validateStatus(String expectedStatus) {
        validateExpectedAndActual("STATUS", txnStatusResponse.getSTATUS(), expectedStatus);
        return this;
    }

    public TxnStatus validateStatus(ValidationType validationType) {
        validate("STATUS", txnStatusResponse.getSTATUS(), validationType);
        return this;
    }


    public TxnStatus validateTxnAmount(String expectedTxnAmount) {

        validateExpectedAndActual("TXNAMOUNT", Double.parseDouble(txnStatusResponse.getTXNAMOUNT()), Double.parseDouble(expectedTxnAmount));
        return this;
    }

    public TxnStatus validateTxnAmount(ValidationType validationType) {
        validate("TXNAMOUNT", txnStatusResponse.getTXNAMOUNT(), validationType);
        return this;
    }

    public TxnStatus validateTxnType(String expectedTxnType) {
        validateExpectedAndActual("TXNTYPE", txnStatusResponse.getTXNTYPE(), expectedTxnType);
        return this;
    }

    public TxnStatus validateTxnType(ValidationType validationType) {
        validate("TXNTYPE", txnStatusResponse.getTXNTYPE(), validationType);
        return this;
    }

    public TxnStatus validateGatewayName(String expectedGatewayName) {
        validateExpectedAndActual("GATEWAYNAME", txnStatusResponse.getGATEWAYNAME(), expectedGatewayName);
        return this;
    }

    public TxnStatus validateGatewayName(ValidationType validationType) {
        validate("GATEWAYNAME", txnStatusResponse.getGATEWAYNAME(), validationType);
        return this;
    }

    public TxnStatus validateMERC_UNQ_REF(String expectedMERC_UNQ_REF) {
        validateExpectedAndActual("MERC_UNQ_REF", txnStatusResponse.getMERC_UNQ_REF(), expectedMERC_UNQ_REF);
        return this;
    }

    @Step
    public TxnStatus validateRespCode(String expectedRespCode) {
        validateExpectedAndActual("RESPCODE", txnStatusResponse.getRESPCODE(), expectedRespCode);
        return this;
    }

    public TxnStatus validateRespCode(ValidationType validationType) {
        validate("RESPCODE", txnStatusResponse.getRESPCODE(), validationType);
        return this;
    }

    @Step
    public TxnStatus validateRespMsg(String expectedRespMsg) {
        if ("Txn Successful.".equals(expectedRespMsg)) {
            expectedRespMsg = "Txn Success";
        }
        validateExpectedAndActual("RESPMSG", txnStatusResponse.getRESPMSG(), expectedRespMsg);
        return this;
    }

    public TxnStatus validateRespMsg(ValidationType validationType) {
        validate("RESPMSG", txnStatusResponse.getRESPMSG(), validationType);
        return this;
    }


    public TxnStatus validateBankName(String expectedBankName) {
        parameters.add("BANKNAME");
        if(txnStatusResponse.getBANKNAME() == null && expectedBankName ==null)
        {}
        else {
            this.softly.assertThat(txnStatusResponse.getBANKNAME()).as("BANKNAME" + " mismatch").contains(expectedBankName);
        }
//        validateExpectedAndActual("BANKNAME",txnStatusResponse.getBANKNAME(),expectedBankName);
        return this;
    }

    public TxnStatus validateBankName(ValidationType validationType) {

        validate("BANKNAME", txnStatusResponse.getBANKNAME(), validationType);
        return this;
    }

    public TxnStatus validatePaymentMode(String expectedPaymentMode) {
        if ("PAYTM_DIGITAL_CREDIT".equals(expectedPaymentMode)) {
            expectedPaymentMode = "Paytm Postpaid";
        }

        validateExpectedAndActual("PAYMENTMODE", txnStatusResponse.getPAYMENTMODE(), expectedPaymentMode);
        return this;
    }

    public TxnStatus validatePaymentMode(ValidationType validationType) {
        validate("PAYMENTMODE", txnStatusResponse.getPAYMENTMODE(), validationType);
        return this;
    }


    public TxnStatus validateRiskInfo(ValidationType validationType) {
        validate("RISKINFO", txnStatusResponse.getRiskInfo(), validationType);
        return this;
    }

    public TxnStatus validateRiskInfo(String riskInfo) {
        this.softly.assertThat(txnStatusResponse.getRiskInfo()).as("Risk Info mismatch").isEqualToIgnoringCase(riskInfo);
        return this;
    }

    public TxnStatus validateSubsid(String subsId) {
        this.softly.assertThat(txnStatusResponse.getSUBS_ID()).as("SUBS_ID mismatch").isEqualToIgnoringCase(subsId);
        return this;
    }

    public TxnStatus validateSplitSettlementInfo(String settlementInfo) {
        this.softly.assertThat(txnStatusResponse.getSplitSettlementInfo()).as("Split Settlement Info mismatch").contains(settlementInfo);
        return this;
    }


    public TxnStatus validateSubsid(ValidationType validationType) {
        validate("SUBS_ID", txnStatusResponse.getSUBS_ID(), validationType);
        return this;
    }

    public TxnStatus validateRefundAmnt(String expectedRefundAmnt) {

        validateExpectedAndActual("REFUNDAMT", Double.parseDouble(txnStatusResponse.getREFUNDAMT()), Double.parseDouble(expectedRefundAmnt));
        return this;
    }

    public TxnStatus validatePayableAmount(String expectedPayableAmount) {

        validateExpectedAndActual("PAYABLE_AMOUNT", Double.parseDouble(txnStatusResponse.getPAYABLE_AMOUNT()), Double.parseDouble(expectedPayableAmount));
        return this;
    }

    public TxnStatus validatePaymentPromoCheckoutDataPresent() {

        this.softly.assertThat(txnStatusResponse.getPaymentPromoCheckoutData()).as("Payment Promo Checkout Data is not present").isNotNull();
        return this;
    }

    public TxnStatus validatePaymentPromoCheckoutDataNotPresent() {

        this.softly.assertThat(txnStatusResponse.getPaymentPromoCheckoutData()).as("Payment Promo Checkout Data is present").isNull();
        return this;
    }

    public TxnStatus validateEmiSubventionInfoPresent() {

        this.softly.assertThat(txnStatusResponse.getEmiSubventionInfo()).as("EMI Subvention Info is not present").isNotNull();
        return this;
    }

    public TxnStatus validateEmiSubventionInfoNotPresent() {

        this.softly.assertThat(txnStatusResponse.getEmiSubventionInfo()).as("EMI Subvention Info is present").isNull();
        return this;
    }


    public TxnStatus validateVPA(String vpa) {

        validateExpectedAndActual("VPA", txnStatusResponse.getVPA(),vpa);
        return this;
    }

    public TxnStatus validatePrepaidCard(String prepaidCardValue) {

        validateExpectedAndActual("PREPAIDCARD", txnStatusResponse.getPrepaidCard(),prepaidCardValue);
        return this;
    }

    public TxnStatus validateRefundAmnt(ValidationType validationType) {
        validate("REFUNDAMT", txnStatusResponse.getREFUNDAMT(), validationType);
        return this;
    }

    public TxnStatus validateTxnDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedExpectedDate = sdf.format(date);
        Date actualDate;
        try {
            actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(txnStatusResponse.getTXNDATE());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String formattedActualDate = sdf.format(actualDate);

        validateExpectedAndActual("TXNDATE", formattedActualDate, formattedExpectedDate);

        return this;
    }


    public TxnStatus validateCardBin(String expectedCardBin) {
        validateExpectedAndActual("BIN", txnStatusResponse.getBIN(), expectedCardBin);
        return this;
    }

    public TxnStatus validateLastFourDigits(String expectedLastFourDigits) {
        validateExpectedAndActual("LASTFOURDIGITS", txnStatusResponse.getLASTFOURDIGITS(), expectedLastFourDigits);
        return this;
    }

    public TxnStatus validateAdditionalParam(ValidationType validationType) {
        validate("ADDITIONAL_PARAM", txnStatusResponse.getAdditionalParam(), validationType);
        return this;
    }


    public TxnStatus validateCardScheme(String expectedCardScheme) {
        validateExpectedAndActual("cardScheme", txnStatusResponse.getCardScheme(), expectedCardScheme);
        return this;
    }

    public TxnStatus validateCurrentTxnCount(ValidationType validationType) {
        validate("currentTxnCount", txnStatusResponse.getCurrentTxnCount(), validationType);
        return this;
    }

    public TxnStatus validatePRN(ValidationType validationType) {
        validate("PRN", txnStatusResponse.getPRN(), validationType);
        return this;
    }

    public TxnStatus validateCountryCode(String country_code){
        validateExpectedAndActual("COUNTRY_CODE",txnStatusResponse.getCOUNTRY_CODE(),country_code);
        return this;
    }


    public TxnStatus validatebinIrcId(String binIrcId){
        validateExpectedAndActual("binIrcId",txnStatusResponse.getBinIrcId(),binIrcId);
        return this;
    }

    public TxnStatus validatebinIrcIdNotPresent() {
        this.softly.assertThat(txnStatusResponse.getBinIrcId()).isNullOrEmpty();
        return this;
    }
    public TxnStatus validateBIN_IDENTIFIER(String BIN_IDENTIFIER){
        validateExpectedAndActual("BIN_IDENTIFIER",txnStatusResponse.getBIN_IDENTIFIER(),BIN_IDENTIFIER);
        return this;
    }

    public TxnStatus validateBIN_IDENTIFIERNotPresent() {
        this.softly.assertThat(txnStatusResponse.getBIN_IDENTIFIER()).isNullOrEmpty();
        return this;
    }

    public TxnStatus validateTxnDate(ValidationType validationType) {
        validate("TXNDATE", txnStatusResponse.getTXNDATE(), validationType);
        return this;
    }

    public TxnStatus validateErrorCode(String errorCode) {
        this.softly.assertThat(txnStatusResponse.getERRORCODE()).as("errorCode mismatch").isEqualToIgnoringCase(errorCode);
        return this;
    }

    public TxnStatus validateErrorMessage(String errorMessage) {
        this.softly.assertThat(txnStatusResponse.getERRORMESSAGE()).as("errorMessage mismatch").isEqualToIgnoringCase(errorMessage);
        return this;
    }

    public TxnStatus validateChargeAmount(String chargeAmount) {
        validateExpectedAndActual("chargeAmount", txnStatusResponse.getChargeAmount(), chargeAmount);
        this.softly.assertThat(txnStatusResponse.getChargeAmount()).as("Charge Amount mismatch").isEqualToIgnoringCase(chargeAmount);
        return this;
    }

    public TxnStatus validateChargeAmount(ValidationType validationType) {
        validate("chargeAmount", txnStatusResponse.getChargeAmount(), validationType);
        return this;
    }

    public TxnStatus validateChildTxnsNotPresent() {
        this.softly.assertThat(txnStatusResponse.getChildTxns()).as("ChildTxns is present").isNull();
        return this;
    }

    public TxnStatus validateChildTxnsPresent() {
        parameters.add("CHILDTXNLIST");
        this.softly.assertThat(txnStatusResponse.getChildTxns()).as("ChildTxns is not present").isNotNull();
        return this;
    }

    public TxnStatus validateChildTxnNotPresent(ChildTxnType childTxnType) {
        TxnStatusResponse.ChildTxn bankTxn = txnStatusResponse.getChildTxnDetails(childTxnType);
        this.softly.assertThat(bankTxn).as(childTxnType.toString()).isNull();
        return this;
    }


    public TxnStatus validateChildTxnPresent(ChildTxnType childTxnType) {
        TxnStatusResponse.ChildTxn bankTxn = txnStatusResponse.getChildTxnDetails(childTxnType);
        this.softly.assertThat(bankTxn).as(childTxnType.toString()).isNotNull();
        return this;
    }

    public TxnStatus validateTxnId(ChildTxnType childTxnType, String expectedTxnId) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getTXNID()).as(childTxnType.toString() + ": TXNID").isEqualToIgnoringCase(expectedTxnId);
        return this;
    }

    public TxnStatus validateTxnId(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": TXNID", txnStatusResponse.getChildTxnDetails(childTxnType).getTXNID(), validationType);
        return this;
    }

    public TxnStatus validateBankTxnId(ChildTxnType childTxnType, String expectedBankTxnId) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getBANKTXNID()).as(childTxnType.toString() + ": BANKTXNID").isEqualToIgnoringCase(expectedBankTxnId);
        return this;
    }

    public TxnStatus validateBankTxnId(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": BANKTXNID", txnStatusResponse.getChildTxnDetails(childTxnType).getBANKTXNID(), validationType);
        return this;
    }

    public TxnStatus validatePaymentMode(ChildTxnType childTxnType, String expectedPaymentMode) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getPAYMENTMODE()).as(childTxnType.toString() + ": PAYMENTMODE").isEqualToIgnoringCase(expectedPaymentMode);
        return this;
    }

    public TxnStatus validatePaymentMode(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": PAYMENTMODE", txnStatusResponse.getChildTxnDetails(childTxnType).getPAYMENTMODE(), validationType);
        return this;
    }

    public TxnStatus validateTxnAmount(ChildTxnType childTxnType, String expectedTxnAmount) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getTXNAMOUNT()).as(childTxnType.toString() + ": TXNAMOUNT").isEqualToIgnoringCase(expectedTxnAmount);
        return this;
    }

    public TxnStatus validateTxnAmount(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": TXNAMOUNT", txnStatusResponse.getChildTxnDetails(childTxnType).getTXNAMOUNT(), validationType);
        return this;
    }

    public TxnStatus validateGatewayName(ChildTxnType childTxnType, String expectedGatewayName) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getGATEWAYNAME()).as(childTxnType.toString() + ": GATEWAYNAME").isEqualToIgnoringCase(expectedGatewayName);
        return this;
    }

    public TxnStatus validateGatewayName(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": GATEWAYNAME", txnStatusResponse.getChildTxnDetails(childTxnType).getGATEWAYNAME(), validationType);
        return this;
    }

    public TxnStatus validateBankName(ChildTxnType childTxnType, String expectedBankName) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getBANKNAME()).as(childTxnType.toString() + ": BANKNAME").contains(expectedBankName);
        return this;
    }

    public TxnStatus validateBankName(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": BANKNAME", txnStatusResponse.getChildTxnDetails(childTxnType).getBANKNAME(), validationType);
        return this;
    }

    public TxnStatus validateStatus(ChildTxnType childTxnType, String expectedStatus) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getSTATUS()).as(childTxnType.toString() + ": STATUS").isEqualToIgnoringCase(expectedStatus);
        return this;
    }

    public TxnStatus validateStatus(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": STATUS", txnStatusResponse.getChildTxnDetails(childTxnType).getSTATUS(), validationType);
        return this;
    }

    public TxnStatus validateBIN(ChildTxnType childTxnType, String expectedStatus) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getBIN()).as(childTxnType.toString() + ": BIN").isEqualToIgnoringCase(expectedStatus);
        return this;
    }

    public TxnStatus validateBIN(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": BIN", txnStatusResponse.getChildTxnDetails(childTxnType).getBIN(), validationType);
        return this;
    }

    public TxnStatus validateLastFourDigits(ChildTxnType childTxnType, String expectedStatus) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getLASTFOURDIGITS()).as(childTxnType.toString() + ": LASTFOURDIGITS").isEqualToIgnoringCase(expectedStatus);
        return this;
    }

    public TxnStatus validateLastFourDigits(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": LASTFOURDIGITS", txnStatusResponse.getChildTxnDetails(childTxnType).getLASTFOURDIGITS(), validationType);
        return this;
    }

    public TxnStatus validateCardBin(ChildTxnType childTxnType, String expectedStatus) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getBIN()).as(childTxnType.toString() + ": BIN").isEqualToIgnoringCase(expectedStatus);
        return this;
    }

    public TxnStatus validateCardBin(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": BIN", txnStatusResponse.getChildTxnDetails(childTxnType).getBIN(), validationType);
        return this;
    }

    public TxnStatus validateCardScheme(ChildTxnType childTxnType, String expectedCardScheme) {
        this.softly.assertThat(txnStatusResponse.getChildTxnDetails(childTxnType).getCardScheme()).as(childTxnType.toString() + ": cardScheme").isEqualToIgnoringCase(expectedCardScheme);
        return this;
    }

    public TxnStatus validateCardScheme(ChildTxnType childTxnType, ValidationType validationType) {
        validate(childTxnType + ": cardScheme", txnStatusResponse.getChildTxnDetails(childTxnType).getCardScheme(), validationType);
        return this;
    }

    public enum ChildTxnType {
        BANK("CHILDTXNBANK"),
        WALLET("CHILDTXNWALLET"),
        COD("CHILDTXNCOD"),
        PPBEX("CHILDTXNPPBEX"),
        DC("CHILDTXNDC"),
        CC("CHILDTXNCC"),
        NB("CHILDTXNNB"),
        STCHC("CHILDTXNSTCHC");


        private final String childTxnType;

        ChildTxnType(String childTxnType) {
            this.childTxnType = childTxnType;
        }

        @Override
        public String toString() {
            return childTxnType;
        }

    }

    /*private void validate(String fieldName, Object actual, ValidationType validationType) {

        parameters.add(fieldName);
        count++;
        if (validationType == ValidationType.NON_EMPTY) {
            this.softly.assertThat(actual.toString()).as(fieldName + " is Empty").isNotEmpty();
        } else if (validationType == ValidationType.NOT_PRESENT) {
            this.softly.assertThat(actual).as(fieldName + " is not null").isNull();
        } else if (validationType == ValidationType.EMPTY) {
            this.softly.assertThat(actual.toString()).as(fieldName + " is not empty").isEmpty();
        } else {
            throw new RuntimeException("Incorrect validation type: " + validationType);
        }
    }

*/

    private void validate(String fieldName, Object actual, ValidationType validationType) {
        parameters.add(fieldName);
        if (validationType == ValidationType.NON_EMPTY) {
            this.softly.assertThat(((String) actual)).as("expecting " + fieldName + " to be present but it is not").isNotNull();
            this.softly.assertThat(((String) actual)).as("expecting " + fieldName + " to be non-empty but it is not").isNotEmpty();
        } else if (validationType == ValidationType.NOT_PRESENT) {
            this.softly.assertThat(((String) actual)).as("expecting " + fieldName + " to be absent but it is not").isNull();
        } else if (validationType == ValidationType.EMPTY) {
            this.softly.assertThat(((String) actual)).as("expecting " + fieldName + " to be empty but it is not").isEmpty();
        } else {
            throw new RuntimeException("Incorrect validation type: " + validationType);
        }

    }


    public void validateExpectedAndActual(String fieldName, String actualValue, String expectedValue) {
        parameters.add(fieldName);
        this.softly.assertThat(actualValue).as(fieldName + " mismatch").isEqualToIgnoringCase(expectedValue);

    }

    public void validateExpectedAndActual(String fieldName, Double actualValue, Double expectedValue) {
        parameters.add(fieldName);
        this.softly.assertThat(actualValue.floatValue()).as(fieldName + " mismatch").isEqualByComparingTo(expectedValue.floatValue());

    }

    public TxnStatus AssertAll() {

//        ExtentTestManager.getTest().pass("<div style='color:#FDE541'>verify txn status api</div>");
        this.softly.assertAll();
        return this;
    }

    public TxnStatusResponse getResponse() {
        return txnStatusResponse;
    }

    public Response getApiResponse() {
        return response;
    }

    public TxnStatus validatePGOnlySuccess(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        this.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(STATUS_TXN_SUCCESS)
                .validateTxnType(TXNTYPE_SALE)
                .validateGatewayName(gatewayName)
                .validateRespCode(RESPCODE_SUCCESS)
                .validateRespMsg(RESPMSG_TXN_SUCCESS)
                .validateBankName(bankName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(paymentMode)
                .validateRefundAmnt(REFUND_AMT)
                .validateTxnDate(new Date());
        return this;
    }

    public TxnStatus validatePGOnlyFailure(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        this.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(gatewayName)
                .validateRespCode(RESPCODE_FAILURE)
                .validateRespMsg(RESPMSG_TXN_FAILURE)
                .validateBankName(bankName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(paymentMode)
                .validateRefundAmnt(REFUND_AMT)
                .validateTxnDate(new Date());
        return this;
    }

    public TxnStatus validateAddMoneyMPSuccess(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        return validatePGOnlySuccess(orderDTO, gatewayName, bankName, paymentMode);
    }

    public TxnStatus validateAddMoneyMPFailure(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        return validatePGOnlyFailure(orderDTO, gatewayName, bankName, paymentMode);
    }

    public TxnStatus validateAddMoneyUberSuccess(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        this.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(STATUS_TXN_SUCCESS)
                .validateTxnType(TXNTYPE_ADDMONEY)
                .validateGatewayName(gatewayName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(paymentMode)
                .validateRespCode(RESPCODE_SUCCESS)
                .validateRespMsg(RESPMSG_TXN_SUCCESS)
                .validateBankName(bankName)
                .validateRefundAmnt(REFUND_AMT)
                .validateTxnDate(new Date());
        return this;
    }

    public TxnStatus validateAddMoneyUberFailure(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        this.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(STATUS_TXN_FAILURE)
                .validateTxnType(TXNTYPE_ADDMONEY)
                .validateGatewayName(gatewayName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(paymentMode)
                .validateRespCode(RESPCODE_FAILURE)
                .validateRespMsg(RESPMSG_TXN_FAILURE)
                .validateBankName(bankName)
                .validateRefundAmnt(REFUND_AMT)
                .validateTxnDate(new Date());
        return this;
    }

    public TxnStatus validateAddnPaySuccess(OrderDTO orderDTO) {
        this.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(STATUS_TXN_SUCCESS)
                .validateTxnType(TXNTYPE_SALE)
                .validateGatewayName("WALLET")
                .validateRespCode(RESPCODE_SUCCESS)
                .validateRespMsg(RESPMSG_TXN_SUCCESS)
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt(REFUND_AMT)
                .validateTxnDate(new Date());
        return this;
    }

    public TxnStatus validateAddnPayFailure(OrderDTO orderDTO) {
        this.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(STATUS_TXN_FAILURE)
                .validateTxnType(TXNTYPE_SALE)
                .validateGatewayName("WALLET")
                .validateRespCode(RESPCODE_FAILURE)
                .validateRespMsg(RESPMSG_TXN_FAILURE)
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt(REFUND_AMT)
                .validateTxnDate(new Date());
        return this;
    }

    public TxnStatus validatePayTMExpressSuccess(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        return validatePGOnlySuccess(orderDTO, gatewayName, bankName, paymentMode);
    }

    public TxnStatus validatePayTMExpressFailure(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        return validatePGOnlyFailure(orderDTO, gatewayName, bankName, paymentMode);
    }

    public TxnStatus validateSeamlessSuccess(OrderDTO orderDTO, String gatewayName, String paymentMode) {
        return validatePGOnlySuccess(orderDTO, gatewayName, null, paymentMode);
    }

    public TxnStatus validateSeamlessFailure(OrderDTO orderDTO, String gatewayName, String paymentMode) {
        return validatePGOnlyFailure(orderDTO, gatewayName, null, paymentMode);
    }

    public TxnStatus validateSeamlessNativeSuccess(OrderDTO orderDTO, String gatewayName, String paymentMode) {
        return validatePGOnlySuccess(orderDTO, gatewayName, null, paymentMode);
    }

    public TxnStatus validateSeamlessNativeFailure(OrderDTO orderDTO, String gatewayName, String paymentMode) {
        return validatePGOnlyFailure(orderDTO, gatewayName, null, paymentMode);
    }

    public TxnStatus validateTopUpExpressSuccess(OrderDTO orderDTO, String gatewayName, String paymentMode) {
        return validateAddMoneyUberSuccess(orderDTO, gatewayName, "", paymentMode);
    }

    public TxnStatus validateTopUpExpressFailure(OrderDTO orderDTO, String gatewayName, String paymentMode) {
        return validateAddMoneyUberFailure(orderDTO, gatewayName, "", paymentMode);
    }

    public TxnStatus validateWalletOnlySuccess(OrderDTO orderDTO) {
        return validateAddnPaySuccess(orderDTO);
    }

    public TxnStatus validateWalletOnlyFailure(OrderDTO orderDTO) {
        return validateAddnPayFailure(orderDTO);
    }

    public TxnStatus validateCCBillPaymentSuccess(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        return validatePGOnlySuccess(orderDTO, gatewayName, bankName, paymentMode);
    }

    public TxnStatus validateCCBillPaymentFailure(OrderDTO orderDTO, String gatewayName, String bankName, String paymentMode) {
        return validatePGOnlyFailure(orderDTO, gatewayName, bankName, paymentMode);
    }

    public TxnStatus validatePaytmDigitalCardSuccess(OrderDTO orderDTO) {
        return validatePGOnlySuccess(orderDTO, "PAYTMCC", null, "PAYTM_DIGITAL_CREDIT");
    }

    public TxnStatus validateUpiModeSubType(String expectedUpiModeSubType) {
        validateExpectedAndActual("UPI_MODE_SUB_TYPE", txnStatusResponse.getUPI_MODE_SUB_TYPE(), expectedUpiModeSubType);
        return this;
    }




    /*public void validateResponse(TxnStatusResponse expectedTxnDetails) {
        if (!validate("TXNID", this.txnStatusResponse.getTXNID(), expectedTxnDetails.getTXNID()))
            this.softly.assertThat(this.txnStatusResponse.getTXNID()).isEqualTo(expectedTxnDetails.getTXNID()).as("TXNID");
        if (!validate("BANKTXNID", this.txnStatusResponse.getBANKTXNID(), expectedTxnDetails.getBANKTXNID()))
            this.softly.assertThat(this.txnStatusResponse.getBANKTXNID()).isEqualTo(expectedTxnDetails.getBANKTXNID()).as("BANKTXNID");
        if (!validate("ORDERID", this.txnStatusResponse.getORDERID(), expectedTxnDetails.getORDERID()))
            this.softly.assertThat(this.txnStatusResponse.getORDERID()).isEqualTo(expectedTxnDetails.getORDERID()).as("ORDERID");
        if (!validate("TXNAMOUNT", this.txnStatusResponse.getTXNAMOUNT(), expectedTxnDetails.getTXNAMOUNT()))
            this.softly.assertThat(this.txnStatusResponse.getTXNAMOUNT()).isEqualTo(expectedTxnDetails.getTXNAMOUNT()).as("TXNAMOUNT");
        if (!validate("STATUS", this.txnStatusResponse.getSTATUS(), expectedTxnDetails.getSTATUS()))
            this.softly.assertThat(this.txnStatusResponse.getSTATUS()).isEqualTo(expectedTxnDetails.getSTATUS()).as("STATUS");
        if (!validate("TXNTYPE", this.txnStatusResponse.getTXNTYPE(), expectedTxnDetails.getTXNTYPE()))
            this.softly.assertThat(this.txnStatusResponse.getTXNTYPE()).isEqualTo(expectedTxnDetails.getTXNTYPE()).as("TXNTYPE");
        if (!validate("GATEWAYNAME", this.txnStatusResponse.getGATEWAYNAME(), expectedTxnDetails.getGATEWAYNAME()))
            this.softly.assertThat(this.txnStatusResponse.getGATEWAYNAME()).isEqualTo(expectedTxnDetails.getGATEWAYNAME()).as("GATEWAYNAME");
        if (!validate("RESPCODE", this.txnStatusResponse.getRESPCODE(), expectedTxnDetails.getRESPCODE()))
            this.softly.assertThat(this.txnStatusResponse.getRESPCODE()).isEqualTo(expectedTxnDetails.getRESPCODE()).as("RESPCODE");
        if (!validate("RESPMSG", this.txnStatusResponse.getRESPMSG(), expectedTxnDetails.getRESPMSG()))
            this.softly.assertThat(this.txnStatusResponse.getRESPMSG()).isEqualTo(expectedTxnDetails.getRESPMSG()).as("RESPMSG");
        if (!validate("BANKNAME", this.txnStatusResponse.getBANKNAME(), expectedTxnDetails.getBANKNAME()))
            this.softly.assertThat(this.txnStatusResponse.getBANKNAME()).isEqualTo(expectedTxnDetails.getBANKNAME()).as("BANKNAME");
        if (!validate("MID", this.txnStatusResponse.getMID(), expectedTxnDetails.getMID()))
            this.softly.assertThat(this.txnStatusResponse.getMID()).isEqualTo(expectedTxnDetails.getMID()).as("MID");
        if (!validate("PAYMENTMODE", this.txnStatusResponse.getPAYMENTMODE(), expectedTxnDetails.getPAYMENTMODE()))
            this.softly.assertThat(this.txnStatusResponse.getPAYMENTMODE()).isEqualTo(expectedTxnDetails.getPAYMENTMODE()).as("PAYMENTMODE");
        if (!validate("SUBS_ID", this.txnStatusResponse.getSUBS_ID(), expectedTxnDetails.getSUBS_ID()))
            this.softly.assertThat(this.txnStatusResponse.getSUBS_ID()).isEqualTo(expectedTxnDetails.getSUBS_ID()).as("SUBS_ID");
        if (!validate("REFUNDAMT", this.txnStatusResponse.getREFUNDAMT(), expectedTxnDetails.getREFUNDAMT()))
            this.softly.assertThat(this.txnStatusResponse.getREFUNDAMT()).isEqualTo(expectedTxnDetails.getREFUNDAMT()).as("REFUNDAMT");
        if (!validate("CHILDTXNBANK", this.txnStatusResponse.getChildTxns()[0], expectedTxnDetails.getChildTxns()[0]))
            this.validateChildTxn(this.txnStatusResponse.getChildTxns()[0], expectedTxnDetails.getChildTxns()[0]);
        if (!validate("CHILDTXNWALLET", this.txnStatusResponse.getChildTxns()[1], expectedTxnDetails.getChildTxns()[1]))
            this.validateChildTxn(this.txnStatusResponse.getChildTxns()[1], expectedTxnDetails.getChildTxns()[1]);

        this.softly.assertAll();
    }
*/
  /*  private boolean validate(String fieldName, Object actual, Object expected) {
        if (expected != null) {
            if (expected == ValidationType.NON_EMPTY) {
                this.softly.assertThat(actual.toString()).isNotEmpty().as(fieldName);
                return true;
            } else if (expected == ValidationType.NOT_PRESENT) {
                this.softly.assertThat(actual).isEqualTo(null).as(fieldName);
                return true;
            } else if (expected == ValidationType.EMPTY) {
                this.softly.assertThat(actual.toString()).as(fieldName).isEmpty();
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
*/

  /*    public void validateChildTxn(TxnStatusResponse.ChildTxn actual, TxnStatusResponse.ChildTxn expected) {
        if (!validate("TXNID", actual.getTXNID(), expected.getTXNID()))
            this.softly.assertThat(actual.getTXNID()).isEqualToIgnoringCase(expected.getTXNID()).as("TXNID");
        if (!validate("PAYMENTMODE", actual.getPAYMENTMODE(), expected.getPAYMENTMODE()))
            this.softly.assertThat(actual.getPAYMENTMODE()).isEqualToIgnoringCase(expected.getPAYMENTMODE()).as("PAYMENTMODE");
        if (!validate("TXNAMOUNT", actual.getTXNAMOUNT(), expected.getTXNAMOUNT()))
            this.softly.assertThat(actual.getTXNAMOUNT()).isEqualToIgnoringCase(expected.getTXNAMOUNT()).as("TXNAMOUNT");
        if (!validate("GATEWAYNAME", actual.getGATEWAYNAME(), expected.getGATEWAYNAME()))
            this.softly.assertThat(actual.getGATEWAYNAME()).isEqualToIgnoringCase(expected.getGATEWAYNAME()).as("GATEWAYNAME");
        if (!validate("BANKTXNID", actual.getBANKTXNID(), expected.getBANKTXNID()))
            this.softly.assertThat(actual.getBANKTXNID()).isEqualToIgnoringCase(expected.getBANKTXNID()).as("BANKTXNID");
        if (!validate("BANKNAME", actual.getBANKNAME(), expected.getBANKNAME()))
            this.softly.assertThat(actual.getBANKNAME()).isEqualToIgnoringCase(expected.getBANKNAME()).as("BANKNAME");
        if (!validate("STATUS", actual.getSTATUS(), expected.getSTATUS()))
            this.softly.assertThat(actual.getSTATUS()).isEqualToIgnoringCase(expected.getSTATUS()).as("STATUS");
    }*/

}
