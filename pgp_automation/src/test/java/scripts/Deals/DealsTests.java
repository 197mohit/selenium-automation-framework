package scripts.Deals;

import com.paytm.api.Deals.ApplyPromo;
import com.paytm.api.Deals.FetchPaymentOptions;
import com.paytm.api.Deals.GetPaymentStatus;
import com.paytm.api.Deals.InitiateTransaction;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.processTransactionV1.HybridPayModeDetail;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class DealsTests extends PGPBaseTest {

    @Feature("PG2-8220")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Deals only COP UPI transactions with merchant contribution of 10")
    public void successfulDealsOnlyUPITransactionViaCOP() throws Exception {
        String merchantId = Constants.MerchantType.PG2_Deals.getId();
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "100";
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");

        // Subtracting merchant deals contribution
        txnAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);
        InitiateTransaction initiateTransaction = new InitiateTransaction().dealsbuildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantId,txnToken,initiateTransaction.getOrderId())
                .setPaymentMode("UPI")
                .setChannelCode("collect")
                .setPayerAccount("9538003818@paytm").build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String createorderAndPay = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String assertion3 = "\"orderPricingInfo\":{\"pricingAmountInfoList\":[{\"amountType\":\"DEAL_DISCOUNT\",\"direction\":\"POSITIVE\",\"pricingAmount\":{\"currency\":\"INR\",\"value\":\"1000\"},\"contriDetail\":{\"merchantContri\":\"10.0\"}}]}}";
        Assertions.assertThat(createorderAndPay).contains(assertion3);
        Assertions.assertThat(createorderAndPay).contains("merchantUniqueReference");
        Assertions.assertThat(createorderAndPay).contains("merchantRequestId");
        Assertions.assertThat(createorderAndPay).contains("posId");

        String affordabilityPlatform = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM");
        String assertion1 = "\"TYPE\" : \"RESPONSE\", \"RESPONSE\" : {\"resultInfo\":{\"code\":\"000\",\"status\":\"S\",\"message\":\"Success\"";
        String assertion2 = "\"orderAmount\":{\"value\":9000,\"currency\":\"INR\"},\"billAmount\":{\"value\":10000,\"currency\":\"INR\"}";
        Assertions.assertThat(affordabilityPlatform).contains(assertion1);
        Assertions.assertThat(affordabilityPlatform).contains(assertion2);

        Thread.sleep(20000);
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantId, initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("UPI");
        Assertions.assertThat(paymentStatusResponse.getString("body.gatewayName")).isEqualTo("PPBLC");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f",Double.parseDouble(txnAmount)));

    }

    @Feature("PG2-8220")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Deals + bank offer COP CC transactions with merchant contribution of 10 and bank discount of 10")
    public void successfulDealsBankOfferCCTransactionViaCOP() throws Exception {
        String merchantId = Constants.MerchantType.PG2_Deals.getId();
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "100";

        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.paymentOffers[0].promocode")).isNotNull();
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");

        // Subtracting merchant deals contribution
        txnAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);

        ApplyPromo applyPromo = new ApplyPromo().buildWithParameters(merchantId,user.ssoToken(),txnAmount);
        JsonPath applyPromoResponse = applyPromo.execute().jsonPath();
        Assertions.assertThat(applyPromoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(applyPromoResponse.getString("body.paymentOffer.verificationCode")).isNotNull();
        String verificationCode = applyPromoResponse.getString("body.paymentOffer.verificationCode");
        String promoCode = applyPromoResponse.getString("body.paymentOffer.promoCode");
        Assertions.assertThat(applyPromoResponse.getString("body.paymentOffer.savings[0].redemptionType")).isEqualTo("discount");

        // Subtracting bank offer contribution
        String payableAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);
        InitiateTransaction initiateTransaction = new InitiateTransaction()
                .dealsBankOfferbuildRequest(merchantId,txnAmount,payableAmount,user.ssoToken(),promoCode,verificationCode);
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantId,txnToken,initiateTransaction.getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp").build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String createorderAndPay = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String assertion3 = "\"orderPricingInfo\":{\"pricingAmountInfoList\":[{\"amountType\":\"DEAL_DISCOUNT\",\"direction\":\"POSITIVE\",\"pricingAmount\":{\"currency\":\"INR\",\"value\":\"1000\"},\"contriDetail\":{\"merchantContri\":\"10.0\"}},{\"amountType\":\"BANK_EMI_INSTANT_CASHBACK\",\"direction\":\"POSITIVE\",\"pricingAmount\":{\"currency\":\"INR\",\"value\":\"1000\"},\"contriDetail\":{\"bankContri\":\"10.0\"}}]}}";
        Assertions.assertThat(createorderAndPay).contains(assertion3);
        Assertions.assertThat(createorderAndPay).contains("merchantUniqueReference");
        Assertions.assertThat(createorderAndPay).contains("merchantRequestId");
        Assertions.assertThat(createorderAndPay).contains("posId");

        String orderAmount = String.format("%.2f",Double.parseDouble(payableAmount));
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantId, initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("CC");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(orderAmount);

        String affordabilityPlatform = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM");
        String assertion1 = "\"TYPE\" : \"RESPONSE\", \"RESPONSE\" : {\"resultInfo\":{\"code\":\"000\",\"status\":\"S\",\"message\":\"Success\"";
        String assertion2 = "\"orderAmount\":{\"value\":8000,\"currency\":\"INR\"},\"billAmount\":{\"value\":10000,\"currency\":\"INR\"}";
        Assertions.assertThat(affordabilityPlatform).contains(assertion1);
        Assertions.assertThat(affordabilityPlatform).contains(assertion2);

    }

    @Feature("PG2-8220")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "AddnPay Deals only COP CC transactions with merchant contribution of 10")
    public void successfulDealsOnlyAddnPayCCTransactionViaCOP() throws Exception {
        String merchantId = Constants.MerchantType.PG2_Deals_AddnPay.getId();
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, Double.valueOf("80"));
        String txnAmount = "100";

        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");

        // Subtracting merchant deals contribution
        txnAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);
        InitiateTransaction initiateTransaction = new InitiateTransaction().dealsbuildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantId,txnToken,initiateTransaction.getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setPaymentFlow("ADDANDPAY")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp").build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String createorderAndPay = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String assertion3 = "\"orderPricingInfo\":{\"pricingAmountInfoList\":[{\"amountType\":\"DEAL_DISCOUNT\",\"direction\":\"POSITIVE\",\"pricingAmount\":{\"currency\":\"INR\",\"value\":\"1000\"},\"contriDetail\":{\"merchantContri\":\"10.0\"}}]}}";
        String assertion4 = "\"acquiringType\":\"OFFLINE_DEALS\"";
        Assertions.assertThat(createorderAndPay).contains(assertion3);
        Assertions.assertThat(createorderAndPay).doesNotContain(assertion4);
        Assertions.assertThat(createorderAndPay).contains("merchantUniqueReference");
        Assertions.assertThat(createorderAndPay).contains("merchantRequestId");
        Assertions.assertThat(createorderAndPay).contains("posId");

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantId, initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("PPI");
        Assertions.assertThat(paymentStatusResponse.getString("body.gatewayName")).isEqualTo("WALLET");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f",Double.parseDouble(txnAmount)));

        String affordabilityPlatform = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM");
        String assertion1 = "\"TYPE\" : \"RESPONSE\", \"RESPONSE\" : {\"resultInfo\":{\"code\":\"000\",\"status\":\"S\",\"message\":\"Success\"";
        String assertion2 = "\"orderAmount\":{\"value\":9000,\"currency\":\"INR\"},\"billAmount\":{\"value\":10000,\"currency\":\"INR\"}";
        Assertions.assertThat(affordabilityPlatform).contains(assertion1);
        Assertions.assertThat(affordabilityPlatform).contains(assertion2);

    }

    @Feature("PG2-8220")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "AddnPay Deals only COTP CC transactions with merchant contribution of 10")
    public void successfulDealsOnlyAddnPayCCTransactionViaCOTP() throws Exception {
        String merchantId = Constants.MerchantType.PG2_Deals_COTP.getId();
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, Double.valueOf("80"));
        String txnAmount = "100";

        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");

        // Subtracting merchant deals contribution
        txnAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);
        InitiateTransaction initiateTransaction = new InitiateTransaction().dealsbuildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantId,txnToken,initiateTransaction.getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setPaymentFlow("ADDANDPAY")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp").build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String createOrder = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(createOrder).contains("merchantUniqueReference");
        Assertions.assertThat(createOrder).contains("merchantRequestId");
        Assertions.assertThat(createOrder).contains("posId");

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantId, initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("PPI");
        Assertions.assertThat(paymentStatusResponse.getString("body.gatewayName")).isEqualTo("WALLET");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f",Double.parseDouble(txnAmount)));

        String affordabilityPlatform = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM");
        String assertion1 = "\"TYPE\" : \"RESPONSE\", \"RESPONSE\" : {\"resultInfo\":{\"code\":\"000\",\"status\":\"S\",\"message\":\"Success\"";
        String assertion2 = "\"orderAmount\":{\"value\":9000,\"currency\":\"INR\"},\"billAmount\":{\"value\":10000,\"currency\":\"INR\"}";
        Assertions.assertThat(affordabilityPlatform).contains(assertion1);
        Assertions.assertThat(affordabilityPlatform).contains(assertion2);


        String orderModify = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_ORDER_MODIFY");
        String assertion3 = "\"orderAmount\":{\"currency\":\"INR\",\"value\":\"9000\"}";
        String assertion4 = "\"extendInfo\":\"{\"billAmount\":\"10000\"}";
        String assertion5 = "\"orderType\":\"DEAL_DISCOUNT\",\"merchantTransType\":\"DEAL_DISCOUNT\"";
        Assertions.assertThat(orderModify).contains(assertion3);
        Assertions.assertThat(orderModify).contains(assertion4);
        Assertions.assertThat(orderModify).contains(assertion5);

    }

    @Feature("PG2-8220")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Deals only COTP NB transactions with merchant contribution of 10")
    public void successfulDealsOnlyNBTransactionViaCOTP() throws Exception {
        String merchantId = Constants.MerchantType.PG2_Deals_COTP.getId();
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "60";
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");

        // Subtracting merchant deals contribution
        txnAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);
        InitiateTransaction initiateTransaction = new InitiateTransaction().dealsbuildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantId,txnToken,initiateTransaction.getOrderId())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String createOrder = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(createOrder).contains("merchantUniqueReference");
        Assertions.assertThat(createOrder).contains("merchantRequestId");
        Assertions.assertThat(createOrder).contains("posId");

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantId, initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("NB");
        Assertions.assertThat(paymentStatusResponse.getString("body.gatewayName")).isEqualTo("ICICI");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f",Double.parseDouble(txnAmount)));

        String affordabilityPlatform = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM");
        String assertion1 = "\"TYPE\" : \"RESPONSE\", \"RESPONSE\" : {\"resultInfo\":{\"code\":\"000\",\"status\":\"S\",\"message\":\"Success\"";
        String assertion2 = "\"orderAmount\":{\"value\":5000,\"currency\":\"INR\"},\"billAmount\":{\"value\":6000,\"currency\":\"INR\"}";
        Assertions.assertThat(affordabilityPlatform).contains(assertion1);
        Assertions.assertThat(affordabilityPlatform).contains(assertion2);

        String orderModify = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_ORDER_MODIFY");
        String assertion3 = "\"orderAmount\":{\"currency\":\"INR\",\"value\":\"5000\"}";
        String assertion4 = "\"extendInfo\":\"{\"billAmount\":\"6000\"}";
        String assertion5 = "\"orderType\":\"DEAL_DISCOUNT\",\"merchantTransType\":\"DEAL_DISCOUNT\"";
        Assertions.assertThat(orderModify).contains(assertion3);
        Assertions.assertThat(orderModify).contains(assertion4);
        Assertions.assertThat(orderModify).contains(assertion5);

    }


    @Feature("PG2-8220")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Deals + bank offer COTP CC transactions with merchant contribution of 10 and bank discount of 10")
    public void successfulDealsBankOfferCCTransactionViaCOTP() throws Exception {
        String merchantId = Constants.MerchantType.PG2_Deals_COTP.getId();
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "100";

        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.paymentOffers[0].promocode")).isNotNull();
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");

        // Subtracting merchant deals contribution
        txnAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);

        ApplyPromo applyPromo = new ApplyPromo().buildWithParameters(merchantId,user.ssoToken(),txnAmount);
        JsonPath applyPromoResponse = applyPromo.execute().jsonPath();
        Assertions.assertThat(applyPromoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(applyPromoResponse.getString("body.paymentOffer.verificationCode")).isNotNull();
        String verificationCode = applyPromoResponse.getString("body.paymentOffer.verificationCode");
        String promoCode = applyPromoResponse.getString("body.paymentOffer.promoCode");
        Assertions.assertThat(applyPromoResponse.getString("body.paymentOffer.savings[0].redemptionType")).isEqualTo("discount");

        // Subtracting bank offer contribution
        String payableAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);
        InitiateTransaction initiateTransaction = new InitiateTransaction()
                .dealsBankOfferbuildRequest(merchantId,txnAmount,payableAmount,user.ssoToken(),promoCode,verificationCode);
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantId,txnToken,initiateTransaction.getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp").build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String createOrder = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(createOrder).contains("merchantUniqueReference");
        Assertions.assertThat(createOrder).contains("merchantRequestId");
        Assertions.assertThat(createOrder).contains("posId");

        String orderAmount = String.format("%.2f",Double.parseDouble(payableAmount));
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantId, initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("CC");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(orderAmount);

        Thread.sleep(5);
        String affordabilityPlatform = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM");
        String assertion1 = "\"TYPE\" : \"RESPONSE\", \"RESPONSE\" : {\"resultInfo\":{\"code\":\"000\",\"status\":\"S\",\"message\":\"Success\"";
        String assertion2 = "\"orderAmount\":{\"value\":8000,\"currency\":\"INR\"},\"billAmount\":{\"value\":10000,\"currency\":\"INR\"}";
        Assertions.assertThat(affordabilityPlatform).contains(assertion1);
        Assertions.assertThat(affordabilityPlatform).contains(assertion2);

        String orderModify = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"ACQUIRING_ORDER_MODIFY");
        String assertion3 = "\"orderAmount\":{\"currency\":\"INR\",\"value\":\"8000\"}";
        String assertion4 = "\"extendInfo\":\"{\"billAmount\":\"10000\"}";
        String assertion5 = "\"orderType\":\"DEAL_DISCOUNT\",\"merchantTransType\":\"DEAL_DISCOUNT\"";
        Assertions.assertThat(orderModify).contains(assertion3);
        Assertions.assertThat(orderModify).contains(assertion4);
        Assertions.assertThat(orderModify).contains(assertion5);
    }

    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Hybrid Deals only COP CC transactions with merchant contribution of 10")
    public void successfulDealsOnlyHybridCCTransactionViaCOP() throws Exception {
        String merchantId = Constants.MerchantType.DEALS_PURE_HYBRID.getId();
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user, Double.valueOf("80"));
        String walletBalance = "80";
        String txnAmount = "100";

        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertThat(fpoResponse.getString("body.merchantPayOption")).containsOnlyOnce("hybridMode:PRIMARY");

        // Subtracting merchant deals contribution
        txnAmount = String.valueOf(Integer.parseInt(txnAmount) - 10);
        InitiateTransaction initiateTransaction = new InitiateTransaction().dealsbuildRequest(merchantId,txnAmount,user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("BALANCE");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(walletBalance);
        hybridPayModeDetail2.setPaymentMode("CREDIT_CARD");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0,hybridPayModeDetail1);
        hybridPayModeDetailList.add(1,hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantId,initiateTransaction.getOrderId(),txnToken)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantId, initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("HYBRID");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].paymentMode")).isEqualTo("CC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].txnAmount")).isEqualTo("10.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].gateway")).isEqualTo("HDFC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].paymentMode")).isEqualTo("PPI");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].gateway")).isEqualTo("WALLET");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].txnAmount")).isEqualTo("80.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f",Double.parseDouble(txnAmount)));

        String affordabilityPlatform = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM");
        String assertion1 = "\"TYPE\" : \"RESPONSE\", \"RESPONSE\" : {\"resultInfo\":{\"code\":\"000\",\"status\":\"S\",\"message\":\"Success\"";
        String assertion2 = "\"orderAmount\":{\"value\":9000,\"currency\":\"INR\"},\"billAmount\":{\"value\":10000,\"currency\":\"INR\"}";
        Assertions.assertThat(affordabilityPlatform).contains(assertion1);
        Assertions.assertThat(affordabilityPlatform).contains(assertion2);

    }
}
