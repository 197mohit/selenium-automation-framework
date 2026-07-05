package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.PaymentService;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner("Shubham Soni")
@Feature("PAPR-2962")
public class shopperStop extends PGPBaseTest {
    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on successful Dynamic QR CC transaction when bin is present")
    public void successfulDynamicCCBinPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "40";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo("|5459648502599138|111|082023")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedMaskedCardNo\\\":\\\"************HC9138\\\"");
    }


    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on Failed Dynamic QR CC transaction when bin is present")
    public void failedDynamicCCBinPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "99.98";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo("|5459648502599138|111|082023")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedMaskedCardNo\\\":\\\"************HC9138\\\"");
    }


    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on pending Dynamic QR CC transaction when bin is present")
    public void pendingDynamicCCBinPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "99.84";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo("|5459648502599138|111|082023")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId("")
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("CC")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedMaskedCardNo\\\":\\\"************HC9138\\\"");
    }


    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on successful Dynamic QR DC transaction when bin is present")
    public void successfulDynamicDCBinPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "40";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "DEBIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo("|5129670510920247|111|082023")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedMaskedCardNo\\\":\\\"************HC0247\\\"");
    }

    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on Failed Dynamic QR DC transaction when bin is present")
    public void failedDynamicDCBinPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "99.98";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "DEBIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo("|5129670510920247|111|082023")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("DC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedMaskedCardNo\\\":\\\"************HC0247\\\"");
    }


    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on pending Dynamic QR DC transaction when bin is present")
    public void pendingDynamicDCBinPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "99.84";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "DEBIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo("|5129670510920247|111|082023")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId("")
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("DC")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedMaskedCardNo\\\":\\\"************HC0247\\\"");
    }


    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on successful Dynamic QR CC transaction when bin is not present in preference")
    public void successfulDynamicCCBinNotPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "40";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo("|4893771000362085|111|082023")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).doesNotContain("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).doesNotContain ("\\\"coBrandedMaskedCardNo\\\":\\\"************HC2085\\\"");
    }

    @Parameters({"theme"})
    @Test(description = "Verify HC prefix on successful Dynamic QR CC saved transaction when bin is present")
    public void successfulDynamicCCSavedBinPresent(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "CO_BRANDED_CARD_IDENTIFIER", "545964,512967");
        }
        String creditCardNumber = "5459648502599138";
        String expMonth = "08";
        String expYear = "2023";
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardAlipay(user,expMonth,expYear, creditCardNumber);
        String txnAmount = "40";
        String OrderId = CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        PaymentService paymentService = new PaymentService(txnAmount,OrderId,Constants.MerchantType.SHOPPERSTOP_CO_BRANDED);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        JsonPath jsonPath1 = fetchQRPaymentDetails.execute().jsonPath();
        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(OrderId));
        String cardID =  jsonPath1.getString("body.paymentOptions.merchantPayOption.savedInstruments[0].cardDetails.cardId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId(), "SSO", user.ssoToken(), OrderId, txnAmount)
                .setPaymentMode(paymentMode)
                .setCardInfo(cardID+"||111|")
                .setQRCodeId(qrCodeId)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId())
                .validateOrderId(OrderId)
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(txnAmount))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        String grepcmd = "grep \"" + OrderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + Constants.MerchantType.SHOPPERSTOP_CO_BRANDED.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedPrefix\\\":\\\"HC\\\"");
        //HCprefix with last 4 digit of card number
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"coBrandedMaskedCardNo\\\":\\\"************HC9138\\\"");
        SavedCardHelpers.deleteSavedCard(user);

    }
}
