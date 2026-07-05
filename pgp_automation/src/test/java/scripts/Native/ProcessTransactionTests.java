package scripts.Native;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.CreateToken;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.*;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.api.theia.FetchCardDetails;
import com.paytm.api.theia.FetchCardIndexNumber;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchCardDetailsDTO.FetchCardDetailsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.PaymentModes;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.SubwalletAmount;
import com.paytm.dto.NativeDTO.UpdateTransaction.Body;
import com.paytm.dto.NativeDTO.UpdateTransaction.Head;
import com.paytm.dto.NativeDTO.UpdateTransaction.UpdateTransactionDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.*;
import com.paytm.utils.ff4j.FF4JClient;
import com.paytm.utils.ff4j.FF4JClientImpl;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.PayMethodType;
import groovy.json.JsonSlurper;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import scripts.IAddMoney;
import scripts.api.savecardService.SaveCard;
import scripts.api.theia.applyPromo.SSOTokenApplyPromoV1Test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.base.test.Group.Status;
import static com.paytm.base.test.Group.Status.BUG;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

@Owner("Gagandeep")
public class ProcessTransactionTests extends PGPBaseTest implements IAddMoney {
    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env="+ LocalConfig.ENV_NAME;
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private static final String theme = "enhancedweb";
    private static final String DISABLED_PAYMENT_MODE_ERROR_MSG = "{paymentMode} is not allowed for this transaction, kindly use some other payment mode";
    private static final String postConvFlag = "";
    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
    }
    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
    private final OopsPage oopsPage = new OopsPage();
    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();
    private final String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    private final String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";

    protected OrderDTO initiateTxnUsingPromo(String ssoToken, MerchantType merchantType, Constants.promoCode promocode, PayMethodType payMethodType, Boolean isNativePlus) {
        OrderDTO orderDTO =null;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, merchantType)
                .setPromoCode(promocode.toString())
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();

        Assertions.assertThat(initTxnResponse.getBody().isPromoCodeValid())
                .as("Promocode is marked as invalid")
                .isEqualTo(true);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptResponseDTO.set(FetchPaymentOption.executeFetchPaymtOption(
                merchantType.getId(), orderId, fetchPaymentOptionsDTO));
        if(payMethodType.name().equals("NET_BANKING")){
             orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, payMethodType)
                    .setChannelCode("ICICI")
                    .setAUTH_MODE("USRPWD")
                    .build();
        }else {
             orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, payMethodType)
               //      .setCardInfo("|4718650100010336|618|1223add23")
                     .setCardInfo("|4718650100010336|618|122323")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();

        return orderDTO;
    }

    protected void validateSuccessPromo(FetchPaymentOptResponseDTO fetchPaymentOptResponse, Constants.promoCode promoCode) {
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getResultInfo().getResultMsg())
                .isEqualToIgnoringCase("Success");
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getPromoCodeData().isPromoCodeValid())
                .as("isPromoCodeValid is marked as false")
                .isTrue();
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getPromoCodeData().getPromoCode())
                .as("PromoCode text mismatch")
                .isEqualToIgnoringCase(promoCode.toString());
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textPromoCampId().getText()).isEqualTo(promoCode.toString());
        Assertions.assertThat(responsePage.textPromoRespcode().getText()).isEqualTo("700");
        Assertions.assertThat(responsePage.textPromoStatus().getText()).isEqualTo("PROMO_SUCCESS");
    }


    protected String Validate_EMIDetails(String txnToken, InitTxnDTO initTxnDTO, String channelCode) {
        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest(txnToken, channelCode);
        Response res = new FetchEMIDetail(fetchEMIDetailRequest, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody()).execute();
        JsonPath path = res.jsonPath();
        return path.get("body.emiDetail.emiChannelInfos[0].planId");
    }


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

    /**
     * Same intent as {@link #Validate_FetchPayInstrument} but calls {@code /theia/api/v5/fetchPaymentOptions}
     * via {@link FetchPaymentOptionV5}.
     * <p>
     * v5 response shape differs from v1: {@code body.merchantPayOption.paymentModes[]} entries do not have a
     * top-level {@code isDisabled} map (see {@code FetchPayOptionsV5Test} schema), so the v1 Groovy filter
     * {@code findAll { it.isDisabled.status == ... }} resolves to nothing and {@link JsonPath#getList} is null.
     * Here we assert that {@code paymentMode} is present among enabled options using {@code paymentModes.paymentMode}.
     */
    public JsonPath Validate_FetchPayInstrumentV5(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options (v5) for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        java.util.List<String> paymentModeList = fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.paymentMode");
        Assertions.assertThat(paymentModeList)
                .as("FPO v5: expected paymentModes to list %s (status filter %s not applied — v5 has no paymentModes[].isDisabled). resultMsg=%s",
                        payMethod, status, fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg"))
                .isNotNull()
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }

    public JsonPath Validate_FetchPayInstrument2(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain(payMethod);
        return fetchPaymentOptionsJson;
    }

    protected FetchPaymentOptResponseDTO Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = null;
        try {
            fetchPaymentOptResponseDTO = mapper.readValue(jsonObject.toJSONString(), FetchPaymentOptResponseDTO.class);
        } catch (IOException e) {
            Assertions.fail("Change in FetchPaymentOption DTO", e);
        }
        return fetchPaymentOptResponseDTO;
    }

    protected void Validate_BinDetail(String txnToken, InitTxnDTO initTxnDTO, OrderDTO orderDTO, String binNum) {
        Reporter.report.info("Validating binDetails API  with txn token" + orderDTO.getBANK_CODE());
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNum).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
    }
    protected RiskExtendInfo riskExtendInfoDTO(){

        return new RiskExtendInfo()
                .setOperationOrigin("APP").setOperationType("rent payment").setRentMonthYear("Dec2019").setRentPerMonth("100000")
                .setUserMerchant("216810000016528000000").setIsRentalsPayment("True").setPaytmMerchantId("amanja38238280372187").setIFSC("SBIN000322")
                .setPanCard("CPGPK1767M").setSelfAccount("TRUE").setPanNameMatchFlag("TRUE").setBankAccountNameMatchFlag("TRUE").setIsHighRiskBankAccount("TRUE").setCpId("216810000016528000000")
                .setCpFirstName("A").setCpMiddleName("B").setCpLastName("C").setCpName("C").setCpEmail("abc@gmail.com").setCpMobile("8989898989").setCpIdentityType("ID_CARD").setCpIdentityNo("5bb910960d8a026159d331777b7a9ba7233351e68561485d17a3bc65faff1c2f")
                .setCpCountry("IN").setCpState("UP").setCpCity("Ghaziabad").setCpArea("indirapuram").setCpPostalCode("201010").setCpStreet1("windsor street").setCpStreet2("windsor street")
                .setCpAddress("plot 211 , Abhay khand 1").setCpPaytmUserId("1107223757").setCpAccountNo("9199939333999939").setCpIFSC("SBIN2022202")
                .setCpglobalCardIndex("20200605666006118312bd3b9f606c4879ede6b9fa5d4").setCpVPA("1107223757").setCpVpaName("xyz");
    }

    @Test(description = "Verify DUPLICATE_PAYMENT_REQUEST_EXCEPTION when same FastForward request execute twice")
    public void verifyFastforwardDuplicateRequest() throws Exception {
        User user = userManager.getForRead(Label.VPAENABLED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(MerchantType.Hybrid.getId()).setGenerateOrderId("true").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(MerchantType.Hybrid.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        System.out.println(fetchPaymentOptionsJson);

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(MerchantType.Hybrid.getId(), fetchPaymentOptionsJson.getString("body.orderId"), "2")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .build();
        JsonPath p = new FastForward(fastForwardAppRequest).execute().jsonPath();
        p = new FastForward(fastForwardAppRequest).execute().jsonPath();

        Assertions.assertThat(p.getString("body.resultInfo.resultCode"))
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("DUPLICATE_PAYMENT_REQUEST_EXCEPTION");
        Assertions.assertThat(p.getString("body.resultInfo.resultCodeId"))
                .as("resultCodeId mismatch")
                .isEqualToIgnoringCase("1005");
        Assertions.assertThat(p.getString("body.resultInfo.resultStatus"))
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Validate cancelTransaction API response and Response when MERC_UNQ_REF is sent for Native Txn")
    public void validateCancelTxn_Native(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID)
                .setExtendInfo(new ExtendInfo().setMercUnqRef("testing1"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String cancelTxn = Constants.NativeAPIResourcePath.THEIA_CANCEL_TRANSACTION
                .replace("{mid}", initTxnDTO.getBody().getMid())
                .replace("{orderId}", initTxnDTO.orderFromBody());
        DriverManager.getDriver().get(LocalConfig.PGP_HOST + cancelTxn);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMERC_UNQ_REF("testing1")
                .validateCheckSum(MerchantType.NATIVE_HYBRID.getKey())
                .assertAll();
    }

    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-19896")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native CC transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
    public void TC_PT_001(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setOrderAdditionalInfo(new OrderAdditionalInfo().setMName("Automation").setMID(merchantType.getId()).setMcc("1234").setMLogo("Paytm"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setExtendInfo(extendInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "471865");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Owner("Eshani")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that <whetherBuyerUserIdChange> is not passed in Native flow")
    public void validateWhetherBuyerUserIdChangeFlagisNotPassedinNativeTxn(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("whetherBuyerUserIdChange");

    }




    @Parameters({"isNativePlus"})
    @Test(description = "Validate Single quotes in Response Message are Replaced With Blank Spaces")
    public void PGP_17782_ValidateSingleQuotesinRespMsgReplacedWithBlanks(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.WalletOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        //317 is the Resp Code for Invalid Payment Mode
        String displayMessage = (String) PGPHelpers.getResponseCodeMappingData(LocalConfig.PGP_DB_CONNECTION_URL,"317")
                .get(0).get("DISPLAY_MESSAGE");
        displayMessage = displayMessage.replace("'"," ");
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.WalletOnly, initTxnDTO.orderFromBody(), txnToken,PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg(displayMessage)
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Bank retry count equals to five in Native/ Native Plus flow")
    public void ValidatePaymentsRetryCountEqualsFive(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT("99.84")
                .build();
        ResponsePage responsePage;
        for(int i=0;i<5; i++) {
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("PENDING")
                    .assertAll();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Test(description = "Verify addDescriptionMandatoryflag and descriptionTextFormat in Fetch Payment options" +
            " response from txn token with pref enabled.")
    public void ValidateDescriptionTextinFPOwithTxnTokenWhenPrefEnabled() {
        MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Boolean addDescriptionMandatoryflag = fetchPaymentOptionsJson.getBoolean("body.addDescriptionMandatory");
        String descriptionTextFormat = fetchPaymentOptionsJson.getString("body.descriptionTextFormat");
        Assertions.assertThat(addDescriptionMandatoryflag).isEqualTo(true);
        Assertions.assertThat(descriptionTextFormat).isEqualTo("Automation UBER Text - Jai1");
    }

    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Test(description = "Verify addDescriptionMandatoryflag and descriptionTextFormat in Fetch Payment options" +
            " response from txn token with pref disabled.")
    public void ValidateDescriptionTextinFPOwithTxnTokenWhenPrefDisabled() {
        MerchantType merchantType = MerchantType.Hybrid;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Boolean addDescriptionMandatoryflag = fetchPaymentOptionsJson.getBoolean("body.addDescriptionMandatory");
        String descriptionTextFormat = fetchPaymentOptionsJson.getString("body.descriptionTextFormat");
        Assertions.assertThat(addDescriptionMandatoryflag).isEqualTo(false);
        Assertions.assertThat(descriptionTextFormat).isEqualTo(null);
    }

    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Test(description = "Verify addDescriptionMandatoryflag and descriptionTextFormat in Fetch Payment options" +
            " response from Guest flow with pref enabled.")
    public void ValidateDescriptionTextinFPOwithGuestWhenPrefEnabled() {
        MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder().setTokenType("GUEST").setMid(initTxnDTO.getBody().getMid()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Boolean addDescriptionMandatoryflag = fetchPaymentOptionsJson.getBoolean("body.addDescriptionMandatory");
        String descriptionTextFormat = fetchPaymentOptionsJson.getString("body.descriptionTextFormat");
        Assertions.assertThat(addDescriptionMandatoryflag).isEqualTo(true);
        Assertions.assertThat(descriptionTextFormat).isEqualTo("Automation UBER Text - Jai1");
    }

    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Test(description = "Verify addDescriptionMandatoryflag and descriptionTextFormat in Fetch Payment options" +
            " response from Guest flow with pref Disabled.")
    public void ValidateDescriptionTextinFPOwithGuestWhenPrefDisabled() {
        MerchantType merchantType = MerchantType.Hybrid;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder().setTokenType("GUEST").setMid(initTxnDTO.getBody().getMid()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Boolean addDescriptionMandatoryflag = fetchPaymentOptionsJson.getBoolean("body.addDescriptionMandatory");
        String descriptionTextFormat = fetchPaymentOptionsJson.getString("body.descriptionTextFormat");
        Assertions.assertThat(addDescriptionMandatoryflag).isEqualTo(false);
        Assertions.assertThat(descriptionTextFormat).isEqualTo(null);
    }

    @Test(description = "Verify Success Native+ CC transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
    public void testOrderSuccessByCCWhenNonMatchingWebsiteProvided() {
        MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setWebsiteName("retail")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setWEBSITE("retail")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "471865");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        NativeHelpers.assertRedisKeysNotPresent(txnToken);
    }

    @Test(description = "Verify VPA V2 verfification in fetchPayOption API when SSOtoken is passed in request.")
    public void TC_PT_FetchVPAV2_WithSSOToken() throws Exception {
        User user = userManager.getForRead(Label.VPAENABLED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(MerchantType.PPBLC_ONLY.getId()).setGenerateOrderId("true").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(MerchantType.PPBLC_ONLY.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        //Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.userProfileSarvatra")).as("userProfileSarvatra is null").isNotNull();
        //Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.userProfileSarvatra.status")).as("userProfileSarvatra status is not success.").isEqualTo("success");
        //Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.userProfileSarvatra.response.vpaDetails[0].name")).as("Vpa Name is not present").endsWith("@paytm");
    }

    @Test(description = "Verify VPA V2 verfification in fetchPayOption API when TxnToken is passed in request.")
    public void TC_PT_FetchVPAV2_With_TxnToken() throws Exception {
        User user = userManager.getForRead(Label.VPAENABLED);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PPBLC_ONLY).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        //Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.userProfileSarvatra")).as("userProfileSarvatra is null").isNotNull();
        //Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.userProfileSarvatra.status")).as("userProfileSarvatra status is not success.").isEqualTo("success");
        //Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.userProfileSarvatra.response.vpaDetails[0].name")).as("Vpa Name is not present").endsWith("@paytm");
    }

    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-20043")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Risk Reject Native CC transaction, when SSo token is not passed in request.")
    public void Verify_RiskReject(@Optional("false") Boolean isNativePlus) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID)
                .setTxnValue("1.88").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                //.validateRespMsg("Please try with lower amount or different payment mode. Transaction limits will revise as you continue using Paytm")
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("501")
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Native DC transaction also validate binDetail and fetchPayOption API when SSo token is passed in request.")
    public void TC_PT_002(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getDebitCardNumber()));
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Order is closed with Txn Status as Failure after closing order during pending status when merchant Id is not exempted.")
    public void VerifyOrderClosedWithTxnFailureWhenMidNotExemptedfromCloseOrder(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateStatus("PENDING")
                .assertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderDTO.getORDER_ID()).setMid(orderDTO.getMID()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response response = closeOrderAPI.execute();
        String resultMsg = response.path("body.resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("SUCCESS");
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_FAILURE")
                .AssertAll();
    }

    @Test(description = "Verify Order is not closed with Txn Status as failure after closing order during pending status when merchant is exempted from Close Order" +
            "in New Native Flow.")
    public void VerifyOrderNotClosedWithTxnFailureWhenMidExemptedfromCloseOrderNewNative(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Hybrid)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING")
                    .assertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderDTO.getORDER_ID()).setMid(orderDTO.getMID()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response response = closeOrderAPI.execute();
        String resultMsg = response.path("body.resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("SUCCESS");
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateStatus("PENDING")
                .AssertAll();
    }

    @Test(description = "Verify Order is not closed with Txn Status as failure after closing order during pending status when merchant is exempted from Close Order" +
            "in Old Native Flow.")
    public void VerifyOrderNotClosedWithTxnFailureWhenMidExemptedfromCloseOrderOldNative(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PGOnly_NativeOldFlow)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.PGOnly_NativeOldFlow, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING")
                .assertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(orderDTO.getORDER_ID()).setMid(orderDTO.getMID()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response response = closeOrderAPI.execute();
        String resultMsg = response.path("body.resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("SUCCESS");
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateStatus("PENDING")
                 .AssertAll();
        NativeHelpers.assertRedisKeysNotPresent(txnToken);
    }

    @Test(description = "Verify Native DC transaction also validate binDetail and fetchPayOption API when SSo token is passed in request.")
    public void testOrderSuccessByDCWhenNonMatchingWebsiteProvided() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setWebsiteName("retail")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setWEBSITE("retail")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getDebitCardNumber()));
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB transaction when SSo token is not passed in request.")
    public void TC_PT_003(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB transaction when SSo token is passed in request.")
    public void TC_PT_004(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB transaction when SSo token is passed in request.")
    public void testOrderSuccessByNBWhenNonMatchingWebsiteProvided() throws Exception {
        MerchantType merchant = MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setWebsiteName("retail")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setWEBSITE("retail")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify PPI transaction failure when SSO id is not passed in intTransaction request also validate payOption BALANCE is disabled.")
    public void TC_PT_005(@Optional("false") Boolean isNativePlus) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_WALLET_ONLY).setTxnValue(txnAmount.toString()).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_WALLET_ONLY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "true");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        WalletHelpers.validateBalance(user, txnAmount);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify PPI transaction success when SSO id is passed in initTransaction request.")
    public void TC_PT_006(@Optional("true") Boolean isNativePlus) throws Exception {
        Double txnAmount = 2.15;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_WALLET_ONLY)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_WALLET_ONLY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        //Assertions.assertThat(path.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo(txnAmount.toString());
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }


    @Test(description = "Verify PPI transaction success when SSO id is passed in initTransaction request.")
    public void testOrderSuccessByPPIWhenNonMatchingWebsiteProvided() throws Exception {
        Double txnAmount = 2.15;
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.WALLET_ONLY_PEON_DISABLED;
        WalletHelpers.modifyBalance(user, txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
       // Assertions.assertThat(path.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo(txnAmount.toString());
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify failure Add n pay txn with response message='Wallet balance Insufficient' when add n pay is not enabled on merchant", enabled = true)
    public void TC_PT_014(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_WALLET_ONLY)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_WALLET_ONLY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isNotEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        WalletHelpers.validateBalance(user, txnAmount - 1.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay success transaction also validate payment options as BALANCE in fetchpayoption and paymentflow as ADDANDPAY.", enabled = true)
    public void TC_PT_015(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
      //  Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Test(description = "Verify add n pay success transaction also validate payment options as BALANCE in fetchpayoption and paymentflow as ADDANDPAY.")
    public void testAddNPayOrderSuccessWhenNonMatchingWebsiteProvided() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.AddnPay;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay success without fetchPayOption.", enabled = true)
    public void TC_PT_AddNPayWithoutFetchPayOption(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        //JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        //Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Successful PCF transaction for NB ICICI in Native/ Native+ Create Order then Pay")
    public void validateSucessfullPCFTransactionviaICICINB(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PPBL_NB_PCF)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderDTO.Builder()
                .setORDER_ID(initTxnDTO.orderFromBody())
                .setMID(MerchantType.PPBL_NB_PCF.getId())
                .setCHANNEL_ID("WEB")
                .setTXN_TOKEN(txnToken)
                .setPAYMENT_TYPE_ID(PayMethodType.NET_BANKING.toString())
                .setChannelCode("ICICI")
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("10.00")
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        //double flatCommission = new Merchant(MerchantType.PPBL_NB_PCF.getId(), MerchantType.PPBL_NB_PCF.getKey(), false).getCommissions().stream().filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1).findAny().get().getFixedFee();
        Double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT())), 0, 4.00, "NB");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
       responsePage.validateChargeAmount(expectedChargeFeeAmt.toString())
                .assertAll();
        //It's failing because of (Need to debug) : ChargeAmount field not available in UI
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB
                        .toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Successful PCF transaction for PPBL for Native/ Native+")
    public void validateSucessfullPCFTransactionviaPPBL(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode = "PPBL";
        User user = userManager.getForWrite(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PPBL_NB_PCF)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderDTO.Builder()
                .setORDER_ID(initTxnDTO.orderFromBody())
                .setMID(MerchantType.PPBL_NB_PCF.getId())
                .setCHANNEL_ID("WEB")
                .setTXN_TOKEN(txnToken)
                .setPAYMENT_TYPE_ID(payMode)
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT("10.00")
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        //double flatCommission = new Merchant(MerchantType.PPBL_NB_PCF.getId(), MerchantType.PPBL_NB_PCF.getKey(), false).getCommissions().stream().filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1).findAny().get().getFixedFee();
        Double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT())), 0, 9.00, "PPBL");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        //Validating PCF amount
        responsePage.validateChargeAmount(expectedChargeFeeAmt.toString())
        .assertAll();
        //It's failing because of (Need to debug) : ChargeAmount field not available in UI
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName(payMode)
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay success transaction with zero wallet balance and without fetchPayOption", enabled = true)
    public void TC_PT_AddNPayWithoutFetchPayOptionWithZeroWalletbalance(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        //JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        //Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify hybrid success transaction, validate success of FetchPaymentOption(CC isDisabled=false), validate FetchBinDetails (binDetails = notEmpty)")
    public void TC_PT_016(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        //Validating Fetch payment Option
        //Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();
        //Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "472642");

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Test(description = "Verify hybrid success transaction, validate success of FetchPaymentOption(CC isDisabled=false), validate FetchBinDetails (binDetails = notEmpty)")
    public void testHybridOrderSuccessWhenNonMatchingWebsiteProvided() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.HYBRID_PEON_DISABLED;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .setWebsiteName("retail")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        //Validating Fetch payment Option
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.DEBIT_CARD)
                .setPaymentFlow("HYBRID")
                .setWEBSITE("retail")
                .build();
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "472642");

        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify UPI Transaction.", enabled = true)
    public void TC_PT_018(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm").build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify UPI Transaction.")
    public void testOrderSuccessByUPIWhenNonMatchingWebsiteProvided() {
        MerchantType merchantType = MerchantType.PGONLY_COD_PEON_DISABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that UPI collect txn successful for Mutual Fund.", enabled = true)
    public void TC_PT_018_MutualFund(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1("test1");
        extendInfo.setUdf2("test2");
        extendInfo.setUdf3("test3");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .setCallbackUrl("https://pg-automation.paytm.in/MerchantSite/bankResponse")
                .setExtendInfo(extendInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("9873966839@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that UPI collect txn successful for Mutual Fund.")
    public void testMutualFundOrderSuccessByUPICollectWhenNonMatchingWebsiteProvided(@Optional("true") Boolean isNativePlus) throws Exception{
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1("test1");
        extendInfo.setUdf2("test2");
        extendInfo.setUdf3("test3");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .setCallbackUrl("https://pg-automation.paytm.in/MerchantSite/bankResponse")
                .setExtendInfo(extendInfo)
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("9873966839@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .setWEBSITE("nonmatchingwebsite")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    //Response Page is not getting opened correctly but getting expected message in logs TODO debugging with Srishti
    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-20043")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify the response message for Mutual Fund merchant risk reject")
    public void mutualFundRiskReject(@Optional("false") Boolean isNativePlus) {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1("test1");
        extendInfo.setUdf2("test2");
        extendInfo.setUdf3("test3");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .setCallbackUrl("https://pg-automation.paytm.in/MerchantSite/bankResponse")
                .setExtendInfo(extendInfo)
                .setTxnValue("1.88")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.MUTUAL_FUND, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("global default message")
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("501")
                .assertAll();

    }


  //  @Parameters({"isNativePlus"})
  //  @Test(description = "Verify that UPI txn is failed when validateAccount number is passed as true and the vpa in the request mismatches from the vpa which is used for payment", enabled = false)
    public void TC_PT_19(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setPayerAccount("9873966839@paytm")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_FAILURE")
                .validateAccNumVarSuccess("false")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespMsg("Txn Failed.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB Transaction is successful and account should be allowed to pay when payment accountnumber matches from the account number passed in request")
    public void TC_PT_020(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("true")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .build();
        System.out.println(initTxnDTO);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = null;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setSTORE_CARD("")
                    .setAUTH_MODE("USRPSWD")
                    .setAccountNumber("7777777777")
                    .setChannelCode("ICICI")
                    .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                    .setCardInfo("")
                    .build();
        }
        else{
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setSTORE_CARD("")
                    .setAUTH_MODE("USRPSWD")
                    .setAccount_number("7777777777")
                    .setChannelCode("ICICI")
                    .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                    .setCardInfo("")
                    .build();
        }
        //JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
        //Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB Transaction is not successful when validateAccountNumber is passed true in request and the payment account number mismatches from the account number passed in the request")
    public void TC_PT_021(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = null;
        if (isNativePlus){
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setSTORE_CARD("")
                    .setAUTH_MODE("USRPSWD")
                    .setAccountNumber("10000000000")
                    .setChannelCode("ICICI")
                    .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                    .build();
        }
        else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setSTORE_CARD("")
                    .setAUTH_MODE("USRPSWD")
                    .setAccount_number("10000000000")
                    .setChannelCode("ICICI")
                    .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                    .build();
        }
        //JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
        //Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB Transaction is not successful when validate account number is passed as false in the request and the payment account number mismatches from the account number passed in the request", groups = {Status.TO_BE_FIXED, "smoke"})
    public void TC_PT_022(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("false")
                .setAllowUnverifiedAccount("false")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = null;
        if (isNativePlus){
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setSTORE_CARD("")
                    .setAUTH_MODE("USRPSWD")
                    .setAccountNumber("10000000000")
                    .setChannelCode("ICICI")
                    .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                    .build();
        }
        else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setSTORE_CARD("")
                    .setAUTH_MODE("USRPSWD")
                    .setAccount_number("10000000000")
                    .setChannelCode("ICICI")
                    .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                    .build();
        }

        //JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
        //Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("227")
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateCheckSum(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("227")
//                .validateRespMsg("Txn Succcess.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

  //  @Test(description = "Verify that Mutual Fund txn is unsuccessful when child MID in the requested parameters is a non mutual fund MID", enabled = false)
    public void TC_PT_023() throws Exception {
        MerchantType merchantType = MerchantType.NON_MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.NON_MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("true")
                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
                .build();
        String resultCode = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Assert.assertEquals(resultCode, "1001");
    }


//    @Test(description = "Verify that Mutual Fund txn is unsuccessful when aggregator MID in the requested parameters is a non mutual fund MID", enabled = false)
    public void TC_PT_024() {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("true")
                .setAggrMid(MerchantType.PGOnly.getId())
                .build();
        String resultCode = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Assert.assertEquals(resultCode, "1001");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify UPI Failure txn in case of duplicate order id.", groups = Status.TO_BE_FIXED)
    public void TC_PT_025(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Successful.")
                .AssertAll();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Failure txn in case of duplicate order id for CC Txn.", enabled = true)
    public void TC_PT_025_CC(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Successful.")
                .AssertAll();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn for channel id - WAP.", enabled = true)
    public void TC_PT_028(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setChannelId("WAP").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).setPayerAccount("test@paytm").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Successful.")
                .AssertAll();
    }


 //   @Parameters({"isNativePlus"})
 //   @Test(description = "Verify txn when DINER card details passed in request.", enabled = false)
    public void TC_PT_030(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("30569309025904");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


  //  @Parameters({"isNativePlus"})
  //  @Test(description = "Verify txn when AMEX card details passed in request.", enabled = false)
    public void TC_PT_031(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("378282246310005");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

  //  @Parameters({"isNativePlus"})
  //  @Test(description = "Verify txn when MAESTRO card details passed in request.", enabled = false)
    public void TC_PT_032(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("6759797637518227");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Issue("PGP-12900")
    @Test(description = "Verify response code when transaction is cancelled by user on bank page.", enabled = true)
    public void TC_PT_035(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validateRespCode("810");
    }

    @Parameters({"isNativePlus"})
    @Issue("PGP-12900")
    @Test(description = "Verify response code when transaction is Failed by bank", enabled = true)
    public void TC_PT_037(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.98")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validateRespCode("810");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "verify refund process for any transaction.", enabled = true)
    public void TC_PT_039(@Optional("true") Boolean isNativePlus) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.NATIVE_HYBRID_PG2_Refund.getId());
        }
        MerchantType merchantType = MerchantType.NATIVE_HYBRID_PG2_Refund;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        PGPHelpers.initiateRefundRequest(
                merchantType.getId(), merchantType.getKey(), initTxnDTO.orderFromBody(), initTxnDTO.orderFromBody()
                , initTxnDTO.getBody().getTxnAmount().getValue(), txnStatus.getResponse().getTXNID(), "");
        PGPHelpers.getRefundStatus(merchantType.getId(), merchantType.getKey(), initTxnDTO.orderFromBody(), true)
                .validateSuccessRefund()
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn using saved cards for CVV of 3 digits.", enabled = true)
    public void TC_PT_040(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2028", "4718650100010336");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        PaymentDTO paymentDTO = new PaymentDTO().setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn using saved cards for CVV of 4 digits.")
    public void TC_PT_041(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", PaymentDTO.AMEX_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        PaymentDTO paymentDTO = new PaymentDTO().setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("1234");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify txn using saved cards for CVV of 3 digits.")
    public void testOrderSuccessBySCWhenNonMatchingWebsiteProvided() throws Exception {
        MerchantType merchantType = MerchantType.HYBRID_PEON_DISABLED;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", "4718650100010336");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setWebsiteName("nonmatchingwebsite")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        PaymentDTO paymentDTO = new PaymentDTO().setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn using saved cards without SSO token", enabled = true)
    public void TC_PT_042(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", "4718650100010336");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        PaymentDTO paymentDTO = new PaymentDTO().setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        oopsPage.imgOops().assertVisible();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify hybrid txn using CC without fetchPayOption.")
    public void TC_PT_hybridWithoutFetchPayOption(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", "4718650100010336");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        //Validating Fetch payment Option
        //Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();
        //Validating Bin Details
        //validate_BinDetail(txnToken, initTxnDTO, orderDTO, "472642");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify hybrid txn using saved card")
    public void TC_PT_043(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", "4718650100010336");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        //Validating Fetch payment Option
        //Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();
        //Validating Bin Details
        //validate_BinDetail(txnToken, initTxnDTO, orderDTO, "472642");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay txn using saved card")
    public void TC_PT_044(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.PGOnly;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", "4718650100010336");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

  //  @Parameters({"isNativePlus"})
  //  @Test(description = "Verify txn when txn amt must be less than 1", enabled = false)
    public void TC_PT_045(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("0.9")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify case when passing decimal txn amt", enabled = true)
    public void TC_PT_046(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("1.1")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.DEBIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Response page when cardTokenRequired is set true in Initiate transaction")
    public void TC_13(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .setCardTokenRequired("true")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateCardIndexNo_isNotEmpty()
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify savedInstruments of FetchPayInstrumentRequest and success of process transaction when card is saved on SSO_TOKEN")
    public void TC_07(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2025", PaymentDTO.DINERS_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getSavedInstruments())
                .as("Saved Instruments is Empty").isNotEmpty();
        PaymentDTO paymentDTO = new PaymentDTO().setSavedCardId(fetchPaymentOptResponse.getBody().getMerchantPayOption()
                .getSavedInstruments().get(0).getCardDetails().getCardId());
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate COD not found as paymethod in fetchPayOption response, when (Txn amount< MIN_COD) and SSo token is passed in request.")
    public void TC_PT_COD_Failure() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.COD)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", false).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("COD");
    }


    @Test(description = "Validate COD not found as paymethod in fetchPayOption response of Native COD txn, when (Txn amount> MIN_COD) and SSo token is not passed in request.")
    public void TC_PT_COD_Failure_WithoutToken() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.COD)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", false).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("COD");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native COD transaction when (Txn_amount> MIN_COD), also validate COD found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void TC_PT_COD_Success(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.COD)
                .setTxnValue("15")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.COD, initTxnDTO.orderFromBody(), txnToken, "COD")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "COD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("CODMOCK")
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native COD transaction when (Txn_amount> MIN_COD), also validate COD found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void testOrderSuccessByCODWhenNonMatchingWebsiteProvided() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.PGONLY_COD_PEON_DISABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("15")
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(merchantType, initTxnDTO.orderFromBody(), txnToken, "COD")
                .setWEBSITE("nonmatchingwebsite")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "COD", "false");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("CODMOCK")
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful Amount based txn with new initiate API with simplifiedSubvention in the request.")
    public void PGP_28966_VerifySuccessEMISimplifiedSubventionTxnAmountBased(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EMI.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.EMI)
                .setTxnValue("10")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),emiId,"1",new OfferDetails().setOfferId("123456")))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.EMI, initTxnDTO.orderFromBody(), txnToken,paymentDTO,PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("8.0")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("HDFC")
                .validatePaymentMode(payMethod)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful Item based txn with new initiate API with simplifiedSubvention in the request.")
    public void PGP_28966_VerifySuccessEMISimplifiedSubventionTxnItemBased(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EMI.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("10");
        items.add(item);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.EMI)
                .setTxnValue("10")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),emiId,items))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.EMI, initTxnDTO.orderFromBody(), txnToken, paymentDTO,PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("9.0")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("HDFC")
                .validatePaymentMode(payMethod)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify Initiate API Request/Response with new simplifiedSubvention parameters but 1 mandatory field is missing or empty (say planId)")
    public void PGP_28966_VerifyEMISimplifiedSubventionWithPlanIdMissing() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.EMI)
                .setTxnValue("10")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),"","1",new OfferDetails().setOfferId("123456")))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Missing mandatory element");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Initiate API Request/Response with new simplifiedSubvention parameters but 1 mandatory field is Incorrect (say planId)")
    public void PGP_28966_VerifyEMISimplifiedSubventionWithIncorrectPlanId(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EMI.getId(), "HDFC");
        String incorrectEmiId = "987";
        String emiPlanId = emiDetails.get(0).get("planId");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.EMI)
                .setTxnValue("10")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),incorrectEmiId,"1",new OfferDetails().setOfferId("123456")))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.EMI, initTxnDTO.orderFromBody(), txnToken,new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER), payMethod)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("810")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Simplified Promo and Simplified EMI Subvention for Discount promo, Amount based")
    public void PGP_28966_ValidateSuccessTxnWhenSimplifiedPromoAndSubventionBothAppliedAmountBased(@Optional("false") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        Constants.MerchantType merchantType = MerchantType.EMI;
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EMI.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),emiId,"1",new OfferDetails().setOfferId("123456")))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), orderId, fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("7.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Simplified Promo and Simplified EMI Subvention for Discount promo, Item based")
    public void PGP_28966_ValidateSuccessTxnWhenSimplifiedPromoAndSubventionBothAppliedItemBased(@Optional("false") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        Constants.MerchantType merchantType = MerchantType.EMI;
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EMI.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("10");
        items.add(item);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),emiId,items))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), orderId, fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("8.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify the successful addnpay txn when simplified offers has valid promo code and validate promo flag is false, txn proceeds without promo.")
    public void PGP_27602_verifyTxnProceedswithoutPromoWhenValidatePromoisFalseAddNPay(@Optional("false") boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("1.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Validate for Add N Pay transaction with ValidatePromo set to true, Bank offers should not be applied, valid discount promo")
    public void PGP_27602_verifyTxnNotProceedingwithPromoWhenValidatePromoisTrueAddNPay(@Optional("true") boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .assertAll();
    }


    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Apply Promo and Simplified EMI Subvention for Discount promo, Amount based")
    public void PGP_28966_ValidateSuccessTxnWhenApplyPromoAndSubventionBothAppliedAmountBased(@Optional("false") boolean isNativePlus) throws Exception {
        ResponsePage responsePage;
        Constants.MerchantType merchantType = MerchantType.EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EMI.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        String txnamt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", "discount");
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),emiId,"1",new OfferDetails().setOfferId("123456")))
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = iniJsonPath.getString("body.txnToken");
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), orderId, fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("7.90")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Apply Promo and Simplified EMI Subvention for Discount promo, Item based")
    public void PGP_28966_ValidateSuccessTxnWhenApplyPromoAndSubventionBothAppliedItemBased(@Optional("false") boolean isNativePlus) throws Exception {
        ResponsePage responsePage;
        Constants.MerchantType merchantType = MerchantType.EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EMI.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        String txnamt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", "discount");
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("body")).put("totalTransactionAmount", txnamt);
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        String instantDiscount = paymentOffersApplied.getOfferBreakupList().get(0).getInstantDiscount();
        item.setPrice(String.valueOf(10 - Double.parseDouble(instantDiscount)));
        items.add(item);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(),emiId,items))
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), orderId, fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("8.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native EMI transaction when (Txn amount> MIN_EMI), also validate EMI found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void TC_PT_EMI_Success(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.EMI)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "HDFC");
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.EMI, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethod)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("HDFC")
                .validatePaymentMode(payMethod)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify Success Native EMI transaction when (Txn amount> MIN_EMI), also validate EMI found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void testOrderSuccessByEMIWhenNonMatchingWebsiteProvided() throws Exception {
        String payMethod = "EMI";
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.HYBRID_PEON_DISABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2")
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "HDFC");
        OrderDTO orderDTO = new OrderFactory.
                Native(merchantType, initTxnDTO.orderFromBody(), txnToken, payMethod)
                .setPlanId(emiPlanId)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("HDFC")
                .validatePaymentMode(payMethod)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Bajaj Finserv Native EMI transaction when (Txn amount> MIN_EMI), also validate EMI found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void TC_PT_BAJAJ_EMI_Success(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Hybrid)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "EMI", "false");

        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "BAJAJFN");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, paymentDTO, payMethod)
                .setPlanId(emiPlanId)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        BajajFinservBankPage bajajFinservBankPage = new BajajFinservBankPage();
        bajajFinservBankPage.waitUntilLoads();
        bajajFinservBankPage.inputOtp("123456");
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("BAJAJFN")
                .validatePaymentMode(payMethod)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify isPromoCodeValid as true and success of transaction when wallet promo is applied transaction using wallet")
    public void verifyWalletPromo_TC_15(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 3.00);
        OrderDTO orderDTO = initiateTxnUsingPromo(user.ssoToken(), MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.WALLET_PROMO, PayMethodType.BALANCE, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();
        validateSuccessPromo(fetchPaymentOptResponseDTO.get(), Constants.promoCode.WALLET_PROMO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify isPromoCodeValid as true and success of transaction when CC promo is applied and transaction is done using CC")
    public void verifyCCPromo_TC_16(@Optional("false") Boolean isNativePlus) {
        OrderDTO orderDTO = initiateTxnUsingPromo(null, MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.CC_PROMO, PayMethodType.CREDIT_CARD, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();
        validateSuccessPromo(fetchPaymentOptResponseDTO.get(), Constants.promoCode.CC_PROMO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify isPromoCodeValid as true and success of transaction when DC promo is applied and transaction is done using DC")
    public void verifyDCPromo_TC_16_1(@Optional("false") Boolean isNativePlus) {
        OrderDTO orderDTO = initiateTxnUsingPromo(null, MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.DC_PROMO, PayMethodType.DEBIT_CARD, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();

        validateSuccessPromo(fetchPaymentOptResponseDTO.get(), Constants.promoCode.DC_PROMO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify isPromoCodeValid as true and success of transaction when NET BANKING promo is applied and transaction is done using NET BANKING")
    public void verifyNBPromo_TC_17(@Optional("true") Boolean isNativePlus) {
        OrderDTO orderDTO = initiateTxnUsingPromo(null, MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.NB_PROMO, PayMethodType.NET_BANKING, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();

        validateSuccessPromo(fetchPaymentOptResponseDTO.get(), Constants.promoCode.NB_PROMO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify isPromoCodeValid as true and paymodes in FetchPaymentOption and success of transaction when restricted CC promo is applied and transaction is done using CC")
    public void verifyRestrictCCPromo_TC_20(@Optional("false") Boolean isNativePlus) {
        OrderDTO orderDTO = initiateTxnUsingPromo(null, MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.RESTRICTED_CC_PROMO, PayMethodType.CREDIT_CARD, isNativePlus);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOptResponseDTO.get();

        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().size())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo(1);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().get(0).getPaymentMode())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo("CREDIT_CARD");
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();

        validateSuccessPromo(fetchPaymentOptResponse, Constants.promoCode.RESTRICTED_CC_PROMO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify isPromoCodeValid as true and paymodes in FetchPaymentOption and success of transaction when restricted DC promo is applied and transaction is done using DC")
    public void verifyRestrictDCPromo_TC_20(@Optional("false") Boolean isNativePlus) {
        OrderDTO orderDTO = initiateTxnUsingPromo(null, MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.RESTRICTED_DC_PROMO, PayMethodType.DEBIT_CARD, isNativePlus);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOptResponseDTO.get();

        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().size())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo(1);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().get(0).getPaymentMode())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo("DEBIT_CARD");
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();

        validateSuccessPromo(fetchPaymentOptResponseDTO.get(), Constants.promoCode.RESTRICTED_DC_PROMO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify isPromoCodeValid as true and paymodes in FetchPaymentOption and success of transaction when restricted PPI promo is applied and transaction is done using PPI")
    public void verifyRestrictPPIPromo_TC_20(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 3.00);

        OrderDTO orderDTO = initiateTxnUsingPromo(user.ssoToken(), MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.RESTRICTED_PPI_PROMO, PayMethodType.BALANCE, isNativePlus);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOptResponseDTO.get();

        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().size())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo(1);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().get(0).getPaymentMode())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo("BALANCE");
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();

        validateSuccessPromo(fetchPaymentOptResponseDTO.get(), Constants.promoCode.RESTRICTED_PPI_PROMO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction failure response when restrict DC promo is used and transaction is done through credit card")
    public void verifyRestrictDCPromo_TC_20_1(@Optional("true") Boolean isNativePlus) {
        OrderDTO orderDTO = initiateTxnUsingPromo(null, MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.RESTRICTED_DC_PROMO, PayMethodType.CREDIT_CARD, isNativePlus);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOptResponseDTO.get();
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().size())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo(1);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes().get(0).getPaymentMode())
                .as("More than 1 paymodes are displayed in fetchPaymentOption response")
                .isEqualTo("DEBIT_CARD");

        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(responsePage.textRespMsg().getText()).isEqualTo("Invalid payment mode");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native PPBL transaction, also validate PPBL found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void TC_PT_PPBL_Success(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMode = "PPBL";
        User user = userManager.getForWrite(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PPBLYONLY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderDTO.Builder()
                .setORDER_ID(initTxnDTO.orderFromBody())
                .setMID(MerchantType.PPBLYONLY.getId())
                .setCHANNEL_ID("WEB")
                .setTXN_TOKEN(txnToken)
                .setPAYMENT_TYPE_ID(payMode)
                .setAUTH_MODE("USRPWD")
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName(payMode)
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify Success Native PPBL transaction, also validate PPBL found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void testOrderSuccessByPPBLWhenNonMatchingWebsiteProvided() throws Exception {
        String payMode = "PPBL";
        User user = userManager.getForWrite(Label.PPBL);
        MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setWebsiteName("retail")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderDTO.Builder()
                .setORDER_ID(initTxnDTO.orderFromBody())
                .setMID(merchantType.getId())
                .setCHANNEL_ID("WEB")
                .setTXN_TOKEN(txnToken)
                .setPAYMENT_TYPE_ID(payMode)
                .setAUTH_MODE("USRPWD")
                .setMpin(new PaymentDTO().getPasscode())
                .setWEBSITE("retail")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName(payMode)
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native Postpaid transaction, also validate Post paid found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void TC_PT_PostPaid_Success(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode = "PAYTM_DIGITAL_CREDIT";
        User user = userManager.getForWrite(Label.POSTPAID);
        Constants.MerchantType mid = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(mid, initTxnDTO.orderFromBody(), txnToken, payMode)
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify Success Native Postpaid transaction, also validate Post paid found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void testOrderSuccessByPaytmCCWhenNonMatchingWebsiteProvided() throws Exception {
        String payMode = "PAYTM_DIGITAL_CREDIT";
        User user = userManager.getForWrite(Label.POSTPAID);
        MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setWebsiteName("retail")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(merchantType, initTxnDTO.orderFromBody(), txnToken, payMode)
                .setMpin(new PaymentDTO().getPasscode())
                .setWEBSITE("retail")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native Zero cost EMI transaction, also validate EMI found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void TC_PT_0CostEMI_Success(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_EMI)
                .setTxnValue("2")
                .setEmiOption("0CostEMI:8565560_" + LocalConfig.ZERO_COST_EMI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "HDFC");
        Assertions.assertThat(emiPlanId).isEqualTo("HDFC|1");
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.NATIVE_EMI, initTxnDTO.orderFromBody(), txnToken, payMode)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("HDFC")
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify process txn should not be allowed again when retry is not available on merchant")
    public void verifyRetryCase_whenRetryNotAppliedOnMerc(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)

                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        if (isNativePlus) {
            Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
            Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
            Assertions.assertThat(responsePage.textRespMsg().getText()).isEqualTo("System Error.");
            Assertions.assertThat(responsePage.textRespCode().getText()).isEqualTo("501");
        } else {
            Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
            Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
            Assertions.assertThat(responsePage.textRespMsg().getText()).isEqualTo("Retry count breached");
            Assertions.assertThat(responsePage.textRespCode().getText()).isEqualTo("372");
        }
    }

    @Parameters({"isNativePlus"})
    @Issue("PGP-12900")
    @Test(description = "Verify process txn should be successful when txn intiated using CC and retried using NB")
    public void verifyRetryCase_whenRetryIsDoneUsingNB(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID_RETRY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Issue("PGP-12900")
    @Test(description = "Verify process txn should be successful, when txn is initiated using invalid CC and retried using correct CC")
    public void verifyRetryCase_whenRetryIsdoneUsingCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID_RETRY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Issue("PGP-12900")
    @Test(description = "Verify process txn should be successful, when txn is initiated using invalid CC and retried using correct DC")
    public void verifyRetryCase_whenRetryIsdoneUsingDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID_RETRY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

 //   @Parameters({"isNativePlus"})
 //   @Test(description = "Verify process txn should be successful, when txn is initiated using invalid CC and retried using PPBL", enabled = false)
    public void verifyRetryCase_whenRetryIsdoneUsingPPBL(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID_RETRY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PPBL)
                .setMpin("5335")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify process txn should be successful, when txn is initiated using invalid CC and retried using POSTPAID", enabled = true)
    public void verifyRetryCase_RetryBy_Postpaid(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.AddnPay)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.AddnPay, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.AddnPay, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateTxnDate(new Date())
                .AssertAll();
    }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native HDFC Direct transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
    public void Native_Success_DirectChannel(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HDFO).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HDFO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify Success Native HDFC Direct transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
    public void testOrderSuccessByHDFCDirectChannelWhenNonMatchingWebsiteProvided() throws Exception {
        MerchantType merchantType = MerchantType.NATIVE_HDFO_PEON_DISABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
        checkoutPage.createNativeOrder(orderDTO, true);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Retry Success Native HDFC Direct transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
    public void Native_Retry_DirectChannel(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HDFO).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HDFO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancelAtOtp(PaymentDTO.bankOtp);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Issue("PGP-14507")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify process txn should be successful,when txn is initiated using invalid DC and retried using correct CC", groups = Status.BUG)
    public void verifyRetryCase_InitiateDC_RetryUsingCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Hybrid_Retry)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.Hybrid_Retry, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.Hybrid_Retry, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify process txn should be successful, when txn is initiated using invalid DC and retried using correct Direct channel (HDFC direct)")
    public void verifyRetryCase_InitiateCC_RetryUsingDirectChannel(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HDFO)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HDFO, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");
        orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HDFO, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Issue("PGP-12900")
    @Test(description = "Verify process txn should be successful when txn intiated using NB and retried using CC", enabled = true)
    public void verifyRetryCase_TryCC_thenRetryIsDoneUsingCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String orderId = "retry" + CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID_RETRY)
                .setOrderId(orderId)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");
        orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Hybrid Native EMI transaction when (Txn amount> MIN_EMI), also validate EMI found as paymethod in fetchPayOption response. when SSo token is passed in request.")
    public void TC_PT_EMI_Hyb_Success(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        User user = userManager.getForWrite(Label.BASIC);
        Double amountToBeRetainedInWallet = 1.0;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_EMI)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "HDFC");
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.NATIVE_EMI, initTxnDTO.orderFromBody(), txnToken, paymentDTO,PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setPaymentFlow("HYBRID")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "EMI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(initTxnDTO.txnAmountFromBody()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS")
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
    @Test(description = "Do native transaction with Postpaid when 'Excluded channel info' flag is set true")
    public void TC_05(@Optional("false") Boolean isNativePlus) throws Exception {//TODO need - user with Postpaid; merchant with Postpaid
        User user = userManager.getForWrite(Label.POSTPAID);
        PostpaidHelpers.updateBalance("2");
        Constants.MerchantType mid = MerchantType.POSTPAIDANDUPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(mid, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        String payMode = "PAYTM_DIGITAL_CREDIT";
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Do native transaction with postpaid applied and first fetch balance and then do submit request")
    public void TC_06(@Optional("false") Boolean isNativePlus) throws Exception {//TODO need - user with Postpaid; merchant with Postpaid
        User user = userManager.getForWrite(Label.POSTPAID);
        Constants.MerchantType mid = MerchantType.POSTPAIDANDUPI;
        PostpaidHelpers.updateBalance("2");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(mid, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        String payMode = "PAYTM_DIGITAL_CREDIT";
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMode, "false");
        PostpaidHelpers.getBalance(orderDTO.getMID(), orderDTO.getORDER_ID(), txnToken);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Do native transaction before fetch balance and do submit request")
    public void nativeTxnWithFetchBalanceAfterSubmission(@Optional("true") Boolean isNativePlus) throws Exception {//TODO need - user with Postpaid; merchant with Postpaid
        User user = userManager.getForWrite(Label.POSTPAID);
        Constants.MerchantType mid = MerchantType.POSTPAIDANDUPI;
        PostpaidHelpers.updateBalance("2");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(mid, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setMpin(new PaymentDTO().getPasscode())
                .build();
        String payMode = "PAYTM_DIGITAL_CREDIT";
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        PostpaidHelpers.getBalance(orderDTO.getMID(), orderDTO.getORDER_ID(), txnToken);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "do hybrid transaction with native flow")
    public void TC_10(@Optional("false") Boolean isNativePlus) throws Exception {//TODO need - user with wallet and Postpaid; merchant with hybrid, wallet and Postpaid
        User user = userManager.getForWrite(Label.POSTPAIDONBOARDING);
        MerchantType merchantType = MerchantType.PPBLC_ONLY;
        Double txnAmount = 2.0;
        double insufficientWalletBalance = 1;
        PostpaidHelpers.updateBalance(String.valueOf(insufficientWalletBalance));
        WalletHelpers.modifyBalance(user, insufficientWalletBalance);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setMpin(new PaymentDTO().getPasscode())
                .setPaymentFlow("HYBRID")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(PostpaidHelpers.getBalance(orderDTO.getMID(), orderDTO.getORDER_ID(), txnToken)).isEqualTo(0);
    }

    @Test(description = "check that during check balance on demand then it show amount in rupee format only")
    public void TC_18() throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);//TODO need - user with Postpaid; merchant with Postpaid
        PostpaidHelpers.updateBalance("9000");
        String mId = MerchantType.PPBLYONLY.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PPBLYONLY).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Assertions.assertThat(PostpaidHelpers.getBalance(mId, initTxnDTO.orderFromBody(), txnToken)).isEqualTo(9000);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "do subwallet transaction with food wallet")
    public void TC_11(@Optional("true") Boolean isNativePlus) throws Exception { //TODO need - user with wallet, food wallet; merchant with wallet, food wallet
        User user = userManager.getForWrite(Label.FOODWALLET);
        MerchantType merchantType = MerchantType.FOOD_WALLET_PAYTMCC;
        WalletHelpers.modifyBalance(user, 20.0);
        WalletHelpers.updateFoodWalletBalance(user, 2);
        SubwalletAmount subwalletAmount = new SubwalletAmount();
        subwalletAmount.setFOOD("2");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSubwalletAmount(subwalletAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setExtendInfo(extendInfo)
                .setTxnValue("22")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "do subwallet with postpaid transaction")
    public void TC_12(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET, Label.POSTPAID);
        MerchantType merchantType = MerchantType.WalletOnly;
        WalletHelpers.modifyBalance(user, 10.0);
        PostpaidHelpers.updateBalance("2");
        WalletHelpers.updateFoodWalletBalance(user, 10);
        SubwalletAmount subwalletAmount = new SubwalletAmount();
        subwalletAmount.setFOOD("2");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSubwalletAmount(subwalletAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setExtendInfo(extendInfo)
                .setTxnValue("14")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setMpin(new PaymentDTO().getPasscode())
                .setPaymentFlow("HYBRID")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(8);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "do transaction with postpaid failed sceanrio")
    public void hybridPostpaidTxnFailWhenInsufficientAmount(@Optional("false") Boolean isNativePlus) throws Exception {//TODO need - user with wallet, food wallet and Postpaid; merchant with hybrid, wallet, food wallet and Postpaid
        User user = userManager.getForWrite(Label.FOODWALLET, Label.POSTPAID);
        MerchantType merchantType = MerchantType.FOOD_WALLET_PAYTMCC;
        WalletHelpers.modifyBalance(user, 10.0);
        PostpaidHelpers.updateBalance("2");
        WalletHelpers.updateFoodWalletBalance(user, 10);
        SubwalletAmount subwalletAmount = new SubwalletAmount();
        subwalletAmount.setFOOD("2");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSubwalletAmount(subwalletAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setExtendInfo(extendInfo)
                .setTxnValue("15")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setMpin(new PaymentDTO().getPasscode())
                .setPaymentFlow("HYBRID")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateRespMsg("TXN_FAILURE")
                .AssertAll();
    }

    @Test(description = "Verify expired card not found as savedInstruments in fetchPayOptions")
    public void Verify_expiredCardNotFoundInFetchPayInstrument() throws Exception {
        SaveCard saveCard = new SaveCard();
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        SavedCardHelpers.deleteSavedCard(user);
        //save credit card on user
        String cardId = savedCardHelpers.saveCardUserId(user.custId(), saveCard.AesEncCardNumCredit, saveCard.AesEncExp, new PaymentDTO().getCreditCardNumber()).getResponse().toString();
        SavedCardHelpers.validateSaveCardDB_ByCardID(cardId);
        //change save card expiry date
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId,user);
        //save debit card on user
        String cardId1 = savedCardHelpers.saveCardUserId(user.custId(), saveCard.AesEncCardNumDebit, saveCard.AesEncExp, new PaymentDTO().getDebitCardNumber()).getResponse().toString();
        SavedCardHelpers.validateSaveCardDB_ByCardID(cardId1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getMerchantPayOption().getSavedInstruments().get(0).getCardDetails().getCardId())
                .as("Only not expired Saved card should be present saved instrument").isEqualTo(cardId1);

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Check card-index-no present in process transaction API response when card-token-required param is set true")
    public void checkCardIndexNoPresentInProcessTransactionApiResponseWhenCardTokenRequiredParamIsSetTrue(@Optional("false") Boolean isNativePlus) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID)
                .setCardTokenRequired("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "471865");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.pause(1);//without pause native plus txn fails due to wait issues
        responsePage.waitUntilLoads();
        responsePage.validateCardIndexNo(Constants.ValidationType.NON_EMPTY);
    }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Check Native AddMoney Txn")
    public void checkNativeAddMoneyTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.AddMoney)
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.AddMoney, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "471865");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        Thread.sleep(5000);//For the time being

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                //.validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
               // .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify NativeAddMoney via CREDIT_CARD")
    public void Verify_NativeAddMoneyCC(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        double txnAmt = 1.0;
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.AddMoney;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).setREQUEST_TYPE("Add_Money")
                .build();/* Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");*/
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validatePaymentMode("CC")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, txnAmt);


    }

    @Issue("PGP-14970")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify NativeAddMoney via UPI")
    public void Verify_NativeAddMoneyUPI(@Optional("false") Boolean isNativePlus) throws Exception {
      //As Discussed , AddMoney is not supported with UPI Collect
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.AddMoney;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("UPI"));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 1);

    }

 //   @Parameters({"isNativePlus"})
 //   @Test(description = "Verify NativeAddMoney via NB for less than 2000 Rs", enabled = false)
    public void Verify_NativeAddMoneyNBLessThan2000Rs(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.AddMoney;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .setTxnValue("1999")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);

        List<PaymentModes> payModes  = fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes();


        for(PaymentModes mode : payModes)
        {
            Assertions.assertThat(mode.getPaymentMode()).isNotEqualTo("NET_BANKING").as("Net_Banking is getting displayed for less than 2000 Rs");
        }

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NativeAddMoney via NB for greater than 2000 Rs")
    public void Verify_NativeAddMoneyNBGreaterThan2000(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .setTxnValue("2001")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).setChannelCode("ICICI").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 2001);

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NativeAddMoney via Debit Card")
    public void Verify_NativeAddMoneyDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.AddMoney;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("Debit_Card"));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setChannelCode("WEB")
                .setAUTH_MODE("otp")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 1);

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NativeAddMoneyRetry via CREDIT_CARD")
    public void Verify_NativeAddMoneyRetry(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.ADD_MONEY_WITH_RETRY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));


        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        CheckoutPage checkoutPageretry = new CheckoutPage();
        OrderDTO orderDTO2 = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPageretry.pause(60);
        checkoutPageretry.createNativeOrder(orderDTO2, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus2 = new TxnStatus(orderDTO2.getMID(), orderDTO2.getORDER_ID());
        txnStatus2.executeUntilNotPending();
        txnStatus2.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO2.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 1);


    }

    @Test(description = "Verify NativeAddMoneyRetry via CREDIT_CARD")
    public void testRetriedAddMoneyOrderSuccessByCCWhenNonMatchingWebsiteProvided() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.ADD_MONEY_WITH_RETRY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));


        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);

        CheckoutPage checkoutPageretry = new CheckoutPage();
        OrderDTO orderDTO2 = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPageretry.pause(60);
        checkoutPageretry.createNativeOrder(orderDTO2, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus2 = new TxnStatus(orderDTO2.getMID(), orderDTO2.getORDER_ID());
        txnStatus2.executeUntilNotPending();
        txnStatus2.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO2.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 1);


    }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Verify NativeAddMoneyRetry max retry count breached")
    public void Verify_NativeAddMoneyRetryCountBreached(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        double txnAmt = 1.0;
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.ADD_MONEY_WITH_RETRY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        CheckoutPage checkoutPageRetry1 = new CheckoutPage();


        checkoutPageRetry1.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");


        OrderDTO orderDTO2 = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();


        CheckoutPage checkoutPageRetry2 = new CheckoutPage();
        checkoutPageRetry2.createNativeOrder(orderDTO2, isNativePlus);
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO2.getMID(), orderDTO2.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO2.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("HDFC")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validatePaymentMode("CC")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, txnAmt);


    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NativeAddMoney RBI limit exceeded")
    public void Verify_NativeAddMoneyRBILimitExceeded(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        double txnAmt = 11000.0;
        WalletHelpers.setZeroBalance(user);
        MerchantType merchantType = MerchantType.AddMoney;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true").setTxnValue("200000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(fetchPaymentOptResponse.getBody().getResultInfo().getResultCode().equals(Constants.ResponseCode.RBI_LIMIT_EXCEEDED));

    }

    @Test(description = "Check emi txn using Amex Credit Card")
    public void checkEmiTxnUsingAmexCreditCard(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        String txnAmt = "2";
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Hybrid)
                .setTxnValue(txnAmt)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "AMEX");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER).setCvvNumber("1111");
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, paymentDTO, payMethod)
                .setPlanId(emiPlanId)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnAmount(txnAmt)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("AMEX")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.AMEX.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success PGOnly Login Txn for Mutual funds Merchant via DC")
    public void validatePgOnlyWithLoginTxnForMutualFundsviaDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.MUTUAL_FUND).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.MUTUAL_FUND, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "444433");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success PGOnly without Login Txn for Mutual funds Merchant via Saved Card")
    public void validatePgOnlyWithOutLoginTxnForMutualFundsviaSavedCard(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.MUTUAL_FUND).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers savedcard = new SavedCardHelpers();
        String saveCardId = savedcard.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), user.custId(), MerchantType.MUTUAL_FUND.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear()).getResponse().toString();
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.MUTUAL_FUND, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "444433");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success PGOnly Login Txn for Mutual funds Merchant via Saved Card")
    public void validatePgOnlyWithLoginTxnForMutualFundsviaSavedCard(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.MUTUAL_FUND).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);

        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.MUTUAL_FUND, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "444433");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
//
//
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Invalid OTP retry on direct page")
    public void validateInvalidOTPRetryNative(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("888888");
        directBankOTPPage.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");
        directBankOTPPage.submitOtp("123456");
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIO")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("ICICI Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Resend OTP Link is Available")
    public void AvailabilityOfResendOTPLinkNative(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.ResendOTPLink().assertVisible();

    }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Go to Bank Site Link is Available")
    public void AvailabilityOfGotToBankSiteLinkNative(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.GoToBankWebsiteLink().assertVisible();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Resend OTP Using Resend Link After Entering Invalid OTP")
    public void ResendOTPUsingResendLinkAfterEnteringInvalidOTPNative(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.GoToBankWebsiteLink().assertVisible();
        directBankOTPPage.submitOtp("888888");
        directBankOTPPage.ResendOTPLink().assertVisible();
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate OTP required message if clicking on pay button without otp")
    public void OTPIsRequiredMessageIfPayedWithoutEnteringOTPNative(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("");
        directBankOTPPage.VerifyErrorMessage("OTP is required");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate After Last OTP resend limit Resend Button will disappear ")
    public void LastOTPResendButtonWillDisappearNative(@Optional("true") Boolean isNativePlus) throws Exception {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        directBankOTPPage.ResendOTPLink().assertNotVisible();


    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Count of Theia submit Retry Count if 1 will control the No of Retry attempt which on Direct Page ")
    public void ValidateTheiaSubmitRetryCountReflectsNumberOfRetryAttemptsNative(@Optional("true") Boolean isNativePlus) throws Exception {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("888888");
        directBankOTPPage.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");
        directBankOTPPage.submitOtp("888888");
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("ICIO")
                .validateRespCode("01")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateBankName("ICICI Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date());

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native HDFC Direct transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
    public void Native_Success_ReDirectChannel(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HDFO).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HDFO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


//       @Parameters({"isNativePlus"})
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify Success Native HDFC Direct transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
//    public void Native_Success_ReDirectChannel(@Optional("true") Boolean isNativePlus) throws Exception {
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HDFO).build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HDFO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
//        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
//        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//        directBankOTPPage.waitUntilLoads();
//        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
//        new ResponsePage().waitUntilLoads();
//        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(orderDTO.getORDER_ID())
//                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateGatewayName(Constants.Gateway.HDFO.toString())
//                .validateRespCode("01")
//                .validateRespMsg("Txn Successful.")
//                .validateBankName(Constants.Bank.HDFC.toString())
//                .validateMid(orderDTO.getMID())
//                .validatePaymentMode("CC")
//                .validateRefundAmnt("0.00")
//                .validateTxnDate(new Date())
//                .AssertAll();
//    }
//
//
       @Parameters({"isNativePlus"})
        @Test(description = "Validate bank logo on Direct Page ")
        public void VerifyBanklogoOnDirectPage(@Optional("true") Boolean isNativePlus) throws Exception {

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
            directBankOTPPage.BankLogo().assertVisible();

        }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Validate 6 digit OTP error message")
    public void SixDigitOTPErrorMsg(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.submitOtp("123");
        directBankOTPPage.pause(2);
        directBankOTPPage.VerifyErrorMessage("Please enter 6 digit OTP");
    }
//
//
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Duration of error message is 5 seconds")
    public void DurationOfErrorMsg(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.ICIO_CC_Enabled_Merchant).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.ICIO_CC_Enabled_Merchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.submitOtp("12311");
        directBankOTPPage.pause(6);
        directBankOTPPage.errorMessage().assertNotVisible();
    }
//
//    @Test(description = "validate Result Info For payment Retry Due To Insufficient Wallet Balance when Retry is present on merchant for Native Txn")
//    public void validateResultInfoForpaymentRetryDueToInsufficientWalletBalance() throws Exception {
//
//        User user = userManager.getForWrite(Label.LOGIN);
//        MerchantType merchantType=MerchantType.WALLETOnly_PCF;
//        Double txnAmount = 5.0;
//        WalletHelpers.setZeroBalance(user);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
//                 .setTxnValue(txnAmount.toString())
//                .build();
//
//        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
//        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
//                as("Txn token is not generated in initiate txn response").isNotEmpty();
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
//                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
//                .setPaymentMode("BALANCE")
//                .build();
//
//
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        Response response = processTransactionV1.execute();
//
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg"))
//                .isEqualTo("Wallet balance Insufficient");
//
//        Assertions.assertThat(response.jsonPath().getString("body.retryInfo.failureMessage"))
//                .as("Incorrect failure Message")
//                .isEqualTo("Balance Not Enough");
//
//        Assertions.assertThat(response.jsonPath().getString("body.retryInfo.blockerMessage"))
//                .as("Response Message is incorrect")
//                .isEqualTo("Your payment has been failed due to wallet Balance Insufficient");
//
//    }
//
//    @Test(description = "validate Result Info For payment Retry Due To Insufficient Wallet Balance when Retry is NOT present on merchant for Native Txn")
//    public void validateResultInfoForpaymentRetryDueToInsufficientWalletBalanceWhenRetryNotPresentMerchant() throws Exception {
//
//        User user = userManager.getForWrite(Label.LOGIN);
//        MerchantType merchantType=MerchantType.WalletOnly;
//        WalletHelpers.setZeroBalance(user);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
//                .build();
//
//        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
//        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
//                as("Txn token is not generated in initiate txn response").isNotEmpty();
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
//                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
//                .setPaymentMode("BALANCE")
//                .build();
//
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        Response response = processTransactionV1.execute();
//        Assert.assertFalse(response.getBody().asString().contains("retryInfo"));
//
//    }
//
//    @Epic(Constants.Sprint.SPRINT30_2)
//    @Feature("PGP-9042")
//    @Parameters({"isNativePlus"})
//    @Test(description = "validate Successful Txn With wallet Amount not equal to zero but less While Fetch Pay And Increased While PTC for Hybrid merchant")
//    public void validateSuccessfulTxnWithAmountlessWhileFetchPayAndIncreaseWhilePTCForHybridTxn(@Optional("false") Boolean isNativePlus) throws Exception {
//
//        User user = userManager.getForWrite(Label.BASIC);
//        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
//        Double txnAmount = 5.0;
//        WalletHelpers.modifyBalance(user, 3.00);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
//                .setTxnValue(txnAmount.toString())
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
//                .build();
//        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
//        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("HYBRID");
//        Assertions.assertThat(path.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo("3.00");
//
//        WalletHelpers.modifyBalance(user, 6.00);
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//        new ResponsePage().waitUntilLoads();
//        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(orderDTO.getORDER_ID())
//                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnType("SALE")
//                .validateGatewayName("WALLET")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Successful.")
//                .validateBankName("WALLET")
//                .validateMid(orderDTO.getMID())
//                .validatePaymentMode("PPI")
//                .validateRefundAmnt("0.00")
//                .validateTxnDate(new Date())
//                .AssertAll();
//        WalletHelpers.validateBalance(user, 1.00);
//    }
//
    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Successful PPI txn when amount is 0 at fetch pay option and sufficient while doing PTC request")
    public void validateAmountZeroAtFetchPayAndSufficientWhilePTC(@Optional("false") Boolean isNativePlus) throws Exception {
        Double txnAmount = 2.15;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_WALLET_ONLY)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_WALLET_ONLY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        //Assertions.assertThat(path.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo("0.00");
        WalletHelpers.modifyBalance(user, txnAmount);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay merchant success transaction with zero amount in wallet while fetchpayoption and sufficient while doing PTC txn.")
    public void validateAmountZeroAtFetchPayAndSufficientWhilePTCForADDNPAY(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        //Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
       // Assertions.assertThat(path.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo("0.00");

        WalletHelpers.modifyBalance(user, txnAmount);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0);
    }
//
    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify PPI failure transaction with sufficient amount at fetch pay option and insufficient amount while doing PTC")
    public void validateSufficientAmountAtFetchPayAndInSufficientWhilePTCForPPI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.NATIVE_WALLET_ONLY;
        Double txnAmount = 7.0;
        WalletHelpers.modifyBalance(user, 3.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
       // Assertions.assertThat(path.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo("3.00");

        WalletHelpers.modifyBalance(user, 5.00);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("PPI")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateCheckSum(MerchantType.NATIVE_WALLET_ONLY.getKey())
                .assertAll();
        WalletHelpers.validateBalance(user, 5.0);
    }

//
    @Epic(Constants.Sprint.SPRINT30_2)
    @Feature("PGP-9042")
    @Parameters({"isNativePlus"})
    @Test(description = "validate Successful Txn With Amount not equal to zero but less While Fetch Pay And Increased While PTC")
    public void
    validateSuccessfulTxnWithAmountlessWhileFetchPayAndIncreaseWhilePTC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.WalletOnly;
        Double txnAmount = 5.0;
        WalletHelpers.modifyBalance(user, 3.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo("3.00");

        WalletHelpers.modifyBalance(user, 6.00);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 1.00);

    }
//
    @Epic(Constants.Sprint.SPRINT32_2)
    @Story("PGP-21561")
    @Parameters({"isNativePlus"})
    @Test(description = "test txn amt in callback and txn status has 2 decimal precision for CC txn")
    public void testTxnAmtInCallbackAndTxnStatusHas2DecimalPrecisionForCCTxn(@Optional("false") Boolean isNativePlus) {
        String txnAmt = "100";
        String formattedTxnAmt = new DecimalFormat("0.00").format(Double.parseDouble(txnAmt));
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmt)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN), PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        pageWait.apply(responsePage.hasLoaded());
        Assertions.assertThat(responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.TXNAMOUNT).getValue()).isEqualTo(formattedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.getTXNAMOUNT()).isEqualTo(formattedTxnAmt);
    }

    @Epic(Constants.Sprint.SPRINT32_2)
    @Story("PGP-21561")
    @Parameters({"isNativePlus"})
    @Test(description = "test txn amt in callback and txn status has 2 decimal precision for PPI txn")
    public void testTxnAmtInCallbackAndTxnStatusHas2DecimalPrecisionForPPITxn(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String txnAmt = "100";
        String formattedTxnAmt = new DecimalFormat("0.00").format(Double.parseDouble(txnAmt));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_WALLET_ONLY)
                .setTxnValue(txnAmt)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_WALLET_ONLY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.getBody().getTxnAmount().getValue()) - 1);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        pageWait.apply(responsePage.hasLoaded());
        Assertions.assertThat(responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.TXNAMOUNT).getValue()).isEqualTo(formattedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        Assertions.assertThat(txnStatus.txnStatusResponse.getTXNAMOUNT()).isEqualTo(formattedTxnAmt);
    }
//
    @Epic(Constants.Sprint.SPRINT32_2)
    @Story("PGP-21561")
    @Parameters({"isNativePlus"})
    @Test(description = "test txn amt in callback and txn status has 2 decimal precision for addnpay txn")
    public void testTxnAmtInCallbackAndTxnStatusHas2DecimalPrecisionForAddnPayTxn(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String txnAmt = "100";
        String formattedTxnAmt = new DecimalFormat("0.00").format(Double.parseDouble(txnAmt));
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmt)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN), PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.getBody().getTxnAmount().getValue()) - 1);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        pageWait.apply(responsePage.hasLoaded());
        Assertions.assertThat(responsePage.get(com.paytm.pages.responsePage.ResponsePage.Attribute.TXNAMOUNT).getValue()).isEqualTo(formattedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        Assertions.assertThat(txnStatus.txnStatusResponse.getTXNAMOUNT()).isEqualTo(formattedTxnAmt);
    }
//
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify DC Transaction failure for Mutual Funds")
//    public void TC_PT_DC_Failure_MutualFunds(@Optional("false") Boolean isNativePlus) throws Exception {
//        MerchantType merchantType = MerchantType.MUTUAL_FUND;
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
//                .setRequestType("NATIVE_MF")
//                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .setValidateAccountNumber("true")
//                .setAllowUnverifiedAccount("false")
//                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .setTxnValue("99.98")
//                .build();
//        System.out.println(initTxnDTO);
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
//        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
//        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
//                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .build();
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.waitUntilLoads();
//        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateCurrency("INR")
//                .validateMid(orderDTO.getMID())
//                .validateOrderId(orderDTO.getORDER_ID())
//                .validatePaymentMode("DC")
//                .validateRespCode("227")
//                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
//                .validateStatus("TXN_FAILURE")
//                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
//                .validateTxnDate(new Date())
//                .validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateGatewayName(Constants.Gateway.HDFC.toString())
//                .validateBankName(Constants.Bank.HDFC.toString())
//                .validateCheckSum(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .validateResponsePageParameters()
//                .assertAll();
//        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
//        txnStatus.executeUntilNotPending();
//        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateOrderid(orderDTO.getORDER_ID())
//                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
//                .validateStatus("TXN_FAILURE")
//                .validateMid(orderDTO.getMID())
//                .validatePaymentMode("DC")
//                .validateTxnDate(new Date())
//                .validateRespCode("227")
//                .validateGatewayName(Constants.Gateway.HDFC.toString())
//                .validateBankName(Constants.Bank.HDFC.toString())
//                .AssertAll();
//    }
//
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify DC Transaction pending for Mutual Funds")
//    public void TC_PT_DC_Pending_MutualFunds(@Optional("false") Boolean isNativePlus) throws Exception {
//        MerchantType merchantType = MerchantType.MUTUAL_FUND;
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
//                .setRequestType("NATIVE_MF")
//                .setMid(MerchantType.MUTUAL_FUND.getId())
//                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .setValidateAccountNumber("true")
//                .setAllowUnverifiedAccount("false")
//                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .setTxnValue("99.84")
//                .build();
//        System.out.println(initTxnDTO);
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
//                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .build();
//
//        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
//        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.waitUntilLoads();
//        responsePage.validateCurrency("INR")
//                .validateMid(orderDTO.getMID())
//                .validateOrderId(orderDTO.getORDER_ID())
//                .validatePaymentMode("DC")
//                .validateRespCode("402")
//                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
//                .validateStatus("PENDING")
//                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
//                .validateTxnDate(new Date())
//                .validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateBankName(Constants.Bank.HDFC.toString())
//                .validateCheckSum(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .assertAll();
//        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
//        txnStatus.execute();
//        txnStatus.validateOrderid(orderDTO.getORDER_ID())
//                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
//                .validateStatus("PENDING")
//                .validateMid(orderDTO.getMID())
//                .validateTxnDate(new Date())
//                .validateRespCode("402")
//                .AssertAll();
//    }
//
//
    @Epic(Constants.Sprint.SPRINT31_2)
    @Feature("PGP-20330")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate when ORDER is created through initiate txn using ACQUIRING_CREATE_ORDER" +
            "and the transaction key is expired from session redis using same order txn is allowed without invoking duplicate OrderID ")
    public void validateSuccessfulTxnWithSameOrderWhenTxnkeyRemovedFromSessionRedis(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.0);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        String OrderID = CommonHelpers.generateOrderId();

        InitTxnDTO initTxnDTO_Req1 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(OrderID)
                .build();

        NativeHelpers.Validate_InitTxn(initTxnDTO_Req1);

        RedisUtil.getInstance()
                .getConnection(LocalConfig.SESSION_REDIS_URI)
                .del("NativeTxnInitiateRequest"+merchantType.getId()+"_"+OrderID);



        InitTxnDTO initTxnDTO_Req2   = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(OrderID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_Req2);


        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO_Req2.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .setORDER_ID(OrderID)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
    }

//
//
//
    @Epic(Constants.Sprint.SPRINT31_2)
    @Feature("PGP-20330")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate when ORDER is created through initiate txn using ACQUIRING_CREATE_ORDER" +
            "and the transaction key is expired from session redis using same order txn is allowed " +
            "with the Retry without invoking duplicate OrderID ")
    public void validateSuccessfulTxnWithSameOrderWhenKeyIsRemovedTwiceandWithRetry(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID_RETRY;
        String OrderID = CommonHelpers.generateOrderId();

        InitTxnDTO initTxnDTO_Req1 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(OrderID)
                .build();

        NativeHelpers.Validate_InitTxn(initTxnDTO_Req1);

        RedisUtil.getInstance()
                .getConnection(LocalConfig.SESSION_REDIS_URI)
                .del("NativeTxnInitiateRequest"+merchantType.getId()+"_"+OrderID);


        InitTxnDTO initTxnDTO_Req2 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(OrderID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_Req2);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO_Req2.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");


        OrderDTO retriedOrder = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO_Req2.orderFromBody(),
                txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(retriedOrder, isNativePlus);


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(retriedOrder.getMID(), retriedOrder.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .AssertAll();
    }
//
//
//
    @Epic(Constants.Sprint.SPRINT31_2)
    @Feature("PGP-20330")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate when ORDER is created through initiate txn using ACQUIRING_CREATE_ORDER" +
            "and the transaction key is NOT expired from session redis then using same order txn is NOT allowed")
    public void validateDuplicateOrderErrorWithSameRequest(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        String OrderID = CommonHelpers.generateOrderId();

        InitTxnDTO initTxnDTO_Req1 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(OrderID)
                .build();
        NativeHelpers.Validate_InitTxn(initTxnDTO_Req1);


        InitTxnDTO initTxnDTO_Req2 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(OrderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO_Req2);

        Response response = initTxn.execute();
        String resultStatus = response.jsonPath().get("body.resultInfo.resultMsg").toString();

        Assertions.assertThat(resultStatus)
                .as("Not getting error for repeat request").isEqualTo("Repeat Request Inconsistent");


    }
//
//
//
//
//
//
    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20549")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success txn when init api contains % symbol in Shipping and Goods Info")
    public void succesfulOrderwhenApercentagesSignInInit(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();

        ShippingInfo shippingInfo = new ShippingInfo();
        shippingInfo.setAddress1("137WSanBer%nard%ino");

        Good goodInfo = new Good();
        goodInfo.setDescription("Women%Sum%mer%Dress");

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        initTxnDTO.getBody().setShippingInfo(new ShippingInfo[]{shippingInfo});
         String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setWEBSITE("nonmatchingwebsite")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "471865");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20549")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify success of transaction for Promo Flow when % sign is passed in  shipping and goods info ")
    public void succesfulOrderwhenApercentagesSignInInitForPromoFlow(@Optional("true") Boolean isNativePlus) {

        MerchantType merchantType = MerchantType.NATIVE_PROMO_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setPromoCode(Constants.promoCode.CC_PROMO.toString())
                .build();

        ShippingInfo shippingInfo = new ShippingInfo();
        shippingInfo.setAddress1("137WSanBer%nard%ino");

        Good goodInfo = new Good();
        goodInfo.setDescription("Women%Sum%mer%Dress");

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        initTxnDTO.getBody().setShippingInfo(new ShippingInfo[]{shippingInfo});
        String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);



        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        Assertions.assertThat(initTxnResponse.getBody().isPromoCodeValid())
                .as("Promocode is marked as invalid")
                .isEqualTo(true);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptResponseDTO.set(FetchPaymentOption.executeFetchPaymtOption(
                merchantType.getId(), orderId, fetchPaymentOptionsDTO));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        Assertions.assertThat(responsePage.textTxnID().getText()).isNotEmpty();
        validateSuccessPromo(fetchPaymentOptResponseDTO.get(), Constants.promoCode.CC_PROMO);
    }


    @Story("PGP-13158")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify VPA return incase of Successful UPI " +
            "when RETURN_USER_VPA_IN_RESPONSE = Y in response page , merchant status and peon for OFFUS Merchant")

    public void verifyReturnVPAforSuccessfulNativeTxnOFFUSMerchant(@Optional("false") Boolean isNativePlus) {


        MerchantType merchantType = MerchantType.PPBLC_ONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateVPA("test@paytm")
                .validateCheckSum(MerchantType.PPBLC_ONLY.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateVPA("test@paytm")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","VPA","PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.vpa().equals("test@paytm")
        );
        sAssert.eval();
    }

//
    @Parameters({"isNativePlus"})
    @Test(description = "To verify VPA return incase of Failure UPI " +
            "when RETURN_USER_VPA_IN_RESPONSE = Y in response page , merchant status and peon for OFFUS Merchant")
    public void verifyReturnVPAforFailedTxnOFFUSMerchant(@Optional("false") Boolean isNativePlus) {



        MerchantType merchantType = MerchantType.PPBLC_ONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateVPA("test@paytm")
                .validateCheckSum(MerchantType.PPBLC_ONLY.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateVPA("test@paytm")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","VPA","PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("227"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.vpa().equals("test@paytm")

        );
        sAssert.eval();
    }

//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify EMI failure when ICICI CC is passed for HDFC CC EMI offline transaction")
//    public void EMI_Native_Bank_failure(@Optional("false") Boolean isNativePlus) throws Exception {
//        String payMethod = "EMI";
//        User user = userManager.getForWrite(Label.BASIC);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_EMI)
//                .setTxnValue("2")
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
//        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "HDFC");
//        OrderDTO orderDTO = new OrderFactory.
//                Native(MerchantType.NATIVE_EMI, initTxnDTO.orderFromBody(), txnToken, payMethod)
//                .setPlanId(emiPlanId)
//                .setCardInfo("4375512810232002|07|2023")
//                .build();
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//        //new ResponsePage().waitUntilLoads();
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.waitUntilLoads();
//        responsePage.validateCurrency("INR")
//                .validateMid(orderDTO.getMID())
//                .validateOrderId(orderDTO.getORDER_ID())
//                .validateRespCode("1001")
//                .validateRespMsg("Request parameters are not valid")
//                .validateStatus("TXN_FAILURE")
//                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
//                .validateCheckSum(MerchantType.NATIVE_EMI.getKey())
//                .assertAll();
//    }
//
    @Epic(Constants.Sprint.SPRINT34_1)
    @Feature("PGP-24487")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when there is special character in custId transaction got successful for logged in user")
    public void SpecialCharCustIdTxnSuccessfulForloggedInUser(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PGOnly)
                .setTxnValue("2")
                .setCustId("Test^==")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();
    }
//
    @Epic(Constants.Sprint.SPRINT34_1)
    @Feature("PGP-24487")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when there is special character in custId transaction got successful for non logged in user")
    public void SpecialCharCustIdTxnSuccessfulForNonloggedInUser(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", MerchantType.PGOnly)
                .setTxnValue("2")
                .setCustId("Test^==")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();
    }


    //With Login

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the NB txn when NB is sent as disablePayment Mode in initate txn API ")
    public void disablePayModeNBInInit() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForRead(Label.BASIC);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("NB")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .assertAll();

    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the PPBL NB txn when NB is sent as disablePayment Mode in initate txn API ")
    public void disablePayModePPBLNBInInit() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForRead(Label.PPBL);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("PPBL")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("PPBL")
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .assertAll();

    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the txn from other paymode when NB is sent as disablePayment Mode in initiate txn API ")
    public void disablePayModeNBInInitButSuccessTxnFromPaytmCC() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForRead(Label.POSTPAID);
        PostpaidHelpers.updateBalance("1000");
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("NB")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("Paytm Postpaid")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(nativeMerchant.getId(),initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("Paytm Postpaid")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the txn from NB paymode when PPBL is sent as disablePayment Mode in initiate txn API ")
    public void disablePayModePPBLInInitButSuccessTxnFromNB() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForRead(Label.BASIC);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("PPBL")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(nativeMerchant.getId(),initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the NB txn when NB is sent as enablePayment Mode in initiate txn API ")
    public void enablePayModeAsNB() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForRead(Label.BASIC);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("NB")})
                .setOrderId(OrderId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(nativeMerchant.getId(),initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the PPBL txn when PPBL is sent as enablePayment Mode in initiate txn API ")
    public void enablePayModeAsPPBL() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user = userManager.getForRead(Label.PPBL);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("PPBL")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("PPBL")
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(nativeMerchant.getId(),initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the PAYTMCC txn when NB is sent as enablePayment Mode in initate txn API ")
    public void enablePayModeNBSuccessPaytmPostpaid() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user =userManager.getForWrite(Label.POSTPAID);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("NB")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .assertAll();

    }


    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the postpaid txn when PPBL NB is sent as enablePayment Mode in initate txn API ")
    public void enablePayModePPBLNBSuccessPaytmPostpaid() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user =userManager.getForWrite(Label.POSTPAID);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("PPBL")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .assertAll();

    }


//    //Without Login
//
    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the NB txn when NB is sent as disablePayment Mode in initate txn API ")
    public void disablePayModeNBInInitWithoutLogin() throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("NB")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .assertAll();

    }
//
    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the CC txn when NB is sent as disablePayment Mode in initate txn API ")
    public void disablePayModeNBInInitCCTxnWithoutLogin() {
        PaymentDTO paymentDTO = new PaymentDTO();
        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("NB")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .assertAll();


    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the NB txn when NB is sent as enabledPayMode in initate txn API ")
    public void enableNbSuccessWithNB() {
        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("NB")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(nativeMerchant.getId(),initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(nativeMerchant.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the NB txn when NB is sent as enabledPayMode in initate txn API ")
    public void enableNbTxtWithCC() {
        PaymentDTO paymentDTO = new PaymentDTO();
        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("NB")})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(nativeMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(nativeMerchant.getId())
                .assertAll();

    }

    //Native Flow using SCW merchant

    @Override
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCWalletCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType merchantType = MerchantType.AddMoneyMP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCWalletSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmt),"MAIN","","","CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Premium");

        MerchantType merchantType = MerchantType.AddMoneyMP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmt.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmt.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 111.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.NATIVE_ADDNPAY;
        String orderId = CommonHelpers.generateOrderId();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validatePaymentMode("CC")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;
        String orderId = CommonHelpers.generateOrderId();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant, paymentDTO.getCreditCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin + "||" + paymentDTO.getCvvNumber() + "|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 111.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;
        String orderId = CommonHelpers.generateOrderId();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant, paymentDTO.getCreditCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin + "||" + paymentDTO.getCvvNumber() + "|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        KYCPage kycPage = new KYCPage();
        Assert.assertTrue(kycPage.submitBtnNew().isElementPresent(), "KYC page is not getting opened for : " + user.mobNo());
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;
        String orderId = CommonHelpers.generateOrderId();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant, paymentDTO.getCreditCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin + "||" + paymentDTO.getCvvNumber() + "|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        Double txnAmount = 111.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.NATIVE_ADDNPAY;
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), initTxnDTO.getBody().getOrderId());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                //cardHash field is not coming on UI (Need to be analysed)
                .AssertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        String orderId = CommonHelpers.generateOrderId();

        MerchantType scwMerchant = MerchantType.AddMoneyMP;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant, paymentDTO.getDebitCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getDebitCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmt.toString())
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("DEBIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setCardInfo(cin + "||" + paymentDTO.getCvvNumber() + "|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFCSC.toString())
                .validatePaymentMode("DC")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        Double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Response response = WalletHelpers.checkWalletLimit(user, String.valueOf(txnAmt), "MAIN", "", "", "NET_BANKING");
        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Min Kyc Expired");

        MerchantType scwMerchant = MerchantType.AddMoneyMP;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmt.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("NET_BANKING"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validatePaymentMode("NB")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        String orderId = CommonHelpers.generateOrderId();

        MerchantType scwMerchant = MerchantType.AddMoneyMP;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant, paymentDTO.getDebitCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getDebitCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmt.toString())
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("DEBIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setCardInfo(cin + "||" + paymentDTO.getCvvNumber() + "|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validatePaymentMode("DC")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

    }


    //New APP New Flow

    @Test(description = "New App New Flow : To validate when full KYC user adds money to wallet")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCWalletNewFlow() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        String orderId = CommonHelpers.generateOrderId();
        Response response = WalletHelpers.checkWalletLimit(user, String.valueOf(txnAmt), "MAIN", "", "", "CREDIT_CARD");
        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Premium");

        MerchantType scwMerchant = MerchantType.AddMoneyMP;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant, paymentDTO.getCreditCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();


        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmt.toString())
                .setCardHash(cardHash)
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin + "||" + paymentDTO.getCvvNumber() + "|") // Txn through CIN
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFCSC.toString())
                .validatePaymentMode("CC")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

    }
//
//
    @Test(description = "New App New Flow : To validate when full KYC user adds money to wallet via saved CC")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCSavedCardWalletNewFlow() throws Exception {

        MerchantType scwMerchant = MerchantType.AddMoneyMP;
        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        String orderId = CommonHelpers.generateOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmt.toString())
                .setOrderId(orderId)
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validatePaymentMode("CC")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test(description = "New App New Flow : To test when full kyc user tries to add money through GV")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCGVNewFlow() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Double txnAmount = 10001.0;
        WalletHelpers.setZeroBalance(user);

        MerchantType scwMerchant = MerchantType.AddMoneyMP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = Validate_FetchPayInstrument(txnToken, initTxnDTO);
        Assertions.assertThat(new ArrayList<>(fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes()).contains("CREDIT_CARD"));
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), initTxnDTO.orderFromBody());         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }
//
    @Test(description = "Verify SBI VISA Credit card Not Allowed Message when only HDFC DC,CC paymodes are enabled.")
    public void verifySBICreditCardNotAllowedMsg() {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String[] banks = {"HDFC"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBanks(banks)
                                , new EnablePaymentMode().setMode("DEBIT_CARD").setBanks(banks)})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "486461").build(); //SBI CC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("SBI VISA Credit card is not allowed for this payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNull();
    }
//
    @Test(description = "Verify HDFC CC Paymode Allowed when only HDFC DC,CC paymodes are enabled.")
    public void verifyHDFCCreditCardAllowedwhenHDFC_DC_CC_Enabled() {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String[] banks = {"HDFC"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBanks(banks)
                                , new EnablePaymentMode().setMode("DEBIT_CARD").setBanks(banks)})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build(); //HDFC CC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
    }
//
    @Test(description = "Verify HDFC DC Paymode not Allowed when only HDFC CC paymode is enabled.")
    public void verifyHDFCDebitNotAllowedwhenOnlyHDFC_CC_Enabled() {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String[] banks = {"HDFC"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBanks(banks)})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument2(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "532571").build(); //HDFC DC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).contains("Debit card is not allowed for this payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNull();
    }
//
    @Test(description = "Verify HDFC CC Paymode Allowed when SBI,ICICI DC with CC for All Banks are enabled.")
    public void verifyHDFCCreditCardAllowedwithSBI_ICICI_DCwithCCAllEnabled() {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String[] banks = {"ICICI", "SBI"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("DEBIT_CARD").setBanks(banks)
                                , new EnablePaymentMode().setMode("CREDIT_CARD")})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build(); //HDFC CC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
    }
//
    @Test(description = "Verify HDFC DC Paymode Not Allowed when SBI,ICICI DC with CC for All Banks are enabled.")
    public void verifyHDFCDebitCardNotAllowedwithSBI_ICICI_DCwithCCAllEnabled() {
        MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String[] banks = {"ICICI", "SBI"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("DEBIT_CARD").setBanks(banks)
                                , new EnablePaymentMode().setMode("CREDIT_CARD")})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "532571").build(); //HDFC DC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Debit card is not allowed for this payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNull();
    }

    @Test(description = "Verify HDFC DC Paymode Not Allowed when SBI,ICICI DC with Wallet enabled for AddnPay Txn.")
    public void verifyHDFCDebitCardNotAllowedwithSBI_ICICI_DCwithWalletAddNPay() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.AddnPay;
        String[] banks = {"ICICI", "SBI"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("DEBIT_CARD").setBanks(banks)
                                , new EnablePaymentMode().setMode("BALANCE")})
                .setTxnValue("10")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "532571").build(); //HDFC DC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Debit card is not allowed for this payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNull();
    }

    @Test(description = "Verify HDFC DC Paymode Not Allowed when SBI,ICICI DC with Wallet enabled for Hybrid Txn.")
    public void verifyHDFCDebitCardNotAllowedwithSBI_ICICI_DCwithWalletHybrid() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        String[] banks = {"ICICI", "SBI"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {
                                new EnablePaymentMode().setMode("DEBIT_CARD").setBanks(banks)
                                , new EnablePaymentMode().setMode("BALANCE")})
                .setTxnValue("10")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        //Using HDFC VISA DC
        String bin = PaymentDTO.DEBIT_CARD_NUMBER.substring(0,6);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, bin).build(); //HDFC DC Bin
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("HDFC VISA Debit card is not allowed for this payment. Please try paying using other cards/options.");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNull();
    }

//    @Owner("Tarun")
//    @Feature("PGP-24851")
//    @Description("Automation JIRA : PGP-25565")
//    @Test(description = "To verify splitSettlementInfo in responsePage for failed NATIVE_MF txn when flag theia.addEscapeCharacterInFinalRespone is ON")
//    public void verifyMFSplitSettlementFailureWithEscape() throws Exception {
//        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
//        User user = userManager.getForWrite(Label.BASIC);
//        MerchantType merchantType = MerchantType.MUTUAL_FUND;
//        String txnAmount = "10";
//
//        SplitInfo splitInfo = new SplitInfo().setMid(merchantType.getId()).setAmount(new Amount().setValue(txnAmount).setPercentage(""));
//        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});
//
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
//                .setTxnValue(txnAmount)
//                .setRequestType("NATIVE_MF")
//                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .setSplitSettlementInfo(splitSettlementInfo)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//
//        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
//                .setTXN_AMOUNT(txnAmount)
//                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .build();
//        checkoutPage.createNativeOrder(orderDTO, false);
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.waitUntilLoads();
//        responsePage.validateSplitSettlementInfoWithEscape().assertAll();
//
//    }

//    @Owner("Tarun")
//    @Feature("PGP-24851")
//    @Description("Automation JIRA : PGP-25565")
//    //theia.addEscapeCharacterInFinalRespone FF4j flag is Enabled on Prod
//    @Test(enabled = false, priority = 1, description = "To verify splitSettlementInfo in responsePage for failed NATIVE_MF txn when flag theia.addEscapeCharacterInFinalRespone is OFF")
//    public void verifyNativeSplitSettlementWithoutEscape() throws Exception {
//        FF4JFlags.disable("theia.addEscapeCharacterInFinalRespone");
//        User user = userManager.getForWrite(Label.BASIC);
//        MerchantType merchantType = MerchantType.MUTUAL_FUND;
//        String txnAmount = "10";
//
//        SplitInfo splitInfo = new SplitInfo().setMid(merchantType.getId()).setAmount(new Amount().setValue("2").setPercentage(""));
//        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});
//
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
//                .setTxnValue(txnAmount)
//                .setRequestType("NATIVE_MF")
//                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .setSplitSettlementInfo(splitSettlementInfo)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//
//        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
//                .setTXN_AMOUNT(txnAmount)
//                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .build();
//        checkoutPage.createNativeOrder(orderDTO, false);
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.waitUntilLoads();
//        responsePage.validateSplitSettlementInfoWithoutEscape().assertAll();
//
//    }
//
    @Feature("PGP-20695")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-25565")
    @Test(description = "Native ADDANDPAY: To verify wallet is disabled, fpo should contain BALANCE & addMoney payOptions and txn should get success with other paymode for add n pay txn when wallet limit is breached")
    public void verifyAddNPayWalletLimitBreachedWithCOP(@Optional("false") Boolean isNativePlus) throws Exception {
        FF4JFlags.enable("createOrderinIntTxn");
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        Double balance = WalletHelpers.getWalletBalance(user);

        Double txnAmount = 100001.0;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        Assertions.assertThat(path.getString("body.addMoneyPayOption.paymentModes")).isEqualTo(null);
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success") //Payment should be success with other payMode
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        WalletHelpers.validateBalance(user, balance);
    }


//    @Feature("PGP-20695") //createOrderinIntTxn flag is ON on Prod
//    @Owner("Tarun")
//    @Description("Automation JIRA : PGP-25565")
//    @Test(enabled = false, priority = 1, description = "Native ADDANDPAY: To verify wallet is disabled, fpo should contain BALANCE & addMoney payOptions and txn should get success with other paymode for add n pay txn when wallet limit is breached")
    public void verifyAddNPayWalletLimitBreachedNativeWithoutCOP(@Optional("false") Boolean isNativePlus) throws Exception {
        FF4JFlags.disable("createOrderinIntTxn");

        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        Double balance = WalletHelpers.getWalletBalance(user);

        Double txnAmount = 100001.0;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        Assertions.assertThat(path.getString("body.addMoneyPayOption.paymentModes")).isEqualTo(null);
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success") //Payment should be success with other payMode
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        WalletHelpers.validateBalance(user, balance);

        //Enabling it again
        FF4JFlags.enable("createOrderinIntTxn");
    }

    @Feature("PGP-20695")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-25565")
    @Test(description = "Native ADDANDPAY: To verify addNPay flow should not be trigerred when main and GV both limits are breached")
    public void verifyGVWalletLimitBreachedAddNPay(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.AddnPay;
        Double balance = 10.0;
        WalletHelpers.modifyBalance(user, balance);
        Double txnAmount = 100022.0;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        Assertions.assertThat(path.getString("body.addMoneyPayOption.paymentModes")).isEqualTo(null);
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success") //Payment should be success with other payMode
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        WalletHelpers.validateBalance(user, balance);
    }

    @Feature("PGP-24778")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify success is coming in case of TRANS_PAID for CC transaction")
    public void validateTransPaidCC(@Optional("false") Boolean isNativePlus) {
        MerchantType hybridMerchant = MerchantType.PGOnly;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(hybridMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .build();

        String newTxnToken = NativeHelpers.Validate_InitTxn(duplicateInitTxnDTO);
        OrderDTO newOrderDTO = new OrderFactory.Native(hybridMerchant, duplicateInitTxnDTO.orderFromBody(), newTxnToken, PayMethodType.DEBIT_CARD).build();

        checkoutPage.createNativeOrder(newOrderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Feature("PGP-24778")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify success is coming in case of TRANS_PAID for DC transactio")
    public void validateTransPaidDC(@Optional("false") Boolean isNativePlus) {
        MerchantType hybridMerchant = Constants.MerchantType.NATIVE_HYBRID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(hybridMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .build();

        String newTxnToken = NativeHelpers.Validate_InitTxn(duplicateInitTxnDTO);
        OrderDTO newOrderDTO = new OrderFactory.Native(hybridMerchant, duplicateInitTxnDTO.orderFromBody(), newTxnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(newOrderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Feature("PGP-24778")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify success is coming in case of TRANS_PAID for NB transaction")
    public void validateTransPaidNB(@Optional("false") Boolean isNativePlus) {
        MerchantType hybridMerchant = Constants.MerchantType.NATIVE_HYBRID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(hybridMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .AssertAll();

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .build();

        String newTxnToken = NativeHelpers.Validate_InitTxn(duplicateInitTxnDTO);
        OrderDTO newOrderDTO = new OrderFactory.Native(hybridMerchant, duplicateInitTxnDTO.orderFromBody(), newTxnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(newOrderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Feature("PGP-24778")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify success is coming in case of TRANS_PAID for UPI Collect transaction")
    public void validateTransPaidUPI(@Optional("false") Boolean isNativePlus) {
        MerchantType hybridMerchant = Constants.MerchantType.NATIVE_HYBRID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(hybridMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .AssertAll();

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, hybridMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .build();

        String newTxnToken = NativeHelpers.Validate_InitTxn(duplicateInitTxnDTO);
        OrderDTO newOrderDTO = new OrderFactory.Native(hybridMerchant, duplicateInitTxnDTO.orderFromBody(), newTxnToken, PayMethodType.DEBIT_CARD).build();

        checkoutPage.createNativeOrder(newOrderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Feature("PGP-24778")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify success is coming in case of TRANS_PAID for WALLET transaction")
    public void validateTransPaidPPI(@Optional("false") Boolean isNativePlus) throws Exception {
        Double txnAmount = 2.0;
        Constants.MerchantType walletOnly = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), walletOnly)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(walletOnly, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), walletOnly)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .setTxnValue(txnAmount.toString())
                .build();

        String newTxnToken = NativeHelpers.Validate_InitTxn(duplicateInitTxnDTO);
        OrderDTO newOrderDTO = new OrderFactory.Native(walletOnly, duplicateInitTxnDTO.orderFromBody(), newTxnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(newOrderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

//
    @Feature("PGP-22222")
    @Description("Automation JIRA : PGP-26989")
    @Owner("Tarun")
    @Test(description = "Native : Verify the native initiate API with amount 1.00 and after the order is created again hit initiate the api with same order and amount 1.0")
    public void validateIdempotentCase1() {

        Constants.MerchantType nativeMerchant = Constants.MerchantType.NATIVE_HYBRID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setTxnValue("1.50")
                .build();

        NativeHelpers.Validate_InitTxn(initTxnDTO);

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .setTxnValue("1.5")
                .build();

        InitTxn initTxn = new InitTxn(duplicateInitTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0002");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success Idempotent");

    }

    @Feature("PGP-22222")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26989")
    @Test(description = "Native : Verify the native initiate API with amount 1.0 and after the order is created again hit initiate the api with same order and amount 1.00")
    public void validateIdempotentCase2() {

        Constants.MerchantType nativeMerchant = Constants.MerchantType.NATIVE_HYBRID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setTxnValue("1.0")
                .build();

        NativeHelpers.Validate_InitTxn(initTxnDTO);

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .setTxnValue("1.00")
                .build();

        InitTxn initTxn = new InitTxn(duplicateInitTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0002");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success Idempotent");

    }

    @Feature("PGP-22222")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26989")
    @Test(description = "Native : Verify the native initiate API with amount 1 and after the order is created again hit initiate the api with same order and amount 01")
    public void validateIdempotentCase3() {

        Constants.MerchantType nativeMerchant = Constants.MerchantType.NATIVE_HYBRID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setTxnValue("1")
                .build();

        NativeHelpers.Validate_InitTxn(initTxnDTO);

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .setTxnValue("01")
                .build();

        InitTxn initTxn = new InitTxn(duplicateInitTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0002");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success Idempotent");

    }


    @Feature("PGP-22222")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26989")
    @Test(description = "Native : Verify the native initiate API with amount 500.50 and after the order is created again hit initiate the api with same order and amount 500.5 and verify the e2e txn")
    public void validateIdempotentCase4(@Optional("false") Boolean isNativePlus) {

        Constants.MerchantType nativeMerchant = Constants.MerchantType.NATIVE_HYBRID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setTxnValue("500.50")
                .build();

        NativeHelpers.Validate_InitTxn(initTxnDTO);

        InitTxnDTO duplicateInitTxnDTO = new InitTxnDTO.Builder(null, nativeMerchant)
                .setOrderId(initTxnDTO.getBody().getOrderId())
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .setTxnValue("500.5")
                .build();

        String token = NativeHelpers.Validate_InitTxn(duplicateInitTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, duplicateInitTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();


    }


    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "Native : To verify success CC corporate bin for corporate merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCCCorporateBinCorporateMerchant(@Optional("true") Boolean isNativePlus) {

        MerchantType corporateCardOnly = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateCardOnly.getId());
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);

        // isCorporate should be true

        JsonPath binResponse = PrepaidHelpers.cardBinQuery(bin).jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("true");

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateCardOnly)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(corporateCardOnly, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateCardOnly, "CC", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "CC", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessNativeTxnStatus(orderDTO, "CC", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPaymentStatusAPI(orderDTO, corporateCardOnly, "CC", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "CC", Constants.Bank.HDFC.toString(), Constants.Gateway.HDFC.toString());

    }

    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "Native : To verify success DC corporate bin for corporate merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateDCBinCorporateMerchant(@Optional("true") Boolean isNativePlus) {

        MerchantType corporateCardOnly = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardDC(corporateCardOnly.getId());
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);

        // isCorporate should be true

        JsonPath binResponse = PrepaidHelpers.cardBinQuery(bin).jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("true");

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateCardOnly)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(corporateCardOnly, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateCardOnly, "DC", Constants.Bank.AXIS.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "DC", Constants.Bank.AXIS.toString());

        CorporateHelpers.validateSuccessNativeTxnStatus(orderDTO, "DC", Constants.Bank.AXIS.toString());

        CorporateHelpers.validateSuccessPaymentStatusAPI(orderDTO, corporateCardOnly, "DC", Constants.Bank.AXIS.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "DC", Constants.Bank.AXIS.toString(), Constants.Gateway.HDFC.toString());

    }


    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "Native : To verify success EMI corporate bin for corporate merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateEMIBinCorporateMerchant(@Optional("false") Boolean isNativePlus) {

        MerchantType corporateCardOnly = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateCardOnly.getId());
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);

        // isCorporate should be true

        JsonPath binResponse = PrepaidHelpers.cardBinQuery(bin).jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("true");

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, corporateCardOnly)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(corporateCardOnly, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setCHANNEL_ID("HDFC")
                .setEMI_TYPE("CREDIT_CARD")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateCardOnly, "EMI", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "EMI", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessNativeTxnStatus(orderDTO, "EMI", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPaymentStatusAPI(orderDTO, corporateCardOnly, "EMI", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "EMI", Constants.Bank.HDFC.toString(), Constants.Gateway.HDFC.toString());

    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Native : To verify success txn EMI corporate bin for corporate EMI only merchant")
    public void PGP_27715_validateOnlyEMIMerchantwithCorporateBin(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType corporateCardOnly = MerchantType.EMIOnly;
        CorporateHelpers.assertCorporateCardCC(corporateCardOnly.getId());
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), corporateCardOnly)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(corporateCardOnly, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setPlanId("HDFC|3")
                .setCHANNEL_ID("HDFC")
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateCardOnly, "EMI", Constants.Bank.HDFC.toString());
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Native : To verify success txn EMI corporate bin for corporate EMI DC only merchant")
    public void PGP_27715_validateOnlyEMIDCMerchantwithCorporateBin(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.EMIDC);
        MerchantType corporateCardOnly = MerchantType.EMIOnly_DC;
        CorporateHelpers.assertCorporateCardDC(corporateCardOnly.getId());
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.ICICI_CORPORATE_DEBIT_CARD_NUMBER);
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), corporateCardOnly)
                .setTxnValue(String.valueOf(txnAmount))
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("EMI")
                })
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(corporateCardOnly, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setPlanId("ICICI|3")
                .setCardInfo("|4731765131526259|618|122021")
                .setCHANNEL_ID("ICICI")
                .setEMI_TYPE("DEBIT_CARD")
                .build();

        //PTC
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI_DC")
                .validateBankName("ICICI Bank")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Issue("PGP-28365")
    @Test(description = "Verify that MERC_UNQ_REF is returned in HANDLER_INTERNAL/TXNSTATUS", groups = {BUG})
    public void PGP_27167_verifyMERC_UNQ_REFinTxnStatus() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT("10.00")
                .setMERC_UNQ_REF("Ref1234")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
       // responsePage.waitUntilLoads();
        responsePage.validateMERC_UNQ_REF("Ref1234")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateMERC_UNQ_REF("Ref1234")
                .AssertAll();
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getMERC_UNQ_REF(), "Ref1234");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), initTxnDTO.txnAmountFromBody());
        softAssert.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that transaction amount is updated and it is reduced w.r.t the original amount")
    public void verifyUpdateTxnApiTxnAmountDecreased(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 10;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new Head(txnToken))
                .setBody(new Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));
        updateTransactionDTO.setChecksum(merchantType.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateTxnAmount(String.valueOf(updatedTxnAmount))
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(String.valueOf(updatedTxnAmount))
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), CommonHelpers.doubleToTwoDigitAfterDecimalPoint(updatedTxnAmount));
        softAssert.assertAll();
    }
//
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that transaction amount is updated and it is increased w.r.t the original amount")
    public void verifyUpdateTxnApiTxnAmountIncreased(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 10;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new Head(txnToken))
                .setBody(new Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));
        updateTransactionDTO.setChecksum(merchantType.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateTxnAmount(String.valueOf(updatedTxnAmount))
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(String.valueOf(updatedTxnAmount))
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), CommonHelpers.doubleToTwoDigitAfterDecimalPoint(updatedTxnAmount));
        softAssert.assertAll();
    }

//
    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for CC transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForCC(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);
        String riskAmount = "5.5";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }

    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null with Length exceeded 100 characters for DC transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForDC(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);
        String riskAmount = "5.6";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for UPI transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForUPI(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);
        String riskAmount = "5.5";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("ICICI"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for HYBRID transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForHybrid(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.Hybrid;

        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 1.00);

        String riskAmount = "5.5";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "CHILDTXNLIST", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for ADDNPAY transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForADDNPAY(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.ADDNPAYPEON;

        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 1.00);

        String riskAmount = "5.5";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("WALLET"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("PPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null with Length exceeded 100 characters for PPBL transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForPPBL(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;

        User user = userManager.getForWrite(Label.PPBL);
        String riskAmount = "5.6";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderDTO.Builder()
                .setORDER_ID(initTxnDTO.orderFromBody())
                .setMID(nativeMerchant.getId())
                .setCHANNEL_ID("WEB")
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID("PPBL")
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT(riskAmount)
                .setMpin(new PaymentDTO().getPasscode())
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }

//
    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info null CC transaction with merchant status and Peon ")
    public void validateRiskInfoNullForCC(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);

        String riskAmount = "5.7";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRiskInfo("")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Feature("PGP-28971")
    @Owner(GAGANDEEP)
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Risk extended info having URL in theia facade in payment cashier pay request")
    public void validateRiskExtendedInfoWithURLs(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"| grep \"risk\"";

        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredAppURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredWebURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("callbackURL");
    }
//
//
    @Feature("PGP-28971")
    @Owner(GAGANDEEP)
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Risk extended info having URL in theia facade in payment cashier pay request with Risk amount")
    public void validateRiskExtendedInfoURLParamswithRiskAmount(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);
        String riskAmount = "5.5";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"| grep \"risk\"";

        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredAppURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredWebURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("callbackURL");

    }
//
//
    @Feature("PGP-28971")
    @Owner(GAGANDEEP)
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Risk extended info having URL in theia facade in payment cashier pay request with explicit callback URL")
    public void validateRiskExtendedInfoURLParamswithExplicitCallBackUrl(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType nativeMerchant = MerchantType.PGOnly;
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setCallbackUrl("https://pgp-automation1.paytm.in/pgp/checkoutpage/automation_theia_odisha.html")
                .build();
        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(nativeMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        Thread.sleep(3000);   //sleep due to response page is not there.
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"| grep \"risk\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredAppURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredWebURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("callbackURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("https://pgp-automation1.paytm.in/pgp/checkoutpage/automation_theia_odisha.html");
    }

    @Feature("PGP-28971")
    @Owner(GAGANDEEP)
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Risk extended info having URL in theia facade in payment cashier pay request with retry")
    public void verifyRiskExtendedInfoURLParamsWithRetry(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID_RETRY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"| grep \"risk\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredAppURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("registeredWebURL");
        Assertions.assertThat(theiaFacadeLogs).containsIgnoringCase("callbackURL");
    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-28416")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Validate sdkType AIO_SDK_PG param should be passed correctly in COP when passed in /ptc")
    public void validateSDKTypeAIO_SDK_PGPCP(@Optional("false") Boolean isNativePlus) throws InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String extendInfo = "{\"udf1\":\"vivek1\",\"sdkType\":\"AIO_SDK_PG\"}";

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setExtendInfo(extendInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\" | grep \"" + orderDTO.getORDER_ID() + "\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in COP request")
                .contains("sdkType", "AIO_SDK_PG");

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-28416")
    @Parameters({"isNativePlus"})
    @Test(description = "Native Plus : Validate sdkType AIO_SDK_PG param should be passed correctly in COP when passed in /ptc")
    public void validateSDKTypeAIO_SDK_PGPCPNativePlus() throws InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        com.paytm.dto.processTransactionV1.ExtendInfo extendInfo = new com.paytm.dto.processTransactionV1.ExtendInfo();
        extendInfo.setSdkType("AIO_SDK_PG");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfo(extendInfo)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(merchantType.getId())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .assertAll();

        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in COP request")
                .contains("sdkType", "AIO_SDK_PG");

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-28416")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Validate sdkType CUI_SDK_PG param should be passed correctly in COP when passed in /ptc")
    public void validateSDKTypeCUI_SDK_PGPCP(@Optional("true") Boolean isNativePlus) throws InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String extendInfo = "{\"udf1\":\"vivek1\",\"sdkType\":\"CUI_SDK_PG\"}";

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setExtendInfo(extendInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\" | grep \"" + orderDTO.getORDER_ID() + "\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in COP request")
                .contains("sdkType", "CUI_SDK_PG");

    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-28416")
    @Test(description = "Native Plus : Validate sdkType CUI_SDK_PG param should be passed correctly in COP when passed in /ptc")
    public void validateSDKTypeCUI_SDK_PGPCPNativePlus() throws InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        com.paytm.dto.processTransactionV1.ExtendInfo extendInfo = new com.paytm.dto.processTransactionV1.ExtendInfo();
        extendInfo.setSdkType("CUI_SDK_PG");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfo(extendInfo)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(merchantType.getId())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .assertAll();

        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in COP request")
                .contains("sdkType", "CUI_SDK_PG");

    }

//
//    @Owner(Constants.Owner.TARUN) //createOrderinIntTxn flag is On on Prod
//    @Feature("PGP-28416")
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Native : Validate sdkType AIO_SDK_PG param should be passed correctly in COP when passed in /ptc")
    public void pwdvalidateSDKTypeAIO_SDK_PGCOP(@Optional("false") Boolean isNativePlus) throws InterruptedException {
        FF4JFlags.disable("createOrderinIntTxn");
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String extendInfo = "{\"udf1\":\"vivek1\",\"sdkType\":\"AIO_SDK_PG\"}";

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setExtendInfo(extendInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep \"" + orderDTO.getORDER_ID() + "\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in COP request")
                .contains("sdkType", "AIO_SDK_PG");

        FF4JFlags.enable("createOrderinIntTxn");
    }

//    @Owner(Constants.Owner.TARUN) //createOrderinIntTxn flag is ON on Prod
//    @Feature("PGP-28416")
//    @Test(enabled = false, description = "Native Plus : Validate sdkType AIO_SDK_PG param should be passed correctly in COP when passed in /ptc")
    public void validateSDKTypeAIO_SDK_PGCPNativePlus() throws InterruptedException {
        FF4JFlags.disable("createOrderinIntTxn");
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        com.paytm.dto.processTransactionV1.ExtendInfo extendInfo = new com.paytm.dto.processTransactionV1.ExtendInfo();
        extendInfo.setSdkType("AIO_SDK_PG");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfo(extendInfo)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(merchantType.getId())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .assertAll();

        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in COP request")
                .contains("sdkType", "AIO_SDK_PG");

    }

//    @Owner(Constants.Owner.TARUN) //createOrderinIntTxn flag is ON on prod
//    @Feature("PGP-28416")
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Native : Validate sdkType CUI_SDK_PG param should be passed correctly in COP when passed in /ptc")
    public void validateSDKTypeCUI_SDK_PGCOP(@Optional("false") Boolean isNativePlus) throws InterruptedException {
        FF4JFlags.disable("createOrderinIntTxn");
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String extendInfo = "{\"udf1\":\"vivek1\",\"sdkType\":\"CUI_SDK_PG\"}";

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setExtendInfo(extendInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep \"" + orderDTO.getORDER_ID() + "\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).as("SDK Type is not getting passed in COP request")
                .contains("sdkType", "CUI_SDK_PG");
        FF4JFlags.enable("createOrderinIntTxn");
    }


    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that the value of the new flag is true when pref is enabled on merchant for Flipkart flow")
    public void validateNewFlagIsTrueWhenPrefIsEnableForFlipkartFlow() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        DriverManager.getDriver().get(response.jsonPath().get("body.callBackUrl"));

        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"LITEPAYVIEW_CONSULT\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("\"addAndPayMigration\":\"true\""));

    }

//
    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that the new flag  when pref is disabled on merchant for Flipkart flow")
    public void validateNewFlagWhenPrefIsDisableForFlipkartFlow() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.ADD_MONEY_WITH_RETRY;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"LITEPAYVIEW_CONSULT\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertFalse(theiaFacadeLogs.contains("addAndPayMigration"));

    }


    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that UPI paymode when pref is enabled on merchant for Flipkart flow")
    public void validateUPIPaymodesWhenPrefIsEnableForFlipkartFlow() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        DriverManager.getDriver().get(response.jsonPath().get("body.callBackUrl"));
        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"LITEPAYVIEW_CONSULT\" | grep \"RESPONSE\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("UPIPUSH"));
        Assert.assertTrue(theiaFacadeLogs.contains("UPI"));
        Assert.assertTrue(theiaFacadeLogs.contains("UPIPUSHEXPRESS"));
    }


    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that UPI paymode when pref is enabled on merchant for Hotstar flow")
    public void validateNewFlagIsTrueWhenPrefIsEnableForHotstarFlow() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"LITEPAYVIEW_CONSULT\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("\"addAndPayMigration\":\"true\""));
    }

    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that UPI paymode when pref is enabled on merchant for Hotstar flow")
    public void validateUPIPaymodesWhenPrefIsEnableForHotstarFlow() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchantType.getId() + "\" | grep \"LITEPAYVIEW_CONSULT\" | grep \"RESPONSE\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("UPIPUSH"));
        Assert.assertTrue(theiaFacadeLogs.contains("UPI"));
        Assert.assertTrue(theiaFacadeLogs.contains("UPIPUSHEXPRESS"));
    }

    @Parameters({"theme"})
    @Test(description = "test PPBL pay mode is disabled on show payment page when user has insufficient PPBL balance")
    public void testPPBLPayModeIsDisabledOnShowPaymentPageWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchant = MerchantType.NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        DriverManager.getDriver().get(response.jsonPath().get("body.callBackUrl"));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        cashierPage.checkboxPPBL().check();
        Assertions.assertThat(cashierPage.checkboxPPBL().isEnabled()).as("PPBL paymode is disabled").isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "test err msg is displayed on show payment page when user has insufficient PPBL balance")
    public void testErrMsgIsDisplayedOnShowPaymentPageWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchant = MerchantType.NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        DriverManager.getDriver().get(response.jsonPath().get("body.callBackUrl"));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        Assertions.assertThat(cashierPage.tabPPBL().content().toString()).as("Getting insufficient PPBL balance msg").contains("You do not have enough balance for this payment");
    }

//    @Issue("PGP-29758")
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "test next pay mode is selected on show payment page when user has insufficient PPBL balance")
    public void testNextPayModeIsSelectedOnShowPaymentPageWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        WalletHelpers.setZeroBalance(user);
        MerchantType merchant = MerchantType.NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        DriverManager.getDriver().get(response.jsonPath().get("body.callBackUrl"));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        WebElement payModeNextToPPBL = DriverManager.getDriver().findElements(By.cssSelector("section.p-option[data-key=ppb] ~ section")).get(0);
        Assertions.assertThat(payModeNextToPPBL.getAttribute("class").contains("active")).as("Paymode next to PPBL is selected").isTrue();
    }


    @Feature("PGP-29616")
    @Owner(GAGANDEEP)
    @Parameters({"isNativePlus"})
    @Test(description = "test new field UDF_2 should passed to P+")
    public void testNewFieldUDF_2(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String UDF_2 = "default_udf2";
        MerchantType merchant = MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setExtendInfo(new ExtendInfo().setUdf2(UDF_2))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateUDF(UDF_2)
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }


    @Feature("PGP-29617")
    @Owner(GAGANDEEP)
    @Test(description = "Verify for validate OTP V2 execute successfully with success transaction")
    public void validateOtpV2APISuccessResponse(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        new ValidateOTPV2(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId()).execute().then()
                .statusCode(200);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        WalletHelpers.validateBalance(user, 0.0);
    }


    @Feature("PGP-31240")
    @Owner(PRIYANSHI)
    @Test(description = "Verify for validate Getting correct param case in /v2/login/validateOtp api")
    public void validateOtpV2APICredAllowed() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        Constants.MerchantType merchantType = MerchantType.IVR;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTPV2 validateOTPV2 = new ValidateOTPV2(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        validateOTPV2.setContext("head.version","v2");
        validateOTPV2.setContext("body.fetchCashierData",true);
        Response response = validateOTPV2.execute();
        response.then().assertThat().body("body.cashierData.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.credsAllowed[0][0]",hasKey("CredsAllowedDLength"));
    }





    @Feature("PGP-29617")
    @Owner(GAGANDEEP)
    @Test(description = "Verify for logout OTP successfully with success transaction")
    public void logoutV2APISuccessResponse() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(Double.toString(txnAmount))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        new LogoutUserV2(txnToken, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId()).execute().then()
                .body("body.resultInfo.resultMsg", equalTo("Success"));
    }

    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is false ,theia.disableStaticSentinelForLPVMidBased false")
    public void testLPVforFlagTheiaBlackListTrueTheiaDisableFalseTheiaLPVMIDFalse() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        String redisKey = "litePayviewConsultResponse_";
        MerchantType merchantType = MerchantType.PGOnly;
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.disable(flagDisableStaticSential);
        ff4JClient.disable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        if (!(STATIC_REDIS_CLUSTER().get(redisKey + merchantType.getId()) == null))
            STATIC_REDIS_CLUSTER().del(redisKey + merchantType.getId());
        if (!(SESSION_REDIS_CLUSTER().get(redisKey + merchantType.getId()) == null))
            SESSION_REDIS_CLUSTER().del(redisKey + merchantType.getId());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();

        Assert.assertTrue(STATIC_REDIS_CLUSTER().get(redisKey + merchantType.getId()) != null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get(redisKey + merchantType.getId()) != null);

        if (flagDisableSential) ff4JClient.enable(flagDisableStaticSential);
        if (flagDisableSentialMID) ff4JClient.enable(flagDisableStaticSentialMID);
    }


    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is true ,theia.disableStaticSentinelForLPVMidBased true")
    public void testLPVforFlagTheiaBlacListTrueTheiaDisableTrueTheiaLPVMIDTrue() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        String redisKey = "litePayviewConsultResponse_";
        MerchantType merchantType = MerchantType.PGOnly;
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.enable(flagDisableStaticSential);
        ff4JClient.enable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        if (!(STATIC_REDIS_CLUSTER().get((redisKey + merchantType.getId())) == null))
            STATIC_REDIS_CLUSTER().del(redisKey + merchantType.getId());
        if (!(SESSION_REDIS_CLUSTER().get((redisKey + merchantType.getId())) == null))
            SESSION_REDIS_CLUSTER().del(redisKey + merchantType.getId());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();
        Assert.assertTrue(STATIC_REDIS_CLUSTER().get(redisKey + merchantType.getId()) == null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get(redisKey + merchantType.getId()) != null);

        if (!flagDisableSential) ff4JClient.disable(flagDisableStaticSential);
        if (!flagDisableSentialMID) ff4JClient.disable(flagDisableStaticSentialMID);


    }

    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is false ,theia.disableStaticSentinelForLPVMidBased true")
    public void testLPVforFlagTheiaBlackListTrueTheiaDisableFalseTheiaLPVMIDTrue() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        String redisKey = "litePayviewConsultResponse_";
        MerchantType merchantType = MerchantType.PGOnly;
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.disable(flagDisableStaticSential);
        ff4JClient.enable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        if (!(STATIC_REDIS_CLUSTER().get((redisKey + merchantType.getId())) == null))
            STATIC_REDIS_CLUSTER().del(redisKey + merchantType.getId());
        if (!(SESSION_REDIS_CLUSTER().get((redisKey + merchantType.getId())) == null))
            SESSION_REDIS_CLUSTER().del(redisKey + merchantType.getId());



        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();

        Assert.assertTrue(STATIC_REDIS_CLUSTER().get(redisKey + merchantType.getId()) == null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get(redisKey + merchantType.getId()) != null);

        if (flagDisableSential) ff4JClient.enable(flagDisableStaticSential);
        if (!flagDisableSentialMID) ff4JClient.disable(flagDisableStaticSentialMID);


    }

    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is false ,theia.disableStaticSentinelForLPVMidBased true")
    public void testLPVforFlagTheiaBlackListFalseTheiaDisableFalseTheiaLPVMIDTrue() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        String redisKey = "litePayviewConsultResponse_";
        MerchantType merchantType = MerchantType.PGOnly;
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.disable(flagDisableStaticSential);
        ff4JClient.enable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        if (!(STATIC_REDIS_CLUSTER().get(redisKey + merchantType.getId()) == null))
            STATIC_REDIS_CLUSTER().del(redisKey + merchantType.getId());
        if (!(SESSION_REDIS_CLUSTER().get((redisKey + merchantType.getId())) == null))
            SESSION_REDIS_CLUSTER().del(redisKey + merchantType.getId());


        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();

        Assert.assertTrue(STATIC_REDIS_CLUSTER().get(redisKey + merchantType.getId()) == null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get(redisKey + merchantType.getId()) != null);

        if (flagDisableSential) ff4JClient.enable(flagDisableStaticSential);
        if (!flagDisableSentialMID) ff4JClient.disable(flagDisableStaticSentialMID);


    }


    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is false ,theia.disableStaticSentinelForLPVMidBased true")
    public void testADDNPayLPVforFlagTheiaBlackListTrueTheiaDisableFalseTheiaLPVMIDFalse() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        String redisKey = "addAndPayLitePayviewConsultResponse";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.disable(flagDisableStaticSential);
        ff4JClient.enable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();

        Assert.assertTrue(STATIC_REDIS_CLUSTER().get(redisKey) != null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get(redisKey) != null);

        if (flagDisableSential) ff4JClient.enable(flagDisableStaticSential);
        if (!flagDisableSentialMID) ff4JClient.disable(flagDisableStaticSentialMID);


    }

    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is true ,theia.disableStaticSentinelForLPVMidBased false")
    public void testADDnPayLPVforFlagTheiaBlackListTrueTheiaDisableFalseTheiaLPVMIDFalse() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        MerchantType merchantType = MerchantType.NATIVE_ADDNPAY;
        String redisKey = "addAndPayLitePayviewConsultResponse";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.enable(flagDisableStaticSential);
        ff4JClient.disable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();


        Assert.assertTrue(STATIC_REDIS_CLUSTER().get(redisKey) == null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get(redisKey) != null);

        if (!flagDisableSential) ff4JClient.disable(flagDisableStaticSential);
        if (flagDisableSentialMID) ff4JClient.enable(flagDisableStaticSentialMID);

    }

    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is true ,theia.disableStaticSentinelForLPVMidBased true: Session key already present"
            , dependsOnMethods = "testLPVforFlagTheiaBlackListTrueTheiaDisableFalseTheiaLPVMIDFalse")
    public void testALPVforFlagTheiaBlackListTrueTheiaDisableTrueTheiaLPVMIDTrueKeyAlreadyPresentSession() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        MerchantType merchantType = MerchantType.PGOnly;
        String redisKey = "litePayviewConsultResponse_";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.enable(flagDisableStaticSential);
        ff4JClient.disable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        //deleting key from static redis

        STATIC_REDIS_CLUSTER().del(redisKey + merchantType.getId());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();
        Assert.assertTrue(STATIC_REDIS_CLUSTER().get((redisKey + merchantType.getId())) != null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get((redisKey + merchantType.getId())) == null);

        if (flagDisableSential) ff4JClient.disable(flagDisableStaticSential);
        if (!flagDisableSentialMID) ff4JClient.disable(flagDisableStaticSentialMID);

    }

    @Feature("PGP-29550")
    @Owner(GAGANDEEP)
    @Test(description = "theia.blacklistLPVfromFPOV2WithAccessToken is true, theia.disableStaticSentinelForLPV is true ,theia.disableStaticSentinelForLPVMidBased true: Static key already present"
            , dependsOnMethods = "testLPVforFlagTheiaBlackListTrueTheiaDisableFalseTheiaLPVMIDFalse")
    public void testALPVforFlagTheiaBlackListTrueTheiaDisableTrueTheiaLPVMIDTrueKeyAlreadyPresentStatic() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String flagDisableStaticSential = "theia.disableStaticSentinelForLPV";
        String flagDisableStaticSentialMID = "theia.disableStaticSentinelForLPVMidBased";
        MerchantType merchantType = MerchantType.PGOnly;
        String redisKey = "litePayviewConsultResponse_";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        FF4JClient ff4JClient = new FF4JClientImpl();
        boolean flagDisableSential = ff4JClient.check(flagDisableStaticSential);
        boolean flagDisableSentialMID = ff4JClient.check(flagDisableStaticSentialMID);
        ff4JClient.enable(flagDisableStaticSential);
        ff4JClient.disable(flagDisableStaticSentialMID);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");

        //deleting key from static redis

        SESSION_REDIS_CLUSTER().del(redisKey + merchantType.getId());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(merchantType.getId()).build();
        FetchPaymentOptionV2 fpov2 = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        fpov2.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        fpov2.execute();
        Thread.sleep(2000);
        Assert.assertTrue(STATIC_REDIS_CLUSTER().get((redisKey + merchantType.getId())) != null);
        Assert.assertTrue(SESSION_REDIS_CLUSTER().get((redisKey + merchantType.getId())) != null);

        if (flagDisableSential) ff4JClient.disable(flagDisableStaticSential);
        if (!flagDisableSentialMID) ff4JClient.disable(flagDisableStaticSentialMID);

    }


    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Amount based Simplified emi data is in merchant status response and in peon with peon FlagON and MerchantStatus Flag ON")
    public void AmountEMISimplifiedSubvention_peonON_MerchantStatusON(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.XIAOMI4;

     //   FF4JFlags.enableMidBased("merchantStatus.setEmiSubventionInfo", m.getId());
     //   FF4JFlags.enableMidBased("notiQueueHandler.setEmiSubventionInfo", m.getId());

        String payMethod = "EMI";
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String, String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(m.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), m)
                .setTxnValue("10")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, "1", new OfferDetails().setOfferId("123456")))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        OrderDTO orderDTO = new OrderFactory.
                Native(m, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethod)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("8.0")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("HDFC")
                .validatePaymentMode(payMethod)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is not present in peon").isNotNull();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), m.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"), "TXN_SUCCESS");
        softAssert.assertNotNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertAll();
    }

    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Item based Simplified emi data is not in merchant status response and not in peon with peon FlagOFF and MerchantStatus Flag OFF")
    public void ItemEMISimplifiedSubvention_peonOFF_MerchantStatusOFF(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType m = MerchantType.EMI;

//            FF4JFlags.disableMidBased("merchantStatus.setEmiSubventionInfo", m.getId());
//            FF4JFlags.disableMidBased("notiQueueHandler.setEmiSubventionInfo", m.getId());

        String payMethod = "EMI";
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String, String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(m.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("10");
        items.add(item);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), m)
                .setTxnValue("10")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, items))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.
                Native(m, initTxnDTO.orderFromBody(), txnToken, paymentDTO,PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoNotPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("9.0")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validateGatewayName("HDFC")
                .validatePaymentMode(payMethod)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is present in peon").isNull();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), m.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"), "TXN_SUCCESS");
        softAssert.assertNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertAll();
    }

    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Simplified Promo and Simplified Amount EMI Subvention data present in in merchant status response and peon; simplified promo data not affected by flag")
    public void SimplifiedPromoAndAmountEmiSubventionBothApplied_PeonON_MerchantStatusON(@Optional("true") boolean isNativePlus) throws Exception {

        Constants.MerchantType merchantType = MerchantType.XIAOMI4;

       // FF4JFlags.enableMidBased("merchantStatus.setEmiSubventionInfo", merchantType.getId());
        //FF4JFlags.enableMidBased("notiQueueHandler.setEmiSubventionInfo", merchantType.getId());
        //FF4JFlags.enable("theia.promoDataInMerchantStatusService"); as simplified promo data goes anyway

        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        User user = userManager.getForWrite(Label.BASIC);

        List<Map<String, String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(merchantType.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, "1", new OfferDetails().setOfferId("123456")))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoPresent()
                .validatePaymentPromoCheckoutDataPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("7.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .AssertAll();


        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());

        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is not present in peon").isNotNull();
        Assertions.assertThat(peonResponse.getPaymentPromoCheckoutData()).as("Simplified promo data not present").isNotNull();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"), "TXN_SUCCESS");
        softAssert.assertNotNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertNotNull(jsonPath.getString("body.paymentPromoCheckoutData"));
        softAssert.assertAll();

    }

    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Simplified Promo and Simplified Item EMI Subvention data present in in merchant status response and not in peon; simplified promo data not affected by flag")
    public void SimplifiedPromoAndItemEmiSubventionBothApplied_PeonOFF_MerchantStatusON(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = MerchantType.XIAOMI1;

     //   FF4JFlags.enableMidBased("merchantStatus.setEmiSubventionInfo", merchantType.getId());
     //       FF4JFlags.disableMidBased("notiQueueHandler.setEmiSubventionInfo", merchantType.getId());

        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();

        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String, String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(merchantType.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("10");
        items.add(item);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, items))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptResponseDTO.set(FetchPaymentOption.executeFetchPaymtOption(
                merchantType.getId(), orderId, fetchPaymentOptionsDTO));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoPresent()
                .validatePaymentPromoCheckoutDataPresent()
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("8.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is present in peon").isNull();
        Assertions.assertThat(peonResponse.getPaymentPromoCheckoutData()).as("Simplified promo data not present").isNotNull();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"), "TXN_SUCCESS");
        softAssert.assertNotNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertNotNull(jsonPath.getString("body.paymentPromoCheckoutData"));
        softAssert.assertAll();

    }


    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Apply Promo data in merchant status and Simplified Amount EMI Subvention data is not present merchant status response and is in peon")
    public void ApplyPromoAndSimplifiedAmountEmiSubventionBothApplied_PeonON_MerchantStatusOFF_PromoON(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = MerchantType.XIAOMI3;

//           FF4JFlags.disableMidBased("merchantStatus.setEmiSubventionInfo",merchantType.getId());
//        FF4JFlags.enableMidBased("notiQueueHandler.setEmiSubventionInfo", merchantType.getId());
//        FF4JFlags.enableMidBased("theia.promoDataInMerchantStatusService", merchantType.getId());

        ResponsePage responsePage;

        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String, String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(merchant.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        String txnamt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchantType.getId());

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        apiV1ApplyPromo
                .setContext("body.promocode", "discount")
                .setContext("head.token", user.ssoToken())
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));
        Response response = apiV1ApplyPromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, "1", new OfferDetails().setOfferId("123456")))
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String txnToken = iniJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoNotPresent()
                .validatePaymentPromoCheckoutDataPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("7.90")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is not present in peon").isNotNull();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"), "TXN_SUCCESS");
        softAssert.assertNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertNotNull(jsonPath.getString("body.paymentPromoCheckoutData"));
        softAssert.assertAll();

    }

    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner(Constants.Owner.ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Apply Promo data not in merchant status and Simplified Item EMI Subvention data is present merchant status response and is not in peon")
    public void ApplyPromoAndSimplifiedItemEmiSubventionBothApplied_PeonOFF_MerchantStatusON_PromoOFF(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = MerchantType.XIAOMI1;

   //     FF4JFlags.enableMidBased("merchantStatus.setEmiSubventionInfo", merchantType.getId());
   //         FF4JFlags.disableMidBased("notiQueueHandler.setEmiSubventionInfo", merchantType.getId());
   //         FF4JFlags.disableMidBased("theia.promoDataInMerchantStatusService",merchantType.getId());

        ResponsePage responsePage;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String, String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(merchant.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        String txnamt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);

        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchant.getId());
        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        apiV1ApplyPromo
                .setContext("body.promocode", "discount")
                .setContext("body.totalTransactionAmount", txnamt)
                .setContext("head.token", user.ssoToken())
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));

        Response response = apiV1ApplyPromo.execute();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        String instantDiscount = paymentOffersApplied.getOfferBreakupList().get(0).getInstantDiscount();
        item.setPrice(String.valueOf(10 - Double.parseDouble(instantDiscount)));
        items.add(item);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, items))
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = iniJsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptResponseDTO.set(FetchPaymentOption.executeFetchPaymtOption(
                merchantType.getId(), orderId, fetchPaymentOptionsDTO));

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateEmiSubventionInfoPresent()
                .validatePaymentPromoCheckoutDataNotPresent()
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("8.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        Assertions.assertThat(peonResponse.getEmiSubventionInfo()).as("Emi Subvention Info is present in peon").isNull();
        Assertions.assertThat(peonResponse.getPaymentPromoCheckoutData()).as("Promo data is not present in peon").isNotNull();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"), "TXN_SUCCESS");
        softAssert.assertNotNull(jsonPath.getString("body.emiSubventionInfo"));
        softAssert.assertNull(jsonPath.getString("body.paymentPromoCheckoutData"));
        softAssert.assertAll();

    }

//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify that sso token is being passed to PPBL bank form request when passed in Initiate txn request")
//    public void verifySSOinPPBL_NBBankFormRequest(@Optional("false") Boolean isNativePlus) throws Exception {
//        User user = userManager.getForRead(Label.BASIC);
//        MerchantType merchantType = MerchantType.MUTUAL_FUND;
//        ExtendInfo extendInfo = new ExtendInfo();
//        extendInfo.setUdf1("test1");
//        extendInfo.setUdf2("test2");
//        extendInfo.setUdf3("test3");
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
//                .setRequestType("NATIVE_MF")
//                .setMid(MerchantType.MUTUAL_FUND.getId())
//                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .setValidateAccountNumber("true")
//                .setAllowUnverifiedAccount("false")
//                .setSsoToken(user.ssoToken())
//                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .setCallbackUrl("https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse")
//                .setExtendInfo(extendInfo)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
//                .setChannelCode("PYTM")
//                .setAccountNumber("91"+ user.mobNo())
//                .setAUTH_MODE("3D")
//                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .setCardInfo("")
//                .build();
//
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//
//        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/instaproxy.log | " +
//                "grep \"FLUXNET_NETBANKING_FORM_RESULT\" | grep \"token\"";
//        System.out.println(grepcmd);
//        String instalogs = Awaitility.await().until(() ->getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd), s -> !"".equals(s));
//        String[] a1= null;
//        String[] a2=null;
//        String[] bankformbody=null;
//        if(!isNativePlus) {
//            a1 = instalogs.split("VALUE='");
//            a2 = a1[1].split("'");
//            bankformbody= a2[0].split("\\.");
//        }
//        else if(isNativePlus){
//            a1= instalogs.split("token");
//            a2= a1[1].split("\"");
//            bankformbody= a2[2].split("\\.");
//
//        }
//        String decodedString = PGPHelpers.Base64Decode(bankformbody[1]);
//        JsonPath jsonPath=new JsonPath(decodedString);
//        Assert.assertNotNull(jsonPath.getString("ssoToken"));
//
//    }
//
//
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify that sso token is not sent to PPBL bank form request when not passed in Initiate txn request")
//    public void verifySSONotinPPBL_NBBankFormRequest(@Optional("false") Boolean isNativePlus) throws Exception {
//        User user = userManager.getForRead(Label.BASIC);
//        MerchantType merchantType = MerchantType.MUTUAL_FUND;
//        ExtendInfo extendInfo = new ExtendInfo();
//        extendInfo.setUdf1("test1");
//        extendInfo.setUdf2("test2");
//        extendInfo.setUdf3("test3");
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
//                .setRequestType("NATIVE_MF")
//                .setMid(MerchantType.MUTUAL_FUND.getId())
//                .setMerchantKey(MerchantType.MUTUAL_FUND_AGGR.getKey())
//                .setValidateAccountNumber("true")
//                .setAllowUnverifiedAccount("false")
//                .setAggrMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .setCallbackUrl("https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse")
//                .setExtendInfo(extendInfo)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
//                .setChannelCode("PYTM")
//                .setAccountNumber("91"+ user.mobNo())
//                .setAUTH_MODE("3D")
//                .setAggMid(MerchantType.MUTUAL_FUND_AGGR.getId())
//                .build();
//
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//
//        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/instaproxy.log | " +
//                "grep \"FLUXNET_NETBANKING_FORM_RESULT\" | grep \"token\"";
//        String instalogs = Awaitility.await().until(() ->getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd), s -> !"".equals(s));
//        String[] a1= null;
//        String[] a2=null;
//        String[] bankformbody=null;
//        if(!isNativePlus) {
//            a1 = instalogs.split("VALUE='");
//            a2 = a1[1].split("'");
//            bankformbody= a2[0].split("\\.");
//        }
//        else if(isNativePlus){
//            a1= instalogs.split("token");
//            a2= a1[1].split("\"");
//            bankformbody= a2[2].split("\\.");
//
//        }
//        String decodedString = PGPHelpers.Base64Decode(bankformbody[1]);
//        JsonPath jsonPath=new JsonPath(decodedString);
//        Assert.assertNull(jsonPath.getString("ssoToken"));
//    }
//
    @Issue("PGP-30431")
    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in FundTopUp API")
    public void IPv6clientIpFundTopUp(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setIsNativeAddMoney("true")
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);//passing clientIp in IPv6 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
            OrderDTO orderDTO;
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();
    }

    @Issue("PGP-30431")
    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in CreateOrder API")
    public void IPv6clientIpCreateOrder(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);//passing clientIp in IPv6 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
            OrderDTO orderDTO;
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();
    }

    @Issue("PGP-30431")
    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in PAY API")
    public void IPv6clientIpCashierPay() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210807921559|645|102024")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");
    }

//    @Issue("PGP-30431")
//    @Owner(ARSH)
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in COP API")
//    public void IPv6clientIpCOP() throws Exception {
//        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
//        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
//        User user = userManager.getForRead(Label.PRIORITY);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
//                .setMid(merchantType.getId())
//                .setMerchantKey(merchantType.getKey())
//                .setTxnValue("20")
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
//                .setPaymentMode("DEBIT_CARD")
//                .setCardInfo("|4160210807921559|645|012024")
//                .setAuthMode("otp")
//                .build();
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);
//        Response response = processTransactionV1.execute();
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");
//    }
//
    @Issue("PGP-30431")
    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in FundTopUp API")
    public void IPv4clientIpFundTopUp(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setIsNativeAddMoney("true")
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);//passing clientIp in IPv4 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
            OrderDTO orderDTO;
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();
    }

    @Issue("PGP-30431")
    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in CreateOrder API")
    public void IPv4clientIpCreateOrder(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);//passing clientIp in IPv4 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
            OrderDTO orderDTO;
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .build();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();
    }

    @Issue("PGP-30431")
    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in PAY API")
    public void IPv4clientIpCashierPay() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4001590000000001|123|102024")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");
    }

    @Issue("PGP-30431")
    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in COP API")
    public void IPv4clientIpCOP() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210807921559|645|012024")
                .setCardInfo("|4001590000000001|123|012024")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");
    }

//    @Issue("PGP-30431")
//    @Owner(ARSH)
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in COP API")
//    public void IPv4clientIpCOP() throws Exception {
//        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
//        String IPv4clientIp = "49.36.131.88";
//        User user = userManager.getForRead(Label.PRIORITY);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
//                .setMid(merchantType.getId())
//                .setMerchantKey(merchantType.getKey())
//                .setTxnValue("20")
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
//                .setPaymentMode("DEBIT_CARD")
//                .setCardInfo("|4160210807921559|645|012024")
//                .setAuthMode("otp")
//                .build();
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);
//        Response response = processTransactionV1.execute();
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");
//    }
//
    @Issue("PGP-31542")
    @Owner(RAJKUMAR)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify SolutionWiseMdr value for OFFLINE txn flow")
    public void VerifySolutionWiseMdrForOfflineTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        com.paytm.dto.processTransactionV1.TxnAmount amount = new com.paytm.dto.processTransactionV1.TxnAmount();
        String addInfo  = "payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT";
        Constants.MerchantType merchantType = Constants.MerchantType.AddMoney;
        GenerateQR generateQR = new GenerateQR(Constants.MerchantType.AddMoney.getId(), "9958579496");
        JsonPath js = generateQR.execute().jsonPath();
        String qrCodeId = js.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setVersion("v2")
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        fetchQRPaymentDetails.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        JsonPath fqrResponse =fetchQRPaymentDetails.execute().jsonPath();
        String orderId = fqrResponse.getString("body.paymentOptions.orderId");
        String add1 = addInfo.replace("{qrCode}" ,qrCodeId);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType,"SSO" , user.ssoToken())
                .setOrderId(orderId)
                .setPaymentMode("DEBIT_CARD")
                .setTxnAmount(amount.setValue("10"))
                .setCardNum(paymentDTO.DEBIT_CARD_NUMBER)
                .setAuthMode("otp")
                .setChannelId("WEB")
                .setChannelCode("ICICI")
                .setExtendedInfoCloseOrderOffline(add1)
                .build();
       NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia_facade.log | "  +
                "grep \"ACQUIRING_INQUIRE_WITH_ACQ_ID\" | " + "grep \"RESPONSE\" | " + "grep \"feeRateFactorsInfo\"";
        String theiaFacadeLogs=getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("feeRateFactorsInfo\":\"{\\\"solutionWiseMdr\\\":\\\"QR\\"));
    }
//
//    @Feature("PGP-40742")
//    @Owner(GAURAV)
//    @Test(description = "Verify Deals object in fetch qr payment details API response")
//    public void VerifyDealsInFetchQrPaymentDetails() throws Exception {
//        User user = userManager.getForRead(Label.BASIC);
//        PaymentDTO paymentDTO = new PaymentDTO();
//        com.paytm.dto.processTransactionV1.TxnAmount amount = new com.paytm.dto.processTransactionV1.TxnAmount();
//        String addInfo  = "payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT";
//        Constants.MerchantType merchantType = MerchantType.DEALS_MID;
//        GenerateQR generateQR = new GenerateQR(MerchantType.DEALS_MID.getId(), "9958579496");
//        JsonPath js = generateQR.execute().jsonPath();
//        String qrCodeId = js.getString("response[0].qrCodeId");
//
//        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
//                .setVersion("v2")
//                .setQRCodeId(qrCodeId)
//                .setMID(merchantType.getId())
//                .setTokenType("SSO")
//                .setToken(user.ssoToken())
//                .build();
//
//        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
//        fetchQRPaymentDetails.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
//        JsonPath fetchqrResponse =fetchQRPaymentDetails.execute().jsonPath();
//        String orderId = fetchqrResponse.getString("body.paymentOptions.orderId");
//        Assertions.assertThat(fetchqrResponse.getString("body.paymentOptions.deals")).isNotNull();
//        Assertions.assertThat(fetchqrResponse.getString("body.paymentOptions.deals.isClaimed")).isEqualTo("true");
//        Assertions.assertThat(fetchqrResponse.getString("body.paymentOptions.deals.isPopUpRequired")).isEqualTo("true");
//        Assertions.assertThat(fetchqrResponse.getString("body.paymentOptions.deals.popUpDetails.title")).isEqualTo("health merchant");
//        String add1 = addInfo.replace("{qrCode}" ,qrCodeId);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
//                merchantType,"SSO" , user.ssoToken())
//                .setOrderId(orderId)
//                .setPaymentMode("DEBIT_CARD")
//                .setTxnAmount(amount.setValue("10"))
//                .setCardNum(paymentDTO.DEBIT_CARD_NUMBER)
//                .setAuthMode("otp")
//                .setChannelId("WEB")
//                .setChannelCode("ICICI")
//                .setExtendedInfoCloseOrderOffline(add1)
//                .build();
//        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
//        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia_facade.log | "  +
//                "grep \"DEALS_SERVICE\" | " + "grep \"RESPONSE\"";
//        String theiaFacadeLogs=getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
//        System.out.println(theiaFacadeLogs);
//        Assert.assertTrue(theiaFacadeLogs.contains("{\"isClaimed\":true,\"deepLink\":\"paytmmp://grid?url=https://dealsstaging.paytm.com/paybill?partner_id=98283\",\"isPopUpRequired\":true,\"popUpDetails\":{\"title\":\"health merchant\",\"subTitle\":\"7% Off upto ???7 + 8% Cashback upto ???8\",\"cta1Text\":null,\"cta2Text\":null,\"cta1Link\":null,\"cat2Link\":null}}"));
//    }
//
    @Issue("PGP-35712")
    @Owner(RAJKUMAR)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that for wrapper enabled mid otp on bank page should come ")
    public void getOtpMerchantPage(@Optional("true") Boolean isNativePlus) throws Exception{
        Constants.MerchantType merchantType = MerchantType.PGOnly;
        PaymentDTO paymentDTO = new PaymentDTO();
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null ,merchantType)
                .setOrderId(OrderId)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(paymentDTO.DEBIT_CARD_NUMBER)
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.findOtpBox().clearAndType("1234");
        directBankOTPPage.clickOnSubmit().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Issue("PGP-35712")
    @Owner(RAJKUMAR)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that for wrapper enabled mid and  preferredOtpPage pref is passed to body then otp on bank page should come ")
    public void getOtpMerchantPageWhenPrefIsBank(@Optional("true") Boolean isNativePlus) throws Exception{
        Constants.MerchantType merchantType = MerchantType.OTP_BANK_PAGE;
        PaymentDTO paymentDTO = new PaymentDTO();
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null ,merchantType)
                .setOrderId(OrderId)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setPreferredOtpPage("bank")
                .setCardNum(paymentDTO.DEBIT_CARD_NUMBER)
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.findOtpBox().clearAndType("1234");
        directBankOTPPage.clickOnSubmit().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateTxnDate(new Date())
                .AssertAll();

    }

//    @Owner(POOJA)
//    @Parameters({"isNativePlus"})
//    @Test(description = "Offline QR code transaction using DC and POS ID")
//    public static String[] offlineQRCodeTxnDC(@Optional("true") Boolean isNativePlus) throws Exception{
//        User user = userManager.getForRead(Label.BASIC);
//        PaymentDTO paymentDTO = new PaymentDTO();
//        com.paytm.dto.processTransactionV1.TxnAmount amount = new com.paytm.dto.processTransactionV1.TxnAmount();
//        String addInfo  = "payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:{qrCode}|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:{qrCode}|comment:|REQUEST_TYPE:QR_MERCHANT|POS_ID:224333666";
//        Constants.MerchantType merchantType = MerchantType.POS_ID;
//        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"",224333666);
//        JsonPath js = generateQR.execute().jsonPath();
//        String qrCodeId = js.getString("response[0].qrCodeId");
//
//        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
//                .setVersion("v2")
//                .setQRCodeId(qrCodeId)
//                .setMID(merchantType.getId())
//                .setTokenType("SSO")
//                .setToken(user.ssoToken())
//                .build();
//
//        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
//        fetchQRPaymentDetails.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
//        JsonPath fqrResponse =fetchQRPaymentDetails.execute().jsonPath();
//        String orderId = fqrResponse.getString("body.paymentOptions.orderId");
//
//        String add1 = addInfo.replace("{qrCode}" ,qrCodeId);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
//                merchantType,"SSO" , user.ssoToken())
//                .setOrderId(orderId)
//                .setPaymentMode("CREDIT_CARD")
//                .setTxnAmount(amount.setValue("10"))
//                .setCardNum(paymentDTO.PROMO_CC_CARD_HDFC)
//                .setAuthMode("otp")
//                .setChannelId("WEB")
//                .setChannelCode("ICICI")
//                .setExtendedInfoCloseOrderOffline(add1)
//                .build();
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        Response response = processTransactionV1.execute();
//        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
//
//        ResponsePage responsePage = new ResponsePage();
//        String TxnID = responsePage.textTxnID().getText();
//
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
//        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");
//
//        return new String[] { orderId , TxnID };
//    }
//
//    @Owner(POOJA)
//    @Parameters({"isNativePlus"})
//    @Test(description = "POS ID Based Payment Push notification for Merchant[Sub Users] for transaction Flow")
//    public void posIDBasedpushPaymentNotificationForSubUsersTransaction(@Optional("true") Boolean isNativePlus) throws Exception {
//        Constants.MerchantType merchantType = MerchantType.POS_ID;
//
//        String[] orderDetails = ProcessTransactionTests.offlineQRCodeTxnDC(true);
//
//        String orderID = orderDetails[0];
//        String TxnID = orderDetails[1];
//
//        String grepcmd = "grep \"" + orderID + "\" /paytm/logs/notificationQueueHandler.log | " +
//                "grep \"NotificationPayloadServiceImpl.sendPushNotificationV3ToUser()\" | grep \"Type=ProcessTxnNotification\"";
//
//        String notificationqueuehandler=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);
//
//        Assertions.assertThat(notificationqueuehandler).contains("templateName='test_template_for_business_app'");
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverType='ANDROID_CHANNEL'");
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverIdentifier=[9dec22d4-be83-4f87-87e0-d2d69503b4b3, f0afc1d3-2678-4f9a-bb1e-fa344cc067d3, t9afc1d3-2678-4f9a-bb1e-fa344cc067d3, b176fee2-c39b-4d90-b701-c5a70b392eaf, sss6fee2-c39b-4d90-b701-c5a70b392eaf]");
//
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverIdentifier=[ttt6fee2-c39b-4d90-b701-c5a70b392eaf]");
//
//        Assertions.assertThat(notificationqueuehandler).contains("templateName='merchant_push_paytm_app'");
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverIdentifier=[uvw6fee2-c39b-4d90-b701-c5a70b392eam]");
//    }
//
//    @Owner(POOJA)
//    @Parameters({"isNativePlus"})
//    @Test(description = "POS ID Based Payment Push notification for Merchant[Sub Users] for refund Flow")
//    public void posIDBasedpushPaymentNotificationForSubUsersRefund(@Optional("true") Boolean isNativePlus) throws Exception {
//        Constants.MerchantType merchantType = MerchantType.POS_ID;
//
//        String[] orderDetails = ProcessTransactionTests.offlineQRCodeTxnDC(true);
//
//        String orderID = orderDetails[0];
//        String TxnID = orderDetails[1];
//
//        PGPHelpers.initiateAsyncRefund(merchantType.getId(), merchantType.getKey(), orderID, orderID, TxnID, "10","REFUND", "", null);
//
//        PGPHelpers.getRefundStatus(merchantType.getId(), merchantType.getKey(), orderID, true)
//                .validateSuccessRefund()
//                .assertAll();
//        String grepcmd = "grep \"" + orderID + "\" /paytm/logs/notificationQueueHandler.log | " +
//                "grep \"NotificationPayloadServiceImpl.sendPushNotificationV3ToUser()\" | grep \"Type=ProcessRefund\"";
//
//        String notificationqueuehandler=getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);
//
//        Assertions.assertThat(notificationqueuehandler).contains("templateName='test_template_for_business_app'");
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverType='ANDROID_CHANNEL'");
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverIdentifier=[9dec22d4-be83-4f87-87e0-d2d69503b4b3, f0afc1d3-2678-4f9a-bb1e-fa344cc067d3, t9afc1d3-2678-4f9a-bb1e-fa344cc067d3, b176fee2-c39b-4d90-b701-c5a70b392eaf, sss6fee2-c39b-4d90-b701-c5a70b392eaf]");
//
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverIdentifier=[ttt6fee2-c39b-4d90-b701-c5a70b392eaf]");
//
//        Assertions.assertThat(notificationqueuehandler).contains("templateName='merchant_push_paytm_app'");
//        Assertions.assertThat(notificationqueuehandler).contains("notificationReceiverIdentifier=[uvw6fee2-c39b-4d90-b701-c5a70b392eam]");
//    }
//
//    @Owner(ABHISHEK_KULKARNI)
//    @Feature("PGP-40150")
//    @Parameters({"isNativePlus"})
//    @Test(description = "Validate inbox notification from pg in communication gateway logs for CC Txn.", enabled = true)
//    // PreRequist - urbanAirshipHash: "+OHt9ab7w46y0YUQl+hdDzjTazXbSE8doXBi/nKvD0o=",urbanAirshipEnabled: true and FF4j-MARKETPLACE_PUSHV3_FF4J_MID_FLAG
//    public void validateCommLogsForCC(@Optional("true") Boolean isNativePlus) throws Exception {
//        MerchantType merchantType = MerchantType.LOYALTY_POINT;
//        User user = userManager.getForWrite(Label.BASIC);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
//        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
//
//        String orderId = initTxnDTO.orderFromBody();
//        String txnToken = initTxnResponse.getBody().getTxnToken();
//        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
//        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
//                orderId, fetchPaymentOptionsDTO);
//        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
//                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
//                .setAuthMode("otp")
//                .setCardInfo("|4514570007356150|644|092028")
//                .build();
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        Response response = processTransactionV1.execute();
//        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
//
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.validateStatus("TXN_SUCCESS");
//        responsePage.validateRespCode("01");
//        String grepcmd = "grep \"" + "com.paytm.pgplus.comm.gateway.service.pushnotification.PushNotificationV3Service.getHttpRequestPayload" + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + orderId + "\"";
//        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
//        Assertions.assertThat(logs).contains("pushNotificationRequest V3 as json").isNotEmpty();
//        String cmd = "grep \"" + "com.paytm.pgplus.comm.gateway.service.pushnotification.PushNotificationV3Service.sendPushNotificationToUser()" + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + merchantType.getId() + "\" | " + "grep \"" + "Push Notification sent sucessfully with job_id" + "\"";
//        String comLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, cmd);
//        Assertions.assertThat(comLogs).contains("\"code\":202,\"message\":\"push job created successfully\",\"status\":\"SUCCESS\"");
//    }
//
//    @Owner(ABHISHEK_KULKARNI)
//    @Feature("PGP-40150")
//    @Parameters({"isNativePlus"})
//    @Test(description = "Validate inbox notification from pg in communication gateway logs for DC Txn.", enabled = true)
//    // PreRequist - urbanAirshipHash: "+OHt9ab7w46y0YUQl+hdDzjTazXbSE8doXBi/nKvD0o=",urbanAirshipEnabled: true and FF4j-MARKETPLACE_PUSHV3_FF4J_MID_FLAG
//    public void validateCommLogsForDC(@Optional("true") Boolean isNativePlus) throws Exception {
//        MerchantType merchantType = MerchantType.LOYALTY_POINT;
//        User user = userManager.getForWrite(Label.BASIC);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
//        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
//
//        String orderId = initTxnDTO.orderFromBody();
//        String txnToken = initTxnResponse.getBody().getTxnToken();
//        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
//        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
//                orderId, fetchPaymentOptionsDTO);
//        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
//                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
//                .setAuthMode("otp")
//                .setCardInfo("|5264193457899881|645|092028")
//                .setPaymentMode("DEBIT_CARD")
//                .build();
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        Response response = processTransactionV1.execute();
//        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
//
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.validateStatus("TXN_SUCCESS");
//        responsePage.validateRespCode("01");
//        String grepcmd = "grep \"" + "com.paytm.pgplus.comm.gateway.service.pushnotification.PushNotificationV3Service.getHttpRequestPayload" + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + orderId + "\"";
//        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
//        Assertions.assertThat(logs).contains("pushNotificationRequest V3 as json").isNotEmpty();
//        String cmd = "grep \"" + "com.paytm.pgplus.comm.gateway.service.pushnotification.PushNotificationV3Service.sendPushNotificationToUser()" + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + merchantType.getId() + "\" | " + "grep \"" + "Push Notification sent sucessfully with job_id" + "\"";
//        String comLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, cmd);
//        Assertions.assertThat(comLogs).contains("\"code\":202,\"message\":\"push job created successfully\",\"status\":\"SUCCESS\"");
//    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-39959")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that SubventionType should be Present in V2 Fpo response on the saved card having Emi available on it")
    public void verifySubventionTypeInFpoResponse(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SimplifiedSubvention simplifiedSubvention= new SimplifiedSubvention("1234","470.555",true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.IOS_INTENT_NOPREF, simplifiedSubvention).setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setMid(initTxnDTO.getBody().getMid()).setGenerateOrderId(initTxnDTO.getBody().getOrderId()).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String subventionType = fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments.savedCardEmisubventionDetail");
        Assertions.assertThat(subventionType).contains("subventionType");
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-39881")
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify that ptc response for UPI_INTENT txn when theia.checkout.pollingForIntent ff4j is ON for mid will contain display field having statusTimeout,upiStatusUrl, statusInterval in it")
//    public void verifyPtcResponseforUpiIntentTxn(@Optional("true") Boolean isNativePlus) throws Exception {
//        User user = userManager.getForRead(Label.BASIC);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.IOS_INTENT_NOPREF).setTxnValue("1")
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
//                MerchantType.IOS_INTENT_NOPREF.getId(),"TXN_TOKEN" ,txnToken, initTxnDTO.getBody().getOrderId(),"1")
//                .setPaymentMode("UPI_INTENT")
//                .setAuthMode("3D")
//                .build();
//        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
//        Response ptcResponse = processTransactionV1.execute();
//        String displayfield = ptcResponse.jsonPath().getString("body.displayField");
//        Assertions.assertThat(displayfield).contains("statusTimeout");
//        Assertions.assertThat(displayfield).contains("upiStatusUrl");
//        Assertions.assertThat(displayfield).contains("statusInterval");
//    }

    @Owner(HIMANSHU)
    @Test(description = "To verify the postpaid txn when PPBL NB is sent as enablePayment Mode in initate txn API : checkoutJS flow")
    public void enablePayModePPBLNBSuccessPaytmPostpaidCheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user =userManager.getForWrite(Label.POSTPAID);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nativeMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("PPBL")})
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.NATIVE_HYBRID.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkboxPPBL().isDisplayed();
        cashierPage.radioButtonPaytmPostpaid().assertNotVisible();

    }
    @Owner(HIMANSHU)
    @Test(description = "To verify the postpaid txn when PPBL NB is sent as enablePayment Mode in initate txn API : Enhanced")
    public void enablePayModePPBLNBSuccessPaytmPostpaidEnhanced(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType nativeMerchant = MerchantType.NATIVE_HYBRID;
        User user =userManager.getForWrite(Label.POSTPAID);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.AddnPay(nativeMerchant, theme, user)
                .setORDER_ID(OrderId)
                .setPAYMENT_MODE_ONLY("YES")
                .setPAYMENT_TYPE_ID("NB")
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .validateStatus("TXN_FAILURE")
                .validateMid(nativeMerchant.getId())
                .assertAll();

    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-41877")
    @Test(description = "Verify success UPI PUSH txn in v1PTC when qrcodeId is passed with no mid and orderid passed in ptc for static qr")
    public void qrcodeIdUpiPushSuccessTxnStaticQr(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.Irctc_binIrcId;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "",29);
        //  JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(user.ssoToken())
                .setQRCodeId(qrCodeId)
                .setPaymentMode("UPI")
                .setChannelCode("push")
                .setMpin("NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\"/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==")
                .setUpiAccRefId("222907")
                .setChannelId("WEB")
                .setmerchantVpa("rohit@paytm")
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue("10"))
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,null);
        Response v1result = processTransactionV1.execute();
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.GATEWAYNAME")).contains("PPBEX");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.PAYMENTMODE")).contains("UPI");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.STATUS")).contains("TXN_SUCCESS");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.MID")).contains(merchant.getId());
        String orderid = v1result.jsonPath().getString("body.txnInfo.ORDERID");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderid,"Payment Request");
        Assertions.assertThat(logs).contains("\"payeeVa\":\"rohit@paytm\"");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-41877")
    @Test(description = "Verify success UPI PUSH txn in v1PTC when qrcodeId is passed with orderId and no mid is passed for dynamic qr non pcf case")
    public void qrcodeIdUpiPushSuccessTxnDynamicQr(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchant = MerchantType.STORE_CASH;
        String txnAmount="10";
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant,txnAmount,OrderId,29);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(user.ssoToken())
                .setQRCodeId(qrCodeId)
                .setPaymentMode("UPI")
                .setChannelCode("push")
                .setOrderId(OrderId)
                .setMpin("NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\"/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==")
                .setUpiAccRefId("222907")
                .setChannelId("WEB")
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue(txnAmount))
                .setExtendInfoOrderAlreadyCreated(true)
                .setmerchantVpa("rohit@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,OrderId);
        Response v1result = processTransactionV1.execute();
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.GATEWAYNAME")).contains("PPBEX");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.PAYMENTMODE")).contains("UPI");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.STATUS")).contains("TXN_SUCCESS");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.MID")).contains(merchant.getId());
        String orderid = v1result.jsonPath().getString("body.txnInfo.ORDERID");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderid,"Payment Request");
        Assertions.assertThat(logs).contains("\"payeeVa\":\"rohit@paytm\"");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-41877")
    @Test(description = "Verify success UPI PUSH txn in v1PTC when qrcodeId is passed with orderId and no mid is passed for dynamic qr pcf case  and validate the CHARGEAMOUNT in response when theia.chargeAmountInPtcResponse is ON")
    public void qrcodeIdUpiPushSuccessTxnDynamicQrPcf(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchant = MerchantType.PGOnly_Pcf;
        String txnAmount = "100";
        String totaltxnAmount = "105.90";
        String pcfAmount = "5.90";
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, txnAmount, OrderId, 29);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(user.ssoToken())
                .setQRCodeId(qrCodeId)
                .setPaymentMode("UPI")
                .setChannelCode("push")
                .setOrderId(OrderId)
                .setMpin("NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\"/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==")
                .setUpiAccRefId("222907")
                .setChannelId("WEB")
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue(totaltxnAmount))
                .setExtendInfoOrderAlreadyCreated(true)
                .setmerchantVpa("rohit@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request, OrderId);
        Response v1result = processTransactionV1.execute();
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.GATEWAYNAME")).contains("PPBEX");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.PAYMENTMODE")).contains("UPI");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.STATUS")).contains("TXN_SUCCESS");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.MID")).contains(merchant.getId());
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.CHARGEAMOUNT")).contains(pcfAmount);
        String orderid = v1result.jsonPath().getString("body.txnInfo.ORDERID");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderid,"Payment Request");
        Assertions.assertThat(logs).contains("\"payeeVa\":\"rohit@paytm\"");
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on native for offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is Y")
    public void NativeOnOfflineMerchantWhenPreferenceIsOn(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.OFFLINE_WHITELISTED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.OFFLINE_WHITELISTED, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Successful.")
                .AssertAll();
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on native for offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is N")
    public void NativeOnOfflineMerchantWhenPreferenceIsOFF(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.OFFLINE_WHITELISTED_OFF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String resultmsg = response.jsonPath().getString("body.resultInfo.resultMsg");
        Assertions.assertThat(resultmsg).isEqualTo("Payment acceptance on this merchant is not available currently, please ask the merchant to contact our helpdesk team.");
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on native+ for offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is Y")
    public void NativePlusOnOfflineMerchantWhenPreferenceIsOn(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.OFFLINE_WHITELISTED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.OFFLINE_WHITELISTED, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, true);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Successful.")
                .AssertAll();
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on native+ for offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is N")
    public void NativePlusOnOfflineMerchantWhenPreferenceIsOFF(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.OFFLINE_WHITELISTED_OFF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        String resultmsg = response.jsonPath().getString("body.resultInfo.resultMsg");
        Assertions.assertThat(resultmsg).isEqualTo("Payment acceptance on this merchant is not available currently, please ask the merchant to contact our helpdesk team.");
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46368")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify EmiInfo details in ExtendInfo object in COP request for native")
    public void verifyCOPEmiInfoOnNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        String txnAmt = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.EmiInfo_COP)
                .setTxnValue(txnAmt)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.EmiInfo_COP, initTxnDTO.orderFromBody(), txnToken, payMethod)
                .setCardInfo("|4572741654006328|742|022024")
                .setEMI_TYPE("DEBIT_CARD")
                .setChannelCode("ICICI")
                .setPlanId("ICICI|3")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid,"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("emiInfo\":\"{\"cardType\":\"DEBIT_CARD\"");
        Assertions.assertThat(logs).contains("\"MID\":\"qa14te01473048855559\"");
        Assertions.assertThat(logs).contains("\"cardNo\":\"6328\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"190.0\"");
        Assertions.assertThat(logs).contains("\"merchantName\":\"pg2EMI1\"");
        Assertions.assertThat(logs).contains("\"cardIssuer\":\"ICICI\"");
        Assertions.assertThat(logs).contains("\"bank\":\"ICICI\"");
        Assertions.assertThat(logs).contains("\"emiAmount\":\"67.22\"");
        Assertions.assertThat(logs).contains("\"ORDER_ID\":\"" + initTxnDTO.getBody().getOrderId() + "\"");
        Assertions.assertThat(logs).contains("\"interest\":\"11.66\"");
        Assertions.assertThat(logs).contains("\"emiMonths\":\"3\"");
        Assertions.assertThat(logs).contains("\"emiInterestRate\":\"5.0\"");
        Assertions.assertThat(logs).contains("\"planID\":\"ICICI|3\"");
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46368")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify EmiInfo details in ExtendInfo object in COP request for nativePlus")
    public void verifyCOPEmiInfoOnNativePlus(@Optional("true") Boolean isNativePlus) throws Exception {
        String payMethod = "EMI";
        String txnAmt = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.EmiInfo_COP)
                .setTxnValue(txnAmt)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, payMethod, "false");
        String emiPlanId = Validate_EMIDetails(txnToken, initTxnDTO, "HDFC");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.
                Native(MerchantType.EmiInfo_COP, initTxnDTO.orderFromBody(), txnToken, paymentDTO, payMethod)
                .setPlanId(emiPlanId)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("emiInfo\":\"{\"cardType\":\"CREDIT_CARD\"");
        Assertions.assertThat(logs).contains("\"MID\":\"qa14te01473048855559\"");
        Assertions.assertThat(logs).contains("\"cardNo\":\"0336\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"190.0\"");
        Assertions.assertThat(logs).contains("\"merchantName\":\"pg2EMI1\"");
        Assertions.assertThat(logs).contains("\"cardIssuer\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"bank\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"emiAmount\":\"67.22\"");
        Assertions.assertThat(logs).contains("\"ORDER_ID\":\"" + initTxnDTO.getBody().getOrderId() + "\"");
        Assertions.assertThat(logs).contains("\"interest\":\"11.66\"");
        Assertions.assertThat(logs).contains("\"emiMonths\":\"3\"");
        Assertions.assertThat(logs).contains("\"emiInterestRate\":\"5.0\"");
        Assertions.assertThat(logs).contains("\"planID\":\"HDFC|3\"");

    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46368")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify EmiInfo details in ExtendInfo object in COP request for native in case of offer applied")
    public void verifyCOPEmiInfoOnNativeWhenApplyPromoAppliedAmountBased(@Optional("false") boolean isNativePlus) throws Exception {
        ResponsePage responsePage;
        Constants.MerchantType merchantType = MerchantType.EmiInfo_COP;
        Merchant merchant = new Merchant(merchantType.EmiInfo_COP.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String,String>> emiDetails = new ArrayList<>();
        emiDetails = PGPHelpers.getEMIDetails(MerchantType.EmiInfo_COP.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        String txnamt = "200.00";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", "discount");
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = iniJsonPath.getString("body.txnToken");
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), orderId, fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("emiInfo\":\"{\"cardType\":\"CREDIT_CARD\"");
        Assertions.assertThat(logs).contains("\"MID\":\"qa14te01473048855559\"");
        Assertions.assertThat(logs).contains("\"cardNo\":\"0336\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"199.9\"");
        Assertions.assertThat(logs).contains("\"merchantName\":\"pg2EMI1\"");
        Assertions.assertThat(logs).contains("\"cardIssuer\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"bank\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"emiAmount\":\"33.66\"");
        Assertions.assertThat(logs).contains("\"ORDER_ID\":\"" + initTxnDTO.getBody().getOrderId() + "\"");
        Assertions.assertThat(logs).contains("\"interest\":\"2.06\"");
        Assertions.assertThat(logs).contains("\"emiMonths\":\"6\"");
        Assertions.assertThat(logs).contains("\"emiPayOption\":\"EMI_HDFC\"");
        Assertions.assertThat(logs).contains("\"emiInterestRate\":\"3.5\"");
        Assertions.assertThat(logs).contains("\"planID\":\"HDFC|6\"");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4754")
    @Test(description = "Verify SSO is being stored in Redis and passed in Accounting API headers for wallet transaction")
    public void PAPR_4754_TC01() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.CancelAllowed;
        WalletHelpers.modifyBalance(user, 10.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String redisKey = "SSO_CACHEKEY_" + merchant.getId() + "_" + orderId;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), orderId)
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey)).contains(user.ssoToken());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.GATEWAYNAME")).contains("WALLET");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.PAYMENTMODE")).contains("PPI");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).contains("TXN_SUCCESS");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ssotoken");
        Assertions.assertThat(logs).contains("ssotoken received from redis key");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4754")
    @Test(description = "Verify SSO is being stored in Redis and passed in Accounting API headers for Fast Forward Wallet transaction")
    public void PAPR_4754_TC02() throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user, 150.0);
        Constants.MerchantType merchant = MerchantType.CancelAllowed;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        String redisKey = "SSO_CACHEKEY_" + merchant.getId() + "_" + orderId;
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchant.getId(), orderId, "123")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .setOrderId(orderId)
                .setOrderAlreadyCreated("false")
                .build();
        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .AssertAll();
        Assertions.assertThat(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey)).contains(user.ssoToken());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ssotoken");
        Assertions.assertThat(logs).contains("ssotoken received from redis key");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4754")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify SSO is being stored in Redis and passed in Accounting API headers for Partial AddnPay transaction")
    public void PAPR_4754_TC03(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.CancelAllowed;
        WalletHelpers.modifyBalance(user, 10.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String redisKey = "SSO_CACHEKEY_" + merchant.getId() + "_" + orderId;
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), initTxnResponse.getBody().getTxnToken(), PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        Assertions.assertThat(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey)).contains(user.ssoToken());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ssotoken");
        Assertions.assertThat(logs).contains("ssotoken received from redis key");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4754")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify SSO is being stored in Redis and passed in Accounting API headers for Full Add And Pay transaction")
    public void PAPR_4754_TC04(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType merchant = MerchantType.CancelAllowed;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String redisKey = "SSO_CACHEKEY_" + merchant.getId() + "_" + orderId;
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), initTxnResponse.getBody().getTxnToken(), PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        Assertions.assertThat(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey)).contains(user.ssoToken());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ssotoken");
        Assertions.assertThat(logs).contains("ssotoken received from redis key");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4754")
    @Parameters({"theme"})
    @Test(description = "Verify SSO is being stored in Redis and passed in Accounting API headers for Subscrption Add And Pay transaction")
    public void PAPR_4754_TC05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme)
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        String orderId = orderDTO.getORDER_ID();
        String redisKey = "SSO_CACHEKEY_" + orderDTO.getMID() + "_" + orderId;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        Assertions.assertThat(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey)).contains(user.ssoToken());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ssotoken");
        Assertions.assertThat(logs).contains("ssotoken received from redis key");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4754")
    @Test(description = "Verify SSO is being stored in Redis and passed in Accounting API headers for SSO Token PTC transaction")
    public void PAPR_4754_TC06() throws Exception {
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:ADD_MONEY|orderAlreadyCreated:{orderAlreadyCreated}";
        com.paytm.dto.processTransactionV1.TxnAmount txnAmount = new com.paytm.dto.processTransactionV1.TxnAmount();
        txnAmount.setValue("123");
        txnAmount.setCurrency("INR");
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user, 150.0);
        Constants.MerchantType merchant = MerchantType.CancelAllowed;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        String mid = merchant.getId();
        String redisKey = "SSO_CACHEKEY_" + mid + "_" + orderId;
        com.paytm.dto.processTransactionV1.ExtendInfo extendInfo = new com.paytm.dto.processTransactionV1.ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "false"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("NATIVE")
                .setWebsite("retail")
                .setPaymentMode("PPI")
                .setAuthMode("USRPWD")
                .setTxnAmount(txnAmount)
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey)).contains(user.ssoToken());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ssotoken");
        Assertions.assertThat(logs).contains("ssotoken received from redis key");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4754")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify SSO is not passed in Accounting API headers for wallet transaction when key is deleted from Redis")
    public void PAPR_4754_TC07(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.CancelAllowed;
        WalletHelpers.modifyBalance(user, 10.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("20")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String redisKey = "SSO_CACHEKEY_" + merchant.getId() + "_" + orderId;
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), initTxnResponse.getBody().getTxnToken(), PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        if(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) != null)
            TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);
        Assertions.assertThat(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey)).isNull();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, "ssotoken");
        Assertions.assertThat(logs).contains("ssotoken not received from redis key");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters to Risk in COP in Wallet")
    public void validatePtcHeaderinRiskExtendInfoinCOPforwallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"content-type\":\"application/json; charset=UTF-8\"");
        Assertions.assertThat(logs).contains("\"accept\":\"*/*\"");
        Assertions.assertThat(logs).contains("\"accept-encoding\":\"gzip\"");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters to Risk in COP in Postpaid")
    public void validatePtcHeaderinRiskExtendInfoinCOPforPostpaid() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String orderID=initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId())
                .AssertAll();


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"content-type\":\"application/json; charset=UTF-8\"");
        Assertions.assertThat(logs).contains("\"accept\":\"*/*\"");
        Assertions.assertThat(logs).contains("\"accept-encoding\":\"gzip\"");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters to Risk in COP for UPI Push")
    public void validatePtcHeaderinRiskExtendInfoinCOPforUPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"content-type\":\"application/json; charset=UTF-8\"");
        Assertions.assertThat(logs).contains("\"accept\":\"*/*\"");
        Assertions.assertThat(logs).contains("\"accept-encoding\":\"gzip\"");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters Not Present to Risk in COP in wallet")
    public void validatePtcHeaderNotPresentinRiskExtendInfoinCOPforwallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        String mid = MerchantType.UPIPUSHPG2.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("\"content-type\":\"application/json; charset=UTF-8\"");
        Assertions.assertThat(logs).doesNotContain("\"accept\":\"*/*\"");
        Assertions.assertThat(logs).doesNotContain("\"accept-encoding\":\"gzip\"");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters Not Present to Risk in COP in Postpaid")
    public void validatePtcHeaderNotPresentinRiskExtendInfoinCOPforPostpaid() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String orderID=initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "PAYTM_DIGITAL_CREDIT", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();


        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("\"content-type\":\"application/json; charset=UTF-8\"");
        Assertions.assertThat(logs).doesNotContain("\"accept\":\"*/*\"");
        Assertions.assertThat(logs).doesNotContain("\"accept-encoding\":\"gzip\"");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters Not Present to Risk in COP in UPI Push")
    public void validatePtcHeaderNotPresentinRiskExtendInfoinCOPforUPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.Static_True_Recent_True.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.Static_True_Recent_True.getId())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("content-type\\\":\\\"application/json\\");
        Assertions.assertThat(logs).doesNotContain("accept-encoding\\\":\\\"gzip\\");

    }


    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters to Risk in COTP for postpaid")
    public void validatePtcHeaderinRiskExtendInfoinCOTPforpostpaid(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, theme)
                .setTXN_AMOUNT("1")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateBankName(Constants.Gateway.PAYTMCC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("content-type\\\":\\\"application/json\\");
        Assertions.assertThat(logs).doesNotContain("accept-encoding\\\":\\\"gzip\\");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters Not Present to Risk in COTP for Postpaid")
    public void validatePtcHeaderNotPresentinRiskExtendInfoinCOTPforpostpaid(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,10.0);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, theme)
                .setTXN_AMOUNT("1")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateBankName(Constants.Gateway.PAYTMCC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("content-type\\\":\\\"application/json\\");
        Assertions.assertThat(logs).doesNotContain("accept-encoding\\\":\\\"gzip\\");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters Not Present to Risk in COP in UPI Push")
    public void validatePtcHeaderNotPresentinRiskExtendInfoinCOPforUPIPush() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.Static_True_Recent_True.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.Static_True_Recent_True.getId())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("\"content-type\":\"application/json; charset=UTF-8\"");
        Assertions.assertThat(logs).doesNotContain("\"accept\":\"*/*\"");
        Assertions.assertThat(logs).doesNotContain("\"accept-encoding\":\"gzip\"");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters Not Present to Risk in COP in UPI Push")
    public void validatePtcHeaderNotPresentinRiskExtendInfoinCOPPforUPIPush() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.Static_True_Recent_True.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.Static_True_Recent_True.getId())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("content-type\\\":\\\"application/json\\");
        Assertions.assertThat(logs).doesNotContain("accept-encoding\\\":\\\"gzip\\");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51159")
    @Test(description = "Validate PTC header parameters Not Present to Risk in COP in UPI Push")
    public void validatePtcHeaderNotPresentinRiskExtendInfoinCOTPforUPIPushAddnPay() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPaymentFlow("ADDANDPAY")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId())
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("content-type\\\":\\\"application/json\\");
        Assertions.assertThat(logs).doesNotContain("accept-encoding\\\":\\\"gzip\\");

    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify success transaction when bin is true at bin center")
    public void VerifyEmiTxnWhenBinIsTrue() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), orderId)
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setCardInfo("|4761360075860428|545|122027")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).contains("Success");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).contains("S");
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify success transaction when bin is false at bin center")
    public void VerifyEmiTxnWhenBinIsFalse() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), orderId)
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setCardInfo("|4718650100010336|545|122024")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).contains("The card number entered is not eligible for EMI. Please use another card");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).contains("F");
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify bin eligible true in FPO for logged in flow for onus merchant")
    public void VerifyBigEligibleTrueInFPO() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        User user = userManager.getForRead(Label.BINTXN3);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1100")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), orderId)
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setCardInfo("65268fa79823bd6696313347||123|")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).contains("Success");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).contains("S");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "GET_TOKENIZED_CARDS_IN_FPO", "RESPONSE");
        Assertions.assertThat(logs).contains("\"isEmiEligible\":\"true\",\"accountRangeCardBin\":\"476136007\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify bin eligible false in FPO for logged in flow for onus merchant")
    public void VerifyBinEligibleFalseInFPO() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        User user = userManager.getForRead(Label.BINTXN3);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1100")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), orderId)
                .setPaymentMode("EMI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setCardInfo("65268fa79823bd6696313347||123|")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.execute();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "GET_TOKENIZED_CARDS_IN_FPO", "RESPONSE");
        Assertions.assertThat(logs).contains("\"isEmiEligible\":\"false\",\"accountRangeCardBin\":\"471865010\"");
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify success transaction when bin is true at bin center")
    public void VerifyE2EEmiTxnWithCC() {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4761360075860428|111|122027")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(merchant.getId())
                .assertAll();
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-47869")
    @Test(description = "Verify success transaction when bin is true at bin center with saved card")
    public void VerifyE2EEmiTxnWithCCSavedCard() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        User user = userManager.getForRead(Label.BINTXN3);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1100")
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("65268fa79823bd6696313347||123|")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(merchant.getId())
                .assertAll();
    }

    @Feature("PAPR-5059")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify that mid is mapped to the client. Check logs for typeValue during sendOtp")
    public void validateMidMappedToClient() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.PG_MID_CLIENT;
        Double txnAmount = 2.0;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response =  sendotp.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).contains("Otp sent to phone");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).contains("SUCCESS");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "MERCHANT_CENTER_SERVICE", "RESPONSE");
        Assertions.assertThat(logs).contains("\"typeValue\":\"pg-mid-client-stag\"");

    }

    @Feature("PAPR-5059")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify that mid is not mapped to the client. Check logs for typeValue during sendOtp")
    public void validateMidNotMappedToClient() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response =  sendotp.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).contains("Otp sent to phone");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).contains("SUCCESS");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "MERCHANT_CENTER_SERVICE", "RESPONSE");
        Assertions.assertThat(logs).doesNotContain("\"typeValue\":\"pg-mid-client-stag\"");

    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when orderId is <p>This is <b>bold</b> text</p>, it should return 403 if its present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInPTC() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderId ="<p>This is <b>bold</b> text</p>";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId().toString(),txnToken,OrderId).
                setPaymentMode("EMI")
                .setCardInfo("|4761360075860428|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.getStatusCode())
                .as("Status code is not equal to 200")
                .isEqualTo(403);
    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when orderId is <script>alert('XSS')</script>, it should return 403 if its present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInPTCOrderId() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderId ="alert(document.cookie)";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId().toString(),txnToken,OrderId).
                setPaymentMode("EMI")
                .setCardInfo("|4761360075860428|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.getStatusCode())
                .as("Status code is not equal to 200")
                .isEqualTo(403);
    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when mid is <script>alert('XSS')</script>, it should return 403 if its present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInPTCMid() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String mid ="<script>alert('XSS')</script>";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4761360075860428|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.getStatusCode())
                .as("Status code is not equal to 200")
                .isEqualTo(403);
    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when paymode is alert(document.cookie), it should not return 403 if its not present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInPTCPaymode() throws Exception {
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("alert(document.cookie)")
                .setCardInfo("|4761360075860428|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).contains("alert(document.cookie) is not allowed for this transaction, kindly use some other payment mode");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).contains("F");

    }
    @Owner(AJEESH)
    @Feature("PGP-56181")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Callback logger is printed for 443 port")
    public void verifyLoggerforCallbackPort443(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("20")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("DC")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia.log | " + "grep \"validateCallbackUrl()\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("Port 443 found in callback url");

    }
    @Owner(AJEESH)
    @Feature("PGP-56181")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Callback logger is printed for 80 port")
    public void verifyLoggerforCallbackPort80(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("20")
                .setCallbackUrl("http://10.170.7.123:80/mockbank/MerchantSite/bankResponse")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        DriverManager.getDriver().findElement(By.id("proceed-button")).click();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia.log | " + "grep \"validateCallbackUrl()\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("Port 80 found in callback url");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Numeric Digits")
    public void CVValidationforVisawith_3_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.Static_True_Recent_True.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String custId = "Test101";
        String CardNumber = "4718650100010336";
       // String tin= "66b36d3b7c182c21a70ccc3d";
       SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||123|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 4 Numeric Digits")
    public void CVValidationforVisawith_4_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.Static_True_Recent_True.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String custId = "Test101";
        String CardNumber = "4718650100010336";
       // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||1234|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 2 Numeric Digits")
    public void CVValidationforVisawith_2_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.Static_True_Recent_True.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String custId = "Test101";
        String CardNumber = "4718650100010336";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||12|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Numeric Digits and space between them ")
    public void CVValidationforVisawith_3_NumericDigitsandSpaceBetweenThem() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.Static_True_Recent_True.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String custId = "Test101";
        String CardNumber = "4718650100010336";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||12 3|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Numeric Digits and space after cvv")
    public void CVValidationforVisawith_3_NumericDigitsandSpaceAfterCvv() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.Static_True_Recent_True.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String custId = "Test101";
        String CardNumber = "4718650100010336";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||123|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Alpha Numeric Digits")
    public void CVValidationforVisawith_3_AlphaNumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.Static_True_Recent_True.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String custId = "Test101";
        String CardNumber = "4718650100010336";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||12a|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 special character Numeric Digits")
    public void CVValidationforVisawith_3_SpecialCharNumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.Static_True_Recent_True.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String custId = "Test101";
        String CardNumber = "4718650100010336";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Static_True_Recent_True).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||12@|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Numeric Digits")
    public void CVValidationforDinerswith_3_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.CORPORATE_CARD_ONLY.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.CORPORATE_CARD_ONLY;
        String custId = "Test101";
        String CardNumber = PaymentDTO.DINERS_CARD_NUMBER;
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CORPORATE_CARD_ONLY).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||123|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 4 Numeric Digits")
    public void CVValidationforDinerswith_4_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.CORPORATE_CARD_ONLY.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.CORPORATE_CARD_ONLY;
        String custId = "Test101";
        String CardNumber = PaymentDTO.DINERS_CARD_NUMBER;
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CORPORATE_CARD_ONLY).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||1234|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 2 Numeric Digits")
    public void CVValidationforDinerswith_2_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.CORPORATE_CARD_ONLY.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.CORPORATE_CARD_ONLY;
        String custId = "Test101";
        String CardNumber = PaymentDTO.DINERS_CARD_NUMBER;
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CORPORATE_CARD_ONLY).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||12|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Numeric Digits and Space Between Them")
    public void CVValidationforDinerswith_3_NumericDigitsandSpaceBetweenThem() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.CORPORATE_CARD_ONLY.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.CORPORATE_CARD_ONLY;
        String custId = "Test101";
        String CardNumber = PaymentDTO.DINERS_CARD_NUMBER;
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CORPORATE_CARD_ONLY).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||12 3|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Numeric Digits and Space After Cvv")
    public void CVValidationforDinerswith_3_NumericDigitsandSpaceAfterCvv() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.CORPORATE_CARD_ONLY.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.CORPORATE_CARD_ONLY;
        String custId = "Test101";
        String CardNumber = PaymentDTO.DINERS_CARD_NUMBER;
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CORPORATE_CARD_ONLY).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||123 |";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Alpha Numeric Digits")
    public void CVValidationforDinerswith_3_AlphaNumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.CORPORATE_CARD_ONLY.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.CORPORATE_CARD_ONLY;
        String custId = "Test101";
        String CardNumber = PaymentDTO.DINERS_CARD_NUMBER;
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CORPORATE_CARD_ONLY).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||1a2|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for Visa with 3 Special Char Numeric Digits")
    public void CVValidationforDinerswith_3_SpecialCharNumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.CORPORATE_CARD_ONLY.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.CORPORATE_CARD_ONLY;
        String custId = "Test101";
        String CardNumber = PaymentDTO.DINERS_CARD_NUMBER;
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CORPORATE_CARD_ONLY).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||1@2|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for AMEX with 4 Numeric Digits")
    public void CVValidationforAMEXwith_4_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_AMEX_EMI.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String custId = "Test101";
        String CardNumber = "376972138213802";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_AMEX_EMI).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||1234|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isNotEqualToIgnoringCase("You have entered wrong CVV for this card. Please try again with correct CVV");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isNotEqualToIgnoringCase("Invalid CVV");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for AMEX with 3 Numeric Digits")
    public void CVValidationforAMEXwith_3_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_AMEX_EMI.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String custId = "Test101";
        String CardNumber = "376972138213802";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_AMEX_EMI).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||123|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check
    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for AMEX with 5 Numeric Digits")
    public void CVValidationforAMEXwith_5_NumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_AMEX_EMI.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String custId = "Test101";
        String CardNumber = "376972138213802";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_AMEX_EMI).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||12345|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for AMEX with 4 Numeric Digits and Space Between Them")
    public void CVValidationforAMEXwith_4_NumericDigitsandSpaceBetweenThem() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_AMEX_EMI.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String custId = "Test101";
        String CardNumber = "376972138213802";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_AMEX_EMI).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||123 4|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for AMEX with 4 Numeric Digits and Space After Cvv")
    public void CVValidationforAMEXwith_4_NumericDigitsandSpaceAfterCvv() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_AMEX_EMI.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String custId = "Test101";
        String CardNumber = "376972138213802";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_AMEX_EMI).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||1234 |";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isNotEqualToIgnoringCase("You have entered wrong CVV for this card. Please try again with correct CVV");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isNotEqualToIgnoringCase("Invalid CVV");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for AMEX with 4 Alpha Numeric Digits")
    public void CVValidationforAMEXwith_4_AlphaNumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_AMEX_EMI.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String custId = "Test101";
        String CardNumber = "376972138213802";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_AMEX_EMI).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||123a|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Owner(CHAKSHU)
    @Feature("PGP-56076")
    @Test(description = "CVV Validation for AMEX with 4 Special Char Numeric Digits")
    public void CVValidationforAMEXwith_4_SpecialCharNumericDigits() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_AMEX_EMI.getId();
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String custId = "Test101";
        String CardNumber = "376972138213802";
        // String tin= "66b36d3b7c182c21a70ccc3d";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,CardNumber);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_AMEX_EMI).setCustId(custId).setTxnValue("5").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        String cardInfo = tin + "||1@23|";
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("You have entered wrong CVV for this card. Please try again with correct CVV");
        // Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid CVV");
        //Getting incorrect respcode from MC so disabling this check

    }

    @Feature("PGP-58764")
    @Owner(AKSHAT)
    @Test(description = "Validate that payerAccountNumbers is returned in NativePayment FPO response with multi TPV against it")
    public void TC01_multiTPV_NativeMF(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("83748239234872|94638239236085|85658239234839|80488239232839|01208239232542|01398239231129")
                .setPayerAccount("9873966839@paytm")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXXXX4872|XXXXXXXXXX6085|XXXXXXXXXX4839|XXXXXXXXXX2839|XXXXXXXXXX2542|XXXXXXXXXX1129"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000041"));


}

    @Feature("PGP-58764")
    @Owner(AKSHAT)
    @Test(description = "Validate that payerAccountNumbers is returned in NativeST(stock trade) FPO response with multi TPV against it")
    public void TC02_multiTPV_NativeST(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.STOCK_TRADE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_ST")
                .setMid(MerchantType.STOCK_TRADE.getId())
                .setMerchantKey(MerchantType.STOCK_TRADE.getKey())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("true")
                .setAccountNumber("83748239234872|94638239236085|85658239234839|80488239232839|01208239232542|01398239231129")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXXXX4872|XXXXXXXXXX6085|XXXXXXXXXX4839|XXXXXXXXXX2839|XXXXXXXXXX2542|XXXXXXXXXX1129"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000020"));


    }

    @Feature("PGP-58764")
    @Owner(AKSHAT)
    @Test(description = "Validate that payerAccountNumbers is returned in NativeST(stock trade) FPO response with multi TPV against it")
    public void TC03_multiTPV_Nativepayments(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("Payments")
                .setMid(MerchantType.Subs_HDFC_CheckoutJS.getId())
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("true")
                .setAccountNumber("83748239234872|94638239236085|85658239234839|80488239232839|01208239232542|01398239231129")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXXXX4872|XXXXXXXXXX6085|XXXXXXXXXX4839|XXXXXXXXXX2839|XXXXXXXXXX2542|XXXXXXXXXX1129"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000001"));


    }

    @Feature("PGQA-531")
    @Owner(AKSHAT)
    @Test(description = "Validate payerAccountNumbers in NativeMF FPO response with multi TPV via payerAccountDetails")
    public void TC01_multiTPV_NativeMF_withPayerAccountDetails(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(MerchantType.MUTUAL_FUND.getId())
                .setMerchantKey(MerchantType.MUTUAL_FUND.getKey())
                .setPayerAccount("9873966839@paytm")
                .setPayerAccountDetails(
                        new PayerAccountDetail("103567890458", "HDFC0000975", "Akshat"),
                        new PayerAccountDetail("103567890456", "SBIN0000123", "Akshat"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXX0458|XXXXXXXX0456"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000041"));


    }

    @Feature("PGQA-531")
    @Owner(AKSHAT)
    @Test(description = "Validate payerAccountNumbers in NativeST FPO response with multi TPV via payerAccountDetails")
    public void TC02_multiTPV_NativeST_withPayerAccountDetails(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.STOCK_TRADE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("NATIVE_ST")
                .setMid(MerchantType.STOCK_TRADE.getId())
                .setMerchantKey(MerchantType.STOCK_TRADE.getKey())
                .setPayerAccountDetails(
                        new PayerAccountDetail("103567890458", "HDFC0000975", "Akshat"),
                        new PayerAccountDetail("103567890456", "SBIN0000123", "Akshat"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXX0458|XXXXXXXX0456"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000020"));


    }

    @Feature("PGQA-531")
    @Owner(AKSHAT)
    @Test(description = "Validate payerAccountNumbers in NativePayment FPO response with multi TPV via payerAccountDetails")
    public void TC03_multiTPV_Nativepayments_withPayerAccountDetails(@Optional("false") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.Subs_HDFC_CheckoutJS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("Payments")
                .setMid(MerchantType.Subs_HDFC_CheckoutJS.getId())
                .setPayerAccountDetails(
                        new PayerAccountDetail("103567890458", "HDFC0000975", "Akshat"),
                        new PayerAccountDetail("103567890456", "SBIN0000123", "Akshat"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm")
                .setSTORE_CARD("")
                .setAUTH_MODE("USRPSWD")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.payerAccountNumbers").contains("XXXXXXXX0458|XXXXXXXX0456"));
        Assert.assertTrue(fetchPaymentOptionsJson.getString("body.productCode").contains("51051000100000000001"));


    }
    @Owner(AJEESH)
    @Feature("PGP-60079")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that if panUniqueReference in /token/gc/generateTokenData API response, then get/panUniqueReference api call shouldnt be made for New Card Txn.")
    public void VerifyMinimizingPARrequest(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("20")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " + "grep \"token/gc/generateTokenData\"";
        String generateTokenDataLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(generateTokenDataLogs).contains("panUniqueReference");

        String grepcmd2 = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " + "grep \"get/panUniqueReference\"";
        String panUniqueReference = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd2);
        Assertions.assertThat(panUniqueReference).doesNotContain("get/panUniqueReference");

    }
}