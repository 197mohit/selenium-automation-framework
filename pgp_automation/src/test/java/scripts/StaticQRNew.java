package scripts;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.FastForward;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.Head;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.merchant.util.Merchant;
import groovy.json.JsonSlurper;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
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
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.LocalConfig.JSON_POST_URL;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static io.restassured.RestAssured.given;

@Owner("Tarun")
public class StaticQRNew extends PGPBaseTest {

    PaymentDTO paymentDTO = new PaymentDTO();
    Constants.MerchantType merchantType = Constants.MerchantType.PG2_CC_FULL_TRAFFIC_ENABLED;

    @BeforeClass
    public void successTxnOfHigherAmountToIncreaseMPABalanceOfUser() throws Exception {

        String txnAmount = "40000.00";//To increase Merchant's MPA balance so that merchant have balance to give back refund to user
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
    }

    //-------------------------------------Test Cases  Started------------------------------------------

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success PCF postpaid transaction using static QR")
    public void validatePostpaid_PCF_Txn_UsingStaticQR() throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_PCF_QR;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, "enhancedweb", user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
        //String qrCodeId = QRHelper.generateQRViaWallet(merchant);


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
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.notNullValue())
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.orderId", Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.merchantAccept",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.userAccountExist",
                            Matchers.equalToIgnoringCase("true"))
            ;
            orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setMpin(new PaymentDTO().getPasscode())
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();
        processTxnV1Request.getHead().setChannelId("APP");
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "PAYTM_DIGITAL_CREDIT", "PAYTMCC");
    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success postpaid transaction using static QR")
    public void validatePostpaid_Txn_UsingStaticQR() throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_PCF_QR;
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, "enhancedweb", user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();
        String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
       // String qrCodeId= QRHelper.generateQRViaWallet(merchant);

        fetchQrPaymentDetails:
        {
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCodeId)
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
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                    .body("body.qrInfo.response.ORDER_ID", Matchers.notNullValue())
                    .body("body.paymentOptions.orderId", Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.merchantAccept",
                            Matchers.equalToIgnoringCase("true"))
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions[0].isDisabled.userAccountExist",
                            Matchers.equalToIgnoringCase("true"))
            ;
            orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
        }

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setMpin(new PaymentDTO().getPasscode())
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();
        processTxnV1Request.getHead().setChannelId("APP");
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "PAYTM_DIGITAL_CREDIT", "PAYTMCC");
    }
    private ThreadLocal<String> qrCodeId = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }
    };

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success GV transaction using static QR")
    public void validateGV_Txn_UsingStaticQR() throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        Double walletBalance = 3.00;
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.setZeroBalance(user);
            WalletHelpers.updateGVBalance(user, walletBalance);
            Constants.MerchantType merchant = Constants.MerchantType.GV_UPI_INTENT;
            OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enahncedweb")
                    .setSSO_TOKEN("")
                    .setTXN_AMOUNT("2.00").build();

            String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
            //String qrCodeid= qrCodeId.get();

            fetchQrPaymentDetails:
            {
                FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                        .setQRCodeId(qrCodeId)
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
                        .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                        .body("body.qrInfo.response.ORDER_ID", Matchers.notNullValue())
                        .body("body.paymentOptions.pcfEnabled", Matchers.is(false))
                        .body("body.paymentOptions.orderId", Matchers.notNullValue())
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
                orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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
                        .isEqualToIgnoringCase("OFFLINE");
                softly.assertThat(j.getString("body.extendInfo.merchantTransId"))
                        .isNotNull();
                softly.assertThat(j.getString("body.extendInfo.PAYTM_USER_ID"))
                        .isNotNull();
                softly.assertThat(j.getString("body.extendInfo.paytmMerchantId"))
                        .isNotNull();
                softly.assertThat(j.getString("body.extendInfo.alipayMerchantId"))
                        .isNotNull();
                softly.assertThat(format.format(WalletHelpers.getGVBalance(user)))
                        .as("GV wallet balance mismatch")
                        .isEqualTo(format.format(walletBalance - Double.valueOf(orderDTO.getTXN_AMOUNT())));
                softly.assertAll();
            }

            PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                    .validateStatus("TXN_SUCCESS")
                    .validateGatewayName("WALLET")
                    .validatePaymentMode("PPI")
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .AssertAll();
        } else throw new RuntimeException("User is not Min Kyc Expired");
    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success PCF GV transaction using static QR")
    public void validateGV_PCF_Txn_UsingStaticQR() throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        Double walletBalance = 3.00;
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.setZeroBalance(user);
            WalletHelpers.updateGVBalance(user, walletBalance);
            Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
            OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enahncedweb")
                    .setSSO_TOKEN("")
                    .setTXN_AMOUNT("2.00").build();

            String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");

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
                        .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                        .body("body.qrInfo.response.ORDER_ID", Matchers.notNullValue())
                        .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                        .body("body.paymentOptions.orderId", Matchers.notNullValue())
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
                orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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
                        .setReqType("CLW_APP_PAY")
                        .setToken(user.ssoToken())
                        .setTokenType("SSO")
                        .setCustomerId(user.custId())
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
                        .isEqualToIgnoringCase("OFFLINE");
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
        } else throw new RuntimeException("User is not Min Kyc Expired");
    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success PCF CC default transaction using static QR")
    public void validateCC_PCF_Txn_UsingStaticQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();

        String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
        //String qrCodeId= QRHelper.generateQRViaWallet(merchant);
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
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.orderId", Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'CREDIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
            orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "CC", "HDFC");
    }

    @Test(description = "Validate Success CC default transaction using static QR")
    public void validateCCTxn_UsingStaticQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);
        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success PCF DC default transaction using static QR")
    public void validateDC_PCF_Txn_UsingStaticQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();

        String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
        //String qrCodeId= QRHelper.generateQRViaWallet(merchant);
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
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.orderId", Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'DEBIT_CARD'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
            orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("DEBIT_CARD")
                .setQRCodeId(qrCodeId)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "DC", "HDFC");
    }

    @Test(description = "Validate Success DC default transaction using static QR")
    public void validateDCTxn_UsingStaticQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "DEBIT_CARD";
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success PCF NB transaction using static QR")
    public void validateNB_PCF_Txn_UsingStaticQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();

        String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
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
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.orderId", Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'NET_BANKING'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
            orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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
                    .spec(pcfBodySuccessVerify("NET_BANKING", orderDTO.getTXN_AMOUNT()))
            ;
            totalTxnAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.NET_BANKING.totalTransactionAmount.value");
            totalPcfAmount = fetchPcfDetailResp.jsonPath().getString("body.consultDetails.NET_BANKING.totalConvenienceCharges.value");
        }

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "NB", "ICICI");
    }

    @Test(description = "Validate Success NB transaction using static QR")
    public void validateNBTxn_UsingStaticQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "NET_BANKING";
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setChannelCode("ICICI")
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "NB", "ICICI");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "NB", "ICICI");
        QRHelper.validateSuccessSMSQR(orderDTO);

    }

    @Issue("PGP-20655")
    @Test(description = "To test Static QR order success by dc when non-matching website provided",groups = Group.Status.BUG)
    public void testOrderSuccessByDCWhenNonMatchingWebsiteProvided() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "DEBIT_CARD";
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setWebsite("nonmatchingwebsite")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success PCF EMI transaction using static QR merchant")
    public void validateEMI_PCF_Txn_UsingStaticQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb_revamp")
                .setTXN_AMOUNT("2.00")
                .build();

        String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
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
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.orderId", Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'EMI'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
            orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("EMI")
                .setPlanId("HDFC|3")
                .setEmiType("CREDIT_CARD")
                .setCardNum(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "EMI", "HDFC");
    }

    @Test(description = "Validate Success EMI transaction using static QR merchant")
    //BAJAJFN is not supported for Offline EMI Txns
    public void validateEMITxn_UsingStaticQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "EMI";
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|3")
                .setEmiType("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    @Test(description = "Validate Success Zero Cost EMI transaction using static QR merchant")
    public void validateZeroCostEMITxn_UsingStaticQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "EMI";
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setPlanId("HDFC|1")
                .setEmiType("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "EMI", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    //ICICI UPI refund is offline
    @Test(description = "Validate Success UPI transaction using static QR merchant")
    public void validateUPITxn_UsingStaticQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "UPI";
        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);
        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "UPI", "ICICI");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "UPI", "ICICI");
        QRHelper.validateSuccessSMSQR(orderDTO);


    }

    @Owner("Ankur Agarwal")
    @Test(description = "Validate Success PCF Balance transaction using static QR merchant using Fast forward API")
    public void validateBalance_PCF_Txn_UsingStaticQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_GV_UPI_INTENT;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), merchant.getKey(), "enhancedweb")
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, 5.00);

        String qrCodeId = merchant.getProperty("STATIC_UPI_QR_CODE_ID");
        //String qrCodeId= QRHelper.generateQRViaWallet(merchant);
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
                    .body("body.qrInfo.response.orderQr", Matchers.equalTo(false))
                    .body("body.paymentOptions.pcfEnabled", Matchers.is(true))
                    .body("body.paymentOptions.orderId", Matchers.notNullValue())
                    .body("body.paymentOptions.merchantPayOption.paymentModes.find{it.paymentMode == 'BALANCE'}.isDisabled.status",
                            Matchers.equalToIgnoringCase("false"))
            ;
            orderDTO.setOrderId(fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId"));
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
                    .setReqType("CLW_APP_PAY")
                    .setToken(user.ssoToken())
                    .setTokenType("SSO")
                    .setCustomerId(user.custId())
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
                    .isEqualToIgnoringCase("OFFLINE");
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

        QRHelper.validateTxnStatus(orderDTO, "PPI", "WALLET");
    }

    @Test(description = "Validate Success Balance transaction using static QR merchant using Fast forward API")
    public void validateBalanceTxn_UsingStaticQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String txnAmount = "2.00";
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount));

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .build();
        QRHelper.executeFastForwardAPP(fastForwardAppRequest);

        QRHelper.validateTxnStatus(orderDTO, "PPI", "WALLET");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "PPI", "WALLET");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);


    }

    @Test(description = "Validate Success PPBL transaction using static QR merchant")
    public void validatePPBLTxn_UsingStaticQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "PPBL";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setMpin("1234")
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "NB", "PPBL");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "NB", "PPBL");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);
    }


    @Test(description = "Validate Success transaction using Saved Card using static QR merchant")
    public void validateSuccessTxn_UsingSavedCardStaticQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "DEBIT_CARD";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user, 0);
        String cardInfo = cardId + "||" + paymentDTO.getCvvNumber() + "|";

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setCardInfo(cardInfo)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "DC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }


    @Test(description = "Validate Failure transaction when incorrect SSO token is provided using static QR merchant")
    public void validateFailureTxn_InvalidSSOTokenUsingStaticQR() {

        //Generate QR
        GenerateQR generateQR = new GenerateQR(merchantType.getId(), "");
        //  JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        //FetchQRPaymentDetails

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken("ABCD")
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).as("Error message is incorrect").isEqualTo("SSO Token is invalid");
    }

    @Test(description = "Validate Failure transaction when empty txn token is provided using static QR merchant")
    public void validateFailureTxnxnTokenUsingStaticQR() throws Exception {

        GenerateQR generateQR = new GenerateQR(merchantType.getId(), "");
        //  JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("")
                .setToken("")
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).as("Error message is incorrect").isEqualTo("System error");

    }

    @Test(description = "Validate Failure transaction when INCORRECT MID is provided using static QR merchant")
    public void validateFailureIncorrectMIDUsingStaticQR() throws Exception {


        GenerateQR generateQR = new GenerateQR(merchantType.getId(), "");
        JsonPath generateJson = generateQR.execute().jsonPath();
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID("1234")
                .setTokenType("")
                .setToken("")
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).as("Error message is incorrect").isEqualTo("System error");

    }


    @Test(description = "Validate DirectForms are returned for static QR when ff4J flag is ON preference nativeOTPSupported not active")
    public void validateGenerateDirectFormParamsWhenFF4JisON_UsingStaticQR() throws Exception {

        String txnAmount = "2.00";
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.STATICQR_DIRECT_HDFO;
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setMpin("1234")
                .setExtendInfoStaticFlow()
                .build();


        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

       List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");


        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();

        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("HDFC Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("HDFO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());

        validateSoftly.assertAll();


    }


    @Test(description = "Validate DirectForms are Not returned for static QR when ff4J flag is Off and preference Not active")
    public void validateDirectFormNotGeneratedWhenFF4JisOFF_UsingStaticQR() throws Exception {

        String txnAmount = "2.00";
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.ICIO_CC_Enabled_Merchant_Retry;

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "nativeOTPSupported", "N");

        }

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)
                .setQRCodeId(qrCodeId)
                .setMpin("1234")
                .setExtendInfoStaticFlow()
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");

        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNull();

    }


    @Test(description = "Validate DirectForms are returned for static QR when ff4J flag is Off and preference is nativeOTPSupported")
    public void validateDirectFormGeneratedWhenFF4JisOFFPrefActive_UsingStaticQR() throws Exception {

        String txnAmount = "2.00";
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.ICIO_CC_Enabled_Merchant;

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "nativeOTPSupported", "Y");

        }

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)
                .setQRCodeId(qrCodeId)
                .setMpin("1234")
                .setExtendInfoStaticFlow()
                .build();


        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");

        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();

        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("ICICI Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("ICIO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());

        validateSoftly.assertAll();


    }



    @Test(description = "Validate DirectForms are returned for static QR when ff4J flag is ON and preference is nativeOTPSupported")
    public void validateDirectFormGeneratedWhenFF4JisONPrefActive_UsingStaticQR() throws Exception {

        String txnAmount = "2.00";
        String paymentMode = "CREDIT_CARD";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.STATICQR_DIRECT_ICIO;

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "nativeOTPSupported", "Y");

        }

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)
                .setQRCodeId(qrCodeId)
                .setMpin("1234")
                .setExtendInfoStaticFlow()
                .build();


        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");

        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();

        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("ICICI Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("ICIO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(orderDTO.getMID());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());

        validateSoftly.assertAll();


    }

    @Test(description = "Validate Success CC default transaction using static QR where txn amount is less than merchant limit list")
    public void validateCCTxn_UsingStaticQR_TxnAmountLessThanLimit() throws Exception {



        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        Double amt = new Merchant(merchant.getId()).getLimits().stream().filter(limit -> limit.getPayMode().equalsIgnoreCase("cc")).findFirst().get().getMax();
        String paymentMode = "CREDIT_CARD";
        Double txnAmount = amt-1.00;

        User user = userManager.getForRead(Label.BASIC);
        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStaticWithMerchantLimitList(merchant, txnAmount.toString(), user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "CC", "HDFC");
        String payMode="CC";
        String gatewayName="HDFC";
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "CC", "HDFC");
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
                peon.txnId().equals("").not()
        );
        sAssert.eval();
        QRHelper.validateSuccessPeonStaticQR(orderDTO, "CC", "HDFC");
        QRHelper.validateSuccessSMSQR(orderDTO);
        QRHelper.validateRefund(orderDTO);
        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);

    }

    @Test(description = "Validate Failure CC default transaction using static QR where txn amount is more than merchant limit list")
    public void validateCCTxnFailure_UsingStaticQR_TxnAmountMoreThanLimit() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        Double amt = new Merchant(merchant.getId()).getLimits().stream().filter(limit -> limit.getPayMode().equalsIgnoreCase("cc")).findFirst().get().getMax();
        String paymentMode = "CREDIT_CARD";
        Double txnAmount = amt+1.00;

        User user = userManager.getForRead(Label.BASIC);
        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStaticWithMerchantLimitList(merchant, txnAmount.toString(), user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        new ProcessTransactionV1(processTxnV1Request).execute().then()
               .statusCode(200)
               .body("body.resultInfo.resultStatus", Matchers.equalToIgnoringCase("F"))
               .body("body.resultInfo.resultCode",Matchers.equalToIgnoringCase("0001"))
               .body("body.resultInfo.resultMsg",Matchers.equalToIgnoringCase("Please try with lower amount or different payment mode for this transaction."));
    }


    @Owner("Eshani")
    @Test(description = "Verify <whetherBuyerUserIdChange> flag is passed in COP request incase of Static QR payment with CLW_APP_PAY/APP")
    public void validateWhetherBuyerUserIdChangeFlagisFalseinFastFwdTxn_UsingStaticQR() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String txnAmount = "2.00";
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount));

        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(orderDTO.getMID(), orderDTO.getORDER_ID(), txnAmount)
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .build();
        QRHelper.executeFastForwardAPP(fastForwardAppRequest);

        QRHelper.validateTxnStatus(orderDTO, "PPI", "WALLET");
//        QRHelper.validateSuccessPeonStaticQR(orderDTO, "PPI", "WALLET");
//        QRHelper.validateSuccessSMSQR(orderDTO);
//        QRHelper.validateRefund(orderDTO);
//        QRHelper.validateSuccessRefundNotifyQR(merchantType, orderDTO);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\"whetherBuyerUserIdChange\":\"FALSE\"");
    }

    @Owner("Eshani")
    @Test(description = "Verify <whetherBuyerUserIdChange> flag is passed in COP request incase of Static QR payment")
    public void validateWhetherBuyerUserIdChangeFlagisFalseinPPBLTxn_UsingStaticQR() throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "PPBL";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setMpin("1234")
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        QRHelper.validateTxnStatus(orderDTO, "NB", "PPBL");
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\"whetherBuyerUserIdChange\":\"FALSE\"");
    }
    private Response ExecuteDirectFormRequest(List<HashMap<String, Object>> DirectForm){

        String actionUrl = (DirectForm.get(0)).get("actionUrl").toString();
        String respBody  = new JSONObject((Map<?,?>) (DirectForm.get(0)).get("content"))
                .toString().replace("<OTP>","123456");
        return given().body(respBody).contentType(ContentType.JSON).when().post(actionUrl);

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
                .expectBody(paymode + ".feeAmount.value", Matchers.notNullValue())
                .expectBody(paymode + ".taxAmount.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalConvenienceCharges.value", Matchers.notNullValue())
                .expectBody(paymode + ".totalTransactionAmount.value", Matchers.notNullValue())
                .build();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-35331")
    @Test(description = "Verify  ppbl and paytm postpaid balance is null in response when fetchPaytmInstrumentsBalance is set to false in request body")
    public void Verify_PPBL_PaytmPostpaid_balance_is_null_in_response() throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        //  JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrid = generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO=new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
          .build();

        FetchQRPaymentDetails qr=new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance","False");
        qr.setContext("head.version","v2");

        Response response=qr.execute();
        for(int j=0;j<10;j++)
        {
            if(response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+j+"].displayName").equals("Paytm Postpaid"))
            {
                String Bal=response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+j+"].payChannelOptions[0].balanceInfo");
                Assertions.assertThat(Bal).isNull();
                break;
            }
        }
        for(int k=0;k<10;k++)
        {
            if(response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+k+"].displayName").equals("Paytm Payments Bank"))
            {
                String Bal=response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+k+"].payChannelOptions[0].balanceInfo");
                Assertions.assertThat(Bal).isNull();
                break;
            }
        }
           }
    @Owner(SRINIVAS)
    @Feature("PGP-35331")
    @Test(description = "Verify currency and value is showing under account balance when fetchPaytmInstrumentsBalance is set to true for wallet,ppbl and paytm postpaid")
    public void Verify_PPBL_Wallet_PaytmPostpaid_currency_value_is_showing_in_response() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        JsonPath generateJson = generateQR.execute().jsonPath();
        String qrid=generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO=new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr=new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance","True");
        qr.setContext("head.version","v2");
        Response response=qr.execute();
        for(int i=0;i<10;i++)
        {
            if(response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+i+"].displayName").equals("Paytm Balance"))
            {
                String Bal=response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+i+"].payChannelOptions[0].balanceInfo");
                Assertions.assertThat(Bal).isNotEmpty().isNotNull();
                break;
            }
        }
        for(int j=0;j<10;j++)
        {
                if(response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+j+"].displayName").equals("Paytm Postpaid"))
            {
                String Bal=response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+j+"].payChannelOptions[0].balanceInfo");
                Assertions.assertThat(Bal).isNotEmpty().isNotNull();
                break;
            }
        }
        for(int k=0;k<10;k++)
        {
            if(response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+k+"].displayName").equals("Paytm Payments Bank"))
            {
                String Bal=response.jsonPath().getString("body.paymentOptions.merchantPayOption.paymentModes["+k+"].payChannelOptions[0].balanceInfo");
                Assertions.assertThat(Bal).isNotEmpty().isNotNull();
                break;
            }
        }
    }

    @Owner(Constants.Owner.ROHIT)
    @Feature("PGP-32296")
    @Test(description = "verify that extendinfo in cop should contain qrCodeId and qrDeeplink")
    public void verifyextendinfoCOPStaticQR() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, "2", user);
        String qrCodeId = orderDTO.getTxnId();
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"2")
                .setPaymentMode("DEBIT_CARD")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        String grepcmd = "grep \"" + "ACQUIRING_CREATE_ORDER_AND_PAY" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"qrCodeId\":\""+qrCodeId+"\"");
        Assertions.assertThat(extendInfo).contains("\"qrDeeplink\":\"https://qr.paytm.in/"+qrCodeId+"\"");

    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-33485")
    @Test(description = "Verify sms notification for BW online settlement merchants")
    public void Verify_sms_notification_BW_Instantsettlement() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BW_InstantSettlement;
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, "200", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);

        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);


        String grepcmdapplypromorequest = "less /paytm/logs/communicationGateway.log | grep 'Inside HIGH_PRIORITY_SMS'";
        String theiafacadelogsapplypromorequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmdapplypromorequest);
        Assertions.assertThat(theiafacadelogsapplypromorequest).contains("It will settle to your bank by 7 am tomorrow or you can transfer now.Visit b.paytm.me");
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-33485")
    @Test(description = "Verify sms notification for BW online settlement merchants")
    public void Verify_sms_notification_BW_only() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BW_Only;
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, "200", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);

        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);


        String grepcmdapplypromorequest = "less /paytm/logs/communicationGateway.log | grep 'Inside HIGH_PRIORITY_SMS'";
        String theiafacadelogsapplypromorequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmdapplypromorequest);
        Assertions.assertThat(theiafacadelogsapplypromorequest).contains("It will settle to your bank by 7 am tomorrow. Visit b.paytm.me");
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-33485")
    @Test(description = "Verify sms notification for BW online settlement merchants")
    public void Verify_sms_notification_NonBW_only() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.NonBW_OS;
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, "200", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);

        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);


        String grepcmdapplypromorequest = "less /paytm/logs/communicationGateway.log | grep 'Inside HIGH_PRIORITY_SMS'";
        String theiafacadelogsapplypromorequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmdapplypromorequest);
        Assertions.assertThat(theiafacadelogsapplypromorequest).contains("It will settle to your bank by 7 am tomorrow or you can transfer now.Visit b.paytm.me");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify posId in cop when we pass posId in generateqrcode request")
    public void verifyPosIdCOPStaticQR_GenerateQr() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String qrCodeId = QRHelper.generateStaticQRViaWallet(merchantType, posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"2")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_CREATE_ORDER_AND_PAY" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+posId+"\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify ptc posId in cop when we pass posId in generateqrcode and ptc txns request")
    public void verifyPosIdCOPStaticQR_GenerateQrAndPtcReq() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String qrCodeId = QRHelper.generateStaticQRViaWallet(merchantType, posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"2")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlowWithPosId()
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_CREATE_ORDER_AND_PAY" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+678900+"\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify posId in cop when we pass posId in generateqrcode request for Fastforword flow")
    public void verifyPosIdCOPStaticQR_GenerateQrForFastFwd() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 20.00);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String qrCodeId = QRHelper.generateStaticQRViaWallet(merchantType, posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderId, "10")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .build();
        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("body.resultInfo.resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_CREATE_ORDER_AND_PAY" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+posId+"\"");
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-32296")
    @Test(description = "verify FastForword posId in cop when we pass posId in generateqrcode and FastForword txns request")
    public void verifyPosIdCOPStaticQR_GenerateQrAndFastFwdReq() throws Exception {
        int posId = CommonHelpers.getRandomWithSize(5);
        int fwPosId = CommonHelpers.getRandomWithSize(5);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 20.00);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        String qrCodeId = QRHelper.generateStaticQRViaWallet(merchantType, posId);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchantType.getId(), orderId, "10")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .setPosId(String.valueOf(fwPosId))
                .build(true);
        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("body.resultInfo.resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertAll();
        String grepcmd = "grep \"" + "ACQUIRING_CREATE_ORDER_AND_PAY" + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderId + "\" | grep \"" + "REQUEST" + "\"" ;
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Map map = (Map) new JsonSlurper().parseText(logs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).contains("\"posId\":\""+fwPosId+"\"");
    }

    @Test(description = "Validate qrCodeId and qrDeepLink in Pay request extendInfo")
    public void validateqrCodeId_qrDeepLink() throws Exception {

        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.assertSMSPrefEnabledForStaticQR(merchantType.getId());
        PGPHelpers.assertRefundSuccessNotifyPeon(merchantType.getId());

        String paymentMode = "UPI";
        String txnAmount = "2.00";

        User user = userManager.getForRead(Label.BASIC);
        //Generate QR and FetchQRPaymentDetails
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        QRHelper.validateTxnStatus(orderDTO, "UPI", "ICICI");


        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiafacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);

        Assertions.assertThat(theiafacadelogs)
                .contains("qrDeeplink\\\""+":\\\"https://qr.paytm.in/"+qrCodeId+"\\\"");

        //Finding Count for qrDeeplink in pay request
        String grepcmd1 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep -o \"qrDeeplink\" | wc -l";
        String qrDeeplinkCount = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd1);

        //qrDeeplink should be present in 2 times(One should be present in extendInfo and 1 in channelInfo)
        Assertions.assertThat(qrDeeplinkCount).isEqualTo("2");

        Assertions.assertThat(theiafacadelogs)
                .contains("qrCodeId\\\""+":\\\""+qrCodeId+"\\\"");
        System.out.println(theiafacadelogs);
        //Finding Count for qrCodeId in pay request
        String grepcmd2 = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep -o \"qrCodeId\" | wc -l";
        String qrCodeIdCount = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd2);

        //QrCodeId should be present 4 times first in additionalInfo provided from PTC call and second added by theia. Same in channelInfo
        Assertions.assertThat(qrCodeIdCount).isEqualTo("4");

    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant Accepting Postpaid & User has postpaid Enabled & Preference is ON and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should  be true display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_display_in_the_fetch_qr_details_repsone() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_POSTPAID;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        JsonPath generateJson = generateQR.execute().jsonPath();
        String qrid = generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "True");
        qr.setContext("head.version", "v2");
        Response response=qr.execute();
        response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue").equals("1500");
        response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled").equals(true);
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant Accepting Postpaid & User has postpaid Enabled & Preference is OFF and FF4j theia.enable.postpaid.2FA is OFF postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should not display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_not_display_in_the_fetch_qr_details_repsone() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_POSTPAID1;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        JsonPath generateJson = generateQR.execute().jsonPath();
        String qrid = generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");
        Response response = qr.execute();
        String res=response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled");
        Assertions.assertThat(res).isNull();
        String res1= response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue");
        Assertions.assertThat(res1).isNull();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant not Accepting Postpaid & User has not postpaid Enabled & Preference is ON and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should not display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_not_be_display_in_the_fetch_qr_details_repsone_pref_ff4j_is_on() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_NO_POSTPAID;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        JsonPath generateJson = generateQR.execute().jsonPath();
        String qrid = generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");
        Response response = qr.execute();
        String res=response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled");
        Assertions.assertThat(res).isNull();
        String res1= response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue");
        Assertions.assertThat(res1).isNull();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-40598")
    @Test(description = "Verify Merchant  Accepting Postpaid & User has postpaid Enabled & Preference is OFF and FF4j theia.enable.postpaid.2FA is ON postpaid2FAThresholdValue  and postpaid2FAEnabled should be true should  display in the fetch qr details repsone")
    public void Verify_postpaid2FAThresholdValue_and_postpaid2FAEnabled_should_be_true_and_should_be_displayed_in_the_fetch_qr_details_repsone_pref_is_off_ff4j_is_on() throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.TWOFA_POSTPAID;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        JsonPath generateJson = generateQR.execute().jsonPath();
        String qrid = generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");
        Response response = qr.execute();
        response.jsonPath().getString("body.paymentOptions.postpaid2FAThresholdValue").equals("1500");
        response.jsonPath().getString("body.paymentOptions.postpaid2FAEnabled").equals(true);
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-44639")
    @Test(description = "verify merchant info in failure response ")
    public void verifyMerchantinfoStaticQR() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, "2", user);
        String qrCodeId = orderDTO.getTxnId();
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"99.97")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlowWithPosId()
                .setPaymentMode("CREDIT_CARD")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");
        processTransactionV1.execute();
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"99.98")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlowWithPosId()
                .setPaymentMode("CREDIT_CARD")
                .build();
        ProcessTransactionV1 processTransactionV12 = new ProcessTransactionV1(processTxnV1Request1);
        processTransactionV12.getRequestSpecBuilder().addHeader("source","OFFLINE");
        Response response = processTransactionV12.execute();
       Assertions.assertThat(response.jsonPath().getString("body.txnInfo.ADDITIONAL_PARAM").contains("merchantInfo")).isTrue();
    }
    @Owner(PAREEKSHITH)
    @Feature("PGP-44639")
    @Test(description = "verify merchant info in failure response ")
    public void verifyMerchantinfoStaticQRwallet() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, "2", user);
        String qrCodeId = orderDTO.getTxnId();
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"99.97")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlowWithPosId()
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");
        processTransactionV1.execute();
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"99.98")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlowWithPosId()
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV12 = new ProcessTransactionV1(processTxnV1Request1);
        processTransactionV12.getRequestSpecBuilder().addHeader("source","OFFLINE");
        Response response = processTransactionV12.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.ADDITIONAL_PARAM").contains("merchantInfo")).isTrue();
    }

    }

