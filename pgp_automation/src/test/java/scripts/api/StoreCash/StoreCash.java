package scripts.api.StoreCash;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.Deals.ApplyPromo;
import com.paytm.api.Deals.FetchPaymentOptions;
import com.paytm.api.Deals.GetPaymentStatus;
import com.paytm.api.Deals.InitiateTransaction;
import com.paytm.api.StoreCash.MLPInitiateTransaction;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.HybridPayModeDetail;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.apphelpers.CommonHelpers.generateOrderId;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;
import static com.paytm.apphelpers.PGPHelpers.assertRefundSuccessNotifyPresence;
import static io.restassured.RestAssured.given;

public class StoreCash extends PGPBaseTest
{


    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate CC+MLP txn")
    @Parameters({"isNativePlus"})
    public void validateMLP_CCTxn(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "100.00";
        String mlpAmmt = "10";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID =  initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("CREDIT_CARD");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantType.getId(), initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("HYBRID");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].paymentMode")).isEqualTo("CC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].txnAmount")).isEqualTo("90.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].gateway")).isEqualTo("HDFC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].paymentMode")).isEqualTo("STORE_CASH");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].gateway")).isEqualTo("STCHC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].txnAmount")).isEqualTo("10.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f", Double.parseDouble(txnAmount)));

        String grepEsn = "grep \"" + orderID + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        String grepCmd = "grep " + orderID +" "+ LocalConfig.INSTAPROXY_LOGS + "|grep Payment Request ";
        String redeemCall = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepCmd);
        Assertions.assertThat(redeemCall.contains("\"client_order_id\":\""+extSnValue+"\""));
    }

    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate DC+MLP txn")
    @Parameters({"isNativePlus"})
    public void validateMLP_DCTxn(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "100.00";
        String mlpAmmt = "10";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID =  initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("DEBIT_CARD");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|5244519765781731|333|122025")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantType.getId(), initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("HYBRID");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].paymentMode")).isEqualTo("DC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].txnAmount")).isEqualTo("90.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].gateway")).isEqualTo("HDFC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].paymentMode")).isEqualTo("STORE_CASH");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].gateway")).isEqualTo("STCHC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].txnAmount")).isEqualTo("10.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f", Double.parseDouble(txnAmount)));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate NB+MLP txn")
    @Parameters({"isNativePlus"})
    public void validateMLP_NBTxn(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "100.00";
        String mlpAmmt = "10";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        System.out.println(user.ssoToken());
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID =  initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("NET_BANKING");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantType.getId(), initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("HYBRID");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].paymentMode")).isEqualTo("NB");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].txnAmount")).isEqualTo("90.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].gateway")).isEqualTo("ICICI");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].paymentMode")).isEqualTo("STORE_CASH");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].gateway")).isEqualTo("STCHC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].txnAmount")).isEqualTo("10.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f", Double.parseDouble(txnAmount)));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate BALANCE+MLP txn")
    @Parameters({"isNativePlus"})
    public void validateMLP_WALLETTxn(@Optional("false") boolean isNativePlus) throws Exception {

        String txnAmount = "10.00";
        String mlpAmmt = "1";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);

        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID =  initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("BALANCE");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("PRIMARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("BALANCE")

                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantType.getId(), initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("HYBRID");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].paymentMode")).isEqualTo("PPI");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].txnAmount")).isEqualTo("9.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].paymentMode")).isEqualTo("STORE_CASH");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].gateway")).isEqualTo("STCHC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].txnAmount")).isEqualTo("1.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f", Double.parseDouble(txnAmount)));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate POSTPAID+MLP txn")
    @Parameters({"isNativePlus"})
    public void validateMLP_PostpaidTxn(@Optional("false") boolean isNativePlus) throws Exception
    {
        String txnAmount = "20.00";
        String mlpAmmt = "1";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);

        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID =  initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("PAYTM_DIGITAL_CREDIT");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantType.getId(), initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("HYBRID");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].paymentMode")).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].txnAmount")).isEqualTo("19.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].paymentMode")).isEqualTo("STORE_CASH");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].gateway")).isEqualTo("STCHC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].txnAmount")).isEqualTo("1.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f", Double.parseDouble(txnAmount)));

    }


    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate PPBL+MLP txn")
    @Parameters({"isNativePlus"})
    public void validateMLP_PPBLTxn(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "20.00";
        String mlpAmmt = "1";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID= initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("PPBL");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("PPBL")
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(merchantType.getId(), initiateTransaction.getOrderId());
        JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.paymentMode")).isEqualTo("HYBRID");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].paymentMode")).isEqualTo("NB");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].gateway")).isEqualTo("PPBL");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].txnAmount")).isEqualTo("19.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[0].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].paymentMode")).isEqualTo("STORE_CASH");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].gateway")).isEqualTo("STCHC");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].status")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(paymentStatusResponse.getString("body.childTransaction[1].txnAmount")).isEqualTo("1.00");
        Assertions.assertThat(paymentStatusResponse.getString("body.txnAmount")).isEqualTo(String.format("%.2f", Double.parseDouble(txnAmount)));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate partial refund failure for MLP txn")
    @Parameters({"isNativePlus"})
    public void validatePartialMLP_CCTxnRefundFailure(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "100.00";
        String mlpAmmt = "10";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        String orderId=initiateTransaction.getOrderId();
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID =  initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("CREDIT_CARD");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        String txnID=txnStatus.getResponse().getTXNID();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(Constants.MerchantType.STORE_CASH, "10", orderId,
                        txnID))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).doesNotContain("Pending");

    }

    @Owner(HIMANSHU)
    @Feature("PGP-48861")
    @Test(description = "Validate CC+MLP txn notification fix")
    @Parameters({"isNativePlus"})
    public void validateMLP_CCTxn_NotificationFix(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "100.00";
        String mlpAmmt = "10";

        Constants.MerchantType merchantType = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        FetchPaymentOptions fetchPaymentOptions = new FetchPaymentOptions().buildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath fpoResponse = fetchPaymentOptions.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(fpoResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertThat(fpoResponse.getString("body.productCode")).isEqualTo("51051000100000000006");
        softAssertions.assertThat(fpoResponse.getString("body.supportedPaymentFlows[0]")).isEqualTo("HYBRID");
        softAssertions.assertAll();

        MLPInitiateTransaction initiateTransaction= new MLPInitiateTransaction().withoutOfferBuildRequest(merchantType.getId(), txnAmount, user.ssoToken());
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssertions.assertThat(initTxnResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softAssertions.assertAll();
        String txnToken = initTxnResponse.getString("body.txnToken");
        String orderID =  initiateTransaction.getOrderId();

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("STORE_CASH");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(mlpAmmt);
        hybridPayModeDetail2.setPaymentMode("CREDIT_CARD");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0, hybridPayModeDetail1);
        hybridPayModeDetailList.add(1, hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(), orderID, txnToken)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderID);
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .AssertAll();
        String txnID=txnStatus.getResponse().getTXNID();
//        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,txnID);
        String grepEsn = "grep \"" + merchantType.getId() +"\" ";
        String response = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepEsn);
        Assertions.assertThat(response).contains("Payment Received of Rs.100");

    }


}