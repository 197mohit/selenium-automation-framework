package com.paytm.apphelpers;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.paytm.RefundSucessNotifyPeon;
import com.paytm.api.*;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.dto.EdcRequestDto.EdcRequest;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.FastForwardRequestDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.PaymentModes;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.api.alipay.BizOrderSearch;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.testng.SkipException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QRHelper {

    private static CheckoutPage checkoutPage = new CheckoutPage();

    @Step
    public static OrderDTO generateDynamicQROrder(Constants.MerchantType merchantType, String theme, User user) {
        OrderDTO orderDTO = null;
        orderDTO = new OrderFactory.Hybrid(merchantType, theme, user)
                .setTXN_AMOUNT("20.00")
                .setSSO_TOKEN("")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String extendInfo = getExtendedInfoBizOrder(orderDTO.getORDER_ID());
        if (extendInfo == null)
            Assertions.fail("Order not generated at P+");
        if (!(extendInfo.contains("requestType") && extendInfo.contains("DYNAMIC_QR")))
            Assertions.fail("Order not generated at P+");
        return orderDTO;
    }


    @Step
    public static boolean validatePostPaidOnboardingEnabled(FetchPaymentOptResponseDTO fetchPaymentOptResponse, String paymentMode) {
        List<PaymentModes> paymodes = fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes();


        for (PaymentModes payMode : paymodes) {
            String payModeName = payMode.getPaymentMode();
            if (payModeName.equalsIgnoreCase(paymentMode)) {
                boolean onboardStatus = payMode.getOnboarding();
                Assertions.assertThat(onboardStatus).as(paymentMode + " Onboarding status is false").isTrue();
                return true;
            }
        }
        return false;
    }

    @Step
    public static boolean validatePaymentModeEnabled(FetchPaymentOptResponseDTO fetchPaymentOptResponse, String paymentMode, boolean payModeStatus) {
        List<PaymentModes> paymodes = fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes();
        for (PaymentModes payMode : paymodes) {
            String payModeName = payMode.getPaymentMode();
            if (payModeName.equalsIgnoreCase(paymentMode)) {
                String status = payMode.getIsDisabled().getStatus();
                if (Boolean.toString(payModeStatus).equalsIgnoreCase(status))
                    return true;
            }
        }
        return false;
    }

    public static List<String> extractAllPaymentModes(FetchPaymentOptResponseDTO fetchPaymentOptResponse) {
        List<String> allPayModes = new ArrayList<>();
        List<PaymentModes> paymodes = fetchPaymentOptResponse.getBody().getMerchantPayOption().getPaymentModes();
        for (PaymentModes payMode : paymodes) {
            allPayModes.add(payMode.getPaymentMode());
         }

        return allPayModes;
    }

    public static String getExtendedInfoBizOrder(String orderId) throws NullPointerException {
        BizOrderSearch bizOrderSearch = new BizOrderSearch(orderId);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Response response = bizOrderSearch.execute();
        if (!(response.getStatusCode() == 200))
            throw new SkipException("Biz order is not successfull");
        try {
            return response.jsonPath().getString("response.body.orders[0].extendInfo");
        } catch (NullPointerException ex) {
            throw new SkipException("Exception occurred when fetching response.body.orders[0].extendInfo from " +
                    "response of bizOrderSearch", ex);
        }
    }

    public static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, OrderDTO orderDTO, boolean generateOrderId) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .build();
        return PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO,generateOrderId);
    }

    public static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, OrderDTO orderDTO, boolean generateOrderId,String emiOptions) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setEMIOption(emiOptions)
                .build();
        return PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO,generateOrderId);
    }

    public static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, OrderDTO orderDTO, boolean generateOrderId, Double amount) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(orderDTO.getMID())
                .setToken(user.ssoToken())
                .setAmount(amount)
                .build();
        return PGPHelpers.executeFetchPaymentOpt(orderDTO.getMID(), orderDTO.getORDER_ID(), fetchPaymentOptionsDTO, generateOrderId);
    }

    public static String executeProcessTransactionV1(ProcessTxnV1Request processTxnV1Request) {
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        return jsonObject.toJSONString();
    }

    public static String executeFastForwardIVR(FastForwardRequestDTO fast) {

        FastForward fastForward = new FastForward(fast);
        Response response = fastForward.execute();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        return jsonObject.toJSONString();
    }

    public static String executeFastForwardAPP(FastForwardAppRequest fast) {

        FastForward fastForward = new FastForward(fast);
        Response response = fastForward.execute();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        return jsonObject.toJSONString();
    }

    public static JsonPath executeFastForwardApp(FastForwardAppRequest fast)
    {
        FastForward fastForward = new FastForward(fast);
        Response response = fastForward.execute();
        JSONObject jsonObject = new JSONObject();
        JsonPath fastFwdResponse = response.jsonPath();
        return fastFwdResponse;
    }



    @Step
    @Deprecated
    public static OrderDTO generatePaymentServiceQROrder(Constants.MerchantType merchantType, String txnAmount,String theme, User user) {
        OrderDTO orderDTO = null;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType,txnAmount,OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        orderDTO = new OrderFactory.Hybrid(merchantType, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .setORDER_ID(OrderId)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        String extendInfo = getExtendedInfoBizOrder(orderDTO.getORDER_ID());
        if (extendInfo == null)
            Assertions.fail("Order not generated at P+");
        if (!(extendInfo.contains("requestType") && extendInfo.contains("DYNAMIC_QR")))
            Assertions.fail("Order not generated at P+");

        return orderDTO;
    }

    public static Response generateEdcOrder(Constants.MerchantType merchantType, String amount, String theme) {
        String OrderId = CommonHelpers.generateOrderId();
        EdcRequest edcRequest = new EdcRequest.Builder(merchantType.getId(),OrderId, amount).build();
        PaymentService paymentService = new PaymentService(edcRequest);
        Response response = paymentService.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        return response;
    }
    public static OrderDTO generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType merchantType,String txnAmount, User user) {

        String qrCodeId = generateQRViaWallet(merchantType);
        //FetchQRPaymentDetails

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Static QR configuration is incorrect").isEqualTo(null);
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Static QR configuration is incorrect").isEqualTo("false");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");

        //OrderDTO created only for assertion purpose
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType.getId(),merchantType.getKey(),"enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .setORDER_ID(orderId)
                .build();
        orderDTO.setTxnId(qrCodeId); //QRCode ID

        return orderDTO;
    }

    public static OrderDTO generateQRAndFetchQRPaymentDetailsStaticWithMerchantLimitList(Constants.MerchantType merchantType,String txnAmount, User user) {

        String qrCodeId = generateQRViaWallet(merchantType);
        //FetchQRPaymentDetails

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Static QR configuration is incorrect").isEqualTo(null);
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Static QR configuration is incorrect").isEqualTo("false");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantLimitInfo.merchantPaymodesLimits")).isNotNull();

        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");

        //OrderDTO created only for assertion purpose
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType.getId(),merchantType.getKey(),"enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .setORDER_ID(orderId)
                .build();
        orderDTO.setTxnId(qrCodeId); //QRCode ID

        return orderDTO;
    }


    public static OrderDTO generateQRAndFetchQRPaymentDetailsPaymentService(Constants.MerchantType merchantType, String txnAmount , User user,boolean isEDC) {
        String qrCodeId;
        if(!isEDC)
         qrCodeId = generateQRViaPaymentService(merchantType,txnAmount);
        else
            qrCodeId = generateQRViaPaymentServiceEDC(merchantType,txnAmount,isEDC);
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

        //OrderDTO created only for assertion purpose
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType.getId(),merchantType.getKey(),"enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .setORDER_ID(orderId)
                .build();
        orderDTO.setTxnId(qrCodeId); //QRCode ID

        return orderDTO;
    }


    public static String generateQRViaWallet(Constants.MerchantType merchantType)
    {
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"");
//        JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        System.out.println(resp);
        JsonPath generateJson = JsonPath.given(resp);

        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
//        String jsonString = "{\"requestGuid\":null,\"orderId\":null,\"status\":\"SUCCESS\",\"statusCode\":\"200\",\"statusMessage\":\"SUCCESS\",\"response\":[{\"stickerId\":\"10000000000I3\",\"qrCodeId\":\"10002x\",\"upiHandle\":null,\"batchId\":null,\"mapResponse\":{\"mappingId\":\"AUTOQ846869232206781\",\"mappingType\":\"2\",\"typeOfQrCode\":\"UPI_QR_CODE\",\"status\":\"1\",\"displayName\":\"ankit34708655987864\",\"category\":null,\"subCategory\":null,\"tagLine\":null,\"secondaryPhoneNumber\":\"\",\"posId\":null,\"metadata\":{\"requestType\":\"UPI_QR_CODE\",\"industryType\":\"Retail\",\"channelId\":\"QRCODE\",\"mid\":\"AUTOQ846869232206781\",\"merchantName\":\"PG2_ENABLED\",\"qrInfo\":{\"handle\":\"upi://pay?pa=paytmqr10002x@paytm&pn=Paytm\"},\"additionalInfo\":{\"shopId\":\"7894165529674487414\",\"kybId\":\"6776562783592985660\"}},\"isUpdated\":false}}]}";
//        System.out.println(jsonString);
//        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonString);
//        Assertions.assertThat(jsonObject.getString("statusCode")).isEqualTo("200");
//        String qrCodeId = jsonObject.getString("response.qrCodeId").replaceAll("\\p{P}", "");
//        return qrCodeId;

    }

    public static String generateQRViaPaymentService(Constants.MerchantType merchantType,String txnAmount)
    {
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType,txnAmount,OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        return qrCodeId;
    }

    public static String generateQRViaPaymentServiceEDC(Constants.MerchantType merchantType,String txnAmount,boolean isEDC)
    {
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType,txnAmount,OrderId,isEDC);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        return qrCodeId;
    }

    public static TxnStatus validateTxnStatus(OrderDTO orderDTO,String payMode,String gateway)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(gateway)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
             //   .validateCurrentTxnCount(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static void validateSuccessPeonStaticQR(OrderDTO orderDTO, String payMode, String gatewayName)
    {
        //Removed bankName from peon assertion to get it called for UPI payMode as well
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME","comments", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","Masked_customer_mobile_number","TXNTYPE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID","udf_3", "udf_2", "TXNDATETIME","udf_1","REFUNDAMT", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gatewayName),
                peon.mercUnqRef().equals("").not(),
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

    }


    public static void validateSuccessPeonDynamicQR(OrderDTO orderDTO, String payMode, String gatewayName)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","TXNTYPE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME","REFUNDAMT", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gatewayName),
                peon.mercUnqRef().equals("vivek4"),
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

    }


    public static void validateSuccessRefundNotifyQR(Constants.MerchantType merchantType,OrderDTO orderDTO)
    {
        RefundSucessNotifyPeon refundSucessNotifyPeon = new RefundSucessNotifyPeon(orderDTO.getORDER_ID(),orderDTO.getMID());
        refundSucessNotifyPeon.validateBasicDetails(merchantType,orderDTO);
    }

    public static void validateSuccessSMSQR(OrderDTO orderDTO)
    {
        SMSPrimary smsPrimary = new SMSPrimary(orderDTO.getORDER_ID());
        String smsToMerchant = smsPrimary.execute().jsonPath().getString("message");
        Assertions.assertThat(smsToMerchant).as("Success SMS message is incorrect").contains("paid by");

    }

    public static void validateFailureSMSQR(String orderId)
    {
        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        String smsToMerchant = smsPrimary.execute().jsonPath().getString("message");
        Assertions.assertThat(smsToMerchant).as("Failure SMS message is incorrect").contains("has failed").contains(orderId);

    }

    public static void validateRefund(OrderDTO orderDTO)
    {
        PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(),orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), orderDTO.getTxnId(), "");
        PGPHelpers.getRefundStatus(orderDTO.getMID(),orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                .validateSuccessRefund()
                .assertAll();
    }

    /**
     * This method will generate DQR via PaymentService and the expiry time in the DQR will be set after
     * certain seconds.
     *
     * @param merchantType
     * @param txnAmount
     * @param seconds
     * @return qrCodeId
     */
    public static String generateQRViaPaymentServiceWithSpecificExpiryDate(Constants.MerchantType merchantType,String txnAmount, int seconds)
    {
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType,txnAmount,OrderId);
        String dqrExpiryDateFormat = "yyyy-MM-dd HH:mm:ss";
        String date = CommonHelpers.addSeconds(CommonHelpers.getDate(new Date(), dqrExpiryDateFormat), dqrExpiryDateFormat, seconds );
        paymentService.setContext("body.expiryDate", date);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        return qrCodeId;
    }

    public static String generateStaticQRViaWallet(Constants.MerchantType merchantType, int POS_ID)
    {
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"",POS_ID);
        JsonPath generateJson = generateQR.execute().jsonPath();
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
    }

    public static String generateDynamicQRViaPaymentService(Constants.MerchantType merchantType,String txnAmount,int posId)
    {
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchantType,txnAmount,OrderId,posId);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        return qrCodeId;
    }

    public static void validateSuccessPeonDynamicQRNew(OrderDTO orderDTO, String payMode, String gatewayName)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","Masked_customer_mobile_number","TXNTYPE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME","REFUNDAMT", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gatewayName),
                peon.mercUnqRef().equals("vivek4"),
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

    }

    public static OrderDTO generateEdcQRAndFetchQRDiffBusynessType(Constants.MerchantType merchantType,String txnAmount,User user,boolean isEDC,String busynessType,String custId) {

            String qrCodeId = generateQRViaPaymentServiceEDCDiffBusynessType(merchantType,txnAmount,isEDC,busynessType,custId);
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

        //OrderDTO created only for assertion purpose
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType.getId(),merchantType.getKey(),"enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .setORDER_ID(orderId)
                .build();
        orderDTO.setTxnId(qrCodeId); //QRCode ID

        return orderDTO;
    }

    public static String generateQRViaPaymentServiceEDCDiffBusynessType(Constants.MerchantType merchantType,String txnAmount,boolean isEDC,String busynessType,String custId)
    {
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType,txnAmount,OrderId,isEDC,busynessType,custId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        return qrCodeId;
    }

    /**
     * Decodes the QR image code data to get the deeplink URL
     * @param base64QrCode Base64 encoded QR code image data from FPO response
     * @return Decoded deeplink URL from QR code
     */
    @Step
    public static String getDeeplinkFromQRImage(String base64QrCode) {
        try {
            // Check if the input is empty or null
            if (base64QrCode == null || base64QrCode.isEmpty()) {
                return "Error: Empty QR code data";
            }

            // Remove data URL prefix if present
            String cleanBase64 = base64QrCode;
            if (base64QrCode.contains("data:image")) {
                cleanBase64 = base64QrCode.substring(base64QrCode.indexOf(",") + 1);
            }

            // Decode Base64 to binary
            byte[] qrCodeBinary;
            try {
                qrCodeBinary = Base64.getDecoder().decode(cleanBase64);
            } catch (IllegalArgumentException e) {
                return "Error: Invalid Base64 data - " + e.getMessage();
            }

            // Check if the decoded data is empty
            if (qrCodeBinary == null || qrCodeBinary.length == 0) {
                return "Error: Decoded binary data is empty";
            }

            // Convert binary to BufferedImage
            BufferedImage qrImage;
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(qrCodeBinary);
                qrImage = ImageIO.read(bis);
                if (qrImage == null) {
                    return "Error: Could not convert binary data to image";
                }
            } catch (Exception e) {
                return "Error: Failed to read image data - " + e.getMessage();
            }

            // Create QR code reader
            MultiFormatReader qrCodeReader = new MultiFormatReader();

            // Convert BufferedImage to BinaryBitmap
            LuminanceSource source = new BufferedImageLuminanceSource(qrImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Decode QR code
            Result result;
            try {
                result = qrCodeReader.decode(bitmap);
            } catch (Exception e) {
                return "Error: Failed to decode QR code - " + e.getMessage();
            }

            // Return decoded text
            String deeplink = result.getText();
            if (deeplink == null || deeplink.isEmpty()) {
                return "Error: No data found in QR code";
            }

            return deeplink;

        } catch (Exception e) {
            return "Error decoding QR code: " + e.getMessage();
        }
    }

    public static Map<String, String> parseDeeplinkInfo(String deeplink) {
        Map<String, String> deeplinkInfo = new HashMap<>();

        try {
            // Extract payeeVpa
            if (deeplink.contains("pa=")) {
                int paStart = deeplink.indexOf("pa=") + 3;
                int paEnd = deeplink.indexOf("&", paStart);
                deeplinkInfo.put("payeeVpa",
                    paEnd > paStart ? deeplink.substring(paStart, paEnd) : deeplink.substring(paStart));
            }

            // Extract amount
            if (deeplink.contains("am=")) {
                int amStart = deeplink.indexOf("am=") + 3;
                int amEnd = deeplink.indexOf("&", amStart);
                deeplinkInfo.put("amount",
                    amEnd > amStart ? deeplink.substring(amStart, amEnd) : deeplink.substring(amStart));
            }

            // Extract transaction reference
            if (deeplink.contains("tr=")) {
                int trStart = deeplink.indexOf("tr=") + 3;
                int trEnd = deeplink.indexOf("&", trStart);
                deeplinkInfo.put("tr",
                    trEnd > trStart ? deeplink.substring(trStart, trEnd) : deeplink.substring(trStart));
            }

            // Extract first amount (fam) - used in subscription deeplinks
            if (deeplink.contains("fam=")) {
                int famStart = deeplink.indexOf("fam=") + 4;
                int famEnd = deeplink.indexOf("&", famStart);
                deeplinkInfo.put("fam",
                    famEnd > famStart ? deeplink.substring(famStart, famEnd) : deeplink.substring(famStart));
            }

            // Extract fees from split parameter
            if (deeplink.contains("split=")) {
                String split = deeplink.substring(deeplink.indexOf("split=") + 6);

                // Extract CCONFEE if present
                if (split.contains("CCONFEE:")) {
                    int ccStart = split.indexOf("CCONFEE:") + 8;
                    int ccEnd = split.indexOf("|", ccStart);
                    deeplinkInfo.put("cconfee",
                        ccEnd > ccStart ? split.substring(ccStart, ccEnd) : split.substring(ccStart));
                }

                // Extract PCONFEE if present
                if (split.contains("PCONFEE:")) {
                    int pcStart = split.indexOf("PCONFEE:") + 8;
                    int pcEnd = split.indexOf("|", pcStart);
                    deeplinkInfo.put("pconfee",
                        pcEnd > pcStart ? split.substring(pcStart, pcEnd) : split.substring(pcStart));
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing deeplink: " + e.getMessage());
            System.out.println("Deeplink: " + deeplink);
        }

        System.out.println("Parsed values - PayeeVpa: " + deeplinkInfo.get("payeeVpa") +
            ", Amount: " + deeplinkInfo.get("amount") +
            ", Transaction Reference: " + deeplinkInfo.get("tr") +
            ", First Amount: " + deeplinkInfo.get("fam") +
            ", CCONFEE: " + deeplinkInfo.get("cconfee") +
            ", PCONFEE: " + deeplinkInfo.get("pconfee"));

        return deeplinkInfo;
    }
}
