package scripts.scanNPay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.api.FastForward;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.api.wallet.GetQRCodeInfoApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
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
import java.util.Date;
import java.util.List;

@Owner("Tarun")
public class ScanNPayTest extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String JSON_POST_URL = LocalConfig.JSON_POST_URL;
    DecimalFormat format = new DecimalFormat("0.00");

    @Parameters({"theme"})
    @Test(description = "Validate FetchQRPaymentDetails option response for AddnPay merchant")
    public void validateFtchQRPayDetails_AddNPayMerch(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 2.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
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
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                .body("body.paymentOptions.paymentFlow", Matchers.equalToIgnoringCase("ADDANDPAY"))
                .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'UPI'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
        ;
    }

    @Parameters({"theme"})
    @Test(description = "Validate FetchQRPaymentDetails response for Hybrid merchant")
    public void validateFtchQRPayDetails_HybridMerch(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.Hybrid;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 2.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
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
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
        ;
    }

    @Parameters({"theme"})
    @Test(description = "Validate FetchQRPaymentDetails response for upi merchant")
    public void validateFtchQrPayDetails_upiMerch(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 2.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
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
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.isDisabled.status",
                        Matchers.equalToIgnoringCase("false"))
                .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.isDisabled.status",
                        Matchers.everyItem(Matchers.containsString("false")))
        ;
    }

    @Parameters({"theme"})
    @Test(description = "Validate QR code is displayed in cashier page when user is not logged in for normal merchant")
    public void validateQrCode_displayed_notLogIn_normMerch(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC, Label.NOPOSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.Hybrid;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.imgScanPayQRCode().assertVisible();
        String qrCodeText = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));

        GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(qrCodeText);      //validating QR code with wallet
        JsonPath jsonPath = getQRCodeInfoApi.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("response.mappingId"))
                .as("mid mismatched")
                .isEqualToIgnoringCase(merchant.getId());
        Assertions.assertThat(jsonPath.getString("response.ORDER_ID"))
                .as("orderId mismatched")
                .isEqualToIgnoringCase(orderDTO.getORDER_ID());
    }

    @Parameters({"theme"})
    @Test(description = "Validate QR code is displayed in cashier page when user is not logged in for UPI merchant")
    public void validateQrCode_displayed_notLogIn_upiMerch(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC, Label.NOPOSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.imgScanPayQRCode().assertVisible();
        String qrCodeText = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));

        GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(qrCodeText);       //validating QR code with wallet
        JsonPath jsonPath = getQRCodeInfoApi.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("response.mappingId"))
                .as("mid mismatched")
                .isEqualToIgnoringCase(merchant.getId());
        Assertions.assertThat(jsonPath.getString("response.ORDER_ID"))
                .as("orderId mismatched")
                .isEqualToIgnoringCase(orderDTO.getORDER_ID());
    }

    @Parameters({"theme"})
    @Test(description = "Validate QR code is displayed in cashier page when user is not logged in for IRCTC merchant")
    public void validateQrCode_displayed_notLogIn_IRCTCMerch(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC, Label.NOPOSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.IRCTC_WalletOnly;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.imgScanPayQRCode().assertVisible();
        String qrCodeText = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));

        GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(qrCodeText);       //validating QR code with wallet
        JsonPath jsonPath = getQRCodeInfoApi.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("response.mappingId"))
                .as("mid mismatched")
                .isEqualToIgnoringCase(merchant.getId());
        Assertions.assertThat(jsonPath.getString("response.ORDER_ID"))
                .as("orderId mismatched")
                .isEqualToIgnoringCase(orderDTO.getORDER_ID());
    }

    @Parameters({"theme"})
    @Test(description = "Validate QR code is displayed in cashier page when user is logged in for UPI merchant")
    public void validateQrCode_displayed_LogIn_UPIMerch(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID, Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.imgScanPayQRCode().assertVisible();
        String qrCodeText = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.tabUPI().click();
       // cashierPage.imgScanPayQRCode().assertVisible();
        //String qrCodeText = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));

        GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(qrCodeText);       //validating QR code with wallet
        JsonPath jsonPath = getQRCodeInfoApi.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("response.mappingId"))
                .as("mid mismatched")
                .isEqualToIgnoringCase(merchant.getId());
        Assertions.assertThat(jsonPath.getString("response.ORDER_ID"))
                .as("orderId mismatched")
                .isEqualToIgnoringCase(orderDTO.getORDER_ID());
    }

    @Parameters({"theme"})
    @Test(description = "Validate success of wallet transaction after scanning paytm QR for Hybrid Merchant")
    public void validate_S_walletTxn_paytmQr(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Hybrid;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 2.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
                    .setMID(merchant.getId())
                    .setTokenType("SSO")
                    .setToken(user.ssoToken())
                    .build();

            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
            Response fetchQRResponse = fetchQRPaymentDetails.execute();
            fetchQRResponse.then()
                    .statusCode(200);
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
    @Test(description = "Validate success of CC transaction after scanning paytm QR for Hybrid Merchant")
    public void validate_S_CCTxn_paytmQr(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Hybrid;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 1.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
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
                    .setQRCodeId(qrCodeId)
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
    @Test(description = "Validate success of DC transaction after scanning paytm QR for AddNPay Merchant")
    public void validate_S_DCTxn_paytmQr(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 1.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.paymentFlow", Matchers.equalToIgnoringCase("ADDANDPAY"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'UPI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.addMoneyPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
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
                    .setQRCodeId(qrCodeId)
                    .setAddMoney(1)
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

    //TODO: As discussed with ankit upi collect is not supported after scanning QR code
//    @Deprecated
//    @Parameters({"theme"})
//    @Test(description = "Validate succes of UPI transaction after scanning paytm QR", enabled = false)
    public void validate_S_UPI_txn_paytmQR(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(true))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.equalToIgnoringCase(orderDTO.getORDER_ID()))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.isDisabled.status",
                            Matchers.everyItem(Matchers.containsString("false")))
            ;
        }

        processTxn:
        {
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                    .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setPaymentMode("UPI")
                    .setPayerAccount("test@paytm")
                    .setExtendInfoOrderAlreadyCreated(true)
                    .build();
            completeTxnInNewTab(processTxnV1Request);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateVPA("test@paytm")
                .validateTxnDate(new Date())
                .AssertAll();

        new ResponsePage()
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("UPI")
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "Validate paytmQr image and upiQR image when user logged in and logged out")
    public void validate_UI_QRSection(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        SoftAssertions softly = new SoftAssertions();
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        // Generate QR Code order
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);

      //  softly.assertThat(cashierPage.imgPaytmQRSymbol().isDisplayed())
            //    .isTrue();
       // softly.assertThat(cashierPage.imgUpiQRSymbol().isDisplayed())
              //  .isTrue();
        softly.assertThat(PGPHelpers.getQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src")))
                .startsWith("upi://pay");

        cashierPage.signin(user.mobNo(), user.password());
        cashierPage.pause(2);
        cashierPage.tabUPI().click();

        softly.assertThat(cashierPage.imgScanPayQRCode().isDisplayed())
                .isTrue();
        softly.assertThat(PGPHelpers.getQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src")))
                .startsWith("upi://pay");

        try {
            cashierPage.logout(user);
        } finally {
            user.purge();
        }
        cashierPage.pause(2);

        cashierPage.imgScanPayQRCode().waitUntilVisible();
        softly.assertThat(cashierPage.imgPaytmQRSymbol().isDisplayed())
                .isTrue();
        softly.assertThat(cashierPage.imgUpiQRSymbol().isDisplayed())
                .isTrue();
        softly.assertThat(PGPHelpers.getQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src")))
                .startsWith("upi://pay");

        softly.assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate PRN section is displayed for IRCTC merchant when PRN is allowed and txn is " +
            "successful after scanning QR code")
    public void validate_PRNSectionDisplayed(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.IRCTC_Wallet_withPRN;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 10.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
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
                            Matchers.equalToIgnoringCase("10.00"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes", Matchers.hasSize(1))
            ;
        }

        String totalTxnAmount = "";
        String chargeAmount = "";
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
            chargeAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalConvenienceCharges.value");
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
                    .setQrCodeId(qrCodeId)
                    .setTxnAmount(totalTxnAmount)
                    .setChargeAmount(chargeAmount)
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
                    .isEqualToIgnoringCase("51051000100000000027");
            softly.assertThat(j.getString("body.extendInfo.requestType"))
                    .as("body.extendInfo.requestType mismatch")
                    .isEqualToIgnoringCase("DYNAMIC_QR_2FA");
            softly.assertThat(j.getString("body.extendInfo.merchantTransId"))
                    .isNotNull();
            softly.assertThat(j.getString("body.extendInfo.PAYTM_USER_ID"))
                    .isNotNull();
            softly.assertThat(j.getString("body.extendInfo.paytmMerchantId"))
                    .isNotNull();
            softly.assertThat(j.getString("body.extendInfo.alipayMerchantId"))
                    .isNotNull();
            softly.assertThat(j.getString("body.prn"))
                    .as("body.prn")
                    .isNotNull();
            softly.assertThat(j.getString("body.chargeAmount"))
                    .as("chargeAmount mismatch")
                    .isEqualTo(format.format(Double.valueOf(totalTxnAmount) - Double.valueOf(orderDTO.getTXN_AMOUNT())));
            softly.assertAll();

        }

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePRN(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        cashierPage.prnVerifySection().assertVisible();
        String prn = txnStatus.getResponse().getPRN();
    }


    @Parameters({"theme"})
    @Test(description = "Verify Success transaction of IRCTC merchant after verify PRN at cashier page")
    public void validate_S_walletTxn_IRCTCMerc_PRNtrue(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.IRCTC_Wallet_withPRN;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 10.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
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
                            Matchers.equalToIgnoringCase("10.00"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes", Matchers.hasSize(1))
            ;
        }

        String totalTxnAmount = "";
        String chargeAmount = "";
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
            chargeAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.BALANCE.totalConvenienceCharges.value");
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
                    .setQrCodeId(qrCodeId)
                    .setTxnAmount(totalTxnAmount)
                    .setChargeAmount(chargeAmount)
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
                    .isEqualToIgnoringCase("51051000100000000027");
            softly.assertThat(j.getString("body.extendInfo.requestType"))
                    .as("body.extendInfo.requestType mismatch")
                    .isEqualToIgnoringCase("DYNAMIC_QR_2FA");
            softly.assertThat(j.getString("body.extendInfo.merchantTransId"))
                    .isNotNull();
            softly.assertThat(j.getString("body.extendInfo.PAYTM_USER_ID"))
                    .isNotNull();
            softly.assertThat(j.getString("body.extendInfo.paytmMerchantId"))
                    .isNotNull();
            softly.assertThat(j.getString("body.extendInfo.alipayMerchantId"))
                    .isNotNull();
            softly.assertThat(j.getString("body.paymentMode"))
                    .isEqualToIgnoringCase("PPI");
            softly.assertThat(j.getString("body.prn"))
                    .as("body.prn")
                    .isNotNull();
            softly.assertThat(j.getString("body.chargeAmount"))
                    .as("chargeAmount mismatch")
                    .isEqualTo(format.format(Double.valueOf(totalTxnAmount) - Double.valueOf(orderDTO.getTXN_AMOUNT())));
            softly.assertAll();

        }

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePRN(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        cashierPage.prnVerifySection().assertVisible();
        String prn = txnStatus.getResponse().getPRN();

        txnCompleteByPrn:
        {
            enterPRNandClickVerify(prn, cashierPage);
            new ResponsePage()
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .validateOrderId(orderDTO.getORDER_ID())
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode("PPI") //Checked Impact on Prod with Dev, Updating as No Impact observed
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify Success add then pay transaction when wallet balance is insufficient")
    public void validate_S_AddThenPay_txn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, 1.00);

        // Generate QR Code order
        String qrCodeId = generateQrCodeOrder(user, merchant, orderDTO, theme);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
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
                    .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
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
                            Matchers.equalToIgnoringCase("1.00"))
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
            WalletHelpers.modifyBalance(user, 4.00);
        }

        fastForward:
        {
            FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                    .Builder(merchant.getId(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                    .setOrderAlreadyCreated("true")
                    .setPaymentMode("PPI")
                    .setReqType("CLW_APP_PAY")
                    .setToken(user.ssoToken())
                    .setTokenType("SSO")
                    .setQrCodeId(qrCodeId)
                    .build();

            performFastForwardTxn(fastForwardAppRequest, orderDTO);
        }

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("WALLET")
                .validatePaymentMode("PPI")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("PPI")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }


    //========================== HELPER METHODS ============================//

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
                .expectBody(paymode + ".feeAmount.value", Matchers.notNullValue())
                .expectBody(paymode + ".taxAmount.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalConvenienceCharges.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalTransactionAmount.value", Matchers.notNullValue())
                .build();
    }

    @Step
    private void performFastForwardTxn(FastForwardAppRequest fastForwardAppRequest, OrderDTO orderDTO) {
        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(jsonPath.getString("body.txnAmount"))
                .isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        softly.assertThat(jsonPath.getString("body.paymentMode"))
                .isEqualToIgnoringCase("PPI");
        softly.assertThat(jsonPath.getString("body.extendInfo.requestType"))
                .isEqualToIgnoringCase("DYNAMIC_QR");
        softly.assertAll();
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
            DriverManager.getDriver().switchTo().window(tabs.get(0));
        }
    }

    @Step
    private void enterPRNandClickVerify(String prn, CashierPage cashierPage) {
        List<UIElement> prnBoxList = cashierPage.textBoxPRNNumber();
        if (prn.length() == prnBoxList.size()) {
            for (int i = 0; i < prn.length(); i++) {
                prnBoxList.get(i).sendKeys(String.valueOf(prn.charAt(i)));
            }
        } else
            Assertions.fail("texts in PRN string and PRN textbox count mismatch");
        cashierPage.buttonPRNVerify().click();
        cashierPage.waitUntilLoads();
    }

    @Step
    private FetchPaymentOptResponseDTO executeFetchPaymentOption(String mid, String orderId, FetchPaymentOptionsDTO fetchPaymentOptionsDTO) {
        FetchPaymentOption fthPymntOpt = new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO);
        FetchPaymentOptResponseDTO fetchPayResp =
                NativeHelpers.convertRespToObject(fthPymntOpt.execute(), FetchPaymentOptResponseDTO.class);
        if (!fetchPayResp.getBody().getResultInfo().getResultStatus().equalsIgnoreCase("S"))
            throw new SkipException("FetchPaymentOption response is not successful");
        return fetchPayResp;
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
            DriverManager.getDriver().switchTo().window(tabs.get(0));
        }
    }

    @Step("Generate QR Code order")
    private String generateQrCodeOrder(User user, Constants.MerchantType merchant, OrderDTO orderDTO, String theme) {
        checkoutPage.createOrder(orderDTO);
        checkoutPage.waitUntilLoads();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.imgScanPayQRCode().assertVisible();
        try {
            cashierPage.pause(2);
            String qrCodeText = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));

            GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(qrCodeText);      //validating QR code with wallet
            JsonPath jsonPath = getQRCodeInfoApi.execute().jsonPath();

            Assertions.assertThat(jsonPath.getString("response.mappingId"))
                    .as("mid mismatched")
                    .isEqualToIgnoringCase(merchant.getId());
            Assertions.assertThat(jsonPath.getString("response.ORDER_ID"))
                    .as("orderId mismatched")
                    .isEqualToIgnoringCase(orderDTO.getORDER_ID());
            return qrCodeText;
        } catch (Throwable throwable) {
            throw new SkipException("scan n pay QR code is not visible or not readed by paytm app", throwable);
        }
    }
}
