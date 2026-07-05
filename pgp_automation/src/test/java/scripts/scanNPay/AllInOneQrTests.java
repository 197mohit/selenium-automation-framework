package scripts.scanNPay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.api.FastForward;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.nativeAPI.FetchEMIDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.api.paymentService.SendPaymentRequestApi;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.api.wallet.GetQRCodeInfoApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.ExtendInfo;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter;
import com.paytm.pages.*;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.appconstants.Constants.MappingService.GET_MERCH_PREFERENCE_INFO;
import static com.paytm.appconstants.Constants.Owner.*;

/**
 * Charge amount is not displayed in merchant-status/transactionStatus api, jira logged: PGP-26209
 */

@Owners(author = "Ankur", qa = "Ankit")
@Issue("PGP-26209")
@Epic("All in One QR")
@Features({@Feature("PGP-20304"), @Feature("PGP-22936")})
public class AllInOneQrTests extends PGPBaseTest {

    private static final String PCF_FEE_SCAN_QR_ALERT_MSG = "For the best experience, scan the QR using the latest version of the Paytm App\n" +
            "Information regarding Convenience Fee will not be displayed in older versions of the app (8.6.0 or below)\n" +
            "Ok Got It!";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String JSON_POST_URL = LocalConfig.JSON_POST_URL;

    private ThreadLocal<String> qrCodeId = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }
    };

    RequestSpecification getMerchantPrefInfo() {
        RequestSpecification reqSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_MERCH_PREFERENCE_INFO)
                .addFilter(new RequestResponseLoggingFilter())
                .build();
        return RestAssured.given().spec(reqSpec);
    }

    @Parameters({"theme"})
    @Test(description = "Verify QR code not displayed when ENHANCE_QR_DISABLED=Y on merchant")
    public void validate_qrNotDisplayed(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.QR_DISABLED;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        getMerchantPrefInfo()
                .pathParam("mid", merchant.getId())
                .get()
                .then()
                .statusCode(200)
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("00000"),
                        "resultInfo.resultStatus", Matchers.equalToIgnoringCase("S"),
                        "resultInfo.messaage", Matchers.equalToIgnoringCase("Success"))
                .body("merchantPreferenceInfos.find{it.prefType == 'ENHANCE_QR_DISABLED'}.prefValue",
                        Matchers.equalToIgnoringCase("Y"))
        ;
        generateQrCodeOrder(user, merchant, orderDTO, theme);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.pause(3);
        cashierPage.imgScanPayQRCode().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of add then pay transaction after scanning paytm QR for PCF allin1qr Merchant")
    public void validate_S_AthenP_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("5.00").build();
        WalletHelpers.modifyBalance(user, 2.00);

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.paymentFlow", Matchers.equalToIgnoringCase("ADDANDPAY"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].channelCode",
                            Matchers.equalToIgnoringCase("ICICI"))
                    .body("body.paymentOptions.addMoneyPayOption", Matchers.notNullValue())
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}", Matchers.notNullValue())
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions.find{it.channelCode == 'ICICI'}",
                            Matchers.notNullValue())
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions.find{it.channelCode == 'ICICI'}.isDisabled.status",
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
                    .body("body.consultDetails", Matchers.notNullValue())
                    .spec(pcfBodySuccessVerify("BALANCE", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalConvenienceCharges.value");
        }

        addMoneyMP:
        {
            OrderDTO addMoneyMP = new OrderFactory.AddMoneyMP(Constants.MerchantType.AddMoneyMP, theme, user)
                    .setORDER_ID(orderDTO.getORDER_ID())
                    .setTXN_AMOUNT("3.00")
                    .build();
            performAddMoneyMP_txn(theme, user, addMoneyMP);

            //temporary hack as amount is not updated in wallet after addmoney
            WalletHelpers.modifyBalance(user, Double.valueOf(totalTxnAmount));
        }

        fastforwardTxn:
        {
            FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                    .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("PPI")
                    .setQrCodeId(qrCodeId.get())
                    .setReqType("CLW_APP_PAY")
                    .setToken(user.ssoToken())
                    .setTokenType("SSO")
                    .setCustomerId(user.custId())
                    .setOrderAlreadyCreated("true")
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
            softly.assertAll();
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("PPI")
                .validateChargeAmount(totalPcfAmount)
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "Validate success of add then pay transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_AthenP_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("5.00").build();
        WalletHelpers.modifyBalance(user, 2.00);

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.paymentFlow", Matchers.equalToIgnoringCase("ADDANDPAY"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].channelCode",
                            Matchers.equalToIgnoringCase("ICICI"))
                    .body("body.paymentOptions.addMoneyPayOption", Matchers.notNullValue())
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}", Matchers.notNullValue())
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions.find{it.channelCode == 'ICICI'}",
                            Matchers.notNullValue())
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions.find{it.channelCode == 'ICICI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
        }

        addMoneyMP:
        {
            OrderDTO addMoneyMP = new OrderFactory.AddMoneyMP(Constants.MerchantType.AddMoneyMP, theme, user)
                    .setORDER_ID(orderDTO.getORDER_ID())
                    .setTXN_AMOUNT("3.00")
                    .build();
            performAddMoneyMP_txn(theme, user, addMoneyMP);

            //temporary hack as amount is not updated in wallet after addmoney
            WalletHelpers.modifyBalance(user, 5.00);
        }

        fastforwardTxn:
        {
            FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                    .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("PPI")
                    .setQrCodeId(qrCodeId.get())
                    .setReqType("CLW_APP_PAY")
                    .setToken(user.ssoToken())
                    .setTokenType("SSO")
                    .setCustomerId(user.custId())
                    .setOrderAlreadyCreated("true")
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
                    .isEqualToIgnoringCase("51051000100000000001");
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
            softly.assertAll();
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("PPI")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of netbanking transaction after scanning paytm QR for PCF allin1qr Merchant")
    public void validate_S_NB_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].channelCode",
                            Matchers.equalToIgnoringCase("ICICI"))
            ;
        }

        String totalTxnAmount = "";
        String totalPcfAmount = "";
        fetchPcfDetails:
        {
            FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(user.ssoToken(), "SSO")
                    .setMid(merchant.getId())
                    .setTxnAmount(orderDTO.getTXN_AMOUNT())
                    .setPayMethods(Arrays.asList(new PayMethod().setPayMethod("NET_BANKING")))
                    .build();
            FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest);
            Response fetchPcfDetailResp = fetchPcfDetail.execute();
            fetchPcfDetailResp.then()
                    .statusCode(200)
                    .spec(pcfSuccessResponse())
                    .body("body.consultDetails", Matchers.notNullValue())
                    .spec(pcfBodySuccessVerify("NET_BANKING", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.NET_BANKING.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.NET_BANKING.totalConvenienceCharges.value");
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("NET_BANKING")
                    .setChannelCode("ICICI")
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            processTxnV1Request.getHead().setChannelId("APP");
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("ICICI")
                .validatePaymentMode("NB")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("NB")
                .validateGatewayName("ICICI")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChargeAmount(totalPcfAmount)
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of netbanking transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_NB_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.payChannelOptions[0].channelCode",
                            Matchers.equalToIgnoringCase("ICICI"))
            ;
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("NET_BANKING")
                    .setChannelCode("ICICI")
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            processTxnV1Request.getHead().setChannelId("APP");
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("ICICI")
                .validatePaymentMode("NB")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("NB")
                .validateGatewayName("ICICI")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate success of postpaid transaction after scanning paytm QR for PCF allin1qr Merchant")
    public void validate_S_postpaid_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.merchantAccept",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.userAccountExist",
                            Matchers.equalToIgnoringCase("true"))
            ;
        }

        String totalTxnAmount = "";
        String totalPcfAmount = "";
        fetchPcfDetails:
        {
            FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(user.ssoToken(), "SSO")
                    .setMid(merchant.getId())
                    .setTxnAmount(orderDTO.getTXN_AMOUNT())
                    .setPayMethods(Arrays.asList(new PayMethod().setPayMethod("PAYTM_DIGITAL_CREDIT")))
                    .build();
            FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest);
            Response fetchPcfDetailResp = fetchPcfDetail.execute();
            fetchPcfDetailResp.then()
                    .statusCode(200)
                    .spec(pcfSuccessResponse())
                    .body("body.consultDetails", Matchers.notNullValue())
                    .spec(pcfBodySuccessVerify("PAYTM_DIGITAL_CREDIT", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.PAYTM_DIGITAL_CREDIT.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.PAYTM_DIGITAL_CREDIT.totalConvenienceCharges.value");
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                    .setMpin(new PaymentDTO().getPasscode())
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            processTxnV1Request.getHead().setChannelId("APP");
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode("Paytm Postpaid")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName("PAYTMCC")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChargeAmount(totalPcfAmount)
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of postpaid transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_postpaid_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.merchantAccept",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.userAccountExist",
                            Matchers.equalToIgnoringCase("true"))
            ;
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                    .setMpin(new PaymentDTO().getPasscode())
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            processTxnV1Request.getHead().setChannelId("APP");
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PAYTMCC")
                .validatePaymentMode("Paytm Postpaid")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("Paytm Postpaid")
                .validateGatewayName("PAYTMCC")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    /**
     * As discussed with ankit2.arora GV will not be displayed as paymentMode it will be displayed under wallet>subwallet
     *
     * @param theme
     * @throws Exception
     */
    @Parameters({"theme"})
    @Test(description = "Validate success of GV transaction after scanning paytm QR for PCF allin1qr Merchant")
    public void validate_S_GV_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        Double walletBalance = 3.00;
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.setZeroBalance(user);
            WalletHelpers.updateGVBalance(user, walletBalance);
            Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
            OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                    .setSSO_TOKEN("")
                    .setTXN_AMOUNT("2.00").build();

            generateQrCodeOrder(user, merchant, orderDTO, theme);
            Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                    .as("all in 1 QR is not displayed")
                    .isTrue();

            fetchQrPaymentDetails:
            {
                FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                        .setQRCodeId(qrCodeId.get())
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
                        .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                        .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                        .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                        .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                                Matchers.equalToIgnoringCase("false"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.userAccountExist",
                                Matchers.equalToIgnoringCase("true"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.merchantAccept",
                                Matchers.equalToIgnoringCase("true"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].isDisabled.status",
                                Matchers.equalToIgnoringCase("false"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo",
                                Matchers.notNullValue())
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo.accountBalance.value",
                                Matchers.equalToIgnoringCase(format.format(walletBalance)))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo.subWalletDetails.find{it.displayName == 'Gift Voucher'}.balance",
                                Matchers.equalToIgnoringCase(format.format(walletBalance)))
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
                        .body("body.consultDetails", Matchers.notNullValue())
                        .spec(pcfBodySuccessVerify("BALANCE", orderDTO.getTXN_AMOUNT()))
                ;
                totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalTransactionAmount.value");
                totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalConvenienceCharges.value");
            }

            fastforwardTxn:
            {
                FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                        .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                        .setPaymentMode("PPI")
                        .setQrCodeId(qrCodeId.get())
                        .setReqType("CLW_APP_PAY")
                        .setToken(user.ssoToken())
                        .setTokenType("SSO")
                        .setCustomerId(user.custId())
                        .setOrderAlreadyCreated("true")
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
                softly.assertThat(format.format(WalletHelpers.getGVBalance(user)))
                        .as("GV wallet balance mismatch")
                        .isEqualTo(format.format(walletBalance - Double.valueOf(totalTxnAmount)));
                softly.assertAll();
            }

            PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                    .validateStatus("TXN_SUCCESS")
                    .validateGatewayName("WALLET")
                    .validatePaymentMode("PPI")
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .AssertAll();
            new ResponsePage()
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .validateOrderId(orderDTO.getORDER_ID())
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateChargeAmount(totalPcfAmount)
                    .validatePaymentMode("PPI")
                    .assertAll();

        } else throw new RuntimeException("User is not Min Kyc Expired");
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of GV transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_GV_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.setZeroBalance(user);
            WalletHelpers.updateGVBalance(user, 2.00);
            Constants.MerchantType merchant = Constants.MerchantType.GV_UPI_INTENT;
            OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                    .setSSO_TOKEN("")
                    .setTXN_AMOUNT("2.00").build();

            generateQrCodeOrder(user, merchant, orderDTO, theme);
            Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                    .as("all in 1 QR is not displayed")
                    .isTrue();

            fetchQrPaymentDetails:
            {
                FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                        .setQRCodeId(qrCodeId.get())
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
                        .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                        .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                        .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                                Matchers.equalToIgnoringCase("false"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.userAccountExist",
                                Matchers.equalToIgnoringCase("true"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.merchantAccept",
                                Matchers.equalToIgnoringCase("true"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].isDisabled.status",
                                Matchers.equalToIgnoringCase("false"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo",
                                Matchers.notNullValue())
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo.accountBalance.value",
                                Matchers.equalToIgnoringCase("2.00"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo.subWalletDetails.find{it.displayName == 'Gift Voucher'}.balance",
                                Matchers.equalToIgnoringCase("2.00"))
                        .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'GIFT_VOUCHER'}.isDisabled.status",
                                Matchers.equalToIgnoringCase("false"))
                ;
            }

            fastforwardTxn:
            {
                FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                        .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                        .setPaymentMode("PPI")
                        .setReqType("CLW_APP_PAY")
                        .setToken(user.ssoToken())
                        .setTokenType("SSO")
                        .setCustomerId(user.custId())
                        .setOrderAlreadyCreated("true")
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
                        .isEqualToIgnoringCase("51051000100000000001");
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
                softly.assertThat(WalletHelpers.getGVBalance(user))
                        .as("")
                        .isEqualTo(0);
                softly.assertAll();
            }

            PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                    .validateStatus("TXN_SUCCESS")
                    .validateGatewayName("WALLET")
                    .validatePaymentMode("PPI")
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .AssertAll();
            new ResponsePage()
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .validateOrderId(orderDTO.getORDER_ID())
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode("PPI")
                    .assertAll();


        } else
            throw new RuntimeException("User is not Min Kyc Expired");
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of wallet transaction after scanning paytm QR for PCF allin1qr Merchant")
    public void validate_S_wallet_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 4.00);

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.userAccountExist",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.merchantAccept",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo",
                            Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo.accountBalance.value",
                            Matchers.equalToIgnoringCase("4.00"))
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
                    .body("body.consultDetails", Matchers.notNullValue())
                    .spec(pcfBodySuccessVerify("BALANCE", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalConvenienceCharges.value");
        }

        fastforwardTxn:
        {
            FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                    .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("PPI")
                    .setQrCodeId(qrCodeId.get())
                    .setReqType("CLW_APP_PAY")
                    .setToken(user.ssoToken())
                    .setTokenType("SSO")
                    .setCustomerId(user.custId())
                    .setOrderAlreadyCreated("true")
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
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("PPI")
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate success of wallet transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_wallet_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 2.00);

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.userAccountExist",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.merchantAccept",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo",
                            Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.payChannelOptions[0].balanceInfo.accountBalance.value",
                            Matchers.equalToIgnoringCase("2.00"))
            ;
        }

        fastforwardTxn:
        {
            FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                    .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("PPI")
                    .setReqType("CLW_APP_PAY")
                    .setQrCodeId(qrCodeId.get())
                    .setToken(user.ssoToken())
                    .setTokenType("SSO")
                    .setCustomerId(user.custId())
                    .setOrderAlreadyCreated("true")
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
                    .isEqualToIgnoringCase("51051000100000000001");
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
            softly.assertAll();
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("PPI")
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate success of EMI transaction after scanning paytm QR for PCF allin1qr Merchant")
    public void validate_S_EMI_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.payChannelOptions[0].emiType",
                            Matchers.equalToIgnoringCase("CREDIT_CARD"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.payChannelOptions[0].channelCode",
                            Matchers.equalToIgnoringCase("HDFC"))
            ;
        }

        String totalTxnAmount = "";
        String totalPcfAmount = "";
        fetchPcfDetails:
        {
            FetchPcfRequest fetchPcfRequest = new FetchPcfRequest.Builder(user.ssoToken(), "SSO")
                    .setMid(merchant.getId())
                    .setTxnAmount(orderDTO.getTXN_AMOUNT())
                    .setPayMethods(Arrays.asList(new PayMethod().setPayMethod("EMI")))
                    .build();
            FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest);
            Response fetchPcfDetailResp = fetchPcfDetail.execute();
            fetchPcfDetailResp.then()
                    .statusCode(200)
                    .spec(pcfSuccessResponse())
                    .spec(pcfBodySuccessVerify("EMI", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.EMI.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.EMI.totalConvenienceCharges.value");
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("EMI")
                    .setPlanId("HDFC|3")
                    .setEmiType("CREDIT_CARD")
                    .setCardNum(new PaymentDTO().getCreditCardNumber())
                    .setAuthMode("otp")
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            processTxnV1Request.getHead().setChannelId("APP");
            completeTxnInNewTab(processTxnV1Request);
        }
        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("EMI")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateChargeAmount(totalPcfAmount)
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Validate success of EMI transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_EMI_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("20.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.payChannelOptions[0].emiType",
                            Matchers.equalToIgnoringCase("CREDIT_CARD"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.payChannelOptions[0].channelCode",
                            Matchers.equalToIgnoringCase("HDFC"))
            ;
        }

        fetchEmiDetails:
        {
            FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest("SSO", user.ssoToken(), "HDFC", merchant.getId());
            Response fetchEmiDetailResponse = new FetchEMIDetail(fetchEMIDetailRequest, merchant.getId()).execute();
            fetchEmiDetailResponse.then()
                    .statusCode(200)
                    .body("body.resultInfo.resultStatus", Matchers.equalToIgnoringCase("S"),
                            "body.resultInfo.resultCode", Matchers.equalToIgnoringCase("0000"),
                            "body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                    .body("body.emiDetail", Matchers.notNullValue())
                    .body("body.emiDetail.emiChannelInfos", Matchers.notNullValue())
                    .body("body.emiDetail.emiChannelInfos.find{it.planId == 'HDFC|3'}", Matchers.notNullValue());
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("EMI")
                    .setPlanId("HDFC|3")
                    .setEmiType("CREDIT_CARD")
                    .setCardNum(new PaymentDTO().getCreditCardNumber())
                    .setAuthMode("otp")
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            processTxnV1Request.getHead().setChannelId("APP");
            completeTxnInNewTab(processTxnV1Request);
        }
        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("EMI")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate success of dc transaction after scanning paytm QR for pcf allin1qr Merchant")
    public void validate_S_dc_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
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
                    .setPayMethods(Arrays.asList(new PayMethod().setPayMethod("DEBIT_CARD")))
                    .build();
            FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest);
            Response fetchPcfDetailResp = fetchPcfDetail.execute();
            fetchPcfDetailResp.then()
                    .statusCode(200)
                    .spec(pcfSuccessResponse())
                    .spec(pcfBodySuccessVerify("DEBIT_CARD", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.DEBIT_CARD.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.DEBIT_CARD.totalConvenienceCharges.value");
        }

        /*
            As discussed with ankit arora in case of PTC actual txn amount need to send in request instead of totalTxnAmount
         */

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("DEBIT_CARD")
                    .setCardNum(new PaymentDTO().getDebitCardNumber())
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("DC")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("DC")
                .validateChargeAmount(totalPcfAmount)
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate success of dc transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_dc_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("DEBIT_CARD")
                    .setCardNum(new PaymentDTO().getDebitCardNumber())
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("DC")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("DC")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of cc transaction after scanning paytm QR for pcf allin1qr Merchant")
    public void validate_S_cc_PCF_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
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
                    .setPayMethods(Arrays.asList(new PayMethod().setPayMethod("CREDIT_CARD")))
                    .build();
            FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest);
            Response fetchPcfDetailResp = fetchPcfDetail.execute();
            fetchPcfDetailResp.then()
                    .statusCode(200)
                    .spec(pcfSuccessResponse())
                    .spec(pcfBodySuccessVerify("CREDIT_CARD", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.CREDIT_CARD.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.CREDIT_CARD.totalConvenienceCharges.value");
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("CREDIT_CARD")
                    .setCardNum(new PaymentDTO().getCreditCardNumber())
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("CC")
                .validateChargeAmount(totalPcfAmount)
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate success of cc transaction after scanning paytm QR for allin1qr Merchant")
    public void validate_S_cc_Txn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("CREDIT_CARD")
                    .setCardNum(new PaymentDTO().getCreditCardNumber())
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validatePaymentMode("CC")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("CC")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of upi intent transaction after scanning paytm QR for PCF INTENT Merchant")
    public void validate_S_intentTxn_PCF_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
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
            staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
            Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                    .as("Result code mismatch")
                    .isEqualToIgnoringCase("SUCCESS");
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
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBL")
                .validatePaymentMode("UPI")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .validateChargeAmount(totalPcfAmount)
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate success of upi intent transaction after scanning paytm QR for INTENT Merchant")
    public void validate_S_intentTxn_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId.get())
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId.get()))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
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
            StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0")
                    .setOrderId(orderDTO.getORDER_ID())
                    .build();
            StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            Response response = staticQrUpiPSP.execute();
            staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
            Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                    .as("Result code mismatch")
                    .isEqualToIgnoringCase("SUCCESS");
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
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBL")
                .validatePaymentMode("UPI")
                .validateOrderid(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .assertAll();


    }

    @Owner(PULKIT)
    @Parameters({"theme"})
    @Test(description = "Validate alert msg on cashier page for pcf all in 1 qr")
    public void validate_Alert_Msg_For_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.pcfAllIn1QrAlertMsg().getText().equalsIgnoreCase(PCF_FEE_SCAN_QR_ALERT_MSG));
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();
    }

    @Owner(PULKIT)
    @Parameters({"theme"})
    @Test(description = "Validate no alert msg is displayed  on cashier page for pcf all in 1 qr after logging in")
    public void validate_No_Alert_Msg_For_allIn1Qr_after_loggingIn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.pcfAllIn1QrAlertMsg().getText().equalsIgnoreCase(PCF_FEE_SCAN_QR_ALERT_MSG));

        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        cashierPage.login(user);
        cashierPage.pcfAllIn1QrAlertMsg().assertNotVisible();
    }

    //Logging Out functionality is disabled now
//    @Owner(PULKIT)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Validate that alert msg is displayed again on cashier page for pcf all in 1 qr after logging out")
    public void validate_Alert_Msg_For_allIn1Qr_after_loggingOut(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();

        cashierPage.login(user);
        cashierPage.pcfAllIn1QrAlertMsg().assertNotVisible();
        cashierPage.logout(user);
        Assertions.assertThat(cashierPage.pcfAllIn1QrAlertMsg().getText().equalsIgnoreCase(PCF_FEE_SCAN_QR_ALERT_MSG));
    }

    @Owner(PULKIT)
    @Parameters({"theme"})
    @Test(description = "Validate alert msg on cashier page for onus pcf all in 1 qr")
    public void validate_Alert_Msg_For_Onus_allIn1Qr(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_ONUS;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        generateQrCodeOrder(user, merchant, orderDTO, theme);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.pcfAllIn1QrAlertMsg().getText().equalsIgnoreCase(PCF_FEE_SCAN_QR_ALERT_MSG));
        Assertions.assertThat(verifyAllInOneQrDisplayed(orderDTO, theme))
                .as("all in 1 QR is not displayed")
                .isTrue();
    }


    // ######  HELPER METHOD ###############

    private String calculatePCF(String txnAmount) {
        final Double convFee = 0.02;
        Double txnAmt = Double.valueOf(txnAmount);
        Double pcfFees = txnAmt * convFee;
        System.out.println("Calculated fees is: " + pcfFees);
        return format.format(pcfFees);
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
                .expectBody(paymode + ".feeAmount.value", Matchers.equalToIgnoringCase(calculatePCF(txnAmount)))
                .expectBody(paymode + ".taxAmount.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalConvenienceCharges.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalTransactionAmount.value", Matchers.notNullValue())
                .build();
    }

    private boolean verifyAllInOneQrDisplayed(OrderDTO orderDTO, String theme) {
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.pause(3);
        cashierPage.imgScanPayQRCode().assertVisible();
        String rawQrCodeString = PGPHelpers.getQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        if (rawQrCodeString.contains("upi://pay")) {
            Map<String, String> m = PGPHelpers.parseUpiIntentDeepLink(rawQrCodeString);
            if (m.containsKey("paytmqr")) {
                String paytmqr = m.get("paytmqr");
                qrCodeId.set(paytmqr);
                GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(paytmqr);      //validating QR code with wallet
                getQRCodeInfoApi.execute()
                        .then()
                        .statusCode(200)
                        .body("response.mappingId", Matchers.equalToIgnoringCase(orderDTO.getMID()),
                                "response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()));
                return true;
            } else return false;
        }
        return false;
    }

    private boolean verifyUpiOnlyQrDisplayed(String theme) {
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.pause(5);
        String rawQrCodeString = PGPHelpers.getQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));

        if (rawQrCodeString.contains("upi://pay")) {
            Map<String, String> m = PGPHelpers.parseUpiIntentDeepLink(rawQrCodeString);
            if (m.containsKey("paytmqr"))
                return false;
            else
                return true;
        }
        return false;
    }

    private boolean verifyWalletOnlyQr(OrderDTO orderDTO, String theme) {
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.pause(3);
        String rawQrCodeString = PGPHelpers.getQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        if (rawQrCodeString.contains("upi://pay"))
            return false;
        qrCodeId.set(rawQrCodeString);
        GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(rawQrCodeString);      //validating QR code with wallet
        getQRCodeInfoApi.execute()
                .then()
                .statusCode(200)
                .body("response.mappingId", Matchers.equalToIgnoringCase(orderDTO.getMID()),
                        "response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()));
        return true;
    }


    @Step
    private void performAddMoneyMP_txn(String theme, User user, OrderDTO orderDTO) {
        ArrayList<String> tabs = null;
        try {
            PGPHelpers.launchNewTab();
            tabs = new ArrayList<String>(DriverManager.getDriver().getWindowHandles());
            if (tabs.size() == 1)
                throw new SkipException("Unable to launch new browser tab");
            DriverManager.getDriver().switchTo().window(tabs.get(1));
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.DC);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_SUCCESS")
                    .validatePaymentMode("DC")
                    .assertAll();
        } finally {
            DriverManager.getDriver().close();
            DriverManager.getDriver().switchTo().window(tabs.get(0));
        }
    }

    @Step("Generate QR Code order")
    private void generateQrCodeOrder(User user, Constants.MerchantType merchant, OrderDTO orderDTO, String theme) {
        checkoutPage.createOrder(orderDTO);
        checkoutPage.waitUntilLoads();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
    }

    @Step("Scan and complete transaction in new tab")
    private void completeTxnInNewTab(ProcessTxnV1Request processTxnV1Request) throws JsonProcessingException {
        ArrayList<String> tabs = null;
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        try {
            String json = null;
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(processTxnV1Response);
            PGPHelpers.launchNewTab();
            tabs = new ArrayList<String>(DriverManager.getDriver().getWindowHandles());
            if (tabs.size() == 1)
                throw new SkipException("Unable to launch new browser tab");
            DriverManager.getDriver().switchTo().window(tabs.get(1));
            new NativePlusHoldpayPage().
                    launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                    .fillAndSubmitJsonForm(json);
            ResponsePage responsePage = new ResponsePage();
            responsePage.validateStatus("TXN_SUCCESS")
                    .assertAll();
        } finally {
            DriverManager.getDriver().close();
            DriverManager.getDriver().switchTo().window(tabs.get(0));
        }
    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 10 and sub wallet amount is 7 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount10SubWallet7AvailableBalanceMW10FW10() throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        Constants.MerchantType mid = Constants.MerchantType.UPI_INTENT;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="10";
        String foodAmount="7";
        String mainwallettxnamount="3";
        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(mid, txnAmount, orderId, foodAmount);
        JsonPath sendPaymentRequestApijson=  sendPaymentRequestApi.execute().jsonPath();
        Assertions.assertThat(sendPaymentRequestApijson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("SUCCESS");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setExtendInfo(new ExtendInfo().setadditionalInfo("tr:"+orderId+"|orderAlreadyCreated:true|REQUEST_TYPE:UPI_POS_ORDER"))
                .setMid(mid.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid.getId(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(Double.parseDouble(String.valueOf(Double.valueOf(txnAmount)+Double.valueOf(foodAmount)))));

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(mid.getId(),orderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath jsonFastForward = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Successful.");
        Assertions.assertThat(jsonFastForward.getString("body.extendInfo.subwalletWithdrawMaxAmountDetails")).isEqualTo("{\"FOOD\":"+foodAmount+"}");
        WalletHelpers.validateBalance(user, MainWalletAmount-Double.valueOf(mainwallettxnamount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(FoodWalletAmount-Double.valueOf(foodAmount)) ;
    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 15 and sub wallet amount is 12 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount15SubWallet12AvailableBalanceMW10FW10() throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        Constants.MerchantType mid = Constants.MerchantType.UPI_INTENT;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="15";
        String foodAmount="12";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(mid, txnAmount, orderId, foodAmount);
        JsonPath sendPaymentRequestApijson=  sendPaymentRequestApi.execute().jsonPath();
        Assertions.assertThat(sendPaymentRequestApijson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("SUCCESS");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setExtendInfo(new ExtendInfo().setadditionalInfo("tr:"+orderId+"|orderAlreadyCreated:true|REQUEST_TYPE:UPI_POS_ORDER"))
                .setMid(mid.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid.getId(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(MainWalletAmount+FoodWalletAmount));

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(mid.getId(),orderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
                .build();

        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        JsonPath jsonFastForward = fastForwardResponse.then()
                .statusCode(200)
                .extract().jsonPath();
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonFastForward.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Successful.");
        Assertions.assertThat(jsonFastForward.getString("body.extendInfo.subwalletWithdrawMaxAmountDetails")).isEqualTo("{\"FOOD\":"+foodAmount+"}");
        WalletHelpers.validateBalance(user, Double.valueOf(txnAmount)-FoodWalletAmount);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.0) ;
    }

    @Feature("PGP-27183")
    @Owner("Karmvir")
    @Description("Automation Jira:-PGP-27183")
    @Test(description = "Validate that when Txn amount is 15 and sub wallet amount is 3 (Available balance MW-10 and FW-10) txn using FastForward")
    public void validateTxnAmount15SubWallet3AvailableBalanceMW10FW10() throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user ,FoodWalletAmount );
        Constants.MerchantType mid = Constants.MerchantType.UPI_INTENT;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="15";
        String foodAmount="3";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(mid, txnAmount, orderId, foodAmount);
        JsonPath sendPaymentRequestApijson=  sendPaymentRequestApi.execute().jsonPath();
        Assertions.assertThat(sendPaymentRequestApijson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("SUCCESS");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setExtendInfo(new ExtendInfo().setadditionalInfo("tr:"+orderId+"|orderAlreadyCreated:true|REQUEST_TYPE:UPI_POS_ORDER"))
                .setMid(mid.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid.getId(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(Double.valueOf(foodAmount)+MainWalletAmount));

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(mid.getId(),orderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
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
    public void validateTxnAmount25SubWallet15AvailableBalanceMW10FW10() throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user,MainWalletAmount );
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        Constants.MerchantType mid = Constants.MerchantType.UPI_INTENT;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="25";
        String foodAmount="15";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(mid, txnAmount, orderId, foodAmount);
        JsonPath sendPaymentRequestApijson=  sendPaymentRequestApi.execute().jsonPath();
        Assertions.assertThat(sendPaymentRequestApijson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("SUCCESS");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setExtendInfo(new ExtendInfo().setadditionalInfo("tr:"+orderId+"|orderAlreadyCreated:true|REQUEST_TYPE:UPI_POS_ORDER"))
                .setMid(mid.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid.getId(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(MainWalletAmount+FoodWalletAmount));

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(mid.getId(),orderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
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
    public void validateTxnAmount25SubWallet12AvailableBalanceMW10FW10() throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        Constants.MerchantType mid = Constants.MerchantType.UPI_INTENT;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="25";
        String foodAmount="12";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(mid, txnAmount, orderId, foodAmount);
        JsonPath sendPaymentRequestApijson=  sendPaymentRequestApi.execute().jsonPath();
        Assertions.assertThat(sendPaymentRequestApijson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("SUCCESS");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setExtendInfo(new ExtendInfo().setadditionalInfo("tr:"+orderId+"|orderAlreadyCreated:true|REQUEST_TYPE:UPI_POS_ORDER"))
                .setMid(mid.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid.getId(),
                orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(MainWalletAmount+FoodWalletAmount));

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(mid.getId(),orderId ,txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setOrderAlreadyCreated("true")
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
    public void validateTxnAmount10SubWallet7AvailableBalanceMW10FW10PTC() throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET);
        double MainWalletAmount= 10.0;
        double FoodWalletAmount= 10.0;
        WalletHelpers.modifyBalance(user, MainWalletAmount);
        WalletHelpers.updateFoodWalletBalance(user , FoodWalletAmount);
        Constants.MerchantType mid = Constants.MerchantType.UPI_INTENT;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="10";
        String foodAmount="7";
        String mainwallettxnamount="3";
        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(mid, txnAmount, orderId, foodAmount);
        JsonPath sendPaymentRequestApijson=  sendPaymentRequestApi.execute().jsonPath();
        Assertions.assertThat(sendPaymentRequestApijson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("SUCCESS");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setExtendInfo(new ExtendInfo().setadditionalInfo("tr:"+orderId+"|orderAlreadyCreated:true|REQUEST_TYPE:UPI_POS_ORDER"))
                .setMid(mid.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid.getId(),
                orderId, fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).
                isEqualToIgnoringCase(new DecimalFormat("0.00").format(Double.parseDouble(String.valueOf(Double.valueOf(txnAmount)+Double.valueOf(foodAmount)))));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid.getId(), "SSO", user.ssoToken(), orderId, txnAmount)
                .setPaymentMode("BALANCE")
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        WalletHelpers.validateBalance(user, MainWalletAmount-Double.valueOf(mainwallettxnamount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(FoodWalletAmount-Double.valueOf(foodAmount)) ;
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant Accepting Postpaid & User has postpaid Enabled & Preference is ON and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should  be true display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_display_in_the_fpo_repsone() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_POSTPAID;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="150";
        String food="0";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(merchant, txnAmount, orderId,food);
        JsonPath paymentrequest=  sendPaymentRequestApi.execute().jsonPath();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setMid(merchant.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(),
                orderId, fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAThresholdValue")).isEqualTo("1500");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAEnabled")).isEqualTo("true");
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant Accepting Postpaid & User has postpaid Enabled & Preference is OFF and FF4j theia.enable.postpaid.2FA is OFF postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should not display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_not_display_in_the_fpo_response() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_POSTPAID1;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="150";
        String food="0";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(merchant, txnAmount, orderId,food);
        JsonPath paymentrequest=  sendPaymentRequestApi.execute().jsonPath();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setMid(merchant.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(),
                orderId, fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAThresholdValue")).isNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAEnabled")).isNull();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant not Accepting Postpaid & User has not postpaid Enabled & Preference is ON and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should not display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_not_be_display_in_the_fpo_repsone_pref_ff4j_is_on() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_NO_POSTPAID;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="150";
        String food="0";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(merchant, txnAmount, orderId,food);
        JsonPath paymentrequest=  sendPaymentRequestApi.execute().jsonPath();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setMid(merchant.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(),
                orderId, fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAThresholdValue")).isNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAEnabled")).isNull();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant  Accepting Postpaid & User has postpaid Enabled & Preference is OFF and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should  display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_be_displayed_in_the_fpo_repsone_pref_is_off_ff4j_is_on() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_POSTPAID;
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount="150";
        String food="0";

        SendPaymentRequestApi sendPaymentRequestApi = new SendPaymentRequestApi(merchant, txnAmount, orderId,food);
        JsonPath paymentrequest=  sendPaymentRequestApi.execute().jsonPath();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setGenerateOrderId("false")
                .setMid(merchant.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(),
                orderId, fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAThresholdValue")).isEqualTo("1500");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.postpaid2FAEnabled")).isEqualTo("true");
    }
}
