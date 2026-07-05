package scripts;

import com.paytm.api.TransactionStatusV1API;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.theia.DeferredSubsAPI.*;
import scripts.api.theia.FPODeferredSubs;
import scripts.api.theia.InitiateSubsDeferred;


public class DeferredFlowSubs extends PGPBaseTest{

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final RiskRejectHelper riskRejectHelper = new RiskRejectHelper();

    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify FPO response for Deferred Subscription Onus Flow")
    public void validateDeferredFlowSubsFPOResponse(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.toString().contains("accessTokenValue"));
        Assertions.assertThat(response.jsonPath().getString("body.accessTokenValue")).isNotEmpty();
        Assertions.assertThat(response.toString().contains("subscriptionDetail"));
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.merchantPayOption.paymentModes.displayName"))
                .contains("BHIM UPI", "Credit Card", "Debit Card", "Bank Account (E-mandate)");
        Assertions.assertThat(response.jsonPath().getString("body.merchantDetails.isOnus")).isNotEmpty();

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify Initiate subscription response from access token passed in request")
    public void validateDeferredFlowInitSubsResponse(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionId")).isNotEmpty();

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify e-2-e subscription txn for wallet via access token in initiate API")
    public void validateDeferredFlowSubsTxnE2EWallet(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.LOGIN);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionId")).isNotEmpty();

        String txnToken = response.jsonPath().getString("body.txnToken").toString();
        String subsId = response.jsonPath().getString("body.subscriptionId").toString();
        WalletHelpers.modifyBalance(user,300.00);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, orderId, txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify e-2-e subscription txn for UPI via access token in initiate API")
    public void validateDeferredFlowSubsTxnE2EUPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID_1;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionId")).isNotEmpty();

        String txnToken = response.jsonPath().getString("body.txnToken").toString();
        String subsId = response.jsonPath().getString("body.subscriptionId").toString();
     //   WalletHelpers.modifyBalance(user,300.00);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, orderId, txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount("9999661503@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify e-2-e subscription txn for PPBL via access token in initiate API")
    public void validateDeferredFlowSubsTxnE2EPPBL(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID_1;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionId")).isNotEmpty();

        String txnToken = response.jsonPath().getString("body.txnToken").toString();
        String subsId = response.jsonPath().getString("body.subscriptionId").toString();
        WalletHelpers.modifyBalance(user,300.00);

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, orderId, txnToken, PayMethodType.PPBL, subsId)
                .setAUTH_MODE("USRPWD")
                .setMpin("1234")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("PPBL")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateSubsid(subsId)
                .AssertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderDTO.getORDER_ID()))
                .as("acquirement_id is null")
                .isNotNull();
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderDTO.getORDER_ID()))
                .as("subs status mismatch")
                .isEqualTo("ACTIVE");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify e-2-e subscription txn for Bank Mandate via access token in initiate API")
    public void validateDeferredFlowSubsTxnE2EBM(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.BANK_MANDATE_NPCI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionId")).isNotEmpty();

        String txnToken = response.jsonPath().getString("body.txnToken").toString();
        String subsId = response.jsonPath().getString("body.subscriptionId").toString();
        WalletHelpers.modifyBalance(user,300.00);

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchantType,txnToken,orderId,subsId)
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchantType.getId())
                .assertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        response = transactionStatusV1API.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("BANK_MANDATE");
        softly.assertThat(response.jsonPath().getString("body.txnInfo.SUBS_ID")).isEqualTo(subsId);
        softly.assertThat(response.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of VPA Validate API with access token & ref id")
    public void validateVPAvalidateAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();
        System.out.println(response);

        ValidateVPADeferredSubs validateVPADeferredSubs = new ValidateVPADeferredSubs(merchantType.getId(), orderId, token);
        response = validateVPADeferredSubs.execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.jsonPath().getString("body.vpa")).isEqualTo("9999661503@paytm");
        Assertions.assertThat(response.jsonPath().getString("body.recurringDetails.pspSupportedRecurring")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("body.recurringDetails.bankSupportedRecurring")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("body.valid")).isEqualTo("true");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of Send OTP API with access token & ref id")
    public void validateSendOTPAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();
        String mobNo = user.mobNo().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        SendOTPDeferredSubs sendOTPDeferredSubs = new SendOTPDeferredSubs(merchantType.getId(), token, mobNo);
        response = sendOTPDeferredSubs.execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Otp sent to phone");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of Validate OTP API with access token & ref id")
    public void validateValidateOTPAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();
        String mobNo = user.mobNo().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        SendOTPDeferredSubs sendOTPDeferredSubs = new SendOTPDeferredSubs(merchantType.getId(), token, mobNo);
        response = sendOTPDeferredSubs.execute();
        System.out.println(response);

        String otp = "123456";
        ValidateOTPDeferredSubs validateOTPDeferredSubs = new ValidateOTPDeferredSubs(merchantType.getId(), token, otp);
        response = validateOTPDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");
        Assertions.assertThat(response.jsonPath().getString("body.authenticated")).isEqualTo("true");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of Fetch Balance API with access token & ref id")
    public void validateFetchBalanceAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.BANK_MANDATE_NPCI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        FetchBalDeferredSubs fetchBalDeferredSubs = new FetchBalDeferredSubs(merchantType.getId(), token);
        response = fetchBalDeferredSubs.execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.balanceInfo.currency")).isEqualTo("INR");
        Assertions.assertThat(response.jsonPath().getString("body.balanceInfo.value")).isNotEmpty();

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of Fetch Bin API with access token & ref id")
    public void validateFetchBinAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.BANK_MANDATE_NPCI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);


        FetchBinDefSubs fetchBinDefSubs = new FetchBinDefSubs(merchantType.getId(), token);
        response = fetchBinDefSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.bin")).isEqualTo("444433");
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.channelName")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.channelCode")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.isEligibleForCoft")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.isCoftPaymentSupported")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.binDetail.issuingBank")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.isSubscriptionAvailable")).isNotEmpty();
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of Fetch NB channels API with access token & ref id")
    public void validateFetchNBChannelsAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.BANK_MANDATE_NPCI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);


        FetchNBChannelsDefSubs fetchNBChannelsDefSubs = new FetchNBChannelsDefSubs(merchantType.getId(), token);
        response = fetchNBChannelsDefSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body")).contains("nbPayOption");
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of Fetch Card Details API with access token & ref id")
    public void validateFetchCardDetailAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.BANK_MANDATE_NPCI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);


        FetchCardDetailsSubsDef fetchCardDetailsSubsDef = new FetchCardDetailsSubsDef(merchantType.getId(), token);
        response = fetchCardDetailsSubsDef.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.cardDetails.isSubscriptionAvailable")).isEqualTo("true");
        Assertions.assertThat(response.jsonPath().getString("body.cardDetails.binDetail.channelName")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.cardDetails.binDetail.channelCode")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.cardDetails.binDetail.isEligibleForCoft")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.cardDetails.binDetail.isCoftPaymentSupported")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.cardDetails.binDetail.issuingBank")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.cardDetails.binDetail.isEligibleForAltId")).isNotEmpty();
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-46055")
    @Test(description = "Verify response of Fetch PSP APPS API with access token & ref id")
    public void validateFetchPSPAPI(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.BANK_MANDATE_NPCI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);


        FetchPSPDefSubs fetchPSPDefSubs = new FetchPSPDefSubs(merchantType.getId(), token);
        response = fetchPSPDefSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCodeId")).isEqualTo("00000000");
        Assertions.assertThat(response.jsonPath().getString("body.pspSchemas[0]")).isNotEmpty();

    }
/*PGP-49226*/
@Owner(Constants.Owner.AKSHAT)
@Feature("PGP-49226")
@Parameters({"isNativePlus"})
@Test(description = "Verify CC/DC paymodes returned in response of Deferred Subscription Flow")
public void validate_CC_DC_returnedInDeferredFlowSubsFPOResponse(@Optional("false") boolean isNativePlus) throws Exception {
    String paymentMode = "";
    String txnAmount = "3.00";

    Constants.MerchantType merchantType = Constants.MerchantType.Deferred_Subs_MID;
    User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
    String sso = user.ssoToken().toString();

    FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
    Response response = fpoDeferredSubs.execute();
    System.out.println(response);

    Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
    Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
    Assertions.assertThat(response.toString().contains("accessTokenValue"));
    Assertions.assertThat(response.jsonPath().getString("body.accessTokenValue")).isNotEmpty();
    Assertions.assertThat(response.toString().contains("subscriptionDetail"));
    Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail")).isNotEmpty();
    Assertions.assertThat(response.jsonPath().getString("body.merchantPayOption.paymentModes.displayName"))
            .contains("Credit Card", "Debit Card");
    Assertions.assertThat(response.jsonPath().getString("body.merchantDetails.isOnus")).isNotEmpty();

}

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-49226")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify CC/DC paymodes not returned in response of Deferred Subscription Flow")
    public void validate_CC_DC_notReturnedInDeferredFlowSubsFPOResponse(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_UPI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.toString().contains("accessTokenValue"));
        Assertions.assertThat(response.jsonPath().getString("body.accessTokenValue")).isNotEmpty();
        Assertions.assertThat(response.toString().contains("subscriptionDetail"));
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.merchantPayOption.paymentModes.displayName"))
                        .doesNotContain("Credit Card", "Debit Card");
        Assertions.assertThat(response.jsonPath().getString("body.merchantDetails.isOnus")).isNotEmpty();

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-54581")
    @Test(description = "Verify Wallet paymodes not returned in response of Deferred Subscription Flow")
    public void validate_Wallet_notReturnedInDeferredFlowSubsFPOResponse() throws Exception {

        // ff4j theia.disable.balance.forSubscriptionEligibility should be ON
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_UPI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.toString().contains("accessTokenValue"));
        Assertions.assertThat(response.jsonPath().getString("body.accessTokenValue")).isNotEmpty();
        Assertions.assertThat(response.toString().contains("subscriptionDetail"));
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Balance", "Paytm Bank Account");
        Assertions.assertThat(response.jsonPath().getString("body.merchantDetails.isOnus")).isNotEmpty();

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-54581")
    @Test(description = "Verify PPBL paymodes not returned in response of Deferred Subscription Flow")
    public void validate_PPBL_notReturnedInDeferredFlowSubsFPOResponse() throws Exception {

        // ff4j theia.disable.ppbl.forSubscriptionEligibility should be ON
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_UPI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.toString().contains("accessTokenValue"));
        Assertions.assertThat(response.jsonPath().getString("body.accessTokenValue")).isNotEmpty();
        Assertions.assertThat(response.toString().contains("subscriptionDetail"));
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail")).isNotEmpty();
        Assertions.assertThat(response.jsonPath().getString("body.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Bank Account");
        Assertions.assertThat(response.jsonPath().getString("body.merchantDetails.isOnus")).isNotEmpty();

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-57608")
    @Test(description = "Verify start date and end date are coming in correct format in resp of fpo of Deferred Subscription Flow")
    public void validateStartEndDateInCorrectFormat() throws Exception
    {
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_UPI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();
        String startDate="2023-07-10";
        String endDate="2026-02-27";

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso,startDate,endDate);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail.startDate").contains("Mon Jul 10 00:00:00 IST 2023"));
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail.endDate").contains("Fri Feb 27 00:00:00 IST 2026"));
    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-57608")
    @Test(description = "Verify start date and end date are not being formatted to correct format in resp of fpo of Deferred Subscription Flow in case start date and end date aren't being sent in correct format")
    public void validateStartEndDateinInorrectFormat() throws Exception
    {
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_UPI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();
        String startDate="09-02-2024";
        String endDate="09-02-2029";

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso,startDate,endDate);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail.startDate").contains(startDate));
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail.endDate").contains(endDate));
    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-57608")
    @Test(description = "Verify start date and end date are not being formatted to correct format in resp of fpo of Deferred Subscription Flow in case start date and end date aren't being sent in correct format")
    public void validateStartEndDateinInorrectFormat_2() throws Exception
    {
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_UPI;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();
        String startDate="00:00:00 IST 2024 Mon Aug 26";
        String endDate="00:00:00 IST Tue Aug 22 2034";

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso,startDate,endDate);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail.startDate").contains(startDate));
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail.endDate").contains(endDate));
    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-57608")
    @Test(description = "Verify subscription details aren't coming in deferred fpo if DEFFERED_FLOW_ON_SUBSCRIPTION_ENABLED is N")
    public void validateSubsDetailsNotComing_PrefN() throws Exception
    {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_CC_DC;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();
        String startDate="2023-07-10";
        String endDate="2026-02-27";

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso,startDate,endDate);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().getString("body.subscriptionDetail")).isNull();
    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58496")
    @Test(description = "Verify that expiry time is passed in create_order request for deferred subscription")
    public void validateExpiryTime_deferredFlow(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.P4B_NOTIFICATION_MID;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"timeoutConfig\":[{\"timeoutType\":\"EXPIRY_TIMEOUT\",\"disabled\":false,\"timeoutInSeconds\":\"900\"}]}");

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58835")
    @Test(description = "Verify order is created on Product Code 51051000100000000004 for PCF & Subs enabled MID")
    public void validateDeferredOrderCreateon_51051000100000000004(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "";
        String txnAmount = "3.00";

        Constants.MerchantType merchantType = Constants.MerchantType.Subs_PCF_fix;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String sso = user.ssoToken().toString();

        FPODeferredSubs fpoDeferredSubs = new FPODeferredSubs(merchantType.getId(), sso);
        Response response = fpoDeferredSubs.execute();
        System.out.println(response);
        String token = response.jsonPath().getString("body.accessTokenValue").toString();

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = RandomStringUtils.randomNumeric(10);

        InitiateSubsDeferred initiateSubsDeferred = new InitiateSubsDeferred(merchantType.getId(), orderId, token, SubscriptionStartDate, sso);
        response = initiateSubsDeferred.execute();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000004\"");

    }


}
