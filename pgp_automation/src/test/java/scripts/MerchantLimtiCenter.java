package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.CloseOrder;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.UpdateTransaction.UpdateTransactionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.*;
import static io.restassured.RestAssured.given;



public class MerchantLimtiCenter extends PGPBaseTest {

    private final String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    private final String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";

    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse, InitTxnDTO initTxnDTO) {
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getBANKTXNID()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getCURRENCY()).isEqualTo("INR");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getMID()).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getORDERID()).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT()).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
    }

    public static String generateQRViaWallet(Constants.MerchantType merchantType)
    {
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"");
        JsonPath generateJson = generateQR.execute().jsonPath();
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate  PostPaid Transaction limit exceed error and postpaid remaining limit")
    public void validatePostPaidTransactionLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("9").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'Paytm Postpaid' }[0].remainingLimit")).isEqualTo("8.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Postpaid transaction limit exceeded due to which transaction won't processed");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate  UPI Push Daily  limit exceed error and UPI Push remaining limit")
    public void validateUPIPushDailyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("6").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].payOptionRemainingLimits[0].remainingLimit")).isEqualTo("5.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant UPI daily acceptance limit exceeded, please try with another payment option");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate  UPI Monthly limit exceed error and UPI remaining limit")
    public void validateUPIMonthlyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("13").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].remainingLimit")).isEqualTo("10.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant UPI monthly acceptance limit exceeded, please try with another payment option");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate Wallet Transaction limit exceed error and Wallet remaining limit")
    public void validateWalletTransactionLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'Paytm Balance' }[0].remainingLimit")).isEqualTo("4.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Wallet transaction limit exceeded due to which transaction won't processed");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate Netbanking Daily limit exceed error and Netbanking remaining limit")
    public void validateNetBankingDailyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("11").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'Net Banking' }[0].remainingLimit")).isEqualTo("10.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant NetBanking daily acceptance limit exceeded, please try with another payment option");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate Netbanking Monthly limit exceed error and Netbanking remaining limit")
    public void validateNetBankingMonthlyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("13").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'Net Banking' }[0].remainingLimit")).isEqualTo("10.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant NetBanking monthly acceptance limit exceeded, please try with another payment option");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate Domestic Card Daily limit exceed error")
    public void validateDomesticCardDailyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("11").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Debit card daily acceptance limit exceeded, please try with another payment option");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate Domestic Card Monthly limit exceed error")
    public void validateDomesticCardMonthlyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("13").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Debit card monthly acceptance limit exceeded, please try with another payment option");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate Merchant Daily limit exceed error and Merchant remaining limit")
    public void validateMerchantDailyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("11000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantLimitInfo.remainingLimit")).isEqualTo("10000.00");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantLimitInfo.merchantRemainingLimits[0].amount")).isEqualTo("10000.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Payment failed as merchant has crossed his daily acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-12876")
    @Test(description = "Validate  Monthly limit exceed error and Merchant remaining limit")
    public void validateMerchantMonthlyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("21000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantLimitInfo.remainingLimit")).isEqualTo("10000.00");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantLimitInfo.merchantRemainingLimits[1].amount")).isEqualTo("20000.00");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Payment failed as merchant has crossed his Monthly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate  subPayMode remaining limit for offline small merchant and limit is not applied on merchant")
    public void validatesubPayModeRemainingLimitsforOfflineSmall() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.OFFLINE_SMALL_STATIC_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:2000.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:2000.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate  subPayMode remaining limit for offline null merchant and limit is applied 1500")
    public void validatesubPayModeRemainingLimitsforOfflineNull() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.OFFLINE_NULL_STATIC_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:1500.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:1500.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate  subPayMode remaining limit for offline Big merchant and CC_ON_UPI_RAILS_ENABLED is N")
    public void validatesubPayModeRemainingLimitsforOfflineBig() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.OFFLINE_BIG_STATIC_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate  subPayMode remaining limit for online merchant and UPI_CC_BLACKLISTED is Y")
    public void validatesubPayModeRemainingLimitsforOnline() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.ONLINE_STATIC_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate FQR subPayMode remaining limit for offline small merchant and limit is not applied on merchant ")
    public void validateFQRsubPayModeRemainingLimitsforOfflineSmall() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.OFFLINE_SMALL_STATIC_LIMIT_MID;
        // generating QR code ID for the merchant
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:2000.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:2000.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate  subPayMode remaining limit for offline null merchant  and limit is applied 1500")
    public void validateFQRsubPayModeRemainingLimitsforOfflineNull() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.OFFLINE_NULL_STATIC_LIMIT_MID;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:1500.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:1500.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate  subPayMode remaining limit for offline Big merchant and CC_ON_UPI_RAILS_ENABLED is N")
    public void validateFQRsubPayModeRemainingLimitsforOfflineBig() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.OFFLINE_BIG_STATIC_LIMIT_MID;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-14933")
    @Test(description = "Validate  subPayMode remaining limit for online merchant and UPI_CC_BLACKLISTED is Y")
    public void validateFQRsubPayModeRemainingLimitsforOnline() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.ONLINE_STATIC_LIMIT_MID;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:CREDIT, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:0.00, limitType:PPIWALLET, channelCode:null]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'BHIM UPI' }[0].subPayModeRemainingLimits")).contains("[remainingLimit:200.00, limitType:LITE, channelCode:null]");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-5175")
    @Test(description = "validate KYB Daily Limit")
    public void validateKYBDailyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.KYB_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("21000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Business daily acceptance limit exceeded due to which transaction won't be processed");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-5175")
    @Test(description = "validate KYB Daily Limit — txn amount 20001")
    public void validateKYBDailyLimitAmount20001() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.KYB_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("20001").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Business daily acceptance limit exceeded due to which transaction won't be processed");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-5175")
    @Test(description = "validate KYB Monthly Limit")
    public void validateKYBMonthlyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.KYB_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("31000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Business Monthly acceptance limit exceeded due to which transaction won't be processed");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-5175")
    @Test(description = "validate KYB Monthly Limit — txn amount 30001")
    public void validateKYBMonthlyLimitAmount30001() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.KYB_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("30001").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Merchant Business Monthly acceptance limit exceeded due to which transaction won't be processed");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-5175")
    @Test(description = "validate KYB no Limit exceed")
    public void validateKYBNoLimitExceed() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.KYB_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("10").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-5175")
    @Test(description = "validate KYB no limit exceed — txn amount 20")
    public void validateKYBNoLimitExceedAmount20() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.KYB_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("20").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchant.getId())
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();
    }

}
