package scripts.api.CC;

import com.paytm.LocalConfig;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
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

import java.time.Instant;


public class CVV_Less_transaction extends PGPBaseTest{
    public static final String PAYMENT_TYPE = "ECOM";
    public static String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-46021")
    @Test(description = "Verify txn using saved HDFC VISA card for CVV LESS ")
    public void Saved_HDFC_VISA_Success_txn() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.CVV_LESS_MID;
        RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");

        String orderID=CommonHelpers.generateOrderId();
        String paymentMode = "CREDIT_CARD";
        custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.AlternateID_VISA_CARD);
        String tin= SavedCardHelpers.getTin();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("100")
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

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"|||")
                .setPaymentMode(paymentMode)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
    }

}
