package scripts.api.theia;

import com.paytm.api.InstaproxyPtybliResponse;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.theia.TheiaApiQrCreate;
import com.paytm.api.upipsp.UpiPspConsultFee;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Map;

import static com.paytm.apphelpers.QRHelper.parseDeeplinkInfo;

public class TheiaApiQrCreateTest extends PGPBaseTest {


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate onetime OnDemand QR with checksum for PTYBLI")
    public void validateonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417321@ptybl");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isEqualTo(orderId);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  Native Subscription QR with checksum for PTYBLI")
    public void validateNativeSubscriptionOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix ;
        String mid = Constants.MerchantType.Subs_PCF_fix.getId();
        String txnAmount = "200";
        String subscriptionType = "NATIVE_SUBSCRIPTION";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount,subscriptionType );
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("SUBS_DQR_0002");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid token type for subscription DQR");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  Native Mf Sip QR with checksum for PTYBLI")
    public void validateNativeMfSipOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.SIP_PCF_fix ;
        String mid = Constants.MerchantType.SIP_PCF_fix.getId();
        String txnAmount = "200";
        String subscriptionType = "NATIVE_MF_SIP";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount,subscriptionType );
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("SUBS_DQR_0002");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid token type for subscription DQR");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate tr As AcqId onetime OnDemand QR with checksum for PTYBLI")
    public void validateonetrAsAcqIdonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417324@ptybl");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf onetime OnDemand QR with checksum for PTYBLI")
    public void validateonePcfonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417322@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf and Platform onetime OnDemand QR with checksum for PTYBLI")
    public void validateonePcfandPlatformonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417325@ptybl");
        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf  onetime OnDemand QR with checksum for PTYBLI")
    public void validateoneMdrandPcfonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417323@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf and Platform onetime OnDemand QR with checksum for PTYBLI")
    public void validateoneMdrandPcfandPlatformonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417326@ptybl");
        Assertions.assertThat(amount).isEqualTo("209.44");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("9.44");
        Assertions.assertThat(pconfee).isNull();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  Platform onetime OnDemand QR with checksum for PTYBLI")
    public void validateonePlatformonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-956932428ab1@ptybl");
        Assertions.assertThat(amount).isEqualTo("205.90");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isNull();
        Assertions.assertThat(pconfee).isNull();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  Mdr and Pcf with tr as AcqId onetime OnDemand QR with checksum for PTYBLI")
    public void validateoneMdrandPcfwithtrAsAcqIdonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417327@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("11.80");
        Assertions.assertThat(pconfee).isEqualTo("23.60");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  onetime OnDemand QR with checksum for HDFU")
    public void validateoneonetimeOnDemandQRwithChecksumforHDFU() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf onetime OnDemand QR with checksum for HDFU")
    public void validateonePcfonetimeOnDemandQRwithChecksumforHDFU() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf and Platform onetime OnDemand QR with checksum for HDFU")
    public void validateonePcfandPlatformonetimeOnDemandQRwithChecksumforHDFU() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf  onetime OnDemand QR with checksum for HDFU")
    public void validateoneMdrandPcfonetimeOnDemandQRwithChecksumforHDFU() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf and Platform onetime OnDemand QR with checksum for HDFU")
    public void validateoneMdrandPcfandPlatformonetimeOnDemandQRwithChecksumforHDFU() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isNull();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417321@ptybl");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isEqualTo(orderId);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate tr As AcqId onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateonetrAsAcqIdonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();

        String txnAmount = "200";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417324@ptybl");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateonePcfonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417322@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf and Platform onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateonePcfandPlatformonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417325@ptybl");
        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf  onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateoneMdrandPcfonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417323@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf and Platform onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateoneMdrandPcfandPlatformonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417326@ptybl");
        Assertions.assertThat(amount).isEqualTo("209.44");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("9.44");
        Assertions.assertThat(pconfee).isNull();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  Platform onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateonePlatformonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);

        Constants.MerchantType merchant = Constants.MerchantType.PLATFORM_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-956932428ab1@ptybl");
        Assertions.assertThat(amount).isEqualTo("205.90");
        Assertions.assertThat(tr).isEqualTo(orderId);
        Assertions.assertThat(cconfee).isNull();
        Assertions.assertThat(pconfee).isNull();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  Mdr and Pcf with tr as AcqId onetime OnDemand QR with TxnToken for PTYBLI")
    public void validateoneMdrandPcfwithtrAsAcqIdonetimeOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417327@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("11.80");
        Assertions.assertThat(pconfee).isEqualTo("23.60");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  onetime OnDemand QR with TxnToken for HDFU")
    public void validateoneonetimeOnDemandQRwithTxnTokenforHDFU() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isNull();
        Assertions.assertThat(pconfee).isNull();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf onetime OnDemand QR with TxnToken for HDFU")
    public void validateonePcfonetimeOnDemandQRwithTxnTokenforHDFU() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Pcf and Platform onetime OnDemand QR with TxnToken for HDFU")
    public void validateonePcfandPlatformonetimeOnDemandQRwithTxnTokenforHDFU() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("4.72");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf  onetime OnDemand QR with TxnToken for HDFU")
    public void validateoneMdrandPcfonetimeOnDemandQRwithTxnTokenforHDFU() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isEqualTo("9.44");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Mdr and Pcf and Platform onetime OnDemand QR with TxnToken for HDFU")
    public void validateoneMdrandPcfandPlatformonetimeOnDemandQRwithTxnTokenforHDFU() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        String orderId = initTxnDTO.getBody().getOrderId();
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");
        String cconfee = deeplinkInfo.get("cconfee");
        String pconfee = deeplinkInfo.get("pconfee");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytmconfee@hdfcbank");
        Assertions.assertThat(amount).isEqualTo("204.72");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
        Assertions.assertThat(cconfee).isEqualTo("4.72");
        Assertions.assertThat(pconfee).isNull();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Native subscription  OnDemand QR with TxnToken for PTYBLI")
    public void validateNativeSubscriptionOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix ;
        String mid = Constants.MerchantType.Subs_PCF_fix.getId();
        String txnAmount = "200";
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String subscriptionRequestType = "NATIVE_SUBSCRIPTION";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(txnAmount)
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(txnAmount)
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO =  NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,subscriptionRequestType,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-100425@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isEqualTo(subsId);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate Native MF Sip  OnDemand QR with TxnToken for PTYBLI")
    public void validateNativeMfSipOnDemandQRwithTxnTokenforPTYBLI() throws Exception {

        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.SIP_PCF_fix ;
        String mid = Constants.MerchantType.SIP_PCF_fix.getId();
        String txnAmount = "200";
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String subscriptionRequestType = "NATIVE_MF_SIP";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(txnAmount)
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(txnAmount)
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponseDTO =  NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,mid,txnAmount,subscriptionRequestType,txnToken);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-170425@ptybl");
        Assertions.assertThat(amount).isEqualTo("200.00");
        Assertions.assertThat(tr).isEqualTo(subsId);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code DQRO_0001 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeDQRO_0001onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        TheiaApiQrCreate TheiaApiQrCreateRequest1 = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath1 = TheiaApiQrCreateRequest1.execute().jsonPath();
        Assertions.assertThat(responseJsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath1.getString("body.resultInfo.resultCode")).isEqualTo("DQRO_0001");
        Assertions.assertThat(responseJsonPath1.getString("body.resultInfo.resultMsg")).isEqualTo("Error occured while generating order.");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code QR_1005 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeQR_1005onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "0";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);

        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_1005");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Amount");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code DQR_0002 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeDQR_0002onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        TheiaApiQrCreateRequest.setContext("body.mid","");
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("DQR_0002");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Field mid can not be blank.");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code DQR_0004 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeDQR_0004onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.ACQUIRING_REFUND_SYSTEM_ERROR_MID ;
        String mid = Constants.MerchantType.ACQUIRING_REFUND_SYSTEM_ERROR_MID.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("DQR_0004");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Merchant's VPA-address not found.");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code DQR_0007 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeDQR_0007onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.REFUND_IMPSPGONLY;
        String mid = Constants.MerchantType.REFUND_IMPSPGONLY.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("DQR_0007");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("UPI Paymode is not enabled on Merchant");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate  error code SUBS_DQR_0001 for Subscription QR with checksum for PTYBLI")
    public void validateNativeRequestSubscriptionOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix ;
        String mid = Constants.MerchantType.Subs_PCF_fix.getId();
        String txnAmount = "200";
        String subscriptionType = "NATIVE_REQUEST_SUBSCRIPTION";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount,subscriptionType );
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("SUBS_DQR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Request type");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code DQR_0008 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeDQR_0008onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId, merchant, txnAmount);
        TheiaApiQrCreateRequest.setContext("body.expiryDate","2025-01-22 15:39:18");

        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("DQR_0008");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Expiry Date should be after the currentDate.");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code DQR_0009 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeDQR_0009onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId, merchant, txnAmount);
        TheiaApiQrCreateRequest.setContext("body.expiryDate","2099-01-22 15:39:18");

        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("DQR_0009");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Expiry Date can't be after 6 months from currentDate");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code QR_1007 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeQR_1007onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant);

        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_1007");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Expiry Date");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate error code DQR_0001 onetime OnDemand QR with checksum for PTYBLI")
    public void validateerrorcodeDQR_0001onetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId, merchant, txnAmount);
        TheiaApiQrCreateRequest.setContext("body",null);

        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("FAILURE");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("DQR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Blank request received.");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate success DQR for invalid expiry with qr.expiry.whitelisted.mids property for onetime OnDemand QR with checksum for PTYBLI")
    public void validatesuccessDQRforinvalidexpiryonetimeOnDemandQRwithChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";

        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant);
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        String qrData =  responseJsonPath.getString("body.qrData");
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(qrData);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("QR_0001");
        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(payeeVpa).isEqualTo("paytm-9759417324@ptybl");
        Assertions.assertThat(amount).isEqualTo("200");
        Assertions.assertThat(tr).isNotEqualTo(orderId);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60135")
    @Test(description = "validate onetime OnDemand QR with Invalid checksum for PTYBLI")
    public void validateonetimeOnDemandQRwithInvalidChecksumforPTYBLI() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE ;
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        TheiaApiQrCreate TheiaApiQrCreateRequest = new TheiaApiQrCreate(orderId,merchant,txnAmount);
        TheiaApiQrCreateRequest.setContext("body.amount", "100");
        JsonPath responseJsonPath = TheiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("resultStatus")).isEqualTo("F");
        Assertions.assertThat(responseJsonPath.getString("resultCode")).isEqualTo("403");
        Assertions.assertThat(responseJsonPath.getString("resultMsg")).isEqualTo("Unauthorized Access");

    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1992")
    @Test(description = "Verify the isDQR flag - TRUE on hitting theia createQR API")
    public void validate_NonreqAuth_createQR_AcquiringPayOrderRequestContainsIsDqr() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE;
        String txnAmount = "200";
        TheiaApiQrCreate theiaApiQrCreateRequest = new TheiaApiQrCreate(orderId, merchant, txnAmount);
        JsonPath responseJsonPath = theiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");

        String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_PAY_ORDER",
                "REQUEST");
     
        Assertions.assertThat(acquiringPayOrderRequestLogs).contains("\"isDQR\":\"true\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1992")
    @Parameters({"theme"})
    @Test(description = "Verify the isDQR flag - TRUE in non req-auth flow  when DQR is coming on cashier page")
    public void validate_NonreqAuth_DQR_AcquiringPayOrderRequestContainsIsDqr(
            @Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE;
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("200")
                .build();
        String orderId = orderDTO.getORDER_ID();

      
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
       

        String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_PAY_ORDER",
                "REQUEST");
    
        Assertions.assertThat(acquiringPayOrderRequestLogs)
                .as("ACQUIRING_PAY_ORDER REQUEST should contain isDQR true")
                .contains("\"isDQR\":\"true\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1992")
    @Parameters({"theme"})
    @Test(description = "Verify the isDQR flag - TRUE in req-auth flow  when UPI psp API is called")
    public void validate_reqAuth_UpiPsp_Success_AcquiringPayOrderRequestContainsIsDqr(
            @Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("200")
                .build();
        String orderId = orderDTO.getORDER_ID();

        try {
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();

            StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), orderDTO.getTXN_AMOUNT())
                    .setOrderId(orderDTO.getORDER_ID())
                    .build();
            StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            Response response = staticQrUpiPSP.execute();
            StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
            Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                    .as("StaticQrUpiPSP response should be success")
                    .isEqualToIgnoringCase("SUCCESS");

            String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                    PG2LogsValidationHelper.setEnvService.theia_facade,
                    orderId,
                    "ACQUIRING_PAY_ORDER",
                    "REQUEST");
            Assertions.assertThat(acquiringPayOrderRequestLogs).contains("\"isDQR\":\"true\"");
        } finally {
            DriverManager.getDriver().close();
        }
    }
    @Owner(Constants.Owner.MOHIT_KHARE)
    @Feature("PG-1992")
    @Test(description = "OnDemand QR (HDFC UPI subtype) — result success; ACQUIRING_PAY_ORDER REQUEST logs contain isDQR true")
    public void validateOnDemandQrCreateHDFU_ResultSuccess_AcquiringPayOrderRequestContainsIsDqr() throws Exception {

        String orderId = CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE;
        String txnAmount = "200";
        TheiaApiQrCreate theiaApiQrCreateRequest = new TheiaApiQrCreate(orderId, merchant, txnAmount);
        JsonPath responseJsonPath = theiaApiQrCreateRequest.execute().jsonPath();

        Assertions.assertThat(responseJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");

        String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_PAY_ORDER",
                "REQUEST");

        Assertions.assertThat(acquiringPayOrderRequestLogs).contains("\"isDQR\":\"true\"");
    }

    @Owner(Constants.Owner.MOHIT_KHARE)
    @Feature("PG-1992")
    @Parameters({"theme"})
    @Test(description = "HDFU (MDR_FEE_ON_HDFC_UPI_SUBTYPE) merchant checkout — open cashier, close, verify ACQUIRING_PAY_ORDER REQUEST logs contain order id and isDQR true")
    public void validateHDFUCheckout_CashierOpenClose_AcquiringPayOrderRequestContainsIsDqr(
            @Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.MDR_FEE_ON_HDFC_UPI_SUBTYPE;
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("200")
                .build();
        String orderId = orderDTO.getORDER_ID();

        try {
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
        } finally {
            DriverManager.getDriver().close();
        }

        String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                orderId,
                "ACQUIRING_PAY_ORDER",
                "REQUEST");

        Assertions.assertThat(acquiringPayOrderRequestLogs)
                .as("ACQUIRING_PAY_ORDER REQUEST should contain isDQR true")
                .contains("\"isDQR\":\"true\"");
    }

    @Owner(Constants.Owner.MOHIT_KHARE)
    @Feature("PG-1992")
    @Parameters({"theme"})
    @Test(description = "PCF_FEE_ON_HDFC_UPI_SUBTYPE — open cashier, StaticQrUpiPSP (mid + order), success; ACQUIRING_PAY_ORDER REQUEST logs contain order id and isDQR true")
    public void validatePCF_HDFC_Checkout_StaticQrUpiPsp_Success_AcquiringPayOrderRequestContainsIsDqr(
            @Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.PCF_FEE_ON_HDFC_UPI_SUBTYPE;
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("200")
                .build();
        String orderId = orderDTO.getORDER_ID();

        try {
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();

            StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), orderDTO.getTXN_AMOUNT())
                    .setOrderId(orderDTO.getORDER_ID())
                    .build();
            StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
            Response response = staticQrUpiPSP.execute();
            StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
            Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                    .as("StaticQrUpiPSP response should be success")
                    .isEqualToIgnoringCase("SUCCESS");

            String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                    PG2LogsValidationHelper.setEnvService.theia_facade,
                    orderId,
                    "ACQUIRING_PAY_ORDER",
                    "REQUEST");
            Assertions.assertThat(acquiringPayOrderRequestLogs).contains("\"isDQR\":\"true\"");
        } finally {
            DriverManager.getDriver().close();
        }
    }
}
