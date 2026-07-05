package scripts.api.theia;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.Head;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.sql.SQLOutput;
import java.time.Instant;

public class schemeCardVariantInCOP extends PGPBaseTest {
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(SUPER_PREMIUM) in COP for new card txn")
    public void SUPER_PREMIUM_NEWCARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SUPER_PREMIUM_COP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
       // custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.PROMO_CC_CARD_HDFC+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
       NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"SUPER_PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Visa Infinite")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(PREMIUM) in COP for new card txn")
    public void PREMIUM_NEWCARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PREMIUM_COP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.COBRANDED_CC+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
       NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Visa Platinum")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(STANDARD) in COP for new card txn")
    public void STANDARD_NEWCARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.STANDARD_COP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.CORPORATE_INDIAN_CC+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
       NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"STANDARD")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Gold Mastercard")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(OTHERS) in COP for new card txn")
    public void OTHERS_NEWCARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.OTHERS_COP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.VISA_CC_BILL_PAYMENT+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        
        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"OTHERS")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Visa Installment Credential")).isEqualTo(true);
    }

    //saved cop txn
  @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(SUPER_PREMIUM) in COP for saved card txn")
    public void SUPER_PREMIUM_SAVED_CARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SUPER_PREMIUM_COP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
        //SavedCardHelpers.deleteSavedCard(custId);
      SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.PROMO_CC_CARD_HDFC);
      String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
      FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
      FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
              initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
      JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
      Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
      Assertions.assertThat(fetchPaymentOptionsJson.getString(
              "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='VISA'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);

      ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
       NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"SUPER_PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Visa Infinite")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(PREMIUM) in COP for saved card txn")
    public void PREMIUM_SAVED_CARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PREMIUM_COP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.COBRANDED_CC);
        String tin= SavedCardHelpers.getTin();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString(
                "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='VISA'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Visa Platinum")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(STANDARD) in COP for saved card txn")
    public void STANDARD_SAVED_CARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.STANDARD_COP;
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();

        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.CORPORATE_INDIAN_CC);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString(
                "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='MASTER'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"STANDARD")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Gold Mastercard")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(OTHERS) in COP for saved card txn")
    public void OTHERS_SAVED_CARD_IN_COP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.OTHERS_COP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_CC_BILL_PAYMENT);
        String tin= SavedCardHelpers.getTin();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString(
                "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='VISA'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(COPRequest.contains("schemeCardVariantCategory\":\"OTHERS")).isEqualTo(true);
        Assertions.assertThat(COPRequest.contains("schemeCardVariant\":\"Visa Installment Credential")).isEqualTo(true);
    }


    // New card COTP flow
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(SUPER_PREMIUM) in COTP for new card txn")
    public void SUPER_PREMIUM_NEWCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SUPER_PREMIUM_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,"", orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.PROMO_CC_CARD_HDFC+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"SUPER_PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Visa Infinite")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(PREMIUM) in COTP for new card txn")
    public void PREMIUM_NEWCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PREMIUM_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.COBRANDED_CC+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Visa Platinum")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(STANDARD) in COTP for new card txn")
    public void STANDARD_NEWCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.STANDARD_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.CORPORATE_INDIAN_CC+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"STANDARD")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Gold Mastercard")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(OTHERS) in COTP for new card txn")
    public void OTHERS_NEWCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.OTHERS_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo("|"+PaymentDTO.VISA_CC_BILL_PAYMENT+"|123|122029")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"OTHERS")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Visa Installment Credential")).isEqualTo(true);
    }

    // Saved card COTP flow
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(SUPER_PREMIUM) in COTP for saved card txn")
    public void SUPER_PREMIUM_SAVEDCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SUPER_PREMIUM_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.PROMO_CC_CARD_HDFC);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,"", orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String txnToken = response.jsonPath().get("body.txnToken").toString();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString(
                "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='VISA'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"SUPER_PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Visa Infinite")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(PREMIUM) in COTP for saved card txn")
    public void PREMIUM_SAVEDCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PREMIUM_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");

        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.COBRANDED_CC);
        String tin= SavedCardHelpers.getTin();

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString(
                "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='VISA'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"PREMIUM")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Visa Platinum")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(STANDARD) in COTP for saved card txn")
    public void STANDARD_SAVEDCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.STANDARD_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.CORPORATE_INDIAN_CC);
        String tin= SavedCardHelpers.getTin();

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString(
                "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='MASTER'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"STANDARD")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Gold Mastercard")).isEqualTo(true);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PG2-27564")
    @Test(description = "Verify sending schemeCardVariant and schemeCardVariantCategory(OTHERS) in COTP for saved card txn")
    public void OTHERS_SAVEDCARD_IN_COTP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.OTHERS_COTP;
        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_CC_BILL_PAYMENT);
        String tin= SavedCardHelpers.getTin();

        String orderID= CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String txnToken = response.jsonPath().get("body.txnToken").toString();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString(
                "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='VISA'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||123|")
                .setPaymentMode(paymentMode)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();

        String COTPRequest = LogsValidationHelper.verifyLogsOnPod(ServerConfigProvider.SERVICE.THEIA_FACADE.toString(), orderID,"ACQUIRING_PAY_ORDER","REQUEST");
        Assertions.assertThat(COTPRequest.contains("schemeCardVariantCategory\":\"OTHERS")).isEqualTo(true);
        Assertions.assertThat(COTPRequest.contains("schemeCardVariant\":\"Visa Installment Credential")).isEqualTo(true);
    }
}
