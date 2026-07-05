package scripts.api.UPI;

import com.paytm.LocalConfig;
import com.paytm.api.*;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.apphelpers.*;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.processTransactionV1.response.BankForm.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpiPspProcessorTests extends PGPBaseTest {

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPProcessorforOfflineSmallMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalTo(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPProcessorforOfflineSmallMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        String payerPaymentInstrumentFee = "";
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmount, "9759417123vpa4bb@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Offline Null Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPProcessorforOfflineNullMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        String payerPaymentInstrumentFee = "";
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmount, "9759417123vpa4bc@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Offline Null Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPProcessorforOfflineNullMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        String payerPaymentInstrumentFee ="";
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bd@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPProcessorforOfflineBigMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4be@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPProcessorforOfflineBigMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bf@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Online Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPProcessorforOnlineMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bg@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for ONline Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPProcessorforOnlineMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bh@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPProcessorforOnusMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bi@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPProcessorforOnusMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bj@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor UPI CC for Offline Small Merchant for  amount greater than 2000")
    public void validateUPIPSPProcessorUPICCforOfflineSmallMerchantforAmountGreaterThan2000() throws Exception {
        String txnAmount = "2001.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed. This merchant can accept Credit Card on UPI upto Rs. 2000."))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("002"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor UPICC for Offline Small Merchant for  amount less than 2000")
    public void validateUPIPSPProcessorUPICCforOfflineSmallMerchantforAmountLessThan2000() throws Exception {
        String txnAmount = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor UPI " +
            " WALLET for Offline Small Merchant for  amount greater than 2000")
    public void validateUPIPSPProcessorUPIPPIWalletforOfflineSmallMerchantforAmountGreaterThan2000() throws Exception {
        String txnAmount = "2001.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor UPI PPI WALLET for Offline Small Merchant for  amount less than 2000")
    public void validateUPIPSPProcessorUPIPPIWALLETforOfflineSmallMerchantforAmountLessThan2000() throws Exception {
        String txnAmount = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor UPI CreditLine for Offline Small Merchant for  amount greater than 2000")
    public void validateUPIPSPProcessorUPICreditLineforOfflineSmallMerchantforAmountGreaterThan2000() throws Exception {
        String txnAmount = "2001.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed. This merchant can accept Credit Line on UPI upto Rs. 2000."))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor UPI CreditLine for Offline Small Merchant for  amount less than 2000")
    public void validateUPIPSPProcessorUPICreditLineforOfflineSmallMerchantforAmountLessThan2000() throws Exception {
        String txnAmount = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI_CC")
    public void validateUPIPSPProcessorforOnusMerchantforUPI_CC() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bi@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);


        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI PPI Wallet")
    public void validateUPIPSPProcessorforOnusMerchantforUPIPPIWALLET() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bi@ptybl", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);


        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI CREDITLINE")
    public void validateUPIPSPProcessorforOnusMerchantforUPICREDITLINE() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bi@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);


        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI_CC Not Enabled")
    public void validateUPIPSPProcessorforOnusMerchantforUPI_CCNotEnabled() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bl@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDIT_CARD"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("002"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

    }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI PPI Wallet Not Enabled")
    public void validateUPIPSPProcessorforOnusMerchantforUPIPPIWALLETNotEnabled() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bl@ptybl", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);


    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57024")
    @Test(description = "validate UPIPSP Processor for Onus Merchant for UPI CREDITLINE NotEnabled")
    public void validateUPIPSPProcessorforOnusMerchantforUPICREDITLINENotEnabled() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bl@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPProcessorforWrongJWT() throws Exception {
        String txnAmount = "100.00";
        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4ba@ptys", "paytmTest@ptys", "UPI_CREDIT_CARD", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Free Bear CreditLine Super Blacklisted")
    public void validateUPIPSPProcessorofflinesmallInterestFreeCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bm@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Interest Bear CreditLine Super Blacklisted")
    public void validateUPIPSPProcessorofflinesmallInterestBearCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bm@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Free Bear CreditLine Super Blacklisted N")
    public void validateUPIPSPProcessorofflinesmallInterestFreeCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bn@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Interest Bear CreditLine Super Blacklisted N")
    public void validateUPIPSPProcessorofflinesmallInterestBearCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bn@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Free Bear CreditLine Super Blacklisted N above 2000")
    public void validateUPIPSPProcessorofflinesmallInterestFreeCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bn@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed. This merchant can accept Credit Line on UPI upto Rs. 2000."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Interest Bear CreditLine Super Blacklisted N above 2000")
    public void validateUPIPSPProcessorofflinesmallInterestBearCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bn@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Interest Free  CreditLine Blacklisted Y ")
    public void validateUPIPSPProcessorofflinesmallInterestFreeCreditLineBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bo@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Interest Bear CreditLine Blacklisted Y")
    public void validateUPIPSPProcessorofflinesmallInterestBearCreditLineBlacklistedY() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bo@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Free Bear  CreditLine Rails Y above 2000")
    public void validateUPIPSPProcessorofflinesmallInterestFreeCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bp@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Small Merchant Interest Bear  CreditLine Rails Y above 2000")
    public void validateUPIPSPProcessorofflinesmallInterestBearCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bp@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big  Merchant Free Bear CreditLine Super Blacklisted")
    public void validateUPIPSPProcessorofflineBigInterestFreeCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bq@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Interest Bear CreditLine Super Blacklisted")
    public void validateUPIPSPProcessorofflineBigInterestBearCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bq@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Free Bear CreditLine Super Blacklisted N")
    public void validateUPIPSPProcessorofflineBigInterestFreeCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4br@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Interest Bear CreditLine Super Blacklisted N")
    public void validateUPIPSPProcessorofflineBigInterestBearCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4br@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Free Bear CreditLine Super Blacklisted N above 2000")
    public void validateUPIPSPProcessorofflineBigInterestFreeCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4br@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Interest Bear CreditLine Super Blacklisted N above 2000")
    public void validateUPIPSPProcessorofflineBigInterestBearCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4br@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Interest Free CreditLine Blacklisted Y ")
    public void validateUPIPSPProcessorofflineBigInterestFreeCreditLineBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bs@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Interest Bear CreditLine Blacklisted Y")
    public void validateUPIPSPProcessorofflineBigInterestBearCreditLineBlacklistedY() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bs@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Free Bear  CreditLine Rails Y above 2000")
    public void validateUPIPSPProcessorofflineBigInterestFreeCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bt@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Offline Big Merchant Interest Bear  CreditLine Rails Y above 2000")
    public void validateUPIPSPProcessorofflineBigInterestBearCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bt@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big  Merchant Free Bear CreditLine Super Blacklisted")
    public void validateUPIPSPProcessoronlineBigInterestFreeCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bu@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Interest Bear CreditLine Super Blacklisted")
    public void validateUPIPSPProcessoronlineBigInterestBearCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bu@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Free Bear CreditLine Super Blacklisted N")
    public void validateUPIPSPProcessoronlineBigInterestFreeCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bv@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Interest Bear CreditLine Super Blacklisted N")
    public void validateUPIPSPProcessoronlineBigInterestBearCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bv@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Free Bear CreditLine Super Blacklisted N above 2000")
    public void validateUPIPSPProcessoronlineBigInterestFreeCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bv@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Interest Bear CreditLine Super Blacklisted N above 2000")
    public void validateUPIPSPProcessoronlineBigInterestBearCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bv@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Free Bear CreditLine Blacklisted Y ")
    public void validateUPIPSPProcessoronlineBigInterestFreeCreditLineBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bw@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Interest Bear CreditLine Blacklisted Y")
    public void validateUPIPSPProcessoronlineBigInterestBearCreditLineBlacklistedY() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bw@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Interest Free CreditLine Rails Y above 2000")
    public void validateUPIPSPProcessoronlineBigInterestFreeCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bx@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate UPIPSP Processor for Online Big Merchant Interest Bear  CreditLine Rails Y above 2000")
    public void validateUPIPSPProcessoronlineBigInterestBearCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bx@ptybl", "paytmTest@ptys", "CREDITLINE_CL01", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CL01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate SQR UPIPSP Processor for UPI_VOUCHER")
    public void validateSQRUPIPSPProcessorforUPI_VOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4by@ptybl", "paytmTest@ptys", "UPI_VOUCHER", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_VOUCHER",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_VOUCHER");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY" , "REQUEST");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");
        System.out.println(logs);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate DQR UPIPSP Processor for UPI_VOUCHER")
    public void validateDQRUPIPSPProcessorforUPI_VOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, txnAmount, orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4by@ptybl", "paytmTest@ptys", "UPI_VOUCHER", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_VOUCHER",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_PAY_ORDER");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate SQR UPIPSP Processor for VOUCHER")
    public void validateSQRUPIPSPProcessorforVOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4by@ptybl", "paytmTest@ptys", "VOUCHER", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "VOUCHER",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_VOUCHER");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate DQR UPIPSP Processor for VOUCHER")
    public void validateDQRUPIPSPProcessorforVOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, txnAmount, orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4by@ptybl", "paytmTest@ptys", "VOUCHER", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "VOUCHER",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_PAY_ORDER");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate SQR UPIPSP Processor for VOUCHER1")
    public void validateSQRUPIPSPProcessorforVOUCHER1() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4by@ptybl", "paytmTest@ptys", "VOUCHER1", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "VOUCHER1",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY");
        Assertions.assertThat(logs).doesNotContain("UPI_VOUCHER");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate DQR UPIPSP Processor for VOUCHER1")
    public void validateDQRUPIPSPProcessorforVOUCHER1() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, txnAmount, orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4by@ptybl", "paytmTest@ptys", "VOUCHER1", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "VOUCHER1",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("UPI_VOUCHER");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Req Auth UPIPSP Voucher SQR Flow")
    public void validateReqAuthUPIPSPVoucherSQRFlow() throws Exception {
        String txnAmount = "25.00";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String payerVPA = "9999725804@ypay";
        String payeeVPA = "paytmqr10q4u2@ptys";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());

        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                ReqAuthUPICreateOrder.PayerInstrument.VOUCHER.name(), txnAmount, payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS", Constants.MerchantType.OCIL_YES.getId(),"DEFERRED_SETTLEMENT");

        Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Req Auth UPIPSP Voucher DQR Flow")
    public void validateReqAuthUPIPSPVoucherDQRFlow() throws Exception {
        String txnAmount = "25.00";
        Constants.MerchantType merchant = Constants.MerchantType.OCIL_YES;
        String payerVPA = "9999725804@ypay";
        String payeeVPA = "paytmqr10q4u2@ptys";
        int posId = 192737392;
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, txnAmount, orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());

        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                ReqAuthUPICreateOrder.PayerInstrument.VOUCHER.name(), txnAmount, payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS", Constants.MerchantType.OCIL_YES.getId(),"DEFERRED_SETTLEMENT");

        Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_PAY_ORDER");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for PCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "4.72";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDIT_CARD" , "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for PCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "4.72";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDITLINE_CREDITLINE01" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR  UPIPSP Upi Ppi Wallet for PCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "4.72";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "PPI_WALLET" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for PCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "0";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for PCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforPCFWhenWrongpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "2.00";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for PCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforPCFWhenWrongpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "3.00";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Ppi Wallet for PCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforPCFWhenWrongpayerPaymentInstrumentFeeisPassedWronginRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "4.00";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for PCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforPCFWhenWrongpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "5.00";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR  UPIPSP Upi Credit Card for PCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "205.72";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "4.72";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for PCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.73";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "4.72";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Ppi Wallet for PCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.71";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "4.72";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for PCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "201.00";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo("200.00"))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for PCF When payerPaymentInstrumentFee is not Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for PCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Ppi Wallet for PCF When payerPaymentInstrumentFee is not Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "204.72";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for PCF When payerPaymentInstrumentFee is not Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417322@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for MDRPCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforMDRPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.88";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "9.44";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDIT_CARD","upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for MDRPCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforMDRPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.88";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "9.44";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDITLINE_CREDITLINE01" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Ppi Wallet for MDRPCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforMDRPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "209.44";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "PPI_WALLET" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for MDRPCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforMDRPCFWhenpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "209.44";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for MDRPCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforMDRPCFWhenWrongpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.88";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "9.45";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for MDRPCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforMDRPCFWhenWrongpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.88";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "9.43";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Ppi Wallet for MDRPCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforMDRPCFWhenWrongpayerPaymentInstrumentFeeisPassedWronginRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "209.44";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "0.1";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for MDRPCF When Wrong payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforMDRPCFWhenWrongpayerPaymentInstrumentFeeisPassedinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "209.44";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "1.00";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for MDRPCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforMDRPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.89";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "9.44";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for MDRPCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforMDRPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.87";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "9.44";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Ppi Wallet for MDRPCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforMDRPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "209.43";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo("209.44"))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for MDRPCF When payerPaymentInstrumentFee is Passed with Wrong txnAmount in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforMDRPCFWhenpayerPaymentInstrumentFeeisPassedwithWrongtxnAmountinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "209.45";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo("209.44"))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for MDRPCF When payerPaymentInstrumentFee is not Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforMDRPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.88";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Creditline for MDRPCF When payerPaymentInstrumentFee is Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforMDRPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "218.88";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate OnlineReqAuth DQR UPIPSP Upi Ppi Wallet for MDRPCF When payerPaymentInstrumentFee is not Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforMDRPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "209.44";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for MDRPCF When payerPaymentInstrumentFee is not Passed in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforMDRPCFWhenpayerPaymentInstrumentFeeisnotPassedinRequest() throws Exception {
        String txnAmount = "200";
        String txnAmountinUpiPspRequest = "209.44";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417326@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi Credit Card for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditCardforMDRWhenpayerPaymentInstrumentFeeisPassedasNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDIT_CARD", "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online Req AuthDQR UPIPSP Upi Creditline for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiCreditlineforMDRWhenpayerPaymentInstrumentFeeisPassedNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDITLINE_CREDITLINE01" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR  UPIPSP Upi Ppi Wallet for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpiPpiWalletforMDRWhenpayerPaymentInstrumentFeeisPassedNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "PPI_WALLET" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingsforMDRWhenpayerPaymentInstrumentFeeisPassedNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate ReqAuth SQR UPIPSP Upi Credit Card for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateReqAuthSQRUPIPSPUpiCreditCardforMDRWhenpayerPaymentInstrumentFeeisPassedasNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDIT_CARD", "upiCcBin" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate ReqAuth SQR UPIPSP Upi Creditline for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateReqAuthSQRUPIPSPUpiCreditlineforMDRWhenpayerPaymentInstrumentFeeisPassedNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "CREDITLINE_CREDITLINE01", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "CREDITLINE_CREDITLINE01",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "CREDITLINE_CREDITLINE01" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICL");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate ReqAuth SQR  UPIPSP Upi Ppi Wallet for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateReqAuthSQRUPIPSPUpiPpiWalletforMDRWhenpayerPaymentInstrumentFeeisPassedNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "PPI_WALLET", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "PPI_WALLET" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_PPIWALLET");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate ReqAuth SQR UPIPSP Upi savings for MDR When payerPaymentInstrumentFee is Passed as null in Request")
    public void validateReqAuthSQRUPIPSPUpisavingsforMDRWhenpayerPaymentInstrumentFeeisPassedNullinRequest() throws Exception {
        String txnAmount = "200.00";
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "";
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId,
                txnAmountinUpiPspRequest, "paytm-9759417321@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",orderId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalToIgnoringCase(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);

        String externalSerialNo = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        String bankRrn = CommonHelpers.generateOrderId();

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountinUpiPspRequest,externalSerialNo ,bankRrn, "" );
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP Upi savings when acquirementId is passed in orderId parameter for tr as acquirementId when preference UPI_TR_ACQ_ID_ENABLE is Y")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingswhenacquirementIdispassedinorderIdparameterfortrasacquirementIdwhenpreferenceUPI_TR_ACQ_ID_ENABLEisY() throws Exception {
        String txnAmount = "200.00";
        SoftAssertions softAssert = new SoftAssertions();
        String txnAmountinUpiPspRequest = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus())
                .isEqualTo("S");
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode())
                .isEqualTo("0000");
        softAssert.assertAll();

        // 2. Hit fetchPaymentOptions API
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .setWorkFlow("checkout")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId,
                fetchPaymentOptionsDTO);
        JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softAssert.assertAll();

        // 3. Get QR data and decode deeplink
        String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
        String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
        System.out.println("Decoded deeplink: " + deeplink);



        String acqId = deeplink.substring(deeplink.indexOf("&tr")+4,deeplink.indexOf("&tr")+4+35);
        System.out.println("acqId is : " + acqId);
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, acqId,
                txnAmountinUpiPspRequest, "paytm-9759417324@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",acqId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.orderId", Matchers.equalTo(acqId))
                .body("body.callbackUrl", Matchers.equalTo(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth SQR UPIPSP Upi savings when Random acquirementId is passed in orderId parameter for tr as acquirementId when preference UPI_TR_ACQ_ID_ENABLE is Y")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingswhenRandomacquirementIdispassedinorderIdparameterfortrasacquirementIdwhenpreferenceUPI_TR_ACQ_ID_ENABLEisY() throws Exception {
        String txnAmountinUpiPspRequest = "200.00";
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        String acqId = "20250710011800000150034498205984162";
        System.out.println("acqId is : " + acqId);
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, acqId,
                txnAmountinUpiPspRequest, "paytm-9759417324@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "",acqId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl", Matchers.equalTo(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP MDRPCF Upi Credit Card for payerPaymentInstrumentFee when acquirementId is passed in orderId parameter for tr as acquirementId when preference UPI_TR_ACQ_ID_ENABLE is Y")
    public void validateOnlineReqAuthDQRUPIPSPMDRPCFUpiCreditCardforpayerPaymentInstrumentFeewhenacquirementIdispassedinorderIdparameterfortrasacquirementIdwhenpreferenceUPI_TR_ACQ_ID_ENABLEisY() throws Exception {
        String txnAmount = "200.00";
        SoftAssertions softAssert = new SoftAssertions();
        String txnAmountinUpiPspRequest = "211.80";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "11.80" ;
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus())
                .isEqualTo("S");
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode())
                .isEqualTo("0000");
        softAssert.assertAll();

        // 2. Hit fetchPaymentOptions API
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .setWorkFlow("checkout")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId,
                fetchPaymentOptionsDTO);
        JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softAssert.assertAll();

        // 3. Get QR data and decode deeplink
        String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
        String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
        System.out.println("Decoded deeplink: " + deeplink);



        String acqId = deeplink.substring(deeplink.indexOf("&tr")+4,deeplink.indexOf("&tr")+4+35);
        System.out.println("acqId is : " + acqId);
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, acqId,
                txnAmountinUpiPspRequest, "paytm-9759417327@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",acqId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.orderId", Matchers.equalTo(acqId))
                .body("body.callbackUrl", Matchers.equalTo(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR UPIPSP MDRPCF Upi Credit Card and payerPaymentInstrumentFee not passed when acquirementId is passed in orderId parameter for tr as acquirementId when preference UPI_TR_ACQ_ID_ENABLE is Y")
    public void validateOnlineReqAuthDQRUPIPSPMDRPCFUpiCreditCardforAndpayerPaymentInstrumentFeeNotPassedwhenacquirementIdispassedinorderIdparameterfortrasacquirementIdwhenpreferenceUPI_TR_ACQ_ID_ENABLEisY() throws Exception {
        String txnAmount = "200.00";
        SoftAssertions softAssert = new SoftAssertions();
        String txnAmountinUpiPspRequest = "211.80";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus())
                .isEqualTo("S");
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode())
                .isEqualTo("0000");
        softAssert.assertAll();

        // 2. Hit fetchPaymentOptions API
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .setWorkFlow("checkout")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId,
                fetchPaymentOptionsDTO);
        JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softAssert.assertAll();

        // 3. Get QR data and decode deeplink
        String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
        String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
        System.out.println("Decoded deeplink: " + deeplink);



        String acqId = deeplink.substring(deeplink.indexOf("&tr")+4,deeplink.indexOf("&tr")+4+35);
        System.out.println("acqId is : " + acqId);
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, acqId,
                txnAmountinUpiPspRequest, "paytm-9759417327@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD",acqId );
        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.orderId", Matchers.equalTo(acqId))
                .body("body.callbackUrl", Matchers.equalTo(callbackUrl))
                .extract().as(StaticQrUpiPSPResponse.class);
    }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for PPIWALLET When UPI_WALLET_BLACKLISTED is N and WALLET_ON_UPI_RAILS_ENABLED N")
    public void validateUPIPSPforPPIWALLETWhenUPI_WALLET_BLACKLISTED_is_N_And_WALLET_ON_UPI_RAILS_ENABLED_is_N() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bb@ptys", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for PPIWALLET When UPI_WALLET_BLACKLISTED is N and WALLET_ON_UPI_RAILS_ENABLED Y")
    public void validateUPIPSPforPPIWALLETWhenUPI_WALLET_BLACKLISTED_is_N_And_WALLET_ON_UPI_RAILS_ENABLED_is_Y() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bd@ptys", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.externalSerialNo", Matchers.notNullValue())
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for PPIWALLET When UPI_WALLET_BLACKLISTED is Y and WALLET_ON_UPI_RAILS_ENABLED N")
    public void validateUPIPSPforPPIWALLETWhenUPI_WALLET_BLACKLISTED_is_Y_And_WALLET_ON_UPI_RAILS_ENABLED_is_N() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bc@ptys", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for PPIWALLET When UPI_WALLET_BLACKLISTED is Y and WALLET_ON_UPI_RAILS_ENABLED Y")
    public void validateUPIPSPforPPIWALLETWhenUPI_WALLET_BLACKLISTED_is_Y_And_WALLET_ON_UPI_RAILS_ENABLED_is_Y() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bf@ptys", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for Online PPIWALLET When UPI_WALLET_BLACKLISTED is N and WALLET_ON_UPI_RAILS_ENABLED Null")
    public void validateUPIPSPforOnlinePPIWALLETWhenUPI_WALLET_BLACKLISTED_is_N_And_WALLET_ON_UPI_RAILS_ENABLED_is_Null() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bh@ptybl", "paytmTest@ptys", "PPI_WALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.externalSerialNo", Matchers.notNullValue())
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for UPI_PPIWALLET When UPI_WALLET_BLACKLISTED is N and WALLET_ON_UPI_RAILS_ENABLED N")
    public void validateUPIPSPforUPI_PPIWALLETWhenUPI_WALLET_BLACKLISTED_is_N_And_WALLET_ON_UPI_RAILS_ENABLED_is_N() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bb@ptys", "paytmTest@ptys", "UPI_PPIWALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_PPIWALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for UPI_PPIWALLET When UPI_WALLET_BLACKLISTED is N and WALLET_ON_UPI_RAILS_ENABLED Y")
    public void validateUPIPSPforUPI_PPIWALLETWhenUPI_WALLET_BLACKLISTED_is_N_And_WALLET_ON_UPI_RAILS_ENABLED_is_Y() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bd@ptys", "paytmTest@ptys", "UPI_PPIWALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_PPIWALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.externalSerialNo", Matchers.notNullValue())
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for UPI_PPIWALLET When UPI_WALLET_BLACKLISTED is Y and WALLET_ON_UPI_RAILS_ENABLED N")
    public void validateUPIPSPforUPI_PPIWALLETWhenUPI_WALLET_BLACKLISTED_is_Y_And_WALLET_ON_UPI_RAILS_ENABLED_is_N() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bc@ptys", "paytmTest@ptys", "UPI_PPIWALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_PPIWALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for UPI_PPIWALLET When UPI_WALLET_BLACKLISTED is Y and WALLET_ON_UPI_RAILS_ENABLED Y")
    public void validateUPIPSPforUPI_PPIWALLETWhenUPI_WALLET_BLACKLISTED_is_Y_And_WALLET_ON_UPI_RAILS_ENABLED_is_Y() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bf@ptys", "paytmTest@ptys", "UPI_PPIWALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_PPIWALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG-4737")
    @Test(description = "validate UPIPSP for Online UPI_PPIWALLET When UPI_WALLET_BLACKLISTED is N and WALLET_ON_UPI_RAILS_ENABLED Null")
    public void validateUPIPSPforOnlineUPI_PPIWALLETWhenUPI_WALLET_BLACKLISTED_is_N_And_WALLET_ON_UPI_RAILS_ENABLED_is_Null() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        String orderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, orderId, txnAmount, "9759417123vpa4bh@ptybl", "paytmTest@ptys", "UPI_PPIWALLET", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_PPIWALLET",orderId );

        UpiPspProcessor upiPspProcessor = new UpiPspProcessor(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.externalSerialNo", Matchers.notNullValue())
                .body("body.orderId", Matchers.equalTo(orderId))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(mid))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

}
