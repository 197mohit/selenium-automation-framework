package scripts.api.savecardService;

import com.paytm.LocalConfig;
import com.paytm.api.saveCard.SavedCardApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.saveCard.BinDetails;
import com.paytm.dto.saveCard.SaveCardRequestGeneric;
import com.paytm.dto.saveCard.SaveCardResponse;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.dto.saveCard.SavedcardOpenAPIServiceCardTypeSsoToken.Body;
import com.paytm.dto.saveCard.SavedcardOpenAPIServiceCardTypeSsoToken.SavedcardOpenAPIServiceCardTypeSsoTokenRequest;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.RedisUtil;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.paytm.framework.reporting.Reporter.report;

/**
 * Created by anjukumari on 04/07/18
 */
@Owner("Deepak")
public class SaveCard extends PGPBaseTest {


    private String EncCardNum = "EEjsWPA5Aea5KYnsCsLN8fB9jSdlxbQqulS5gQ9v6Aw=";
    private String EncExp = "KXWY2XwzQepdHBZuF4BQQA==";
    public String AesEncCardNumCredit = "23x+MnTWrvIrm136o6EllXZuVIDgxbZOKdMyihuOVIw=";
    public String encrSavedVpa="7NT1INkF7UQlyVFE2FJXO19bK9Ya9E0nVBrOhXQO+NE=";
    public String AesEncExp = "yDOQlmOW7hFDTDPqd8QvTg==";
    public String AesEncCardNumDebit = "VbQH0TJkHr0Q9b78FypLaZmyzONihCqA12Xlgaa+THc=";
    public String AesEncExpDebit = "YGaFPwX5vHV3Oxwer9va9Q==";
    private String mid = "";
    private String custId = "test";
    SoftAssert softAssert = new SoftAssert();
    PaymentDTO paymentDTO = new PaymentDTO();
    private String saveCardNum;
    private String expiry;
    String userId_SAVED_CARD_INFO = "select user_id from SAVED_CARD_INFO where card_id='{card_id}'";
    String mid_custId_userId_SAVED_MID_CARD_INFO = "select mid, cust_id, user_id from SAVED_MID_CARD_INFO where card_id='{card_id}'";
    String status_SAVED_CARD_INFO = "select status from SAVED_CARD_INFO where card_id='{card_id}'";


    @BeforeClass
    public void getMid() {
        mid = Constants.MerchantType.PGOnly.getId();
    }

    @BeforeTest
    public void disableScreenShotCapture() {
        DriverManager.setCaptureScreenShot(false);
    }

    private SaveCard assertFromDb(String query, String key, Object toCompare) {
        Object val = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0).get(key);
        softAssert.assertEquals(toCompare, val);
        return this;
    }

    private SaveCard assertAll() {
        softAssert.assertAll();
        return this;
    }


    public void validateSavedCardResponseBody(SaveCardResponse saveCardResponse, String cardNum) throws Exception {
        Assertions.assertThat(saveCardResponse.getCardId()).isNotNull();
        Assertions.assertThat(saveCardResponse.getCardScheme()).isNotEmpty();
        BinDetails binDetails = PGPHelpers.getBinDetails(cardNum.substring(0, 6));
        Assertions.assertThat(binDetails.getCardName()).isEqualToIgnoringCase(saveCardResponse.getCardScheme()).withFailMessage("card scheme did not matched");
        Assertions.assertThat(binDetails.getCardType()).isEqualToIgnoringCase(saveCardResponse.getCardType()).withFailMessage("card type did not matched");
        report.info("Save card response body Validation done");

    }

    public void validateSavedCardResponse(SavedCardHelpers savedCardHelpers) {
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("responseStatus").toString()).isEqualToIgnoringCase("SUCCESS");
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("httpCode").toString()).isEqualToIgnoringCase("200");
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("httpSubCode").toString()).isEqualToIgnoringCase("200");
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("codeDetail").toString()).isEqualToIgnoringCase("SUCCESS");
        report.info("Save card response Validation for success response");

    }

    public void validateSavedCardInvalidResponse(SavedCardHelpers savedCardHelpers) {
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("responseStatus").toString()).isEqualToIgnoringCase("FAILURE");
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("httpCode").toString()).isEqualToIgnoringCase("400");
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("httpSubCode").toString()).isEqualToIgnoringCase("406");
        Assertions.assertThat(savedCardHelpers.getJsonPath().get("codeDetail").toString()).isEqualToIgnoringCase("Invalid card number");
        report.info("Save card response Validation for failure response");
    }

    @Test(description = "Validate success response for trusted save card API for HDFC CC card")
    public void saveTrustedCard_HDFC_CC() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        SaveCardResponse saveCardResponse;
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.deleteSavedCard(user);
        saveCardNum = paymentDTO.getCreditCardNumber();
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        saveCardResponseBase = savedCardHelpers.saveTrustedCard(saveCardNum, user.custId(), expiry);
        validateSavedCardResponse(savedCardHelpers);
        saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
        validateSavedCardResponseBody(saveCardResponse, saveCardNum);
        savedCardHelpers.ValidateSaveCardDbEntry(saveCardNum, user.custId(), saveCardResponse.getCardId().toString());
    }


    @Test(description = "Validate success response in trusted save card API for AMEX card")
    public void saveTrustedCard_AMEX() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        SaveCardResponse saveCardResponse;
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.deleteSavedCard(user);
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        saveCardResponseBase = savedCardHelpers.saveTrustedCard(saveCardNum, user.custId(), expiry);
        validateSavedCardResponse(savedCardHelpers);
        saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
        validateSavedCardResponseBody(saveCardResponse, saveCardNum);
        savedCardHelpers.ValidateSaveCardDbEntry(saveCardNum, user.custId(), saveCardResponse.getCardId().toString());
    }

    //disabled TC as it is not valid
   // @Test(description = "Validate saved card response with invalid card number", enabled = false)
    public void saveTrustedCard_InvalidCard() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        User user = userManager.getForWrite(Label.BASIC);
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        savedCardHelpers.saveTrustedCard("12345678901234", user.custId(), expiry);
        validateSavedCardInvalidResponse(savedCardHelpers);
    }

    @Test(description = "Validate card scheme field in response of trusted savecard API ")
    public void saveTrused_ValidateScheme() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        SaveCardResponse saveCardResponse;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.deleteSavedCard(user);
        saveCardResponseBase = savedCardHelpers.saveTrustedCard(paymentDTO.AMEX_CARD_NUMBER, user.custId(), expiry);
        validateSavedCardResponse(savedCardHelpers);
        saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
        validateSavedCardResponseBody(saveCardResponse, paymentDTO.AMEX_CARD_NUMBER);
    }

    @Test(description = "Validate success response in trusted savecard API for Invalid user id")
    public void saveTrused_InvalidUserId() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        SaveCardResponse saveCardResponse;
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        saveCardResponseBase = savedCardHelpers.saveTrustedCard(saveCardNum, "12345", expiry);
        validateSavedCardResponse(savedCardHelpers);
        saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
        validateSavedCardResponseBody(saveCardResponse, saveCardNum);
    }


    @Test(description = "Validate same card can be saved on different user id by trusted savecard API")
    public void saveTrused_SaveDifferentUser_withSameCard() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        SaveCardResponse saveCardResponse;
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        saveCardResponseBase = savedCardHelpers.saveTrustedCard(saveCardNum, "12345", expiry);
        validateSavedCardResponse(savedCardHelpers);
        saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
        validateSavedCardResponseBody(saveCardResponse, saveCardNum);
        saveCardResponseBase = savedCardHelpers.saveTrustedCard(saveCardNum, "1235", expiry);
        validateSavedCardResponse(savedCardHelpers);
        saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
        validateSavedCardResponseBody(saveCardResponse, saveCardNum);
    }


    @Test(description = "Validate successful save card for multiple card on same user")
    public void saveTrused_SameCard_OnSameUser() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        SaveCardResponse saveCardResponse;
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        saveCardResponseBase = savedCardHelpers.saveTrustedCard(saveCardNum, "12345", expiry);
        validateSavedCardResponse(savedCardHelpers);
        saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
        validateSavedCardResponseBody(saveCardResponse, saveCardNum);
    }


    @Test(description = "Validate encrypted Expiry and encrypted cardNumer saved in DB by trusted save card API")
    public void saveTrused_ValidateExpiryInDB() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        saveCardNum = paymentDTO.getCreditCardNumber();
        savedCardHelpers.saveTrustedCard(saveCardNum, "12345", "072030");
        String card_id = savedCardHelpers.getSavedSaveCardId_FromDB_ByUserAndCardNum("12345", saveCardNum);
        savedCardHelpers.ValidateEncrSaveCardExpiry_FromDB(card_id, AesEncExp);
        // Commenting below assertion as card encryption is giving different values for different runs. Need to check encryption logic
        //savedCardHelpers.ValidateEncrSaveCardNumber_FromDB(card_id, AesEncCardNumCredit);
    }


    @Test(description = "Validate card saved by trusted save card API is in active status")
    public void saveTrused_validateCardIsActive() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.deleteSavedCard(user);
        savedCardHelpers.saveTrustedCard(saveCardNum, user.custId(), expiry);
        validateSavedCardResponse(savedCardHelpers);
        String card_id = savedCardHelpers.getSavedSaveCardId_FromDB_ByUserAndCardNum(user.custId(), saveCardNum);
        savedCardHelpers.ValidateCardStatus(card_id, "true");
    }


    //SAVE_CARD_DETAILS_IN_CACHE
    @Test(description = "Validate success response for SAVE_CARD_DETAILS_IN_CACHE")
    public void saveCardInCache() throws Exception {
        String txnId = CommonHelpers.generateOrderId();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardResponseBase = savedCardHelpers.saveCardCache(user.custId(), EncCardNum, EncExp, paymentDTO.AMEX_CARD_NUMBER, txnId);
        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
    }

    @Test(description = "Validate entry in cache for SAVE_CARD_DETAILS_IN_CACHE API")
    public void saveCardInCache_ValidateCache() throws Exception {
        String txnId = CommonHelpers.generateOrderId();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardResponseBase = savedCardHelpers.saveCardCache(user.custId(), EncCardNum, EncExp, saveCardNum, txnId);
        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
        CommonHelpers.validateCache("savedcard_" + txnId);
    }

    @Test(description = "Validate Failure SAVE_CARD_DETAILS_IN_CACHE API response when TxnId=blank")
    public void saveCardInCache_WithTxnID_Blank() throws Exception {
        String txnId = "";
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardResponseBase = savedCardHelpers.saveCardCache(user.custId(), EncCardNum, EncExp, saveCardNum, txnId);
        savedCardHelpers.validateSavedCardResponse_Failure(saveCardResponseBase);
    }

    @Test(description = "Validate failure response for SAVE_CARD_DETAILS_IN_CACHE API when status is alphanumeric")
    public void saveCardInCache_Status_nonInteger() throws Exception {
        String txnId = "";
        saveCardNum = paymentDTO.AMEX_CARD_NUMBER;
        User user = userManager.getForWrite(Label.BASIC);
        int cardLength = saveCardNum.length();
        String cardFirstSix = saveCardNum.substring(0, 6);
        String cardLastFour = saveCardNum.substring(cardLength - 4, cardLength);
        SaveCardRequestGeneric saveCardRequestGeneric = new SaveCardRequestGeneric();
        saveCardRequestGeneric.setCardNumber(EncCardNum);
        saveCardRequestGeneric.setUserId(user.custId());
        saveCardRequestGeneric.setStatus("a");
        saveCardRequestGeneric.setCardType("0");
        saveCardRequestGeneric.setExpiryDate(EncExp);
        saveCardRequestGeneric.setFirstSixDigit(cardFirstSix);
        saveCardRequestGeneric.setLastFourDigit(cardLastFour);
        Assertions.assertThat(SavedCardApi.saveCardDetailCache(saveCardRequestGeneric).getStatusCode()).isEqualTo(400);
        report.info("Response validation done for invalid status in request");
    }

    //SAVE_TRANSACTION_CARD_DETAIL
    @Test(description = "Validate successful response for SAVE_TRANSACTION_CARD_DETAIL when txn details are saved in cache")
    public void saveCard_saveTxnCardDetail_FromCache() throws Exception {
        String txnId = CommonHelpers.generateOrderId();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        saveCardNum = paymentDTO.getDebitCardNumber();
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.saveCardCache(user.custId(), AesEncCardNumDebit, AesEncExpDebit, "4444333322221111", txnId);
        CommonHelpers.validateCache("savedcard_" + txnId);
        saveCardResponseBase = savedCardHelpers.saveCardDetail_ToDB_FromCache(txnId);
        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
        String card_id = saveCardResponseBase.getResponse().toString();
        Assertions.assertThat(card_id).isNotNull();
    }

    @Test(description = "Validate success response for SAVE_TRANSACTION_CARD_DETAIL when cache details are not saved in cache")
    public void saveCard_saveTxnCardDetail_FromCache2() throws Exception {
        String txnId = CommonHelpers.generateOrderId();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        saveCardResponseBase = savedCardHelpers.saveCardDetail_ToDB_FromCache(txnId);
        Assertions.assertThat(saveCardResponseBase.getCodeDetail()).isEqualToIgnoringCase("Request cannot be rendered. Not found");
    }

    @Test(description = "Validate entry in SAVED_CARD_INFO, SAVED_MID_CARD_INFO and SAVED_BIN_INFO DB when SAVE_TRANSACTION_CARD_DETAIL API is sucessful")
    public void saveCard_saveTxnCardDetail_FromCache3() throws Exception {
        String txnId = CommonHelpers.generateOrderId();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        saveCardNum = paymentDTO.getDebitCardNumber();
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.saveCardCache(user.custId(), AesEncCardNumDebit, AesEncExpDebit, "4444333322221111", txnId);
        CommonHelpers.validateCache("savedcard_" + txnId);
        saveCardResponseBase = savedCardHelpers.saveCardDetail_ToDB_FromCache(txnId);
        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
        String card_id = saveCardResponseBase.getResponse().toString();
        savedCardHelpers.validateSaveCardDB_ByCardID(card_id);
    }

    @Test(description = "Validate key from redis deleted when SAVE_TRANSACTION_CARD_DETAIL is successful")
    public void saveCard_saveTxnCardDetail_FromCache4() throws Exception {
        String txnId = CommonHelpers.generateOrderId();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        saveCardNum = paymentDTO.getDebitCardNumber();
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.saveCardCache(user.custId(), AesEncCardNumDebit, AesEncExpDebit, "4444333322221111", txnId);
        CommonHelpers.validateCache("savedcard_" + txnId);
        saveCardResponseBase = savedCardHelpers.saveCardDetail_ToDB_FromCache(txnId);
        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
        savedCardHelpers.validateCacheForDelete("savecard_" + txnId);
    }


    @Test(description = "Validate failure for SAVE_TRANSACTION_CARD_DETAIL whe card details saved in cache is Invalid")
    public void saveCard_saveTxnCardDetail_FromCache5() throws Exception {
        String txnId = CommonHelpers.generateOrderId();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase;
        saveCardNum = paymentDTO.getDebitCardNumber();
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        User user = userManager.getForWrite(Label.BASIC);
        savedCardHelpers.saveCardCache(user.custId(), "VbQH0TJkHr0Q9b78FypLaZ2Xlgaa+THc=", "YGaFPwX5va9Q==", "4444333322221111", txnId);
        CommonHelpers.validateCache("savedcard_" + txnId);
        saveCardResponseBase = savedCardHelpers.saveCardDetail_ToDB_FromCache(txnId);
        Assertions.assertThat(saveCardResponseBase.getCodeDetail()).isEqualToIgnoringCase("Something broken, unknown exception occured");
        savedCardHelpers.validateCacheForDelete("savecard_" + txnId);
    }

    //SAVE_CARD_BY_USER_ID
//    @Test(description = "Validate success response for SAVE_CARD_BY_USER_ID API")
//    public void savecard_USER_ID() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, saveCardNum);
//        Assertions.assertThat(responseBase.getResponse()).isNotNull();
//    }

//    @Test(description = "Validate entry in SAVED_CARD_INFO, SAVED_MID_CARD_INFO and SAVED_BIN_INFO DB when save card by userId API is successful")
//    public void savecard_USER_ID1() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, saveCardNum);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        savedCardHelpers.validateSaveCardDB_ByCardID(responseBase.getResponse().toString());
//    }

//    @Test(description = "Validate failure for SAVE_CARD_BY_USER_ID API when encryption card detail in request is Invalid")
//    public void savecard_USER_ID2() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCardUserId(user.custId(), "VbQH0TJkHr0Q9b78FypLaZmyhCqA12Xlgaa+THc=", "YGaFPwX5vHV3Oxwer9va9Q==", saveCardNum);
//        Assertions.assertThat(responseBase.getCodeDetail()).isEqualToIgnoringCase("Something broken, unknown exception occured");
//    }

    //SAVE_CARD_BY_MID_CUSTID
//    @Test(description = "Validate success response for SAVE_CARD_BY_MID_CUSTID API")
//    public void savecard_CUSTID_MID() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        mid = CommonHelpers.generateOrderId();
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry);
//        String cardId = responseBase.getResponse().toString();
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        this.assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "user_id", null)
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "mid", mid)
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "cust_id", custId).assertAll();
//    }


//    @Test(description = "Validate DB entry for MID and cust ID in SAVED_MID_CARD_INFO after success response from SAVE_CARD_BY_MID_CUSTID")
//    public void savecard_CUSTID_MID_validate_SAVED_MID_CARD_INFO() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        String cardId = responseBase.getResponse().toString();
//        this.assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "mid", mid)
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "cust_id", custId)
//                .assertAll();
//    }

//    @Test(description = "Validate key in Redis for successful save card by SAVE_CARD_BY_MID_CUSTID")
//    public void savecard_CUSTID_MID_validate_Redis() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        CommonHelpers.validateCache("USER_CARDS_V4_" + mid + "_" + custId);
//        CommonHelpers.validateCache("PG_USER_CARDS_V4_" + mid + "_" + custId);
//    }

//    @Test(description = "Validate card saved by SAVE_CARD_BY_MID_CUSTID has status=1")
//    public void savecard_CUSTID_MID_validate_status() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        String cardId = responseBase.getResponse().toString();
//        this.assertFromDb(status_SAVED_CARD_INFO.replace("{card_id}", cardId), "status", true).assertAll();
//
//    }

//    @Test(description = "Validate encrypted card number and expiry in DB for successful save card by SAVE_CARD_BY_MID_CUSTID API")
//    public void savecard_CUSTID_MID_validate_checkEncyptedDetails() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, UUID.randomUUID().toString(), "072021");
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        String cardID = responseBase.getResponse().toString();
//        savedCardHelpers.ValidateEncrSaveCardExpiry_FromDB(cardID, AesEncExpDebit);
//        savedCardHelpers.ValidateEncrSaveCardNumber_FromDB(cardID, AesEncCardNumDebit);
//    }

//    @Test(description = "Validate user is able to save multiple card on same merchant")
//    public void savecard_CUSTID_MID_validate_MultipleCardOnSameUser() throws Exception {
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        report.info("One card has been saved to user now save another card for validation");
//        SaveCardResponseBase responseBase1 = savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, mid, expiry);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase1);
//    }


    //GET_SAVED_CARD_BY_USER_ID
//    @Test(description = "Validate success response of get savecard on user id API")
//    public void getSaveCard_userId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber());
//        Long cardId = Long.parseLong(responseBase.getResponse().toString());
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_userId(user);
//        SaveCardResponse cardDetails = saveCardResponseBase.getResponseInSaveCardResponseList().get(0);
//        cardDetails.validateCardNumber(AesEncCardNumDebit)
//                .validateCardId(cardId)
//                .validateCardScheme("DEBIT_CARD")
//                .validateFirstSixDigit("444433")
//                .validateLastFour("1111")
//                .validateCardType("0")
//                .validateExpiry(AesEncExpDebit)
//                .validateStatus(1)
//                .validateUserId(user.custId())
//                .validateCardScheme("DEBIT_CARD")
//                .assertAll();
//    }

//    @Test(description = "Validate last four digit in savecard API response")
//    public void getSaveCard_userId_validate_0InLastFourDigit() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExpDebit, paymentDTO.getCreditCardNumber());
//        Long cardId = Long.parseLong(responseBase.getResponse().toString());
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_userId(user);
//        SaveCardResponse cardDetails = saveCardResponseBase.getResponseInSaveCardResponseList().get(0);
//        cardDetails.validateCardNumber(AesEncCardNumCredit)
//                .validateCardId(cardId)
//                .validateCardScheme("CREDIT_CARD")
//                .validateFirstSixDigit("479947")
//                .validateLastFour("6601")
//                .validateCardType("0")
//                .validateExpiry(AesEncExpDebit)
//                .validateStatus(1)
//                .validateUserId(user.custId())
//                .validateCardScheme("CREDIT_CARD")
//                .assertAll();
//    }

//    @Test(description = "Validate last four digit in ccBillPayment saved Card API response")
//    public void getSaveCard_OpenAPIService_savedcardsBycardTypeSsoToken_validate_0InLastFourDigit() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        PaymentDTO paymentDTO=new PaymentDTO();
//        paymentDTO.setCreditCardNumber("4718650100010336");
//       // savedCardHelpers.saveCardUserId(user.custId(),encrSavedVpa,"","test@paytm","","9");
//        savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExpDebit, paymentDTO.getCreditCardNumber(),"336","0");
//        Body body=new Body("CC",user.ssoToken());
//        SavedcardOpenAPIServiceCardTypeSsoTokenRequest savedcardOpenAPIServiceCardTypeSsoTokenRequest =new SavedcardOpenAPIServiceCardTypeSsoTokenRequest();
//        savedcardOpenAPIServiceCardTypeSsoTokenRequest.setBody(body);
//        Response response = SavedCardApi.getCardDetailForSavedcardOpenAPIServiceBycardTypeSsoToken(savedcardOpenAPIServiceCardTypeSsoTokenRequest);
//        int cardLength=paymentDTO.getCreditCardNumber().length();
//        Assertions.assertThat(response.jsonPath().get("body.savedCards[0].cardNumber").toString()).as("Saved Card's last 4 digits mismatched.").endsWith(paymentDTO.getCreditCardNumber().substring(cardLength-4, cardLength));
//        Assertions.assertThat(response.jsonPath().get("body.savedCards[0].displayName").toString()).as("Saved Cards DisplayName is Incorrect").isNotNull().isNotEmpty();
//    }


//    @Test(description = "Validate success response of get savecard on user id when multiple card saved on user. Also validate ordering of saved card response")
//    public void getSaveCard_userId_multiple() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber());
//        SaveCardResponseBase responseBase1 = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExp, paymentDTO.getCreditCardNumber());
//        int noOfCardsSaved = 2;
//        Long debitCardId = Long.parseLong(responseBase.getResponse().toString());
//        Long creditCardId = Long.parseLong(responseBase1.getResponse().toString());
//        report.info("Two card has been saved to user now validate number of saved card");
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_userId(user);
//        List<SaveCardResponse> saveCardResponseList = saveCardResponseBase.getResponseInSaveCardResponseList();
//
//        Assertions.assertThat(saveCardResponseList.size()).as("No. of cards saved is not as expected").isEqualTo(noOfCardsSaved);
//        //Validating saved card presence for debit card
//        Optional<SaveCardResponse> savedDebitCard = saveCardResponseList.stream()
//                .filter(saveCardResponse -> saveCardResponse.getCardId().toString().equals(debitCardId.toString()))
//                .findAny();
//        Assertions.assertThat(savedDebitCard.isPresent()).as("Debit Card with Card Id - " + debitCardId + " is expected to be present but is not").isEqualTo(true);
//        if (savedDebitCard.isPresent()) {
//            savedDebitCard.get()
//                    .validateCardNumber(AesEncCardNumDebit)
//                    .validateCardId(debitCardId)
//                    .validateCardScheme("DEBIT_CARD")
//                    .validateFirstSixDigit("444433")
//                    .validateLastFour("1111")
//                    .validateCardType("0")
//                    .validateExpiry(AesEncExpDebit)
//                    .validateStatus(1)
//                    .validateUserId(user.custId())
//                    .validateCardScheme("DEBIT_CARD")
//                    .assertAll();
//        }
//
//        //Validating saved card presence for credit card
//        Optional<SaveCardResponse> savedCreditCard = saveCardResponseList.stream()
//                .filter(saveCardResponse -> saveCardResponse.getCardId().toString().equals(creditCardId.toString()))
//                .findAny();
//        Assertions.assertThat(savedCreditCard.isPresent()).as("Credit Card with Card Id - " + creditCardId + " is expected to be present but is not").isEqualTo(true);
//        if (savedCreditCard.isPresent()) {
//            savedCreditCard.get()
//                    .validateCardNumber(AesEncCardNumCredit)
//                    .validateCardId(creditCardId)
//                    .validateCardScheme("CREDIT_CARD")
//                    .validateFirstSixDigit("479947")
//                    .validateLastFour("6601")
//                    .validateCardType("0")
//                    .validateExpiry(AesEncExp)
//                    .validateStatus(1)
//                    .validateUserId(user.custId())
//                    .validateCardScheme("CREDIT_CARD")
//                    .assertAll();
//        }
//    }

    @Test(description = "Validate success response of get savecard on user id when card not saved on user")
    public void getSaveCard_userId_withoutCardSaved() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        report.info("Card details has been deleted on this user");
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_userId(user);
        saveCardResponseBase.validateCodeDetail("Card does not exist for given parameters")
                .validateHttpCode("200")
                .validateHttpSubCode("204")
                .validateResponseStatus("SUCCESS")
                .assertAll();
    }

    //GET_SAVED_CARD_BY_CARD_ID
//    @Test(description = "Validate success for get savecard by card id API")
//    public void getSaveCard_byCardId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        report.info("Card saved on user with cardId: " + cardId);
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_cardId(user, cardId);
//        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
//        SaveCardResponse saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
//        saveCardResponse.validateUserId(user.custId())
//                .validateCardScheme("DEBIT_CARD")
//                .validateCardNumber(AesEncCardNumDebit)
//                .validateExpiry(AesEncExpDebit)
//                .validateCardType("0")
//                .validateLastFour(CommonHelpers.getCardLastFourDigit(paymentDTO.getDebitCardNumber()))
//                .validateFirstSixDigit(CommonHelpers.getCardFirstSixDigit(paymentDTO.getDebitCardNumber()))
//                .assertAll();
//
//    }

//    @Test(description = "Validate last four digit in get savecard by card id API")
//    public void getSaveCard_byCardId_validate0InLastFourDigit() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExpDebit, paymentDTO.getCreditCardNumber()).getResponse().toString();
//        report.info("Card saved on user with cardId: " + cardId);
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_cardId(user, cardId);
//        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
//        SaveCardResponse saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponse();
//        saveCardResponse.validateUserId(user.custId())
//                .validateCardScheme("CREDIT_CARD")
//                .validateCardNumber(AesEncCardNumCredit)
//                .validateExpiry(AesEncExpDebit)
//                .validateCardType("0")
//                .validateLastFour(CommonHelpers.getCardLastFourDigit(paymentDTO.getCreditCardNumber()))
//                .validateFirstSixDigit(CommonHelpers.getCardFirstSixDigit(paymentDTO.getCreditCardNumber()))
//                .assertAll();
//    }


    @Test(description = "Validate success for get savecard by card id API when no card exist for cardId")
    public void getSaveCard_byCardId_whenNoCardId() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        User user = userManager.getForWrite(Label.BASIC);
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_cardId(user, "123");
        saveCardResponseBase.validateCodeDetail("Card does not exist for given parameters")
                .validateHttpCode("200")
                .validateHttpSubCode("204")
                .validateResponseStatus("SUCCESS")
                .assertAll();
    }

    @Test(description = "Validate failed response for get savecard by card id when card id is alphanumric")
    public void getSaveCard_byCardId_failed() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        User user = userManager.getForWrite(Label.BASIC);
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_cardId(user, "123asd");
        saveCardResponseBase.validateCodeDetail("System Error")
                .validateHttpCode("500")
                .validateResponseStatus("FAILURE")
                .validateResponse("System Error")
                .assertAll();
    }

    //GET_SAVED_CARD_BY_USER_ID_AND_STATUS
//    @Test(description = "Validate success response for get savecard by user id and status=1")
//    public void getSaveCard_byUser_status() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        String cardId1 = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExp, paymentDTO.getCreditCardNumber()).getResponse().toString();
//        int noOfCardsSaved = 2;
//        report.info("Card saved on user with cardId: " + cardId + " and " + cardId1);
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_byUser_andStatus(user, "1");
//        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
//        List<SaveCardResponse> saveCardResponseList = saveCardResponseBase.getResponseInSaveCardResponseList();
//        Assertions.assertThat(saveCardResponseList.size()).as("No. of cards saved is not as expected").isEqualTo(noOfCardsSaved);
//        //Validating saved card presence for card1
//        Optional<SaveCardResponse> savedCard = saveCardResponseList.stream()
//                .filter(saveCardResponse -> saveCardResponse.getCardId().toString().equals(cardId.toString()))
//                .findAny();
//        Assertions.assertThat(savedCard.isPresent()).as("Card with Card Id - " + cardId + " is expected to be present but is not").isEqualTo(true);
//        if (savedCard.isPresent()) {
//            Assertions.assertThat(savedCard.get().getStatus()).as("Status is not as expected").isEqualTo(1);
//        }
//
//        //Validating saved card presence for card2
//        savedCard = saveCardResponseList.stream()
//                .filter(saveCardResponse -> saveCardResponse.getCardId().toString().equals(cardId1.toString()))
//                .findAny();
//        Assertions.assertThat(savedCard.isPresent()).as("Card with Card Id - " + cardId1 + " is expected to be present but is not").isEqualTo(true);
//        if (savedCard.isPresent()) {
//            Assertions.assertThat(savedCard.get().getStatus()).as("Status is not as expected").isEqualTo(1);
//        }
//
//    }

//    @Test(description = "Validate status=1 in response for GET_SAVED_CARD_BY_USER_ID_AND_STATUS API when search for status=0")
//    public void getSaveCard_byUser_status1() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        report.info("Card saved on user with cardId: " + cardId);
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_byUser_andStatus(user, "0");
//        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
//        SaveCardResponse saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponseList().get(0);
//        saveCardResponse.validateCardId(Long.parseLong(cardId));
//        saveCardResponse.validateStatus(1)
//                .validateCardId(Long.parseLong(cardId))
//                .assertAll();
//
//    }

//    @Test(description = "Validate, only card with status=1 obtain in response, when get saved card for status=1, also validate last four list of credit card")
//    public void getSaveCard_byUser_status_1() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        String cardId1 = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExp, paymentDTO.getCreditCardNumber()).getResponse().toString();
//        String updateStatus = "update SAVED_CARD_INFO set status=0 where card_id='" + cardId + "'";
//        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, updateStatus);
//        TRANSACTIONAL_REDIS_CLUSTER().del("PG_USER_CARDS_V4_"+user.custId(),"USER_CARDS_V4_"+user.custId());
////        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("PG_USER_CARDS_V4_"+user.custId(),"USER_CARDS_V4_"+user.custId());
//        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.getSaveCardDetails_byUser_andStatus(user, "1");
//        savedCardHelpers.validateSavedCardResponse_Success(saveCardResponseBase);
//        SaveCardResponse saveCardResponse = saveCardResponseBase.getResponseInSaveCardResponseList().get(0);
//        saveCardResponse.validateStatus(1)
//                .validateCardId(Long.parseLong(cardId1))
//                .validateLastFour(CommonHelpers.getCardLastFourDigit(paymentDTO.getCreditCardNumber()))
//                .validateFirstSixDigit(CommonHelpers.getCardFirstSixDigit(paymentDTO.getCreditCardNumber()))
//                .assertAll();
//
//    }

    //GET_MID_CUSTID_USERID_CARD_DETAIL
//    @Test(description = "Validate success response of GET_MID_CUSTID_USERID_CARD_DETAIL API, also validate card last four digit")
//    public void getSaveCard_byMid_custId_userId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers.deleteSavedCard(user);
//        mid = CommonHelpers.generateOrderId();
//        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, mid, expiry);
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_byMid_custId_userId(user, custId, mid);
//        SaveCardResponse response = responseBase.getResponseInSaveCardResponseList().get(0);
//        response.validateMid(mid)
//                .validateCustId(custId)
//                .validateLastFour(CommonHelpers.getCardLastFourDigit(paymentDTO.getCreditCardNumber()))
//                .validateFirstSixDigit(CommonHelpers.getCardFirstSixDigit(paymentDTO.getCreditCardNumber()))
//                .validateStatus(1)
//                .assertAll();
//    }

//    @Test(description = "Validate count of card saved on user and custId when user and cust Id is different")
//    public void getSaveCard_byMid_custId_userId1() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers.deleteSavedCard(user);
//        mid = CommonHelpers.generateOrderId();
//        savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber());
//        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, mid, expiry);
//        savedCardHelpers.saveCard_custId_mId(PaymentDTO.getAmexCardNumber(), custId, mid, expiry);
//        savedCardHelpers.saveCard_custId_mId(PaymentDTO.getPromoCC(), custId, mid, expiry);
//        savedCardHelpers.saveCard_custId_mId("6073180505920479", custId, mid, expiry);
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_byMid_custId_userId(user, custId, mid);
//        List<SaveCardResponse> list = responseBase.getResponseInSaveCardResponseList();
//        Assertions.assertThat(list.size()).isEqualTo(4);
//    }


//    @Test(description = "Validate success response for saved card on userId when no card exist on user but card exist on custId")
//    public void getSaveCard_byMid_custId_userId2() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        SavedCardHelpers.deleteSavedCard(user);
//        mid = CommonHelpers.generateOrderId();
//        savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry);
//        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, mid, expiry);
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_byMid_custId_userId(user, custId, mid);
//        List<SaveCardResponse> list = responseBase.getResponseInSaveCardResponseList();
//        Assertions.assertThat(list.size()).isEqualTo(2);
//    }

//    @Test(description = "Validate success response when card does not exit on custId and mid but it exist on userid")
//    public void getSaveCard_byMid_custId_userId3() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        mid = CommonHelpers.generateOrderId();
//        SavedCardHelpers.deleteSavedCard(user);
//        savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber());
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_byMid_custId_userId(user, custId, mid);
//        List<SaveCardResponse> list = responseBase.getResponseInSaveCardResponseList();
//        Assertions.assertThat(list.size()).isEqualTo(1);
//    }

//    @Test(description = "Validate response when no card exist on user or merchant and custid")
//    public void getSaveCard_byMid_custId_userId4() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        mid = CommonHelpers.generateOrderId();
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_byMid_custId_userId(user, custId, mid);
//        responseBase.validateResponseStatus("SUCCESS")
//                .validateHttpCode("200")
//                .validateHttpSubCode("204")
//                .validateCodeDetail("Card does not exist for given parameters")
//                .assertAll();
//    }


    //GET_SAVEDCARD_ON_SSOTOKEN
//    @Test(description = "Validate success response of GET_SAVEDCARD_ON_SSOTOKEN API, Validate encoded card details and card scheme")
//    public void getSaveCard_bySsoToken() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExp, saveCardNum).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.getSavedCard_BySsoToken(user);
//        SaveCardResponse response = responseBase.getResponseInSaveCardResponseList().get(0);
//        response.validateCardId(Long.parseLong(cardId))
//                .validateCardNumber("444433XXXXXX1111")
//                .validateCardScheme("DC")
//                .assertAll();
//    }


//    @Test(description = "Validate success response of GET_SAVEDCARD_ON_SSOTOKEN API, Validate encoded card details and card scheme, also validate card four lastdigit")
//    public void getSaveCard_bySsoToken_CC() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getCreditCardNumber();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExp, saveCardNum).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.getSavedCard_BySsoToken(user);
//        SaveCardResponse response = responseBase.getResponseInSaveCardResponseList().get(0);
//        response.validateCardId(Long.parseLong(cardId))
//                .validateCardNumber("479947XXXXXX6601")
//                .validateCardScheme("CC")
//                .assertAll();
//    }


    @Test(description = "Validate response of GET_SAVEDCARD_ON_SSOTOKEN API when there  is no data saved on user")
    public void getSaveCard_bySsoToken_withOutData() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardNum = paymentDTO.getDebitCardNumber();
        SavedCardHelpers.deleteSavedCard(user);
        SaveCardResponseBase responseBase = savedCardHelpers.getSavedCard_BySsoToken(user);
        responseBase.validateResponseStatus("SUCCESS")
                .validateCodeDetail("Card does not exist for given parameters")
                .validateHttpCode("200")
                .validateHttpSubCode("204")
                .assertAll();
    }

    @Test(description = "Validate response of GET_SAVEDCARD_ON_SSOTOKEN API for Invalid token")
    public void getSaveCard_bySsoToken_InvalidToken() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardNum = paymentDTO.getDebitCardNumber();
        SavedCardHelpers.deleteSavedCard(user);
        SaveCardResponseBase responseBase = savedCardHelpers.getSavedCard_BySsoToken("abc");
        responseBase.validateResponseStatus("FAILURE")
                .validateCodeDetail("System Error")
                .validateHttpCode("500")
                .validateHttpSubCode("500")
                .assertAll();
    }

    //GET_SAVEDCARD_BY_MID_CUSTID_USERID_AND_CARDID
//    @Test(description = "Validate success response for get saved card on custId and mid, userid and card id")
//    public void getSaveCard_byMid_custId_userId_cardId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getCreditCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = CommonHelpers.generateOrderId();
//        String cardId = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_byMid_custId_userId_cardId(user, custId, mid, cardId);
//        SaveCardResponse saveCardResponse = responseBase.getResponseInSaveCardResponse();
//        saveCardResponse
//                .validateCardId(Long.parseLong(cardId))
//                .validateFirstSixDigit(CommonHelpers.getCardFirstSixDigit(saveCardNum))
//                .validateLastFour(CommonHelpers.getCardLastFourDigit(saveCardNum))
//                .validateCardScheme("CREDIT_CARD")
//                .assertAll();
//    }

    @Test(description = "Validate success response for get saved card on custId and mid, userid and cardid when no card exist on mid and custId")
    public void getSaveCard_byMid_custId_userId_cardId1() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardNum = paymentDTO.getDebitCardNumber();
        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        mid = CommonHelpers.generateOrderId();
        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCardDetails_byMid_custId_userId_cardId(user, custId, mid, "0000");
        responseBase.validateResponseStatus("SUCCESS")
                .validateCodeDetail("Card does not exist for given parameters").assertAll();
    }

    //DELETE_SAVED_CARD_BY_USERID_AND_CARD_ID
//    @Test(description = "Validate successful response for DELETE_SAVED_CARD_BY_USERID_AND_CARD_ID API")
//    public void deleteSaveCard_byUserId_cardId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExp, saveCardNum).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSave_byUserId_cardId(user, cardId);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        SaveCardResponse saveCardResponse = responseBase.getResponseInSaveCardResponse();
//        saveCardResponse.validateCardId(Long.parseLong(cardId))
//                .validateCardScheme("DEBIT_CARD")
//                .validateStatus(1)
//                .validateFirstSixDigit(CommonHelpers.getCardFirstSixDigit(saveCardNum))
//                .validateLastFour(CommonHelpers.getCardLastFourDigit(saveCardNum))
//                .assertAll();
//    }


//    @Test(description = "Validate successful response for DELETE_SAVED_CARD_BY_USERID_AND_CARD_ID API when no card saved on user")
//    public void deleteSaveCard_byUserId_cardId_noCardSaved() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers.deleteSavedCard(user);
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSave_byUserId_cardId(user, "abc");
//        responseBase.validateResponseStatus("FAILURE")
//                .validateCodeDetail("System Error")
//                .validateHttpCode("500")
//                .validateHttpSubCode("500")
//                .assertAll();
//    }

//    @Test(description = "Validate saved card status in DB is changed to 0 after saved card delete API is success")
//    public void deleteSaveCard_byUserId_cardId_validateStatusInDB0() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExp, saveCardNum).getResponse().toString();
//        savedCardHelpers.Validate_CardStatus_FromDB(cardId, true);
//        savedCardHelpers.deleteSave_byUserId_cardId(user, cardId);
//        savedCardHelpers.Validate_CardStatus_FromDB(cardId, false);
//    }

    //	DELETE_CACHE_CARD_DETAIL
    @Test(description = "Validate delete save card from cache API response when Key exist in cache")
    public void deleteSaveCard_from_Cache() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        String txnId = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardNum = paymentDTO.getDebitCardNumber();
        savedCardHelpers.saveCardCache(user.custId(), EncCardNum, EncExp, saveCardNum, txnId).getResponse().toString();
        CommonHelpers.validateCache("savedcard_" + txnId);
        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_fromCache(txnId);
        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
        Assertions.assertThat(responseBase.getResponse()).isEqualTo(true);
    }

    @Test(description = "Validate key savedcard_txnId deleted from cache when delete API is successful.")
    public void deleteSaveCard_from_Cache_validateCache() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        String txnId = CommonHelpers.generateOrderId();
        User user = userManager.getForWrite(Label.BASIC);
        saveCardNum = paymentDTO.getDebitCardNumber();
        savedCardHelpers.saveCardCache(user.custId(), EncCardNum, EncExp, saveCardNum, txnId).getResponse().toString();
        CommonHelpers.validateCache("savedcard_" + txnId);
        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_fromCache(txnId);
        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
        savedCardHelpers.validateCacheForDelete("savedcard_" + txnId);
    }

    @Test(description = "Validate delete save card from cache API response when Key does not exist in cache")
    public void deleteSaveCard_from_Cache_whenNoDataInCache() throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        String txnId = CommonHelpers.generateOrderId();
        saveCardNum = paymentDTO.getDebitCardNumber();
        savedCardHelpers.validateCacheForDelete("savedcard_" + txnId);
        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_fromCache(txnId);
        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
        savedCardHelpers.validateCacheForDelete("savedcard_" + txnId);
    }


//    //DELETE_SAVEDCARD_BY_CARDID_USERID_MID_CUSTID
//    @Test(description = "Validate userId column deleted from SAVED_CARD_INFO DB when delete executed with userId")
//    public void deleteSaveCard_byCardId_userId_mid_custId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, saveCardNum).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSave_byCardId_mId_custId_userId(cardId, mid, custId, user);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        this.assertFromDb(userId_SAVED_CARD_INFO.replace("{card_id}", cardId), "user_id", "").assertAll();
//    }

//    @Test(description = "Validate userId column deleted from SAVED_CARD_INFO DB when delete executed with userId when card is saved on userid")
//    public void deleteSaveCard_byCardId_userId_mid_custId1() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSave_byCardId_mId_custId_userId(cardId, mid, custId, user);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        this.assertFromDb(userId_SAVED_CARD_INFO.replace("{card_id}", cardId), "user_id", "")
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "mid", null)
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "cust_id", null)
//                .assertAll();
//    }

//    @Test(description = "Validate mid and custid column deleted from SAVED_MID_CARD_INFO DB when delete executed with custId and mid, when card is saved on mid,custid")
//    public void deleteSaveCard_byCardId_userId_mid_custId2() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        String cardId = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSave_byCardId_mId_custId_userId(cardId, mid, custId, user);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        this.assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "mid", "")
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "cust_id", "")
//                .assertAll();
//    }


//    @Test(description = "Validate card number column deleted and status=0 from SAVED_CARD_INFO DB when delete executed with custId and mid, when card is saved on userid, mid, custid")
//    public void deleteSaveCard_byCardId_userId_mid_custId3() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        String cardId = savedCardHelpers.saveCard_custId_mId(saveCardNum, custId, mid, expiry).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSave_byCardId_mId_custId_userId(cardId, mid, custId, user);
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        this.assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "mid", "")
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "cust_id", "")
//                .assertAll();
//    }


    //GET_SAVEDCARD_ON_CUSTID_MID
//    @Test(description = "Validate success response of GET_SAVEDCARD_ON_CUSTID_MID API")
//    public void getSaveCard_byMid_custId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        String merchantKey = Constants.MerchantType.PGOnly.getKey();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, mid, expiry).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCard_ByMid_custId(mid, merchantKey, custId);
//        SaveCardResponse response = responseBase.getResponseInSaveCardResponseList().get(0);
//        response.validateCardId(Long.parseLong(cardId))
//                .validateCardNumber("444433XXXXXX1111")
//                .validateCardScheme("DC")
//                .assertAll();
//    }


//    @Test(description = "Validate Failure response of GET_SAVEDCARD_ON_CUSTID_MID API when the Invalid checksum is passed")
//    public void getSaveCard_byMid_custId_withInvalidChecksum() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        SavedCardHelpers.deleteSavedCard(user);
//        savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, mid, expiry);
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCard_ByMid_custId(mid, "rNdPDzPIk8rvv%s1", custId);
//        responseBase.validateResponseStatus("FAILURE")
//                .validateHttpCode("500")
//                .assertAll();
//    }

    //GET_SAVEDCARD_ON_MID_CUSTID_SSOTOKEN
//    @Test(description = "Validate Success response of GET_SAVEDCARD_ON_MID_CUSTID_SSOTOKEN API")
//    public void getSaveCard_byMid_custId_token() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        String merchantKey = Constants.MerchantType.PGOnly.getKey();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCard_ByMid_custId_token(mid, merchantKey, custId, user.ssoToken());
//        responseBase.validateResponseStatus("SUCCESS").assertAll();
//        SaveCardResponse saveCardResponse = responseBase.getResponseInSaveCardResponseList().get(0);
//        saveCardResponse.validateCardId(Long.parseLong(cardId))
//                .validateCardScheme("DC")
//                .validateCardNumber("444433XXXXXX1111")
//                .assertAll();
//    }

//    @Test(description = "Validate Failure response of GET_SAVEDCARD_ON_MID_CUSTID_SSOTOKEN API when checksum invalid")
//    public void getSaveCard_byMid_custId_token_invalidChecksum() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        SaveCardResponseBase responseBase = savedCardHelpers.getSaveCard_ByMid_custId_token(mid, "rNdPDzPIk8rvv%s1", custId, user.ssoToken());
//        responseBase.validateResponseStatus("FAILURE")
//                .validateHttpCode("500")
//                .assertAll();
//    }


    //DELETE_SAVEDCARD_ON_CUSTID_MID_CARDID
//    @Test(description = "Validate Success response of DELETE_SAVEDCARD_ON_CUSTID_MID_CARDID API")
//    public void deleteSavecard_on_custId_mid_cardId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        String merchantKey = Constants.MerchantType.PGOnly.getKey();
//        SavedCardHelpers.deleteSavedCard(custId);
//        String cardId = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, mid, expiry).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_On_custId_mid_cardId(mid, merchantKey, custId, cardId);
//        responseBase.validateResponseStatus("SUCCESS")
//                .validateCodeDetail("Card deactivated successfully")
//                .assertAll();
//    }

//    @Test(description = "Validate Status=0 in DB after success response of DELETE_SAVEDCARD_ON_CUSTID_MID_CARDID API")
//    public void deleteSavecard_on_custId_mid_cardId_CheckStatus_1() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        String merchantKey = Constants.MerchantType.PGOnly.getKey();
//        SavedCardHelpers.deleteSavedCard(custId);
//        String cardId = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, mid, expiry).getResponse().toString();
//        savedCardHelpers.ValidateCardStatus(cardId, "true");
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_On_custId_mid_cardId(mid, merchantKey, custId, cardId);
//        responseBase.validateResponseStatus("SUCCESS")
//                .validateCodeDetail("Card deactivated successfully")
//                .assertAll();
//        savedCardHelpers.ValidateCardStatus(cardId, "false");
//    }

//    @Test(description = "Validate success response of DELETE_SAVEDCARD_ON_CUSTID_MID_CARDID API when card does not exit")
//    public void deleteSavecard_on_custId_mid_cardId_CardDoesNotExist() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        String merchantKey = Constants.MerchantType.PGOnly.getKey();
//        SavedCardHelpers.deleteSavedCard(custId);
//        String cardId = "12345";
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_On_custId_mid_cardId(mid, merchantKey, custId, cardId);
//        responseBase
//                .validateHttpCode("500")
//                .validateResponseStatus("FAILURE")
//                .assertAll();
//    }

//    @Test(description = "Validate Failure response of DELETE_SAVEDCARD_ON_CUSTID_MID_CARDID API when Invalid checksum is passed")
//    public void deleteSavecard_on_custId_mid_cardId_InvalidChecksum() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        SavedCardHelpers.deleteSavedCard(custId);
//        String cardId = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, mid, expiry).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_On_custId_mid_cardId(mid, "rNdPDzPIk8rvv%s1", custId, cardId);
//        responseBase.validateResponseStatus("FAILURE")
//                .validateCodeDetail("System Error")
//                .assertAll();
//    }

    //DELETE_SAVEDCARD_ON_SSOTOKEN_CARDID
//    @Test(description = "Validate success response of DELETE_SAVEDCARD_ON_SSOTOKEN_CARDID API")
//    public void deleteSavecard_on_ssotoken_cardId() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_On_ssotoken_cardId(cardId, user.ssoToken());
//        responseBase.validateResponseStatus("SUCCESS")
//                .validateCodeDetail("Card deactivated successfully")
//                .assertAll();
//    }

//    @Test(description = "Validate failure response of DELETE_SAVEDCARD_ON_SSOTOKEN_CARDID API when invalid token is passed")
//    public void deleteSavecard_on_ssotoken_cardId_InvalidToken() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        SavedCardHelpers.deleteSavedCard(user);
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, paymentDTO.getDebitCardNumber()).getResponse().toString();
//        SaveCardResponseBase responseBase = savedCardHelpers.deleteSaveCard_On_ssotoken_cardId(cardId, "123abc");
//        responseBase.validateResponseStatus("FAILURE")
//                .validateCodeDetail("System Error")
//                .assertAll();
//    }


//    @Test(description = "Validate expired card not found in result of  get savecard on user id API")
//    public void validate_expiredcard_on_userId() throws Exception {
//        SaveCardResponseBase saveCardResponseBase;
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        User user = userManager.getForWrite(Label.BASIC);
//        SavedCardHelpers.deleteSavedCard(user);
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        //save credit card
//        String cardId = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumCredit, AesEncExpDebit, paymentDTO.getCreditCardNumber()).getResponse().toString();
//        savedCardHelpers.validateSaveCardDB_ByCardID(cardId);
//        //Change expiry of first save card
//        savedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId, user);
//        //Save debit card
//        String cardId2 = savedCardHelpers.saveCardUserId(user.custId(), AesEncCardNumDebit, AesEncExpDebit, saveCardNum).getResponse().toString();
//        savedCardHelpers.validateSaveCardDB_ByCardID(cardId2);
//        saveCardResponseBase = savedCardHelpers.getSaveCardDetails_userId(user);
//        Assertions.assertThat(saveCardResponseBase.getResponseInSaveCardResponseList().size()).isEqualTo(1);
//        SaveCardResponse cardDetails = saveCardResponseBase.getResponseInSaveCardResponseList().get(0);
//        cardDetails.validateCardNumber(AesEncCardNumDebit)
//                .validateCardId(Long.parseLong(cardId2))
//                .validateCardScheme("DEBIT_CARD")
//                .validateFirstSixDigit("444433")
//                .validateLastFour("1111")
//                .validateCardType("0")
//                .validateExpiry(AesEncExpDebit)
//                .validateStatus(1)
//                .validateUserId(user.custId())
//                .validateCardScheme("DEBIT_CARD")
//                .assertAll();
//    }

//    @Test(description = "Validate expired card not found in result of  get savecard on user id, custId and Mid API")
//    public void validate_expiredcard_on_userId_custId_mid() throws Exception {
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        saveCardNum = paymentDTO.getDebitCardNumber();
//        expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();
//        mid = Constants.MerchantType.PGOnly.getId();
//        User user = userManager.getForRead(Label.BASIC);
//        //Random custId
//        String randomCustId = CommonHelpers.generateOrderId();
//        SavedCardHelpers.deleteSavedCard(user);
//        //Save credit card on mid and randomCustId
//        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), randomCustId, mid, expiry);
//        String cardId = responseBase.getResponse().toString();
//        savedCardHelpers.validateSavedCardResponse_Success(responseBase);
//        this.assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "user_id", null)
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "mid", mid)
//                .assertFromDb(mid_custId_userId_SAVED_MID_CARD_INFO.replace("{card_id}", cardId), "cust_id", randomCustId).assertAll();
//        //Change expiry of first save card
//        savedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId, user); //Save debit  card on mid and randomCustId
//        SaveCardResponseBase responseBase1 = savedCardHelpers.saveCard_custId_mId(saveCardNum, randomCustId, mid, expiry);
//        String cardId2 = responseBase1.getResponse().toString();
//        responseBase1 = savedCardHelpers.getSaveCardDetails_byMid_custId_userId(user, randomCustId, mid);
//        Assertions.assertThat(responseBase1.getResponseInSaveCardResponseList().size()).isEqualTo(1);
//        SaveCardResponse response = responseBase1.getResponseInSaveCardResponseList().get(0);
//        response.validateCardId(Long.parseLong(cardId2))
//                .validateCardScheme("DEBIT_CARD")
//                .assertAll();
//    }


}