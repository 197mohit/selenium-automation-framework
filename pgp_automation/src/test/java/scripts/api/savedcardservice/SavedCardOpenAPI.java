package scripts.api.savedcardservice;

import com.paytm.api.saveCard.SavedCardApi;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

@Owner(Constants.Owner.TARUN)
//TODO Need to control FF4J out of test cases
public class SavedCardOpenAPI extends PGPBaseTest {

    private static PaymentDTO paymentDTO = new PaymentDTO();
    private static final int CARD_LENGTH = 16;
    private static final int AMEX_CARD_LENGTH = 15;
    private CheckoutPage checkoutPage = new CheckoutPage();

    //FF4j Flags modified in this class
    private static final String returnFromPlatformUserId =FF4JFeatures.SC_PLATFORMSAVEDCARDUSERID;
    private static final String returnFromPlatformMID =FF4JFeatures.SC_FETCH_FROM_PLATFORM_FOR_MID;
    private static final String SC_ADDITIONALINFO =FF4JFeatures.SC_ADDITIONALINFO;

    private SavedCardApi savedCardApi = new SavedCardApi();

    private String getEncryptedCardDetails(String cardNumber,int expectedSize)
    {
        String enc=cardNumber.substring(0,6) + "XXXXXX" + cardNumber.substring(cardNumber.length()-4);
        if(cardNumber.length()!=15)
            Assertions.assertThat(enc.length()).isEqualTo(expectedSize);
        else
            Assertions.assertThat(cardNumber.length()).isEqualTo(expectedSize);
        return enc;
    }

    @Feature("PGP-29587")
    @Test(description = "SavedcardsByTokenType JWT Token API : To verify new cardNoLength param in response when ff4j flag is ON")
    public void cardNoLengthOpenType() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        FF4JFlags.enable(returnFromPlatformUserId);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());// Card 1
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());//  Card 2

        //API

        try {
            Response response = savedCardApi.getSavedCardTokenTypeJWT(user.custId()).execute();
            response.then().assertThat()
                    .root("body.savedCardDetails")
                    .body("cardNoLength", hasItems(String.valueOf(CARD_LENGTH)))
                    .body("maskedCardNumber", contains(equalTo(getEncryptedCardDetails(paymentDTO.getDebitCardNumber(), CARD_LENGTH))
                            , equalTo(getEncryptedCardDetails(paymentDTO.getCreditCardNumber(), CARD_LENGTH))));
        } finally {
            FF4JFlags.disable(returnFromPlatformUserId);
        }
    }

    @Feature("PGP-29587")
    @Test(description = "SavedcardsByTokenType JWT Token API : AMEX Card : To verify new cardNoLength param in response when ff4j flag is ON")
    public void cardNoLengthAmex() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        FF4JFlags.enable(returnFromPlatformUserId);


        try {
            Response response = savedCardApi.getSavedCardTokenTypeJWT(user.custId()).execute();
            response.then().assertThat()
                    .root("body.savedCardDetails")
                    .body("cardNoLength", hasItems(String.valueOf(CARD_LENGTH), String.valueOf(AMEX_CARD_LENGTH)))
                    .body("maskedCardNumber", contains(equalTo(getEncryptedCardDetails(paymentDTO.getDebitCardNumber(), CARD_LENGTH))
                            , equalTo(getEncryptedCardDetails(paymentDTO.getCreditCardNumber(), AMEX_CARD_LENGTH))));
        } finally {
            FF4JFlags.disable(returnFromPlatformUserId);
        }
    }
    @Feature("PGP-29587")
    @Test(description = "SavedcardsByTokenType JWT Token API : To verify new cardNoLength param in response when ff4j flag is OFF")
    public void cardNoLengthFF4jOff() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        FF4JFlags.disable(returnFromPlatformUserId);

        Response response = savedCardApi.getSavedCardTokenTypeJWT(user.custId()).execute();
        response.then().assertThat()
                .root("body.savedCardDetails")
                .body("cardNoLength",hasItems(nullValue(),nullValue()))
                .body("maskedCardNumber", contains(equalTo(getEncryptedCardDetails(paymentDTO.getDebitCardNumber(),CARD_LENGTH))
                        ,equalTo(getEncryptedCardDetails(paymentDTO.getCreditCardNumber(),CARD_LENGTH))));


    }

    @Feature("PGP-29587")
    @Test(description = "SavedcardsByTokenType SSO Token API : To verify new cardNoLength param in response when ff4j flag is ON")
    public void cardNoLengthOpenTypeSSO() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        FF4JFlags.enable(returnFromPlatformUserId);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());// Card 1
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());//  Card 2

        //API
        try {

            savedCardApi.getSavedCardTokenTypeJWT(user.custId());
            Response response = savedCardApi.getSavedCardTokenTypeJWT(user.custId())
                    .setContext("head.tokenType", "SSO")
                    .setContext("head.token", user.ssoToken())
                    .execute();
            response.then().assertThat()
                    .root("body.savedCardDetails")
                    .body("cardNoLength", hasItems(String.valueOf(CARD_LENGTH)))
                    .body("maskedCardNumber", contains(equalTo(getEncryptedCardDetails(paymentDTO.getDebitCardNumber(), CARD_LENGTH))
                            , equalTo(getEncryptedCardDetails(paymentDTO.getCreditCardNumber(), CARD_LENGTH))));
        } finally {
            FF4JFlags.disable(returnFromPlatformUserId);
        }
    }

    @Feature("PGP-29587")
    @Test(description = "SavedcardsByTokenType SSO Token API : Amex Card : To verify new cardNoLength param in response when ff4j flag is ON")
    public void cardNoLengthOpenTypeSSOAmex() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER);

        FF4JFlags.enable(returnFromPlatformUserId);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());// Card 1
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());//  Card 2

        //API

        try {

            savedCardApi.getSavedCardTokenTypeJWT(user.custId());
            Response response = savedCardApi.getSavedCardTokenTypeJWT(user.custId())
                    .setContext("head.tokenType", "SSO")
                    .setContext("head.token", user.ssoToken())
                    .execute();
            response.then().assertThat()
                    .root("body.savedCardDetails")
                    .body("cardNoLength", hasItems(String.valueOf(CARD_LENGTH), String.valueOf(AMEX_CARD_LENGTH)))
                    .body("maskedCardNumber", contains(equalTo(getEncryptedCardDetails(paymentDTO.getDebitCardNumber(), CARD_LENGTH))
                            , equalTo(getEncryptedCardDetails(paymentDTO.getCreditCardNumber(), AMEX_CARD_LENGTH))));
        }
        finally {
            FF4JFlags.disable(returnFromPlatformUserId);
        }
    }

    @Feature("PGP-29587")
    @Test(description = "SavedcardsByTokenType SSO Token API : To verify new cardNoLength param in response when ff4j flag is OFF")
    public void cardNoLengthSSOFF4jOff() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        FF4JFlags.disable(returnFromPlatformUserId);

        savedCardApi.getSavedCardTokenTypeJWT(user.custId());
        Response response = savedCardApi.getSavedCardTokenTypeJWT(user.custId())
                .setContext("head.tokenType","SSO")
                .setContext("head.token",user.ssoToken())
                .execute();

        response.then().assertThat()
                .root("body.savedCardDetails")
                .body("cardNoLength",hasItems(nullValue(),nullValue()))
                .body("maskedCardNumber", contains(equalTo(getEncryptedCardDetails(paymentDTO.getDebitCardNumber(),CARD_LENGTH))
                        ,equalTo(getEncryptedCardDetails(paymentDTO.getCreditCardNumber(),CARD_LENGTH))));


    }

//    @Owner(Constants.Owner.TARUN)
//    @Feature("PGP-29627")
//    @Test(description = "Non Logged In Flow : Service : When scFetchFromPlatformForMid FF4J -> FALSE, scAddAdditionalInfoInOpenApi -> False ,/get/card API will not return any extra param")
//    public void getCardAPIFF4jOffService() throws Exception {
//
//        String custId = CommonHelpers.generateOrderId();
//        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
//        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
//        savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(),custId,merchantType.getId(),paymentDTO.getExpMonth()+paymentDTO.getExpYear());
//
//        try {
//
//            FF4JFlags.disable(returnFromPlatformMID);
//            FF4JFlags.disable(SC_ADDITIONALINFO);
//
//
//            SavedCardApi.getMerchantCardAPI(merchantType, custId)
//                    .then()
//                    .body("responseStatus", equalToIgnoringCase("SUCCESS"),
//                            "httpCode", equalToIgnoringCase("200"),
//                            "httpSubCode", equalToIgnoringCase("200"),
//                            "codeDetail", equalToIgnoringCase("Success"),
//                            "response", not(empty()))
//                    .root("response")
//                    .body("expiryDate", hasItems(nullValue()),
//                            "oneClickSupported", hasItems(nullValue()),
//                            "status", hasItems(nullValue()),
//                            "cvvLength", hasItems(nullValue()),
//                            "cvvRequired", hasItems(nullValue()),
//                            "channelName", hasItems(nullValue()));
//
//        }
//        finally {
//            FF4JFlags.enable(returnFromPlatformMID);
//            FF4JFlags.enable(SC_ADDITIONALINFO);
//
//        }
//
//    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-29627")
    @Test(description = "Non Logged In Flow :P + When scFetchFromPlatformForMid FF4J -> True, scAddAdditionalInfoInOpenApi -> FALSE ,/get/card API will not return any extra param")
    public void getCardAPIFF4jOffPlatform() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        SavedCardHelpers.addCardAlipay(merchantType.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());


        try {
            FF4JFlags.enable(returnFromPlatformMID);
            FF4JFlags.disable(SC_ADDITIONALINFO);


            SavedCardApi.getMerchantCardAPI(merchantType, custId)
                    .then()
                    .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                            "httpCode", equalToIgnoringCase("200"),
                            "httpSubCode", equalToIgnoringCase("200"),
                            "codeDetail", equalToIgnoringCase("Success"),
                            "response", not(empty()))
                    .root("response")
                    .body("expiryDate", hasItems(nullValue()),
                            "oneClickSupported", hasItems(nullValue()),
                            "status", hasItems(nullValue()),
                            "cvvLength", hasItems(nullValue()),
                            "cvvRequired", hasItems(nullValue()),
                            "channelName", hasItems(nullValue()));
        }
        finally {
            FF4JFlags.enable(SC_ADDITIONALINFO);
        }


    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-29627")
    @Test(description = "Non Logged In Flow :Service : When scFetchFromPlatformForMid FF4J -> FALSE, scAddAdditionalInfoInOpenApi -> TRUE ,/get/card API will not return any extra param")
    public void getCardAPIFF4jONService() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        SavedCardHelpers.addCardAlipay(merchantType.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());


        try {
            FF4JFlags.disable(returnFromPlatformMID);
            FF4JFlags.enable(SC_ADDITIONALINFO);


            SavedCardApi.getMerchantCardAPI(merchantType, custId)
                    .then()
                    .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                            "httpCode", equalToIgnoringCase("200"),
                            "httpSubCode", equalToIgnoringCase("200"),
                            "codeDetail", equalToIgnoringCase("Success"),
                            "response", not(empty()))
                    .root("response")
                    .body("expiryDate", hasItems(notNullValue()),
                            "oneClickSupported", hasItems(notNullValue()),
                            "cvvLength", hasItems(notNullValue()),
                            "cvvRequired", hasItems(notNullValue()),
                            "channelName", hasItems(notNullValue()),
                            "hasLowSuccess", hasItems(notNullValue()),
                            "status", hasItems(notNullValue()),
                            "iconUrl", hasItems(notNullValue()));
        }
        finally {
            FF4JFlags.enable(returnFromPlatformMID);
        }

    }


    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-29627")
    @Test(description = "Non Logged In Flow :P + When scFetchFromPlatformForMid FF4J -> TRUE, scAddAdditionalInfoInOpenApi -> TRUE ,/get/card API will not return any extra param")
    public void getCardAPIFF4jONPlatform() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        SavedCardHelpers.addCardAlipay(merchantType.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        FF4JFlags.enable(returnFromPlatformMID);
        FF4JFlags.enable(SC_ADDITIONALINFO);


        SavedCardApi.getMerchantCardAPI(merchantType,custId)
                .then()
                .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                        "httpCode", equalToIgnoringCase("200"),
                        "httpSubCode", equalToIgnoringCase("200"),
                        "codeDetail", equalToIgnoringCase("Success"),
                        "response", not(empty()))
                .root("response")
                .body("expiryDate",hasItems(notNullValue()),
                        "oneClickSupported",hasItems(notNullValue()),
                        "cvvLength",hasItems(notNullValue()),
                        "cvvRequired",hasItems(notNullValue()),
                        "channelName",hasItems(notNullValue()),
                        "hasLowSuccess",hasItems(notNullValue()),
                        "status",hasItems(notNullValue()),
                        "iconUrl",hasItems(notNullValue()));
    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-29627")
    @Test(description = "Non Logged In Flow :Service : Successful txn via card which was saved after txn in /getCard API")
    public void successTransaction() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;

        try {
            FF4JFlags.disable(returnFromPlatformMID);
            FF4JFlags.enable(SC_ADDITIONALINFO);

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                    .build();
            String custId = initTxnDTO.getBody().getUserInfo().getCustId();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .setCUST_ID(custId)
                    .build();

            checkoutPage.createNativeOrder(orderDTO, true);
            new ResponsePage().waitUntilLoads();

            SavedCardApi.getMerchantCardAPI(merchantType, custId)
                    .then()
                    .body("responseStatus", equalToIgnoringCase("SUCCESS"),
                            "httpCode", equalToIgnoringCase("200"),
                            "httpSubCode", equalToIgnoringCase("200"),
                            "codeDetail", equalToIgnoringCase("Success"),
                            "response", not(empty()))
                    .root("response")
                    .body("expiryDate", hasItems(notNullValue()),
                            "oneClickSupported", hasItems(notNullValue()),
                            "cvvLength", hasItems(notNullValue()),
                            "cvvRequired", hasItems(notNullValue()),
                            "channelName", hasItems(notNullValue()),
                            "hasLowSuccess", hasItems(notNullValue()),
                            "status", hasItems(notNullValue()),
                            "iconUrl", hasItems(notNullValue()));
        }
        finally {
            FF4JFlags.enable(returnFromPlatformMID);
            FF4JFlags.enable(SC_ADDITIONALINFO);
        }

    }




}