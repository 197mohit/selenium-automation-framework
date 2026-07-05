package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.HandlerTxnStatusApi;
import com.paytm.api.Peon;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.theia.FetchCardDetails;
import com.paytm.api.theia.FetchCardIndexNumber;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Body;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.HandlerInternalTxnstatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Head;
import com.paytm.dto.NativeDTO.FetchCardDetailsDTO.FetchCardDetailsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.OrderAdditionalInfo;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Date;

public class LogValidationScripts extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

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

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify merchant Rate Limit and also verify api is returning " +
            "'NO_TOKEN-TRANSCATION_LIMITED' in ACQUIRING_CREATE_ORDER_AND_PAY & ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID" +
            " api call logs.", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void verifyMerchantRateLimitforOldNativeFlow(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_RateLimit;
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setOrderAdditionalInfo(new OrderAdditionalInfo().setMName("Automation").setMID(merchantType.getId()).setMcc("1234").setMLogo("Paytm"));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setExtendInfo(extendInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PGOnly_RateLimit, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "471865");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"RESPONSE\"";
        String theiafacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiafacadelogs).contains("NO_TOKEN-TRANSCATION_LIMITED");
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO(new GetPaymentStatusDTO.Builder(orderDTO.getORDER_ID(), merchantType));
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        getPaymentStatus.execute();
        grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/merchantstatus_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID\" | grep \"RESPONSE\"";
        String merchantstatusfacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.MERCHANT_STATUS,grepcmd);
        Assertions.assertThat(merchantstatusfacadelogs).contains("NO_TOKEN-TRANSCATION_LIMITED");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify merchant Rate Limit and also verify api is returning " +
            "'NO_TOKEN-TRANSCATION_LIMITED' in FUND_USER_TOPUP_FROM_MERCHANT api call logs.", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void verifyMerchantRateLimitforNativeAddMoneyFlow(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_RateLimit;
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(merchantType,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(merchantType.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();
        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,merchantType.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .setTxnValue("10.00")
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"FUND_USER_TOPUP_FROM_MERCHANT\" | grep \"RESPONSE\"";
        String theiafacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiafacadelogs).contains("NO_TOKEN-TRANSCATION_LIMITED");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify merchant Rate Limit and also Verify on hitting HANDLER_INTERNAL/TXNSTATUS api" +
            " we are getting \"NO_TOKEN-TRANSCATION_LIMITED' in response to FUND_USER_ORDER_QUERY_BY_MERCHANT_REQUEST_ID api call logs",
    retryAnalyzer = LogValidationRetryAnalyser.class)
    public void verifyMerchantRateLimitforNativeAddMoneyFlowAndVerifyTXNSTATUSapi(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_RateLimit2;
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(merchantType,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(merchantType.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();
        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,merchantType.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setIsNativeAddMoney("true")
                .setTxnValue("10.00")
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String grepcmd = "";
        HandlerInternalTxnstatusDTO handlerInternalTxnstatusDTO = new HandlerInternalTxnstatusDTO();
        handlerInternalTxnstatusDTO.setBody(new Body(orderDTO.getORDER_ID())).setHead(new Head(merchantType.getId(), user.ssoToken()));
        HandlerTxnStatusApi handlerTxnStatusApi = new HandlerTxnStatusApi(handlerInternalTxnstatusDTO);
        handlerTxnStatusApi.execute();

        grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/merchantstatus_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"FUND_USER_ORDER_QUERY_BY_MERCHANT_REQUEST_ID\" | grep \"RESPONSE\"";
        String merchantstatusfacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.MERCHANT_STATUS,grepcmd);
        Assertions.assertThat(merchantstatusfacadelogs).contains("NO_TOKEN-TRANSCATION_LIMITED");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Perform a successful CC transaction and verify peon sent using OFFUS merchant")
    public void PGP_28959_VerifyPeonSentinLogsCCTxnOFFUS(@Optional("enhancedweb") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();

        /*String txnId = responsePage.textTxnID().getText();
        String grepcmd = "grep \"" + txnId + "\" /paytm/logs/notificationQueueHandler.log | " +" grep \"com.paytm.pgplus.comm.template.service.impl.ProcessTxnNotificationServiceImpl.processTransactionNotify()\" " +
                "| grep \"Merchant PEON has been successfully forwarded to Payload Engine Service for txnId:"+ txnId +"\"";
        String notificationqueueHandlerlogs = getLogs("10.144.18.118",grepcmd);
        Assertions.assertThat(notificationqueueHandlerlogs).isNotEmpty();

        grepcmd = "grep \"" + txnId + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + orderDTO.getMID() + "\" |" +" grep \"com.paytm.pgplus.comm.gateway.service.peon.impl.PeonSentServiceImpl.sendPeon()\" " +
                "| grep \"Peon Sent successfully to MID: " + orderDTO.getMID() + " for OrderId:"+ orderDTO.getORDER_ID() +"\"";
        String commGatewaylogs = getLogs("10.144.18.117",grepcmd);
        Assertions.assertThat(commGatewaylogs).isNotEmpty();*/
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Perform a successful CC transaction and verify peon sent using ONUS merchant")
    public void PGP_28959_VerifyPeonSentinLogsCCTxnONUS(@Optional("enhancedweb") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_Retry, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();

        /*String txnId = responsePage.textTxnID().getText();
        String grepcmd = "grep \"" + txnId + "\" /paytm/logs/notificationQueueHandler.log | " +" grep \"com.paytm.pgplus.comm.template.service.impl.ProcessTxnNotificationServiceImpl.processTransactionNotify()\" " +
                "| grep \"Merchant PEON has been successfully forwarded to Payload Engine Service for txnId:"+ txnId +"\"";
        String notificationqueueHandlerlogs = getLogs("10.144.18.118",grepcmd);
        Assertions.assertThat(notificationqueueHandlerlogs).isNotEmpty();

        grepcmd = "grep \"" + txnId + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + orderDTO.getMID() + "\" |" +" grep \"com.paytm.pgplus.comm.gateway.service.peon.impl.PeonSentServiceImpl.sendPeon()\" " +
                "| grep \"Peon Sent successfully to MID: " + orderDTO.getMID() + " for OrderId:"+ orderDTO.getORDER_ID() +"\"";
        String commGatewaylogs = getLogs("10.144.18.117",grepcmd);
        Assertions.assertThat(commGatewaylogs).isNotEmpty();*/
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Perform a successful DC transaction and verify peon sent using OFFUS merchant")
    public void PGP_28959_VerifyPeonSentinLogsDCTxnOFFUS(@Optional("enhancedweb") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();
        /*String txnId = responsePage.textTxnID().getText();
        String grepcmd = "grep \"" + txnId + "\" /paytm/logs/notificationQueueHandler.log | " +" grep \"com.paytm.pgplus.comm.template.service.impl.ProcessTxnNotificationServiceImpl.processTransactionNotify()\" " +
                "| grep \"Merchant PEON has been successfully forwarded to Payload Engine Service for txnId:"+ txnId +"\"";
        String notificationqueueHandlerlogs = getLogs("10.144.18.118",grepcmd);
        Assertions.assertThat(notificationqueueHandlerlogs).isNotEmpty();

        grepcmd = "grep \"" + txnId + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + orderDTO.getMID() + "\" |" +" grep \"com.paytm.pgplus.comm.gateway.service.peon.impl.PeonSentServiceImpl.sendPeon()\" " +
                "| grep \"Peon Sent successfully to MID: " + orderDTO.getMID() + " for OrderId:"+ orderDTO.getORDER_ID() +"\"";
        String commGatewaylogs = getLogs("10.144.18.117",grepcmd);
        Assertions.assertThat(commGatewaylogs).isNotEmpty();*/
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Perform a successful DC transaction and verify peon sent using ONUS merchant")
    public void PGP_28959_VerifyPeonSentinLogsDCTxnONUS(@Optional("enhancedweb") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_Retry, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();

        /*String txnId = responsePage.textTxnID().getText();
        String grepcmd = "grep \"" + txnId + "\" /paytm/logs/notificationQueueHandler.log | " +" grep \"com.paytm.pgplus.comm.template.service.impl.ProcessTxnNotificationServiceImpl.processTransactionNotify()\" " +
                "| grep \"Merchant PEON has been successfully forwarded to Payload Engine Service for txnId:"+ txnId +"\"";
        String notificationqueueHandlerlogs = getLogs("10.144.18.118",grepcmd);
        Assertions.assertThat(notificationqueueHandlerlogs).isNotEmpty();

        grepcmd = "grep \"" + txnId + "\" /paytm/logs/communicationGateway.log | " + "grep \"" + orderDTO.getMID() + "\" |" +" grep \"com.paytm.pgplus.comm.gateway.service.peon.impl.PeonSentServiceImpl.sendPeon()\" " +
                "| grep \"Peon Sent successfully to MID: " + orderDTO.getMID() + " for OrderId:"+ orderDTO.getORDER_ID() +"\"";
        String commGatewaylogs = getLogs("10.144.18.117",grepcmd);
        Assertions.assertThat(commGatewaylogs).isNotEmpty();*/
    }
}
