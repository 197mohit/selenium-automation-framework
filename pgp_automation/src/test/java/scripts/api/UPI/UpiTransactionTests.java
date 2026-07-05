package scripts.api.UPI;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingService.GetAlipayIdDetail;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TransactionStatusV1API;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.PaymentOptions;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.DirectBankOTPPage;
import com.paytm.pages.OopsPage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.appconstants.Constants.Owner.AJEESH;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

@Owner(ABHISHEK_KULKARNI)
public class UpiTransactionTests extends PGPBaseTest {

    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
    private final OopsPage oopsPage = new OopsPage();
    private final String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    private final String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private static final String PASS_THROUGH_EXTEND_INFO_PATTERN = "\"passThroughExtendInfo\":\"([^\"]*)\"";
    private static final String EXTEND_INFO_PATTERN = "\"extendInfo\":\"([^\"]*)\"";
    private static final String PAYMENT_TIMEOUT_FIELD = "paymentTimeoutInMinsForUPI";

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }

    public JsonPath Validate_FetchPayInstrumentV2(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        return fetchPaymentOptionsJson;
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate UPI push transaction")
    public void validateUpiPushTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHNEW).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHNEW.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate UPI push transaction with Andflow promo")
    public void validateUpiPushTxnWithAndPromo() throws Exception{
        Constants.MerchantType promoMerchant = Constants.MerchantType.UPIPUSHNEW;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "UPI", "PPBEX", "", "", null);
        ApplyPromoDTO applyPromoDTO = new ApplyPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(token)
                .setMID(promoMerchant.getId())
                .setPromocode(promocode.getName())
                .setPaymentOptions(new PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ApiV1ApplyPromo applypromo = new ApiV1ApplyPromo(applyPromoDTO);

        Response response = applypromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token,promoMerchant, paymentOffersApplied).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHNEW.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .validatePaymentPromoCheckoutDataPresent()
                .AssertAll();
        Assert.assertTrue(txnStatus.getResponse().getPaymentPromoCheckoutData().contains(promocode.getName()));

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate UPI push transaction with Simplified flow promo")
    public void validateUpiPushTxnWithSimplifiedPromo() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.UPIPUSHNEW;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promocode.getName()).setApplyAvailablePromo("true").setValidatePromo("true");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token,promoMerchant, simplifiedPaymentOffers).setTxnValue("100").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHNEW.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .validatePaymentPromoCheckoutDataPresent()
                .AssertAll();
        Assert.assertTrue(txnStatus.getResponse().getPaymentPromoCheckoutData().contains(promocode.getName()));

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Failure UPI push transaction")
    public void validateUpiPushFailTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHNEW).setTxnValue("39").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHNEW.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Pending State UPI push transaction")
    public void validateUpiPushPendingTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHNEW).setTxnValue("52").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHNEW.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("PENDING")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .AssertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.UPIPUSHNEW.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Looks like the payment is not complete. Please wait while we confirm the status with your bank.");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate UPI push Hybrid transaction")
    public void validateUpiPushHybridTxn() throws Exception{
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHHYB).setTxnValue("10").build();
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHHYB.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setPayerAccount("arsh.test2@paytm")
                .setPaymentFlow("HYBRID")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateTxnAmount("10")
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("HYBRID")
                .validateChildTxnsPresent()
                .AssertAll();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.PPBEX)
                .validateTxnId(TxnStatus.ChildTxnType.PPBEX, Constants.ValidationType.NON_EMPTY)
                .validateTxnAmount(TxnStatus.ChildTxnType.PPBEX, "1.00")
                .validateGatewayName(TxnStatus.ChildTxnType.PPBEX, Constants.Gateway.PPBEX.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.PPBEX, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.PPBEX, "TXN_SUCCESS")
                .AssertAll();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate UPI push AddAndPay transaction")
    public void validateUpiPushAddAndPayTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHAAP).setTxnValue("10").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHAAP.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setPayerAccount("arsh.test2@paytm")
                .setPaymentFlow("ADDANDPAY")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnAmount("10")
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Owner(HIMANSHU)
    @Feature("PGP-33485")
    @Test(description = "Validate correct failure code and reason being sent for failed UPI Push txn")
    public void PGP_34485_p2mErrorCodes() throws Exception{
        String expectedResponseCode="227";
        String expectedResultCode="FGW_BANK_FAIL_SPECIFIC_REASON";
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.UPIPUSHNEW;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("39").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID= initTxnDTO.orderFromBody();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(),txnToken,orderID,null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();


        String cmdToFetchInstaLogger=" grep \"" + "216820000007690284669" + "\" /paytm/logs/instaproxy.log" + " | grep \"FLUXNET_UPI_PAYMENT_RESULT\"";
        String cmdToFetchTheiaLogger=" grep \"" + orderID + "\" /paytm/logs/theia.log" + " | grep \"Final responseCodeDetails for queryRespCode\"";
        String instaLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, cmdToFetchInstaLogger), s -> !"".equals(s));
        String theiaLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchTheiaLogger), s -> !"".equals(s));
        Assertions.assertThat(instaLogger.contains(expectedResultCode)&&instaLogger.contains(expectedResponseCode));
        Assertions.assertThat(theiaLogger.contains(expectedResultCode)&&theiaLogger.contains(expectedResponseCode));

    }
    //Flag theia.enableUPIOnboardingForNewBankAcct
    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39832")
    @Test(description = "Verify the value of field onboardNewUPIBankAcct in FPO (Txn Tocken) response FF4j Flag Off")
    public void Verify_the_value_of_field_onboardNewUPIBankAcct_ONus() throws Exception{

        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_ONUS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(merchantType.getId(),initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.onboardNewUPIBankAcct")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39832")
    @Test(description = "Verify the value of field onboardNewUPIBankAcct in FPO (Txn Tocken) response FF4j Flag Off")
    public void Verify_the_value_of_field_onboardNewUPIBankAcct_OFFus() throws Exception{

        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_3;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(merchantType.getId(),initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.onboardNewUPIBankAcct")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39832")
    @Test(description = "Verify the value of field onboardNewUPIBankAcct in FPO (Txn Tocken) response  FF4j Flag ON ONUS")
    public void Verify_the_value_of_field_onboardNewUPIBankAcct3() throws Exception{

        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(merchantType.getId(),initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.onboardNewUPIBankAcct")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39832")
    @Test(description = "Verify the value of field onboardNewUPIBankAcct in FPO (Txn Tocken) response  Flag ON OffUs")
    public void Verify_the_value_of_field_onboardNewUPIBankAcct4() throws Exception{

        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_NEW_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(merchantType.getId(),initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.onboardNewUPIBankAcct")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39832")
    @Test(description = "Verify the value of field onboardNewUPIBankAcct in FPO (Txn Tocken) response ADD_MONEY FF4j Flag ON")
    public void Verify_the_value_of_field_onboardNewUPIBankAcct5() throws Exception{

        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_NEW_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").setIsNativeAddMoney("true").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(merchantType.getId(),initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.onboardNewUPIBankAcct")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }

    @Owner(Constants.Owner.DEVENDRA_SINGH)
    @Feature("PGP-39832")
    @Test(description = "Verify the value of field onboardNewUPIBankAcct in FPO (Txn Tocken) response FF4j flag ON User w/o UPI")
    public void Verify_the_value_of_field_onboardNewUPIBankAcct6() throws Exception{

        User user = userManager.getForRead(Label.BASIC,Label.NOPOSTPAID);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_NEW_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue("2").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(merchantType.getId(),initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.onboardNewUPIBankAcct")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }


    @Feature("PGP-40525")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validate revocable=N for UPI push transaction when MCC Code = 7322 and SUBSCRIPTION_REVOCABLE_FLAG is enabled")
    public void PGP_40525_TC_01_validateUpiPushTxnSubs() throws Exception{
        FF4JFlags.enable("SUBSCRIPTION_REVOCABLE_FLAG");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_ALL_PAYMODES;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

       ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SUBS_ALL_PAYMODES.getId(),responseDTO.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setUpiAccRefId("222939")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();
        String grepcmd = "grep \"Payment Request\" /paytm/logs/instaproxy.log | grep "+ initTxnDTO.getBody().getOrderId() +"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("\"revocable\""+":"+"\"N\"");

    }

    @Feature("PGP-40525")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Validate revocable=Y for UPI push transaction when MCC Code = 7322 and SUBSCRIPTION_REVOCABLE_FLAG is enabled")
    public void PGP_40525_TC_02_validateUpiPushTxnSubs() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.UPI_NATIVE_SUBS.getId(),responseDTO.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setUpiAccRefId("222939")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();
        String grepcmd = "grep \"Payment Request\" /paytm/logs/instaproxy.log | grep "+ initTxnDTO.getBody().getOrderId() +"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("\"revocable\""+":"+"\"Y\"");

    }

    @Feature("PGP-52310")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Txn Limit Error Msg For UPI Push Txn")
    public void ValidateTxnLimitForUPIPush() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPILITE_LIMIT).setTxnValue("251").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPILITE_LIMIT.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110055\"");
    }

    @Feature("PGP-52310")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Daily Limit Error Msg For UPI Push Txn")
    public void ValidateDailyLimitForUPIPush() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_CC_LIMIT).setTxnValue("261").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPI_CC_LIMIT.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110053\"");
    }

    @Feature("PGP-52310")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Monthly Limit Error Msg For UPI Push Txn")
    public void ValidateMonthlyLimitForUPIPush() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_CC_LIMIT).setTxnValue("271").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPI_CC_LIMIT.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110054\"");
    }

    @Feature("PGP-52310")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Txn Limit Error Msg For UPI Push COTP Txn")
    public void ValidateTxnLimitForUPIPushCotp() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_PUSH_TXNLIMIT_COTP).setTxnValue("251").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPI_PUSH_TXNLIMIT_COTP.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110055\"");
    }

    @Feature("PGP-52310")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Daily Limit Error Msg For UPI Push COTP Txn")
    public void ValidateDailyLimitForUPIPushCotp() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_PUSH_LIMIT_COTP).setTxnValue("261").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPI_PUSH_LIMIT_COTP.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110053\"");
    }

    @Feature("PGP-52310")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate the Monthly Limit Error Msg For UPI Push COTP Txn")
    public void ValidateMonthlyLimitForUPIPushCotp() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_PUSH_LIMIT_COTP).setTxnValue("271").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPI_PUSH_LIMIT_COTP.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_PUSH_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110054\"");
    }
    @Feature("PGP-55179")
    @Owner(HIMANSHU)
    @Parameters({"isNativePlus"})
    @Test(description = "Validate internalErrorCode and internalErrorCodeId come in PTC resp if pref: ENABLE_UPI_WRONG_MPIN_UX is on for mid for UPI Push failure txn")
    public void ValidateIncorrectPinPTCResp_UPIPush_Pref_On() throws Exception
    {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        MerchantType merchantType=MerchantType.HIGH_PRIORITY_SMS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("29.12")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), txnToken, orderId, null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(response.getBody().getResultInfo().getResultMsg()).isEqualTo("Mapping-Payment failed - Wrong Pin. Retry payment with correct UPI Pin");
        Assertions.assertThat(response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_FAILURE");

        String errorCodeID_PTC = response.getBody().getTxnInfo().getAdditionalProperties().get("INTERNALERRORCODEID").toString();
        String errorCode_PTC = response.getBody().getTxnInfo().getAdditionalProperties().get("INTERNALERRORCODE").toString();
        String instaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "thirdResultInfo");
        Assertions.assertThat(instaLogs).contains(errorCode_PTC);
        Assertions.assertThat(instaLogs).contains(errorCodeID_PTC);

    }

    @Feature("PGP-55179")
    @Owner(HIMANSHU)
    @Parameters({"isNativePlus"})
    @Test(description = "Validate internalErrorCode and internalErrorCodeId comes in PTC resp if pref: ENABLE_UPI_WRONG_MPIN_UX is on for mid for UPI Lite failure txn")
    public void ValidateIncorrectPinPTCResp_UPILite_Pref_On() throws Exception
    {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        MerchantType merchant=MerchantType.HIGH_PRIORITY_SMS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("30.60")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId= initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),txnToken,orderId,null,null,null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(response.getBody().getResultInfo().getResultMsg()).isEqualTo("Mapping-Payment failed - Wrong Pin. Retry payment with correct UPI Pin");
        Assertions.assertThat(response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_FAILURE");

        String errorCodeID_PTC = response.getBody().getTxnInfo().getAdditionalProperties().get("INTERNALERRORCODEID").toString();
        String errorCode_PTC = response.getBody().getTxnInfo().getAdditionalProperties().get("INTERNALERRORCODE").toString();

        String instaLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "thirdResultInfo");

        Assertions.assertThat(instaLogs).contains(errorCode_PTC);
        Assertions.assertThat(instaLogs).contains(errorCodeID_PTC);


    }

    @Feature("PGP-55179")
    @Owner(HIMANSHU)
    @Parameters({"isNativePlus"})
    @Test(description = "Validate internalErrorCode and internalErrorCodeId don't come in PTC resp if pref: ENABLE_UPI_WRONG_MPIN_UX is off for mid for UPI Push failure txn")
    public void ValidateIncorrectPinPTCResp_UPIPush_Pref_Off() throws Exception
    {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        MerchantType merchantType=MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("29.12")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), txnToken, orderId, null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(response.getBody().getResultInfo().getResultMsg()).isEqualTo("Mapping-Payment failed - Wrong Pin. Retry payment with correct UPI Pin");
        Assertions.assertThat(response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_FAILURE");

        String PTCresponse=response.getBody().getTxnInfo().getAdditionalProperties().toString();
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODEID");
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODE");


    }

    @Feature("PGP-55179")
    @Owner(HIMANSHU)
    @Parameters({"isNativePlus"})
    @Test(description = "Validate internalErrorCode and internalErrorCodeId don't come in PTC resp if pref: ENABLE_UPI_WRONG_MPIN_UX is off for mid for UPI Lite failure txn")
    public void ValidateIncorrectPinPTCResp_UPILite_Pref_Off() throws Exception
    {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        MerchantType merchant=MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("30.60")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId= initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),txnToken,orderId,null,null,null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(response.getBody().getResultInfo().getResultMsg()).isEqualTo("Mapping-Payment failed - Wrong Pin. Retry payment with correct UPI Pin");
        Assertions.assertThat(response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_FAILURE");

        String PTCresponse=response.getBody().getTxnInfo().getAdditionalProperties().toString();
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODEID");
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODE");


    }

    @Feature("PGP-55179")
    @Owner(HIMANSHU)
    @Parameters({"isNativePlus"})
    @Test(description = "Validate internalErrorCode and internalErrorCodeId don't come in PTC resp if pref: ENABLE_UPI_WRONG_MPIN_UX is on for mid for UPI Push success txn")
    public void ValidateSuccessPTCResp_UPIPush_Pref_On() throws Exception
    {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        MerchantType merchantType=MerchantType.HIGH_PRIORITY_SMS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("5")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId=initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), txnToken, orderId, null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_SUCCESS");

        String PTCresponse=response.getBody().getTxnInfo().getAdditionalProperties().toString();
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODEID");
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODE");


    }

    @Feature("PGP-55179")
    @Owner(HIMANSHU)
    @Parameters({"isNativePlus"})
    @Test(description = "Validate internalErrorCode and internalErrorCodeId don't come in PTC resp if pref: ENABLE_UPI_WRONG_MPIN_UX is on for mid for UPI Lite success txn")
    public void ValidateSuccessPTCResp_UPILite_Pref_On() throws Exception
    {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        MerchantType merchant=MerchantType.HIGH_PRIORITY_SMS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId= initTxnDTO.getBody().getOrderId();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),txnToken,orderId,null,null,null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_SUCCESS");

        String PTCresponse=response.getBody().getTxnInfo().getAdditionalProperties().toString();
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODEID");
        Assertions.assertThat(PTCresponse).doesNotContain("INTERNALERRORCODE");
    }



    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Test(description = "Verify the UPI_SAVINGS PayMode in SuperCash Offers Request When SolutionType=OFFLINE AND FF4J enable")
    public void VerifyUpiSavingsRequestForOfflineandff4jEnable() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_SUPERCASH).setTxnValue("20").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrumentV2(txnToken, initTxnDTO, "UPI", "false");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "SUPERCASH_SERVICE");
        Assertions.assertThat(theia_facade).contains("\"paymentMode\":\"UPI_SAVINGS\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Test(description = "Verify the UPI_SAVINGS PayMode in SuperCash Offers Request When SolutionType=OFFLINE AND FF4J Disable")
    public void VerifyUpiSavingsRequestForOfflineandff4jDisable() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_SUPERCASH_FF4J_DISABLE).setTxnValue("20").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrumentV2(txnToken, initTxnDTO, "UPI", "false");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "SUPERCASH_SERVICE");
        Assertions.assertThat(theia_facade).doesNotContain("\"paymentMode\":\"UPI_SAVINGS\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-53502")
    @Test(description = "Verify the UPI_SAVINGS PayMode in SuperCash Offers Request When SolutionType=ONLINE AND FF4J enable")
    public void VerifyUpiSavingsRequestForOnlineff4jEnable() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_SUPERCASH_ONLINE).setTxnValue("20").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrumentV2(txnToken, initTxnDTO, "UPI", "false");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "SUPERCASH_SERVICE");
        Assertions.assertThat(theia_facade).doesNotContain("\"paymentMode\":\"UPI_SAVINGS\"");
    }
    @Owner(AJEESH)
    @Feature("PGP-59165")
    @Test(description = "Validate functionality Order Timeout getting captured correctly in Online Intent")
    public void verifyThatOrderTimeoutisCapturedwhenFlagisOnCOP() throws InterruptedException {

        Constants.MerchantType MerchID = NON_TPV_MERCHANT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchID)
                .setTxnValue("2.00")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchID.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        ProcessTxnV1Request processTxnV1Request = builder
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();

        // Extract timeout from Theia facade logs
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_CREATE_ORDER_AND_PAY"
        );

        String passThroughExtendInfo = PGPHelpers.extractFieldWithRegex(logsResponse, PASS_THROUGH_EXTEND_INFO_PATTERN);
        System.out.println("PassThroughExtendInfo: " + passThroughExtendInfo);

        String decodedBody = PGPHelpers.decodeBase64(passThroughExtendInfo);
        System.out.println("Decoded Theia Body: " + decodedBody);

        JsonPath jsonPath = new JsonPath(decodedBody);
        String theiaTimeout = jsonPath.getString(PAYMENT_TIMEOUT_FIELD);

        // Extract timeout from Insta proxy logs
        String logsResponse1 = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy,
                orderId,
                "pg.router.paytm.upi.payment.request"
        );
        System.out.println("Insta Proxy Logs: " + logsResponse1);

        String extendInfo = PGPHelpers.extractFieldWithRegex(logsResponse1, EXTEND_INFO_PATTERN);
        System.out.println("ExtendInfo: " + extendInfo);

        String decodedExtendInfo = PGPHelpers.decodeBase64(extendInfo);
        JsonPath jsonPath1 = new JsonPath(decodedExtendInfo);
        String passThroughExtendInfo1 = jsonPath1.getString("passThroughExtendInfo");

        String finalDecodedBody = PGPHelpers.decodeBase64(passThroughExtendInfo1);
        System.out.println("Decoded Insta Body: " + finalDecodedBody);

        JsonPath jsonPath2 = new JsonPath(finalDecodedBody);
        String instaTimeout = jsonPath2.getString(PAYMENT_TIMEOUT_FIELD);

        // Assert timeout values are equal
        Assertions.assertThat(theiaTimeout)
                .as("Payment timeout values should be same in both logs")
                .isEqualToIgnoringCase(instaTimeout);
    }

    @Owner(AJEESH)
    @Feature("PGP-59165")
    @Test(description = "Validate functionality Order Timeout getting captured correctly in Online Intent")
    public void verifyThatOrderTimeoutisCapturedwhenFlagisOnPayOrder() throws InterruptedException {

        Constants.MerchantType MerchID = UPI_ONLY;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchID)
                .setTxnValue("2.00")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchID.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        ProcessTxnV1Request processTxnV1Request = builder
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();

        // Extract timeout from Theia facade logs
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_PAY_ORDER"
        );

        String passThroughExtendInfo = PGPHelpers.extractFieldWithRegex(logsResponse, PASS_THROUGH_EXTEND_INFO_PATTERN);
        System.out.println("PassThroughExtendInfo: " + passThroughExtendInfo);

        String decodedBody = PGPHelpers.decodeBase64(passThroughExtendInfo);
        System.out.println("Decoded Theia Body: " + decodedBody);

        JsonPath jsonPath = new JsonPath(decodedBody);
        String theiaTimeout = jsonPath.getString(PAYMENT_TIMEOUT_FIELD);

        // Extract timeout from Insta proxy logs
        String logsResponse1 = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy,
                orderId,
                "pg.router.paytm.upi.payment.request"
        );
        System.out.println("Insta Proxy Logs: " + logsResponse1);

        String extendInfo = PGPHelpers.extractFieldWithRegex(logsResponse1, EXTEND_INFO_PATTERN);
        System.out.println("ExtendInfo: " + extendInfo);

        String decodedExtendInfo = PGPHelpers.decodeBase64(extendInfo);
        JsonPath jsonPath1 = new JsonPath(decodedExtendInfo);
        String passThroughExtendInfo1 = jsonPath1.getString("passThroughExtendInfo");

        String finalDecodedBody = PGPHelpers.decodeBase64(passThroughExtendInfo1);
        System.out.println("Decoded Insta Body: " + finalDecodedBody);

        JsonPath jsonPath2 = new JsonPath(finalDecodedBody);
        String instaTimeout = jsonPath2.getString(PAYMENT_TIMEOUT_FIELD);

        // Assert timeout values are equal
        Assertions.assertThat(theiaTimeout)
                .as("Payment timeout values should be same in both logs")
                .isEqualToIgnoringCase(instaTimeout);
    }

    @Owner(AJEESH)
    @Feature("PGP-59165")
    @Test(description = "Validate that PAYMENT_TIMEOUT_FIELD should NOT be present in logs")
    public void verifyPaymentTimeoutFieldShouldNotBePresentInLogs() throws InterruptedException {

        Constants.MerchantType MerchID = UPI_INTENT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchID)
                .setTxnValue("2.00")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchID.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        ProcessTxnV1Request processTxnV1Request = builder
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();

        // Extract timeout from Theia facade logs
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_CREATE_ORDER_AND_PAY"
        );
        System.out.println("Theia Facade Logs: " + logsResponse);

        String passThroughExtendInfo = PGPHelpers.extractFieldWithRegex(logsResponse, PASS_THROUGH_EXTEND_INFO_PATTERN);
        System.out.println("PassThroughExtendInfo: " + passThroughExtendInfo);

        String decodedBody = PGPHelpers.decodeBase64(passThroughExtendInfo);
        System.out.println("Decoded Theia Body: " + decodedBody);

        // Assert that PAYMENT_TIMEOUT_FIELD should NOT be present in Theia logs
        Assertions.assertThat(decodedBody)
                .as("PAYMENT_TIMEOUT_FIELD should NOT be present in Theia facade logs")
                .doesNotContain(PAYMENT_TIMEOUT_FIELD);

        // Extract timeout from Insta proxy logs
        String logsResponse1 = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy,
                orderId,
                "pg.router.paytm.upi.payment.request"
        );
        System.out.println("Insta Proxy Logs: " + logsResponse1);

        String extendInfo = PGPHelpers.extractFieldWithRegex(logsResponse1, EXTEND_INFO_PATTERN);
        System.out.println("ExtendInfo: " + extendInfo);

        String decodedExtendInfo = PGPHelpers.decodeBase64(extendInfo);
        JsonPath jsonPath1 = new JsonPath(decodedExtendInfo);
        String passThroughExtendInfo1 = jsonPath1.getString("passThroughExtendInfo");

        String finalDecodedBody = PGPHelpers.decodeBase64(passThroughExtendInfo1);
        System.out.println("Decoded Insta Body: " + finalDecodedBody);

        // Assert that PAYMENT_TIMEOUT_FIELD should NOT be present in Insta logs
        Assertions.assertThat(finalDecodedBody)
                .as("PAYMENT_TIMEOUT_FIELD should NOT be present in Insta proxy logs")
                .doesNotContain(PAYMENT_TIMEOUT_FIELD);

        // Additional assertion: Check that the decoded JSON doesn't contain the field
        Assertions.assertThat(finalDecodedBody)
                .as("Decoded JSON should not contain paymentTimeoutInMinsForUPI field")
                .doesNotContain("paymentTimeoutInMinsForUPI");
    }

}

