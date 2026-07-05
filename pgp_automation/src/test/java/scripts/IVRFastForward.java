package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.FastForward;
import com.paytm.api.FetchBalance;
import com.paytm.api.notification.LoyalityPointsNotify;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.FastForwardRequestDTO;
import com.paytm.dto.FastForwardResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.DecimalFormat;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner("Tarun")
public class IVRFastForward extends PGPBaseTest {

    private final RiskRejectHelper riskRejectHelper = new RiskRejectHelper();
    String PreExpiry_Message = "Hurry! 30 Paytm Points will be expiring this month. Redeem your points by May 30, 2021.";
    String PostExpiry_Message = "30 Paytm Points expired on May 30, 2021. Updated balance: 1200.";

    @BeforeTest
    public void disableScreenShotCapture() {
        DriverManager.setCaptureScreenShot(false);
    }

    @Test(description = "Validate successful IVR transaction.", groups = {"smoke", "regression"})
    public void PGP_236_successfulIVRTxn() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WALLETOnly_PCF_PG2_RTDD, user)
                .addChecksumToRequest(MerchantType.WALLETOnly_PCF_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.valueOf(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate successful Auto Debit by PCF merchant IVR transaction. ")
    public void testAutoDebitTxnByPCFMerchant() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WALLETOnly_PCF_PG2_RTDD, user)
                .addChecksumToRequest(MerchantType.WALLETOnly_PCF_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.valueOf(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Verify <whetherBuyerUserIdChange> flag is passed in COP request incase of autodebit transaction")
    public void validateWhetherBuyerUserIdChangeFlagisFalseinAutoDebitTxn() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WALLETOnly_PCF_PG2_RTDD, user)
                .addChecksumToRequest(MerchantType.WALLETOnly_PCF_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.valueOf(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
        String grepcmd = "grep \"" + ivrRequest.getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + ivrRequest.getMId() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\"whetherBuyerUserIdChange\":\"FALSE\"");

    }

    @Test(description = "Validate risk reject txn with IVR fast forward")
    public void testRiskRejectIVRFF() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WALLETOnly_PCF, user)
                .setTxnAmount("1.88")
                .addChecksumToRequest(MerchantType.WALLETOnly_PCF.getKey());
        WalletHelpers.modifyBalance(user, Double.valueOf(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateRiskReject(ivrResponse);
    }


    @Test(description = "Validate Unsuccessful IVR Txn with insufficient wallet balance", groups = {"regression"})
    public void PGP_437_IVR_UnsuccessfulTxnInsuficientWalletBalance() throws Exception {
        SoftAssertions soft = new SoftAssertions();
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());;
        WalletHelpers.modifyBalance(user, Double.valueOf("0"));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        soft.assertThat(ivrResponse.getResponseMessage()).isEqualTo("Wallet balance Insufficient");
        soft.assertThat(ivrResponse.getResponseCode()).isEqualTo("235");
        soft.assertThat(ivrResponse.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertAll();
    }

    @Test(description = "Validate Unsuccessful IVR Txn with invalid SsoToken", groups = {"regression"})
    public void PGP_438_UnsuccessfulTxnInvalidToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user).setSSOToken("12345")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateInvalidDataInRequestIVRTxn(ivrRequest, ivrResponse);
    }


    @Test(description = "Validate Unsuccessful IVR Txn with expired SsoToken", groups = {"regression"})
    public void PGP_439_UnsuccessfulTxnExpiredToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String activeSSOToken = user.ssoToken();
        try {
            AuthHelpers.logout(activeSSOToken);
        }finally {
            user.purge();
        }
        String expiredSSOToken = activeSSOToken;
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setSSOToken(expiredSSOToken)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());;
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateInvalidDataInRequestIVRTxn(ivrRequest, ivrResponse);
    }


    @Test(description = "Validate Unsuccessful IVR Txn for invalid MID", groups = {"regression"})
    public void PGP_441_UnsuccessfulTxnInvalidMid() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setMId("abdABc")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateBadRequestIVRTxn(ivrResponse);
    }

    @Test(description = "Validate Unsuccessful IVR Txn Non migrated MID", groups = {"regression"})
    public void PGP_442_UnsuccessfulTxnNonMigratedMid() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.NonMigrated, user)
                .addChecksumToRequest(MerchantType.NonMigrated.getKey());
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateBadRequestIVRTxn(ivrResponse);
    }

    @Test(description = "Validate Successful IVR Txn when AppID param not send", groups = {"regression"})
    public void PGP_443_SuccessfulTxnAppIDNotSend() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setAppIP(null)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.valueOf(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate Unsuccessful IVR Txn when Txn Amount 0", groups = {"regression"})
    public void PGP_444_UnsuccessfulTxnTxnAmount_0() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setTxnAmount("0")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        Assertions.assertThat(ivrResponse.getResponseCode().equals("501"));
        Assertions.assertThat(ivrResponse.getResponseMessage()).isEqualTo("System Error");
    }

    @Test(description = "Validate Unsuccessful Txn with Alphanumric Txn Amount '0AB'", groups = {"regression"})
    public void PGP_445_UnsuccessfulTxnAlphanumricAmount() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setTxnAmount("0AB")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateBadRequestIVRTxn(ivrResponse);
    }

    @Test(description = "Validate Unsuccessful Txn with Negative Txn Amount '-10'", groups = {"regression"})
    public void PGP_446_UnsuccessfulTxnNegativeAmount() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setTxnAmount("-10")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateBadRequestIVRTxn(ivrResponse);
    }

    @Test(description = "Validate Successful Txn with Invalid Currency type 'RR'", groups = {"regression"})
    public void PGP_447_SuccessfulTxnInvalidCurrency() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setCurrency("RR")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate Successful Txn with Invalid Currency type 'blank'", groups = {"regression"})
    public void PGP_448_SuccessfulTxnBlankCurrencyType() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setCurrency("")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate Successful Txn when Currency type not passed in request", groups = {"regression"})
    public void PGP_449_SuccessfulTxnCurrencyNotPassed() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setCurrency(null)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate Successful Txn when Device Id parameter not passed", groups = {"regression"})
    public void PGP_450_SuccessfulTxnDeviceIdNotPassed() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setDeviceId(null)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate Success Txn with paytm scope token", groups = {"regression"})
    public void PGP_451_SuccessfulTxnPaytmToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setSSOToken(user.paytmToken())
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }



    @Test(description = "Validate Txn Failure with invalid paymode", groups = {"regression"})
    public void PGP_453_UnsuccessfulTxnInvalidPaymode() throws Exception {
        SoftAssertions soft = new SoftAssertions();
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setPaymentMode("WALLET")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        soft.assertThat(ivrResponse.getResponseMessage()).isEqualTo("System Error");
        soft.assertThat(ivrResponse.getResponseCode()).isEqualTo("501");
        soft.assertThat(ivrResponse.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertThat(ivrResponse.getMerchantId()).isEqualTo(ivrRequest.getMId());
        soft.assertThat(ivrResponse.getPaymentMode()).isEqualTo(ivrRequest.getPaymentMode());
        soft.assertAll();
    }


    @Test(description = "Validate Txn Failure when cust id is not passed in request", groups = {"regression"})
    public void PGP_455_UnsuccessfulTxnWithoutCustID() throws Exception {
        SoftAssertions soft = new SoftAssertions();
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setCustomerId(null)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        soft.assertThat(ivrResponse.getResponseMessage()).isEqualTo("Invalid CustID");
        soft.assertThat(ivrResponse.getResponseCode()).isEqualTo("318");
        soft.assertThat(ivrResponse.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertAll();
    }


    @Test(description = "Validate Txn Failure with blank cust id", groups = {"regression"})
    public void PGP_456_UnsuccessfulTxnWithBlankCustID() throws Exception {
        SoftAssertions soft = new SoftAssertions();
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setCustomerId("")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        soft.assertThat(ivrResponse.getResponseMessage()).isEqualTo("Invalid CustID");
        soft.assertThat(ivrResponse.getResponseCode()).isEqualTo("318");
        soft.assertThat(ivrResponse.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertAll();
    }


//    @Test(description = "Validate Txn Failure when industry type is not passed", groups = {"regression"}, enabled = false)
    public void PGP_457_UnsuccessfulTxnInvalidIndustryType() throws Exception {//Test case is obsolete, functionality changed in 22.2.0 release
        SoftAssertions soft = new SoftAssertions();
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly, user)
                .setIndustryType(null)
                .addChecksumToRequest(MerchantType.WalletOnly.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest,ivrResponse);
    }

    @Test(description = "Validate Success Txn with channel type = WAP", groups = {"regression"})
    public void PGP_458_SuccessfulTxnChannelWAP() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setChannel("WAP")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate success Txn for Invalid oauth mode", groups = {"regression"})
    public void PGP_459_SuccessfulTxnInvalidAuthMode() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .setAuthMode("USR")
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Test(description = "Validate 'Merchant unique reference not found' in response when not passed in request", groups = {"regression"})
    public void PGP_460_SuccessfulTxnMerchantUniqueNotPassed() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
    }

    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-20043")
    @Owner("Tarun")
    @Test(description = "Validate response message for Auto Debit risk reject", groups = {"regression"})
    public void riskRejectAutoDebit() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly, user)
                .setMercUnqRef("checkMerchantUniq")
                .addChecksumToRequest(MerchantType.WalletOnly.getKey());
        WalletHelpers.modifyBalance(user, Double.parseDouble(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateRiskReject(ivrResponse);
    }

    //Fast Forward Risk Reject Txn
    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-20043")
    @Owner("Tarun")
    @Parameters({"theme"})
    @Test(description= "Validate response message for risk reject fast forward txn")
    public void riskRejectFastForward(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = MerchantType.WalletOnly_PG2_RTDD;
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 2.0);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType,theme,user)
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .build();

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAdditionalInfoMCC("mc1234")
                .setOrderAdditionalInfoMName("automation")
                .setOrderAdditionalInfoMLogo("logo")
                .build();

        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo(riskRejectHelper.riskRejectRespCode);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo(riskRejectHelper.riskRejectRespMsg);
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualTo("PPI");

    }

//--------------Customer Loyality Points-------------------------

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate fetch Balance call With Payment Mode LOYALTY_POINT with Valid SSO Token")
    public void successfulFetchBalanceLP() throws Exception {
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        LoyalityPointsHelper.updateBalance(user,100);
        FetchBalance fetchBalance  = new FetchBalance(loyaltyMerchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"LOYALTY_POINT");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("Fetch Balance API failed").isEqualTo("Success");
        Double availablePoints = fetchBalanceResponse.getDouble("body.availablePoints");
        Double exchangeRate = fetchBalanceResponse.getDouble("body.exchangeRate");
        Assertions.assertThat(fetchBalanceResponse.getDouble("body.balanceInfo.value")).as("Balance is getting calculated incorrectly").isEqualTo(availablePoints/exchangeRate);
    }
    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate fetch Balance call With Payment Mode LOYALTY_POINT with Valid SSO Token but user don't have Loyalty Point")
    public void fetchBalanceOnUserNotHavingLP() throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        FetchBalance fetchBalance  = new FetchBalance(loyaltyMerchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"LOYALTY_POINT");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid SSO token").isEqualTo("We are unable to fetch your balance, kindly try after sometime");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid SSO token").isEqualTo("3005");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid SSO token").isEqualTo("F");
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate fetch Balance call With Payment Mode LOYALTY_POINT with invalid SSO Token")
    public void fetchBalanceOnInvalidSSOTokenLP() throws Exception {
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        FetchBalance fetchBalance  = new FetchBalance(loyaltyMerchant.getId(),CommonHelpers.generateOrderId(),"Invalid_Token","LOYALTY_POINT");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid SSO token").isEqualTo("SSO Token is invalid");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid SSO token").isEqualTo("2004");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid SSO token").isEqualTo("F");
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate fetch Balance when MID don't have LOYALTY_POINT as Payment mode but user have Loyalty Point")
    public void fetchBalanceOnMidNotHavingLP() throws Exception {
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.PGOnly_PG2_RTDD;
        FetchBalance fetchBalance  = new FetchBalance(loyaltyMerchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"LOYALTY_POINT");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for required combination").isEqualTo("Operation is not supported");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for required combination").isEqualTo("2012");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for required combination").isEqualTo("F");
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate fetch Balance With Invalid Payment Mode for LOYALTY_POINT merchant with Valid SSO Token")
    public void fetchBalanceWithInvalidPayModeLP() throws Exception {
        User user = userManager.getForWrite(Label.LOYALTY);
        LoyalityPointsHelper.updateBalance(user,100);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        FetchBalance fetchBalance  = new FetchBalance(loyaltyMerchant.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"DEBIT_CARD"); //DC is not configured on merchant, will be considered as invalid payMode
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("Request parameters are not valid");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("1001");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("F");
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Fast Forward Transaction with PaymentMode LOYALTY_POINT and with valid ssoToken and exchange Rate")
    public void successFFTxnWithPayPaymodeLP() throws Exception {
        int txnAmount = 2;
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
       // LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Fast Forward Transaction with PaymentMode LOYALTY_POINT and with invalid ssoToken and exchange Rate")
    public void invalidSSOtokenFastForwardLP() throws Exception {
        int txnAmount = 2;
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        User user = userManager.getForWrite(Label.LOYALTY);
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);

        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .setSSOToken("Invalid_Token")
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSystemErrorFailureLP(ivrRequest,ivrResponse);

    }

    @Issue("PGP-24227")
    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Fast Forward Transaction with PaymentMode LOYALTY_POINT and with valid ssoToken and invalid exchange Rate",groups = Group.Status.BUG)
    public void validSSOInvalidExchangeLP() throws Exception {
        int txnAmount = 2;
        User user = userManager.getForWrite(Label.LOYALTY);
        LoyalityPointsHelper.updateBalance(user,txnAmount*100);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS;
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate("0")//Invalid Exchange Rate
                .addChecksumToRequest(loyaltyMerchant.getKey());

        WalletHelpers.modifyBalance(user, Double.valueOf(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validatePaymentDeclinedFailureLP(ivrRequest,ivrResponse);
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Fast Forward Transaction with PaymentMode LOYALTY_POINT and transaction amount is greater than user Loyalty Point account balance")
    public void txnAmountGreaterThanLP() throws Exception {
        int txnAmount = 2;
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,(txnAmount-1)*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Fast Forward Transaction with PaymentMode LOYALTY_POINT and with valid ssoToken and exchange Rate but don't have LoyaltyPoint as payment Mode")
    public void payModeNotThereOnMerchantLP() throws Exception {
        int txnAmount = 2;

        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.PGOnly_PG2_RTDD;
        int exchangeRate = 10;
        LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSystemErrorFailureLP(ivrRequest,ivrResponse);

    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Fast Forward Transaction with PaymentMode LOYALTY_POINT and with valid ssoToken and exchange Rate But user don't have loyalty point account")
    public void payModeNotThereOnUserLP() throws Exception {
        int txnAmount = 2;

        User user = userManager.getForWrite(Label.PPBL);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = 10;
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSystemErrorPendingLP(ivrRequest,ivrResponse);

    }

    @Issue("PGP-24227")
    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Fast Forward Transaction with PaymentMode LOYALTY_POINT and with exchange rate is negative number",groups = Group.Status.BUG)
    public void ffExchangeNumberNegativeLP() throws Exception {
        int txnAmount = 2;
        int exchangeRate = -5;
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS;
        LoyalityPointsHelper.updateBalance(user,txnAmount);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validatePaymentDeclinedFailureLP(ivrRequest,ivrResponse);

    }

    @Issue("PGP-24227")
    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Fast Forward Transaction with PaymentMode LOYALTY_POINT and with exchange rate is having alphanumeric characters",groups = Group.Status.BUG)
    public void exchangeNumberAlphaNumericLP() throws Exception {
        int txnAmount = 2;
        String exchangeRate = "12ABC";
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS;
        LoyalityPointsHelper.updateBalance(user,txnAmount);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(exchangeRate)
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validatePaymentDeclinedFailureLP(ivrRequest,ivrResponse);

    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate fast Forward Transaction with PaymentMode LOYALTY_POINT with duplicate orderId")
    public void duplicateOrderIdLP() throws Exception {
        int txnAmount = 2;

        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

        FastForwardRequestDTO duplicateOrderIdRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setOrderId(ivrRequest.getOrderId())
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward newFastforward = new FastForward(duplicateOrderIdRequest);
        FastForwardResponseDTO newResponse = newFastforward.execute().as(FastForwardResponseDTO.class);
        validateDuplicateOrderId(duplicateOrderIdRequest,newResponse);
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate refund for loyality point with full amount")
    public void refundFullAmountLP() throws Exception {
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        DecimalFormat formatter = new DecimalFormat("0.00");
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(loyaltyMerchant.getId());
        }

        int txnAmount = 2;

        User user = userManager.getForWrite(Label.LOYALTY);
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(formatter.format(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

        PGPHelpers.initiateRefundRequest(loyaltyMerchant.getId(), loyaltyMerchant.getKey(), ivrRequest.getOrderId(), ivrRequest.getOrderId(), ivrRequest.getTxnAmount(), ivrResponse.getTxnId(), "");
        PGPHelpers.getRefundStatus(loyaltyMerchant.getId(), loyaltyMerchant.getKey(), ivrRequest.getOrderId(), true)
                .validateSuccessRefund()
                .validatePAYMENTMODE("LOYALTY_POINT",0)
                .validateREFUNDAMOUNT(ivrRequest.getTxnAmount(),0)
                .validateTOTALREFUNDAMT(ivrRequest.getTxnAmount(),0)
                .validateMID(ivrRequest.getMId(),0)
                .assertAll();
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate refund for loyality point with partial amount")
    public void refundPartialAmountLP() throws Exception {
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int txnAmount = 4;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(loyaltyMerchant.getId());
        }

        DecimalFormat formatter = new DecimalFormat("0.00");
        User user = userManager.getForWrite(Label.LOYALTY);
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(formatter.format(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

        String partialRefundAmount = "2.00";
        PGPHelpers.initiateRefundRequest(loyaltyMerchant.getId(), loyaltyMerchant.getKey(), ivrRequest.getOrderId(), ivrRequest.getOrderId(), partialRefundAmount, ivrResponse.getTxnId(), "");
        PGPHelpers.getRefundStatus(loyaltyMerchant.getId(), loyaltyMerchant.getKey(), ivrRequest.getOrderId(), true)
                .validateSuccessRefund()
                .validatePAYMENTMODE("LOYALTY_POINT",0)
                .validateREFUNDAMOUNT(partialRefundAmount,0)
                .validateTOTALREFUNDAMT(partialRefundAmount,0)
                .validateMID(ivrRequest.getMId(),0)
                .assertAll();
    }
    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19455")
    @Test(description = "Validate Unsuccessful IVR Txn with expired SsoToken with payMode Loyalty_point")
    public void expiredSSOTokenLP() throws Exception {
        int txnAmount = 2;

        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        String activeSSOToken = user.ssoToken();
        try {
            AuthHelpers.logout(activeSSOToken);
        }finally {
            user.purge();
        }
        String expiredSSOToken = activeSSOToken;

        LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setSSOToken(expiredSSOToken)
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSystemErrorFailureLP(ivrRequest,ivrResponse);

    }

    @Owner("Tarun")
    @Feature("PGP-13514")
    @Description("Automation JIRA : PGP-26961")
    @Test(description = "Validate PEON for successful AUTO DEBIT transaction with PPI")
    public void validateSuccessPeonPPI() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(MerchantType.WalletOnly_PG2_RTDD, user)
                .addChecksumToRequest(MerchantType.WalletOnly_PG2_RTDD.getKey());
        WalletHelpers.modifyBalance(user, Double.valueOf(ivrRequest.getTxnAmount()));
        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxn(ivrRequest, ivrResponse);
        validateSuccessPeonTxn(ivrRequest);

    }

    @Owner("Tarun")
    @Feature("PGP-13514")
    @Issue("PGP-27270")
    @Description("Automation JIRA : PGP-26961")
    @Test(description = "Validate PEON for success AUTO DEBIT transaction with Loyalty Point",groups = Group.Status.BUG)
    public void validateSuccessPeonLP() throws Exception {
        int txnAmount = 2;
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,(txnAmount-1)*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);
        Peons peons = new Peons();
        Peon peon = peons.getAt(ivrRequest.getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE","LOYALTYPOINTS", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(ivrRequest.getMId()),
                peon.orderId().equals(ivrRequest.getOrderId()),
                peon.payMode().equals(ivrRequest.getPaymentMode()),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(ivrRequest.getTxnAmount()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();


    }


    //--------------Merchant Loyality Points-------------------------

    @Owner("Tarun")
    @Description("Loyality Points Phase 2")
    @Feature("PGP-23578")
    @Test(description = "Verify that new parameters are coming in request for CLW_APP_PAY and request is getting accepted and getting success")
    public void newParamMerchantMLP() throws Exception {
        int txnAmount = 2;

        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,(txnAmount-1)*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .setLoyltyPointRootUserId(user.custId())//new params
                .setRootUserPGMid("SCWREP12963320539686")//new params
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

    }


    @Owner("Tarun")
    @Description("Loyality Points Phase 2")
    @Feature("PGP-23578")
    @Test(description = "Verify that if new params are coming as null in request for CLW_APP_PAY , then old flow should work")
    public void oldFlowMerchantIfNewParamNullLP() throws Exception {
        int txnAmount = 2;
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,(txnAmount-1)*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .setLoyltyPointRootUserId("")//new params
                .setRootUserPGMid("")//new params
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

    }

    @Owner("Tarun")
    @Description("Loyality Points Phase 2")
    @Feature("PGP-23578")
    @Test(description = "Verify if merchant which is not present in property(theia-biz) file then request is coming with new parameter then exception is coming")
    public void midNotListedInPropFFLP() throws Exception {
        int txnAmount = 2;
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.PGOnly_PG2_RTDD;
        int exchangeRate = 10;
        LoyalityPointsHelper.updateBalance(user,(txnAmount-1)*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .setLoyltyPointRootUserId(user.custId())//new params
                .setRootUserPGMid("SCWREP12963320539686")//new params
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSystemErrorPendingLP(ivrRequest,ivrResponse);

    }

    @Owner("Tarun")
    @Description("Loyality Points Phase 2")
    @Feature("PGP-23578")
    @Test(description = "Verify that when invalid rootCustId is coming in the request")
    public void invalidRootCustId() throws Exception {
        int txnAmount = 2;
        User user = userManager.getForWrite(Label.LOYALTY);
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,1);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .setLoyltyPointRootUserId("123123")//Invalid
                .setRootUserPGMid("SCWREP12963320539686")//new params
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSystemErrorPendingLP(ivrRequest,ivrResponse);

    }

    @Owner("Tarun")
    @Description("Loyality Points Phase 2")
    @Feature("PGP-23578")
    @Test(description = "Validate refund for loyality point with full amount for merchant's Loyalty Point")
    public void refundFullAmountMLP() throws Exception {
        MerchantType loyaltyMerchant = MerchantType.LOYALTY_POINTS_PG2_RTDD;
        DecimalFormat formatter = new DecimalFormat("0.00");
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(loyaltyMerchant.getId());
        }

        int txnAmount = 2;

        User user = userManager.getForWrite(Label.LOYALTY);
        int exchangeRate = getExchangeRate(loyaltyMerchant,user);
        LoyalityPointsHelper.updateBalance(user,txnAmount*exchangeRate);
        FastForwardRequestDTO ivrRequest = new FastForwardRequestDTO(loyaltyMerchant, user)
                .setTxnAmount(formatter.format(txnAmount))
                .setPaymentMode("LOYALTY_POINT")
                .setExchangeRate(String.valueOf(exchangeRate))
                .setLoyltyPointRootUserId(user.custId())//new params
                .setRootUserPGMid("SCWREP12963320539686")//new params
                .addChecksumToRequest(loyaltyMerchant.getKey());

        FastForward fastforward = new FastForward(ivrRequest);
        FastForwardResponseDTO ivrResponse = fastforward.execute().as(FastForwardResponseDTO.class);
        validateSuccessIVRTxnLP(ivrRequest,ivrResponse);

        PGPHelpers.initiateRefundRequest(loyaltyMerchant.getId(), loyaltyMerchant.getKey(), ivrRequest.getOrderId(), ivrRequest.getOrderId(), ivrRequest.getTxnAmount(), ivrResponse.getTxnId(), "");
        PGPHelpers.getRefundStatus(loyaltyMerchant.getId(), loyaltyMerchant.getKey(), ivrRequest.getOrderId(), true)
                .validateSuccessRefund()
                .validatePAYMENTMODE("LOYALTY_POINT",0)
                .validateREFUNDAMOUNT(ivrRequest.getTxnAmount(),0)
                .validateTOTALREFUNDAMT(ivrRequest.getTxnAmount(),0)
                .validateMID(ivrRequest.getMId(),0)
                .assertAll();
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Feature("PGP-32472")
    @Test(description = "Verify sending PRE_EXPIRY Notification for 1 user")
    public void verifyLoyalityPointExpiry_Notification_ForPRE_Expiry() throws Exception {

        User user = userManager.getForWrite(Label.LOYALTY);
        LoyalityPointsNotify loyalityPointsNotify = (LoyalityPointsNotify) new LoyalityPointsNotify()
                .setContext("request.body.notificationDataList[1].userId",user.custId());
        JsonPath withDrawJson = loyalityPointsNotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        String grepcmd = "grep '\"templateName\":\"PG_push_for_paytm_app\"' /paytm/logs/communicationGateway.log";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commLogs).contains(PreExpiry_Message);
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Feature("PGP-32472")
    @Test(description = "Verify sending POST_EXPIRY Notification for 1 user")
    public void verifyLoyalityPointExpiry_Notification_ForPOST_Expiry() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        String category = "POST_EXPIRY";
        LoyalityPointsNotify loyalityPointsNotify = (LoyalityPointsNotify) new LoyalityPointsNotify()
                .setContext("request.body.notificationDataList[0].category",category)
                .setContext("request.body.notificationDataList[1].userId",user.custId());
        JsonPath withDrawJson = loyalityPointsNotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        String grepcmd = "grep '\"templateName\":\"PG_push_for_paytm_app\"' /paytm/logs/communicationGateway.log";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commLogs).contains(PostExpiry_Message);
    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Feature("PGP-32472")
    @Test(description = "Verify sending PRE_EXPIRY Notification for more than 1 user")
    public void verifyLoyalityPointExpiry_Notification_ForPreExpiry_MoreThan_OneUser() throws Exception {

        String notificationDetails = "{\n" +
                "            \"notificationDataList\": [\n" +
                "                {\n" +
                "                    \"category\": \"PRE_EXPIRY\",\n" +
                "                    \"extendInfo\": \"\",\n" +
                "                    \"notificationPlaceholders\": {\n" +
                "                        \"expiryPoints\": \"30\",\n" +
                "                        \"expiryDate\": \"Sun May 30 23:59:59 CST 2021\",\n" +
                "                        \"availableBalancePoints\":\"1200\"\n" +
                "                    },\n" +
                "                    \"userId\": \"1000703898\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"category\": \"PRE_EXPIRY\",\n" +
                "                    \"extendInfo\": \"\",\n" +
                "                    \"notificationPlaceholders\": {\n" +
                "                        \"expiryPoints\": \"30\",\n" +
                "                        \"expiryDate\": \"Sun May 30 23:59:59 CST 2021\",\n" +
                "                        \"availableBalancePoints\":\"1200\"\n" +
                "                    },\n" +
                "                    \"userId\": \"1000703897\"\n" +
                "                }\n" +
                "                \n" +
                "            ]\n" +
                "        }";

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(notificationDetails);
        LoyalityPointsNotify loyalityPointsNotify = (LoyalityPointsNotify) new LoyalityPointsNotify()
                .setContext("request.body",json);

        JsonPath withDrawJson = loyalityPointsNotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        String grepcmd = "grep '\"templateName\":\"PG_push_for_paytm_app\"' /paytm/logs/communicationGateway.log";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commLogs).contains(PreExpiry_Message);
        Assertions.assertThat(commLogs).contains("\"notificationReceiverIdentifier\":[\"1000703898\"]");
        Assertions.assertThat(commLogs).contains("\"notificationReceiverIdentifier\":[\"1000703897\"]");

    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Feature("PGP-32472")
    @Test(description = "Verify sending POST_EXPIRY Notification for more than 1 user")
    public void verifyLoyalityPointExpiry_Notification_ForPostExpiry_MoreThan_OneUser() throws Exception {

        String notificationDetails = "{\n" +
                "            \"notificationDataList\": [\n" +
                "                {\n" +
                "                    \"category\": \"POST_EXPIRY\",\n" +
                "                    \"extendInfo\": \"\",\n" +
                "                    \"notificationPlaceholders\": {\n" +
                "                        \"expiryPoints\": \"30\",\n" +
                "                        \"expiryDate\": \"Sun May 30 23:59:59 CST 2021\",\n" +
                "                        \"availableBalancePoints\":\"1200\"\n" +
                "                    },\n" +
                "                    \"userId\": \"1000703778\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"category\": \"POST_EXPIRY\",\n" +
                "                    \"extendInfo\": \"\",\n" +
                "                    \"notificationPlaceholders\": {\n" +
                "                        \"expiryPoints\": \"30\",\n" +
                "                        \"expiryDate\": \"Sun May 30 23:59:59 CST 2021\",\n" +
                "                        \"availableBalancePoints\":\"1200\"\n" +
                "                    },\n" +
                "                    \"userId\": \"1000703328\"\n" +
                "                }\n" +
                "                \n" +
                "            ]\n" +
                "        }";

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(notificationDetails);
        LoyalityPointsNotify loyalityPointsNotify = (LoyalityPointsNotify) new LoyalityPointsNotify()
                .setContext("request.body",json);
        JsonPath withDrawJson = loyalityPointsNotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        String grepcmd = "grep '\"templateName\":\"PG_push_for_paytm_app\"' /paytm/logs/communicationGateway.log";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commLogs).contains(PostExpiry_Message);
        Assertions.assertThat(commLogs).contains("\"notificationReceiverIdentifier\":[\"1000703778\"]");
        Assertions.assertThat(commLogs).contains("\"notificationReceiverIdentifier\":[\"1000703328\"]");

    }

    @Owner(Constants.Owner.PRIYANSHI)
    @Feature("PGP-32472")
    @Test(description = "Verify sending PRE_EXPIRY and POST_EXPIRY for different users")
    public void verifyLoyalityPointExpiry_Notification_ForPreExpiry_AND_ForPostExpiry() throws Exception {

        String notificationDetails = "{\n" +
                "            \"notificationDataList\": [\n" +
                "                {\n" +
                "                    \"category\": \"PRE_EXPIRY\",\n" +
                "                    \"extendInfo\": \"\",\n" +
                "                    \"notificationPlaceholders\": {\n" +
                "                        \"expiryPoints\": \"30\",\n" +
                "                        \"expiryDate\": \"Sun May 30 23:59:59 CST 2021\",\n" +
                "                        \"availableBalancePoints\":\"1200\"\n" +
                "                    },\n" +
                "                    \"userId\": \"1000723993\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"category\": \"POST_EXPIRY\",\n" +
                "                    \"extendInfo\": \"\",\n" +
                "                    \"notificationPlaceholders\": {\n" +
                "                        \"expiryPoints\": \"30\",\n" +
                "                        \"expiryDate\": \"Sun May 30 23:59:59 CST 2021\",\n" +
                "                        \"availableBalancePoints\":\"1200\"\n" +
                "                    },\n" +
                "                    \"userId\": \"1000713604\"\n" +
                "                }\n" +
                "                \n" +
                "            ]\n" +
                "        }";

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(notificationDetails);
        LoyalityPointsNotify loyalityPointsNotify = (LoyalityPointsNotify) new LoyalityPointsNotify()
                .setContext("request.body",json);
        JsonPath withDrawJson = loyalityPointsNotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        String grepcmd = "grep '\"templateName\":\"PG_push_for_paytm_app\"' /paytm/logs/communicationGateway.log";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commLogs).contains(PreExpiry_Message);
        Assertions.assertThat(commLogs).contains(PostExpiry_Message);
        Assertions.assertThat(commLogs).contains("\"notificationReceiverIdentifier\":[\"1000723993\"]");
        Assertions.assertThat(commLogs).contains("\"notificationReceiverIdentifier\":[\"1000713604\"]");

    }



    //--------get Exchange Rate ----------//
    public int getExchangeRate(MerchantType merchantType, User user)
    {
        FetchBalance fetchBalance  = new FetchBalance(merchantType.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"LOYALTY_POINT");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("Fetch Balance API failed").isEqualTo("Success");
        int exchangeRate = fetchBalanceResponse.getInt("body.exchangeRate");
        return exchangeRate;
    }


//-----------------Validations Wrappers-------------------------------


    public void validateSuccessPeonTxn(FastForwardRequestDTO ivrRequest)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(ivrRequest.getOrderId());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(ivrRequest.getMId()),
                peon.orderId().equals(ivrRequest.getOrderId()),
                peon.payMode().equals(ivrRequest.getPaymentMode()),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(ivrRequest.getTxnAmount()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();

    }


    public void validateSuccessIVRTxn(FastForwardRequestDTO request, FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getTxnId()).isNotEmpty();
        soft.assertThat(response.getMerchantId()).isEqualTo(request.getMId());
        soft.assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        soft.assertThat(response.getTxnAmount()).isEqualTo(request.getTxnAmount());
        soft.assertThat(response.getResponseCode()).isEqualTo("01");
        soft.assertThat(response.getResponseMessage()).isEqualTo("Txn Successful.");
        soft.assertThat(response.getStatus()).isEqualTo("TXN_SUCCESS");
        soft.assertThat(response.getPaymentMode()).isEqualTo("PPI");
        soft.assertThat(response.getBankName()).isEqualTo("WALLET");
        soft.assertThat(response.getCustId()).isEqualTo(request.getCustomerId());
        soft.assertThat((request.getMercUnqRef())).isEqualTo(response.getMercUnqRef());
        soft.assertThat(response.getBankTxnId()).isNotEmpty();
        soft.assertAll();

    }

    public void validateRiskReject(FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getResponseCode()).isEqualTo("501");
        soft.assertThat(response.getResponseMessage()).isEqualTo("global default message");
        soft.assertThat(response.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertThat(response.getPaymentMode()).isEqualTo("PPI");
        soft.assertThat(response.getBankName()).isEqualTo("WALLET");
        soft.assertAll();

    }

    public void validateBadRequestIVRTxn(FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getTxnId()).isNull();
        soft.assertThat(response.getMerchantId()).isNull();
        soft.assertThat(response.getOrderId()).isNull();
        soft.assertThat(response.getTxnAmount()).isNull();
        soft.assertThat(response.getResponseCode()).isEqualTo("501");
        soft.assertThat(response.getResponseMessage()).isEqualTo("System Error");
        soft.assertThat(response.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertThat(response.getPaymentMode()).isNull();
        soft.assertThat(response.getBankName()).isNull();
        soft.assertThat(response.getCustId()).isNull();
        soft.assertThat(response.getMercUnqRef()).isNull();
        soft.assertAll();

    }

    public void validateInvalidDataInRequestIVRTxn(FastForwardRequestDTO request, FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getTxnId()).isNull();
        soft.assertThat(response.getMerchantId()).isEqualTo(request.getMId());
        soft.assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        soft.assertThat(response.getTxnAmount()).isEqualTo(request.getTxnAmount());
        soft.assertThat(response.getResponseCode()).isEqualTo("501");
        soft.assertThat(response.getResponseMessage().equals("System Error"));
        soft.assertThat(response.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertThat(response.getPaymentMode()).isEqualTo(request.getPaymentMode());
        soft.assertThat(response.getBankName()).isEqualTo("WALLET");
        soft.assertThat(response.getCustId()).isEqualTo(request.getCustomerId());
        soft.assertThat(request.getMercUnqRef()).isEqualTo(response.getMercUnqRef());
        soft.assertAll();

    }

    public void validateSuccessIVRTxnLP(FastForwardRequestDTO request, FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getTxnId()).isNotEmpty();
        soft.assertThat(response.getMerchantId()).isEqualTo(request.getMId());
        soft.assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        soft.assertThat(response.getTxnAmount()).isEqualTo(request.getTxnAmount());
        soft.assertThat(response.getResponseCode()).isEqualTo("01");
        soft.assertThat(response.getResponseMessage()).isEqualTo("Txn Successful.");
        soft.assertThat(response.getStatus()).isEqualTo("TXN_SUCCESS");
        soft.assertThat(response.getPaymentMode()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getBankName()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getCustId()).isEqualTo(request.getCustomerId());
        soft.assertThat(response.getLoyaltyPoints()).isNotEmpty().isNotNull();
        soft.assertAll();
    }

    public void validateSystemErrorFailureLP(FastForwardRequestDTO request, FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getMerchantId()).isEqualTo(request.getMId());
        soft.assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        soft.assertThat(response.getTxnAmount()).isEqualTo(request.getTxnAmount());
        soft.assertThat(response.getResponseCode()).isEqualTo("501");
        soft.assertThat(response.getResponseMessage()).isEqualTo("System Error");
        soft.assertThat(response.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertThat(response.getPaymentMode()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getBankName()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getCustId()).isEqualTo(request.getCustomerId());
        soft.assertThat(response.getLoyaltyPoints()).isNull();
        soft.assertAll();

    }

    public void validatePaymentDeclinedFailureLP(FastForwardRequestDTO request, FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getMerchantId()).isEqualTo(request.getMId());
        soft.assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        soft.assertThat(response.getTxnAmount()).isEqualTo(request.getTxnAmount());
        soft.assertThat(response.getResponseCode()).isEqualTo("501");
        soft.assertThat(response.getResponseMessage()).isEqualTo("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        soft.assertThat(response.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertThat(response.getPaymentMode()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getBankName()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getCustId()).isEqualTo(request.getCustomerId());
        soft.assertThat(response.getLoyaltyPoints()).isNull();
        soft.assertAll();

    }

    public void validateSystemErrorPendingLP(FastForwardRequestDTO request, FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getMerchantId()).isEqualTo(request.getMId());
        soft.assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        soft.assertThat(response.getTxnAmount()).isEqualTo(request.getTxnAmount());
        soft.assertThat(response.getResponseCode()).isEqualTo("501");
        soft.assertThat(response.getResponseMessage()).isEqualTo("System Error");
        soft.assertThat(response.getStatus()).isEqualTo("PENDING");
        soft.assertThat(response.getPaymentMode()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getBankName()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getCustId()).isEqualTo(request.getCustomerId());
        soft.assertThat(response.getLoyaltyPoints()).isNull();
        soft.assertAll();

    }

    public void validateDuplicateOrderId(FastForwardRequestDTO request, FastForwardResponseDTO response) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.getMerchantId()).isEqualTo(request.getMId());
        soft.assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        soft.assertThat(response.getTxnAmount()).isEqualTo(request.getTxnAmount());
        soft.assertThat(response.getResponseCode()).isEqualTo("325");
        soft.assertThat(response.getResponseMessage()).isEqualTo("The payment failed due to duplicate order id. Please try again.");
        soft.assertThat(response.getStatus()).isEqualTo("TXN_FAILURE");
        soft.assertThat(response.getPaymentMode()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getBankName()).isEqualTo("LOYALTY_POINT");
        soft.assertThat(response.getCustId()).isEqualTo(request.getCustomerId());
        soft.assertThat(response.getLoyaltyPoints()).isNull();
        soft.assertAll();

    }
}
