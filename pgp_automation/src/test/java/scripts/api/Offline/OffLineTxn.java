package scripts.api.Offline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchEMIDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.offline.FetchPayInstrument;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.OfflineDto.FetchPayInstrumentRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.DirectBankOTPPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.OfflineIvrFastForward;
import com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.request.Body;
import com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.request.ExtendInfo;
import com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.request.Head;
import com.paytm.utils.merchant.api.pgp.theia.offline_ivr_fastforward.request.RequestBody;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;

import static com.paytm.appconstants.Constants.Owner.GAGANDEEP;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

/**
 * Created by anjukumari on 09/03/18
 */

@Owner("Deepak")
public class OffLineTxn extends PGPBaseTest {
    private static final String PAYTM_POSTPAID = "PAYTM_DIGITAL_CREDIT";
    private static final String CCPaymentDetails = "4718650100010336|882|052026";
    private static final String ICIOPaymentDetails =  "4375512441465005|882|052026";
    private static final String DCPaymentDetails = "4444333322221111|882|052026";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType pgOnly  = Constants.MerchantType.PGOnly;
    PaymentDTO paymentDTO = new PaymentDTO();

    public Response validateFetchPayInstrument(String mid, User user) throws AuthException {
        FetchPayInstrumentRequest fetchPayInstrumentRequest = new FetchPayInstrumentRequest();
        fetchPayInstrumentRequest.changeMidInRequest(mid);
        fetchPayInstrumentRequest.changeTokenInRequest(user.ssoToken());
        return FetchPayInstrument.executeFetchPaymtInstrument(fetchPayInstrumentRequest);
    }

    public Response validateFetchPayInstrument(String mid, User user, String txnAmount, boolean postpaidOnboarding) throws AuthException {
        FetchPayInstrumentRequest fetchPayInstrumentRequest = new FetchPayInstrumentRequest();
        fetchPayInstrumentRequest.changeMidInRequest(mid);
        fetchPayInstrumentRequest.changeTokenInRequest(user.ssoToken());
        fetchPayInstrumentRequest.changeTxnAmount(txnAmount);
        fetchPayInstrumentRequest.changePostPaidOnboarding(Boolean.toString(postpaidOnboarding));
        return FetchPayInstrument.executeFetchPaymtInstrument(fetchPayInstrumentRequest);
    }

    public String createPaymentDetails(JsonPath jsonPath, String payMethod, User user) {
        String accountNum = jsonPath.param("payMethod", payMethod).getList("body.payMethodViews.merchantPayMethods.findAll { merchantPayMethods -> merchantPayMethods.payMethod == payMethod}.payChannelOptions.balanceInfo.payerAccountNo[0]").get(0).toString();
        String lenderId = jsonPath.param("payMethod", payMethod).getList("body.payMethodViews.merchantPayMethods.findAll { merchantPayMethods -> merchantPayMethods.payMethod == payMethod}.payChannelOptions.balanceInfo.extendInfo.lenderId[0]").get(0).toString();
        String PasscodeToken = ValidatePassCode.accessTokenFromPasscode(new ValidatePassCode(user, "dc_txn"));
        String paymentDetail = accountNum + "|" + lenderId + "|" + PasscodeToken;
        return paymentDetail;
    }

    public String validateFPO(User user, Constants.MerchantType merchantType){
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptionResponse.get("body.orderId").toString();
        return orderId;
    }


    // theia.offline.staticCallbackUrl=https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse?ORDER_ID=
    @Parameters({"theme"})
    @Test(description = "Validate PG only OffLine txn with invalid token")
    public void PGP_527_OfflinePG_TxnWithInvalidToken(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.ADDNPAYPEON, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setSSO_TOKEN("12345asdfg")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespCode())
                .validateRespMsg(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespMsg())
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    // theia.offline.staticCallbackUrl=https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse?ORDER_ID=
    @Parameters({"theme"})
    @Test(description = "Validate PG only OffLine txn with expired token")
    public void PGP_528_OfflinePGTxn_WithExpiredToken(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.ADDNPAYPEON, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setSSO_TOKEN(AuthHelpers.getExpiredToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespCode())
                .validateRespMsg(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespMsg())
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate PG only OffLine txn also validate peon")
    public void PGP_529_OfflinePGTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Test(description = "Validate Single quotes in Response Message are Replaced With Blank Spaces PG only in OffLine txn also validate peon")
    public void PGP_17782_ValidateSingleQuotesinRespMsgReplacedWithBlanks() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.WalletOnly;
        String orderID = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(merchant.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(),
                orderID, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), "SSO",user.ssoToken(),orderID,"1.00")
                .setPaymentMode("CC")
                .build();


        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        String respCode = response.jsonPath().getString("body.txnInfo.RESPCODE");
        String displayMessage = (String) PGPHelpers.getResponseCodeMappingData(LocalConfig.PGP_DB_CONNECTION_URL,respCode)
                .get(0).get("DISPLAY_MESSAGE");
        displayMessage = displayMessage.replace("'"," ");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(displayMessage);
    }

    @Parameters({"theme"})
    @Test(description = "Validate PG only OffLine txn  with non-matching website.")
    public void testOfflinePGTxnWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.PGONLY_COD_PEON_DISABLED, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate PG only OffLine Risk reject txn")
    public void PGP_529_OfflinePGTxnRiskReject(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("1.31")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespMsg("Please try with lower amount or different payment mode. Transaction limits will revise as you continue using Paytm")
                .assertAll();
   }

    @Parameters({"theme"})
    @Test(description = "Validate OffLine Wallet only txn also validate peon send")
    public void PGP_530_OfflineWalletTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.PPBL);
        WalletHelpers.modifyBalance(user, 2.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.WALLETPEON, theme, user)
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("PPI")
                .validateGatewayName("WALLET")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate OffLine Wallet only txn with Non-Matching Website")
    public void testOfflineWalletTxnWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.WALLET_ONLY_PEON_DISABLED, theme, user)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("PPI")
                .validateGatewayName("WALLET")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate peon send for successfull OffLine txn through UPI Collect", enabled = true)
    public void PGP_531_OfflineTxn_UPI_Collect(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.Hybrid, theme, user)
                .setPAYMENT_TYPE_ID("UPI")
                .setPAYMENT_DETAILS(new PaymentDTO().getVpa())
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("ICICI")
                .validatePaymentMode("UPI")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending(Duration.TWO_MINUTES);
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("ICICI")
                .validatePaymentMode("UPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate  successfull OffLine txn through UPI Collect with Non-Matching website")
    public void testOfflineTxn_UPI_CollectWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.PGONLY_COD_PEON_DISABLED, theme, user)
                .setPAYMENT_TYPE_ID("UPI")
                .setPAYMENT_DETAILS(new PaymentDTO().getVpa())
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("ICICI")
                .validatePaymentMode("UPI")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending(Duration.TWO_MINUTES);
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("ICICI")
                .validatePaymentMode("UPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    /** Test case can be disabled as transaction is performed through mock instead of PPBL staging
     purpose of test case is not resolved
     */
  //  @Parameters({"theme"})
  //  @Test(description = "Validate OffLine PPBL Success txn also check peon send for this txn", enabled = false)
    public void PGP_532_OfflinePPBLTxn(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC, PGPBaseTest.Label.PPBL);
        Response ValidatePassCodeResponse = new ValidatePassCode(user, "bank_txn").execute();
        JsonPath path = ValidatePassCodeResponse.jsonPath();
        String PasscodeToken = path.get("access_token").toString();
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.HYBPEON, theme, user)
                .setPAYMENT_TYPE_ID("NB")
                .setBANK_CODE("PPBL")
                .setPAYMENT_DETAILS(PasscodeToken)
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBL")
                .validatePaymentMode("NB")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateGatewayName("PPBL")
                .validateBankName("PPBL")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("NB")
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate OffLine PPBL Success txn for non-matching website")
    public void testOfflinePPBLTxnWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC, PGPBaseTest.Label.PPBL);
        Response ValidatePassCodeResponse = new ValidatePassCode(user, "bank_txn").execute();
        JsonPath path = ValidatePassCodeResponse.jsonPath();
        String PasscodeToken = path.get("access_token").toString();
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.HYBRID_PEON_DISABLED, theme, user)
                .setPAYMENT_TYPE_ID("NB")
                .setBANK_CODE("PPBL")
                .setPAYMENT_DETAILS(PasscodeToken)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBL")
                .validatePaymentMode("NB")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateGatewayName("PPBL")
                .validateBankName("PPBL")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("NB")
                .AssertAll();
    }

    @Test(description = "Validate successful offline transaction using postpaid when onboarding")
    public void successOfflineTxn_postpaidOnboarding() throws Exception {
        User user = userManager.getForWrite(Label.POSTPAIDONBOARDING);
        PostpaidHelpers.updatePostpaidUserAttributes(user, PostpaidHelpers.WHITELISTED);
        PostpaidHelpers.updateBalance("1000");
        JsonPath validateFetchPayInstrumentPath = validateFetchPayInstrument(Constants.MerchantType.PPBL_PAYTMCC_VPA.getId(),
                user, "75", true).jsonPath();
        String accountNum = "", lenderId = "";
        try {
            accountNum = validateFetchPayInstrumentPath
                    .param("payMethod", "PAYTM_DIGITAL_CREDIT")
                    .getList("body.payMethodViews.merchantPayMethods.findAll { merchantPayMethods -> merchantPayMethods.payMethod == payMethod}.payChannelOptions.balanceInfo.payerAccountNo[0]")
                    .get(0).toString();
            lenderId = validateFetchPayInstrumentPath
                    .param("payMethod", "PAYTM_DIGITAL_CREDIT")
                    .getList("body.payMethodViews.merchantPayMethods.findAll { merchantPayMethods -> merchantPayMethods.payMethod == payMethod}.payChannelOptions.balanceInfo.extendInfo.lenderId[0]")
                    .get(0).toString();
        } catch (NullPointerException ex) {
            Assertions.fail("Fail to fetch PAYTM_DIGITAL_CREDIT payMethod in FetchPaymentInstrument response");
        }
        String paymentDetail = accountNum + "|" + lenderId + "|";
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.PPBL_PAYTMCC_VPA, "", user)
                .setPAYMENT_TYPE_ID("PAYTM_DIGITAL_CREDIT")
                .setPAYMENT_DETAILS(paymentDetail)
                .setTXN_AMOUNT("75")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(PAYTM_POSTPAID)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateGatewayName("PAYTMCC")
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(PAYTM_POSTPAID)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate OffLine PAYTMCC Success txn also check peon send for this txn")
    public void PGP_532_OfflinePaytmCCTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC, Label.POSTPAID);
        String payMethod = "PAYTM_DIGITAL_CREDIT";
        JsonPath validateFetchPayInstrumentPath = validateFetchPayInstrument(Constants.MerchantType.PPBL_PAYTMCC_VPA.getId(), user).jsonPath();
        String paymentDetails = createPaymentDetails(validateFetchPayInstrumentPath, payMethod, user);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.PPBL_PAYTMCC_VPA, theme, user)
                .setPAYMENT_TYPE_ID(payMethod)
                .setPAYMENT_DETAILS(paymentDetails)
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(PAYTM_POSTPAID)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateGatewayName("PAYTMCC")
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(PAYTM_POSTPAID)
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate OffLine PAYTMCC Success txn with non-matching website ")
    public void testOfflinePaytmCCTxnWhenNonMatchingWebsiteProvided(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC, Label.POSTPAID);
        Constants.MerchantType merchantType = Constants.MerchantType.HYBRID_PEON_DISABLED;
        String payMethod = "PAYTM_DIGITAL_CREDIT";
        JsonPath validateFetchPayInstrumentPath = validateFetchPayInstrument(merchantType.getId(), user).jsonPath();
        String paymentDetails = createPaymentDetails(validateFetchPayInstrumentPath, payMethod, user);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(merchantType, theme, user)
                .setPAYMENT_TYPE_ID(payMethod)
                .setPAYMENT_DETAILS(paymentDetails)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(PAYTM_POSTPAID)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateGatewayName("PAYTMCC")
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(PAYTM_POSTPAID)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Hybrid only OffLine txn also validate peon")
    public void PGP_529_OfflineHybTxn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setAddMoney("0")
                .setTXN_AMOUNT("2.0")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("HYBRID")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validatePaymentMode("HYBRID")
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString());
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Hybrid only OffLine for non-matching website")
    public void testOfflineHybTxnWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.HYBRID_PEON_DISABLED, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setAddMoney("0")
                .setTXN_AMOUNT("2.0")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("HYBRID")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validatePaymentMode("HYBRID")
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString());
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Add n Pay only OffLine txn also validate peon")
    public void PGP_529_OfflineAddnPayTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.SUBSCRIPTION_ADDNPAY, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .setAddMoney("1")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0);
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Add n Pay only OffLine with non-matching website.")
    public void testOfflineAddnPayTxnWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.ADDNPAY_PEON_DISABLED, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .setAddMoney("1")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Saved card OffLine txn also validate peon by saving card on user with trusted card api")
    public void PGP_529_OfflineSavedCardTxn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_ADDNPAY, theme, user)
                .setPAYMENT_DETAILS(saveCardId + "|" + paymentDTO.getCvvNumber())
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Saved card OffLine txn also validate peon by saving card on user with trusted card api")
    public void testOfflineSavedCardTxnWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.PGONLY_COD_PEON_DISABLED, theme, user)
                .setPAYMENT_DETAILS(saveCardId + "|" + paymentDTO.getCvvNumber())
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .AssertAll();
    }

    @Test(description = "Check successful Offline Fast Forward Txn")
    public void checkOfflineFastForwardTxn() throws Exception {
        String mid = Constants.MerchantType.FastForward.getId();
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        String txnAmt = "1";

        Head head = new Head();
        head.setVersion("M");
        head.setMid(mid);
        head.setRequestTimestamp("M");
        head.setRequestId("M");
        head.setClientId("paytm-pg-client-staging");
        head.setToken(user.ssoToken());
        head.setTokenType("SSO");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setKey("value");

        Body body = new Body();
        body.setSignature("");
        body.setReqType("CLW_APP_PAY");
        body.setPaymentMode("PPI");
        body.setTxnAmount(txnAmt);
        body.setCustomerId("1000036031");
        body.setIndustryType("Retail");
        body.setCurrency("INR");
        body.setDeviceId("9650788700");
        body.setAppIP("");
        body.setAuthMode("USRPWD");
        body.setChannel("WEB");
        body.setExtendInfo(extendInfo);

        RequestBody requestBody = new RequestBody();
        requestBody.setHead(head);
        requestBody.setBody(body);

        new OfflineIvrFastForward(requestBody).execute();
        WalletHelpers.validateBalance(user, 0);
    }

    @Parameters({"theme"})
    @Test(description = "Validate PG only OffLine txn with DC")
    public void validatePGonlyWithLoginUsingDC(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setPAYMENT_DETAILS(DCPaymentDetails)
                .setPAYMENT_TYPE_ID("DC")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("DC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("DC")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate PG only OffLine txn with Saved Card")
    public void validatePGonlyWithLoginUsingSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        String saveCardId = SavedCardHelpers.getSavedCardId(user,0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setPAYMENT_DETAILS(saveCardId + "|" + paymentDTO.getCvvNumber())
                .setPAYMENT_TYPE_ID("CC")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate AddNPay OffLine txn with Debit Card and validate Peon as well")
    public void validateAddNPayUsingDC(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_ADDNPAY, theme, user)
                .setPAYMENT_DETAILS(DCPaymentDetails)
                .setPAYMENT_TYPE_ID("DC")
                .setTXN_AMOUNT("2.0")
                .setAddMoney("1")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0);
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate AddNPay OffLine txn with Debit Card for non-matching website")
    public void testAddNPayUsingDCWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.ADDNPAY_PEON_DISABLED, theme, user)
                .setPAYMENT_DETAILS(DCPaymentDetails)
                .setPAYMENT_TYPE_ID("DC")
                .setTXN_AMOUNT("2.0")
                .setAddMoney("1")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0);
    }

    @Parameters({"theme"})
    @Test(description = "Validate AddNPay OffLine txn with Saved Card and validate Peon as well")
    public void validateAddNPayUsingSavedCard(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        String saveCardId =  SavedCardHelpers.getSavedCardId(user,0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_ADDNPAY, theme, user)
                .setPAYMENT_DETAILS(saveCardId + "|" + paymentDTO.getCvvNumber())
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .setAddMoney("1")
                .setSTORE_CARD("1")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0);
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Parameters({"theme"})
    @Test(description = "Validate AddNPay OffLine txn with Saved Card for non-matching website")
    public void testAddNPayUsingSavedCardWhenNonMatchingWebsiteProvided(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        String saveCardId =  SavedCardHelpers.getSavedCardId(user,0);
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.ADDNPAY_PEON_DISABLED, theme, user)
                .setPAYMENT_DETAILS(saveCardId + "|" + paymentDTO.getCvvNumber())
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .setAddMoney("1")
                .setSTORE_CARD("1")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        CheckoutPage checkoutPage=new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0);
    }



    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Parameters({"theme"})
    @Test(description = "Validate Oflline txn with Direct Bank HDFO gateway")
    public void validateHDFObankDIrectBankTxn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);

        JsonPath validateFetchPayInstrumentPath = validateFetchPayInstrument(Constants.MerchantType.STATICQR_DIRECT_HDFO.getId(), user).jsonPath();
        String OrderId = (String) validateFetchPayInstrumentPath.getMap("body").get("orderId");

        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.STATICQR_DIRECT_HDFO, theme, user)
                .setPAYMENT_DETAILS(CCPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .setORDER_ID(OrderId)
                .build();

        ObjectMapper oMapper = new ObjectMapper();
        HashMap<String, String> formData = oMapper.convertValue(orderDTO, HashMap.class);
        ProcessTransaction processTransaction = new ProcessTransaction();
        processTransaction.getRequestSpecBuilder().addFormParams(formData);
        checkoutPage.createOrder(orderDTO);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
      //  directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("123456");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }



    @Epic(Constants.Sprint.SPRINT29_2)
    @Feature("PGP-17769")
    @Parameters({"theme"})
    @Issue("PGP-22045")
    @Test(description = "Validate offline txn with ICICI bank gateway", groups = Group.Status.BUG)
    public void validateICICIbankDIrectBankTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);

        JsonPath validateFetchPayInstrumentPath = validateFetchPayInstrument(Constants.MerchantType.STATICQR_DIRECT_ICIO.getId(), user).jsonPath();
        String OrderId = (String) validateFetchPayInstrumentPath.getMap("body").get("orderId");

        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.STATICQR_DIRECT_ICIO, theme, user)
                .setPAYMENT_DETAILS(ICIOPaymentDetails)
                .setPAYMENT_TYPE_ID("CC")
                .setTXN_AMOUNT("2.0")
                .setORDER_ID(OrderId)
                .build();

        ObjectMapper oMapper = new ObjectMapper();
        HashMap<String, String> formData = oMapper.convertValue(orderDTO, HashMap.class);
        ProcessTransaction processTransaction = new ProcessTransaction();
        processTransaction.getRequestSpecBuilder().addFormParams(formData);
        checkoutPage.createOrder(orderDTO);

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("123456");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    // New Offline Flow


    @Test(description = "Verify EMI failure when HDFC DC is passed for HDFC CC EMI offline transaction")
    public  void EMI_Offline_Card_Type_failure() throws Exception {
        User user = userManager.getForRead(Label.EMIDC,Label.MGV);

        String orderId = validateFPO(user,Constants.MerchantType.NATIVE_EMI);
        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest("SSO", user.ssoToken(), "HDFC", Constants.MerchantType.NATIVE_EMI.getId());
        JsonPath fetchEMIDetail = new FetchEMIDetail(fetchEMIDetailRequest, Constants.MerchantType.NATIVE_EMI.getId()).execute().jsonPath();
        String planId = fetchEMIDetail.get("body.emiDetail.emiChannelInfos[0].planId").toString();

        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(Constants.MerchantType.NATIVE_EMI, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("20"))
                .setPaymentMode("EMI")
                .setCardInfo(paymentDTO.getDebitCardNumber())
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setPlanId(planId)
                .setPayerAccount(null)
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        JsonPath processTransactionV1Response = processTransactionV1.execute().jsonPath();
        Assertions.assertThat(processTransactionV1Response.getString("body.resultInfo.resultCode")).as("resultCode mismatch").isEqualTo("0001");
        Assertions.assertThat(processTransactionV1Response.getString("body.resultInfo.resultMsg")).as("resultCode mismatch").isEqualTo("Request parameters are not valid");
    }


    //Risk Extend Info cases

    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn success with duplicate mode parameter in risk Extend Info as for duplicate entry only 1 map should be created at p+ side for PayMode DC")
    public void validateRiskExtendInfoDuplicateModeDC() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        //FPO with MID, SSO
        String orderId = validateFPO(user,pgOnly);

        //PTC with MID, SSO
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setChannelCode("HDFC")
                .setExtendInfoStaticFlow()
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                    //Duplicate Mode in Risk Extend Info
                .setRiskExtendInfo("scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Yumm Mumm|mode:recentBeneficiary|wifi:|mode:recentBeneficiary|userLbsLatitude:|userLbsLongitude:")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(pgOnly.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();

    }

    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn success with duplicate mode parameter in risk Extend Info as for duplicate entry only 1 map should be created at p+ side for PayMode CC")
    public void validateRiskExtendInfoDuplicateModeCC() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String orderId = validateFPO(user,pgOnly);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setChannelCode("HDFC")
                .setExtendInfoStaticFlow()
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                //Duplicate Mode in Risk Extend Info
                .setRiskExtendInfo("scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Yumm Mumm|mode:recentBeneficiary|wifi:|mode:recentBeneficiary|userLbsLatitude:|userLbsLongitude:")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(pgOnly.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();

    }

    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn success with duplicate mode parameter in risk Extend Info as for duplicate entry only 1 map should be created at p+ side for PayMode NB")
    public void validateRiskExtendInfoDuplicateModeNB() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        String orderId = validateFPO(user, pgOnly);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setExtendInfoStaticFlow()
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                //Duplicate Mode in Risk Extend Info
                .setRiskExtendInfo("scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Yumm Mumm|mode:recentBeneficiary|wifi:|mode:recentBeneficiary|userLbsLatitude:|userLbsLongitude:")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(pgOnly.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();
    }

    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn success with Risk Extenf Info as comma seperated parameter in risk Extend Info as for duplicate entry only 1 map should be created at p+ side for PayMode DC")
    public void validateRiskExtendInfoCommaSeperatedDC() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        //FPO with MID, SSO
        String orderId = validateFPO(user,pgOnly);

        //PTC with MID, SSO
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setChannelCode("HDFC")
                .setExtendInfoStaticFlow()
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                //Comma Seperated in Risk Extend Info
                .setRiskExtendInfo("{ number:9876543210, alphabets:qazxswedcvfrtgbnhyujmkiolp, symbols: _+=-/:';|<>?{}[]()*&^%$#@!}")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(pgOnly.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();

    }


    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn success with Risk Extend Info as comma seperated parameter in risk Extend Info as for duplicate entry only 1 map should be created at p+ side for PayMode DC")
    public void validateRiskExtendInfoPipeSeperatedSpecialDC() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        //FPO with MID, SSO
        String orderId = validateFPO(user,pgOnly);

        //PTC with MID, SSO
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setChannelCode("HDFC")
                .setExtendInfoStaticFlow()
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                //Pipe and Special Char seperated params
                .setRiskExtendInfo("{ number:9876543210| alphabets:qazxswedcvfrtgbnhyujmkiolp| symbols: _+=-/:';|<>?{}[]()*&^%$#@!}")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(pgOnly.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();

    }

    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn success with Risk Extend Info as 300 length in risk Extend Info as for duplicate entry only 1 map should be created at p+ side for PayMode EMI")
    public void validateRiskExtendInfoPipeEMI() throws Exception {
        Constants.MerchantType emiMerchant = Constants.MerchantType.EMI;
        User user = userManager.getForRead(Label.EMIDC,Label.MGV);

        //FPO with MID, SSO
        String orderId = validateFPO(user,emiMerchant);

        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest("SSO", user.ssoToken(), "HDFC", Constants.MerchantType.EMI.getId());
        JsonPath fetchEMIDetail = new FetchEMIDetail(fetchEMIDetailRequest, emiMerchant.getId()).execute().jsonPath();
        String planId = fetchEMIDetail.get("body.emiDetail.emiChannelInfos[0].planId").toString();

        //PTC with MID, SSO
        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(emiMerchant, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("20"))
                .setPaymentMode("EMI")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setPlanId(planId)
                .setPayerAccount(null)
                .setRiskExtendInfo("\"longstring\":\"(hnG/w[2$m57)/9P#i?Hfj[w9Z]2SP,$SQS(GQDY&YEk)=-2Q$upKXd$Z;m5;T7NHCzK;S3+R!CJu)8%*JtHfw[-.Ww*56(6FJvG}fTPPMCZYjK:q?LZ;fiU=H8K3-[_hwhWpvR/_a2RQCzC:_pY}NCUGpQy;6R3C(ArL;fGV8{U7KmwPV-Q;J-_*8m.wFjkZPpGx;+rhp9(*#)qpiJ9.E{Pcd/}YVNwXE(%uX_QNz.Vw7EC!E7{+Y;jk?y@_=}Di[ELES=h-Z*WJ$DW%ng%z*/2tnW$.y8r&MHW%BZfXUG7\"")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(emiMerchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();

    }


    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn success with Risk Extend Info as 200 key value pairs length in risk Extend Info as for duplicate entry only 1 map should be created at p+ side for PayMode NB")
    public void validateRiskExtendInfoPipePPI() throws Exception {

        String txnAmount = "2.00";
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));
        //FPO with MID, SSO
        String orderId = validateFPO(user,pgOnly);

        //PTC with MID, SSO
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setExtendInfoStaticFlow()
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                .setRiskExtendInfo("duty:2005368154|naturally:camera|see:1403291981|cheese:title|since:-1514117502|take:550859158|chicken:467465660|expression:cast|electric:-625104068.0134006|cotton:1524943056.1961737|dinner:note|slip:583633326|boat:numeral|clean:knew|shown:-1649066190|grow:1957413555.6415787|tears:son|divide:string|trail:those|pot:shinning|number:237033080.01871538|sit:-1156317769.2955914|yet:decide|noted:fifteen|space:radio|even:moving|least:-250120322.989933|burn:mind|across:2113070739.8695507|needs:worse|fence:-321709922|outline:492690518.17281294|bet:worry|greater:environment|seat:-60832532|flow:1341962448.695022|shore:straw|range:large|watch:judge|again:everywhere|problem:grandmother|breathing:60479479|cowboy:-40665334.65555525|pair:721369330|alike:-477002126.5058818|hunter:stove|old:788287536.2479758|older:1184298023|hospital:-837438459.536047|buffalo:1853965602|caught:equator|them:basic|frighten:-1704527181|distance:-349907402.6008711|wagon:-1921466496|therefore:68080814|height:1353225097.572805|police:1610957769.7517524|vapor:darkness|escape:410016718|thee:2147232962.3251052|law:nature|percent:shout|row:bound|pattern:blue|triangle:-1163242114|wing:remember|cattle:1140884754|thrown:began|harbor:229478435.35504627|hunt:organized|title:scared|edge:1580397431|largest:loss|universe:-539596483.303927|battle:706049783|successful:day|course:castle|political:start|else:feet|friendly:longer|theory:-618200817.6472664|proper:-639501847.1281104|paper:dig|enemy:day|avoid:-716945431|corn:562452287.0416751|bark:-1073092533.8675566|beat:usually|also:degree|buried:recall|hardly:oil|railroad:feature|share:-362851547.61389494|spite:mine|ruler:die|own:apartment|positive:speak|unknown:-1720737359.9717216|clear:account|love:1397171425.6121035|early:swept|anybody:cell|stems:339802962.9979892|ranch:-1217245675|many:question|baby:634667661|require:-1273666748|grass:1889746321.3808937|me:-1622242595.4711285|had:-1970815904|book:someone|verb:-559756903|earth:-1680793|dig:1241560624|barn:1969022822|wish:1232206578.1043706|later:58863487.873878|sitting:-1724594447.0403428|research:1234494946.4720325|in:naturally|develop:review|remove:1509619882.170298|sound:base|neck:softly|surrounded:take|chosen:having|realize:-2103822889.7066317|soil:-1567958904.3920321|beyond:trunk|place:1113924752.4864874|known:spin|union:shown|ear:key|blow:442687698|heavy:267125188|stone:sitting|pick:thick|silence:-782643427.865869|balloon:1660295045.846579|fifteen:2110452526|ability:-517395462|coat:cave|recently:great|extra:911774215.5228171|brother:consonant|sharp:1213803544|introduced:-981574507|anyone:town|service:748428438|close:97434952.08315277|pay:-442100076|state:number|division:motion|on:298781114|becoming:trunk|lunch:1702143533|fair:-883218933.4952712|upon:-1355857958|point:leave|attempt:-1003857175|plain:congress|jungle:bus|want:1681468690|warn:-1438833880|situation:strong|bit:-1280760283|pine:brown|apple:hold|obtain:348930573.5337143|fuel:1373625853|do:-1007915000.7154479|matter:older|red:breathe|soldier:-690442123.3105979|folks:1351359199.4611511|afraid:jungle|go:-1080296359|prize:volume|tightly:fence|tightly:fence|planned:planned|now:brick|fox:-398263317|noon:closely|straight:-2093513383|quiet:1920506974|certainly:215850404|pie:632166336")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(pgOnly.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();

    }

    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-20562")
    @Owner("Tarun")
    @Test(description = "To validate txn failure with Risk Extend Info as more than 4096 characters with PayMode NB")
    public void validateRikExtendInfoMaxSize() throws Exception {

        String txnAmount = "2.00";
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));
        //FPO with MID, SSO
        String orderId = validateFPO(user,pgOnly);

        //PTC with MID, SSO
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setExtendInfoStaticFlow()
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                .setRiskExtendInfo("original:seed|learn:region|wire:thumb|girl:mostly|taught:290251508|practical:recently|shorter:-943201137|else:army|passage:1949224072.3686132|rush:root|alone:-1533250710|fast:-72660371.00757122|education:prize|high:139108259|small:soap|happened:nine|later:instead|trace:meat|settlers:his|window:member|unusual:angle|darkness:famous|rice:time|sell:not|it:1021622623.7093701|struggle:-1903696845|mill:1815376629.277121|class:-839510262|tip:bright|struck:usually|too:stock|fire:1756018868.7124314|hurry:72057662.50718117|seed:but|late:blood|soft:1266776249.4352894|nearer:-662533327.2409058|conversation:white|control:coal|does:1671246707|answer:glad|require:-629645077|post:final|thirty:2030903919|dangerous:verb|cattle:grandmother|other:struck|easier:your|income:steep|facing:-1445340554|knew:burn|information:speed|hole:eaten|drink:rule|substance:gather|slept:use|gone:generally|represent:fine|main:-261005394|seen:1184264317|basic:1954554561.6010728|accurate:coat|piece:eat|symbol:-554087741|again:somehow|depth:my|means:1719386309.9695816|belong:-448841589|vertical:weather|ranch:sold|sick:folks|attached:conversation|pool:537005141.8146644|fifty:someone|we:property|movie:satisfied|furniture:hurried|on:-1828100897.7753358|hour:-1718266068|voyage:break|grow:pocket|care:relationship|driving:-1054385373.8032079|growth:leader|species:gun|death:toy|drop:charge|college:flame|declared:210669669.83947515|field:1846680652|carbon:low|occur:2084706392|ate:-1908553350|figure:1244740105|pain:touch|brush:-1868966858|repeat:muscle|past:various|amount:environment|pattern:hide|train:stood|connected:chose|shirt:1154948994.9258084|fell:787691088.7684121|bright:-1715512932.4823742|nearly:481175552.6103897|twelve:1677110131|steam:999881399|hat:1813523019|how:adult|unhappy:announced|short:-1572676896.5362635|money:-802388326|himself:-25507759.321148396|however:apartment|anyone:uncle|hundred:174982055|factory:herd|kids:sand|land:155328149|garage:above|gasoline:-1344963574|lovely:escape|indicate:-1741275849|change:-527456133.7015457|ago:look|path:popular|vegetable:camp|donkey:bit|excitement:52623027|measure:special|pile:using|meant:crew|slightly:relationship|triangle:-467663641|bit:basis|date:1836699128.5720487|solar:definition|magnet:-1899264871.502245|chart:849617023.8750682|huge:visitor|river:mud|equator:572098103.3962259|capital:complete|cage:ten|search:15218587|getting:clearly|price:1080248316.9163003|tropical:2103873215.498074|inch:-1598223500.8657684|question:vessels|mark:basic|yourself:shallow|activity:ate|nation:-665416454|wild:1431883301|metal:-84957284|therefore:330938764|condition:danger|cover:death|cool:son|right:154237170|physical:1972547672|birds:-1252866411.398591|bow:2020665717|locate:toy|hospital:1874276108|was:818077576|will:saw|weigh:-1938489535|until:-638256676|zoo:750405376.2646594|badly:-412833577|sheep:1770848306|congress:tribe|frighten:226610424|spent:875275428|ground:233663199|route:team|top:301172049|best:accept|stand:187835800|effect:aboard|sail:silence|everything:baby|tomorrow:term|origin:226765152.3613186|airplane:several|long:acres|mine:broad|pen:conversation|any:kill|engine:1028671318.9269285|see:pressure|slide:surprise|dress:result|layers:paragraph|private:row|pony:guard|have:-2102433687|available:solar|pink:saved|size:higher|strong:1853583072.877986|football:-530047900.17330503|dirt:-1756672905|elephant:compound|gold:-1837990292|rock:does|compare:-253591878|baseball:ask|several:-868305640.1949468|written:realize|changing:square|serious:-272457840|better:-1411814982.7580595|mass:selection|west:trick|skill:2090384477|ruler:1659285048|sent:tired|process:gasoline|luck:rapidly|tobacco:924907209|allow:1341718364.799924|hurried:1297248750.8431044|research:-470957962.4436722|farther:flag|grade:alive|garden:708387785|clothing:-84944014.49858618|call:1577930587|paragraph:led|function:441007057|lay:-1488273829|under:closely|industrial:either|off:-312344625|brown:51948790|swimming:-229297686|upward:grandfather|made:unless|mud:-2075976416|whispered:wheel|day:television|sink:sun|same:-1506\"")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(pgOnly.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .AssertAll();

    }

    @Feature("PGP-28971")
    @Owner(GAGANDEEP)
    @Test(description = "To validate risk extended info with URL parameter in Payment cashier pay request in theia facade")
    public void validateRiskExtendedInfoURLParamsInPayRequest() throws Exception {

        String txnAmount = "2.00";
        User user = userManager.getForWrite(Label.LOGIN);
        String orderId = validateFPO(user,pgOnly);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(pgOnly, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setTxnAmount(new TxnAmount().setValue("2.00"))
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + pgOnly.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"| grep \"risk\"";

        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("registeredAppURL");
        Assertions.assertThat(theiaFacadeLogs).contains("registeredWebURL");
        Assertions.assertThat(theiaFacadeLogs).contains("callbackURL"); }


}