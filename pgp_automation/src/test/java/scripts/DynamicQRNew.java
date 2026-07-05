package scripts;

import com.google.errorprone.annotations.Var;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.coft.PTS.CardToPar;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import groovy.json.JsonSlurper;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.luaj.vm2.ast.Str;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import play.api.libs.json.Json;
import scripts.coft.pts.CardToParTests;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;
import static com.paytm.apphelpers.NativeHelpers.submitProcessTxnResponseFromReq;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static io.restassured.RestAssured.given;

@Owner("Tarun")
public class DynamicQRNew extends PGPBaseTest {

    Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PG2_Refund;
    PaymentDTO paymentDTO = new PaymentDTO();
    public String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
    public static String TIN = null;

    @BeforeClass
    public void successTxnOfHigherAmountToIncreaseMPABalanceOfUser() throws Exception {
        String txnAmount = "40000.00";//To increase Merchant's MPA balance so that merchant have balance to give back refund to user
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);
    }


    @Test(description = "To Validate CC transaction using Dynamic QR", groups = "P0")
    public void validateCCTxn_UsingDynmQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);
    }


    @Test(description = "To Validate DC transaction using Dynamic QR", groups = "P0")
    public void validateDCTxn_UsingDynmQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "DEBIT_CARD";
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);
    }

    @Issue("PGP-20655")
    @Test(description = "test Dynamic QR order success by cc when non-matching website provided", groups = Group.Status.BUG)
    public void testOrderSuccessByCCWhenNonMatchingWebsiteProvided() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setWebsite("nonmatchingwebsite")
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessPeonDynamicQR(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);
    }

    //NB refund is offline
    @Test(description = "Validate NB transaction using Dynamic QR")
    public void validateNBTxn_UsingDynmQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "NET_BANKING";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setChannelCode("ICICI")
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "NB", "ICICI");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "NB", "ICICI");
        QRHelper.validateSuccessSMSQR(orderDTO);

    }


    @Test(description = "Validate Card is saved using Dynamic QR merchant")
    public void validateCardIsSaved_UsingDynmQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "STORE CARD DETAILS", "YES");
        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setStoreInstrument("1")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

        SavedCardHelpers.validateSavedCardPresence(user);

    }

    @Test(description = "Validate Success transaction using saved card using Dynamic QR merchant")
    public void validateSuccessTxnUsingSvdCard_UsingDynmQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "STORE CARD DETAILS", "YES");

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo(cardInfo)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    //TODO getting failed
   // @Test(description = "Validate Success Postpaid Onboarding transaction using Dynamic QR merchant", enabled = false)
    public void validateSucess_PostpaidOnboarding_UsingDynmQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "PAYTM_DIGITAL_CREDIT";
        String txnAmount = "2.00";
        User user = userManager.getForWrite(Label.POSTPAIDONBOARDING);
        WalletHelpers.setZeroBalance(user);
        PostpaidHelpers.updatePostpaidUserAttributes(user, PostpaidHelpers.WHITELISTED);
        PostpaidHelpers.updateBalance("1000");

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);


        QRHelper.validateTxnStatus(orderDTO, "PAYTMCC", "");
        QRHelper.validateSuccessPeonDynamicQR(orderDTO, "PAYTMCC", "");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);


    }

    @Test(description = "Validate Success PPBL transaction using Dynamic QR merchant")
    public void validateSucesPPBLTxn_UsingDynmQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "PPBL";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.PPBL);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setAuthMode("USRPWD")
                .setMpin("5335")
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "NB", "PPBL");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "NB", "PPBL");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);


    }


    @Test(description = "Validate Success transaction using EMI using Dynamic QR")
    public void validateSuccessTxnUsingEMI_UsingDynmQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "EMI";
        String txnAmount = "2.00";

        User user = userManager.getForWrite(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setPlanId("HDFC|3")
                .setEmiType("CREDIT_CARD")
                .setCardNum(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    @Test(description = "Validate Success transaction using 0 Cost EMI using Dynamic QR")
    public void validateSuccessTxnUsingZeroCostEMI_UsingDynmQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "EMI";
        String txnAmount = "2.00";

        User user = userManager.getForWrite(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setPlanId("HDFC|1")
                .setEmiType("CREDIT_CARD")
                .setCardNum(paymentDTO.PROMO_CC_CARD_HDFC)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

//    @Test(description = "Validate Success transaction using UPI using Dynamic QR", enabled = false)
//As discussed with Ankit this flow will not work on APP
    public void validateSuccessTxnUsingUPI_UsingDynmQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "UPI";
        String txnAmount = "2.00";

        User user = userManager.getForWrite(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "UPI", "ICICI");
        QRHelper.validateSuccessPeonDynamicQR(orderDTO, "UPI", "ICICI");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success transaction using UPI intent using Dynamic QR fast forward")
    public void validateSuccessTxnUsing_intent_UsingDynmQR() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();

        PaymentService paymentService = new PaymentService(merchant, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID());
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
                    .setMID(merchant.getId())
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            fetchQRResponse.then()
                    .statusCode(200)
                    .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                    .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(false))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.find{it.channelCode  == 'UPI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.find{it.channelCode  == 'UPIPUSH'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.find{it.channelCode  == 'UPIPUSHEXPRESS'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
        }

        StaticQrUpiPSPResponse staticQrUpiPSPResponse = null;
        upiPSP:
        {
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), orderDTO.getTXN_AMOUNT())
                    .setOrderId(orderDTO.getORDER_ID())
                    .build();
            StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            Response response = staticQrUpiPSP.execute();
            staticQrUpiPSPResponse = response.then()
                    .statusCode(200)
                    .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                    .extract().as(StaticQrUpiPSPResponse.class);
        }

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(orderDTO.getTXN_AMOUNT())
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));
        QRHelper.validateTxnStatus(orderDTO, "UPI", "PPBL");
    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success transaction using PCF UPI intent using Dynamic QR fast forward")
    public void validateSuccessTxnUsing_intent_PCF_UsingDynmQR() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();

        PaymentService paymentService = new PaymentService(merchant, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID());
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
                    .setMID(merchant.getId())
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            fetchQRResponse.then()
                    .statusCode(200)
                    .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                    .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.find{it.channelCode  == 'UPI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.find{it.channelCode  == 'UPIPUSH'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.find{it.channelCode  == 'UPIPUSHEXPRESS'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
        }
        String totalTxnAmount = "";
        String totalPcfAmount = "";
        fetchPcfDetails:
        {
            FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(user.ssoToken(), "SSO")
                    .setMid(merchant.getId())
                    .setTxnAmount(orderDTO.getTXN_AMOUNT())
                    .setPayMethods(Arrays.asList(new PayMethod().setPayMethod("UPI")))
                    .build();
            FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest);
            Response fetchPcfDetailResp = fetchPcfDetail.execute();
            fetchPcfDetailResp.then()
                    .statusCode(200)
                    .spec(pcfSuccessResponse())
                    .spec(pcfBodySuccessVerify("UPI", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.UPI.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.UPI.totalConvenienceCharges.value");
        }
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = null;

        upiPSP:
        {
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), totalTxnAmount)
                    .setOrderId(orderDTO.getORDER_ID())
                    .build();
            StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            Response response = staticQrUpiPSP.execute();
            staticQrUpiPSPResponse = response.then()
                    .statusCode(200)
                    .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                    .extract().as(StaticQrUpiPSPResponse.class);
        }

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(totalTxnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));
        QRHelper.validateTxnStatus(orderDTO, "UPI", "PPBL");
    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success transaction using PCF BALANCE using Dynamic QR fast forward")
    public void validateSuccessTxnUsingBALANCE_PCF_UsingDynmQR() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 5.00);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();

        PaymentService paymentService = new PaymentService(merchant, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID());
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
                    .setMID(merchant.getId())
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            fetchQRPaymentDetails.execute().then()
                    .statusCode(200)
                    .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                    .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.orderId", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
        }

        String totalTxnAmount = "";
        String totalPcfAmount = "";
        fetchPcfDetails:
        {
            FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(user.ssoToken(), "SSO")
                    .setMid(merchant.getId())
                    .setTxnAmount(orderDTO.getTXN_AMOUNT())
                    .setPayMethods(Arrays.asList(new PayMethod().setPayMethod("BALANCE")))
                    .build();
            FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest);
            Response fetchPcfDetailResp = fetchPcfDetail.execute();
            fetchPcfDetailResp.then()
                    .statusCode(200)
                    .spec(pcfSuccessResponse())
                    .spec(pcfBodySuccessVerify("BALANCE", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalConvenienceCharges.value");
        }

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(qrCodeId)
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath j = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(j.getString("body.resultInfo.resultStatus"))
                .as("body.resultInfo.resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(j.getString("body.resultInfo.resultCode"))
                .as("body.resultInfo.resultCode mismatch")
                .isEqualToIgnoringCase("01");
        softly.assertThat(j.getString("body.txnId"))
                .isNotNull();
        softly.assertThat(j.getString("body.orderId"))
                .as("body.orderId mismatch")
                .isEqualToIgnoringCase(fastForwardAppRequest.getBody().getOrderId());
        softly.assertThat(j.getString("body.txnAmount"))
                .as("body.txnAmount mismatch")
                .isEqualToIgnoringCase(fastForwardAppRequest.getBody().getTxnAmount());
        softly.assertThat(j.getString("body.paymentMode"))
                .as("body.paymentMode mismatch")
                .isEqualToIgnoringCase(fastForwardAppRequest.getBody().getPaymentMode());
        softly.assertThat(j.getString("body.bankName"))
                .as("body.bankName mismatch")
                .isEqualToIgnoringCase("WALLET");
        softly.assertThat(j.getString("body.extendInfo.totalTxnAmount"))
                .isNotNull();
        softly.assertThat(j.getString("body.extendInfo.productCode"))
                .as("body.extendInfo.productCode mismatch")
                .isEqualToIgnoringCase("51051000100000000002");
        softly.assertThat(j.getString("body.extendInfo.requestType"))
                .as("body.extendInfo.requestType mismatch")
                .isEqualToIgnoringCase("DYNAMIC_QR");
        softly.assertThat(j.getString("body.extendInfo.merchantTransId"))
                .isNotNull();
        softly.assertThat(j.getString("body.extendInfo.PAYTM_USER_ID"))
                .isNotNull();
        softly.assertThat(j.getString("body.extendInfo.paytmMerchantId"))
                .isNotNull();
        softly.assertThat(j.getString("body.extendInfo.alipayMerchantId"))
                .isNotNull();
        softly.assertThat(format.format(Double.valueOf(j.getString("body.chargeAmount"))))
                .as("chargeAmount mismatch")
                .isEqualTo(format.format(Double.valueOf(totalTxnAmount) - Double.valueOf(orderDTO.getTXN_AMOUNT())));
        softly.assertAll();

        QRHelper.validateTxnStatus(orderDTO, "PPI", "WALLET");

    }

    @Test(description = "Validate Success transaction using BALANCE using Dynamic QR fast forward")
    public void validateSuccessTxnUsingBALANCE_UsingDynmQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "PPI";
        String txnAmount = "2.00";

        User user = userManager.getForWrite(Label.BASIC);

        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount));

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(orderDTO.getTxnId())
                .build();

        //QRHelper.executeFastForwardAPP(fastForwardAppRequest);
        Assertions.assertThat(QRHelper.executeFastForwardApp(fastForwardAppRequest).getString("body.paymentMode")).isEqualTo("PPI");

        QRHelper.validateTxnStatus(orderDTO, "PPI", "WALLET");
       // QRHelper.validateSuccessPeonDynamicQR(orderDTO, "PPI", "WALLET");
        String gatewayName="WALLET";
        String payMode="PPI";
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","TXNTYPE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME","REFUNDAMT", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gatewayName),
                peon.mercUnqRef().equals("2810050501011BBRF2ET3Y18"),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
        QRHelper.validateSuccessPeonDynamicQRNew(orderDTO, "PPI", "WALLET");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);


    }




    @Test(description = "Verify the response when incorrect sso token is provided")
    public void incorrectSSOToken() {

        String txnAmount = "2.00";
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken("ABCD")
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();

        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SSO Token is invalid");
    }

    @Issue("PGP-25804")
    @Test(description = "Verify the response when empty txn token is provided", groups = Group.Status.BUG)
    public void emptyTxnToken() {
        String txnAmount = "2.00";
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("TXN_TOKEN")
                .setToken("")
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();

        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Test(description = "Verify the response when incorrect mid is provided")
    public void incorrectMID() throws Exception {
        String txnAmount = "2.00";
        String OrderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID("1234")
                .setToken("")
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();

        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("System error");


    }


    @Test(description = "Validate DirectForms are returned for Dynamic QR when ff4J flag is ON preference nativeOTPSupported not active")
    public void validateDirectFormParamInPTCFF4JFlagisONPrefNotActiveForCC_UsingDynmQR() throws Exception {


        String txnAmount = "2.00";
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.NATIVE_HDFO_PEON_DISABLED;


        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchant, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");

        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();

        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("HDFC Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("HDFO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());

        validateSoftly.assertAll();

    }





    @Test(description = "Validate DirectForms are Not returned for Dynamic QR when ff4J flag is Off and preference Not active")
    public void validateDirectFormParamNotInPTCFF4JFlagisOFFPrefNotActiveForCC_UsingDynmQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");

        Assertions.assertThat(DirectForm).as("Direct Forms are getting  fetched in PTC").isNull();
    }


    @Test(description = "Validate DirectForms are returned for Dynamic QR when ff4J flag is Off and preference is nativeOTPSupported")
    public void validateDirectFormParamInPTCFF4JFlagisOFFPrefIsActiveForCC_UsingDynmQR() throws Exception {

        String txnAmount = "2.00";
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.NATIVE_HDFO;


        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "nativeOTPSupported", "Y");

        }


        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchant, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");

        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();

        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("HDFC Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("HDFO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());

        validateSoftly.assertAll();

    }


    private ResponseSpecification pcfSuccessResponse() {
        return new ResponseSpecBuilder()
                .expectBody("body.resultInfo", Matchers.notNullValue())
                .rootPath("body.resultInfo")
                .expectBody("resultStatus", Matchers.equalTo("S"))
                .expectBody("resultCode", Matchers.equalTo("0000"))
                .expectBody("resultMsg", Matchers.equalTo("Success"))
                .build();
    }

    private ResponseSpecification pcfBodySuccessVerify(String paymode, String txnAmount) {
        return new ResponseSpecBuilder()
                .rootPath("body.consultDetails")
                .expectBody(paymode, Matchers.notNullValue())
                .expectBody(paymode + ".payMethod", Matchers.equalToIgnoringCase(paymode))
                .expectBody(paymode + ".baseTransactionAmount", Matchers.notNullValue())
                .expectBody(paymode + ".feeAmount", Matchers.notNullValue())
                .expectBody(paymode + ".taxAmount", Matchers.notNullValue())
                .expectBody(paymode + ".totalConvenienceCharges", Matchers.notNullValue())
                .expectBody(paymode + ".totalTransactionAmount", Matchers.notNullValue())
                .expectBody(paymode + ".baseTransactionAmount.value", Matchers.equalToIgnoringCase(txnAmount))
                .expectBody(paymode + ".feeAmount.value", Matchers.notNullValue())
                .expectBody(paymode + ".taxAmount.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalConvenienceCharges.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalTransactionAmount.value", Matchers.notNullValue())
                .build();
    }


    private Response ExecuteDirectFormRequest(List<HashMap<String, Object>> DirectForm){

        String actionUrl = (DirectForm.get(0)).get("actionUrl").toString();
        String respBody  = new JSONObject((Map<?,?>) (DirectForm.get(0)).get("content"))
                .toString().replace("<OTP>","123456");
        return given().body(respBody).contentType(ContentType.JSON).when().post(actionUrl);

    }
    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 10 and sub wallet amount is 7 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount10SubWallet7AvailableBalanceMW10FW10() throws Exception{
        User user= userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        String txnAmount = "10.00";
        String OrderId = CommonHelpers.generateOrderId();
        String foodamount="7";
        String mainwallettxnamount="3";
        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId,foodamount);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setgenerateorderId("false")
                .setTokenType("SSO")
                .setorderId(OrderId)
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.subwalletWithdrawMaxAmountDetails")).isEqualToIgnoringCase("{\"FOOD\":"+foodamount+"}");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(Double.parseDouble(String.valueOf(Double.valueOf(txnAmount)+Double.valueOf(foodamount)))));
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(),OrderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(qrCodeId)
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath jsonFastForward = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Successful.");
        Assertions.assertThat(jsonFastForward.getString("body.extendInfo.subwalletWithdrawMaxAmountDetails")).isEqualTo("{\"FOOD\":"+foodamount+"}");
        WalletHelpers.validateBalance(user, MainWalletAmount-Double.valueOf(mainwallettxnamount));
       Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(FoodWalletAmount-Double.valueOf(foodamount)) ;

    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 15 and sub wallet amount is 12 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount15SubWallet12AvailableBalanceMW10FW10() throws Exception{
        User user= userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        String txnAmount = "15.00";
        String OrderId = CommonHelpers.generateOrderId();
        String Mainwallettxnamount="5";
        String foodamount="12";
        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId,foodamount);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setgenerateorderId("false")
                .setTokenType("SSO")
                .setorderId(OrderId)
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.subwalletWithdrawMaxAmountDetails")).isEqualToIgnoringCase("{\"FOOD\":"+foodamount+"}");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(MainWalletAmount+FoodWalletAmount));
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(),OrderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(qrCodeId)
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath jsonFastForward = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Successful.");
        Assertions.assertThat(jsonFastForward.getString("body.extendInfo.subwalletWithdrawMaxAmountDetails")).isEqualTo("{\"FOOD\":"+foodamount+"}");
        WalletHelpers.validateBalance(user, Double.valueOf(txnAmount)-FoodWalletAmount);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.0) ;

    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 15 and sub wallet amount is 3 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount15SubWallet3AvailableBalanceMW10FW10() throws Exception{
        User user= userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        String txnAmount = "15.00";
        String OrderId = CommonHelpers.generateOrderId();
        String foodamount="3";

        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId,foodamount);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setgenerateorderId("false")
                .setTokenType("SSO")
                .setorderId(OrderId)
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.subwalletWithdrawMaxAmountDetails")).isEqualToIgnoringCase("{\"FOOD\":"+foodamount+"}");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(Double.valueOf(foodamount)+MainWalletAmount));
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(),OrderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(qrCodeId)
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath jsonFastForward = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Wallet balance Insufficient");
        WalletHelpers.validateBalance(user, MainWalletAmount);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(FoodWalletAmount) ;

    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 25 and sub wallet amount is 15 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount25SubWallet15AvailableBalanceMW10FW10() throws Exception{
        User user= userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        String txnAmount = "25.00";
        String OrderId = CommonHelpers.generateOrderId();
        String foodamount="15";
        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId,foodamount);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setgenerateorderId("false")
                .setTokenType("SSO")
                .setorderId(OrderId)
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.subwalletWithdrawMaxAmountDetails")).isEqualToIgnoringCase("{\"FOOD\":"+foodamount+"}");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(MainWalletAmount+FoodWalletAmount));
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(),OrderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(qrCodeId)
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath jsonFastForward = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Wallet balance Insufficient");
        WalletHelpers.validateBalance(user, MainWalletAmount);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(FoodWalletAmount) ;

    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 25 and sub wallet amount is 12 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount25SubWallet12AvailableBalanceMW10FW10() throws Exception{
        User user= userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        String txnAmount = "25.00";
        String OrderId = CommonHelpers.generateOrderId();
        String foodamount="12";
        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId,foodamount);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setgenerateorderId("false")
                .setTokenType("SSO")
                .setorderId(OrderId)
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.subwalletWithdrawMaxAmountDetails")).isEqualToIgnoringCase("{\"FOOD\":"+foodamount+"}");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(MainWalletAmount+FoodWalletAmount));
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(),OrderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(qrCodeId)
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath jsonFastForward = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Wallet balance Insufficient");
        WalletHelpers.validateBalance(user, MainWalletAmount);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(FoodWalletAmount) ;
    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 10 and sub wallet amount is 7 (Available balance MW-10 and FW-10) txn using provess txn")
    public void validateTxnAmount10SubWallet7AvailableBalanceMW10FW10PTC() throws Exception{
        User user= userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        String txnAmount = "10.00";
        String OrderId = CommonHelpers.generateOrderId();
        String foodamount="7";
        String mainwallettxnamount="3";
        PaymentService paymentService = new PaymentService(merchantType, txnAmount, OrderId,foodamount);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setgenerateorderId("false")
                .setTokenType("SSO")
                .setorderId(OrderId)
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.subwalletWithdrawMaxAmountDetails")).isEqualToIgnoringCase("{\"FOOD\":"+foodamount+"}");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(Double.parseDouble(String.valueOf(Double.valueOf(txnAmount)+Double.valueOf(foodamount)))));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode("BALANCE")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

           submitProcessTxnResponseFromReq(processTxnV1Request);

        WalletHelpers.validateBalance(user, MainWalletAmount-Double.valueOf(mainwallettxnamount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(FoodWalletAmount-Double.valueOf(foodamount)) ;

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-28416")
    @Test(description = "To Validate AIO SDK Type Param is getting passed in Payment Cashier Pay if getting passed in /ptc", groups = "P0")
    public void validateAIOSDKTypeParam() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSdkType("AIO_SDK_PG");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfo(extendInfo)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\" | grep \"" +orderDTO.getORDER_ID() +"\"";
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in payment cashier pay request")
                .contains("sdkType","AIO_SDK_PG");

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-28416")
    @Test(description = "To Validate CUI SDK Type Param is getting passed in Payment Cashier Pay if getting passed in /ptc", groups = "P0")
    public void validateCUISDKTypeParam() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSdkType("CUI_SDK_PG");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfo(extendInfo)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\" | grep \"" +orderDTO.getORDER_ID() +"\"";
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in payment cashier pay request")
                .contains("sdkType","CUI_SDK_PG");

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-28416")
    @Test(description = "Without COP : To Validate SDK Type Param is getting passed in Payment Cashier Pay if getting passed in /ptc", groups = "P0")
    public void validateSDKTypeParamWithoutCOP() throws Exception {

        FF4JFlags.disable("createOrderinIntTxn");
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabled(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSdkType("AIO_SDK_PG");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfo(extendInfo)
                .setExtendInfoDynamicFlow()
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\" | grep \"" +orderDTO.getORDER_ID() +"\"";
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in payment cashier pay request")
                .contains("sdkType","AIO_SDK_PG");

        FF4JFlags.enable("createOrderinIntTxn");

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-29553")
    @Test(description = "Validate if retry in case of payment fails, system will retry on the basis of number of allowed retry configured on merchant for same paymode", groups = "P0")
    public void validateRetryEDCMerchantCC() throws Exception {

        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_RETRY;

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "72";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(edcMerchant, txnAmount, user,true);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        QRHelper.executeProcessTransactionV1(processTxnV1Request);

        QRHelper.executeProcessTransactionV1(processTxnV1Request);

        QRHelper.executeProcessTransactionV1(processTxnV1Request);

       Assertions.assertThat(QRHelper.executeProcessTransactionV1(processTxnV1Request)).contains("Retry count breached");

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-29553")
    @Test(description = "Validate if retry in case of payment fails, system will retry on the basis of number of allowed retry configured on merchant for different paymode", groups = "P0")
    public void validateRetryEDCMerchantEmi() throws Exception {

        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_RETRY;

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "72";

        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(edcMerchant, txnAmount, user,true);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        QRHelper.executeProcessTransactionV1(processTxnV1Request);

        QRHelper.executeProcessTransactionV1(processTxnV1Request);

        QRHelper.executeProcessTransactionV1(processTxnV1Request);

        ProcessTxnV1Request emiRequest = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("EMI")
                .setChannelCode("HDFC")
                .setPlanId("HDFC|6")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        Assertions.assertThat(QRHelper.executeProcessTransactionV1(emiRequest)).contains("Retry count breached");

    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-34821")
    @Test(description = "To validate error code in v1/ptc API when the DQR gets expired")
    public void validateErrorCodeInV1PTCWhenDQRExpires() throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.MERCHANT_UPI_PPI_CC_DC_SUBS;
        User user = userManager.getForRead(Label.BASIC);
        //Generate DQR with expiry time set after 40 seconds
        String qrCodeId = QRHelper.generateQRViaPaymentServiceWithSpecificExpiryDate(merchantType, txnAmount, 40);
        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Order not created by Payment Service").isEqualTo("true");
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Order not created by Payment Service").isEqualTo("true");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        // Waiting for 40 seconds so that the DQR gets expired and then execute v1/ptc
        TimeUnit.SECONDS.sleep(40);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderId, txnAmount)
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.DQR_EXPIRED.getRespCode());
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.DQR_EXPIRED.getRespMsg());
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-34821")
    @Test(description = "To validate error code in FastForward API when the DQR gets expired")
    public void validateErrorCodeInFastForwardAPIWhenDQRExpires() throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.MERCHANT_UPI_PPI_CC_DC_SUBS;
        User user = userManager.getForRead(Label.BASIC);
        //Generate DQR with expiry time set after 40 seconds
        String qrCodeId = QRHelper.generateQRViaPaymentServiceWithSpecificExpiryDate(merchantType, txnAmount, 40);
        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Order not created by Payment Service").isEqualTo("true");
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Order not created by Payment Service").isEqualTo("true");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        // Waiting for 40 seconds so that the DQR gets expired and then execute FastForward API
        TimeUnit.SECONDS.sleep(40);

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderId, txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(qrCodeId)
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultCodeId")).isEqualTo("0001");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("PAYTM_BALANCE_PAYMENT_MODE_EXCEPTION");
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-34821")
    @Test(description = "To validate error code in v1/ptc API when the order is closed for DQR")
    public void validateErrorCodeInV1PTCWhenOrderIsClosedForDQR() throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.MERCHANT_UPI_PPI_CC_DC_SUBS;
        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderDTO.getORDER_ID()).setMid(orderDTO.getMID()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response response = closeOrderAPI.execute();
        String resultMsg = response.path("body.resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("SUCCESS");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.DQR_EXPIRED.getRespCode());
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.DQR_EXPIRED.getRespMsg());
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-34821")
    @Test(description = "To validate error code in FastForward API when the order is closed for DQR")
    public void validateErrorCodeInFastForwardAPIWhenOrderIsClosedForDQR() throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.MERCHANT_UPI_PPI_CC_DC_SUBS;
        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderDTO.getORDER_ID()).setMid(orderDTO.getMID()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response response = closeOrderAPI.execute();
        String resultMsg = response.path("body.resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("SUCCESS");

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .setQrCodeId(orderDTO.getTxnId())
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultCodeId")).isEqualTo("0001");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("PAYTM_BALANCE_PAYMENT_MODE_EXCEPTION");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify posId in payment cashier pay when we pass posId in generateqrcode request for DynamicQR flow")
    public void verifyPosIdGenerateQr_UsingDynmQR() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.BASIC);
        String qrCodeId = QRHelper.generateDynamicQRViaPaymentService(merchantType, "10",posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"10")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_PAY_ORDER" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+posId+"\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify ptc posId in payment cashier pay when we pass posId in generateqrcode and ptc request for DynamicQR flow")
    public void verifyPosIdPtceQr_UsingDynmQR() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.BASIC);
        String qrCodeId = QRHelper.generateDynamicQRViaPaymentService(merchantType, "10",posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"10")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlowWithPosId()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_PAY_ORDER" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+119988+"\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify posId in payment cashier pay when we pass posId in generateqrcode request for DynamicQR Fastforword flow")
    public void verifyPosIdGenerateQr_UsingDynmQrFastForword() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 20.00);
        String qrCodeId = QRHelper.generateDynamicQRViaPaymentService(merchantType, "10",posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderId, "10")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .setOrderAlreadyCreated("true")
                .build();
        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("body.resultInfo.resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_PAY_ORDER" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+posId+"\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify FastForword posId in payment cashier pay when we pass posId in generateqrcode and FastForword request for DynamicQR flow")
    public void verifyPosIdFastFarword_UsingDynamicQR() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        int fwPosId = CommonHelpers.getRandomWithSize(5);
        Constants.MerchantType merchantType = Constants.MerchantType.DYNAMICQR;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 20.00);
        String qrCodeId = QRHelper.generateDynamicQRViaPaymentService(merchantType, "10",posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderId, "10")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .setPosId(String.valueOf(fwPosId))
                .setOrderAlreadyCreated("true")
                .build(true);
        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("body.resultInfo.resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_PAY_ORDER" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+fwPosId+"\"");
    }
    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39566")
    @Test(description = "verify the merchant_TXN_status for EDC_Merchant dynamic QR_flow")
    public void verifyMerchant_txn_status_EDCdynamicqr_flow_TXN_SUCCESS() throws Exception {
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_SUCCESS_DC;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(edcMerchant, "100", user, true);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "100")
                .setQRCodeId(qrCodeId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum("5244519765781731")
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();
    }
    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39566")
    @Test(description = "verify the merchant_TXN_status for EDC_Merchant dynamic QR_flow")
    public void verifyMerchant_txn_status_EDCdynamicqr_flow_TXN_PENDING() throws Exception {
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_SUCCESS_DC;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(edcMerchant, "99.84", user, true);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "99.84")
                .setQRCodeId(qrCodeId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum("5244519765781731")
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("PENDING");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .AssertAll();
    }
    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39566")
    @Test(description = "verify the merchant_TXN_status for EDC_Merchant dynamic QR_flow")
    public void verifyMerchant_txn_status_EDCdynamicqr_flow_TXN_FAILURE() throws Exception {
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_SUCCESS_DC;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(edcMerchant, "99.98", user, true);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "99.98")
                .setQRCodeId(qrCodeId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum("5244519765781731")
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .AssertAll();
    }

    @Owner(Constants.Owner.SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant Accepting Postpaid & User has postpaid Enabled & Preference is ON and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should  be true display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_display_in_the_fetch_qr_details_repsone() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant1 = Constants.MerchantType.TWOFA_POSTPAID;
        String txnamount="150";

        String OrderId = CommonHelpers.generateOrderId();
        String OrderDetails=CommonHelpers.generateOrderId();

        PaymentService paymentService=new PaymentService(merchant1, txnamount, OrderId);
        paymentService.setContext("body.orderDetails",OrderDetails);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");



        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");
        Response response = qr.execute();
        response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue").equals("1500");
        response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled").equals(true);
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant Accepting Postpaid & User has postpaid Enabled & Preference is OFF and FF4j theia.enable.postpaid.2FA is OFF postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should not display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_not_display_in_the_fetch_qr_details_repsone() throws Exception {

        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant1 = Constants.MerchantType.TWOFA_POSTPAID1;
        String txnamount="150";

        String OrderId = CommonHelpers.generateOrderId();
        String OrderDetails=CommonHelpers.generateOrderId();

        PaymentService paymentService=new PaymentService(merchant1, txnamount, OrderId);
        paymentService.setContext("body.orderDetails",OrderDetails);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");
        Response response = qr.execute();
        String res=response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled");
        Assertions.assertThat(res).isNull();
        String res1= response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue");
        Assertions.assertThat(res1).isNull();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant not Accepting Postpaid & User has not postpaid Enabled & Preference is ON and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should not display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_not_be_display_in_the_fetch_qr_details_repsone_pref_ff4j_is_on() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant1 = Constants.MerchantType.TWOFA_NO_POSTPAID;
        String txnamount="150";

        String OrderId = CommonHelpers.generateOrderId();
        String OrderDetails=CommonHelpers.generateOrderId();

        PaymentService paymentService=new PaymentService(merchant1, txnamount, OrderId);
        paymentService.setContext("body.orderDetails",OrderDetails);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");
        Response response = qr.execute();
        String res=response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled");
        Assertions.assertThat(res).isNull();
        String res1= response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue");
        Assertions.assertThat(res1).isNull();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant  Accepting Postpaid & User has  postpaid Enabled & Preference is OFF and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should  display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_be_displayed_in_the_fetch_qr_details_repsone_pref_is_off_ff4j_is_on() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant1 = Constants.MerchantType.TWOFA_POSTPAID;
        String txnamount="150";

        String OrderId = CommonHelpers.generateOrderId();
        String OrderDetails=CommonHelpers.generateOrderId();

        PaymentService paymentService=new PaymentService(merchant1, txnamount, OrderId);
        paymentService.setContext("body.orderDetails",OrderDetails);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");
        Response response = qr.execute();
        response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue").equals("1500");
        response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled").equals(true);
    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-42767")
    @Test(description = "Verify Repeat Dynamic QR payment and QRDeeplink Parameter")
    public void verifyRepeatDQRTxnQRDeeplinkParam() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        Constants.MerchantType merchantType = Constants.MerchantType.QR_MERCHANT;
        User user = userManager.getForWrite(Label.BASIC);
        String qrCodeId = QRHelper.generateDynamicQRViaPaymentService(merchantType, "10",posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"10")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_PAY_ORDER" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(logs).doesNotContain("qrDeeplink");

    }
    @Owner(ASHISH_JASWAL)
    @Feature("PGP-42767")
    @Test(description = "Verify Repeat Dynamic QR payment and QRDeeplink Parameter")
    public void verifyRepeatDQRTxnQRDeeplinkParamforUPI() throws Exception {
        String paymentMode = "UPI";
        String txnAmount = "2.00";
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "UPI", "ICICI");
        String grepcmd = "grep \"" + "ACQUIRING_PAY_ORDER" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(logs).doesNotContain("qrDeeplink");
    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-42767")
    @Test(description = "Verify Repeat Dynamic QR payment and QRDeeplink Parameter")
    public void verifyRepeatDQRTxnQRDeeplinkParamforPPI() throws Exception {
        String paymentMode = "PPI";
        String txnAmount = "2.00";
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 5.00);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsPaymentService(merchantType, txnAmount, user,false);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "PPI", "WALLET");
        String grepcmd = "grep \"" + "ACQUIRING_PAY_ORDER" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getORDER_ID() + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(logs).doesNotContain("qrDeeplink");
    }
    @Owner("Pareekshith")
    @Feature("PGP-44639")
    @Test(description = "verify merchant info object in ptc response")
    public void verifyMerchantinfoDynamicQR() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 5.00);
        Constants.MerchantType merchant1 = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant1.getId(), merchant1.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();

        PaymentService paymentService = new PaymentService(merchant1, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID());
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
                    .setMID(merchant1.getId())
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant1.getId(), "SSO", user.ssoToken(), orderId,"10")
                    .setQRCodeId(qrCodeId)
                    .setExtendInfoDynamicFlow()
                    .build();
            ProcessTransactionV1 processTransactionV12 = new ProcessTransactionV1(processTxnV1Request);
            processTransactionV12.getRequestSpecBuilder().addHeader("source","OFFLINE");
            Response response = processTransactionV12.execute();
            Assertions.assertThat(response.jsonPath().getString("body.txnInfo.ADDITIONAL_PARAM").contains("merchantInfo")).isTrue();
        }
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-46250")
    @Test(description = "verify the CIN in COP for edcqr flow with newcard")
    public void verifycin_EdcQRFlow_NewCard() throws Exception {
        PaymentDTO paymentdto = new PaymentDTO();
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_CIN;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateEdcQRAndFetchQRDiffBusynessType(edcMerchant, "10", user, true,"UPI_QR_CODE",custId);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "10")
                .setQRCodeId(qrCodeId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentdto.VISA_COFT_CARD_NUMBER)
                .setExtendInfoDynamicFlow()
                .setChannelId("APP")
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success").AssertAll();
        String coft = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"coft-center/get/panUniqueReference");
       String gcin = coft.substring(coft.indexOf("globalPanIndex")+17,coft.indexOf("\"}}\","));
        String pay = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(pay).contains("\"cardIndexNo\":\""+gcin+"\"");
    }


    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-46250")
    @Test(description = "verify the CIN in COP for edcqr flow with TIN")
    public void verifycin_EdcQRFlow_Tin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_CIN;
        PaymentDTO paymentdto = new PaymentDTO();
        SavedCardHelpers.addCardOnMidCustId(edcMerchant,custId,paymentdto.getExpMonth(),paymentdto.getExpYear(),paymentdto.getCreditCardNumber());
        String tin = SavedCardHelpers.getTin();
        String gcin = SavedCardHelpers.getGcin();
        OrderDTO orderDTO = QRHelper.generateEdcQRAndFetchQRDiffBusynessType(edcMerchant, "10", user, true,"UPI_QR_CODE",custId);
        String qrCodeId = orderDTO.getTxnId();
        String cardInfo =  tin+ "||" + paymentDTO.getCvvNumber() + "|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), "10")
                .setQRCodeId(qrCodeId)
                .setCustId(custId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setExtendInfoDynamicFlow()
                .setChannelId("APP")
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success").AssertAll();
        String pay = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(pay).contains("\"cardIndexNo\":\""+gcin+"\"");
    }

    @Owner(VIDHI)
    @Feature("PGP-50339")
    @Test(description = "Verify the paymentOptionsAvailable Field in fetchQRPaymentDetails response when paymentOptionsAvailable is passed in EDC create QR request ")
    public void EDCQR_FetchQRPaymentDetailswithpaymentOptionsAvailable001() throws Exception {
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String OrderId = CommonHelpers.generateOrderId();
        String businessType="UPI_QR_CODE";
        String paymentOptionsAvailable="PAYTM_DIGITAL_CREDIT,BALANCE,UPI";
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_MERCHANT;
        User user = userManager.getForRead(Label.BASIC);
        PaymentService paymentService = new PaymentService(edcMerchant,"10",OrderId,businessType,paymentOptionsAvailable,"true");
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        System.out.println("QR CODE ID "+qrCodeId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(edcMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.paymentOptionsAvailable")).isEqualTo(paymentOptionsAvailable);

    }
    @Owner(VIDHI)
    @Feature("PGP-50339")
    @Test(description = "Verify the paymentOptionsAvailable Field in fetchQRPaymentDetails response when paymentOptionsAvailable is passed null in EDC create QR request ")
    public void EDCQR_FetchQRPaymentDetailswithpaymentOptionsAvailable002() throws Exception {
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String OrderId = CommonHelpers.generateOrderId();
        String businessType="UPI_QR_CODE";
        String paymentOptionsAvailable="";
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_CIN;
        User user = userManager.getForRead(Label.BASIC);
        PaymentService paymentService = new PaymentService(edcMerchant,"10",OrderId,businessType,paymentOptionsAvailable,"true");
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        System.out.println("QR CODE ID "+qrCodeId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.paymentOptionsAvailable")).isEqualTo("");

    }

    @Owner(VIDHI)
    @Feature("PGP-50339")
    @Test(description = "Verify the postpaid paymode in FQR response when paymentOptionsAvailable as PAYTM_DIGITAL_CREDIT,BALANCE,UPI is passed in EDC  create QR request ")
    public void EDCQR_FetchQRPaymentDetailswithpaymentOptionsAvailable003() throws Exception {
        //String paymode="";
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String OrderId = CommonHelpers.generateOrderId();
        String businessType="UPI_QR_CODE";
        String paymentOptionsAvailable="PAYTM_DIGITAL_CREDIT,BALANCE,UPI";
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_CIN;
        User user = userManager.getForRead(Label.BASIC);
        PaymentService paymentService = new PaymentService(edcMerchant,"10",OrderId,businessType,paymentOptionsAvailable,"true");
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        System.out.println("QR CODE ID "+qrCodeId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        for(int n=0;n<3;n++) {
          String   paymode = fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode[" + n + "]");
            if (paymode.equals("PAYTM_DIGITAL_CREDIT")) {
                assert(true);
            }
        }
}

    @Owner(VIDHI)
    @Feature("PGP-50339")
    @Test(description = "Verify the BALANCE paymode in FQR response when paymentOptionsAvailable as PAYTM_DIGITAL_CREDIT,BALANCE,UPI is passed in EDC  create QR request ")
    public void EDCQR_FetchQRPaymentDetailswithpaymentOptionsAvailable004() throws Exception {
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String OrderId = CommonHelpers.generateOrderId();
        String businessType="UPI_QR_CODE";
        String paymentOptionsAvailable="PAYTM_DIGITAL_CREDIT,BALANCE,UPI";
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_CIN;
        User user = userManager.getForRead(Label.BASIC);
        PaymentService paymentService = new PaymentService(edcMerchant,"10",OrderId,businessType,paymentOptionsAvailable,"true");
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        System.out.println("QR CODE ID "+qrCodeId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        for(int n=0;n<3;n++) {
            String   paymode = fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode[" + n + "]");
            if (paymode.equals("BALANCE")) {
                assert(true);
            }
        }
    }
    @Owner(VIDHI)
    @Feature("PGP-50339")
    @Test(description = "Verify the UPI paymode in FQR response when paymentOptionsAvailable as PAYTM_DIGITAL_CREDIT,BALANCE,UPI is passed in EDC  create QR request ")
    public void EDCQR_FetchQRPaymentDetailswithpaymentOptionsAvailable005() throws Exception {
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String OrderId = CommonHelpers.generateOrderId();
        String businessType="UPI_QR_CODE";
        String paymentOptionsAvailable="PAYTM_DIGITAL_CREDIT,BALANCE,UPI";
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_CIN;
        User user = userManager.getForRead(Label.BASIC);
        PaymentService paymentService = new PaymentService(edcMerchant,"10",OrderId,businessType,paymentOptionsAvailable,"true");
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        System.out.println("QR CODE ID "+qrCodeId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        for(int n=0;n<3;n++) {
            String   paymode = fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode[" + n + "]");
            if (paymode.equals("UPI")) {
                assert(true);
            }
        }
    }
    @Owner(VIDHI)
    @Feature("PGP-50339")
    @Test(description = "Verify the CREDIT_CARD paymode should not come in FQR response when paymentOptionsAvailable as PAYTM_DIGITAL_CREDIT,BALANCE,UPI is passed in EDC  create QR request ")
    public void EDCQR_FetchQRPaymentDetailswithpaymentOptionsAvailable006() throws Exception {
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String OrderId = CommonHelpers.generateOrderId();
        String businessType="UPI_QR_CODE";
        String paymentOptionsAvailable="PAYTM_DIGITAL_CREDIT,BALANCE,UPI";
        Constants.MerchantType edcMerchant = Constants.MerchantType.EDC_QR_CIN;
        User user = userManager.getForRead(Label.BASIC);
        PaymentService paymentService = new PaymentService(edcMerchant,"10",OrderId,businessType,paymentOptionsAvailable,"true");
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        System.out.println("QR CODE ID "+qrCodeId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        for(int n=0;n<3;n++) {
            Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode[" + n + "]")).isNotEqualTo("CREDIT_CARD");
        }
    }
}