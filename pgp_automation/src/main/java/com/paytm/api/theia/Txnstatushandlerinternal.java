package com.paytm.api.theia;

import com.paytm.api.FastForward;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.framework.reporting.Reporter;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;
import static com.paytm.appconstants.Constants.Owner.VIKASH_VERMA;
import static com.paytm.apphelpers.QRHelper.generateQRViaWallet;

public class Txnstatushandlerinternal extends PGPBaseTest {

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

    @Owner(VIKASH_VERMA)
    @Feature("PGP-46510")
    @Test(description = "Validate charge amount is passing in txnstatus app api when CHARGE_AMOUNT_IN_RESPONSE is Y ")
    public void validatechargeamountintxnstatuswhenpreferenceisY() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly_Pcf;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Static QR configuration is incorrect").isEqualTo(null);
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Static QR configuration is incorrect").isEqualTo("false");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        String mid = fetchQRResponse.getString("body.qrInfo.response.mappingId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchant.getId(), orderId, "50")
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
        fastForwardResponse.prettyPrint();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.txnId")).contains("0000");

        Handlerinternaltxnstatus txnStatus = new Handlerinternaltxnstatus(merchant,orderId,user.ssoToken());
        JsonPath jsonpath=txnStatus.execute().jsonPath();
        Assert.assertEquals(jsonpath.getString("head.mid"), mid);
        Assert.assertNotNull(jsonpath.getString("body.chargeAmount"));
        Assert.assertNotNull(jsonpath.getString("body.TXNID"));
        Assert.assertEquals(jsonpath.getString("body.ORDERID"), orderId);
        Assert.assertEquals(jsonpath.getString("body.RESPMSG"), "Txn Success");
        Assert.assertEquals(jsonpath.getString("body.PAYMENTMODE"), "PPI");
    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-46510")
    @Test(description = "Validate charge amount is passing in txnstatus app api when CHARGE_AMOUNT_IN_RESPONSE is N ")
    public void validatechargeamountintxnstatuswhenpreferenceisN() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.WALLETOnly_PCF;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Static QR configuration is incorrect").isEqualTo(null);
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Static QR configuration is incorrect").isEqualTo("false");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        String mid = fetchQRResponse.getString("body.qrInfo.response.mappingId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchant.getId(), orderId, "50")
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
        fastForwardResponse.prettyPrint();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.txnId")).contains("0000");

        Handlerinternaltxnstatus txnStatus = new Handlerinternaltxnstatus(merchant,orderId,user.ssoToken());
        JsonPath jsonpath=txnStatus.execute().jsonPath();
        Assert.assertEquals(jsonpath.getString("head.mid"), mid);
        Assert.assertNotNull(jsonpath.getString("body.TXNID"));
        Assertions.assertThat(jsonpath.getString("body")).doesNotContain("chargeAmount");
        Assert.assertEquals(jsonpath.getString("body.ORDERID"), orderId);
        Assert.assertEquals(jsonpath.getString("body.RESPMSG"), "Txn Success");
        Assert.assertEquals(jsonpath.getString("body.PAYMENTMODE"), "PPI");
    }
}
