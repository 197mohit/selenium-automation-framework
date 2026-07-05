package scripts.api.UPI;

import com.paytm.LocalConfig;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.RiskExtendInfo;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.pg.crypto.AesEncryption;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.time.Instant;

import static com.paytm.appconstants.Constants.Owner.MANISH_MISHRA;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

public class Risk_Rules_In_OrderPayUpipsp extends PGPBaseTest {
    private SoftAssertions softly = new SoftAssertions();
    String payerVPA = "9999725804@ypay";
    String payeeVPA = "paytmqr10q4u2@ptys";
    @Owner(MANISH_MISHRA)
    @Feature("PGP-56708")
    @Test(description = "Validate Theia is sending new Risk parameter in COP which are present in riskExtendInfo in /theia/v1/order/pay/upipsp")
    public void ValidateriskExtendInfoin_TheiaOrderPayUPIPSP() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();
        RiskExtendInfo riskExtendInfo=new RiskExtendInfo();
        riskExtendInfo.setAmount("10")
                .setBusinessType("Mandate")
                .setPayeeVpa("paytm.d956913490@ptys")
                .setIsVerifiedMerchant("true")
                .setPurposeCode("00")
                .setInitiationMode("01")
                .setMerchantGenre("OFFLINE");

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),OrderId, "10",riskExtendInfo);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"");
        System.out.println("staticQrUpiPSPRequest: "+staticQrUpiPSPRequest.getBody());
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);

        softly.assertThat(response.jsonPath().getString("body.resultCode")).isEqualTo("SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.resultCodeId")).isEqualTo("001");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");

        //New parameter
        softly.assertThat(theia_facade.contains("businessType")).isEqualTo(true);
        softly.assertThat(theia_facade.substring(theia_facade.indexOf("businessType")+15,theia_facade.indexOf("businessType")+22)).isEqualTo("Mandate");

        softly.assertThat(theia_facade.contains("payeeVpa")).isEqualTo(true);
        softly.assertThat(theia_facade.substring(theia_facade.indexOf("payeeVpa")+11,theia_facade.indexOf("payeeVpa")+32)).isEqualTo("paytm.d956913490@ptys");

        softly.assertThat(theia_facade.contains("isVerifiedMerchant")).isEqualTo(true);
        softly.assertThat(theia_facade.substring(theia_facade.indexOf("isVerifiedMerchant")+21,theia_facade.indexOf("isVerifiedMerchant")+25)).isEqualTo("true");

        softly.assertThat(theia_facade.contains("merchantGenre")).isEqualTo(true);
        softly.assertThat(theia_facade.substring(theia_facade.indexOf("merchantGenre")+16,theia_facade.indexOf("merchantGenre")+23)).isEqualTo("OFFLINE");

        softly.assertThat(theia_facade.contains("initiationMode")).isEqualTo(true);
        softly.assertThat(theia_facade.substring(theia_facade.indexOf("initiationMode")+17,theia_facade.indexOf("initiationMode")+19)).isEqualTo("01");

        softly.assertThat(theia_facade.contains("purposeCode")).isEqualTo(true);
        softly.assertThat(theia_facade.substring(theia_facade.indexOf("purposeCode")+14,theia_facade.indexOf("purposeCode")+16)).isEqualTo("00");

    }

@Owner(MANISH_MISHRA)
@Feature("PGP-56708")
@Test(description = "Validate riskExtendInfo parameterfrom bank to insta and from insta to UPI PSP and COP request")
    public void ValidateRiskParam_IN_UPIPspV1OrderPay() throws Exception {
    String orderId = "YES" + LocalConfig.ENV_NAME + CommonHelpers.generateOrderId();
    System.out.println("OrdersId is: " + orderId);
    String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
    ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
            ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE.name() + "_" + ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "10.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
            epochSeconds, "", "SAVINGS", Constants.MerchantType.OCIL_YES.getId(), "DEFERRED_SETTLEMENT");

    Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
    String response = upiCreateOrderResponse.asString();
    System.out.println("Encrypted response is+: " + response);
    AesEncryption aesEncryption = new AesEncryption();
    String decryptedResponse;
    try {
        decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    System.out.println("decryptedResponse is: " + decryptedResponse);
    JSONObject json = new JSONObject(decryptedResponse);
    softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
    softly.assertThat(json.get("status")).isEqualTo("accept");
    softly.assertAll();

    String bankRequest = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Decrypted Request from Bank for order creation:");
    String riskExtendInfo=bankRequest.substring(bankRequest.indexOf("riskExtendInfo")+16,bankRequest.indexOf("riskExtendInfo")+163);
    //Validate riskExtendInfo param are present in bank request
    softly.assertThat(bankRequest.contains("riskExtendInfo")).isEqualTo(true);

    softly.assertThat(riskExtendInfo.contains("purposeCode")).isEqualTo(true);
    softly.assertThat(riskExtendInfo.contains("initiationMode")).isEqualTo(true);
    softly.assertThat(riskExtendInfo.contains("payeeVpa")).isEqualTo(true);
    softly.assertThat(riskExtendInfo.contains("payerIfsc")).isEqualTo(true);
    softly.assertThat(riskExtendInfo.contains("payerName")).isEqualTo(true);
    softly.assertThat(riskExtendInfo.contains("payerAccountType")).isEqualTo(true);

    softly.assertThat(riskExtendInfo.substring(riskExtendInfo.indexOf("purposeCode")+14,riskExtendInfo.indexOf("purposeCode")+16)).isEqualTo("44");
    softly.assertThat(riskExtendInfo.substring(riskExtendInfo.indexOf("initiationMode")+17,riskExtendInfo.indexOf("initiationMode")+19)).isEqualTo("19");
    softly.assertThat(riskExtendInfo.substring(riskExtendInfo.indexOf("payeeVpa")+11,riskExtendInfo.indexOf("payeeVpa")+11+payeeVPA.length())).isEqualTo(payeeVPA);
    softly.assertThat(riskExtendInfo.substring(riskExtendInfo.indexOf("payerIfsc")+12,riskExtendInfo.indexOf("payerIfsc")+23)).isEqualTo("AABD0000011");
    softly.assertThat(riskExtendInfo.substring(riskExtendInfo.indexOf("payerName")+12,riskExtendInfo.indexOf("payerName")+15)).isEqualTo("ABC");
    softly.assertThat(riskExtendInfo.substring(riskExtendInfo.indexOf("payerAccountType")+19,riskExtendInfo.indexOf("payerAccountType")+26)).isEqualTo("SAVINGS");

    //Validate additionalInfo param are present in bank request
    softly.assertThat(bankRequest.contains("additionalInfo")).isEqualTo(true);
    softly.assertThat(bankRequest.contains("comment")).isEqualTo(true);
    softly.assertThat(bankRequest.substring(bankRequest.indexOf("additionalInfo")+17,bankRequest.indexOf("additionalInfo")+49)).isEqualTo("comment:YES PAY NEXT Transaction");

    //Validate riskExtendInfo param are present in UPI PSP Order Creation Payload
    String UPIPSPRequest = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Order Creation Payload for UPI PSP");
    String UPIPSPriskParam=UPIPSPRequest.substring(UPIPSPRequest.indexOf("riskExtendInfo")+16,UPIPSPRequest.indexOf("riskExtendInfo")+163);
    softly.assertThat(UPIPSPRequest.contains("riskExtendInfo")).isEqualTo(true);

    softly.assertThat(UPIPSPriskParam.contains("purposeCode")).isEqualTo(true);
    softly.assertThat(UPIPSPriskParam.contains("initiationMode")).isEqualTo(true);
    softly.assertThat(UPIPSPriskParam.contains("payeeVpa")).isEqualTo(true);
    softly.assertThat(UPIPSPriskParam.contains("payerIfsc")).isEqualTo(true);
    softly.assertThat(UPIPSPriskParam.contains("payerName")).isEqualTo(true);
    softly.assertThat(UPIPSPriskParam.contains("payerAccountType")).isEqualTo(true);

    softly.assertThat(UPIPSPriskParam.substring(UPIPSPriskParam.indexOf("purposeCode")+12,UPIPSPriskParam.indexOf("purposeCode")+14)).isEqualTo("44");
    softly.assertThat(UPIPSPriskParam.substring(UPIPSPriskParam.indexOf("initiationMode")+15,UPIPSPriskParam.indexOf("initiationMode")+17)).isEqualTo("19");
    softly.assertThat(UPIPSPriskParam.substring(UPIPSPriskParam.indexOf("payeeVpa")+9,UPIPSPriskParam.indexOf("payeeVpa")+9+payeeVPA.length())).isEqualTo(payeeVPA);
    softly.assertThat(UPIPSPriskParam.substring(UPIPSPriskParam.indexOf("payerIfsc")+10,UPIPSPriskParam.indexOf("payerIfsc")+21)).isEqualTo("AABD0000011");
    softly.assertThat(UPIPSPriskParam.substring(UPIPSPriskParam.indexOf("payerName")+10,UPIPSPriskParam.indexOf("payerName")+13)).isEqualTo("ABC");
    softly.assertThat(UPIPSPriskParam.substring(UPIPSPriskParam.indexOf("payerAccountType")+17,UPIPSPriskParam.indexOf("payerAccountType")+24)).isEqualTo("SAVINGS");
    //Validate additionalInfo param are present in bank request
    softly.assertThat(UPIPSPRequest.contains("additionalInfo")).isEqualTo(true);
    softly.assertThat(UPIPSPRequest.contains("comment")).isEqualTo(true);
    softly.assertThat(UPIPSPRequest.substring(UPIPSPRequest.indexOf("additionalInfo")+15,UPIPSPRequest.indexOf("additionalInfo")+47)).isEqualTo("comment:YES PAY NEXT Transaction");

    //Validate riskExtendInfo param are present in COP Payload
    String COPRequest = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY","REQUEST");
    String COP_RiskParam=COPRequest.substring(COPRequest.indexOf("riskExtendInfo")+17,COPRequest.indexOf("riskExtendInfo")+439);

    softly.assertThat(COPRequest.contains("riskExtendInfo")).isEqualTo(true);

    softly.assertThat(COP_RiskParam.contains("purposeCode")).isEqualTo(true);
    softly.assertThat(COP_RiskParam.contains("initiationMode")).isEqualTo(true);
    softly.assertThat(COP_RiskParam.contains("payeeVpa")).isEqualTo(true);
    softly.assertThat(COP_RiskParam.contains("payerIfsc")).isEqualTo(true);
    softly.assertThat(COP_RiskParam.contains("payerName")).isEqualTo(true);
    softly.assertThat(COP_RiskParam.contains("payerAccountType")).isEqualTo(true);

    softly.assertThat(COP_RiskParam.substring(COP_RiskParam.indexOf("purposeCode")+14,COP_RiskParam.indexOf("purposeCode")+16)).isEqualTo("44");
    softly.assertThat(COP_RiskParam.substring(COP_RiskParam.indexOf("initiationMode")+17,COP_RiskParam.indexOf("initiationMode")+19)).isEqualTo("19");
    softly.assertThat(COP_RiskParam.substring(COP_RiskParam.indexOf("payeeVpa")+11,COP_RiskParam.indexOf("payeeVpa")+11+payeeVPA.length())).isEqualTo(payeeVPA);
    softly.assertThat(COP_RiskParam.substring(COP_RiskParam.indexOf("payerIfsc")+12,COP_RiskParam.indexOf("payerIfsc")+23)).isEqualTo("AABD0000011");
    softly.assertThat(COP_RiskParam.substring(COP_RiskParam.indexOf("payerName")+12,COP_RiskParam.indexOf("payerName")+15)).isEqualTo("ABC");
    softly.assertThat(COP_RiskParam.substring(COP_RiskParam.indexOf("payerAccountType")+19,COP_RiskParam.indexOf("payerAccountType")+26)).isEqualTo("SAVINGS");

    //Validate additionalInfo param are present in bank request
    softly.assertThat(COPRequest.contains("additionalInfo")).isEqualTo(true);
    softly.assertThat(COPRequest.contains("comment")).isEqualTo(true);
    softly.assertThat(COPRequest.substring(COPRequest.indexOf("additionalInfo")+17,COPRequest.indexOf("additionalInfo")+49)).isEqualTo("comment:YES PAY NEXT Transaction");

    softly.assertAll();

}
}
