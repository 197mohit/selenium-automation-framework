package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.CloseOrderAPI;
import com.paytm.api.CreateUpiLinkApi;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.createUPILink.CreateUpiLinkRequest;
import com.paytm.dto.createUPILink.CreateUpiLinkResponse;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CheckoutPage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.PUSPA;
import static com.paytm.appconstants.Constants.Owner.TARUN;

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Somesh Saxena")
public class UpiIntent extends PGPBaseTest {

    @Test(description = "Verfy deeplink generated successful for upi intent")
    public void validateUpiIntentLinkGenerated() {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(
                fetchPaymentOptResponse, PayMethodType.UPI.toString(), false))
                .as(PayMethodType.UPI.toString() + " status mismatched")
                .isTrue();
        CreateUpiLinkRequest createUpiLinkRequest = new CreateUpiLinkRequest(merchant.getId(), merchant.getKey(), txnToken,
                "", "", initTxnDTO.getBody().getOrderId());
        CreateUpiLinkResponse createUpiLinkResponse = new CreateUpiLinkApi(createUpiLinkRequest)
                .execute()
                .as(CreateUpiLinkResponse.class);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(createUpiLinkResponse.getBody().getResultCodeId())
                .as("ResultCodeId mismatch")
                .isEqualToIgnoringCase("001");
        softly.assertThat(createUpiLinkResponse.getBody().getResultCode())
                .as("ResultCode mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        softly.assertAll();
    }

    @Test(description = "Verify Success Upi Intent transaction, also validate " +
            "fetchPayOption API when SSo token is not passed in request, for Paytm Money flow"
            , dependsOnMethods = {"validateUpiIntentLinkGenerated"})
    public void successUpiIntent_PaytmMoney() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions
                .assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponse, PayMethodType.UPI.toString(),false))
                .as(PayMethodType.UPI.toString() + " status mismatched")
                .isTrue();

        CreateUpiLinkRequest createUpiLinkRequest = new CreateUpiLinkRequest(merchant.getId(), merchant.getKey(), txnToken,
                "", "", initTxnDTO.getBody().getOrderId());
        CreateUpiLinkResponse createUpiLinkResponse = new CreateUpiLinkApi(createUpiLinkRequest)
                .execute()
                .as(CreateUpiLinkResponse.class);

        PGPHelpers.generateUpiIntentPayRequest(createUpiLinkResponse, initTxnDTO.txnAmountFromBody(), merchant.getId(), Constants.Intent_Callback.SUCCESS);
        PGPHelpers.getTxnStatus(Constants.MerchantType.UPI_INTENT.getId(), initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBL")
                .validatePaymentMode("UPI")
                .AssertAll();
    }

    @Test(description = "Verify Success Upi Intent transaction, also validate " +
            "fetchPayOption API when SSo token is not passed in request, for Paytm Money flow")
    public void testOrderSuccessByUpiIntent_PaytmMoneyWhenNonMatchingWebsiteProvided() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions
                .assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponse, PayMethodType.UPI.toString(),false))
                .as(PayMethodType.UPI.toString() + " status mismatched")
                .isTrue();

        CreateUpiLinkRequest createUpiLinkRequest = new CreateUpiLinkRequest(merchant.getId(), merchant.getKey(), txnToken,
                "", "", initTxnDTO.getBody().getOrderId());
        CreateUpiLinkResponse createUpiLinkResponse = new CreateUpiLinkApi(createUpiLinkRequest)
                .execute()
                .as(CreateUpiLinkResponse.class);

        PGPHelpers.generateUpiIntentPayRequest(createUpiLinkResponse, initTxnDTO.txnAmountFromBody(), merchant.getId(), Constants.Intent_Callback.SUCCESS);
        PGPHelpers.getTxnStatus(Constants.MerchantType.UPI_INTENTONLY.getId(), initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBL")
                .validatePaymentMode("UPI")
                .AssertAll();
    }

    @Owner(TARUN)
    @Feature("PGP-26896")
    @Test(description = "To test if payerName  & payerPSP is getting sent in ACQUIRING_PAY_ORDER request in /v1/order/pay/upipsp API")
    public void cashierPayUPIPSPRequestValidation() throws InterruptedException {
        String payerName = "Tarun Papneja";
        String payerPSP = "PhonePay";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, "enhancedweb")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), orderDTO.getTXN_AMOUNT())
                .setOrderId(orderDTO.getORDER_ID())
                .setPayerName(payerName)
                .setPayerPSP(payerPSP)
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        staticQrUpiPSP.execute();

        String reqMsgId =staticQrUpiPSPRequest.getHeader().getRequestMsgId();

        String grepcmd = "grep \"" + reqMsgId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\" | grep \"" +orderDTO.getORDER_ID() +"\"";
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaLogs).as("Theia is not sending payerName & payerPSP in ACQUIRING_PAY_ORDER request").contains("payerName","payerPSP",payerName,payerPSP);

    }

    @Owner(TARUN)
    @Feature("PGP-26896")
    @Test(description = "To test if payerName  & payerPSP is getting sent in ACQUIRING_CREATE_ORDER_AND_PAY request in /v1/order/pay/upipsp API")
    public void copUPIPSPRequestValidation() throws InterruptedException {
        String payerName = "Tarun Papneja";
        String payerPSP = "PhonePay";

        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0")
                .setPayerName(payerName)
                .setPayerPSP(payerPSP)
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response staticResponse = staticQrUpiPSP.execute();

        String reqMsgId =staticQrUpiPSPRequest.getHeader().getRequestMsgId();
        String orderId = staticResponse.jsonPath().getString("body.orderId");

        String grepcmd = "grep \"" + reqMsgId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep \"" +orderId +"\"";
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaLogs).as("Theia is not sending payerName & payerPSP in ACQUIRING_CREATE_ORDER_AND_PAY request").contains("payerName","payerPSP",payerName,payerPSP);
    }

    @Test(description = "Verify Success Upi Intent transaction, Static QR")
    public void successUpiIntent_staticQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
    }

    @Test(description = "Verify Failed Upi Intent transaction, Static QR")
    public void failUpiIntent_staticQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0")
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.FAIL.getStatus())
                .setResponseCode(Constants.Intent_Callback.FAIL.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.FAIL.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(response.jsonPath().getString("body.resultCodeId"))
                .as("resultCodeId mismatch")
                .isEqualToIgnoringCase("009");
        // Adding this code to manually close the order as it's not getting closed and txn is going in pending state.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(staticQrUpiPSPResponse.getBody().getOrderId()).setMid(Constants.MerchantType.UPI_INTENTONLY.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
        PGPHelpers.getTxnStatus(merchant.getId(), staticQrUpiPSPResponse.getBody().getOrderId())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("227")
                .AssertAll();
    }

    @Test(description = "Verify success Upi Intent transaction, also validate fetchPayOption API " +
            "for deeplink (PAYMENT_MODE=UPI_INTENT)")
    public void successUpiIntent_deepLink() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO =
                NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, PayMethodType.UPI.toString(), false))
                .as(PayMethodType.UPI.toString() + " paymethod status mismatched")
                .isTrue();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(initTxnDTO.orderFromBody())
                .setExternalSerialNo(map.get("tr"))
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PGPHelpers.getTxnStatus(merchant.getId(), initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .AssertAll();
    }

    @Issue("PGP-15732")
    @Test(description = "Verify fail Upi Intent transaction for deeplink (PAYMENT_MODE=UPI_INTENT)")
    public void failUpiIntent_deepLink() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("2.0")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(initTxnDTO.orderFromBody())
                .setExternalSerialNo(map.get("tr"))
                .setTxnStatus(Constants.Intent_Callback.FAIL.getStatus())
                .setResponseCode(Constants.Intent_Callback.FAIL.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.FAIL.getRespmsg())
                .setMid(merchant.getId());
        Response response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(response.jsonPath().getString("body.resultCodeId"))
                .as("resultCodeId mismatch")
                .isEqualToIgnoringCase("009");
        PGPHelpers.getTxnStatus(merchant.getId(), initTxnDTO.orderFromBody())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("227")
                .AssertAll();
    }


    @Test(description = "Verify that new deeplink - paytm deeplink url is returned in response if 'osType sent' is 'IOS' and no pspApp parameter is sent in request ")
    public void validateNewDeeplinkPaytmReturnedWhenOStypeIsIOS() {


        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .setpspApp(null)
                .setosType("ios")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

        Assert.assertTrue(map.containsKey("paytmmp://upi/pay?pa"));
    }



    @Test(description = "Verify that new deeplink -  paytm deeplink url is returned in response if 'osType sent' is 'IOS' and pspApp parameter sent is other than 'Paytm/GPAY/Phonepe' in request  ")
    public void validateNewDeeplinkPaytmReturnedWhenOStypeIsIOSAndPSPOtherThanPaytmGPAYPhonepe() {

        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .setosType("ios")
                .setpspApp("BHIM")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

        Assert.assertTrue(map.containsKey("paytmmp://upi/pay?pa"));
    }




    @Test(description = "Verify that new deeplink  - paytm deeplink url is returned in response if 'osType = IOS' and 'pspApp=Paytm' parameter is sent in request")
    public void validateNewDeeplinkPaytmReturnedWhenOStypeIsIOSAndPSPIsPaytm() {

        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .setosType("ios")
                .setpspApp("Paytm")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

        Assert.assertTrue(map.containsKey("paytmmp://upi/pay?pa"));
    }




    @Test(description = "Verify that new deeplink tez deeplink url is returned in response if 'osType = IOS' and 'pspApp=GPAY' parameter is sent in request ")
    public void validateNewDeeplinkTezReturnedWhenOStypeIsIOSAndPSPIsGpay(){

        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .setosType("ios")
                .setpspApp("GPAY")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

        Assert.assertTrue(map.containsKey("tez://upi/pay?pa"));
    }


    @Test(description = "Verify that new deeplink phonepe deeplink url is returned in response if 'osType = IOS' and 'pspApp=Phonepe' parameter is sent in request")
    public void validateNewDeeplinkPhonepayReturnedWhenOStypeIsIOSAndPSPIsPhonePay() {

        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .setosType("ios")
                .setpspApp("phonepe")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());

        Assert.assertTrue(map.containsKey("phonepe://upi/pay?pa"));
    }


    @Test(description = "Verify that system returnes old deeplink if 'osType' is not IOS no matter what pspApp is sent in request")
    public void validateOldDeeplinkIsReturnedWhenOsTypeIsOtherThanIOS() {

        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENTONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("2.0")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .setosType("android")
                .setpspApp("phonepe")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assert.assertTrue(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink().contains("upi://pay?pa"));
    }

    @Owner(PUSPA)
    @Feature("PGP-35537")
    @Test(description = "To test qrCodeId to be send in ACQUIRING_CREATE_ORDER_AND_PAY request in /v1/order/pay/upipsp API")
    public void verifyqrCodeIdInRequest() throws InterruptedException {
        String payeeVPA = "paytmqr2810050501015e8uln3x8b0m@paytm";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0")
                .setPayeeVpa(payeeVPA)
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response staticResponse = staticQrUpiPSP.execute();

        String reqMsgId =staticQrUpiPSPRequest.getHeader().getRequestMsgId();
        String orderId = staticResponse.jsonPath().getString("body.orderId");
        String grepcmd = "grep \"" + reqMsgId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep \"" +orderId +"\"";
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaLogs).contains("\"qrCodeId\":\"2810050501015E8ULN3X8B0M\"");
    }


}